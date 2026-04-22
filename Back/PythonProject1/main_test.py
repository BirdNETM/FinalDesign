
from ai_summary import ai_summary_service
from fastapi import FastAPI
import uvicorn

# 你的数据库工具
from db import connect_db, close_db

# 你的 OCR 函数（直接导入或写在这里）
from ocr import ocr_file

from ai_summary import ai_summary_service
# ======================
# 启动服务（一个端口搞定）
# ======================
if __name__ == "__main__":
    connect_db()
    ai_summary_service(6)