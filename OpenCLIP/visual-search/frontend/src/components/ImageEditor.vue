<template>
  <el-dialog
    v-model="dialogVisible"
    title="图片编辑"
    width="900px"
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
      <div class="editor-canvas" @mousedown="startCrop" @mousemove="updateCrop" @mouseup="endCrop" @mouseleave="endCrop">
        <canvas ref="canvasRef" class="canvas"></canvas>
        
        <!-- 裁剪框覆盖层 -->
        <div v-if="activeTool === 'crop' && isCropping" 
             class="crop-overlay"
             :style="cropOverlayStyle"
        >
          <div class="crop-box" :style="cropBoxStyle">
            <div class="crop-handle nw" @mousedown.stop="startResize('nw')"></div>
            <div class="crop-handle n" @mousedown.stop="startResize('n')"></div>
            <div class="crop-handle ne" @mousedown.stop="startResize('ne')"></div>
            <div class="crop-handle w" @mousedown.stop="startResize('w')"></div>
            <div class="crop-handle e" @mousedown.stop="startResize('e')"></div>
            <div class="crop-handle sw" @mousedown.stop="startResize('sw')"></div>
            <div class="crop-handle s" @mousedown.stop="startResize('s')"></div>
            <div class="crop-handle se" @mousedown.stop="startResize('se')"></div>
          </div>
        </div>
      </div>
      
      <!-- 参数调节面板 -->
      <div v-if="activeTool" class="editor-controls">
        <!-- 裁剪控制 -->
        <div v-if="activeTool === 'crop'" class="control-panel">
          <el-alert
            v-if="!isCropping"
            title="请在图片上拖动鼠标选择裁剪区域"
            type="info"
            :closable="false"
            show-icon
            style="margin-bottom: 12px;"
          />
          
          <el-form label-width="80px" size="small">
            <el-form-item label="裁剪比例">
              <el-select v-model="cropRatio" @change="applyCropRatioSelect">
                <el-option label="自由裁剪" value="free" />
                <el-option label="1:1 正方形" value="1" />
                <el-option label="4:3" value="1.333" />
                <el-option label="16:9" value="1.778" />
                <el-option label="3:4" value="0.75" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button 
                type="primary" 
                size="small" 
                @click="confirmCrop"
                :disabled="!isCropping"
              >
                确认裁剪
              </el-button>
              <el-button 
                v-if="isCropping"
                size="small" 
                @click="cancelCrop"
              >
                取消
              </el-button>
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
import { ref, watch, nextTick, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Crop, RefreshLeft, ScaleToOriginal, Sunny, MagicStick, 
  Refresh, Check, Bottom, Picture
} from '@element-plus/icons-vue'

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
const activeTool = ref(null)
const processing = ref(false)
const saving = ref(false)

// 图片状态
const originalImage = ref(null)
const currentImage = ref(null)
const ctx = ref(null)

// 编辑参数
const rotateAngle = ref(0)
const flipH = ref(false)
const flipV = ref(false)
const brightness = ref(100)
const contrast = ref(100)
const saturation = ref(100)
const cropRatio = ref('free')

// 裁剪相关
const isCropping = ref(false)
const cropStart = ref({ x: 0, y: 0 })
const cropEnd = ref({ x: 0, y: 0 })
const isResizing = ref(false)
const resizeHandle = ref('')
const isDragging = ref(false) // 是否正在拖动裁剪框

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
    drawImage()
  } catch (error) {
    ElMessage.error('图片加载失败')
    console.error(error)
  }
}

// 绘制图片到画布
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
    isCropping.value = false
    isDragging.value = false
    isResizing.value = false
    cropStart.value = { x: 0, y: 0 }
    cropEnd.value = { x: 0, y: 0 }
  }
  activeTool.value = tool
}

// 重置参数
const resetParams = () => {
  rotateAngle.value = 0
  flipH.value = false
  flipV.value = false
  brightness.value = 100
  contrast.value = 100
  saturation.value = 100
}

// 重置图片
const resetImage = () => {
  if (originalImage.value) {
    currentImage.value = originalImage.value
    resetParams()
    drawImage()
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

// 裁剪框样式计算
const cropOverlayStyle = computed(() => {
  if (!canvasRef.value) return {}
  const rect = canvasRef.value.getBoundingClientRect()
  return {
    width: rect.width + 'px',
    height: rect.height + 'px'
  }
})

const cropBoxStyle = computed(() => {
  const x = Math.min(cropStart.value.x, cropEnd.value.x)
  const y = Math.min(cropStart.value.y, cropEnd.value.y)
  const width = Math.abs(cropEnd.value.x - cropStart.value.x)
  const height = Math.abs(cropEnd.value.y - cropStart.value.y)
  
  return {
    left: x + 'px',
    top: y + 'px',
    width: width + 'px',
    height: height + 'px'
  }
})

// 开始裁剪或拖动
const startCrop = (e) => {
  if (activeTool.value !== 'crop') return
  
  const rect = e.currentTarget.getBoundingClientRect()
  const x = e.clientX - rect.left
  const y = e.clientY - rect.top
  
  // 如果已经在裁剪状态，检查是否点击了裁剪框内部（拖动）
  if (isCropping.value && !isResizing.value) {
    const cropX = Math.min(cropStart.value.x, cropEnd.value.x)
    const cropY = Math.min(cropStart.value.y, cropEnd.value.y)
    const cropW = Math.abs(cropEnd.value.x - cropStart.value.x)
    const cropH = Math.abs(cropEnd.value.y - cropStart.value.y)
    
    // 判断是否在裁剪框内
    if (x >= cropX && x <= cropX + cropW && y >= cropY && y <= cropY + cropH) {
      isDragging.value = true
      cropStart.value = { x: x - cropX, y: y - cropY } // 保存偏移量
      return
    }
  }
  
  // 开始新的裁剪区域
  if (!isResizing.value) {
    cropStart.value = { x, y }
    cropEnd.value = { x, y }
    isCropping.value = true
  }
}

// 更新裁剪区域
const updateCrop = (e) => {
  if (activeTool.value !== 'crop') return
  
  const rect = e.currentTarget.getBoundingClientRect()
  let x = e.clientX - rect.left
  let y = e.clientY - rect.top
  
  // 限制在画布范围内
  x = Math.max(0, Math.min(x, rect.width))
  y = Math.max(0, Math.min(y, rect.height))
  
  if (isDragging.value) {
    // 拖动裁剪框
    const oldWidth = Math.abs(cropEnd.value.x - cropStart.value.x)
    const oldHeight = Math.abs(cropEnd.value.y - cropStart.value.y)
    const newX = x - cropStart.value.x
    const newY = y - cropStart.value.y
    
    // 确保不超出边界
    const boundedX = Math.max(0, Math.min(newX, rect.width - oldWidth))
    const boundedY = Math.max(0, Math.min(newY, rect.height - oldHeight))
    
    cropStart.value = { x: boundedX, y: boundedY }
    cropEnd.value = { x: boundedX + oldWidth, y: boundedY + oldHeight }
  } else if (isResizing.value) {
    // 调整裁剪框大小
    handleResize(x, y, rect)
  } else if (isCropping.value) {
    // 绘制新裁剪区域
    cropEnd.value = { x, y }
    
    // 应用裁剪比例约束
    if (cropRatio.value !== 'free') {
      applyCropRatio(rect)
    }
  }
}

// 处理调整大小
const handleResize = (x, y, rect) => {
  const handle = resizeHandle.value
  const minSize = 20 // 最小尺寸
  
  switch(handle) {
    case 'nw':
      cropStart.value.x = Math.min(x, cropEnd.value.x - minSize)
      cropStart.value.y = Math.min(y, cropEnd.value.y - minSize)
      break
    case 'n':
      cropStart.value.y = Math.min(y, cropEnd.value.y - minSize)
      break
    case 'ne':
      cropEnd.value.x = Math.max(x, cropStart.value.x + minSize)
      cropStart.value.y = Math.min(y, cropEnd.value.y - minSize)
      break
    case 'w':
      cropStart.value.x = Math.min(x, cropEnd.value.x - minSize)
      break
    case 'e':
      cropEnd.value.x = Math.max(x, cropStart.value.x + minSize)
      break
    case 'sw':
      cropStart.value.x = Math.min(x, cropEnd.value.x - minSize)
      cropEnd.value.y = Math.max(y, cropStart.value.y + minSize)
      break
    case 's':
      cropEnd.value.y = Math.max(y, cropStart.value.y + minSize)
      break
    case 'se':
      cropEnd.value.x = Math.max(x, cropStart.value.x + minSize)
      cropEnd.value.y = Math.max(y, cropStart.value.y + minSize)
      break
  }
  
  // 限制在画布范围内
  cropStart.value.x = Math.max(0, cropStart.value.x)
  cropStart.value.y = Math.max(0, cropStart.value.y)
  cropEnd.value.x = Math.min(rect.width, cropEnd.value.x)
  cropEnd.value.y = Math.min(rect.height, cropEnd.value.y)
}

// 应用裁剪比例
const applyCropRatio = (rect) => {
  const ratio = parseFloat(cropRatio.value)
  if (isNaN(ratio)) return
  
  const width = Math.abs(cropEnd.value.x - cropStart.value.x)
  const height = Math.abs(cropEnd.value.y - cropStart.value.y)
  
  if (width === 0 || height === 0) return
  
  // 根据当前宽度计算应该的高度
  const targetHeight = width / ratio
  const startX = Math.min(cropStart.value.x, cropEnd.value.x)
  const startY = Math.min(cropStart.value.y, cropEnd.value.y)
  
  // 确保不超出画布
  const boundedHeight = Math.min(targetHeight, rect.height - startY)
  
  cropEnd.value.y = startY + boundedHeight
}

// 结束裁剪
const endCrop = () => {
  if (isDragging.value) {
    isDragging.value = false
    // 拖动结束后保持裁剪状态
    return
  }
  
  if (isResizing.value) {
    isResizing.value = false
    resizeHandle.value = ''
    // 调整大小结束后保持裁剪状态
    return
  }
  
  // 如果是新绘制的裁剪区域，保持状态让用户可以确认
  // isCropping 保持不变
}

// 开始调整大小
const startResize = (handle) => {
  isResizing.value = true
  resizeHandle.value = handle
}

// 确认裁剪
const confirmCrop = () => {
  if (!canvasRef.value || !isCropping.value) {
    ElMessage.warning('请先拖动鼠标选择裁剪区域')
    return
  }
  
  const canvas = canvasRef.value
  
  // 计算裁剪区域
  const x = Math.min(cropStart.value.x, cropEnd.value.x)
  const y = Math.min(cropStart.value.y, cropEnd.value.y)
  const width = Math.abs(cropEnd.value.x - cropStart.value.x)
  const height = Math.abs(cropEnd.value.y - cropStart.value.y)
  
  if (width < 10 || height < 10) {
    ElMessage.warning('裁剪区域太小，请重新选择')
    return
  }
  
  // 创建新画布
  const newCanvas = document.createElement('canvas')
  newCanvas.width = width
  newCanvas.height = height
  const newCtx = newCanvas.getContext('2d')
  
  // 复制裁剪区域
  newCtx.drawImage(canvas, x, y, width, height, 0, 0, width, height)
  
  // 更新当前图片
  const img = new Image()
  img.onload = () => {
    currentImage.value = img
    isCropping.value = false
    cropStart.value = { x: 0, y: 0 }
    cropEnd.value = { x: 0, y: 0 }
    drawImage()
    ElMessage.success('裁剪成功')
  }
  img.src = newCanvas.toDataURL('image/png')
}

// 应用裁剪比例（选择器改变时）
const applyCropRatioSelect = () => {
  // 如果已经有裁剪区域，重新应用比例
  if (isCropping.value && canvasRef.value) {
    const rect = canvasRef.value.getBoundingClientRect()
    applyCropRatio(rect)
  }
}

// 取消裁剪
const cancelCrop = () => {
  isCropping.value = false
  isDragging.value = false
  isResizing.value = false
  cropStart.value = { x: 0, y: 0 }
  cropEnd.value = { x: 0, y: 0 }
  ElMessage.info('已取消裁剪')
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
}
</script>

<style scoped>
.image-editor {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.editor-toolbar {
  display: flex;
  align-items: center;
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 6px;
}

.editor-canvas {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
  background-color: #fafafa;
  border: 1px dashed #dcdfe6;
  border-radius: 6px;
  overflow: hidden;
  position: relative;
  cursor: crosshair;
}

.canvas {
  max-width: 100%;
  max-height: 500px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  user-select: none;
}

/* 裁剪覆盖层 */
.crop-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.crop-box {
  position: absolute;
  border: 2px dashed #409EFF;
  background-color: rgba(64, 158, 255, 0.1);
  box-shadow: 0 0 0 9999px rgba(0, 0, 0, 0.5);
  pointer-events: auto;
  cursor: move;
  transition: border-color 0.2s;
}

.crop-box:hover {
  border-color: #66b1ff;
}

.crop-handle {
  position: absolute;
  width: 10px;
  height: 10px;
  background-color: #fff;
  border: 2px solid #409EFF;
  border-radius: 50%;
  z-index: 100;
}

.crop-handle.nw { top: -5px; left: -5px; cursor: nw-resize; }
.crop-handle.n { top: -5px; left: 50%; transform: translateX(-50%); cursor: n-resize; }
.crop-handle.ne { top: -5px; right: -5px; cursor: ne-resize; }
.crop-handle.w { top: 50%; left: -5px; transform: translateY(-50%); cursor: w-resize; }
.crop-handle.e { top: 50%; right: -5px; transform: translateY(-50%); cursor: e-resize; }
.crop-handle.sw { bottom: -5px; left: -5px; cursor: sw-resize; }
.crop-handle.s { bottom: -5px; left: 50%; transform: translateX(-50%); cursor: s-resize; }
.crop-handle.se { bottom: -5px; right: -5px; cursor: se-resize; }

.editor-controls {
  padding: 16px;
  background-color: #f9fafb;
  border-radius: 6px;
  border: 1px solid #e4e7ed;
}

.control-panel {
  max-width: 400px;
  margin: 0 auto;
}

.el-divider--vertical {
  height: 24px;
  margin: 0 12px;
}
</style>
