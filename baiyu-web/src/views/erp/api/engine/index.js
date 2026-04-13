/**
 * ERP Generic Engine API
 * Applicable to all configurable modules, no need to create separate API files for each module
 * 
 * Usage:
 * import { EngineAPI } from '@/views/erp/api/engine'
 * 
 * // Query sales order list
 * EngineAPI.query('saleOrder', { pageNum: 1, pageSize: 10 })
 * 
 * // Query purchase order list
 * EngineAPI.query('purchaseOrder', { pageNum: 1, pageSize: 10 })
 */

import request from '@/utils/request'

/**
 * Generic Engine API Object
 * Shared by all configurable modules
 */
export const EngineAPI = {
  
  // ==================== Dynamic Query Engine ====================
  
  /**
   * Execute dynamic query (Generic method, applicable to all modules)
   * @param {string} moduleCode - Module code (e.g., saleOrder, purchaseOrder)
   * @param {Object} queryParams - Query parameters
   * @param {number} queryParams.pageNum - Page number
   * @param {number} queryParams.pageSize - Page size
   * @param {Object} queryParams.searchConfig - Search configuration (optional)
   * @returns {Promise}
   */
  query(moduleCode, queryParams = {}) {
    return request({
      url: '/erp/engine/query/execute',
      method: 'post',
      data: {
        moduleCode,
        queryParams,
        searchConfig: queryParams.searchConfig || null
      }
    })
  },
  
  /**
   * Build query conditions (Generic method)
   * @param {string} moduleCode - Module code
   * @param {Object} searchConfig - Search configuration
   * @param {Object} queryParams - Query parameters
   * @returns {Promise}
   */
  buildQuery(moduleCode, searchConfig, queryParams) {
    return request({
      url: '/erp/engine/query/build',
      method: 'post',
      data: {
        moduleCode,
        searchConfig,
        queryParams
      }
    })
  },
  
  /**
   * Get available query types
   * @param {string} moduleCode - Module code (optional)
   * @returns {Promise}
   */
  getQueryTypes(moduleCode = null) {
    return request({
      url: '/erp/engine/query/types',
      method: 'get',
      params: moduleCode ? { moduleCode } : {}
    })
  },
  
  // ==================== Form Validation Engine ====================
  
  /**
   * Execute form validation (Generic method)
   * @param {string} moduleCode - Module code
   * @param {Object} formData - Form data
   * @param {Object} validationConfig - Validation configuration
   * @returns {Promise}
   */
  validate(moduleCode, formData, validationConfig) {
    return request({
      url: '/erp/engine/validation/execute',
      method: 'post',
      data: {
        moduleCode,
        formData,
        validationConfig
      }
    })
  },
  
  /**
   * Batch validation (Generic method)
   * @param {string} moduleCode - Module code
   * @param {Array} formDataList - Form data list
   * @param {Object} validationConfig - Validation configuration
   * @returns {Promise}
   */
  batchValidate(moduleCode, formDataList, validationConfig) {
    return request({
      url: '/erp/engine/validation/batch',
      method: 'post',
      data: {
        moduleCode,
        formDataList,
        validationConfig
      }
    })
  },
  
  /**
   * Get available validation rules
   * @param {string} moduleCode - Module code (optional)
   * @returns {Promise}
   */
  getValidationRules(moduleCode = null) {
    return request({
      url: '/erp/engine/validation/rules',
      method: 'get',
      params: moduleCode ? { moduleCode } : {}
    })
  },
  
  /**
   * Validate single field (Generic method)
   * @param {string} moduleCode - Module code
   * @param {string} field - Field name
   * @param {any} value - Field value
   * @param {Object} rule - Validation rule
   * @returns {Promise}
   */
  validateField(moduleCode, field, value, rule) {
    return request({
      url: '/erp/engine/validation/field',
      method: 'post',
      data: {
        moduleCode,
        field,
        value,
        rule
      }
    })
  },
  
  // ==================== Approval Workflow Engine ====================
  
  /**
   * Get current approval step (Generic method)
   * @param {string} moduleCode - Module code
   * @param {Object} billData - Bill data
   * @returns {Promise}
   */
  getCurrentStep(moduleCode, billData) {
    return request({
      url: '/erp/engine/approval/current-step',
      method: 'post',
      data: {
        moduleCode,
        billData
      }
    })
  },
  
  /**
   * Execute approval operation (Generic method)
   * @param {string} moduleCode - Module code
   * @param {string} billId - Bill ID
   * @param {string} action - Operation type (APPROVE/REJECT)
   * @param {Object} options - Other options
   * @param {string} options.opinion - Approval opinion
   * @param {string} options.userId - User ID
   * @param {Array} options.userRoles - User roles
   * @returns {Promise}
   */
  executeApproval(moduleCode, billId, action, options = {}) {
    return request({
      url: '/erp/engine/approval/execute',
      method: 'post',
      data: {
        moduleCode,
        billId,
        action,
        ...options
      }
    })
  },
  
  /**
   * Get approval history (Generic method)
   * @param {string} moduleCode - Module code
   * @param {string|number} billId - Bill ID
   * @returns {Promise}
   */
  getApprovalHistory(moduleCode, billId) {
    return request({
      url: '/erp/engine/approval/history',
      method: 'post',
      data: {
        moduleCode,
        billId
      }
    })
  },
  
  /**
   * Get workflow definition (Generic method)
   * @param {string} moduleCode - Module code
   * @returns {Promise}
   */
  getWorkflowDefinition(moduleCode) {
    return request({
      url: '/erp/engine/approval/workflow',
      method: 'get',
      params: { moduleCode }
    })
  },
  
  /**
   * Check approval permission (Generic method)
   * @param {string} moduleCode - Module code
   * @param {string} billId - Bill ID
   * @param {string} userId - User ID
   * @param {Array} userRoles - User roles
   * @param {Object} billData - Bill data
   * @returns {Promise}
   */
  checkApprovalPermission(moduleCode, billId, userId, userRoles, billData) {
    return request({
      url: '/erp/engine/approval/check-permission',
      method: 'post',
      data: {
        moduleCode,
        billId,
        userId,
        userRoles,
        billData
      }
    })
  },
  
  /**
   * Transfer approval (Generic method)
   * @param {string} moduleCode - Module code
   * @param {string} billId - Bill ID
   * @param {string} currentUserId - Current user ID
   * @param {string} targetUserId - Target user ID
   * @param {Object} options - Other options
   * @returns {Promise}
   */
  transferApproval(moduleCode, billId, currentUserId, targetUserId, options = {}) {
    return request({
      url: '/erp/engine/approval/transfer',
      method: 'post',
      data: {
        moduleCode,
        billId,
        currentUserId,
        targetUserId,
        ...options
      }
    })
  },
  
  /**
   * Withdraw approval (Generic method)
   * @param {string} moduleCode - Module code
   * @param {string} billId - Bill ID
   * @param {string} userId - User ID
   * @param {Object} options - Other options
   * @returns {Promise}
   */
  withdrawApproval(moduleCode, billId, userId, options = {}) {
    return request({
      url: '/erp/engine/approval/withdraw',
      method: 'post',
      data: {
        moduleCode,
        billId,
        userId,
        ...options
      }
    })
  },
  
  // ==================== Push Engine ====================
  
  /**
   * Get push target list (Generic method)
   * @param {string} moduleCode - Module code
   * @returns {Promise}
   */
  getPushTargets(moduleCode) {
    return request({
      url: '/erp/engine/push/targets',
      method: 'get',
      params: { moduleCode }
    })
  },
  
  /**
   * Execute push operation (Generic method)
   * @param {string} sourceModule - Source module code
   * @param {string} targetModule - Target module code
   * @param {Object} sourceData - Source data
   * @param {Object} options - Other options
   * @returns {Promise}
   */
  executePushDown(sourceModule, targetModule, sourceData, options = {}) {
    return request({
      url: '/erp/engine/push/execute',
      method: 'post',
      data: {
        sourceModule,
        targetModule,
        sourceData,
        ...options
      }
    })
  },
  
  /**
   * Preview push data (Generic method)
   * @param {string} sourceModule - Source module code
   * @param {string} targetModule - Target module code
   * @param {Array} sourceDataList - Source data list
   * @returns {Promise}
   */
  previewPushDown(sourceModule, targetModule, sourceDataList) {
    return request({
      url: '/erp/engine/push/preview',
      method: 'post',
      data: {
        sourceModule,
        targetModule,
        sourceDataList
      }
    })
  },
  
  /**
   * Batch push (Generic method)
   * @param {string} sourceModule - Source module code
   * @param {string} targetModule - Target module code
   * @param {Array} sourceDataList - Source data list
   * @param {Object} options - Other options
   * @returns {Promise}
   */
  batchPushDown(sourceModule, targetModule, sourceDataList, options = {}) {
    return request({
      url: '/erp/engine/push/batch',
      method: 'post',
      data: {
        sourceModule,
        targetModule,
        sourceDataList,
        ...options
      }
    })
  },
  
  /**
   * Get push mapping configuration (Generic method)
   * @param {string} sourceModule - Source module code
   * @param {string} targetModule - Target module code
   * @returns {Promise}
   */
  getPushMapping(sourceModule, targetModule) {
    return request({
      url: '/erp/engine/push/mapping',
      method: 'get',
      params: { sourceModule, targetModule }
    })
  },
  
  /**
   * Validate push data (Generic method)
   * @param {string} sourceModule - Source module code
   * @param {string} targetModule - Target module code
   * @param {Array} sourceDataList - Source data list
   * @returns {Promise}
   */
  validatePushData(sourceModule, targetModule, sourceDataList) {
    return request({
      url: '/erp/engine/push/validate',
      method: 'post',
      data: {
        sourceModule,
        targetModule,
        sourceDataList
      }
    })
  },
  
  /**
   * Cancel push (Generic method)
   * @param {string} targetModule - Target module code
   * @param {string} targetBillId - Target bill ID
   * @param {Object} options - Other options
   * @returns {Promise}
   */
  cancelPushDown(targetModule, targetBillId, options = {}) {
    return request({
      url: '/erp/engine/push/cancel',
      method: 'post',
      data: {
        targetModule,
        targetBillId,
        ...options
      }
    })
  },
  
  /**
   * Get push history (Generic method)
   * @param {string} moduleCode - Module code
   * @returns {Promise}
   */
  getPushHistory(moduleCode) {
    return request({
      url: '/erp/engine/push/history',
      method: 'get',
      params: { moduleCode }
    })
  },
  
  // ==================== Custom Query (Detail/Cost etc.) ====================
  
  /**
   * Get related data (Generic method)
   * Used to query order details, cost accrual and other related data
   * 
   * @param {string} moduleCode - Module code
   * @param {string} relationType - Relation type ('entry'/'cost'/other)
   * @param {string} billNo - Bill number
   * @returns {Promise}
   */
  getRelatedData(moduleCode, relationType, billNo) {
    return request({
      url: `/erp/engine/custom/${relationType}`,
      method: 'get',
      params: {
        moduleCode,
        billNo
      }
    })
  },
  
  /**
   * Get order details (Convenience method)
   * @param {string} moduleCode - Module code
   * @param {string} billNo - Bill number
   * @returns {Promise}
   */
  getEntry(moduleCode, billNo) {
    return this.getRelatedData(moduleCode, 'entry', billNo)
  },
  
  /**
   * Get cost data (Convenience method)
   * @param {string} moduleCode - Module code
   * @param {string} billNo - Bill number
   * @returns {Promise}
   */
  getCost(moduleCode, billNo) {
    return this.getRelatedData(moduleCode, 'cost', billNo)
  },
  
  // ==================== Dictionary Data (Generic) ====================
  
  /**
   * Get dictionary data (Generic method)
   * @param {string} dictType - Dictionary type
   * @param {Object} params - Query parameters
   * @returns {Promise}
   */
  getDictionary(dictType, params = {}) {
    return request({
      url: `/erp/engine/dictionary/${dictType}`,
      method: 'get',
      params
    })
  },
  
  /**
   * Get dictionary list by type (Generic method)
   * @param {string} type - Dictionary type
   * @param {Object} params - Query parameters
   * @returns {Promise}
   */
  getDictionaryByType(type, params = {}) {
    return request({
      url: `/erp/engine/dictionary/listByType/${type}`,
      method: 'get',
      params
    })
  }
}

/**
 * Export default object (compatible with different import methods)
 */
export default EngineAPI
