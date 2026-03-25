import request from '@/utils/request'

/**
 * 通用文件添加函数
 * @param {FormData} formData FormData 对象
 * @param {File} file 文件对象
 * @param {string} fieldName 后端接收的字段名
 */
function appendFileToFormData(formData, file, fieldName) {
  if (file && file instanceof File) {
    formData.append(fieldName, file)
  }
}

/**
 * 获取客户分页列表
 * @param {Object} queryParams 查询参数
 * @returns {Promise}
 */
export function listCustomer(queryParams) {
  return request({
    url: '/k3/customer/list',
    method: 'get',
    params: queryParams
  })
}

/**
 * 新增客户
 * @param {Object} data 客户数据（包含文件字段 fKhlogoFile, fZmmttpFile, fcmmttpFile）
 * @returns {Promise}
 */
export function addCustomer(data) {
  const formData = new FormData()

  // 分离文件字段和普通字段
  const { fKhlogoFile, fZmmttpFile, fZmmttpsFile, fcmmttpFile, fcmmttpsFile, ...customerData } = data

  // 将普通字段转换为 JSON 并添加到 FormData
  const customerBlob = new Blob([JSON.stringify(data)], {type: 'application/json'});
  formData.append('customer', customerBlob)

  // 添加文件（字段名与后端控制器参数名一致）
  appendFileToFormData(formData, fKhlogoFile, 'logoFile')
  appendFileToFormData(formData, fZmmttpFile, 'zmmttpFile')
  appendFileToFormData(formData, fZmmttpsFile, 'zmmttpsFile')
  appendFileToFormData(formData, fcmmttpFile, 'cmmttpFile')
  appendFileToFormData(formData, fcmmttpsFile, 'cmmttpsFile')

  // 处理客户转让或其他附件的文件（预留）
  if (customerData.customerVisitRecord && customerData.customerVisitRecord.length > 0) {
    const visitRecordWithFile = customerData.customerVisitRecord.find(
      record => record.attachmentFile && record.attachmentFile instanceof File
    )
    if (visitRecordWithFile && visitRecordWithFile.attachmentFile) {
      formData.append('visitFile', visitRecordWithFile.attachmentFile)
    }
  }

  return request({
    url: '/k3/customer/save',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 修改客户
 * @param {Object} data 客户数据（包含文件字段 fKhlogoFile, fZmmttpFile, fcmmttpFile）
 * @returns {Promise}
 */
export function updateCustomer(data) {
  const formData = new FormData()

  // 分离文件字段和普通字段
  const { fKhlogoFile, fZmmttpFile, fZmmttpsFile, fcmmttpFile, fcmmttpsFile, ...customerData } = data

  // 将普通字段转换为 JSON 并添加到 FormData
  const customerBlob = new Blob([JSON.stringify(data)], {type: 'application/json'});
  formData.append('customer', customerBlob)

  // 添加文件（字段名与后端控制器参数名一致）
  // 添加文件（字段名与后端控制器参数名一致）
  appendFileToFormData(formData, fKhlogoFile, 'logoFile')
  appendFileToFormData(formData, fZmmttpFile, 'zmmttpFile')
  appendFileToFormData(formData, fZmmttpsFile, 'zmmttpsFile')
  appendFileToFormData(formData, fcmmttpFile, 'cmmttpFile')
  appendFileToFormData(formData, fcmmttpsFile, 'cmmttpsFile')

  // 处理客户转让或其他附件的文件（预留）
  if (customerData.customerVisitRecord && customerData.customerVisitRecord.length > 0) {
    const visitRecordWithFile = customerData.customerVisitRecord.find(
      record => record.attachmentFile && record.attachmentFile instanceof File
    )
    if (visitRecordWithFile && visitRecordWithFile.attachmentFile) {
      formData.append('visitFile', visitRecordWithFile.attachmentFile)
    }
  }

  return request({
    url: '/k3/customer/update',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 删除客户
 * @param {Array|String} id 客户ID或ID数组
 * @returns {Promise}
 */
export function deleteCustomer(idsToDelete) {
  const id = Array.isArray(idsToDelete) ? idsToDelete.join(',') : idsToDelete
  return request({
    url: `/k3/customer/delete/${id}`,
    method: 'delete'
  })
}

/**
 * 获取客户详情
 * @param {String} id 客户ID
 * @returns {Promise}
 */
export function getCustomer(id) {
  return request({
    url: '/k3/customer/query',
    method: 'get',
    params: { id }
  })
}

/**
 * 获取图片预览地址
 * @param {String} imageUrl 图片地址
 * @returns {String}
 */
export function getImagePreviewUrl(imageUrl) {
  if (!imageUrl) return ''
  if (imageUrl.startsWith('http') || imageUrl.startsWith('https')) {
    return imageUrl
  }
  const baseUrl = import.meta.env.VITE_APP_BASE_API || ''
  return `${baseUrl}${imageUrl}`
}

/**
 * 获取国家列表
 * @returns {Promise}
 */
export function getNation() {
  return request({
    url: '/api/v1/kingdee/getnAtion',
    method: 'get'
  })
}

/**
 * 获取客户来源列表
 * @returns {Promise}
 */
export function getCustomerSources() {
  return request({
    url: '/api/v1/kingdee/customer/all',
    method: 'get'
  })
}

/**
 * 获取结算币别列表
 */
export function gettlementCurrency() {
  return request({
    url: '/api/v1/kingdee/currency',
    method: 'get'
  })
}

/** 获取结算方式列表 */
export function settlementMethod() {
  return request({
    url: '/api/v1/kingdee/lementMethod/all',
    method: 'get'
  })
}

/** 收款条件 */
export function paymentterms() {
  return request({
    url: '/api/v1/kingdee/payment',
    method: 'get'
  })
}

/** 销售员 */
export function salesperson() {
  return request({
    url: '/api/v1/kingdee/user/sales',
    method: 'get'
  })
}

/** 客户分组 */
export function awaitgroupId() {
  return request({
    url: '/api/v1/kingdee/groupId',
    method: 'get'
  })
}

/** 包装方式 */
export function awaitpackaging(){
  return request({
    url: '/api/v1/kingdee/customer/packaging',
    method: 'get'
  })
}

/** 客户类别 */
export function awaitClient(){
  return request({
    url: '/api/v1/kingdee/client',
    method: 'get'
  })
}
 