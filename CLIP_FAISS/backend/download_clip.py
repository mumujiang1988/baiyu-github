"""
CLIP模型下载脚本
使用国内镜像下载模型
"""
import os
os.environ['HF_ENDPOINT'] = 'https://hf-mirror.com'

import torch
from transformers import CLIPProcessor, CLIPModel
import sys

def download_clip_model():
    """下载CLIP模型"""
    model_name = "openai/clip-vit-base-patch32"
    
    print(f"正在从国内镜像下载CLIP模型: {model_name}")
    print(f"镜像地址: https://hf-mirror.com")
    print("-" * 60)
    
    try:
        # 下载模型
        print("1. 下载模型配置和权重...")
        model = CLIPModel.from_pretrained(model_name)
        print("   ✓ 模型下载成功")
        
        # 下载处理器
        print("2. 下载预处理器配置...")
        processor = CLIPProcessor.from_pretrained(model_name)
        print("   ✓ 预处理器下载成功")
        
        # 测试模型
        print("3. 测试模型加载...")
        device = "cuda" if torch.cuda.is_available() else "cpu"
        model = model.to(device)
        print(f"   ✓ 模型加载成功，使用设备: {device}")
        
        print("-" * 60)
        print("✓ CLIP模型下载完成！")
        print(f"模型缓存位置: {os.path.expanduser('~/.cache/huggingface')}")
        
        return True
        
    except Exception as e:
        print(f"✗ 模型下载失败: {str(e)}")
        return False

if __name__ == "__main__":
    success = download_clip_model()
    sys.exit(0 if success else 1)
