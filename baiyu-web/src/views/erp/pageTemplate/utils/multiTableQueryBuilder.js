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
    
    // 注意：后端返回的是 R.ok(result),所以数据在 response.data 中
    return response.data || {}
  } catch (error) {
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
    console.error('[成本表查询 - querySubTable] 📝 请求参数:', JSON.stringify({ moduleCode, tableName, queryConfig, pageNum, pageSize }, null, 2))
    
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
    
    // 成本表异常日志
    if (tableName === 't_sale_order_cost') {
      console.error('[成本表查询 - querySubTable] ✅ 响应数据:', JSON.stringify(response.data, null, 2))
      if (!response.data || !response.data.rows || response.data.rows.length === 0) {
        console.error('[成本表查询 - querySubTable] ⚠️ 警告：返回数据为空')
        console.error('[成本表查询 - querySubTable] 🔍 检查 SQL 配置和数据库连接')
      }
    }
    
    // 注意：后端返回的是 R.ok(result),所以数据在 response.data 中
    return response.data || {}
  } catch (error) {
    // 成本表异常日志
    if (tableName === 't_sale_order_cost') {
      console.error('[成本表查询 - querySubTable] ❌ 查询失败:', error.message)
      console.error('[成本表查询 - querySubTable] 错误堆栈:', error.stack)
    }
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
    console.error('[成本表汇总 - queryAllSubTables] 🚀 开始批量查询子表格')
    console.error('[成本表汇总 - queryAllSubTables] 🔧 moduleCode:', moduleCode)
    console.error('[成本表汇总 - queryAllSubTables] 📋 子表格数量:', subTableConfigs.length)
    console.error('[成本表汇总 - queryAllSubTables] 💾 contextData:', JSON.stringify(contextData, null, 2))
    
    // 构建所有子表格的查询请求
    const promises = subTableConfigs.map(async (config) => {
      const { key, tableName, defaultConditions, defaultOrderBy } = config
      
      console.error(`[成本表汇总 - queryAllSubTables] 📊 处理子表格 [${key}], tableName: ${tableName}`)
      
      // 替换模板变量（如 ${billNo}）
      const conditions = replaceTemplateVariables(defaultConditions, contextData)
      
      console.error(`[成本表汇总 - queryAllSubTables] 🔧 子表格 [${key}] 查询条件:`, JSON.stringify(conditions, null, 2))
      
      const queryConfig = {
        conditions,
        orderBy: defaultOrderBy || []
      }
      
      console.error(`[成本表汇总 - queryAllSubTables] 🔍 子表格 [${key}] 完整 queryConfig:`, JSON.stringify(queryConfig, null, 2))
      
      const result = await querySubTable({
        moduleCode,
        tableName,
        queryConfig,
        pageNum: 1,
        pageSize: 100 // 子表格通常不需要分页
      })
      
      console.error(`[成本表汇总 - queryAllSubTables] ✅ 子表格 [${key}] 查询完成，rows: ${result?.rows?.length || 0}, total: ${result?.total || 0}`)
      
      // result 已经是 response.data，包含 rows 和 total
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
    
    console.error('[成本表汇总 - queryAllSubTables] 📦 所有子表格查询完成，结果:', JSON.stringify(resultMap, null, 2))
    
    // 成本表异常日志
    if (resultMap.cost) {
      console.error('[成本表汇总 - queryAllSubTables] 💰 成本表最终结果:', JSON.stringify(resultMap.cost, null, 2))
      if (!resultMap.cost.data || resultMap.cost.data.length === 0) {
        console.error('[成本表汇总 - queryAllSubTables] ⚠️ 警告：成本表数据为空')
        console.error('[成本表汇总 - queryAllSubTables] 🔍 请检查：1)SQL 配置 2) 数据库连接 3) 查询条件 4) 单据编号是否正确')
      }
    } else {
      console.error('[成本表汇总 - queryAllSubTables] ⚠️ 警告：结果中没有 cost 数据')
    }
    
    return resultMap
  } catch (error) {
    console.error('[成本表汇总 - queryAllSubTables] ❌ 批量查询子表格失败:', error)
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
  
  // 优先从 detailConfig 中解析（新版本）
  if (pageConfig && pageConfig.detailConfig && pageConfig.detailConfig.detail) {
    const tabs = pageConfig.detailConfig.detail.tabs || []
    
    for (const tab of tabs) {
      // 支持表格、描述列表和表单（成本表）类型
      if (tab.type === 'table' || tab.type === 'descriptions' || tab.type === 'form') {
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
        field: 'FBillNo',
        operator: 'eq',
        value: billNo
      }
    ],
    orderBy: [
      {
        field: 'FPlanMaterialId',
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
  
  // response 已经是 response.data，包含 rows 和 total
  return response.rows || []
}

/**
 * 根据单据编号查询成本表 (专用方法)
 * @param {String} moduleCode - 模块编码
 * @param {String} billNo - 单据编号
 * @returns {Promise<Object>} - 成本数据
 */
export const queryCostByBillNo = async (moduleCode, billNo) => {
  const queryConfig = {
    conditions: [
      {
        field: 'FBillNo',
        operator: 'eq',
        value: billNo
      }
    ],
    orderBy: [
      {
        field: 'FID',
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
  
  // response 已经是 response.data，包含 rows 和 total
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
