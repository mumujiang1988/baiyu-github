"""
事务管理工具
确保FAISS和MySQL数据一致性
"""
from functools import wraps
from typing import Callable
import logging
from faiss_service import remove_vector

logger = logging.getLogger(__name__)

def transactional(func: Callable) -> Callable:
    """
    事务装饰器
    确保FAISS和MySQL操作的原子性
    
    使用示例:
        @transactional
        def add_product_with_vector(db, product_data, vector):
            faiss_idx = add_vector(vector)
            try:
                product = create_product(db, product_data)
                return product
            except Exception:
                # 自动回滚FAISS索引
                remove_vector(faiss_idx)
                raise
    """
    @wraps(func)
    async def async_wrapper(*args, **kwargs):
        try:
            result = await func(*args, **kwargs)
            return result
        except Exception as e:
            logger.error(f"事务执行失败: {str(e)}")
            raise
    
    @wraps(func)
    def sync_wrapper(*args, **kwargs):
        try:
            result = func(*args, **kwargs)
            return result
        except Exception as e:
            logger.error(f"事务执行失败: {str(e)}")
            raise
    
    # 判断是异步函数还是同步函数
    import asyncio
    if asyncio.iscoroutinefunction(func):
        return async_wrapper
    else:
        return sync_wrapper

class TransactionManager:
    """事务管理器"""
    
    def __init__(self, db_session):
        self.db_session = db_session
        self.faiss_indices = []
    
    def add_faiss_index(self, index: int):
        """记录FAISS索引"""
        self.faiss_indices.append(index)
    
    def commit(self):
        """提交事务"""
        try:
            self.db_session.commit()
            logger.info("事务提交成功")
        except Exception as e:
            # 回滚数据库
            self.db_session.rollback()
            # 回滚FAISS索引
            for idx in self.faiss_indices:
                try:
                    remove_vector(idx)
                    logger.info(f"回滚FAISS索引: {idx}")
                except Exception as e2:
                    logger.error(f"回滚FAISS索引失败: {str(e2)}")
            raise e
    
    def rollback(self):
        """回滚事务"""
        # 回滚数据库
        self.db_session.rollback()
        # 回滚FAISS索引
        for idx in self.faiss_indices:
            try:
                remove_vector(idx)
                logger.info(f"回滚FAISS索引: {idx}")
            except Exception as e:
                logger.error(f"回滚FAISS索引失败: {str(e)}")
