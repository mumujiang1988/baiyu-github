<template>
  <el-dialog
    v-model="dialogVisible"
    title="图片编辑"
    width="700px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div class="image-editor">
      <!-- 工具栏 -->
      <div class="editor-toolbar">
        <el-button-group>
          <el-tooltip content="裁剪" placement="bottom">
            <el-button 
              :type="activeTool === 'crop' ? 'primary' : ''" 
              @click="setTool('crop')"
            >
              <el-icon><Crop /></el-icon>
            </el-button>
          </el-tooltip>
          
          <el-tooltip content="去除背景" placement="bottom">
            <el-button 
              :type="activeTool === 'removeBg' ? 'primary' : ''" 
              @click="handleRemoveBgClick"
              :loading="processing"
            >
              <el-icon><MagicStick /></el-icon>
            </el-button>
          </el-tooltip>
        </el-button-group>
        
        <el-divider direction="vertical" />
        
        <el-button @click="resetImage" size="small">
          <el-icon><Refresh /></el-icon>
          重置
        </el-button>
      </div>
      
      <!-- 画布区域 -->
      <div class="editor-canvas">
        <!-- Cropper.js 容器 -->
        <div v-if="activeTool === 'crop'" class="cropper-container">
          <img 
            ref="cropperImgRef"
            :src="currentImageUrl" 
            alt="要裁剪的图片"
          />
        </div>
        
        <!-- 其他工具的 Canvas -->
        <canvas v-else ref="canvasRef" class="canvas"></canvas>
      </div>
      
      <!-- 参数调节面板 -->
      <div v-if="activeTool" class="editor-controls">
        <!-- 裁剪控制 -->
        <div v-if="activeTool === 'crop'" class="control-panel">
          <el-form size="small">
            <el-form-item>
              <el-space>
                <el-button 
                  type="primary" 
                  size="small" 
                  @click="confirmCrop"
                >
                  确认裁剪
                </el-button>
                <el-button 
                  size="small" 
                  @click="resetCropper"
                >
                  重置裁剪框
                </el-button>
              </el-space>
            </el-form-item>
          </el-form>
        </div>
        
        <!-- 去背景控制面板 -->
        <div v-if="activeTool === 'removeBg'" class="control-panel">
          <div v-if="processing" class="processing-status">
            <el-icon class="is-loading" :size="40"><Loading /></el-icon>
            <p style="margin-top: 15px; font-size: 16px;">正在智能抠图中...</p>
            <p style="margin-top: 8px; font-size: 13px; color: #909399;">AI 模型正在分析图片，请稍候</p>
          </div>
          <div v-else class="result-status">
            <el-result icon="success" title="抠图完成">
              <template #sub-title>
                <p>背景已成功移除，图片保留透明通道</p>
                <p style="margin-top: 8px; font-size: 12px; color: #909399;">您可以继续编辑或保存图片</p>
              </template>
            </el-result>
          </div>
        </div>
      </div>
    </div>
    
    <template #footer>
      <el-space>
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="saveImage" :loading="saving">
          <el-icon><Check /></el-icon>
          保存编辑
        </el-button>
      </el-space>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch, nextTick, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { Crop, MagicStick, Refresh, Check } from '@element-plus/icons-vue'
import Cropper from 'cropperjs'
import 'cropperjs/dist/cropper.css'
import { API_BASE_URL } from '../config/api'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  imageUrl: {
    type: String,
    required: true
  }
})

const emit = defineEmits(['update:modelValue', 'save'])

const dialogVisible = ref(false)
const canvasRef = ref(null)
const cropperImgRef = ref(null)
const activeTool = ref(null)
const processing = ref(false)
const saving = ref(false)

// 当前编辑的图片 URL（抠图后会更新）
const currentImageUrl = ref('')

const originalImage = ref(null)
const currentImage = ref(null)
const ctx = ref(null)

let cropperInstance = null

const aspectRatio = ref(NaN)

watch(() => props.modelValue, (val) => {
  dialogVisible.value = val
  if (val && props.imageUrl) {
    loadImage()
  }
})

watch(dialogVisible, (val) => {
  emit('update:modelValue', val)
})

const loadImage = async () => {
  try {
    // 清理旧的 URL 对象
    if (currentImageUrl.value && currentImageUrl.value.startsWith('blob:')) {
      URL.revokeObjectURL(currentImageUrl.value)
    }
    
    const img = new Image()
    img.crossOrigin = 'anonymous'
    img.src = props.imageUrl
    
    await new Promise((resolve, reject) => {
      img.onload = resolve
      img.onerror = reject
    })
    
    // 初始化当前图片 URL
    currentImageUrl.value = props.imageUrl
    
    originalImage.value = img
    currentImage.value = img
    resetParams()
    
    if (activeTool.value === 'crop') {
      await nextTick()
      initCropper()
    } else {
      drawImage()
    }
  } catch (error) {
    ElMessage.error('图片加载失败')
    console.error(error)
  }
}

const initCropper = () => {
  if (!cropperImgRef.value) return
  
  if (cropperInstance) {
    cropperInstance.destroy()
  }
  
  cropperInstance = new Cropper(cropperImgRef.value, {
    aspectRatio: aspectRatio.value,
    viewMode: 1,
    dragMode: 'move',
    autoCropArea: 0.8,
    restore: false,
    guides: true,
    center: true,
    highlight: false,
    cropBoxMovable: true,
    cropBoxResizable: true,
    toggleDragModeOnDblclick: false,
  })
}

const changeAspectRatio = () => {
  if (cropperInstance) {
    cropperInstance.setAspectRatio(aspectRatio.value)
  }
}

const resetCropper = () => {
  if (cropperInstance) {
    cropperInstance.reset()
  }
}

const confirmCrop = () => {
  if (!cropperInstance) {
    ElMessage.warning('Cropper 未初始化')
    return
  }
  
  // 检测当前图片是否有透明背景
  const hasTransparency = activeTool.value === 'removeBg'
  
  // 获取裁剪区域的数据
  const cropData = cropperInstance.getData()
  
  // 使用原始尺寸进行裁剪，避免缩放导致的失真
  const croppedCanvas = cropperInstance.getCroppedCanvas({
    // 如果有透明背景，使用透明填充；否则使用白色
    fillColor: hasTransparency ? 'transparent' : '#fff',
    imageSmoothingEnabled: true,
    imageSmoothingQuality: 'high',
    // 保持原始尺寸，不缩放
    width: cropData.width,
    height: cropData.height,
  })
  
  if (!croppedCanvas) {
    ElMessage.error('裁剪失败')
    return
  }
  
  const img = new Image()
  img.onload = () => {
    currentImage.value = img
    originalImage.value = img // 更新原始图片引用
    
    // 重置所有参数
    rotateAngle.value = 0
    flipH.value = false
    flipV.value = false
    brightness.value = 100
    contrast.value = 100
    saturation.value = 100
    
    // 销毁 cropper 实例
    if (cropperInstance) {
      cropperInstance.destroy()
      cropperInstance = null
    }
    
    // 关键修复：先取消工具状态，再在 nextTick 中重新绘制
    activeTool.value = null
    
    nextTick(() => {
      drawImage()
      ElMessage.success('裁剪成功')
    })
  }
  img.onerror = () => {
    ElMessage.error('裁剪后图片加载失败')
  }
  img.src = croppedCanvas.toDataURL('image/png')
}

const drawImage = () => {
  if (!canvasRef.value || !currentImage.value) return
  
  const canvas = canvasRef.value
  const ctx = canvas.getContext('2d')
  
  const maxWidth = 800
  const maxHeight = 500
  let width = currentImage.value.width
  let height = currentImage.value.height
  
  // 计算缩放比例，保持宽高比
  if (width > maxWidth || height > maxHeight) {
    const ratio = Math.min(maxWidth / width, maxHeight / height)
    width *= ratio
    height *= ratio
  }
  
  canvas.width = width
  canvas.height = height
  
  // 清空画布，保持透明背景
  ctx.clearRect(0, 0, canvas.width, canvas.height)
  
  // 绘制图片，保持宽高比，居中显示
  const imgRatio = currentImage.value.width / currentImage.value.height
  const canvasRatio = width / height
  
  let drawWidth, drawHeight, offsetX, offsetY
  
  if (imgRatio > canvasRatio) {
    // 图片更宽，以宽度为准
    drawWidth = width
    drawHeight = width / imgRatio
    offsetX = 0
    offsetY = (height - drawHeight) / 2
  } else {
    // 图片更高，以高度为准
    drawHeight = height
    drawWidth = height * imgRatio
    offsetX = (width - drawWidth) / 2
    offsetY = 0
  }
  
  ctx.drawImage(currentImage.value, offsetX, offsetY, drawWidth, drawHeight)
}

const setTool = (tool) => {
  if (activeTool.value !== tool) {
    if (cropperInstance) {
      cropperInstance.destroy()
      cropperInstance = null
    }
  }
  
  activeTool.value = tool
  
  if (tool === 'crop') {
    nextTick(() => {
      initCropper()
    })
  } else {
    drawImage()
  }
}

const resetParams = () => {
  aspectRatio.value = NaN
}

const resetImage = () => {
  if (originalImage.value) {
    currentImage.value = originalImage.value
    
    // 重置为原始图片 URL
    currentImageUrl.value = props.imageUrl
    
    resetParams()
    
    if (cropperInstance) {
      cropperInstance.destroy()
      cropperInstance = null
    }
    
    if (activeTool.value === 'crop') {
      nextTick(() => {
        initCropper()
      })
    } else {
      drawImage()
    }
    
    ElMessage.success('已重置')
  }
}

// 处理魔法棒按钮点击 - 直接开始抠图
const handleRemoveBgClick = async () => {
  if (!currentImage.value) {
    ElMessage.warning('请先加载图片')
    return
  }
  
  // 设置工具状态
  activeTool.value = 'removeBg'
  
  // 立即开始抠图
  await removeBackground()
}

// 调用后端 Rembg 服务去除背景
const removeBackground = async () => {
  if (!currentImage.value) {
    ElMessage.warning('请先加载图片')
    return
  }
  
  processing.value = true
  
  try {
    // 将当前图片转换为 Blob
    const canvas = document.createElement('canvas')
    const ctx = canvas.getContext('2d')
    canvas.width = currentImage.value.width
    canvas.height = currentImage.value.height
    ctx.drawImage(currentImage.value, 0, 0)
    
    const blob = await new Promise(resolve => {
      canvas.toBlob(resolve, 'image/png')
    })
    
    // 创建 FormData
    const formData = new FormData()
    formData.append('file', blob, 'image.png')
    
    // 调用后端 API
    const response = await fetch(`${API_BASE_URL}/api/v1/rembg/remove`, {
      method: 'POST',
      body: formData
    })
    
    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}))
      throw new Error(errorData.message || errorData.detail || '抠图失败')
    }
    
    // 获取抠图后的图片
    const resultBlob = await response.blob()
    const resultUrl = URL.createObjectURL(resultBlob)
    
    // 加载抠图后的图片
    const img = new Image()
    img.crossOrigin = 'anonymous'
    img.onload = () => {
      currentImage.value = img
      originalImage.value = img // 更新原始图片引用
      
      // 更新当前图片 URL（关键修复）
      currentImageUrl.value = resultUrl
      
      // 重置所有参数
      resetParams()
      
      // 销毁 cropper（如果存在）
      if (cropperInstance) {
        cropperInstance.destroy()
        cropperInstance = null
      }
      
      // 取消激活的工具
      activeTool.value = null
      
      // 重新绘制
      nextTick(() => {
        drawImage()
        ElMessage.success('背景移除成功')
      })
      
      // 注意：不在这里清理 URL 对象，因为 currentImageUrl 还在使用它
      // 会在关闭对话框或加载新图片时清理
    }
    img.onerror = () => {
      ElMessage.error('抠图后图片加载失败')
      URL.revokeObjectURL(resultUrl)
    }
    img.src = resultUrl
    
  } catch (error) {
    console.error('抠图失败:', error)
    ElMessage.error(error.message || '抠图失败，请检查后端服务是否正常运行')
  } finally {
    processing.value = false
  }
}

const saveImage = async () => {
  if (!canvasRef.value) return
  
  saving.value = true
  
  try {
    const canvas = canvasRef.value
    const ctx = canvas.getContext('2d')
    
    // 检测画布是否有透明像素
    const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height)
    const data = imageData.data
    let hasTransparency = false
    
    // 检查 alpha 通道（每4个字节中的第4个）
    for (let i = 3; i < data.length; i += 4) {
      if (data[i] < 255) {
        hasTransparency = true
        break
      }
    }
    
    // 如果当前是抠图模式，强制使用 PNG
    if (activeTool.value === 'removeBg') {
      hasTransparency = true
    }
    
    let dataUrl, file
    
    if (hasTransparency) {
      // 使用 PNG 格式保留透明背景
      console.log('检测到透明背景，使用 PNG 格式保存')
      dataUrl = canvas.toDataURL('image/png')
      const response = await fetch(dataUrl)
      const blob = await response.blob()
      file = new File([blob], 'edited-image.png', { type: 'image/png' })
    } else {
      // 使用 JPEG 格式（文件更小）
      console.log('无透明背景，使用 JPEG 格式保存')
      dataUrl = canvas.toDataURL('image/jpeg', 0.9)
      const response = await fetch(dataUrl)
      const blob = await response.blob()
      file = new File([blob], 'edited-image.jpg', { type: 'image/jpeg' })
    }
    
    emit('save', file)
    ElMessage.success('保存成功')
    handleClose()
  } catch (error) {
    ElMessage.error('保存失败')
    console.error(error)
  } finally {
    saving.value = false
  }
}

const handleClose = () => {
  dialogVisible.value = false
  activeTool.value = null
  
  if (cropperInstance) {
    cropperInstance.destroy()
    cropperInstance = null
  }
  
  // 清理 blob URL，防止内存泄漏
  if (currentImageUrl.value && currentImageUrl.value.startsWith('blob:')) {
    URL.revokeObjectURL(currentImageUrl.value)
    currentImageUrl.value = ''
  }
}

onBeforeUnmount(() => {
  if (cropperInstance) {
    cropperInstance.destroy()
    cropperInstance = null
  }
})
</script>

<style scoped>
.image-editor {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.editor-toolbar {
  display: flex;
  align-items: center;
  padding: 8px;
  background-color: #f5f7fa;
  border-radius: 6px;
}

.editor-canvas {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
  max-height: 400px;
  background-color: #fafafa;
  border: 1px dashed #dcdfe6;
  border-radius: 6px;
  overflow: hidden;
  position: relative;
}

.cropper-container {
  width: 100%;
  max-width: 800px;
}

.cropper-container img {
  max-width: 100%;
  display: block;
}

.canvas {
  max-width: 100%;
  max-height: 400px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  user-select: none;
}

.editor-controls {
  padding: 12px;
  background-color: #f9fafb;
  border-radius: 6px;
  border: 1px solid #e4e7ed;
}

.control-panel {
  max-width: 100%;
}

.processing-status,
.result-status {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  text-align: center;
}

.el-divider--vertical {
  height: 24px;
  margin: 0 12px;
}
</style>
