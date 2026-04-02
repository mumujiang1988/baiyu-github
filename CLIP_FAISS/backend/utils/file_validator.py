"""
文件上传安全验证工具
"""
import os
import uuid
from typing import Tuple
from fastapi import UploadFile, HTTPException
from PIL import Image
import io
from config import settings

class FileValidator:
    """文件验证器"""
    
    ALLOWED_MIME_TYPES = {
        "image/jpeg",
        "image/jpg", 
        "image/png",
        "image/webp"
    }
    
    @staticmethod
    def validate_file(file: UploadFile) -> Tuple[str, str]:
        """
        验证上传文件
        
        Args:
            file: 上传的文件对象
            
        Returns:
            Tuple[str, str]: (文件ID, 文件扩展名)
            
        Raises:
            HTTPException: 文件验证失败
        """
        # 1. 验证文件扩展名
        if not file.filename:
            raise HTTPException(status_code=400, detail="文件名不能为空")
        
        file_extension = os.path.splitext(file.filename)[1].lower().lstrip('.')
        
        if file_extension not in settings.ALLOWED_EXTENSIONS:
            raise HTTPException(
                status_code=400, 
                detail=f"不支持的文件类型，仅支持: {', '.join(settings.ALLOWED_EXTENSIONS)}"
            )
        
        # 2. 验证MIME类型
        if file.content_type not in FileValidator.ALLOWED_MIME_TYPES:
            raise HTTPException(
                status_code=400,
                detail=f"不支持的文件类型: {file.content_type}"
            )
        
        # 3. 生成唯一文件ID
        file_id = str(uuid.uuid4())
        
        return file_id, file_extension
    
    @staticmethod
    async def validate_image_content(file: UploadFile) -> Image.Image:
        """
        验证图片内容（防止恶意文件）
        
        Args:
            file: 上传的文件对象
            
        Returns:
            Image.Image: PIL图片对象
            
        Raises:
            HTTPException: 图片验证失败
        """
        try:
            # 读取文件内容
            content = await file.read()
            
            # 验证文件大小
            if len(content) > settings.MAX_FILE_SIZE:
                raise HTTPException(
                    status_code=400,
                    detail=f"文件大小超过限制（最大 {settings.MAX_FILE_SIZE // 1024 // 1024}MB）"
                )
            
            # 验证是否为有效图片
            image = Image.open(io.BytesIO(content))
            image.verify()  # 验证图片完整性
            
            # 重新打开（verify后会关闭文件）
            image = Image.open(io.BytesIO(content))
            
            # 验证图片尺寸（防止超大图片）
            width, height = image.size
            if width > 10000 or height > 10000:
                raise HTTPException(
                    status_code=400,
                    detail="图片尺寸过大（最大 10000x10000）"
                )
            
            # 转换为RGB模式
            if image.mode != "RGB":
                image = image.convert("RGB")
            
            return image
            
        except HTTPException:
            raise
        except Exception as e:
            raise HTTPException(
                status_code=400,
                detail=f"无效的图片文件: {str(e)}"
            )
    
    @staticmethod
    def generate_safe_filename(file_id: str, extension: str) -> str:
        """
        生成安全的文件名
        
        Args:
            file_id: 文件唯一ID
            extension: 文件扩展名
            
        Returns:
            str: 安全的文件名
        """
        return f"{file_id}.{extension}"
    
    @staticmethod
    def get_save_path(filename: str, subdir: str = "") -> str:
        """
        获取文件保存路径
        
        Args:
            filename: 文件名
            subdir: 子目录（可选）
            
        Returns:
            str: 完整保存路径
        """
        if subdir:
            save_dir = os.path.join(settings.UPLOAD_DIR, subdir)
        else:
            save_dir = settings.UPLOAD_DIR
        
        os.makedirs(save_dir, exist_ok=True)
        return os.path.join(save_dir, filename)
