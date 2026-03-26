/**
 * ERP 字典构建器 - 构建器模式
 * @module views/erp/utils/DictionaryBuilder
 * @description 负责字典数据的构建、加载、缓存和搜索
 * 
 * @author ERP Development Team
 * @date 2026-03-25
 */

import request from '@/utils/request'

/**
 * 字典缓存内部类
 */
class DictionaryCache {
  constructor(data, ttl) {
    this.data = data
    this.timestamp = Date.now()
    this.ttl = ttl
    this.loaded = true
    this.error = null
  }

  isExpired() {
    return Date.now() - this.timestamp > this.ttl
  }
}

/**
 * 字典构建器类
 */
class DictionaryBuilder {
  /**
   * 构造函数
   * @param {string} name - 字典名称
   * @param {Object} options - 配置选项
   */
  constructor(name, options = {}) {
    this.name = name
    this.type = options.type || 'static' // static | dynamic | remote
    this.config = options.config || {}
    this.ttl = options.ttl || 5 * 60 * 1000 // 默认 5 分钟
    this.cache = null
  }

  /**
   * 构建静态字典
   * @param {string} name - 字典名称
   * @param {Array} options - 静态选项数据
   * @returns {DictionaryBuilder}
   */
  static buildStatic(name, options) {
    const builder = new DictionaryBuilder(name, {
      type: 'static',
      ttl: Infinity // 静态字典永不过期
    })
    builder.cache = new DictionaryCache(options, Infinity)
    console.log(` 构建静态字典：${name}, 共 ${options.length} 条`)
    return builder
  }

  /**
   * 构建动态字典
   * @param {string} name - 字典名称
   * @param {Object} config - 配置对象
   * @returns {DictionaryBuilder}
   */
  static buildDynamic(name, config) {
    const builder = new DictionaryBuilder(name, {
      type: 'dynamic',
      config: config,
      ttl: config.ttl || 5 * 60 * 1000
    })
    console.log(` 构建动态字典：${name}`)
    return builder
  }

  /**
   * 构建远程搜索字典
   * @param {string} name - 字典名称
   * @param {Object} config - 配置对象
   * @returns {DictionaryBuilder}
   */
  static buildRemoteSearch(name, config) {
    const builder = new DictionaryBuilder(name, {
      type: 'remote',
      config: config,
      ttl: config.ttl || 5 * 60 * 1000
    })
    console.log(` 构建远程搜索字典：${name}`)
    return builder
  }

  /**
   * 加载字典数据
   * @param {Function} loader - 数据加载函数
   * @returns {Promise<Array>}
   */
  async load(loader) {
    // 检查缓存是否有效
    if (this.cache && this.cache.data && !this.cache.isExpired()) {
      console.debug(`💾 使用缓存字典：${this.name}`)
      return this.cache.data
    }

    try {
      console.log(`🌐 加载字典：${this.name}`)
      const data = await loader()
      
      // 更新缓存
      this.cache = new DictionaryCache(data, this.ttl)
      console.log(` 字典加载成功：${this.name}, 共 ${data.length} 条`)
      
      return data
    } catch (error) {
      console.error(`❌ 字典加载失败：${this.name}`, error)
      if (this.cache) {
        this.cache.error = error.message
      }
      return []
    }
  }

  /**
   * 远程搜索字典
   * @param {string} keyword - 搜索关键词
   * @param {Function} searcher - 搜索函数
   * @returns {Promise<Array>}
   */
  async search(keyword, searcher) {
    if (!this.cache) {
      console.warn(` 字典不存在：${this.name}`)
      return []
    }

    try {
      console.log(`🔍 搜索字典：${this.name}, 关键词：${keyword}`)
      const data = await searcher(keyword)
      
      // 更新缓存
      this.cache = new DictionaryCache(data, this.ttl)
      console.log(` 字典搜索成功：${this.name}, 共 ${data.length} 条`)
      
      return data
    } catch (error) {
      console.error(`❌ 字典搜索失败：${this.name}`, error)
      return []
    }
  }

  /**
   * 获取字典数据（直接从缓存读取）
   * @returns {Array}
   */
  get() {
    if (!this.cache || !this.cache.data) {
      return []
    }
    return this.cache.data
  }

  /**
   * 清除缓存
   */
  clear() {
    if (this.cache) {
      this.cache.loaded = false
      this.cache.data = null
      this.cache.timestamp = 0
      console.log(`🗑️ 已清除字典缓存：${this.name}`)
    }
  }

  /**
   * 获取字典状态
   * @returns {Object}
   */
  getStatus() {
    if (!this.cache) {
      return { exists: false }
    }

    return {
      exists: true,
      loaded: this.cache.loaded,
      expired: this.cache.isExpired(),
      size: this.cache.data ? this.cache.data.length : 0,
      error: this.cache.error
    }
  }
}

/**
 * 字典构建器引擎 - 单例模式
 */
class DictionaryBuilderEngine {
  constructor() {
    if (DictionaryBuilderEngine.instance) {
      return DictionaryBuilderEngine.instance
    }

    // 字典注册表
    this.registry = new Map()
    DictionaryBuilderEngine.instance = this
  }

  /**
   * 注册字典
   * @param {string} name - 字典名称
   * @param {DictionaryBuilder} builder - 字典构建器实例
   * @returns {DictionaryBuilderEngine}
   */
  register(name, builder) {
    this.registry.set(name, builder)
    console.log(`📦 注册字典：${name}`)
    return this
  }

  /**
   * 获取字典构建器
   * @param {string} name - 字典名称
   * @returns {DictionaryBuilder|null}
   */
  get(name) {
    const builder = this.registry.get(name)
    if (!builder) {
      console.warn(` 字典未注册：${name}`)
    }
    return builder
  }

  /**
   * 加载字典数据
   * @param {string} name - 字典名称
   * @param {Function} loader - 数据加载函数
   * @returns {Promise<Array>}
   */
  async load(name, loader) {
    const builder = this.get(name)
    if (!builder) {
      return []
    }
    return await builder.load(loader)
  }

  /**
   * 搜索字典
   * @param {string} name - 字典名称
   * @param {string} keyword - 搜索关键词
   * @param {Function} searcher - 搜索函数
   * @returns {Promise<Array>}
   */
  async search(name, keyword, searcher) {
    const builder = this.get(name)
    if (!builder) {
      return []
    }
    return await builder.search(keyword, searcher)
  }

  /**
   * 获取字典数据(直接从缓存读取)
   * @param {string} name - 字典名称
   * @returns {Array}
   */
  getData(name) {
    const builder = this.get(name)
    if (!builder) {
      return []
    }
    return builder.get()
  }

  /**
   * 清除字典缓存
   * @param {string} name - 字典名称
   */
  clear(name) {
    const builder = this.registry.get(name)
    if (builder) {
      builder.clear()
    }
  }

  /**
   * 清除所有字典缓存
   */
  clearAll() {
    this.registry.forEach((builder, name) => {
      builder.clear()
    })
    console.log('🗑️ 已清除所有字典缓存')
  }

  /**
   * 获取所有字典状态
   * @returns {Object}
   */
  getAllStatus() {
    const allStatus = {}
    this.registry.forEach((builder, name) => {
      allStatus[name] = builder.getStatus()
    })
    return allStatus
  }

  /**
   * 从 JSON 配置批量构建字典
   * @param {Object} dictionaryConfig - 字典配置对象
   * @param {string} moduleCode - 模块编码
   * @returns {DictionaryBuilderEngine}
   */
  buildFromConfig(dictionaryConfig, moduleCode) {
    if (!dictionaryConfig) {
      return this
    }

    for (const [key, config] of Object.entries(dictionaryConfig)) {
      if (Array.isArray(config)) {
        // 静态字典
        this.register(key, DictionaryBuilder.buildStatic(key, config))
      } else if (typeof config === 'object' && config.api) {
        // 动态字典
        this.register(key, DictionaryBuilder.buildDynamic(key, config))
      } else if (typeof config === 'object' && config.searchApi) {
        // 远程搜索字典
        this.register(key, DictionaryBuilder.buildRemoteSearch(key, config))
      }
    }

    console.log(` 从配置构建字典完成，共 ${this.registry.size} 个`)
    return this
  }

  /**
   * 预加载所有字典
   * @param {string} moduleCode - 模块编码
   * @returns {Promise<void>}
   */
  async preloadAll(moduleCode) {
    const promises = []

    this.registry.forEach((builder, name) => {
      if (builder.type === 'dynamic') {
        const promise = builder.load(async () => {
          const api = builder.config.api.replace(/{moduleCode}/g, moduleCode)
          const response = await request(api)
          
          let data = []
          if (response.code === 200 || response.errorCode === 0) {
            data = response.data || response.rows || []
          } else if (Array.isArray(response)) {
            data = response
          }

          // 数据映射 - 仅当后端未映射时才进行（避免重复映射）
          const labelField = builder.config.labelField || 'label'
          const valueField = builder.config.valueField || 'value'
          
          // 检查后端是否已经映射了 label/value 字段
          if (data.length > 0 && data[0].label !== undefined && data[0].value !== undefined) {
            console.log(`✅ 字典 ${name} 已映射，直接使用后端数据`)
            return data
          }
          
          // 后端未映射，前端进行映射
          console.log(`🔧 字典 ${name} 前端映射：label=${labelField}, value=${valueField}`)
          return data.map(item => ({
            label: item[labelField],
            value: item[valueField],
            ...item
          }))
        })
        promises.push(promise)
      }
    })

    await Promise.all(promises)
    console.log(' 字典预加载完成')
  }
}

// 导出单例
const engine = new DictionaryBuilderEngine()

export default engine
export { DictionaryBuilder, DictionaryBuilderEngine }
