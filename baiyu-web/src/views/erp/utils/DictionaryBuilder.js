/**
 * ERP 字典构建器 - 构建器模式
 * @module views/erp/utils/DictionaryBuilder
 * @description 负责字典数据的构建、加载、缓存和搜索
 * 
 * @author JMH
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
      console.error(`字典加载失败：${this.name}`, error)
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
      console.log(`搜索字典：${this.name}, 关键词：${keyword}`)
      const data = await searcher(keyword)
      
      // 更新缓存
      this.cache = new DictionaryCache(data, this.ttl)
      console.log(` 字典搜索成功：${this.name}, 共 ${data.length} 条`)
      
      return data
    } catch (error) {
      console.error(`字典搜索失败：${this.name}`, error)
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
      console.log(`已清除字典缓存：${this.name}`)
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
    console.log(`[字典引擎] 注册字典：${name}, 类型：${builder.type}`)
    return this
  }

  /**
   * 获取字典构建器（增强日志）
   * @param {string} name - 字典名称
   * @returns {DictionaryBuilder|null}
   */
  get(name) {
    const builder = this.registry.get(name)
    if (!builder) {
      console.error(`[字典引擎] ❌ 字典未注册：${name}`)
      console.error(`[字典引擎] 当前已注册的字典：`, Array.from(this.registry.keys()))
      console.error(`[字典引擎] 调用堆栈:`, new Error().stack)
    } else {
      console.debug(`[字典引擎] ✅ 找到字典：${name}, 类型：${builder.type}, 状态：`, builder.getStatus())
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
    console.log('已清除所有字典缓存')
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
   * 从 JSON 配置批量构建字典（仅显示关键日志）
   * @param {Object} dictionaryConfig - 字典配置对象
   * @param {string} moduleCode - 模块编码
   * @returns {DictionaryBuilderEngine}
   */
  buildFromConfig(dictionaryConfig, moduleCode) {
    if (!dictionaryConfig) {
      console.warn(`[字典引擎] 字典配置为空`)
      return this
    }

    const dictionaries = dictionaryConfig.dictionaries || {}
    console.log('\n========== 后端返回的字典数据 ==========')
    console.log('字典列表:', Object.keys(dictionaries))
    
    // 🔍 只打印每个字典的关键信息
    for (const [dictName, dictConfig] of Object.entries(dictionaries)) {
      const { type, data, config } = dictConfig
      if (type === 'static') {
        console.log(`${dictName}: 类型=static, 数据量=${data?.length || 0}`)
        if (data && data.length > 0) {
          console.log(`  数据示例:`, data[0])
        }
      } else {
        console.log(`${dictName}: 类型=${type}`)
        // 🔍 打印完整的配置对象，查看后端返回的结构
        console.log(`  完整配置:`, JSON.stringify(dictConfig, null, 2))
      }
    }
    console.log('========================================\n')
    
    let successCount = 0
    let errorCount = 0
    
    for (const [dictName, dictConfig] of Object.entries(dictionaries)) {
      try {
        const { type, data, config } = dictConfig
        
        if (type === 'static' && Array.isArray(data)) {
          this.register(dictName, DictionaryBuilder.buildStatic(dictName, data))
          successCount++
        } else if (type === 'dynamic' || type === 'remote') {
          const builderConfig = config || {}
          const finalConfig = {
            ...builderConfig,
            type: type === 'remote' ? 'remote' : 'dynamic',
            // ✅ 优先使用配置的 api，否则使用新接口
            api: builderConfig.api || `/erp/engine/dict/union/${dictName}`,
            searchApi: dictName === 'nation'
              ? (builderConfig.searchApi || `/erp/engine/country/search?keyword={keyword}&limit=20`)
              : builderConfig.searchApi
          }
          this.register(dictName, DictionaryBuilder.buildDynamic(dictName, finalConfig))
          successCount++
        }
      } catch (error) {
        console.error(`构建失败：${dictName}`, error)
        errorCount++
      }
    }

    console.log(`构建完成：成功=${successCount}, 失败=${errorCount}, 总计=${this.registry.size}`)
    console.log(`已注册字典:`, Array.from(this.registry.keys()))
    
    return this
  }

  /**
   * 预加载所有字典（精简日志）
   * @param {string} moduleCode - 模块编码
   * @returns {Promise<void>}
   */
  async preloadAll(moduleCode) {
    const promises = []
    let loadCount = 0
    let skipCount = 0

    this.registry.forEach((builder, name) => {
      if (builder.type === 'dynamic') {
        const promise = builder.load(async () => {
          try {
            let apiUrl = builder.config?.api || `/erp/engine/dict/union/${name}`
            
            // 🔧 替换 URL 中的模板变量
            if (moduleCode) {
              apiUrl = apiUrl.replace(/{moduleCode}/g, moduleCode)
            }
            
            console.log(`🔍 [${name}] 请求 API:`, apiUrl)
            
            const response = await request({ url: apiUrl, method: 'get' })
            
            let data = []
            if (response.code === 200 || response.errorCode === 0) {
              const result = response.data || response
              data = [
                ...(result.dictTypeList || []),
                ...(result.dictDataList || [])
              ]
              console.log(`${name}: 后端返回数据 ${data.length} 条`)
            } else if (Array.isArray(response)) {
              data = response
              console.log(`${name}: 后端返回数据 ${data.length} 条（旧格式）`)
            }
            
            return data
          } catch (error) {
            console.error(`${name}: 加载失败`, error.message)
            return []
          }
        })
        promises.push(promise)
        loadCount++
      } else if (builder.type === 'static') {
        console.log(`${name}: 静态字典（已缓存）`)
        skipCount++
      } else if (builder.type === 'remote') {
        console.log(`${name}: 远程搜索（按需加载）`)
        skipCount++
      }
    })

    if (promises.length > 0) {
      await Promise.all(promises)
      console.log(`动态字典加载完成：成功=${loadCount}, 跳过=${skipCount}`)
    }
  }
}

// 导出单例
const engine = new DictionaryBuilderEngine()

// ==================== 调试工具（全局暴露） ====================
/**
 * 在浏览器控制台中手动检查字典状态
 * 使用方法：window.checkDict('orderStatus') 或 window.checkAllDicts()
 */
if (typeof window !== 'undefined') {
  // 检查单个字典
  window.checkDict = (dictName) => {
    const builder = engine.get(dictName)
    if (!builder) {
      console.error(`❌ 字典 "${dictName}" 未注册`)
      console.log(`当前已注册的字典:`, Array.from(engine.registry.keys()))
      return null
    }
    
    const status = builder.getStatus()
    const data = builder.get()
    
    console.log(`\n========== 字典 "${dictName}" 信息 ==========`)
    console.log(`名称：${dictName}`)
    console.log(`类型：${builder.type}`)
    console.log(`状态:`, status)
    console.log(`数据量：${data?.length || 0}`)
    console.log(`数据预览:`, data?.slice(0, 5))
    console.log(`完整配置:`, builder.config)
    
    return {
      name: dictName,
      type: builder.type,
      status,
      data,
      config: builder.config
    }
  }
  
  // 检查所有字典
  window.checkAllDicts = () => {
    console.log(`\n========== 所有字典状态 ==========`)
    console.log(`总数量：${engine.registry.size}`)
    console.log(`字典列表:`, Array.from(engine.registry.keys()))
    
    const allStatus = {}
    engine.registry.forEach((builder, name) => {
      allStatus[name] = {
        type: builder.type,
        status: builder.getStatus(),
        dataLength: builder.get()?.length || 0
      }
    })
    
    console.log(`详细状态:`, allStatus)
    return allStatus
  }
  
  // 打印字典引擎实例
  window.getDictEngine = () => {
    console.log('字典引擎实例:', engine)
    return engine
  }
  
  console.log('\n✅ 字典调试工具已加载:')
  console.log('  - window.checkDict(name) - 检查单个字典')
  console.log('  - window.checkAllDicts() - 检查所有字典')
  console.log('  - window.getDictEngine() - 获取引擎实例')
  console.log('使用方法：在控制台输入 checkDict("orderStatus")\n')
}

export default engine
export { DictionaryBuilder, DictionaryBuilderEngine }
