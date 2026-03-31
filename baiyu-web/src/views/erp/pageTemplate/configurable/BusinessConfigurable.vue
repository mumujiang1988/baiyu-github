<template>
  <div class="app-container">
    <div v-if="!dictLoaded" class="dict-loading-container">
      <el-icon class="is-loading" :size="40"><Loading /></el-icon>
      <p>字典数据加载中...</p>
    </div>
    
    <el-card shadow="never" class="search-card" v-else-if="parsedConfig.search?.showSearch">
      <div class="page-header" v-if="pageTitle">
        <el-icon :size="20" v-if="parsedConfig.page?.icon"><component :is="parsedConfig.page.icon" /></el-icon>
        <span class="page-title">{{ pageTitle }}</span>
      </div>
      
      <div class="toolbar-row" v-if="leftToolbarActions.length > 0">
        <el-space wrap>
          <el-button
            v-for="action in leftToolbarActions"
            :key="action.label"
            :type="action.type"
            :icon="action.icon"
            :disabled="getButtonDisabled(action.disabled)"
            @click="handleAction(action.handler)"
            v-hasPermi="action.permission ? [action.permission] : []"
          >
            {{ action.label }}
          </el-button>
        </el-space>
      </div>
      
      <el-form :model="queryParams" ref="queryRef" :inline="true" label-width="70px" size="default" class="search-form">
        <template v-for="field in parsedConfig.search?.fields" :key="field.field">
          <el-form-item :label="field.label" :prop="field.field">
            <el-date-picker
              v-if="field.component === 'daterange'"
              v-model="dateRange"
              :type="field.component"
              range-separator="至"
              :start-placeholder="field.props.startPlaceholder"
              :end-placeholder="field.props.endPlaceholder"
              :value-format="field.props.valueFormat"
              :style="field.props.style"
              @change="handleQuery"
            />
            
            <el-date-picker
              v-else-if="field.component === 'date'"
              v-model="queryParams[field.field]"
              :type="field.component"
              :placeholder="field.props.placeholder"
              :value-format="field.props.valueFormat"
              :style="field.props.style"
              :clearable="field.props.clearable"
              @change="handleQuery"
            />
            
            <el-input
              v-else-if="field.component === 'input'"
              v-model="queryParams[field.field]"
              :placeholder="field.props.placeholder"
              :clearable="field.props.clearable"
              :style="field.props.style"
              @keyup.enter="handleQuery"
            >
              <template #prefix v-if="field.props.prefixIcon">
                <el-icon><component :is="field.props.prefixIcon" /></el-icon>
              </template>
            </el-input>
            
            <el-select
              v-else-if="field.component === 'select'"
              v-model="queryParams[field.field]"
              :placeholder="field.props.placeholder"
              :clearable="field.props.clearable"
              :filterable="field.props.filterable"
              :style="field.props.style"
            >
              <el-option
                v-for="option in getDictOptions(field.dictionary, field.options, field.dictRequired)"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              />
            </el-select>
          </el-form-item>
        </template>
        
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="table-card" v-if="dictLoaded">
      <div class="table-wrapper">
        <el-table
          v-loading="loading"
          :data="tableData"
          :row-key="parsedConfig.table?.rowKey || 'id'"
          :border="parsedConfig.table?.border ?? true"
          :stripe="parsedConfig.table?.stripe ?? true"
          :show-overflow-tooltip="parsedConfig.table?.showOverflowTooltip ?? true"
          :resizable="parsedConfig.table?.resizable ?? true"
          @selection-change="handleSelectionChange"
          @row-click="handleRowClick"
        >
        <template v-for="(column, index) in visibleColumns" :key="index">
          <el-table-column
            v-if="column.type === 'selection'"
            :type="column.type"
            :width="column.width"
            :fixed="column.fixed"
            :resizable="column.resizable"
          />
          
          <el-table-column
            v-else-if="column.type === 'expand'"
            :width="column.width"
            :fixed="column.fixed"
            :label="column.label"
            align="center"
          >
            <template #default="scope">
              <el-button
                type="primary"
                link
                icon="View"
                @click.stop="handleViewDetail(scope.row)"
              >
                查看
              </el-button>
            </template>
          </el-table-column>
                    
          <el-table-column
            v-else"
            :prop="column.prop"
            :label="column.label"
            :width="column.width"
            :min-width="column.minWidth"
            :fixed="column.fixed"
            :align="column.align"
            :sortable="column.sortable"
            :show-overflow-tooltip="column.showOverflowTooltip"
            :resizable="column.resizable"
          >
            <template #default="scope">
              <el-tag
                v-if="column.renderType === 'tag'"
                :type="getTagConfig(scope.row[column.prop], column.dictionary).type"
                size="small"
                disable-transitions
              >
                {{ getTagConfig(scope.row[column.prop], column.dictionary).label }}
              </el-tag>
              
              <el-link
                v-else-if="column.renderType === 'link'"
                type="primary"
                :underline="false"
              >
                {{ scope.row[column.prop] }}
              </el-link>
              
              <span v-else-if="column.renderType === 'text' && column.dictionary">
                {{ getDictLabel(scope.row[column.prop], column.dictionary) }}
              </span>
              
              <span v-else-if="column.renderType === 'currency'">
                {{ formatCurrency(scope.row[column.prop], column.precision) }}
              </span>
              
              <span v-else-if="column.renderType === 'date'">
                {{ formatDate(scope.row[column.prop], column.format) }}
              </span>
              
              <span v-else-if="column.renderType === 'datetime'">
                {{ formatDateTime(scope.row[column.prop], column.format) }}
              </span>
              
              <span v-else-if="column.renderType === 'percent'">
                {{ formatPercent(scope.row[column.prop], column.precision) }}
              </span>
              
              <span v-else>
                {{ scope.row[column.prop] ?? '-' }}
              </span>
            </template>
          </el-table-column>
        </template>
        </el-table>
      </div>
      
      <div class="pagination-wrapper">
        <el-pagination
          v-show="total > 0"
          :total="total"
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          @size-change="handlePageSizeChange"
          @current-change="handlePageChange"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          background
        />
      </div>
    </el-card>

    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      :width="parsedConfig.form?.dialogWidth || '1000px'"
      append-to-body
      @close="handleDialogClose"
      :close-on-click-modal="false"
    >
      <el-form :model="formData" :rules="formRules" ref="formRef" :label-width="parsedConfig.form?.labelWidth || '120px'">
        <el-scrollbar max-height="65vh">
          <el-card
            v-for="(section, index) in parsedConfig.form?.sections || []"
            :key="index"
            shadow="never"
            class="form-section-card"
          >
            <template #header>
              <div class="card-header">
                <el-icon v-if="section.icon"><component :is="section.icon" /></el-icon>
                <span>{{ section.title }}</span>
              </div>
            </template>
            
            <el-row :gutter="20">
              <el-col
                v-for="field in section.fields"
                :key="field.field"
                :span="field.span || (24 / section.columns)"
              >
                <el-form-item :label="field.label" :prop="field.field">
                  <el-input
                    v-if="field.component === 'input'"
                    v-model="formData[field.field]"
                    v-bind="field.componentProps"
                    clearable
                  />
                  
                  <el-date-picker
                    v-else-if="['date', 'datetime'].includes(field.component)"
                    v-model="formData[field.field]"
                    :type="field.component"
                    :placeholder="field.componentProps?.placeholder || '选择日期'"
                    :value-format="field.componentProps?.valueFormat || 'YYYY-MM-DD'"
                    style="width: 100%"
                  />
                  
                  <el-input-number
                    v-else-if="field.component === 'input-number'"
                    v-model="formData[field.field]"
                    v-bind="field.componentProps"
                    style="width: 100%"
                  />
                  
                  <el-select
                    v-else-if="field.component === 'select'"
                    v-model="formData[field.field]"
                    :placeholder="field.props?.placeholder || field.componentProps?.placeholder || '请选择'"
                    :clearable="field.props?.clearable ?? field.componentProps?.clearable ?? true"
                    :filterable="field.props?.filterable ?? field.componentProps?.filterable ?? false"
                    :remote="field.props?.remote ?? field.componentProps?.remote"
                    :loading="field.dictionary === 'nation' ? nationSearchLoading : false"
                    :remote-method="field.dictionary === 'nation' ? searchNations : undefined"
                    style="width: 100%"
                  >
                    <el-option
                      v-for="option in getDictOptions(field.dictionary, field.options, field.dictRequired)"
                      :key="option.value"
                      :label="option.label"
                      :value="option.value"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
          </el-card>
          
          <el-card
            v-if="parsedConfig.form?.formTabs?.enabled"
            shadow="never"
            class="form-tabs-card"
          >
            <el-tabs v-model="formActiveTab" stretch>
              <el-tab-pane
                v-for="tab in parsedConfig.form.formTabs.tabs"
                :key="tab.name"
                :label="tab.label"
                :name="tab.name"
              >
                <div v-if="tab.name === 'entry' && tab.table" class="tab-pane-content">
                  <div class="tab-pane-toolbar">
                    <el-button
                      v-if="tab.table.addRow"
                      type="primary"
                      size="small"
                      icon="Plus"
                      @click="handleAddEntryRow"
                    >
                      添加明细
                    </el-button>
                  </div>
                  <el-table
                    :data="entryList"
                    border
                    size="small"
                    max-height="300"
                    stripe
                    class="entry-table"
                  >
                    <el-table-column
                      v-for="(col, index) in tab.table.columns"
                      :key="index"
                      :prop="col.prop"
                      :label="col.label"
                      :width="col.width"
                      :align="col.align || 'center'"
                      show-overflow-tooltip
                    >
                      <template #default="scope">
                        <el-input
                          v-if="!col.type || col.type === 'text'"
                          v-model="scope.row[col.prop]"
                          size="small"
                          clearable
                          :disabled="!col.editable"
                        />
                        <el-input-number
                          v-else-if="col.type === 'number'"
                          v-model="scope.row[col.prop]"
                          size="small"
                          :min="0"
                          :precision="2"
                          :step="0.01"
                          controls-position="right"
                          style="width: 100%"
                          :disabled="!col.editable"
                        />
                      </template>
                    </el-table-column>
                    <el-table-column
                      v-if="tab.table.deleteRow"
                      label="操作"
                      width="80"
                      align="center"
                      fixed="right"
                    >
                      <template #default="scope">
                        <el-button
                          type="danger"
                          size="small"
                          icon="Delete"
                          link
                          @click="handleDeleteEntryRow(scope.$index)"
                        >
                          删除
                        </el-button>
                      </template>
                    </el-table-column>
                  </el-table>
                </div>
                
                <div v-else-if="tab.name === 'cost' && tab.type === 'form'" class="tab-pane-content">
                  <el-row :gutter="20">
                    <el-col
                      v-for="field in tab.fields"
                      :key="field.field"
                      :span="field.span || (24 / tab.columns)"
                    >
                      <el-form-item :label="field.label" :prop="field.field">
                        <el-input-number
                          v-if="field.component === 'input-number'"
                          v-model="costData[field.field]"
                          v-bind="field.props"
                          style="width: 100%"
                        />
                        <el-input
                          v-else
                          v-model="costData[field.field]"
                          clearable
                          style="width: 100%"
                        />
                      </el-form-item>
                    </el-col>
                  </el-row>
                </div>
              </el-tab-pane>
            </el-tabs>
          </el-card>
        </el-scrollbar>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
            确 定
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 详情抽屉 -->
    <el-drawer
      v-model="drawerVisible"
      :title="drawerTitle"
      direction="rtl"
      size="60%"
      :before-close="handleDrawerClose"
      :close-on-click-modal="true"
      :modal="true"
    >
      <div v-if="drawerLoading" class="drawer-loading">
        <el-icon class="is-loading" :size="40"><Loading /></el-icon>
        <p>正在加载数据...</p>
      </div>
      <div v-else class="drawer-content">
        <el-tabs v-model="detailActiveTab" stretch>
          <el-tab-pane
            v-for="tab in parsedConfig.drawer?.tabs || []"
            :key="tab.name"
            :label="tab.label"
            :name="tab.name"
          >
            <div style="display:none;" v-text="debugTabConfig(tab)"></div>
            
            <div v-if="tab.type === 'table' || !tab.type" class="tab-content">
              <!-- 修复：使用 toRaw 转换 Proxy 为普通对象，确保数据访问正常 -->
              <template v-for="tabKey in [tab.name]" :key="tabKey">
                <div v-if="!getTabData(tab) || getTabData(tab).length === 0" class="tab-empty">
                  <el-empty :description="`暂无${tab.label}数据`" :image-size="120" />
                </div>
                <el-table 
                  v-else
                  :data="getTabData(tab)" 
                  size="small" 
                  border 
                  style="width: 100%" 
                  max-height="500"
                  stripe
                >
                  <el-table-column
                    v-for="col in tab.table?.columns || []"
                    :key="col.prop"
                    :prop="col.prop"
                    :label="col.label"
                    :width="col.width"
                    :show-overflow-tooltip="col.showOverflowTooltip || false"
                    :align="col.align || 'center'"
                  >
                    <template #default="{ row }">
                      <span v-if="col.renderType === 'currency'">{{ formatAmount(getFieldValue(row, col.prop)) }}</span>
                      <span v-else-if="col.renderType === 'number'">{{ getFieldValue(row, col.prop) }}</span>
                      <span v-else>{{ getFieldValue(row, col.prop) ?? '-' }}</span>
                    </template>
                  </el-table-column>
                </el-table>
              </template>
            </div>
            
            <div v-else-if="tab.type === 'form'" class="tab-content">
              <template v-for="tabKey in [tab.name]" :key="tabKey">
                <div style="display:none;" v-text="debugFormTabConfig(tab)"></div>
                
                <div v-if="!getTabData(tab) || Object.keys(getTabData(tab)).length === 0" class="tab-empty">
                  <el-empty :description="`暂无${tab.label}数据`" :image-size="120" />
                </div>
                <el-form 
                  v-else 
                  :model="getTabData(tab)" 
                  label-width="120px" 
                  size="small"
                >
                  <el-row :gutter="20">
                    <el-col
                      v-for="field in getFormFields(tab)"
                      :key="field.prop"
                      :span="field.span || 12"
                    >
                      <el-form-item :label="field.label">
                        <span v-if="field.renderType === 'currency'">{{ formatAmount(getFieldValue(getTabData(tab), field.prop)) }}</span>
                        <span v-else-if="field.renderType === 'percent'">{{ getFieldValue(getTabData(tab), field.prop) ? (getFieldValue(getTabData(tab), field.prop) + '%') : '-' }}</span>
                        <span v-else>{{ getFieldValue(getTabData(tab), field.prop) ?? '-' }}</span>
                      </el-form-item>
                    </el-col>
                  </el-row>
                </el-form>
              </template>
            </div>
            
            <div v-else-if="tab.type === 'descriptions'" class="tab-content">
              <template v-for="tabKey in [tab.name]" :key="tabKey">
                <div v-if="!getTabData(tab) || (Array.isArray(getTabData(tab)) ? getTabData(tab).length === 0 : Object.keys(getTabData(tab)).length === 0)" class="tab-empty">
                  <el-empty :description="`暂无${tab.label}数据`" :image-size="120" />
                </div>
                <el-descriptions 
                  v-else 
                  :column="tab.columns || 3" 
                  border 
                  size="small"
                >
                  <el-descriptions-item
                    v-for="field in tab.fields || []"
                    :key="field.prop"
                    :label="field.label"
                  >
                    <span v-if="field.renderType === 'currency'">{{ formatAmount(getFieldValue(getTabData(tab), field.prop)) }}</span>
                    <span v-else-if="field.renderType === 'percent'">{{ getFieldValue(getTabData(tab), field.prop) ? (getFieldValue(getTabData(tab), field.prop) + '%') : '-' }}</span>
                    <span v-else>{{ getFieldValue(getTabData(tab), field.prop) ?? '-' }}</span>
                  </el-descriptions-item>
                </el-descriptions>
              </template>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-drawer>
  </div>
</template>

<script setup name="BusinessConfigurable">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading, View, Document, Money } from '@element-plus/icons-vue'
import ERPConfigParser from '@/views/erp/utils/ERPConfigParser.mjs'
import { validateAllTabs, printValidationResult } from '@/views/erp/utils/validateTabConfig.js'
import dayjs from 'dayjs'
import request from '@/utils/request'
import { toRaw } from 'vue'

import { formatCurrency, formatDate, formatDateTime, formatPercent, formatAmount } from '@/views/erp/utils'
import { isSuccessResponse, getResponseData } from '@/views/erp/utils'

const isResponseSuccess = (response) => {
  return response && (response.code === 200 || response.code === 0 || response.errorCode === 0)
}

const getResponseResult = (response, defaultValue = null) => {
  return isResponseSuccess(response) ? (response.data || defaultValue) : defaultValue
}

import multiTableQueryBuilder from '../utils/multiTableQueryBuilder'
import dictionaryManager from '@/views/erp/utils/DictionaryManager'

import {
  executeDynamicQuery,
  buildQueryConditions,
  getAvailableQueryTypes
} from '../../api/engine/query'
import {
  executeValidation,
  batchValidate,
  getAvailableValidationRules,
  validateField
} from '../../api/engine/validation'
import {
  getCurrentApprovalStep,
  executeApproval,
  checkApprovalPermission,
  getApprovalHistory,
  getWorkflowDefinition,
  transferApproval,
  withdrawApproval
} from '../../api/engine/approval'
import {
  getPushTargets,
  executePushDown,
  previewPushDown,
  batchPushDown,
  getPushMappingConfig,
  validatePushData,
  cancelPushDown,
  getPushHistory
} from '../../api/engine/push'

const getApiMethod = async (methodType) => {
  const apiConfig = currentConfig.value?.apiConfig
  
  if (!apiConfig || !apiConfig.methods) {
    return null
  }
  
  const methodConfig = apiConfig.methods[methodType]
  
  if (!methodConfig) {
    return null
  }
  
  if (typeof methodConfig === 'string') {
    return (data) => request({
      url: methodConfig,
      method: methodType === 'get' || methodType === 'entry' || methodType === 'cost' ? 'get' : 'post',
      params: methodType === 'get' || methodType === 'entry' || methodType === 'cost' ? { id: data } : undefined,
      data: methodType === 'get' || methodType === 'entry' || methodType === 'cost' ? undefined : data
    })
  }
  
  if (typeof methodConfig === 'object' && methodConfig.url) {
    return (data) => request({
      url: methodConfig.url,
      method: methodConfig.method || 'post',
      params: methodConfig.method === 'get' ? data : undefined,
      data: methodConfig.method !== 'get' ? data : undefined
    })
  }
  
  return null
}

const props = defineProps({
  moduleCode: {
    type: String,
    required: false,
    default: ''
  }
})

const route = useRoute()
const getModuleCode = () => {
  return route.query.moduleCode || props.moduleCode  
}

const BusinessTemplate = computed(() => ({
  apiConfig: currentConfig.value?.apiConfig || {},
  dictionaryConfig: currentConfig.value?.dictionaryConfig || {},
  pageConfig: currentConfig.value?.pageConfig || {}
}))

const getList = async () => {
  loading.value = true
  
  try {
    const mainQueryConfig = buildMainQueryConfig()
    const tableName = getTableNameFromConfig()
    const moduleCode = currentConfig.value?.moduleCode
    
    if (!moduleCode) {
      throw new Error('模块配置中缺少 moduleCode 字段，无法执行查询')
    }
    
    const response = await request({
      url: '/erp/engine/query/execute',
      method: 'post',
      data: {
        moduleCode: moduleCode,
        tableName: tableName,
        queryConfig: mainQueryConfig,
        pageNum: queryParams.value.pageNum,
        pageSize: queryParams.value.pageSize
      }
    })
    
    tableData.value = response.data?.rows || []
    total.value = response.data?.total || 0
    
    await loadSubTablesData()
    
  } catch (error) {
    ElMessage.error(businessConfig.value.messages?.error?.load || '查询列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * Build main table queryConfig
 */
const buildMainQueryConfig = () => {
  const conditions = []
  
  const searchFields = parsedConfig.search?.fields || []
  
  searchFields.forEach(field => {
    let value = queryParams.value[field.field]
    const operator = field.queryOperator || 'eq'
    
    if (field.component === 'daterange') {
      if (Array.isArray(dateRange.value) && dateRange.value.length === 2) {
        value = dateRange.value
      } else {
        return
      }
    }
    
    if (value === undefined || value === null || value === '') {
      return
    }
    
    if (field.component === 'daterange') {
      conditions.push({
        field: field.field,
        operator: operator,
        value: value
      })
    } else if (Array.isArray(value)) {
      conditions.push({
        field: field.field,
        operator: operator,
        value: value
      })
    } else {
      conditions.push({
        field: field.field,
        operator: operator,
        value: value
      })
    }
  })
  
  const orderBy = parsedConfig.table?.orderBy || [
    { field: 'FCreateDate', direction: 'DESC' }
  ]
  
  return {
    conditions: conditions,
    orderBy: orderBy
  }
}

/**
 * Load sub-table data
 */
const loadSubTablesData = async () => {
  try {
    const subTableConfigs = multiTableQueryBuilder.parseSubTableConfigs(currentConfig.value)
    
    if (subTableConfigs.length === 0) {
      return
    }
    
    const contextData = {
      billNo: 'PENDING'
    }
  } catch (error) {
    // Ignore errors
  }
}

/**
 * Query sub-table data by bill number
 * @param {String} billNo - Bill number
 */
const loadSubTablesByBillNo = async (billNo) => {
  try {
    const moduleCode = currentConfig.value?.moduleCode
    
    if (!moduleCode) {
      console.error('[loadSubTablesByBillNo] Module code is empty')
      throw new Error('Module code is required for query')
    }
    
    const subTableConfigs = multiTableQueryBuilder.parseSubTableConfigs(currentConfig.value)
    
    if (subTableConfigs.length === 0) {
      console.error('[loadSubTablesByBillNo] No sub-table configs found')
      return
    }
    
    console.error('[loadSubTablesByBillNo] Querying sub-tables, billNo:', billNo)
    console.error('[loadSubTablesByBillNo] Sub-table configs:', JSON.stringify(subTableConfigs, null, 2))
    console.error('[loadSubTablesByBillNo] moduleCode:', moduleCode)
    
    const results = await multiTableQueryBuilder.queryAllSubTables(
      moduleCode,
      subTableConfigs,
      { billNo }
    )
    
    console.error('[loadSubTablesByBillNo] Query completed')
    
    if (results.entry) {
      entryList.value = results.entry.data
      currentDetailRow.value.entryList = results.entry.data
      console.error('[loadSubTablesByBillNo] Entry data loaded:', results.entry.data.length, 'records')
    }
    
    if (results.cost) {
      costData.value = results.cost.data[0] || {}
      currentDetailRow.value.costData = results.cost.data[0] || {}
      console.error('[loadSubTablesByBillNo] Cost data loaded')
    }
  } catch (error) {
    console.error('[loadSubTablesByBillNo] Query failed:', error.message)
    ElMessage.error('Failed to load sub-table data: ' + error.message)
  }
}

/**
 * Get table name from config
 * @returns {string} Table name
 * @throws {Error} Throw error if table name is not specified in config
 */
const getTableNameFromConfig = () => {
  const tableName = currentConfig.value?.pageConfig?.tableName
  
  if (!tableName) {
    const moduleCode = getModuleCode()
    throw new Error(`Config error: Please specify table name in pageConfig.tableName`)
  }
  
  return tableName
}

// Config parser
let parser = null

// Current config
const currentConfig = ref(null)

// API methods from config
const apiMethods = computed(() => currentConfig.value?.apiConfig?.methods || {})

// Business config
const businessConfig = computed(() => currentConfig.value?.businessConfig || {})

// Page title
const pageTitle = computed(() => {
  const titleTemplate = parsedConfig.page?.title || '{entityName}管理'
  const entityName = businessConfig.value.entityName || '数据'
  return titleTemplate.replace(/{entityName}/g, entityName)
})

// Parsed config
const parsedConfig = reactive({
  page: {},
  search: {},
  table: {},
  form: {},
  drawer: {},
  actions: {}
})

// Status data
const loading = ref(true)
const submitLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = ref({
  pageNum: 1,
  pageSize: 10
})
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formData = ref({})
const formRef = ref(null)
const dateRange = ref([])
const queryRef = ref(null)

// Drawer status
const drawerVisible = ref(false)
const drawerTitle = ref('订单详情')
const drawerLoading = ref(false)
const currentDetailRow = ref({})
const detailActiveTab = ref('entry')

// Entry list and cost data
const entryList = ref([])
const costData = ref({})

// Form tab active state
const formActiveTab = ref('entry')

// Engine config
const engineConfig = reactive({
  query: null,
  validation: null,
  approval: null,
  push: null
})

// Approval status
const approvalStep = ref(null)
const approvalHistory = ref([])
const workflowDefinition = ref(null)

// Push status
const pushTargets = ref([])
const pushDialogVisible = ref(false)
const pushTargetModule = ref('')

// Country search
const nationSearchLoading = ref(false)
const nationOptions = ref([])

// Dictionary load status
const dictLoaded = ref(false)

// Left toolbar actions
const leftToolbarActions = computed(() => {
  return parsedConfig.actions?.toolbar?.filter(a => a.position === 'left') || []
})

// Visible columns
const visibleColumns = computed(() => {
  return parsedConfig.table?.columns?.filter(col => col.visible !== false) || []
})

// Form validation rules
const formRules = computed(() => {
  const rules = {}
  parsedConfig.form?.sections?.forEach(section => {
    section.fields.forEach(field => {
      if (field.rules && field.rules.length > 0) {
        rules[field.field] = field.rules
      }
    })
  })
  return rules
})

// Initialize config (force load from database)
const initConfig = async () => {
  try {
    const moduleCode = getModuleCode()
    
    await loadDatabaseConfig(moduleCode)
    
    parsedConfig.page = parser.parsePageConfig()
    parsedConfig.search = parser.parseSearchForm()
    parsedConfig.table = parser.parseTableColumns()
    parsedConfig.form = parser.parseFormConfig()
    parsedConfig.drawer = parser.parseDrawerConfig()
    parsedConfig.actions = parser.parseActions()
    
    markRequiredDictionaries()
  } catch (error) {
    ElMessage.error(`Failed to load config: ${error.message}`)
    throw error
  }
}

const markRequiredDictionaries = () => {
  const requiredDicts = new Set()
  
  parsedConfig.form?.sections?.forEach(section => {
    section.fields.forEach(field => {
      if (field.required && field.dictionary) {
        field.dictRequired = true
        requiredDicts.add(field.dictionary)
      }
    })
  })
  
  parsedConfig.search?.fields?.forEach(field => {
    if (field.required && field.dictionary) {
      field.dictRequired = true
      requiredDicts.add(field.dictionary)
    }
  })
  
  window._erpRequiredDicts = requiredDicts
}

const loadDatabaseConfig = async (moduleCode) => {
  try {
    const configContent = await ERPConfigParser.loadFromDatabase(moduleCode)
    
    if (!configContent) {
      throw new Error(`Module [${moduleCode}] config not found`)
    }
    
    currentConfig.value = configContent
    
    parser = new ERPConfigParser(configContent)
  } catch (error) {
    throw new Error(`Failed to load config: ${error.message}`)
  }
}

const getDictOptions = (dictName, staticOptions = null, required = false) => {
  if (dictName === 'nation') {
    return nationOptions.value
  }
  
  if (staticOptions && Array.isArray(staticOptions)) {
  }
  
  const dataFromManager = dictionaryManager.getDictOptions(dictName)
  
  if (!dataFromManager || dataFromManager.length === 0) {
    if (required) {
    }
    return []
  }
  
  if (dictName === 'salespersons') {
    return dataFromManager.map(option => {
      const nickName = option.label || ''
      const departmentName = option.departmentName || ''
      
      const label = departmentName ? `${nickName}(${departmentName})` : nickName
      
      return {
        ...option,
        label: label
      }
    })
  }
  
  return dataFromManager
}

const getButtonDisabled = (disabledKey) => {
  if (!disabledKey) return false
  if (disabledKey === 'single') return single.value
  if (disabledKey === 'multiple') return multiple.value
  return false
}

const getTagConfig = (value, dictName) => {
  const dict = getDictOptions(dictName)
  
  const option = dict.find(item => {
    return String(item.value) === String(value)
  })
  
  const validTypes = ['success', 'info', 'warning', 'danger', '']
  let tagType = option?.type || 'info'
  
  if (!validTypes.includes(tagType)) {
    tagType = 'info'
  }
  
  return {
    label: option?.label || value,
    type: tagType
  }
}

const getDictLabel = (value, dictName) => {
  if (!dictName || !value && value !== 0) return value || '-'
  const dict = getDictOptions(dictName)
  const option = dict.find(item => String(item.value) === String(value))
  return option ? option.label : value
}

let queryTimer = null
const handleQuery = () => {
  if (loading.value) {
    ElMessage.warning('数据正在处理，请勿重复提交')
    return
  }
  
  if (queryTimer) {
    clearTimeout(queryTimer)
  }
  
  if (dateRange.value && dateRange.value.length === 2) {
    queryParams.value.beginDate = dateRange.value[0]
    queryParams.value.endDate = dateRange.value[1]
  } else {
    queryParams.value.beginDate = undefined
    queryParams.value.endDate = undefined
  }
  queryParams.value.pageNum = 1
  
  queryTimer = setTimeout(() => {
    getList()
  }, 300)
}

const initDateRange = () => {
  console.log('🔍 [initDateRange] ========== 开始初始化日期范围 ==========')
  
  const searchFields = parsedConfig.search?.fields || []
  console.log('📋 [initDateRange] searchFields:', searchFields)
  
  // ✅ 方案 1：处理 daterange 类型的单个字段（如 FDate）
  const dateRangeField = searchFields.find(f => f.component === 'daterange' && f.defaultValue)
  console.log('🔍 [initDateRange] dateRangeField found:', dateRangeField)
  
  if (dateRangeField) {
    console.log('✅ [initDateRange] 找到 daterange 字段:', dateRangeField.field)
    console.log('📦 [initDateRange] defaultValue:', dateRangeField.defaultValue)
    console.log('📦 [initDateRange] defaultValue isArray:', Array.isArray(dateRangeField.defaultValue))
    console.log('📦 [initDateRange] defaultValue length:', dateRangeField.defaultValue?.length)
  }
  
  if (dateRangeField && Array.isArray(dateRangeField.defaultValue) && dateRangeField.defaultValue.length === 2) {
    console.log('⏰ [initDateRange] 解析开始日期:', dateRangeField.defaultValue[0])
    const startDate = parseDynamicDate(dateRangeField.defaultValue[0])
    console.log('⏰ [initDateRange] 开始日期结果:', startDate)
    
    console.log('⏰ [initDateRange] 解析结束日期:', dateRangeField.defaultValue[1])
    const endDate = parseDynamicDate(dateRangeField.defaultValue[1])
    console.log('⏰ [initDateRange] 结束日期结果:', endDate)
    
    if (startDate && endDate) {
      console.log('✅ [initDateRange] 设置日期范围:', [startDate, endDate])
      dateRange.value = [startDate, endDate]
      queryParams.value.beginDate = startDate
      queryParams.value.endDate = endDate
      console.log('✅ [initDateRange] ========== 日期初始化完成 ==========')
      return
    } else {
      console.warn('❌ [initDateRange] 日期解析失败，startDate 或 endDate 为 null')
    }
  } else {
    console.log('⚠️ [initDateRange] 不满足 daterange 条件，尝试方案 2')
  }
  
  // 方案 2：处理 beginDate + endDate 两个独立字段（兼容旧逻辑）
  const beginDateField = searchFields.find(f => f.field === 'beginDate')
  const endDateField = searchFields.find(f => f.field === 'endDate')
  
  let beginDateValue = null
  let endDateValue = null
  
  // Handle begin date
  if (beginDateField && beginDateField.defaultValue) {
    beginDateValue = parseDynamicDate(beginDateField.defaultValue)
  }
  
  // Handle end date
  if (endDateField && endDateField.defaultValue) {
    endDateValue = parseDynamicDate(endDateField.defaultValue)
  }
  
  // Use configured default values if available, otherwise use "current month 1st to today"
  if (beginDateValue && endDateValue) {
    console.log('✅ [方案 2] 使用配置的 beginDate + endDate:', [beginDateValue, endDateValue])
    dateRange.value = [beginDateValue, endDateValue]
  } else {
    console.warn('⚠️ [方案 2] beginDate 或 endDate 为空，执行 fallback 逻辑')
    console.warn('📋 [fallback] beginDateValue:', beginDateValue)
    console.warn('📋 [fallback] endDateValue:', endDateValue)
    // Fallback: 1st of current month to today
    const now = new Date()
    const firstDayOfMonth = new Date(now.getFullYear(), now.getMonth(), 1)
    const fallbackStart = dayjs(firstDayOfMonth).format('YYYY-MM-DD')
    const fallbackEnd = dayjs(now).format('YYYY-MM-DD')
    console.warn(`❌ [fallback] 使用默认值：[${fallbackStart}, ${fallbackEnd}]`)
    dateRange.value = [
      fallbackStart,
      fallbackEnd
    ]
  }
  
  console.log('📋 [initDateRange] 最终 dateRange.value:', dateRange.value)
  console.log('📋 [initDateRange] 最终 queryParams.beginDate:', queryParams.value.beginDate)
  console.log('📋 [initDateRange] 最终 queryParams.endDate:', queryParams.value.endDate)
  console.log('✅ [initDateRange] ========== 日期初始化完成 ==========')
}

/**
 * Parse dynamic date value
 * @param {string} value - Date value, can be: fixed date "2010-01-01", dynamic value "today", "yesterday", "monthStart", etc.
 * @returns {string|null} - Formatted date string YYYY-MM-DD
 */
const parseDynamicDate = (value) => {
  console.log(`🕒 [parseDynamicDate] 输入值：${value}`)
  
  if (!value) {
    console.log('❌ [parseDynamicDate] 值为空，返回 null')
    return null
  }
  
  const today = new Date()
  
  // Dynamic value: today
  if (value === 'today') {
    const result = dayjs(today).format('YYYY-MM-DD')
    console.log(`✅ [parseDynamicDate] 'today' -> ${result}`)
    return result
  }
  
  // Dynamic value: yesterday
  if (value === 'yesterday') {
    const yesterday = new Date(today)
    yesterday.setDate(yesterday.getDate() - 1)
    const result = dayjs(yesterday).format('YYYY-MM-DD')
    console.log(`✅ [parseDynamicDate] 'yesterday' -> ${result}`)
    return result
  }
  
  // Dynamic value: monthStart (1st of current month)
  if (value === 'monthStart') {
    const monthStart = new Date(today.getFullYear(), today.getMonth(), 1)
    const result = dayjs(monthStart).format('YYYY-MM-DD')
    console.log(`✅ [parseDynamicDate] 'monthStart' -> ${result}`)
    return result
  }
  
  // Dynamic value: yearStart (Jan 1st of current year)
  if (value === 'yearStart') {
    const yearStart = new Date(today.getFullYear(), 0, 1)
    const result = dayjs(yearStart).format('YYYY-MM-DD')
    console.log(`✅ [parseDynamicDate] 'yearStart' -> ${result}`)
    return result
  }
  
  // Fixed date: try to parse as YYYY-MM-DD format
  const dateRegex = /^\d{4}-\d{2}-\d{2}$/
  if (dateRegex.test(value)) {
    console.log(`✅ [parseDynamicDate] 固定日期格式 '${value}' -> ${value}`)
    return value
  }
  
  // Other cases, try to parse directly (may be a dayjs-parseable format)
  const parsed = dayjs(value)
  if (parsed.isValid()) {
    const result = parsed.format('YYYY-MM-DD')
    console.log(`✅ [parseDynamicDate] 解析成功 '${value}' -> ${result}`)
    return result
  }
  
  console.warn(`❌ [parseDynamicDate] 无法解析的值：${value}`)
  return null
}

// Reset query (with duplicate submission control)
const resetQuery = () => {
  // Return if loading
  if (loading.value) {
    ElMessage.warning('Data is being processed, please do not submit repeatedly')
    return
  }
  
  queryRef.value?.resetFields()
  initDateRange()
  handleQuery()
}

// Handle page size change (with duplicate submission control)
const handlePageSizeChange = (newSize) => {
  // Return if loading
  if (loading.value) return
  
  queryParams.value.pageSize = newSize
  getList()
}

const handlePageChange = (newPage) => {
  // Return if loading
  if (loading.value) return
  
  queryParams.value.pageNum = newPage
  getList()
}

// Handle selection change
const handleSelectionChange = (selection) => {
  ids.value = selection.map(item => item.id)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

// Handle row click
const handleRowClick = (row) => { 
}
 
const handleViewDetail = async (row) => {
  drawerVisible.value = true
   
  const billNoField = parsedConfig.page?.billNoField || 'FBillNo'
   
  const titleTemplate = businessConfig.value.drawerTitle || '{entityName}详情 - {billNo}'
  const entityName = businessConfig.value.entityName || '订单'
   
  drawerTitle.value = titleTemplate
    .replace(/{entityName}/g, entityName)
    .replace(/{billNo}/g, row[billNoField] || row.FBillNo || '')
  
  drawerLoading.value = true
  currentDetailRow.value = { ...row }
  
  console.error('[成本暂估页签 - handleViewDetail] 开始加载详情页，billNo:', row[billNoField] || row.FBillNo)
  
  try { 
    const billNoValue = row[billNoField] || row.FBillNo
    
    console.error('[成本暂估页签 - handleViewDetail] billNoValue:', billNoValue)
    
    if (billNoValue) { 
      console.error('[成本暂估页签 - handleViewDetail] 调用 loadSubTablesByBillNo')
      await loadSubTablesByBillNo(billNoValue)
    } else {
      console.error('[成本暂估页签 - handleViewDetail] ⚠️ 警告：billNoValue 为空')
    }
     
    const hasEntryData = entryList.value && entryList.value.length > 0
    const hasCostData = costData.value && Object.keys(costData.value).length > 0
    console.error('[成本暂估页签 - handleViewDetail] 数据检查 - hasEntryData:', hasEntryData, 'hasCostData:', hasCostData)
    detailActiveTab.value = hasEntryData ? 'entry' : (hasCostData ? 'cost' : 'entry')
    
  } catch (error) {
    ElMessage.error('加载详情数据失败')
  } finally {
    drawerLoading.value = false
  }
}
 
const handleDrawerClose = (done) => {
  currentDetailRow.value = {}
  done()
}

/**
 * Get field value (supports case-insensitive field name matching)
 * @param {Object} row - Data row object
 * @param {String} fieldName - Field name (may be uppercase)
 * @returns {Any} Field value
 */
const getFieldValue = (row, fieldName) => {
  if (!row || !fieldName) return undefined
  
  // Try exact match first
  if (row.hasOwnProperty(fieldName)) {
    return row[fieldName]
  }
  
  // Try lowercase match (database field names are usually lowercase)
  const lowerFieldName = fieldName.toLowerCase()
  if (row.hasOwnProperty(lowerFieldName)) {
    return row[lowerFieldName]
  }
  
  // Try case-insensitive search in all row keys
  const rowKeys = Object.keys(row)
  const matchedKey = rowKeys.find(key => key.toLowerCase() === fieldName.toLowerCase())
  if (matchedKey) {
    return row[matchedKey]
  }
  
  return undefined
}

/**
 * Get tab data (fix Proxy access issue)
 * @param {Object} tab - Tab config object
 * @returns {Array|Object} Data for the tab
 */
const getTabData = (tab) => {
  if (!tab || !tab.dataField) {
    return null
  }
  
  const data = currentDetailRow.value[tab.dataField]
  
  // Special handling for cost tab with debug logs
  if (tab.name === 'cost') {
    console.error('[getTabData] tab:', tab.name, 'dataField:', tab.dataField, 'data:', JSON.stringify(data, null, 2))
  }
  
  // Return directly if array
  if (Array.isArray(data)) {
    return data
  }
  
  // Return if object (cost tab)
  if (data && typeof data === 'object') {
    return data
  }
  
  // Return empty array for other cases
  return []
}

/**
 * Validate drawer tabs (enabled in development environment)
 */
const validateDrawerTabs = () => {
  if (process.env.NODE_ENV !== 'development') {
    return
  }
  
  const tabs = parsedConfig.drawer?.tabs || []
  if (tabs.length === 0) {
    return
  }
  
  const validationResult = validateAllTabs(tabs)
  printValidationResult(validationResult)
  
  if (!validationResult.valid) {
    console.warn('[Config Suggestion] Please check detail_config JSON config, ensure field paths are correct. Refer to: DOC/公共模块文档/落地 sql/销售订单初始化配置.sql')
  }
}

/**
 * Debug tab config (called in template)
 */
const debugTabConfig = (tab) => {
  console.error('[Drawer - Tab Config]', {
    name: tab.name,
    label: tab.label,
    type: tab.type,
    dataField: tab.dataField,
    hasFields: !!tab.fields,
    fieldsCount: tab.fields?.length || 0,
    fullConfig: tab
  })
  return ''
}

/**
 * Debug form tab config (called in template)
 */
const debugFormTabConfig = (tab) => {
  const formFields = getFormFields(tab)
  console.error('字段检查', {
    tabName: tab.name,
    hasFieldsProp: !!tab.fields,
    hasForm: !!tab.form,
    hasFormFields: !!tab.form?.fields,
    fieldsFromMethod: formFields?.length || 0,
    fields: formFields
  })
  return ''  
}

/**
 * Get form field config (compatible with two formats)
 * @param {Object} tab - Tab config object
 * @returns {Array} Field config array
 */
const getFormFields = (tab) => {
  if (tab.form && Array.isArray(tab.form.fields)) {
    return tab.form.fields
  }
  
  if (Array.isArray(tab.fields)) {
    return tab.fields
  }
  
  return []
}

// Handle action
const handleAction = (handlerName) => {
  const handlerMap = {
    handleAdd: () => openDialog('add'),
    handleUpdate: () => openDialog('edit'),
    handleDelete: confirmDelete,
    handleAudit: batchAudit,
    handleUnAudit: batchUnAudit,
    openColumnSetting: () => ElMessage.info('列设置功能待实现')
  }
  
  if (handlerMap[handlerName]) {
    handlerMap[handlerName]()
  }
}

// Open dialog - use title from config
const openDialog = (type) => {
  formData.value = {}
  
  if (type === 'add' && parsedConfig.form?.sections) {
    parsedConfig.form.sections.forEach(section => {
      section.fields.forEach(field => {
        if (field.defaultValue !== undefined) {
          formData.value[field.field] = field.defaultValue
        }
      })
    })
  }
  
  entryList.value = []
  costData.value = {}
  
  if (type === 'edit') {
    if (ids.value.length !== 1) {
      ElMessage.warning(businessConfig.value.messages?.selectOne || '请选择一条数据')
      return
    }
    loadFormData(ids.value[0])
    const titleConfig = businessConfig.value.dialogTitle || { add: '新增数据', edit: '修改数据' }
    const entityName = businessConfig.value.entityName || '数据'
    dialogTitle.value = type === 'edit' 
      ? titleConfig.edit.replace(/{entityName}/g, entityName)
      : titleConfig.add.replace(/{entityName}/g, entityName)
  } else {
    const titleConfig = businessConfig.value.dialogTitle || { add: '新增数据', edit: '修改数据' }
    const entityName = businessConfig.value.entityName || '数据'
    dialogTitle.value = titleConfig.add.replace(/{entityName}/g, entityName)
  }
  
  dialogVisible.value = true
}

// 加载表单数据 - 使用配置中的 API
const loadFormData = async (id) => {
  try {
    const apiMethod = await getApiMethod('get')
    if (!apiMethod) {
      ElMessage.error('API 未配置')
      return
    }
    const response = await apiMethod(id)
    formData.value = response.data || response
    
    // 从配置中获取单据编号字段名（完全配置化）
    const billNoField = parsedConfig.page?.billNoField || 'FBillNo'
    const billNoValue = formData.value[billNoField] || formData.value.FBillNo
    
    // 加载明细数据
    if (billNoValue) {
      const entryMethod = await getApiMethod('entry')
      if (entryMethod) {
        const entryResponse = await entryMethod(billNoValue)
        const entryData = getResponseResult(entryResponse, [])
        entryList.value = Array.isArray(entryData) ? entryData : []
      }
      
      // 加载成本数据
      const costMethod = await getApiMethod('cost')
      if (costMethod) {
        const costResponse = await costMethod(billNoValue)
        const costResult = getResponseResult(costResponse, {})
        costData.value = typeof costResult === 'object' && Object.keys(costResult).length > 0 ? costResult : {}
      }
    }
  } catch (error) {
    ElMessage.error('加载数据失败')
  }
}

// 提交表单 - 使用配置中的 API 和消息
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitLoading.value = true
    
    // 合并数据
    const submitData = { ...formData.value }
    if (entryList.value && entryList.value.length > 0) {
      submitData.entryList = entryList.value
    }
    if (costData.value && Object.keys(costData.value).length > 0) {
      submitData.costData = costData.value
    }
    
    const isNew = !submitData.id
    const apiMethod = await getApiMethod(isNew ? 'add' : 'update')
    
    if (!apiMethod) {
      ElMessage.error('API 未配置')
      return
    }
    
    await apiMethod(submitData)
    ElMessage.success(businessConfig.value.messages?.success?.[isNew ? 'add' : 'edit'] || (isNew ? '新增成功' : '修改成功'))
    
    dialogVisible.value = false
    getList()
  } catch (error) {
    if (error !== 'validate') {
      ElMessage.error('保存失败：' + (error.message || '请检查表单填写是否正确'))
    }
  } finally {
    submitLoading.value = false
  }
}

// 确认删除 - 使用配置中的 API 和消息
const confirmDelete = async () => {
  if (!ids.value || ids.value.length === 0) {
    ElMessage.warning('请选择要删除的数据')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      businessConfig.value.messages?.confirmDelete?.replace(/{count}/g, ids.value.length) || `是否确认删除选中的 ${ids.value.length} 条数据？`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const apiMethod = await getApiMethod('delete')
    if (!apiMethod) {
      ElMessage.error('API 未配置')
      return
    }
    
    await apiMethod(ids.value)
    ElMessage.success(businessConfig.value.messages?.success?.delete || '删除成功')
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败：' + (error.message || '未知错误'))
    }
  }
}

// 批量审核 - 使用配置中的 API 和消息
const batchAudit = async () => {
  if (!ids.value || ids.value.length === 0) {
    ElMessage.warning('请选择要审核的数据')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      businessConfig.value.messages?.confirmAudit?.replace(/{count}/g, ids.value.length) || `是否确认审核选中的 ${ids.value.length} 条数据？`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const apiMethod = await getApiMethod('audit')
    if (!apiMethod) {
      ElMessage.error('API 未配置')
      return
    }
    
    await apiMethod(ids.value)
    ElMessage.success(businessConfig.value.messages?.success?.audit || '审核成功')
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('审核失败：' + (error.message || '未知错误'))
    }
  }
}

// 批量反审核 - 使用配置中的 API 和消息
const batchUnAudit = async () => {
  if (!ids.value || ids.value.length === 0) {
    ElMessage.warning('请选择要反审核的数据')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      businessConfig.value.messages?.confirmUnAudit?.replace(/{count}/g, ids.value.length) || `是否确认反审核选中的 ${ids.value.length} 条数据？`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const apiMethod = await getApiMethod('unAudit')
    if (!apiMethod) {
      ElMessage.error('API 未配置')
      return
    }
    
    await apiMethod(ids.value)
    ElMessage.success(businessConfig.value.messages?.success?.unAudit || '反审核成功')
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('反审核失败：' + (error.message || '未知错误'))
    }
  }
}

// 关闭对话框
const handleDialogClose = () => {
  formRef.value?.resetFields()
  formData.value = {}
  entryList.value = []
  costData.value = {}
}

// 添加明细行 - 基于配置生成默认值
const handleAddEntryRow = () => {
  // 从配置中获取明细表格的列定义
  const entryTab = parsedConfig.form?.formTabs?.tabs?.find(tab => tab.name === 'entry')
  const columns = entryTab?.table?.columns || []
  
  // 根据列类型生成默认值
  const newRow = {}
  columns.forEach(col => {
    if (col.type === 'number') {
      newRow[col.prop] = 0
    } else if (col.type === 'boolean') {
      newRow[col.prop] = false
    } else {
      newRow[col.prop] = ''
    }
  })
  
  entryList.value.push(newRow)
}

// 删除明细行
const handleDeleteEntryRow = (index) => {
  entryList.value.splice(index, 1)
}

// 搜索国家（模糊搜索）- 基于配置
const searchNations = async (keyword) => {
  if (!keyword || keyword.trim() === '') {
    nationOptions.value = []
    return
  }
  
  nationSearchLoading.value = true
  try {
    // ✅ 从配置中获取国家字典的配置
    const dictConfig = BusinessTemplate.value.dictionaryConfig?.dictionaries?.nation
    
    if (dictConfig && dictConfig.type === 'remote') {
      // 使用配置中的搜索 API
      const searchApi = dictConfig.config?.searchApi || '/erp/engine/country/search?keyword={keyword}&limit=20'
      const searchUrl = searchApi.replace('{keyword}', encodeURIComponent(keyword))
      
      const response = await request(searchUrl)
      
      let data = []
      if (response.code === 200 || response.errorCode === 0) {
        data = response.data || response.rows || []
      } else if (Array.isArray(response)) {
        data = response
      }
      
      // 映射为标准格式 { label, value }
      nationOptions.value = data.map(item => ({
        label: item.labelZh || item.name || item.label,
        value: item.id || item.kingdee || item.value,
        labelEn: item.labelEn || item.name_en // 保留英文用于扩展
      }))
    } else {
      nationOptions.value = []
    }
  } catch (error) {
    nationOptions.value = []
  } finally {
    nationSearchLoading.value = false
  }
}

/**
 * 预加载字典数据（最终优化版 - 统一使用 DictionaryManager）
 * 
 * 加载策略:
 * 1. 使用 DictionaryManager.loadAll() 一次性加载所有字典
 * 2. 不再依赖 JSON 配置中的 builder 设置
 * 3. 国家字典等特殊字典通过远程搜索接口单独处理
 * 
 * @returns {Promise<void>}
 */
const preloadDictionaries = async () => {
  try {
    // 步骤 1: 使用 DictionaryManager 一次性加载所有字典
    const allDicts = await dictionaryManager.loadAll()
    
    // 步骤 2: 检查加载结果
    const dictStatus = dictionaryManager.getStatus()
    
    // 步骤 3: 验证必填字典（可选）- 仅记录日志，不显示警告
    if (window._erpRequiredDicts && window._erpRequiredDicts.size > 0) {
      const missingDicts = []
      for (const dictName of window._erpRequiredDicts) {
        if (!allDicts[dictName] || allDicts[dictName].length === 0) {
          missingDicts.push(dictName)
        }
      }
      
      if (missingDicts.length > 0) {
        // 使用 console.warn 记录警告到浏览器控制台，不显示 UI 提示
        console.warn('[字典预加载] 部分字典数据缺失:', missingDicts.join(', '))
      }
    }
    
    // 步骤 4: 标记字典加载完成
    dictLoaded.value = true
    
  } catch (error) {
    ElMessage.error(`预加载字典失败：${error.message}`)
    dictLoaded.value = true // 即使失败也允许页面渲染（降级处理）
  }
}


// 初始化 - 简化加载顺序：配置 → 字典 → 渲染
onMounted(async () => {
  try {
    // 步骤 1: 加载页面配置
    await initConfig()

    // 步骤2: 加载字典数据（必须完成后才允许渲染）
    await preloadDictionaries()

    // 步骤3: 初始化引擎配置
    await initEngineConfig()

    // 步骤4: 设置默认日期区间
    initDateRange()

    // 步骤 5: 加载列表数据
    getList()
  } catch (error) {
    ElMessage.error(`页面初始化失败：${error.message}`)
  }
})

// ==================== 引擎相关方法 ====================

/**
 * 初始化引擎配置
 */
const initEngineConfig = async () => {
  const moduleCode = BusinessTemplate.value.pageConfig?.moduleCode
  if (!moduleCode) return
  
  try {
    // 🔍 新增：校验详情页签配置（开发环境）
    validateDrawerTabs()
    
    // 1. 初始化动态查询引擎
    if (parsedConfig.search) {
      engineConfig.query = {
        moduleCode,
        searchConfig: parsedConfig.search
      }
    }
    
    // 2. 初始化表单验证引擎
    if (parsedConfig.form?.validationConfig) {
      engineConfig.validation = {
        moduleCode,
        validationConfig: parsedConfig.form.validationConfig
      }
    }
    
    // 3. 初始化审批流程引擎
    if (parsedConfig.actions?.approvalConfig?.enabled) {
      engineConfig.approval = {
        moduleCode,
        workflowConfig: parsedConfig.actions.approvalConfig
      }
      // 加载审批流程定义
      await loadWorkflowDefinition(moduleCode)
    }
    
    // 4. 初始化下推引擎
    if (parsedConfig.actions?.pushConfig?.enabled) {
      engineConfig.push = {
        moduleCode,
        pushConfig: parsedConfig.actions.pushConfig
      }
      // 加载可下推目标
      await loadPushTargets(moduleCode)
    }
  } catch (error) {
    // 忽略错误
  }
}

/**
 * 加载审批流程定义
 */
const loadWorkflowDefinition = async (moduleCode) => {
  try {
    const response = await getWorkflowDefinition(moduleCode)
    if (response.code === 200 || response.code === 0) {
      workflowDefinition.value = response.data
    }
  } catch (error) {
    // 忽略错误
  }
}

/**
 * 加载可下推目标
 */
const loadPushTargets = async (moduleCode) => {
  try {
    const response = await getPushTargets(moduleCode)
    if (response.code === 200 || response.code === 0) {
      pushTargets.value = response.data || []
    }
  } catch (error) {
    // 忽略错误
  }
}

/**
 * 执行动态查询
 */
const executeEngineQuery = async (queryParams) => {
  if (!engineConfig.query) {
    return null
  }
  
  try {
    const response = await executeDynamicQuery({
      moduleCode: engineConfig.query.moduleCode,
      queryParams,
      searchConfig: engineConfig.query.searchConfig
    })
    
    if (response.code === 200 || response.code === 0) {
      return response.data
    }
    return null
  } catch (error) {
    return null
  }
}

/**
 * 执行表单验证
 */
const executeEngineValidation = async (formData) => {
  if (!engineConfig.validation) {
    return { valid: true, message: '未启用验证' }
  }
  
  try {
    const response = await executeValidation({
      moduleCode: engineConfig.validation.moduleCode,
      formData,
      validationConfig: engineConfig.validation.validationConfig
    })
    
    if (response.code === 200 || response.code === 0) {
      return response.data
    }
    return { valid: false, message: '验证失败' }
  } catch (error) {
    return { valid: false, message: error.message }
  }
}

/**
 * 获取当前审批步骤
 */
const getCurrentStep = async (billData) => {
  if (!engineConfig.approval) {
    return null
  }
  
  try {
    const response = await getCurrentApprovalStep({
      moduleCode: engineConfig.approval.moduleCode,
      billId: billData.id || billData.fbillNo,
      billData
    })
    
    if (response.code === 200 || response.code === 0) {
      approvalStep.value = response.data
      return response.data
    }
    return null
  } catch (error) {
    return null
  }
}

/**
 * 执行审批操作
 */
const executeEngineApproval = async (billId, action, opinion = '') => {
  if (!engineConfig.approval) {
    throw new Error('未启用审批流程')
  }
  
  try {
    const response = await executeApproval({
      moduleCode: engineConfig.approval.moduleCode,
      billId,
      action,
      opinion,
      step: approvalStep.value?.step
    })
    
    if (response.code === 200 || response.code === 0) {
      ElMessage.success('审批成功')
      // 刷新审批历史和步骤
      await loadApprovalHistory(billId)
      return response.data
    }
    throw new Error(response.msg || '审批失败')
  } catch (error) {
    throw error
  }
}

/**
 * 加载审批历史
 */
const loadApprovalHistory = async (billId) => {
  if (!engineConfig.approval) return
  
  try {
    const response = await getApprovalHistory({
      moduleCode: engineConfig.approval.moduleCode,
      billId
    })
    
    if (response.code === 200 || response.code === 0) {
      approvalHistory.value = response.data || []
    }
  } catch (error) {
    // 忽略错误
  }
}

/**
 * 执行下推操作
 */
const executeEnginePushDown = async (sourceId, targetModule, confirmData = {}) => {
  if (!engineConfig.push) {
    throw new Error('未启用下推功能')
  }
  
  try {
    const response = await executePushDown({
      sourceId,
      sourceModule: engineConfig.push.moduleCode,
      targetModule,
      confirmData
    })
    
    if (response.code === 200 || response.code === 0) {
      ElMessage.success('下推成功')
      // 加载下推历史
      await loadPushHistory(sourceId)
      return response.data
    }
    throw new Error(response.msg || '下推失败')
  } catch (error) {
    throw error
  }
}

/**
 * 预览下推数据
 */
const previewEnginePushDown = async (sourceId, targetModule) => {
  if (!engineConfig.push) {
    return null
  }
  
  try {
    const response = await previewPushDown({
      sourceId,
      sourceModule: engineConfig.push.moduleCode,
      targetModule
    })
    
    if (response.code === 200 || response.code === 0) {
      return response.data
    }
    return null
  } catch (error) {
    return null
  }
}

/**
 * 加载下推历史
 */
const loadPushHistory = async (billId) => {
  if (!engineConfig.push) return
  
  try {
    const response = await getPushHistory({
      moduleCode: engineConfig.push.moduleCode,
      billId
    })
    
    if (response.code === 200 || response.code === 0) {
      // 可以在这里更新 UI
    }
  } catch (error) {
    // 忽略错误
  }
}

/**
 * 打开下推对话框
 */
const handleOpenPushDialog = async (row) => {
  if (!engineConfig.push || pushTargets.value.length === 0) {
    ElMessage.warning('没有可用的下推目标')
    return
  }
  
  // TODO: 实现下推对话框 UI
  pushTargetModule.value = pushTargets.value[0]?.targetModule
  pushDialogVisible.value = true
}

/**
 * 执行下推
 */
const handlePushDown = async () => {
  if (!pushTargetModule.value) {
    ElMessage.warning('请选择下推目标')
    return
  }
  
  try {
    await executeEnginePushDown(ids.value[0], pushTargetModule.value)
    pushDialogVisible.value = false
  } catch (error) {
    ElMessage.error(error.message)
  }
}
</script>

<style scoped>
@import './BusinessConfigurable.styles.css';

/* 字典加载容器样式 */
.dict-loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  color: #409EFF;
}

.dict-loading-container p {
  margin-top: 16px;
  font-size: 14px;
}

/* 抽屉样式 */
.drawer-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  color: #409EFF;
}

.drawer-loading p {
  margin-top: 16px;
  font-size: 14px;
}

.drawer-content {
  padding: 0;
}

.drawer-content :deep(.el-tabs__header) {
  margin: 0 0 20px 0;
}

.drawer-content :deep(.el-table) {
  margin-top: 0;
}

.drawer-content :deep(.el-descriptions) {
  margin-top: 0;
}

/* 页签空状态样式 */
.tab-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  padding: 40px 20px;
}

/* 表单页签样式 */
.form-tabs-card {
  margin-top: 16px;
}

.form-tabs-card :deep(.el-card__body) {
  padding: 16px;
}

.form-tabs-card :deep(.el-tabs__header) {
  margin: 0 0 16px 0;
}

.tab-pane-content {
  padding: 8px 0;
}

.tab-pane-toolbar {
  margin-bottom: 12px;
  display: flex;
  justify-content: flex-end;
}

.entry-table {
  width: 100%;
}

.cost-form-card :deep(.el-form-item) {
  margin-bottom: 16px;
}
</style>