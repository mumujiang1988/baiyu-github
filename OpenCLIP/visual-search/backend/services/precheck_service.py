"""
入库前预检查服务
"""
import logging
import shutil
from typing import List
from fastapi import UploadFile

from dependencies import (
    get_milvus_service,
    get_minio_service,
    get_product_service
)

logger = logging.getLogger(__name__)


def precheck_ingest(product_code: str, files: List[UploadFile]) -> dict:
    """
    入库前预检查
    
    Args:
        product_code: 产品编码
        files: 图片文件列表
    
    Returns:
        {
            "passed": bool,
            "milvus_available": bool,
            "minio_available": bool,
            "mysql_available": bool,
            "disk_space_sufficient": bool,
            "errors": []
        }
    """
    checks = {
        "milvus_available": False,
        "minio_available": False,
        "mysql_available": False,
        "disk_space_sufficient": False,
        "errors": []
    }
    
    # 1. 检查 Milvus
    try:
        milvus_service = get_milvus_service()
        if milvus_service and milvus_service.collection:
            checks["milvus_available"] = True
            logger.debug("✅ Milvus 服务可用")
        else:
            checks["errors"].append("Milvus 服务不可用")
            logger.warning("⚠️ Milvus collection 未初始化")
    except Exception as e:
        checks["errors"].append(f"Milvus 检查失败: {str(e)}")
        logger.error(f"Milvus 检查失败: {e}")
    
    # 2. 检查 MinIO
    try:
        minio_service = get_minio_service()
        if minio_service and minio_service.health_check():
            checks["minio_available"] = True
            logger.debug("✅ MinIO 服务可用")
        else:
            checks["errors"].append("MinIO 服务不可用")
            logger.warning("⚠️ MinIO 健康检查失败")
    except Exception as e:
        checks["errors"].append(f"MinIO 检查失败: {str(e)}")
        logger.error(f"MinIO 检查失败: {e}")
    
    # 3. 检查 MySQL
    try:
        product_service = get_product_service()
        product_service._execute_query("SELECT 1")
        checks["mysql_available"] = True
        logger.debug("✅ MySQL 服务可用")
    except Exception as e:
        checks["errors"].append(f"MySQL 检查失败: {str(e)}")
        logger.error(f"MySQL 检查失败: {e}")
    
    # 4. 检查磁盘空间
    try:
        storage_path = "/app/storage"
        usage = shutil.disk_usage(storage_path)
        # 假设每张图 10MB，预留 20% 空间
        required_space = len(files) * 10 * 1024 * 1024 * 1.2
        if usage.free > required_space:
            checks["disk_space_sufficient"] = True
            logger.debug(f"✅ 磁盘空间充足: 剩余 {usage.free/1024/1024:.1f}MB")
        else:
            checks["errors"].append(
                f"磁盘空间不足: 需要 {required_space/1024/1024:.1f}MB, "
                f"剩余 {usage.free/1024/1024:.1f}MB"
            )
            logger.warning(f"⚠️ 磁盘空间不足")
    except Exception as e:
        checks["errors"].append(f"磁盘空间检查失败: {str(e)}")
        logger.error(f"磁盘空间检查失败: {e}")
    
    checks["passed"] = all([
        checks["milvus_available"],
        checks["minio_available"],
        checks["mysql_available"],
        checks["disk_space_sufficient"]
    ])
    
    if checks["passed"]:
        logger.info(f"✅ 入库预检查通过: {product_code}, {len(files)} 张图片")
    else:
        logger.warning(f"❌ 入库预检查失败: {product_code}, 错误: {checks['errors']}")
    
    return checks
