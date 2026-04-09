"""
重试工具模块 - 为关键操作提供自动重试机制
"""
import logging
from tenacity import (
    retry,
    stop_after_attempt,
    wait_exponential,
    retry_if_exception_type,
    before_log,
    after_log
)

logger = logging.getLogger(__name__)


# ==================== 通用重试装饰器 ====================

def retry_on_failure(
    max_attempts: int = 3,
    min_wait: float = 1,
    max_wait: float = 10,
    multiplier: float = 1,
    log_level: int = logging.WARNING
):
    """
    通用重试装饰器
    
    Args:
        max_attempts: 最大重试次数（包含首次尝试）
        min_wait: 最小等待时间（秒）
        max_wait: 最大等待时间（秒）
        multiplier: 指数退避乘数
        log_level: 日志级别
    
    Returns:
        重试装饰器
    """
    return retry(
        stop=stop_after_attempt(max_attempts),
        wait=wait_exponential(multiplier=multiplier, min=min_wait, max=max_wait),
        retry=retry_if_exception_type(Exception),
        before=before_log(logger, log_level),
        after=after_log(logger, log_level),
        reraise=True
    )


# ==================== Milvus 操作重试 ====================

@retry(
    stop=stop_after_attempt(3),
    wait=wait_exponential(multiplier=1, min=2, max=10),
    retry=retry_if_exception_type(Exception),
    before=before_log(logger, logging.INFO),
    after=after_log(logger, logging.WARNING),
    reraise=True
)
def delete_milvus_with_retry(milvus_service, milvus_ids: list):
    """
    带重试的 Milvus 删除操作
    
    Args:
        milvus_service: Milvus 服务实例
        milvus_ids: 要删除的向量 ID 列表
    
    Returns:
        删除结果
    
    Raises:
        Exception: 重试 3 次后仍然失败则抛出异常
    """
    if not milvus_ids:
        logger.debug("没有需要删除的 Milvus 向量")
        return
    
    logger.info(f"正在删除 {len(milvus_ids)} 个 Milvus 向量...")
    result = milvus_service.delete(milvus_ids)
    logger.info(f"✅ Milvus 向量删除成功")
    return result


@retry(
    stop=stop_after_attempt(3),
    wait=wait_exponential(multiplier=1, min=2, max=10),
    retry=retry_if_exception_type(Exception),
    before=before_log(logger, logging.INFO),
    after=after_log(logger, logging.WARNING),
    reraise=True
)
def insert_milvus_with_retry(milvus_service, product_code: str, embedding, image_id: int = 0):
    """
    带重试的 Milvus 插入操作
    
    Args:
        milvus_service: Milvus 服务实例
        product_code: 产品编码
        embedding: 特征向量
        image_id: 图片 ID
    
    Returns:
        Milvus ID
    
    Raises:
        Exception: 重试 3 次后仍然失败则抛出异常
    """
    logger.debug(f"正在插入 Milvus 向量: {product_code}")
    milvus_id = milvus_service.insert(product_code, embedding, image_id)
    logger.debug(f"✅ Milvus 向量插入成功: ID={milvus_id}")
    return milvus_id


# ==================== MinIO 操作重试 ====================

@retry(
    stop=stop_after_attempt(3),
    wait=wait_exponential(multiplier=1, min=2, max=10),
    retry=retry_if_exception_type(Exception),
    before=before_log(logger, logging.INFO),
    after=after_log(logger, logging.WARNING),
    reraise=True
)
def delete_minio_with_retry(image_processor, image_path: str):
    """
    带重试的 MinIO 删除操作
    
    Args:
        image_processor: 图片处理器实例
        image_path: 图片路径
    
    Raises:
        Exception: 重试 3 次后仍然失败则抛出异常
    """
    logger.debug(f"正在删除 MinIO 图片: {image_path}")
    image_processor.delete_image(image_path)
    logger.debug(f"✅ MinIO 图片删除成功: {image_path}")


@retry(
    stop=stop_after_attempt(3),
    wait=wait_exponential(multiplier=1, min=2, max=10),
    retry=retry_if_exception_type(Exception),
    before=before_log(logger, logging.INFO),
    after=after_log(logger, logging.WARNING),
    reraise=True
)
def save_minio_with_retry(image_processor, image_bytes, object_name: str, content_type: str = "image/jpeg"):
    """
    带重试的 MinIO 保存操作
    
    Args:
        image_processor: 图片处理器实例
        image_bytes: 图片字节数据
        object_name: 对象名称
        content_type: 内容类型
    
    Returns:
        保存结果
    
    Raises:
        Exception: 重试 3 次后仍然失败则抛出异常
    """
    logger.debug(f"正在保存 MinIO 图片: {object_name}")
    result = image_processor.client.put_object(
        bucket_name=image_processor.bucket_name,
        object_name=object_name,
        data=image_bytes,
        length=len(image_bytes),
        content_type=content_type
    )
    logger.debug(f"✅ MinIO 图片保存成功: {object_name}")
    return result


# ==================== MySQL 操作重试 ====================

@retry(
    stop=stop_after_attempt(3),
    wait=wait_exponential(multiplier=1, min=1, max=5),
    retry=retry_if_exception_type(Exception),
    before=before_log(logger, logging.INFO),
    after=after_log(logger, logging.WARNING),
    reraise=True
)
def execute_mysql_with_retry(product_service, sql: str, params: tuple = None):
    """
    带重试的 MySQL 执行操作
    
    Args:
        product_service: 产品服务实例
        sql: SQL 语句
        params: SQL 参数
    
    Returns:
        执行结果
    
    Raises:
        Exception: 重试 3 次后仍然失败则抛出异常
    """
    logger.debug(f"正在执行 MySQL: {sql[:50]}...")
    if params:
        result = product_service._execute_update(sql, params)
    else:
        result = product_service._execute_update(sql)
    logger.debug(f"✅ MySQL 执行成功")
    return result
