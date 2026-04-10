/**
 * 搜索相关常量
 * 
 * 集中管理所有魔法数字和配置值，便于维护和修改
 */

// ==================== 轮播相关 ====================

/**
 * 轮播滚动步长（像素）
 */
export const CAROUSEL_SCROLL_STEP = 180

// ==================== 相似度相关 ====================

/**
 * 相似度阈值
 */
export const SIMILARITY_THRESHOLDS = {
  HIGH: 0.8,    // 高相似度
  MEDIUM: 0.6   // 中等相似度
}

/**
 * 相似度颜色映射
 */
export const SIMILARITY_COLORS = {
  HIGH: '#67C23A',   // 绿色 - 高相似度
  MEDIUM: '#E6A23C', // 橙色 - 中等相似度
  LOW: '#F56C6C'     // 红色 - 低相似度
}

// ==================== 检索配置 ====================

/**
 * 检索配置限制
 */
export const SEARCH_CONFIG = {
  MIN_TOP_K: 1,      // 最小返回数量
  MAX_TOP_K: 50,     // 最大返回数量
  DEFAULT_TOP_K: 10  // 默认返回数量
}

/**
 * 聚合策略选项
 */
export const AGGREGATION_STRATEGIES = {
  MAX: 'max',  // 最大相似度
  AVG: 'avg'   // 平均相似度
}

// ==================== 文本搜索 ====================

/**
 * 文本搜索限制
 */
export const TEXT_SEARCH = {
  MAX_KEYWORD_LENGTH: 100,           // 最大关键词长度
  DANGEROUS_CHARS: /[<>;'"]/g        // 危险字符正则
}

// ==================== 排名样式 ====================

/**
 * 排名徽章样式类
 */
export const RANK_CLASSES = {
  FIRST: 'rank-first',  // 第一名
  TOP3: 'rank-top3'     // 前三名
}

/**
 * 获取排名样式类
 * @param {number} index - 索引（从0开始）
 * @returns {string} 样式类名
 */
export function getRankClass(index) {
  if (index === 0) return RANK_CLASSES.FIRST
  if (index < 3) return RANK_CLASSES.TOP3
  return ''
}

// ==================== 相似度工具函数 ====================

/**
 * 根据相似度获取颜色
 * @param {number} similarity - 相似度 (0-1)
 * @returns {string} 颜色值
 */
export function getSimilarityColor(similarity) {
  if (similarity >= SIMILARITY_THRESHOLDS.HIGH) {
    return SIMILARITY_COLORS.HIGH
  }
  if (similarity >= SIMILARITY_THRESHOLDS.MEDIUM) {
    return SIMILARITY_COLORS.MEDIUM
  }
  return SIMILARITY_COLORS.LOW
}

/**
 * 将相似度转换为百分比
 * @param {number} similarity - 相似度 (0-1)
 * @returns {number} 百分比 (0-100)
 */
export function similarityToPercent(similarity) {
  return Math.round(similarity * 100)
}
