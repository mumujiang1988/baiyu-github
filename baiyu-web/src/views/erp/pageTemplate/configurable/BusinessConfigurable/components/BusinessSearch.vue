<template>
  <el-card shadow="never" class="search-card">
    <div class="page-header" v-if="pageConfig?.icon || title">
      <el-icon :size="20" v-if="pageConfig?.icon">
        <component :is="pageConfig.icon" />
      </el-icon>
      <span class="page-title">{{ title }}</span>
    </div>
    
    <div class="toolbar-row" v-if="toolbarActions && toolbarActions.length > 0">
      <el-space wrap>
        <el-button
          v-for="action in toolbarActions"
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
    
    <el-form 
      :model="queryParams" 
      ref="queryRef" 
      :inline="true" 
      label-width="70px" 
      size="default" 
      class="search-form"
    >
      <template v-for="field in searchFields" :key="field.field">
        <el-form-item :label="field.label" :prop="field.field">
          <!-- 日期范围 -->
          <el-date-picker
            v-if="field.component === 'daterange'"
            v-model="dateRangeModel"
            :type="field.component"
            range-separator="至"
            :start-placeholder="field.props.startPlaceholder"
            :end-placeholder="field.props.endPlaceholder"
            :value-format="field.props.valueFormat"
            :style="field.props.style"
            @change="handleDateChange"
          />
          
          <!-- 单个日期 -->
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
          
          <!-- 文本输入 -->
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
          
          <!-- 下拉选择 -->
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
        <el-button icon="Refresh" @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup name="BusinessSearch">
import { ref, computed, watch } from 'vue'
import dictionaryManager from '@/views/erp/utils/DictionaryManager'

const props = defineProps({
  searchConfig: {
    type: Object,
    required: true
  },
  pageConfig: {
    type: Object,
    default: null
  },
  businessConfig: {
    type: Object,
    default: null
  },
  actionsConfig: {
    type: Object,
    default: null
  },
  queryParams: {
    type: Object,
    required: true
  },
  dateRange: {
    type: Array,
    default: () => []
  },
  selectionInfo: {
    type: Object,
    default: () => ({
      single: false,
      multiple: false
    })
  }
})

const emit = defineEmits(['query', 'reset'])

// 计算属性
const searchFields = computed(() => props.searchConfig?.fields || [])
const title = computed(() => {
  const titleTemplate = props.pageConfig?.title || '{entityName}管理'
  // 优先从 businessConfig 获取 entityName，兼容 pageConfig
  const entityName = props.businessConfig?.entityName || props.pageConfig?.entityName || '数据'
  return titleTemplate.replace(/{entityName}/g, entityName)
})
const toolbarActions = computed(() => {
  // 优先从 actionsConfig 获取，兼容旧版从 searchConfig 获取
  return props.actionsConfig?.toolbar?.filter(a => a.position === 'left') || 
         props.searchConfig?.toolbarActions || []
})

// 日期范围本地模型
const dateRangeModel = ref([...props.dateRange])

// 监听父组件 dateRange 变化
watch(() => props.dateRange, (newVal) => {
  dateRangeModel.value = [...newVal]
}, { deep: true })

// 字典选项
const getDictOptions = (dictName, staticOptions = null, required = false) => {
  if (staticOptions && Array.isArray(staticOptions)) {
    return staticOptions
  }
  
  const dataFromManager = dictionaryManager.getDictOptions(dictName)
  
  if (!dataFromManager || dataFromManager.length === 0) {
    if (required) {
      console.warn(`[BusinessSearch] 必填字典 ${dictName} 数据为空`)
    }
    return []
  }
  
  // 特殊处理销售员字典
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

// 按钮禁用状态
const getButtonDisabled = (disabledKey) => {
  if (!disabledKey) return false
  if (disabledKey === 'single') return props.selectionInfo?.single ?? false
  if (disabledKey === 'multiple') return props.selectionInfo?.multiple ?? false
  return false
}

// 事件处理
const handleQuery = () => {
  emit('query')
}

const handleReset = () => {
  emit('reset')
}

const handleDateChange = () => {
  emit('query')
}

const handleAction = (handlerName) => {
  // 触发父组件的 action 事件
  emit('action', handlerName)
}
</script>

<style scoped>
/* ✅ 使用父组件的全局样式，不重复定义 */

/* 页面标题样式 */
.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
  margin-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  line-height: 1.5;
}

.page-header .el-icon {
  color: #409EFF;
  font-size: 20px;
}

/* 工具栏行样式 */
.toolbar-row {
  margin-bottom: 8px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}

/* 搜索表单样式 */
.search-form {
  width: 100%;
}

.search-form :deep(.el-form-item) {
  margin-bottom: 2px;  /* 从 4px 改为 2px，更紧凑 */
  margin-right: 8px;  /* 从 16px 改为 12px，减少横向间距 */
}
</style>
