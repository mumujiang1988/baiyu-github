import request from '@/utils/request'

/**
 * 获取采购价目表分页列表
 * @param {Object} queryParams 查询参数
 * @returns {Promise}
 */
export function priceList(queryParams) {
  return request({
    url: '/k3/pricelist/list',
    method: 'get',
    params: queryParams
  })
}

/**
 * 获取采购价目详情
 * @param {String} id 采购价目表FNumber
 * @returns {Promise}
 */
export function procurementdetails(id) {
  return request({
    url: '/k3/pricelist/query',
    method: 'get',
    params: { id }
  })
}

/**
 * 删除采购价目表
 * @param {Array} number 供应商ID数组
 * @returns
 */
export function procurementdelete(ids) {
  return request({
    url: '/k3/pricelist/delete/' +ids,
    method: 'delete'
  })
}

/**
 * 获取币别
 */
export function gettlementCurrency() {
  return request({
    url: '/api/v1/kingdee/currency',
    method: 'get'
  })
}

/**
 * 获取价格类型
 */
export function getpricetype() {
  return request({
    url: '/api/v1/kingdee/pricetype',
    method: 'get'
  })
}

/**
 * 供应商
* */
export function getSupplierList() {
  return request({
    url: '/api/v1/kingdee/supplier',
    method: 'get'
  })
}

/**
 * 物料
 * */
export function getMaterialList() {
  return request({
    url: '/api/v1/kingdee/material',
    method: 'get'
  })
}

/**
 * 计价单位
 * */
export function getPricingUnit() {
  return request({
    url: '/api/v1/kingdee/pricingunit',
    method: 'get'
  })
}

/**
 * 新增采购价目（支持多文件上传）
 * @param {Object} data 采购价目数据，格式：{ ...普通字段, ftp1File: File[] }
 * @returns {Promise}
 */
export function addProcurement(data) {
  const formData = new FormData()
  // 分离文件字段和普通字段
  const { ftp1File, ...customerData } = data

  // 将普通字段转换为 JSON 并添加到 FormData
  const customerBlob = new Blob([JSON.stringify(customerData)], { type: 'application/json' })
  formData.append('priceList', customerBlob)

  // 添加文件（如果有），使用相同字段名，后端可通过 List<MultipartFile> 接收
  if (ftp1File && Array.isArray(ftp1File)) {
    ftp1File.forEach(file => {
      formData.append('ftp1File', file)
    })
  }

  return request({
    url: '/k3/pricelist/save',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 修改采购价目（支持多文件上传）
 * @param {Object} data 采购价目数据，格式：{ ...普通字段, ftp1File: File[] }
 * @returns {Promise}
 */
export function updateProcurement(data) {
  const formData = new FormData()
  const { ftp1File, ...customerData } = data

  const customerBlob = new Blob([JSON.stringify(customerData)], { type: 'application/json' })
  formData.append('priceList', customerBlob)

  if (ftp1File && Array.isArray(ftp1File)) {
    ftp1File.forEach(file => {
      formData.append('ftp1File', file)
    })
  }

  return request({
    url: '/k3/pricelist/update',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * （支持多文件上传）
 * @param {Object} data 采购价目数据，格式：{ ...普通字段, ftp1File: File[] }
 * @returns {Promise}
 */

export function addupdateProcurementPriceAdjustment(data) {
/*  const formData = new FormData()
  // 分离文件字段和普通字段
  const { ftp1File, ...customerData } = data

  // 将普通字段转换为 JSON 并添加到 FormData
  const customerBlob = new Blob([JSON.stringify(customerData)], { type: 'application/json' })
  formData.append('priceList', customerBlob)*/

  /*// 添加文件（如果有），使用相同字段名，后端可通过 List<MultipartFile> 接收
  if (ftp1File && Array.isArray(ftp1File)) {
    ftp1File.forEach(file => {
      formData.append('ftp1File', file)
    })
  }*/

  return request({
   /* url: '/k3/pricelist/save',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }*/
  })
}





