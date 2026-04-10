<template>
  <div class="product-card">
    <!-- 左侧：产品信息 -->
    <div class="product-info-section">
      <!-- 排名徽章 -->
      <div class="rank-badge">
        <span :class="['rank-number', rankClass]">
          {{ index + 1 }}
        </span>
      </div>
      
      <!-- 产品编码和名称并排 -->
      <div class="product-info-row">
        <div class="product-code">{{ result.product_code }}</div>
        <div class="product-name">{{ result.product_name || '未命名产品' }}</div>
      </div>
      
      <!-- 匹配度 -->
      <div class="similarity-section">
        <div class="similarity-header">
          <span class="similarity-label">匹配度</span>
          <span class="similarity-percent" :style="{ color: similarityColor }">
            {{ similarityPercent }}%
          </span>
        </div>
        <el-progress
          :percentage="similarityPercent"
          :color="similarityColor"
          :stroke-width="8"
          :show-text="false"
        />
      </div>
    </div>
    
    <!-- 右侧：图片轮播 -->
    <div class="image-carousel-wrapper">
      <button 
        v-if="result.image_paths.length > 1"
        class="carousel-btn carousel-left" 
        @click="$emit('scroll', { index, direction: -1 })"
      >
        <el-icon><ArrowLeft /></el-icon>
      </button>
      
      <div class="image-carousel" :ref="(el) => $emit('setCarouselRef', { el, index })">
        <img 
          v-for="(imgPath, imgIndex) in result.image_paths" 
          :key="imgIndex"
          :src="getImageUrl(imgPath)"
          class="carousel-image"
          @click="$emit('preview', { imagePaths: result.image_paths, currentIndex: imgIndex })"
        />
      </div>
      
      <button 
        v-if="result.image_paths.length > 1"
        class="carousel-btn carousel-right" 
        @click="$emit('scroll', { index, direction: 1 })"
      >
        <el-icon><ArrowRight /></el-icon>
      </button>
    </div>
  </div>
</template>

<script setup>
/**
 * 搜索结果项组件
 * 
 * 展示单个产品的检索结果，包括产品信息和图片轮播
 */
import { computed } from 'vue'
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { getImageUrl } from '../../utils/imageHelper'
import { getSimilarityColor, similarityToPercent, getRankClass } from '../../constants/search'

const props = defineProps({
  /**
   * 搜索结果数据
   */
  result: {
    type: Object,
    required: true
  },
  
  /**
   * 结果索引（从0开始）
   */
  index: {
    type: Number,
    required: true
  }
})

defineEmits(['scroll', 'preview', 'setCarouselRef'])

// 计算排名样式类
const rankClass = computed(() => {
  return getRankClass(props.index)
})

// 计算相似度百分比
const similarityPercent = computed(() => {
  return similarityToPercent(props.result.similarity)
})

// 计算相似度颜色
const similarityColor = computed(() => {
  return getSimilarityColor(props.result.similarity)
})
</script>

<style scoped>
.product-card {
  display: flex;
  gap: 20px;
  padding: 16px;
  margin-bottom: 16px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
}

.product-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

/* 左侧产品信息 */
.product-info-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.rank-badge {
  display: inline-flex;
}

.rank-number {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  font-size: 14px;
  font-weight: 600;
  background: #f0f0f0;
  color: #909399;
}

.rank-first {
  background: linear-gradient(135deg, #ffd700, #ffed4e);
  color: #fff;
}

.rank-top3 {
  background: linear-gradient(135deg, #c0c0c0, #e8e8e8);
  color: #fff;
}

.product-info-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.product-code {
  font-size: 14px;
  font-weight: 600;
  color: #409eff;
  white-space: nowrap;
}

.product-name {
  font-size: 14px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 匹配度 */
.similarity-section {
  margin-top: 8px;
}

.similarity-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.similarity-label {
  font-size: 12px;
  color: #909399;
}

.similarity-percent {
  font-size: 14px;
  font-weight: 600;
}

/* 右侧图片轮播 */
.image-carousel-wrapper {
  position: relative;
  width: 200px;
  height: 200px;
  flex-shrink: 0;
}

.image-carousel {
  display: flex;
  width: 100%;
  height: 100%;
  overflow-x: auto;
  scroll-behavior: smooth;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.image-carousel::-webkit-scrollbar {
  display: none;
}

.carousel-image {
  flex-shrink: 0;
  width: 200px;
  height: 200px;
  object-fit: cover;
  border-radius: 6px;
  cursor: pointer;
  transition: transform 0.2s;
}

.carousel-image:hover {
  transform: scale(1.05);
}

.carousel-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  z-index: 10;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.5);
  border: none;
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.carousel-btn:hover {
  background: rgba(0, 0, 0, 0.7);
}

.carousel-left {
  left: 8px;
}

.carousel-right {
  right: 8px;
}
</style>
