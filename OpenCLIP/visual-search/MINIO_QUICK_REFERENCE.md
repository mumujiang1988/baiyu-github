# MinIO 集成 - 快速参考卡

## 🚀 一键启动
```bash
cd OpenCLIP/visual-search
docker-compose up -d
```

## ✅ 验证状态
```bash
# 健康检查
curl http://localhost:8000/health

# 期望输出
{"status":"healthy","checks":{"mysql":true,"milvus":true,"clip":true,"minio":true}}
```

## 📤 上传图片（自动存 MinIO）
```bash
curl -X POST http://localhost:8000/api/v1/product/ingest \
  -F "product_code=P001" \
  -F "name=产品名" \
  -F "spec=规格" \
  -F "category=分类" \
  -F "files=@image.jpg" \
  -F "remove_bg=false"
```

## 📥 访问图片
```
http://localhost:8000/api/v1/images/{product_code}/{image_hash}.jpg
示例: http://localhost:8000/api/v1/images/P001/abc123def456.jpg
```

## 🔍 运行测试
```bash
cd backend
python test_minio_integration.py
```

## ⚙️ 切换存储方式

### 使用 MinIO（默认）
```yaml
# docker-compose.yml
environment:
  - USE_MINIO=true
```

### 使用本地文件系统
```yaml
# docker-compose.yml
environment:
  - USE_MINIO=false
```

## 📊 MinIO 管理

### 查看存储桶
```bash
docker exec visual-search-backend python -c "
from services.minio_service import MinioService
m = MinioService()
objects = list(m.client.list_objects('product-images', recursive=True))
print(f'对象数量: {len(objects)}')
for obj in objects[:10]:
    print(f'  - {obj.object_name} ({obj.size} bytes)')
"
```

### 删除测试数据
```bash
docker exec visual-search-backend python -c "
from services.minio_service import MinioService
m = MinioService()
m.client.remove_object('product-images', 'TEST_MINIO_001/22226275d88bcc85c98bfb63f1320a42.jpg')
print('删除成功')
"
```

## 🔧 配置项

| 环境变量 | 默认值 | 说明 |
|---------|--------|------|
| MINIO_ENDPOINT | minio:9000 | MinIO 服务端点 |
| MINIO_ACCESS_KEY | minioadmin | 访问密钥 |
| MINIO_SECRET_KEY | minioadmin | 秘密密钥 |
| MINIO_SECURE | false | 是否使用 HTTPS |
| MINIO_BUCKET | product-images | 存储桶名称 |
| USE_MINIO | true | 是否启用 MinIO |

## 🎯 关键特性

- ✅ **高可用**: 支持集群部署
- ✅ **易扩展**: 横向扩展到 PB 级别
- ✅ **S3 兼容**: 易于迁移到云存储
- ✅ **安全性**: 访问密钥认证
- ✅ **CDN 友好**: 缓存控制头

## 📁 存储结构

```
product-images/          # Bucket
├── P001/               # 产品编码
│   ├── abc123.jpg      # 图片哈希.扩展名
│   └── def456.png
├── P002/
│   └── xyz789.jpg
└── ...
```

## 💡 常见问题

**Q: 如何确认图片存储在 MinIO？**
```bash
# 查看后端日志
docker-compose logs backend | grep "图片上传到 MinIO"

# 或直接查询 MinIO
docker exec visual-search-backend python -c "
from services.minio_service import MinioService
m = MinioService()
print(m.image_exists('P001/abc123.jpg'))
"
```

**Q: MinIO 连接失败怎么办？**
```bash
# 检查 MinIO 容器状态
docker-compose ps minio

# 查看 MinIO 日志
docker-compose logs minio

# 重启 MinIO
docker-compose restart minio

# 临时切换到本地存储
# 修改 docker-compose.yml: USE_MINIO=false
docker-compose up -d backend
```

**Q: 如何备份 MinIO 数据？**
```bash
# 使用 mc 工具备份到本地
docker run --rm -v $(pwd)/backup:/backup minio/mc mirror myminio/product-images /backup

# 或复制 Docker 卷
docker run --rm -v visual-search_minio_data:/data -v $(pwd):/backup alpine tar czf /backup/minio_backup.tar.gz -C /data .
```

## 🔗 相关链接

- [完整集成报告](./MINIO_INTEGRATION_COMPLETE.md)
- [MinIO 官方文档](https://docs.min.io/)
- [S3 API 参考](https://docs.aws.amazon.com/AmazonS3/latest/API/Welcome.html)

---

**最后更新**: 2026-04-09
