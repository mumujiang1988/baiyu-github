/**
 * CRUD审计器
 * 验证后端查询构建器、CRUD操作配置
 */

const { AuditResult } = require('../core/models/AuditResult');

/**
 * CRUD审计器
 */
class CrudAuditor {
  get name() {
    return 'CrudAuditor';
  }

  get description() {
    return '验证CRUD操作配置完整性';
  }

  /**
   * 执行审计
   * @param {Object} config - 配置对象
   * @returns {Promise<AuditResult[]>}
   */
  async audit(config) {
    const results = [];

    // 1. 验证查询构建器配置
    results.push(...this.validateQueryBuilder(config));

    // 2. 验证主表CRUD配置
    results.push(...this.validateMainTableCrud(config));

    // 3. 验证子表CRUD配置
    results.push(...this.validateSubTableCrud(config));

    // 4. 验证数据库兼容性
    results.push(...this.validateDatabaseCompatibility(config));

    return results;
  }

  /**
   * 验证查询构建器配置
   */
  validateQueryBuilder(config) {
    const results = [];

    if (!config.apiConfig?.queryConfig) {
      return results;
    }

    const queryConfig = config.apiConfig.queryConfig;

    // 1. 验证defaultConditions非空
    if (!queryConfig.defaultConditions || queryConfig.defaultConditions.length === 0) {
      results.push(AuditResult.warning(
        'REQ-7.1.1',
        '查询配置缺少defaultConditions',
        '$.apiConfig.queryConfig.defaultConditions',
        '建议配置默认查询条件，至少包含数据权限过滤'
      ));
    } else {
      // 验证查询条件格式
      queryConfig.defaultConditions.forEach((condition, index) => {
        const path = `$.apiConfig.queryConfig.defaultConditions[${index}]`;

        // 验证field存在
        if (!condition.field) {
          results.push(AuditResult.error(
            'REQ-7.1.2',
            `查询条件 ${index} 缺少field`,
            path,
            '请为查询条件配置field字段'
          ));
        }

        // 验证operator有效性
        const validOperators = ['=', '!=', '>', '<', '>=', '<=', 'like', 'in', 'between', 'is null', 'is not null'];
        if (condition.operator && !validOperators.includes(condition.operator.toLowerCase())) {
          results.push(AuditResult.error(
            'REQ-7.1.3',
            `查询条件使用了无效的operator: ${condition.operator}`,
            path,
            `请使用有效操作符: ${validOperators.join(', ')}`
          ));
        }

        // 验证between操作符值格式
        if (condition.operator === 'between') {
          if (!Array.isArray(condition.value) || condition.value.length !== 2) {
            results.push(AuditResult.error(
              'REQ-7.1.4',
              `between操作符的值格式错误`,
              path,
              'between操作符的value应为包含2个元素的数组，如: ["2024-01-01", "2024-12-31"]'
            ));
          }
        }

        // 验证in操作符值格式
        if (condition.operator === 'in') {
          if (!Array.isArray(condition.value)) {
            results.push(AuditResult.error(
              'REQ-7.1.5',
              `in操作符的值格式错误`,
              path,
              'in操作符的value应为数组'
            ));
          }
        }
      });
    }

    // 2. 验证变量占位符可解析
    if (queryConfig.defaultConditions) {
      queryConfig.defaultConditions.forEach((condition, index) => {
        if (typeof condition.value === 'string' && condition.value.startsWith('{') && condition.value.endsWith('}')) {
          const varName = condition.value.slice(1, -1);
          const validVars = ['moduleCode', 'userId', 'orgId', 'deptId', 'billId', 'billNo'];
          
          if (!validVars.includes(varName)) {
            results.push(AuditResult.warning(
              'REQ-7.1.6',
              `查询条件使用了非标准变量占位符: ${condition.value}`,
              `$.apiConfig.queryConfig.defaultConditions[${index}].value`,
              `请确保变量 ${varName} 在运行时可解析，标准变量: ${validVars.join(', ')}`
            ));
          }
        }
      });
    }

    // 3. 验证排序配置合理性
    if (queryConfig.orderBy) {
      if (!Array.isArray(queryConfig.orderBy)) {
        results.push(AuditResult.warning(
          'REQ-7.1.7',
          'orderBy配置格式错误',
          '$.apiConfig.queryConfig.orderBy',
          'orderBy应为数组格式，如: [{field: "createTime", order: "desc"}]'
        ));
      } else {
        queryConfig.orderBy.forEach((order, index) => {
          if (!order.field) {
            results.push(AuditResult.error(
              'REQ-7.1.8',
              `排序配置 ${index} 缺少field`,
              `$.apiConfig.queryConfig.orderBy[${index}]`,
              '请为排序配置指定field字段'
            ));
          }
          if (order.order && !['asc', 'desc'].includes(order.order.toLowerCase())) {
            results.push(AuditResult.error(
              'REQ-7.1.9',
              `排序配置使用了无效的order: ${order.order}`,
              `$.apiConfig.queryConfig.orderBy[${index}]`,
              '请使用 asc 或 desc'
            ));
          }
        });
      }
    }

    return results;
  }

  /**
   * 验证主表CRUD配置
   */
  validateMainTableCrud(config) {
    const results = [];

    if (!config.pageConfig) {
      return results;
    }

    // 1. 验证tableName
    if (!config.pageConfig.tableName) {
      results.push(AuditResult.error(
        'REQ-7.2.1',
        'pageConfig缺少tableName配置',
        '$.pageConfig.tableName',
        '请配置tableName指定主表名称'
      ));
    } else {
      // 验证表名格式
      if (!this.isValidTableName(config.pageConfig.tableName)) {
        results.push(AuditResult.warning(
          'REQ-7.2.2',
          `表名 ${config.pageConfig.tableName} 格式不规范`,
          '$.pageConfig.tableName',
          '建议使用小写字母和下划线，如: t_sale_order'
        ));
      }
    }

    // 2. 验证primaryKey
    if (!config.pageConfig.primaryKey) {
      results.push(AuditResult.error(
        'REQ-7.2.3',
        'pageConfig缺少primaryKey配置',
        '$.pageConfig.primaryKey',
        '请配置primaryKey指定主键字段'
      ));
    }

    // 3. 验证CRUD接口配置
    const engineApis = config.apiConfig?.engineApis || {};
    const crudApis = {
      query: '查询',
      add: '新增',
      edit: '编辑',
      delete: '删除'
    };

    for (const [apiName, description] of Object.entries(crudApis)) {
      if (!engineApis[apiName]) {
        results.push(AuditResult.warning(
          'REQ-7.2.4',
          `缺少${description}接口: ${apiName}`,
          `$.apiConfig.engineApis.${apiName}`,
          `建议配置 ${apiName} 接口支持${description}操作`
        ));
      }
    }

    return results;
  }

  /**
   * 验证子表CRUD配置
   */
  validateSubTableCrud(config) {
    const results = [];

    if (!config.subTableQueryConfigs) {
      return results;
    }

    config.subTableQueryConfigs.forEach((subTable, index) => {
      const path = `$.subTableQueryConfigs[${index}]`;
      const tableName = subTable.name || subTable.tableName;

      // 1. 验证子表tableName配置
      if (!subTable.tableName) {
        results.push(AuditResult.error(
          'REQ-7.3.1',
          `子表 ${tableName} 缺少tableName配置`,
          `${path}.tableName`,
          '请为子表配置tableName'
        ));
      }

      // 2. 验证子表primaryKey配置
      if (!subTable.primaryKey) {
        results.push(AuditResult.warning(
          'REQ-7.3.2',
          `子表 ${tableName} 缺少primaryKey配置`,
          `${path}.primaryKey`,
          '建议配置子表主键字段'
        ));
      }

      // 3. 验证可编辑子表操作标识
      if (subTable.editable) {
        // 可编辑子表应配置CRUD接口
        const requiredApis = ['add', 'edit', 'delete'];
        requiredApis.forEach(apiName => {
          const apiPath = subTable[`${apiName}Api`] || subTable.apis?.[apiName];
          if (!apiPath) {
            results.push(AuditResult.warning(
              'REQ-7.3.3',
              `可编辑子表 ${tableName} 缺少${apiName}接口配置`,
              path,
              `请为可编辑子表配置 ${apiName} 接口`
            ));
          }
        });
      }
    });

    return results;
  }

  /**
   * 验证数据库兼容性
   */
  validateDatabaseCompatibility(config) {
    const results = [];

    // 1. 验证表名符合数据库命名规范
    const tableNames = [];
    
    if (config.pageConfig?.tableName) {
      tableNames.push({ name: config.pageConfig.tableName, path: '$.pageConfig.tableName' });
    }

    if (config.subTableQueryConfigs) {
      config.subTableQueryConfigs.forEach((subTable, index) => {
        if (subTable.tableName) {
          tableNames.push({ 
            name: subTable.tableName, 
            path: `$.subTableQueryConfigs[${index}].tableName` 
          });
        }
      });
    }

    tableNames.forEach(({ name, path }) => {
      // MySQL表名规范: 小写字母、数字、下划线，最长64字符
      if (!/^[a-z0-9_]{1,64}$/.test(name)) {
        results.push(AuditResult.warning(
          'REQ-7.4.1',
          `表名 ${name} 不符合MySQL命名规范`,
          path,
          'MySQL表名应使用小写字母、数字、下划线，最长64字符'
        ));
      }

      // 检查保留字
      const reservedWords = ['order', 'group', 'user', 'table', 'column', 'index', 'key'];
      if (reservedWords.includes(name.toLowerCase())) {
        results.push(AuditResult.error(
          'REQ-7.4.2',
          `表名 ${name} 使用了数据库保留字`,
          path,
          '请避免使用数据库保留字作为表名'
        ));
      }
    });

    // 2. 验证字段与数据库列映射关系
    if (config.tableConfig?.columns) {
      config.tableConfig.columns.forEach((column, index) => {
        const fieldName = column.prop || column.field;
        
        if (fieldName) {
          // 字段名长度检查
          if (fieldName.length > 64) {
            results.push(AuditResult.warning(
              'REQ-7.4.3',
              `字段名 ${fieldName} 过长`,
              `$.tableConfig.columns[${index}]`,
              'MySQL列名最长64字符'
            ));
          }

          // 字段名格式检查
          if (!/^[a-zA-Z0-9_]+$/.test(fieldName)) {
            results.push(AuditResult.warning(
              'REQ-7.4.4',
              `字段名 ${fieldName} 包含特殊字符`,
              `$.tableConfig.columns[${index}]`,
              '建议使用字母、数字、下划线'
            ));
          }
        }
      });
    }

    return results;
  }

  /**
   * 验证表名格式
   */
  isValidTableName(tableName) {
    // 表名规范: 小写字母开头，可包含小写字母、数字、下划线
    return /^[a-z][a-z0-9_]*$/.test(tableName);
  }

  /**
   * 获取审计器支持的规则列表
   */
  getRules() {
    return [
      { id: 'REQ-7.1.1', name: 'defaultConditions', severity: 'warning' },
      { id: 'REQ-7.1.2', name: '查询条件field', severity: 'error' },
      { id: 'REQ-7.1.3', name: 'operator有效性', severity: 'error' },
      { id: 'REQ-7.1.4', name: 'between格式', severity: 'error' },
      { id: 'REQ-7.1.5', name: 'in格式', severity: 'error' },
      { id: 'REQ-7.1.6', name: '变量占位符', severity: 'warning' },
      { id: 'REQ-7.1.7', name: 'orderBy格式', severity: 'warning' },
      { id: 'REQ-7.1.8', name: '排序field', severity: 'error' },
      { id: 'REQ-7.1.9', name: '排序order', severity: 'error' },
      { id: 'REQ-7.2.1', name: '主表tableName', severity: 'error' },
      { id: 'REQ-7.2.2', name: '表名格式', severity: 'warning' },
      { id: 'REQ-7.2.3', name: 'primaryKey', severity: 'error' },
      { id: 'REQ-7.2.4', name: 'CRUD接口', severity: 'warning' },
      { id: 'REQ-7.3.1', name: '子表tableName', severity: 'error' },
      { id: 'REQ-7.3.2', name: '子表primaryKey', severity: 'warning' },
      { id: 'REQ-7.3.3', name: '可编辑子表接口', severity: 'warning' },
      { id: 'REQ-7.4.1', name: 'MySQL表名规范', severity: 'warning' },
      { id: 'REQ-7.4.2', name: '保留字检查', severity: 'error' },
      { id: 'REQ-7.4.3', name: '字段名长度', severity: 'warning' },
      { id: 'REQ-7.4.4', name: '字段名格式', severity: 'warning' }
    ];
  }
}

module.exports = CrudAuditor;
