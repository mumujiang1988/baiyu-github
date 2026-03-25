/**
 * 可配置表格组件
 * 基于 JSON 配置动态渲染表格
 */

import { ref, computed, watch } from 'vue'
import { ElTable, ElTableColumn, ElTag, ElLink, ElButton, ElSpace } from 'element-plus'

export default {
  name: 'ConfigurableTable',
  
  props: {
    // 表格配置
    config: {
      type: Object,
      required: true
    },
    // 表格数据
    data: {
      type: Array,
      default: () => []
    },
    // 加载状态
    loading: {
      type: Boolean,
      default: false
    },
    // 操作按钮配置
    actionConfig: {
      type: Object,
      default: () => ({})
    },
    // 虚拟字段配置
    virtualFields: {
      type: Array,
      default: () => []
    }
  },

  emits: ['selection-change', 'expand-change', 'action-click', 'row-click'],

  setup(props, { emit, slots }) {
    const selection = ref([])

    // 处理后的数据（包含虚拟字段）
    const processedData = computed(() => {
      if (!props.virtualFields || props.virtualFields.length === 0) {
        return props.data
      }

      return props.data.map(row => {
        const computedFields = {}
        props.virtualFields.forEach(field => {
          try {
            const computeFn = new Function('row', `return ${field.computeRule}`)
            Object.assign(computedFields, { [field.name]: computeFn(row) })
          } catch (error) {
            console.error(`计算虚拟字段 ${field.name} 失败:`, error)
            computedFields[field.name] = null
          }
        })
        return { ...row, ...computedFields }
      })
    })

    // 可见的列
    const visibleColumns = computed(() => {
      return props.config.columns.filter(col => col.visible !== false)
    })

    // 获取单元格值
    const getCellValue = (row, column) => {
      const value = row[column.prop]

      if (column.formatter) {
        return column.formatter(row)
      }

      return value ?? '-'
    }

    // 获取渲染组件
    const getRenderComponent = (column) => {
      switch (column.renderType) {
        case 'tag':
          return 'ElTag'
        case 'link':
          return 'ElLink'
        case 'badge':
          return 'ElBadge'
        default:
          return 'span'
      }
    }

    // 获取渲染属性
    const getRenderProps = (column, row) => {
      switch (column.renderType) {
        case 'tag':
          const tagConfig = column.formatter?.(row) || {}
          return {
            type: tagConfig.type || 'info',
            size: 'small'
          }
        case 'link':
          return {
            type: 'primary',
            underline: false
          }
        case 'badge':
          const value = row[column.prop]
          return {
            value: value,
            hidden: !value
          }
        default:
          return {}
      }
    }

    // 获取可见的行操作
    const getVisibleRowActions = (row) => {
      if (!props.actionConfig.row) return []

      return props.actionConfig.row.filter(action => {
        if (action.visible && typeof action.visible === 'string') {
          try {
            const visibleFn = new Function('row', `return ${action.visible}`)
            return visibleFn(row)
          } catch (error) {
            console.error('评估操作可见性失败:', error)
            return false
          }
        }
        return true
      })
    }

    // 处理选择变化
    const handleSelectionChange = (selection) => {
      emit('selection-change', selection)
    }

    // 处理展开变化
    const handleExpandChange = (row, expandedRows) => {
      emit('expand-change', row, expandedRows)
    }

    // 处理操作点击
    const handleActionClick = (handlerName, row) => {
      emit('action-click', handlerName, row)
    }

    // 处理行点击
    const handleRowClick = (row, column, event) => {
      emit('row-click', row, column, event)
    }

    return () => (
      <ElTable
        v-loading={props.loading}
        data={processedData.value}
        row-key={props.config.rowKey}
        border={props.config.border}
        stripe={props.config.stripe}
        max-height={props.config.maxHeight}
        show-overflow-tooltip={props.config.showOverflowTooltip}
        resizable={props.config.resizable}
        onSelection-change={handleSelectionChange}
        onExpand-change={handleExpandChange}
      >
        {visibleColumns.value.map((column, index) => {
          // 选择列
          if (column.type === 'selection') {
            return (
              <ElTableColumn
                key={index}
                type="selection"
                width={column.width}
                fixed={column.fixed}
                resizable={column.resizable}
              />
            )
          }

          // 展开列
          if (column.type === 'expand') {
            return (
              <ElTableColumn
                key={index}
                type="expand"
                width={column.width}
                fixed={column.fixed}
              >
                {{
                  header: () => <span>{column.label}</span>,
                  default: (scope) => slots.expand?.(scope) || slots.default?.(scope)
                }}
              </ElTableColumn>
            )
          }

          // 普通列
          return (
            <ElTableColumn
              key={index}
              prop={column.prop}
              label={column.label}
              width={column.width}
              min-width={column.minWidth}
              fixed={column.fixed}
              align={column.align}
              sortable={column.sortable}
              show-overflow-tooltip={column.showOverflowTooltip}
              resizable={column.resizable}
            >
              {{
                default: (scope) => {
                  const RenderComponent = getRenderComponent(column)
                  const renderProps = getRenderProps(column, scope.row)
                  
                  return (
                    <RenderComponent {...renderProps}>
                      {getCellValue(scope.row, column)}
                    </RenderComponent>
                  )
                }
              }}
            </ElTableColumn>
          )
        })}

        {/* 行操作列 */}
        {props.actionConfig.row?.length > 0 && (
          <ElTableColumn
            label="操作"
            width="200"
            fixed="right"
            align="center"
          >
            {{
              default: (scope) => (
                <ElSpace wrap>
                  {getVisibleRowActions(scope.row).map(action => (
                    <ElButton
                      key={action.label}
                      type={action.type}
                      icon={action.icon}
                      size={action.size || 'default'}
                      onClick={() => handleActionClick(action.handler, scope.row)}
                    >
                      {action.label}
                    </ElButton>
                  ))}
                </ElSpace>
              )
            }}
          </ElTableColumn>
        )}
      </ElTable>
    )
  }
}
