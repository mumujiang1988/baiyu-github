<template>
  <div class="app-container">
    <!-- 头部 -->
    <header class="header">
      <h1>企业产品以图搜系统</h1>
      <p class="subtitle">基于CLIP+FAISS的智能产品检索平台</p>
    </header>

    <!-- 主要内容区 -->
    <main class="main-content">
      <!-- 搜索方式选择 -->
      <el-tabs v-model="activeTab" class="search-tabs">
        <!-- 以图搜图 -->
        <el-tab-pane label="以图搜图" name="image">
          <div class="search-section">
            <el-upload
              class="upload-area"
              drag
              action="/api/search-by-image"
              :show-file-list="false"
              :on-success="handleSearchSuccess"
              :on-error="handleUploadError"
              :before-upload="beforeUpload"
              accept="image/*"
              :data="{ top_k: topK }"
            >
              <el-icon class="el-icon--upload"><upload-filled /></el-icon>
              <div class="el-upload__text">
                将产品图片拖到此处，或<em>点击上传</em>
              </div>
              <template #tip>
                <div class="el-upload__tip">
                  支持 JPG、PNG、WEBP 格式，文件大小不超过 10MB
                </div>
              </template>
            </el-upload>

            <!-- 搜索参数 -->
            <div class="search-params">
              <span>返回结果数量：</span>
              <el-input-number v-model="topK" :min="1" :max="20" />
            </div>
          </div>
        </el-tab-pane>

        <!-- 以文搜图 -->
        <el-tab-pane label="以文搜图" name="text">
          <div class="search-section">
            <el-input
              v-model="searchText"
              placeholder="请输入产品描述，如：红色连衣裙、黑色皮鞋..."
              size="large"
              clearable
              @keyup.enter="handleTextSearch"
            >
              <template #append>
                <el-button type="primary" @click="handleTextSearch" :loading="searching">
                  <el-icon><search /></el-icon>
                  搜索
                </el-button>
              </template>
            </el-input>

            <div class="search-params">
              <span>返回结果数量：</span>
              <el-input-number v-model="topK" :min="1" :max="20" />
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>

      <!-- 搜索结果 -->
      <div class="result-section" v-if="resultList.length > 0">
        <div class="result-header">
          <h3>搜索结果</h3>
          <el-tag type="success">找到 {{ resultList.length }} 个匹配产品</el-tag>
        </div>

        <div class="product-grid">
          <el-card
            v-for="item in resultList"
            :key="item.id"
            class="product-card"
            shadow="hover"
          >
            <div class="card-content">
              <div class="product-image">
                <img :src="getImageUrl(item.image)" alt="产品图" />
              </div>
              <div class="product-info">
                <h4 class="product-name">{{ item.name }}</h4>
                <div class="info-row">
                  <span class="label">型号：</span>
                  <span class="value">{{ item.model || '暂无' }}</span>
                </div>
                <div class="info-row">
                  <span class="label">价格：</span>
                  <span class="price">¥{{ item.price }}</span>
                </div>
                <div class="info-row">
                  <span class="label">库存：</span>
                  <span class="value">{{ item.stock }} 件</span>
                </div>
                <div class="similarity-bar">
                  <span>相似度</span>
                  <el-progress
                    :percentage="item.similarity"
                    :color="getSimilarityColor(item.similarity)"
                  />
                </div>
              </div>
            </div>
          </el-card>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty
        v-if="searched && resultList.length === 0"
        description="未找到匹配的产品"
      />
    </main>

    <!-- 底部 -->
    <footer class="footer">
      <p>© 2024 企业产品以图搜系统 | 技术支持：CLIP + FAISS + FastAPI + Vue3</p>
    </footer>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { UploadFilled, Search } from '@element-plus/icons-vue'

// 状态变量
const activeTab = ref('image')
const searchText = ref('')
const topK = ref(5)
const resultList = ref([])
const searching = ref(false)
const searched = ref(false)

// API基础URL
const API_BASE_URL = 'http://localhost:8000'

// 图片上传前校验
const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('图片大小不能超过 10MB！')
    return false
  }

  searching.value = true
  return true
}

// 图片搜索成功回调
const handleSearchSuccess = (response) => {
  searching.value = false
  searched.value = true

  if (response.code === 200) {
    resultList.value = response.data
    ElMessage.success(`找到 ${response.data.length} 个匹配产品`)
  } else {
    ElMessage.error(response.message || '搜索失败')
  }
}

// 上传失败回调
const handleUploadError = () => {
  searching.value = false
  ElMessage.error('上传失败，请重试')
}

// 文本搜索
const handleTextSearch = async () => {
  if (!searchText.value.trim()) {
    ElMessage.warning('请输入搜索内容')
    return
  }

  searching.value = true
  searched.value = false

  try {
    const response = await axios.get(`${API_BASE_URL}/api/search-by-text`, {
      params: {
        text: searchText.value,
        top_k: topK.value
      }
    })

    searched.value = true

    if (response.data.code === 200) {
      resultList.value = response.data.data
      ElMessage.success(`找到 ${response.data.data.length} 个匹配产品`)
    } else {
      ElMessage.error(response.data.message || '搜索失败')
    }
  } catch (error) {
    ElMessage.error('搜索失败，请检查网络连接')
  } finally {
    searching.value = false
  }
}

// 获取图片URL
const getImageUrl = (imagePath) => {
  if (!imagePath) return ''
  if (imagePath.startsWith('http')) return imagePath
  return `${API_BASE_URL}/${imagePath}`
}

// 根据相似度获取颜色
const getSimilarityColor = (percentage) => {
  if (percentage >= 80) return '#67c23a'
  if (percentage >= 60) return '#e6a23c'
  return '#f56c6c'
}
</script>

<style scoped>
.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.header {
  text-align: center;
  padding: 40px 20px 20px;
  color: white;
}

.header h1 {
  font-size: 36px;
  margin: 0 0 10px 0;
  font-weight: 600;
}

.subtitle {
  font-size: 16px;
  opacity: 0.9;
  margin: 0;
}

.main-content {
  flex: 1;
  max-width: 1400px;
  width: 100%;
  margin: 0 auto;
  padding: 20px;
}

.search-tabs {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.search-section {
  padding: 20px 0;
}

.upload-area {
  width: 100%;
}

.upload-area :deep(.el-upload-dragger) {
  width: 100%;
  height: 200px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.search-params {
  margin-top: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.result-section {
  margin-top: 30px;
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 2px solid #f0f0f0;
}

.result-header h3 {
  margin: 0;
  font-size: 20px;
  color: #333;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.product-card {
  transition: transform 0.3s;
}

.product-card:hover {
  transform: translateY(-5px);
}

.card-content {
  display: flex;
  gap: 20px;
}

.product-image {
  width: 150px;
  height: 150px;
  flex-shrink: 0;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f5f5;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.product-name {
  margin: 0 0 10px 0;
  font-size: 18px;
  color: #333;
  font-weight: 600;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.label {
  color: #666;
  font-size: 14px;
}

.value {
  color: #333;
  font-size: 14px;
}

.price {
  color: #f56c6c;
  font-size: 18px;
  font-weight: 600;
}

.similarity-bar {
  margin-top: 10px;
}

.similarity-bar span {
  display: block;
  margin-bottom: 5px;
  font-size: 12px;
  color: #666;
}

.footer {
  text-align: center;
  padding: 20px;
  color: white;
  opacity: 0.8;
}

.footer p {
  margin: 0;
  font-size: 14px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .product-grid {
    grid-template-columns: 1fr;
  }

  .card-content {
    flex-direction: column;
  }

  .product-image {
    width: 100%;
    height: 200px;
  }
}
</style>
