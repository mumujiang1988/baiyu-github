import request from '@/utils/request'

/**
 * 审批流程引擎 API
 */

/**
 * 获取当前审批步骤
 * @param {Object} data - 参数
 * @param {string} data.moduleCode - 模块编码
 * @param {string|number} data.billId - 单据 ID
 * @param {Object} data.billData - 单据数据
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
 * 执行审批操作
 * @param {Object} data - 审批参数
 * @param {string|number} data.billId - 单据 ID
 * @param {string} data.moduleCode - 模块编码
 * @param {string} data.action - 审批动作 (AUDIT/REJECT/TRANSFER)
 * @param {string} data.opinion - 审批意见
 * @param {number} data.step - 审批步骤
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
 * 检查用户是否有审批权限
 * @param {Object} data - 参数
 * @param {string|number} data.billId - 单据 ID
 * @param {string} data.moduleCode - 模块编码
 * @param {string} data.userId - 用户 ID
 * @param {Array} data.userRoles - 用户角色列表
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
 * 获取审批历史
 * @param {Object} params - 查询参数
 * @param {string|number} params.billId - 单据 ID
 * @param {string} params.moduleCode - 模块编码
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
 * 获取审批流程定义
 * @param {string} moduleCode - 模块编码
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
 * 转审操作
 * @param {Object} data - 转审参数
 * @param {string|number} data.billId - 单据 ID
 * @param {string} data.moduleCode - 模块编码
 * @param {string} data.targetUserId - 目标用户 ID
 * @param {string} data.reason - 转审原因
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
 * 撤回审批
 * @param {Object} data - 撤回参数
 * @param {string|number} data.billId - 单据 ID
 * @param {string} data.moduleCode - 模块编码
 * @returns {Promise}
 */
export function withdrawApproval(data) {
  return request({
    url: '/erp/engine/approval/withdraw',
    method: 'post',
    data: data
  })
}
