/**
 * ERP Dictionary Builder - Builder Pattern
 * @module views/erp/utils/DictionaryBuilder
 * @description Responsible for dictionary data building, loading, caching and searching
 * 
 * @author JMH
 * @date 2026-03-25
 */

import request from '@/utils/request'

/**
 * Dictionary cache internal class
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
 * Dictionary builder class
 */
class DictionaryBuilder {
  /**
   * Constructor
   * @param {string} name - Dictionary name
   * @param {Object} options - Config options
   */
  constructor(name, options = {}) {
    this.name = name
    this.type = options.type || 'static' // static | dynamic | remote
    this.cache = null
  }

  /**
   * Build dynamic dictionary (only supports backend loading)
   * @param {string} name - Dictionary name
   * @param {Object} config - Config object (must contain api field)
   * @returns {DictionaryBuilder}
   */
  static buildDynamic(name, config) {
    if (!config || !config.api) {
      throw new Error(`Dictionary "${name}" config must contain api field for loading data from backend`)
    }
    
    const builder = new DictionaryBuilder(name, {
      type: 'dynamic',
      config: config,
      ttl: config.ttl || 5 * 60 * 1000
    })
    return builder
  }

  /**
   * Build remote search dictionary
   * @param {string} name - Dictionary name
   * @param {Object} config - Config object
   * @returns {DictionaryBuilder}
   */
  static buildRemoteSearch(name, config) {
    const builder = new DictionaryBuilder(name, {
      type: 'remote',
      config: config,
      ttl: config.ttl || 5 * 60 * 1000
    })
    return builder
  }

  /**
   * Load dictionary data
   * @param {Function} loader - Data load function
   * @returns {Promise<Array>}
   */
  async load(loader) {
    // Check if cache is valid
    if (this.cache && this.cache.data && !this.cache.isExpired()) {
      return this.cache.data
    }

    try {
      const data = await loader()
      
      // Update cache
      this.cache = new DictionaryCache(data, this.ttl)
      
      return data
    } catch (error) {
      if (this.cache) {
        this.cache.error = error.message
      }
      return []
    }
  }

  /**
   * Remote search dictionary
   * @param {string} keyword - Search keyword
   * @param {Function} searcher - Search function
   * @returns {Promise<Array>}
   */
  async search(keyword, searcher) {
    if (!this.cache) {
      return []
    }

    try {
      const data = await searcher(keyword)
      
      // Update cache
      this.cache = new DictionaryCache(data, this.ttl)
      
      return data
    } catch (error) {
      return []
    }
  }

  /**
   * Get dictionary data (directly from cache)
   * @returns {Array}
   */
  get() {
    if (!this.cache || !this.cache.data) {
      return []
    }
    return this.cache.data
  }

  /**
   * Clear cache
   */
  clear() {
    if (this.cache) {
      this.cache.loaded = false
      this.cache.data = null
      this.cache.timestamp = 0
    }
  }

  /**
   * Get dictionary status
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
 * Dictionary builder engine - Singleton pattern
 */
class DictionaryBuilderEngine {
  constructor() {
    if (DictionaryBuilderEngine.instance) {
      return DictionaryBuilderEngine.instance
    }

    // Dictionary registry
    this.registry = new Map()
    DictionaryBuilderEngine.instance = this
  }

  /**
   * Register dictionary
   * @param {string} name - Dictionary name
   * @param {DictionaryBuilder} builder - Dictionary builder instance
   * @returns {DictionaryBuilderEngine}
   */
  register(name, builder) {
    this.registry.set(name, builder)
    return this
  }

  /**
   * Get dictionary builder
   * @param {string} name - Dictionary name
   * @returns {DictionaryBuilder|null}
   */
  get(name) {
    const builder = this.registry.get(name)
    if (!builder) {
      console.error(`Dictionary not found: ${name}`)
    }
    return builder
  }

  /**
   * Load dictionary data
   * @param {string} name - Dictionary name
   * @param {Function} loader - Data load function
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
   * Search dictionary
   * @param {string} name - Dictionary name
   * @param {string} keyword - Search keyword
   * @param {Function} searcher - Search function
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
   * Get dictionary data (directly from cache)
   * @param {string} name - Dictionary name
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
   * Clear dictionary cache
   * @param {string} name - Dictionary name
   */
  clear(name) {
    const builder = this.registry.get(name)
    if (builder) {
      builder.clear()
    }
  }

  /**
   * Clear all dictionary caches
   */
  clearAll() {
    this.registry.forEach((builder, name) => {
      builder.clear()
    })
  }

  /**
   * Get all dictionary statuses
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
   * Build dictionaries from JSON config (only supports dynamic and remote types)
   * @param {Object} dictionaryConfig - Dictionary config object
   * @param {string} moduleCode - Module code
   * @returns {DictionaryBuilderEngine}
   */
  buildFromConfig(dictionaryConfig, moduleCode) {
    if (!dictionaryConfig) {
      return this
    }

    const dictionaries = dictionaryConfig.dictionaries || {}
    
    let successCount = 0
    let errorCount = 0
    
    for (const [dictName, dictConfig] of Object.entries(dictionaries)) {
      try {
        const { type, config } = dictConfig
        
        // No longer support static type
        if (type === 'static') {
          errorCount++
          continue
        }
        
        if (type === 'dynamic' || type === 'remote') {
          const builderConfig = config || {}
          const finalConfig = {
            ...builderConfig,
            type: type === 'remote' ? 'remote' : 'dynamic',
            // Must use backend API
            api: builderConfig.api || `/erp/engine/dict/union/${dictName}`,
            searchApi: dictName === 'nation'
              ? (builderConfig.searchApi || `/erp/engine/country/search?keyword={keyword}&limit=20`)
              : builderConfig.searchApi
          }
          this.register(dictName, DictionaryBuilder.buildDynamic(dictName, finalConfig))
          successCount++
        } else {
          errorCount++
        }
      } catch (error) {
        errorCount++
      }
    }

    return this
  }

  /**
   * Preload all dictionaries (simplified logs)
   * @param {string} moduleCode - Module code
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
            
            // Replace template variables in URL
            if (moduleCode) {
              apiUrl = apiUrl.replace(/{moduleCode}/g, moduleCode)
            }
            const response = await request({ url: apiUrl, method: 'get' })
            
            let data = []
            if (response.code === 200 || response.errorCode === 0) {
              const result = response.data || response
              data = [
                ...(result.dictTypeList || []),
                ...(result.dictDataList || [])
              ]
            }
            
            return data
          } catch (error) {
            console.error(`${name}: Load failed`, error.message)
            return []
          }
        })
        promises.push(promise)
        loadCount++
      } else if (builder.type === 'static') {
        skipCount++
      } else if (builder.type === 'remote') {
        skipCount++
      }
    })

    if (promises.length > 0) {
      await Promise.all(promises)
    }
  }
}

// Export singleton
const engine = new DictionaryBuilderEngine()

// ==================== Debug Tools (global exposure) ====================
/**
 * Manually check dictionary status in browser console
 * Usage: window.checkDict('orderStatus') or window.checkAllDicts()
 */
if (typeof window !== 'undefined') {
  // Check single dictionary
  window.checkDict = (dictName) => {
    const builder = engine.get(dictName)
    if (!builder) {
      console.error(`Dictionary "${dictName}" not registered`)
      console.log(`Currently registered dictionaries:`, Array.from(engine.registry.keys()))
      return null
    }
    
    const status = builder.getStatus()
    const data = builder.get()
    
    console.log(`\n========== Dictionary "${dictName}" Info ==========`)
    console.log(`Name: ${dictName}`)
    console.log(`Type: ${builder.type}`)
    console.log(`Status:`, status)
    console.log(`Data count: ${data?.length || 0}`)
    console.log(`Data preview:`, data?.slice(0, 5))
    console.log(`Full config:`, builder.config)
    
    return {
      name: dictName,
      type: builder.type,
      status,
      data,
      config: builder.config
    }
  }
  
  // Check all dictionaries
  window.checkAllDicts = () => {
    console.log(`\n========== All Dictionaries Status ==========`)
    console.log(`Total count: ${engine.registry.size}`)
    console.log(`Dictionary list:`, Array.from(engine.registry.keys()))
    
    const allStatus = {}
    engine.registry.forEach((builder, name) => {
      allStatus[name] = {
        type: builder.type,
        status: builder.getStatus(),
        dataLength: builder.get()?.length || 0
      }
    })
    
    console.log(`Detailed status:`, allStatus)
    return allStatus
  }
  
  // Print dictionary engine instance
  window.getDictEngine = () => {
    console.log('Dictionary engine instance:', engine)
    return engine
  }
}

export default engine
export { DictionaryBuilder, DictionaryBuilderEngine }
