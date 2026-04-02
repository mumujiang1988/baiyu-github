"""
错误处理中间件
"""
from fastapi import Request, status
from fastapi.responses import JSONResponse
from fastapi.exceptions import RequestValidationError
from sqlalchemy.exc import SQLAlchemyError
import logging
import traceback

logger = logging.getLogger(__name__)

async def validation_exception_handler(request: Request, exc: RequestValidationError):
    """参数验证错误处理"""
    errors = []
    for error in exc.errors():
        errors.append({
            "field": ".".join(str(loc) for loc in error["loc"]),
            "message": error["msg"],
            "type": error["type"]
        })
    
    logger.warning(f"参数验证失败: {errors}")
    
    return JSONResponse(
        status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
        content={
            "code": 422,
            "message": "参数验证失败",
            "errors": errors
        }
    )

async def sqlalchemy_exception_handler(request: Request, exc: SQLAlchemyError):
    """数据库错误处理"""
    logger.error(f"数据库错误: {str(exc)}\n{traceback.format_exc()}")
    
    return JSONResponse(
        status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
        content={
            "code": 500,
            "message": "数据库操作失败",
            "detail": str(exc) if logger.level == logging.DEBUG else None
        }
    )

async def generic_exception_handler(request: Request, exc: Exception):
    """通用错误处理"""
    logger.error(f"未处理的异常: {str(exc)}\n{traceback.format_exc()}")
    
    return JSONResponse(
        status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
        content={
            "code": 500,
            "message": "服务器内部错误",
            "detail": str(exc) if logger.level == logging.DEBUG else None
        }
    )

class ExceptionMiddleware:
    """异常处理中间件"""
    
    def __init__(self, app):
        self.app = app
    
    async def __call__(self, scope, receive, send):
        try:
            await self.app(scope, receive, send)
        except Exception as exc:
            # 记录异常
            logger.exception(f"请求处理异常: {str(exc)}")
            
            # 返回错误响应
            response = JSONResponse(
                status_code=500,
                content={
                    "code": 500,
                    "message": "服务器内部错误"
                }
            )
            await response(scope, receive, send)
