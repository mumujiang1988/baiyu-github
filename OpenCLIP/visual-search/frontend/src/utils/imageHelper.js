import axios from 'axios'

// API 基础路径
const API_BASE = '/api/v1'

/**
 * 获取图片访问 URL
 * @param {string} path - 图片存储路径（保持原始格式，如 minio://bucket/path/file.jpg）
 * @returns {string} 完整的图片访问地址
 */
export function getImageUrl(path) {
  if (!path) return ''
  
  // 如果已经是完整 URL，直接返回
  if (path.startsWith('http')) return path
  
  // 直接透传原始路径，由后端统一处理
  // 后端会智能识别并移除协议前缀和 bucket 名称
  return `${API_BASE}/images/${path}`
}
