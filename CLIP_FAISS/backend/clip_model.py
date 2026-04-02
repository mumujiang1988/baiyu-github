"""
CLIP模型封装模块
负责加载预训练CLIP模型，实现图片到向量的转换
"""
import os

# 配置国内镜像（解决下载问题）
os.environ['HF_ENDPOINT'] = 'https://hf-mirror.com'

import torch
from transformers import CLIPProcessor, CLIPModel
from PIL import Image
import logging

# 配置日志
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# 全局变量存储模型和处理器
model = None
processor = None
device = None

def init_clip_model(model_name: str = "openai/clip-vit-base-patch32"):
    """
    初始化CLIP模型
    
    Args:
        model_name: 预训练模型名称，默认使用clip-vit-base-patch32
    """
    global model, processor, device
    
    try:
        logger.info(f"正在加载CLIP模型: {model_name}")
        
        # 加载模型和处理器
        model = CLIPModel.from_pretrained(model_name)
        processor = CLIPProcessor.from_pretrained(model_name)
        
        # 检测是否有GPU可用
        device = "cuda" if torch.cuda.is_available() else "cpu"
        model = model.to(device)
        
        logger.info(f"CLIP模型加载成功，使用设备: {device}")
        
    except Exception as e:
        logger.error(f"CLIP模型加载失败: {str(e)}")
        raise

def image_to_vector(image: Image.Image) -> 'numpy.ndarray':
    """
    将图片转换为512维向量
    
    Args:
        image: PIL.Image对象
        
    Returns:
        numpy.ndarray: 512维特征向量（已归一化）
    """
    global model, processor, device
    
    if model is None or processor is None:
        raise RuntimeError("CLIP模型未初始化，请先调用init_clip_model()")
    
    try:
        # 预处理图片
        inputs = processor(images=image, return_tensors="pt").to(device)
        
        # 提取特征向量
        with torch.no_grad():
            image_features = model.get_image_features(**inputs)
        
        # 归一化向量（提升检索精度）
        image_features = image_features / image_features.norm(dim=-1, keepdim=True)
        
        # 转换为numpy数组并展平
        vector = image_features.cpu().numpy().flatten()
        
        return vector
        
    except Exception as e:
        logger.error(f"图片转向量失败: {str(e)}")
        raise

def text_to_vector(text: str) -> 'numpy.ndarray':
    """
    将文本转换为512维向量（支持以文搜图）
    
    Args:
        text: 文本描述
        
    Returns:
        numpy.ndarray: 512维特征向量（已归一化）
    """
    global model, processor, device
    
    if model is None or processor is None:
        raise RuntimeError("CLIP模型未初始化，请先调用init_clip_model()")
    
    try:
        # 预处理文本
        inputs = processor(text=[text], return_tensors="pt", padding=True).to(device)
        
        # 提取特征向量
        with torch.no_grad():
            text_features = model.get_text_features(**inputs)
        
        # 归一化向量
        text_features = text_features / text_features.norm(dim=-1, keepdim=True)
        
        # 转换为numpy数组并展平
        vector = text_features.cpu().numpy().flatten()
        
        return vector
        
    except Exception as e:
        logger.error(f"文本转向量失败: {str(e)}")
        raise

def get_vector_dimension() -> int:
    """
    获取向量维度
    
    Returns:
        int: 向量维度（默认512）
    """
    return 512
