/**
 * 展开行详情组件 - 配置化版本
 * 支持多页签显示订单明细和成本数据
 * 完全由 JSON 配置驱动
 */

import { ref, reactive, watch, computed, h } from 'vue'
import { ElTabs, ElTabPane, ElTable, ElDescriptions, ElDescriptionsItem, ElEmpty, ElIcon, ElTag } from 'element-plus'
import { Loading, Close } from '@element-plus/icons-vue'
import ERPConfigParser from '@/views/erp/utils/ERPConfigParser.mjs'

export default {
  name: 'ExpandRowDetail',
  
  props: {
    row: {
      type: Object,
      required: true
    },
    expandConfig: {
      type: Object,
      default: null
    },
    parser: {
      type: Object,
      default: null
    },
    isExpanded: {
      type: Boolean,
      default: false
    }
  },

  emits: ['load-data'],

  setup(props, { emit }) {
    const activeTab = ref('')
    const loadingData = ref(false)

    // 获取字典选项
    const getDictOptions = (dictName) => {
      if (!props.parser || !dictName) return []
      return props.parser.getDictOptions(dictName) || []
    }

    // 获取标签配置
    const getTagConfig = (value, dictName) => {
      const dict = getDictOptions(dictName)
      const option = dict.find(item => item.value === value)
      return {
        label: option?.label || value,
        type: option?.type || 'info'
      }
    }

    // 格式化货币
    const formatCurrency = (value, precision = 2) => {
      if (!value && value !== 0) return '-'
      return Number(value).toLocaleString('zh-CN', {
        minimumFractionDigits: precision,
        maximumFractionDigits: precision
      })
    }

    // 格式化百分比
    const formatPercent = (value, precision = 2) => {
      if (!value && value !== 0) return '-'
      return `${Number(value).toFixed(precision)}%`
    }

    // 格式化日期
    const formatDate = (value, format = 'YYYY-MM-DD') => {
      if (!value) return '-'
      try {
        const dayjs = require('dayjs')
        return dayjs(value).format(format)
      } catch (error) {
        return value
      }
    }

    // 渲染单元格内容
    const renderCellContent = (row, prop, renderType, dictionary, precision, format) => {
      const value = row[prop]
      
      switch (renderType) {
        case 'tag':
          const tagConfig = getTagConfig(value, dictionary)
          return h(
            'el-tag',
            { type: tagConfig.type, size: 'small', disableTransitions: true },
            { default: () => tagConfig.label }
          )
        case 'currency':
          return formatCurrency(value, precision)
        case 'percent':
          return formatPercent(value, precision)
        case 'date':
          return formatDate(value, format)
        case 'datetime':
          return formatDate(value, format || 'YYYY-MM-DD HH:mm:ss')
        default:
          return value ?? '-'
      }
    }

    // 加载数据
    const loadData = () => {
      if (loadingData.value) return
      
      loadingData.value = true
      emit('load-data', props.row)
      // 注意: loadingData 会在 watch 中检测到数据变化后重置
    }

    // 监听展开状态变化
    watch(() => props.isExpanded, async (newVal) => {
      if (newVal) {
        // 展开时,如果没有数据则加载
        const hasEntryList = props.row.entryList && props.row.entryList.length > 0
        const hasCostData = props.row.costData && Object.keys(props.row.costData).length > 0
        
        if (!hasEntryList && !hasCostData) {
          loadData()
        } else {
          // 有数据时设置活跃标签
          if (!activeTab.value) {
            activeTab.value = hasEntryList ? 'entry' : 'cost'
          }
        }
      }
    }, { immediate: true })

    // 关闭展开行
    const closeExpand = () => {
      // 通知父组件关闭（如果需要）
    }

    // 监听数据变化
    watch(() => props.row.entryList, () => {
      if (props.row.entryList?.length > 0) {
        loadingData.value = false
        if (!activeTab.value) {
          activeTab.value = 'entry'
        }
      }
    })

    watch(() => props.row.costData, () => {
      if (props.row.costData && Object.keys(props.row.costData).length > 0) {
        loadingData.value = false
        if (!activeTab.value) {
          activeTab.value = 'cost'
        }
      }
    })

    // 渲染表格列
    const renderTableColumns = (columns) => {
      return columns.map((col, index) => {
        const columnSlots = {}
        
        if (col.renderType || col.dictionary) {
          columnSlots.default = ({ row }) => {
            return renderCellContent(
              row,
              col.prop,
              col.renderType,
              col.dictionary,
              col.precision,
              col.format
            )
          }
        }
        
        return h(
          'el-table-column',
          {
            key: index,
            prop: col.prop,
            label: col.label,
            width: col.width,
            minWidth: col.minWidth,
            align: col.align || 'center',
            showOverflowTooltip: col.showOverflowTooltip !== false
          },
          columnSlots
        )
      })
    }

    // 渲染描述列表字段
    const renderDescriptionFields = (fields, data) => {
      return fields.map((field, index) => {
        return h(
          'el-descriptions-item',
          { key: index, label: field.label },
          {
            default: () => renderCellContent(
              data,
              field.prop,
              field.renderType,
              field.dictionary,
              field.precision,
              field.format
            )
          }
        )
      })
    }

    // 渲染页签
    const renderTabs = () => {
      if (!props.expandConfig?.tabs) return null

      const tabPanes = props.expandConfig.tabs.map((tab) => {
        const data = props.row[tab.dataField]
        const hasData = data && (Array.isArray(data) ? data.length > 0 : Object.keys(data).length > 0)
        
        if (!hasData) return null

        let content
        if (tab.type === 'descriptions') {
          // 描述列表 - 使用 expand-content 包裹
          content = h(
            'div',
            { class: 'expand-content' },
            h(
              'el-descriptions',
              { column: tab.columns || 3, border: true, size: 'small' },
              { default: () => renderDescriptionFields(tab.fields, data) }
            )
          )
        } else {
          // 表格 - 使用 expand-section 和 expand-content 包裹
          content = h(
            'div',
            { class: 'expand-section' },
            [
              h('div', { class: 'expand-content' }, [
                h(
                  'el-table',
                  {
                    data: data,
                    size: 'small',
                    border: true,
                    maxHeight: 400,
                    stripe: true
                  },
                  { default: () => renderTableColumns(tab.table.columns) }
                )
              ])
            ]
          )
        }

        return h(
          'el-tab-pane',
          {
            key: tab.name,
            label: tab.label,
            name: tab.name
          },
          {
            default: () => content
          }
        )
      })

      return h(
        'el-tabs',
        {
          modelValue: activeTab.value,
          'onUpdate:modelValue': (val) => { activeTab.value = val },
          class: 'expand-tabs',
          stretch: true
        },
        { default: () => tabPanes.filter(Boolean) }
      )
    }

    return () => {
      // 如果未展开,返回空
      if (!props.isExpanded) {
        return null
      }

      const children = []
      
      // 检查是否有数据
      const hasEntryList = props.row.entryList && props.row.entryList.length > 0
      const hasCostData = props.row.costData && Object.keys(props.row.costData).length > 0
      const hasAnyData = hasEntryList || hasCostData
      
      // 有关闭按钮时才渲染
      if (hasAnyData) {
        children.push(h(
          'div',
          { class: 'expand-close-btn', onClick: closeExpand, title: '关闭' },
          h('el-icon', {}, h(Close))
        ))
      }
      
      // 加载中状态
      if (loadingData.value) {
        children.push(h(
          'div',
          { class: 'loading-data-tip' },
          [
            h('el-icon', { class: 'is-loading' }, h(Loading)),
            h('span', {}, '正在加载数据...')
          ]
        ))
      }
      // 无数据状态 - 已加载但数据为空
      else if (!hasAnyData) {
        children.push(h(
          'div',
          { class: 'no-data-tip' },
          h('el-empty', { description: '暂无订单明细或成本数据', imageSize: 80 })
        ))
      }
      // 有数据，渲染页签
      else {
        children.push(renderTabs())
      }

      return h('div', { class: 'row-expand-container' }, children)
    }
  }
}
