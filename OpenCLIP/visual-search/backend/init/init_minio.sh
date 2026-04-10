#!/bin/bash
# MinIO 初始化脚本 - 创建存储桶和设置策略

set -e

echo "等待 MinIO 启动..."
sleep 5

# 创建存储桶
mc alias set myminio http://minio:9000 minioadmin minioadmin
mc mb myminio/product-images || true

# 设置公开读取策略
mc anonymous set download myminio/product-images

echo "MinIO 初始化完成"
