// 配置项类型
export interface ErpConfig {
  configId: number | null
  moduleCode: string
  configName: string
  configType: string
  pageConfig?: string
  formConfig?: string
  tableConfig?: string
  searchConfig?: string
  actionConfig?: string
  apiConfig?: string
  dictConfig?: string
  businessConfig?: string
  detailConfig?: string
  version: number
  status: '0' | '1'
  isPublic: '0' | '1'
  remark: string
  createTime?: string
  updateTime?: string
  createBy?: string
  updateBy?: string
}

export interface ConfigQueryParams {
  pageNum: number
  pageSize: number
  moduleCode: string
  configName: string
  configType: string
  status: string
}

export interface ConfigVersion {
  version: number
  configContent: string
  updateTime: string
  updateBy: string
  changeReason?: string
}

export interface EditFormData {
  configId: number | null
  moduleCode: string
  configName: string
  configType: string
  pageConfig: string
  formConfig: string
  tableConfig: string
  searchConfig: string
  actionConfig: string
  apiConfig: string
  dictConfig: string
  businessConfig: string
  detailConfig: string
  status: '0' | '1'
  isPublic: '0' | '1'
  remark: string
  changeReason: string
  version: number
}

export interface DictItem {
  type: string
  value: string
  label: string
  list?: number
  sort?: number
  remark?: string
}

export interface GroupedDictData {
  [key: string]: DictItem[]
}

export interface SelectOption {
  label: string
  value: string
}

export type FormComponentType = 
  | 'input' 
  | 'input-number' 
  | 'textarea' 
  | 'select' 
  | 'radio' 
  | 'checkbox' 
  | 'switch' 
  | 'date-picker'
  | 'time-picker'
  | 'color-picker'
  | 'upload'
  | 'cascader'
  | 'slider'
  | 'rate'
  | 'editor' // 富文本编辑器
  | 'json-editor'
  | 'custom-form-fields-editor' // 自定义表单字段可视化编辑器
  | 'custom-table-columns-editor' // 自定义表格列可视化编辑器
  | 'custom-search-fields-editor' // 自定义查询字段可视化编辑器
  | 'custom-relation-tabs-editor' // 自定义关系页签可视化编辑器

export type DataSourceType = 'manual' | 'api' | 'dictionary' | 'database'

export interface ValidationRule {
  required?: boolean
  message?: string
  trigger?: 'blur' | 'change' | 'submit'
  type?: string // 例如：'string', 'number', 'array', 'date'
  min?: number
  max?: number
  len?: number
  pattern?: RegExp
  validator?: (rule: any, value: any, callback: Function) => void
}

export interface ConfigFieldMeta {
  field: string
  label: string
  component: FormComponentType
  dataType?: string
  defaultValue?: any
  placeholder?: string
  required?: boolean
  disabled?: boolean
  hidden?: boolean
  readonly?: boolean
  validation?: ValidationRule[]
  props?: Record<string, any>
  dictionary?: string
  dataSource?: DataSourceConfig
  span?: number
  offset?: number
  helpText?: string
  category?: string
  sort?: number
}

export interface DataSourceConfig {
  type: DataSourceType
  api?: string
  method?: 'get' | 'post' | 'put' | 'delete'
  params?: Record<string, any>
  labelField?: string
  valueField?: string
  childrenField?: string
  cacheable?: boolean
  cacheTTL?: number
}

export interface ConfigGroup {
  name: string
  label: string
  icon?: string
  fields: ConfigFieldMeta[]
  collapsed?: boolean
  sort?: number
}

export interface ConfigCategoryMeta {
  category: string
  title: string
  description?: string
  groups: ConfigGroup[]
  tableName?: string
  supportTables?: boolean
}

export interface DatabaseTableInfo {
  tableName: string
  tableComment: string
  columns: DatabaseColumnInfo[]
}

export interface DatabaseColumnInfo {
  columnName: string
  columnType: string
  columnComment: string
  isNullable: 'YES' | 'NO'
  columnKey: string
  defaultValue: any
  extra: string
}

export interface FormRenderContext {
  model: Record<string, any>
  rules: Record<string, ValidationRule[]>
  metaMap: Map<string, ConfigFieldMeta>
  loadingFields: Set<string>
  updateModel: (field: string, value: any) => void
  validateField: (field: string) => Promise<void>
}

export interface ParsedConfigResult {
  formData: Record<string, any>
  validationRules: Record<string, ValidationRule[]>
  fieldMetas: ConfigFieldMeta[]
  groups: ConfigGroup[]
  errors: Array<{ field: string; message: string }>
}
