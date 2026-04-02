"""
FastAPI主服务（优化版）
提供图片上传、以图搜图、产品管理等接口
"""
from fastapi import FastAPI, UploadFile, File, Depends, HTTPException, Query
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from fastapi.exceptions import RequestValidationError
from PIL import Image
import os
import numpy as np
from typing import List, Optional
from datetime import datetime
from pathlib import Path

# 导入配置
from config import settings

# 导入核心模块
from clip_model import init_clip_model, image_to_vector, text_to_vector
from faiss_service import init_faiss, add_vector, search_vector, get_index_size, save_index
from mysql_db import (
    get_db, test_connection,
    create_product, get_product_by_id, get_product_by_faiss_index,
    update_product, delete_product, get_all_products
)

# 导入验证模型
from schemas import (
    ProductCreate, ProductUpdate, ProductResponse,
    ProductSearchResult, SearchResponse, SystemInfo
)

# 导入工具模块
from utils.file_validator import FileValidator
from utils.logger import setup_logger
from utils.transaction import TransactionManager

# 导入错误处理
from middleware.error_handler import (
    validation_exception_handler,
    sqlalchemy_exception_handler,
    generic_exception_handler
)

# 配置日志
logger = setup_logger(__name__)

# 创建FastAPI应用
app = FastAPI(
    title=settings.APP_NAME,
    description="基于CLIP+FAISS的以图搜产品服务，支持百万级产品毫秒级检索",
    version=settings.APP_VERSION,
    docs_url="/docs",
    redoc_url="/redoc"
)

# 注册异常处理器
app.add_exception_handler(RequestValidationError, validation_exception_handler)
app.add_exception_handler(Exception, generic_exception_handler)

# CORS配置（使用配置管理）
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.CORS_ORIGINS,
    allow_credentials=settings.CORS_ALLOW_CREDENTIALS,
    allow_methods=settings.CORS_ALLOW_METHODS,
    allow_headers=settings.CORS_ALLOW_HEADERS,
)

# 创建上传目录
Path(settings.UPLOAD_DIR).mkdir(parents=True, exist_ok=True)

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

# 应用关闭事件
@app.on_event("shutdown")
async def shutdown_event():
    """应用关闭时保存索引"""
    try:
        save_index()
        logger.info("系统关闭，索引已保存")
    except Exception as e:
        logger.error(f"索引保存失败: {str(e)}")

# ==================== 辅助函数 ====================

def serialize_product(product, score=None) -> dict:
    """序列化产品数据"""
    data = {
        "id": product.id,
        "name": product.product_name,
        "model": product.product_model,
        "price": float(product.price) if product.price else 0,
        "stock": product.stock,
        "image": product.product_image,
    }
    
    if score is not None:
        data["similarity"] = round(float(score) * 100, 2)
    
    if hasattr(product, 'create_time') and product.create_time:
        data["create_time"] = product.create_time.strftime("%Y-%m-%d %H:%M:%S")
    
    return data

# ==================== 核心业务接口 ====================

@app.post("/api/search-by-image", summary="以图搜图")
async def search_by_image(
    file: UploadFile = File(..., description="产品图片"),
    top_k: int = Query(5, description="返回结果数量", ge=1, le=100),
    db = Depends(get_db)
):
    """
    以图搜图核心接口
    
    - **file**: 上传的产品图片
    - **top_k**: 返回最相似的K个产品，默认5个
    
    返回按相似度排序的产品列表
    """
    try:
        # 1. 验证文件
        file_id, file_ext = FileValidator.validate_file(file)
        
        # 2. 验证图片内容
        image = await FileValidator.validate_image_content(file)
        
        # 3. 保存上传图片（用于记录）
        filename = FileValidator.generate_safe_filename(file_id, file_ext)
        image_path = FileValidator.get_save_path(filename, "search")
        
        # 保存图片
        image.save(image_path, quality=95)
        
        # 4. 图片转向量
        vector = image_to_vector(image)
        
        # 5. FAISS检索
        scores, indices = search_vector(vector, top_k)
        
        # 6. 根据FAISS索引查询MySQL产品信息
        products = []
        for idx, score in zip(indices, scores):
            if idx >= 0:  # FAISS返回-1表示未找到
                product = get_product_by_faiss_index(db, int(idx))
                if product:
                    products.append(serialize_product(product, score))
        
        logger.info(f"搜索完成，找到 {len(products)} 个匹配产品")
        
        return {
            "code": 200,
            "message": "搜索成功",
            "data": products
        }
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"以图搜图失败: {str(e)}")
        raise HTTPException(status_code=500, detail=f"搜索失败: {str(e)}")

@app.get("/api/search-by-text", summary="以文搜图")
async def search_by_text(
    text: str = Query(..., description="文本描述", min_length=1, max_length=500),
    top_k: int = Query(5, description="返回结果数量", ge=1, le=100),
    db = Depends(get_db)
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
                    products.append(serialize_product(product, score))
        
        logger.info(f"文本搜索完成，找到 {len(products)} 个匹配产品")
        
        return {
            "code": 200,
            "message": "搜索成功",
            "data": products
        }
        
    except Exception as e:
        logger.error(f"以文搜图失败: {str(e)}")
        raise HTTPException(status_code=500, detail=f"搜索失败: {str(e)}")

@app.post("/api/add-product", summary="添加产品")
async def add_product(
    name: str = Query(..., description="产品名称", min_length=1, max_length=255),
    model: Optional[str] = Query(None, description="产品型号", max_length=100),
    price: Optional[float] = Query(None, description="产品价格", ge=0, le=10000000),
    stock: Optional[int] = Query(0, description="库存数量", ge=0, le=10000000),
    file: UploadFile = File(..., description="产品图片"),
    db = Depends(get_db)
):
    """
    添加产品接口（自动生成向量并存储）
    
    使用事务管理确保数据一致性
    """
    # 创建事务管理器
    transaction = TransactionManager(db)
    faiss_idx = None
    
    try:
        # 1. 验证文件
        file_id, file_ext = FileValidator.validate_file(file)
        
        # 2. 验证图片内容
        image = await FileValidator.validate_image_content(file)
        
        # 3. 保存产品图片
        filename = FileValidator.generate_safe_filename(file_id, file_ext)
        image_path = FileValidator.get_save_path(filename, "products")
        
        # 保存图片
        image.save(image_path, quality=95)
        
        # 4. 图片转向量
        vector = image_to_vector(image)
        
        # 5. 存入FAISS
        faiss_idx = add_vector(vector)
        transaction.add_faiss_index(faiss_idx)
        
        # 6. 存入MySQL
        product_data = {
            "product_name": name,
            "product_model": model,
            "price": price,
            "stock": stock,
            "product_image": image_path,
            "faiss_index": faiss_idx
        }
        product = create_product(db, product_data)
        
        # 7. 提交事务
        transaction.commit()
        
        logger.info(f"产品添加成功: {name}, FAISS索引: {faiss_idx}")
        
        return {
            "code": 200,
            "message": "产品添加成功",
            "data": {
                "product_id": product.id,
                "faiss_index": faiss_idx
            }
        }
        
    except HTTPException:
        # 回滚事务
        transaction.rollback()
        raise
    except Exception as e:
        # 回滚事务
        transaction.rollback()
        logger.error(f"添加产品失败: {str(e)}")
        raise HTTPException(status_code=500, detail=f"添加失败: {str(e)}")

# ==================== 产品管理接口 ====================

@app.get("/api/products", summary="获取产品列表")
async def list_products(
    skip: int = Query(0, description="跳过记录数", ge=0),
    limit: int = Query(100, description="返回记录数", ge=1, le=1000),
    db = Depends(get_db)
):
    """获取产品列表（分页）"""
    try:
        products = get_all_products(db, skip, limit)
        product_list = [serialize_product(p) for p in products]
        
        return {
            "code": 200,
            "message": "查询成功",
            "data": product_list,
            "total": len(product_list)
        }
        
    except Exception as e:
        logger.error(f"获取产品列表失败: {str(e)}")
        raise HTTPException(status_code=500, detail=f"查询失败: {str(e)}")

@app.get("/api/product/{product_id}", summary="获取产品详情")
async def get_product(product_id: int, db = Depends(get_db)):
    """根据ID获取产品详情"""
    try:
        product = get_product_by_id(db, product_id)
        
        if not product:
            raise HTTPException(status_code=404, detail="产品不存在")
        
        return {
            "code": 200,
            "message": "查询成功",
            "data": serialize_product(product)
        }
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"获取产品详情失败: {str(e)}")
        raise HTTPException(status_code=500, detail=f"查询失败: {str(e)}")

@app.put("/api/product/{product_id}", summary="更新产品信息")
async def update_product_info(
    product_id: int,
    name: Optional[str] = Query(None, description="产品名称", min_length=1, max_length=255),
    model: Optional[str] = Query(None, description="产品型号", max_length=100),
    price: Optional[float] = Query(None, description="产品价格", ge=0, le=10000000),
    stock: Optional[int] = Query(None, description="库存数量", ge=0, le=10000000),
    db = Depends(get_db)
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
        
        if not update_data:
            raise HTTPException(status_code=400, detail="没有要更新的数据")
        
        product = update_product(db, product_id, update_data)
        
        if not product:
            raise HTTPException(status_code=404, detail="产品不存在")
        
        logger.info(f"产品更新成功: {product_id}")
        
        return {
            "code": 200,
            "message": "更新成功"
        }
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"更新产品失败: {str(e)}")
        raise HTTPException(status_code=500, detail=f"更新失败: {str(e)}")

@app.delete("/api/product/{product_id}", summary="删除产品")
async def delete_product_by_id(product_id: int, db = Depends(get_db)):
    """删除产品"""
    try:
        # 获取产品信息
        product = get_product_by_id(db, product_id)
        if not product:
            raise HTTPException(status_code=404, detail="产品不存在")
        
        # 删除产品
        success = delete_product(db, product_id)
        
        if not success:
            raise HTTPException(status_code=500, detail="删除失败")
        
        # TODO: 删除对应的FAISS向量（需要实现）
        
        logger.info(f"产品删除成功: {product_id}")
        
        return {
            "code": 200,
            "message": "删除成功"
        }
        
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"删除产品失败: {str(e)}")
        raise HTTPException(status_code=500, detail=f"删除失败: {str(e)}")

# ==================== 系统信息接口 ====================

@app.get("/api/health", summary="健康检查")
async def health_check():
    """健康检查接口"""
    try:
        # 检查数据库连接
        db_ok = test_connection()
        
        # 检查FAISS索引
        faiss_ok = get_index_size() >= 0
        
        status = "healthy" if (db_ok and faiss_ok) else "unhealthy"
        
        return {
            "status": status,
            "database": "connected" if db_ok else "disconnected",
            "faiss": "ready" if faiss_ok else "error",
            "vector_count": get_index_size(),
            "timestamp": datetime.now().isoformat()
        }
    except Exception as e:
        return {
            "status": "error",
            "message": str(e),
            "timestamp": datetime.now().isoformat()
        }

@app.get("/api/system/info", summary="获取系统信息")
async def get_system_info():
    """获取系统运行状态信息"""
    try:
        return {
            "code": 200,
            "data": {
                "vector_count": get_index_size(),
                "upload_dir": settings.UPLOAD_DIR,
                "vector_dimension": settings.VECTOR_DIM,
                "model": settings.CLIP_MODEL,
                "status": "running"
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
        "message": f"{settings.APP_NAME} API",
        "version": settings.APP_VERSION,
        "docs": "/docs",
        "health": "/api/health"
    }

# 启动服务
if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        app,
        host=settings.API_HOST,
        port=settings.API_PORT,
        log_level=settings.LOG_LEVEL.lower()
    )
