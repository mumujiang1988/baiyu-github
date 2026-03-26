/**
 * 数据流审计器
 * 验证查询数据流、表单数据流、字典数据流的完整性和闭环性
 */

const { AuditResult } = require('../core/models/AuditResult');

/**
 * 数据流审计器
 */
class DataFlowAuditor {
  get name() {
    return 'DataFlowAuditor';
  }

  get description() {
    return '验证数据流完整性和闭环性';
  }

  /**
   * 执行审计
   * @param {Object} config - 配置对象
   * @returns {Promise<AuditResult[]>}
   */
  async audit(config) {
    const results = [];

    // 1. 验证查询数据流
    results.push(...this.validateQueryDataFlow(config));

    // 2. 验证子表数据流
    results.push(...this.validateSubTableDataFlow(config));

    // 3. 验证表单数据流
    results.push(...this.validateFormDataFlow(config));

    // 4. 验证字典数据流
    results.push(...this.validateDictionaryDataFlow(config));

    return results;
  }

  /**
   * 验证查询数据流
   * searchConfig → queryConfig → apiConfig → tableConfig
   */
  validateQueryDataFlow(config) {
    const results = [];

    // 1. 验证查询接口存在
    if (!config.apiConfig?.engineApis?.query) {
      results.push(AuditResult.error(
        'REQ-6.1.1',
        '缺少查询接口，数据流断裂',
        '$.apiConfig.engineApis.query',
        '请在 engineApis 中配置 query 接口'
      ));
      return results;
    }

    // 2. 验证查询结果字段与表格列匹配
    if (config.tableConfig?.columns) {
      const tableColumns = config.tableConfig.columns;
      const queryFields = this.extractQueryFields(config);

      tableColumns.forEach((column, index) => {
        const fieldName = column.prop || column.field;
        
        // 跳过特殊列（操作列、序号列等）
        if (column.type === 'selection' || column.type === 'index' || column.type === 'expand') {
          return;
        }

        if (fieldName && !queryFields.has(fieldName)) {
          results.push(AuditResult.warning(
            'REQ-6.1.2',
            `表格列 ${fieldName} 无法从查询结果获取数据`,
            `$.tableConfig.columns[${index}]`,
            '请确保查询接口返回该字段，或在queryConfig中配置字段映射'
          ));
        }
      });
    }

    // 3. 验证查询条件配置完整性
    if (config.apiConfig?.queryConfig?.defaultConditions) {
      const conditions = config.apiConfig.queryConfig.defaultConditions;
      
      conditions.forEach((condition, index) => {
        if (!condition.field) {
          results.push(AuditResult.error(
            'REQ-6.1.3',
            `查询条件 ${index} 缺少field配置`,
            `$.apiConfig.queryConfig.defaultConditions[${index}]`,
            '请为查询条件配置field字段'
          ));
        }

        if (!condition.operator) {
          results.push(AuditResult.warning(
            'REQ-6.1.4',
            `查询条件 ${condition.field} 缺少operator配置`,
            `$.apiConfig.queryConfig.defaultConditions[${index}]`,
            '建议配置operator，默认为 "="'
          ));
        }
      });
    }

    return results;
  }

  /**
   * 验证子表数据流
   * subTableQueryConfigs → expandRow/drawerConfig → 子表展示
   */
  validateSubTableDataFlow(config) {
    const results = [];

    if (!config.subTableQueryConfigs) {
      return results;
    }

    config.subTableQueryConfigs.forEach((subTable, index) => {
      const path = `$.subTableQueryConfigs[${index}]`;
      const tableName = subTable.name || subTable.tableName;

      // 1. 验证子表查询条件定义
      if (!subTable.queryConditions || subTable.queryConditions.length === 0) {
        results.push(AuditResult.error(
          'REQ-6.2.1',
          `子表 ${tableName} 未定义查询条件`,
          `${path}.queryConditions`,
          '请定义子表查询条件，通常需要关联主表ID'
        ));
      } else {
        // 验证查询条件包含主表关联
        const hasMainTableRelation = subTable.queryConditions.some(c => 
          c.field && (c.field.includes('billId') || c.field.includes('mainId') || c.value === '{billId}')
        );
        
        if (!hasMainTableRelation) {
          results.push(AuditResult.warning(
            'REQ-6.2.2',
            `子表 ${tableName} 查询条件未关联主表`,
            `${path}.queryConditions`,
            '建议添加主表ID关联条件，如: {field: "billId", operator: "=", value: "{billId}"}'
          ));
        }
      }

      // 2. 验证子表API路径配置
      if (!subTable.apiPath && !subTable.api) {
        results.push(AuditResult.warning(
          'REQ-6.2.3',
          `子表 ${tableName} 未配置API路径`,
          path,
          '请配置apiPath或api指定子表数据查询接口'
        ));
      }

      // 3. 验证子表数据展示配置完整性
      const hasExpandRow = config.expandRowDetail?.subTable === tableName;
      const hasDrawer = config.drawerConfig?.some(d => d.subTable === tableName);
      
      if (!hasExpandRow && !hasDrawer) {
        results.push(AuditResult.warning(
          'REQ-6.2.4',
          `子表 ${tableName} 未配置展示方式`,
          path,
          '请在 expandRowDetail 或 drawerConfig 中配置子表展示'
        ));
      } else {
        // 验证展示配置中的列定义
        if (hasExpandRow && config.expandRowDetail?.columns) {
          if (!subTable.columns && !config.expandRowDetail.columns.length) {
            results.push(AuditResult.warning(
              'REQ-6.2.5',
              `子表 ${tableName} 的展开行配置缺少列定义`,
              '$.expandRowDetail.columns',
              '请为子表配置展示列'
            ));
          }
        }
      }
    });

    return results;
  }

  /**
   * 验证表单数据流
   * formConfig → validation → 保存接口
   */
  validateFormDataFlow(config) {
    const results = [];

    if (!config.formConfig) {
      return results;
    }

    // 1. 验证表单字段验证规则完整性
    if (config.formConfig.sections) {
      config.formConfig.sections.forEach((section, sIndex) => {
        if (!section.fields) return;

        section.fields.forEach((field, fIndex) => {
          const path = `$.formConfig.sections[${sIndex}].fields[${fIndex}]`;
          
          // 必填字段应有验证规则
          if (field.required && !field.rules) {
            results.push(AuditResult.warning(
              'REQ-6.3.1',
              `必填字段 ${field.prop} 缺少验证规则`,
              path,
              '建议添加必填验证规则: {required: true, message: "此项为必填项"}'
            ));
          }

          // 验证规则应包含message
          if (field.rules && Array.isArray(field.rules)) {
            field.rules.forEach((rule, rIndex) => {
              if (rule.required && !rule.message) {
                results.push(AuditResult.info(
                  'REQ-6.3.2',
                  `字段 ${field.prop} 的必填规则缺少提示消息`,
                  `${path}.rules[${rIndex}]`,
                  '建议添加message提升用户体验'
                ));
              }
            });
          }
        });
      });
    }

    // 2. 验证validation接口配置
    if (config.pageConfig?.enableValidation !== false) {
      if (!config.apiConfig?.engineApis?.validation) {
        results.push(AuditResult.warning(
          'REQ-6.3.3',
          '未配置validation接口',
          '$.apiConfig.engineApis.validation',
          '建议配置validation接口进行后端数据验证'
        ));
      }
    }

    // 3. 验证保存接口配置
    const hasSaveApi = config.apiConfig?.engineApis?.save || 
                       config.apiConfig?.engineApis?.add ||
                       config.apiConfig?.engineApis?.edit;
    
    if (!hasSaveApi) {
      results.push(AuditResult.warning(
        'REQ-6.3.4',
        '未配置保存接口',
        '$.apiConfig.engineApis',
        '建议配置save/add/edit接口支持数据保存'
      ));
    }

    return results;
  }

  /**
   * 验证字典数据流
   * 字段引用 → dictionaryConfig定义 → 数据源配置
   */
  validateDictionaryDataFlow(config) {
    const results = [];

    // 1. 收集所有字典引用
    const dictReferences = this.collectDictionaryReferences(config);

    // 2. 验证字典定义存在性
    const dictionaries = config.dictionaryConfig || {};

    for (const [dictKey, locations] of Object.entries(dictReferences)) {
      if (!dictionaries[dictKey]) {
        results.push(AuditResult.error(
          'REQ-6.4.1',
          `字典 ${dictKey} 被引用但未定义`,
          locations.join('\n'),
          `请在 dictionaryConfig 中定义 ${dictKey} 字典`
        ));
        continue;
      }

      const dictConfig = dictionaries[dictKey];

      // 3. 验证动态字典配置完整性
      if (dictConfig.type === 'dynamic') {
        if (!dictConfig.tableName) {
          results.push(AuditResult.error(
            'REQ-6.4.2',
            `动态字典 ${dictKey} 缺少tableName配置`,
            `$.dictionaryConfig.${dictKey}`,
            '请配置tableName指定数据源表'
          ));
        }

        if (!dictConfig.queryConfig) {
          results.push(AuditResult.error(
            'REQ-6.4.3',
            `动态字典 ${dictKey} 缺少queryConfig配置`,
            `$.dictionaryConfig.${dictKey}`,
            '请配置queryConfig定义查询条件'
          ));
        }

        if (!dictConfig.fieldMapping) {
          results.push(AuditResult.error(
            'REQ-6.4.4',
            `动态字典 ${dictKey} 缺少fieldMapping配置`,
            `$.dictionaryConfig.${dictKey}`,
            '请配置fieldMapping定义字段映射，如: {label: "name", value: "id"}'
          ));
        }
      }

      // 4. 验证远程字典配置完整性
      if (dictConfig.type === 'remote') {
        if (!dictConfig.searchApi) {
          results.push(AuditResult.error(
            'REQ-6.4.5',
            `远程字典 ${dictKey} 缺少searchApi配置`,
            `$.dictionaryConfig.${dictKey}`,
            '请配置searchApi指定远程搜索接口'
          ));
        }

        if (!dictConfig.debounce) {
          results.push(AuditResult.warning(
            'REQ-6.4.6',
            `远程字典 ${dictKey} 未配置防抖时间`,
            `$.dictionaryConfig.${dictKey}`,
            '建议配置debounce防止频繁请求，如: 300'
          ));
        }
      }

      // 5. 验证静态字典数据完整性
      if (dictConfig.type === 'static' || !dictConfig.type) {
        if (!dictConfig.data || dictConfig.data.length === 0) {
          results.push(AuditResult.error(
            'REQ-6.4.7',
            `静态字典 ${dictKey} 数据为空`,
            `$.dictionaryConfig.${dictKey}.data`,
            '请为静态字典配置data数组'
          ));
        }
      }
    }

    return results;
  }

  /**
   * 提取查询返回的字段
   */
  extractQueryFields(config) {
    const fields = new Set();

    // 从表格列提取
    if (config.tableConfig?.columns) {
      config.tableConfig.columns.forEach(col => {
        const fieldName = col.prop || col.field;
        if (fieldName) fields.add(fieldName);
      });
    }

    // 从查询配置提取
    if (config.apiConfig?.queryConfig?.defaultConditions) {
      config.apiConfig.queryConfig.defaultConditions.forEach(cond => {
        if (cond.field) fields.add(cond.field);
      });
    }

    return fields;
  }

  /**
   * 收集所有字典引用
   */
  collectDictionaryReferences(config) {
    const references = {};

    const addReference = (dictKey, location) => {
      if (!references[dictKey]) {
        references[dictKey] = [];
      }
      references[dictKey].push(location);
    };

    // 从搜索配置收集
    if (config.searchConfig?.fields) {
      config.searchConfig.fields.forEach((field, index) => {
        const dictKey = field.dictionary || field.dictKey;
        if (dictKey) {
          addReference(dictKey, `$.searchConfig.fields[${index}]`);
        }
      });
    }

    // 从表格配置收集
    if (config.tableConfig?.columns) {
      config.tableConfig.columns.forEach((column, index) => {
        const dictKey = column.dictionary || column.dictKey;
        if (dictKey) {
          addReference(dictKey, `$.tableConfig.columns[${index}]`);
        }
      });
    }

    // 从表单配置收集
    if (config.formConfig?.sections) {
      config.formConfig.sections.forEach((section, sIndex) => {
        if (!section.fields) return;
        section.fields.forEach((field, fIndex) => {
          const dictKey = field.dictionary || field.dictKey;
          if (dictKey) {
            addReference(dictKey, `$.formConfig.sections[${sIndex}].fields[${fIndex}]`);
          }
        });
      });
    }

    return references;
  }

  /**
   * 获取审计器支持的规则列表
   */
  getRules() {
    return [
      { id: 'REQ-6.1.1', name: '查询接口存在', severity: 'error' },
      { id: 'REQ-6.1.2', name: '表格列数据源', severity: 'warning' },
      { id: 'REQ-6.1.3', name: '查询条件field', severity: 'error' },
      { id: 'REQ-6.1.4', name: '查询条件operator', severity: 'warning' },
      { id: 'REQ-6.2.1', name: '子表查询条件', severity: 'error' },
      { id: 'REQ-6.2.2', name: '子表主表关联', severity: 'warning' },
      { id: 'REQ-6.2.3', name: '子表API路径', severity: 'warning' },
      { id: 'REQ-6.2.4', name: '子表展示配置', severity: 'warning' },
      { id: 'REQ-6.2.5', name: '子表列定义', severity: 'warning' },
      { id: 'REQ-6.3.1', name: '必填字段验证', severity: 'warning' },
      { id: 'REQ-6.3.2', name: '验证消息', severity: 'info' },
      { id: 'REQ-6.3.3', name: 'validation接口', severity: 'warning' },
      { id: 'REQ-6.3.4', name: '保存接口', severity: 'warning' },
      { id: 'REQ-6.4.1', name: '字典定义存在', severity: 'error' },
      { id: 'REQ-6.4.2', name: '动态字典tableName', severity: 'error' },
      { id: 'REQ-6.4.3', name: '动态字典queryConfig', severity: 'error' },
      { id: 'REQ-6.4.4', name: '动态字典fieldMapping', severity: 'error' },
      { id: 'REQ-6.4.5', name: '远程字典searchApi', severity: 'error' },
      { id: 'REQ-6.4.6', name: '远程字典防抖', severity: 'warning' },
      { id: 'REQ-6.4.7', name: '静态字典数据', severity: 'error' }
    ];
  }
}

module.exports = DataFlowAuditor;
