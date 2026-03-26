/**
 * 生产可行性审计器
 * 验证生产环境数据库兼容性、权限配置、性能可行性
 */

const { AuditResult } = require('../core/models/AuditResult');

/**
 * 生产可行性审计器
 */
class FeasibilityAuditor {
  get name() {
    return 'FeasibilityAuditor';
  }

  get description() {
    return '验证生产环境可行性';
  }

  /**
   * 执行审计
   * @param {Object} config - 配置对象
   * @returns {Promise<AuditResult[]>}
   */
  async audit(config) {
    const results = [];

    // 1. 验证权限配置
    results.push(...this.validatePermissionConfig(config));

    // 2. 验证性能可行性
    results.push(...this.validatePerformanceFeasibility(config));

    // 3. 验证数据源配置
    results.push(...this.validateDataSourceConfig(config));

    return results;
  }

  /**
   * 验证权限配置
   */
  validatePermissionConfig(config) {
    const results = [];

    // 1. 验证权限前缀存在
    if (!config.pageConfig?.permissionPrefix) {
      results.push(AuditResult.warning(
        'REQ-8.1.1',
        'pageConfig缺少permissionPrefix配置',
        '$.pageConfig.permissionPrefix',
        '建议配置permissionPrefix进行权限控制，如: "k3:saleorder"'
      ));
    } else {
      const permissionPrefix = config.pageConfig.permissionPrefix;

      // 2. 验证按钮权限与前缀一致性
      if (config.tableConfig?.actions) {
        config.tableConfig.actions.forEach((action, index) => {
          if (action.permission) {
            // 检查权限是否以prefix开头
            if (!action.permission.includes(permissionPrefix) && 
                !action.permission.includes('{permissionPrefix}')) {
              results.push(AuditResult.warning(
                'REQ-8.1.2',
                `按钮 ${action.label} 的permission与permissionPrefix不一致`,
                `$.tableConfig.actions[${index}].permission`,
                `建议使用权限前缀: ${permissionPrefix}:xxx`
              ));
            }
          }
        });
      }

      // 3. 验证菜单权限配置
      if (config.pageConfig?.menuPermission) {
        if (!config.pageConfig.menuPermission.includes(permissionPrefix)) {
          results.push(AuditResult.warning(
            'REQ-8.1.3',
            'menuPermission与permissionPrefix不一致',
            '$.pageConfig.menuPermission',
            `建议menuPermission使用权限前缀: ${permissionPrefix}`
          ));
        }
      }
    }

    // 4. 验证数据权限配置
    if (config.apiConfig?.queryConfig?.defaultConditions) {
      const hasDataPermission = config.apiConfig.queryConfig.defaultConditions.some(c => 
        c.field && (c.field.includes('orgId') || c.field.includes('deptId') || c.field.includes('createBy'))
      );

      if (!hasDataPermission) {
        results.push(AuditResult.info(
          'REQ-8.1.4',
          '查询条件未配置数据权限过滤',
          '$.apiConfig.queryConfig.defaultConditions',
          '建议添加数据权限过滤条件，如: orgId、deptId、createBy'
        ));
      }
    }

    return results;
  }

  /**
   * 验证性能可行性
   */
  validatePerformanceFeasibility(config) {
    const results = [];

    // 1. 评估表格列数量对性能影响
    if (config.tableConfig?.columns) {
      const columnCount = config.tableConfig.columns.length;
      
      if (columnCount > 20) {
        results.push(AuditResult.warning(
          'REQ-8.2.1',
          `表格列数量过多: ${columnCount}列`,
          '$.tableConfig.columns',
          '列数量过多会影响渲染性能，建议控制在20列以内，或使用分页加载'
        ));
      } else if (columnCount > 15) {
        results.push(AuditResult.info(
          'REQ-8.2.2',
          `表格列数量较多: ${columnCount}列`,
          '$.tableConfig.columns',
          '建议考虑是否所有列都需要展示，可隐藏低频使用列'
        ));
      }
    }

    // 2. 评估字典预加载数量对内存影响
    if (config.dictionaryConfig) {
      const dictCount = Object.keys(config.dictionaryConfig).length;
      
      if (dictCount > 20) {
        results.push(AuditResult.warning(
          'REQ-8.2.3',
          `字典数量过多: ${dictCount}个`,
          '$.dictionaryConfig',
          '字典数量过多会增加内存占用和初始化时间，建议按需加载或合并相似字典'
        ));
      }

      // 检查静态字典数据量
      for (const [dictKey, dictConfig] of Object.entries(config.dictionaryConfig)) {
        if (dictConfig.type === 'static' && dictConfig.data) {
          if (dictConfig.data.length > 100) {
            results.push(AuditResult.warning(
              'REQ-8.2.4',
              `静态字典 ${dictKey} 数据量过大: ${dictConfig.data.length}条`,
              `$.dictionaryConfig.${dictKey}.data`,
              '静态字典数据量过大影响性能，建议改为动态字典或远程字典'
            ));
          }
        }
      }
    }

    // 3. 评估子表加载策略对响应时间要求
    if (config.subTableQueryConfigs) {
      config.subTableQueryConfigs.forEach((subTable, index) => {
        const tableName = subTable.name || subTable.tableName;
        
        // 检查子表加载策略
        if (!subTable.lazy && !subTable.loadStrategy) {
          results.push(AuditResult.info(
            'REQ-8.2.5',
            `子表 ${tableName} 未配置加载策略`,
            `$.subTableQueryConfigs[${index}]`,
            '建议配置lazy懒加载或loadStrategy优化性能'
          ));
        }

        // 检查子表分页配置
        if (!subTable.pagination && !subTable.pageSize) {
          results.push(AuditResult.info(
            'REQ-8.2.6',
            `子表 ${tableName} 未配置分页`,
            `$.subTableQueryConfigs[${index}]`,
            '建议为子表配置分页避免一次性加载大量数据'
          ));
        }
      });
    }

    // 4. 评估查询条件复杂度
    if (config.apiConfig?.queryConfig?.defaultConditions) {
      const conditionCount = config.apiConfig.queryConfig.defaultConditions.length;
      
      if (conditionCount > 10) {
        results.push(AuditResult.warning(
          'REQ-8.2.7',
          `查询条件过多: ${conditionCount}个`,
          '$.apiConfig.queryConfig.defaultConditions',
          '查询条件过多会影响查询性能，建议优化查询逻辑'
        ));
      }

      // 检查复杂查询操作符
      const complexOperators = ['like', 'in', 'between'];
      const complexConditions = config.apiConfig.queryConfig.defaultConditions.filter(c => 
        complexOperators.includes(c.operator?.toLowerCase())
      );

      if (complexConditions.length > 3) {
        results.push(AuditResult.info(
          'REQ-8.2.8',
          `存在多个复杂查询条件: ${complexConditions.length}个`,
          '$.apiConfig.queryConfig.defaultConditions',
          '复杂查询条件(like/in/between)可能影响性能，建议添加索引优化'
        ));
      }
    }

    return results;
  }

  /**
   * 验证数据源配置
   */
  validateDataSourceConfig(config) {
    const results = [];

    // 1. 验证动态字典数据源可用性
    if (config.dictionaryConfig) {
      for (const [dictKey, dictConfig] of Object.entries(config.dictionaryConfig)) {
        if (dictConfig.type === 'dynamic') {
          // 检查tableName
          if (!dictConfig.tableName) {
            results.push(AuditResult.error(
              'REQ-8.3.1',
              `动态字典 ${dictKey} 缺少tableName`,
              `$.dictionaryConfig.${dictKey}.tableName`,
              '请配置tableName指定数据源表'
            ));
          }

          // 检查queryConfig
          if (!dictConfig.queryConfig) {
            results.push(AuditResult.error(
              'REQ-8.3.2',
              `动态字典 ${dictKey} 缺少queryConfig`,
              `$.dictionaryConfig.${dictKey}.queryConfig`,
              '请配置queryConfig定义查询条件'
            ));
          }

          // 检查fieldMapping
          if (!dictConfig.fieldMapping) {
            results.push(AuditResult.error(
              'REQ-8.3.3',
              `动态字典 ${dictKey} 缺少fieldMapping`,
              `$.dictionaryConfig.${dictKey}.fieldMapping`,
              '请配置fieldMapping定义字段映射'
            ));
          } else {
            // 验证fieldMapping包含必需字段
            if (!dictConfig.fieldMapping.label && !dictConfig.fieldMapping.name) {
              results.push(AuditResult.warning(
                'REQ-8.3.4',
                `动态字典 ${dictKey} 的fieldMapping缺少label字段`,
                `$.dictionaryConfig.${dictKey}.fieldMapping`,
                '请配置label字段映射，如: {label: "name", value: "id"}'
              ));
            }
            if (!dictConfig.fieldMapping.value && !dictConfig.fieldMapping.id) {
              results.push(AuditResult.warning(
                'REQ-8.3.5',
                `动态字典 ${dictKey} 的fieldMapping缺少value字段`,
                `$.dictionaryConfig.${dictKey}.fieldMapping`,
                '请配置value字段映射，如: {label: "name", value: "id"}'
              ));
            }
          }
        }
      }
    }

    // 2. 验证远程字典数据源可用性
    if (config.dictionaryConfig) {
      for (const [dictKey, dictConfig] of Object.entries(config.dictionaryConfig)) {
        if (dictConfig.type === 'remote') {
          // 检查searchApi
          if (!dictConfig.searchApi) {
            results.push(AuditResult.error(
              'REQ-8.3.6',
              `远程字典 ${dictKey} 缺少searchApi`,
              `$.dictionaryConfig.${dictKey}.searchApi`,
              '请配置searchApi指定远程搜索接口'
            ));
          }

          // 检查响应字段映射
          if (!dictConfig.responseMapping) {
            results.push(AuditResult.info(
              'REQ-8.3.7',
              `远程字典 ${dictKey} 未配置responseMapping`,
              `$.dictionaryConfig.${dictKey}.responseMapping`,
              '建议配置responseMapping指定响应数据字段映射'
            ));
          }
        }
      }
    }

    return results;
  }

  /**
   * 获取审计器支持的规则列表
   */
  getRules() {
    return [
      { id: 'REQ-8.1.1', name: 'permissionPrefix', severity: 'warning' },
      { id: 'REQ-8.1.2', name: '按钮权限一致性', severity: 'warning' },
      { id: 'REQ-8.1.3', name: '菜单权限一致性', severity: 'warning' },
      { id: 'REQ-8.1.4', name: '数据权限过滤', severity: 'info' },
      { id: 'REQ-8.2.1', name: '表格列数量', severity: 'warning' },
      { id: 'REQ-8.2.2', name: '表格列优化', severity: 'info' },
      { id: 'REQ-8.2.3', name: '字典数量', severity: 'warning' },
      { id: 'REQ-8.2.4', name: '静态字典数据量', severity: 'warning' },
      { id: 'REQ-8.2.5', name: '子表加载策略', severity: 'info' },
      { id: 'REQ-8.2.6', name: '子表分页', severity: 'info' },
      { id: 'REQ-8.2.7', name: '查询条件数量', severity: 'warning' },
      { id: 'REQ-8.2.8', name: '复杂查询优化', severity: 'info' },
      { id: 'REQ-8.3.1', name: '动态字典tableName', severity: 'error' },
      { id: 'REQ-8.3.2', name: '动态字典queryConfig', severity: 'error' },
      { id: 'REQ-8.3.3', name: '动态字典fieldMapping', severity: 'error' },
      { id: 'REQ-8.3.4', name: 'fieldMapping label', severity: 'warning' },
      { id: 'REQ-8.3.5', name: 'fieldMapping value', severity: 'warning' },
      { id: 'REQ-8.3.6', name: '远程字典searchApi', severity: 'error' },
      { id: 'REQ-8.3.7', name: '远程字典responseMapping', severity: 'info' }
    ];
  }
}

module.exports = FeasibilityAuditor;
