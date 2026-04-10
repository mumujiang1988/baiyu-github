<template>
  <div class="search-results">
    <!-- 结果列表 -->
    <div v-if="results.length > 0" class="result-list">
      <SearchResultItem
        v-for="(result, index) in results"
        :key="result.product_code"
        :result="result"
        :index="index"
        @scroll="$emit('scroll', $event)"
        @preview="$emit('preview', $event)"
        @set-carousel-ref="$emit('setCarouselRef', $event)"
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
  }
})

defineEmits(['scroll', 'preview', 'clear', 'setCarouselRef'])
</script>

<style scoped>
.search-results {
  margin-top: 16px;
}

.result-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.empty-state {
  padding: 40px 0;
}
</style>
