#!/usr/bin/env node

/**
 * ERP配置审计系统 - 命令行工具
 */

const { program } = require('commander');
const path = require('path');
const { auditAndReport } = require('./index');

// 版本信息
const VERSION = '1.0.0';

// 配置命令行参数
program
  .name('erp-auditor')
  .version(VERSION)
  .description('ERP低代码配置文件审计工具')
  .argument('<config-file>', '配置文件路径')
  .option('-f, --format <format>', '输出格式 (json|markdown|html)', 'json')
  .option('-o, --output <path>', '输出文件路径')
  .option('--no-parallel', '禁用并行执行')
  .option('--timeout <ms>', '审计器超时时间(毫秒)', '30000')
  .action(async (configFile, options) => {
    try {
      console.log('🔍 ERP配置审计系统 v' + VERSION);
      console.log('━'.repeat(50));

      // 解析配置文件路径
      const configPath = path.resolve(configFile);
      console.log(`📁 配置文件: ${configPath}`);

      // 解析输出路径
      let outputPath = options.output;
      if (outputPath) {
        outputPath = path.resolve(outputPath);
      } else {
        // 默认输出路径
        const ext = options.format === 'json' ? 'json' : 
                    options.format === 'markdown' ? 'md' : 'html';
        outputPath = configPath.replace(/\.[^.]+$/, `.audit-report.${ext}`);
      }

      console.log(`📄 输出格式: ${options.format}`);
      console.log(`📝 输出路径: ${outputPath}`);
      console.log('━'.repeat(50));

      // 执行审计
      const auditOptions = {
        parallel: options.parallel,
        timeout: parseInt(options.timeout)
      };

      await auditAndReport(configPath, outputPath, options.format, auditOptions);

      console.log('\n✅ 审计完成！');
      console.log(`📊 报告已生成: ${outputPath}`);

      // 退出码
      process.exit(0);
    } catch (error) {
      console.error('\n❌ 审计失败:', error.message);
      console.error(error.stack);
      process.exit(1);
    }
  });

// 添加批量审计命令
program
  .command('batch <directory>')
  .description('批量审计目录下的所有配置文件')
  .option('-f, --format <format>', '输出格式 (json|markdown|html)', 'json')
  .option('-o, --output <path>', '输出目录路径')
  .option('-p, --pattern <pattern>', '文件匹配模式', '**/*.config.json')
  .action(async (directory, options) => {
    try {
      console.log('🔍 ERP配置审计系统 - 批量审计模式');
      console.log('━'.repeat(50));

      const fs = require('fs').promises;
      const glob = require('glob');
      
      // 查找配置文件
      const configFiles = await new Promise((resolve, reject) => {
        glob(options.pattern, { cwd: directory }, (err, files) => {
          if (err) reject(err);
          else resolve(files);
        });
      });

      if (configFiles.length === 0) {
        console.log('⚠️  未找到匹配的配置文件');
        process.exit(0);
      }

      console.log(`📁 找到 ${configFiles.length} 个配置文件`);
      console.log('━'.repeat(50));

      // 批量审计
      for (const file of configFiles) {
        const configPath = path.join(directory, file);
        console.log(`\n▶ 审计: ${file}`);

        try {
          let outputPath;
          if (options.output) {
            const ext = options.format === 'json' ? 'json' : 
                        options.format === 'markdown' ? 'md' : 'html';
            outputPath = path.join(options.output, file.replace(/\.[^.]+$/, `.audit-report.${ext}`));
          } else {
            const ext = options.format === 'json' ? 'json' : 
                        options.format === 'markdown' ? 'md' : 'html';
            outputPath = configPath.replace(/\.[^.]+$/, `.audit-report.${ext}`);
          }

          await auditAndReport(configPath, outputPath, options.format);
          console.log(`  ✅ 完成: ${outputPath}`);
        } catch (error) {
          console.error(`  ❌ 失败: ${error.message}`);
        }
      }

      console.log('\n✅ 批量审计完成！');
      process.exit(0);
    } catch (error) {
      console.error('\n❌ 批量审计失败:', error.message);
      process.exit(1);
    }
  });

// 添加规则列表命令
program
  .command('rules')
  .description('显示所有审计规则')
  .action(() => {
    console.log('📋 审计规则列表');
    console.log('━'.repeat(50));

    const auditors = [
      require('./auditors/StructureAuditor'),
      require('./auditors/NamingAuditor'),
      require('./auditors/ApiAuditor'),
      require('./auditors/RenderingAuditor'),
      require('./auditors/DataFlowAuditor'),
      require('./auditors/CrudAuditor'),
      require('./auditors/FeasibilityAuditor')
    ];

    auditors.forEach(AuditorClass => {
      const auditor = new AuditorClass();
      const rules = auditor.getRules();

      console.log(`\n【${auditor.name}】${auditor.description}`);
      console.log('─'.repeat(40));

      rules.forEach(rule => {
        const severityIcon = {
          error: '❌',
          warning: '⚠️',
          info: '💡'
        }[rule.severity];

        console.log(`  ${severityIcon} ${rule.id}: ${rule.name}`);
      });
    });

    process.exit(0);
  });

// 解析命令行参数
program.parse(process.argv);
