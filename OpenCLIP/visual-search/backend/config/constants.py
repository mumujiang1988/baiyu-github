"""
系统常量定义

集中管理所有魔法数字和配置值，提高代码可维护性。
"""

# ==================== 批量操作限制 ====================

# 批量入库最大产品数
MAX_BATCH_INGEST_SIZE = 50

# 批量删除最大产品数
MAX_BATCH_DELETE_SIZE = 100


# ==================== 搜索限制 ====================

# 搜索返回数量范围
MIN_SEARCH_TOP_K = 1
MAX_SEARCH_TOP_K = 100

# 默认搜索返回数量
DEFAULT_SEARCH_TOP_K = 10


# ==================== 限流配置 ====================

# 图像检索限流
RATE_LIMIT_IMAGE_SEARCH = "10/minute"

# 文本搜索限流
RATE_LIMIT_TEXT_SEARCH = "20/minute"

# 单产品入库限流
RATE_LIMIT_PRODUCT_INGEST = "5/minute"

# 批量入库限流
RATE_LIMIT_BATCH_INGEST = "2/minute"

# 背景移除限流
RATE_LIMIT_REMBG = "5/minute"


# ==================== 文件限制 ====================

# 最大图片文件大小（MB）
MAX_IMAGE_SIZE_MB = 10

# 允许的图片格式
ALLOWED_IMAGE_TYPES = {'.jpg', '.jpeg', '.png', '.webp', '.gif'}

# 单次上传最大图片数
MAX_IMAGES_PER_PRODUCT = 20


# ==================== 分页配置 ====================

# 默认每页数量
DEFAULT_PAGE_SIZE = 20

# 最小每页数量
MIN_PAGE_SIZE = 1

# 最大每页数量
MAX_PAGE_SIZE = 100


# ==================== 缓存配置 ====================

# 数据一致性检查缓存时间（秒）
DATA_CONSISTENCY_CACHE_TTL = 300  # 5分钟

# 统计信息缓存时间（秒）
STATS_CACHE_TTL = 60  # 1分钟


# ==================== 重试配置 ====================

# Milvus删除重试次数
MILVUS_DELETE_MAX_RETRIES = 3

# MinIO删除重试次数
MINIO_DELETE_MAX_RETRIES = 3

# 重试间隔（秒）
RETRY_DELAY_SECONDS = 1


# ==================== 日志配置 ====================

# 慢查询阈值（秒）
SLOW_QUERY_THRESHOLD = 1.0

# 日志保留天数
LOG_RETENTION_DAYS = 30


# ==================== 业务规则 ====================

# 产品编码格式正则
PRODUCT_CODE_PATTERN = r'^[A-Z0-9_-]+$'

# 产品名称最大长度
MAX_PRODUCT_NAME_LENGTH = 200

# 产品规格最大长度
MAX_PRODUCT_SPEC_LENGTH = 500

# 产品分类最大长度
MAX_PRODUCT_CATEGORY_LENGTH = 100
