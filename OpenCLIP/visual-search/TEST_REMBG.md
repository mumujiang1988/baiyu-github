# Rembg 抠图功能快速测试指南

## 🚀 快速开始

### 1. 确保服务正在运行

```bash
cd OpenCLIP/visual-search
docker-compose ps
```

应该看到所有服务状态为 `Up`，包括：
- visual-search-rembg
- visual-search-backend
- visual-search-frontend
- visual-search-mysql
- visual-search-milvus

### 2. 验证 Rembg 服务

```bash
# 测试 Rembg API 是否可访问
curl http://localhost:5000/
```

预期输出: HTML 页面内容

### 3. 验证后端集成

```bash
# 检查后端健康状态
curl http://localhost:8000/health
```

预期输出:
```json
{
  "status": "healthy",
  "checks": {
    "mysql": true,
    "milvus": true,
    "clip": true
  }
}
```

## 🧪 功能测试

### 测试 1: 通过 ImageEditor 抠图

**步骤**:
1. 打开浏览器访问: http://localhost:8080
2. 点击"产品入库"菜单
3. 填写产品信息（编码、名称等）
4. 上传一张产品图片（建议有背景的图片）
5. 点击上传后的图片缩略图
6. 在弹出的编辑器中，点击工具栏的"魔法棒"图标 (✨)
7. 等待处理完成（约 2-5 秒）
8. 观察图片背景是否被移除

**预期结果**:
- ✅ 显示"背景移除成功"提示
- ✅ 图片背景变为透明（棋盘格图案）
- ✅ 可以保存编辑后的图片

### 测试 2: 批量一键抠图

**步骤**:
1. 在"产品入库"页面
2. 上传 2-3 张产品图片
3. 点击"一键抠图"按钮
4. 在确认对话框中点击"开始抠图"
5. 观察每张图片的处理进度
6. 查看最终统计结果

**预期结果**:
- ✅ 显示确认对话框
- ✅ 逐个处理图片并显示进度
- ✅ 每张图片处理后显示成功提示
- ✅ 最后显示总结（成功 X 张，失败 Y 张）
- ✅ 预览图自动更新为抠图后的版本

### 测试 3: 直接 API 调用

**准备测试图片**:
```bash
# 如果没有测试图片，可以先下载一个示例
curl -o test-product.jpg https://via.placeholder.com/400x400/FF0000/FFFFFF?text=Product
```

**调用 API**:
```bash
# Windows PowerShell
curl.exe -X POST http://localhost:8000/api/v1/rembg/remove `
  -F "file=@test-product.jpg" `
  -o result.png

# Linux/Mac
curl -X POST http://localhost:8000/api/v1/rembg/remove \
  -F "file=@test-product.jpg" \
  -o result.png
```

**验证结果**:
```bash
# 检查文件是否生成
ls -lh result.png

# 查看文件信息（需要安装 file 命令）
file result.png
```

预期输出: `result.png: PNG image data, ...`

## 🔍 故障诊断

### 问题 1: Rembg 服务未启动

**症状**: 
- `docker-compose ps` 显示 rembg 状态为 Exited
- 前端显示"抠图失败"

**解决**:
```bash
# 查看日志
docker logs visual-search-rembg

# 重启服务
docker-compose restart rembg

# 等待模型下载完成（首次启动可能需要几分钟）
docker logs -f visual-search-rembg
```

### 问题 2: 后端无法连接 Rembg

**症状**:
- 后端日志显示 "Connection refused"
- 健康检查通过但抠图失败

**解决**:
```bash
# 检查后端日志
docker-compose logs backend | Select-String "rembg"

# 验证网络连接
docker exec visual-search-backend ping rembg

# 重启后端
docker-compose restart backend
```

### 问题 3: 抠图超时

**症状**:
- 请求超过 30 秒后失败
- 错误消息: "Rembg 服务响应超时"

**解决**:
1. 减小图片尺寸（建议 < 2000x2000 像素）
2. 检查服务器 CPU 负载
3. 增加超时时间（修改 `backend/services/rembg_service.py`）:
   ```python
   response = requests.post(
       f"{self.api_url}/api/remove",
       files={"file": ("image.png", image_bytes, "image/png")},
       timeout=60  # 增加到 60 秒
   )
   ```

### 问题 4: 内存不足

**症状**:
- Rembg 容器崩溃
- 系统变慢或无响应

**解决**:
```bash
# 限制 Rembg 容器内存
# 编辑 docker-compose.yml
services:
  rembg:
    deploy:
      resources:
        limits:
          memory: 4G

# 重新创建容器
docker-compose up -d rembg
```

## 📊 性能基准测试

### 单张图片处理时间

```powershell
# PowerShell 测试脚本
$startTime = Get-Date
curl.exe -X POST http://localhost:8000/api/v1/rembg/remove `
  -F "file=@test.jpg" `
  -o result.png
$endTime = Get-Date
$duration = ($endTime - $startTime).TotalSeconds
Write-Host "处理时间: $duration 秒"
```

**预期结果**:
- 小图片 (< 500KB): 2-3 秒
- 中等图片 (500KB-2MB): 3-5 秒
- 大图片 (> 2MB): 5-10 秒

### 批量处理测试

上传 5 张图片并使用"一键抠图"功能：

**预期结果**:
- 总处理时间: 10-25 秒
- 成功率: 100%
- 内存占用稳定

## ✅ 验收标准

### 功能验收
- [ ] ImageEditor 中可以成功抠图
- [ ] ProductIngest 批量抠图功能正常
- [ ] API 可以直接调用
- [ ] 错误处理完善（网络错误、超时等）
- [ ] 用户提示友好

### 性能验收
- [ ] 单张图片处理 < 10 秒
- [ ] 批量处理不阻塞 UI
- [ ] 内存占用稳定
- [ ] 支持并发请求（限流保护）

### 用户体验验收
- [ ] 操作简单直观
- [ ] 进度反馈清晰
- [ ] 错误提示明确
- [ ] 预览图实时更新

## 🎯 常见问题 FAQ

**Q: 为什么首次启动很慢？**  
A: Rembg 需要下载 AI 模型文件（约 170MB），后续启动会很快。

**Q: 支持哪些图片格式？**  
A: 输入支持 JPG、PNG、BMP、GIF、WEBP；输出统一为 PNG（保留透明通道）。

**Q: 可以自定义背景颜色吗？**  
A: 当前版本输出透明背景，可以在前端添加背景色选择功能。

**Q: 抠图质量如何？**  
A: 使用 U²Net 模型，对常见商品图片效果很好，复杂场景可能需要手动调整。

**Q: 是否支持 GPU 加速？**  
A: 当前使用 CPU 版本，如需 GPU 加速可使用 `danielgatis/rembg:gpu` 镜像。

**Q: 如何处理大量图片？**  
A: 建议分批处理（每批 10-20 张），避免内存溢出和超时。

## 📝 测试报告模板

```markdown
## 测试日期: YYYY-MM-DD

### 环境信息
- Docker 版本: 
- 操作系统: 
- CPU: 
- 内存: 

### 测试结果

#### 测试 1: ImageEditor 抠图
- [ ] 通过 / [ ] 失败
- 处理时间: ___ 秒
- 备注: 

#### 测试 2: 批量抠图
- [ ] 通过 / [ ] 失败
- 图片数量: ___ 张
- 成功率: ___%
- 总耗时: ___ 秒
- 备注: 

#### 测试 3: API 调用
- [ ] 通过 / [ ] 失败
- 响应时间: ___ 秒
- 输出文件大小: ___ KB
- 备注: 

### 发现的问题
1. 
2. 
3. 

### 建议改进
1. 
2. 
3. 
```

## 🎉 测试完成

如果所有测试都通过，恭喜您！Rembg 抠图功能已成功集成并可以投入使用。

下一步：
1. 开始在产品入库时使用抠图功能
2. 观察检索准确度的提升
3. 根据实际使用情况优化参数
4. 考虑添加更多高级功能

如有问题，请查看：
- [完整使用指南](./REMBG_USAGE.md)
- [集成报告](./REMBG_INTEGRATION_COMPLETE.md)
