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
          <el-select 
            v-model="folderStructure" 
            style="width: 200px" 
            @change="handleStructureChange"
          >
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
      @change="(event) => {
        logger.log('[BatchProductTable] File input change event triggered', {
          filesCount: event.target?.files?.length,
          firstFile: event.target?.files?.[0]?.webkitRelativePath
        });
        handleBatchFileSelect(event);
      }"
    />

    <!-- 错误提示说明 -->
    <el-alert
      v-if="batchResults.some(r => r.status === 'error')"
      title="失败处理建议"
      type="warning"
      :closable="false"
      style="margin-top: 15px"
    >
      <template #default>
        <div style="line-height: 1.8; font-size: 13px">
          <div><strong>🔄 所有图片均已存在：</strong>无需处理，该产品已入库</div>
          <div><strong>⏱️ 请求过于频繁：</strong>使用“批量重试”功能，系统会自动控制频率</div>
          <div><strong>⌛ 处理超时：</strong>稍后点击单个产品的“重试”按钮再次尝试</div>
          <div><strong>❌ 服务器内部错误：</strong>联系管理员或使用“重试”功能</div>
        </div>
      </template>
    </el-alert>

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
      <el-table-column label="消息" min-width="250">
        <template #default="{ row }">
          <div v-if="row.message" style="line-height: 1.6">
            <!-- 错误消息分段显示 -->
            <template v-if="row.status === 'error' && row.message.includes(' - ')">
              <div :style="{ color: '#f56c6c', fontWeight: 'bold' }">
                {{ row.message.split(' - ')[0] }}
              </div>
              <div :style="{ color: '#909399', fontSize: '12px', marginTop: '4px' }">
                💡 {{ row.message.split(' - ').slice(1).join(' - ') }}
              </div>
            </template>
            <!-- 成功消息 -->
            <template v-else-if="row.status === 'success'">
              <span :style="{ color: '#67c23a' }">✅ {{ row.message }}</span>
            </template>
            <!-- 其他消息 -->
            <template v-else>
              <span :style="{ color: row.status === 'error' ? '#f56c6c' : '#67c23a' }">
                {{ row.message }}
              </span>
            </template>
          </div>
          <span v-else style="color: #909399">-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" align="center" fixed="right">
        <template #default="{ row, $index }">
          <el-button 
            v-if="row.status === 'error'"
            type="primary" 
            size="small" 
            link
            @click="retryProduct($index)"
            :disabled="batchSubmitting"
          >
            <el-icon><Refresh /></el-icon>
            重试
          </el-button>
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
    
    <!-- 批量重试按钮 -->
    <div v-if="failCount > 0" style="margin-top: 15px; text-align: center;">
      <el-button 
        type="warning" 
        size="default"
        @click="retryAllFailed"
        :disabled="batchSubmitting"
        :loading="batchSubmitting"
      >
        <el-icon><Refresh /></el-icon>
        重试所有失败项 ({{ failCount }})
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { useBatchIngest } from './composables/useBatchIngest'
import { Setting, FolderOpened, Upload, Delete, Loading, Refresh } from '@element-plus/icons-vue'
import { FOLDER_STRUCTURE_OPTIONS } from './constants'
import { logger } from '../../utils/logger'

// 使用 Composable 管理批量入库逻辑
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
  retryProduct,  // 单个重试
  retryAllFailed,  // 批量重试
  handleStructureChange
} = useBatchIngest()
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
