/**
 * ERP配置审计引擎主类
 */

const fs = require('fs').promises;
const path = require('path');
const { AuditReport } = require('./models/AuditReport');
const { AuditResult, Severity } = require('./models/AuditResult');

/**
 * 配置审计引擎
 */
class ConfigAuditor {
  /**
   * @param {string} configPath - 配置文件路径
   * @param {Object} [options] - 配置选项
   */
  constructor(configPath, options = {}) {
    this.configPath = configPath;
    this.options = {
      parallel: true,  // 并行执行审计器
      timeout: 30000,  // 超时时间(ms)
      ...options
    };
    this.auditors = [];  // 注册的审计器列表
    this.config = null;  // 加载的配置对象
  }

  /**
   * 加载配置文件
   * @returns {Promise<Object>} 配置对象
   */
  async loadConfig() {
    try {
      const absolutePath = path.resolve(this.configPath);
      const content = await fs.readFile(absolutePath, 'utf-8');
      this.config = JSON.parse(content);
      
      console.log(`✅ 配置文件加载成功: ${absolutePath}`);
      return this.config;
    } catch (error) {
      throw new Error(`配置文件加载失败: ${error.message}`);
    }
  }

  /**
   * 注册审计器
   * @param {IAuditor} auditor - 审计器实例
   */
  registerAuditor(auditor) {
    if (!auditor.name || typeof auditor.audit !== 'function') {
      throw new Error('审计器必须实现 IAuditor 接口');
    }
    this.auditors.push(auditor);
    console.log(`✅ 注册审计器: ${auditor.name}`);
  }

  /**
   * 执行审计
   * @returns {Promise<AuditReport>} 审计报告
   */
  async runAudit() {
    if (!this.config) {
      await this.loadConfig();
    }

    if (this.auditors.length === 0) {
      throw new Error('未注册任何审计器');
    }

    console.log(`\n🔍 开始执行审计...`);
    console.log(`📋 已注册 ${this.auditors.length} 个审计器`);

    const startTime = Date.now();
    let results = [];

    try {
      if (this.options.parallel) {
        // 并行执行所有审计器
        const promises = this.auditors.map(auditor => 
          this.executeAuditor(auditor)
        );
        const resultsArray = await Promise.all(promises);
        results = resultsArray.flat();
      } else {
        // 串行执行所有审计器
        for (const auditor of this.auditors) {
          const auditorResults = await this.executeAuditor(auditor);
          results.push(...auditorResults);
        }
      }
    } catch (error) {
      throw new Error(`审计执行失败: ${error.message}`);
    }

    const endTime = Date.now();
    const duration = endTime - startTime;

    // 生成审计报告
    const report = new AuditReport({
      configPath: this.configPath,
      results,
      metadata: {
        duration: `${duration}ms`,
        auditorCount: this.auditors.length,
        parallel: this.options.parallel
      }
    });

    console.log(`\n✅ 审计完成，耗时: ${duration}ms`);
    console.log(`📊 汇总: 总计 ${report.summary.total} 项，通过 ${report.summary.passed} 项，失败 ${report.summary.failed} 项`);
    console.log(`   错误: ${report.summary.errors}，警告: ${report.summary.warnings}，提示: ${report.summary.infos}`);

    return report;
  }

  /**
   * 执行单个审计器
   * @param {IAuditor} auditor 
   * @returns {Promise<AuditResult[]>}
   */
  async executeAuditor(auditor) {
    const startTime = Date.now();
    console.log(`  ▶ 执行审计器: ${auditor.name}`);

    try {
      const results = await Promise.race([
        auditor.audit(this.config, this.options),
        this.createTimeoutPromise(auditor.name)
      ]);

      const duration = Date.now() - startTime;
      const failedCount = results.filter(r => !r.passed).length;
      console.log(`    ✓ ${auditor.name} 完成 (${duration}ms)，发现 ${failedCount} 个问题`);

      return results;
    } catch (error) {
      console.error(`    ✗ ${auditor.name} 执行失败: ${error.message}`);
      return [
        AuditResult.error(
          'AUDITOR-ERROR',
          `审计器执行失败: ${error.message}`,
          '',
          '请检查审计器实现或配置文件格式'
        )
      ];
    }
  }

  /**
   * 创建超时Promise
   * @param {string} auditorName 
   * @returns {Promise}
   */
  createTimeoutPromise(auditorName) {
    return new Promise((_, reject) => {
      setTimeout(() => {
        reject(new Error(`审计器 ${auditorName} 执行超时`));
      }, this.options.timeout);
    });
  }

  /**
   * 生成报告
   * @param {AuditReport} report - 审计报告
   * @param {string} format - 输出格式 (json/markdown/html)
   * @param {string} outputPath - 输出路径
   * @returns {Promise<string>} 报告内容
   */
  async generateReport(report, format = 'json', outputPath) {
    let content;

    switch (format.toLowerCase()) {
      case 'json':
        content = JSON.stringify(report.toJSON(), null, 2);
        break;
      case 'markdown':
        content = this.generateMarkdownReport(report);
        break;
      case 'html':
        content = this.generateHtmlReport(report);
        break;
      default:
        throw new Error(`不支持的报告格式: ${format}`);
    }

    if (outputPath) {
      const absolutePath = path.resolve(outputPath);
      await fs.writeFile(absolutePath, content, 'utf-8');
      console.log(`\n📄 报告已生成: ${absolutePath}`);
    }

    return content;
  }

  /**
   * 生成Markdown报告
   * @param {AuditReport} report 
   * @returns {string}
   */
  generateMarkdownReport(report) {
    const lines = [];
    
    // 标题
    lines.push('# ERP配置审计报告');
    lines.push('');
    
    // 概览
    lines.push('## 📊 审计概览');
    lines.push('');
    lines.push(`- **配置文件**: ${report.configPath}`);
    lines.push(`- **审计时间**: ${report.auditTime}`);
    lines.push(`- **总检查项**: ${report.summary.total}`);
    lines.push(`- **通过项**: ${report.summary.passed}`);
    lines.push(`- **失败项**: ${report.summary.failed}`);
    lines.push(`- **通过率**: ${report.summary.getPassRate()}%`);
    lines.push('');

    // 错误
    const errors = report.getErrors();
    if (errors.length > 0) {
      lines.push('## ❌ 严重错误 (阻塞部署)');
      lines.push('');
      lines.push('| 规则ID | 消息 | 路径 | 建议 |');
      lines.push('|--------|------|------|------|');
      errors.forEach(e => {
        lines.push(`| ${e.ruleId} | ${e.message} | ${e.path} | ${e.suggestion} |`);
      });
      lines.push('');
    }

    // 警告
    const warnings = report.getWarnings();
    if (warnings.length > 0) {
      lines.push('## ⚠️ 警告 (建议修复)');
      lines.push('');
      lines.push('| 规则ID | 消息 | 路径 | 建议 |');
      lines.push('|--------|------|------|------|');
      warnings.forEach(w => {
        lines.push(`| ${w.ruleId} | ${w.message} | ${w.path} | ${w.suggestion} |`);
      });
      lines.push('');
    }

    // 提示
    const infos = report.getInfos();
    if (infos.length > 0) {
      lines.push('## 💡 优化建议');
      lines.push('');
      infos.forEach(i => {
        lines.push(`- **${i.ruleId}**: ${i.message}`);
        if (i.suggestion) {
          lines.push(`  - 建议: ${i.suggestion}`);
        }
      });
      lines.push('');
    }

    return lines.join('\n');
  }

  /**
   * 生成HTML报告
   * @param {AuditReport} report 
   * @returns {string}
   */
  generateHtmlReport(report) {
    return `<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>ERP配置审计报告</title>
  <style>
    body { font-family: Arial, sans-serif; margin: 20px; background: #f5f5f5; }
    .container { max-width: 1200px; margin: 0 auto; background: white; padding: 20px; border-radius: 8px; }
    h1 { color: #333; border-bottom: 2px solid #4CAF50; padding-bottom: 10px; }
    h2 { color: #555; margin-top: 30px; }
    .summary { background: #e8f5e9; padding: 15px; border-radius: 4px; margin: 20px 0; }
    .summary-item { display: inline-block; margin-right: 30px; }
    .error { color: #f44336; }
    .warning { color: #ff9800; }
    .info { color: #2196f3; }
    table { width: 100%; border-collapse: collapse; margin: 20px 0; }
    th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }
    th { background: #f5f5f5; font-weight: bold; }
    tr:hover { background: #f9f9f9; }
    .badge { padding: 4px 8px; border-radius: 4px; font-size: 12px; font-weight: bold; }
    .badge-error { background: #ffebee; color: #f44336; }
    .badge-warning { background: #fff3e0; color: #ff9800; }
    .badge-info { background: #e3f2fd; color: #2196f3; }
  </style>
</head>
<body>
  <div class="container">
    <h1>ERP配置审计报告</h1>
    
    <div class="summary">
      <h3>📊 审计概览</h3>
      <div class="summary-item"><strong>配置文件:</strong> ${report.configPath}</div>
      <div class="summary-item"><strong>审计时间:</strong> ${report.auditTime}</div>
      <div class="summary-item"><strong>总检查项:</strong> ${report.summary.total}</div>
      <div class="summary-item"><strong>通过项:</strong> ${report.summary.passed}</div>
      <div class="summary-item"><strong>失败项:</strong> ${report.summary.failed}</div>
      <div class="summary-item"><strong>通过率:</strong> ${report.summary.getPassRate()}%</div>
    </div>

    ${this.generateHtmlTable('❌ 严重错误', report.getErrors(), 'error')}
    ${this.generateHtmlTable('⚠️ 警告', report.getWarnings(), 'warning')}
    ${this.generateHtmlTable('💡 提示', report.getInfos(), 'info')}
  </div>
</body>
</html>`;
  }

  /**
   * 生成HTML表格
   */
  generateHtmlTable(title, results, type) {
    if (results.length === 0) return '';
    
    const rows = results.map(r => `
      <tr>
        <td><span class="badge badge-${type}">${r.ruleId}</span></td>
        <td>${r.message}</td>
        <td><code>${r.path}</code></td>
        <td>${r.suggestion}</td>
      </tr>
    `).join('');

    return `
      <h2>${title}</h2>
      <table>
        <thead>
          <tr>
            <th>规则ID</th>
            <th>消息</th>
            <th>路径</th>
            <th>建议</th>
          </tr>
        </thead>
        <tbody>
          ${rows}
        </tbody>
      </table>
    `;
  }
}

module.exports = ConfigAuditor;
