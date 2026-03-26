/**
 * 审计报告模型
 */

/**
 * 审计报告汇总统计
 */
class AuditSummary {
  constructor() {
    this.total = 0;        // 总检查项
    this.passed = 0;       // 通过项
    this.failed = 0;       // 失败项
    this.errors = 0;       // 错误数
    this.warnings = 0;     // 警告数
    this.infos = 0;        // 提示数
  }

  /**
   * 添加结果统计
   * @param {AuditResult} result 
   */
  addResult(result) {
    this.total++;
    if (result.passed) {
      this.passed++;
    } else {
      this.failed++;
    }

    switch (result.severity) {
      case 'error':
        this.errors++;
        break;
      case 'warning':
        this.warnings++;
        break;
      case 'info':
        this.infos++;
        break;
    }
  }

  /**
   * 获取通过率
   */
  getPassRate() {
    return this.total > 0 ? ((this.passed / this.total) * 100).toFixed(2) : 0;
  }

  /**
   * 转换为JSON
   */
  toJSON() {
    return {
      total: this.total,
      passed: this.passed,
      failed: this.failed,
      errors: this.errors,
      warnings: this.warnings,
      infos: this.infos,
      passRate: this.getPassRate()
    };
  }
}

/**
 * 审计报告类
 */
class AuditReport {
  /**
   * @param {Object} params
   * @param {string} params.configPath - 配置文件路径
   * @param {AuditResult[]} params.results - 审计结果数组
   * @param {Object} [params.metadata] - 元数据
   */
  constructor({ configPath, results, metadata = {} }) {
    this.configPath = configPath;
    this.results = results;
    this.metadata = metadata;
    this.auditTime = new Date().toISOString();
    this.summary = new AuditSummary();

    // 计算汇总统计
    results.forEach(result => this.summary.addResult(result));

    // 按严重程度和规则编号排序
    this.sortResults();
  }

  /**
   * 排序审计结果
   * 优先级: error > warning > info, 同级别按ruleId排序
   */
  sortResults() {
    const severityOrder = { error: 0, warning: 1, info: 2 };
    this.results.sort((a, b) => {
      if (severityOrder[a.severity] !== severityOrder[b.severity]) {
        return severityOrder[a.severity] - severityOrder[b.severity];
      }
      return a.ruleId.localeCompare(b.ruleId);
    });
  }

  /**
   * 获取错误结果
   */
  getErrors() {
    return this.results.filter(r => r.severity === 'error');
  }

  /**
   * 获取警告结果
   */
  getWarnings() {
    return this.results.filter(r => r.severity === 'warning');
  }

  /**
   * 获取提示结果
   */
  getInfos() {
    return this.results.filter(r => r.severity === 'info');
  }

  /**
   * 获取失败结果
   */
  getFailed() {
    return this.results.filter(r => !r.passed);
  }

  /**
   * 是否通过所有检查
   */
  isPassed() {
    return this.summary.failed === 0;
  }

  /**
   * 转换为JSON
   */
  toJSON() {
    return {
      configPath: this.configPath,
      auditTime: this.auditTime,
      summary: this.summary.toJSON(),
      results: this.results.map(r => r.toJSON()),
      metadata: this.metadata
    };
  }
}

module.exports = { AuditReport, AuditSummary };
