/**
 * 审计结果模型
 */

/**
 * 严重程度枚举
 */
const Severity = {
  ERROR: 'error',    // 错误 - 阻塞部署
  WARNING: 'warning', // 警告 - 建议修复
  INFO: 'info'       // 提示 - 优化建议
};

/**
 * 审计结果类
 */
class AuditResult {
  /**
   * @param {Object} params
   * @param {string} params.ruleId - 规则编号
   * @param {string} params.severity - 严重程度
   * @param {boolean} params.passed - 是否通过
   * @param {string} params.message - 审计消息
   * @param {string} params.path - 配置路径
   * @param {string} params.suggestion - 修复建议
   * @param {Object} [params.details] - 详细信息
   */
  constructor({ ruleId, severity, passed, message, path, suggestion, details }) {
    this.ruleId = ruleId;
    this.severity = severity;
    this.passed = passed;
    this.message = message;
    this.path = path;
    this.suggestion = suggestion;
    this.details = details || {};
    this.timestamp = new Date().toISOString();
  }

  /**
   * 创建通过结果
   */
  static pass(ruleId, message, path = '') {
    return new AuditResult({
      ruleId,
      severity: Severity.INFO,
      passed: true,
      message,
      path,
      suggestion: ''
    });
  }

  /**
   * 创建错误结果
   */
  static error(ruleId, message, path, suggestion) {
    return new AuditResult({
      ruleId,
      severity: Severity.ERROR,
      passed: false,
      message,
      path,
      suggestion
    });
  }

  /**
   * 创建警告结果
   */
  static warning(ruleId, message, path, suggestion) {
    return new AuditResult({
      ruleId,
      severity: Severity.WARNING,
      passed: false,
      message,
      path,
      suggestion
    });
  }

  /**
   * 创建提示结果
   */
  static info(ruleId, message, path, suggestion) {
    return new AuditResult({
      ruleId,
      severity: Severity.INFO,
      passed: true,
      message,
      path,
      suggestion
    });
  }

  /**
   * 转换为JSON
   */
  toJSON() {
    return {
      ruleId: this.ruleId,
      severity: this.severity,
      passed: this.passed,
      message: this.message,
      path: this.path,
      suggestion: this.suggestion,
      details: this.details,
      timestamp: this.timestamp
    };
  }
}

module.exports = { AuditResult, Severity };
