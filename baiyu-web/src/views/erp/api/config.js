import request from '@/utils/request'

/**
 * 查询配置列表
 * @param {Object} query - 查询参数
 * @returns {Promise}
 */
export function listConfig(query) {
  return request({
    url: '/erp/config/list',
    method: 'get',
    params: query
  })
}

/**
 * 查询配置详情（支持 moduleCode 和 configId 两种方式）
 * @param {string|number} id - 配置 ID 或 moduleCode
 * @param {string} configType - 配置类型（可选，当使用 moduleCode 时需要）
 * @returns {Promise}
 */
export function getConfig(id, configType) {
  // ✅ 如果是数字或可以转换为数字的字符串，使用 ID 查询
  const numericId = typeof id === 'number' ? id : Number(id)
  if (!isNaN(numericId)) {
    return request({
      url: `/erp/config/${numericId}`,
      method: 'get'
    })
  }
  // 否则，使用 moduleCode + configType 查询
  else {
    const params = configType ? { moduleCode: id, configType } : { moduleCode: id }
    return request({
      url: '/erp/config/get',
      method: 'get',
      params: params
    })
  }
}

/**
 * 保存配置（低代码通用接口 - 智能判断新增或修改）
 * @param {Object} data - 配置数据
 * @returns {Promise}
 */
export function saveConfig(data) {
  // ✅ 根据是否有 configId 判断使用 POST 还是 PUT
  if (data.configId) {
    // ✅ 修改操作，使用 PUT 方法
    return request({
      url: '/erp/config',
      method: 'put',
      data: data
    })
  } else {
    // ✅ 新增操作，使用 POST 方法
    return request({
      url: '/erp/config',
      method: 'post',
      data: data
    })
  }
}

/**
 * 删除配置
 * @param {string|number} id - 配置 ID
 * @returns {Promise}
 */
export function delConfig(id) {
  return request({
    url: `/erp/config/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除配置
 * @param {Array} ids - 配置 ID 数组
 * @returns {Promise}
 */
export function batchDelConfig(ids) {
  return request({
    url: '/erp/config/batchDelete',
    method: 'post',
    data: ids
  })
}

/**
 * 查询配置历史版本
 * @param {string|number} configId - 配置 ID
 * @returns {Promise}
 */
export function getConfigHistory(configId) {
  return request({
    url: `/erp/config/history/${configId}`,
    method: 'get'
  })
}

/**
 * 查看版本详情
 * @param {string|number} configId - 配置 ID
 * @param {number} version - 版本号
 * @returns {Promise}
 */
export function getVersionDetail(configId, version) {
  return request({
    url: `/erp/config/history/${configId}/${version}`,
    method: 'get'
  })
}

/**
 * 回滚到指定版本
 * @param {Object} data - 回滚参数
 * @returns {Promise}
 */
export function rollbackToVersion(data) {
  return request({
    url: '/erp/config/rollback',
    method: 'post',
    data: data
  })
}

/**
 * 导出配置
 * @param {string|number} id - 配置 ID
 * @returns {Promise}
 */
export function exportConfig(id) {
  return request({
    url: `/erp/config/${id}/export`,
    method: 'get',
    responseType: 'blob'
  })
}

/**
 * 导入配置
 * @param {FormData} data - 导入数据（包含文件）
 * @returns {Promise}
 */
export function importConfig(data) {
  return request({
    url: '/erp/config/import',
    method: 'post',
    data: data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 复制配置
 * @param {string|number} id - 配置 ID
 * @returns {Promise}
 */
export function copyConfig(id) {
  return request({
    url: `/erp/config/${id}/copy`,
    method: 'post'
  })
}

/**
 * 获取配置模板列表
 * @param {string} type - 配置类型
 * @returns {Promise}
 */
export function getConfigTemplates(type) {
  return request({
    url: '/erp/config/templates',
    method: 'get',
    params: { type }
  })
}

/**
 * 获取配置模板内容
 * @param {string} templateId - 模板 ID
 * @returns {Promise}
 */
export function getTemplateContent(templateId) {
  return request({
    url: `/erp/config/templates/${templateId}`,
    method: 'get'
  })
}

/**
 * 更新配置状态
 * @param {Object} data - 状态数据（configId, status）
 * @returns {Promise}
 */
export function updateConfigStatus(data) {
  return request({
    url: '/erp/config/status',
    method: 'put',
    data: data
  })
}

/**
 * 验证配置内容
 * @param {Object} data - 验证数据（configType, configContent）
 * @returns {Promise}
 */
export function validateConfigContent(data) {
  return request({
    url: '/erp/config/validate',
    method: 'post',
    data: data
  })
}
