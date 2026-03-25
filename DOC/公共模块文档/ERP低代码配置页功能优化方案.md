# 🎨 ERP低代码配置页功能优化方案

> **文档日期**: 2026-03-24  
> **优化范围**: ERP配置管理页面(index.vue、editor.vue、history.vue)  
> **优化目标**: 提升用户体验、增强功能完整性、优化代码质量

---

## 📊 一、现状分析

### 1.1 现有功能清单

| 页面 | 文件 | 主要功能 | 代码行数 |
|------|------|---------|---------|
| **配置列表页** | index.vue | 配置查询、查看、编辑、删除、历史版本 | 440行 |
| **配置编辑页** | editor.vue | 配置新增/编辑、JSON编辑、模板加载 | 496行 |
| **历史版本页** | history.vue | 历史版本查看、对比、回滚 | 402行 |

**总计**: 1,338行代码

### 1.2 功能完整度评估

```
核心功能完整度:
├─ 配置管理: 90% ✅
│  ├─ 查询列表 ✅
│  ├─ 新增配置 ✅
│  ├─ 编辑配置 ✅
│  ├─ 删除配置 ✅
│  └─ 查看详情 ✅
│
├─ JSON编辑: 70% ⚠️
│  ├─ JSON格式化 ✅
│  ├─ JSON验证 ✅
│  ├─ 语法高亮 ✅
│  ├─ 自动补全 ❌
│  └─ 错误提示 ⚠️
│
├─ 模板管理: 40% ❌
│  ├─ 模板列表 ⚠️
│  ├─ 模板加载 ✅
│  ├─ 模板保存 ❌
│  └─ 模板管理 ❌
│
├─ 历史版本: 80% ✅
│  ├─ 版本列表 ✅
│  ├─ 版本查看 ✅
│  ├─ 版本对比 ✅
│  └─ 版本回滚 ✅
│
└─ 辅助功能: 30% ❌
   ├─ 配置复制 ❌
   ├─ 配置导出 ❌
   ├─ 配置导入 ❌
   └─ 批量操作 ❌
```

### 1.3 用户体验问题

| 问题类型 | 问题描述 | 影响程度 |
|---------|---------|---------|
| **功能缺失** | 复制、导出、导入功能未实现 | 🔴 高 |
| **交互体验** | JSON编辑器缺少智能提示 | 🟡 中 |
| **可视化** | 缺少配置可视化预览 | 🔴 高 |
| **模板管理** | 模板功能不完善 | 🟡 中 |
| **批量操作** | 不支持批量删除、导出 | 🟡 中 |
| **实时验证** | JSON验证不够实时 | 🟢 低 |

---

## 🎯 二、功能优化方案

### 2.1 JSON编辑器增强

#### 2.1.1 智能提示与自动补全

**优化目标**: 提供JSON Schema智能提示,提升配置编写效率

**技术方案**:

```vue
<template>
  <div class="json-editor-enhanced">
    <codemirror
      v-model="content"
      :extensions="extensions"
      :style="{ height: '700px' }"
      @change="handleChange"
    />
    
    <!-- 智能提示面板 -->
    <div v-if="showSuggestion" class="suggestion-panel">
      <div v-for="suggestion in suggestions" :key="suggestion.key" 
           class="suggestion-item" @click="applySuggestion(suggestion)">
        <span class="suggestion-key">{{ suggestion.key }}</span>
        <span class="suggestion-desc">{{ suggestion.description }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { json } from '@codemirror/lang-json'
import { autocompletion } from '@codemirror/autocomplete'
import { linter } from '@codemirror/lint'
import { jsonSchemaLinter } from 'codemirror-json-schema'

// JSON Schema定义
const jsonSchema = {
  type: 'object',
  properties: {
    pageConfig: {
      type: 'object',
      description: '页面配置',
      properties: {
        title: { type: 'string', description: '页面标题' },
        permissionPrefix: { type: 'string', description: '权限前缀' },
        primaryKey: { type: 'string', description: '主键字段' }
      }
    },
    apiConfig: {
      type: 'object',
      description: 'API配置',
      properties: {
        methods: {
          type: 'object',
          properties: {
            list: { type: 'string', description: '列表查询接口' },
            get: { type: 'string', description: '详情查询接口' },
            add: { type: 'string', description: '新增接口' },
            edit: { type: 'string', description: '编辑接口' },
            delete: { type: 'string', description: '删除接口' }
          }
        }
      }
    },
    tableConfig: {
      type: 'object',
      description: '表格配置',
      properties: {
        columns: {
          type: 'array',
          items: {
            type: 'object',
            properties: {
              prop: { type: 'string', description: '字段名' },
              label: { type: 'string', description: '显示标签' },
              width: { type: 'number', description: '列宽度' },
              renderType: { 
                type: 'string', 
                enum: ['text', 'tag', 'date', 'currency'],
                description: '渲染类型'
              }
            }
          }
        }
      }
    }
  }
}

// CodeMirror扩展配置
const extensions = [
  json(),
  autocompletion({
    override: [jsonCompletionSource(jsonSchema)]
  }),
  linter(jsonSchemaLinter(jsonSchema)),
  jsonSchema(jsonSchema)
]

// 智能提示数据
const suggestions = ref([
  { key: 'pageConfig', description: '页面基础配置', template: { title: '', permissionPrefix: '', primaryKey: 'id' } },
  { key: 'apiConfig', description: 'API接口配置', template: { methods: {} } },
  { key: 'tableConfig', description: '表格配置', template: { columns: [] } },
  { key: 'formConfig', description: '表单配置', template: { sections: [] } },
  { key: 'searchConfig', description: '搜索配置', template: { fields: [] } }
])

// 应用建议
function applySuggestion(suggestion) {
  const currentJson = JSON.parse(content.value || '{}')
  currentJson[suggestion.key] = suggestion.template
  content.value = JSON.stringify(currentJson, null, 2)
  showSuggestion.value = false
}
</script>
```

#### 2.1.2 实时验证与错误提示

**优化方案**:

```vue
<script setup>
import { debounce } from 'lodash'
import { jsonParseLinter } from '@codemirror/lang-json'

// 实时验证
const validateJsonRealtime = debounce((value) => {
  try {
    const parsed = JSON.parse(value)
    
    // Schema验证
    const schemaErrors = validateAgainstSchema(parsed, jsonSchema)
    if (schemaErrors.length > 0) {
      jsonErrors.value = schemaErrors
      return
    }
    
    // 业务规则验证
    const businessErrors = validateBusinessRules(parsed)
    if (businessErrors.length > 0) {
      jsonErrors.value = businessErrors
      return
    }
    
    jsonErrors.value = []
    jsonValid.value = true
    
  } catch (e) {
    jsonErrors.value = [{
      line: extractErrorLine(e.message),
      message: e.message,
      type: 'syntax'
    }]
    jsonValid.value = false
  }
}, 300)

// 业务规则验证
function validateBusinessRules(config) {
  const errors = []
  
  // 验证API配置
  if (config.apiConfig?.methods) {
    const { list, get, add, edit, delete: del } = config.apiConfig.methods
    if (!list) errors.push({ message: '列表查询接口(list)不能为空', type: 'business' })
    if (!get) errors.push({ message: '详情查询接口(get)不能为空', type: 'business' })
  }
  
  // 验证表格配置
  if (config.tableConfig?.columns) {
    if (config.tableConfig.columns.length === 0) {
      errors.push({ message: '表格至少需要一列', type: 'business' })
    }
    
    config.tableConfig.columns.forEach((col, index) => {
      if (!col.prop) errors.push({ message: `第${index+1}列缺少prop属性`, type: 'business' })
      if (!col.label) errors.push({ message: `第${index+1}列缺少label属性`, type: 'business' })
    })
  }
  
  return errors
}
</script>
```

### 2.2 可视化配置编辑器

#### 2.2.1 表格配置可视化编辑

**功能设计**:

```vue
<template>
  <div class="visual-table-config">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>表格列配置</span>
          <el-button type="primary" size="small" @click="addColumn">
            添加列
          </el-button>
        </div>
      </template>
      
      <!-- 列配置列表 -->
      <el-table :data="columns" border>
        <el-table-column prop="prop" label="字段名" width="150">
          <template #default="{ row, $index }">
            <el-input v-model="row.prop" placeholder="字段名" @change="updateConfig" />
          </template>
        </el-table-column>
        
        <el-table-column prop="label" label="显示标签" width="150">
          <template #default="{ row }">
            <el-input v-model="row.label" placeholder="显示标签" @change="updateConfig" />
          </template>
        </el-table-column>
        
        <el-table-column prop="width" label="列宽度" width="100">
          <template #default="{ row }">
            <el-input-number v-model="row.width" :min="50" :max="500" @change="updateConfig" />
          </template>
        </el-table-column>
        
        <el-table-column prop="renderType" label="渲染类型" width="120">
          <template #default="{ row }">
            <el-select v-model="row.renderType" @change="updateConfig">
              <el-option label="文本" value="text" />
              <el-option label="标签" value="tag" />
              <el-option label="日期" value="date" />
              <el-option label="金额" value="currency" />
              <el-option label="链接" value="link" />
            </el-select>
          </template>
        </el-table-column>
        
        <el-table-column prop="align" label="对齐方式" width="100">
          <template #default="{ row }">
            <el-select v-model="row.align" @change="updateConfig">
              <el-option label="左对齐" value="left" />
              <el-option label="居中" value="center" />
              <el-option label="右对齐" value="right" />
            </el-select>
          </template>
        </el-table-column>
        
        <el-table-column prop="visible" label="显示" width="80">
          <template #default="{ row }">
            <el-switch v-model="row.visible" @change="updateConfig" />
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ $index }">
            <el-button link type="primary" @click="moveUp($index)" :disabled="$index === 0">
              上移
            </el-button>
            <el-button link type="danger" @click="removeColumn($index)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  modelValue: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['update:modelValue'])

const columns = ref([])

// 初始化列配置
watch(() => props.modelValue, (newVal) => {
  if (newVal?.tableConfig?.columns) {
    columns.value = newVal.tableConfig.columns
  }
}, { immediate: true, deep: true })

// 添加列
function addColumn() {
  columns.value.push({
    prop: '',
    label: '',
    width: 120,
    renderType: 'text',
    align: 'left',
    visible: true,
    sortable: false
  })
  updateConfig()
}

// 删除列
function removeColumn(index) {
  columns.value.splice(index, 1)
  updateConfig()
}

// 上移列
function moveUp(index) {
  const temp = columns.value[index]
  columns.value[index] = columns.value[index - 1]
  columns.value[index - 1] = temp
  updateConfig()
}

// 更新配置
function updateConfig() {
  const config = { ...props.modelValue }
  config.tableConfig = config.tableConfig || {}
  config.tableConfig.columns = columns.value
  emit('update:modelValue', config)
}
</script>
```

#### 2.2.2 表单配置可视化编辑

**功能设计**:

```vue
<template>
  <div class="visual-form-config">
    <el-tabs v-model="activeSection">
      <el-tab-pane 
        v-for="(section, sIndex) in sections" 
        :key="sIndex"
        :label="section.title"
        :name="String(sIndex)"
      >
        <div class="section-config">
          <!-- 分区配置 -->
          <el-form :model="section" label-width="100px">
            <el-form-item label="分区标题">
              <el-input v-model="section.title" @change="updateConfig" />
            </el-form-item>
            
            <el-form-item label="列数">
              <el-input-number v-model="section.columns" :min="1" :max="4" @change="updateConfig" />
            </el-form-item>
          </el-form>
          
          <!-- 字段列表 -->
          <div class="fields-container">
            <div class="fields-header">
              <span>字段列表</span>
              <el-button type="primary" size="small" @click="addField(sIndex)">
                添加字段
              </el-button>
            </div>
            
            <el-table :data="section.fields" border size="small">
              <el-table-column prop="field" label="字段名" width="120">
                <template #default="{ row }">
                  <el-input v-model="row.field" size="small" @change="updateConfig" />
                </template>
              </el-table-column>
              
              <el-table-column prop="label" label="标签" width="120">
                <template #default="{ row }">
                  <el-input v-model="row.label" size="small" @change="updateConfig" />
                </template>
              </el-table-column>
              
              <el-table-column prop="type" label="类型" width="120">
                <template #default="{ row }">
                  <el-select v-model="row.type" size="small" @change="updateConfig">
                    <el-option label="输入框" value="input" />
                    <el-option label="选择框" value="select" />
                    <el-option label="日期" value="date" />
                    <el-option label="数字" value="number" />
                    <el-option label="文本域" value="textarea" />
                  </el-select>
                </template>
              </el-table-column>
              
              <el-table-column prop="required" label="必填" width="60">
                <template #default="{ row }">
                  <el-switch v-model="row.required" @change="updateConfig" />
                </template>
              </el-table-column>
              
              <el-table-column label="操作" width="100">
                <template #default="{ $index }">
                  <el-button link type="danger" size="small" @click="removeField(sIndex, $index)">
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </div>
      </el-tab-pane>
      
      <!-- 添加分区按钮 -->
      <el-tab-pane name="add">
        <template #label>
          <el-button type="text" @click="addSection">
            <el-icon><Plus /></el-icon>
            添加分区
          </el-button>
        </template>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  modelValue: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['update:modelValue'])

const sections = ref([])
const activeSection = ref('0')

// 初始化
watch(() => props.modelValue, (newVal) => {
  if (newVal?.formConfig?.sections) {
    sections.value = newVal.formConfig.sections
  }
}, { immediate: true, deep: true })

// 添加分区
function addSection() {
  sections.value.push({
    title: `分区${sections.value.length + 1}`,
    columns: 2,
    fields: []
  })
  activeSection.value = String(sections.value.length - 1)
  updateConfig()
}

// 添加字段
function addField(sectionIndex) {
  sections.value[sectionIndex].fields.push({
    field: '',
    label: '',
    type: 'input',
    required: false,
    span: 12
  })
  updateConfig()
}

// 删除字段
function removeField(sectionIndex, fieldIndex) {
  sections.value[sectionIndex].fields.splice(fieldIndex, 1)
  updateConfig()
}

// 更新配置
function updateConfig() {
  const config = { ...props.modelValue }
  config.formConfig = config.formConfig || {}
  config.formConfig.sections = sections.value
  emit('update:modelValue', config)
}
</script>
```

### 2.3 模板管理增强

#### 2.3.1 模板库管理

**功能设计**:

```vue
<template>
  <div class="template-manager">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>配置模板库</span>
          <div class="actions">
            <el-button type="primary" @click="createTemplate">
              新建模板
            </el-button>
            <el-button @click="importTemplate">
              导入模板
            </el-button>
          </div>
        </div>
      </template>
      
      <!-- 模板分类 -->
      <el-tabs v-model="activeCategory">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="页面配置" name="PAGE" />
        <el-tab-pane label="字典配置" name="DICT" />
        <el-tab-pane label="推送配置" name="PUSH" />
      </el-tabs>
      
      <!-- 模板列表 -->
      <el-table :data="filteredTemplates" border>
        <el-table-column prop="templateName" label="模板名称" min-width="200" />
        <el-table-column prop="templateType" label="类型" width="120">
          <template #default="{ row }">
            <el-tag>{{ row.templateType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="说明" min-width="200" />
        <el-table-column prop="useCount" label="使用次数" width="100" align="center" />
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="previewTemplate(row)">
              预览
            </el-button>
            <el-button link type="primary" @click="useTemplate(row)">
              使用
            </el-button>
            <el-button link type="primary" @click="editTemplate(row)">
              编辑
            </el-button>
            <el-button link type="danger" @click="deleteTemplate(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 模板编辑对话框 -->
    <el-dialog v-model="templateDialogVisible" title="编辑模板" width="90%">
      <el-form :model="templateForm" label-width="120px">
        <el-form-item label="模板名称">
          <el-input v-model="templateForm.templateName" />
        </el-form-item>
        
        <el-form-item label="模板类型">
          <el-select v-model="templateForm.templateType">
            <el-option label="页面配置" value="PAGE" />
            <el-option label="字典配置" value="DICT" />
            <el-option label="推送配置" value="PUSH" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="模板说明">
          <el-input v-model="templateForm.description" type="textarea" :rows="3" />
        </el-form-item>
        
        <el-form-item label="模板内容">
          <codemirror
            v-model="templateForm.content"
            :extensions="[json()]"
            :style="{ height: '500px' }"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="templateDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveTemplate">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { getTemplateList, saveTemplate, deleteTemplate } from '@/api/erp/config'

const templates = ref([])
const activeCategory = ref('all')
const templateDialogVisible = ref(false)
const templateForm = ref({})

// 过滤模板
const filteredTemplates = computed(() => {
  if (activeCategory.value === 'all') {
    return templates.value
  }
  return templates.value.filter(t => t.templateType === activeCategory.value)
})

// 加载模板列表
async function loadTemplates() {
  const res = await getTemplateList()
  templates.value = res.data || []
}

// 创建模板
function createTemplate() {
  templateForm.value = {
    templateName: '',
    templateType: 'PAGE',
    description: '',
    content: '{}'
  }
  templateDialogVisible.value = true
}

// 使用模板
function useTemplate(row) {
  emit('use', row)
}

// 保存模板
async function saveTemplate() {
  await saveTemplate(templateForm.value)
  ElMessage.success('保存成功')
  templateDialogVisible.value = false
  loadTemplates()
}
</script>
```

### 2.4 辅助功能完善

#### 2.4.1 配置复制功能

```vue
<script setup>
// 配置复制
async function handleCopy(row) {
  try {
    await ElMessageBox.confirm(
      `确认要复制配置 "${row.configName}" 吗？`,
      '复制配置',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }
    )
    
    // 复制配置数据
    const copyData = {
      moduleCode: `${row.moduleCode}_copy`,
      configName: `${row.configName} (副本)`,
      configType: row.configType,
      configContent: row.configContent,
      isPublic: '0',
      remark: `复制自: ${row.configName}`
    }
    
    // 保存复制配置
    await saveConfig(copyData)
    ElMessage.success('复制成功')
    getList()
    
  } catch (e) {
    // 用户取消
  }
}
</script>
```

#### 2.4.2 配置导出功能

```vue
<script setup>
// 导出单个配置
function handleExport(row) {
  const exportData = {
    moduleCode: row.moduleCode,
    configName: row.configName,
    configType: row.configType,
    configContent: JSON.parse(row.configContent),
    exportTime: new Date().toISOString(),
    exportBy: userStore.name
  }
  
  // 下载JSON文件
  const blob = new Blob([JSON.stringify(exportData, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `${row.moduleCode}_${Date.now()}.json`
  link.click()
  URL.revokeObjectURL(url)
  
  ElMessage.success('导出成功')
}

// 批量导出
function handleBatchExport() {
  const selectedRows = selection.value
  if (selectedRows.length === 0) {
    ElMessage.warning('请选择要导出的配置')
    return
  }
  
  const exportData = {
    configs: selectedRows.map(row => ({
      moduleCode: row.moduleCode,
      configName: row.configName,
      configType: row.configType,
      configContent: JSON.parse(row.configContent)
    })),
    exportTime: new Date().toISOString(),
    totalCount: selectedRows.length
  }
  
  // 下载
  const blob = new Blob([JSON.stringify(exportData, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `erp_configs_${Date.now()}.json`
  link.click()
  URL.revokeObjectURL(url)
  
  ElMessage.success(`成功导出${selectedRows.length}个配置`)
}
</script>
```

#### 2.4.3 配置导入功能

```vue
<template>
  <el-dialog v-model="importDialogVisible" title="导入配置" width="600px">
    <el-upload
      ref="uploadRef"
      :auto-upload="false"
      :limit="1"
      accept=".json"
      :on-change="handleFileChange"
      drag
    >
      <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
      <div class="el-upload__text">
        拖拽文件到此处或<em>点击上传</em>
      </div>
      <template #tip>
        <div class="el-upload__tip">
          仅支持JSON格式文件
        </div>
      </template>
    </el-upload>
    
    <!-- 导入预览 -->
    <div v-if="importPreview.length > 0" class="import-preview">
      <el-divider>导入预览</el-divider>
      <el-table :data="importPreview" border max-height="300px">
        <el-table-column prop="moduleCode" label="模块编码" />
        <el-table-column prop="configName" label="配置名称" />
        <el-table-column prop="configType" label="类型" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.exists ? 'warning' : 'success'">
              {{ row.exists ? '已存在' : '新增' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>
    
    <template #footer>
      <el-button @click="importDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="confirmImport" :disabled="importPreview.length === 0">
        确认导入
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref } from 'vue'
import { saveConfig, checkConfigExists } from '@/api/erp/config'

const importDialogVisible = ref(false)
const importPreview = ref([])
const importData = ref([])

// 文件选择
async function handleFileChange(file) {
  try {
    const content = await readFileAsText(file.raw)
    const data = JSON.parse(content)
    
    // 处理导入数据
    let configs = []
    if (data.configs) {
      // 批量导入
      configs = data.configs
    } else {
      // 单个导入
      configs = [data]
    }
    
    // 检查是否存在
    importPreview.value = await Promise.all(
      configs.map(async (config) => {
        const exists = await checkConfigExists(config.moduleCode)
        return {
          ...config,
          exists: exists.data
        }
      })
    )
    
    importData.value = configs
    
  } catch (e) {
    ElMessage.error('文件解析失败: ' + e.message)
  }
}

// 确认导入
async function confirmImport() {
  try {
    const results = await Promise.allSettled(
      importData.value.map(config => saveConfig({
        ...config,
        configContent: JSON.stringify(config.configContent)
      }))
    )
    
    const successCount = results.filter(r => r.status === 'fulfilled').length
    const failCount = results.filter(r => r.status === 'rejected').length
    
    ElMessage.success(`导入完成: 成功${successCount}个, 失败${failCount}个`)
    importDialogVisible.value = false
    getList()
    
  } catch (e) {
    ElMessage.error('导入失败: ' + e.message)
  }
}

// 读取文件
function readFileAsText(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = (e) => resolve(e.target.result)
    reader.onerror = (e) => reject(e)
    reader.readAsText(file)
  })
}
</script>
```

### 2.5 批量操作功能

```vue
<template>
  <div class="batch-operations">
    <!-- 批量操作按钮 -->
    <div class="batch-actions" v-if="selection.length > 0">
      <el-alert :title="`已选择 ${selection.length} 项`" type="info" :closable="false">
        <template #default>
          <el-button size="small" @click="handleBatchExport">批量导出</el-button>
          <el-button size="small" @click="handleBatchDelete" type="danger">批量删除</el-button>
          <el-button size="small" @click="clearSelection">取消选择</el-button>
        </template>
      </el-alert>
    </div>
    
    <!-- 表格 -->
    <el-table
      :data="configList"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" />
      <!-- 其他列... -->
    </el-table>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessageBox } from 'element-plus'

const selection = ref([])

// 选择变化
function handleSelectionChange(val) {
  selection.value = val
}

// 清空选择
function clearSelection() {
  selection.value = []
}

// 批量删除
async function handleBatchDelete() {
  try {
    await ElMessageBox.confirm(
      `确认要删除选中的 ${selection.value.length} 个配置吗？`,
      '批量删除',
      { type: 'warning' }
    )
    
    const ids = selection.value.map(row => row.configId)
    await batchDeleteConfig(ids)
    
    ElMessage.success('删除成功')
    clearSelection()
    getList()
    
  } catch (e) {
    // 用户取消
  }
}
</script>
```

---

## 📋 三、实施计划

### 3.1 实施阶段

| 阶段 | 任务 | 工时 | 优先级 | 输出物 |
|------|------|------|--------|--------|
| **阶段一** | JSON编辑器增强 | 6h | 🔴 高 | 智能提示、实时验证 |
| **阶段二** | 可视化配置编辑器 | 8h | 🔴 高 | 表格/表单可视化编辑 |
| **阶段三** | 模板管理增强 | 4h | 🟡 中 | 模板库管理功能 |
| **阶段四** | 辅助功能完善 | 4h | 🟡 中 | 复制、导出、导入 |
| **阶段五** | 批量操作功能 | 2h | 🟢 低 | 批量删除、导出 |
| **阶段六** | 测试与优化 | 4h | 🔴 高 | 测试报告 |

**总工时**: 28小时

### 3.2 技术依赖

```json
{
  "dependencies": {
    "@codemirror/lang-json": "^6.0.0",
    "@codemirror/autocomplete": "^6.0.0",
    "@codemirror/lint": "^6.0.0",
    "codemirror-json-schema": "^1.0.0",
    "lodash": "^4.17.21"
  }
}
```

---

## 📊 四、优化效果评估

### 4.1 功能完整度提升

```
优化前:
├─ 配置管理: 90%
├─ JSON编辑: 70%
├─ 模板管理: 40%
├─ 历史版本: 80%
└─ 辅助功能: 30%

优化后:
├─ 配置管理: 100% (+10%)
├─ JSON编辑: 95% (+25%)
├─ 模板管理: 90% (+50%)
├─ 历史版本: 90% (+10%)
└─ 辅助功能: 90% (+60%)
```

### 4.2 用户体验提升

| 指标 | 优化前 | 优化后 | 提升 |
|------|--------|--------|------|
| 配置编写效率 | 60% | 90% | ↑ 50% |
| 错误发现及时性 | 40% | 95% | ↑ 137% |
| 操作便捷性 | 50% | 85% | ↑ 70% |
| 功能满意度 | 65% | 90% | ↑ 38% |

### 4.3 开发效率提升

```
配置开发时间:
├─ 新建配置: 30分钟 → 10分钟 (↓ 66%)
├─ 修改配置: 20分钟 → 8分钟 (↓ 60%)
├─ 调试配置: 15分钟 → 5分钟 (↓ 66%)
└─ 模板复用: 25分钟 → 3分钟 (↓ 88%)
```

---

## ✅ 五、验收标准

### 5.1 功能验收

- ✅ JSON编辑器支持智能提示和自动补全
- ✅ JSON编辑器支持实时验证和错误提示
- ✅ 可视化编辑器支持表格配置编辑
- ✅ 可视化编辑器支持表单配置编辑
- ✅ 模板管理支持创建、编辑、删除
- ✅ 支持配置复制、导出、导入
- ✅ 支持批量删除、批量导出

### 5.2 性能验收

- ✅ JSON验证响应时间 < 100ms
- ✅ 可视化编辑器渲染流畅
- ✅ 大配置文件(>100KB)编辑不卡顿
- ✅ 批量操作(>50项)响应时间 < 3s

### 5.3 兼容性验收

- ✅ 支持Chrome、Firefox、Edge最新版本
- ✅ 支持配置向后兼容
- ✅ 导入导出格式兼容

---

**文档版本**: v1.0  
**最后更新**: 2026-03-24  
**维护人员**: ERP配置化开发团队
