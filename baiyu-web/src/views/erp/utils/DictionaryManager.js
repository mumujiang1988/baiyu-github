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
      console.log('使用缓存的全部字典数据')
      return this.allDictData
    }
    
    // 如果正在加载中，等待完成
    if (this.isLoading && this.loadPromise) {
      console.log('等待字典加载中...')
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
        
        // 先放入字典类型
        dictTypeList.forEach(item => {
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
        
        // 再放入字典数据
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
        
        console.log(`✅ 字典加载完成：${Object.keys(groupedDicts).length} 个类型`)
        
        return groupedDicts
        
      } catch (error) {
        console.error('\n❌ [字典加载] 失败')
        console.error('错误信息:', error.message)
        console.error('完整错误:', error)
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
   * 获取字典选项（兼容 getDictOptions）
   * @param {string} dictName - 字典名称
   * @param {Array} staticOptions - 静态选项（优先级最高）
   * @returns {Array}
   */
  getDictOptions(dictName, staticOptions = null) {
    // 优先使用静态配置
    if (staticOptions && Array.isArray(staticOptions)) {
      return staticOptions
    }
    
    // 从全局管理器获取
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
    console.log('\n========== 字典管理器状态 ==========')
    console.log('已加载:', status.loaded)
    console.log('加载中:', status.loading)
    console.log('字典类型数量:', status.dictCount)
    console.log('字典类型列表:', status.dictTypes)
    console.log('=====================================\n')
    return status
  }
  
  // 获取单个字典
  window.getDict = (dictType) => {
    const dict = manager.getDict(dictType)
    console.log(`\n字典 "${dictType}":`, dict)
    return dict
  }
  
  // 重新加载
  window.reloadAllDicts = async () => {
    console.log('重新加载全部字典...')
    manager.clear()
    await manager.loadAll(true)
    window.checkDictManager()
  }
}

export default manager
export { DictionaryManager }
