import request from '@/utils/request'

/**
 * 表单验证引擎 API
 */

/**
 * 执行表单验证
 * @param {Object} data - 验证参数
 * @param {Object} data.formData - 表单数据
 * @param {Object} data.validationConfig - 验证配置
 * @param {string} data.moduleCode - 模块编码
 * @returns {Promise}
 */
export function executeValidation(data) {
  return request({
    url: '/erp/engine/validation/execute',
    method: 'post',
    data: data
  })
}

/**
 * 批量验证
 * @param {Object} data - 验证参数
 * @param {Array} data.formDataList - 表单数据列表
 * @param {Object} data.validationConfig - 验证配置
 * @returns {Promise}
 */
export function batchValidate(data) {
  return request({
    url: '/erp/engine/validation/batch',
    method: 'post',
    data: data
  })
}

/**
 * 获取可用的验证规则
 * @returns {Promise}
 */
export function getAvailableValidationRules() {
  return request({
    url: '/erp/engine/validation/rules',
    method: 'get'
  })
}

/**
 * 验证单个字段
 * @param {Object} data - 验证参数
 * @param {string} data.field - 字段名
 * @param {any} data.value - 字段值
 * @param {Object} data.rule - 验证规则
 * @returns {Promise}
 */
export function validateField(data) {
  return request({
    url: '/erp/engine/validation/field',
    method: 'post',
    data: data
  })
}
