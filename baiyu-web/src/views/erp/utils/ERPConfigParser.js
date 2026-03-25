/**
 * ERP 低代码引擎 - 配置解析器
 * @module views/erp/utils/ERPConfigParser
 * @description 负责解析 JSON 配置并生成可用的页面组件配置
 */

import dayjs from 'dayjs'

class ERPConfigParser {
  constructor(config) {
    this.config = config
    this.dictionaries = new Map()
  }

  /**
   * 解析页面基础配置
   */
  parsePageConfig() {
    const { pageConfig } = this.config
    return {
      title: pageConfig.title,
      permissionPrefix: pageConfig.permissionPrefix,
      apiPrefix: pageConfig.apiPrefix,
      primaryKey: pageConfig.primaryKey || 'id',
      billNoField: pageConfig.billNoField || 'fbillNo',
      layout: pageConfig.layout || 'standard'
    }
  }

  /**
   * 解析查询表单配置
   */
  parseSearchForm() {
    const { searchConfig } = this.config
    if (!searchConfig) return { showSearch: false, fields: [] }

    return {
      showSearch: searchConfig.showSearch !== false,
      defaultExpand: searchConfig.defaultExpand !== false,
      fields: searchConfig.fields.map(field => ({
        ...field,
        componentType: this.getComponentType(field.component),
        eventHandlers: this.parseEventHandlers(field)
      }))
    }
  }

  /**
   * 解析表格列配置
   */
  parseTableColumns() {
    const { tableConfig } = this.config
    if (!tableConfig) return { columns: [], rowKey: 'id' }

    return {
      rowKey: tableConfig.rowKey || 'id',
      border: tableConfig.border !== false,
      stripe: tableConfig.stripe !== false,
      maxHeight: tableConfig.maxHeight || 'calc(100vh - 380px)',
      showOverflowTooltip: tableConfig.showOverflowTooltip !== false,
      resizable: tableConfig.resizable !== false,
      columns: tableConfig.columns.map(col => ({
        ...col,
        renderType: col.renderType || 'text',
        visible: col.visible !== false,
        formatter: this.getFormatter(col)
      })),
      expandRow: tableConfig.expandRow || null
    }
  }

  /**
   * 解析表单配置
   */
  parseFormConfig() {
    const { formConfig } = this.config
    if (!formConfig) return { sections: [], dialogWidth: '1000px', labelWidth: '120px' }

    return {
      dialogWidth: formConfig.dialogWidth || '1000px',
      labelWidth: formConfig.labelWidth || '120px',
      sections: formConfig.sections.map(section => ({
        ...section,
        fields: section.fields.map(field => ({
          ...field,
          rules: this.parseFieldRules(field),
          componentProps: field.props || {}
        }))
      })),
      formTabs: formConfig.formTabs ? {
        enabled: formConfig.formTabs.enabled !== false,
        tabs: (formConfig.formTabs.tabs || []).map(tab => ({
          ...tab,
          type: tab.type || 'table'
        }))
      } : null
    }
  }

  /**
   * 解析抽屉详情配置
   */
  parseDrawerConfig() {
    const { drawerConfig } = this.config
    if (!drawerConfig) return { enabled: false, tabs: [] }

    return {
      enabled: drawerConfig.enabled !== false,
      trigger: drawerConfig.trigger || 'click',
      loadStrategy: drawerConfig.loadStrategy || 'lazy',
      title: drawerConfig.title || '详情',
      tabs: (drawerConfig.tabs || []).map(tab => ({
        ...tab,
        type: tab.type || 'table',
        columns: tab.columns || 3
      }))
    }
  }

  /**
   * 解析操作按钮配置
   */
  parseActions() {
    const { actionConfig } = this.config
    if (!actionConfig) return { toolbar: [], row: [] }

    return {
      toolbar: actionConfig.toolbar || [],
      row: actionConfig.row || []
    }
  }

  /**
   * 加载字典数据
   */
  async loadDictionaries(moduleCode) {
    const { dictionaryConfig } = this.config
    if (!dictionaryConfig) return

    for (const [key, value] of Object.entries(dictionaryConfig)) {
      if (Array.isArray(value)) {
        // 静态字典
        this.dictionaries.set(key, value)
      } else if (typeof value === 'object' && value.api) {
        // 动态字典（排除客户搜索）
        if (key !== 'customers') {
          try {
            const api = value.api.replace(/{moduleCode}/g, moduleCode)
            const response = await fetch(api).then(r => r.json())
            
            let data = []
            if (response.code === 200 || response.errorCode === 0) {
              data = response.data || response.rows || []
            } else if (Array.isArray(response)) {
              data = response
            }
            
            const mappedData = data.map(item => ({
              label: item[value.labelField] || item.label || item.name,
              value: item[value.valueField] || item.value || item.kingdee,
              ...item
            }))
            this.dictionaries.set(key, mappedData)
          } catch (error) {
            console.warn(`字典 "${key}" 加载失败：`, error.message)
            this.dictionaries.set(key, [])
          }
        }
      }
    }
  }

  /**
   * 获取字典选项
   */
  getDictOptions(dictName) {
    return this.dictionaries.get(dictName) || []
  }

  /**
   * 解析字段验证规则
   */
  parseFieldRules(field) {
    const rules = []

    if (field.required) {
      rules.push({
        required: true,
        message: `${field.label}不能为空`,
        trigger: field.rules?.[0]?.trigger || 'blur'
      })
    }

    if (field.rules) {
      rules.push(...field.rules)
    }

    return rules
  }

  /**
   * 获取格式化器
   */
  getFormatter(column) {
    switch (column.renderType) {
      case 'tag':
        return (row) => this.getTagConfig(row[column.prop], column.dictionary)
      case 'currency':
        return (row) => this.formatCurrency(row[column.prop], column.precision)
      case 'date':
        return (row) => this.formatDate(row[column.prop], column.format)
      case 'datetime':
        return (row) => this.formatDateTime(row[column.prop], column.format)
      case 'percent':
        return (row) => this.formatPercent(row[column.prop], column.precision)
      case 'number':
        return (row) => this.formatNumber(row[column.prop], column.precision)
      default:
        return null
    }
  }

  /**
   * 标签配置
   */
  getTagConfig(value, dictName) {
    const dict = this.getDictOptions(dictName)
    const option = dict.find(item => item.value === value)
    return {
      label: option?.label || value,
      type: option?.type || 'info'
    }
  }

  /**
   * 货币格式化
   */
  formatCurrency(value, precision = 2) {
    if (!value && value !== 0) return '-'
    return Number(value).toLocaleString('zh-CN', {
      minimumFractionDigits: precision,
      maximumFractionDigits: precision
    })
  }

  /**
   * 日期格式化
   */
  formatDate(value, format = 'YYYY-MM-DD') {
    if (!value) return '-'
    try {
      return dayjs(value).format(format)
    } catch (error) {
      return value
    }
  }

  /**
   * 日期时间格式化
   */
  formatDateTime(value, format = 'YYYY-MM-DD HH:mm:ss') {
    if (!value) return '-'
    try {
      return dayjs(value).format(format)
    } catch (error) {
      return value
    }
  }

  /**
   * 百分比格式化
   */
  formatPercent(value, precision = 2) {
    if (!value && value !== 0) return '-'
    return `${Number(value).toFixed(precision)}%`
  }

  /**
   * 数字格式化
   */
  formatNumber(value, precision = 0) {
    if (!value && value !== 0) return '-'
    return Number(value).toFixed(precision)
  }

  /**
   * 组件类型映射
   */
  getComponentType(component) {
    const componentMap = {
      'input': 'ElInput',
      'select': 'ElSelect',
      'date': 'ElDatePicker',
      'daterange': 'ElDatePicker',
      'number': 'ElInputNumber',
      'textarea': 'ElInput',
      'radio': 'ElRadioGroup',
      'checkbox': 'ElCheckboxGroup',
      'switch': 'ElSwitch'
    }
    return componentMap[component] || 'ElInput'
  }

  /**
   * 解析事件处理器
   */
  parseEventHandlers(field) {
    const handlers = {}

    if (field.changeEvent) {
      handlers.change = field.changeEvent
    }

    return handlers
  }
}

export default ERPConfigParser
