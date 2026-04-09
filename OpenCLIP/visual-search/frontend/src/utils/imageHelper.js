import axios from 'axios'

// API 基础路径
const API_BASE = '/api/v1'

/**
 * 获取图片访问 URL
 * @param {string} path - 图片存储路径
 * @returns {string} 完整的图片访问地址
 */
export function getImageUrl(path) {
  if (!path) return ''
  // 如果已经是完整 URL，直接返回
  if (path.startsWith('http')) return path
  return `${API_BASE}/images/${path}`
}
