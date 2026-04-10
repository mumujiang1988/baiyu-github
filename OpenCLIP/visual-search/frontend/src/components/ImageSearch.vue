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
                <div v-if="!imageSearchState.previewImage" class="upload-placeholder">
                  <el-icon class="upload-icon"><UploadFilled /></el-icon>
                  <div class="upload-text">
                    <span>拖拽或<em>点击</em></span>
                  </div>
                </div>
                <div v-else class="preview-container">
                  <div class="preview-image-wrapper">
                    <img :src="imageSearchState.previewImage" class="preview-image" />
                  </div>
                  <div class="preview-actions">
                    <el-button type="primary" size="small" @click.stop="openEditor">
                      <el-icon><Edit /></el-icon>
                      编辑
                    </el-button>
                    <el-button type="success" size="small" @click.stop="handleSearch" :loading="imageSearchState.searching">
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
              v-model:topK="searchConfig.topK" 
              v-model:aggregation="searchConfig.aggregation"
            />
            
            <!-- 分隔线 -->
            <el-divider content-position="center">或</el-divider>
            
            <!-- 文本搜索区域 -->
            <TextSearch
              v-model:keyword="textSearchState.keyword"
              :loading="textSearchState.searching"
              @search="handleTextSearch"
            />
          </div>
        </el-col>
        
        <!-- 右侧：结果区域 -->
        <el-col :span="18">
          <div class="result-section">
            <div class="section-header">
              <span class="section-title">{{ textSearchState.mode === 'image' ? '图像检索结果' : '文本搜索结果' }}</span>
              <el-tag v-if="imageSearchState.results.length > 0" type="success">{{ imageSearchState.results.length }} 个，{{ imageSearchState.time }}ms</el-tag>
            </div>
        
            <!-- 使用子组件展示结果 -->
            <SearchResults
              :results="imageSearchState.results"
              :has-searched="textSearchState.hasSearched"
              :search-mode="textSearchState.mode"
              :is-loading="imageSearchState.searching || textSearchState.searching"
              @preview="handleImagePreview"
              @clear="clearImage"
            />
          </div>
        </el-col>
      </el-row>
    </el-card>
    
    <!-- 图片预览器 -->
    <el-image-viewer
      v-if="imageViewerState.show"
      :url-list="imageViewerState.imageList"
      :initial-index="imageViewerState.currentIndex"
      @close="imageViewerState.show = false"
    />
    
    <!-- 图片编辑器 -->
    <ImageEditor
      v-model="imageEditorState.show"
      :image-url="imageSearchState.previewImage"
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

// 设置组件名称（用于 keep-alive）
defineOptions({
  name: 'ImageSearch'
})

import { ref, reactive, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage, ElImageViewer } from 'element-plus'
import { UploadFilled, Search, Delete, Edit } from '@element-plus/icons-vue'
import { searchImage, searchByText } from '../api/search'
import { showSuccess, handleApiError } from '../utils/messageHandler'
import { getImageUrl } from '../utils/imageHelper'
import { TEXT_SEARCH } from '../constants/search'
import ImageEditor from './ImageEditor.vue'

// 导入子组件
import SearchConfig from './ImageSearch/SearchConfig.vue'
import TextSearch from './ImageSearch/TextSearch.vue'
import SearchResults from './ImageSearch/SearchResults.vue'

// 响应式状态分组

// 搜索配置
const searchConfig = reactive({
  topK: 10,
  aggregation: 'max'
})

// 图像搜索状态
const imageSearchState = reactive({
  previewImage: '',
  selectedFile: null,
  searching: false,
  results: [],
  time: 0
})

// 文本搜索状态
const textSearchState = reactive({
  keyword: '',
  searching: false,
  mode: 'image',  // 'image' | 'text'
  hasSearched: false
})

// 图片预览器状态
const imageViewerState = reactive({
  show: false,
  imageList: [],
  currentIndex: 0
})

// 图片编辑器状态
const imageEditorState = reactive({
  show: false
})

// 处理文件选择
const handleFileChange = (file) => {
  imageSearchState.selectedFile = file.raw
  
  // 预览图片
  const reader = new FileReader()
  reader.onload = (e) => {
    imageSearchState.previewImage = e.target.result
  }
  reader.readAsDataURL(file.raw)
}

// 清除图片
const clearImage = () => {
  imageSearchState.previewImage = ''
  imageSearchState.selectedFile = null
  imageSearchState.results = []
  textSearchState.hasSearched = false  // 重置搜索状态
}

// 打开图片编辑器
const openEditor = () => {
  if (!imageSearchState.previewImage) {
    ElMessage.warning('请先选择图片')
    return
  }
  imageEditorState.show = true
}

// 处理图片保存
const handleImageSaved = (editedFile) => {
  // 更新选中的文件为编辑后的文件
  imageSearchState.selectedFile = editedFile
  
  // 更新预览图
  const reader = new FileReader()
  reader.onload = (e) => {
    imageSearchState.previewImage = e.target.result
  }
  reader.readAsDataURL(editedFile)
  
  ElMessage.success('图片已更新，可以开始检索')
}

// 执行检索
const handleSearch = async () => {
  if (!imageSearchState.selectedFile) {
    ElMessage.warning('请先选择图片')
    return
  }
  
  imageSearchState.searching = true
  textSearchState.mode = 'image'  // 设置为图像检索模式
  textSearchState.hasSearched = true  // 标记已搜索
  imageSearchState.results = []  // 清空旧结果，显示加载状态
  
  try {
    const startTime = Date.now()
    const response = await searchImage(imageSearchState.selectedFile, searchConfig.topK, searchConfig.aggregation)
    
    imageSearchState.time = Date.now() - startTime
    
    if (response.success) {
      imageSearchState.results = response.results || []
      showSuccess(response.message)
    } else {
      handleApiError(response, '检索失败')
      imageSearchState.results = []
    }
  } catch (error) {
    handleApiError(error.response || error, '检索失败')
    imageSearchState.results = []
  } finally {
    imageSearchState.searching = false
  }
}

// 执行文本搜索
const handleTextSearch = async () => {
  const keyword = textSearchState.keyword.trim()
  
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
  
  textSearchState.searching = true
  textSearchState.mode = 'text'
  textSearchState.hasSearched = true
  imageSearchState.results = []  // 清空旧结果，显示加载状态
  
  try {
    const startTime = Date.now()
    const response = await searchByText(sanitizedKeyword, '', searchConfig.topK)
    
    imageSearchState.time = Date.now() - startTime
    
    if (response.success) {
      imageSearchState.results = response.results || []
      showSuccess(response.message)
    } else {
      handleApiError(response, '搜索失败')
      imageSearchState.results = []
    }
  } catch (error) {
    handleApiError(error.response || error, '搜索失败')
    imageSearchState.results = []
  } finally {
    textSearchState.searching = false
  }
}

// 处理图片预览（由子组件事件触发）
const handleImagePreview = ({ imagePaths, currentIndex }) => {
  imageViewerState.imageList = imagePaths.map(path => getImageUrl(path))
  imageViewerState.currentIndex = currentIndex
  imageViewerState.show = true
}

// 粘贴事件处理
const handlePaste = (e) => {
  const items = e.clipboardData.items
  for (const item of items) {
    if (item.type.indexOf('image') !== -1) {
      const file = item.getAsFile()
      imageSearchState.selectedFile = file
      
      const reader = new FileReader()
      reader.onload = (e) => {
        imageSearchState.previewImage = e.target.result
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
  padding-bottom: 16px;
  margin-bottom: 20px;
  border-bottom: 2px solid #e8eaed;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1E293B;
  letter-spacing: 0.3px;
}

.upload-area {
  margin-bottom: 15px;
}

.upload-dragger {
  width: 100%;
}

/* 自定义上传区域样式 */
:deep(.el-upload-dragger) {
  padding: 0;
  border: 2px dashed #dcdfe6;
  border-radius: 12px;
  background: #fafbfc;
  transition: all 0.3s ease;
}

:deep(.el-upload-dragger:hover) {
  border-color: #165DFF;
  background: #f0f5ff;
}

.upload-placeholder {
  padding: 40px 20px;
  text-align: center;
}

.upload-icon {
  font-size: 48px;
  color: #95a5a6;
  margin-bottom: 12px;
  transition: all 0.3s ease;
}

.upload-dragger:hover .upload-icon {
  color: #165DFF;
  transform: scale(1.1);
}

.upload-text {
  color: #606266;
  font-size: 14px;
}

.upload-text em {
  color: #165DFF;
  font-style: normal;
  font-weight: 500;
}

/* 预览容器 - 紧凑布局 */
.preview-container {
  position: relative;
  padding: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.preview-image-wrapper {
  position: relative;
  width: 100%;
  max-width: 100%;
  aspect-ratio: 1;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f7fa;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
  display: block;
  transition: all 0.3s ease;
}

.preview-image:hover {
  transform: scale(1.02);
}

.preview-actions {
  display: flex;
  gap: 8px;
  justify-content: center;
  width: 100%;
}

.text-search-section {
  margin-top: 15px;
}

.text-search-section .section-header {
  margin-bottom: 12px;
}
</style>
