<template>
  <div class="dict-loading-container" v-if="!dictLoaded">
    <el-icon class="is-loading" :size="40"><Loading /></el-icon>
    <p>字典数据加载中...</p>
  </div>
  
  <div v-else> 
    <BusinessSearch
      v-if="parsedConfig.search?.showSearch"
      :search-config="parsedConfig.search"
      :page-config="parsedConfig.page"
      :business-config="businessConfig"
      :actions-config="parsedConfig.actions"
      :query-params="queryParams"
      :date-range="dateRange"
      :selection-info="{ single: single, multiple: multiple }"
      @query="handleQuery"
      @reset="resetQuery"
      @action="handleToolbarAction"
    />
    
    <!-- Main table area -->
    <BusinessTable
      :table-config="parsedConfig.table"
      :table-data="tableData"
      :loading="loading"
      :total="total"
      :query-params="queryParams"
      :dictionary-config="currentConfig?.dictionaryConfig || {}"
      @selection-change="handleSelectionChange"
      @row-click="handleRowClick"
      @view-detail="handleViewDetail"
      @page-size-change="handlePageSizeChange"
      @page-change="handlePageChange"
    />
    
    <!-- Form dialog -->
    <BusinessForm
      v-model:visible="dialogVisible"
      :form-config="parsedConfig.form"
      :business-config="businessConfig"
      :dialog-title="dialogTitle"
      :form-data="formData"
      :entry-list="entryList"
      :cost-data="costData"
      :submit-loading="submitLoading"
      @submit="handleSubmit"
      @add-entry="handleAddEntryRow"
      @delete-entry="handleDeleteEntryRow"
    />
    
   
    <BusinessDetail
      v-model:visible="drawerVisible"
      :drawer-config="parsedConfig.drawer"
      :drawer-title="drawerTitle"
      :detail-row="currentDetailRow"
      :loading="drawerLoading"
      @close="handleDrawerClose"
    />
  </div>
</template>

<script setup name="BusinessConfigurable">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import ERPConfigParser from '@/views/erp/utils/ERPConfigParser.mjs'
import dayjs from 'dayjs'
import request from '@/utils/request'
import dictionaryManager from '@/views/erp/utils/DictionaryManager'
import multiTableQueryBuilder from '../../utils/multiTableQueryBuilder'
import { formatCurrency, formatDate, formatDateTime, formatPercent, formatAmount } from '@/views/erp/utils/index.js'
import { isSuccessResponse, getResponseData } from '@/views/erp/utils/index.js'

// Import business components
import BusinessSearch from './components/BusinessSearch.vue'
import BusinessTable from './components/BusinessTable.vue'
import BusinessForm from './components/BusinessForm.vue'
import BusinessDetail from './components/BusinessDetail.vue'

const route = useRoute()

// Props
const props = defineProps({
  moduleCode: {
    type: String,
    required: false,
    default: ''
  }
})

// Get module code
const getModuleCode = () => {
  return route.query.moduleCode || props.moduleCode  
}

// Configuration related
const currentConfig = ref(null)
const parsedConfig = reactive({
  page: {},
  search: {},
  table: {},
  form: {},
  drawer: {},
  actions: {}
})

  // Business template (computed property)
const BusinessTemplate = computed(() => ({
  apiConfig: currentConfig.value?.apiConfig || {},
  dictionaryConfig: currentConfig.value?.dictionaryConfig || {},
  pageConfig: currentConfig.value?.pageConfig || {}
}))

// State management
const dictLoaded = ref(false)
const loading = ref(true)
const submitLoading = ref(false)
const drawerLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = ref({
  pageNum: 1,
  pageSize: 10
})
const dateRange = ref([])
const dialogVisible = ref(false)
const drawerVisible = ref(false)
const dialogTitle = ref('')
const drawerTitle = ref('订单详情')
const formData = ref({})
const entryList = ref([])
const costData = ref({})
const currentDetailRow = ref({})

 
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const formRef = ref(null)
const queryRef = ref(null)
const detailActiveTab = ref('entry')
const formActiveTab = ref('entry')

// Computed properties
const businessConfig = computed(() => currentConfig.value?.businessConfig || {})
const pageTitle = computed(() => {
  const titleTemplate = parsedConfig.page?.title || '{entityName}管理'
  const entityName = businessConfig.value.entityName || '数据'
  return titleTemplate.replace(/{entityName}/g, entityName)
})

// ==================== Config loading ====================
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

let parser = null
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

// ==================== Dictionary loading ====================
const preloadDictionaries = async () => {
  try {
    const allDicts = await dictionaryManager.loadAll()
    dictLoaded.value = true
  } catch (error) {
    console.error('预加载字典失败:', error)
    ElMessage.error(`预加载字典失败：${error.message}`)
    dictLoaded.value = true
  }
}

// ==================== Data query ====================
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
    
    conditions.push({
      field: field.field,
      operator: operator,
      value: value
    })
  })
  
  const orderBy = parsedConfig.table?.orderBy || [
    { field: 'FCreateDate', direction: 'DESC' }
  ]
  
  return {
    conditions: conditions,
    orderBy: orderBy
  }
}

const getTableNameFromConfig = () => {
  const tableName = currentConfig.value?.pageConfig?.tableName
  if (!tableName) {
    const moduleCode = getModuleCode()
    throw new Error(`Config error: Please specify table name in pageConfig.tableName`)
  }
  return tableName
}

const loadSubTablesData = async () => {
  try {
    const subTableConfigs = multiTableQueryBuilder.parseSubTableConfigs(currentConfig.value)
    if (subTableConfigs.length === 0) {
      return
    }
    const contextData = { billNo: 'PENDING' }
  } catch (error) {
    // Ignore errors
  }
}

const loadSubTablesByBillNo = async (rowData) => {
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
    
    // 直接传递整个 row 对象，让 relationConfig 可以访问所有字段
    const results = await multiTableQueryBuilder.queryAllSubTables(
      moduleCode,
      subTableConfigs,
      rowData  // 传递整个 row 对象
    )
    
    if (results.entry) {
      entryList.value = results.entry.data
      currentDetailRow.value.entryList = results.entry.data
    }
    
    if (results.cost) {
      costData.value = results.cost.data[0] || {}
      currentDetailRow.value.costData = results.cost.data[0] || {}
    }
  } catch (error) {
    console.error('[loadSubTablesByBillNo] Query failed:', error.message)
    ElMessage.error('Failed to load sub-table data: ' + error.message)
  }
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

const resetQuery = () => {
  if (loading.value) {
    ElMessage.warning('Data is being processed, please do not submit repeatedly')
    return
  }
  
  queryRef.value?.resetFields()
  initDateRange()
  handleQuery()
}

const handlePageSizeChange = (newSize) => {
  if (loading.value) return
  queryParams.value.pageSize = newSize
  getList()
}

const handlePageChange = (newPage) => {
  if (loading.value) return
  queryParams.value.pageNum = newPage
  getList()
}

const handleSelectionChange = (selection) => {
  ids.value = selection.map(item => item.id)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

 
const handleToolbarAction = (handlerName) => {
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

 
const loadFormData = async (id) => {
  try {
    loading.value = true
    const apiMethod = await getApiMethod('get')   
    if (!apiMethod) {
      ElMessage.error('API 未配置')
      return
    }
    
    const data = await apiMethod(id)
    formData.value = data.data || data
    
    
    await loadSubTablesByBillNo(formData.value.billNo)
  } catch (error) {
    ElMessage.error('加载数据失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

const handleRowClick = (row) => { 
   
}

const handleViewDetail = async (row) => {
  drawerVisible.value = true
  const billNoField = parsedConfig.page?.billNoField || 'FBillNo'
  const titleTemplate = businessConfig.value.drawerTitle || '{entityName}详情 - {billNo}'
  const entityName = businessConfig.value.entityName || '订单'
  
  // 直接使用配置的编号字段获取内容
  const fieldValue = row[billNoField] || row.FBillNo || ''
  
  // 替换模板中的 {entityName} 和第二个占位符（无论是什么格式）
  drawerTitle.value = titleTemplate
    .replace(/{entityName}/g, entityName)
    .replace(/\{[^}]+\}/g, fieldValue)
  
  drawerLoading.value = true
  currentDetailRow.value = { ...row }
  
  try {
    // 直接传递整个 row 对象，让 relationConfig 可以访问所有字段
    await loadSubTablesByBillNo(row)
    detailActiveTab.value = entryList.value?.length > 0 ? 'entry' : (Object.keys(costData.value).length > 0 ? 'cost' : 'entry')
  } catch (error) {
    ElMessage.error('加载详情数据失败')
  } finally {
    drawerLoading.value = false
  }
}

const handleDrawerClose = () => {
  currentDetailRow.value = {}
  drawerVisible.value = false
}

// ==================== Engine configuration ====================
/**
 * Initialize engine configuration
 */
const initEngineConfig = async () => {
  const moduleCode = BusinessTemplate.value.pageConfig?.moduleCode
  if (!moduleCode) return
  
  try { 
    if (parsedConfig.search) {
      engineConfig.query = {
        moduleCode,
        searchConfig: parsedConfig.search
      }
    }
     
    if (parsedConfig.form?.validationConfig) {
      engineConfig.validation = {
        moduleCode,
        validationConfig: parsedConfig.form.validationConfig
      }
    }
     
    if (parsedConfig.actions?.approvalConfig?.enabled) {
      engineConfig.approval = {
        moduleCode,
        workflowConfig: parsedConfig.actions.approvalConfig
      } 
      await loadWorkflowDefinition(moduleCode)
    }
     
    if (parsedConfig.actions?.pushConfig?.enabled) {
      engineConfig.push = {
        moduleCode,
        pushConfig: parsedConfig.actions.pushConfig
      } 
      await loadPushTargets(moduleCode)
    }
  } catch (error) { 
  }
}

 
const loadWorkflowDefinition = async (moduleCode) => {
  try {
    const response = await getWorkflowDefinition(moduleCode)
    if (response.code === 200 || response.code === 0) {
      workflowDefinition.value = response.data
    }
  } catch (error) {
     
  }
}

 
const loadPushTargets = async (moduleCode) => {
  try {
    const response = await getPushTargets(moduleCode)
    if (response.code === 200 || response.code === 0) {
      pushTargets.value = response.data || []
    }
  } catch (error) {
    
  }
}
 
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
      
      await loadApprovalHistory(billId)
      return response.data
    }
    throw new Error(response.msg || '审批失败')
  } catch (error) {
    throw error
  }
}

 
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
     
  }
}
 
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
     
      await loadPushHistory(sourceId)
      return response.data
    }
    throw new Error(response.msg || '下推失败')
  } catch (error) {
    throw error
  }
}

 
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

 
const loadPushHistory = async (billId) => {
  if (!engineConfig.push) return
  
  try {
    const response = await getPushHistory({
      moduleCode: engineConfig.push.moduleCode,
      billId
    })
    
    if (response.code === 200 || response.code === 0) {
      // You can update the UI here
    }
  } catch (error) {
   
  }
}

 
const handleOpenPushDialog = async (row) => {
  if (!engineConfig.push || pushTargets.value.length === 0) {
    ElMessage.warning('没有可用的下推目标')
    return
  }
  
  pushTargetModule.value = pushTargets.value[0]?.targetModule
  pushDialogVisible.value = true
}

 
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

const handleSubmit = async (data) => {
  try {
    submitLoading.value = true
    const isNew = !data.id
    const apiMethod = await getApiMethod(isNew ? 'add' : 'update')
    
    if (!apiMethod) {
      ElMessage.error('API 未配置')
      return
    }
    
    await apiMethod(data)
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

const handleAddEntryRow = () => {
  const entryTab = parsedConfig.form?.formTabs?.tabs?.find(tab => tab.name === 'entry')
  const columns = entryTab?.table?.columns || []
  
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

const handleDeleteEntryRow = (index) => {
  entryList.value.splice(index, 1)
}

// ==================== Deletion and Review ==================== 
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

// ==================== API   ====================
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

// ==================== Data initialization ====================
const initDateRange = () => {
  const searchFields = parsedConfig.search?.fields || []
  
  // ✅ 优先处理 daterange 类型的单个字段（如 FDate）
  const dateRangeField = searchFields.find(f => 
    f.component === 'daterange' && 
    f.defaultValue && 
    Array.isArray(f.defaultValue) && 
    f.defaultValue.length === 2
  )
  
  if (dateRangeField) {
    const startDate = parseDynamicDate(dateRangeField.defaultValue[0])
    const endDate = parseDynamicDate(dateRangeField.defaultValue[1])
    
    if (startDate && endDate) {
      dateRange.value = [startDate, endDate]
      queryParams.value.beginDate = startDate
      queryParams.value.endDate = endDate
      return  // ✅ 提前返回，不再执行后面的 beginDate/endDate 逻辑
    }
  }
  
  // === 原有逻辑保持不变（兼容旧格式）===
  const beginDateField = searchFields.find(f => f.field === 'beginDate')
  const endDateField = searchFields.find(f => f.field === 'endDate')
  
  let beginDateValue = null
  let endDateValue = null
  
  if (beginDateField && beginDateField.defaultValue) {
    beginDateValue = parseDynamicDate(beginDateField.defaultValue)
  }
  
  if (endDateField && endDateField.defaultValue) {
    endDateValue = parseDynamicDate(endDateField.defaultValue)
  }
  
  if (beginDateValue && endDateValue) {
    dateRange.value = [beginDateValue, endDateValue]
  } else {
    // Fallback: 本月 1 号到今天
    const now = new Date()
    const firstDayOfMonth = new Date(now.getFullYear(), now.getMonth(), 1)
    dateRange.value = [
      dayjs(firstDayOfMonth).format('YYYY-MM-DD'),
      dayjs(now).format('YYYY-MM-DD')
    ]
  }
  
  queryParams.value.beginDate = dateRange.value[0]
  queryParams.value.endDate = dateRange.value[1]
}

const parseDynamicDate = (value) => {
  if (!value) return null
  
  const today = new Date()
  
  if (value === 'today') {
    return dayjs(today).format('YYYY-MM-DD')
  }
  
  if (value === 'yesterday') {
    const yesterday = new Date(today)
    yesterday.setDate(yesterday.getDate() - 1)
    return dayjs(yesterday).format('YYYY-MM-DD')
  }
  
  if (value === 'monthStart') {
    const monthStart = new Date(today.getFullYear(), today.getMonth(), 1)
    return dayjs(monthStart).format('YYYY-MM-DD')
  }
  
  if (value === 'yearStart') {
    const yearStart = new Date(today.getFullYear(), 0, 1)
    return dayjs(yearStart).format('YYYY-MM-DD')
  }
  
  const dateRegex = /^\d{4}-\d{2}-\d{2}$/
  if (dateRegex.test(value)) {
    return value
  }
  
  const parsed = dayjs(value)
  if (parsed.isValid()) {
    return parsed.format('YYYY-MM-DD')
  }
  
  return null
}

onMounted(async () => {
  try { 
    await initConfig()
     
    await preloadDictionaries()
     
    await initEngineConfig()
     
    initDateRange()
     
    getList()
  } catch (error) {
    ElMessage.error(`页面初始化失败：${error.message}`)
  }
})
</script>

<style scoped>
@import './style/index.css';

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
</style>
