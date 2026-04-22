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
    return np.dot(a, b) / (np.linalg.norm(a) * np.linalg.norm(b))


# ======================
# 5. 入库（文档级）
# ======================
def bert_store_service(doc_id):
    cursor = get_cursor()
    sql = "select text from vector where doc_id = %s"
    cursor.execute(sql, (
        doc_id
    ))
    result = cursor.fetchone()
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

    expanded_query = repeat_to_length(query, 200)
    # 查询向量
    query_vec = model.encode([expanded_query])[0]

    # 取所有文档
    cursor.execute("SELECT doc_id, text, vector FROM vector")
    rows = cursor.fetchall()

    results = []

    for doc_id, text, vec_str in rows:
        vec = np.array(json.loads(vec_str))

        score = cosine_similarity(query_vec, vec)

        results.append((doc_id, text, score))

    # 排序
    results.sort(key=lambda x: x[2], reverse=True)

    return results[:top_k]