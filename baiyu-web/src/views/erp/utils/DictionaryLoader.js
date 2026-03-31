/**
 * ERP Dictionary Data Loader - Enhanced Version
 * @module views/erp/utils/DictionaryLoader
 * @description Specifically for loading segmented dictionary interface data
 * 
 * @author JMH
 * @date 2026-03-27
 */

import request from '@/utils/request'

/**
 * Dictionary loader class
 */
class DictionaryLoader {
  /**
   * Load single dictionary (by type)
   * @param {string} dictType - Dictionary type (e.g., 'currency', 'sys_user_sex')
   * @param {Object} options - Config options
   * @returns {Promise<Object>} Returns { dictTypeList, dictDataList }
   */
  static async loadByType(dictType, options = {}) {
    try {
      const { mergeData = true } = options
        
      const response = await request({
        url: `/erp/engine/dict/union/${dictType}`,
        method: 'get'
      })
        
      if (response.code === 200 || response.errorCode === 0) {
        const result = response.data || response
          
        // If mergeData is true, merge two segments of data
        if (mergeData) {
          return {
            dictTypeList: result.dictTypeList || [],
            dictDataList: result.dictDataList || [],
            allData: [
              ...(result.dictTypeList || []),
              ...(result.dictDataList || [])
            ]
          }
        }
          
        // Otherwise return segmented data
        return {
          dictTypeList: result.dictTypeList || [],
          dictDataList: result.dictDataList || []
        }
      }
        
      throw new Error(response.msg || 'Failed to load dictionary')
    } catch (error) {
      console.error(`Failed to load dictionary [${dictType}]:`, error)
      return {
        dictTypeList: [],
        dictDataList: [],
        allData: []
      }
    }
  }

  /**
   * Load all dictionaries
   * @returns {Promise<Object>} Returns { dictTypeList, dictDataList }
   */
  static async loadAll() {
    try {
      const response = await request({
        url: '/erp/engine/dict/all',
        method: 'get'
      })
      
      if (response.code === 200 || response.errorCode === 0) {
        const result = response.data || response
        
        return {
          dictTypeList: result.dictTypeList || [],
          dictDataList: result.dictDataList || [],
          allData: [
            ...(result.dictTypeList || []),
            ...(result.dictDataList || [])
          ]
        }
      }
      
      throw new Error(response.msg || 'Failed to load dictionary')
    } catch (error) {
      console.error('Failed to load all dictionaries:', error)
      return {
        dictTypeList: [],
        dictDataList: [],
        allData: []
      }
    }
  }

  /**
   * Load only dictionary type list
   * @param {string} dictType - Dictionary type (optional, for filtering)
   * @returns {Promise<Array>} Returns dictTypeList
   */
  static async loadDictTypes(dictType = null) {
    try {
      const url = dictType 
        ? `/erp/engine/dict/union/${dictType}`
        : '/erp/engine/dict/all'
      
      const response = await request({
        url,
        method: 'get'
      })
      
      if (response.code === 200 || response.errorCode === 0) {
        const result = response.data || response
        return result.dictTypeList || []
      }
      
      return []
    } catch (error) {
      console.error('Failed to load dictionary type:', error)
      return []
    }
  }

  /**
   * Load only dictionary data list
   * @param {string} dictType - Dictionary type (optional, for filtering)
   * @returns {Promise<Array>} Returns dictDataList
   */
  static async loadDictData(dictType = null) {
    try {
      const url = dictType 
        ? `/erp/engine/dict/union/${dictType}`
        : '/erp/engine/dict/all'
      
      const response = await request({
        url,
        method: 'get'
      })
      
      if (response.code === 200 || response.errorCode === 0) {
        const result = response.data || response
        return result.dictDataList || []
      }
      
      return []
    } catch (error) {
      console.error('Failed to load dictionary data:', error)
      return []
    }
  }

  /**
   * Batch load multiple dictionary types
   * @param {Array<string>} dictTypes - Dictionary type array
   * @returns {Promise<Map>} Returns Map<dictType, {dictTypeList, dictDataList}>
   */
  static async loadBatch(dictTypes) {
    const promises = dictTypes.map(type => this.loadByType(type))
    const results = await Promise.all(promises)
    
    const resultMap = new Map()
    dictTypes.forEach((type, index) => {
      resultMap.set(type, results[index])
    })
    
    return resultMap
  }

  /**
   * Cached version of loader (with TTL)
   */
  static cache = new Map()
  static cacheTimestamp = new Map()

  /**
   * Load dictionary (with cache)
   * @param {string} dictType - Dictionary type
   * @param {number} ttl - Cache time in milliseconds, default 5 minutes
   * @returns {Promise<Object>}
   */
  static async loadWithCache(dictType, ttl = 5 * 60 * 1000) {
    const now = Date.now()
    const cached = this.cache.get(dictType)
    const timestamp = this.cacheTimestamp.get(dictType)
    
    // Check if cache is valid
    if (cached && timestamp && (now - timestamp < ttl)) {
      return cached
    }
    
    // Reload
    const data = await this.loadByType(dictType)
    
    // Update cache
    this.cache.set(dictType, data)
    this.cacheTimestamp.set(dictType, now)
    
    return data
  }

  /**
   * Clear cache
   * @param {string} dictType - Dictionary type (optional, clear all if not provided)
   */
  static clearCache(dictType = null) {
    if (dictType) {
      this.cache.delete(dictType)
      this.cacheTimestamp.delete(dictType)
    } else {
      this.cache.clear()
      this.cacheTimestamp.clear()
    }
  }
}

// Export default instance
export default DictionaryLoader
