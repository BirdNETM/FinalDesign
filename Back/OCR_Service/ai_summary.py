import requests
from db import get_cursor, commit

# 豆包配置（已验证可用）
API_KEY = "0e05066e-35b7-44e4-8516-5f4bad233a41"
API_URL = "https://ark.cn-beijing.volces.com/api/v3/chat/completions"
MODEL_NAME = "doubao-seed-1-6-250615"


def ai_summary_service(doc_id: int):
    # 1. 从数据库取原文
    text = ""
    cursor = get_cursor()
    try:
        sql = "SELECT text FROM vector WHERE doc_id = %s"
        cursor.execute(sql, (doc_id,))
        result = cursor.fetchone()
        if result and result[0]:
            text = result[0]
        else:
            return "未找到对应文档内容"
    finally:
        print("get")

    # 2. 调用豆包生成总结
    summary = call_llm_summary(text)

    # 3. 把总结存回数据库
    cursor = get_cursor()
    try:
        update_sql = "UPDATE vector SET summary = %s WHERE doc_id = %s"
        cursor.execute(update_sql, (summary, doc_id))
        commit()
    finally:
        print("back")

    return summary


def call_llm_summary(text: str) -> str:
    headers = {
        "Authorization": f"Bearer {API_KEY}",
        "Content-Type": "application/json"
    }

    prompt = f"""请对下面的文本进行简洁、准确、通顺的总结，保留核心信息，最多不超过150字：
文本内容：
{text}

总结："""

    data = {
        "model": MODEL_NAME,
        "messages": [{"role": "user", "content": prompt}],
        "temperature": 0.2
    }

    try:
        resp = requests.post(API_URL, json=data, headers=headers, timeout=30)
        resp.raise_for_status()
        result = resp.json()
        return result["choices"][0]["message"]["content"].strip()
    except Exception as e:
        return f"AI总结失败：{str(e)}"


if __name__ == '__main__':
    # 测试传入 doc_id=1，会自动读库→总结→写回库
    print(ai_summary_service(1))