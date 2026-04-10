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
            <SearchConfig 
              v-model:topK="topK" 
              v-model:aggregation="aggregation"
            />
            
            <!-- 分隔线 -->
            <el-divider content-position="center">或</el-divider>
            
            <!-- 文本搜索区域 -->
            <TextSearch
              v-model:keyword="textKeyword"
              :loading="textSearching"
              @search="handleTextSearch"
            />
          </div>
        </el-col>
        
        <!-- 右侧：结果区域 -->
        <el-col :span="18">
          <div class="result-section">
            <div class="section-header">
              <span class="section-title">{{ searchMode === 'image' ? '图像检索结果' : '文本搜索结果' }}</span>
              <el-tag v-if="searchResults.length > 0" type="success">{{ searchResults.length }} 个，{{ searchTime }}ms</el-tag>
            </div>
        
            <!-- 使用子组件展示结果 -->
            <SearchResults
              :results="searchResults"
              :has-searched="hasSearched"
              :search-mode="searchMode"
              @scroll="handleCarouselScroll"
              @preview="handleImagePreview"
              @clear="clearImage"
              @set-carousel-ref="handleSetCarouselRef"
            />
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
/**
 * 图像检索主组件
 * 
 * 职责：
 * - 管理搜索状态和配置
 * - 协调子组件交互
 * - 处理文件上传和图片编辑
 */
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage, ElImageViewer } from 'element-plus'
import { UploadFilled, Search, Delete, Edit } from '@element-plus/icons-vue'
import { searchImage, searchByText } from '../api/search'
import { showSuccess, handleApiError } from '../utils/messageHandler'
import { getImageUrl } from '../utils/imageHelper'
import { CAROUSEL_SCROLL_STEP, TEXT_SEARCH } from '../constants/search'
import ImageEditor from './ImageEditor.vue'

// 导入子组件
import SearchConfig from './ImageSearch/SearchConfig.vue'
import TextSearch from './ImageSearch/TextSearch.vue'
import SearchResults from './ImageSearch/SearchResults.vue'

// 数据
const previewImage = ref('')
const selectedFile = ref(null)
const searching = ref(false)
const searchResults = ref([])
const searchTime = ref(0)
const topK = ref(10)
const aggregation = ref('max')
const showImageViewer = ref(false)
const currentImageList = ref([])
const currentImageIndex = ref(0)
const showImageEditor = ref(false)

// 文本搜索相关
const textKeyword = ref('')
const textSearching = ref(false)
const searchMode = ref('image') // 'image' | 'text'
const hasSearched = ref(false)  // 是否已执行过搜索

// 轮播引用管理
const carouselRefs = ref({})

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
  hasSearched.value = false  // 重置搜索状态
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
  searchMode.value = 'image'  // 设置为图像检索模式
  hasSearched.value = true  // 标记已搜索
  
  try {
    const startTime = Date.now()
    const response = await searchImage(selectedFile.value, topK.value, aggregation.value)
    
    searchTime.value = Date.now() - startTime
    
    if (response.success) {
      searchResults.value = response.results || []
      showSuccess(response.message)
    } else {
      handleApiError(response, '检索失败')
      searchResults.value = []
    }
  } catch (error) {
    handleApiError(error.response || error, '检索失败')
    searchResults.value = []
  } finally {
    searching.value = false
  }
}

// 执行文本搜索
const handleTextSearch = async () => {
  const keyword = textKeyword.value.trim()
  
  // 前端验证
  if (!keyword) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  
  if (keyword.length > TEXT_SEARCH.MAX_KEYWORD_LENGTH) {
    ElMessage.warning(`关键词过长，请缩短至${TEXT_SEARCH.MAX_KEYWORD_LENGTH}字符以内`)
    return
  }
  
  // 过滤危险字符（简单防护）
  const sanitizedKeyword = keyword.replace(TEXT_SEARCH.DANGEROUS_CHARS, '')
  
  textSearching.value = true
  searchMode.value = 'text'
  hasSearched.value = true
  
  try {
    const startTime = Date.now()
    const response = await searchByText(sanitizedKeyword, '', topK.value)
    
    searchTime.value = Date.now() - startTime
    
    if (response.success) {
      searchResults.value = response.results || []
      showSuccess(response.message)
    } else {
      handleApiError(response, '搜索失败')
      searchResults.value = []
    }
  } catch (error) {
    handleApiError(error.response || error, '搜索失败')
    searchResults.value = []
  } finally {
    textSearching.value = false
  }
}

// 处理轮播滚动（由子组件事件触发）
const handleCarouselScroll = ({ index, direction }) => {
  const carousel = carouselRefs.value[index]
  if (carousel) {
    carousel.scrollLeft += direction * CAROUSEL_SCROLL_STEP
  }
}

// 设置轮播引用（由子组件事件触发）
const handleSetCarouselRef = ({ el, index }) => {
  if (el) {
    carouselRefs.value[index] = el
  } else {
    // 清理无效引用，防止内存泄漏
    delete carouselRefs.value[index]
  }
}

// 监听搜索结果变化，清空旧的轮播引用
watch(searchResults, () => {
  carouselRefs.value = {}
})

// 处理图片预览（由子组件事件触发）
const handleImagePreview = ({ imagePaths, currentIndex }) => {
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

.text-search-section {
  margin-top: 15px;
}

.text-search-section .section-header {
  margin-bottom: 12px;
}
</style>
