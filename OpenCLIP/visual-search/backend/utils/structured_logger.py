"""
结构化日志工具

提供 JSON 格式的日志记录，便于日志分析工具解析。
"""
import json
import logging
from datetime import datetime
from typing import Any, Dict


class JsonFormatter(logging.Formatter):
    """JSON 格式的日志格式化器"""
    
    def format(self, record: logging.LogRecord) -> str:
        """将日志记录格式化为 JSON 字符串"""
        log_data = {
            "timestamp": datetime.fromtimestamp(record.created).isoformat(),
            "level": record.levelname,
            "logger": record.name,
            "message": record.getMessage(),
            "module": record.module,
            "function": record.funcName,
            "line": record.lineno,
        }
        
        # 添加异常信息
        if record.exc_info and record.exc_info[1]:
            log_data["exception"] = {
                "type": record.exc_info[0].__name__,
                "message": str(record.exc_info[1]),
            }
        
        # 添加自定义字段（通过 extra 参数传入）
        if hasattr(record, 'custom_fields'):
            log_data.update(record.custom_fields)
        
        return json.dumps(log_data, ensure_ascii=False, default=str)


def get_json_logger(name: str) -> logging.Logger:
    """
    获取 JSON 格式的日志记录器
    
    Args:
        name: 日志记录器名称
    
    Returns:
        配置了 JSON 格式化器的 Logger 实例
    """
    logger = logging.getLogger(name)
    
    # 如果已经配置过，直接返回
    if logger.handlers:
        return logger
    
    logger.setLevel(logging.INFO)
    
    # 创建控制台处理器
    console_handler = logging.StreamHandler()
    console_handler.setLevel(logging.INFO)
    
    # 设置 JSON 格式化器
    json_formatter = JsonFormatter()
    console_handler.setFormatter(json_formatter)
    
    logger.addHandler(console_handler)
    
    return logger


def log_structured(
    logger: logging.Logger,
    level: str,
    message: str,
    **kwargs
):
    """
    记录结构化日志
    
    Args:
        logger: 日志记录器
        level: 日志级别 (INFO, WARNING, ERROR, etc.)
        message: 日志消息
        **kwargs: 自定义字段
    
    Example:
        >>> log_structured(logger, "INFO", "产品入库成功", 
        ...                product_code="P001", image_count=5)
        {"timestamp": "...", "level": "INFO", "message": "产品入库成功", 
         "product_code": "P001", "image_count": 5}
    """
    # 创建 LogRecord
    record = logger.makeRecord(
        logger.name,
        getattr(logging, level.upper()),
        "(unknown file)",
        0,
        message,
        (),
        None
    )
    
    # 添加自定义字段
    record.custom_fields = kwargs
    
    # 记录日志
    logger.handle(record)


# ==================== 便捷函数 ====================

def log_batch_ingest_event(
    logger: logging.Logger,
    event_type: str,
    product_code: str,
    status: str,
    **kwargs
):
    """
    记录批量入库事件
    
    Args:
        logger: 日志记录器
        event_type: 事件类型 (start, success, failure, cleanup)
        product_code: 产品编码
        status: 状态 (success, failed, partial)
        **kwargs: 额外字段
    """
    log_structured(
        logger,
        "INFO" if status == "success" else "ERROR",
        f"批量入库事件: {event_type}",
        event_type=event_type,
        product_code=product_code,
        status=status,
        **kwargs
    )


def log_data_consistency_check(
    logger: logging.Logger,
    check_type: str,
    result: Dict[str, Any],
    duration_ms: int
):
    """
    记录数据一致性检查事件
    
    Args:
        logger: 日志记录器
        check_type: 检查类型 (consistency, orphan_data)
        result: 检查结果
        duration_ms: 耗时（毫秒）
    """
    log_structured(
        logger,
        "INFO",
        f"数据一致性检查完成: {check_type}",
        check_type=check_type,
        mysql_products=result.get("mysql_product_count", 0),
        milvus_vectors=result.get("milvus_vector_count", 0),
        minio_files=result.get("minio_file_count", 0),
        issues_found=result.get("issues_count", 0),
        duration_ms=duration_ms
    )


def log_cleanup_event(
    logger: logging.Logger,
    product_code: str,
    milvus_count: int = 0,
    minio_count: int = 0,
    mysql_count: int = 0,
    status: str = "success",
    errors: list = None
):
    """
    记录清理事件
    
    Args:
        logger: 日志记录器
        product_code: 产品编码
        milvus_count: 清理的 Milvus 向量数
        minio_count: 清理的 MinIO 文件数
        mysql_count: 清理的 MySQL 记录数
        status: 状态 (success, partial_failure, failure)
        errors: 错误列表
    """
    log_structured(
        logger,
        "WARNING" if status != "success" else "INFO",
        f"数据清理完成: {product_code}",
        product_code=product_code,
        milvus_count=milvus_count,
        minio_count=minio_count,
        mysql_count=mysql_count,
        status=status,
        errors=errors or []
    )


def log_performance_metric(
    logger: logging.Logger,
    operation: str,
    duration_ms: int,
    count: int = 0,
    **kwargs
):
    """
    记录性能指标
    
    Args:
        logger: 日志记录器
        operation: 操作名称
        duration_ms: 耗时（毫秒）
        count: 处理数量
        **kwargs: 额外字段
    """
    log_structured(
        logger,
        "INFO",
        f"性能指标: {operation}",
        operation=operation,
        duration_ms=duration_ms,
        count=count,
        throughput=count / (duration_ms / 1000) if duration_ms > 0 else 0,
        **kwargs
    )
