/**
 * ERP 配置元数据定义 - 按 ERP 页面配置完全指南 v4.0 规范
 */
import type { ConfigCategoryMeta, ConfigFieldMeta } from '../types/config'

/**
 * 页面基础配置元数据
 */
export const pageConfigMeta: ConfigFieldMeta[] = [
  {
    field: 'pageId',
    label: '页面 ID',
    component: 'input',
    required: true,
    placeholder: '例如：saleorder',
    helpText: '页面的唯一标识，使用小写字母和下划线',
    span: 12,
    validation: [
      { required: true, message: '请输入页面 ID', trigger: 'blur' },
      { pattern: /^[a-z_]+$/, message: '只能包含小写字母和下划线', trigger: 'blur' }
    ]
  },
  {
    field: 'pageName',
    label: '页面名称',
    component: 'input',
    required: true,
    placeholder: '例如：销售订单管理',
    span: 12,
    validation: [{ required: true, message: '请输入页面名称', trigger: 'blur' }]
  },
  {
    field: 'permission',
    label: '权限标识',
    component: 'input',
    required: true,
    placeholder: '例如：k3:saleorder:query',
    helpText: '格式：k3:{模块}:操作',
    span: 12,
    validation: [{ required: true, message: '请输入权限标识', trigger: 'blur' }]
  },
  {
    field: 'layout',
    label: '页面布局',
    component: 'select',
    defaultValue: 'standard',
    span: 12,
    dictionary: 'page_layout',
    props: {
      options: [
        { label: '标准布局', value: 'standard' },
        { label: '简洁布局', value: 'simple' },
        { label: '全屏布局', value: 'fullscreen' }
      ]
    }
  },
  {
    field: 'apiPrefix',
    label: 'API 前缀',
    component: 'input',
    defaultValue: '/erp/engine',
    placeholder: '例如：/erp/engine',
    span: 12,
    validation: [{ required: true, message: '请输入 API 前缀', trigger: 'blur' }]
  },
  {
    field: 'tableName',
    label: '主表名',
    component: 'input',
    placeholder: '例如：t_sale_order',
    span: 12,
    helpText: '对应的数据库主表名',
    validation: [{ required: true, message: '请输入主表名', trigger: 'blur' }]
  },
  {
    field: 'billNoField',
    label: '单据编号字段',
    component: 'input',
    defaultValue: 'FBillNo',
    placeholder: '例如：FBillNo',
    span: 12,
    helpText: '单据编号的字段名'
  }
]

/**
 * 表单配置元数据
 */
export const formConfigMeta: ConfigFieldMeta[] = [
  {
    field: 'dialogWidth',
    label: '对话框宽度',
    component: 'input',
    defaultValue: '1400px',
    placeholder: '例如：1400px 或 80%',
    span: 8,
    helpText: '支持 px、%、vw 单位'
  },
  {
    field: 'labelWidth',
    label: '标签宽度',
    component: 'input',
    defaultValue: '120px',
    placeholder: '例如：120px',
    span: 8,
    helpText: '表单标签的固定宽度'
  },
  {
    field: 'layout',
    label: '表单布局',
    component: 'select',
    defaultValue: 'horizontal',
    span: 8,
    props: {
      options: [
        { label: '水平布局', value: 'horizontal' },
        { label: '垂直布局', value: 'vertical' },
        { label: '内联布局', value: 'inline' }
      ]
    }
  }
  // 字段列表在可视化编辑器中动态添加
]

/**
 * 表格配置元数据
 */
export const tableConfigMeta: ConfigFieldMeta[] = [
  {
    field: 'tableName',
    label: '表名',
    component: 'input',
    required: true,
    placeholder: '例如：t_sale_order',
    span: 12,
    validation: [{ required: true, message: '请输入表名', trigger: 'blur' }]
  },
  {
    field: 'primaryKey',
    label: '主键字段',
    component: 'input',
    defaultValue: 'id',
    placeholder: '例如：id',
    span: 12,
    helpText: '表格的主键字段名'
  },
  {
    field: 'border',
    label: '边框',
    component: 'switch',
    defaultValue: true,
    span: 6,
    helpText: '是否显示纵向边框'
  },
  {
    field: 'stripe',
    label: '斑马纹',
    component: 'switch',
    defaultValue: true,
    span: 6,
    helpText: '是否使用斑马纹样式'
  },
  {
    field: 'defaultPageSize',
    label: '默认每页条数',
    component: 'input-number',
    defaultValue: 10,
    span: 6,
    props: {
      min: 5,
      max: 100,
      step: 5
    }
  },
  {
    field: 'showPagination',
    label: '显示分页',
    component: 'switch',
    defaultValue: true,
    span: 6,
    helpText: '是否显示分页控件'
  }
  // 列配置在可视化编辑器中动态添加
]

/**
 * 查询配置元数据
 */
export const searchConfigMeta: ConfigFieldMeta[] = [
  {
    field: 'showSearch',
    label: '显示查询',
    component: 'switch',
    defaultValue: true,
    span: 8
  },
  {
    field: 'defaultExpand',
    label: '默认展开',
    component: 'switch',
    defaultValue: true,
    span: 8,
    helpText: '查询条件是否默认展开'
  },
  {
    field: 'labelWidth',
    label: '标签宽度',
    component: 'input',
    defaultValue: '100px',
    placeholder: '例如：100px',
    span: 8
  }
  // 查询字段在可视化编辑器中动态添加
]

/**
 * 按钮操作配置元数据
 */
export const actionConfigMeta: ConfigFieldMeta[] = [
  {
    field: 'toolbarPosition',
    label: '工具栏位置',
    component: 'select',
    defaultValue: 'top',
    span: 12,
    props: {
      options: [
        { label: '顶部', value: 'top' },
        { label: '底部', value: 'bottom' }
      ]
    }
  },
  {
    field: 'showToolbar',
    label: '显示工具栏',
    component: 'switch',
    defaultValue: true,
    span: 12
  }
  // 按钮配置在可视化编辑器中动态添加
]

/**
 * API 配置元数据
 */
export const apiConfigMeta: ConfigFieldMeta[] = [
  {
    field: 'baseUrl',
    label: '基础路径',
    component: 'input',
    required: true,
    placeholder: '例如：/api/saleorder',
    span: 24,
    validation: [{ required: true, message: '请输入基础路径', trigger: 'blur' }],
    helpText: 'API 接口的基础路径'
  }
  // API 方法在可视化编辑器中动态添加
]

/**
 * 字典配置元数据
 */
export const dictConfigMeta: ConfigFieldMeta[] = [
  {
    field: 'enabled',
    label: '启用字典构建器',
    component: 'switch',
    defaultValue: true,
    span: 24,
    helpText: '是否启用自动字典构建功能'
  },
  {
    field: 'globalCacheEnabled',
    label: '启用全局缓存',
    component: 'switch',
    defaultValue: true,
    span: 12,
    helpText: '是否启用字典数据全局缓存'
  },
  {
    field: 'globalCacheTTL',
    label: '缓存时间 (毫秒)',
    component: 'input-number',
    defaultValue: 300000,
    span: 12,
    props: {
      min: 60000,
      max: 86400000,
      step: 60000
    },
    helpText: '字典缓存的有效期（毫秒）'
  }
  // 字典数据源在可视化编辑器中动态添加
]

/**
 * 业务配置元数据
 */
export const businessConfigMeta: ConfigFieldMeta[] = [
  {
    field: 'entityName',
    label: '实体名称',
    component: 'input',
    required: true,
    placeholder: '例如：销售订单',
    span: 12,
    validation: [{ required: true, message: '请输入实体名称', trigger: 'blur' }],
    helpText: '业务的中文名称'
  },
  {
    field: 'entityNameSingular',
    label: '实体简称',
    component: 'input',
    placeholder: '例如：订单',
    span: 12,
    helpText: '业务的简称'
  },
  {
    field: 'addDialogTitle',
    label: '新增对话框标题',
    component: 'input',
    defaultValue: '新增{entityName}',
    placeholder: '例如：新增{entityName}',
    span: 12,
    helpText: '支持模板变量：{entityName}'
  },
  {
    field: 'editDialogTitle',
    label: '编辑对话框标题',
    component: 'input',
    defaultValue: '编辑{entityName}',
    placeholder: '例如：编辑{entityName}',
    span: 12,
    helpText: '支持模板变量：{entityName}'
  },
  {
    field: 'confirmDeleteMessage',
    label: '删除确认提示',
    component: 'textarea',
    defaultValue: '是否确认删除选中的 {count} 条数据？',
    placeholder: '例如：是否确认删除选中的 {count} 条数据？',
    span: 24,
    helpText: '支持模板变量：{count}'
  }
]

/**
 * 详情页签配置元数据
 */
export const detailConfigMeta: ConfigFieldMeta[] = [
  {
    field: 'enabled',
    label: '启用详情页',
    component: 'switch',
    defaultValue: true,
    span: 24
  },
  {
    field: 'displayType',
    label: '展示类型',
    component: 'select',
    defaultValue: 'drawer',
    span: 12,
    props: {
      options: [
        { label: '抽屉', value: 'drawer' },
        { label: '对话框', value: 'dialog' }
      ]
    }
  },
  {
    field: 'drawerWidth',
    label: '抽屉宽度',
    component: 'input',
    defaultValue: '60%',
    placeholder: '例如：60%',
    span: 12,
    helpText: '抽屉的宽度，支持 px、%'
  },
  {
    field: 'drawerDirection',
    label: '抽屉方向',
    component: 'select',
    defaultValue: 'rtl',
    span: 12,
    props: {
      options: [
        { label: '从右向左', value: 'rtl' },
        { label: '从左向右', value: 'ltr' }
      ]
    }
  },
  {
    field: 'loadStrategy',
    label: '加载策略',
    component: 'select',
    defaultValue: 'lazy',
    span: 12,
    props: {
      options: [
        { label: '懒加载', value: 'lazy' },
        { label: '预加载', value: 'eager' }
      ]
    },
    helpText: '懒加载：打开时才加载；预加载：列表加载时就加载'
  }
  // 页签配置在可视化编辑器中动态添加
]

/**
 * 配置类别元数据映射
 */
export const configCategoryMetas: Record<string, ConfigCategoryMeta> = {
  PAGE: {
    category: 'PAGE',
    title: '页面配置',
    description: '配置页面的基本信息、权限、布局等',
    groups: [
      {
        name: 'basic',
        label: '基本信息',
        icon: 'InfoFilled',
        fields: pageConfigMeta,
        sort: 1
      }
    ],
    tableName: 'erp_page_config',
    supportTables: false
  },
  FORM: {
    category: 'FORM',
    title: '表单配置',
    description: '配置新增/编辑对话框的表单字段和布局',
    groups: [
      {
        name: 'formSettings',
        label: '表单设置',
        icon: 'Setting',
        fields: formConfigMeta,
        sort: 1
      },
      {
        name: 'fields',
        label: '字段列表',
        icon: 'Document',
        fields: [],
        sort: 2
      }
    ],
    tableName: 't_sale_order',
    supportTables: true
  },
  TABLE: {
    category: 'TABLE',
    title: '表格配置',
    description: '配置列表的表格列、分页、排序等',
    groups: [
      {
        name: 'tableSettings',
        label: '表格设置',
        icon: 'Setting',
        fields: tableConfigMeta,
        sort: 1
      },
      {
        name: 'columns',
        label: '列配置',
        icon: 'Grid',
        fields: [],
        sort: 2
      }
    ],
    tableName: 't_sale_order',
    supportTables: true
  },
  SEARCH: {
    category: 'SEARCH',
    title: '查询配置',
    description: '配置查询条件的表单和逻辑',
    groups: [
      {
        name: 'searchSettings',
        label: '查询设置',
        icon: 'Setting',
        fields: searchConfigMeta,
        sort: 1
      },
      {
        name: 'searchFields',
        label: '查询字段',
        icon: 'Search',
        fields: [],
        sort: 2
      }
    ],
    tableName: 't_sale_order',
    supportTables: true
  },
  ACTION: {
    category: 'ACTION',
    title: '按钮配置',
    description: '配置工具栏和行操作按钮',
    groups: [
      {
        name: 'actionSettings',
        label: '按钮设置',
        icon: 'Setting',
        fields: actionConfigMeta,
        sort: 1
      },
      {
        name: 'toolbarButtons',
        label: '工具栏按钮',
        icon: 'Pointer',
        fields: [],
        sort: 2
      },
      {
        name: 'rowButtons',
        label: '行操作按钮',
        icon: 'List',
        fields: [],
        sort: 3
      }
    ],
    tableName: 't_sale_order',
    supportTables: false
  },
  API: {
    category: 'API',
    title: '接口配置',
    description: '配置 API 接口地址和方法',
    groups: [
      {
        name: 'apiSettings',
        label: '接口设置',
        icon: 'Link',
        fields: apiConfigMeta,
        sort: 1
      },
      {
        name: 'apiMethods',
        label: '接口方法',
        icon: 'Connection',
        fields: [],
        sort: 2
      }
    ],
    tableName: 'erp_page_config',
    supportTables: false
  },
  DICT: {
    category: 'DICT',
    title: '字典配置',
    description: '配置字典数据源和缓存策略',
    groups: [
      {
        name: 'dictSettings',
        label: '字典设置',
        icon: 'Setting',
        fields: dictConfigMeta,
        sort: 1
      },
      {
        name: 'dataSources',
        label: '数据源',
        icon: 'Grid',
        fields: [],
        sort: 2
      }
    ],
    tableName: 'erp_page_config',
    supportTables: false
  },
  BUSINESS: {
    category: 'BUSINESS',
    title: '业务配置',
    description: '配置业务规则、消息提示等',
    groups: [
      {
        name: 'businessSettings',
        label: '业务设置',
        icon: 'Briefcase',
        fields: businessConfigMeta,
        sort: 1
      }
    ],
    tableName: 'erp_page_config',
    supportTables: false
  },
  DETAIL: {
    category: 'DETAIL',
    title: '详情配置',
    description: '配置详情页签和数据加载',
    groups: [
      {
        name: 'detailSettings',
        label: '详情设置',
        icon: 'Setting',
        fields: detailConfigMeta,
        sort: 1
      },
      {
        name: 'tabs',
        label: '页签列表',
        icon: 'Tickets',
        fields: [],
        sort: 2
      }
    ],
    tableName: 't_sale_order',
    supportTables: true
  }
}
