# Rembg 抠图功能使用指南

## 功能概述

已成功集成 Rembg AI 抠图服务到您的视觉搜索系统中。该功能可以自动移除产品图片的背景，提高图像检索的准确性。

## 架构说明

```
前端 Vue → FastAPI 后端 → Rembg Docker 服务 → 返回透明背景图片
```

### 服务组件

1. **Rembg Docker 容器** (`visual-search-rembg`)
   - 镜像: `danielgatis/rembg`
   - 内部端口: 7000
   - 外部映射端口: 5000
   - 模型缓存: `rembg_models` 卷

2. **FastAPI 后端**
   - API 端点: `POST /api/v1/rembg/remove`
   - 限流: 10次/分钟
   - 服务地址: `http://rembg:5000` (Docker 内部网络)

3. **前端 ImageEditor 组件**
   - 位置: `frontend/src/components/ImageEditor.vue`
   - 功能: 调用后端 API 进行抠图

## 使用方法

### 方法一：通过 ImageEditor 组件（推荐）

1. 在图片编辑对话框中，点击工具栏的"魔法棒"图标（去除背景）
2. 系统会自动调用后端 API 进行抠图
3. 抠图完成后，图片会自动更新为透明背景版本
4. 可以保存编辑后的图片

### 方法二：直接调用 API

```bash
curl -X POST http://localhost:8000/api/v1/rembg/remove \
  -F "file=@your-image.jpg" \
  -o result.png
```

### 方法三：在产品入库时使用

在 `ProductIngest.vue` 组件中上传产品图片时，可以添加一个"一键抠图"按钮：

```vue
<el-button 
  type="success" 
  @click="removeBackgroundForAllImages"
  :loading="processing"
>
  <el-icon><MagicStick /></el-icon>
  一键抠图
</el-button>
```

## 技术实现细节

### 前端调用流程

```javascript
// 1. 将当前图片转换为 Blob
const canvas = document.createElement('canvas')
const ctx = canvas.getContext('2d')
canvas.width = currentImage.value.width
canvas.height = currentImage.value.height
ctx.drawImage(currentImage.value, 0, 0)

const blob = await new Promise(resolve => {
  canvas.toBlob(resolve, 'image/png')
})

// 2. 创建 FormData
const formData = new FormData()
formData.append('file', blob, 'image.png')

// 3. 调用后端 API
const response = await fetch('http://localhost:8000/api/v1/rembg/remove', {
  method: 'POST',
  body: formData
})

// 4. 获取并加载抠图后的图片
const resultBlob = await response.blob()
const resultUrl = URL.createObjectURL(resultBlob)
```

### 后端处理流程

```python
# main.py 中的 API 端点
@app.post("/api/v1/rembg/remove")
async def remove_background(request: Request, file: UploadFile = File(...)):
    # 1. 读取上传的图片
    image_bytes = await file.read()
    
    # 2. 调用 Rembg 服务
    transparent_bytes = rembg_service.remove_background(image_bytes)
    
    # 3. 返回抠图后的图片
    return Response(
        content=transparent_bytes,
        media_type="image/png"
    )
```

## 注意事项

1. **首次启动延迟**: Rembg 服务首次启动时需要下载模型文件（约 170MB），可能需要几分钟时间
   
2. **性能考虑**: 
   - 抠图是 CPU 密集型操作
   - 建议对大图片先进行压缩再抠图
   - 已设置 30 秒超时限制

3. **错误处理**:
   - 如果 Rembg 服务不可用，会显示错误提示
   - 建议在产品入库时添加"是否抠图"选项

4. **图片格式**:
   - 输入: 支持 JPG、PNG 等常见格式
   - 输出: PNG 格式（保留透明通道）

## 验证安装

### 1. 检查 Docker 容器状态

```bash
docker-compose ps
```

应该看到 `visual-search-rembg` 状态为 `Up`

### 2. 测试 Rembg API

```bash
curl http://localhost:5000/
```

应该返回 HTML 页面

### 3. 测试后端集成

```bash
curl http://localhost:8000/health
```

应该返回:
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

### 4. 完整功能测试

1. 访问前端: http://localhost:8080
2. 切换到"产品入库"页面
3. 上传一张产品图片
4. 点击图片进行编辑
5. 点击"魔法棒"图标进行抠图
6. 验证背景是否被成功移除

## 故障排查

### 问题 1: Rembg 服务无法连接

**症状**: 前端显示"抠图失败，请检查后端服务是否正常运行"

**解决方案**:
```bash
# 检查 Rembg 容器日志
docker logs visual-search-rembg

# 重启 Rembg 服务
docker-compose restart rembg
```

### 问题 2: 抠图超时

**症状**: 请求超过 30 秒后失败

**解决方案**:
- 减小图片尺寸（建议不超过 2000x2000 像素）
- 检查服务器 CPU 负载
- 增加超时时间（修改 `rembg_service.py` 中的 `timeout` 参数）

### 问题 3: 内存不足

**症状**: Rembg 容器崩溃或系统变慢

**解决方案**:
```bash
# 限制 Rembg 容器内存
# 在 docker-compose.yml 中添加:
services:
  rembg:
    deploy:
      resources:
        limits:
          memory: 4G
```

## 性能优化建议

1. **图片预处理**: 在抠图前将图片缩放到合适大小（如 1024x1024）
2. **批量处理**: 对于大量图片，考虑使用队列异步处理
3. **缓存机制**: 对相同图片的抠图结果进行缓存
4. **GPU 加速**: 如果需要更高性能，可以使用支持 GPU 的 Rembg 版本

## 下一步改进

1. ✅ 已完成：基础抠图功能集成
2. 🔄 进行中：在产品入库页面添加"一键抠图"按钮
3. 📋 计划中：添加批量抠图功能
4. 📋 计划中：添加抠图前后对比预览
5. 📋 计划中：添加抠图质量选择（快速/标准/高质量）

## 相关文档

- [Rembg 官方文档](https://github.com/danielgatis/rembg)
- [Docker Compose 配置](../docker-compose.yml)
- [后端 Rembg 服务](../backend/services/rembg_service.py)
- [前端 ImageEditor 组件](../frontend/src/components/ImageEditor.vue)
