/**
 * 字典数据全局管理器 - 单例模式
 * @module views/erp/utils/DictionaryManager
 * @description 负责页面初始化时一次性加载全部字典，并提供缓存和访问接口
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
    
    // 全部字典数据缓存
    this.allDictData = null
    // 加载状态标记
    this.isLoading = false
    // 加载完成的 Promise（用于等待）
    this.loadPromise = null
    
    DictionaryManager.instance = this
  }
  
  /**
   * 一次性加载全部字典数据（推荐）
   * @param {boolean} forceReload - 是否强制重新加载
   * @returns {Promise<Object>}
   */
  async loadAll(forceReload = false) {
    // 如果已加载完成，直接返回
    if (this.allDictData && !forceReload) {
      return this.allDictData
    }
    
    // 如果正在加载中，等待完成
    if (this.isLoading && this.loadPromise) {
      return this.loadPromise
    }
    
    // 开始加载
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
          throw new Error(`加载失败：${response.msg || '未知错误'}`)
        }
        
        // 合并两段数据
        const dictTypeList = result.dictTypeList || []
        const dictDataList = result.dictDataList || []
        
        // 按类型分组存储
        const groupedDicts = {}
        
        // ⚠️ 不再将 dictTypeList 放入结果中，仅使用 dictDataList
        // 原因：dictTypeList 是字典类型定义，不是实际的字典数据
        // 例如：{ label: '销售人员', value: 'salespersons', type: 'salespersons' } 不应该出现在下拉选项中
        
        // 只处理字典数据列表
        dictDataList.forEach(item => {
          const type = item.type
          if (!groupedDicts[type]) {
            groupedDicts[type] = []
          }
          // 避免重复
          const exists = groupedDicts[type].some(d => d.value === item.value)
          if (!exists) {
            groupedDicts[type].push(item)
          }
        })
        
        // 缓存结果
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
   * 获取指定类型的字典
   * @param {string} dictType - 字典类型
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
   * 获取字典选项（统一从后端加载）
   * @param {string} dictName - 字典名称
   * @returns {Array}
   */
  getDictOptions(dictName) {
    // 统一从后端加载，不再支持静态配置
    return this.getDict(dictName)
  }
  
  /**
   * 清除缓存
   */
  clear() {
    this.allDictData = null
  }
  
  /**
   * 获取缓存状态
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

// 导出单例
const manager = new DictionaryManager()

// ==================== 调试工具（全局暴露） ====================
if (typeof window !== 'undefined') {
  // 检查字典状态
  window.checkDictManager = () => {
    const status = manager.getStatus()
    return status
  }
  
  // 获取单个字典
  window.getDict = (dictType) => {
    const dict = manager.getDict(dictType)
    return dict
  }
  
  // 重新加载
  window.reloadAllDicts = async () => {
    manager.clear()
    await manager.loadAll(true)
    window.checkDictManager()
  }
}

export default manager
export { DictionaryManager }
