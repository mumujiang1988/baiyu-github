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
            <el-button icon="Refresh" @click="refreshList">刷新</el-button>
          </div>
        </div>
      </template>

      <!-- 页签结构 -->
      <el-tabs v-model="activeTab" class="config-tabs">
        <!-- 第一页签：页面配置 -->
        <el-tab-pane label="页面配置" name="config">
          <div class="tab-content">

      <!-- Search Form -->
      <el-form :model="queryParams" :inline="true" label-width="70px">
        <el-row :gutter="8">
          <el-col :span="6">
            <el-form-item label="模块编码">
              <el-input
                v-model="queryParams.moduleCode"
                placeholder="请输入模块编码"
                clearable
                style="width: 100%"
                @keyup.enter="handleQuery"
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="配置名称">
              <el-input
                v-model="queryParams.configName"
                placeholder="请输入配置名称"
                clearable
                style="width: 100%"
                @keyup.enter="handleQuery"
              />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="配置类型">
              <el-select
                v-model="queryParams.configType"
                placeholder="请选择配置类型"
                clearable
                style="width: 100%"
              >
                <el-option
                  v-for="option in getConfigTypeOptions()"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="3">
            <el-form-item label="状态">
              <el-select
                v-model="queryParams.status"
                placeholder="请选择状态"
                clearable
                style="width: 100%"
              >
                <el-option label="正常" value="1" />
                <el-option label="停用" value="0" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="3">
            <el-form-item>
              <el-button type="primary" icon="Search" @click="handleQuery">
                搜索
              </el-button>
              <el-button icon="Refresh" @click="resetQuery">
                重置
              </el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <!-- Table -->
      <el-table
        v-loading="loading"
        :data="configList"
        border
        stripe
        highlight-current-row
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="序号" type="index" width="60" align="center" />
        <el-table-column prop="configName" label="配置名称" min-width="200" :show-overflow-tooltip="true" />
        <el-table-column prop="moduleCode" label="模块编码" width="150" />
        <el-table-column prop="configType" label="配置类型" width="120" align="center">
          <template #default="scope">
            <el-tag :type="getConfigTypeTag(scope.row.configType)">
              {{ getConfigTypeLabel(scope.row.configType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="version" label="版本号" width="80" align="center" />
        <el-table-column prop="isPublic" label="公共" width="70" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.isPublic === '1' ? 'success' : 'info'">
              {{ scope.row.isPublic === '1' ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="70" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === '1' ? 'success' : 'danger'">
              {{ scope.row.status === '1' ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" :show-overflow-tooltip="true" />
        <el-table-column prop="updateTime" label="更新时间" width="160" />
        <el-table-column label="操作" width="300" fixed="right" align="center">
          <template #default="scope">
            <el-button
              link
              type="primary"
              icon="View"
              @click.stop="handleView(scope.row)"
            >
              查看
            </el-button>
            <el-button
              link
              type="primary"
              icon="Clock"
              @click.stop="handleHistory(scope.row)"
            >
              历史
            </el-button>
            <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, scope.row)">
              <el-button link type="primary" icon="More">
                更多
                <el-icon><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="copy" icon="DocumentCopy">
                    复制配置
                  </el-dropdown-item>
                  <el-dropdown-item command="export" icon="Download">
                    导出配置
                  </el-dropdown-item>
                  <el-dropdown-item divided command="delete" icon="Delete">
                    删除配置
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <Pagination
        v-show="total > 0"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        :total="total"
        @pagination="getList"
      />
          </div>
        </el-tab-pane>

        <!-- Dictionary Tab -->
        <el-tab-pane label="字典接口" name="lowcode">
          <div class="tab-content">
            <!-- 字典接口展示 -->
            <el-card shadow="never" class="dict-card">
              <template #header>
                <div class="dict-header">
                  <span>字典接口数据展示</span>
                  <div class="header-actions">
                    <el-button type="primary" size="small" icon="Refresh" @click="loadAllDicts">
                      刷新数据
                    </el-button>
                  </div>
                </div>
              </template>

              <el-tabs v-model="dictActiveTab" type="card">
              <!-- All Dict -->
                <el-tab-pane label="All 字典接口" name="all">
                  <div class="dict-section">
                    <div class="dict-info">
                      <el-tag type="info" size="small">接口：/erp/engine/dict/all</el-tag>
                      <el-tag type="success" size="small" style="margin-left: 8px">
                        共 {{ Object.keys(allDictsData).length }} 个字典类型
                      </el-tag>
                    </div>
                    <div class="json-viewer">
                      <codemirror
                        v-model="allDictsJson"
                        :extensions="[json()]"
                        :style="{ height: '400px' }"
                        :readonly="true"
                      />
                    </div>
                  </div>
                </el-tab-pane>
              
              <!-- Country Dict -->
                <el-tab-pane label="国家字典" name="nation">
                  <div class="dict-section">
                    <div class="dict-info">
                      <el-tag type="info" size="small">接口：/erp/engine/country/search</el-tag>
                      <el-tag type="success" size="small" style="margin-left: 8px">
                        共 {{ nationDictData.length }} 条数据
                      </el-tag>
                    </div>
                    <div class="json-viewer">
                      <codemirror
                        v-model="nationDictJson"
                        :extensions="[json()]"
                        :style="{ height: '400px' }"
                        :readonly="true"
                      />
                    </div>
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
      :close-on-click-modal="false"
      class="config-view-dialog"
    >
      <!-- 查看模式 -->
      <div v-if="!isEditMode">
        <el-descriptions :column="3" border size="small">
          <el-descriptions-item label="配置 ID">{{ currentConfig.configId }}</el-descriptions-item>
          <el-descriptions-item label="模块编码">{{ currentConfig.moduleCode }}</el-descriptions-item>
          <el-descriptions-item label="配置名称">{{ currentConfig.configName }}</el-descriptions-item>
          <el-descriptions-item label="配置类型">
            <el-tag :type="getConfigTypeTag(currentConfig.configType)" size="small">
              {{ getConfigTypeLabel(currentConfig.configType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="版本号">v{{ currentConfig.version }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentConfig.status === '1' ? 'success' : 'danger'" size="small">
              {{ currentConfig.status === '1' ? '正常' : '停用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="是否公共">{{ currentConfig.isPublic === '1' ? '是' : '否' }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentConfig.createTime }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ currentConfig.updateTime }}</el-descriptions-item>
          <el-descriptions-item label="备注">{{ currentConfig.remark }}</el-descriptions-item>
        </el-descriptions>

        <el-divider>配置内容</el-divider>

        <div class="config-content">
          <codemirror
            v-model="combinedConfigContent"
            :extensions="[json()]"
            :style="{ height: '45vh' }"
            :readonly="true"
            placeholder="配置内容为空"
          />
        </div>
      </div>

      <!-- Edit Mode -->
      <div v-else>
        <el-descriptions :column="3" border size="small">
          <el-descriptions-item label="配置 ID">
            {{ editFormData.configId || '新增中' }}
          </el-descriptions-item>
          <el-descriptions-item label="模块编码">
            <el-input
              v-model="editFormData.moduleCode"
              placeholder="如：saleorder, purchaseorder"
              :disabled="!!editFormData.configId"
              maxlength="50"
              size="small"
            />
          </el-descriptions-item>
          <el-descriptions-item label="配置名称">
            <el-input
              v-model="editFormData.configName"
              placeholder="请输入配置名称"
              maxlength="100"
              size="small"
            />
          </el-descriptions-item>
          <el-descriptions-item label="配置类型">
            <el-select
              v-model="editFormData.configType"
              placeholder="请选择"
              size="small"
              style="width: 100%"
              :disabled="!!editFormData.configId"
            >
              <el-option
                v-for="option in getConfigTypeOptions()"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              />
            </el-select>
          </el-descriptions-item>
          <el-descriptions-item label="版本号">
            <el-tag size="small">v{{ editFormData.version || 1 }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="是否公共">
            <el-radio-group v-model="editFormData.isPublic" size="small">
              <el-radio label="1">是</el-radio>
              <el-radio label="0">否</el-radio>
            </el-radio-group>
          </el-descriptions-item>
          <el-descriptions-item label="备注" :span="3">
            <el-input
              v-model="editFormData.remark"
              type="textarea"
              :rows="2"
              placeholder="请输入备注信息"
              maxlength="500"
              show-word-limit
            />
          </el-descriptions-item>
        </el-descriptions>

        <el-divider>配置内容</el-divider>

        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px">
          <span style="font-size: 14px; color: var(--el-text-color-regular)">JSON 格式配置</span>
          <el-button
            type="info"
            size="small"
            icon="MagicStick"
            @click="formatEditJson"
          >
            格式化 JSON
          </el-button>
        </div>
        <div class="config-content">
          <codemirror
            v-model="editCombinedConfigContent"
            :extensions="[json()]"
            :style="{ height: 'calc(100vh - 400px)', minHeight: '400px' }"
            :autofocus="true"
            :indent-with-tab="true"
            :tab-size="2"
          />
        </div>
        <el-alert
          v-if="jsonError"
          type="error"
          :closable="false"
          show-icon
          class="mt-2"
        >
          {{ jsonError }}
        </el-alert>
        <el-alert
          v-else-if="jsonValid"
          type="success"
          :closable="false"
          show-icon
          class="mt-2"
        >
          JSON 格式验证通过
        </el-alert>

        <div style="margin-top: 16px">
          <el-input
            v-model="editFormData.changeReason"
            type="textarea"
            :rows="2"
            placeholder="请填写本次变更的原因说明（选填）"
            maxlength="500"
            show-word-limit
          />
        </div>
      </div>

      <!-- Footer -->
      <template #footer>
        <div v-if="!isEditMode">
          <el-button @click="viewDialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="enterEditMode">编辑配置</el-button>
        </div>
        <div v-else>
          <el-button type="primary" @click="handleEditSubmit" :loading="submitLoading">
            保存配置
          </el-button>
          <el-button @click="cancelEditMode">取消编辑</el-button>
        </div>
      </template>
    </el-dialog>



    <!-- 配置历史对话框 -->
    <el-dialog
      v-model="historyDialogVisible"
      title="配置历史版本"
      width="90%"
      top="5vh"
      :close-on-click-modal="false"
    >
      <!-- 当前配置信息 -->
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

      <!-- Version List -->
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
  </div>
</template>

<script setup name="ErpConfig">
import { listConfig, delConfig, getConfig, saveConfig, getConfigHistory, rollbackToVersion } from '../api/config'
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import Pagination from '@/components/Pagination'
import { getConfigTypeLabel, getConfigTypeTag, getConfigTypeOptions } from '@/constants/configTypes'
import { Codemirror } from 'vue-codemirror'
import { json } from '@codemirror/lang-json'
import request from '@/utils/request'

// ==================== State ====================
// 页签状态
const activeTab = ref('config')
const dictActiveTab = ref('all')

// 字典数据状态
const allDictsData = ref({})
const nationDictData = ref([])
const dictLoading = ref(false)

// 字典 JSON 展示
const allDictsJson = computed(() => JSON.stringify(allDictsData.value, null, 2))
const nationDictJson = computed(() => JSON.stringify(nationDictData.value, null, 2))
const loading = ref(false)
const total = ref(0)
const configList = ref([])

// Combined config content (JSON display)
const combinedConfigContent = computed(() => {
  const configObj = {}
  if (currentConfig.value.pageConfig) {
    try { configObj.pageConfig = JSON.parse(currentConfig.value.pageConfig) } catch { configObj.pageConfig = currentConfig.value.pageConfig }
  }
  if (currentConfig.value.formConfig) {
    try { configObj.formConfig = JSON.parse(currentConfig.value.formConfig) } catch { configObj.formConfig = currentConfig.value.formConfig }
  }
  if (currentConfig.value.tableConfig) {
    try { configObj.tableConfig = JSON.parse(currentConfig.value.tableConfig) } catch { configObj.tableConfig = currentConfig.value.tableConfig }
  }
  if (currentConfig.value.searchConfig) {
    try { configObj.searchConfig = JSON.parse(currentConfig.value.searchConfig) } catch { configObj.searchConfig = currentConfig.value.searchConfig }
  }
  if (currentConfig.value.actionConfig) {
    try { configObj.actionConfig = JSON.parse(currentConfig.value.actionConfig) } catch { configObj.actionConfig = currentConfig.value.actionConfig }
  }
  if (currentConfig.value.apiConfig) {
    try { configObj.apiConfig = JSON.parse(currentConfig.value.apiConfig) } catch { configObj.apiConfig = currentConfig.value.apiConfig }
  }
  if (currentConfig.value.dictConfig) {
    try { configObj.dictConfig = JSON.parse(currentConfig.value.dictConfig) } catch { configObj.dictConfig = currentConfig.value.dictConfig }
  }
  if (currentConfig.value.businessConfig) {
    try { configObj.businessConfig = JSON.parse(currentConfig.value.businessConfig) } catch { configObj.businessConfig = currentConfig.value.businessConfig }
  }
  if (currentConfig.value.detailConfig) {
    try { configObj.detailConfig = JSON.parse(currentConfig.value.detailConfig) } catch { configObj.detailConfig = currentConfig.value.detailConfig }
  }
  return Object.keys(configObj).length > 0 ? JSON.stringify(configObj, null, 2) : '{}'
})

// Edit mode combined config (two-way binding)
const editCombinedConfigContent = computed({
  get() {
    const configObj = {}
    if (editFormData.pageConfig) {
      try { configObj.pageConfig = JSON.parse(editFormData.pageConfig) } catch { configObj.pageConfig = editFormData.pageConfig }
    }
    if (editFormData.formConfig) {
      try { configObj.formConfig = JSON.parse(editFormData.formConfig) } catch { configObj.formConfig = editFormData.formConfig }
    }
    if (editFormData.tableConfig) {
      try { configObj.tableConfig = JSON.parse(editFormData.tableConfig) } catch { configObj.tableConfig = editFormData.tableConfig }
    }
    if (editFormData.searchConfig) {
      try { configObj.searchConfig = JSON.parse(editFormData.searchConfig) } catch { configObj.searchConfig = editFormData.searchConfig }
    }
    if (editFormData.actionConfig) {
      try { configObj.actionConfig = JSON.parse(editFormData.actionConfig) } catch { configObj.actionConfig = editFormData.actionConfig }
    }
    if (editFormData.apiConfig) {
      try { configObj.apiConfig = JSON.parse(editFormData.apiConfig) } catch { configObj.apiConfig = editFormData.apiConfig }
    }
    if (editFormData.dictConfig) {
      try { configObj.dictConfig = JSON.parse(editFormData.dictConfig) } catch { configObj.dictConfig = editFormData.dictConfig }
    }
    if (editFormData.businessConfig) {
      try { configObj.businessConfig = JSON.parse(editFormData.businessConfig) } catch { configObj.businessConfig = editFormData.businessConfig }
    }
    if (editFormData.detailConfig) {
      try { configObj.detailConfig = JSON.parse(editFormData.detailConfig) } catch { configObj.detailConfig = editFormData.detailConfig }
    }
    return Object.keys(configObj).length > 0 ? JSON.stringify(configObj, null, 2) : '{}'
  },
  set(val) {
    // 解析用户输入的 JSON，并分散到各个字段
    try {
      const parsed = JSON.parse(val)
      editFormData.pageConfig = parsed.pageConfig ? JSON.stringify(parsed.pageConfig) : ''
      editFormData.formConfig = parsed.formConfig ? JSON.stringify(parsed.formConfig) : ''
      editFormData.tableConfig = parsed.tableConfig ? JSON.stringify(parsed.tableConfig) : ''
      editFormData.searchConfig = parsed.searchConfig ? JSON.stringify(parsed.searchConfig) : ''
      editFormData.actionConfig = parsed.actionConfig ? JSON.stringify(parsed.actionConfig) : ''
      editFormData.apiConfig = parsed.apiConfig ? JSON.stringify(parsed.apiConfig) : ''
      editFormData.dictConfig = parsed.dictConfig ? JSON.stringify(parsed.dictConfig) : ''
      editFormData.businessConfig = parsed.businessConfig ? JSON.stringify(parsed.businessConfig) : ''
      editFormData.detailConfig = parsed.detailConfig ? JSON.stringify(parsed.detailConfig) : ''
    } catch (e) {
      console.error('JSON 解析失败:', e)
    }
  }
})

// Dialog state
const viewDialogVisible = ref(false)
const historyDialogVisible = ref(false)

const currentConfig = ref({})
const currentHistoryConfig = ref({})
const versionList = ref([])
const historyLoading = ref(false)

// Edit state
const isEditMode = ref(false)
const submitLoading = ref(false)
const jsonValid = ref(false)
const jsonError = ref('')

const editFormData = reactive({
  configId: null,
  moduleCode: '',
  configName: '',
  configType: '',
  pageConfig: '',
  formConfig: '',
  tableConfig: '',
  searchConfig: '',
  actionConfig: '',
  apiConfig: '',
  dictConfig: '',
  businessConfig: '',
  detailConfig: '',
  isPublic: '0',
  remark: '',
  changeReason: ''
})

const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  moduleCode: '',
  configName: '',
  configType: '',
  status: ''
})

// ==================== Removed Rules ====================
// const editFormRules = { ... }

// ==================== Methods ====================

/**
 * Load config list
 */
function getList() {
  loading.value = true
  listConfig(queryParams)
    .then(res => {
    // Adapt ErpResponse wrapped TableDataInfo structure
      const data = res.data || res
      configList.value = data.rows || []
      total.value = data.total !== undefined ? data.total : 0
    })
    .catch(error => {
      console.error('查询配置列表失败:', error)
      configList.value = []
      total.value = 0
      ElMessage.error('加载数据失败')
    })
    .finally(() => {
      loading.value = false
    })
}

/**
 * Search
 */
function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

/**
 * Reset
 */
function resetQuery() {
  queryParams.pageNum = 1
  queryParams.moduleCode = ''
  queryParams.configName = ''
  queryParams.configType = ''
  queryParams.status = ''
  getList()
}

/**
 * Refresh
 */
function refreshList() {
  resetQuery()
}

/**
 * Clear cache
 */
function handleClearCache() {
  ElMessageBox.confirm(
    '确定要清理所有 ERP 配置缓存吗？清理后系统将重新从数据库加载配置。',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    return request({
      url: '/erp/cache/clear-all',
      method: 'delete'
    })
  }).then(res => {
    ElMessage.success('缓存清理成功')
  }).catch(error => {
    if (error !== 'cancel') {
      ElMessage.error('缓存清理失败：' + (error.message || '未知错误'))
    }
  })
}

/**
 * Add
 */
function handleAdd() {
  isEditMode.value = false
  resetEditForm()
  // Open dialog directly for new config
  viewDialogVisible.value = true
}

/**
 * Edit
 */
function handleEdit(row) {
  isEditMode.value = false
  currentConfig.value = { ...row }
  
  // Load edit data and open dialog
  loadEditData(row.configId)
    .then(() => {
      viewDialogVisible.value = true
    })
    .catch(error => {
    // Error already handled in loadEditData
    })
}

/**
 * Enter edit mode from view
 */
function enterEditMode() {
  isEditMode.value = true
  // Prepare edit data
  const data = currentConfig.value
  Object.assign(editFormData, {
    configId: data.configId,
    moduleCode: data.moduleCode,
    configName: data.configName,
    configType: data.configType,
    configContent: data.configContent,
    isPublic: data.isPublic,
    remark: data.remark,
    version: data.version || 1
  })
}

/**
 * Format JSON (edit mode)
 */
function formatEditJson() {
  // Parse JSON and distribute to fields
  try {
    const parsed = JSON.parse(editCombinedConfigContent.value)
    editFormData.pageConfig = parsed.pageConfig ? JSON.stringify(parsed.pageConfig, null, 2) : ''
    editFormData.formConfig = parsed.formConfig ? JSON.stringify(parsed.formConfig, null, 2) : ''
    editFormData.tableConfig = parsed.tableConfig ? JSON.stringify(parsed.tableConfig, null, 2) : ''
    editFormData.searchConfig = parsed.searchConfig ? JSON.stringify(parsed.searchConfig, null, 2) : ''
    editFormData.actionConfig = parsed.actionConfig ? JSON.stringify(parsed.actionConfig, null, 2) : ''
    editFormData.apiConfig = parsed.apiConfig ? JSON.stringify(parsed.apiConfig, null, 2) : ''
    editFormData.dictConfig = parsed.dictConfig ? JSON.stringify(parsed.dictConfig, null, 2) : ''
    editFormData.businessConfig = parsed.businessConfig ? JSON.stringify(parsed.businessConfig, null, 2) : ''
    editFormData.detailConfig = parsed.detailConfig ? JSON.stringify(parsed.detailConfig, null, 2) : ''
    jsonValid.value = true
    jsonError.value = ''
    ElMessage.success('JSON 格式化成功')
  } catch (e) {
    jsonValid.value = false
    jsonError.value = 'JSON 格式无效：' + e.message
    ElMessage.error('JSON 格式无效，无法格式化')
  }
}

/**
 * Cancel edit, return to view mode
 */
function cancelEditMode() {
  ElMessageBox.confirm('确定要取消编辑吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    isEditMode.value = false
    // No need to clear validation (el-form removed)
  })
}

/**
 * Load edit data
 */
function loadEditData(configId) {
  return getConfig(configId)
    .then(res => {
      // 处理 ErpResponse 包装结构
      let data
      if (res.code === 200 || res.code === 0) {
        // Success response, extract data field
        data = res.data || {}
      } else {
        // Error response
        throw new Error(res.msg || '加载配置失败')
      }
      
      // Check required fields
      if (!data.configId) {
        throw new Error('配置不存在或已删除')
      }
      
      // Update edit form data (use backend fields directly)
      Object.assign(editFormData, {
        configId: data.configId,
        moduleCode: data.moduleCode || '',
        configName: data.configName || '',
        configType: data.configType || '',
        pageConfig: data.pageConfig || '',
        formConfig: data.formConfig || '',
        tableConfig: data.tableConfig || '',
        searchConfig: data.searchConfig || '',
        actionConfig: data.actionConfig || '',
        apiConfig: data.apiConfig || '',
        dictConfig: data.dictConfig || '',
        businessConfig: data.businessConfig || '',
        detailConfig: data.detailConfig || '',
        status: data.status || '1',
        isPublic: data.isPublic || '0',
        remark: data.remark || '',
        version: data.version || 1
      })
      
      return data
    })
    .catch(error => {
      ElMessage.error('加载配置数据失败：' + (error.message || '未知错误'))
      throw error // 继续抛出错误，让调用者处理
    })
}

/**
 * Reset edit form
 */
function resetEditForm() {
  Object.assign(editFormData, {
    configId: null,
    moduleCode: '',
    configName: '',
    configType: '',
    configContent: '',
    isPublic: '0',
    remark: '',
    changeReason: ''
  })
  jsonValid.value = false
  jsonError.value = ''
  if (editFormRef.value) {
    editFormRef.value.clearValidate()
  }
}

// ==================== Removed Methods ====================

/**
 * Submit edit form
 */
function handleEditSubmit() {
  // No form validation needed (el-form removed)
  // Direct save logic
  submitLoading.value = true

  const data = {
    configId: editFormData.configId,
    moduleCode: editFormData.moduleCode,
    configName: editFormData.configName,
    configType: editFormData.configType,
    pageConfig: editFormData.pageConfig,
    formConfig: editFormData.formConfig,
    tableConfig: editFormData.tableConfig,
    searchConfig: editFormData.searchConfig,
    actionConfig: editFormData.actionConfig,
    apiConfig: editFormData.apiConfig,
    dictConfig: editFormData.dictConfig,
    businessConfig: editFormData.businessConfig,
    detailConfig: editFormData.detailConfig,
    status: editFormData.status || '1',
    isPublic: editFormData.isPublic,
    remark: editFormData.remark,
    changeReason: editFormData.changeReason,
    version: editFormData.version || 1
  }

  saveConfig(data)
    .then(res => {
      ElMessage.success(isEditMode.value ? '修改成功' : '新增成功')
      viewDialogVisible.value = false
      getList()
    })
    .catch(error => {
      ElMessage.error('保存失败：' + (error.message || '未知错误'))
    })
    .finally(() => {
      submitLoading.value = false
    })
}

/**
 * View
 */
function handleView(row) {
  // Initialize with list data
  currentConfig.value = { ...row }
  isEditMode.value = false
  viewDialogVisible.value = true
  
  // Load full data asynchronously (updates after dialog opens)
  loadEditData(row.configId)
    .then(() => {
      // Update currentConfig after data loads successfully
      currentConfig.value = { ...editFormData }
    })
    .catch(error => {
      // Set default value for configContent to avoid undefined
      if (!currentConfig.value.configContent) {
        currentConfig.value.configContent = '{\n  "error": "配置加载失败，请稍后重试"\n}'
      }
    })
}

/**
 * View history
 */
function handleHistory(row) {
  currentHistoryConfig.value = { ...row }
  loadHistoryData(row.configId)
  historyDialogVisible.value = true
}

/**
 * Load history data
 */
function loadHistoryData(configId) {
  historyLoading.value = true
  getConfigHistory(configId)
    .then(res => {
      versionList.value = res.data || []
    })
    .finally(() => {
      historyLoading.value = false
    })
}

/**
 * View version details
 */
function viewVersion(row) {
  ElMessageBox.alert(
    `<pre>${JSON.stringify(JSON.parse(row.configContent), null, 2)}</pre>`,
    `版本 ${row.version} 详情`,
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '关闭'
    }
  )
}

/**
 * Rollback
 */
function rollbackVersion(row) {
  ElMessageBox.confirm(
    `确定要回滚到版本 ${row.version} 吗？`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    rollbackToVersion({
      configId: currentHistoryConfig.value.configId,
      targetVersion: row.version
    }).then(res => {
      ElMessage.success('回滚成功')
      historyDialogVisible.value = false
      getList()
    })
  })
}

/**
 * Dropdown command
 */
function handleCommand(command, row) {
  switch (command) {
    case 'copy':
      handleCopy(row)
      break
    case 'export':
      handleExport(row)
      break
    case 'delete':
      handleDelete(row)
      break
  }
}

/**
 * Copy config
 */
function handleCopy(row) {
  ElMessage.info('复制功能开发中')
}

/**
 * Export config
 */
function handleExport(row) {
  ElMessage.info('导出功能开发中')
}

/**
 * Delete config
 */
function handleDelete(row) {
  ElMessageBox.confirm(
    `确认要删除配置 "${row.configName}" 吗？`,
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    return delConfig(row.configId)
  }).then(res => {
    ElMessage.success('删除成功')
    getList()
  })
}

// ==================== Lifecycle ====================
onMounted(() => {
  getList()
})

// ==================== Dict Methods ====================
/**
 * Load all dicts
 */
async function loadAllDicts() {
  dictLoading.value = true
  try {
    // Parallel load All dict and country dict
    const promises = [
      request({ url: '/erp/engine/dict/all', method: 'get' }).then(res => {
        if (res && (res.code === 200 || res.code === 0)) {
          // 🔧 Fix: Handle backend { dictTypeList, dictDataList } structure correctly
          const data = res.data || {}
          
          // 如果返回的是 { dictTypeList, dictDataList } 结构
          if (data.dictTypeList && Array.isArray(data.dictTypeList)) {
            // Extract all dict types
            const dictTypes = data.dictTypeList.map(item => item.type || item.value).filter(Boolean)
            
            // Group by type from dictDataList
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
            console.log('✅ All 字典加载成功:', Object.keys(groupedDicts).length, '个类型')
          } else {
            // Already grouped data
            allDictsData.value = data
          }
        }
        return { success: true, data: allDictsData.value }
      }).catch(error => {
        console.warn('加载 All 字典接口失败:', error)
        return { success: false, error }
      }),
      
      request({ url: '/erp/engine/country/search', method: 'get', params: { keyword: '', limit: 100 } }).then(res => {
        if (res && (res.code === 200 || res.code === 0)) {
          nationDictData.value = res.data || []
        }
        return { success: true, data: res.data || [] }
      }).catch(error => {
        console.warn('加载国家字典接口失败:', error)
        return { success: false, error }
      })
    ]

    const results = await Promise.all(promises)
    
    // Count success and failed calls
    const successCount = results.filter(r => r.success).length
    const failCount = results.filter(r => !r.success).length
    
    if (successCount > 0) {
      ElMessage.success(`字典数据加载完成：成功${successCount}个，失败${failCount}个`)
    } else {
      ElMessage.warning('字典数据加载失败')
    }
  } catch (error) {
    console.error('加载字典数据异常:', error)
    ElMessage.error('加载字典数据时发生异常')
  } finally {
    dictLoading.value = false
  }
}

// ==================== Dict Methods (End) ====================

/**
 * Show generated config
 */
function showGeneratedConfig(dictConfig) {
  const jsonStr = JSON.stringify(dictConfig, null, 2)
  
  ElMessageBox.alert(
    `<pre style="max-height: 400px; overflow: auto; background: #f5f7fa; padding: 12px; border-radius: 4px;">${jsonStr}</pre>`,
    '生成的字典配置',
    {
      dangerouslyUseHTMLString: true,
      confirmButtonText: '关闭',
      type: 'success'
    }
  )
}
</script>

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

/* 搜索表单紧凑化样式 */
.el-form--inline .el-form-item {
  margin-right: 8px;
}

.el-form--inline .el-form-item:last-child {
  margin-right: 0;
}

.search-card {
  margin-bottom: 12px;
}

.search-card :deep(.el-card__body) {
  padding: 12px 12px 4px;
}

.table-card {
  margin-top: 12px;
}

/* 输入框和选择框高度调整 */
.el-form-item :deep(.el-input__wrapper),
.el-form-item :deep(.el-select-wrapper) {
  height: 36px;
}

.el-form-item :deep(.el-input__inner),
.el-form-item :deep(.el-select__input) {
  font-size: 14px;
}

.config-content {
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
}

.form-tip {
  color: var(--el-text-color-secondary);
  font-size: 12px;
  line-height: 1.5;
  margin-top: 4px;
}

.mt-2 {
  margin-top: 8px;
}

/* 查看/编辑对话框样式优化 */
.config-view-dialog :deep(.el-dialog__header) {
  padding-bottom: 12px;
  margin-right: 0;
}

.config-view-dialog :deep(.el-descriptions__label) {
  width: 120px;
  font-weight: 500;
}

.config-view-dialog :deep(.el-descriptions__content) {
  overflow-wrap: break-word;
}

.config-view-dialog :deep(.el-divider) {
  margin: 16px 0;
}

/* 页签样式 */
.config-tabs {
  margin-top: 16px;
}

.config-tabs :deep(.el-tabs__header) {
  margin-bottom: 16px;
}

.tab-content {
  min-height: 400px;
}

/* 字典卡片样式 */
.dict-card {
  margin-bottom: 16px;
}

.dict-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .header-actions {
    display: flex;
    gap: 8px;
  }
}

.dict-section {
  padding: 16px 0;
}

.dict-info {
  margin-bottom: 12px;
  display: flex;
  align-items: center;
}

.json-viewer {
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  overflow: hidden;
}
</style>
