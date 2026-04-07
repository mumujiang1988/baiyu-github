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

const originalImage = ref(null)
const currentImage = ref(null)
const ctx = ref(null)

let cropperInstance = null

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
  
  const croppedCanvas = cropperInstance.getCroppedCanvas({
    fillColor: '#fff',
  })
  
  if (!croppedCanvas) {
    ElMessage.error('裁剪失败')
    return
  }
  
  const img = new Image()
  img.onload = () => {
    currentImage.value = img
    
    rotateAngle.value = 0
    flipH.value = false
    flipV.value = false
    brightness.value = 100
    contrast.value = 100
    saturation.value = 100
    
    if (cropperInstance) {
      cropperInstance.destroy()
      cropperInstance = null
    }
    
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
  
  if (width > maxWidth || height > maxHeight) {
    const ratio = Math.min(maxWidth / width, maxHeight / height)
    width *= ratio
    height *= ratio
  }
  
  canvas.width = width
  canvas.height = height
  
  ctx.clearRect(0, 0, canvas.width, canvas.height)
  
  ctx.save()
  ctx.translate(canvas.width / 2, canvas.height / 2)
  ctx.rotate((rotateAngle.value * Math.PI) / 180)
  ctx.scale(flipH.value ? -1 : 1, flipV.value ? -1 : 1)
  ctx.translate(-canvas.width / 2, -canvas.height / 2)
  
  ctx.filter = `brightness(${brightness.value}%) contrast(${contrast.value}%) saturate(${saturation.value}%)`
  
  ctx.drawImage(currentImage.value, 0, 0, canvas.width, canvas.height)
  ctx.restore()
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
  rotateAngle.value = 0
  flipH.value = false
  flipV.value = false
  brightness.value = 100
  contrast.value = 100
  saturation.value = 100
  aspectRatio.value = NaN
}

const resetImage = () => {
  if (originalImage.value) {
    currentImage.value = originalImage.value
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

const applyRotate = () => {
  drawImage()
}

const rotateImage = (angle) => {
  rotateAngle.value += angle
  drawImage()
}

const flipImage = (direction) => {
  if (direction === 'horizontal') {
    flipH.value = !flipH.value
  } else {
    flipV.value = !flipV.value
  }
  drawImage()
}

const applyFilters = () => {
  drawImage()
}

const saveImage = async () => {
  if (!canvasRef.value) return
  
  saving.value = true
  
  try {
    const canvas = canvasRef.value
    const dataUrl = canvas.toDataURL('image/jpeg', 0.9)
    
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

const handleClose = () => {
  dialogVisible.value = false
  activeTool.value = null
  
  if (cropperInstance) {
    cropperInstance.destroy()
    cropperInstance = null
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

.el-divider--vertical {
  height: 24px;
  margin: 0 12px;
}
</style>
