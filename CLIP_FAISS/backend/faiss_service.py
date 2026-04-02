"""
FAISS向量检索服务模块
负责向量存储、索引构建和相似度检索
"""
import faiss
import numpy as np
import os
import logging
import asyncio
from typing import Tuple, List
from config import settings

# 配置日志
logger = logging.getLogger(__name__)

# 全局FAISS索引
faiss_index = None
# 索引是否需要保存
_index_dirty = False
# 后台保存任务
_save_task = None

async def save_index_periodically():
    """后台任务：定期保存索引"""
    global _index_dirty
    
    while True:
        try:
            await asyncio.sleep(settings.FAISS_SAVE_INTERVAL)
            
            if _index_dirty and faiss_index is not None:
                faiss.write_index(faiss_index, settings.FAISS_INDEX_PATH)
                _index_dirty = False
                logger.info("FAISS索引已自动保存")
                
        except Exception as e:
            logger.error(f"索引保存失败: {str(e)}")

def init_faiss():
    """
    初始化FAISS索引
    """
    global faiss_index, _save_task
    
    try:
        if os.path.exists(settings.FAISS_INDEX_PATH):
            # 加载已有索引
            faiss_index = faiss.read_index(settings.FAISS_INDEX_PATH)
            logger.info(f"加载已有FAISS索引，包含 {faiss_index.ntotal} 个向量")
        else:
            # 创建新索引（使用内积索引，适合归一化向量）
            faiss_index = faiss.IndexFlatIP(settings.VECTOR_DIM)
            logger.info(f"创建新的FAISS索引，向量维度: {settings.VECTOR_DIM}")
        
        # 启动后台保存任务
        if _save_task is None:
            _save_task = asyncio.create_task(save_index_periodically())
            logger.info("启动FAISS索引自动保存任务")
            
    except Exception as e:
        logger.error(f"FAISS索引初始化失败: {str(e)}")
        raise

def add_vector(vector: np.ndarray) -> int:
    """
    添加向量到FAISS索引
    
    Args:
        vector: 512维特征向量
        
    Returns:
        int: 新增向量的索引ID
    """
    global faiss_index, _index_dirty
    
    if faiss_index is None:
        raise RuntimeError("FAISS索引未初始化，请先调用init_faiss()")
    
    try:
        # 转换向量格式
        vector = vector.reshape(1, -1).astype('float32')
        
        # 添加到索引
        faiss_index.add(vector)
        
        # 标记索引需要保存（异步保存）
        _index_dirty = True
        
        # 返回新增向量的索引ID
        new_index = faiss_index.ntotal - 1
        logger.info(f"成功添加向量，索引ID: {new_index}")
        
        return new_index
        
    except Exception as e:
        logger.error(f"添加向量失败: {str(e)}")
        raise

def add_vectors_batch(vectors: np.ndarray) -> List[int]:
    """
    批量添加向量到FAISS索引
    
    Args:
        vectors: N×512维向量数组
        
    Returns:
        List[int]: 新增向量的索引ID列表
    """
    global faiss_index
    
    if faiss_index is None:
        raise RuntimeError("FAISS索引未初始化，请先调用init_faiss()")
    
    try:
        # 转换向量格式
        vectors = vectors.astype('float32')
        
        # 记录起始索引
        start_index = faiss_index.ntotal
        
        # 批量添加
        faiss_index.add(vectors)
        
        # 保存索引
        faiss.write_index(faiss_index, FAISS_INDEX_PATH)
        
        # 返回新增向量的索引ID列表
        end_index = faiss_index.ntotal
        index_list = list(range(start_index, end_index))
        logger.info(f"成功批量添加 {len(index_list)} 个向量")
        
        return index_list
        
    except Exception as e:
        logger.error(f"批量添加向量失败: {str(e)}")
        raise

def search_vector(vector: np.ndarray, top_k: int = 5) -> Tuple[np.ndarray, np.ndarray]:
    """
    向量相似度检索
    
    Args:
        vector: 查询向量
        top_k: 返回最相似的K个结果
        
    Returns:
        Tuple[np.ndarray, np.ndarray]: (相似度分数数组, FAISS索引数组)
    """
    global faiss_index
    
    if faiss_index is None:
        raise RuntimeError("FAISS索引未初始化，请先调用init_faiss()")
    
    try:
        # 转换向量格式
        vector = vector.reshape(1, -1).astype('float32')
        
        # 检索
        scores, indices = faiss_index.search(vector, top_k)
        
        logger.info(f"检索完成，返回前 {top_k} 个结果")
        
        return scores[0], indices[0]
        
    except Exception as e:
        logger.error(f"向量检索失败: {str(e)}")
        raise

def remove_vector(index_id: int):
    """
    删除指定索引的向量（FAISS不支持直接删除，需要重建索引）
    
    Args:
        index_id: 要删除的向量索引ID
    """
    global faiss_index
    
    if faiss_index is None:
        raise RuntimeError("FAISS索引未初始化，请先调用init_faiss()")
    
    try:
        # FAISS的IndexFlatIP不支持直接删除，需要重建索引
        # 获取所有向量
        all_vectors = faiss_index.reconstruct_n(0, faiss_index.ntotal)
        
        # 删除指定向量
        mask = np.ones(faiss_index.ntotal, dtype=bool)
        mask[index_id] = False
        new_vectors = all_vectors[mask]
        
        # 重建索引
        faiss_index = faiss.IndexFlatIP(VECTOR_DIM)
        if len(new_vectors) > 0:
            faiss_index.add(new_vectors)
        
        # 保存索引
        faiss.write_index(faiss_index, FAISS_INDEX_PATH)
        
        logger.info(f"成功删除向量索引 {index_id}")
        
    except Exception as e:
        logger.error(f"删除向量失败: {str(e)}")
        raise

def get_index_size() -> int:
    """
    获取索引中向量的总数
    
    Returns:
        int: 向量总数
    """
    global faiss_index
    
    if faiss_index is None:
        return 0
    
    return faiss_index.ntotal

def save_index():
    """
    手动保存索引到磁盘
    """
    global faiss_index, _index_dirty
    
    if faiss_index is not None:
        faiss.write_index(faiss_index, settings.FAISS_INDEX_PATH)
        _index_dirty = False
        logger.info("FAISS索引已保存")
