"""
MySQL数据库连接模块
负责数据库连接管理和ORM模型定义
"""
from sqlalchemy import create_engine, Column, Integer, String, Float, DateTime, ForeignKey, text
from sqlalchemy.orm import sessionmaker, relationship
from sqlalchemy.ext.declarative import declarative_base
from datetime import datetime
import logging
from config import settings

# 配置日志
logger = logging.getLogger(__name__)

# 创建数据库引擎（使用配置管理）
engine = create_engine(
    settings.DATABASE_URL,
    echo=settings.DEBUG,  # 根据DEBUG配置决定是否打印SQL
    pool_size=settings.DB_POOL_SIZE,
    max_overflow=settings.DB_MAX_OVERFLOW,
    pool_recycle=settings.DB_POOL_RECYCLE,
    pool_pre_ping=True,  # 连接前检查可用性
    echo_pool=False  # 不打印连接池日志
)

# 创建会话工厂
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

# 创建基类
Base = declarative_base()

# 定义产品信息表模型
class Product(Base):
    """产品信息表"""
    __tablename__ = "products"
    
    id = Column(Integer, primary_key=True, autoincrement=True, comment="产品唯一ID")
    product_name = Column(String(255), nullable=False, comment="产品名称")
    product_model = Column(String(100), comment="产品型号")
    price = Column(Float, comment="产品价格")
    stock = Column(Integer, default=0, comment="库存")
    product_image = Column(String(500), comment="产品图片路径")
    faiss_index = Column(Integer, comment="对应FAISS向量索引ID")
    create_time = Column(DateTime, default=datetime.now, comment="创建时间")
    update_time = Column(DateTime, default=datetime.now, onupdate=datetime.now, comment="更新时间")
    
    # 关联向量映射
    vector_mapping = relationship("VectorMapping", back_populates="product", uselist=False)

# 定义向量索引映射表模型
class VectorMapping(Base):
    """向量索引映射表"""
    __tablename__ = "vector_mapping"
    
    faiss_index = Column(Integer, primary_key=True, autoincrement=True, comment="FAISS向量索引ID")
    product_id = Column(Integer, ForeignKey("products.id"), comment="产品ID")
    
    # 关联产品
    product = relationship("Product", back_populates="vector_mapping")

def get_db():
    """
    获取数据库会话（依赖注入）
    
    Yields:
        Session: 数据库会话对象
    """
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

def init_database():
    """
    初始化数据库（创建所有表）
    """
    try:
        Base.metadata.create_all(bind=engine)
        logger.info("数据库表创建成功")
    except Exception as e:
        logger.error(f"数据库初始化失败: {str(e)}")
        raise

def test_connection():
    """
    测试数据库连接
    """
    try:
        with engine.connect() as conn:
            conn.execute(text("SELECT 1"))
        logger.info("数据库连接成功")
        return True
    except Exception as e:
        logger.error(f"数据库连接失败: {str(e)}")
        return False

# 产品数据访问函数
def create_product(db, product_data: dict) -> Product:
    """
    创建产品记录
    
    Args:
        db: 数据库会话
        product_data: 产品数据字典
        
    Returns:
        Product: 产品对象
    """
    product = Product(**product_data)
    db.add(product)
    db.commit()
    db.refresh(product)
    return product

def get_product_by_id(db, product_id: int) -> Product:
    """
    根据ID查询产品
    
    Args:
        db: 数据库会话
        product_id: 产品ID
        
    Returns:
        Product: 产品对象
    """
    return db.query(Product).filter(Product.id == product_id).first()

def get_product_by_faiss_index(db, faiss_index: int) -> Product:
    """
    根据FAISS索引查询产品
    
    Args:
        db: 数据库会话
        faiss_index: FAISS向量索引
        
    Returns:
        Product: 产品对象
    """
    return db.query(Product).filter(Product.faiss_index == faiss_index).first()

def update_product(db, product_id: int, update_data: dict) -> Product:
    """
    更新产品信息
    
    Args:
        db: 数据库会话
        product_id: 产品ID
        update_data: 更新数据字典
        
    Returns:
        Product: 更新后的产品对象
    """
    product = db.query(Product).filter(Product.id == product_id).first()
    if product:
        for key, value in update_data.items():
            setattr(product, key, value)
        db.commit()
        db.refresh(product)
    return product

def delete_product(db, product_id: int) -> bool:
    """
    删除产品
    
    Args:
        db: 数据库会话
        product_id: 产品ID
        
    Returns:
        bool: 删除成功返回True
    """
    product = db.query(Product).filter(Product.id == product_id).first()
    if product:
        db.delete(product)
        db.commit()
        return True
    return False

def get_all_products(db, skip: int = 0, limit: int = 100):
    """
    获取所有产品列表（分页）
    
    Args:
        db: 数据库会话
        skip: 跳过记录数
        limit: 返回记录数
        
    Returns:
        List[Product]: 产品列表
    """
    return db.query(Product).offset(skip).limit(limit).all()
