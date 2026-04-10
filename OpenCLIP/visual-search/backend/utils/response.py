"""
统一 API 响应格式工具

提供标准化的成功/失败响应格式，确保前后端一致性。

## 响应格式规范

### 成功响应（扁平格式）
{
    "success": true,
    "message": "操作成功描述",
    "results": [...],        // 业务数据直接在根级别
    "search_time_ms": 150    // 其他业务字段
}

### 失败响应
{
    "success": false,
    "message": "错误描述",
    "error_code": "ERROR_CODE",  // 可选，错误码
    "details": { ... }           // 可选，详细错误信息
}

## 使用示例

```python
from utils.response import build_success_response, build_error_response

# 成功响应
return build_success_response(
    message="产品入库成功",
    product_code="P001",
    ingested_images=5,
    elapsed_seconds=2.3
)

# 失败响应
return build_error_response(
    message="产品不存在",
    error_code="PRODUCT_NOT_FOUND"
)
```

## 字段分类规范

### 元数据字段（固定）
- `success`: 布尔值，表示请求是否成功
- `message`: 字符串，人类可读的消息
- `error_code`: 字符串，错误代码（仅失败时）

### 业务数据字段（动态）
- 直接放在响应根级别
- 使用 snake_case 命名
- 避免与元数据字段重名
- 复数形式表示列表（如 `results`, `products`）
"""

from typing import Any, Optional, Dict
from datetime import datetime


def build_success_response(
    message: str = "操作成功",
    **business_data
) -> Dict[str, Any]:
    """
    构建成功响应（扁平格式）
    
    Args:
        message: 成功消息
        **business_data: 业务数据（自动展开到根级别）
    
    Returns:
        扁平化的成功响应
    
    Example:
        >>> build_success_response(
        ...     message="检索成功",
        ...     results=[...],
        ...     search_time_ms=150
        ... )
        {
            "success": True,
            "message": "检索成功",
            "results": [...],
            "search_time_ms": 150
        }
    """
    return {
        "success": True,
        "message": message,
        **business_data  # 展开业务数据到根级别
    }


def build_error_response(
    message: str = "操作失败",
    error_code: Optional[str] = None,
    **extra_info
) -> Dict[str, Any]:
    """
    构建错误响应（扁平格式）
    
    Args:
        message: 错误消息
        error_code: 错误代码（可选）
        **extra_info: 额外错误信息（可选）
    
    Returns:
        扁平化的错误响应
    
    Example:
        >>> build_error_response(
        ...     message="产品不存在",
        ...     error_code="PRODUCT_NOT_FOUND"
        ... )
        {
            "success": False,
            "message": "产品不存在",
            "error_code": "PRODUCT_NOT_FOUND"
        }
    """
    response = {
        "success": False,
        "message": message,
    }
    
    if error_code:
        response["error_code"] = error_code
    
    # 添加额外的错误信息
    response.update(extra_info)
    
    return response


# ==================== 向后兼容别名 ====================
# 保留旧函数名以避免破坏现有代码
# 建议新代码使用 build_success_response / build_error_response

success_response = build_success_response
error_response = build_error_response


def paginated_response(
    items: list,
    total: int,
    page: int = 1,
    page_size: int = 20,
    message: str = "查询成功"
) -> Dict[str, Any]:
    """
    构建分页响应（扁平格式）
    
    Args:
        items: 数据列表
        total: 总记录数
        page: 当前页码
        page_size: 每页数量
        message: 成功消息
        
    Returns:
        扁平化的分页响应
    
    Example:
        >>> paginated_response(
        ...     items=[...],
        ...     total=100,
        ...     page=1,
        ...     page_size=20
        ... )
        {
            "success": True,
            "message": "查询成功",
            "items": [...],
            "pagination": {
                "total": 100,
                "page": 1,
                "page_size": 20,
                "total_pages": 5
            }
        }
    """
    return build_success_response(
        message=message,
        items=items,
        pagination={
            "total": total,
            "page": page,
            "page_size": page_size,
            "total_pages": (total + page_size - 1) // page_size if page_size > 0 else 0
        }
    )
