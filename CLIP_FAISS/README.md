# 企业产品以图搜系统

基于 **CLIP + FAISS + FastAPI + Vue3 + MySQL** 的生产级以图搜产品全栈方案。

## 项目简介

企业业务员专用系统：上传产品图片 → 系统秒级匹配企业产品库 → 返回精准产品信息（名称、型号、价格、库存等），支持百万级产品库毫秒级检索。

## 核心技术栈

- **CLIP**: 图像-文本跨模态特征提取（512维向量）
- **FAISS**: 百万级向量毫秒级检索引擎
- **FastAPI**: 高性能异步Web框架
- **Vue3**: 现代化前端框架
- **MySQL**: 产品数据持久化存储

## 项目结构

```
CLIP_FAISS/
├── backend/                 # 后端服务
│   ├── main.py             # FastAPI主服务
│   ├── clip_model.py       # CLIP模型封装
│   ├── faiss_service.py    # FAISS向量服务
│   ├── mysql_db.py         # 数据库连接
│   ├── requirements.txt    # Python依赖
│   ├── .env.example        # 环境变量示例
│   └── uploads/            # 上传图片存储
├── frontend/               # 前端应用
│   ├── src/
│   │   ├── App.vue        # 主页面组件
│   │   └── main.js        # 入口文件
│   ├── package.json       # 前端依赖
│   └── vite.config.js     # Vite配置
├── database/              # 数据库脚本
│   └── init.sql           # 初始化SQL
└── README.md              # 项目文档
```

## 快速开始

### 1. 环境准备

#### 后端环境（Python 3.8+）

```bash
# 进入后端目录
cd backend

# 创建虚拟环境
python -m venv venv

# 激活虚拟环境
# Windows:
venv\Scripts\activate
# Linux/Mac:
source venv/bin/activate

# 安装依赖
pip install -r requirements.txt
```

#### 前端环境（Node.js 16+）

```bash
# 进入前端目录
cd frontend

# 安装依赖
npm install
```

### 2. 数据库配置

#### 创建MySQL数据库

```bash
# 登录MySQL
mysql -u root -p

# 执行初始化脚本
source database/init.sql
```

#### 配置数据库连接

```bash
# 复制环境变量示例
cd backend
cp .env.example .env

# 编辑.env文件，修改数据库配置
# DB_HOST=localhost
# DB_PORT=3306
# DB_USER=root
# DB_PASSWORD=your_password
# DB_NAME=product_search
```

### 3. 启动服务

#### 启动后端服务

```bash
cd backend
python main.py
```

后端服务运行在：http://localhost:8000

API文档：http://localhost:8000/docs

#### 启动前端服务

```bash
cd frontend
npm run dev
```

前端服务运行在：http://localhost:5173

### 4. 添加测试产品

使用API文档或Postman调用添加产品接口：

```
POST http://localhost:8000/api/add-product
```

参数：
- name: 产品名称
- model: 产品型号
- price: 产品价格
- stock: 库存数量
- file: 产品图片

### 5. 测试搜索功能

访问前端页面 http://localhost:5173，上传产品图片进行搜索。

## API接口文档

### 核心接口

#### 1. 以图搜图

```
POST /api/search-by-image
```

**参数：**
- file: 产品图片（必填）
- top_k: 返回结果数量（默认5）

**返回示例：**
```json
{
  "code": 200,
  "message": "搜索成功",
  "data": [
    {
      "id": 1,
      "name": "红色连衣裙",
      "model": "RD-001",
      "price": 299.00,
      "stock": 100,
      "image": "uploads/product_20240101_120000.jpg",
      "similarity": 95.32
    }
  ]
}
```

#### 2. 以文搜图

```
GET /api/search-by-text
```

**参数：**
- text: 产品描述（必填）
- top_k: 返回结果数量（默认5）

#### 3. 添加产品

```
POST /api/add-product
```

**参数：**
- name: 产品名称（必填）
- model: 产品型号
- price: 产品价格
- stock: 库存数量
- file: 产品图片（必填）

#### 4. 获取产品列表

```
GET /api/products
```

**参数：**
- skip: 跳过记录数（默认0）
- limit: 返回记录数（默认100）

#### 5. 获取产品详情

```
GET /api/product/{product_id}
```

#### 6. 更新产品信息

```
PUT /api/product/{product_id}
```

#### 7. 删除产品

```
DELETE /api/product/{product_id}
```

#### 8. 系统信息

```
GET /api/system/info
```

## 核心业务流程

### 管理员录入产品

1. 上传产品图片
2. CLIP模型提取512维特征向量
3. 向量存入FAISS索引
4. 产品信息存入MySQL数据库
5. 建立向量索引与产品ID映射关系

### 业务员搜索产品

1. 上传客户提供的产品图片
2. CLIP模型提取查询向量
3. FAISS毫秒级检索相似向量
4. 根据向量索引查询MySQL产品信息
5. 返回按相似度排序的产品列表

## 性能优化建议

### 1. GPU加速

安装GPU版FAISS：

```bash
pip uninstall faiss-cpu
pip install faiss-gpu
```

### 2. 模型优化

- 使用更大的CLIP模型（如clip-vit-large-patch14）
- 使用模型量化减少内存占用

### 3. 索引优化

- 使用FAISS的IVF索引加速检索
- 使用PQ压缩减少内存占用

```python
# IVF索引示例
nlist = 100  # 聚类中心数量
quantizer = faiss.IndexFlatIP(512)
index = faiss.IndexIVFFlat(quantizer, 512, nlist)
```

### 4. 数据库优化

- 为faiss_index字段建立索引
- 使用连接池优化数据库连接
- 考虑使用Redis缓存热门产品

## 生产部署

### Docker部署（推荐）

创建 `docker-compose.yml`：

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: your_password
      MYSQL_DATABASE: product_search
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./database/init.sql:/docker-entrypoint-initdb.d/init.sql

  backend:
    build: ./backend
    ports:
      - "8000:8000"
    depends_on:
      - mysql
    volumes:
      - ./backend/uploads:/app/uploads

  frontend:
    build: ./frontend
    ports:
      - "80:80"
    depends_on:
      - backend

volumes:
  mysql_data:
```

启动：

```bash
docker-compose up -d
```

### Nginx反向代理

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 前端
    location / {
        root /path/to/frontend/dist;
        try_files $uri $uri/ /index.html;
    }

    # 后端API
    location /api {
        proxy_pass http://localhost:8000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 常见问题

### 1. CLIP模型下载慢？

使用国内镜像：

```python
model = CLIPModel.from_pretrained(
    "openai/clip-vit-base-patch32",
    cache_dir="./models"
)
```

或设置环境变量：

```bash
export HF_ENDPOINT=https://hf-mirror.com
```

### 2. FAISS索引损坏？

删除索引文件重新构建：

```bash
rm product_index.faiss
```

### 3. 数据库连接失败？

检查：
- MySQL服务是否启动
- .env配置是否正确
- 防火墙是否开放3306端口

### 4. 图片上传失败？

检查：
- uploads目录权限
- 图片大小是否超过限制
- 磁盘空间是否充足

## 技术支持

- CLIP模型：https://github.com/openai/CLIP
- FAISS文档：https://faiss.ai/
- FastAPI文档：https://fastapi.tiangolo.com/
- Vue3文档：https://cn.vuejs.org/

## 许可证

MIT License

## 更新日志

### v1.0.0 (2024-01-01)

- 初始版本发布
- 支持以图搜图和以文搜图
- 支持产品CRUD管理
- 支持百万级向量检索
