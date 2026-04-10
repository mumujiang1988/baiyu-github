"""
查询缓存服务

提供简单的内存缓存机制，减少重复的数据库查询。
适用于不频繁变化的数据，如产品列表、统计数据等。
"""
import time
import logging
from typing import Dict, Any, Optional, Callable
from functools import wraps

logger = logging.getLogger(__name__)


class QueryCache:
    """
    简单的内存缓存实现
    
    使用字典存储缓存数据，支持TTL过期机制。
    适用于单进程应用，多进程场景建议使用Redis。
    
    Attributes:
        cache: 缓存数据存储 {key: (value, timestamp)}
        ttl: 默认缓存时间（秒）
    """
    
    def __init__(self, ttl: int = 300):
        """
        初始化缓存
        
        Args:
            ttl: 默认缓存时间（秒），默认5分钟
        """
        self.cache: Dict[str, tuple] = {}
        self.ttl = ttl
        logger.info(f"QueryCache initialized with TTL={ttl}s")
    
    def get(self, key: str) -> Optional[Any]:
        """
        获取缓存数据
        
        Args:
            key: 缓存键
            
        Returns:
            缓存的值，如果不存在或已过期则返回 None
        """
        if key not in self.cache:
            return None
        
        value, timestamp = self.cache[key]
        
        # 检查是否过期
        if time.time() - timestamp >= self.ttl:
            logger.debug(f"Cache expired for key: {key}")
            del self.cache[key]
            return None
        
        logger.debug(f"Cache hit for key: {key}")
        return value
    
    def set(self, key: str, value: Any, ttl: Optional[int] = None):
        """
        设置缓存数据
        
        Args:
            key: 缓存键
            value: 缓存的值
            ttl: 可选的自定义TTL（秒），不使用则用默认值
        """
        effective_ttl = ttl if ttl is not None else self.ttl
        self.cache[key] = (value, time.time())
        logger.debug(f"Cache set for key: {key}, TTL: {effective_ttl}s")
    
    def delete(self, key: str):
        """
        删除缓存
        
        Args:
            key: 缓存键
        """
        if key in self.cache:
            del self.cache[key]
            logger.debug(f"Cache deleted for key: {key}")
    
    def clear(self):
        """清空所有缓存"""
        cache_size = len(self.cache)
        self.cache.clear()
        logger.info(f"Cache cleared, removed {cache_size} entries")
    
    def invalidate_pattern(self, pattern: str):
        """
        根据模式删除缓存
        
        Args:
            pattern: 键的前缀模式，如 "product_" 会删除所有以 product_ 开头的缓存
        """
        keys_to_delete = [key for key in self.cache.keys() if key.startswith(pattern)]
        for key in keys_to_delete:
            del self.cache[key]
        logger.info(f"Invalidated {len(keys_to_delete)} cache entries with pattern: {pattern}")
    
    def get_stats(self) -> Dict[str, int]:
        """
        获取缓存统计信息
        
        Returns:
            包含缓存大小等信息的字典
        """
        now = time.time()
        valid_count = 0
        expired_count = 0
        
        for key, (_, timestamp) in self.cache.items():
            if now - timestamp < self.ttl:
                valid_count += 1
            else:
                expired_count += 1
        
        return {
            "total_entries": len(self.cache),
            "valid_entries": valid_count,
            "expired_entries": expired_count,
            "ttl_seconds": self.ttl
        }


def cached(ttl: Optional[int] = None):
    """
    装饰器：自动缓存函数返回值
    
    Args:
        ttl: 缓存时间（秒），None 则使用默认值
        
    Returns:
        装饰器函数
        
    Example:
        @cached(ttl=600)
        def get_product_list():
            # 数据库查询
            return products
    """
    def decorator(func: Callable):
        @wraps(func)
        def wrapper(*args, **kwargs):
            # 生成缓存键
            cache_key = f"{func.__name__}:{str(args)}:{str(sorted(kwargs.items()))}"
            
            # 尝试从缓存获取
            cached_value = global_cache.get(cache_key)
            if cached_value is not None:
                logger.debug(f"Using cached result for {func.__name__}")
                return cached_value
            
            # 执行函数并缓存结果
            result = func(*args, **kwargs)
            global_cache.set(cache_key, result, ttl)
            logger.debug(f"Cached result for {func.__name__}")
            
            return result
        return wrapper
    return decorator


# 全局缓存实例
# 不同业务场景可以使用不同的TTL
global_cache = QueryCache(ttl=300)  # 默认5分钟

# 专用缓存实例
stats_cache = QueryCache(ttl=60)  # 统计数据缓存1分钟
product_list_cache = QueryCache(ttl=300)  # 产品列表缓存5分钟
consistency_check_cache = QueryCache(ttl=300)  # 一致性检查缓存5分钟
