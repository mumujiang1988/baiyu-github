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
 * 查询配置详情
 * @param {string|number} id - 配置 ID
 * @returns {Promise}
 */
export function getConfig(id) {
  return request({
    url: `/erp/config/${id}`,
    method: 'get'
  })
}

/**
 * 新增配置
 * @param {Object} data - 配置数据
 * @returns {Promise}
 */
export function addConfig(data) {
  return request({
    url: '/erp/config',  // ✅ 修正：与后端保持一致
    method: 'post',
    data: data
  })
}

/**
 * 修改配置
 * @param {Object} data - 配置数据（包含 configId）
 * @returns {Promise}
 */
export function updateConfig(data) {
  return request({
    url: '/erp/config',  // ✅ 修正：与后端保持一致
    method: 'put',
    data: data
  })
}

/**
 * 保存配置（新增或修改）
 * @param {Object} data - 配置数据
 * @returns {Promise}
 */
export function saveConfig(data) {
  if (data.configId) {
    return updateConfig(data)
  } else {
    return addConfig(data)
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
