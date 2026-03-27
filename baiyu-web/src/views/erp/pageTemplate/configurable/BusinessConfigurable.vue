<template>
  <div class="app-container">
    <!-- 搜索区域 -->
    <el-card shadow="never" class="search-card" v-if="parsedConfig.search?.showSearch">
      <!-- 页面标题 -->
      <div class="page-header" v-if="pageTitle">
        <el-icon :size="20" v-if="parsedConfig.page?.icon"><component :is="parsedConfig.page.icon" /></el-icon>
        <span class="page-title">{{ pageTitle }}</span>
      </div>
      
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
            v-hasPermi="action.permission ? [action.permission] : []"
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
                v-for="option in getDictOptions(field.dictionary, field.options, field.dictRequired)"
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

<script setup name="BusinessConfigurable">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading, View, Document, Money } from '@element-plus/icons-vue'
import ERPConfigParser from '@/views/erp/utils/ERPConfigParser'
import dayjs from 'dayjs'
import request from '@/utils/request'

// ==================== 导入公共工具模块 ====================
import { formatCurrency, formatDate, formatDateTime, formatPercent, formatAmount } from '@/views/erp/utils'
import { isSuccessResponse, getResponseData } from '@/views/erp/utils'

// ==================== 导入多表格查询构建器 ====================
import multiTableQueryBuilder from '../utils/multiTableQueryBuilder'

// ==================== 导入字典构建器 ====================
import dictionaryBuilderEngine, { DictionaryBuilder } from '@/views/erp/utils/DictionaryBuilder'

// ==================== 导入引擎 API====================
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

// ==================== Props 定义（强制在线模式）====================
const props = defineProps({
  // 模块编码，用于从数据库加载配置（从路由 query 参数获取）
  moduleCode: {
    type: String,
    required: false,
    default: ''
  }
})

// ==================== 路由参数获取 ====================
const route = useRoute()
// 优先从路由 query 参数获取，其次从 props 获取，最后使用默认值
const getModuleCode = () => {
  return route.query.moduleCode || props.moduleCode  
}

// ==================== 业务模板配置（从 currentConfig 获取）====================
const BusinessTemplate = computed(() => ({
  apiConfig: currentConfig.value?.apiConfig || {},
  dictionaryConfig: currentConfig.value?.dictionaryConfig || {},
  pageConfig: currentConfig.value?.pageConfig || {}
}))

// ==================== 动态 API 导入（根据配置）====================
const apiModuleMap = new Map()

// ==================== 通用引擎 API（低代码方案）====================
/**
 * 通用列表查询接口 - 使用 ERP 引擎构建器模式（支持多表格）
 */
const getList = async () => {
  loading.value = true
  try {
    // 构建主表格的 queryConfig 配置
    const mainQueryConfig = buildMainQueryConfig()
    
    // 获取主表表名
    const tableName = getTableNameFromConfig()
    
    // 获取 moduleCode
    const moduleCode = currentConfig.value?.moduleCode
    
    if (!moduleCode) {
      throw new Error('模块配置中缺少 moduleCode 字段，无法执行查询')
    }
    
    console.log('主表格查询 - 开始')
    console.log('主表格查询 - moduleCode:', moduleCode)
    console.log('主表格查询 - 表名:', tableName)
    console.log('主表格查询 - queryConfig:', mainQueryConfig)
    console.log('主表格查询 - currentConfig:', currentConfig.value)
    console.log('主表格查询 - pageConfig:', currentConfig.value?.pageConfig)
    
    // 使用通用引擎查询接口（构建器模式）
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
    
    console.log('主表格查询结果:', response)
    console.log('主表格查询 - response.data:', response.data)
    console.log('主表格查询 - response.data.rows:', response.data?.rows)
    
    // 后端返回的是 R.ok(result),所以数据在 response.data 中
    tableData.value = response.data?.rows || []
    total.value = response.data?.total || 0
    
    // 并行查询所有子表格数据
    await loadSubTablesData()
    
  } catch (error) {
    console.error(' 主表格查询失败:', error)
    ElMessage.error(businessConfig.value.messages?.error?.load || '查询列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 构建主表格的 queryConfig 配置（构建器模式）
 */
const buildMainQueryConfig = () => {
  const conditions = []
  
  // 从 searchConfig 构建查询条件
  const searchFields = parsedConfig.search?.fields || []
  
  searchFields.forEach(field => {
    const value = queryParams.value[field.field]
    const operator = field.queryOperator || 'eq'
    
    // 跳过空值
    if (value === undefined || value === null || value === '') {
      return
    }
    
    // 日期范围特殊处理
    if (field.component === 'daterange' && Array.isArray(dateRange.value) && dateRange.value.length === 2) {
      conditions.push({
        field: field.field,
        operator: 'between',
        value: dateRange.value
      })
    } else if (Array.isArray(value)) {
      // IN 条件
      conditions.push({
        field: field.field,
        operator: 'in',
        value: value
      })
    } else {
      // 单个值条件
      conditions.push({
        field: field.field,
        operator: operator,
        value: value
      })
    }
  })
  
  // 构建排序配置
  const orderBy = parsedConfig.table?.orderBy || [
    { field: 'FCreateDate', direction: 'DESC' }
  ]
  
  return {
    conditions: conditions,
    orderBy: orderBy
  }
}

/**
 * 加载子表格数据（明细表和成本表）
 */
const loadSubTablesData = async () => {
  try {
    // 检查是否有子表格配置
    const subTableConfigs = multiTableQueryBuilder.parseSubTableConfigs(currentConfig.value)
    
    if (subTableConfigs.length === 0) {
      console.log('没有配置子表格，跳过查询')
      return
    }
    
    // 准备上下文数据（用于替换模板变量）
    const contextData = {
      billNo: 'PENDING' // 主表格加载后才会知道具体的 billNo，这里先不查询
    }
    
    // 暂时不查询子表格，等待展开行或详情页时再查询
    console.log(' 子表格配置已解析，等待需要时再查询')
  } catch (error) {
    console.warn(' 加载子表格配置失败:', error.message)
  }
}

/**
 * 查询指定单据的子表格数据（用于展开行或详情页）
 * @param {String} billNo - 单据编号
 */
const loadSubTablesByBillNo = async (billNo) => {
  try {
    const moduleCode = currentConfig.value?.moduleCode
    
    if (!moduleCode) {
      throw new Error('模块配置中缺少 moduleCode 字段，无法执行查询')
    }
    
    // 使用多表格查询构建器
    const subTableConfigs = multiTableQueryBuilder.parseSubTableConfigs(currentConfig.value)
    
    if (subTableConfigs.length === 0) {
      return
    }
    
    // 并行查询所有子表格
    const results = await multiTableQueryBuilder.queryAllSubTables(
      moduleCode,
      subTableConfigs,
      { billNo } // 上下文数据，用于替换 ${billNo}
    )
    
    console.log(' 子表格查询完成:', results)
    
    // 存储到对应的数据变量中
    if (results.entry) {
      entryList.value = results.entry.data
      console.log(` 明细表数据：${results.entry.data.length} 条`)
    }
    
    if (results.cost) {
      costData.value = results.cost.data[0] || {}
      console.log(' 成本表数据:', results.cost.data[0])
    }
  } catch (error) {
    console.error(' 查询子表格失败:', error)
  }
}

/**
 * 从配置获取表名
 * @returns {string} 表名
 * @throws {Error} 当配置中未指定表名时抛出错误
 */
const getTableNameFromConfig = () => {
  const tableName = currentConfig.value?.pageConfig?.tableName
  
  if (!tableName) {
    const moduleCode = getModuleCode()
    console.error(`模块 [${moduleCode}] 的配置中缺少 pageConfig.tableName 字段`)
    throw new Error(`配置错误：请在 JSON 配置的 pageConfig.tableName 中指定表名`)
  }
  
  return tableName
}

// 配置解析器
let parser = null

// 当前使用的配置（仅数据库配置）
const currentConfig = ref(null)

// 从配置中获取 API 方法映射
const apiMethods = computed(() => currentConfig.value?.apiConfig?.methods || {})

// 从配置中获取业务配置
const businessConfig = computed(() => currentConfig.value?.businessConfig || {})

// 页面标题
const pageTitle = computed(() => {
  const titleTemplate = parsedConfig.page?.title || '{entityName}管理'
  const entityName = businessConfig.value.entityName || '数据'
  return titleTemplate.replace(/{entityName}/g, entityName)
})

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

// 引擎相关配置
const engineConfig = reactive({
  query: null,      // 动态查询引擎配置
  validation: null, // 表单验证引擎配置
  approval: null,   // 审批流程引擎配置
  push: null        // 下推引擎配置
})

// 审批相关状态
const approvalStep = ref(null)
const approvalHistory = ref([])
const workflowDefinition = ref(null)

// 下推相关状态
const pushTargets = ref([])
const pushDialogVisible = ref(false)
const pushTargetModule = ref('')

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

// 初始化配置（强制从数据库加载）
const initConfig = async () => {
  try {
    // 从路由或 props 获取 moduleCode
    const moduleCode = getModuleCode()
    
    // 从数据库加载配置
    await loadDatabaseConfig(moduleCode)
    
    // 解析配置
    parsedConfig.page = parser.parsePageConfig()
    parsedConfig.search = parser.parseSearchForm()
    parsedConfig.table = parser.parseTableColumns()
    parsedConfig.form = parser.parseFormConfig()
    parsedConfig.drawer = parser.parseDrawerConfig()
    parsedConfig.actions = parser.parseActions()
    
    // 增强：在解析表单配置时标记必填字典
    markRequiredDictionaries()
    
    //  构建器模式：无需单独加载字典，在 preloadDictionaries 中统一处理
  } catch (error) {
    ElMessage.error(`加载配置失败：${error.message}`)
    throw error
  }
}

/**
 * 标记必填字典（用于后续验证）
 */
const markRequiredDictionaries = () => {
  const requiredDicts = new Set()
  
  // 遍历表单配置中的所有字段
  parsedConfig.form?.sections?.forEach(section => {
    section.fields.forEach(field => {
      // 必填字段且有字典配置
      if (field.required && field.dictionary) {
        field.dictRequired = true
        requiredDicts.add(field.dictionary)
      }
    })
  })
  
  // 遍历搜索表单中的所有字段
  parsedConfig.search?.fields?.forEach(field => {
    if (field.required && field.dictionary) {
      field.dictRequired = true
      requiredDicts.add(field.dictionary)
    }
  })
  
  // 保存必填字典列表（供后续验证使用）
  window._erpRequiredDicts = requiredDicts
  
  console.log('必填字典列表:', Array.from(requiredDicts))
}

/**
 * 从数据库加载配置（无降级方案）
 * @param {string} moduleCode - 模块编码
 */
const loadDatabaseConfig = async (moduleCode) => {
  try {
    // 使用 ERPConfigParser 的静态方法加载（带缓存）
    const configContent = await ERPConfigParser.loadFromDatabase(moduleCode)
    
    if (!configContent) {
      throw new Error(`未找到模块 [${moduleCode}] 的配置`)
    }
    
    // 更新当前配置
    currentConfig.value = configContent
    
    // 创建配置解析器
    parser = new ERPConfigParser(configContent)
  } catch (error) {
    throw new Error(`无法加载配置：${error.message}`)
  }
}

// 获取字典选项（纯构建器模式）
const getDictOptions = (dictName, staticOptions, required = false) => {
  // 优先使用静态配置
  if (staticOptions && Array.isArray(staticOptions)) {
    return staticOptions
  }
  
  // 国家字典特殊处理（远程搜索）
  if (dictName === 'nation') { 
    if (nationOptions.value.length > 0) {
      return nationOptions.value
    }
    // 未搜索时不显示任何数据，等待用户输入
    return []
  }
  
  //  使用字典构建器引擎获取（无降级方案）
  const data = dictionaryBuilderEngine.get(dictName)
  if (data && data.length > 0) {
    return data
  }
  
  //  增强验证：必填字典未注册时报错
  if (required) {
    console.error(`必填字典 "${dictName}" 未注册，页面无法正常使用`)
    ElMessage.error(`必填字典 "${dictName}" 未注册，页面无法正常使用`)
    throw new Error(`必填字典缺失：${dictName}`)
  }
  
  // 非必填字典未注册时返回空数组
  console.warn(`字典未注册：${dictName}`)
  return []
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

// 查看详情 - 使用配置中的标题
const handleViewDetail = async (row) => {
  drawerVisible.value = true
  
  // 从配置中获取单据编号字段名（完全配置化，无转换）
  const billNoField = parsedConfig.page?.billNoField || 'FBillNo'
  
  // 使用配置化的抽屉标题模板
  const titleTemplate = businessConfig.value.drawerTitle || '{entityName}详情 - {billNo}'
  const entityName = businessConfig.value.entityName || '订单'
  
  // 动态替换模板变量（支持任意字段名）
  drawerTitle.value = titleTemplate
    .replace(/{entityName}/g, entityName)
    .replace(/{billNo}/g, row[billNoField] || row.FBillNo || '')
  
  drawerLoading.value = true
  currentDetailRow.value = { ...row }
  
  try {
    // 使用新的多表格查询方法
    const billNoValue = row[billNoField] || row.FBillNo
    if (billNoValue) {
      // 查询子表格数据（明细表和成本表）
      await loadSubTablesByBillNo(billNoValue)
    }
    
    // 设置默认激活的标签
    const hasEntryData = entryList.value && entryList.value.length > 0
    const hasCostData = costData.value && Object.keys(costData.value).length > 0
    detailActiveTab.value = hasEntryData ? 'entry' : (hasCostData ? 'cost' : 'entry')
    
  } catch (error) {
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

// 打开对话框 - 使用配置中的标题
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
        if (entryResponse.code === 200 || entryResponse.code === 0 || entryResponse.errorCode === 0) {
          entryList.value = entryResponse.data && Array.isArray(entryResponse.data) 
            ? entryResponse.data 
            : (entryResponse.data || [])
        }
      }
      
      // 加载成本数据
      const costMethod = await getApiMethod('cost')
      if (costMethod) {
        const costResponse = await costMethod(billNoValue)
        if (costResponse.code === 200 || costResponse.code === 0 || costResponse.errorCode === 0) {
          costData.value = costResponse.data && typeof costResponse.data === 'object' && Object.keys(costResponse.data).length > 0
            ? costResponse.data
            : {}
        }
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
      // ✅ 使用配置中的搜索 API
      const searchApi = dictConfig.config?.searchApi || '/erp/engine/country/search?keyword={keyword}&limit=20'
      const searchUrl = searchApi.replace('{keyword}', encodeURIComponent(keyword))
      
      console.log(`🔍 搜索国家：${keyword}, URL: ${searchUrl}`)
      
      const response = await request(searchUrl)
      
      let data = []
      if (response.code === 200 || response.errorCode === 0) {
        data = response.data || response.rows || []
      } else if (Array.isArray(response)) {
        data = response
      }
      
      // ✅ 映射为标准格式 { label, value }
      nationOptions.value = data.map(item => ({
        label: item.labelZh || item.name || item.label,
        value: item.id || item.kingdee || item.value,
        labelEn: item.labelEn || item.name_en // 保留英文用于扩展
      }))
      
      console.log(`✅ 国家搜索结果：${nationOptions.value.length} 条`)
    } else {
      console.warn('⚠️ 国家字典未配置或类型不正确')
      nationOptions.value = []
    }
  } catch (error) {
    console.warn('搜索国家失败:', error.message)
    nationOptions.value = []
  } finally {
    nationSearchLoading.value = false
  }
}

/**
 * 预加载字典数据（使用构建器模式）
 */
const preloadDictionaries = async () => {
  try {
    const dictConfig = BusinessTemplate.value.dictionaryConfig
    if (!dictConfig || !dictConfig.builder?.enabled) {
      console.log(' 字典构建器未启用，跳过预加载')
      return
    }

    // 从配置构建字典
    dictionaryBuilderEngine.buildFromConfig(dictConfig.dictionaries, getModuleCode())

    // 预加载所有动态字典
    await dictionaryBuilderEngine.preloadAll(getModuleCode())

    console.log(' 字典构建器预加载完成')
  } catch (error) {
    console.warn('预加载字典失败:', error.message)
  }
}

// 初始化
onMounted(async () => {
  // 1. 先加载配置
  await initConfig()
  
  // 2. 预加载字典数据（国家、销售人员等）
  await preloadDictionaries()
  
  // 3. 初始化引擎配置
  await initEngineConfig()
  
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

// ==================== 引擎相关方法 ====================

/**
 * 初始化引擎配置
 */
const initEngineConfig = async () => {
  const moduleCode = BusinessTemplate.value.pageConfig?.moduleCode
  if (!moduleCode) return
  
  try {
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