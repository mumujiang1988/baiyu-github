import os

# MinIO config
MINIO_BUCKET_NAME = os.getenv("MINIO_BUCKET_NAME", "product-images")
MINIO_PATH_PREFIX = f"minio://{MINIO_BUCKET_NAME}/"

# Batch limits
MAX_BATCH_INGEST_SIZE = 20
MAX_BATCH_DELETE_SIZE = 50

# Concurrency control (1-10)
_raw_ingest = int(os.getenv("MAX_CONCURRENT_INGEST", "4"))
MAX_CONCURRENT_INGEST = max(1, min(10, _raw_ingest))

_raw_delete = int(os.getenv("MAX_CONCURRENT_DELETE", "2"))
MAX_CONCURRENT_DELETE = max(1, min(10, _raw_delete))

import logging
_logger = logging.getLogger(__name__)
_logger.info(f"并发配置: INGEST={MAX_CONCURRENT_INGEST}, DELETE={MAX_CONCURRENT_DELETE}")

# Search limits
MIN_SEARCH_TOP_K = 1
MAX_SEARCH_TOP_K = 100
DEFAULT_SEARCH_TOP_K = 10

# Rate limits
RATE_LIMIT_IMAGE_SEARCH = "10/minute"
RATE_LIMIT_TEXT_SEARCH = "20/minute"
RATE_LIMIT_REMBG = "5/minute"

# File limits
MAX_IMAGE_SIZE_MB = 10
ALLOWED_IMAGE_TYPES = {'.jpg', '.jpeg', '.png', '.webp', '.gif'}
MAX_IMAGES_PER_PRODUCT = 20

# Pagination
DEFAULT_PAGE_SIZE = 20
MIN_PAGE_SIZE = 1
MAX_PAGE_SIZE = 100

# Cache TTL (seconds)
DATA_CONSISTENCY_CACHE_TTL = 300
STATS_CACHE_TTL = 60

# Retry config
MILVUS_DELETE_MAX_RETRIES = 3
MINIO_DELETE_MAX_RETRIES = 3
RETRY_DELAY_SECONDS = 1

# Logging
SLOW_QUERY_THRESHOLD = 1.0
LOG_RETENTION_DAYS = 30

# Business rules
PRODUCT_CODE_PATTERN = r'^[A-Z0-9_-]+$'
MAX_PRODUCT_NAME_LENGTH = 200
MAX_PRODUCT_SPEC_LENGTH = 500
MAX_PRODUCT_CATEGORY_LENGTH = 100
