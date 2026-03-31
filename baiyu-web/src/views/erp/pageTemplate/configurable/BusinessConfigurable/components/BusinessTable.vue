<template>
  <el-card shadow="never" class="table-card">
    <div class="table-wrapper">
      <el-table
        v-loading="loading"
        :data="tableData"
        :row-key="rowKey"
        :border="border"
        :stripe="stripe"
        :show-overflow-tooltip="showOverflowTooltip"
        :resizable="resizable"
        @selection-change="handleSelectionChange"
        @row-click="handleRowClick"
      >
        <!-- 动态列 -->
        <template v-for="(column, index) in visibleColumns" :key="index">
          <!-- 选择列 -->
          <el-table-column
            v-if="column.type === 'selection'"
            :type="column.type"
            :width="column.width"
            :fixed="column.fixed"
            :resizable="column.resizable"
          />
          
          <!-- 展开列 -->
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
              
              <!-- 字典文本渲染 -->
              <span v-else-if="column.renderType === 'text' && column.dictionary">
                {{ getDictLabel(scope.row[column.prop], column.dictionary) }}
              </span>
              
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
              
              <!-- 默认渲染 -->
              <span v-else>
                {{ scope.row[column.prop] ?? '-' }}
              </span>
            </template>
          </el-table-column>
        </template>
      </el-table>
    </div>
    
    <!-- 分页 -->
    <div class="pagination-wrapper">
      <el-pagination
        v-show="total > 0"
        :total="total"
        v-model:current-page="internalPageNum"
        v-model:page-size="internalPageSize"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        background
      />
    </div>
  </el-card>
</template>

<script setup name="BusinessTable">
import { ref, computed, watch } from 'vue'
import dictionaryManager from '@/views/erp/utils/DictionaryManager'
import { formatCurrency, formatDate, formatDateTime, formatPercent } from '@/views/erp/utils/index.js'

const props = defineProps({
  tableConfig: {
    type: Object,
    required: true
  },
  tableData: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  },
  total: {
    type: Number,
    default: 0
  },
  queryParams: {
    type: Object,
    required: true
  },
  dictionaryConfig: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits([
  'selection-change',
  'row-click',
  'view-detail',
  'page-size-change',
  'page-change'
])

// 内部页码状态
const internalPageNum = ref(props.queryParams?.pageNum || 1)
const internalPageSize = ref(props.queryParams?.pageSize || 10)

// 监听外部页码变化
watch(() => props.queryParams?.pageNum, (newVal) => {
  if (newVal) {
    internalPageNum.value = newVal
  }
})

watch(() => props.queryParams?.pageSize, (newVal) => {
  if (newVal) {
    internalPageSize.value = newVal
  }
})

// 计算属性
const rowKey = computed(() => props.tableConfig?.rowKey || 'id')
const border = computed(() => props.tableConfig?.border ?? true)
const stripe = computed(() => props.tableConfig?.stripe ?? true)
const showOverflowTooltip = computed(() => props.tableConfig?.showOverflowTooltip ?? true)
const resizable = computed(() => props.tableConfig?.resizable ?? true)

const visibleColumns = computed(() => {
  return props.tableConfig?.columns?.filter(col => col.visible !== false) || []
})

// 字典选项
const getDictOptions = (dictName) => {
  return dictionaryManager.getDictOptions(dictName)
}

// 标签配置
const getTagConfig = (value, dictName) => {
  const dict = getDictOptions(dictName)
  
  const option = dict.find(item => {
    return String(item.value) === String(value)
  })
  
  const validTypes = ['success', 'info', 'warning', 'danger', '']
  let tagType = option?.type || 'info'
  
  if (!validTypes.includes(tagType)) {
    tagType = 'info'
  }
  
  return {
    label: option?.label || value,
    type: tagType
  }
}

// 字典文本
const getDictLabel = (value, dictName) => {
  if (!dictName || !value && value !== 0) return value || '-'
  const dict = getDictOptions(dictName)
  const option = dict.find(item => String(item.value) === String(value))
  return option ? option.label : value
}

// 事件处理
const handleSelectionChange = (selection) => {
  emit('selection-change', selection)
}

const handleRowClick = (row, column) => {
  if (column.type === 'expand') {
    return
  }
  emit('row-click', row)
}

const handleViewDetail = (row) => {
  emit('view-detail', row)
}

const handleSizeChange = (size) => {
  emit('page-size-change', size)
}

const handleCurrentChange = (page) => {
  emit('page-change', page)
}
</script>

<style scoped>
/* ✅ 使用父组件的全局样式，不重复定义 */

/* 表格卡片样式 */
.table-card {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  margin-bottom: 0;
  overflow: hidden;
}

.table-card :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 12px 16px;
  overflow: hidden;
  min-height: 0;
}

/* 表格容器样式 */
.table-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  overflow: hidden;
}

.table-wrapper :deep(.el-table) {
  flex: 0 1 auto;
  min-height: 0;
  max-height: 100%;
}

.table-wrapper :deep(.el-table__body-wrapper) {
  overflow-y: auto;
  max-height: calc(100% - 40px);
}

/* 分页包装器样式 */
.pagination-wrapper {
  flex-shrink: 0;
  padding-top: 12px;
  display: flex;
  justify-content: flex-end;
}
</style>
