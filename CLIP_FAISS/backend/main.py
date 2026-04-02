"""
FastAPI主服务
提供图片上传、以图搜图、产品管理等接口
"""
from fastapi import FastAPI, UploadFile, File, Depends, HTTPException, Query
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse, FileResponse
from PIL import Image
import os
import numpy as np
from typing import List, Optional
import logging
from datetime import datetime

from clip_model import init_clip_model, image_to_vector, text_to_vector
from faiss_service import init_faiss, add_vector, search_vector, get_index_size
from mysql_db import (
    get_db, init_database, test_connection,
    create_product, get_product_by_id, get_product_by_faiss_index,
    update_product, delete_product, get_all_products, Product
)
from sqlalchemy.orm import Session

# 配置日志
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# 创建FastAPI应用
app = FastAPI(
    title="企业产品以图搜系统",
    description="基于CLIP+FAISS的以图搜产品服务，支持百万级产品毫秒级检索",
    version="1.0.0"
)

# 跨域配置
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # 生产环境应设置具体域名
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 上传文件夹配置
UPLOAD_DIR = "uploads"
os.makedirs(UPLOAD_DIR, exist_ok=True)

# 应用启动事件
@app.on_event("startup")
async def startup_event():
    """应用启动时初始化模型和数据库"""
    try:
        logger.info("正在初始化系统...")
        
        # 初始化CLIP模型
        init_clip_model()
        
        # 初始化FAISS索引
        init_faiss()
        
        # 测试数据库连接
        if not test_connection():
            logger.warning("数据库连接失败，请检查配置")
        
        logger.info("系统初始化完成")
        
    except Exception as e:
        logger.error(f"系统初始化失败: {str(e)}")
        raise

# ==================== 核心业务接口 ====================

@app.post("/api/search-by-image", summary="以图搜图")
async def search_by_image(
    file: UploadFile = File(..., description="产品图片"),
    top_k: int = Query(5, description="返回结果数量"),
    db: Session = Depends(get_db)
):
    """
    以图搜图核心接口
    
    - **file**: 上传的产品图片
    - **top_k**: 返回最相似的K个产品，默认5个
    
    返回按相似度排序的产品列表
    """
    try:
        # 保存上传图片
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        file_extension = os.path.splitext(file.filename)[1]
        image_filename = f"search_{timestamp}{file_extension}"
        image_path = os.path.join(UPLOAD_DIR, image_filename)
        
        with open(image_path, "wb") as f:
            f.write(await file.read())
        
        # 图片转向量
        image = Image.open(image_path).convert("RGB")
        vector = image_to_vector(image)
        
        # FAISS检索
        scores, indices = search_vector(vector, top_k)
        
        # 根据FAISS索引查询MySQL产品信息
        products = []
        for idx, score in zip(indices, scores):
            if idx >= 0:  # FAISS返回-1表示未找到
                product = get_product_by_faiss_index(db, int(idx))
                if product:
                    products.append({
                        "id": product.id,
                        "name": product.product_name,
                        "model": product.product_model,
                        "price": float(product.price) if product.price else 0,
                        "stock": product.stock,
                        "image": product.product_image,
                        "similarity": round(float(score) * 100, 2),  # 相似度百分比
                        "create_time": product.create_time.strftime("%Y-%m-%d %H:%M:%S") if product.create_time else None
                    })
        
        logger.info(f"搜索完成，找到 {len(products)} 个匹配产品")
        
        return {
            "code": 200,
            "message": "搜索成功",
            "data": products
        }
        
    except Exception as e:
        logger.error(f"以图搜图失败: {str(e)}")
        return JSONResponse(
            status_code=500,
            content={"code": 500, "message": f"搜索失败: {str(e)}"}
        )

@app.post("/api/search-by-text", summary="以文搜图")
async def search_by_text(
    text: str = Query(..., description="文本描述"),
    top_k: int = Query(5, description="返回结果数量"),
    db: Session = Depends(get_db)
):
    """
    以文搜图接口（CLIP跨模态检索）
    
    - **text**: 产品描述文本
    - **top_k**: 返回最相似的K个产品
    
    返回按相似度排序的产品列表
    """
    try:
        # 文本转向量
        vector = text_to_vector(text)
        
        # FAISS检索
        scores, indices = search_vector(vector, top_k)
        
        # 查询产品信息
        products = []
        for idx, score in zip(indices, scores):
            if idx >= 0:
                product = get_product_by_faiss_index(db, int(idx))
                if product:
                    products.append({
                        "id": product.id,
                        "name": product.product_name,
                        "model": product.product_model,
                        "price": float(product.price) if product.price else 0,
                        "stock": product.stock,
                        "image": product.product_image,
                        "similarity": round(float(score) * 100, 2)
                    })
        
        return {
            "code": 200,
            "message": "搜索成功",
            "data": products
        }
        
    except Exception as e:
        logger.error(f"以文搜图失败: {str(e)}")
        return JSONResponse(
            status_code=500,
            content={"code": 500, "message": f"搜索失败: {str(e)}"}
        )

@app.post("/api/add-product", summary="添加产品")
async def add_product(
    name: str = Query(..., description="产品名称"),
    model: str = Query(None, description="产品型号"),
    price: float = Query(None, description="产品价格"),
    stock: int = Query(0, description="库存数量"),
    file: UploadFile = File(..., description="产品图片"),
    db: Session = Depends(get_db)
):
    """
    添加产品接口（自动生成向量并存储）
    
    - **name**: 产品名称（必填）
    - **model**: 产品型号
    - **price**: 产品价格
    - **stock**: 库存数量
    - **file**: 产品图片
    
    返回添加结果
    """
    try:
        # 保存产品图片
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        file_extension = os.path.splitext(file.filename)[1]
        image_filename = f"product_{timestamp}{file_extension}"
        image_path = os.path.join(UPLOAD_DIR, image_filename)
        
        with open(image_path, "wb") as f:
            f.write(await file.read())
        
        # 图片转向量
        image = Image.open(image_path).convert("RGB")
        vector = image_to_vector(image)
        
        # 存入FAISS
        faiss_idx = add_vector(vector)
        
        # 存入MySQL
        product_data = {
            "product_name": name,
            "product_model": model,
            "price": price,
            "stock": stock,
            "product_image": image_path,
            "faiss_index": faiss_idx
        }
        product = create_product(db, product_data)
        
        logger.info(f"产品添加成功: {name}, FAISS索引: {faiss_idx}")
        
        return {
            "code": 200,
            "message": "产品添加成功",
            "data": {
                "product_id": product.id,
                "faiss_index": faiss_idx
            }
        }
        
    except Exception as e:
        logger.error(f"添加产品失败: {str(e)}")
        return JSONResponse(
            status_code=500,
            content={"code": 500, "message": f"添加失败: {str(e)}"}
        )

# ==================== 产品管理接口 ====================

@app.get("/api/products", summary="获取产品列表")
async def list_products(
    skip: int = Query(0, description="跳过记录数"),
    limit: int = Query(100, description="返回记录数"),
    db: Session = Depends(get_db)
):
    """获取产品列表（分页）"""
    try:
        products = get_all_products(db, skip, limit)
        
        product_list = []
        for p in products:
            product_list.append({
                "id": p.id,
                "name": p.product_name,
                "model": p.product_model,
                "price": float(p.price) if p.price else 0,
                "stock": p.stock,
                "image": p.product_image,
                "faiss_index": p.faiss_index,
                "create_time": p.create_time.strftime("%Y-%m-%d %H:%M:%S") if p.create_time else None
            })
        
        return {
            "code": 200,
            "message": "查询成功",
            "data": product_list
        }
        
    except Exception as e:
        logger.error(f"获取产品列表失败: {str(e)}")
        return JSONResponse(
            status_code=500,
            content={"code": 500, "message": f"查询失败: {str(e)}"}
        )

@app.get("/api/product/{product_id}", summary="获取产品详情")
async def get_product(product_id: int, db: Session = Depends(get_db)):
    """根据ID获取产品详情"""
    try:
        product = get_product_by_id(db, product_id)
        
        if not product:
            return JSONResponse(
                status_code=404,
                content={"code": 404, "message": "产品不存在"}
            )
        
        return {
            "code": 200,
            "message": "查询成功",
            "data": {
                "id": product.id,
                "name": product.product_name,
                "model": product.product_model,
                "price": float(product.price) if product.price else 0,
                "stock": product.stock,
                "image": product.product_image,
                "faiss_index": product.faiss_index,
                "create_time": product.create_time.strftime("%Y-%m-%d %H:%M:%S") if product.create_time else None
            }
        }
        
    except Exception as e:
        logger.error(f"获取产品详情失败: {str(e)}")
        return JSONResponse(
            status_code=500,
            content={"code": 500, "message": f"查询失败: {str(e)}"}
        )

@app.put("/api/product/{product_id}", summary="更新产品信息")
async def update_product_info(
    product_id: int,
    name: Optional[str] = Query(None, description="产品名称"),
    model: Optional[str] = Query(None, description="产品型号"),
    price: Optional[float] = Query(None, description="产品价格"),
    stock: Optional[int] = Query(None, description="库存数量"),
    db: Session = Depends(get_db)
):
    """更新产品信息"""
    try:
        update_data = {}
        if name is not None:
            update_data["product_name"] = name
        if model is not None:
            update_data["product_model"] = model
        if price is not None:
            update_data["price"] = price
        if stock is not None:
            update_data["stock"] = stock
        
        product = update_product(db, product_id, update_data)
        
        if not product:
            return JSONResponse(
                status_code=404,
                content={"code": 404, "message": "产品不存在"}
            )
        
        return {
            "code": 200,
            "message": "更新成功"
        }
        
    except Exception as e:
        logger.error(f"更新产品失败: {str(e)}")
        return JSONResponse(
            status_code=500,
            content={"code": 500, "message": f"更新失败: {str(e)}"}
        )

@app.delete("/api/product/{product_id}", summary="删除产品")
async def delete_product_by_id(product_id: int, db: Session = Depends(get_db)):
    """删除产品"""
    try:
        success = delete_product(db, product_id)
        
        if not success:
            return JSONResponse(
                status_code=404,
                content={"code": 404, "message": "产品不存在"}
            )
        
        return {
            "code": 200,
            "message": "删除成功"
        }
        
    except Exception as e:
        logger.error(f"删除产品失败: {str(e)}")
        return JSONResponse(
            status_code=500,
            content={"code": 500, "message": f"删除失败: {str(e)}"}
        )

# ==================== 系统信息接口 ====================

@app.get("/api/system/info", summary="获取系统信息")
async def get_system_info():
    """获取系统运行状态信息"""
    try:
        return {
            "code": 200,
            "data": {
                "vector_count": get_index_size(),
                "upload_dir": UPLOAD_DIR,
                "vector_dimension": 512,
                "model": "CLIP-ViT-Base-Patch32"
            }
        }
    except Exception as e:
        return {
            "code": 500,
            "message": f"获取系统信息失败: {str(e)}"
        }

@app.get("/", summary="API首页")
async def root():
    """API首页"""
    return {
        "message": "企业产品以图搜系统 API",
        "version": "1.0.0",
        "docs": "/docs"
    }

# 启动服务
if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
