/**
 * 审计系统测试脚本
 */

const path = require('path');
const { audit, auditAndReport } = require('./index');

async function test() {
  console.log('🧪 开始测试审计系统...\n');

  try {
    // 测试配置文件路径
    const configPath = path.join(
      __dirname, 
      '../pageTemplate/configs/business.config.template.json'
    );

    console.log('📁 测试配置文件:', configPath);
    console.log('━'.repeat(50));

    // 执行审计
    const report = await audit(configPath);

    // 输出结果
    console.log('\n📊 审计结果汇总:');
    console.log('━'.repeat(50));
    console.log(`总检查项: ${report.summary.total}`);
    console.log(`通过项: ${report.summary.passed}`);
    console.log(`失败项: ${report.summary.failed}`);
    console.log(`错误数: ${report.summary.errors}`);
    console.log(`警告数: ${report.summary.warnings}`);
    console.log(`提示数: ${report.summary.infos}`);
    console.log(`通过率: ${report.summary.getPassRate()}%`);

    // 输出错误详情
    const errors = report.getErrors();
    if (errors.length > 0) {
      console.log('\n❌ 错误详情:');
      console.log('━'.repeat(50));
      errors.slice(0, 5).forEach((error, index) => {
        console.log(`\n${index + 1}. [${error.ruleId}] ${error.message}`);
        console.log(`   路径: ${error.path}`);
        console.log(`   建议: ${error.suggestion}`);
      });
      if (errors.length > 5) {
        console.log(`\n... 还有 ${errors.length - 5} 个错误`);
      }
    }

    // 输出警告详情
    const warnings = report.getWarnings();
    if (warnings.length > 0) {
      console.log('\n⚠️ 警告详情:');
      console.log('━'.repeat(50));
      warnings.slice(0, 5).forEach((warning, index) => {
        console.log(`\n${index + 1}. [${warning.ruleId}] ${warning.message}`);
        console.log(`   路径: ${warning.path}`);
        console.log(`   建议: ${warning.suggestion}`);
      });
      if (warnings.length > 5) {
        console.log(`\n... 还有 ${warnings.length - 5} 个警告`);
      }
    }

    // 生成报告文件
    console.log('\n📝 生成报告文件...');
    console.log('━'.repeat(50));

    const outputDir = path.join(__dirname, 'test-output');
    const fs = require('fs').promises;
    await fs.mkdir(outputDir, { recursive: true });

    // JSON报告
    await auditAndReport(
      configPath, 
      path.join(outputDir, 'audit-report.json'), 
      'json'
    );
    console.log('✅ JSON报告已生成');

    // Markdown报告
    await auditAndReport(
      configPath, 
      path.join(outputDir, 'audit-report.md'), 
      'markdown'
    );
    console.log('✅ Markdown报告已生成');

    // HTML报告
    await auditAndReport(
      configPath, 
      path.join(outputDir, 'audit-report.html'), 
      'html'
    );
    console.log('✅ HTML报告已生成');

    console.log('\n✅ 测试完成！');
    console.log(`📁 报告目录: ${outputDir}`);

    // 返回退出码
    process.exit(report.isPassed() ? 0 : 1);
  } catch (error) {
    console.error('\n❌ 测试失败:', error.message);
    console.error(error.stack);
    process.exit(1);
  }
}

// 执行测试
test();
