<template>
  <div class="ingest-logs-container">
    <!-- 筛选区 -->
    <el-form :inline="true" class="filter-form">
      <el-form-item label="批次ID">
        <el-input 
          v-model="filters.batchId" 
          placeholder="请输入批次ID" 
          clearable
          style="width: 240px"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 日志表格 -->
    <el-table 
      :data="logs" 
      v-loading="loading" 
      border 
      stripe
      style="width: 100%"
    >
      <el-table-column prop="batch_id" label="批次ID" width="180" show-overflow-tooltip />
      <el-table-column prop="product_code" label="产品编码" width="150" />
      <el-table-column prop="product_name" label="产品名称" min-width="150" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 'success' ? 'success' : 'danger'" size="small">
            {{ row.status === 'success' ? '成功' : '失败' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="error_message" label="失败原因" min-width="200">
        <template #default="{ row }">
          <span v-if="row.status === 'failed'">
            <el-tooltip :content="row.error_message" placement="top" effect="dark">
              <span class="error-text">{{ truncateText(row.error_message, 30) }}</span>
            </el-tooltip>
          </span>
          <span v-else class="text-muted">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="created_at" label="入库时间" width="180">
        <template #default="{ row }">
          {{ formatTime(row.created_at) }}
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
      v-model:current-page="pagination.page"
      v-model:page-size="pagination.pageSize"
      :total="pagination.total"
      :page-sizes="[10, 20, 50, 100]"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="fetchLogs"
      @current-change="fetchLogs"
      style="margin-top: 20px; justify-content: flex-end;"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

// 创建 axios 实例（不设置 baseURL，直接使用完整路径）
const apiClient = axios.create({
  timeout: 60000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// API基础路径
const API_BASE = '/api/v1'

const logs = ref([])
const loading = ref(false)
const filters = reactive({ batchId: '' })
const pagination = reactive({ page: 1, pageSize: 20, total: 0 })

const fetchLogs = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      page_size: pagination.pageSize,
      ...(filters.batchId && { batch_id: filters.batchId })
    }
    // 使用完整的API路径
    const res = await apiClient.get(`${API_BASE}/ingest/logs`, { params })
    if (res.data.success) {
      logs.value = res.data.data
      pagination.total = res.data.total
    }
  } catch (err) {
    console.error('获取入库日志失败:', err)
    ElMessage.error('获取入库日志失败，请稍后重试')
    logs.value = []  // 清空数据，避免显示旧数据
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchLogs()
}

const handleReset = () => {
  filters.batchId = ''
  pagination.page = 1
  fetchLogs()
}

const truncateText = (text, len) => {
  if (!text) return ''
  return text.length > len ? text.substring(0, len) + '...' : text
}

// 格式化时间为东八区（北京时间）
const formatTime = (utcTime) => {
  if (!utcTime) return '-'
  
  // 创建Date对象（自动识别UTC时间）
  const date = new Date(utcTime)
  
  // 转换为东八区时间（UTC+8）
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

onMounted(() => {
  fetchLogs()
})
</script>

<style scoped>
.ingest-logs-container {
  padding: 10px;
}
.filter-form {
  margin-bottom: 20px;
  background: #f9fafc;
  padding: 15px;
  border-radius: 4px;
}
.error-text {
  color: #f56c6c;
  cursor: pointer;
  border-bottom: 1px dashed #f56c6c;
}
.text-muted {
  color: #909399;
}
</style>
