import request from '@/utils/request'

/**
 * Form Validation Engine API
 */

/**
 * Execute form validation
 * @param {Object} data - Validation parameters
 * @param {Object} data.formData - Form data
 * @param {Object} data.validationConfig - Validation configuration
 * @param {string} data.moduleCode - Module code
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
 * Batch validation
 * @param {Object} data - Validation parameters
 * @param {Array} data.formDataList - Form data list
 * @param {Object} data.validationConfig - Validation configuration
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
 * Get available validation rules
 * @returns {Promise}
 */
export function getAvailableValidationRules() {
  return request({
    url: '/erp/engine/validation/rules',
    method: 'get'
  })
}

/**
 * Validate single field
 * @param {Object} data - Validation parameters
 * @param {string} data.field - Field name
 * @param {any} data.value - Field value
 * @param {Object} data.rule - Validation rule
 * @returns {Promise}
 */
export function validateField(data) {
  return request({
    url: '/erp/engine/validation/field',
    method: 'post',
    data: data
  })
}
