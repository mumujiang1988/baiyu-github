#!/bin/bash
echo "=== 停止旧容器 ==="
docker compose down -v

echo "=== 清理 Docker 构建缓存 ==="
docker builder prune -a -f

echo "=== 重新构建并启动 ==="
docker compose up -d --build

echo "=== 更新完成！请浏览器强制刷新：Ctrl+Shift+R ==="
