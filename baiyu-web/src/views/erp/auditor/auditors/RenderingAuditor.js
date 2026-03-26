/**
 * 前端渲染审计器
 * 验证前端组件配置、表格列渲染、表单字段验证的可执行性
 */

const { AuditResult } = require('../core/models/AuditResult');

/**
 * 前端渲染审计器
 */
class RenderingAuditor {
  get name() {
    return 'RenderingAuditor';
  }

  get description() {
    return '验证前端渲染配置完整性';
  }

  /**
   * 执行审计
   * @param {Object} config - 配置对象
   * @returns {Promise<AuditResult[]>}
   */
  async audit(config) {
    const results = [];

    // 1. 验证搜索组件配置
    results.push(...this.validateSearchComponents(config));

    // 2. 验证表格列渲染配置
    results.push(...this.validateColumnRendering(config));

    // 3. 验证表单字段配置
    results.push(...this.validateFormFields(config));

    // 4. 验证操作按钮配置
    results.push(...this.validateActionButtons(config));

    return results;
  }

  /**
   * 验证搜索组件配置
   */
  validateSearchComponents(config) {
    const results = [];

    if (!config.searchConfig?.fields) {
      return results;
    }

    config.searchConfig.fields.forEach((field, index) => {
      const component = field.component;
      const path = `$.searchConfig.fields[${index}]`;

      switch (component) {
        case 'daterange':
          // daterange组件需要placeholder和valueFormat
          if (!field.placeholder) {
            results.push(AuditResult.warning(
              'REQ-5.1.1',
              `daterange组件缺少placeholder配置`,
              path,
              '建议配置placeholder提升用户体验，如: ["开始日期", "结束日期"]'
            ));
          }
          if (!field.valueFormat) {
            results.push(AuditResult.warning(
              'REQ-5.1.2',
              `daterange组件缺少valueFormat配置`,
              path,
              '建议配置valueFormat，如: "YYYY-MM-DD"'
            ));
          }
          break;

        case 'select':
          // select组件需要数据源配置
          if (!field.dictionary && !field.options && !field.dictKey) {
            results.push(AuditResult.error(
              'REQ-5.1.3',
              `select组件缺少数据源配置`,
              path,
              '请配置dictionary、options或dictKey指定下拉选项数据源'
            ));
          }
          break;

        case 'input':
          // input组件建议配置placeholder
          if (!field.placeholder) {
            results.push(AuditResult.info(
              'REQ-5.1.4',
              `input组件未配置placeholder`,
              path,
              '建议配置placeholder提升用户体验'
            ));
          }
          break;

        case 'input-number':
          // input-number组件建议配置数值约束
          if (field.min === undefined && field.max === undefined) {
            results.push(AuditResult.info(
              'REQ-5.1.5',
              `input-number组件未配置数值范围`,
              path,
              '建议配置min和max约束数值范围'
            ));
          }
          break;
      }
    });

    return results;
  }

  /**
   * 验证表格列渲染配置
   */
  validateColumnRendering(config) {
    const results = [];

    if (!config.tableConfig?.columns) {
      return results;
    }

    const columns = config.tableConfig.columns;
    const dictionaries = config.dictionaryConfig || {};

    columns.forEach((column, index) => {
      const renderType = column.renderType || column.type;
      const path = `$.tableConfig.columns[${index}]`;

      switch (renderType) {
        case 'tag':
          // tag渲染需要dictionary配置
          if (!column.dictionary && !column.dictKey) {
            results.push(AuditResult.error(
              'REQ-5.2.1',
              `tag渲染列 ${column.prop} 缺少dictionary配置`,
              path,
              '请配置dictionary指定标签颜色映射'
            ));
          } else {
            // 验证字典存在性
            const dictKey = column.dictionary || column.dictKey;
            if (!dictionaries[dictKey]) {
              results.push(AuditResult.error(
                'REQ-5.2.2',
                `tag渲染列 ${column.prop} 引用的字典 ${dictKey} 不存在`,
                path,
                `请在 dictionaryConfig 中定义 ${dictKey} 字典`
              ));
            }
          }
          break;

        case 'currency':
        case 'money':
          // currency渲染需要precision配置
          if (column.precision === undefined) {
            results.push(AuditResult.warning(
              'REQ-5.2.3',
              `currency渲染列 ${column.prop} 未配置precision`,
              path,
              '建议配置precision指定小数位数，默认为2'
            ));
          }
          break;

        case 'date':
        case 'datetime':
          // date渲染需要format配置
          if (!column.format) {
            results.push(AuditResult.warning(
              'REQ-5.2.4',
              `${renderType}渲染列 ${column.prop} 未配置format`,
              path,
              `建议配置format，如: "${renderType === 'date' ? 'YYYY-MM-DD' : 'YYYY-MM-DD HH:mm:ss'}"`
            ));
          }
          break;

        case 'number':
          // number渲染建议配置precision
          if (column.precision === undefined && column.decimals === undefined) {
            results.push(AuditResult.info(
              'REQ-5.2.5',
              `number渲染列 ${column.prop} 未配置精度`,
              path,
              '建议配置precision或decimals指定小数位数'
            ));
          }
          break;

        case 'image':
          // image渲染建议配置预览
          if (!column.preview && !column.previewSrc) {
            results.push(AuditResult.info(
              'REQ-5.2.6',
              `image渲染列 ${column.prop} 未配置预览功能`,
              path,
              '建议配置preview启用图片预览功能'
            ));
          }
          break;
      }
    });

    return results;
  }

  /**
   * 验证表单字段配置
   */
  validateFormFields(config) {
    const results = [];

    if (!config.formConfig?.sections) {
      return results;
    }

    config.formConfig.sections.forEach((section, sIndex) => {
      // 验证分区非空
      if (!section.fields || section.fields.length === 0) {
        results.push(AuditResult.warning(
          'REQ-5.3.1',
          `表单分区 ${section.title || sIndex} 没有任何字段`,
          `$.formConfig.sections[${sIndex}]`,
          '建议添加表单字段或删除空分区'
        ));
        return;
      }

      section.fields.forEach((field, fIndex) => {
        const path = `$.formConfig.sections[${sIndex}].fields[${fIndex}]`;

        // 1. 验证required字段包含验证规则
        if (field.required && !field.rules) {
          results.push(AuditResult.warning(
            'REQ-5.3.2',
            `必填字段 ${field.prop} 未配置验证规则`,
            path,
            '建议添加required验证规则'
          ));
        }

        // 2. 验证input-number组件数值约束
        if (field.component === 'input-number') {
          if (field.min === undefined && field.max === undefined) {
            results.push(AuditResult.info(
              'REQ-5.3.3',
              `input-number字段 ${field.prop} 未配置数值范围`,
              path,
              '建议配置min和max约束数值范围'
            ));
          }
        }

        // 3. 验证验证规则trigger合理性
        if (field.rules && Array.isArray(field.rules)) {
          field.rules.forEach((rule, rIndex) => {
            if (rule.trigger) {
              const validTriggers = ['blur', 'change'];
              if (!validTriggers.includes(rule.trigger)) {
                results.push(AuditResult.warning(
                  'REQ-5.3.4',
                  `字段 ${field.prop} 的验证规则使用了无效的trigger: ${rule.trigger}`,
                  `${path}.rules[${rIndex}]`,
                  `请使用有效的trigger: ${validTriggers.join(', ')}`
                ));
              }
            }
          });
        }

        // 4. 验证select组件数据源
        if (field.component === 'select') {
          if (!field.dictionary && !field.options && !field.dictKey) {
            results.push(AuditResult.error(
              'REQ-5.3.5',
              `select字段 ${field.prop} 缺少数据源配置`,
              path,
              '请配置dictionary、options或dictKey指定下拉选项'
            ));
          }
        }
      });
    });

    return results;
  }

  /**
   * 验证操作按钮配置
   */
  validateActionButtons(config) {
    const results = [];

    if (!config.tableConfig?.actions) {
      return results;
    }

    const permissionPrefix = config.pageConfig?.permissionPrefix;

    config.tableConfig.actions.forEach((action, index) => {
      const path = `$.tableConfig.actions[${index}]`;

      // 1. 验证按钮permission配置
      if (!action.permission) {
        results.push(AuditResult.warning(
          'REQ-5.4.1',
          `操作按钮 ${action.label} 未配置permission`,
          path,
          '建议配置permission进行权限控制'
        ));
      } else {
        // 2. 验证permission占位符可替换
        if (action.permission.includes('{permissionPrefix}')) {
          if (!permissionPrefix) {
            results.push(AuditResult.error(
              'REQ-5.4.2',
              `按钮 ${action.label} 的permission使用了占位符但pageConfig未定义permissionPrefix`,
              path,
              '请在 pageConfig.permissionPrefix 中定义权限前缀'
            ));
          }
        }
      }

      // 3. 验证disabled逻辑合理性
      if (action.disabled && typeof action.disabled === 'string') {
        // 检查disabled表达式是否包含有效字段
        const validFields = ['status', 'billStatus', 'approvalStatus', 'isApproved'];
        const hasValidField = validFields.some(f => action.disabled.includes(f));
        
        if (!hasValidField) {
          results.push(AuditResult.info(
            'REQ-5.4.3',
            `按钮 ${action.label} 的disabled表达式可能无效`,
            path,
            `建议使用有效字段: ${validFields.join(', ')}`
          ));
        }
      }

      // 4. 验证按钮类型
      const validActionTypes = [
        'add', 'edit', 'delete', 'view', 'query', 'export', 'import',
        'approve', 'reject', 'withdraw', 'submit', 'push', 'custom'
      ];
      const actionType = action.type || action.action;
      
      if (actionType && !validActionTypes.includes(actionType)) {
        results.push(AuditResult.warning(
          'REQ-5.4.4',
          `按钮 ${action.label} 使用了非标准类型: ${actionType}`,
          path,
          `建议使用标准类型: ${validActionTypes.join(', ')}`
        ));
      }
    });

    return results;
  }

  /**
   * 获取审计器支持的规则列表
   */
  getRules() {
    return [
      { id: 'REQ-5.1.1', name: 'daterange placeholder', severity: 'warning' },
      { id: 'REQ-5.1.2', name: 'daterange valueFormat', severity: 'warning' },
      { id: 'REQ-5.1.3', name: 'select数据源', severity: 'error' },
      { id: 'REQ-5.1.4', name: 'input placeholder', severity: 'info' },
      { id: 'REQ-5.1.5', name: 'input-number范围', severity: 'info' },
      { id: 'REQ-5.2.1', name: 'tag dictionary', severity: 'error' },
      { id: 'REQ-5.2.2', name: 'tag字典存在性', severity: 'error' },
      { id: 'REQ-5.2.3', name: 'currency precision', severity: 'warning' },
      { id: 'REQ-5.2.4', name: 'date format', severity: 'warning' },
      { id: 'REQ-5.2.5', name: 'number精度', severity: 'info' },
      { id: 'REQ-5.2.6', name: 'image预览', severity: 'info' },
      { id: 'REQ-5.3.1', name: '表单分区非空', severity: 'warning' },
      { id: 'REQ-5.3.2', name: '必填字段验证', severity: 'warning' },
      { id: 'REQ-5.3.3', name: 'input-number范围', severity: 'info' },
      { id: 'REQ-5.3.4', name: '验证trigger', severity: 'warning' },
      { id: 'REQ-5.3.5', name: 'select数据源', severity: 'error' },
      { id: 'REQ-5.4.1', name: '按钮permission', severity: 'warning' },
      { id: 'REQ-5.4.2', name: 'permission占位符', severity: 'error' },
      { id: 'REQ-5.4.3', name: 'disabled逻辑', severity: 'info' },
      { id: 'REQ-5.4.4', name: '按钮类型', severity: 'warning' }
    ];
  }
}

module.exports = RenderingAuditor;
