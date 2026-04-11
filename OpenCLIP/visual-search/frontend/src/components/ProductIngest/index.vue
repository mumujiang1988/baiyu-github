<template>
  <div class="product-ingest">
    <el-card class="main-card">
      <el-tabs v-model="activeTab" type="border-card" style="height: 100%; display: flex; flex-direction: column;">
        <!-- 单产品入库页签 -->
        <el-tab-pane label="单产品入库" name="single">
          <SingleProductForm />
        </el-tab-pane>

        <!-- 批量入库页签 -->
        <el-tab-pane label="批量入库" name="batch">
          <BatchProductTable />
        </el-tab-pane>

        <!-- 入库日志页签 -->
        <el-tab-pane label="入库日志" name="logs">
          <IngestLogs />
        </el-tab-pane>
      </el-tabs>
    
      <!-- 入库结果 -->
      <el-card v-if="ingestResult" class="result-card">
        <template #header>
          <span>入库结果</span>
        </template>
        
        <el-result
          :icon="ingestResult.success ? 'success' : 'error'"
          :title="ingestResult.message"
        >
          <template #extra>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="产品编码">{{ ingestResult.product_code }}</el-descriptions-item>
              <el-descriptions-item label="成功数量">
                <el-tag type="success">{{ ingestResult.success_count }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="失败数量">
                <el-tag :type="ingestResult.fail_count > 0 ? 'danger' : 'info'">
                  {{ ingestResult.fail_count }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="耗时">{{ ingestResult.ingest_time_ms }}ms</el-descriptions-item>
            </el-descriptions>
            
            <div v-if="ingestResult.errors && ingestResult.errors.length > 0" class="error-list">
              <el-divider>错误详情</el-divider>
              <el-alert
                v-for="(error, index) in ingestResult.errors"
                :key="index"
                type="error"
                :closable="false"
                show-icon
                style="margin-bottom: 10px"
              >
                <template #title>
                  <div class="error-title">{{ error }}</div>
                </template>
              </el-alert>

              <!-- 显示解决建议 -->
              <el-alert
                v-if="ingestResult.suggestion"
                type="info"
                :closable="false"
                show-icon
                style="margin-top: 15px"
              >
                <template #title>
                  <div class="suggestion-title">
                    <el-icon><InfoFilled /></el-icon>
                    解决建议: {{ ingestResult.suggestion }}
                  </div>
                </template>
              </el-alert>
            </div>
          </template>
        </el-result>
      </el-card>
    </el-card>
  </div>
</template>

<script setup>
// 设置组件名称（用于 keep-alive）
defineOptions({
  name: 'ProductIngest'
})

import { ref, watch, computed } from 'vue'
import { InfoFilled } from '@element-plus/icons-vue'
import SingleProductForm from './SingleProductForm.vue'
import BatchProductTable from './BatchProductTable.vue'
import IngestLogs from './IngestLogs.vue'
import { useIngestStore } from '../../stores/ingest'
import { TAB_CONFIG } from './constants'

// 使用 Pinia Store（仅用于单产品入库结果）
const ingestStore = useIngestStore()

// 页签控制
const activeTab = ref(TAB_CONFIG.SINGLE)

// 从 Store 中获取单产品入库结果
const ingestResult = computed(() => ingestStore.activeResult)

// 监听页签切换
watch(activeTab, (newTab) => {
  ingestStore.switchTab(newTab)
})
</script>

<style scoped>
.product-ingest {
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

/* 让 tabs 占满 card */
:deep(.el-tabs) {
  flex: 1;
  display: flex;
  flex-direction: column;
}

:deep(.el-tabs__content) {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.result-card {
  margin-top: 20px;
}

.error-list {
  margin-top: 20px;
}

.error-title {
  word-break: break-word;
  line-height: 1.6;
}

.suggestion-title {
  display: flex;
  align-items: center;
  gap: 8px;
  word-break: break-word;
  line-height: 1.6;
}

.suggestion-title .el-icon {
  flex-shrink: 0;
  color: #165DFF;
}
</style>
