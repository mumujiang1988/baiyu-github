/**
 * ERP 配置解析器 - JSON 与表单数据双向转换
 */
import type { 
  ConfigFieldMeta, 
  ValidationRule,
  ParsedConfigResult,
  ConfigGroup 
} from '../types/config'
import { configCategoryMetas } from '../metadata/configMetadata'

/**
 * 配置解析器类
 */
export class ConfigParser {
  private fieldMetas: ConfigFieldMeta[] = []
  private errors: Array<{ field: string; message: string }> = []

  /**
   * 从 JSON 解析为表单数据
   * @param jsonData - JSON 字符串或对象
   * @param category - 配置类别
   * @returns 解析结果
   */
  parseToJsonData(jsonData: string | Record<string, any>, category: string): ParsedConfigResult {
    this.errors = []
    
    try {
      // 解析 JSON 字符串
      const data = typeof jsonData === 'string' ? JSON.parse(jsonData) : jsonData
      
      // 获取元数据
      const meta = configCategoryMetas[category]
      if (!meta) {
        throw new Error(`未知的配置类别：${category}`)
      }

      // 收集所有字段元数据
      this.fieldMetas = this.collectAllFieldMetas(meta.groups)

      // 构建表单数据
      const formData: Record<string, any> = {}
      const validationRules: Record<string, ValidationRule[]> = {}

      this.fieldMetas.forEach(fieldMeta => {
        const field = fieldMeta.field
        
        // 设置默认值
        if (fieldMeta.defaultValue !== undefined) {
          formData[field] = fieldMeta.defaultValue
        } else {
          formData[field] = null
        }

        // 从 JSON 中读取值
        if (data[field] !== undefined) {
          formData[field] = data[field]
        }

        // 构建验证规则
        if (fieldMeta.validation && fieldMeta.validation.length > 0) {
          validationRules[field] = fieldMeta.validation
        }
      })

      return {
        formData,
        validationRules,
        fieldMetas: this.fieldMetas,
        groups: meta.groups,
        errors: this.errors
      }
    } catch (error: any) {
      this.errors.push({
        field: '_root',
        message: `解析失败：${error.message}`
      })

      return {
        formData: {},
        validationRules: {},
        fieldMetas: [],
        groups: [],
        errors: this.errors
      }
    }
  }

  /**
   * 从表单数据生成为 JSON
   * @param formData - 表单数据
   * @param category - 配置类别
   * @returns JSON 字符串
   */
  generateFromFormData(formData: Record<string, any>, category: string): string {
    try {
      // 获取元数据
      const meta = configCategoryMetas[category]
      if (!meta) {
        throw new Error(`未知的配置类别：${category}`)
      }

      // 收集所有字段元数据
      const fieldMetas = this.collectAllFieldMetas(meta.groups)

      // 构建 JSON 对象
      const jsonResult: Record<string, any> = {}

      fieldMetas.forEach(fieldMeta => {
        const field = fieldMeta.field
        const value = formData[field]

        // 跳过空值（除非有默认值）
        if (value === null || value === undefined) {
          if (fieldMeta.defaultValue !== undefined) {
            jsonResult[field] = fieldMeta.defaultValue
          }
          return
        }

        // 根据数据类型转换
        jsonResult[field] = this.convertValueByType(value, fieldMeta.dataType)
      })

      return JSON.stringify(jsonResult, null, 2)
    } catch (error: any) {
      console.error('生成 JSON 失败:', error)
      throw error
    }
  }

  /**
   * 收集所有分组中的字段元数据
   */
  private collectAllFieldMetas(groups: ConfigGroup[]): ConfigFieldMeta[] {
    const allFields: ConfigFieldMeta[] = []
    
    groups.forEach(group => {
      if (group.fields && group.fields.length > 0) {
        allFields.push(...group.fields)
      }
    })

    return allFields
  }

  /**
   * 根据数据类型转换值
   */
  private convertValueByType(value: any, dataType?: string): any {
    if (value === null || value === undefined) {
      return value
    }

    switch (dataType) {
      case 'number':
        return Number(value)
      case 'boolean':
        return Boolean(value)
      case 'string':
        return String(value)
      case 'array':
        return Array.isArray(value) ? value : [value]
      case 'object':
        return typeof value === 'object' ? value : JSON.parse(value)
      default:
        return value
    }
  }

  /**
   * 验证表单数据
   */
  validateFormData(formData: Record<string, any>): Array<{ field: string; message: string }> {
    const validationErrors: Array<{ field: string; message: string }> = []

    this.fieldMetas.forEach(fieldMeta => {
      const field = fieldMeta.field
      const value = formData[field]

      if (!fieldMeta.validation) return

      fieldMeta.validation.forEach(rule => {
        // 必填检查
        if (rule.required && (value === null || value === undefined || value === '')) {
          validationErrors.push({
            field,
            message: rule.message || `${fieldMeta.label}不能为空`
          })
          return
        }

        // 最小长度检查
        if (rule.min !== undefined && typeof value === 'string' && value.length < rule.min) {
          validationErrors.push({
            field,
            message: rule.message || `${fieldMeta.label}长度不能少于${rule.min}个字符`
          })
        }

        // 最大长度检查
        if (rule.max !== undefined && typeof value === 'string' && value.length > rule.max) {
          validationErrors.push({
            field,
            message: rule.message || `${fieldMeta.label}长度不能超过${rule.max}个字符`
          })
        }

        // 正则表达式检查
        if (rule.pattern && typeof value === 'string' && !rule.pattern.test(value)) {
          validationErrors.push({
            field,
            message: rule.message || `${fieldMeta.label}格式不正确`
          })
        }
      })
    })

    return validationErrors
  }

  /**
   * 清空错误
   */
  clearErrors() {
    this.errors = []
  }

  /**
   * 获取所有错误
   */
  getErrors(): Array<{ field: string; message: string }> {
    return [...this.errors]
  }
}

/**
 * 创建解析器实例
 */
export function createConfigParser(): ConfigParser {
  return new ConfigParser()
}

/**
 * 便捷方法：解析 JSON 到表单数据
 */
export function parseJsonToForm(
  jsonData: string | Record<string, any>, 
  category: string
): ParsedConfigResult {
  const parser = createConfigParser()
  return parser.parseToJsonData(jsonData, category)
}

/**
 * 便捷方法：从表单数据生成 JSON
 */
export function generateFormToJson(
  formData: Record<string, any>, 
  category: string
): string {
  const parser = createConfigParser()
  return parser.generateFromFormData(formData, category)
}
