"""
FastAPI 主入口 - 以图搜品系统
"""
import os
import time
import logging
from typing import List, Optional
from fastapi import FastAPI, UploadFile, File, Form, HTTPException, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse, FileResponse
from pydantic import BaseModel
from pydantic_settings import BaseSettings
from slowapi import Limiter
from slowapi.util import get_remote_address
from slowapi.errors import RateLimitExceeded

# 配置日志
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(name)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

# 请求限流配置
limiter = Limiter(key_func=get_remote_address)

# 导入服务模块
from services.clip_service import ClipService
from services.milvus_service import MilvusService
from services.product_service import ProductService
from services.image_processor import ImageProcessor
from utils.error_handler import ErrorHandler


# 配置类
class Settings(BaseSettings):
    mysql_host: str = "localhost"
    mysql_port: int = 3306
    mysql_user: str = "vs_user"
    mysql_password: str = "vs_pass123"
    mysql_db: str = "visual_search"
    milvus_host: str = "localhost"
    milvus_port: int = 19530
    openclip_model: str = "ViT-B-32"
    openclip_pretrained: str = "laion2b_s34b_b79k"

    class Config:
        env_file = ".env"


settings = Settings()

# FastAPI 应用
app = FastAPI(title="以图搜品系统", description="基于OpenCLIP+Milvus的图像检索系统", version="1.0.0")
app.state.limiter = limiter

# 异常处理
@app.exception_handler(RateLimitExceeded)
async def rate_limit_handler(request: Request, exc: RateLimitExceeded):
    return JSONResponse(status_code=429, content={"detail": "请求过于频繁，请稍后再试"})


# CORS 配置
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["GET", "POST", "DELETE", "PUT"],
    allow_headers=["*"],
)

# 全局服务实例
clip_service = None
milvus_service = None
product_service = None
image_processor = None


@app.on_event("startup")
async def startup_event():
    """应用启动时初始化服务"""
    global clip_service, milvus_service, product_service, image_processor

    print("🚀 正在初始化服务...")

    try:
        clip_service = ClipService(
            model_name=settings.openclip_model,
            pretrained=settings.openclip_pretrained,
            cache_dir='/app/models_cache'
        )
        print("✅ OpenCLIP 模型加载完成")

        milvus_service = MilvusService(host=settings.milvus_host, port=settings.milvus_port)
        milvus_service.create_collection()
        print("✅ Milvus 连接成功")

        product_service = ProductService(
            host=settings.mysql_host,
            port=settings.mysql_port,
            user=settings.mysql_user,
            password=settings.mysql_password,
            database=settings.mysql_db
        )
        print("✅ MySQL 连接成功")

        image_processor = ImageProcessor()
        print("✅ 图片处理器初始化完成")

        print("🎉 所有服务初始化完成！")

    except Exception as e:
        print(f"❌ 服务初始化失败: {str(e)}")
        print("⚠️ 应用将在受限模式下运行（部分功能不可用）")
        logger.error(f"服务初始化失败: {str(e)}", exc_info=True)


# ==================== 数据模型 ====================

class SearchResult(BaseModel):
    product_code: str
    product_name: str
    spec: Optional[str]
    category: Optional[str]
    similarity: float
    image_paths: List[str]


class SearchResponse(BaseModel):
    success: bool
    message: str
    results: List[SearchResult]
    search_time_ms: int


# ==================== API 接口 ====================

@app.get("/")
async def root():
    """健康检查"""
    return {"status": "ok", "message": "以图搜品系统运行中"}


@app.get("/health")
async def health_check():
    """详细健康检查"""
    checks = {
        "mysql": product_service is not None,
        "milvus": milvus_service is not None,
        "clip": clip_service is not None
    }
    return {"status": "healthy" if all(checks.values()) else "unhealthy", "checks": checks}


@app.post("/api/v1/search", response_model=SearchResponse)
@limiter.limit("10/minute")
async def search_image(
    request: Request,
    file: UploadFile = File(...),
    top_k: int = 10,
    aggregation: str = "max"
):
    """图像检索接口"""
    # 参数验证
    if not (1 <= top_k <= 100):
        raise HTTPException(status_code=400, detail="top_k 必须在 1-100 之间")
    if aggregation not in ["max", "avg"]:
        raise HTTPException(status_code=400, detail="aggregation 必须是 max 或 avg")

    start_time = time.time()

    try:
        image_bytes = await file.read()
        logger.info(f"检索请求: 文件大小 {len(image_bytes)} bytes")

        processed_image = image_processor.preprocess(image_bytes)
        embedding = clip_service.extract_features(processed_image)

        raw_results = milvus_service.search(query_vector=embedding, top_k=top_k * 3)
        aggregated_results = product_service.aggregate_results(
            raw_results=raw_results, aggregation=aggregation, top_k=top_k
        )

        results = []
        for item in aggregated_results:
            product_info = product_service.get_product(item["product_code"])
            if product_info:
                results.append(SearchResult(
                    product_code=item["product_code"],
                    product_name=product_info["name"],
                    spec=product_info.get("spec"),
                    category=product_info.get("category"),
                    similarity=item["similarity"],
                    image_paths=item["image_paths"]
                ))

        search_time = int((time.time() - start_time) * 1000)
        logger.info(f"检索完成: 找到 {len(results)} 个结果, 耗时 {search_time}ms")

        if results:
            image_hash = image_processor.compute_hash(image_bytes)
            product_service.log_search(
                query_hash=image_hash,
                top_product_code=results[0].product_code,
                similarity=results[0].similarity,
                search_time_ms=search_time,
                result_count=len(results)
            )

        return SearchResponse(
            success=True,
            message=f"检索完成，找到 {len(results)} 个相似产品",
            results=results,
            search_time_ms=search_time
        )

    except Exception as e:
        search_time = int((time.time() - start_time) * 1000)
        logger.error(f"检索失败: {str(e)}", exc_info=True)
        return SearchResponse(
            success=False,
            message=ErrorHandler.format_error(error=e, context="图片检索"),
            results=[],
            search_time_ms=search_time
        )


@app.post("/api/v1/product/ingest")
@limiter.limit("5/minute")
async def ingest_product(
    request: Request,
    product_code: str = Form(...),
    name: str = Form(...),
    spec: Optional[str] = Form(None),
    category: Optional[str] = Form(None),
    files: List[UploadFile] = File(...)
):
    """产品入库接口"""
    start_time = time.time()

    try:
        if not files:
            raise HTTPException(status_code=400, detail="至少需要上传一张图片")

        logger.info(f"产品入库请求: product_code={product_code}, name={name}, spec={spec}, category={category}, 图片数量={len(files)}")
        product_service.upsert_product(product_code=product_code, name=name, spec=spec, category=category)
        logger.info(f"产品信息已保存/更新: {product_code}")

        success_count = 0
        fail_count = 0
        errors = []

        for file in files:
            try:
                image_bytes = await file.read()
                processed_image = image_processor.preprocess(image_bytes)
                image_hash = image_processor.compute_hash(image_bytes)

                # 检查图片是否已存在（全局去重）
                if product_service.image_exists(image_hash):
                    logger.warning(f"图片已存在: {file.filename}, hash={image_hash[:16]}...")
                    suggestion = ErrorHandler.get_error_suggestion('duplicate')
                    error_msg = f"{file.filename}: 图片已存在"
                    if suggestion:
                        error_msg += f" ({suggestion})"
                    errors.append(error_msg)
                    fail_count += 1
                    continue

                # 提取特征向量
                embedding = clip_service.extract_features(processed_image)
                
                # 插入到 Milvus
                milvus_id = milvus_service.insert(product_code=product_code, embedding=embedding)
                
                # 保存图片文件
                image_path = image_processor.save_image(
                    image_bytes=image_bytes, product_code=product_code, filename=file.filename
                )
                
                # 保存图片记录到数据库
                product_service.insert_image(
                    product_code=product_code,
                    image_path=image_path,
                    image_hash=image_hash,
                    milvus_id=milvus_id,
                    image_size=len(image_bytes)
                )
                success_count += 1
                logger.debug(f"图片入库成功: {file.filename}")

            except Exception as e:
                error_detail = ErrorHandler.format_error(error=e, context="图片处理失败", filename=file.filename)
                logger.error(f"图片入库失败: {file.filename}, 错误: {str(e)}")
                errors.append(error_detail)
                fail_count += 1

        ingest_time = int((time.time() - start_time) * 1000)
        logger.info(f"入库完成: {product_code}, 成功 {success_count} 张, 失败 {fail_count} 张, 耗时 {ingest_time}ms")

        product_service.log_ingest(
            product_code=product_code,
            image_count=len(files),
            success_count=success_count,
            fail_count=fail_count,
            ingest_time_ms=ingest_time,
            error_msg="; ".join(errors) if errors else None
        )

        return {
            "success": True,
            "message": f"入库完成: 成功 {success_count} 张, 失败 {fail_count} 张",
            "product_code": product_code,
            "success_count": success_count,
            "fail_count": fail_count,
            "ingest_time_ms": ingest_time,
            "errors": errors
        }

    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"入库失败: {str(e)}", exc_info=True)
        return {
            "success": False,
            "message": ErrorHandler.format_error(error=e, context="产品入库"),
            "product_code": product_code,
            "suggestion": ErrorHandler.get_error_suggestion(str(e))
        }


@app.delete("/api/v1/product/{product_code}")
async def delete_product(product_code: str):
    """删除产品及其所有图片"""
    try:
        images = product_service.get_product_images(product_code)
        milvus_ids = [img["milvus_id"] for img in images if img["milvus_id"]]
        if milvus_ids:
            milvus_service.delete(milvus_ids)

        for img in images:
            image_processor.delete_image(img["image_path"])

        product_service.delete_product(product_code)

        return {"success": True, "message": f"产品 {product_code} 删除成功", "deleted_images": len(images)}

    except Exception as e:
        return {"success": False, "message": ErrorHandler.format_error(error=e, context="删除产品")}


@app.get("/api/v1/product/{product_code}")
async def get_product(product_code: str):
    """获取产品详情"""
    product = product_service.get_product(product_code)
    if not product:
        raise HTTPException(status_code=404, detail="产品不存在")

    images = product_service.get_product_images(product_code)
    return {"success": True, "product": product, "images": images}


@app.get("/api/v1/products")
async def list_products(category: Optional[str] = None, page: int = 1, page_size: int = 20):
    """产品列表查询"""
    if page < 1:
        raise HTTPException(status_code=400, detail="page 必须大于 0")
    if not (1 <= page_size <= 100):
        raise HTTPException(status_code=400, detail="page_size 必须在 1-100 之间")

    products = product_service.list_products(category=category, page=page, page_size=page_size)
    total = product_service.count_products(category=category)

    return {"success": True, "products": products, "total": total, "page": page, "page_size": page_size}


@app.get("/api/v1/stats")
async def get_stats():
    """获取系统统计信息"""
    stats = {
        "product_count": product_service.count_products(),
        "image_count": product_service.count_images(),
        "search_count": product_service.count_searches(),
        "milvus_stats": milvus_service.get_stats()
    }
    return {"success": True, "stats": stats}


@app.get("/api/v1/images/{image_path:path}")
async def get_image(image_path: str):
    """获取图片文件"""
    file_path = os.path.join("storage", "images", image_path)

    if not os.path.exists(file_path):
        raise HTTPException(status_code=404, detail="图片不存在")
    if not os.path.isfile(file_path):
        raise HTTPException(status_code=400, detail="无效的图片路径")

    return FileResponse(
        file_path,
        media_type="image/jpeg",
        headers={
            "Cache-Control": "public, max-age=31536000",
            "Content-Disposition": f"inline; filename={os.path.basename(file_path)}"
        }
    )


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
