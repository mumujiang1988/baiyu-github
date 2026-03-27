/**
 * ERP 字典数据加载器 - 增强版
 * @module views/erp/utils/DictionaryLoader
 * @description 专门用于加载新的分段返回字典接口数据
 * 
 * @author JMH
 * @date 2026-03-27
 */

import request from '@/utils/request'

/**
 * 字典加载器类
 */
class DictionaryLoader {
  /**
   * 加载单个字典（按类型）
   * @param {string} dictType - 字典类型（如 'currency', 'sys_user_sex'）
   * @param {Object} options - 配置选项
   * @returns {Promise<Object>} 返回 { dictTypeList, dictDataList }
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
        
        // 如果 mergeData 为 true，则合并两段数据
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
        
        // 否则返回分段数据
        return {
          dictTypeList: result.dictTypeList || [],
          dictDataList: result.dictDataList || []
        }
      }
      
      throw new Error(response.msg || '字典加载失败')
    } catch (error) {
      console.error(`加载字典失败 [${dictType}]:`, error)
      return {
        dictTypeList: [],
        dictDataList: [],
        allData: []
      }
    }
  }

  /**
   * 加载所有字典
   * @returns {Promise<Object>} 返回 { dictTypeList, dictDataList }
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
      
      throw new Error(response.msg || '字典加载失败')
    } catch (error) {
      console.error('加载所有字典失败:', error)
      return {
        dictTypeList: [],
        dictDataList: [],
        allData: []
      }
    }
  }

  /**
   * 仅加载字典类型列表
   * @param {string} dictType - 字典类型（可选，用于过滤）
   * @returns {Promise<Array>} 返回 dictTypeList
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
      console.error('加载字典类型失败:', error)
      return []
    }
  }

  /**
   * 仅加载字典数据列表
   * @param {string} dictType - 字典类型（可选，用于过滤）
   * @returns {Promise<Array>} 返回 dictDataList
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
      console.error('加载字典数据失败:', error)
      return []
    }
  }

  /**
   * 批量加载多个字典类型
   * @param {Array<string>} dictTypes - 字典类型数组
   * @returns {Promise<Map>} 返回 Map<dictType, {dictTypeList, dictDataList}>
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
   * 缓存版本的加载器（带 TTL）
   */
  static cache = new Map()
  static cacheTimestamp = new Map()

  /**
   * 加载字典（带缓存）
   * @param {string} dictType - 字典类型
   * @param {number} ttl - 缓存时间（毫秒），默认 5 分钟
   * @returns {Promise<Object>}
   */
  static async loadWithCache(dictType, ttl = 5 * 60 * 1000) {
    const now = Date.now()
    const cached = this.cache.get(dictType)
    const timestamp = this.cacheTimestamp.get(dictType)
    
    // 检查缓存是否有效
    if (cached && timestamp && (now - timestamp < ttl)) {
      console.debug(`💾 使用缓存字典：${dictType}`)
      return cached
    }
    
    // 重新加载
    console.log(`🌐 加载字典：${dictType}`)
    const data = await this.loadByType(dictType)
    
    // 更新缓存
    this.cache.set(dictType, data)
    this.cacheTimestamp.set(dictType, now)
    
    console.log(`字典加载成功：${dictType}, 共 ${data.allData?.length || 0} 条`)
    return data
  }

  /**
   * 清除缓存
   * @param {string} dictType - 字典类型（可选，不传则清除所有）
   */
  static clearCache(dictType = null) {
    if (dictType) {
      this.cache.delete(dictType)
      this.cacheTimestamp.delete(dictType)
      console.log(`已清除字典缓存：${dictType}`)
    } else {
      this.cache.clear()
      this.cacheTimestamp.clear()
      console.log('已清除所有字典缓存')
    }
  }
}

// 导出默认实例
export default DictionaryLoader
