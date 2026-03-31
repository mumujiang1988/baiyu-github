import request from '@/utils/request'

/**
 * Push Engine API
 */

/**
 * Get push target list
 * @param {string} moduleCode - Source module code
 * @returns {Promise}
 */
export function getPushTargets(moduleCode) {
  return request({
    url: '/erp/engine/push/targets',
    method: 'get',
    params: { moduleCode }
  })
}

/**
 * Execute push operation
 * @param {Object} data - Push parameters
 * @param {string|number} data.sourceId - Source bill ID
 * @param {string} data.sourceModule - Source module code
 * @param {string} data.targetModule - Target module code
 * @param {Object} data.confirmData - Confirmation data (optional)
 * @returns {Promise}
 */
export function executePushDown(data) {
  return request({
    url: '/erp/engine/push/execute',
    method: 'post',
    data: data
  })
}

/**
 * Preview push data
 * @param {Object} data - Preview parameters
 * @param {string|number} data.sourceId - Source bill ID
 * @param {string} data.sourceModule - Source module code
 * @param {string} data.targetModule - Target module code
 * @returns {Promise}
 */
export function previewPushDown(data) {
  return request({
    url: '/erp/engine/push/preview',
    method: 'post',
    data: data
  })
}

/**
 * Batch push
 * @param {Object} data - Push parameters
 * @param {Array} data.sourceIds - Source bill ID list
 * @param {string} data.sourceModule - Source module code
 * @param {string} data.targetModule - Target module code
 * @returns {Promise}
 */
export function batchPushDown(data) {
  return request({
    url: '/erp/engine/push/batch',
    method: 'post',
    data: data
  })
}

/**
 * Get push mapping configuration
 * @param {Object} params - Query parameters
 * @param {string} params.sourceModule - Source module code
 * @param {string} params.targetModule - Target module code
 * @returns {Promise}
 */
export function getPushMappingConfig(params) {
  return request({
    url: '/erp/engine/push/mapping',
    method: 'get',
    params: params
  })
}

/**
 * Validate push data
 * @param {Object} data - Validation parameters
 * @param {Object} data.sourceData - Source data
 * @param {string} data.sourceModule - Source module code
 * @param {string} data.targetModule - Target module code
 * @returns {Promise}
 */
export function validatePushData(data) {
  return request({
    url: '/erp/engine/push/validate',
    method: 'post',
    data: data
  })
}

/**
 * Cancel push
 * @param {Object} data - Cancel parameters
 * @param {string|number} data.targetId - Target bill ID
 * @param {string} data.targetModule - Target module code
 * @param {string} data.reason - Cancel reason
 * @returns {Promise}
 */
export function cancelPushDown(data) {
  return request({
    url: '/erp/engine/push/cancel',
    method: 'post',
    data: data
  })
}

/**
 * Get push history
 * @param {Object} params - Query parameters
 * @param {string|number} params.billId - Bill ID
 * @param {string} params.moduleCode - Module code
 * @returns {Promise}
 */
export function getPushHistory(params) {
  return request({
    url: '/erp/engine/push/history',
    method: 'get',
    params: params
  })
}
