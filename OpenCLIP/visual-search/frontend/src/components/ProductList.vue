<template>
  <div class="product-list">
    <el-card class="main-card">
      <template #header>
        <div class="card-header">
          <span class="section-title">产品管理</span>
          <div class="header-actions">
            <el-button type="success" @click="checkConsistency" :loading="checking">
              <el-icon><DataAnalysis /></el-icon>
              数据一致性检查
            </el-button>
            <el-button 
              type="warning" 
              @click="queryOrphanDataHandler"
              :loading="queryingOrphan"
            >
              <el-icon><Warning /></el-icon>
              孤儿数据查询
            </el-button>
            <el-button 
              type="danger" 
              @click="handleBatchDelete"
              :disabled="selectedProducts.length === 0"
              :loading="batchDeleting"
            >
              <el-icon><Delete /></el-icon>
              批量删除 ({{ selectedProducts.length }})
            </el-button>
            <el-input 
              v-model="searchCategory" 
              placeholder="分类筛选" 
              clearable 
              style="width: 200px"
              @clear="loadProducts" 
              @keyup.enter="loadProducts" 
            />
            <el-button type="primary" @click="loadProducts">
              <el-icon><Search /></el-icon>
              查询
            </el-button>
          </div>
        </div>
      </template>
      
      <!-- 数据一致性检查结果 -->
      <el-alert
        v-if="consistencyResult"
        :title="`数据一致性检查报告`"
        type="info"
        :closable="true"
        show-icon
        style="margin-bottom: 16px"
      >
        <template #default>
          <div class="consistency-summary">
            <el-descriptions :column="3" border size="small">
              <el-descriptions-item label="MySQL 产品数">{{ consistencyResult.summary.total_mysql_products }}</el-descriptions-item>
              <el-descriptions-item label="Milvus 向量数">{{ consistencyResult.summary.total_milvus_products }}</el-descriptions-item>
              <el-descriptions-item label="MinIO 文件数">{{ consistencyResult.summary.total_minio_products }}</el-descriptions-item>
              <el-descriptions-item label="完整产品">
                <el-tag type="success">{{ consistencyResult.summary.complete_products }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="残缺产品">
                <el-tag type="danger">{{ 
                  consistencyResult.summary.mysql_only_count + 
                  consistencyResult.summary.milvus_only_count + 
                  consistencyResult.summary.minio_only_count 
                }}</el-tag>
              </el-descriptions-item>
            </el-descriptions>
            
            <el-divider />
            
            <!-- 问题详情 -->
            <div v-if="hasIssues" class="issues-section">
              <el-tabs type="border-card">
                <el-tab-pane v-if="consistencyResult.mysql_only.length > 0" :label="`只在 MySQL (${consistencyResult.mysql_only.length})`">
                  <el-table :data="consistencyResult.mysql_only" size="small" max-height="200">
                    <el-table-column prop="product_code" label="产品编码" width="150" />
                    <el-table-column prop="issue" label="问题描述" />
                    <el-table-column label="严重程度" width="100">
                      <template #default="{ row }">
                        <el-tag :type="row.severity === 'high' ? 'danger' : 'warning'" size="small">
                          {{ row.severity === 'high' ? '高' : '中' }}
                        </el-tag>
                      </template>
                    </el-table-column>
                  </el-table>
                </el-tab-pane>
                
                <el-tab-pane v-if="consistencyResult.milvus_only.length > 0" :label="`只在 Milvus (${consistencyResult.milvus_only.length})`">
                  <el-table :data="consistencyResult.milvus_only" size="small" max-height="200">
                    <el-table-column prop="product_code" label="产品编码" width="150" />
                    <el-table-column prop="issue" label="问题描述" />
                    <el-table-column label="严重程度" width="100">
                      <template #default="{ row }">
                        <el-tag :type="row.severity === 'high' ? 'danger' : 'warning'" size="small">
                          {{ row.severity === 'high' ? '高' : '中' }}
                        </el-tag>
                      </template>
                    </el-table-column>
                  </el-table>
                </el-tab-pane>
                
                <el-tab-pane v-if="consistencyResult.minio_only.length > 0" :label="`只在 MinIO (${consistencyResult.minio_only.length})`">
                  <el-table :data="consistencyResult.minio_only" size="small" max-height="200">
                    <el-table-column prop="product_code" label="产品编码" width="150" />
                    <el-table-column prop="issue" label="问题描述" />
                    <el-table-column label="严重程度" width="100">
                      <template #default="{ row }">
                        <el-tag :type="row.severity === 'high' ? 'danger' : 'warning'" size="small">
                          {{ row.severity === 'high' ? '高' : '中' }}
                        </el-tag>
                      </template>
                    </el-table-column>
                  </el-table>
                </el-tab-pane>
              </el-tabs>
            </div>
            
            <div v-else class="no-issues">
              <el-empty description="所有数据一致，没有发现问题" :image-size="80" />
            </div>
          </div>
        </template>
      </el-alert>
      
      <!-- 孤儿数据查询结果 -->
      <el-alert
        v-if="orphanDataResult"
        title="孤儿数据查询结果"
        type="warning"
        :closable="true"
        @close="orphanDataResult = null"
        show-icon
        style="margin-bottom: 16px"
      >
        <template #default>
          <div class="orphan-data-summary">
            <el-descriptions :column="3" border size="small">
              <el-descriptions-item label="MySQL 孤儿记录">
                <el-tag type="danger">{{ orphanDataResult.mysql_orphans || 0 }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="Milvus 孤儿向量">
                <el-tag type="danger">{{ orphanDataResult.milvus_orphans || 0 }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="MinIO 孤儿文件">
                <el-tag type="danger">{{ orphanDataResult.minio_orphans || 0 }}</el-tag>
              </el-descriptions-item>
            </el-descriptions>
            
            <el-divider />
            
            <!-- 孤儿数据详情 -->
            <div v-if="hasOrphanData" class="orphan-details">
              <el-tabs type="border-card">
                <el-tab-pane v-if="orphanDataResult.mysql_orphan_details && orphanDataResult.mysql_orphan_details.length > 0" 
                  :label="`MySQL 孤儿记录 (${orphanDataResult.mysql_orphan_details.length})`">
                  <el-table :data="orphanDataResult.mysql_orphan_details" size="small" max-height="300">
                    <el-table-column prop="product_code" label="产品编码" width="150" />
                    <el-table-column prop="name" label="产品名称" width="200" />
                    <el-table-column prop="reason" label="原因" />
                    <el-table-column label="操作" width="100" align="center">
                      <template #default="{ row }">
                        <el-button 
                          type="danger" 
                          size="small" 
                          link
                          @click="deleteOrphanRecord('mysql', row)"
                        >
                          删除
                        </el-button>
                      </template>
                    </el-table-column>
                  </el-table>
                </el-tab-pane>
                
                <el-tab-pane v-if="orphanDataResult.milvus_orphan_details && orphanDataResult.milvus_orphan_details.length > 0" 
                  :label="`Milvus 孤儿向量 (${orphanDataResult.milvus_orphan_details.length})`">
                  <el-table :data="orphanDataResult.milvus_orphan_details" size="small" max-height="300">
                    <el-table-column prop="product_code" label="产品编码" width="150" />
                    <el-table-column prop="milvus_id" label="Milvus ID" width="120" />
                    <el-table-column prop="reason" label="原因" />
                    <el-table-column label="操作" width="100" align="center">
                      <template #default="{ row }">
                        <el-button 
                          type="danger" 
                          size="small" 
                          link
                          @click="deleteOrphanRecord('milvus', row)"
                        >
                          删除
                        </el-button>
                      </template>
                    </el-table-column>
                  </el-table>
                </el-tab-pane>
                
                <el-tab-pane v-if="orphanDataResult.minio_orphan_details && orphanDataResult.minio_orphan_details.length > 0" 
                  :label="`MinIO 孤儿文件 (${orphanDataResult.minio_orphan_details.length})`">
                  <el-table :data="orphanDataResult.minio_orphan_details" size="small" max-height="300">
                    <el-table-column prop="object_name" label="文件路径" min-width="300" />
                    <el-table-column prop="size" label="文件大小" width="120" />
                    <el-table-column label="操作" width="100" align="center">
                      <template #default="{ row }">
                        <el-button 
                          type="danger" 
                          size="small" 
                          link
                          @click="deleteOrphanRecord('minio', row)"
                        >
                          删除
                        </el-button>
                      </template>
                    </el-table-column>
                  </el-table>
                </el-tab-pane>
              </el-tabs>
              
              <!-- 批量清理按钮 -->
              <div style="margin-top: 16px; text-align: right;">
                <el-button 
                  type="danger" 
                  @click="cleanAllOrphanData"
                  :loading="cleaningOrphan"
                >
                  <el-icon><Delete /></el-icon>
                  批量清理所有孤儿数据
                </el-button>
              </div>
            </div>
            
            <div v-else class="no-orphan">
              <el-empty description="未发现孤儿数据" :image-size="80" />
            </div>
          </div>
        </template>
      </el-alert>
      
      <el-table 
        :data="products" 
        v-loading="loading" 
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="product_code" label="产品编码" width="150" />
        <el-table-column prop="name" label="产品名称" min-width="200" />
        <el-table-column prop="spec" label="规格" width="150" />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column label="图片数量" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.image_count || 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Milvus ID" width="120" align="center">
          <template #default="{ row }">
            <span v-if="row.milvus_ids && row.milvus_ids.length > 0">
              {{ row.milvus_ids.length }} 个
            </span>
            <el-tag v-else size="small" type="danger">缺失</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="MinIO 文件" width="120" align="center">
          <template #default="{ row }">
            <span v-if="row.minio_files && row.minio_files.length > 0">
              {{ row.minio_files.length }} 个
            </span>
            <el-tag v-else size="small" type="danger">缺失</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="数据状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag 
              v-if="row.data_status === 'complete'" 
              size="small" 
              type="success"
            >
              完整
            </el-tag>
            <el-tag 
              v-else-if="row.data_status === 'incomplete'" 
              size="small" 
              type="warning"
            >
              残缺
            </el-tag>
            <el-tag 
              v-else 
              size="small" 
              type="info"
            >
              未知
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="created_at" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.created_at) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right" align="center">
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
        @update:current-page="loadProducts"
        @update:page-size="handlePageSizeChange"
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
          lazy
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
// 设置组件名称（用于 keep-alive）
defineOptions({
  name: 'ProductList'
})

import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listProducts, getProduct, deleteProduct as deleteProductApi, checkDataConsistency, queryOrphanData, cleanOrphanData, batchDeleteProducts, deleteSingleOrphan } from '../api/search'
import { handleApiError } from '../utils/messageHandler'
import { getImageUrl } from '../utils/imageHelper'
import { extractPaginatedData, extractData } from '../utils/responseAdapter'
import { logger } from '../utils/logger'

const products = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const searchCategory = ref('')

// 批量删除相关状态
const selectedProducts = ref([])
const batchDeleting = ref(false)

const detailVisible = ref(false)
const currentProduct = ref(null)
const currentImages = ref([])

// 数据一致性检查相关
const checking = ref(false)
const consistencyResult = ref(null)

// Orphan data query related
const queryingOrphan = ref(false)
const orphanDataResult = ref(null)
const cleaningOrphan = ref(false)

// 计算是否有问题
const hasIssues = computed(() => {
  if (!consistencyResult.value) return false
  return (
    consistencyResult.value.mysql_only.length > 0 ||
    consistencyResult.value.milvus_only.length > 0 ||
    consistencyResult.value.minio_only.length > 0
  )
})

// 计算是否有孤儿数据
const hasOrphanData = computed(() => {
  if (!orphanDataResult.value) return false
  return (
    (orphanDataResult.value.mysql_orphan_details && orphanDataResult.value.mysql_orphan_details.length > 0) ||
    (orphanDataResult.value.milvus_orphan_details && orphanDataResult.value.milvus_orphan_details.length > 0) ||
    (orphanDataResult.value.minio_orphan_details && orphanDataResult.value.minio_orphan_details.length > 0)
  )
})

const loadProducts = async () => {
  loading.value = true
  
  try {
    const response = await listProducts(searchCategory.value, page.value, pageSize.value)
    
    logger.log('📦 产品列表响应:', response)
    
    if (response && response.success) {
      // 使用响应适配器提取分页数据
      const { items, pagination } = extractPaginatedData(response)
      products.value = items || []
      total.value = pagination?.total || 0
      
      logger.log(' 产品列表加载成功:', {
        count: products.value.length,
        total: total.value
      })
    } else {
      logger.warn(' 响应格式不正确:', response)
      products.value = []
      total.value = 0
    }
  } catch (error) {
    logger.error(' 加载产品列表失败:', error)
    handleApiError(error.response || error, '加载失败')
    products.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// Handle page size change
const handlePageSizeChange = (newPageSize) => {
  // 当页面大小改变时，重置到第一页
  page.value = 1
  loadProducts()
}

const checkConsistency = async () => {
  checking.value = true
  
  try {
    const response = await checkDataConsistency()
    
    logger.log('📊 数据一致性检查原始响应:', response)
    logger.log('  - response类型:', typeof response)
    logger.log('  - response.success:', response?.success)
    logger.log('  - response keys:', Object.keys(response || {}))
    
    if (!response) {
      ElMessage.error('检查失败：未收到服务器响应')
      return
    }
    
    // 兼容两种格式: {success: true, ...} 和 直接的业务数据
    const isSuccess = response.success === true || (response.summary && !response.message?.includes('失败'))
    
    if (isSuccess) {
      consistencyResult.value = response
      
      // 显示统计信息
      const summary = response.summary || {}
      ElMessage.success(
        `检查完成！完整产品: ${summary.complete_products || 0}, ` +
        `残缺产品: ${(summary.mysql_only_count || 0) + (summary.milvus_only_count || 0) + (summary.minio_only_count || 0)}`
      )
    } else {
      logger.error('❌ 数据一致性检查失败:', response)
      handleApiError(response, '检查失败')
    }
  } catch (error) {
    logger.error('❌ 数据一致性检查异常:', error)
    logger.error('  错误详情:', {
      message: error.message,
      code: error.code,
      status: error.response?.status,
      data: error.response?.data
    })
    handleApiError(error.response || error, '检查失败')
  } finally {
    checking.value = false
  }
}

const viewProduct = async (row) => {
  try {
    const response = await getProduct(row.product_code)
    
    if (response.success) {
      // 使用响应适配器提取数据
      const data = extractData(response)
      currentProduct.value = data.product
      currentImages.value = data.images || []
      detailVisible.value = true
    }
  } catch (error) {
    handleApiError(error.response || error, '获取详情失败')
  }
}

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
      // 清除一致性检查结果，需要重新检查
      consistencyResult.value = null
    } else {
      handleApiError(response, '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      handleApiError(error.response || error, '删除失败')
    }
  }
}

// Handle selection change
const handleSelectionChange = (selection) => {
  selectedProducts.value = selection
}

// Handle batch delete
const handleBatchDelete = async () => {
  if (selectedProducts.value.length === 0) {
    ElMessage.warning('请先选择要删除的产品')
    return
  }
  
  const productCodes = selectedProducts.value.map(p => p.product_code)
  const productNames = selectedProducts.value.map(p => p.name).join(', ')
  
  try {
    await ElMessageBox.confirm(
      `确定要批量删除以下 ${selectedProducts.value.length} 个产品吗？\n\n${productNames}\n\n此操作将同时删除这些产品的所有图片和向量数据，且不可恢复。`,
      '批量删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        distinguishCancelAndClose: true
      }
    )
    
    batchDeleting.value = true
    
    const response = await batchDeleteProducts(productCodes)
    
    // 检查响应是否有效
    if (!response) {
      ElMessage.error('批量删除失败：未收到服务器响应')
      return
    }
    
    // 统一格式：扁平化响应，数据直接在 response 根级别
    if (response.success) {
      const successCount = response.success?.length || 0
      const failedCount = response.failed?.length || 0
      const totalDeletedImages = response.total_deleted_images || 0
      
      // 显示详细结果
      let message = `批量删除完成！成功: ${successCount} 个, 失败: ${failedCount} 个, 共删除 ${totalDeletedImages} 张图片`
      
      if (failedCount > 0) {
        const failedProducts = response.failed.map(f => f.product_code).join(', ')
        message += `\n\n失败产品: ${failedProducts}`
        ElMessage.warning({
          message,
          duration: 8000,
          showClose: true
        })
      } else {
        ElMessage.success({
          message,
          duration: 5000
        })
      }
      
      // 清空选择
      selectedProducts.value = []
      
      // 刷新列表
      loadProducts()
      
      // 清除一致性检查结果
      consistencyResult.value = null
    } else {
      handleApiError(response, '批量删除失败')
    }
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      handleApiError(error.response || error, '批量删除失败')
    }
  } finally {
    batchDeleting.value = false
  }
}

// Query orphan data
const queryOrphanDataHandler = async () => {
  queryingOrphan.value = true
  orphanDataResult.value = null  // 清除旧数据
  
  try {
    const response = await queryOrphanData()
    
    logger.log('🔍 孤儿数据查询原始响应:', response)
    logger.log('  - response类型:', typeof response)
    logger.log('  - response.success:', response?.success)
    logger.log('  - response keys:', Object.keys(response || {}))
    
    // 检查响应是否有效
    if (!response) {
      ElMessage.error('查询失败：未收到服务器响应')
      return
    }
    
    // 兼容两种格式: {success: true, ...} 和 直接的业务数据
    const isSuccess = response.success === true || (response.mysql_orphans !== undefined && !response.message?.includes('失败'))
    
    if (isSuccess) {
      orphanDataResult.value = response
      
      const total = 
        (response.mysql_orphans || 0) +
        (response.milvus_orphans || 0) +
        (response.minio_orphans || 0)
      
      if (total === 0) {
        ElMessage.success('未发现孤儿数据')
      } else {
        ElMessage.warning(`发现 ${total} 条孤儿数据`)
      }
    } else {
      logger.error('❌ 孤儿数据查询失败:', response)
      handleApiError(response, '查询失败')
    }
  } catch (error) {
    logger.error('❌ 孤儿数据查询异常:', error)
    logger.error('  错误详情:', {
      message: error.message,
      code: error.code,
      status: error.response?.status,
      data: error.response?.data
    })
    handleApiError(error.response || error, '查询失败')
  } finally {
    queryingOrphan.value = false
  }
}

// Delete single orphan record
const deleteOrphanRecord = async (type, row) => {
  try {
    let message = ''
    if (type === 'mysql') {
      message = `确定要删除 MySQL 中的孤儿记录 "${row.product_code}" 吗？`
    } else if (type === 'milvus') {
      message = `确定要删除 Milvus 中的孤儿向量 ID=${row.milvus_id} 吗？`
    } else if (type === 'minio') {
      message = `确定要删除 MinIO 中的孤儿文件 "${row.object_name}" 吗？`
    }
    
    await ElMessageBox.confirm(message, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // Determine identifier
    let identifier = ''
    if (type === 'mysql') {
      identifier = row.product_code
    } else if (type === 'milvus') {
      identifier = String(row.milvus_id)
    } else if (type === 'minio') {
      identifier = row.object_name
    }
    
    // Call API
    const response = await deleteSingleOrphan(type, identifier)
    
    if (response.success) {
      ElMessage.success(response.message)
      
      // Re-query orphan data
      await queryOrphanDataHandler()
      
      // Refresh product list
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

// Clean all orphan data
const cleanAllOrphanData = async () => {
  if (!orphanDataResult.value) {
    ElMessage.warning('请先查询孤儿数据')
    return
  }
  
  const { mysql_orphans, milvus_orphans, minio_orphans } = orphanDataResult.value
  const total = (mysql_orphans || 0) + (milvus_orphans || 0) + (minio_orphans || 0)
  
  if (total === 0) {
    ElMessage.info('没有孤儿数据需要清理')
    return
  }
  
  try {
    // Step 1: Get preview from backend
    const previewResponse = await cleanOrphanData(false)
    
    if (!previewResponse.requires_confirmation) {
      // Backend returned old format, use directly
      await executeCleanOrphans()
      return
    }
    
    // Show confirmation dialog with preview data
    const { mysql_count, milvus_count, minio_count, total: previewTotal } = previewResponse.orphan_summary
    
    await ElMessageBox.confirm(
      `即将清理以下孤儿数据，此操作不可恢复！\n\n` +
      `- MySQL: ${mysql_count} 条\n` +
      `- Milvus: ${milvus_count} 条\n` +
      `- MinIO: ${minio_count} 个文件\n\n` +
      `总计: ${previewTotal} 条`,
      '确认清理孤儿数据',
      {
        confirmButtonText: '确定清理',
        cancelButtonText: '取消',
        type: 'warning',
        distinguishCancelAndClose: true
      }
    )
    
    // Step 2: Execute cleanup
    await executeCleanOrphans()
    
  } catch (error) {
    if (error !== 'cancel' && error !== 'close') {
      handleApiError(error.response || error, '获取预览失败')
    }
  }
}

// Helper function to execute cleanup
async function executeCleanOrphans() {
  cleaningOrphan.value = true
  
  try {
    const response = await cleanOrphanData(true)
    
    if (response.success) {
      const { mysql_count, milvus_count, minio_count } = response.cleaned
      const cleanedTotal = mysql_count + milvus_count + minio_count
      
      // 显示详细结果
      let message = response.message
      if (cleanedTotal > 0) {
        message += `\n\n实际清理：\n- MySQL: ${mysql_count} 条\n- Milvus: ${milvus_count} 条\n- MinIO: ${minio_count} 条`
      }
      
      ElMessage.success({
        message,
        duration: 8000,
        showClose: true
      })
      
      // 重新查询孤儿数据
      await queryOrphanDataHandler()
      
      // 刷新产品列表
      loadProducts()
    } else {
      handleApiError(response, '清理失败')
    }
    
  } catch (error) {
    handleApiError(error.response || error, '清理失败')
  } finally {
    cleaningOrphan.value = false
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

onMounted(() => {
  loadProducts()
})
</script>

<style scoped>
.product-list {
  width: 100%;
  height: 100%;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
}

.main-card {
  flex: 1;
  min-height: 0;
  overflow: hidden;
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

.card-header .section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1E293B;
  letter-spacing: 0.3px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.consistency-summary {
  padding: 10px 0;
}

.issues-section {
  margin-top: 16px;
}

.no-issues {
  padding: 20px 0;
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 16px;
}

.product-image {
  width: 150px;
  height: 150px;
  border-radius: 8px;
  cursor: pointer;
  /* 禁用过渡效果，避免闪烁 */
  transition: none;
}

/* 完全移除 hover 效果，保持简洁稳定 */

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

/* 优化 Element Plus 图片查看器，防止闪烁 */
:deep(.el-image-viewer__wrapper) {
  /* 禁用查看器的过渡动画，避免闪烁 */
  transition: none !important;
}

:deep(.el-image-viewer__canvas) {
  /* 确保图片渲染稳定 */
  will-change: auto;
  transition: none !important;
}

:deep(.el-image-viewer__img) {
  /* 图片本身也不要有过渡效果 */
  transition: none !important;
}
</style>
