/**
 * 关系页签可视化编辑器
 * 用于配置详情页的页签结构 (表格/表单类型)
 */

<template>
  <div class="relation-tabs-editor">
    <el-card shadow="hover" class="mb-3">
      <template #header>
        <div class="card-header">
          <span>{{ title }}</span>
          <el-button type="primary" size="small" @click="handleAddTab">
            <el-icon><Plus /></el-icon>
            添加页签
          </el-button>
        </div>
      </template>

      <!-- 页签列表 -->
      <el-table :data="localTabs" border stripe row-key="id">
        <el-table-column type="index" width="50" align="center" label="序号" />
        
        <el-table-column prop="title" label="页签标题" min-width="120">
          <template #default="{ row, $index }">
            <el-input v-if="editingIndex === $index" v-model="row.title" placeholder="页签标题" />
            <span v-else>{{ row.title }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="type" label="页签类型" width="120">
          <template #default="{ row, $index }">
            <el-select v-if="editingIndex === $index" v-model="row.type" placeholder="选择类型">
              <el-option label="表格" value="table" />
              <el-option label="表单" value="form" />
            </el-select>
            <el-tag v-else :type="row.type === 'table' ? 'primary' : 'success'" size="small">
              {{ row.type === 'table' ? '表格' : '表单' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="tableName" label="表名" min-width="150">
          <template #default="{ row, $index }">
            <el-input v-if="editingIndex === $index" v-model="row.tableName" placeholder="数据库表名" />
            <span v-else>{{ row.tableName || '-' }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="apiPrefix" label="API 前缀" min-width="150">
          <template #default="{ row, $index }">
            <el-input v-if="editingIndex === $index" v-model="row.apiPrefix" placeholder="/erp/engine" />
            <span v-else>{{ row.apiPrefix || '-' }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ $index }">
            <el-button
              v-if="editingIndex !== $index"
              type="primary"
              link
              size="small"
              @click="handleEdit($index)"
            >
              编辑
            </el-button>
            <el-button
              v-if="editingIndex === $index"
              type="success"
              link
              size="small"
              @click="handleSave($index)"
            >
              保存
            </el-button>
            <el-button
              v-if="editingIndex === $index"
              type="danger"
              link
              size="small"
              @click="handleCancel"
            >
              取消
            </el-button>
            <el-button
              type="danger"
              link
              size="small"
              @click="handleDelete($index)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 配置详情对话框 -->
    <el-dialog
      v-model="configDialogVisible"
      title="页签详细配置"
      width="80%"
      top="5vh"
    >
      <el-alert
        :title="`当前配置：${currentTab?.title || ''}`"
        type="info"
        :closable="false"
        show-icon
        class="mb-4"
      />

      <el-tabs v-model="configActiveTab">
        <!-- 表格配置 -->
        <el-tab-pane v-if="currentTab?.type === 'table'" label="表格配置" name="table">
          <el-form :model="currentTab" label-width="140px" size="default">
            <el-form-item label="表格列配置">
              <el-input
                v-model="currentTab.columnsJson"
                type="textarea"
                :rows="10"
                placeholder='例如：[{"prop": "field", "label": "字段"}]'
              />
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 表单配置 -->
        <el-tab-pane v-if="currentTab?.type === 'form'" label="表单配置" name="form">
          <el-form :model="currentTab" label-width="140px" size="default">
            <el-form-item label="表单字段配置">
              <el-input
                v-model="currentTab.fieldsJson"
                type="textarea"
                :rows="10"
                placeholder='例如：[{"field": "field", "label": "字段", "component": "input"}]'
              />
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 通用配置 -->
        <el-tab-pane label="通用配置" name="common">
          <el-form :model="currentTab" label-width="140px" size="default">
            <el-form-item label="页签 Key">
              <el-input v-model="currentTab.key" placeholder="唯一标识" />
            </el-form-item>
            <el-form-item label="显示标题">
              <el-switch v-model="currentTab.showTitle" />
            </el-form-item>
            <el-form-item label="启用虚拟字段">
              <el-switch v-model="currentTab.enableVirtualFields" />
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <template #footer>
        <el-button @click="configDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveConfig">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { Document as Plus } from '@element-plus/icons-vue'

interface RelationTabConfig {
  id?: string | number
  key?: string
  title: string
  type: 'table' | 'form'
  tableName?: string
  apiPrefix?: string
  showTitle?: boolean
  enableVirtualFields?: boolean
  columnsJson?: string
  fieldsJson?: string
  [key: string]: any
}

interface Props {
  modelValue: RelationTabConfig[]
  title?: string
}

const props = withDefaults(defineProps<Props>(), {
  title: '页签配置'
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: RelationTabConfig[]): void
}>()

// 本地页签列表
const localTabs = ref<RelationTabConfig[]>([...props.modelValue])

// 正在编辑的索引
const editingIndex = ref<number>(-1)

// 配置对话框
const configDialogVisible = ref(false)
const configActiveTab = ref('common')
const currentTab = ref<RelationTabConfig | null>(null)

// 监听外部变化
watch(() => props.modelValue, (newVal) => {
  if (editingIndex.value === -1 && !configDialogVisible.value) {
    localTabs.value = [...newVal]
  }
}, { deep: true })

/**
 * 添加页签
 */
function handleAddTab() {
  localTabs.value.push({
    id: Date.now(),
    key: `tab_${Date.now()}`,
    title: '新页签',
    type: 'table',
    tableName: '',
    apiPrefix: '/erp/engine',
    showTitle: true,
    enableVirtualFields: false,
    columnsJson: '[]',
    fieldsJson: '[]'
  })
  emit('update:modelValue', localTabs.value)
}

/**
 * 编辑页签
 */
function handleEdit(index: number) {
  editingIndex.value = index
  // 打开详细配置对话框
  currentTab.value = { ...localTabs.value[index] }
  configActiveTab.value = currentTab.value.type === 'table' ? 'table' : 'form'
  configDialogVisible.value = true
}

/**
 * 保存页签
 */
function handleSave(index: number) {
  const tab = localTabs.value[index]
  
  // 验证
  if (!tab.title || !tab.type) {
    alert('请填写完整的页签信息')
    return
  }
  
  editingIndex.value = -1
  emit('update:modelValue', localTabs.value)
}

/**
 * 保存详细配置
 */
function handleSaveConfig() {
  if (!currentTab.value || editingIndex.value === -1) return
  
  // 验证 JSON 格式
  if (currentTab.value.type === 'table' && currentTab.value.columnsJson) {
    try {
      JSON.parse(currentTab.value.columnsJson)
    } catch (e) {
      alert('表格列配置 JSON 格式错误')
      return
    }
  }
  
  if (currentTab.value.type === 'form' && currentTab.value.fieldsJson) {
    try {
      JSON.parse(currentTab.value.fieldsJson)
    } catch (e) {
      alert('表单字段配置 JSON 格式错误')
      return
    }
  }
  
  // 更新到本地列表
  localTabs.value[editingIndex.value] = { ...currentTab.value }
  emit('update:modelValue', localTabs.value)
  
  configDialogVisible.value = false
  currentTab.value = null
  editingIndex.value = -1
}

/**
 * 取消编辑
 */
function handleCancel() {
  editingIndex.value = -1
  configDialogVisible.value = false
  currentTab.value = null
}

/**
 * 删除页签
 */
async function handleDelete(index: number) {
  if (confirm('确认删除该页签？')) {
    localTabs.value.splice(index, 1)
    emit('update:modelValue', localTabs.value)
  }
}
</script>

<style scoped lang="scss">
.relation-tabs-editor {
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .mb-3 {
    margin-bottom: 12px;
  }
}
</style>
