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
          
          <el-tooltip content="旋转" placement="bottom">
            <el-button 
              :type="activeTool === 'rotate' ? 'primary' : ''" 
              @click="setTool('rotate')"
            >
              <el-icon><RefreshLeft /></el-icon>
            </el-button>
          </el-tooltip>
          
          <el-tooltip content="翻转" placement="bottom">
            <el-button 
              :type="activeTool === 'flip' ? 'primary' : ''" 
              @click="setTool('flip')"
            >
              <el-icon><ScaleToOriginal /></el-icon>
            </el-button>
          </el-tooltip>
          
          <el-tooltip content="调整亮度" placement="bottom">
            <el-button 
              :type="activeTool === 'brightness' ? 'primary' : ''" 
              @click="setTool('brightness')"
            >
              <el-icon><Sunny /></el-icon>
            </el-button>
          </el-tooltip>
          
          <el-tooltip content="去除背景" placement="bottom">
            <el-button 
              :type="activeTool === 'removeBg' ? 'primary' : ''" 
              @click="setTool('removeBg')"
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
            :src="imageUrl" 
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
        
        <!-- 旋转控制 -->
        <div v-if="activeTool === 'rotate'" class="control-panel">
          <el-form label-width="80px" size="small">
            <el-form-item label="旋转角度">
              <el-slider 
                v-model="rotateAngle" 
                :min="-180" 
                :max="180"
                @input="applyRotate"
              />
            </el-form-item>
            <el-form-item>
              <el-space>
                <el-button size="small" @click="rotateImage(-90)">-90°</el-button>
                <el-button size="small" @click="rotateImage(90)">+90°</el-button>
                <el-button size="small" @click="rotateImage(180)">180°</el-button>
              </el-space>
            </el-form-item>
          </el-form>
        </div>
        
        <!-- 翻转控制 -->
        <div v-if="activeTool === 'flip'" class="control-panel">
          <el-space>
            <el-button @click="flipImage('horizontal')">
              <el-icon><ScaleToOriginal /></el-icon>
              水平翻转
            </el-button>
            <el-button @click="flipImage('vertical')">
              <el-icon><Bottom /></el-icon>
              垂直翻转
            </el-button>
          </el-space>
        </div>
        
        <!-- 亮度调节 -->
        <div v-if="activeTool === 'brightness'" class="control-panel">
          <el-form label-width="80px" size="small">
            <el-form-item label="亮度">
              <el-slider 
                v-model="brightness" 
                :min="0" 
                :max="200"
                @input="applyFilters"
              />
            </el-form-item>
            <el-form-item label="对比度">
              <el-slider 
                v-model="contrast" 
                :min="0" 
                :max="200"
                @input="applyFilters"
              />
            </el-form-item>
            <el-form-item label="饱和度">
              <el-slider 
                v-model="saturation" 
                :min="0" 
                :max="200"
                @input="applyFilters"
              />
            </el-form-item>
          </el-form>
        </div>
        
        <!-- 去背景提示 -->
        <div v-if="activeTool === 'removeBg'" class="control-panel">
          <el-alert
            title="AI 去背景功能需要后端支持"
            type="info"
            :closable="false"
            show-icon
          >
            <template #default>
              <p>此功能需要配置后端 AI 服务（如 remove.bg API）</p>
              <p>当前版本暂不支持自动去背景</p>
            </template>
          </el-alert>
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
import { 
  Crop, RefreshLeft, ScaleToOriginal, Sunny, MagicStick, 
  Refresh, Check, Bottom
} from '@element-plus/icons-vue'
import Cropper from 'cropperjs'
import 'cropperjs/dist/cropper.css'

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

// 图片状态
const originalImage = ref(null)
const currentImage = ref(null)
const ctx = ref(null)

// Cropper 实例
let cropperInstance = null

// 编辑参数
const rotateAngle = ref(0)
const flipH = ref(false)
const flipV = ref(false)
const brightness = ref(100)
const contrast = ref(100)
const saturation = ref(100)
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

// 加载图片
const loadImage = async () => {
  try {
    const img = new Image()
    img.crossOrigin = 'anonymous'
    img.src = props.imageUrl
    
    await new Promise((resolve, reject) => {
      img.onload = resolve
      img.onerror = reject
    })
    
    originalImage.value = img
    currentImage.value = img
    resetParams()
    
    // 如果当前是裁剪工具，初始化 Cropper
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

// 初始化 Cropper
const initCropper = () => {
  if (!cropperImgRef.value) return
  
  // 销毁旧的 Cropper 实例
  if (cropperInstance) {
    cropperInstance.destroy()
  }
  
  // 创建新的 Cropper 实例
  cropperInstance = new Cropper(cropperImgRef.value, {
    aspectRatio: aspectRatio.value,
    viewMode: 1,  // 限制裁剪框不能超出图片
    dragMode: 'move',  // 可以拖动图片
    autoCropArea: 0.8,  // 自动裁剪区域大小
    restore: false,
    guides: true,  // 显示网格线
    center: true,
    highlight: false,
    cropBoxMovable: true,  // 可以移动裁剪框
    cropBoxResizable: true,  // 可以调整裁剪框大小
    toggleDragModeOnDblclick: false,
  })
}

// 更改裁剪比例
const changeAspectRatio = () => {
  if (cropperInstance) {
    cropperInstance.setAspectRatio(aspectRatio.value)
  }
}

// 重置裁剪框
const resetCropper = () => {
  if (cropperInstance) {
    cropperInstance.reset()
  }
}

// 确认裁剪
const confirmCrop = () => {
  if (!cropperInstance) {
    ElMessage.warning('Cropper 未初始化')
    return
  }
  
  // 获取裁剪后的 Canvas
  const croppedCanvas = cropperInstance.getCroppedCanvas({
    fillColor: '#fff',
  })
  
  if (!croppedCanvas) {
    ElMessage.error('裁剪失败')
    return
  }
  
  // 转换为图片
  const img = new Image()
  img.onload = () => {
    currentImage.value = img
    
    // 重置所有编辑参数
    rotateAngle.value = 0
    flipH.value = false
    flipV.value = false
    brightness.value = 100
    contrast.value = 100
    saturation.value = 100
    
    // 销毁 Cropper
    if (cropperInstance) {
      cropperInstance.destroy()
      cropperInstance = null
    }
    
    // 先切换到 Canvas 模式
    activeTool.value = null
    
    // 等待 DOM 更新后再绘制
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

// 绘制图片到画布（非裁剪模式）
const drawImage = () => {
  if (!canvasRef.value || !currentImage.value) return
  
  const canvas = canvasRef.value
  const ctx = canvas.getContext('2d')
  
  // 设置画布尺寸
  const maxWidth = 800
  const maxHeight = 500
  let width = currentImage.value.width
  let height = currentImage.value.height
  
  // 缩放以适应画布
  if (width > maxWidth || height > maxHeight) {
    const ratio = Math.min(maxWidth / width, maxHeight / height)
    width *= ratio
    height *= ratio
  }
  
  canvas.width = width
  canvas.height = height
  
  // 清除画布
  ctx.clearRect(0, 0, canvas.width, canvas.height)
  
  // 应用变换
  ctx.save()
  ctx.translate(canvas.width / 2, canvas.height / 2)
  ctx.rotate((rotateAngle.value * Math.PI) / 180)
  ctx.scale(flipH.value ? -1 : 1, flipV.value ? -1 : 1)
  ctx.translate(-canvas.width / 2, -canvas.height / 2)
  
  // 应用滤镜
  ctx.filter = `brightness(${brightness.value}%) contrast(${contrast.value}%) saturate(${saturation.value}%)`
  
  // 绘制图片
  ctx.drawImage(currentImage.value, 0, 0, canvas.width, canvas.height)
  ctx.restore()
}

// 设置工具
const setTool = (tool) => {
  // 切换工具时清除裁剪状态
  if (activeTool.value !== tool) {
    // 销毁 Cropper
    if (cropperInstance) {
      cropperInstance.destroy()
      cropperInstance = null
    }
  }
  
  activeTool.value = tool
  
  // 如果切换到裁剪工具，初始化 Cropper
  if (tool === 'crop') {
    nextTick(() => {
      initCropper()
    })
  } else {
    // 其他工具使用 Canvas
    drawImage()
  }
}

// 重置参数
const resetParams = () => {
  rotateAngle.value = 0
  flipH.value = false
  flipV.value = false
  brightness.value = 100
  contrast.value = 100
  saturation.value = 100
  aspectRatio.value = NaN
}

// 重置图片
const resetImage = () => {
  if (originalImage.value) {
    currentImage.value = originalImage.value
    resetParams()
    
    // 销毁 Cropper
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

// 应用旋转
const applyRotate = () => {
  drawImage()
}

// 旋转图片
const rotateImage = (angle) => {
  rotateAngle.value += angle
  drawImage()
}

// 翻转图片
const flipImage = (direction) => {
  if (direction === 'horizontal') {
    flipH.value = !flipH.value
  } else {
    flipV.value = !flipV.value
  }
  drawImage()
}

// 应用滤镜
const applyFilters = () => {
  drawImage()
}

// 保存图片
const saveImage = async () => {
  if (!canvasRef.value) return
  
  saving.value = true
  
  try {
    const canvas = canvasRef.value
    const dataUrl = canvas.toDataURL('image/jpeg', 0.9)
    
    // 转换为 Blob
    const response = await fetch(dataUrl)
    const blob = await response.blob()
    const file = new File([blob], 'edited-image.jpg', { type: 'image/jpeg' })
    
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

// 关闭对话框
const handleClose = () => {
  dialogVisible.value = false
  activeTool.value = null
  
  // 销毁 Cropper
  if (cropperInstance) {
    cropperInstance.destroy()
    cropperInstance = null
  }
}

// 组件卸载前清理
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

.el-divider--vertical {
  height: 24px;
  margin: 0 12px;
}
</style>
