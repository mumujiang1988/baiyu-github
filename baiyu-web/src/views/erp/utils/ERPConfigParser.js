/**
 * ERP 低代码引擎 - 配置解析器
 * @module views/erp/utils/ERPConfigParser
 * @description 负责解析 JSON 配置并生成可用的页面组件配置
 */

import request from '@/utils/request'
import dayjs from 'dayjs'

// 配置缓存（内存缓存，5 分钟过期）
const configCache = new Map()
const CACHE_TTL = 5 * 60 * 1000 // 5 分钟

class ERPConfigParser {
  /**
   * 静态方法：从数据库加载配置（带缓存）
   * @param {string} moduleCode - 模块编码
   * @returns {Promise<Object>} - 配置对象
   */
  static async loadFromDatabase(moduleCode) {
    const cacheKey = `erp_config_${moduleCode}`
    
    // 检查缓存
    const cached = configCache.get(cacheKey)
    if (cached && Date.now() - cached.timestamp < CACHE_TTL) {
      console.log('💾 命中缓存配置:', moduleCode)
      return cached.config
    }
    
    try {
      console.log('🌐 从数据库加载配置:', moduleCode)
      const response = await request({
        url: `/erp/config/get/${moduleCode}`,
        method: 'get'
      })
      
      // 🔍 添加详细调试信息
      console.log('📥 后端返回的完整响应:', response)
      console.log('📥 response.code:', response.code)
      console.log('📥 response.data 类型:', typeof response.data, '值:', response.data)
      console.log('📥 response.msg:', typeof response.msg, '长度:', response.msg?.length)
      
      if (response.code === 200 || response.code === 0) {
        // ✅ 修复：后端返回的 data 字段可能是字符串或对象
        let configContent;
        
        // 🔍 检查 response.data 是否存在，如果不存在尝试从 msg 字段获取（兼容处理）
        let rawData = response.data;
        if (!rawData && response.msg) {
          console.warn('⚠️ response.data 为空，尝试从 msg 字段读取配置...')
          rawData = response.msg;
        }
        
        if (!rawData) {
          console.error('❌ response.data 和 response.msg 都为空！', response)
          throw new Error('配置内容为空')
        }
        
        if (typeof rawData === 'string') {
          console.log('📝 rawData 是字符串，长度:', rawData.length)
          // 如果是字符串，尝试解析 JSON
          try {
            configContent = JSON.parse(rawData);
            console.log('✅ JSON 解析成功')
          } catch (parseError) {
            console.error('❌ JSON 解析失败:', parseError, '原始数据:', rawData);
            throw new Error('配置内容格式错误');
          }
        } else if (rawData && typeof rawData === 'object') {
          console.log('📝 rawData 已经是对象')
          // 如果已经是对象，直接使用
          configContent = rawData;
        } else {
          console.error('❌ rawData 类型异常:', typeof rawData, '值:', rawData)
          throw new Error('配置内容为空或格式不正确');
        }
        
        // 更新缓存
        configCache.set(cacheKey, {
          config: configContent,
          timestamp: Date.now()
        });
        
        console.log('✅ 数据库配置加载成功:', configContent.pageConfig?.title);
        console.log('📦 配置版本:', response.version || configContent.version || 'N/A');
        
        return configContent;
      } else {
        console.error('❌ 后端返回错误码:', response.code, '错误信息:', response.msg)
        throw new Error(response.msg || '配置加载失败');
      }
    } catch (error) {
      console.error('❌ 加载数据库配置失败:', error);
      throw error;
    }
  }
  
  /**
   * 静态方法：清除配置缓存
   * @param {string} moduleCode - 模块编码
   */
  static clearCache(moduleCode) {
    const cacheKey = `erp_config_${moduleCode}`
    configCache.delete(cacheKey)
    console.log('🗑️ 已清除配置缓存:', moduleCode)
  }
  
  /**
   * 静态方法：清除所有配置缓存
   */
  static clearAllCache() {
    configCache.clear()
    console.log('🗑️ 已清除所有配置缓存')
  }
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
