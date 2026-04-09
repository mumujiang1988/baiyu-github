"""
错误处理工具类
用于统一处理和格式化错误信息
"""
import re
from typing import Optional, Dict, Any


class ErrorHandler:
    """错误处理器"""

    # 错误码到友好提示的映射
    ERROR_MESSAGES = {
        # MySQL 错误码
        '1062': '数据已存在，请检查是否重复上传',
        '1048': '必填字段不能为空',
        '1054': '数据字段不存在',
        '1146': '数据表不存在',
        '2003': '数据库连接失败',
        '2006': '数据库连接断开',

        # 通用错误
        'duplicate_entry': '图片已存在，系统会自动去重',
        'file_not_found': '文件不存在或已被删除',
        'invalid_image': '图片格式不支持或文件已损坏',
        'image_too_large': '图片文件过大，请压缩后上传',
        'image_too_small': '图片尺寸过小，请上传清晰图片',
        'upload_failed': '文件上传失败，请重试',

        # Milvus 错误
        'milvus_connection': '向量数据库连接失败',
        'milvus_collection': '向量集合创建失败',
        'milvus_insert': '向量数据插入失败',

        # CLIP 模型错误
        'model_load': 'AI模型加载失败',
        'feature_extract': '图片特征提取失败',
    }

    @staticmethod
    def parse_mysql_error(error_msg: str) -> str:
        """
        解析MySQL错误信息

        Args:
            error_msg: 原始错误信息

        Returns:
            友好的错误提示
        """
        # 提取错误码
        error_code_match = re.search(r'(\d{4})\s*\(', error_msg)
        if error_code_match:
            error_code = error_code_match.group(1)
            if error_code in ErrorHandler.ERROR_MESSAGES:
                return ErrorHandler.ERROR_MESSAGES[error_code]

        # 检查重复键错误
        if 'Duplicate entry' in error_msg:
            return ErrorHandler.ERROR_MESSAGES['duplicate_entry']

        # 检查字段不存在错误
        if "Unknown column" in error_msg:
            return ErrorHandler.ERROR_MESSAGES['1054']

        # 检查必填字段错误
        if "doesn't have a default value" in error_msg:
            return ErrorHandler.ERROR_MESSAGES['1048']

        # 默认返回原始错误
        return error_msg

    @staticmethod
    def parse_image_error(error_msg: str, filename: Optional[str] = None) -> str:
        """
        解析图片处理错误

        Args:
            error_msg: 原始错误信息
            filename: 文件名(可选)

        Returns:
            友好的错误提示
        """
        prefix = f"{filename}: " if filename else ""

        # 检查图片格式错误
        if 'cannot identify image file' in error_msg.lower():
            return prefix + ErrorHandler.ERROR_MESSAGES['invalid_image']

        # 检查图片尺寸错误
        if '尺寸过小' in error_msg or 'too small' in error_msg.lower():
            return prefix + ErrorHandler.ERROR_MESSAGES['image_too_small']

        # 检查图片大小错误
        if '尺寸过大' in error_msg or 'too large' in error_msg.lower():
            return prefix + ErrorHandler.ERROR_MESSAGES['image_too_large']

        # 检查不支持的格式
        if '不支持的图片格式' in error_msg or 'not supported' in error_msg.lower():
            return prefix + ErrorHandler.ERROR_MESSAGES['invalid_image']

        return prefix + error_msg

    @staticmethod
    def parse_milvus_error(error_msg: str) -> str:
        """
        解析Milvus错误信息

        Args:
            error_msg: 原始错误信息

        Returns:
            友好的错误提示
        """
        # 检查连接错误
        if 'connect' in error_msg.lower() or 'connection' in error_msg.lower():
            return ErrorHandler.ERROR_MESSAGES['milvus_connection']

        # 检查集合错误
        if 'collection' in error_msg.lower():
            return ErrorHandler.ERROR_MESSAGES['milvus_collection']

        # 检查插入错误
        if 'insert' in error_msg.lower():
            return ErrorHandler.ERROR_MESSAGES['milvus_insert']

        return error_msg

    @staticmethod
    def parse_model_error(error_msg: str) -> str:
        """
        解析模型相关错误

        Args:
            error_msg: 原始错误信息

        Returns:
            友好的错误提示
        """
        # 检查模型加载错误
        if 'load' in error_msg.lower() or '模型' in error_msg:
            return ErrorHandler.ERROR_MESSAGES['model_load']

        # 检查特征提取错误
        if 'extract' in error_msg.lower() or 'feature' in error_msg.lower():
            return ErrorHandler.ERROR_MESSAGES['feature_extract']

        return error_msg

    @staticmethod
    def format_error(
        error: Exception,
        context: Optional[str] = None,
        filename: Optional[str] = None
    ) -> str:
        """
        统一格式化错误信息

        Args:
            error: 异常对象
            context: 错误上下文(可选)
            filename: 文件名(可选)

        Returns:
            格式化的错误信息
        """
        error_msg = str(error)

        # 根据错误类型进行解析
        if 'mysql' in error_msg.lower() or 'duplicate' in error_msg.lower():
            formatted_msg = ErrorHandler.parse_mysql_error(error_msg)
        elif 'image' in error_msg.lower() or 'pil' in error_msg.lower():
            formatted_msg = ErrorHandler.parse_image_error(error_msg, filename)
        elif 'milvus' in error_msg.lower():
            formatted_msg = ErrorHandler.parse_milvus_error(error_msg)
        elif 'clip' in error_msg.lower() or 'model' in error_msg.lower():
            formatted_msg = ErrorHandler.parse_model_error(error_msg)
        else:
            formatted_msg = error_msg

        # 添加上下文信息
        if context:
            return f"{context}: {formatted_msg}"

        return formatted_msg

    @staticmethod
    def get_error_suggestion(error_msg: str) -> Optional[str]:
        """
        根据错误信息提供解决建议

        Args:
            error_msg: 错误信息

        Returns:
            解决建议
        """
        suggestions = {
            'duplicate': '建议: 检查是否已上传过相同的图片，或使用其他图片',
            'invalid_image': '建议: 请确保上传的是有效的图片文件(JPG、PNG、BMP等格式)',
            'image_too_large': '建议: 请压缩图片文件大小后再上传',
            'image_too_small': '建议: 请上传尺寸更大的清晰图片',
            'database': '建议: 请检查数据库连接状态，或联系管理员',
            'model': '建议: 请稍后重试，或联系管理员检查模型服务',
            'milvus': '建议: 请检查向量数据库服务是否正常运行',
            'rembg': '建议: 请检查 Rembg 抠图服务是否已启动且网络通畅',
        }

        if 'duplicate' in error_msg.lower() or '已存在' in error_msg:
            return suggestions['duplicate']
        elif 'invalid' in error_msg.lower() or '格式' in error_msg or 'cannot identify' in error_msg.lower():
            return suggestions['invalid_image']
        elif 'large' in error_msg.lower() or '过大' in error_msg:
            return suggestions['image_too_large']
        elif 'small' in error_msg.lower() or '过小' in error_msg:
            return suggestions['image_too_small']
        elif 'database' in error_msg.lower() or 'mysql' in error_msg.lower():
            return suggestions['database']
        elif 'milvus' in error_msg.lower():
            return suggestions['milvus']
        elif 'rembg' in error_msg.lower() or '抠图' in error_msg:
            return suggestions['rembg']
        elif 'model' in error_msg.lower() or 'clip' in error_msg.lower():
            return suggestions['model']

        return None
