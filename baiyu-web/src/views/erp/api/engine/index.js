/**
 * ERP 通用引擎 API
 * 适用于所有配置化模块，无需为每个模块单独创建 API 文件
 * 
 * 使用方式：
 * import { EngineAPI } from '@/views/erp/api/engine'
 * 
 * // 查询销售订单列表
 * EngineAPI.query('saleOrder', { pageNum: 1, pageSize: 10 })
 * 
 * // 查询采购订单列表
 * EngineAPI.query('purchaseOrder', { pageNum: 1, pageSize: 10 })
 */

import request from '@/utils/request'

/**
 * 通用引擎 API 对象
 * 所有配置化模块共享这一套 API
 */
export const EngineAPI = {
  
  // ==================== 动态查询引擎 ====================
  
  /**
   * 执行动态查询（通用方法，适用于所有模块）
   * @param {string} moduleCode - 模块编码（如 saleOrder, purchaseOrder）
   * @param {Object} queryParams - 查询参数
   * @param {number} queryParams.pageNum - 页码
   * @param {number} queryParams.pageSize - 每页条数
   * @param {Object} queryParams.searchConfig - 搜索配置（可选）
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
   * 构建查询条件（通用方法）
   * @param {string} moduleCode - 模块编码
   * @param {Object} searchConfig - 搜索配置
   * @param {Object} queryParams - 查询参数
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
   * 获取可用的查询类型
   * @param {string} moduleCode - 模块编码（可选）
   * @returns {Promise}
   */
  getQueryTypes(moduleCode = null) {
    return request({
      url: '/erp/engine/query/types',
      method: 'get',
      params: moduleCode ? { moduleCode } : {}
    })
  },
  
  // ==================== 表单验证引擎 ====================
  
  /**
   * 执行表单验证（通用方法）
   * @param {string} moduleCode - 模块编码
   * @param {Object} formData - 表单数据
   * @param {Object} validationConfig - 验证配置
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
   * 批量验证（通用方法）
   * @param {string} moduleCode - 模块编码
   * @param {Array} formDataList - 表单数据列表
   * @param {Object} validationConfig - 验证配置
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
   * 获取可用的验证规则
   * @param {string} moduleCode - 模块编码（可选）
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
   * 验证单个字段（通用方法）
   * @param {string} moduleCode - 模块编码
   * @param {string} field - 字段名
   * @param {any} value - 字段值
   * @param {Object} rule - 验证规则
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
  
  // ==================== 审批流程引擎 ====================
  
  /**
   * 获取当前审批步骤（通用方法）
   * @param {string} moduleCode - 模块编码
   * @param {Object} billData - 单据数据
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
   * 执行审批操作（通用方法）
   * @param {string} moduleCode - 模块编码
   * @param {string} billId - 单据 ID
   * @param {string} action - 操作类型（APPROVE/REJECT）
   * @param {Object} options - 其他选项
   * @param {string} options.opinion - 审批意见
   * @param {string} options.userId - 用户 ID
   * @param {Array} options.userRoles - 用户角色
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
   * 获取审批历史（通用方法）
   * @param {string} moduleCode - 模块编码
   * @param {string|number} billId - 单据 ID
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
   * 获取审批流程定义（通用方法）
   * @param {string} moduleCode - 模块编码
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
   * 检查审批权限（通用方法）
   * @param {string} moduleCode - 模块编码
   * @param {string} billId - 单据 ID
   * @param {string} userId - 用户 ID
   * @param {Array} userRoles - 用户角色
   * @param {Object} billData - 单据数据
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
   * 转审操作（通用方法）
   * @param {string} moduleCode - 模块编码
   * @param {string} billId - 单据 ID
   * @param {string} currentUserId - 当前用户 ID
   * @param {string} targetUserId - 目标用户 ID
   * @param {Object} options - 其他选项
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
   * 撤回审批（通用方法）
   * @param {string} moduleCode - 模块编码
   * @param {string} billId - 单据 ID
   * @param {string} userId - 用户 ID
   * @param {Object} options - 其他选项
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
  
  // ==================== 下推引擎 ====================
  
  /**
   * 获取可下推的目标列表（通用方法）
   * @param {string} moduleCode - 模块编码
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
   * 执行下推操作（通用方法）
   * @param {string} sourceModule - 源模块编码
   * @param {string} targetModule - 目标模块编码
   * @param {Object} sourceData - 源数据
   * @param {Object} options - 其他选项
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
   * 预览下推数据（通用方法）
   * @param {string} sourceModule - 源模块编码
   * @param {string} targetModule - 目标模块编码
   * @param {Array} sourceDataList - 源数据列表
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
   * 批量下推（通用方法）
   * @param {string} sourceModule - 源模块编码
   * @param {string} targetModule - 目标模块编码
   * @param {Array} sourceDataList - 源数据列表
   * @param {Object} options - 其他选项
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
   * 获取下推映射配置（通用方法）
   * @param {string} sourceModule - 源模块编码
   * @param {string} targetModule - 目标模块编码
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
   * 验证下推数据（通用方法）
   * @param {string} sourceModule - 源模块编码
   * @param {string} targetModule - 目标模块编码
   * @param {Array} sourceDataList - 源数据列表
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
   * 取消下推（通用方法）
   * @param {string} targetModule - 目标模块编码
   * @param {string} targetBillId - 目标单据 ID
   * @param {Object} options - 其他选项
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
   * 获取下推历史（通用方法）
   * @param {string} moduleCode - 模块编码
   * @returns {Promise}
   */
  getPushHistory(moduleCode) {
    return request({
      url: '/erp/engine/push/history',
      method: 'get',
      params: { moduleCode }
    })
  },
  
  // ==================== 自定义查询（明细/成本等） ====================
  
  /**
   * 获取关联数据（通用方法）
   * 用于查询订单明细、成本暂估等关联数据
   * 
   * @param {string} moduleCode - 模块编码
   * @param {string} relationType - 关联类型（'entry'/'cost'/其他）
   * @param {string} billNo - 单据编号
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
   * 获取订单明细（便捷方法）
   * @param {string} moduleCode - 模块编码
   * @param {string} billNo - 单据编号
   * @returns {Promise}
   */
  getEntry(moduleCode, billNo) {
    return this.getRelatedData(moduleCode, 'entry', billNo)
  },
  
  /**
   * 获取成本数据（便捷方法）
   * @param {string} moduleCode - 模块编码
   * @param {string} billNo - 单据编号
   * @returns {Promise}
   */
  getCost(moduleCode, billNo) {
    return this.getRelatedData(moduleCode, 'cost', billNo)
  },
  
  // ==================== 字典数据（通用） ====================
  
  /**
   * 获取字典数据（通用方法）
   * @param {string} dictType - 字典类型
   * @param {Object} params - 查询参数
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
   * 按类型获取字典列表（通用方法）
   * @param {string} type - 字典类型
   * @param {Object} params - 查询参数
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
 * 导出默认对象（兼容不同导入方式）
 */
export default EngineAPI
