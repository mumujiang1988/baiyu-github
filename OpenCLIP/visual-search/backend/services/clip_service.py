"""
OpenCLIP 特征提取服务
"""
import torch
import open_clip
from PIL import Image
import numpy as np
from typing import Union
import os
import logging

logger = logging.getLogger(__name__)


class ClipService:
    """OpenCLIP 特征提取服务"""

    def __init__(
        self,
        model_name: str = "ViT-B-32",
        pretrained: str = "laion2b_s34b_b79k",
        device: str = None,
        cache_dir: str = None
    ):
        """
        初始化 OpenCLIP 模型

        Args:
            model_name: 模型名称
            pretrained: 预训练权重
            device: 设备 (cuda/cpu)
            cache_dir: 模型缓存目录
        """
        # 自动选择设备
        if device is None:
            self.device = "cuda" if torch.cuda.is_available() else "cpu"
        else:
            self.device = device

        # 设置缓存目录
        if cache_dir is None:
            cache_dir = os.getenv('MODEL_CACHE_DIR', '/app/models_cache')

        # 确保缓存目录存在
        os.makedirs(cache_dir, exist_ok=True)

        logger.info(f"📦 加载 OpenCLIP 模型: {model_name} ({pretrained}) on {self.device}")
        logger.info(f"📂 模型缓存目录: {cache_dir}")

        try:
            # 加载模型，指定缓存目录
            self.model, self.preprocess, self.tokenizer = open_clip.create_model_and_transforms(
                model_name,
                pretrained=pretrained,
                device=self.device,
                cache_dir=cache_dir
            )
        except Exception as e:
            logger.error(f"❌ 模型加载失败: {str(e)}")
            logger.info("🔄 尝试使用默认配置重新加载...")
            try:
                # 尝试不指定缓存目录重新加载
                self.model, self.preprocess, self.tokenizer = open_clip.create_model_and_transforms(
                    model_name,
                    pretrained=pretrained,
                    device=self.device
                )
            except Exception as e2:
                logger.error(f"❌ 模型加载彻底失败: {str(e2)}")
                raise RuntimeError(f"无法加载OpenCLIP模型: {str(e2)}")
        
        # 设置为评估模式
        self.model.eval()
        
        # 动态获取向量维度
        with torch.no_grad():
            dummy_input = torch.randn(1, 3, 224, 224).to(self.device)
            dummy_output = self.model.encode_image(dummy_input)
            self.embedding_dim = dummy_output.shape[-1]
        logger.info(f"✅ 向量维度: {self.embedding_dim}")
    
    def extract_features(self, image: Union[Image.Image, np.ndarray]) -> np.ndarray:
        """
        提取图像特征向量
        
        Args:
            image: PIL Image 或 numpy 数组
        
        Returns:
            归一化的特征向量 (numpy array)
        """
        # 转换为 PIL Image
        if isinstance(image, np.ndarray):
            image = Image.fromarray(image)
        
        # 预处理
        image_tensor = self.preprocess(image).unsqueeze(0).to(self.device)
        
        # 提取特征
        with torch.no_grad():
            image_features = self.model.encode_image(image_tensor)
            # 归一化
            image_features = image_features / image_features.norm(dim=-1, keepdim=True)
        
        # 转换为 numpy
        embedding = image_features.cpu().numpy().flatten()
        
        return embedding
    
    def extract_batch_features(self, images: list) -> np.ndarray:
        """
        批量提取图像特征
        
        Args:
            images: 图片列表
        
        Returns:
            特征向量矩阵 (N x D)
        """
        embeddings = []
        
        for image in images:
            embedding = self.extract_features(image)
            embeddings.append(embedding)
        
        return np.array(embeddings)
    
    def compute_similarity(
        self,
        embedding1: np.ndarray,
        embedding2: np.ndarray
    ) -> float:
        """
        计算两个向量的余弦相似度
        
        Args:
            embedding1: 向量1
            embedding2: 向量2
        
        Returns:
            相似度分数 (0-1)
        """
        # 余弦相似度
        similarity = np.dot(embedding1, embedding2) / (
            np.linalg.norm(embedding1) * np.linalg.norm(embedding2)
        )
        
        # 归一化到 0-1
        similarity = (similarity + 1) / 2
        
        return float(similarity)
    
    def get_embedding_dim(self) -> int:
        """获取向量维度"""
        return self.embedding_dim
