/**
 * Dictionary Data Global Manager - Singleton Pattern
 * @module views/erp/utils/DictionaryManager
 * @description Responsible for loading all dictionaries during page initialization, providing cache and access interfaces
 * 
 * @author JMH
 * @date 2026-03-27
 */

import request from '@/utils/request'

class DictionaryManager {
  constructor() {
    if (DictionaryManager.instance) {
      return DictionaryManager.instance
    }
    
    // All dictionary data cache
    this.allDictData = null
    // Loading status flag
    this.isLoading = false
    // Load completion Promise (for waiting)
    this.loadPromise = null
    
    DictionaryManager.instance = this
  }
  
  /**
   * Load all dictionary data at once (recommended)
   * @param {boolean} forceReload - Force reload
   * @returns {Promise<Object>}
   */
  async loadAll(forceReload = false) {
    // If already loaded, return directly
    if (this.allDictData && !forceReload) {
      return this.allDictData
    }
    
    // If loading, wait for completion
    if (this.isLoading && this.loadPromise) {
      return this.loadPromise
    }
    
    // Start loading
    this.isLoading = true
    this.loadPromise = (async () => {
      try {
        const response = await request({
          url: '/erp/engine/dict/all',
          method: 'get'
        })
        
        let result
        if (response.code === 200 || response.errorCode === 0) {
          result = response.data || response
        } else {
          throw new Error(`Load failed: ${response.msg || 'Unknown error'}`)
        }
        
        // Merge two segments of data
        const dictTypeList = result.dictTypeList || []
        const dictDataList = result.dictDataList || []
        
        // Group by type
        const groupedDicts = {}
        
        //  No longer put dictTypeList in result, only use dictDataList
        // Reason: dictTypeList is dictionary type definition, not actual dictionary data
        // Example: { label: '销售人员', value: 'salespersons', type: 'salespersons' } should not appear in dropdown options
        
        // Only process dictionary data list
        dictDataList.forEach(item => {
          const type = item.type
          if (!groupedDicts[type]) {
            groupedDicts[type] = []
          }
          // Avoid duplicates
          const exists = groupedDicts[type].some(d => d.value === item.value)
          if (!exists) {
            groupedDicts[type].push(item)
          }
        })
        
        // Cache result
        this.allDictData = groupedDicts
        
        return groupedDicts
        
      } catch (error) {
        this.allDictData = {}
        return {}
      } finally {
        this.isLoading = false
        this.loadPromise = null
      }
    })()
    
    return this.loadPromise
  }
  
  /**
   * Get dictionary by type
   * @param {string} dictType - Dictionary type
   * @returns {Array}
   */
  getDict(dictType) {
    if (!this.allDictData) {
      return []
    }
    
    const dict = this.allDictData[dictType]
    if (!dict) {
      return []
    }
    
    return dict
  }
  
  /**
   * Get dictionary options (uniformly loaded from backend)
   * @param {string} dictName - Dictionary name
   * @returns {Array}
   */
  getDictOptions(dictName) {
    // Uniformly loaded from backend, no longer support static config
    return this.getDict(dictName)
  }
  
  /**
   * Clear cache
   */
  clear() {
    this.allDictData = null
  }
  
  /**
   * Get cache status
   * @returns {Object}
   */
  getStatus() {
    return {
      loaded: !!this.allDictData,
      loading: this.isLoading,
      dictCount: this.allDictData ? Object.keys(this.allDictData).length : 0,
      dictTypes: this.allDictData ? Object.keys(this.allDictData) : []
    }
  }
}

// Export singleton
const manager = new DictionaryManager()

// ==================== Debug Tools (global exposure) ====================
if (typeof window !== 'undefined') {
  // Check dictionary status
  window.checkDictManager = () => {
    const status = manager.getStatus()
    return status
  }
  
  // Get single dictionary
  window.getDict = (dictType) => {
    const dict = manager.getDict(dictType)
    return dict
  }
  
  // Reload all
  window.reloadAllDicts = async () => {
    manager.clear()
    await manager.loadAll(true)
    window.checkDictManager()
  }
}

export default manager
export { DictionaryManager }
