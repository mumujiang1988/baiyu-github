/**
 * 可配置表单组件
 * 基于 JSON 配置动态渲染表单
 */

import { ref, computed } from 'vue'
import { 
  ElForm, ElFormItem, ElRow, ElCol, ElCard, ElScrollbar,
  ElInput, ElSelect, ElOption, ElDatePicker, ElInputNumber,
  ElRadioGroup, ElRadio, ElCheckboxGroup, ElCheckbox, ElSwitch
} from 'element-plus'

export default {
  name: 'ConfigurableForm',
  
  props: {
    // 表单配置
    config: {
      type: Object,
      required: true
    },
    // 表单数据
    formData: {
      type: Object,
      required: true
    },
    // 字典数据
    dictionaries: {
      type: Map,
      default: () => new Map()
    },
    // 是否只读
    readonly: {
      type: Boolean,
      default: false
    }
  },

  emits: ['update', 'validate'],

  setup(props, { emit }) {
    const formRef = ref(null)

    // 解析表单验证规则
    const formRules = computed(() => {
      const rules = {}

      props.config.sections.forEach(section => {
        section.fields.forEach(field => {
          if (field.rules && field.rules.length > 0) {
            rules[field.field] = field.rules
          }
        })
      })

      return rules
    })

    // 获取表单组件
    const getFormComponent = (field) => {
      const componentMap = {
        'input': ElInput,
        'textarea': ElInput,
        'select': ElSelect,
        'radio': ElRadioGroup,
        'checkbox': ElCheckboxGroup,
        'date': ElDatePicker,
        'datetime': ElDatePicker,
        'input-number': ElInputNumber,
        'switch': ElSwitch
      }

      return componentMap[field.component] || ElInput
    }

    // 获取字段选项
    const getFieldOptions = (field) => {
      // 静态选项
      if (field.options && Array.isArray(field.options)) {
        return field.options
      }

      // 动态字典
      if (field.dictionary) {
        return props.dictionaries.get(field.dictionary) || []
      }

      return []
    }

    // 判断字段是否可见
    const isFieldVisible = (field) => {
      if (field.visible && typeof field.visible === 'string') {
        try {
          const visibleFn = new Function('formData', `return ${field.visible}`)
          return visibleFn(props.formData)
        } catch (error) {
          console.error('评估字段可见性失败:', error)
          return true
        }
      }
      return true
    }

    // 判断字段是否禁用
    const isFieldDisabled = (field) => {
      if (props.readonly) return true
      
      if (field.disabled && typeof field.disabled === 'string') {
        try {
          const disabledFn = new Function('formData', `return ${field.disabled}`)
          return disabledFn(props.formData)
        } catch (error) {
          console.error('评估字段禁用状态失败:', error)
          return false
        }
      }
      return false
    }

    // 获取组件属性
    const getComponentProps = (field) => {
      const baseProps = {
        ...field.componentProps,
        clearable: field.componentProps?.clearable !== false,
        disabled: isFieldDisabled(field)
      }

      // 特殊组件处理
      if (field.component === 'textarea') {
        baseProps.type = 'textarea'
        baseProps.rows = field.componentProps?.rows || 3
      }

      if (['date', 'datetime'].includes(field.component)) {
        baseProps.type = field.component === 'datetime' ? 'datetime' : 'date'
        baseProps.valueFormat = field.componentProps?.valueFormat || 'YYYY-MM-DD'
      }

      return baseProps
    }

    // 验证表单
    const validate = async () => {
      if (!formRef.value) return false

      try {
        await formRef.value.validate()
        return true
      } catch (error) {
        return false
      }
    }

    // 重置表单
    const resetFields = () => {
      if (formRef.value) {
        formRef.value.resetFields()
      }
    }

    // 暴露方法
    defineExpose({
      validate,
      resetFields
    })

    return () => (
      <ElForm
        model={props.formData}
        rules={formRules.value}
        label-width={props.config.labelWidth}
        ref={formRef}
      >
        <ElScrollbar max-height="65vh">
          {props.config.sections.map((section, sectionIndex) => (
            <ElCard
              key={sectionIndex}
              shadow="never"
              class="form-section-card"
            >
              {{
                header: () => (
                  <div class="card-header">
                    {section.icon && <el-icon>{section.icon}</el-icon>}
                    <span>{section.title}</span>
                  </div>
                ),
                default: () => (
                  <ElRow gutter={20}>
                    {section.fields.map((field, fieldIndex) => {
                      if (!isFieldVisible(field)) return null

                      const span = field.span || (24 / (section.columns || 3))
                      const FieldComponent = getFormComponent(field)
                      const componentProps = getComponentProps(field)

                      return (
                        <ElCol key={fieldIndex} span={span}>
                          <ElFormItem label={field.label} prop={field.field}>
                            <FieldComponent
                              v-model={props.formData[field.field]}
                              {...componentProps}
                            >
                              {/* 下拉选项 */}
                              {['select', 'radio', 'checkbox'].includes(field.component) && (
                                getFieldOptions(field).map(option => (
                                  <ElOption
                                    key={option.value}
                                    label={option.label}
                                    value={option.value}
                                  />
                                ))
                              )}
                            </FieldComponent>
                          </ElFormItem>
                        </ElCol>
                      )
                    })}
                  </ElRow>
                )
              }}
            </ElCard>
          ))}
        </ElScrollbar>
      </ElForm>
    )
  }
}
