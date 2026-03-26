/**
 * 结构完整性审计器
 * 验证配置文件结构完整性，检查必需节点、节点属性、节点关联性
 */

const { AuditResult } = require('../core/models/AuditResult');

/**
 * 必需的顶级节点定义
 */
const REQUIRED_NODES = {
  pageConfig: {
    required: true,
    fields: ['moduleCode', 'tableName', 'primaryKey', 'permissionPrefix']
  },
  apiConfig: {
    required: true,
    fields: ['engineApis']
  },
  businessConfig: {
    required: true,
    fields: []
  },
  searchConfig: {
    required: false,
    fields: []
  },
  tableConfig: {
    required: true,
    fields: ['columns']
  },
  formConfig: {
    required: true,
    fields: ['sections']
  },
  dictionaryConfig: {
    required: false,
    fields: []
  }
};

/**
 * 核心API接口定义
 */
const REQUIRED_APIS = {
  query: { required: true, description: '查询接口' },
  buildQuery: { required: true, description: '构建查询接口' },
  validation: { required: true, description: '验证接口' }
};

/**
 * 结构完整性审计器
 */
class StructureAuditor {
  get name() {
    return 'StructureAuditor';
  }

  get description() {
    return '验证配置文件结构完整性';
  }

  /**
   * 执行审计
   * @param {Object} config - 配置对象
   * @returns {Promise<AuditResult[]>}
   */
  async audit(config) {
    const results = [];

    // 1. 验证必需节点
    results.push(...this.validateRequiredNodes(config));

    // 2. 验证节点关联性
    results.push(...this.validateNodeRelations(config));

    // 3. 验证API接口完整性
    results.push(...this.validateApiIntegrity(config));

    return results;
  }

  /**
   * 验证必需节点
   */
  validateRequiredNodes(config) {
    const results = [];

    for (const [nodeName, nodeConfig] of Object.entries(REQUIRED_NODES)) {
      // 检查节点存在性
      if (!config[nodeName]) {
        if (nodeConfig.required) {
          results.push(AuditResult.error(
            'REQ-2.1.1',
            `缺少必需节点: ${nodeName}`,
            `$.${nodeName}`,
            `请添加 ${nodeName} 节点配置`
          ));
        }
        continue;
      }

      // 检查必需字段
      for (const field of nodeConfig.fields) {
        if (!config[nodeName][field]) {
          results.push(AuditResult.error(
            'REQ-2.1.2',
            `节点 ${nodeName} 缺少必需字段: ${field}`,
            `$.${nodeName}.${field}`,
            `请在 ${nodeName} 中添加 ${field} 字段`
          ));
        }
      }

      // 通过检查
      results.push(AuditResult.pass(
        'REQ-2.1.3',
        `节点 ${nodeName} 结构完整`,
        `$.${nodeName}`
      ));
    }

    return results;
  }

  /**
   * 验证节点关联性
   */
  validateNodeRelations(config) {
    const results = [];

    // 1. 验证searchConfig与queryConfig字段一致性
    if (config.searchConfig && config.apiConfig?.queryConfig) {
      results.push(...this.validateSearchQueryConsistency(config));
    }

    // 2. 验证tableConfig与formConfig字段映射
    if (config.tableConfig && config.formConfig) {
      results.push(...this.validateTableFormMapping(config));
    }

    // 3. 验证子表配置一致性
    if (config.subTableQueryConfigs) {
      results.push(...this.validateSubTableConfig(config));
    }

    return results;
  }

  /**
   * 验证搜索字段与查询条件一致性
   */
  validateSearchQueryConsistency(config) {
    const results = [];
    const searchFields = config.searchConfig.fields || [];
    const queryConditions = config.apiConfig.queryConfig?.defaultConditions || [];

    // 收集搜索字段名
    const searchFieldNames = new Set(
      searchFields.map(f => f.field || f.prop).filter(Boolean)
    );

    // 收集查询条件字段名
    const queryFieldNames = new Set(
      queryConditions.map(c => c.field).filter(Boolean)
    );

    // 检查搜索字段是否都有对应的查询条件
    for (const fieldName of searchFieldNames) {
      if (!queryFieldNames.has(fieldName)) {
        results.push(AuditResult.warning(
          'REQ-2.2.1',
          `搜索字段 ${fieldName} 在查询条件中未定义`,
          `$.searchConfig.fields[?(@.field=='${fieldName}')]`,
          `请在 apiConfig.queryConfig.defaultConditions 中添加 ${fieldName} 的查询条件`
        ));
      }
    }

    return results;
  }

  /**
   * 验证表格列与表单字段映射
   */
  validateTableFormMapping(config) {
    const results = [];
    const tableColumns = config.tableConfig.columns || [];
    const formSections = config.formConfig.sections || [];

    // 收集表格列字段名
    const tableFieldNames = new Set(
      tableColumns.map(c => c.prop || c.field).filter(Boolean)
    );

    // 收集表单字段名
    const formFieldNames = new Set();
    formSections.forEach(section => {
      const fields = section.fields || [];
      fields.forEach(f => {
        const fieldName = f.prop || f.field;
        if (fieldName) formFieldNames.add(fieldName);
      });
    });

    // 检查表格列是否都有对应的表单字段
    for (const fieldName of tableFieldNames) {
      if (!formFieldNames.has(fieldName)) {
        results.push(AuditResult.warning(
          'REQ-2.2.2',
          `表格列 ${fieldName} 在表单中未定义`,
          `$.tableConfig.columns[?(@.prop=='${fieldName}')]`,
          `建议在 formConfig 中添加 ${fieldName} 字段，或标记为只展示字段`
        ));
      }
    }

    return results;
  }

  /**
   * 验证子表配置一致性
   */
  validateSubTableConfig(config) {
    const results = [];
    const subTableConfigs = config.subTableQueryConfigs || [];

    subTableConfigs.forEach((subTable, index) => {
      // 检查子表名称
      if (!subTable.name && !subTable.tableName) {
        results.push(AuditResult.error(
          'REQ-2.2.3',
          `子表配置 ${index} 缺少名称标识`,
          `$.subTableQueryConfigs[${index}]`,
          '请为子表配置添加 name 或 tableName 字段'
        ));
      }

      // 检查子表查询条件
      if (!subTable.queryConditions || subTable.queryConditions.length === 0) {
        results.push(AuditResult.warning(
          'REQ-2.2.4',
          `子表 ${subTable.name || subTable.tableName} 未定义查询条件`,
          `$.subTableQueryConfigs[${index}].queryConditions`,
          '请定义子表查询条件，通常需要关联主表ID'
        ));
      }

      // 检查子表展示配置
      const hasExpandRow = config.expandRowDetail?.subTable === subTable.name;
      const hasDrawer = config.drawerConfig?.some(d => d.subTable === subTable.name);
      
      if (!hasExpandRow && !hasDrawer) {
        results.push(AuditResult.warning(
          'REQ-2.2.5',
          `子表 ${subTable.name || subTable.tableName} 未配置展示方式`,
          `$.subTableQueryConfigs[${index}]`,
          '请在 expandRowDetail 或 drawerConfig 中配置子表展示'
        ));
      }
    });

    return results;
  }

  /**
   * 验证API接口完整性
   */
  validateApiIntegrity(config) {
    const results = [];

    if (!config.apiConfig?.engineApis) {
      return results;
    }

    const engineApis = config.apiConfig.engineApis;

    // 检查核心API接口
    for (const [apiName, apiConfig] of Object.entries(REQUIRED_APIS)) {
      if (!engineApis[apiName]) {
        if (apiConfig.required) {
          results.push(AuditResult.error(
            'REQ-4.1.1',
            `缺少核心API接口: ${apiName} (${apiConfig.description})`,
            `$.apiConfig.engineApis.${apiName}`,
            `请在 engineApis 中添加 ${apiName} 接口配置`
          ));
        }
      } else {
        // 检查接口路径
        if (!engineApis[apiName].url && !engineApis[apiName].path) {
          results.push(AuditResult.error(
            'REQ-4.1.2',
            `API接口 ${apiName} 缺少路径配置`,
            `$.apiConfig.engineApis.${apiName}`,
            `请为 ${apiName} 接口添加 url 或 path 字段`
          ));
        }
      }
    }

    // 检查审批接口（如果启用审批）
    if (config.pageConfig?.enableApproval) {
      const approvalApis = ['submit', 'approve', 'reject', 'withdraw'];
      approvalApis.forEach(apiName => {
        if (!engineApis[apiName]) {
          results.push(AuditResult.warning(
            'REQ-4.2.1',
            `启用审批功能但缺少审批接口: ${apiName}`,
            `$.apiConfig.engineApis.${apiName}`,
            `请添加 ${apiName} 审批接口配置`
          ));
        }
      });
    }

    // 检查下推接口（如果启用下推）
    if (config.pageConfig?.enablePush) {
      if (!engineApis.push) {
        results.push(AuditResult.warning(
          'REQ-4.2.2',
          '启用下推功能但缺少下推接口',
          '$.apiConfig.engineApis.push',
          '请添加 push 下推接口配置'
        ));
      }
    }

    return results;
  }

  /**
   * 获取审计器支持的规则列表
   */
  getRules() {
    return [
      { id: 'REQ-2.1.1', name: '必需节点检查', severity: 'error' },
      { id: 'REQ-2.1.2', name: '必需字段检查', severity: 'error' },
      { id: 'REQ-2.1.3', name: '节点结构完整性', severity: 'info' },
      { id: 'REQ-2.2.1', name: '搜索查询一致性', severity: 'warning' },
      { id: 'REQ-2.2.2', name: '表格表单映射', severity: 'warning' },
      { id: 'REQ-2.2.3', name: '子表名称检查', severity: 'error' },
      { id: 'REQ-2.2.4', name: '子表查询条件', severity: 'warning' },
      { id: 'REQ-2.2.5', name: '子表展示配置', severity: 'warning' },
      { id: 'REQ-4.1.1', name: '核心API接口', severity: 'error' },
      { id: 'REQ-4.1.2', name: 'API路径配置', severity: 'error' },
      { id: 'REQ-4.2.1', name: '审批接口完整性', severity: 'warning' },
      { id: 'REQ-4.2.2', name: '下推接口完整性', severity: 'warning' }
    ];
  }
}

module.exports = StructureAuditor;
