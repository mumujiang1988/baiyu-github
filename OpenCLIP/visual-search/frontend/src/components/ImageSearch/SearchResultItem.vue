<template>
  <div class="product-card">
    <!-- 左侧：产品信息面板 -->
    <aside class="info-panel">
      <!-- 排名徽章 -->
      <div class="rank-badge">
        <span :class="['rank-number', rankClass]">
          {{ index + 1 }}
        </span>
      </div>
      
      <!-- 产品编码 -->
      <div class="product-code">{{ result.product_code }}</div>
      
      <!-- 产品名称 -->
      <h3 class="product-name">{{ result.product_name || '未命名产品' }}</h3>
      
      <!-- 匹配度指示器 -->
      <div class="similarity-indicator">
        <div class="similarity-bar" :style="{ width: similarityPercent + '%', background: similarityColor }"></div>
        <span class="similarity-text" :style="{ color: similarityColor }">
          {{ similarityPercent }}%
        </span>
      </div>
    </aside>
    
    <!-- 右侧：图片网格展示 -->
    <section class="image-gallery">
      <div class="gallery-grid">
        <div 
          v-for="(imgPath, imgIndex) in result.image_paths" 
          :key="imgIndex"
          class="gallery-item"
          @click="$emit('preview', { imagePaths: result.image_paths, currentIndex: imgIndex })"
        >
          <img 
            :src="getImageUrl(imgPath)"
            loading="lazy"
            decoding="async"
            class="gallery-image"
            :alt="`产品图片 ${imgIndex + 1}`"
          />
          <div class="image-overlay">
            <el-icon class="zoom-icon"><ZoomIn /></el-icon>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
/**
 * 搜索结果项组件 - 重构版
 * 
 * 采用现代商务设计风格，左侧展示产品信息，右侧网格展示所有产品图片
 * 强调图片优先展示，最大化可视面积
 */
import { computed } from 'vue'
import { ZoomIn } from '@element-plus/icons-vue'
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

defineEmits(['preview'])

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
/* ============================================
   产品卡片 - 现代商务风格
   ============================================ */
.product-card {
  display: flex;
  gap: 16px;
  padding: 16px;
  margin-bottom: 12px;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-radius: 12px;
  border: 1px solid #e9ecef;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.product-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
  border-color: #dee2e6;
}

/* ============================================
   左侧信息面板 - 固定宽度
   ============================================ */
.info-panel {
  flex: 0 0 220px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 14px;
  padding: 20px 16px;
  background: #fafbfc;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

/* 排名徽章 */
.rank-badge {
  display: inline-flex;
}

.rank-number {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  font-size: 15px;
  font-weight: 700;
  color: #165DFF;
  background: linear-gradient(135deg, #f0f5ff 0%, #e6f0ff 100%);
  border: 2px solid #165DFF;
  box-shadow: 0 2px 8px rgba(22, 93, 255, 0.15);
  transition: all 0.3s ease;
}

.product-card:hover .rank-number {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(22, 93, 255, 0.25);
}

.rank-first {
  background: linear-gradient(135deg, #ffd700 0%, #ffed4e 100%);
  color: #fff;
  border-color: #ffc107;
  box-shadow: 0 2px 8px rgba(255, 193, 7, 0.3);
}

.rank-top3 {
  background: linear-gradient(135deg, #c0c0c0 0%, #e8e8e8 100%);
  color: #fff;
  border-color: #adb5bd;
  box-shadow: 0 2px 8px rgba(173, 181, 189, 0.3);
}

/* 产品编码 */
.product-code {
  font-size: 13px;
  font-weight: 500;
  color: #6c757d;
  text-align: center;
  word-break: break-all;
  letter-spacing: 0.3px;
}

/* 产品名称 */
.product-name {
  font-size: 16px;
  font-weight: 600;
  color: #212529;
  text-align: center;
  line-height: 1.5;
  margin: 0;
  /* 允许换行，最多显示2行 */
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 匹配度指示器 */
.similarity-indicator {
  width: 100%;
  position: relative;
  margin-top: 4px;
}

.similarity-bar {
  height: 6px;
  border-radius: 3px;
  background: linear-gradient(90deg, var(--bar-color) 0%, var(--bar-color) 100%);
  transition: width 0.6s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.similarity-text {
  display: block;
  text-align: center;
  font-size: 16px;
  font-weight: 700;
  margin-top: 8px;
  letter-spacing: 0.5px;
}

/* ============================================
   右侧图片网格 - 自适应布局
   ============================================ */
.image-gallery {
  flex: 1;
  min-width: 0; /* 防止溢出 */
  display: flex;
  align-items: stretch;
}

.gallery-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 10px;
  width: 100%;
  align-content: start;
}

.gallery-item {
  position: relative;
  aspect-ratio: 1;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  background: #f8f9fa;
  border: 2px solid transparent;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.gallery-item:hover {
  border-color: #165DFF;
  transform: scale(1.02);
  box-shadow: 0 4px 16px rgba(22, 93, 255, 0.2);
}

.gallery-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.gallery-item:hover .gallery-image {
  transform: scale(1.05);
}

/* 图片悬停遮罩 */
.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(22, 93, 255, 0.15);
  backdrop-filter: blur(2px);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: all 0.3s ease;
}

.gallery-item:hover .image-overlay {
  opacity: 1;
}

.zoom-icon {
  font-size: 32px;
  color: #fff;
  filter: drop-shadow(0 2px 8px rgba(0, 0, 0, 0.3));
  transform: scale(0.8);
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.gallery-item:hover .zoom-icon {
  transform: scale(1);
}

/* ============================================
   响应式优化
   ============================================ */
@media (max-width: 1200px) {
  .gallery-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  }
}

@media (max-width: 768px) {
  .product-card {
    flex-direction: column;
  }
  
  .info-panel {
    flex: 0 0 auto;
    width: 100%;
  }
  
  .gallery-grid {
    grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  }
}
</style>
