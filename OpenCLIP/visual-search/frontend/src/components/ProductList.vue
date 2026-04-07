<template>
  <div class="product-list">
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <span>产品管理</span>
          <el-form inline>
            <el-form-item>
              <el-input v-model="searchCategory" placeholder="分类筛选" clearable @clear="loadProducts" @keyup.enter="loadProducts" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadProducts">
                <el-icon><Search /></el-icon>
                查询
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </template>
      
      <el-table :data="products" v-loading="loading" stripe>
        <el-table-column prop="product_code" label="产品编码" width="150" />
        <el-table-column prop="name" label="产品名称" min-width="200" />
        <el-table-column prop="spec" label="规格" width="150" />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="created_at" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.created_at) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-space :size="8">
              <el-button type="primary" size="small" @click="viewProduct(row)" link>
                <el-icon><View /></el-icon>
                查看
              </el-button>
              <el-button type="danger" size="small" @click="deleteProduct(row)" link>
                <el-icon><Delete /></el-icon>
                删除
              </el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>
      
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadProducts"
        @current-change="loadProducts"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>
    
    <!-- 产品详情对话框 -->
    <el-dialog v-model="detailVisible" title="产品详情" width="800px">
      <el-descriptions :column="2" border v-if="currentProduct">
        <el-descriptions-item label="产品编码">{{ currentProduct.product_code }}</el-descriptions-item>
        <el-descriptions-item label="产品名称">{{ currentProduct.name }}</el-descriptions-item>
        <el-descriptions-item label="规格">{{ currentProduct.spec || '-' }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ currentProduct.category || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDate(currentProduct.created_at) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatDate(currentProduct.updated_at) }}</el-descriptions-item>
      </el-descriptions>
      
      <el-divider>产品图片</el-divider>
      
      <div class="image-grid">
        <el-image
          v-for="image in currentImages"
          :key="image.id"
          :src="getImageUrl(image.image_path)"
          fit="cover"
          class="product-image"
          :preview-src-list="currentImages.map(img => getImageUrl(img.image_path))"
        >
          <template #error>
            <div class="image-error">
              <el-icon><Picture /></el-icon>
            </div>
          </template>
        </el-image>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listProducts, getProduct, deleteProduct as deleteProductApi } from '../api/search'
import { handleApiError } from '../utils/messageHandler'

// 数据
const products = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const searchCategory = ref('')

const detailVisible = ref(false)
const currentProduct = ref(null)
const currentImages = ref([])

// 加载产品列表
const loadProducts = async () => {
  loading.value = true
  
  try {
    const response = await listProducts(searchCategory.value, page.value, pageSize.value)
    
    if (response.success) {
      products.value = response.products
      total.value = response.total
    }
  } catch (error) {
    handleApiError(error.response || error, '加载失败')
  } finally {
    loading.value = false
  }
}

// 查看产品详情
const viewProduct = async (row) => {
  try {
    const response = await getProduct(row.product_code)
    
    if (response.success) {
      currentProduct.value = response.product
      currentImages.value = response.images
      detailVisible.value = true
    }
  } catch (error) {
    handleApiError(error.response || error, '获取详情失败')
  }
}

// 删除产品
const deleteProduct = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除产品 "${row.name}" 吗？此操作将同时删除该产品的所有图片和向量数据。`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const response = await deleteProductApi(row.product_code)
    
    if (response.success) {
      ElMessage.success(response.message)
      loadProducts()
    } else {
      handleApiError(response, '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      handleApiError(error.response || error, '删除失败')
    }
  }
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

// 获取图片URL
const getImageUrl = (path) => {
  return `/api/v1/images/${path}`
}

// 初始化加载
onMounted(() => {
  loadProducts()
})
</script>

<style scoped>
.product-list {
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

/* 让 card body 可滚动 */
:deep(.el-card__body) {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 16px;
}

.product-image {
  width: 150px;
  height: 150px;
  border-radius: 4px;
}

.image-error {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  color: #909399;
  font-size: 30px;
}
</style>
