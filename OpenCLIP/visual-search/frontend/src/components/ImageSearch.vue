<template>
  <div class="image-search">
    <el-card class="main-card">
      <el-row :gutter="20">
        <!-- 左侧：检索区域 -->
        <el-col :span="6">
          <div class="search-section">
            <div class="section-header">
              <span class="section-title">图像检索</span>
            </div>
            
            <!-- 上传区域 -->
            <div class="upload-area">
              <el-upload
                ref="uploadRef"
                class="upload-dragger"
                drag
                action="#"
                :auto-upload="false"
                :show-file-list="false"
                :on-change="handleFileChange"
                accept="image/*"
              >
                <div v-if="!previewImage" class="upload-placeholder">
                  <el-icon class="upload-icon"><UploadFilled /></el-icon>
                  <div class="upload-text">
                    <span>拖拽或<em>点击</em></span>
                  </div>
                </div>
                <div v-else class="preview-container">
                  <img :src="previewImage" class="preview-image" />
                  <div class="preview-actions">
                    <el-button type="primary" size="small" @click.stop="openEditor">
                      <el-icon><Edit /></el-icon>
                      编辑
                    </el-button>
                    <el-button type="success" size="small" @click.stop="handleSearch" :loading="searching">
                      <el-icon><Search /></el-icon>
                      检索
                    </el-button>
                    <el-button size="small" @click.stop="clearImage">
                      <el-icon><Delete /></el-icon>
                    </el-button>
                  </div>
                </div>
              </el-upload>
            </div>
            
            <!-- 检索配置 -->
            <div class="search-config">
              <el-form label-width="70px" size="small">
                <el-form-item label="返回数量">
                  <el-input-number v-model="topK" :min="1" :max="50" style="width: 100%" />
                </el-form-item>
                <el-form-item label="聚合策略">
                  <el-select v-model="aggregation" style="width: 100%">
                    <el-option label="最大相似度" value="max" />
                    <el-option label="平均相似度" value="avg" />
                  </el-select>
                </el-form-item>
              </el-form>
            </div>
          </div>
        </el-col>
        
        <!-- 右侧：结果区域 -->
        <el-col :span="18">
          <div class="result-section">
            <div class="section-header">
              <span class="section-title">检索结果</span>
              <el-tag v-if="searchResults.length > 0" type="success">{{ searchResults.length }} 个，{{ searchTime }}ms</el-tag>
            </div>
        
            <div v-if="searchResults.length > 0" class="result-list">
              <div 
                v-for="(result, index) in searchResults" 
                :key="result.product_code" 
                class="product-card"
              >
                <!-- 左侧：产品信息 -->
                <div class="product-info-section">
                  <!-- 排名徽章 -->
                  <div class="rank-badge">
                    <span :class="['rank-number', index === 0 ? 'rank-first' : (index < 3 ? 'rank-top3' : '')]">
                      {{ index + 1 }}
                    </span>
                  </div>
                  
                  <!-- 产品编码和名称并排 -->
                  <div class="product-info-row">
                    <div class="product-code">{{ result.product_code }}</div>
                    <div class="product-name">{{ result.product_name || '未命名产品' }}</div>
                  </div>
                  
                  <!-- 匹配度 -->
                  <div class="similarity-section">
                    <div class="similarity-header">
                      <span class="similarity-label">匹配度</span>
                      <span class="similarity-percent" :style="{ color: getSimilarityColor(result.similarity) }">
                        {{ Math.round(result.similarity * 100) }}%
                      </span>
                    </div>
                    <el-progress
                      :percentage="Math.round(result.similarity * 100)"
                      :color="getSimilarityColor(result.similarity)"
                      :stroke-width="8"
                      :show-text="false"
                    />
                  </div>
                </div>
                
                <!-- 右侧：图片轮播 -->
                <div class="image-carousel-wrapper">
                  <button 
                    v-if="result.image_paths.length > 1"
                    class="carousel-btn carousel-left" 
                    @click="scrollCarousel(index, -1)"
                  >
                    <el-icon><ArrowLeft /></el-icon>
                  </button>
                  
                  <div class="image-carousel" :ref="el => setCarouselRef(el, index)">
                    <img 
                      v-for="(imgPath, imgIndex) in result.image_paths" 
                      :key="imgIndex"
                      :src="getImageUrl(imgPath)"
                      class="carousel-image"
                      @click="previewImages(result.image_paths, imgIndex)"
                    />
                  </div>
                  
                  <button 
                    v-if="result.image_paths.length > 1"
                    class="carousel-btn carousel-right" 
                    @click="scrollCarousel(index, 1)"
                  >
                    <el-icon><ArrowRight /></el-icon>
                  </button>
                </div>
              </div>
            </div>
            
            <!-- 空状态提示 -->
            <div v-else class="empty-state">
              <el-empty description="请上传图片开始检索" :image-size="120" />
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>
    
    <!-- 图片预览器 -->
    <el-image-viewer
      v-if="showImageViewer"
      :url-list="currentImageList"
      :initial-index="currentImageIndex"
      @close="showImageViewer = false"
    />
    
    <!-- 图片编辑器 -->
    <ImageEditor
      v-model="showImageEditor"
      :image-url="previewImage"
      @save="handleImageSaved"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled, Search, Delete, ArrowLeft, ArrowRight, Edit } from '@element-plus/icons-vue'
import { searchImage } from '../api/search'
import { showSuccess, handleApiError } from '../utils/messageHandler'
import { ElImageViewer } from 'element-plus'
import ImageEditor from './ImageEditor.vue'

// 数据
const previewImage = ref('')
const selectedFile = ref(null)
const searching = ref(false)
const searchResults = ref([])
const searchTime = ref(0)
const topK = ref(10)
const aggregation = ref('max')
const carouselRefs = ref({})
const showImageViewer = ref(false)
const currentImageList = ref([])
const currentImageIndex = ref(0)
const showImageEditor = ref(false)

// 处理文件选择
const handleFileChange = (file) => {
  selectedFile.value = file.raw
  
  // 预览图片
  const reader = new FileReader()
  reader.onload = (e) => {
    previewImage.value = e.target.result
  }
  reader.readAsDataURL(file.raw)
}

// 清除图片
const clearImage = () => {
  previewImage.value = ''
  selectedFile.value = null
  searchResults.value = []
}

// 打开图片编辑器
const openEditor = () => {
  if (!previewImage.value) {
    ElMessage.warning('请先选择图片')
    return
  }
  showImageEditor.value = true
}

// 处理图片保存
const handleImageSaved = (editedFile) => {
  // 更新选中的文件为编辑后的文件
  selectedFile.value = editedFile
  
  // 更新预览图
  const reader = new FileReader()
  reader.onload = (e) => {
    previewImage.value = e.target.result
  }
  reader.readAsDataURL(editedFile)
  
  ElMessage.success('图片已更新，可以开始检索')
}

// 执行检索
const handleSearch = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择图片')
    return
  }
  
  searching.value = true
  
  try {
    const response = await searchImage(selectedFile.value, topK.value, aggregation.value)
    
    if (response.success) {
      searchResults.value = response.results
      searchTime.value = response.search_time_ms
      showSuccess(response.message)
    } else {
      handleApiError(response, '检索失败')
    }
  } catch (error) {
    handleApiError(error.response || error, '检索失败')
  } finally {
    searching.value = false
  }
}



// 获取图片URL
const getImageUrl = (path) => {
  return `/api/v1/images/${path}`
}

// 相似度颜色
const getSimilarityColor = (similarity) => {
  if (similarity >= 0.8) return '#67C23A'
  if (similarity >= 0.6) return '#E6A23C'
  return '#F56C6C'
}

// 设置轮播引用
const setCarouselRef = (el, index) => {
  if (el) {
    carouselRefs.value[index] = el
  }
}

// 滚动轮播
const scrollCarousel = (index, direction) => {
  const carousel = carouselRefs.value[index]
  if (carousel) {
    const scrollAmount = 180
    carousel.scrollLeft += direction * scrollAmount
  }
}

// 预览图片
const previewImages = (imagePaths, currentIndex) => {
  currentImageList.value = imagePaths.map(path => getImageUrl(path))
  currentImageIndex.value = currentIndex
  showImageViewer.value = true
}

// 粘贴事件处理
const handlePaste = (e) => {
  const items = e.clipboardData.items
  for (const item of items) {
    if (item.type.indexOf('image') !== -1) {
      const file = item.getAsFile()
      selectedFile.value = file
      
      const reader = new FileReader()
      reader.onload = (e) => {
        previewImage.value = e.target.result
      }
      reader.readAsDataURL(file)
      
      ElMessage.success('图片已粘贴')
      break
    }
  }
}

// 挂载粘贴事件
onMounted(() => {
  document.addEventListener('paste', handlePaste)
})

onUnmounted(() => {
  document.removeEventListener('paste', handlePaste)
})
</script>

<style scoped>
.image-search {
  width: 100%;
  height: 100%;  /* 占满父容器 */
  margin: 0 auto;
  display: flex;
  flex-direction: column;
}

.main-card {
  flex: 1;  /* 自动填充剩余空间 */
  min-height: 0;  /* 允许收缩 */
  overflow: hidden;  /* 防止溢出 */
  display: flex;
  flex-direction: column;
}

.search-section,
.result-section {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 15px;
  margin-bottom: 15px;
  border-bottom: 1px solid #EBEEF5;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.upload-area {
  margin-bottom: 15px;
}

.upload-dragger {
  width: 100%;
}

.upload-placeholder {
  padding: 30px 0;
  text-align: center;
}

.upload-icon {
  font-size: 40px;
  color: #c0c4cc;
  margin-bottom: 8px;
}

.upload-text {
  color: #606266;
  font-size: 13px;
}

.upload-text em {
  color: #409EFF;
  font-style: normal;
}

.preview-container {
  text-align: center;
  padding: 10px;
}

.preview-image {
  max-width: 200px;
  max-height: 200px;
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.preview-actions {
  margin-top: 10px;
  display: flex;
  gap: 8px;
  justify-content: center;
}

.search-config {
  padding: 10px 0;
  border-top: 1px solid #EBEEF5;
}

.result-card {
  margin-top: 20px;
}

.result-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  flex: 1;  /* 使用 flex 而不是固定高度 */
  overflow-y: auto;
  min-height: 0;  /* 允许收缩 */
}

.product-card {
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 24px;
  transition: all 0.3s;
}

.product-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  border-color: #c0c4cc;
}

/* 左侧产品信息 */
.product-info-section {
  flex-shrink: 0;
  width: 180px;
  display: flex;
  flex-direction: column;
  gap: 0;
  position: relative;
  padding-top: 24px;
  padding-left: 8px;
}

.rank-badge {
  position: absolute;
  top: -20px;
  left: 0px;
  z-index: 10;
}

.rank-number {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  background-color: rgba(0, 0, 0, 0.6);
  color: #fff;
  font-size: 16px;
  font-weight: 700;
  border-radius: 50%;
  backdrop-filter: blur(4px);
}

.rank-first {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a6f 100%);
  box-shadow: 0 2px 12px rgba(255, 107, 107, 0.5);
}

.rank-top3 {
  background: linear-gradient(135deg, #ffa502 0%, #ff7f50 100%);
  box-shadow: 0 2px 12px rgba(255, 165, 2, 0.5);
}

/* 产品信息行 */
.product-info-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 8px;
  margin-bottom: 8px;
}

.product-code {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
  flex-shrink: 0;
}

.product-name {
  font-size: 14px;
  color: #303133;
  font-weight: 600;
  line-height: 1.4;
  word-break: break-word;
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.similarity-section {
  margin-top: 4px;
}

.similarity-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.similarity-label {
  font-size: 13px;
  color: #606266;
  font-weight: 500;
}

.similarity-percent {
  font-size: 16px;
  font-weight: 700;
}

/* 右侧图片轮播 */
.image-carousel-wrapper {
  flex: 1;
  position: relative;
  padding: 8px 44px;
  min-height: 200px;
  overflow: hidden;
  display: flex;
  align-items: center;
}

.image-carousel {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  scroll-behavior: smooth;
  scrollbar-width: none;
  -ms-overflow-style: none;
  padding: 8px 4px;
  width: 100%;
}

.image-carousel::-webkit-scrollbar {
  display: none;
}

.carousel-image {
  width: 120px;
  height: 120px;
  object-fit: cover;
  border-radius: 6px;
  flex-shrink: 0;
  cursor: pointer;
  transition: all 0.3s;
  border: 2px solid transparent;
}

.carousel-image:hover {
  transform: scale(1.05);
  border-color: #409EFF;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.carousel-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 36px;
  height: 36px;
  background: rgba(0, 0, 0, 0.5);
  color: white;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  transition: all 0.3s;
}

.carousel-btn:hover {
  background: rgba(0, 0, 0, 0.7);
  transform: translateY(-50%) scale(1.1);
}

.carousel-left {
  left: 8px;
}

.carousel-right {
  right: 8px;
}



.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}


</style>
