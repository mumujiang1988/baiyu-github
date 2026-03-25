import request from '@/utils/request'

/**
 * 下推引擎 API
 */

/**
 * 获取可下推的目标列表
 * @param {string} moduleCode - 源模块编码
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
 * 执行下推操作
 * @param {Object} data - 下推参数
 * @param {string|number} data.sourceId - 源单 ID
 * @param {string} data.sourceModule - 源模块编码
 * @param {string} data.targetModule - 目标模块编码
 * @param {Object} data.confirmData - 确认数据（可选）
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
 * 预览下推数据
 * @param {Object} data - 预览参数
 * @param {string|number} data.sourceId - 源单 ID
 * @param {string} data.sourceModule - 源模块编码
 * @param {string} data.targetModule - 目标模块编码
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
 * 批量下推
 * @param {Object} data - 下推参数
 * @param {Array} data.sourceIds - 源单 ID 列表
 * @param {string} data.sourceModule - 源模块编码
 * @param {string} data.targetModule - 目标模块编码
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
 * 获取下推映射配置
 * @param {Object} params - 查询参数
 * @param {string} params.sourceModule - 源模块编码
 * @param {string} params.targetModule - 目标模块编码
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
 * 验证下推数据
 * @param {Object} data - 验证参数
 * @param {Object} data.sourceData - 源单数据
 * @param {string} data.sourceModule - 源模块编码
 * @param {string} data.targetModule - 目标模块编码
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
 * 取消下推
 * @param {Object} data - 取消参数
 * @param {string|number} data.targetId - 目标单 ID
 * @param {string} data.targetModule - 目标模块编码
 * @param {string} data.reason - 取消原因
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
 * 获取下推历史记录
 * @param {Object} params - 查询参数
 * @param {string|number} params.billId - 单据 ID
 * @param {string} params.moduleCode - 模块编码
 * @returns {Promise}
 */
export function getPushHistory(params) {
  return request({
    url: '/erp/engine/push/history',
    method: 'get',
    params: params
  })
}
