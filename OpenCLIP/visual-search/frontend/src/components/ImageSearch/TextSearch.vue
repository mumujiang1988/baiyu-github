<template>
  <div class="text-search-section">
    <div class="section-header">
      <span class="section-title">文本搜索</span>
    </div>
    
    <el-input
      :model-value="keyword"
      placeholder="输入产品名称、规格或编码"
      size="default"
      clearable
      @update:model-value="handleInput"
      @keyup.enter="handleInput"
    >
      <template #prefix>
        <el-icon><Search /></el-icon>
      </template>
      <template #append>
        <el-button 
          type="primary" 
          @click="$emit('search')" 
          :loading="loading"
        >
          搜索
        </el-button>
      </template>
    </el-input>
  </div>
</template>

<script setup>
/**
 * 文本搜索组件
 * 
 * 负责关键词输入和触发文本搜索
 */
import { ref, onUnmounted } from 'vue'
import { Search } from '@element-plus/icons-vue'

const props = defineProps({
  /**
   * 搜索关键词
   */
  keyword: {
    type: String,
    default: ''
  },
  
  /**
   * 是否正在搜索中
   */
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:keyword', 'search'])

// 防抖定时器
let searchTimer = null

/**
 * 处理输入（带防抖）
 */
const handleInput = (value) => {
  emit('update:keyword', value)
  
  // 清除之前的定时器
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  
  // 设置新的防抖定时器 (500ms)
  searchTimer = setTimeout(() => {
    if (value.trim()) {
      emit('search')
    }
  }, 500)
}

// 组件卸载时清理定时器
onUnmounted(() => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
})
</script>

<style scoped>
.text-search-section {
  margin-top: 16px;
}

.section-header {
  margin-bottom: 12px;
}

.section-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}
</style>
