/**
 * 可视化配置编辑器 - 三栏布局（导航 + 表单 + 预览）
 */
<script setup name="VisualConfigEditor">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, ElTag, ElIcon } from 'element-plus'
import { 
  Document, 
  Setting, 
  Grid, 
  Search, 
  Pointer, 
  Link, 
  Briefcase, 
  Tickets,
  InfoFilled,
  List,
  Connection
} from '@element-plus/icons-vue'
import DynamicForm from './DynamicForm.vue'
import { createConfigParser } from '../utils/ConfigParser'
import { configCategoryMetas } from '../metadata/configMetadata'
import { mergeConfigFields, splitConfigFields } from '../utils/configJsonUtils'

// ==================== Props & Emits ====================
const props = defineProps({
  // 配置 ID（编辑模式时必填）
  configId: {
    type: Number,
    default: null
  },
  // 初始配置数据
  initialData: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['save', 'cancel'])

// ==================== State ====================
const parser = createConfigParser()

// 当前选中的配置类别
const activeCategory = ref('PAGE')

// 当前选中的分组
const activeGroup = ref('basic')

// 左侧面板折叠状态
const leftPanelCollapsed = ref(false)

// 右侧预览面板默认展开
const rightPanelCollapsed = ref(false)

// 所有配置数据（9 个 JSON 字段）
const allConfigData = reactive({
  pageConfig: {},
  formConfig: {},
  tableConfig: {},
  searchConfig: {},
  actionConfig: {},
  apiConfig: {},
  dictConfig: {},
  businessConfig: {},
  detailConfig: {}
})

// 当前类别的表单数据
const currentFormData = ref({})

// 当前类别的验证规则
const currentRules = ref({})

// 当前类别的字段元数据
const currentFieldMetas = ref([])

// 当前类别的分组
const currentGroups = ref([])

// 字典数据
const dictionaryData = ref({})

// 表单引用
const formRef = ref(null)

// ==================== Computed Properties ====================

/**
 * 配置类别选项
 */
const categoryOptions = computed(() => {
  return Object.values(configCategoryMetas).map(meta => ({
    value: meta.category,
    label: meta.title,
    icon: meta.groups[0]?.icon || 'Document',
    description: meta.description
  }))
})

/**
 * 当前类别的元数据
 */
const currentCategoryMeta = computed(() => {
  return configCategoryMetas[activeCategory.value]
})

/**
 * 当前分组的元数据
 */
const currentGroupMeta = computed(() => {
  if (!currentCategoryMeta.value) return null
  
  const group = currentCategoryMeta.value.groups.find(g => g.name === activeGroup.value)
  return group || currentCategoryMeta.value.groups[0]
})

/**
 * 分组列表
 */
const groupList = computed(() => {
  if (!currentCategoryMeta.value) return []
  return currentCategoryMeta.value.groups
})

// ==================== Methods ====================

/**
 * 加载配置数据
 */
async function loadConfigData() {
  try {
    if (props.configId && props.initialData) {
      // 编辑模式：合并所有配置字段
      const merged = mergeConfigFields(props.initialData)
      
      console.log('加载配置数据 - 合并后的数据:', merged)
      
      // 解析各个配置块，增加错误处理
      try {
        allConfigData.pageConfig = merged.pageConfig ? (typeof merged.pageConfig === 'string' ? JSON.parse(merged.pageConfig) : merged.pageConfig) : {}
      } catch (e) {
        console.warn('解析 pageConfig 失败:', e)
        allConfigData.pageConfig = {}
      }
      
      try {
        allConfigData.formConfig = merged.formConfig ? (typeof merged.formConfig === 'string' ? JSON.parse(merged.formConfig) : merged.formConfig) : {}
      } catch (e) {
        console.warn('解析 formConfig 失败:', e)
        allConfigData.formConfig = {}
      }
      
      try {
        allConfigData.tableConfig = merged.tableConfig ? (typeof merged.tableConfig === 'string' ? JSON.parse(merged.tableConfig) : merged.tableConfig) : {}
      } catch (e) {
        console.warn('解析 tableConfig 失败:', e)
        allConfigData.tableConfig = {}
      }
      
      try {
        allConfigData.searchConfig = merged.searchConfig ? (typeof merged.searchConfig === 'string' ? JSON.parse(merged.searchConfig) : merged.searchConfig) : {}
      } catch (e) {
        console.warn('解析 searchConfig 失败:', e)
        allConfigData.searchConfig = {}
      }
      
      try {
        allConfigData.actionConfig = merged.actionConfig ? (typeof merged.actionConfig === 'string' ? JSON.parse(merged.actionConfig) : merged.actionConfig) : {}
      } catch (e) {
        console.warn('解析 actionConfig 失败:', e)
        allConfigData.actionConfig = {}
      }
      
      try {
        allConfigData.apiConfig = merged.apiConfig ? (typeof merged.apiConfig === 'string' ? JSON.parse(merged.apiConfig) : merged.apiConfig) : {}
      } catch (e) {
        console.warn('解析 apiConfig 失败:', e)
        allConfigData.apiConfig = {}
      }
      
      try {
        allConfigData.dictConfig = merged.dictConfig ? (typeof merged.dictConfig === 'string' ? JSON.parse(merged.dictConfig) : merged.dictConfig) : {}
      } catch (e) {
        console.warn('解析 dictConfig 失败:', e)
        allConfigData.dictConfig = {}
      }
      
      try {
        allConfigData.businessConfig = merged.businessConfig ? (typeof merged.businessConfig === 'string' ? JSON.parse(merged.businessConfig) : merged.businessConfig) : {}
      } catch (e) {
        console.warn('解析 businessConfig 失败:', e)
        allConfigData.businessConfig = {}
      }
      
      try {
        allConfigData.detailConfig = merged.detailConfig ? (typeof merged.detailConfig === 'string' ? JSON.parse(merged.detailConfig) : merged.detailConfig) : {}
      } catch (e) {
        console.warn('解析 detailConfig 失败:', e)
        allConfigData.detailConfig = {}
      }
      
      console.log('加载配置数据 - 解析后的 pageConfig:', allConfigData.pageConfig)
      
      // 初始化当前类别
      switchToCategory(activeCategory.value)
    } else {
      // 新增模式：使用默认值
      switchToCategory('PAGE')
    }
    
    ElMessage.success('配置加载成功')
  } catch (error) {
    console.error('加载配置失败:', error)
    ElMessage.error('加载配置失败：' + (error.message || '未知错误'))
  }
}

/**
 * 切换到指定配置类别
 */
function switchToCategory(category) {
  activeCategory.value = category
  const meta = configCategoryMetas[category]
  
  if (!meta) {
    console.warn(`未知的配置类别：${category}`)
    ElMessage.warning(`未知的配置类别：${category}`)
    return
  }
  
  // 设置第一个分组为激活状态
  if (meta.groups && meta.groups.length > 0) {
    activeGroup.value = meta.groups[0].name
  }
  
  // 获取对应的配置数据
  let configJsonData = '{}'
  switch (category) {
    case 'PAGE':
      configJsonData = JSON.stringify(allConfigData.pageConfig)
      break
    case 'FORM':
      configJsonData = JSON.stringify(allConfigData.formConfig)
      break
    case 'TABLE':
      configJsonData = JSON.stringify(allConfigData.tableConfig)
      break
    case 'SEARCH':
      configJsonData = JSON.stringify(allConfigData.searchConfig)
      break
    case 'ACTION':
      configJsonData = JSON.stringify(allConfigData.actionConfig)
      break
    case 'API':
      configJsonData = JSON.stringify(allConfigData.apiConfig)
      break
    case 'DICT':
      configJsonData = JSON.stringify(allConfigData.dictConfig)
      break
    case 'BUSINESS':
      configJsonData = JSON.stringify(allConfigData.businessConfig)
      break
    case 'DETAIL':
      configJsonData = JSON.stringify(allConfigData.detailConfig)
      break
    default:
      console.warn(`未处理的配置类别：${category}`)
      configJsonData = '{}'
  }
  
  // 解析配置
  try {
    const result = parser.parseToJsonData(configJsonData, category)
    
    currentFormData.value = result.formData
    currentRules.value = result.validationRules
    currentFieldMetas.value = result.fieldMetas
    currentGroups.value = result.groups
    
    // 检查解析错误
    if (result.errors && result.errors.length > 0) {
      console.warn(`${category} 解析警告:`, result.errors)
    }
  } catch (error) {
    console.error(`解析${category}配置失败:`, error)
    ElMessage.error(`解析${category}配置失败：${error.message}`)
    // 使用空配置继续
    currentFormData.value = {}
    currentRules.value = {}
    currentFieldMetas.value = []
    currentGroups.value = []
  }
  
  // 清空错误
  parser.clearErrors()
}

/**
 * 切换分组
 */
function switchGroup(groupName) {
  activeGroup.value = groupName
}

/**
 * 表单数据变化
 */
function handleFormChange(field, value) {
  currentFormData.value[field] = value
}

/**
 * 保存配置
 */
async function handleSave() {
  try {
    // 先保存当前类别的配置
    saveCurrentCategory()
    
    // 验证所有配置
    const validationErrors = validateAllCategories()
    if (validationErrors.length > 0) {
      const errorMsg = validationErrors.slice(0, 3).join('\n')
      ElMessageBox.alert(
        `发现 ${validationErrors.length} 个验证错误：\n\n${errorMsg}`,
        '验证失败',
        {
          type: 'warning',
          confirmButtonText: '确定'
        }
      )
      return
    }
    
    // 合并所有配置
    const splitData = splitConfigFields(JSON.stringify(allConfigData))
    
    emit('save', splitData)
    ElMessage.success('保存成功')
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error('保存失败：' + (error.message || '未知错误'))
  }
}

/**
 * 保存当前类别的配置
 */
function saveCurrentCategory() {
  const jsonData = parser.generateFromFormData(currentFormData.value, activeCategory.value)
  const parsedData = JSON.parse(jsonData)
  
  switch (activeCategory.value) {
    case 'PAGE':
      Object.assign(allConfigData.pageConfig, parsedData)
      break
    case 'FORM':
      Object.assign(allConfigData.formConfig, parsedData)
      break
    case 'TABLE':
      Object.assign(allConfigData.tableConfig, parsedData)
      break
    case 'SEARCH':
      Object.assign(allConfigData.searchConfig, parsedData)
      break
    case 'ACTION':
      Object.assign(allConfigData.actionConfig, parsedData)
      break
    case 'API':
      Object.assign(allConfigData.apiConfig, parsedData)
      break
    case 'DICT':
      Object.assign(allConfigData.dictConfig, parsedData)
      break
    case 'BUSINESS':
      Object.assign(allConfigData.businessConfig, parsedData)
      break
    case 'DETAIL':
      Object.assign(allConfigData.detailConfig, parsedData)
      break
  }
}

/**
 * 验证所有类别
 */
function validateAllCategories() {
  const errors = []
  
  Object.keys(configCategoryMetas).forEach(category => {
    // 临时切换到该类别进行验证
    const currentBackup = { ...currentFormData.value }
    switchToCategory(category)
    
    const validationErrors = parser.validateFormData(currentFormData.value)
    if (validationErrors.length > 0) {
      errors.push(
        `${configCategoryMetas[category].title}: ${validationErrors.map(e => e.message).join(', ')}`
      )
    }
    
    // 恢复原来的类别
    currentFormData.value = currentBackup
  })
  
  // 切换回原来的类别
  switchToCategory(activeCategory.value)
  
  return errors
}

/**
 * 取消
 */
function handleCancel() {
  emit('cancel')
}

/**
 * 重置当前类别
 */
function handleReset() {
  ElMessageBox.confirm('确定要重置当前类别的配置吗？', '提示', {
    type: 'warning'
  }).then(() => {
    switchToCategory(activeCategory.value)
    ElMessage.success('重置成功')
  })
}

/**
 * 导出当前类别 JSON
 */
function handleExportJson() {
  saveCurrentCategory()
  
  let jsonData = '{}'
  switch (activeCategory.value) {
    case 'PAGE':
      jsonData = JSON.stringify(allConfigData.pageConfig, null, 2)
      break
    case 'FORM':
      jsonData = JSON.stringify(allConfigData.formConfig, null, 2)
      break
    case 'TABLE':
      jsonData = JSON.stringify(allConfigData.tableConfig, null, 2)
      break
    case 'SEARCH':
      jsonData = JSON.stringify(allConfigData.searchConfig, null, 2)
      break
    case 'ACTION':
      jsonData = JSON.stringify(allConfigData.actionConfig, null, 2)
      break
    case 'API':
      jsonData = JSON.stringify(allConfigData.apiConfig, null, 2)
      break
    case 'DICT':
      jsonData = JSON.stringify(allConfigData.dictConfig, null, 2)
      break
    case 'BUSINESS':
      jsonData = JSON.stringify(allConfigData.businessConfig, null, 2)
      break
    case 'DETAIL':
      jsonData = JSON.stringify(allConfigData.detailConfig, null, 2)
      break
  }
  
  // 复制到剪贴板
  navigator.clipboard.writeText(jsonData)
  ElMessage.success('JSON 已复制到剪贴板')
}

// ==================== Lifecycle ====================
onMounted(() => {
  loadConfigData()
})
</script>

<template>
  <div class="visual-config-editor">
    <!-- 左侧：配置导航 -->
    <div class="left-panel" :class="{ collapsed: leftPanelCollapsed }">
      <div class="panel-header">
        <span class="panel-title">配置类别</span>
        <el-button link icon="Fold" @click="leftPanelCollapsed = !leftPanelCollapsed" />
      </div>
      
      <el-menu
        v-show="!leftPanelCollapsed"
        :default-active="activeCategory"
        class="category-menu"
        @select="switchToCategory"
      >
        <el-menu-item
          v-for="cat in categoryOptions"
          :key="cat.value"
          :index="cat.value"
        >
          <el-icon><component :is="cat.icon" /></el-icon>
          <template #title>
            <div class="category-item">
              <span>{{ cat.label }}</span>
              <el-tag size="small" type="info">{{ cat.value }}</el-tag>
            </div>
          </template>
        </el-menu-item>
      </el-menu>
    </div>

    <!-- 中间：配置表单 -->
    <div class="center-panel">
      <div class="panel-header">
        <div class="header-left">
          <el-icon><Setting /></el-icon>
          <span class="panel-title">{{ currentCategoryMeta?.title }}</span>
          <el-tag v-if="currentCategoryMeta" type="success" size="small">
            {{ currentCategoryMeta.description }}
          </el-tag>
        </div>
        
        <div class="header-right">
          <el-button icon="Refresh" size="small" @click="handleReset">重置</el-button>
          <el-button icon="Document" size="small" @click="handleExportJson">导出 JSON</el-button>
        </div>
      </div>
      
      <!-- 分组标签页 -->
      <el-tabs v-model="activeGroup" class="group-tabs" @tab-change="switchGroup">
        <el-tab-pane
          v-for="group in groupList"
          :key="group.name"
          :label="group.label"
          :name="group.name"
        >
          <el-card shadow="never" class="form-card">
            <!-- 动态表单 -->
            <el-form
              ref="formRef"
              :model="currentFormData"
              :rules="currentRules"
              label-width="120px"
              size="default"
            >
              <el-row :gutter="20">
                <DynamicForm
                  v-model="currentFormData"
                  :field-metas="currentGroupMeta?.fields || []"
                  :dictionary-data="dictionaryData"
                />
              </el-row>
            </el-form>
          </el-card>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 右侧：实时预览 -->
    <div class="right-panel" :class="{ collapsed: rightPanelCollapsed }">
      <div class="panel-header">
        <span class="panel-title">实时预览</span>
        <el-button link icon="Fold" @click="rightPanelCollapsed = !rightPanelCollapsed" />
      </div>
      
      <div v-show="!rightPanelCollapsed" class="preview-content">
        <el-card shadow="never" class="preview-card">
          <template #header>
            <div class="card-header">
              <span>JSON 预览</span>
              <el-tag size="small" type="warning">{{ activeCategory }}</el-tag>
            </div>
          </template>
          
          <pre class="json-preview">{{ JSON.stringify(currentFormData, null, 2) }}</pre>
        </el-card>
        
        <el-card shadow="never" class="preview-card">
          <template #header>
            <span>配置统计</span>
          </template>
          
          <el-descriptions :column="1" size="small">
            <el-descriptions-item label="字段数">
              {{ currentFieldMetas.length }}
            </el-descriptions-item>
            <el-descriptions-item label="分组数">
              {{ currentGroups.length }}
            </el-descriptions-item>
            <el-descriptions-item label="验证规则数">
              {{ Object.keys(currentRules).length }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </div>
    </div>

    <!-- 底部操作栏 -->
    <div class="footer-actions">
      <el-button type="primary" icon="Check" @click="handleSave">保存配置</el-button>
      <el-button icon="Close" @click="handleCancel">取消</el-button>
    </div>
  </div>
</template>

<style scoped lang="scss">
.visual-config-editor {
  display: flex;
  flex-direction: row;
  height: calc(100vh - 200px);
  background: #f5f7fa;
  overflow: hidden;
  
  .left-panel {
    width: 260px;
    background: #fff;
    border-right: 1px solid #e4e7ed;
    transition: width 0.3s;
    flex-shrink: 0;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    
    &.collapsed {
      width: 60px;
      
      .panel-header {
        justify-content: center;
        padding: 16px 8px;
        
        .panel-title {
          display: none;
        }
        
        .el-button {
          padding: 8px;
        }
      }
      
      .category-menu {
        display: none;
      }
    }
    
    .panel-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px;
      border-bottom: 1px solid #e4e7ed;
      flex-shrink: 0;
      
      .panel-title {
        font-size: 16px;
        font-weight: 500;
        color: #303133;
        white-space: nowrap;
        overflow: hidden;
      }
    }
    
    .category-menu {
      border-right: none;
      flex: 1;
      overflow-y: auto;
      overflow-x: hidden;
      
      .category-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        width: 100%;
        
        span {
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }
      }
    }
  }
  
  .center-panel {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    min-width: 0;
    
    .panel-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px;
      background: #fff;
      border-bottom: 1px solid #e4e7ed;
      flex-shrink: 0;
      
      .header-left {
        display: flex;
        align-items: center;
        gap: 12px;
        
        .panel-title {
          font-size: 18px;
          font-weight: 500;
          color: #303133;
        }
      }
    }
    
    .group-tabs {
      flex: 1;
      overflow-y: auto;
      overflow-x: hidden;
      padding: 16px;
      
      .form-card {
        margin-top: 16px;
      }
    }
  }
  
  .right-panel {
    width: 320px;
    background: #fff;
    border-left: 1px solid #e4e7ed;
    transition: width 0.3s;
    flex-shrink: 0;
    display: flex;
    flex-direction: column;
    overflow: hidden;
    
    &.collapsed {
      width: 60px;
      
      .panel-header {
        justify-content: center;
        
        .panel-title {
          display: none;
        }
      }
      
      .preview-content {
        display: none;
      }
    }
    
    .panel-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px;
      border-bottom: 1px solid #e4e7ed;
      flex-shrink: 0;
      
      .panel-title {
        font-size: 16px;
        font-weight: 500;
        color: #303133;
      }
    }
    
    .preview-content {
      flex: 1;
      overflow-y: auto;
      overflow-x: hidden;
      padding: 16px;
      
      .preview-card {
        margin-bottom: 16px;
        
        .card-header {
          display: flex;
          justify-content: space-between;
          align-items: center;
        }
        
        .json-preview {
          background: #f5f7fa;
          padding: 12px;
          border-radius: 4px;
          font-family: 'Courier New', monospace;
          font-size: 12px;
          color: #606266;
          max-height: 400px;
          overflow-y: auto;
          overflow-x: hidden;
          white-space: pre-wrap;
          word-wrap: break-word;
          line-height: 1.5;
        }
      }
    }
  }
  
  .footer-actions {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    padding: 16px;
    background: #fff;
    border-top: 1px solid #e4e7ed;
  }
}

.form-item-help-text {
  color: #909399;
  font-size: 12px;
  line-height: 1.5;
}
</style>
