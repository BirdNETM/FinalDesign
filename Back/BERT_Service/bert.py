import torch
from sentence_transformers import SentenceTransformer
import numpy as np
import json
from db import get_cursor, commit

model = SentenceTransformer('./all-MiniLM-L6-v2')

# ======================
# 1. 分块（用于提升向量质量）
# ======================
def split_text(text, chunk_size=200, overlap=50):
    chunks = []
    start = 0

    while start < len(text):
        chunk = text[start:start + chunk_size]
        chunks.append(chunk)
        start += chunk_size - overlap

    return chunks


# ======================
# 2. 融合向量（核心）
# ======================
def merge_embeddings(embeddings):
    return np.mean(embeddings, axis=0)


# ======================
# 3. 存储文档向量
# ======================
def save_doc_vector(doc_id, vector):
    cursor = get_cursor()

    # 【正确】UPDATE 语法
    sql = "UPDATE vector SET vector = %s WHERE doc_id = %s"

    # 执行
    cursor.execute(sql, (
        json.dumps(vector.tolist()),
        doc_id
    ))

    commit()
    print(f"✅ 文档 {doc_id} 向量更新成功")


# ======================
# 4. 余弦相似度
# ======================
def cosine_similarity(a, b):
    a_norm = np.linalg.norm(a)
    b_norm = np.linalg.norm(b)
    if a_norm == 0 or b_norm == 0:
        return 0.0
    return float(np.dot(a, b) / (a_norm * b_norm))


def parse_vector(vec_str):
    if not vec_str:
        return None

    try:
        vec = np.array(json.loads(vec_str), dtype=float)
    except (TypeError, ValueError, json.JSONDecodeError):
        return None

    if vec.size == 0 or np.linalg.norm(vec) == 0:
        return None

    return vec


# ======================
# 5. 入库（文档级）
# ======================
def bert_store_service(doc_id):
    cursor = get_cursor()
    sql = "select text from vector where doc_id = %s"
    cursor.execute(sql, (
        doc_id,
    ))
    result = cursor.fetchone()
    if not result or not result[0]:
        raise ValueError(f"doc_id={doc_id} 未找到文本内容")
    text = result[0]
    commit()
    # 分块
    chunks = split_text(text)

    # 向量化
    embeddings = model.encode(chunks)

    # 融合
    doc_vector = merge_embeddings(embeddings)

    # 存储
    save_doc_vector(doc_id, doc_vector)

def repeat_to_length(s, max_len=200):
    if len(s) >= 100:
        return s
    # 无限重复原字符串，然后截断到 max_len
    repeated = (s + " ") * (max_len // len(s) + 2)
    return repeated.strip()[:max_len]

# ======================
# 6. 查询
# ======================
def bert_query_service(query, top_k=5):
    cursor = get_cursor()

    if not query or not query.strip():
        raise ValueError("query 不能为空")

    expanded_query = repeat_to_length(query, 200)
    # 查询向量
    query_vec = model.encode([expanded_query])[0]

    # 取所有文档
    cursor.execute("SELECT doc_id, text, vector FROM vector")
    rows = cursor.fetchall()

    results = []

    for doc_id, text, vec_str in rows:
        vec = parse_vector(vec_str)
        if vec is None:
            continue

        score = cosine_similarity(query_vec, vec)

        results.append((doc_id, text, score))

    # 排序
    results.sort(key=lambda x: x[2], reverse=True)

    return results[:top_k]


def bert_embedding_tag(tag_id):
    cursor = get_cursor()
    try:
        # 1. 获取标签内容
        sql = "SELECT tag_content FROM tags WHERE tag_id = %s"
        cursor.execute(sql, (tag_id,))
        result = cursor.fetchone()

        if not result:
            print(f"Tag ID {tag_id} not found.")
            return

        tag_content = result[0]

        # 2. 生成向量
        # model.encode 默认返回 numpy 数组
        vector = model.encode(tag_content)

        # 3. 关键：将 numpy 数组转换为列表或 JSON 字符串
        # 否则直接 update 可能会报错：TypeError: Object of type ndarray is not JSON serializable
        vector_data = json.dumps(vector.tolist())

        # 4. 更新数据库
        update_sql = "UPDATE tags SET tag_vector = %s WHERE tag_id = %s"
        cursor.execute(update_sql, (vector_data, tag_id))

        # 别忘了 commit，否则更改不会生效
        commit()
    except Exception as e:
        print(f"Embedding error: {e}")
    # 注意：根据你的框架习惯，决定是否在这里 close cursor

def bert_set_tag(doc_id, top_k=2):
    cursor = get_cursor()
    try:
        # 1. 获取文档向量
        cursor.execute("SELECT vector FROM vector WHERE doc_id = %s", (doc_id,))
        result = cursor.fetchone()
        if not result:
            return []
        # 确保 doc_vector 是 numpy 数组 (假设数据库存的是 JSON 字符串)
        doc_vector = parse_vector(result[0])
        if doc_vector is None:
            raise ValueError(f"doc_id={doc_id} 的向量无效")

        # 2. 取出所有 tag 并向量化计算 (优化循环)
        cursor.execute("SELECT tag_id, tag_name, tag_vector FROM tags WHERE tag_vector IS NOT NULL")
        rows = cursor.fetchall()
        if not rows:
            return []

        tag_ids = []
        tag_names = []
        tag_vectors = []

        for tid, name, vec_str in rows:
            vec = parse_vector(vec_str)
            if vec is None:
                continue
            tag_ids.append(tid)
            tag_names.append(name)
            tag_vectors.append(vec)

        if not tag_vectors:
            return []

        # 转换为矩阵一次性计算相似度 (假设 doc_vector 是一维的)
        tag_matrix = np.array(tag_vectors)
        # 余弦相似度简易实现: (A·B) / (|A|*|B|)
        # 如果向量已归一化，直接点积即可
        scores = np.dot(tag_matrix, doc_vector) / (
                np.linalg.norm(tag_matrix, axis=1) * np.linalg.norm(doc_vector)
        )

        # 3. 组合并排序
        results = sorted(zip(tag_ids, tag_names, scores), key=lambda x: x[2], reverse=True)
        top_results = results[:top_k]

        # 4. 批量插入数据库 (使用参数化循环或 executemany)
        insert_sql = """
            INSERT INTO doc_tag (doc_id, tag_id)
            SELECT %s, %s
            WHERE NOT EXISTS (
                SELECT 1 FROM doc_tag WHERE doc_id = %s AND tag_id = %s
            )
        """
        insert_data = [(doc_id, r[0]) for r in top_results]

        if insert_data:
            cursor.executemany(
                insert_sql,
                [(current_doc_id, tag_id, current_doc_id, tag_id) for current_doc_id, tag_id in insert_data]
            )

        commit()
        return top_results

    except Exception as e:
        print(f"Error: {e}")
        raise
    # 注意：连接的 close 建议在外部或使用 with 语句管理

