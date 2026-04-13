import axios from 'axios'

// 创建 axios 实例，配置默认超时时间
const apiClient = axios.create({
  timeout: 60000, // 60秒超时（数据一致性检查可能需要较长时间）
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
apiClient.interceptors.request.use(
  config => {
    // 可以在这里添加 token 等认证信息
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
apiClient.interceptors.response.use(
  response => {
    return response
  },
  error => {
    // 统一处理错误
    if (error.code === 'ECONNABORTED') {
      error.message = '请求超时，请稍后重试'
    } else if (!error.response) {
      error.message = '网络连接失败，请检查网络或后端服务'
    }
    return Promise.reject(error)
  }
)

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
  
  const response = await apiClient.post(
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
 * @param {Function} onProgress - 上传进度回调 (progressEvent) => void
 * @returns {Promise}
 */
export async function ingestProduct(productCode, name, files, spec = '', category = '', onProgress = null, batchId = null) {
  const formData = new FormData()
  formData.append('product_code', productCode)
  formData.append('name', name || '')  // 允许空名称
  if (spec) formData.append('spec', spec)
  if (category) formData.append('category', category)
  if (batchId) formData.append('batch_id', batchId)
  
  files.forEach(file => {
    formData.append('files', file)
  })
  
  const config = {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  }
  
  // 如果提供了进度回调，添加到配置中
  if (onProgress && typeof onProgress === 'function') {
    config.onUploadProgress = onProgress
  }
  
  const response = await apiClient.post(
    `${API_BASE}/product/ingest`,
    formData,
    config
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
  
  const response = await apiClient.get(`${API_BASE}/products?${params}`)
  return response.data
}

/**
 * 获取产品详情
 * @param {string} productCode - 产品编码
 * @returns {Promise}
 */
export async function getProduct(productCode) {
  const response = await apiClient.get(`${API_BASE}/product/${productCode}`)
  return response.data
}

/**
 * 更新产品信息
 * @param {string} productCode - 产品编码
 * @param {Object} updates - 要更新的字段 { name?, spec?, category? }
 * @returns {Promise}
 */
export async function updateProduct(productCode, updates = {}) {
  const formData = new FormData()
  
  if (updates.name !== undefined) {
    formData.append('name', updates.name)
  }
  if (updates.spec !== undefined) {
    formData.append('spec', updates.spec)
  }
  if (updates.category !== undefined) {
    formData.append('category', updates.category)
  }
  
  const response = await apiClient.put(
    `${API_BASE}/product/${productCode}`,
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
 * 删除产品
 * @param {string} productCode - 产品编码
 * @returns {Promise}
 */
export async function deleteProduct(productCode) {
  const response = await apiClient.delete(`${API_BASE}/product/${productCode}`)
  return response.data
}

/**
 * 获取系统统计
 * @deprecated 未使用，已移除 (2026-04-09)
 */
// export async function getStats() { ... } // 已删除

/**
 * 检查数据一致性
 * @returns {Promise}
 */
export async function checkDataConsistency() {
  const response = await apiClient.get(`${API_BASE}/products/data-consistency`)
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
  
  const response = await apiClient.delete(`${API_BASE}/products/batch`, {
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
  
  return response.data
}

/**
 * 批量产品入库
 * @deprecated 未使用，当前使用逐个入库方式 (2026-04-09)
 */
// export async function batchIngestProducts(...) { ... } // 已删除

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
  
  const response = await apiClient.post(
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
 * 清理失败产品的残留数据
 * @param {string} productCode - 产品编码
 * @returns {Promise}
 */
export async function cleanupFailedProduct(productCode) {
  const response = await apiClient.post(`${API_BASE}/product/${productCode}/cleanup-failed`)
  return response.data
}

/**
 * 查询孤儿数据
 * @returns {Promise}
 */
export async function queryOrphanData() {
  const response = await apiClient.get(`${API_BASE}/products/orphan-data`)
  return response.data
}

/**
 * 清理所有孤儿数据（两步确认）
 * @param {boolean} confirm - 是否确认执行清理
 * @returns {Promise}
 */
export async function cleanOrphanData(confirm = false) {
  const formData = new FormData()
  formData.append('confirm', confirm)
  
  const response = await apiClient.post(`${API_BASE}/products/clean-orphans`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
  return response.data
}

/**
 * 删除单个孤儿数据
 * @param {string} type - 孤儿类型 (mysql, milvus, minio)
 * @param {string} identifier - 标识符
 * @returns {Promise}
 */
export async function deleteSingleOrphan(type, identifier) {
  // 对于 MinIO，identifier 可能包含 /，需要编码
  const encodedIdentifier = encodeURIComponent(identifier)
  const response = await apiClient.delete(`${API_BASE}/products/orphan/${type}/${encodedIdentifier}`)
  return response.data
}

/**
 * 重试产品入库（清除现有数据并允许重新上传）
 * @param {string} productCode - 产品编码
 * @returns {Promise}
 */
export async function retryProductIngest(productCode) {
  const response = await apiClient.post(`${API_BASE}/products/${productCode}/retry`)
  return response.data
}

/**
 * 获取所有已入库的产品编码列表
 * @returns {Promise<{success: boolean, product_codes: string[], count: number}>}
 */
export async function getAllProductCodes() {
  const response = await apiClient.get(`${API_BASE}/products/codes`)
  return response.data
}
