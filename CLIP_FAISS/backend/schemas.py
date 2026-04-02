"""
Pydantic数据验证模型
"""
from pydantic import BaseModel, validator, constr
from typing import Optional, List
from datetime import datetime

# ==================== 产品相关 ====================

class ProductBase(BaseModel):
    """产品基础模型"""
    name: constr(min_length=1, max_length=255, strip_whitespace=True)
    model: Optional[constr(max_length=100)]
    price: Optional[float]
    stock: Optional[int]
    
    @validator('price')
    def validate_price(cls, v):
        if v is not None:
            if v < 0:
                raise ValueError('价格不能为负数')
            if v > 10000000:
                raise ValueError('价格超出范围（最大 10,000,000）')
        return v
    
    @validator('stock')
    def validate_stock(cls, v):
        if v is not None:
            if v < 0:
                raise ValueError('库存不能为负数')
            if v > 10000000:
                raise ValueError('库存超出范围（最大 10,000,000）')
        return v

class ProductCreate(ProductBase):
    """产品创建模型"""
    pass

class ProductUpdate(BaseModel):
    """产品更新模型"""
    name: Optional[constr(min_length=1, max_length=255, strip_whitespace=True)]
    model: Optional[constr(max_length=100)]
    price: Optional[float]
    stock: Optional[int]
    
    @validator('price')
    def validate_price(cls, v):
        if v is not None:
            if v < 0:
                raise ValueError('价格不能为负数')
            if v > 10000000:
                raise ValueError('价格超出范围')
        return v
    
    @validator('stock')
    def validate_stock(cls, v):
        if v is not None:
            if v < 0:
                raise ValueError('库存不能为负数')
        return v

class ProductResponse(BaseModel):
    """产品响应模型"""
    id: int
    name: str
    model: Optional[str]
    price: float
    stock: int
    image: Optional[str]
    faiss_index: Optional[int]
    create_time: Optional[datetime]
    
    class Config:
        orm_mode = True

class ProductSearchResult(BaseModel):
    """产品搜索结果模型"""
    id: int
    name: str
    model: Optional[str]
    price: float
    stock: int
    image: Optional[str]
    similarity: float
    create_time: Optional[str]

# ==================== 搜索相关 ====================

class SearchByTextRequest(BaseModel):
    """文本搜索请求"""
    text: constr(min_length=1, max_length=500, strip_whitespace=True)
    top_k: int = 5
    
    @validator('top_k')
    def validate_top_k(cls, v):
        if v < 1 or v > 100:
            raise ValueError('top_k范围: 1-100')
        return v

class SearchResponse(BaseModel):
    """搜索响应模型"""
    code: int = 200
    message: str = "成功"
    data: List[ProductSearchResult]

# ==================== 通用响应 ====================

class BaseResponse(BaseModel):
    """基础响应模型"""
    code: int = 200
    message: str = "成功"

class DataResponse(BaseResponse):
    """数据响应模型"""
    data: dict

class ErrorResponse(BaseModel):
    """错误响应模型"""
    code: int
    message: str
    detail: Optional[str]

# ==================== 系统信息 ====================

class SystemInfo(BaseModel):
    """系统信息模型"""
    vector_count: int
    upload_dir: str
    vector_dimension: int
    model: str
    status: str = "healthy"
