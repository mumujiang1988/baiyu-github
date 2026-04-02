# 企业产品以图搜系统 - 部署文档

## 目录

1. [部署概述](#部署概述)
2. [环境要求](#环境要求)
3. [快速部署](#快速部署)
4. [Docker部署](#docker部署)
5. [Kubernetes部署](#kubernetes部署)
6. [生产环境配置](#生产环境配置)
7. [监控和日志](#监控和日志)
8. [故障排查](#故障排查)
9. [性能优化](#性能优化)

---

## 部署概述

本系统支持多种部署方式：

- **Docker Compose**：推荐用于开发和测试环境
- **Kubernetes**：推荐用于生产环境
- **手动部署**：用于特殊环境

---

## 环境要求

### 硬件要求

| 组件 | 最低配置 | 推荐配置 |
|------|----------|----------|
| CPU | 4核 | 8核+ |
| 内存 | 8GB | 16GB+ |
| 存储 | 50GB | 100GB+ SSD |
| GPU | 无 | NVIDIA GPU（可选） |

### 软件要求

- Docker 20.10+
- Docker Compose 2.0+
- Kubernetes 1.20+（可选）
- Git

---

## 快速部署

### Windows系统

```bash
# 1. 克隆项目
git clone <repository-url>
cd CLIP_FAISS

# 2. 运行部署脚本
deploy.bat

# 选择 "1. 部署系统"
```

### Linux/Mac系统

```bash
# 1. 克隆项目
git clone <repository-url>
cd CLIP_FAISS

# 2. 添加执行权限
chmod +x deploy.sh

# 3. 运行部署脚本
./deploy.sh deploy
```

### 访问系统

部署完成后，访问以下地址：

- **前端界面**: http://localhost
- **后端API**: http://localhost:8000
- **API文档**: http://localhost:8000/docs
- **Grafana监控**: http://localhost:3000

---

## Docker部署

### 1. 准备配置文件

```bash
# 复制环境变量模板
cp .env.production .env

# 编辑配置文件
vim .env
```

### 2. 构建镜像

```bash
# 构建所有镜像
docker-compose build

# 或单独构建
docker-compose build backend
docker-compose build frontend
```

### 3. 启动服务

```bash
# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

### 4. 服务管理

```bash
# 停止服务
docker-compose stop

# 重启服务
docker-compose restart

# 停止并删除容器
docker-compose down

# 停止并删除所有数据
docker-compose down -v
```

### 5. 数据备份

```bash
# 备份MySQL数据
docker exec product-search-mysql mysqldump -u root -p product_search > backup.sql

# 备份FAISS索引
docker cp product-search-backend:/app/product_index.faiss ./backup/
```

---

## Kubernetes部署

### 1. 创建命名空间

```bash
kubectl apply -f k8s/deployment.yml
```

### 2. 检查部署状态

```bash
kubectl get all -n product-search
```

### 3. 查看日志

```bash
kubectl logs -f deployment/backend -n product-search
```

### 4. 扩容

```bash
# 扩容后端服务
kubectl scale deployment backend --replicas=3 -n product-search
```

### 5. 更新镜像

```bash
kubectl set image deployment/backend backend=product-search-backend:v2 -n product-search
```

---

## 生产环境配置

### 1. 数据库配置

```bash
# 修改.env文件
DB_ROOT_PASSWORD=<strong-password>
DB_USER=product_user
DB_PASSWORD=<strong-password>
```

### 2. 安全配置

```bash
# 生成密钥
python -c "import secrets; print(secrets.token_urlsafe(32))"

# 设置到.env
SECRET_KEY=<generated-key>
```

### 3. HTTPS配置

创建SSL证书：

```bash
# 使用Let's Encrypt
certbot certonly --standalone -d your-domain.com
```

更新Nginx配置：

```nginx
server {
    listen 443 ssl;
    ssl_certificate /path/to/cert.pem;
    ssl_certificate_key /path/to/key.pem;
    # ... 其他配置
}
```

### 4. 性能调优

```bash
# 数据库连接池
DB_POOL_SIZE=30
DB_MAX_OVERFLOW=60

# FAISS保存间隔
FAISS_SAVE_INTERVAL=120

# 日志级别
LOG_LEVEL=WARNING
```

---

## 监控和日志

### Prometheus监控

访问：http://localhost:9090

常用查询：

```promql
# 请求速率
rate(http_requests_total[5m])

# 响应时间
histogram_quantile(0.95, rate(http_request_duration_seconds_bucket[5m]))

# 错误率
rate(http_requests_total{status=~"5.."}[5m])
```

### Grafana可视化

访问：http://localhost:3000

默认账号：admin / admin123456

导入仪表盘：

1. 点击 "+" → "Import"
2. 输入仪表盘ID或上传JSON文件

### 日志管理

```bash
# 查看后端日志
docker-compose logs -f backend

# 查看最近100行
docker-compose logs --tail=100 backend

# 导出日志
docker-compose logs > logs.txt
```

---

## 故障排查

### 常见问题

#### 1. 服务无法启动

```bash
# 检查日志
docker-compose logs backend

# 检查端口占用
netstat -tlnp | grep 8000

# 检查容器状态
docker-compose ps
```

#### 2. 数据库连接失败

```bash
# 检查MySQL状态
docker-compose ps mysql

# 测试连接
docker exec -it product-search-mysql mysql -u root -p

# 检查网络
docker network inspect clp_fass_product-network
```

#### 3. 内存不足

```bash
# 查看资源使用
docker stats

# 增加Docker内存限制
# 修改docker-compose.yml:
deploy:
  resources:
    limits:
      memory: 4G
```

#### 4. CLIP模型加载失败

```bash
# 检查模型缓存
ls -la ~/.cache/huggingface/

# 手动下载模型
python -c "from transformers import CLIPModel; CLIPModel.from_pretrained('openai/clip-vit-base-patch32')"
```

---

## 性能优化

### 1. 使用GPU加速

```bash
# 安装GPU版FAISS
pip uninstall faiss-cpu
pip install faiss-gpu

# 修改Dockerfile
FROM nvidia/cuda:11.8-base
```

### 2. 优化FAISS索引

```python
# 使用IVF索引
nlist = 100
quantizer = faiss.IndexFlatIP(512)
index = faiss.IndexIVFFlat(quantizer, 512, nlist)
index.train(vectors)
```

### 3. 添加Redis缓存

```python
import redis
r = redis.Redis(host='redis', port=6379)

# 缓存热门产品
r.setex(f"product:{id}", 3600, json.dumps(product))
```

### 4. 负载均衡

使用Nginx负载均衡：

```nginx
upstream backend {
    server backend1:8000;
    server backend2:8000;
    server backend3:8000;
}

server {
    location /api {
        proxy_pass http://backend;
    }
}
```

---

## 维护操作

### 定期备份

```bash
# 创建备份脚本
cat > backup.sh << 'EOF'
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
docker exec product-search-mysql mysqldump -u root -p product_search > backup_${DATE}.sql
docker cp product-search-backend:/app/product_index.faiss ./faiss_${DATE}.index
EOF

# 添加到crontab
crontab -e
# 每天凌晨2点备份
0 2 * * * /path/to/backup.sh
```

### 更新系统

```bash
# 拉取最新代码
git pull

# 重新构建
docker-compose build

# 滚动更新
docker-compose up -d --no-deps --build backend
```

### 清理资源

```bash
# 清理未使用的镜像
docker image prune -a

# 清理未使用的容器
docker container prune

# 清理未使用的数据卷
docker volume prune
```

---

## 安全建议

1. **修改默认密码**
   - MySQL root密码
   - Grafana admin密码
   - 应用SECRET_KEY

2. **配置防火墙**
   ```bash
   # 只开放必要端口
   ufw allow 80/tcp
   ufw allow 443/tcp
   ufw enable
   ```

3. **定期更新**
   ```bash
   # 更新基础镜像
   docker-compose pull
   docker-compose up -d
   ```

4. **启用HTTPS**
   - 使用Let's Encrypt证书
   - 配置SSL/TLS

5. **限制资源**
   ```yaml
   deploy:
     resources:
       limits:
         cpus: '2'
         memory: 4G
   ```

---

## 附录

### 端口说明

| 端口 | 服务 | 说明 |
|------|------|------|
| 80 | Nginx | 前端服务 |
| 8000 | FastAPI | 后端API |
| 3306 | MySQL | 数据库 |
| 6379 | Redis | 缓存 |
| 9090 | Prometheus | 监控 |
| 3000 | Grafana | 可视化 |

### 环境变量

| 变量 | 说明 | 默认值 |
|------|------|--------|
| DB_HOST | 数据库地址 | localhost |
| DB_PORT | 数据库端口 | 3306 |
| DB_USER | 数据库用户 | root |
| DB_PASSWORD | 数据库密码 | - |
| SECRET_KEY | 应用密钥 | - |
| DEBUG | 调试模式 | false |
| LOG_LEVEL | 日志级别 | INFO |

---

**部署文档版本**: v1.0.0
**最后更新**: 2024-01-01
