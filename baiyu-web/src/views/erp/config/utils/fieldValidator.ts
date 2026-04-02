/**
 * 配置字段一致性验证工具
 * 用于验证配置字段与数据库实际字段的一致性
 */
import request from '@/utils/request'

export interface FieldMismatch {
  field: string
  issueType: 'missing_in_db' | 'missing_in_config' | 'type_mismatch'
  description: string
}

export interface ValidationResult {
  moduleCode: string
  fieldName: string
  configValue: any
  dbValue: any
  status: 'success' | 'warning' | 'error'
  message: string
}

/**
 * 验证配置字段与数据库的一致性
 * @param moduleCode - 模块编码（如：saleorder）
 * @returns 验证结果
 */
export async function verifyFieldConsistency(moduleCode: string): Promise<{
  success: boolean
  errors: FieldMismatch[]
  warnings: string[]
  summary: string
}> {
  try {
    // 1. 获取配置数据
    const configRes = await request({
      url: '/erp/config/get',
      method: 'get',
      params: { moduleCode }
    })

    if (!configRes.data || !configRes.data.code) {
      throw new Error('配置不存在')
    }

    const configData = configRes.data.data
    
    // 2. 解析各个配置块
    const formFields = parseJsonField(configData.formConfig, '$.fields')
    const tableColumns = parseJsonField(configData.tableConfig, '$.columns')
    const searchFields = parseJsonField(configData.searchConfig, '$.fields')
    const detailTabs = parseJsonField(configData.detailConfig, '$.detail.tabs')
    
    // 3. 收集所有配置的字段名
    const configuredFields = new Set<string>()
    
    // 表单字段
    formFields.forEach((field: any) => {
      if (field.field) configuredFields.add(field.field)
    })
    
    // 表格列
    tableColumns.forEach((col: any) => {
      if (col.prop) configuredFields.add(col.prop)
    })
    
    // 查询字段
    searchFields.forEach((field: any) => {
      if (field.field) configuredFields.add(field.field)
    })
    
    // 详情页签字段
    detailTabs.forEach((tab: any) => {
      if (tab.type === 'table' && tab.table?.columns) {
        tab.table.columns.forEach((col: any) => {
          if (col.prop) configuredFields.add(col.prop)
        })
      } else if (tab.type === 'form' && tab.form?.fields) {
        tab.form.fields.forEach((field: any) => {
          if (field.prop) configuredFields.add(field.prop)
        })
      }
    })
    
    // 4. 获取数据库表结构（需要后端支持）
    const pageConfig = JSON.parse(typeof configData.pageConfig === 'string' 
      ? configData.pageConfig 
      : JSON.stringify(configData.pageConfig))
    
    const mainTable = pageConfig.tableName
    if (!mainTable) {
      throw new Error('配置中缺少主表名信息')
    }
    
    // TODO: 调用后端接口获取数据库字段
    // const dbRes = await request({
    //   url: '/erp/engine/db/columns',
    //   method: 'get',
    //   params: { tableName: mainTable }
    // })
    // const dbFields = new Set(dbRes.data.map((col: any) => col.columnName))
    
    // 5. 对比差异（临时实现，仅返回警告）
    const errors: FieldMismatch[] = []
    const warnings: string[] = [
      `检测到 ${configuredFields.size} 个配置字段`,
      `主表：${mainTable}`,
      '⚠️ 数据库字段验证功能需要后端接口支持'
    ]
    
    return {
      success: true,
      errors,
      warnings,
      summary: `验证完成：${configuredFields.size} 个配置字段，发现 ${errors.length} 个错误`
    }
  } catch (error: any) {
    console.error('字段验证失败:', error)
    return {
      success: false,
      errors: [],
      warnings: [],
      summary: `验证失败：${error.message}`
    }
  }
}

/**
 * 解析 JSON 字段中的特定路径
 */
function parseJsonField(jsonData: any, path: string): any[] {
  if (!jsonData) return []
  
  try {
    const data = typeof jsonData === 'string' 
      ? JSON.parse(jsonData) 
      : jsonData
    
    // 简单路径解析（支持 $.xxx 格式）
    const keys = path.replace('$.', '').split('.')
    let result = data
    
    for (const key of keys) {
      if (result && result[key] !== undefined) {
        result = result[key]
      } else {
        return []
      }
    }
    
    return Array.isArray(result) ? result : []
  } catch (e) {
    console.warn(`解析 JSON 路径失败：${path}`, e)
    return []
  }
}

/**
 * 生成验证报告 HTML
 */
export function generateValidationReport(result: {
  success: boolean
  errors: FieldMismatch[]
  warnings: string[]
  summary: string
}): string {
  const timestamp = new Date().toLocaleString('zh-CN')
  
  return `
<div style="padding: 16px; font-family: Arial, sans-serif;">
  <h3 style="color: ${result.success ? '#67C23A' : '#F56C6C'}">
    ${result.success ? '✅ 验证通过' : '❌ 验证失败'}
  </h3>
  <p><strong>时间:</strong> ${timestamp}</p>
  <p><strong>摘要:</strong> ${result.summary}</p>
  
  ${result.errors.length > 0 ? `
    <div style="margin-top: 16px;">
      <h4 style="color: #F56C6C">❌ 错误列表 (${result.errors.length})</h4>
      <ul>
        ${result.errors.map(err => `
          <li style="color: #606266; margin: 8px 0;">
            <strong>${err.field}</strong>: ${err.description}
          </li>
        `).join('')}
      </ul>
    </div>
  ` : ''}
  
  ${result.warnings.length > 0 ? `
    <div style="margin-top: 16px;">
      <h4 style="color: #E6A23C">⚠️ 警告信息 (${result.warnings.length})</h4>
      <ul>
        ${result.warnings.map(warn => `
          <li style="color: #909399; margin: 8px 0;">${warn}</li>
        `).join('')}
      </ul>
    </div>
  ` : ''}
</div>
  `.trim()
}
