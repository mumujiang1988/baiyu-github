/**
 * ProductIngest 常量配置
 */

// 表单验证规则
export const FORM_RULES = {
  product_code: [
    { required: true, message: '请输入产品编码', trigger: 'blur' }
  ]
}

// 默认表单数据
export const DEFAULT_FORM = {
  product_code: '',
  name: '',
  spec: '',
  category: ''
}

// 目录结构选项
export const FOLDER_STRUCTURE_OPTIONS = [
  { label: '标准模式 (父/产品/图片)', value: 'standard' },
  { label: '场景模式 (父/产品/场景/图片)', value: 'scene' }
]

// 页签配置
export const TAB_CONFIG = {
  SINGLE: 'single',
  BATCH: 'batch'
}
