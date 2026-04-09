<template>
  <div class="text-search">
    <el-card class="search-card">
      <!-- 搜索输入区 -->
      <div class="search-input-section">
        <el-input
          v-model="keyword"
          placeholder="输入产品名称、规格或编码进行搜索"
          size="large"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
          <template #append>
            <el-button type="primary" @click="handleSearch" :loading="searching">
              搜索
            </el-button>
          </template>
        </el-input>
        
        <!-- 高级选项 -->
        <el-collapse-transition>
          <div v-show="showAdvanced" class="advanced-options">
            <el-form :inline="true" size="small">
              <el-form-item label="分类筛选">
                <el-select 
                  v-model="category" 
                  placeholder="全部分类" 
                  clearable
                  style="width: 150px"
                >
                  <el-option label="服装" value="服装" />
                  <el-option label="电子产品" value="电子产品" />
                  <el-option label="家居用品" value="家居用品" />
                  <el-option label="其他" value="其他" />
                </el-select>
              </el-form-item>
              <el-form-item label="返回数量">
                <el-input-number 
                  v-model="topK" 
                  :min="1" 
                  :max="50" 
                  style="width: 120px"
                />
              </el-form-item>
            </el-form>
          </div>
        </el-collapse-transition>
        
        <div class="search-actions">
          <el-button text @click="showAdvanced = !showAdvanced">
            <el-icon><ArrowDown v-if="!showAdvanced" /><ArrowUp v-else /></el-icon>
            {{ showAdvanced ? '收起' : '高级选项' }}
          </el-button>
        </div>
      </div>
      
      <!-- 搜索结果 -->
      <div v-if="searchResults.length > 0" class="results-section">
        <div class="results-header">
          <span class="results-title">找到 {{ searchResults.length }} 个产品</span>
          <el-tag type="success">{{ searchTime }}ms</el-tag>
        </div>
        
        <el-row :gutter="16" class="results-grid">
          <el-col 
            v-for="(result, index) in searchResults" 
            :key="result.product_code"
            :xs="24" 
            :sm="12" 
            :md="8" 
            :lg="6"
          >
            <el-card class="product-card" shadow="hover">
              <!-- 排名徽章 -->
              <div class="rank-badge">
                <span :class="['rank-number', index === 0 ? 'rank-first' : (index < 3 ? 'rank-top3' : '')]">
                  {{ index + 1 }}
                </span>
              </div>
              
              <!-- 产品图片 -->
              <div class="product-image-container">
                <img 
                  v-if="result.image_paths && result.image_paths.length > 0"
                  :src="getImageUrl(result.image_paths[0])"
                  class="product-image"
                  @error="handleImageError"
                />
                <div v-else class="no-image">
                  <el-icon><Picture /></el-icon>
                  <span>无图片</span>
                </div>
              </div>
              
              <!-- 产品信息 -->
              <div class="product-info">
                <div class="product-code">{{ result.product_code }}</div>
                <div class="product-name">{{ result.product_name || '未命名产品' }}</div>
                
                <div v-if="result.spec" class="product-spec">
                  <el-tag size="small" type="info">{{ result.spec }}</el-tag>
                </div>
                
                <div v-if="result.category" class="product-category">
                  <el-tag size="small">{{ result.category }}</el-tag>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
      
      <!-- 空状态 -->
      <el-empty 
        v-else-if="hasSearched" 
        description="未找到匹配的产品"
        :image-size="120"
      >
        <el-button type="primary" @click="clearSearch">
          重新搜索
        </el-button>
      </el-empty>
      
      <!-- 初始提示 -->
      <el-empty 
        v-else
        description="输入关键词开始搜索"
        :image-size="100"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, ArrowDown, ArrowUp, Picture } from '@element-plus/icons-vue'
import { searchByText } from '../api/search'
import { handleApiError } from '../utils/messageHandler'

const keyword = ref('')
const category = ref('')
const topK = ref(10)
const searching = ref(false)
const showAdvanced = ref(false)
const searchResults = ref([])
const searchTime = ref(0)
const hasSearched = ref(false)

// 执行搜索
const handleSearch = async () => {
  if (!keyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  
  searching.value = true
  hasSearched.value = true
  
  try {
    const startTime = Date.now()
    const response = await searchByText(keyword.value, category.value, topK.value)
    
    searchTime.value = Date.now() - startTime
    
    if (response.success) {
      searchResults.value = response.results || []
      ElMessage.success(response.message)
    } else {
      ElMessage.error(response.message || '搜索失败')
      searchResults.value = []
    }
  } catch (error) {
    handleApiError(error.response || error, '搜索失败')
    searchResults.value = []
  } finally {
    searching.value = false
  }
}

// 清空搜索
const clearSearch = () => {
  keyword.value = ''
  category.value = ''
  searchResults.value = []
  hasSearched.value = false
  searchTime.value = 0
}

// 获取图片URL
const getImageUrl = (imagePath) => {
  if (!imagePath) return ''
  return `/api/v1/images/${imagePath}`
}

// 图片加载错误处理
const handleImageError = (event) => {
  event.target.style.display = 'none'
}
</script>

<style scoped>
.text-search {
  height: 100%;
}

.search-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.search-input-section {
  margin-bottom: 20px;
}

.advanced-options {
  margin-top: 16px;
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.search-actions {
  margin-top: 12px;
  text-align: right;
}

.results-section {
  flex: 1;
  overflow-y: auto;
}

.results-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #e4e7ed;
}

.results-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.results-grid {
  margin-top: 16px;
}

.product-card {
  position: relative;
  margin-bottom: 16px;
  transition: all 0.3s;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.rank-badge {
  position: absolute;
  top: -8px;
  left: -8px;
  z-index: 10;
}

.rank-number {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-weight: bold;
  font-size: 14px;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.4);
}

.rank-first {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  box-shadow: 0 2px 8px rgba(245, 87, 108, 0.4);
}

.rank-top3 {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  box-shadow: 0 2px 8px rgba(79, 172, 254, 0.4);
}

.product-image-container {
  width: 100%;
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 12px;
}

.product-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.no-image {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #909399;
}

.no-image .el-icon {
  font-size: 48px;
  margin-bottom: 8px;
}

.product-info {
  padding: 8px 0;
}

.product-code {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
  font-family: monospace;
}

.product-name {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-spec,
.product-category {
  margin-top: 6px;
}
</style>
