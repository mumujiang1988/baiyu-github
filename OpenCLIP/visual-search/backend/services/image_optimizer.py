"""
图片优化服务 - 压缩、缩略图、格式转换
"""
import io
from PIL import Image
from typing import Tuple, Optional
import logging

logger = logging.getLogger(__name__)


class ImageOptimizer:
    """图片优化服务"""
    
    # 缩略图尺寸配置
    THUMBNAIL_SIZES = {
        'small': (100, 100),      # 小缩略图（列表页）
        'medium': (300, 300),     # 中缩略图（详情页）
        'large': (800, 800)       # 大缩略图（预览）
    }
    
    # 压缩质量
    COMPRESSION_QUALITY = 85  # JPEG/WebP 质量 (1-100)
    MAX_IMAGE_SIZE = (4096, 4096)  # 最大尺寸
    
    def compress_image(
        self,
        image_bytes: bytes,
        max_size: Tuple[int, int] = None,
        quality: int = None,
        output_format: str = 'WEBP'
    ) -> bytes:
        """
        压缩图片
        
        Args:
            image_bytes: 原始图片字节
            max_size: 最大尺寸 (宽, 高)
            quality: 压缩质量 (1-100)
            output_format: 输出格式 ('WEBP' 或 'PNG')
            
        Returns:
            压缩后的图片字节
        """
        try:
            if max_size is None:
                max_size = self.MAX_IMAGE_SIZE
            if quality is None:
                quality = self.COMPRESSION_QUALITY
            
            # 打开图片
            image = Image.open(io.BytesIO(image_bytes))
            
            # 检测是否有透明背景
            has_transparency = image.mode in ('RGBA', 'LA') or (
                image.mode == 'P' and 'transparency' in image.info
            )
            
            # 如果输出格式是 WEBP 且有透明背景，保持 RGBA
            # 如果输出格式是 PNG，保持原有模式
            if output_format == 'WEBP' and not has_transparency:
                # 无透明背景：转换为 RGB
                if image.mode in ('RGBA', 'LA', 'P'):
                    background = Image.new('RGB', image.size, (255, 255, 255))
                    if image.mode == 'P':
                        image = image.convert('RGBA')
                    background.paste(image, mask=image.split()[-1] if image.mode == 'RGBA' else None)
                    image = background
            # 如果有透明背景，保持 RGBA 模式不变
            
            # 缩放图片（如果超过最大尺寸）
            if image.width > max_size[0] or image.height > max_size[1]:
                image.thumbnail(max_size, Image.LANCZOS)
                logger.info(f"图片缩放: {image.width}x{image.height}")
            
            # 压缩保存
            output = io.BytesIO()
            if output_format == 'PNG':
                # PNG 格式：保持透明度
                image.save(output, format='PNG', optimize=True)
            else:
                # WebP 格式
                image.save(
                    output,
                    format='WEBP',
                    quality=quality,
                    method=6  # 最高压缩率
                )
            
            compressed_bytes = output.getvalue()
            compression_ratio = (1 - len(compressed_bytes) / len(image_bytes)) * 100
            
            logger.info(
                f"图片压缩完成: {len(image_bytes)} -> {len(compressed_bytes)} bytes "
                f"(格式: {output_format}, 压缩率: {compression_ratio:.1f}%)"
            )
            
            return compressed_bytes
            
        except Exception as e:
            logger.error(f"图片压缩失败: {str(e)}", exc_info=True)
            # 压缩失败返回原图
            return image_bytes
    
    def generate_thumbnails(
        self,
        image_bytes: bytes,
        sizes: dict = None,
        output_format: str = 'WEBP'
    ) -> dict:
        """
        生成多种尺寸的缩略图
        
        Args:
            image_bytes: 原始图片字节
            sizes: 缩略图尺寸配置
            output_format: 输出格式 ('WEBP' 或 'PNG')
            
        Returns:
            {size_name: thumbnail_bytes}
        """
        if sizes is None:
            sizes = self.THUMBNAIL_SIZES
        
        thumbnails = {}
        
        try:
            # 打开图片
            image = Image.open(io.BytesIO(image_bytes))
            
            # 检测是否有透明背景
            has_transparency = image.mode in ('RGBA', 'LA') or (
                image.mode == 'P' and 'transparency' in image.info
            )
            
            # 如果输出格式是 WEBP 且无透明背景，转换为 RGB
            # 如果有透明背景，保持 RGBA
            if output_format == 'WEBP' and not has_transparency:
                if image.mode in ('RGBA', 'LA', 'P'):
                    background = Image.new('RGB', image.size, (255, 255, 255))
                    if image.mode == 'P':
                        image = image.convert('RGBA')
                    background.paste(image, mask=image.split()[-1] if image.mode == 'RGBA' else None)
                    image = background
            # 如果有透明背景或输出 PNG，保持原有模式
            
            # 生成各个尺寸的缩略图
            for size_name, (width, height) in sizes.items():
                thumbnail = image.copy()
                thumbnail.thumbnail((width, height), Image.LANCZOS)
                
                # 保存缩略图
                output = io.BytesIO()
                if output_format == 'PNG':
                    thumbnail.save(output, format='PNG', optimize=True)
                else:
                    thumbnail.save(
                        output,
                        format='WEBP',
                        quality=80,
                        method=6
                    )
                
                thumbnails[size_name] = output.getvalue()
            
            logger.info(f"生成 {len(thumbnails)} 个缩略图 (格式: {output_format})")
            return thumbnails
            
        except Exception as e:
            logger.error(f"生成缩略图失败: {str(e)}", exc_info=True)
            return thumbnails
    
    def convert_to_webp(
        self,
        image_bytes: bytes,
        quality: int = None
    ) -> bytes:
        """
        转换为 WebP 格式
        
        Args:
            image_bytes: 原始图片字节
            quality: 质量 (1-100)
            
        Returns:
            WebP 格式图片字节
        """
        if quality is None:
            quality = self.COMPRESSION_QUALITY
        
        try:
            image = Image.open(io.BytesIO(image_bytes))
            
            # 转换为 RGB
            if image.mode in ('RGBA', 'LA', 'P'):
                background = Image.new('RGB', image.size, (255, 255, 255))
                if image.mode == 'P':
                    image = image.convert('RGBA')
                background.paste(image, mask=image.split()[-1] if image.mode == 'RGBA' else None)
                image = background
            
            # 保存为 WebP
            output = io.BytesIO()
            image.save(output, format='WEBP', quality=quality, method=6)
            
            webp_bytes = output.getvalue()
            
            logger.info(
                f"WebP 转换完成: {len(image_bytes)} -> {len(webp_bytes)} bytes"
            )
            
            return webp_bytes
            
        except Exception as e:
            logger.error(f"WebP 转换失败: {str(e)}", exc_info=True)
            return image_bytes
    
    def optimize_and_save(
        self,
        image_bytes: bytes,
        product_code: str,
        filename: str,
        image_processor,
        generate_thumb: bool = True
    ) -> dict:
        """
        优化并保存图片（主流程）
        
        Args:
            image_bytes: 原始图片
            product_code: 产品编码
            filename: 文件名
            image_processor: ImageProcessor 实例
            generate_thumb: 是否生成缩略图
            
        Returns:
            {
                'main_path': 主图路径,
                'thumbnails': {size_name: path},
                'original_size': 原始大小,
                'compressed_size': 压缩后大小
            }
        """
        result = {
            'main_path': '',
            'thumbnails': {},
            'original_size': len(image_bytes),
            'compressed_size': 0
        }
        
        try:
            # 检测是否有透明背景
            from PIL import Image
            import io
            img = Image.open(io.BytesIO(image_bytes))
            has_transparency = img.mode in ('RGBA', 'LA') or (
                img.mode == 'P' and 'transparency' in img.info
            )
            
            # 1. 压缩图片（有透明背景时不转换格式）
            if has_transparency:
                # 透明背景图片：保持 PNG 格式
                compressed_bytes = self.compress_image(image_bytes, output_format='PNG')
                save_filename = filename if filename.endswith('.png') else filename.rsplit('.', 1)[0] + '.png'
            else:
                # 无透明背景：转换为 WebP（更小）
                compressed_bytes = self.compress_image(image_bytes)
                save_filename = filename.replace('.jpg', '.webp').replace('.png', '.webp')
            
            result['compressed_size'] = len(compressed_bytes)
            
            # 2. 保存主图
            main_path = image_processor.save_image(
                compressed_bytes,
                product_code,
                save_filename
            )
            result['main_path'] = main_path
            
            # 3. 生成缩略图
            if generate_thumb:
                thumbnails = self.generate_thumbnails(compressed_bytes, output_format='PNG' if has_transparency else 'WEBP')
                
                for size_name, thumb_bytes in thumbnails.items():
                    ext = '.png' if has_transparency else '.webp'
                    thumb_filename = f"{filename.rsplit('.', 1)[0]}_{size_name}{ext}"
                    thumb_path = image_processor.save_image(
                        thumb_bytes,
                        product_code,
                        thumb_filename
                    )
                    result['thumbnails'][size_name] = thumb_path
            
            logger.info(
                f"图片优化完成: {product_code}/{filename}, "
                f"格式: {'PNG (透明)' if has_transparency else 'WebP'}, "
                f"压缩率: {(1 - result['compressed_size']/result['original_size'])*100:.1f}%"
            )
            
            return result
            
        except Exception as e:
            logger.error(f"图片优化失败: {str(e)}", exc_info=True)
            # 降级：使用原始流程
            main_path = image_processor.save_image(image_bytes, product_code, filename)
            result['main_path'] = main_path
            result['compressed_size'] = len(image_bytes)
            return result
