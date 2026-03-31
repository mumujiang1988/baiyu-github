<template>
  <el-drawer
    :model-value="visible"
    :title="drawerTitle"
    direction="rtl"
    size="60%"
    :before-close="handleBeforeClose"
    :close-on-click-modal="true"
    :modal="true"
  >
    <div v-if="loading" class="drawer-loading">
      <el-icon class="is-loading" :size="40"><Loading /></el-icon>
      <p>Loading data...</p>
    </div>
    
    <!-- Drawer content -->
    <div class="drawer-content">
      <el-tabs v-model="detailActiveTab" stretch>
        <el-tab-pane
          v-for="tab in tabs"
          :key="tab.name"
          :label="tab.label"
          :name="tab.name"
        >
          <!-- Table tab -->
          <div v-if="tab.type === 'table' || !tab.type" class="tab-content">
            <template v-for="tabKey in [tab.name]" :key="tabKey">
              <div v-if="!getTabData(tab) || getTabData(tab).length === 0" class="tab-empty">
                <el-empty :description="`No ${tab.label} data`" :image-size="120" />
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
          
          <!-- Form tab -->
          <div v-else-if="tab.type === 'form'" class="tab-content">
            <template v-for="tabKey in [tab.name]" :key="tabKey">
              <div v-if="!getTabData(tab) || Object.keys(getTabData(tab)).length === 0" class="tab-empty">
                <el-empty :description="`No ${tab.label} data`" :image-size="120" />
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
          
          <!-- Descriptions tab -->
          <div v-else-if="tab.type === 'descriptions'" class="tab-content">
            <template v-for="tabKey in [tab.name]" :key="tabKey">
              <div v-if="!getTabData(tab) || (Array.isArray(getTabData(tab)) ? getTabData(tab).length === 0 : Object.keys(getTabData(tab)).length === 0)" class="tab-empty">
                <el-empty :description="`No ${tab.label} data`" :image-size="120" />
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
</template>

<script setup name="BusinessDetail">
import { ref, computed, watch } from 'vue'
import { Loading } from '@element-plus/icons-vue'
import { formatAmount } from '@/views/erp/utils/index.js'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  drawerConfig: {
    type: Object,
    required: true
  },
  drawerTitle: {
    type: String,
    default: '详情'
  },
  detailRow: {
    type: Object,
    default: () => ({})
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:visible', 'close'])

const detailActiveTab = ref('entry')

// Watch visible
watch(() => props.visible, (newVal) => {
  if (newVal) {
    detailActiveTab.value = 'entry'
  }
})

// Computed tabs
const tabs = computed(() => {
  return props.drawerConfig?.tabs || []
})

// Get tab data
const getTabData = (tab) => {
  if (!tab || !tab.dataField) {
    return null
  }
  
  const data = props.detailRow[tab.dataField]
  
  // Return array
  if (Array.isArray(data)) {
    return data
  }
  
  // Return object
  if (data && typeof data === 'object') {
    return data
  }
  
  // Return empty array
  return []
}

// Get form fields
const getFormFields = (tab) => {
  if (tab.form && Array.isArray(tab.form.fields)) {
    return tab.form.fields
  }
  
  if (Array.isArray(tab.fields)) {
    return tab.fields
  }
  
  return []
}

// Get field value (case-insensitive)
const getFieldValue = (row, fieldName) => {
  if (!row || !fieldName) return undefined
  
  // Exact match
  if (row.hasOwnProperty(fieldName)) {
    return row[fieldName]
  }
  
  // Lowercase match
  const lowerFieldName = fieldName.toLowerCase()
  if (row.hasOwnProperty(lowerFieldName)) {
    return row[lowerFieldName]
  }
  
  // Case-insensitive match
  const rowKeys = Object.keys(row)
  const matchedKey = rowKeys.find(key => key.toLowerCase() === fieldName.toLowerCase())
  if (matchedKey) {
    return row[matchedKey]
  }
  
  return undefined
}

// Event handlers
const handleBeforeClose = (done) => {
  emit('close')
  done()
}
</script>

<style scoped>
/* ✅ Use parent global styles */

/* Drawer loading */
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

/* Drawer content */
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

/* Tab empty state */
.tab-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  padding: 40px 20px;
}

/* Tab content */
.tab-content {
  padding: 0;
}
</style>
