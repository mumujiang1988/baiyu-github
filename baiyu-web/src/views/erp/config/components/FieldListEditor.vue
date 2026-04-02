/**
 * 通用字段可视化编辑器
 * 用于配置表单字段、表格列、查询字段等
 */

<template>
  <div class="field-list-editor">
    <el-card shadow="hover" class="mb-3">
      <template #header>
        <div class="card-header">
          <span>{{ title }}</span>
          <el-button type="primary" size="small" @click="handleAdd">
            <el-icon><Plus /></el-icon>
            添加字段
          </el-button>
        </div>
      </template>

      <!-- 字段列表 -->
      <el-table :data="localFields" border stripe draggable row-key="id">
        <el-table-column type="index" width="50" align="center" label="序号" />
        
        <el-table-column prop="label" label="字段标签" min-width="120">
          <template #default="{ row, $index }">
            <el-input v-if="editingIndex === $index" v-model="row.label" placeholder="字段标签" />
            <span v-else>{{ row.label }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="field" label="字段名" min-width="120">
          <template #default="{ row, $index }">
            <el-input v-if="editingIndex === $index" v-model="row.field" placeholder="字段名" />
            <span v-else>{{ row.field }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="component" label="组件类型" width="120">
          <template #default="{ row, $index }">
            <el-select v-if="editingIndex === $index" v-model="row.component" placeholder="选择组件">
              <el-option label="输入框" value="input" />
              <el-option label="数字输入" value="input-number" />
              <el-option label="文本域" value="textarea" />
              <el-option label="下拉选择" value="select" />
              <el-option label="单选框" value="radio" />
              <el-option label="复选框" value="checkbox" />
              <el-option label="开关" value="switch" />
              <el-option label="日期选择" value="date-picker" />
              <el-option label="时间选择" value="time-picker" />
            </el-select>
            <el-tag v-else size="small">{{ getComponentLabel(row.component) }}</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="required" label="必填" width="60" align="center">
          <template #default="{ row, $index }">
            <el-checkbox 
              v-if="editingIndex === $index" 
              v-model="row.required" 
              :true-label="true"
              :false-label="false"
            />
            <el-tag v-else :type="row.required ? 'danger' : 'info'" size="small">
              {{ row.required ? '是' : '否' }}
            </el-tag>
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
  </div>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { Document as Plus } from '@element-plus/icons-vue'

interface FieldConfig {
  id?: string | number
  field: string
  label: string
  component?: string
  required?: boolean
  [key: string]: any
}

interface Props {
  modelValue: FieldConfig[]
  title?: string
}

const props = withDefaults(defineProps<Props>(), {
  title: '字段配置'
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: FieldConfig[]): void
}>()

// 本地字段列表
const localFields = ref<FieldConfig[]>([...props.modelValue])

// 正在编辑的索引
const editingIndex = ref<number>(-1)

// 监听外部变化
watch(() => props.modelValue, (newVal) => {
  if (editingIndex.value === -1) {
    localFields.value = [...newVal]
  }
}, { deep: true })

/**
 * 获取组件类型的中文标签
 */
const getComponentLabel = (component: string) => {
  const map: Record<string, string> = {
    'input': '输入框',
    'input-number': '数字输入',
    'textarea': '文本域',
    'select': '下拉选择',
    'radio': '单选框',
    'checkbox': '复选框',
    'switch': '开关',
    'date-picker': '日期选择',
    'time-picker': '时间选择'
  }
  return map[component] || component
}

/**
 * 添加字段
 */
function handleAdd() {
  localFields.value.push({
    id: Date.now(),
    field: '',
    label: '',
    component: 'input',
    required: false
  })
  emit('update:modelValue', localFields.value)
}

/**
 * 编辑字段
 */
function handleEdit(index: number) {
  editingIndex.value = index
}

/**
 * 保存字段
 */
function handleSave(index: number) {
  const field = localFields.value[index]
  
  // 验证
  if (!field.field || !field.label) {
    alert('请填写完整的字段信息')
    return
  }
  
  editingIndex.value = -1
  emit('update:modelValue', localFields.value)
}

/**
 * 取消编辑
 */
function handleCancel() {
  editingIndex.value = -1
}

/**
 * 删除字段
 */
async function handleDelete(index: number) {
  if (confirm('确认删除该字段？')) {
    localFields.value.splice(index, 1)
    emit('update:modelValue', localFields.value)
  }
}
</script>

<style scoped lang="scss">
.field-list-editor {
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
