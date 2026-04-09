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

/**
 * 批量删除产品
 * @param {string[]} productCodes - 产品编码数组
 * @returns {Promise}
 */
export async function batchDeleteProducts(productCodes) {
  const formData = new FormData()
  formData.append('product_codes', productCodes.join(','))
  
  const response = await axios.delete(`${API_BASE}/products/batch`, {
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
  
  return response.data
}

/**
 * 批量产品入库
 * @param {Object[]} products - 产品信息数组
 * @param {Object} filesMap - 文件映射 {productCode: [fileIndex1, fileIndex2]}
 * @param {File[]} files - 所有图片文件
 * @param {boolean} removeBg - 是否移除背景
 * @returns {Promise}
 */
export async function batchIngestProducts(products, filesMap, files, removeBg = false) {
  const formData = new FormData()
  formData.append('products_json', JSON.stringify(products))
  formData.append('files_map', JSON.stringify(filesMap))
  formData.append('remove_bg', removeBg)
  
  files.forEach(file => {
    formData.append('files', file)
  })
  
  const response = await axios.post(
    `${API_BASE}/products/batch-ingest`,
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
 * 文本关键词搜索
 * @param {string} keyword - 搜索关键词
 * @param {string} category - 分类筛选（可选）
 * @param {number} topK - 返回数量
 * @returns {Promise}
 */
export async function searchByText(keyword, category = '', topK = 10) {
  const formData = new FormData()
  formData.append('keyword', keyword)
  if (category) formData.append('category', category)
  formData.append('top_k', topK)
  
  const response = await axios.post(
    `${API_BASE}/search/text`,
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
 * 图像+文本组合搜索
 * @param {Object} params - 搜索参数
 * @param {File|null} params.file - 查询图片（可选）
 * @param {string} params.keyword - 搜索关键词（可选）
 * @param {string} params.category - 分类筛选（可选）
 * @param {number} params.topK - 返回数量
 * @param {number} params.imageWeight - 图像权重 (0-1)
 * @param {number} params.textWeight - 文本权重 (0-1)
 * @returns {Promise}
 */
export async function hybridSearch({
  file = null,
  keyword = '',
  category = '',
  topK = 10,
  imageWeight = 0.7,
  textWeight = 0.3
}) {
  const formData = new FormData()
  
  if (file) formData.append('file', file)
  if (keyword) formData.append('keyword', keyword)
  if (category) formData.append('category', category)
  formData.append('top_k', topK)
  formData.append('image_weight', imageWeight)
  formData.append('text_weight', textWeight)
  
  const response = await axios.post(
    `${API_BASE}/search/hybrid`,
    formData,
    {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    }
  )
  
  return response.data
}
