import { ref, reactive } from 'vue'
import type { ErpConfig, ConfigQueryParams, EditFormData } from '../types/config'
import { listConfig, delConfig, getConfig, saveConfig } from '../api'

export function useConfigData() {
  // List state
  const loading = ref(false)
  const configList = ref<ErpConfig[]>([])
  const total = ref(0)
  
  // Query parameters
  const queryParams = reactive<ConfigQueryParams>({
    pageNum: 1,
    pageSize: 10,
    moduleCode: '',
    configName: '',
    configType: '',
    status: ''
  })
  
  // Initial edit form data
  const initialEditForm: EditFormData = {
    configId: null,
    moduleCode: '',
    configName: '',
    configType: '',
    pageConfig: '',
    formConfig: '',
    tableConfig: '',
    searchConfig: '',
    actionConfig: '',
    apiConfig: '',
    dictConfig: '',
    businessConfig: '',
    detailConfig: '',
    status: '1',
    isPublic: '0',
    remark: '',
    changeReason: '',
    version: 1
  }
  
  const getList = async () => {
    loading.value = true
    try {
      const res = await listConfig(queryParams)
      const data = res.data as { rows: ErpConfig[]; total: number }
      configList.value = data?.rows || []
      total.value = data?.total !== undefined ? data.total : 0
    } catch (error: any) {
      console.error('Failed to load config list:', error)
      configList.value = []
      total.value = 0
      // Error handling by caller
    } finally {
      loading.value = false
    }
  }
  
  const handleQuery = () => {
    queryParams.pageNum = 1
    getList()
  }
  
  const resetQuery = () => {
    queryParams.pageNum = 1
    queryParams.moduleCode = ''
    queryParams.configName = ''
    queryParams.configType = ''
    queryParams.status = ''
    getList()
  }
  
  const handleDelete = async (row: ErpConfig) => {
    try {
      await delConfig(row.configId!)
      // Success handling by caller
      await getList()
    } catch (error: any) {
      console.error('Failed to delete config:', error)
      throw error // Throw error for caller to handle
    }
  }
  
  const loadConfigDetail = async (configId: number): Promise<ErpConfig> => {
    try {
      const res = await getConfig(configId)
      const data = res.data as ErpConfig
      
      if (!data.configId) {
        throw new Error('配置不存在或已删除')
      }
      
      return data
    } catch (error: any) {
      console.error('Failed to load config detail:', error)
      throw error  
    }
  }
  
  const handleSave = async (data: Partial<EditFormData>, isNew: boolean) => {
    try {
      await saveConfig(data)
      // Success handling by caller
      await getList()
      return true
    } catch (error: any) {
      console.error('Failed to save config:', error)
      throw error // Throw error for caller to handle
    }
  }
  
  return {
    // State
    loading,
    configList,
    total,
    queryParams,
    initialEditForm,
    
    // Methods
    getList,
    handleQuery,
    resetQuery,
    handleDelete,
    loadConfigDetail,
    handleSave
  }
}
