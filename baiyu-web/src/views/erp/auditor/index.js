/**
 * ERP配置审计系统 - 主入口
 */

const ConfigAuditor = require('./core/ConfigAuditor');
const StructureAuditor = require('./auditors/StructureAuditor');
const NamingAuditor = require('./auditors/NamingAuditor');
const ApiAuditor = require('./auditors/ApiAuditor');
const RenderingAuditor = require('./auditors/RenderingAuditor');
const DataFlowAuditor = require('./auditors/DataFlowAuditor');
const CrudAuditor = require('./auditors/CrudAuditor');
const FeasibilityAuditor = require('./auditors/FeasibilityAuditor');

/**
 * 创建审计引擎实例并注册所有审计器
 * @param {string} configPath - 配置文件路径
 * @param {Object} options - 配置选项
 * @returns {ConfigAuditor}
 */
function createAuditor(configPath, options = {}) {
  const auditor = new ConfigAuditor(configPath, options);

  // 注册所有审计器
  auditor.registerAuditor(new StructureAuditor());
  auditor.registerAuditor(new NamingAuditor());
  auditor.registerAuditor(new ApiAuditor());
  auditor.registerAuditor(new RenderingAuditor());
  auditor.registerAuditor(new DataFlowAuditor());
  auditor.registerAuditor(new CrudAuditor());
  auditor.registerAuditor(new FeasibilityAuditor());

  return auditor;
}

/**
 * 执行审计并生成报告
 * @param {string} configPath - 配置文件路径
 * @param {Object} options - 配置选项
 * @returns {Promise<AuditReport>}
 */
async function audit(configPath, options = {}) {
  const auditor = createAuditor(configPath, options);
  const report = await auditor.runAudit();
  return report;
}

/**
 * 执行审计并生成报告文件
 * @param {string} configPath - 配置文件路径
 * @param {string} outputPath - 输出路径
 * @param {string} format - 输出格式 (json/markdown/html)
 * @param {Object} options - 配置选项
 * @returns {Promise<string>}
 */
async function auditAndReport(configPath, outputPath, format = 'json', options = {}) {
  const auditor = createAuditor(configPath, options);
  const report = await auditor.runAudit();
  const content = await auditor.generateReport(report, format, outputPath);
  return content;
}

// 导出
module.exports = {
  ConfigAuditor,
  StructureAuditor,
  NamingAuditor,
  ApiAuditor,
  RenderingAuditor,
  DataFlowAuditor,
  CrudAuditor,
  FeasibilityAuditor,
  createAuditor,
  audit,
  auditAndReport
};
