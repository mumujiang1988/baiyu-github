/**
 * ERP 详情页签配置校验器
 * @description 验证页签配置的完整性和正确性
 */

/**
 * 校验页签配置
 * @param {Object} tab - 页签配置对象
 * @returns {Object} 校验结果
 */
export function validateTabConfig(tab) {
  const errors = []
  const warnings = []
  
  // 基础字段检查
  if (!tab.name) {
    errors.push('缺少必填字段：name')
  }
  
  if (!tab.label) {
    errors.push('缺少必填字段：label')
  }
  
  if (!tab.type) {
    warnings.push('未指定 type 字段，将使用默认值 "table"')
  }
  
  if (!tab.dataField) {
    errors.push('缺少必填字段：dataField，无法获取数据')
  }
  
  // 根据类型检查特定配置
  const type = tab.type || 'table'
  
  if (type === 'table') {
    // 表格类型必须包含 table.columns 或 columns
    const hasTableColumns = tab.table && Array.isArray(tab.table.columns)
    const hasColumns = Array.isArray(tab.columns)
    
    if (!hasTableColumns && !hasColumns) {
      errors.push(`表格类型页签 "${tab.name}" 缺少列配置：需要在 tab.table.columns 或 tab.columns 中定义`)
    }
    
    // 检查是否有 tableName 或 queryConfig
    if (!tab.tableName && !tab.queryConfig) {
      warnings.push(`表格类型页签 "${tab.name}" 未指定 tableName 或 queryConfig`)
    }
  }
  
  if (type === 'form') {
    // 表单类型必须包含 form.fields 或 fields
    const hasFormFields = tab.form && Array.isArray(tab.form.fields)
    const hasFields = Array.isArray(tab.fields)
    
    if (!hasFormFields && !hasFields) {
      errors.push(`表单类型页签 "${tab.name}" 缺少字段配置：需要在 tab.form.fields 或 tab.fields 中定义`)
    }
    
    // 检查是否有 tableName 或 queryConfig
    if (!tab.tableName && !tab.queryConfig) {
      warnings.push(`表单类型页签 "${tab.name}" 未指定 tableName 或 queryConfig`)
    }
  }
  
  if (type === 'descriptions') {
    // 描述列表类型必须包含 fields
    if (!Array.isArray(tab.fields)) {
      errors.push(`描述列表类型页签 "${tab.name}" 缺少字段配置：需要在 tab.fields 中定义`)
    }
  }
  
  return {
    valid: errors.length === 0,
    errors,
    warnings,
    config: tab
  }
}

/**
 * 批量校验所有页签配置
 * @param {Array} tabs - 页签配置数组
 * @returns {Object} 校验结果
 */
export function validateAllTabs(tabs) {
  const results = {
    valid: true,
    total: tabs.length,
    success: 0,
    failed: 0,
    details: []
  }
  
  tabs.forEach((tab, index) => {
    const validation = validateTabConfig(tab)
    
    if (validation.valid) {
      results.success++
    } else {
      results.failed++
      results.valid = false
    }
    
    results.details.push({
      index,
      tabName: tab.name || `未命名-${index}`,
      ...validation
    })
  })
  
  return results
}

/**
 * 打印校验结果到控制台
 * @param {Object} validationResult - 校验结果对象
 */
export function printValidationResult(validationResult) {
  // 精简日志：仅输出错误信息
  if (!validationResult.valid) {
    validationResult.details.forEach(detail => {
      if (!detail.valid) {
        console.error(`页签配置错误 "${detail.tabName}":`, detail.errors)
      }
    })
  }
}
