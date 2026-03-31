/**
 * Expand Row Detail Component - Config-driven version
 * Supports multi-tab display for order details and cost data
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

    const getDictOptions = (dictName) => {
      if (!props.parser || !dictName) return []
      return props.parser.getDictOptions(dictName) || []
    }

    const getTagConfig = (value, dictName) => {
      const dict = getDictOptions(dictName)
      const option = dict.find(item => item.value === value)
      return {
        label: option?.label || value,
        type: option?.type || 'info'
      }
    }

    const formatCurrency = (value, precision = 2) => {
      if (!value && value !== 0) return '-'
      return Number(value).toLocaleString('zh-CN', {
        minimumFractionDigits: precision,
        maximumFractionDigits: precision
      })
    }

    const formatPercent = (value, precision = 2) => {
      if (!value && value !== 0) return '-'
      return `${Number(value).toFixed(precision)}%`
    }

    const formatDate = (value, format = 'YYYY-MM-DD') => {
      if (!value) return '-'
      try {
        const dayjs = require('dayjs')
        return dayjs(value).format(format)
      } catch (error) {
        return value
      }
    }

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
    
    const loadData = () => {
      if (loadingData.value) return
          
      loadingData.value = true
      emit('load-data', props.row)
    }

    // Listen for expand state changes
    watch(() => props.isExpanded, async (newVal) => {
      if (newVal) {
        // Load data when expanded if no data exists
        const hasEntryList = props.row.entryList && props.row.entryList.length > 0
        const hasCostData = props.row.costData && Object.keys(props.row.costData).length > 0
        
        if (!hasEntryList && !hasCostData) {
          loadData()
        } else {
          // Set active tab when data exists
          if (!activeTab.value) {
            activeTab.value = hasEntryList ? 'entry' : 'cost'
          }
        }
      }
    }, { immediate: true })

    const closeExpand = () => {
    }

    // Watch for data changes
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

    const renderTabs = () => {
      if (!props.expandConfig?.tabs) return null

      const tabPanes = props.expandConfig.tabs.map((tab) => {
        const data = props.row[tab.dataField]
        const hasData = data && (Array.isArray(data) ? data.length > 0 : Object.keys(data).length > 0)
        
        if (!hasData) return null

        let content
        if (tab.type === 'descriptions') {
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
          // Table - wrapped with expand-section and expand-content
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
      if (!props.isExpanded) {
        return null
      }

      const children = []
      
      const hasEntryList = props.row.entryList && props.row.entryList.length > 0
      const hasCostData = props.row.costData && Object.keys(props.row.costData).length > 0
      const hasAnyData = hasEntryList || hasCostData
      
      if (hasAnyData) {
        children.push(h(
          'div',
          { class: 'expand-close-btn', onClick: closeExpand, title: 'Close' },
          h('el-icon', {}, h(Close))
        ))
      }
      
      if (loadingData.value) {
        children.push(h(
          'div',
          { class: 'loading-data-tip' },
          [
            h('el-icon', { class: 'is-loading' }, h(Loading)),
            h('span', {}, 'Loading data...')
          ]
        ))
      }
      else if (!hasAnyData) {
        children.push(h(
          'div',
          { class: 'no-data-tip' },
          h('el-empty', { description: 'No order details or cost data available', imageSize: 80 })
        ))
      }
      else {
        children.push(renderTabs())
      }

      return h('div', { class: 'row-expand-container' }, children)
    }
  }
}
