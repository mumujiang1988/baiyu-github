import request from '@/utils/request'

/**
 * Query configuration list
 * @param {Object} query - Query parameters
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
 * Query configuration details (supports both moduleCode and configId methods)
 * @param {string|number} id - Configuration ID or moduleCode
 * @param {string} configType - Configuration type (optional, required when using moduleCode)
 * @returns {Promise}
 */
export function getConfig(id, configType) {
  // If numeric or convertible to numeric string, use ID query
  const numericId = typeof id === 'number' ? id : Number(id)
  if (!isNaN(numericId)) {
    return request({
      url: `/erp/config/${numericId}`,
      method: 'get'
    })
  }
  // Otherwise, use moduleCode + configType query
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
 * Save configuration (low-code universal interface - intelligently determines add or update)
 * @param {Object} data - Configuration data
 * @returns {Promise}
 */
export function saveConfig(data) {
  // Determine whether to use POST or PUT based on configId
  if (data.configId) {
    // Update operation, use PUT method
    return request({
      url: '/erp/config',
      method: 'put',
      data: data
    })
  } else {
    // Add operation, use POST method
    return request({
      url: '/erp/config',
      method: 'post',
      data: data
    })
  }
}

/**
 * Delete configuration
 * @param {string|number} id - Configuration ID
 * @returns {Promise}
 */
export function delConfig(id) {
  return request({
    url: `/erp/config/${id}`,
    method: 'delete'
  })
}

/**
 * Batch delete configurations
 * @param {Array} ids - Configuration ID array
 * @returns {Promise}
 */
export function batchDelConfig(ids) {
  return request({
    url: '/erp/config/batch',        // Use /batch, consistent with backend
    method: 'delete',                // Use DELETE, consistent with backend
    data: ids
  })
}

/**
 * Query configuration history versions
 * @param {string|number} configId - Configuration ID
 * @returns {Promise}
 */
export function getConfigHistory(configId) {
  return request({
    url: `/erp/config/history/${configId}`,
    method: 'get'
  })
}

/**
 * View version details
 * @param {string|number} configId - Configuration ID
 * @param {number} version - Version number
 * @returns {Promise}
 */
export function getVersionDetail(configId, version) {
  return request({
    url: `/erp/config/history/${configId}/${version}`,
    method: 'get'
  })
}

/**
 * Rollback to specified version
 * @param {Object} data - Rollback parameters
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
 * Export configuration
 * @param {string|number} id - Configuration ID
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
 * Import configuration
 * @param {FormData} data - Import data (contains file)
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
 * Copy configuration
 * @param {string|number} id - Configuration ID
 * @returns {Promise}
 */
export function copyConfig(id) {
  return request({
    url: `/erp/config/${id}/copy`,
    method: 'post'
  })
}

/**
 * Get configuration template list
 * @param {string} type - Configuration type
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
 * Get configuration template content
 * @param {string} templateId - Template ID
 * @returns {Promise}
 */
export function getTemplateContent(templateId) {
  return request({
    url: `/erp/config/templates/${templateId}`,
    method: 'get'
  })
}

/**
 * Update configuration status
 * @param {Object} data - Status data (configId, status)
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
 * Validate configuration content
 * @param {Object} data - Validation data (configType, configContent)
 * @returns {Promise}
 */
export function validateConfigContent(data) {
  return request({
    url: '/erp/config/validate',
    method: 'post',
    data: data
  })
}
