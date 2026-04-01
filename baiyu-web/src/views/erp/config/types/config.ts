/**
 * ERP 配置管理 - TypeScript 类型定义
 */

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

// 查询参数类型
export interface ConfigQueryParams {
  pageNum: number
  pageSize: number
  moduleCode: string
  configName: string
  configType: string
  status: string
}

// 配置历史版本类型
export interface ConfigVersion {
  version: number
  configContent: string
  updateTime: string
  updateBy: string
  changeReason?: string
}

// 编辑表单数据类型
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

// 字典数据类型
export interface DictItem {
  type: string
  value: string
  label: string
  list?: number
  sort?: number
  remark?: string
}

// 分组后的字典数据
export interface GroupedDictData {
  [key: string]: DictItem[]
}

// 下拉选项类型
export interface SelectOption {
  label: string
  value: string
}

// ==================== 可视化配置元数据类型 ====================

/**
 * 表单组件类型
 */
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

/**
 * 数据源类型
 */
export type DataSourceType = 'manual' | 'api' | 'dictionary' | 'database'

/**
 * 验证规则类型
 */
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

/**
 * 配置项元数据
 */
export interface ConfigFieldMeta {
  field: string // 字段名（英文）
  label: string // 字段标签（中文）
  component: FormComponentType // 表单组件类型
  dataType?: string // 数据类型：string, number, boolean, array, object
  defaultValue?: any // 默认值
  placeholder?: string // 占位符
  required?: boolean // 是否必填
  disabled?: boolean // 是否禁用
  hidden?: boolean // 是否隐藏
  readonly?: boolean // 是否只读
  validation?: ValidationRule[] // 验证规则
  props?: Record<string, any> // 组件属性
  dictionary?: string // 关联字典（用于 select/radio/checkbox）
  dataSource?: DataSourceConfig // 数据源配置
  span?: number // 栅格占比（1-24）
  offset?: number // 栅格偏移
  helpText?: string // 帮助说明
  category?: string // 所属分类
  sort?: number // 排序号
}

/**
 * 数据源配置
 */
export interface DataSourceConfig {
  type: DataSourceType
  api?: string // API 接口地址
  method?: 'get' | 'post' | 'put' | 'delete'
  params?: Record<string, any> // 请求参数
  labelField?: string // 显示的字段名
  valueField?: string // 值的字段名
  childrenField?: string // 子级字段名（用于树形/级联）
  cacheable?: boolean // 是否可缓存
  cacheTTL?: number // 缓存时间（毫秒）
}

/**
 * 配置分组
 */
export interface ConfigGroup {
  name: string // 分组名（英文）
  label: string // 分组标签（中文）
  icon?: string // 图标
  fields: ConfigFieldMeta[] // 字段列表
  collapsed?: boolean // 是否默认折叠
  sort?: number // 排序号
}

/**
 * 配置类别元数据
 */
export interface ConfigCategoryMeta {
  category: string // 类别标识（与 configType 对应）
  title: string // 类别标题
  description?: string // 类别说明
  groups: ConfigGroup[] // 配置分组
  tableName?: string // 关联的数据库表
  supportTables?: boolean // 是否支持多表配置
}

/**
 * 数据库表信息
 */
export interface DatabaseTableInfo {
  tableName: string
  tableComment: string
  columns: DatabaseColumnInfo[]
}

/**
 * 数据库列信息
 */
export interface DatabaseColumnInfo {
  columnName: string
  columnType: string
  columnComment: string
  isNullable: 'YES' | 'NO'
  columnKey: string
  defaultValue: any
  extra: string
}

/**
 * 动态表单渲染上下文
 */
export interface FormRenderContext {
  model: Record<string, any>
  rules: Record<string, ValidationRule[]>
  metaMap: Map<string, ConfigFieldMeta>
  loadingFields: Set<string>
  updateModel: (field: string, value: any) => void
  validateField: (field: string) => Promise<void>
}

/**
 * 配置解析结果
 */
export interface ParsedConfigResult {
  formData: Record<string, any>
  validationRules: Record<string, ValidationRule[]>
  fieldMetas: ConfigFieldMeta[]
  groups: ConfigGroup[]
  errors: Array<{ field: string; message: string }>
}
