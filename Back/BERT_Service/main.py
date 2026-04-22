from contextlib import asynccontextmanager
from fastapi import FastAPI, HTTPException

# 数据库
from db import connect_db, close_db

# 业务
from bert import *


# ======================
# 生命周期管理
# ======================
@asynccontextmanager
async def lifespan(app: FastAPI):
    connect_db()
    print("✅ 服务启动 - 数据库已连接")
    yield
    close_db()
    print("✅ 服务关闭 - 数据库已断开")


app = FastAPI(
    title="BERT向量服务（bert向量生成和查找）",
    lifespan=lifespan
)


# ======================
# 接口 1：BERT_store
# ======================
@app.post("/api/bert_store")
def api_bert_store(doc_id: int):
    try:
        result = bert_store_service(doc_id)
        return {
            "code": 200,
            "doc_id": doc_id,
            "data": result,
            "msg": "向量存储成功"
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"向量存储失败：{str(e)}")

@app.post("/api/bert_query")
def api_bert_query(query: str):
    try:
        result = bert_query_service(query)
        print(result)
        return {
            "code": 200,
            "data": result,
            "msg": "文件查找成功"
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"文件查找失败：{str(e)}")

@app.post("/api/update_tag_vector")
def api_bert_query(tag_id: int):
    try:
        result = bert_embedding_tag(tag_id)
        print(result)
        return {
            "code": 200,
            "data": result,
            "msg": "set tag向量成功"
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"set tag向量失败：{str(e)}")


@app.post("/api/set_doc_tag")
def api_bert_query(doc_id: int):
    try:
        result = bert_set_tag(doc_id)
        print(result)
        return {
            "code": 200,
            "data": result,
            "msg": "set tag成功"
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"set tag失败：{str(e)}")
