/**
 * 审计器接口定义
 * 所有审计器必须实现此接口
 */

/**
 * @typedef {Object} AuditResult
 * @property {string} ruleId - 规则编号 (如: REQ-2.1.1)
 * @property {'error'|'warning'|'info'} severity - 严重程度
 * @property {boolean} passed - 是否通过
 * @property {string} message - 审计消息
 * @property {string} path - 配置路径 (JSONPath格式)
 * @property {string} suggestion - 修复建议
 * @property {Object} [details] - 详细信息
 */

/**
 * @typedef {Object} AuditRule
 * @property {string} id - 规则ID
 * @property {string} name - 规则名称
 * @property {string} description - 规则描述
 * @property {'error'|'warning'|'info'} severity - 默认严重程度
 * @property {Function} validate - 验证函数
 */

/**
 * 审计器接口
 * @interface IAuditor
 */
class IAuditor {
  /**
   * 审计器名称
   * @type {string}
   */
  get name() {
    throw new Error('必须实现 name 属性');
  }

  /**
   * 审计器描述
   * @type {string}
   */
  get description() {
    throw new Error('必须实现 description 属性');
  }

  /**
   * 执行审计
   * @param {Object} config - 配置对象
   * @param {Object} [options] - 审计选项
   * @returns {Promise<AuditResult[]>} 审计结果数组
   */
  async audit(config, options = {}) {
    throw new Error('必须实现 audit 方法');
  }

  /**
   * 获取审计器支持的规则列表
   * @returns {AuditRule[]} 规则列表
   */
  getRules() {
    return [];
  }
}

module.exports = IAuditor;
