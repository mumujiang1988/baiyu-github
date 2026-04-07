import axios from 'axios'

// API 基础路径
const API_BASE = '/api/v1'

/**
 * 图像检索
 * @param {File} file - 图片文件
 * @param {number} topK - 返回数量
 * @param {string} aggregation - 聚合策略
 * @returns {Promise}
 */
export async function searchImage(file, topK = 10, aggregation = 'max') {
  const formData = new FormData()
  formData.append('file', file)
  
  const response = await axios.post(
    `${API_BASE}/search?top_k=${topK}&aggregation=${aggregation}`,
    formData,
    {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    }
  )
  
  return response.data
}

/**
 * 产品入库
 * @param {string} productCode - 产品编码
 * @param {string} name - 产品名称
 * @param {File[]} files - 图片文件列表
 * @param {string} spec - 规格
 * @param {string} category - 分类
 * @returns {Promise}
 */
export async function ingestProduct(productCode, name, files, spec = '', category = '') {
  const formData = new FormData()
  formData.append('product_code', productCode)
  formData.append('name', name)
  if (spec) formData.append('spec', spec)
  if (category) formData.append('category', category)
  
  files.forEach(file => {
    formData.append('files', file)
  })
  
  const response = await axios.post(
    `${API_BASE}/product/ingest`,
    formData,
    {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    }
  )
  
  return response.data
}

/**
 * 获取产品列表
 * @param {string} category - 分类筛选
 * @param {number} page - 页码
 * @param {number} pageSize - 每页数量
 * @returns {Promise}
 */
export async function listProducts(category = '', page = 1, pageSize = 20) {
  const params = new URLSearchParams()
  if (category) params.append('category', category)
  params.append('page', page)
  params.append('page_size', pageSize)
  
  const response = await axios.get(`${API_BASE}/products?${params}`)
  return response.data
}

/**
 * 获取产品详情
 * @param {string} productCode - 产品编码
 * @returns {Promise}
 */
export async function getProduct(productCode) {
  const response = await axios.get(`${API_BASE}/product/${productCode}`)
  return response.data
}

/**
 * 删除产品
 * @param {string} productCode - 产品编码
 * @returns {Promise}
 */
export async function deleteProduct(productCode) {
  const response = await axios.delete(`${API_BASE}/product/${productCode}`)
  return response.data
}

/**
 * 获取系统统计
 * @returns {Promise}
 */
export async function getStats() {
  const response = await axios.get(`${API_BASE}/stats`)
  return response.data
}
