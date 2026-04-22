from contextlib import asynccontextmanager
from fastapi import FastAPI

# 数据库
from db import connect_db, close_db

# 业务
from ocr import ocr_file
from ai_summary import ai_summary_service


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
    title="统一AI服务（OCR + 摘要）",
    lifespan=lifespan
)


# ======================
# 接口 1：OCR
# ======================
@app.post("/api/ocr")
def api_ocr(doc_id: int):
    try:
        result = ocr_file(doc_id)
        return {
            "code": 200,
            "doc_id": doc_id,
            "data": result,
            "msg": "OCR成功"
        }
    except Exception as e:
        return {"code": 500, "msg": f"OCR失败：{str(e)}"}


# ======================
# 接口 2：AI摘要
# ======================
@app.post("/api/ai_summary")
def api_ai_summary(doc_id: int):
    try:
        result = ai_summary_service(doc_id)
        return {
            "code": 200,
            "data": result,
            "msg": "AI摘要成功"
        }
    except Exception as e:
        return {"code": 500, "msg": f"AI摘要失败：{str(e)}"}