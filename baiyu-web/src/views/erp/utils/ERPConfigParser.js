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
      console.log('命中缓存配置:', moduleCode)
      return cached.config
    }
    
    try {
      console.log('从数据库加载配置:', moduleCode)
      const response = await request({
        url: `/erp/config/get/${moduleCode}`,
        method: 'get'
      })
      
      // 添加详细调试信息
      console.log('后端返回的完整响应:', response)
      console.log('response.code:', response.code)
      console.log('response.data 类型:', typeof response.data, '值:', response.data)
      console.log('response.msg:', typeof response.msg, '长度:', response.msg?.length)
      console.log('response 所有键:', Object.keys(response))
      
      if (response.code === 200 || response.code === 0) {
        // 标准处理：后端直接返回JSON字符串，前端自己解析
        let configContent;
        const rawData = response.data;

        if (!rawData) {
          console.error('配置数据为空！', response)
          throw new Error('配置内容为空')
        }

        if (typeof rawData === 'string') {
          console.log('rawData 是字符串，长度:', rawData.length)
          try {
            configContent = JSON.parse(rawData);
            console.log('JSON 解析成功')
          } catch (parseError) {
            console.error('JSON 解析失败:', parseError, '原始数据:', rawData);
            throw new Error('配置内容格式错误');
          }
        } else {
          console.error('rawData 类型异常:', typeof rawData, '期望类型: string')
          throw new Error('后端返回的数据类型不正确，期望JSON字符串');
        }

        // 更新缓存
        configCache.set(cacheKey, {
          config: configContent,
          timestamp: Date.now()
        });

        console.log('数据库配置加载成功:', configContent.pageConfig?.title || configContent.pageConfig?.pageName);
        console.log('配置版本:', configContent.version || 'N/A');

        return configContent;
      } else {
        console.error('后端返回错误码:', response.code, '错误信息:', response.msg)
        throw new Error(response.msg || '配置加载失败');
      }
    } catch (error) {
      console.error(' 加载数据库配置失败:', error);
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
    console.log(' 已清除配置缓存:', moduleCode)
  }
  
  /**
   * 静态方法：清除所有配置缓存
   */
  static clearAllCache() {
    configCache.clear()
    console.log(' 已清除所有配置缓存')
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
      moduleCode: pageConfig.moduleCode,  // 新增：模块编码
      permissionPrefix: pageConfig.permissionPrefix,
      apiPrefix: pageConfig.apiPrefix,
      primaryKey: pageConfig.primaryKey || 'id',
      billNoField: pageConfig.billNoField || 'FBillNo',
      layout: pageConfig.layout || 'standard',
      tableName: pageConfig.tableName || null
    }
  }

  /**
   * 解析查询构建器配置（新增）
   */
  parseQueryConfig() {
    const { queryConfig } = this.config
    if (!queryConfig || !queryConfig.enabled) {
      return null
    }

    return {
      enabled: true,
      defaultConditions: queryConfig.defaultConditions || [],
      defaultOrderBy: queryConfig.defaultOrderBy || [],
      select: queryConfig.select || null,
      groupBy: queryConfig.groupBy || null,
      having: queryConfig.having || null
    }
  }

  /**
   * 解析查询表单配置
   */
  parseSearchForm() {
    const { searchConfig } = this.config
    if (!searchConfig) return { showSearch: false, fields: [] }
  
    // 兼容两种格式：fields 数组或 sections 数组
    const hasFields = Array.isArray(searchConfig.fields)
    const hasSections = Array.isArray(searchConfig.sections)
      
    if (!hasFields && !hasSections) {
      console.warn('searchConfig 缺少 fields 或 sections 数组')
      return { showSearch: false, fields: [] }
    }
  
    return {
      showSearch: searchConfig.showSearch !== false,
      defaultExpand: searchConfig.defaultExpand !== false,
      fields: (hasFields ? searchConfig.fields : 
               hasSections ? searchConfig.sections.flatMap(s => s.fields || []) : []
              ).map(field => ({
        ...field,
        componentType: this.getComponentType(field.component),
        eventHandlers: this.parseEventHandlers(field),
        queryOperator: field.queryOperator || 'eq' // 新增：查询运算符配置
      }))
    }
  }

  /**
   * 解析表格列配置
   */
  parseTableColumns() {
    const { tableConfig } = this.config
    if (!tableConfig) return { columns: [], rowKey: 'id' }

    // 兼容两种格式：columns 数组或 fields 数组
    const hasColumns = Array.isArray(tableConfig.columns)
    const hasFields = Array.isArray(tableConfig.fields)
    
    if (!hasColumns && !hasFields) {
      console.warn('tableConfig 缺少 columns 或 fields 数组')
      return {
        rowKey: tableConfig.rowKey || 'id',
        border: tableConfig.border !== false,
        stripe: tableConfig.stripe !== false,
        maxHeight: tableConfig.maxHeight || 'calc(100vh - 380px)',
        showOverflowTooltip: tableConfig.showOverflowTooltip !== false,
        resizable: tableConfig.resizable !== false,
        columns: [],
        expandRow: tableConfig.expandRow || null,
        orderBy: tableConfig.orderBy || []
      }
    }

    // 统一使用 columns 字段（优先）或从 fields 转换
    const columnsData = hasColumns ? tableConfig.columns : 
                        hasFields ? tableConfig.fields.map(f => ({
                          prop: f.prop || f.field,
                          label: f.label,
                          width: f.width,
                          fixed: f.fixed,
                          align: f.align || 'left',
                          renderType: f.renderType || f.type || 'text',
                          dictionary: f.dictionary,
                          precision: f.precision,
                          format: f.format
                        })) : []

    return {
      rowKey: tableConfig.rowKey || 'id',
      border: tableConfig.border !== false,
      stripe: tableConfig.stripe !== false,
      maxHeight: tableConfig.maxHeight || 'calc(100vh - 380px)',
      showOverflowTooltip: tableConfig.showOverflowTooltip !== false,
      resizable: tableConfig.resizable !== false,
      columns: columnsData.map(col => ({
        ...col,
        renderType: col.renderType || 'text',
        visible: col.visible !== false,
        formatter: this.getFormatter(col)
      })),
      expandRow: tableConfig.expandRow || null,
      orderBy: tableConfig.orderBy || [] // 新增：默认排序配置
    }
  }

  /**
   * 解析表单配置
   */
  parseFormConfig() {
    const { formConfig } = this.config
    if (!formConfig) return { sections: [], dialogWidth: '1000px', labelWidth: '120px' }

    // 兼容两种格式：
    // 格式 1（新）：formConfig.fields - 扁平字段数组
    // 格式 2（旧）：formConfig.sections - 分组 sections 数组
    
    if (Array.isArray(formConfig.fields)) {
      // 新格式：扁平字段数组
      console.log('使用新格式 formConfig.fields，字段数量:', formConfig.fields.length)
      return {
        dialogWidth: formConfig.dialogWidth || '1000px',
        labelWidth: formConfig.labelWidth || '120px',
        fields: formConfig.fields.map(field => ({
          ...field,
          rules: this.parseFieldRules(field),
          componentProps: field.props || {},
          componentType: this.getComponentType(field.component)
        })),
        // 保持向后兼容：将 fields 包装成 sections 格式
        sections: [{
          title: '',
          fields: formConfig.fields.map(field => ({
            ...field,
            rules: this.parseFieldRules(field),
            componentProps: field.props || {}
          }))
        }],
        formTabs: formConfig.formTabs ? {
          enabled: formConfig.formTabs.enabled !== false,
          tabs: (formConfig.formTabs.tabs || []).map(tab => ({
            ...tab,
            type: tab.type || 'table'
          }))
        } : null
      }
    } else if (Array.isArray(formConfig.sections)) {
      // 旧格式：sections 数组
      console.log('使用旧格式 formConfig.sections，分组数量:', formConfig.sections.length)
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
    } else {
      console.warn('formConfig 格式未知，返回空配置')
      return { sections: [], dialogWidth: '1000px', labelWidth: '120px' }
    }
  }

  /**
   * 解析抽屉详情配置
   */
  parseDrawerConfig() {
    // 优先使用新的 detailConfig（6 字段版本）
    const { detailConfig, drawerConfig } = this.config
    
    if (detailConfig && detailConfig.detail) {
      console.log('使用新的 detailConfig')
      return {
        enabled: detailConfig.detail.enabled !== false,
        displayType: detailConfig.detail.displayType || 'drawer',
        title: detailConfig.detail.title || '详情',
        width: detailConfig.detail.width || '60%',
        direction: detailConfig.detail.direction || 'rtl',
        loadStrategy: detailConfig.detail.loadStrategy || 'lazy',
        tabs: (detailConfig.detail.tabs || []).map(tab => ({
          ...tab,
          type: tab.type || 'table',
          dataField: tab.dataField || `${tab.name}Data`,
          tableName: tab.tableName,
          queryConfig: tab.queryConfig || {},
          columns: tab.columns || 3,
          fields: tab.fields || [],
          table: tab.table || {}
        }))
      }
    }
    
    // 兼容旧的 drawerConfig
    if (drawerConfig) {
      console.log('使用旧的 drawerConfig（已废弃）')
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
    
    return { enabled: false, tabs: [] }
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
   * 解析子表格查询配置（新增）
   */
  parseSubTableQueryConfig() {
    const { subTableQueryConfigs } = this.config
    if (!subTableQueryConfigs) return {}
    
    const result = {}
    for (const [key, config] of Object.entries(subTableQueryConfigs)) {
      result[key] = {
        enabled: config.enabled !== false,
        tableName: config.tableName,
        defaultConditions: config.defaultConditions || [],
        defaultOrderBy: config.defaultOrderBy || []
      }
    }
    return result
  }

  /**
   * 加载字典数据（仅支持新格式）
   */
  async loadDictionaries(moduleCode) {
    const { dictionaryConfig } = this.config
    if (!dictionaryConfig) return

    //  强制使用新构建器模式
    if (!dictionaryConfig.builder?.enabled) {
      console.warn(' 未启用字典构建器，请设置 dictionaryConfig.builder.enabled = true')
      return
    }

    //  强制使用 dictionaries 结构
    const dictMap = dictionaryConfig.dictionaries
    if (!dictMap) {
      console.error('字典配置格式错误：缺少 dictionaryConfig.dictionaries')
      return
    }

    for (const [key, config] of Object.entries(dictMap)) {
      // 新格式：必须包含 type 字段
      if (!config.type) {
        console.error(`字典 "${key}" 配置错误：缺少 type 字段（static/dynamic/remote）`)
        continue
      }

      if (config.type === 'static') {
        // 静态字典
        if (!config.data || !Array.isArray(config.data)) {
          console.error(`静态字典 "${key}" 配置错误：缺少 data 数组`)
          continue
        }
        this.dictionaries.set(key, config.data)
      } else if (config.type === 'dynamic') {
        // 动态字典
        if (!config.config?.api) {
          console.error(`动态字典 "${key}" 配置错误：缺少 config.api`)
          continue
        }
        try {
          const api = config.config.api.replace(/{moduleCode}/g, moduleCode)
          const response = await fetch(api).then(r => r.json())
          
          let data = []
          if (response.code === 200 || response.errorCode === 0) {
            data = response.data || response.rows || []
          } else if (Array.isArray(response)) {
            data = response
          }
          
          const labelField = config.config.labelField || 'label'
          const valueField = config.config.valueField || 'value'
          const mappedData = data.map(item => ({
            label: item[labelField],
            value: item[valueField],
            ...item
          }))
          this.dictionaries.set(key, mappedData)
        } catch (error) {
          console.error(`动态字典 "${key}" 加载失败：`, error.message)
          this.dictionaries.set(key, [])
        }
      } else if (config.type === 'remote') {
        // 远程搜索字典（不预加载，由搜索触发）
        console.log(`远程搜索字典 "${key}" 已注册，等待搜索时加载`)
      } else {
        console.error(`字典 "${key}" 类型错误：type 必须是 static/dynamic/remote 之一`)
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
