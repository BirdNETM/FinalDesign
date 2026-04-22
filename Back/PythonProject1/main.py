from contextlib import asynccontextmanager
from ai_summary import ai_summary_service
from fastapi import FastAPI
import uvicorn

# 数据库工具
from db import connect_db, close_db

# OCR + BERT
from ocr import ocr_file
from bert import bert_store_service, bert_query_service

@asynccontextmanager
async def lifespan(app: FastAPI):
    connect_db()
    print("✅ 服务启动 - 数据库已连接")
    yield
    close_db()
    print("✅ 服务关闭 - 数据库已断开")

app = FastAPI(
    title="统一AI服务（OCR + BERT + 摘要）",
    lifespan=lifespan
)

# ======================
# 接口 1：OCR
# ======================
@app.post("/api/ocr")
def api_ocr(doc_id: int):
    try:
        result = ocr_file(doc_id)
        return {"code": 200, "doc_id": doc_id, "data": result, "msg": "OCR成功"}
    except Exception as e:
        return {"code": 500, "msg": f"OCR失败：{str(e)}"}

# ======================
# 接口 2：BERT 向量化存储
# ======================
@app.post("/api/bert/store")
def api_bert_store(doc_id: int):
    try:
        result = bert_store_service(doc_id)
        return {"code": 200, "doc_id": doc_id, "data": result, "msg": "BERT_store成功"}
    except Exception as e:
        return {"code": 500, "msg": f"BERT失败：{str(e)}"}

# ======================
# 接口 3：BERT 语义检索
# ======================
@app.post("/api/bert/query")
def api_bert_query(query: str):
    try:
        result = bert_query_service(query)
        return {"code": 200, "data": result, "msg": "BERT_query成功"}
    except Exception as e:
        return {"code": 500, "msg": f"BERT失败：{str(e)}"}

# ======================
# 接口 4：AI 摘要
# ======================
@app.post("/api/ai_summary")
def api_ai_summary(doc_id: int):
    try:
        result = ai_summary_service(doc_id)
        return {"code": 200, "data": result, "msg": "ai_summary成功"}
    except Exception as e:
        return {"code": 500, "msg": f"ai_summary失败：{str(e)}"}

# ======================
# 启动服务
# ======================
if __name__ == "__main__":
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)