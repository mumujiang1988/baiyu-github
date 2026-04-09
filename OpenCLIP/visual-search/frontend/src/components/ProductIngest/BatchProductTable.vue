<template>
  <div class="batch-upload-container">
    <!-- 目录结构配置 -->
    <el-card shadow="never" style="margin-bottom: 20px; background-color: #f5f7fa;">
      <template #header>
        <div style="display: flex; align-items: center; gap: 10px;">
          <el-icon><Setting /></el-icon>
          <span>目录结构配置</span>
        </div>
      </template>
      <el-form :inline="true" size="small">
        <el-form-item label="目录层级">
          <el-select v-model="folderStructure" style="width: 200px" @change="handleStructureChange">
            <el-option 
              v-for="opt in FOLDER_STRUCTURE_OPTIONS" 
              :key="opt.value"
              :label="opt.label" 
              :value="opt.value" 
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="folderStructure === 'scene'" label="场景文件夹名称">
          <el-input 
            v-model="sceneFolderNames" 
            placeholder="多个用逗号分隔，如：正面,侧面,背面"
            style="width: 300px"
          />
          <span style="margin-left: 10px; color: #909399; font-size: 12px;">
            留空则自动识别所有子文件夹为场景
          </span>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作按钮组 -->
    <div class="batch-actions">
      <el-button type="primary" size="large" @click="triggerBatchUpload" :disabled="batchSubmitting">
        <el-icon><FolderOpened /></el-icon>
        导入产品
      </el-button>
      <el-button 
        type="success" 
        size="large" 
        @click="startBatchIngest" 
        :disabled="batchResults.length === 0 || batchSubmitting"
        :loading="batchSubmitting"
      >
        <el-icon><Upload /></el-icon>
        {{ batchSubmitting ? '入库中...' : '产品入库' }}
      </el-button>
      <el-button 
        type="warning" 
        size="large" 
        @click="clearBatchProducts" 
        :disabled="batchResults.length === 0 || batchSubmitting"
      >
        <el-icon><Delete /></el-icon>
        清空产品
      </el-button>
    </div>
    <input
      ref="batchInputRef"
      type="file"
      webkitdirectory
      directory
      multiple
      style="display: none"
      @change="handleBatchFileSelect"
    />

    <!-- 批量入库结果表格 -->
    <el-table 
      v-if="batchResults.length > 0" 
      :data="batchResults" 
      style="margin-top: 20px"
      border
      stripe
    >
      <el-table-column prop="folderName" label="文件夹名称" min-width="150" />
      <el-table-column prop="productCode" label="产品编码" width="120" />
      <el-table-column prop="productName" label="产品名称" min-width="150" />
      <el-table-column prop="spec" label="规格" width="100" />
      <el-table-column prop="category" label="分类" width="100" />
      <el-table-column prop="imageCount" label="图片数量" width="100" align="center" />
      <el-table-column label="进度" width="200" align="center">
        <template #default="{ row }">
          <div v-if="row.status === 'processing' || row.status === 'success' || row.status === 'error'">
            <el-progress 
              :percentage="row.progress || 0" 
              :status="row.status === 'success' ? 'success' : row.status === 'error' ? 'exception' : ''"
              :stroke-width="16"
            />
          </div>
          <span v-else style="color: #909399">-</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.status === 'pending'" type="info">待处理</el-tag>
          <el-tag v-else-if="row.status === 'processing'" type="warning">
            <el-icon class="is-loading"><Loading /></el-icon>
            处理中
          </el-tag>
          <el-tag v-else-if="row.status === 'success'" type="success">成功</el-tag>
          <el-tag v-else-if="row.status === 'error'" type="danger">失败</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="消息" min-width="150">
        <template #default="{ row }">
          <span :style="{ color: row.status === 'error' ? '#f56c6c' : '#67c23a' }">
            {{ row.message || '-' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" align="center" fixed="right">
        <template #default="{ row, $index }">
          <el-button 
            type="danger" 
            size="small" 
            link
            @click="removeProduct($index)"
            :disabled="row.status === 'processing' || batchSubmitting"
          >
            <el-icon><Delete /></el-icon>
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 批量入库统计 -->
    <el-descriptions 
      v-if="batchResults.length > 0" 
      :column="4" 
      border 
      style="margin-top: 20px"
    >
      <el-descriptions-item label="总数">
        <el-tag>{{ batchResults.length }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="成功">
        <el-tag type="success">{{ successCount }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="失败">
        <el-tag type="danger">{{ failCount }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="成功率">
        <el-tag :type="successRate >= 80 ? 'success' : successRate >= 50 ? 'warning' : 'danger'">
          {{ successRate }}%
        </el-tag>
      </el-descriptions-item>
    </el-descriptions>
  </div>
</template>

<script setup>
import { Setting, FolderOpened, Upload, Delete, Loading } from '@element-plus/icons-vue'
import { useIngestStore } from '../../stores/ingest'
import { FOLDER_STRUCTURE_OPTIONS } from './constants'

const ingestStore = useIngestStore()

// 解构 store 中的状态和方法
const {
  batchInputRef,
  batchSubmitting,
  batchResults,
  successCount,
  failCount,
  successRate,
  folderStructure,
  sceneFolderNames,
  triggerBatchUpload,
  handleBatchFileSelect,
  startBatchIngest,
  clearBatchProducts,
  removeProduct,
  handleStructureChange
} = ingestStore
</script>

<style scoped>
.batch-upload-container {
  padding: 20px;
}

.batch-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-bottom: 20px;
}
</style>
