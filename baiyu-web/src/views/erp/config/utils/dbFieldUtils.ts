/**
 * 数据库表字段获取工具
 */
import request from '@/utils/request'
import type { DatabaseTableInfo, DatabaseColumnInfo } from '../types/config'
import type { ResponseData } from '@/utils/request'

/**
 * 获取数据库表列表
 */
export async function getDatabaseTables(schema?: string): Promise<string[]> {
  try {
    const response = await request({
      url: '/erp/database/tables',
      method: 'get',
      params: { schema: schema || 'test' }
    }) as unknown as ResponseData<{ tables: string[] }>
    
    if (response.code === 200 && response.data) {
      return response.data.tables || []
    }
    
    return []
  } catch (error) {
    console.error('获取表列表失败:', error)
    return []
  }
}

/**
 * 获取表字段信息
 */
export async function getTableColumns(
  tableName: string, 
  schema?: string
): Promise<DatabaseColumnInfo[]> {
  try {
    const response = await request({
      url: '/erp/database/columns',
      method: 'get',
      params: { 
        table: tableName,
        schema: schema || 'test'
      }
    }) as unknown as ResponseData<{ columns: DatabaseColumnInfo[] }>
    
    if (response.code === 200 && response.data) {
      return response.data.columns || []
    }
    
    return []
  } catch (error) {
    console.error('获取字段信息失败:', error)
    return []
  }
}

/**
 * 获取完整表信息
 */
export async function getTableInfo(
  tableName: string, 
  schema?: string
): Promise<DatabaseTableInfo | null> {
  try {
    const columns = await getTableColumns(tableName, schema)
    
    return {
      tableName,
      tableComment: '', // 如果需要，可以单独调用 API 获取表注释
      columns
    }
  } catch (error) {
    console.error('获取表信息失败:', error)
    return null
  }
}

/**
 * 将数据库字段转换为配置元数据
 */
export function convertDbColumnToFieldMeta(
  column: DatabaseColumnInfo
): any {
  const fieldMeta: any = {
    field: column.columnName,
    label: column.columnComment || column.columnName,
    span: 12,
    required: column.isNullable === 'NO' && !column.columnKey,
    helpText: column.columnComment
  }
  
  // 根据数据库类型推断组件类型
  const columnType = column.columnType.toLowerCase()
  
  if (columnType.includes('int') || columnType.includes('decimal') || columnType.includes('numeric')) {
    fieldMeta.component = 'input-number'
    fieldMeta.dataType = 'number'
    fieldMeta.props = {
      precision: columnType.includes('decimal') ? 2 : 0,
      step: 1
    }
  } else if (columnType.includes('date')) {
    fieldMeta.component = 'date-picker'
    fieldMeta.props = {
      type: columnType.includes('time') ? 'datetime' : 'date',
      format: 'YYYY-MM-DD HH:mm:ss',
      valueFormat: 'YYYY-MM-DD HH:mm:ss'
    }
  } else if (columnType.includes('text')) {
    fieldMeta.component = 'textarea'
    fieldMeta.props = {
      rows: 4,
      maxlength: 1000
    }
  } else if (columnType.includes('tinyint') && column.columnName.toLowerCase().includes('status')) {
    fieldMeta.component = 'switch'
    fieldMeta.dataType = 'boolean'
  } else {
    fieldMeta.component = 'input'
    fieldMeta.dataType = 'string'
    fieldMeta.props = {
      maxlength: 200,
      clearable: true
    }
  }
  
  // 主键或自增字段设为只读
  if (column.columnKey === 'PRI' || column.extra?.toLowerCase().includes('auto_increment')) {
    fieldMeta.disabled = true
    fieldMeta.readonly = true
  }
  
  // 设置默认值
  if (column.defaultValue !== null && column.defaultValue !== undefined) {
    fieldMeta.defaultValue = column.defaultValue
  }
  
  return fieldMeta
}

/**
 * 批量转换字段
 */
export function convertDbColumnsToFieldMetas(
  columns: DatabaseColumnInfo[]
): any[] {
  return columns.map(column => convertDbColumnToFieldMeta(column))
}

/**
 * 搜索表（支持模糊匹配）
 */
export async function searchTables(
  keyword: string, 
  schema?: string
): Promise<string[]> {
  const allTables = await getDatabaseTables(schema)
  
  if (!keyword) {
    return allTables
  }
  
  return allTables.filter(table => 
    table.toLowerCase().includes(keyword.toLowerCase())
  )
}

/**
 * 验证字段是否存在于数据库
 */
export async function validateFieldExists(
  tableName: string,
  fieldName: string,
  schema?: string
): Promise<boolean> {
  try {
    const columns = await getTableColumns(tableName, schema)
    return columns.some(col => col.columnName === fieldName)
  } catch (error) {
    console.error('验证字段失败:', error)
    return false
  }
}
