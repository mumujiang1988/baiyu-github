/**
 * API接口审计器
 * 验证API接口路径规范性、接口依赖关系、动态参数可解析性
 */

const { AuditResult } = require('../core/models/AuditResult');

/**
 * API接口审计器
 */
class ApiAuditor {
  get name() {
    return 'ApiAuditor';
  }

  get description() {
    return '验证API接口配置完整性';
  }

  /**
   * 执行审计
   * @param {Object} config - 配置对象
   * @returns {Promise<AuditResult[]>}
   */
  async audit(config) {
    const results = [];

    if (!config.apiConfig?.engineApis) {
      return results;
    }

    // 1. 验证接口路径规范性
    results.push(...this.validateApiPaths(config));

    // 2. 验证动态参数可解析性
    results.push(...this.validateDynamicParams(config));

    // 3. 验证接口依赖关系
    results.push(...this.validateApiDependencies(config));

    return results;
  }

  /**
   * 验证接口路径规范性
   */
  validateApiPaths(config) {
    const results = [];
    const engineApis = config.apiConfig.engineApis;

    for (const [apiName, apiConfig] of Object.entries(engineApis)) {
      const path = apiConfig.url || apiConfig.path;
      
      if (!path) {
        continue; // 已在StructureAuditor中检查
      }

      // 1. 验证路径以/erp/engine开头
      if (!path.startsWith('/erp/engine')) {
        results.push(AuditResult.warning(
          'REQ-4.1.3',
          `API接口 ${apiName} 路径不符合规范: ${path}`,
          `$.apiConfig.engineApis.${apiName}.url`,
          '建议使用 /erp/engine 开头的RESTful路径'
        ));
      }

      // 2. 验证RESTful规范
      if (!this.isValidRestfulPath(path)) {
        results.push(AuditResult.warning(
          'REQ-4.1.4',
          `API接口 ${apiName} 路径不符合RESTful规范: ${path}`,
          `$.apiConfig.engineApis.${apiName}.url`,
          '建议使用RESTful风格路径，如: /erp/engine/{moduleCode}/query'
        ));
      }

      // 3. 验证HTTP方法
      const method = apiConfig.method || 'GET';
      const validMethods = ['GET', 'POST', 'PUT', 'DELETE', 'PATCH'];
      if (!validMethods.includes(method.toUpperCase())) {
        results.push(AuditResult.error(
          'REQ-4.1.5',
          `API接口 ${apiName} 使用了无效的HTTP方法: ${method}`,
          `$.apiConfig.engineApis.${apiName}.method`,
          `请使用标准HTTP方法: ${validMethods.join(', ')}`
        ));
      }
    }

    return results;
  }

  /**
   * 验证动态参数可解析性
   */
  validateDynamicParams(config) {
    const results = [];
    const engineApis = config.apiConfig.engineApis;
    const moduleCode = config.pageConfig?.moduleCode;

    // 定义动态参数模式
    const dynamicParamPattern = /\{(\w+)\}/g;

    for (const [apiName, apiConfig] of Object.entries(engineApis)) {
      const path = apiConfig.url || apiConfig.path;
      
      if (!path) {
        continue;
      }

      // 提取动态参数
      const dynamicParams = [];
      let match;
      while ((match = dynamicParamPattern.exec(path)) !== null) {
        dynamicParams.push(match[1]);
      }

      // 验证动态参数可解析
      for (const param of dynamicParams) {
        // 常见动态参数
        const commonParams = ['moduleCode', 'billNo', 'billId', 'id', 'tableName'];
        
        if (param === 'moduleCode') {
          // moduleCode应该在pageConfig中定义
          if (!moduleCode) {
            results.push(AuditResult.error(
              'REQ-4.1.6',
              `API接口 ${apiName} 使用动态参数 {moduleCode} 但pageConfig未定义moduleCode`,
              `$.apiConfig.engineApis.${apiName}.url`,
              '请在 pageConfig.moduleCode 中定义模块编码'
            ));
          }
        } else if (!commonParams.includes(param)) {
          // 非常见参数，给出警告
          results.push(AuditResult.warning(
            'REQ-4.1.7',
            `API接口 ${apiName} 使用了非标准动态参数: {${param}}`,
            `$.apiConfig.engineApis.${apiName}.url`,
            `请确保参数 ${param} 在运行时可解析`
          ));
        }
      }
    }

    // 验证子表API路径格式
    if (config.subTableQueryConfigs) {
      config.subTableQueryConfigs.forEach((subTable, index) => {
        if (subTable.apiPath) {
          // 子表API应包含主表ID参数
          if (!subTable.apiPath.includes('{billId}') && !subTable.apiPath.includes('{id}')) {
            results.push(AuditResult.warning(
              'REQ-4.1.8',
              `子表 ${subTable.name} API路径未包含主表ID参数`,
              `$.subTableQueryConfigs[${index}].apiPath`,
              '建议子表API路径包含 {billId} 或 {id} 参数以关联主表'
            ));
          }
        }
      });
    }

    return results;
  }

  /**
   * 验证接口依赖关系
   */
  validateApiDependencies(config) {
    const results = [];
    const engineApis = config.apiConfig.engineApis;

    // 1. 验证审批按钮与审批接口依赖
    if (config.tableConfig?.actions) {
      const approvalActions = ['approve', 'reject', 'withdraw', 'submit'];
      
      config.tableConfig.actions.forEach((action, index) => {
        if (approvalActions.includes(action.type || action.action)) {
          const apiName = action.api || action.type;
          
          if (!engineApis[apiName]) {
            results.push(AuditResult.error(
              'REQ-4.2.3',
              `审批按钮 ${action.label || action.type} 缺少对应的API接口: ${apiName}`,
              `$.tableConfig.actions[${index}]`,
              `请在 engineApis 中添加 ${apiName} 接口配置`
            ));
          }
        }
      });
    }

    // 2. 验证下推按钮与下推接口依赖
    if (config.tableConfig?.actions) {
      config.tableConfig.actions.forEach((action, index) => {
        if (action.type === 'push' || action.action === 'push') {
          if (!engineApis.push && !action.api) {
            results.push(AuditResult.error(
              'REQ-4.2.4',
              `下推按钮 ${action.label} 缺少下推接口配置`,
              `$.tableConfig.actions[${index}]`,
              '请在 engineApis 中添加 push 接口配置或在按钮中指定 api 字段'
            ));
          }

          // 验证下推目标配置
          if (!action.pushConfig && !action.targetModule) {
            results.push(AuditResult.warning(
              'REQ-4.2.5',
              `下推按钮 ${action.label} 未配置下推目标`,
              `$.tableConfig.actions[${index}]`,
              '请配置 pushConfig 或 targetModule 指定下推目标模块'
            ));
          }
        }
      });
    }

    // 3. 验证导出按钮与导出接口依赖
    if (config.tableConfig?.actions) {
      config.tableConfig.actions.forEach((action, index) => {
        if (action.type === 'export' || action.action === 'export') {
          if (!engineApis.export && !action.api) {
            results.push(AuditResult.warning(
              'REQ-4.2.6',
              `导出按钮 ${action.label} 未配置导出接口`,
              `$.tableConfig.actions[${index}]`,
              '建议配置 export 接口以支持数据导出功能'
            ));
          }
        }
      });
    }

    return results;
  }

  /**
   * 验证是否为有效的RESTful路径
   */
  isValidRestfulPath(path) {
    // RESTful路径规范:
    // 1. 使用小写字母和连字符
    // 2. 使用名词表示资源
    // 3. 使用动态参数表示资源ID
    // 4. 避免动词

    const segments = path.split('/').filter(s => s && !s.startsWith('{'));
    
    // 检查每个段是否符合规范
    for (const segment of segments) {
      // 允许小写字母、数字、连字符
      if (!/^[a-z0-9-]+$/.test(segment)) {
        return false;
      }

      // 避免常见动词
      const verbs = ['get', 'post', 'put', 'delete', 'add', 'update', 'remove', 'query', 'search'];
      if (verbs.includes(segment)) {
        return false;
      }
    }

    return true;
  }

  /**
   * 获取审计器支持的规则列表
   */
  getRules() {
    return [
      { id: 'REQ-4.1.3', name: 'API路径规范', severity: 'warning' },
      { id: 'REQ-4.1.4', name: 'RESTful规范', severity: 'warning' },
      { id: 'REQ-4.1.5', name: 'HTTP方法验证', severity: 'error' },
      { id: 'REQ-4.1.6', name: '动态参数moduleCode', severity: 'error' },
      { id: 'REQ-4.1.7', name: '非标准动态参数', severity: 'warning' },
      { id: 'REQ-4.1.8', name: '子表API参数', severity: 'warning' },
      { id: 'REQ-4.2.3', name: '审批接口依赖', severity: 'error' },
      { id: 'REQ-4.2.4', name: '下推接口依赖', severity: 'error' },
      { id: 'REQ-4.2.5', name: '下推目标配置', severity: 'warning' },
      { id: 'REQ-4.2.6', name: '导出接口配置', severity: 'warning' }
    ];
  }
}

module.exports = ApiAuditor;
