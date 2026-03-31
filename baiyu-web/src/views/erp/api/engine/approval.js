import request from '@/utils/request'

/**
 * Approval Workflow Engine API
 */

/**
 * Get current approval step
 * @param {Object} data - Parameters
 * @param {string} data.moduleCode - Module code
 * @param {string|number} data.billId - Bill ID
 * @param {Object} data.billData - Bill data
 * @returns {Promise}
 */
export function getCurrentApprovalStep(data) {
  return request({
    url: '/erp/engine/approval/current-step',
    method: 'post',
    data: data
  })
}

/**
 * Execute approval operation
 * @param {Object} data - Approval parameters
 * @param {string|number} data.billId - Bill ID
 * @param {string} data.moduleCode - Module code
 * @param {string} data.action - Approval action (AUDIT/REJECT/TRANSFER)
 * @param {string} data.opinion - Approval opinion
 * @param {number} data.step - Approval step
 * @returns {Promise}
 */
export function executeApproval(data) {
  return request({
    url: '/erp/engine/approval/execute',
    method: 'post',
    data: data
  })
}

/**
 * Check user approval permission
 * @param {Object} data - Parameters
 * @param {string|number} data.billId - Bill ID
 * @param {string} data.moduleCode - Module code
 * @param {string} data.userId - User ID
 * @param {Array} data.userRoles - User roles list
 * @returns {Promise}
 */
export function checkApprovalPermission(data) {
  return request({
    url: '/erp/engine/approval/check-permission',
    method: 'post',
    data: data
  })
}

/**
 * Get approval history
 * @param {Object} params - Query parameters
 * @param {string|number} params.billId - Bill ID
 * @param {string} params.moduleCode - Module code
 * @returns {Promise}
 */
export function getApprovalHistory(params) {
  return request({
    url: '/erp/engine/approval/history',
    method: 'get',
    params: params
  })
}

/**
 * Get workflow definition
 * @param {string} moduleCode - Module code
 * @returns {Promise}
 */
export function getWorkflowDefinition(moduleCode) {
  return request({
    url: '/erp/engine/approval/workflow',
    method: 'get',
    params: { moduleCode }
  })
}

/**
 * Transfer approval
 * @param {Object} data - Transfer parameters
 * @param {string|number} data.billId - Bill ID
 * @param {string} data.moduleCode - Module code
 * @param {string} data.targetUserId - Target user ID
 * @param {string} data.reason - Transfer reason
 * @returns {Promise}
 */
export function transferApproval(data) {
  return request({
    url: '/erp/engine/approval/transfer',
    method: 'post',
    data: data
  })
}

/**
 * Withdraw approval
 * @param {Object} data - Withdraw parameters
 * @param {string|number} data.billId - Bill ID
 * @param {string} data.moduleCode - Module code
 * @returns {Promise}
 */
export function withdrawApproval(data) {
  return request({
    url: '/erp/engine/approval/withdraw',
    method: 'post',
    data: data
  })
}
