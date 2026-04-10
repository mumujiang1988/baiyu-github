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
      
      <!-- 产品编码 -->
      <div class="product-code">{{ result.product_code }}</div>
      
      <!-- 产品名称（主标题） -->
      <div class="product-name">{{ result.product_name || '未命名产品' }}</div>
      
      <!-- 匹配度 -->
      <div class="similarity-section">
        <span class="similarity-label">匹配度</span>
        <span class="similarity-percent" :style="{ color: similarityColor }">
          {{ similarityPercent }}%
        </span>
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
          loading="lazy"
          decoding="async"
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
  gap: 12px; /* 进一步减少左右间距 */
  padding: 8px; /* 进一步减少内边距 */
  margin-bottom: 0; /* 移除下边距，使用 gap 控制 */
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
  flex: 0 0 280px; /* 固定宽度，不伸缩 */
  display: flex;
  flex-direction: column;
  align-items: center; /* 整体居中 */
  justify-content: center; /* 垂直居中 */
  gap: 12px;
  min-width: 280px;
  padding: 16px 12px;
}

.rank-badge {
  display: inline-flex;
}

.rank-number {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  font-size: 14px; /* 商务标准：14px */
  font-weight: 700; /* 加粗高亮 */
  color: #165DFF; /* 商务蓝 */
  background: #f0f5ff;
  border: 2px solid #165DFF;
}

.rank-first {
  background: linear-gradient(135deg, #ffd700, #ffed4e);
  color: #fff;
  border-color: #ffd700;
}

.rank-top3 {
  background: linear-gradient(135deg, #c0c0c0, #e8e8e8);
  color: #fff;
  border-color: #c0c0c0;
}

.product-code {
  font-size: 14px; /* 商务标准：14px */
  font-weight: 400; /* 常规字重 */
  color: #4E5969; /* 商务灰 */
  text-align: center;
  word-break: break-all;
}

.product-name {
  font-size: 15px; /* 商务标准：15px 主标题 */
  font-weight: 600; /* 半粗体 */
  color: #1D2129; /* 深黑色 */
  text-align: center;
  line-height: 1.5;
  /* 允许换行，最多显示2行 */
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 匹配度 */
.similarity-section {
  display: flex;
  align-items: center;
  gap: 6px;
}

.similarity-label {
  font-size: 13px; /* 商务标准：13px 次要信息 */
  font-weight: 400; /* 常规字重 */
  color: #86909C; /* 浅灰色 */
}

.similarity-percent {
  font-size: 14px; /* 商务标准：14px */
  font-weight: 700; /* 加粗高亮 */
}

/* 右侧图片轮播 */
.image-carousel-wrapper {
  position: relative;
  flex: 1; /* 占据剩余空间 */
  min-width: 300px; /* 最小宽度 */
  height: 280px; /* 固定高度 */
  display: flex;
  align-items: center;
  justify-content: center;
}

.image-carousel {
  display: flex;
  width: 100%;
  height: 100%;
  overflow-x: auto;
  scroll-behavior: smooth;
  scrollbar-width: none;
  -ms-overflow-style: none;
  border-radius: 6px;
  gap: 4px; /* 图片之间的间距 - 商务标准 */
  padding: 0 2px; /* 左右内边距 */
}

.image-carousel::-webkit-scrollbar {
  display: none;
}

.carousel-image {
  flex-shrink: 0;
  width: calc(100% - 4px); /* 减去间距，避免溢出 */
  height: 100%; /* 填满容器高度 */
  object-fit: contain; /* 保持比例，完整显示 */
  border-radius: 6px;
  cursor: pointer;
  background: #f5f7fa; /* 添加浅色背景 */
}

.carousel-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  z-index: 10;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.6);
  border: none;
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.carousel-btn:hover {
  background: rgba(0, 0, 0, 0.8);
  transform: translateY(-50%) scale(1.1);
}

.carousel-left {
  left: 12px;
}

.carousel-right {
  right: 12px;
}
</style>
