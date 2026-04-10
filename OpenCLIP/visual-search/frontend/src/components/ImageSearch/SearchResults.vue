<template>
  <div class="search-results">
    <!-- 加载中状态 -->
    <div v-if="isLoading" class="loading-state">
      <el-skeleton :rows="3" animated />
      <el-skeleton :rows="3" animated style="margin-top: 16px" />
      <el-skeleton :rows="3" animated style="margin-top: 16px" />
    </div>
    
    <!-- 结果列表 -->
    <div v-else-if="results.length > 0" class="result-list">
      <SearchResultItem
        v-for="(result, index) in results"
        :key="result.product_code"
        :result="result"
        :index="index"
        @preview="$emit('preview', $event)"
      />
    </div>
    
    <!-- 空状态提示 -->
    <div v-else class="empty-state">
      <el-empty 
        v-if="!hasSearched"
        :description="searchMode === 'image' ? '请上传图片开始检索' : '请输入关键词开始搜索'" 
        :image-size="120" 
      />
      <el-empty 
        v-else
        description="未找到相似产品，请尝试其他图片或关键词"
        :image-size="120"
      >
        <template #default>
          <el-button type="primary" size="small" @click="$emit('clear')">
            {{ searchMode === 'image' ? '尝试其他图片' : '重新搜索' }}
          </el-button>
        </template>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
/**
 * 搜索结果列表组件
 * 
 * 负责展示搜索结果列表和空状态
 */
import SearchResultItem from './SearchResultItem.vue'

defineProps({
  /**
   * 搜索结果数组
   */
  results: {
    type: Array,
    default: () => []
  },
  
  /**
   * 是否已执行过搜索
   */
  hasSearched: {
    type: Boolean,
    default: false
  },
  
  /**
   * 搜索模式：'image' | 'text'
   */
  searchMode: {
    type: String,
    default: 'image',
    validator: (value) => ['image', 'text'].includes(value)
  },
  
  /**
   * 是否正在加载中
   */
  isLoading: {
    type: Boolean,
    default: false
  }
})

defineEmits(['preview', 'clear'])
</script>

<style scoped>
.search-results {
  margin-top: 4px; /* 进一步减少上边距 */
}

.result-list {
  display: flex;
  flex-direction: column;
  gap: 8px; /* 进一步减少卡片间距 */
}

.empty-state {
  padding: 20px 0; /* 减少空状态内边距 */
}

.loading-state {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
}
</style>
