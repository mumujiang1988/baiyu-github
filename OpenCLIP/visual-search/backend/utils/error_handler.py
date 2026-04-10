"""
统一的API错误处理工具

提供标准化的异常处理和日志记录功能。
"""
import logging
from typing import Optional, Dict, Any
from utils.response import build_error_response

logger = logging.getLogger(__name__)


class ErrorHandler:
    """错误处理器类"""
    
    @staticmethod
    def format_error(error: Exception, context: str) -> str:
        """
        格式化错误消息
        
        Args:
            error: 异常对象
            context: 操作上下文
        
        Returns:
            格式化的错误消息字符串
        """
        return f"{context}失败: {str(error)}"


def handle_api_error(
    e: Exception,
    context: str,
    error_code: str = "OPERATION_FAILED",
    log_level: str = "error"
) -> Dict[str, Any]:
    """
    统一的API错误处理
    
    Args:
        e: 异常对象
        context: 操作上下文描述（如"批量入库"、"删除产品"）
        error_code: 错误代码
        log_level: 日志级别 ("error", "warning", "info")
    
    Returns:
        标准错误响应字典
    
    Example:
        try:
            # 业务逻辑
            pass
        except Exception as e:
            return handle_api_error(e, "批量入库", "BATCH_INGEST_FAILED")
    """
    # 记录日志
    log_message = f"{context}失败: {str(e)}"
    
    if log_level == "error":
        logger.error(log_message, exc_info=True)
    elif log_level == "warning":
        logger.warning(log_message)
    else:
        logger.info(log_message)
    
    # 返回标准错误响应
    return build_error_response(
        message=f"{context}失败: {str(e)}",
        error_code=error_code
    )


def handle_validation_error(
    field: str,
    message: str,
    error_code: str = "VALIDATION_ERROR"
) -> Dict[str, Any]:
    """
    处理参数验证错误
    
    Args:
        field: 验证失败的字段名
        message: 错误消息
        error_code: 错误代码
    
    Returns:
        标准错误响应字典
    
    Example:
        if not product_code:
            return handle_validation_error("product_code", "产品编码不能为空")
    """
    return build_error_response(
        message=message,
        error_code=error_code,
        field=field
    )


def handle_not_found_error(
    resource_type: str,
    identifier: str,
    error_code: str = "NOT_FOUND"
) -> Dict[str, Any]:
    """
    处理资源不存在错误
    
    Args:
        resource_type: 资源类型（如"产品"、"图片"）
        identifier: 资源标识符
        error_code: 错误代码
    
    Returns:
        标准错误响应字典
    
    Example:
        if not product:
            return handle_not_found_error("产品", product_code)
    """
    return build_error_response(
        message=f"{resource_type} {identifier} 不存在",
        error_code=error_code,
        resource_type=resource_type,
        identifier=identifier
    )
