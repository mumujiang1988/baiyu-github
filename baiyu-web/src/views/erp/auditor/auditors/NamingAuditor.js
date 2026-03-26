/**
 * 命名规范审计器
 * 验证字段命名、属性命名、字典键命名的规范性
 */

const { AuditResult } = require('../core/models/AuditResult');

/**
 * 命名规范定义
 */
const NAMING_RULES = {
  // 金蝶K3规范: F开头驼峰 (如: FBillNo, FQty)
  kingdee: {
    pattern: /^F[A-Z][a-zA-Z0-9]*$/,
    description: '金蝶K3规范 (F开头驼峰)',
    example: 'FBillNo, FQty, FAmount'
  },
  // 自定义规范: f_开头小写 (如: f_bill_no, f_qty)
  custom: {
    pattern: /^f_[a-z][a-z0-9_]*$/,
    description: '自定义规范 (f_开头小写下划线)',
    example: 'f_bill_no, f_qty, f_amount'
  },
  // Oracle规范: F_ora_开头 (如: F_ora_bill_no)
  oracle: {
    pattern: /^F_ora_[a-z][a-z0-9_]*$/,
    description: 'Oracle规范 (F_ora_开头)',
    example: 'F_ora_bill_no, F_ora_qty'
  }
};

/**
 * 命名规范审计器
 */
class NamingAuditor {
  get name() {
    return 'NamingAuditor';
  }

  get description() {
    return '验证命名规范一致性';
  }

  /**
   * 执行审计
   * @param {Object} config - 配置对象
   * @returns {Promise<AuditResult[]>}
   */
  async audit(config) {
    const results = [];

    // 1. 验证字段命名规范
    results.push(...this.validateFieldNaming(config));

    // 2. 验证大小写一致性
    results.push(...this.validateCaseConsistency(config));

    // 3. 验证字典命名规范
    results.push(...this.validateDictionaryNaming(config));

    return results;
  }

  /**
   * 验证字段命名规范
   */
  validateFieldNaming(config) {
    const results = [];
    const fieldOccurrences = this.collectAllFields(config);

    for (const [fieldName, locations] of Object.entries(fieldOccurrences)) {
      // 跳过特殊字段
      if (this.isSpecialField(fieldName)) {
        continue;
      }

      // 检查是否符合任一命名规范
      const matchedRules = [];
      for (const [ruleName, rule] of Object.entries(NAMING_RULES)) {
        if (rule.pattern.test(fieldName)) {
          matchedRules.push(ruleName);
        }
      }

      if (matchedRules.length === 0) {
        // 不符合任何规范
        results.push(AuditResult.warning(
          'REQ-3.1.1',
          `字段 ${fieldName} 不符合任何命名规范`,
          locations[0],
          `建议使用以下规范之一:\n  - 金蝶K3: ${NAMING_RULES.kingdee.example}\n  - 自定义: ${NAMING_RULES.custom.example}\n  - Oracle: ${NAMING_RULES.oracle.example}`
        ));
      } else if (matchedRules.length > 0) {
        // 符合规范
        results.push(AuditResult.pass(
          'REQ-3.1.2',
          `字段 ${fieldName} 符合 ${matchedRules.join(', ')} 命名规范`,
          locations[0]
        ));
      }
    }

    return results;
  }

  /**
   * 验证大小写一致性
   */
  validateCaseConsistency(config) {
    const results = [];
    const fieldOccurrences = this.collectAllFields(config);

    // 按字段名（忽略大小写）分组
    const caseGroups = {};
    for (const [fieldName, locations] of Object.entries(fieldOccurrences)) {
      const lowerName = fieldName.toLowerCase();
      if (!caseGroups[lowerName]) {
        caseGroups[lowerName] = [];
      }
      caseGroups[lowerName].push({ fieldName, locations });
    }

    // 检查大小写不一致
    for (const [lowerName, variants] of Object.entries(caseGroups)) {
      if (variants.length > 1) {
        // 存在大小写不一致
        const fieldNames = variants.map(v => v.fieldName).join(', ');
        const allLocations = variants.flatMap(v => v.locations);
        
        results.push(AuditResult.error(
          'REQ-3.1.3',
          `字段 ${fieldNames} 大小写不一致`,
          allLocations.join('\n'),
          `请统一使用同一种大小写形式，建议: ${variants[0].fieldName}`
        ));
      }
    }

    return results;
  }

  /**
   * 验证字典命名规范
   */
  validateDictionaryNaming(config) {
    const results = [];

    if (!config.dictionaryConfig) {
      return results;
    }

    const dictionaries = config.dictionaryConfig;

    for (const [dictKey, dictConfig] of Object.entries(dictionaries)) {
      // 1. 验证字典键使用驼峰命名
      if (!/^[a-z][a-zA-Z0-9]*$/.test(dictKey)) {
        results.push(AuditResult.warning(
          'REQ-3.1.4',
          `字典键 ${dictKey} 不符合驼峰命名规范`,
          `$.dictionaryConfig.${dictKey}`,
          '建议使用小驼峰命名法，如: billStatus, orderType'
        ));
      }

      // 2. 验证静态字典value唯一性
      if (dictConfig.type === 'static' && dictConfig.data) {
        const values = dictConfig.data.map(item => item.value);
        const uniqueValues = new Set(values);
        
        if (values.length !== uniqueValues.size) {
          results.push(AuditResult.error(
            'REQ-3.1.5',
            `字典 ${dictKey} 存在重复的value值`,
            `$.dictionaryConfig.${dictKey}.data`,
            '请确保字典项的value值唯一'
          ));
        }

        // 检查value非空
        const emptyValues = values.filter(v => v === null || v === undefined || v === '');
        if (emptyValues.length > 0) {
          results.push(AuditResult.error(
            'REQ-3.1.6',
            `字典 ${dictKey} 存在空的value值`,
            `$.dictionaryConfig.${dictKey}.data`,
            '请确保所有字典项的value值非空'
          ));
        }
      }

      // 3. 验证动态字典配置完整性
      if (dictConfig.type === 'dynamic') {
        const requiredFields = ['tableName', 'queryConfig', 'fieldMapping'];
        for (const field of requiredFields) {
          if (!dictConfig[field]) {
            results.push(AuditResult.error(
              'REQ-3.1.7',
              `动态字典 ${dictKey} 缺少必需字段: ${field}`,
              `$.dictionaryConfig.${dictKey}.${field}`,
              `请为动态字典配置 ${field} 字段`
            ));
          }
        }
      }

      // 4. 验证远程字典配置完整性
      if (dictConfig.type === 'remote') {
        if (!dictConfig.searchApi) {
          results.push(AuditResult.error(
            'REQ-3.1.8',
            `远程字典 ${dictKey} 缺少searchApi配置`,
            `$.dictionaryConfig.${dictKey}.searchApi`,
            '请为远程字典配置 searchApi 字段'
          ));
        }
        if (!dictConfig.debounce) {
          results.push(AuditResult.warning(
            'REQ-3.1.9',
            `远程字典 ${dictKey} 未配置防抖时间`,
            `$.dictionaryConfig.${dictKey}.debounce`,
            '建议为远程搜索配置debounce防抖，如: 300'
          ));
        }
      }
    }

    return results;
  }

  /**
   * 收集所有字段名及其出现位置
   */
  collectAllFields(config) {
    const fieldOccurrences = {};

    // 从搜索配置收集
    if (config.searchConfig?.fields) {
      config.searchConfig.fields.forEach((field, index) => {
        const fieldName = field.field || field.prop;
        if (fieldName) {
          this.addFieldOccurrence(fieldOccurrences, fieldName, `$.searchConfig.fields[${index}]`);
        }
      });
    }

    // 从表格配置收集
    if (config.tableConfig?.columns) {
      config.tableConfig.columns.forEach((column, index) => {
        const fieldName = column.prop || column.field;
        if (fieldName) {
          this.addFieldOccurrence(fieldOccurrences, fieldName, `$.tableConfig.columns[${index}]`);
        }
      });
    }

    // 从表单配置收集
    if (config.formConfig?.sections) {
      config.formConfig.sections.forEach((section, sIndex) => {
        if (section.fields) {
          section.fields.forEach((field, fIndex) => {
            const fieldName = field.prop || field.field;
            if (fieldName) {
              this.addFieldOccurrence(fieldOccurrences, fieldName, `$.formConfig.sections[${sIndex}].fields[${fIndex}]`);
            }
          });
        }
      });
    }

    // 从查询配置收集
    if (config.apiConfig?.queryConfig?.defaultConditions) {
      config.apiConfig.queryConfig.defaultConditions.forEach((condition, index) => {
        if (condition.field) {
          this.addFieldOccurrence(fieldOccurrences, condition.field, `$.apiConfig.queryConfig.defaultConditions[${index}].field`);
        }
      });
    }

    return fieldOccurrences;
  }

  /**
   * 添加字段出现位置
   */
  addFieldOccurrence(occurrences, fieldName, location) {
    if (!occurrences[fieldName]) {
      occurrences[fieldName] = [];
    }
    occurrences[fieldName].push(location);
  }

  /**
   * 判断是否为特殊字段（跳过命名检查）
   */
  isSpecialField(fieldName) {
    const specialFields = [
      'id', 'createTime', 'updateTime', 'createBy', 'updateBy',
      'deleted', 'version', 'tenantId', 'orgId'
    ];
    return specialFields.includes(fieldName.toLowerCase());
  }

  /**
   * 获取审计器支持的规则列表
   */
  getRules() {
    return [
      { id: 'REQ-3.1.1', name: '字段命名规范', severity: 'warning' },
      { id: 'REQ-3.1.2', name: '命名规范符合性', severity: 'info' },
      { id: 'REQ-3.1.3', name: '大小写一致性', severity: 'error' },
      { id: 'REQ-3.1.4', name: '字典键命名规范', severity: 'warning' },
      { id: 'REQ-3.1.5', name: '字典value唯一性', severity: 'error' },
      { id: 'REQ-3.1.6', name: '字典value非空', severity: 'error' },
      { id: 'REQ-3.1.7', name: '动态字典配置完整性', severity: 'error' },
      { id: 'REQ-3.1.8', name: '远程字典searchApi', severity: 'error' },
      { id: 'REQ-3.1.9', name: '远程字典防抖配置', severity: 'warning' }
    ];
  }
}

module.exports = NamingAuditor;
