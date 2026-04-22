import os

# 🔴 必须在 import paddleocr 之前
os.environ["FLAGS_use_mkldnn"] = "0"
os.environ["FLAGS_enable_pir_api"] = "0"
os.environ["FLAGS_new_executor"] = "0"


from fastapi import FastAPI, Query, HTTPException, Body

from pathlib import Path
import numpy as np
from paddleocr import PaddleOCR
from pdf2image import convert_from_path
from PIL import Image
from pptx import Presentation
from io import BytesIO

from db import get_cursor, commit

ocr = PaddleOCR(use_angle_cls=True, lang='ch', enable_mkldnn=False)  # 中文

def ocr_image(img: Image.Image):
    """将 PIL.Image 转为 numpy array 后用 PaddleOCR 识别文字"""
    img_rgb = img.convert("RGB")  # 确保是 RGB
    img_np = np.array(img_rgb)
    try:
        result = ocr.ocr(img_np)
    except Exception as e:
        return [f"OCR 识别失败: {e}"]

    texts = []
    for line in result:
        for box in line:
            texts.append(box[1][0])
    return texts


def ocr_file(doc_id):
    cursor = get_cursor()
    sql = "select doc_url from docs where doc_id = %s"
    cursor.execute(sql, (
        doc_id,
    ))
    result = cursor.fetchone()
    path = Path(result[0])
    print(path)
    commit()

    if not path.exists():
        raise HTTPException(status_code=400, detail="File not found")

    suffix = path.suffix.lower().lstrip(".")
    texts = []

    if suffix in ["jpg", "jpeg", "png"]:
        try:
            img = Image.open(path)
            texts += ocr_image(img)
        except Exception as e:
            raise HTTPException(status_code=500, detail=f"图片打开或识别失败: {e}")

    elif suffix == "pdf":
        try:
            pages = convert_from_path(
                str(path),
                dpi=300,
                poppler_path=r"D:\complier\popular\poppler-25.12.0\Library\bin"
            )
        except Exception as e:
            raise HTTPException(status_code=500, detail=f"PDF 转图片失败: {e}")

        for idx, page in enumerate(pages, start=1):
            page_texts = ocr_image(page)
            # texts.append(f"=== 第 {idx} 页 ===")
            texts.extend(page_texts)

    elif suffix in ["ppt", "pptx"]:
        try:
            prs = Presentation(path)
        except Exception as e:
            raise HTTPException(status_code=500, detail=f"PPT 打开失败: {e}")

        for idx, slide in enumerate(prs.slides, start=1):
            # texts.append(f"=== 第 {idx} 张幻灯片 ===")
            for shape in slide.shapes:
                if shape.has_text_frame:
                    texts.append(shape.text)
                elif getattr(shape, "image", None) is not None:
                    try:
                        img_bytes = shape.image.blob
                        img = Image.open(BytesIO(img_bytes))
                        texts.extend(ocr_image(img))
                    except Exception:
                        pass
    else:
        raise HTTPException(status_code=400, detail=f"不支持的文件格式: {suffix}")
    print("识别结果为：\n" + "\n".join(texts))
    sql = "INSERT INTO vector(doc_id, text) VALUES(%s,%s)"
    cursor.execute(sql, (
        doc_id,
        "\n".join(texts)
    ))
    commit()
    return {"text": "\n".join(texts)}