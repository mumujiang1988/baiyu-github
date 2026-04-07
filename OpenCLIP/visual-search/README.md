# 以图搜品系统

基于 OpenCLIP + Milvus 的企业级图像检索系统，支持私有化部署。

## 技术栈

- **前端**: Vue3 + Vite + Element Plus
- **后端**: FastAPI + Python
- **特征模型**: OpenCLIP (CPU 可运行，无需训练)
- **向量库**: Milvus (轻量版，免复杂运维)
- **业务库**: MySQL
- **部署**: Docker Compose 一键启动

## 功能特性

- ✅ 图像检索：支持拖拽、粘贴、上传三种方式
- ✅ 产品入库：批量上传产品图片，自动提取特征
- ✅ 产品管理：查看、删除产品及图片
- ✅ 相似度聚合：支持 MAX/AVG 两种聚合策略
- ✅ 图片去重：基于 MD5 哈希自动去重
- ✅ 检索日志：记录检索历史，便于分析优化

## 快速开始

### 1. 克隆项目

```bash
git clone <repository-url>
cd visual-search
```

### 2. 启动服务

```bash
# 一键启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

### 3. 访问系统

- 前端界面: http://localhost
- 后端 API: http://localhost:8000
- API 文档: http://localhost:8000/docs

### 4. 停止服务

```bash
docker-compose down
```

## 项目结构

```
visual-search/
├── docker-compose.yml       # Docker 编排配置
├── mysql/                   # MySQL 数据库
│   └── init/
│       └── init.sql         # 初始化脚本
├── backend/                 # FastAPI 后端
│   ├── main.py              # API 入口
│   ├── requirements.txt     # Python 依赖
│   ├── Dockerfile
│   └── services/            # 业务服务
│       ├── clip_service.py      # OpenCLIP 特征提取
│       ├── milvus_service.py    # Milvus 向量检索
│       ├── product_service.py   # MySQL 业务操作
│       └── image_processor.py   # 图片预处理
└── frontend/                # Vue3 前端
    ├── src/
    │   ├── components/
    │   │   ├── ImageSearch.vue    # 图像检索页面
    │   │   ├── ProductIngest.vue  # 产品入库页面
    │   │   └── ProductList.vue    # 产品管理页面
    │   ├── api/
    │   │   └── search.js          # API 请求
    │   ├── App.vue
    │   └── main.js
    ├── package.json
    ├── vite.config.js
    └── Dockerfile
```

## API 接口

### 图像检索

```http
POST /api/v1/search?top_k=10&aggregation=max
Content-Type: multipart/form-data

file: <图片文件>
```

### 产品入库

```http
POST /api/v1/product/ingest
Content-Type: multipart/form-data

product_code: P001
name: 产品名称
spec: 规格
category: 分类
files: <图片文件1>
files: <图片文件2>
...
```

### 产品列表

```http
GET /api/v1/products?category=电子&page=1&page_size=20
```

### 产品详情

```http
GET /api/v1/product/{product_code}
```

### 删除产品

```http
DELETE /api/v1/product/{product_code}
```

### 系统统计

```http
GET /api/v1/stats
```

## 数据库设计

### product 产品表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| product_code | VARCHAR(50) | 产品编码（唯一） |
| name | VARCHAR(255) | 产品名称 |
| spec | VARCHAR(500) | 规格 |
| category | VARCHAR(100) | 分类 |
| status | TINYINT | 状态：1启用 0禁用 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

### product_image 产品图片表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| product_code | VARCHAR(50) | 产品编码 |
| image_path | VARCHAR(255) | 图片路径 |
| image_hash | VARCHAR(64) | MD5 哈希（去重） |
| milvus_id | BIGINT | Milvus 向量 ID |
| image_size | INT | 图片大小 |
| status | TINYINT | 状态 |
| created_at | DATETIME | 创建时间 |

### search_log 检索日志表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| query_image_hash | VARCHAR(64) | 查询图片哈希 |
| top_product_code | VARCHAR(50) | 最相似产品编码 |
| similarity_score | FLOAT | 相似度分数 |
| search_time_ms | INT | 检索耗时 |
| result_count | INT | 返回结果数量 |
| created_at | DATETIME | 创建时间 |

## 配置说明

### 环境变量

在 `backend/.env` 文件中配置：

```env
# MySQL 配置
MYSQL_HOST=mysql
MYSQL_PORT=3306
MYSQL_USER=vs_user
MYSQL_PASSWORD=vs_pass123
MYSQL_DB=visual_search

# Milvus 配置
MILVUS_HOST=milvus
MILVUS_PORT=19530

# OpenCLIP 配置
OPENCLIP_MODEL=ViT-B-32
OPENCLIP_PRETRAINED=laion2b_s34b_b79k
```

### OpenCLIP 模型选择

| 模型 | 向量维度 | 性能 | 准确率 |
|------|---------|------|--------|
| ViT-B-32 | 512 | 快 | 中 |
| ViT-B-16 | 512 | 中 | 高 |
| ViT-L-14 | 768 | 慢 | 很高 |

## 性能优化

### 大规模场景（>10万图片）

1. **Milvus 索引优化**
   ```python
   # 使用 IVF_PQ 索引（压缩向量）
   index_params = {
       "metric_type": "COSINE",
       "index_type": "IVF_PQ",
       "params": {"nlist": 1024, "m": 8}
   }
   ```

2. **分区策略**
   ```python
   # 按分类分区
   milvus_service.create_partition(category)
   ```

3. **模型量化**
   ```python
   # 使用 INT8 量化加速推理
   model = torch.quantization.quantize_dynamic(model)
   ```

### 高并发场景

1. **Redis 缓存热门产品向量**
2. **FastAPI 异步处理**
3. **负载均衡 + 多实例部署**

## 常见问题

### 1. Milvus 连接失败

检查 Milvus 服务是否启动：
```bash
docker-compose ps milvus
```

### 2. OpenCLIP 模型下载慢

预下载模型到本地：
```bash
# 在 backend 目录下
python -c "import open_clip; open_clip.create_model_and_transforms('ViT-B-32', pretrained='laion2b_s34b_b79k')"
```

### 3. 图片入库失败

检查图片格式和大小：
- 支持格式：JPG、PNG、BMP、GIF、WEBP
- 最小尺寸：50x50
- 最大尺寸：4096x4096

## 开发指南

### 本地开发

```bash
# 后端
cd backend
pip install -r requirements.txt
uvicorn main:app --reload

# 前端
cd frontend
npm install
npm run dev
```

### 添加新功能

1. 在 `backend/services/` 添加服务模块
2. 在 `backend/main.py` 添加 API 接口
3. 在 `frontend/src/api/` 添加请求函数
4. 在 `frontend/src/components/` 添加页面组件

## 许可证

MIT License
