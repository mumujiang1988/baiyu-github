import axios from 'axios'

// API 基础路径
const API_BASE = '/api/v1'

/**
 * 移除图片背景（抠图）
 * @param {File} file - 原始图片文件
 * @returns {Promise<Blob>} 抠图后的透明背景图片（PNG格式）
 */
export async function removeBackground(file) {
  const formData = new FormData()
  formData.append('file', file)
  
  const response = await axios.post(
    `${API_BASE}/rembg/remove`,
    formData,
    {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      responseType: 'blob'  // 重要：返回二进制数据
    }
  )
  
  return response.data
}
