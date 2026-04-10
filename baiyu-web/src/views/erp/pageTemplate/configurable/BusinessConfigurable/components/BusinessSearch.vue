<template>
  <el-card shadow="never" class="search-card">
    <!-- Header -->
    <div class="page-header">
      <el-icon :size="20" v-if="pageConfig?.icon">
        <component :is="pageConfig.icon" />
      </el-icon>
      <span class="page-title">{{ title }}</span>
    </div>
    
    <!-- Toolbar -->
    <div class="toolbar-row">
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
          <!-- Date range -->
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
          
          <!-- Single date -->
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
          
          <!-- Text input -->
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
          
          <!-- Dropdown select -->
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

// Computed
const searchFields = computed(() => props.searchConfig?.fields || [])
const title = computed(() => {
  const titleTemplate = props.pageConfig?.title || '{entityName} Management'
  // Use entityName from businessConfig first, fallback to pageConfig
  const entityName = props.businessConfig?.entityName || props.pageConfig?.entityName || 'Data'
  return titleTemplate.replace(/{entityName}/g, entityName)
})
const toolbarActions = computed(() => {
  // Use actionsConfig first, fallback to searchConfig.toolbarActions
  return props.actionsConfig?.toolbar?.filter(a => a.position === 'left') || 
         props.searchConfig?.toolbarActions || []
})

// Date range model
const dateRangeModel = ref([...props.dateRange])

// Watch parent dateRange
watch(() => props.dateRange, (newVal) => {
  dateRangeModel.value = [...newVal]
}, { deep: true })

// Dict options
const getDictOptions = (dictName, staticOptions = null, required = false) => {
  if (staticOptions && Array.isArray(staticOptions)) {
    return staticOptions
  }
  
  const dataFromManager = dictionaryManager.getDictOptions(dictName)
  
  if (!dataFromManager || dataFromManager.length === 0) {
    if (required) {
      //  记录到日志而不是警告
      console.log(`[BusinessSearch] ℹ️ Required dictionary '${dictName}' data is empty - this may affect form functionality`)
    }
    return []
  }
  
  // Special handling for salespersons
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

// Button disabled
const getButtonDisabled = (disabledKey) => {
  if (!disabledKey) return false
  if (disabledKey === 'single') return props.selectionInfo?.single ?? false
  if (disabledKey === 'multiple') return props.selectionInfo?.multiple ?? false
  return false
}

// Event handlers
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
  // Trigger parent action event
  emit('action', handlerName)
}
</script>

<style scoped>
/*  Use parent global styles */

/* Page header */
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

/* Toolbar row */
.toolbar-row {
  margin-bottom: 8px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}

/* Search form */
.search-form {
  width: 100%;
}

.search-form :deep(.el-form-item) {
  margin-bottom: 2px;   
  margin-right: 8px;  
}
</style>
