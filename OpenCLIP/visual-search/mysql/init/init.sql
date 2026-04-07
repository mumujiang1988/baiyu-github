-- 产品表
CREATE TABLE IF NOT EXISTS product (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_code VARCHAR(50) NOT NULL UNIQUE COMMENT '产品编码',
  name VARCHAR(255) NOT NULL COMMENT '产品名称',
  spec VARCHAR(500) COMMENT '规格',
  category VARCHAR(100) COMMENT '分类',
  status TINYINT DEFAULT 1 COMMENT '状态：1启用 0禁用',
  created_at DATETIME DEFAULT NOW(),
  updated_at DATETIME DEFAULT NOW() ON UPDATE NOW(),
  INDEX idx_category (category),
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='产品信息表';

-- 产品图片表
CREATE TABLE IF NOT EXISTS product_image (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_code VARCHAR(50) NOT NULL,
  image_path VARCHAR(255) NOT NULL COMMENT '图片存储路径',
  image_hash VARCHAR(64) COMMENT '图片MD5哈希，用于去重',
  milvus_id BIGINT COMMENT 'Milvus向量ID',
  image_size INT COMMENT '图片大小(字节)',
  width INT COMMENT '图片宽度',
  height INT COMMENT '图片高度',
  status TINYINT DEFAULT 1 COMMENT '状态：1正常 0删除',
  created_at DATETIME DEFAULT NOW(),
  INDEX idx_product_code (product_code),
  INDEX idx_image_hash (image_hash),
  UNIQUE KEY uk_hash (image_hash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='产品图片表';

-- 检索日志表
CREATE TABLE IF NOT EXISTS search_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  query_image_hash VARCHAR(64) COMMENT '查询图片哈希',
  top_product_code VARCHAR(50) COMMENT '最相似产品编码',
  similarity_score FLOAT COMMENT '相似度分数',
  search_time_ms INT COMMENT '检索耗时(毫秒)',
  result_count INT COMMENT '返回结果数量',
  user_ip VARCHAR(50) COMMENT '用户IP',
  created_at DATETIME DEFAULT NOW(),
  INDEX idx_created_at (created_at),
  INDEX idx_product_code (top_product_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='检索日志表';

-- 产品入库日志表
CREATE TABLE IF NOT EXISTS ingest_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_code VARCHAR(50) NOT NULL,
  image_count INT COMMENT '图片数量',
  success_count INT COMMENT '成功数量',
  fail_count INT COMMENT '失败数量',
  ingest_time_ms INT COMMENT '入库耗时(毫秒)',
  error_msg TEXT COMMENT '错误信息',
  created_at DATETIME DEFAULT NOW(),
  INDEX idx_product_code (product_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='入库日志表';
