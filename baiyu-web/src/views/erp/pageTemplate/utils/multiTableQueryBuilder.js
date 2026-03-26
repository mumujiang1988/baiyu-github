/**
 * 多表格查询构建器 - 支持单个页面多个表格的独立查询
 * @module views/erp/pageTemplate/utils/multiTableQueryBuilder
 * @description 为每个子表格生成独立的 queryConfig，支持不同的表名和查询条件
 */

import request from '@/utils/request'

/**
 * 主表格查询（标准构建器模式）
 * @param {Object} params - 查询参数
 * @returns {Promise} - 查询结果
 */
export const queryMainTable = async (params) => {
  const { moduleCode, tableName, queryConfig, pageNum = 1, pageSize = 10 } = params
  
  try {
    const response = await request({
      url: '/erp/engine/query/execute',
      method: 'post',
      data: {
        moduleCode,
        tableName,
        queryConfig,
        pageNum,
        pageSize
      }
    })
    
    console.log(' 主表格查询结果:', response)
    return response
  } catch (error) {
    console.error(' 主表格查询失败:', error)
    throw error
  }
}

/**
 * 子表格查询（通用方法）
 * @param {Object} params - 查询参数
 * @returns {Promise} - 查询结果
 */
export const querySubTable = async (params) => {
  const { moduleCode, tableName, queryConfig, pageNum = 1, pageSize = 100 } = params
  
  try {
    const response = await request({
      url: '/erp/engine/query/execute',
      method: 'post',
      data: {
        moduleCode,
        tableName,
        queryConfig,
        pageNum,
        pageSize
      }
    })
    
    console.log(` 子表格 [${tableName}] 查询结果:`, response)
    return response
  } catch (error) {
    console.error(` 子表格 [${tableName}] 查询失败:`, error)
    throw error
  }
}

/**
 * 并行查询多个子表格
 * @param {String} moduleCode - 模块编码
 * @param {Array} subTableConfigs - 子表格配置数组
 * @param {Object} contextData - 上下文数据（如 billNo）
 * @returns {Promise<Object>} - 所有子表格的查询结果
 */
export const queryAllSubTables = async (moduleCode, subTableConfigs, contextData = {}) => {
  try {
    // 构建所有子表格的查询请求
    const promises = subTableConfigs.map(async (config) => {
      const { key, tableName, defaultConditions, defaultOrderBy } = config
      
      // 替换模板变量（如 ${billNo}）
      const conditions = replaceTemplateVariables(defaultConditions, contextData)
      
      const queryConfig = {
        conditions,
        orderBy: defaultOrderBy || []
      }
      
      const result = await querySubTable({
        moduleCode,
        tableName,
        queryConfig,
        pageNum: 1,
        pageSize: 100 // 子表格通常不需要分页
      })
      
      return {
        key,
        data: result.rows || [],
        total: result.total || 0
      }
    })
    
    // 等待所有查询完成
    const results = await Promise.all(promises)
    
    // 转换为对象格式
    const resultMap = {}
    results.forEach(result => {
      resultMap[result.key] = {
        data: result.data,
        total: result.total
      }
    })
    
    console.log(' 所有子表格查询完成:', resultMap)
    return resultMap
  } catch (error) {
    console.error(' 批量查询子表格失败:', error)
    throw error
  }
}

/**
 * 替换查询条件中的模板变量
 * @param {Array} conditions - 查询条件数组
 * @param {Object} contextData - 上下文数据
 * @returns {Array} - 替换后的条件数组
 */
export const replaceTemplateVariables = (conditions, contextData) => {
  if (!Array.isArray(conditions)) {
    return conditions
  }
  
  return conditions.map(condition => {
    let value = condition.value
    
    // 处理数组类型的值
    if (Array.isArray(value)) {
      value = value.map(v => {
        if (typeof v === 'string' && v.startsWith('${') && v.endsWith('}')) {
          const varName = v.slice(2, -1)
          return contextData[varName] || v
        }
        return v
      })
    } 
    // 处理字符串类型的值
    else if (typeof value === 'string' && value.startsWith('${') && value.endsWith('}')) {
      const varName = value.slice(2, -1)
      value = contextData[varName] || value
    }
    
    return {
      ...condition,
      value
    }
  })
}

/**
 * 从配置中解析子表格查询配置
 * @param {Object} pageConfig - 页面配置对象
 * @returns {Array} - 子表格配置数组
 */
export const parseSubTableConfigs = (pageConfig) => {
  const configs = []
  
  // 🔥 优先从 detailConfig 中解析（新版本）
  if (pageConfig && pageConfig.detailConfig && pageConfig.detailConfig.detail) {
    console.log('✅ 从 detailConfig 中解析子表格配置')
    const tabs = pageConfig.detailConfig.detail.tabs || []
    
    for (const tab of tabs) {
      if (tab.type === 'table' || tab.type === 'descriptions') {
        configs.push({
          key: tab.name,
          tableName: tab.tableName,
          defaultConditions: tab.queryConfig?.defaultConditions || [],
          defaultOrderBy: tab.queryConfig?.defaultOrderBy || [],
          dataField: tab.dataField || `${tab.name}Data`,
          type: tab.type
        })
      }
    }
  }
  // 兼容旧的 subTableQueryConfigs（旧版本）
  else if (pageConfig && pageConfig.subTableQueryConfigs) {
    console.log('⚠️ 使用旧的 subTableQueryConfigs（已废弃）')
    const subTableConfigs = pageConfig.subTableQueryConfigs
    
    // 遍历所有子表格配置
    for (const [key, config] of Object.entries(subTableConfigs)) {
      if (config.enabled) {
        configs.push({
          key,
          tableName: config.tableName,
          defaultConditions: config.defaultConditions || [],
          defaultOrderBy: config.defaultOrderBy || []
        })
      }
    }
  }
  
  console.log('📋 解析到的子表格配置:', configs)
  return configs
}

/**
 * 获取主表格查询配置
 * @param {Object} pageConfig - 页面配置对象
 * @returns {Object|null} - 主表格查询配置
 */
export const getMainTableQueryConfig = (pageConfig) => {
  if (!pageConfig || !pageConfig.queryConfig || !pageConfig.queryConfig.enabled) {
    return null
  }
  
  return {
    tableName: pageConfig.pageConfig?.tableName,
    defaultConditions: pageConfig.queryConfig.defaultConditions || [],
    defaultOrderBy: pageConfig.queryConfig.defaultOrderBy || []
  }
}

/**
 * 根据单据编号查询明细表（专用方法）
 * @param {String} moduleCode - 模块编码
 * @param {String} billNo - 单据编号
 * @returns {Promise<Array>} - 明细数据
 */
export const queryEntryByBillNo = async (moduleCode, billNo) => {
  const queryConfig = {
    conditions: [
      {
        field: 'order_id',
        operator: 'eq',
        value: billNo
      }
    ],
    orderBy: [
      {
        field: 'fPlanMaterialId',
        direction: 'ASC'
      }
    ]
  }
  
  const response = await querySubTable({
    moduleCode,
    tableName: 't_sale_order_entry',
    queryConfig,
    pageNum: 1,
    pageSize: 100
  })
  
  return response.rows || []
}

/**
 * 根据单据编号查询成本表（专用方法）
 * @param {String} moduleCode - 模块编码
 * @param {String} billNo - 单据编号
 * @returns {Promise<Object>} - 成本数据
 */
export const queryCostByBillNo = async (moduleCode, billNo) => {
  const queryConfig = {
    conditions: [
      {
        field: 'order_id',
        operator: 'eq',
        value: billNo
      }
    ],
    orderBy: [
      {
        field: 'id',
        direction: 'ASC'
      }
    ]
  }
  
  const response = await querySubTable({
    moduleCode,
    tableName: 't_sale_order_cost',
    queryConfig,
    pageNum: 1,
    pageSize: 1 // 成本表通常只有一条记录
  })
  
  return response.rows?.[0] || {}
}

export default {
  queryMainTable,
  querySubTable,
  queryAllSubTables,
  replaceTemplateVariables,
  parseSubTableConfigs,
  getMainTableQueryConfig,
  queryEntryByBillNo,
  queryCostByBillNo
}
