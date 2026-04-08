<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-card shadow="never" class="search-card" v-if="parsedConfig.search?.showSearch">
      <!-- 第一排：操作按钮 -->
      <div class="toolbar-row" v-if="leftToolbarActions.length > 0">
        <el-space wrap>
          <el-button
            v-for="action in leftToolbarActions"
            :key="action.label"
            :type="action.type"
            :icon="action.icon"
            :disabled="getButtonDisabled(action.disabled)"
            @click="handleAction(action.handler)"
            v-hasPermi="[action.permission]"
          >
            {{ action.label }}
          </el-button>
        </el-space>
      </div>
      
      <!-- 第二排：查询条件和查询按钮 -->
      <el-form :model="queryParams" ref="queryRef" :inline="true" label-width="70px" size="default" class="search-form">
        <!-- 动态渲染查询字段 -->
        <template v-for="field in parsedConfig.search?.fields" :key="field.field">
          <el-form-item :label="field.label" :prop="field.field">
            <!-- 日期范围选择器 -->
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
            
            <!-- 普通输入框 -->
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
            
            <!-- 下拉选择框 -->
            <el-select
              v-else-if="field.component === 'select'"
              v-model="queryParams[field.field]"
              :placeholder="field.props.placeholder"
              :clearable="field.props.clearable"
              :filterable="field.props.filterable"
              :style="field.props.style"
            >
              <el-option
                v-for="option in getDictOptions(field.dictionary, field.options)"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              />
            </el-select>
          </el-form-item>
        </template>
        
        <!-- 查询和重置按钮 -->
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格区域 -->
    <el-card shadow="never" class="table-card">
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
        <!-- 动态渲染列 -->
        <template v-for="(column, index) in visibleColumns" :key="index">
          <!-- 选择列 -->
          <el-table-column
            v-if="column.type === 'selection'"
            :type="column.type"
            :width="column.width"
            :fixed="column.fixed"
            :resizable="column.resizable"
          />
          
          <!-- 展开列 - 改为操作列 -->
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
          
          <!-- 普通列 -->
          <el-table-column
            v-else
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
              <!-- 标签渲染 -->
              <el-tag
                v-if="column.renderType === 'tag'"
                :type="getTagConfig(scope.row[column.prop], column.dictionary).type"
                size="small"
                disable-transitions
              >
                {{ getTagConfig(scope.row[column.prop], column.dictionary).label }}
              </el-tag>
              
              <!-- 链接渲染 -->
              <el-link
                v-else-if="column.renderType === 'link'"
                type="primary"
                :underline="false"
              >
                {{ scope.row[column.prop] }}
              </el-link>
              
              <!-- 货币渲染 -->
              <span v-else-if="column.renderType === 'currency'">
                {{ formatCurrency(scope.row[column.prop], column.precision) }}
              </span>
              
              <!-- 日期渲染 -->
              <span v-else-if="column.renderType === 'date'">
                {{ formatDate(scope.row[column.prop], column.format) }}
              </span>
              
              <!-- 日期时间渲染 -->
              <span v-else-if="column.renderType === 'datetime'">
                {{ formatDateTime(scope.row[column.prop], column.format) }}
              </span>
              
              <!-- 百分比渲染 -->
              <span v-else-if="column.renderType === 'percent'">
                {{ formatPercent(scope.row[column.prop], column.precision) }}
              </span>
              
              <!-- 默认文本 -->
              <span v-else>
                {{ scope.row[column.prop] ?? '-' }}
              </span>
            </template>
          </el-table-column>
        </template>
      </el-table>
      
      <!-- 分页 -->
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
      </div>
    </el-card>

    <!-- 编辑对话框 -->
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
          <!-- 动态渲染表单分区 -->
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
              <!-- 动态渲染表单字段 -->
              <el-col
                v-for="field in section.fields"
                :key="field.field"
                :span="field.span || (24 / section.columns)"
              >
                <el-form-item :label="field.label" :prop="field.field">
                  <!-- 输入框 -->
                  <el-input
                    v-if="field.component === 'input'"
                    v-model="formData[field.field]"
                    v-bind="field.componentProps"
                    clearable
                  />
                  
                  <!-- 日期选择器 -->
                  <el-date-picker
                    v-else-if="['date', 'datetime'].includes(field.component)"
                    v-model="formData[field.field]"
                    :type="field.component"
                    :placeholder="field.componentProps?.placeholder || '选择日期'"
                    :value-format="field.componentProps?.valueFormat || 'YYYY-MM-DD'"
                    style="width: 100%"
                  />
                  
                  <!-- 数字输入框 -->
                  <el-input-number
                    v-else-if="field.component === 'input-number'"
                    v-model="formData[field.field]"
                    v-bind="field.componentProps"
                    style="width: 100%"
                  />
                  
                  <!-- 下拉选择框 -->
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
                      v-for="option in getDictOptions(field.dictionary, field.options)"
                      :key="option.value"
                      :label="option.label"
                      :value="option.value"
                    />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
          </el-card>
          
          <!-- 页签形式的明细和成本表格 -->
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
                <!-- 明细表格页签 -->
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
                
                <!-- 成本表单页签 -->
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
          <!-- 动态渲染页签 -->
          <el-tab-pane
            v-for="tab in parsedConfig.drawer?.tabs || []"
            :key="tab.name"
            :label="tab.label"
            :name="tab.name"
          >
            <!-- 表格类型页签 -->
            <div v-if="tab.type === 'table' || !tab.type" class="tab-content">
              <div v-if="!currentDetailRow[tab.dataField] || currentDetailRow[tab.dataField].length === 0" class="tab-empty">
                <el-empty :description="`暂无${tab.label}数据`" :image-size="120" />
              </div>
              <el-table 
                v-else
                :data="currentDetailRow[tab.dataField] || []" 
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
                    <span v-if="col.renderType === 'currency'">{{ formatAmount(row[col.prop]) }}</span>
                    <span v-else-if="col.renderType === 'number'">{{ row[col.prop] }}</span>
                    <span v-else>{{ row[col.prop] }}</span>
                  </template>
                </el-table-column>
              </el-table>
            </div>
            
            <!-- 描述列表类型页签 -->
            <div v-else-if="tab.type === 'descriptions'" class="tab-content">
              <div v-if="!currentDetailRow[tab.dataField] || Object.keys(currentDetailRow[tab.dataField]).length === 0" class="tab-empty">
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
                  <span v-if="field.renderType === 'currency'">{{ formatAmount(currentDetailRow[tab.dataField][field.prop]) }}</span>
                  <span v-else-if="field.renderType === 'percent'">{{ currentDetailRow[tab.dataField][field.prop] ? (currentDetailRow[tab.dataField][field.prop] + '%') : '-' }}</span>
                  <span v-else>{{ currentDetailRow[tab.dataField][field.prop] ?? '-' }}</span>
                </el-descriptions-item>
              </el-descriptions>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-drawer>
  </div>
</template>

<script setup name="SaleOrderConfig">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading, View, Document, Money } from '@element-plus/icons-vue'
import ERPConfigParser from '@/utils/erpConfigParser'
import saleOrderConfig from '../configs/saleOrder.config.json'
import {
  listSaleOrder,
  getSaleOrder,
  addSaleOrder,
  updateSaleOrder,
  deleteSaleOrder,
  auditSaleOrders,
  unAuditSaleOrders,
  getOrderEntry,
  getOrderCost
} from '@/api/k3/saleOrder'
import dayjs from 'dayjs'

// 配置解析器
const parser = new ERPConfigParser(saleOrderConfig)

// 解析后的配置
const parsedConfig = reactive({
  page: {},
  search: {},
  table: {},
  form: {},
  drawer: {},
  actions: {}
})

// 状态数据
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

// 抽屉相关状态
const drawerVisible = ref(false)
const drawerTitle = ref('订单详情')
const drawerLoading = ref(false)
const currentDetailRow = ref({})
const detailActiveTab = ref('entry')

// 明细表格和成本数据
const entryList = ref([])
const costData = ref({})

// 表单页签激活状态
const formActiveTab = ref('entry')

// 国家搜索相关
const nationSearchLoading = ref(false)
const nationOptions = ref([])

// 按钮状态
const buttonState = reactive({
  single: true,
  multiple: true
})

// 左侧工具栏按钮
const leftToolbarActions = computed(() => {
  return parsedConfig.actions?.toolbar?.filter(a => a.position === 'left') || []
})

// 右侧工具栏按钮
const rightToolbarActions = computed(() => {
  return parsedConfig.actions?.toolbar?.filter(a => a.position === 'right') || []
})

// 可见的列
const visibleColumns = computed(() => {
  return parsedConfig.table?.columns?.filter(col => col.visible !== false) || []
})

// 表单验证规则
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

// 初始化配置
const initConfig = async () => {
  // 解析配置
  parsedConfig.page = parser.parsePageConfig()
  parsedConfig.search = parser.parseSearchForm()
  parsedConfig.table = parser.parseTableColumns()
  parsedConfig.form = parser.parseFormConfig()
  parsedConfig.drawer = parser.parseDrawerConfig()
  parsedConfig.actions = parser.parseActions()
  
  // 加载字典
  await parser.loadDictionaries()
}

// 获取字典选项
const getDictOptions = (dictName, staticOptions) => {
  if (staticOptions && Array.isArray(staticOptions)) {
    return staticOptions
  }
  
  // 如果是国家字典且启用了远程搜索，优先使用搜索结果
  if (dictName === 'nation') { 
    if (nationOptions.value.length > 0) {
      return nationOptions.value
    }
    // 未搜索时不显示任何数据，等待用户输入
    return []
  }
  
  return parser.getDictOptions(dictName) || []
}

// 获取按钮禁用状态
const getButtonDisabled = (disabledKey) => {
  if (!disabledKey) return false
  return buttonState[disabledKey] || false
}

// 获取标签配置
const getTagConfig = (value, dictName) => {
  const dict = getDictOptions(dictName)
  const option = dict.find(item => item.value === value)
  return {
    label: option?.label || value,
    type: option?.type || 'info'
  }
}

// 格式化货币
const formatCurrency = (value, precision = 2) => {
  if (!value && value !== 0) return '-'
  return Number(value).toLocaleString('zh-CN', {
    minimumFractionDigits: precision,
    maximumFractionDigits: precision
  })
}

// 格式化日期
const formatDate = (value, format = 'YYYY-MM-DD') => {
  if (!value) return '-'
  try {
    return dayjs(value).format(format)
  } catch (error) {
    return value
  }
}

// 格式化日期时间
const formatDateTime = (value, format = 'YYYY-MM-DD HH:mm:ss') => {
  if (!value) return '-'
  try {
    return dayjs(value).format(format)
  } catch (error) {
    return value
  }
}

// 格式化百分比
const formatPercent = (value, precision = 2) => {
  if (!value && value !== 0) return '-'
  return `${Number(value).toFixed(precision)}%`
}

// 查询列表
const getList = async () => {
  loading.value = true
  try {
    const response = await listSaleOrder(queryParams.value)
    tableData.value = response.rows || []
    total.value = response.total || 0
  } catch (error) {
    ElMessage.error('查询列表失败')
  } finally {
    loading.value = false
  }
}

// 处理查询
const handleQuery = () => {
  if (dateRange.value && dateRange.value.length === 2) {
    queryParams.value.beginDate = dateRange.value[0]
    queryParams.value.endDate = dateRange.value[1]
  } else {
    queryParams.value.beginDate = undefined
    queryParams.value.endDate = undefined
  }
  queryParams.value.pageNum = 1
  getList()
}

// 重置查询
const resetQuery = () => {
  queryRef.value?.resetFields()
  const now = new Date()
  const firstDayOfMonth = new Date(now.getFullYear(), now.getMonth(), 1)
  dateRange.value = [
    dayjs(firstDayOfMonth).format('YYYY-MM-DD'),
    dayjs(now).format('YYYY-MM-DD')
  ]
  handleQuery()
}

// 处理分页
const handlePageSizeChange = (newSize) => {
  queryParams.value.pageSize = newSize
  getList()
}

const handlePageChange = (newPage) => {
  queryParams.value.pageNum = newPage
  getList()
}

// 处理选择变化
const handleSelectionChange = (selection) => {
  ids.value = selection.map(item => item.id)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

// 处理表格行点击
const handleRowClick = (row) => {
  // 可以在这里添加行点击逻辑
}

// 查看详情
const handleViewDetail = async (row) => {
  drawerVisible.value = true
  // 使用配置化的标题模板
  const titleTemplate = parsedConfig.drawer?.title || '订单详情 - {fbillNo}'
  drawerTitle.value = titleTemplate.replace(/{fbillNo}/g, row.fbillNo || '')
  drawerLoading.value = true
  currentDetailRow.value = { ...row }
  
  try {
    // 加载明细数据
    if (!row.entryList || row.entryList.length === 0) {
      if (row.fbillNo) {
        const entryResponse = await getOrderEntry(row.fbillNo)
        if (entryResponse.code === 200 || entryResponse.code === 0 || entryResponse.errorCode === 0) {
          currentDetailRow.value.entryList = entryResponse.data && Array.isArray(entryResponse.data) 
            ? entryResponse.data 
            : (entryResponse.data || [])
        }
      }
    } else {
      currentDetailRow.value.entryList = row.entryList
    }
    
    // 加载成本数据
    if (!row.costData || Object.keys(row.costData).length === 0) {
      if (row.fbillNo) {
        const costResponse = await getOrderCost(row.fbillNo)
        if (costResponse.code === 200 || costResponse.code === 0 || costResponse.errorCode === 0) {
          currentDetailRow.value.costData = costResponse.data && typeof costResponse.data === 'object' && Object.keys(costResponse.data).length > 0
            ? costResponse.data
            : (costResponse.data || null)
        }
      }
    } else {
      currentDetailRow.value.costData = row.costData
    }
    
    // 设置默认激活的标签
    const hasEntryData = currentDetailRow.value.entryList && currentDetailRow.value.entryList.length > 0
    const hasCostData = currentDetailRow.value.costData && Object.keys(currentDetailRow.value.costData).length > 0
    detailActiveTab.value = hasEntryData ? 'entry' : (hasCostData ? 'cost' : 'entry')
    
  } catch (error) {
    console.error('加载详情数据失败:', error)
    ElMessage.error('加载详情数据失败')
  } finally {
    drawerLoading.value = false
  }
}

// 关闭抽屉
const handleDrawerClose = (done) => {
  currentDetailRow.value = {}
  done()
}

// 格式化金额
const formatAmount = (value) => {
  if (!value && value !== 0) return '-'
  return Number(value).toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

// 处理操作
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

// 打开对话框
const openDialog = (type) => {
  // 初始化表单数据为默认值
  formData.value = {}
  
  // 如果是新增模式，应用配置中的默认值
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
      ElMessage.warning('请选择一条数据')
      return
    }
    loadFormData(ids.value[0])
    dialogTitle.value = '修改销售订单'
  } else {
    dialogTitle.value = '新增销售订单'
  }
  
  dialogVisible.value = true
}

// 加载表单数据
const loadFormData = async (id) => {
  try {
    const response = await getSaleOrder(id)
    formData.value = response.data || response
    // 加载明细数据
    if (formData.value.fbillNo) {
      const entryResponse = await getOrderEntry(formData.value.fbillNo)
      if (entryResponse.code === 200 || entryResponse.code === 0 || entryResponse.errorCode === 0) {
        entryList.value = entryResponse.data && Array.isArray(entryResponse.data) 
          ? entryResponse.data 
          : (entryResponse.data || [])
      }
      // 加载成本数据
      const costResponse = await getOrderCost(formData.value.fbillNo)
      if (costResponse.code === 200 || costResponse.code === 0 || costResponse.errorCode === 0) {
        costData.value = costResponse.data && typeof costResponse.data === 'object' && Object.keys(costResponse.data).length > 0
          ? costResponse.data
          : {}
      }
    }
  } catch (error) {
    ElMessage.error('加载数据失败')
  }
}

// 提交表单
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
    
    if (submitData.id) {
      await updateSaleOrder(submitData)
      ElMessage.success('修改成功')
    } else {
      await addSaleOrder(submitData)
      ElMessage.success('新增成功')
    }
    
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

// 确认删除
const confirmDelete = async () => {
  if (!ids.value || ids.value.length === 0) {
    ElMessage.warning('请选择要删除的数据')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `是否确认删除选中的 ${ids.value.length} 条数据？`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteSaleOrder(ids.value)
    ElMessage.success('删除成功')
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败：' + (error.message || '未知错误'))
    }
  }
}

// 批量审核
const batchAudit = async () => {
  if (!ids.value || ids.value.length === 0) {
    ElMessage.warning('请选择要审核的数据')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `是否确认审核选中的 ${ids.value.length} 条数据？`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await auditSaleOrders(ids.value)
    ElMessage.success('审核成功')
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('审核失败：' + (error.message || '未知错误'))
    }
  }
}

// 批量反审核
const batchUnAudit = async () => {
  if (!ids.value || ids.value.length === 0) {
    ElMessage.warning('请选择要反审核的数据')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `是否确认反审核选中的 ${ids.value.length} 条数据？`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await unAuditSaleOrders(ids.value)
    ElMessage.success('反审核成功')
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

// 添加明细行
const handleAddEntryRow = () => {
  entryList.value.push({
    fPlanMaterialId: '',
    fPlanMaterialName: '',
    fQty: 0,
    fPrice: 0,
    fTaxPrice: 0,
    fAllAmount: 0,
    fDeliQty: 0,
    f_mz: 0,
    f_jz: 0,
    f_kpdj: 0,
    f_ygcb: 0,
    f_hsbm: '',
    f_cplb: ''
  })
}

// 删除明细行
const handleDeleteEntryRow = (index) => {
  entryList.value.splice(index, 1)
}

// 搜索国家（模糊搜索）
const searchNations = async (keyword) => {


}

// 初始化
onMounted(async () => {
  await initConfig()
  
  // 设置默认日期区间为当月
  const now = new Date()
  const firstDayOfMonth = new Date(now.getFullYear(), now.getMonth(), 1)
  dateRange.value = [
    dayjs(firstDayOfMonth).format('YYYY-MM-DD'),
    dayjs(now).format('YYYY-MM-DD')
  ]
  
  // 设置查询参数的日期
  queryParams.value.beginDate = dateRange.value[0]
  queryParams.value.endDate = dateRange.value[1]
  
  getList()
})
</script>

<style scoped>
@import './saleOrder.styles.css';

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