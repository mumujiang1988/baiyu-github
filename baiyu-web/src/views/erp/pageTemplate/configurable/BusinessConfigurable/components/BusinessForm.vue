<template>
  <el-dialog
    :title="dialogTitle"
    :model-value="visible"
    :width="formConfig?.dialogWidth || '1000px'"
    append-to-body
    @close="handleClose"
    :close-on-click-modal="false"
  >
    <el-form 
      :model="formData" 
      :rules="formRules" 
      ref="formRef" 
      :label-width="formConfig?.labelWidth || '120px'"
    >
      <el-scrollbar max-height="65vh">
        <!-- Sections -->
        <el-card
          v-for="(section, index) in formConfig?.sections || []"
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
                <!-- Text input -->
                <el-input
                  v-if="field.component === 'input'"
                  v-model="formData[field.field]"
                  v-bind="field.componentProps"
                  clearable
                />
                
                <!-- Date picker -->
                <el-date-picker
                  v-else-if="['date', 'datetime'].includes(field.component)"
                  v-model="formData[field.field]"
                  :type="field.component"
                  :placeholder="field.componentProps?.placeholder || '选择日期'"
                  :value-format="field.componentProps?.valueFormat || 'YYYY-MM-DD'"
                  style="width: 100%"
                />
                
                <!-- Number input -->
                <el-input-number
                  v-else-if="field.component === 'input-number'"
                  v-model="formData[field.field]"
                  v-bind="field.componentProps"
                  style="width: 100%"
                />
                
                <!-- Dropdown select -->
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
        
        <!-- Tabs -->
        <el-card
          v-if="formConfig?.formTabs?.enabled"
          shadow="never"
          class="form-tabs-card"
        >
          <el-tabs v-model="formActiveTab" stretch>
            <el-tab-pane
              v-for="tab in formConfig.formTabs.tabs"
              :key="tab.name"
              :label="tab.label"
              :name="tab.name"
            >
              <!-- Entry table -->
              <div v-if="tab.name === 'entry' && tab.table" class="tab-pane-content">
                <div class="tab-pane-toolbar">
                  <el-button
                    v-if="tab.table.addRow"
                    type="primary"
                    size="small"
                    icon="Plus"
                    @click="handleAddEntry"
                  >
                    Add Detail
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
                    label="Actions"
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
                        @click="handleDeleteEntry(scope.$index)"
                      >
                        Delete
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
              
              <!-- Cost form -->
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
        <el-button @click="handleCancel">Cancel</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
          Confirm
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup name="BusinessForm">
import { ref, computed, watch } from 'vue'
import dictionaryManager from '@/views/erp/utils/DictionaryManager'
import request from '@/utils/request'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  formConfig: {
    type: Object,
    required: true
  },
  businessConfig: {
    type: Object,
    default: () => ({})
  },
  dialogTitle: {
    type: String,
    default: ''
  },
  formData: {
    type: Object,
    default: () => ({})
  },
  entryList: {
    type: Array,
    default: () => []
  },
  costData: {
    type: Object,
    default: () => ({})
  },
  submitLoading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:visible', 'submit', 'add-entry', 'delete-entry'])

// Form ref
const formRef = ref(null)
const formActiveTab = ref('entry')
const nationSearchLoading = ref(false)
const nationOptions = ref([])

// Watch visible
watch(() => props.visible, (newVal) => {
  if (!newVal) {
    formRef.value?.resetFields()
  }
})

// Validation rules
const formRules = computed(() => {
  const rules = {}
  props.formConfig?.sections?.forEach(section => {
    section.fields.forEach(field => {
      if (field.rules && field.rules.length > 0) {
        rules[field.field] = field.rules
      }
    })
  })
  return rules
})

// Dict options
const getDictOptions = (dictName, staticOptions = null, required = false) => {
  if (dictName === 'nation') {
    return nationOptions.value
  }
  
  if (staticOptions && Array.isArray(staticOptions)) {
    return staticOptions
  }
  
  const dataFromManager = dictionaryManager.getDictOptions(dictName)
  
  if (!dataFromManager || dataFromManager.length === 0) {
    if (required) {
      //  记录到日志而不是警告
      console.log(`[BusinessForm] ℹ️ Required dictionary '${dictName}' data is empty - this may affect form functionality`)
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

// Search country
const searchNations = async (keyword) => {
  if (!keyword || keyword.trim() === '') {
    nationOptions.value = []
    return
  }
  
  nationSearchLoading.value = true
  try {
    const dictConfig = props.businessConfig?.dictionaryConfig?.dictionaries?.nation
    
    if (dictConfig && dictConfig.type === 'remote') {
      const searchApi = dictConfig.config?.searchApi || '/erp/engine/country/search?keyword={keyword}&limit=20'
      const searchUrl = searchApi.replace('{keyword}', encodeURIComponent(keyword))
      
      const response = await request(searchUrl)
      
      let data = []
      if (response.code === 200 || response.errorCode === 0) {
        data = response.data || response.rows || []
      } else if (Array.isArray(response)) {
        data = response
      }
      
      nationOptions.value = data.map(item => ({
        label: item.labelZh || item.name || item.label,
        value: item.id || item.kingdee || item.value,
        labelEn: item.labelEn || item.name_en
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

// Event handlers
const handleClose = () => {
  emit('update:visible', false)
}

const handleCancel = () => {
  emit('update:visible', false)
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    
    const submitData = { ...props.formData }
    if (props.entryList && props.entryList.length > 0) {
      submitData.entryList = props.entryList
    }
    if (props.costData && Object.keys(props.costData).length > 0) {
      submitData.costData = props.costData
    }
    
    emit('submit', submitData)
  } catch (error) {
    console.error('[BusinessForm] Form validation failed:', error)
  }
}

const handleAddEntry = () => {
  emit('add-entry')
}

const handleDeleteEntry = (index) => {
  emit('delete-entry', index)
}
</script>

<style scoped>
/*  Use parent global styles */

/* Section card */
.form-section-card {
  margin-bottom: 16px;
  border: none;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

.form-section-card:last-child {
  margin-bottom: 0;
}

/* Card header */
.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.card-header .el-icon {
  color: #409EFF;
  font-size: 18px;
}

/* Form tabs card */
.form-tabs-card {
  margin-top: 16px;
}

.form-tabs-card :deep(.el-card__body) {
  padding: 16px;
}

.form-tabs-card :deep(.el-tabs__header) {
  margin: 0 0 16px 0;
}

/* Tab pane content */
.tab-pane-content {
  padding: 8px 0;
}

/* Tab pane toolbar */
.tab-pane-toolbar {
  margin-bottom: 12px;
  display: flex;
  justify-content: flex-end;
}

/* Entry table */
.entry-table {
  width: 100%;
}

.entry-table :deep(.el-table__header th) {
  background-color: #fafafa;
  font-weight: 600;
  font-size: 13px;
}

.entry-table :deep(.el-table__body td) {
  padding: 8px 0;
}

/* Dialog footer */
.dialog-footer {
  padding-top: 8px;
}
</style>
