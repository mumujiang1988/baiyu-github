<script setup name="ErpConfig">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import Pagination from '@/components/Pagination'
import { getConfigTypeLabel, getConfigTypeTag, getConfigTypeOptions } from '@/constants/configTypes'
import request from '@/utils/request'
import ConfigSearch from './components/ConfigSearch.vue'
import ConfigTable from './components/ConfigTable.vue'
import JsonEditor from './components/JsonEditor.vue'
import VisualConfigEditor from './components/VisualConfigEditor.vue'
import { useConfigData } from './composables/useConfigData'
import { mergeConfigFields, splitConfigFields } from './utils/configJsonUtils'
import { verifyFieldConsistency, generateValidationReport } from './utils/fieldValidator'

// Computed Properties
const allDictsJson = computed(() => JSON.stringify(allDictsData.value, null, 2))
const nationDictsJson = computed(() => JSON.stringify(nationDictData.value, null, 2))

// State
const {
  loading,
  configList,
  total,
  queryParams,
  getList,
  handleQuery,
  resetQuery,
  handleDelete: composableDelete,
  loadConfigDetail,
  handleSave: composableSave
} = useConfigData()

// 页签状态
const activeTab = ref('config')
const dictActiveTab = ref('all')

// 字典数据
const allDictsData = ref({})
const nationDictData = ref([])

// Dialog 状态
const viewDialogVisible = ref(false)
const visualEditorVisible = ref(false)
const validationDialogVisible = ref(false) // 验证报告对话框
const isEditMode = ref(false)
const currentConfig = ref({})
const editFormData = ref({})
const combinedConfigContent = ref('')
const validationReport = ref('') // 验证报告 HTML

// Methods

async function handleClearCache() {
  try {
    await ElMessageBox.confirm(
      '确定要清理所有 ERP 配置缓存吗？',
      '提示',
      { 
        confirmButtonText: '确定', 
        cancelButtonText: '取消', 
        type: 'warning',
        distinguishCancelAndClose: true
      }
    )
    
    const res = await request({ url: '/erp/cache/clear-all', method: 'delete' })
    ElMessage.success('缓存清理成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('清理缓存失败:', error)
      ElMessage.error('缓存清理失败：' + (error.message || '未知错误'))
    }
  }
}

function handleAdd() {
  isEditMode.value = false
  editFormData.value = {}
  combinedConfigContent.value = '{}'
  currentConfig.value = {} // ✅ 重置为当前配置
  // ✨ 使用可视化编辑器
  visualEditorVisible.value = true
}

async function handleView(row) {
  isEditMode.value = false
  try {
    const data = await loadConfigDetail(row.configId)
    currentConfig.value = data
    const merged = mergeConfigFields(data)
    combinedConfigContent.value = JSON.stringify(merged, null, 2) 
    visualEditorVisible.value = true
  } catch (error) {
    console.error('加载配置详情失败:', error)
    ElMessage.error('加载配置失败：' + (error.message || '未知错误'))
  }
}

function enterEditMode() {
  isEditMode.value = true
}

async function handleVisualSave(configData) {
  try {
    const data = {
      ...currentConfig.value,
      ...editFormData.value,
      ...configData
    }
    
    const success = await composableSave(data, !data.configId)
    if (success) {
      visualEditorVisible.value = false
      ElMessage.success('保存成功')
      getList()
    }
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败：' + (error.message || '未知错误'))
  }
}

async function handleEditSubmit() {
  try {
    const splitData = splitConfigFields(combinedConfigContent.value)
    const data = {
      ...currentConfig.value,
      ...editFormData.value,
      ...splitData
    }
    
    const success = await composableSave(data, !data.configId)
    if (success) {
      ElMessage.success('保存成功')
      viewDialogVisible.value = false
    }
  } catch (error) {
    ElMessage.error('保存失败：' + (error.message || '未知错误'))
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确认要删除配置 "${row.configName}" 吗？`,
      '警告',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    await composableDelete(row)
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败：' + (error.message || '未知错误'))
    }
  }
}

async function handleVerify(row) {
  try {
    const result = await verifyFieldConsistency(row.moduleCode)
    validationReport.value = generateValidationReport(result)
    currentConfig.value = row
    validationDialogVisible.value = true
  } catch (error) {
    ElMessage.error('验证失败：' + (error.message || '未知错误'))
  }
}

function handleCopy(row) {
  ElMessage.info('复制功能开发中')
}

function handleExport(row) {
  ElMessage.info('导出功能开发中')
}

async function loadAllDicts() {
  try {
    const [allRes, nationRes] = await Promise.all([
      request({ url: '/erp/engine/dict/all', method: 'get' }),
      request({ url: '/erp/engine/country/search', method: 'get', params: { keyword: '', limit: 100 } })
    ])
    
    // 处理 All 字典响应
    if (allRes.data) {
      const data = allRes.data
      if (data.dictTypeList && Array.isArray(data.dictTypeList)) {
        const groupedDicts = {}
        if (data.dictDataList && Array.isArray(data.dictDataList)) {
          data.dictDataList.forEach(item => {
            const type = item.type
            if (type && !groupedDicts[type]) {
              groupedDicts[type] = []
            }
            if (type) {
              groupedDicts[type].push(item)
            }
          })
        }
        allDictsData.value = groupedDicts
      } else {
        allDictsData.value = data
      }
    }
    
    // 处理国家字典
    if (nationRes.data) {
      nationDictData.value = nationRes.data || []
    }
  } catch (error) {
    console.error('加载字典失败:', error)
    ElMessage.error('加载字典数据失败')
  }
}

// Lifecycle
onMounted(() => {
  getList()
  loadAllDicts()
})
</script>

<template>
  <div class="app-container">
    <el-card shadow="never">
      <!-- Header -->
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span class="title">ERP 公共配置管理</span>
          </div>
          <div class="header-right">
            <el-button type="primary" icon="Plus" @click="handleAdd">
              新增配置
            </el-button>
            <el-button type="warning" icon="Delete" @click="handleClearCache">
              清理缓存
            </el-button>
            <el-button icon="Refresh" @click="resetQuery">刷新</el-button>
          </div>
        </div>
      </template>

      <!-- 页签 -->
      <el-tabs v-model="activeTab" class="config-tabs">
        <!-- 页面配置 -->
        <el-tab-pane label="页面配置" name="config">
          <div class="tab-content">
            <!-- 搜索表单 -->
            <ConfigSearch
              v-model="queryParams"
              @query="handleQuery"
              @reset="resetQuery"
            />
            
            <!-- 表格列表 -->
            <ConfigTable
              :config-list="configList"
              :loading="loading"
              @view="handleView"
              @delete="handleDelete"
              @copy="handleCopy"
              @export="handleExport"
              @verify="handleVerify"
            />
            
            <!-- 分页 -->
            <Pagination
              v-show="total > 0"
              v-model:page="queryParams.pageNum"
              v-model:limit="queryParams.pageSize"
              :total="total"
              @pagination="getList"
            />
          </div>
        </el-tab-pane>

        <!-- 字典接口 -->
        <el-tab-pane label="字典接口" name="lowcode">
          <div class="tab-content">
            <el-card shadow="never" class="dict-card">
              <template #header>
                <div class="dict-header">
                  <span>字典接口数据展示</span>
                  <el-button type="primary" size="small" icon="Refresh" @click="loadAllDicts">
                    刷新数据
                  </el-button>
                </div>
              </template>

              <el-tabs v-model="dictActiveTab" type="card">
                <el-tab-pane label="All 字典接口" name="all">
                  <div class="dict-section">
                    <div class="dict-info">
                      <el-tag type="info" size="small">接口：/erp/engine/dict/all</el-tag>
                      <el-tag type="success" size="small">
                        共 {{ Object.keys(allDictsData).length }} 个字典类型
                      </el-tag>
                    </div>
                    <JsonEditor
                      v-model="allDictsJson"
                      readonly
                      height="400px"
                    />
                  </div>
                </el-tab-pane>
              
                <el-tab-pane label="国家字典" name="nation">
                  <div class="dict-section">
                    <div class="dict-info">
                      <el-tag type="info" size="small">接口：/erp/engine/country/search</el-tag>
                      <el-tag type="success" size="small">
                        共 {{ nationDictData.length }} 条数据
                      </el-tag>
                    </div>
                    <JsonEditor
                      v-model="nationDictsJson"
                      readonly
                      height="400px"
                    />
                  </div>
                </el-tab-pane>
              </el-tabs>
            </el-card>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 配置查看对话框 -->
    <el-dialog
      v-model="viewDialogVisible"
      :title="isEditMode ? '编辑配置' : '查看配置'"
      width="90%"
      top="5vh"
      class="config-view-dialog"
    >
      <JsonEditor
        v-model="combinedConfigContent"
        :readonly="!isEditMode"
        height="calc(100vh - 400px)"
        :autofocus="isEditMode"
      />
      
      <template #footer>
        <div v-if="!isEditMode">
          <el-button @click="viewDialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="enterEditMode">编辑配置</el-button>
        </div>
        <div v-else>
          <el-button type="primary" @click="handleEditSubmit">保存配置</el-button>
          <el-button @click="viewDialogVisible = false">取消</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 可视化编辑器对话框 -->
    <el-dialog
      v-model="visualEditorVisible"
      :title="currentConfig?.configId ? '编辑配置' : '新增配置'"
      width="95%"
      top="2vh"
      class="visual-editor-dialog"
      :close-on-click-modal="false"
    >
      <VisualConfigEditor
        :config-id="currentConfig?.configId"
        :initial-data="currentConfig"
        @save="handleVisualSave"
        @cancel="visualEditorVisible = false"
      />
    </el-dialog>

    <!-- 字段验证报告对话框 -->
    <el-dialog
      v-model="validationDialogVisible"
      title="字段一致性验证报告"
      width="80%"
      top="5vh"
      class="validation-dialog"
    >
      <div v-html="validationReport" class="validation-report"></div>
      
      <template #footer>
        <el-button type="primary" @click="validationDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 历史对话框（待实现） -->
    <!-- TODO: 当实现历史功能时，取消下面的注释 -->
    <!-- 
    <el-dialog
      v-model="historyDialogVisible"
      title="配置历史版本"
      width="90%"
      top="5vh"
      :close-on-click-modal="false"
    >
      <el-alert
        title="当前配置"
        type="info"
        :closable="false"
        show-icon
        class="mb-4"
      >
        <template #default>
          <el-descriptions :column="3" size="small">
            <el-descriptions-item label="配置名称">
              {{ currentHistoryConfig.configName }}
            </el-descriptions-item>
            <el-descriptions-item label="模块编码">
              {{ currentHistoryConfig.moduleCode }}
            </el-descriptions-item>
            <el-descriptions-item label="当前版本">
              <el-tag size="small">v{{ currentHistoryConfig.version }}</el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </template>
      </el-alert>

      <el-table
        v-loading="historyLoading"
        :data="versionList"
        border
        stripe
        highlight-current-row
      >
        <el-table-column label="序号" type="index" width="60" align="center" />
        <el-table-column prop="version" label="版本号" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.version === currentHistoryConfig.version ? 'success' : 'info'">
              v{{ scope.row.version }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="configContent" label="配置内容" min-width="400" :show-overflow-tooltip="true">
          <template #default="scope">
            <el-button link type="primary" @click="viewVersion(scope.row)">
              查看
            </el-button>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="160" />
        <el-table-column prop="updateBy" label="更新人" width="100" />
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="scope">
            <el-button
              link
              type="primary"
              icon="Clock"
              :disabled="scope.row.version === currentHistoryConfig.version"
              @click="rollbackVersion(scope.row)"
            >
              回滚
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="historyDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
    -->
  </div>
</template>

<style scoped lang="scss">
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;
    
    .title {
      font-size: 16px;
      font-weight: 500;
      color: var(--el-text-color-primary);
    }
  }
  
  .header-right {
    display: flex;
    gap: 8px;
  }
}

.config-tabs {
  margin-top: 16px;
}

.tab-content {
  min-height: 400px;
}

/* 字典卡片样式 */
.dict-card {
  .dict-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .dict-section {
    padding: 16px 0;
  }
  
  .dict-info {
    margin-bottom: 12px;
    display: flex;
    align-items: center;
    gap: 8px;
  }
}

.config-view-dialog {
  :deep(.el-dialog__header) {
    padding-bottom: 12px;
  }
}

.visual-editor-dialog {
  :deep(.el-dialog__body) {
    padding: 0;
    height: calc(100vh - 120px);
    overflow: hidden;
  }
}

.validation-dialog {
  .validation-report {
    max-height: 60vh;
    overflow-y: auto;
    padding: 16px;
    background: #f5f7fa;
    border-radius: 4px;
  }
}
</style>
