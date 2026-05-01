-- ============================================
-- AI模块示例数据初始化脚本
-- 用途: 插入AI模块的默认配置和示例数据
-- 作者: Lingma
-- 日期: 2026-04-28
-- 版本: v2.3 (API Key动态配置版本)
-- 
-- 【重要】使用说明:
-- 1. ⚠️  执行前请先执行 ai_module_structure.sql 创建表结构
-- 2. 本脚本会删除旧数据后重新插入,支持重复执行
-- 3. 使用命令: mysql -u root -p your_database < ai_sample_data.sql
-- ============================================

SET NAMES utf8mb4 COLLATE utf8mb4_general_ci;
SET CHARACTER SET utf8mb4;

USE test;  -- ⚠️ 如果需要其他数据库,请修改此行
SELECT CONCAT('✅ 已切换到数据库: ', DATABASE()) AS database_info;

-- ============================================
-- 第一步: 清理旧数据(支持幂等性)
-- ============================================
SELECT '========================================' AS '';
SELECT '🗑️  清理旧示例数据...' AS cleanup_status;
SELECT '========================================' AS '';

-- 禁用外键检查
SET FOREIGN_KEY_CHECKS = 0;

-- 清理提示词数据(包括系统预设和用户自定义)
DELETE FROM ai_prompt;

-- 清理Skill模板数据
DELETE FROM ai_skill_template;

-- 清理Skill分类数据
DELETE FROM ai_skill_category;

-- 清理模型配置数据
DELETE FROM ai_model_config;

-- 清理系统配置数据
DELETE FROM ai_system_config;

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;

SELECT '✅ 旧数据清理完成' AS cleanup_result;

-- ============================================
-- 第二步: 插入示例数据
-- ============================================
SELECT '========================================' AS '';
SELECT '📝 开始插入示例数据...' AS insert_status;
SELECT '========================================' AS '';

-- --------------------------------------------
-- 2.1 插入AI模型配置(2个默认模型)
-- ⚠️  说明: 仅保留阿里云通义千问模型(国内可用)
-- --------------------------------------------
INSERT INTO ai_model_config (id, provider, model_code, model_name, api_key, base_url, temperature, max_tokens, top_p, is_default, is_enabled, sort_order, description, create_by) VALUES
(1001, 'dashscope', 'qwen-plus', '通义千问-Plus', NULL, 'https://dashscope.aliyuncs.com/compatible-mode/v1', 0.7, 2000, 0.9, 1, 1, 1, '平衡性能与成本,适合大多数场景', 'admin'),
(1002, 'dashscope', 'qwen-max', '通义千问-Max', NULL, 'https://dashscope.aliyuncs.com/compatible-mode/v1', 0.7, 2000, 0.9, 0, 1, 2, '最强推理能力,适合复杂任务', 'admin');

SELECT '✅ ai_model_config: 插入2条模型配置(通义千问-Plus/Max)' AS insert_result;

-- --------------------------------------------
-- 2.2 插入Skill分类(4个默认分类)
-- --------------------------------------------
INSERT INTO ai_skill_category (category_code, category_name, description, icon, color, sort_order, is_enabled, create_by) VALUES
('erp-config', 'ERP配置生成', '生成ERP页面配置的AI技能', 'system', '#409EFF', 1, 1, 'admin'),
('sql-helper', 'SQL助手', 'SQL生成和优化相关的技能', 'document', '#67C23A', 2, 1, 'admin'),
('data-analysis', '数据分析', '数据分析和洞察相关的技能', 'trend-charts', '#E6A23C', 3, 1, 'admin'),
('text-process', '文本处理', '文本处理和转换相关的技能', 'edit', '#F56C6C', 4, 1, 'admin');

SELECT '✅ ai_skill_category: 插入4个分类' AS insert_result;

-- --------------------------------------------
-- 2.3 插入Skill模板(3个示例Skill)
-- --------------------------------------------
INSERT INTO ai_skill_template (skill_code, skill_name, category_code, description, system_prompt, user_prompt_template, parameters_schema, default_model_code, temperature, max_tokens, timeout_seconds, is_enabled, is_public, sort_order, create_by) VALUES
('erp-config-generator', 'ERP配置生成器', 'erp-config', '根据表结构和字段信息,生成符合9字段拆分规范的ERP页面配置JSON', 
'你是一个专业的ERP页面配置专家。你的任务是根据用户提供的表结构和字段信息,生成符合若依框架规范的页面配置JSON。\n\n配置规范:\n1. columns: 表格列配置数组\n2. filters: 筛选条件配置数组\n3. buttons: 按钮配置数组\n4. formFields: 表单字段配置数组\n\n请严格按照JSON格式返回,不要包含其他说明文字。',
'请为以下表生成ERP页面配置:\n\n表名: {{tableName}}\n模块编码: {{moduleCode}}\n模块类型: {{moduleType}}\n字段列表: {{fields}}\n\n要求:\n1. 根据字段类型自动推断合适的组件(input/select/date等)\n2. 主键字段默认隐藏\n3. 时间字段使用date-picker组件\n4. 状态字段使用select组件并提供默认选项\n5. 金额字段右对齐并添加千分位格式化',
'{"parameters": [{"name": "tableName", "type": "string", "required": true, "label": "表名"}, {"name": "moduleCode", "type": "string", "required": true, "label": "模块编码"}, {"name": "moduleType", "type": "select", "required": true, "label": "模块类型", "options": ["list", "form", "detail"]}, {"name": "fields", "type": "textarea", "required": true, "label": "字段列表(JSON格式)", "placeholder": "[{\\"name\\":\\"id\\",\\"label\\":\\"ID\\",\\"type\\":\\"bigint\\"},...]"}]}',
'qwen-plus', 0.7, 3000, 60, 1, 1, 1, 'admin'),

('erp-sql-generator', '智能SQL生成', 'sql-helper', '通过自然语言描述自动生成高质量、安全的MySQL查询语句', 
'你是一位资深的MySQL数据库专家和ERP系统架构师。\n\n你的任务是根据用户的自然语言描述,生成高质量、安全、高效的MySQL查询SQL。\n\n## 约束条件:\n1. 只生成SELECT查询,禁止生成INSERT/UPDATE/DELETE/DROP等危险操作\n2. 必须使用参数化查询思想,避免SQL注入\n3. 优先使用索引友好的查询方式\n4. 对于大数据量表,必须考虑分页\n5. 避免SELECT *,明确指定需要的字段\n6. 合理使用JOIN,避免笛卡尔积\n\n## 输出格式:\n请以JSON格式返回:\n{\n  "sql": "生成的SQL语句",\n  "explanation": "SQL解释说明",\n  "tables": ["使用的表1", "使用的表2"],\n  "performance_tips": ["性能建议1", "性能建议2"]\n}',
'用户需求:{{description}}\n目标表:{{tableName}}\n模块编码:{{moduleCode}}\n是否需要分页:{{needPagination}}',
'{"parameters": [{"name": "description", "type": "textarea", "required": true, "label": "需求描述"}, {"name": "tableName", "type": "string", "required": false, "label": "目标表名"}, {"name": "moduleCode", "type": "string", "required": false, "label": "模块编码"}, {"name": "needPagination", "type": "boolean", "required": false, "label": "是否分页", "defaultValue": true}]}',
'qwen-plus', 0.7, 2000, 60, 1, 1, 2, 'admin'),

('erp-config-assistant', '通用配置助手', 'erp-config', 'AI辅助生成各种ERP页面或功能的配置JSON', 
'你是一位ERP低代码平台配置专家。\n\n你的任务是根据用户需求,生成符合规范的页面配置JSON。\n\n## 配置规范:\n1. 包含columns数组,定义表格列\n2. 包含filters数组,定义筛选条件\n3. 包含buttons数组,定义操作按钮\n4. 字段类型支持: string, number, date, select, boolean\n\n## 输出格式:\n请以JSON格式返回完整配置',
'需求:{{requirement}}\n模块类型:{{moduleType}}',
'{"parameters": [{"name": "requirement", "type": "textarea", "required": true, "label": "详细需求"}, {"name": "moduleType", "type": "select", "required": true, "label": "模块类型", "options": ["generic", "list", "form", "detail"]}]}',
'qwen-plus', 0.7, 2000, 60, 1, 1, 3, 'admin');

SELECT '✅ ai_skill_template: 插入3个示例Skill(配置生成器/SQL生成/配置助手)' AS insert_result;

-- --------------------------------------------
-- 2.4 插入系统预设提示词(2个默认提示词)
-- --------------------------------------------
INSERT INTO ai_prompt (user_id, prompt_name, prompt_code, category, prompt_template, description, variables, is_system, status, sort_order, create_by, remark) VALUES
(1, 'SQL生成-标准查询', 'sql_standard_query', 'sql_generator', 
'你是一个专业的SQL生成助手。请根据以下需求生成MySQL查询语句:\n\n需求描述: {{description}}\n表名: {{tableName}}\n是否需要分页: {{needPagination}}\n\n要求:\n1. 只返回JSON格式,包含sql、explanation、tables字段\n2. SQL必须符合MySQL语法规范\n3. 考虑性能优化,添加必要的索引建议\n4. 确保SQL安全性,防止SQL注入', 
'用于生成标准的SELECT查询语句', 
'[{"name":"description","label":"需求描述","required":true},{"name":"tableName","label":"表名","required":true},{"name":"needPagination","label":"是否需要分页","required":false,"defaultValue":true}]', 
1, 1, 1, 'admin', '系统预设模板'),

(1, '配置助手-页面生成', 'config_page_generator', 'config_assistant', 
'你是一个ERP页面配置专家。根据用户需求生成Vue3页面配置JSON。\n\n用户需求: {{requirement}}\n模块类型: {{moduleType}}\n\n请返回JSON格式配置,包含:\n- columns: 表格列配置\n- filters: 筛选条件配置\n- buttons: 按钮配置\n- formFields: 表单字段配置', 
'用于AI辅助生成ERP页面配置', 
'[{"name":"requirement","label":"用户需求","required":true},{"name":"moduleType","label":"模块类型","required":true}]', 
1, 1, 2, 'admin', '系统预设模板');

SELECT '✅ ai_prompt: 插入2个系统预设提示词' AS insert_result;

-- --------------------------------------------
-- 2.5 插入AI系统配置(4个默认配置) [新增 v2.3]
-- ⚠️  重要说明:
--    1. API Key 已预配置为示例值,生产环境请修改为实际的 API Key
--    2. is_encrypted=1 表示该字段需要加密存储
--    3. Base URL 可以直接配置,无需加密
-- --------------------------------------------
INSERT INTO ai_system_config (config_key, config_value, config_desc, is_encrypted, create_by) VALUES
('spring.ai.openai.api-key', '', 'OpenAI API Key(留空则使用application.yml配置)', 1, 'admin'),
('spring.ai.openai.base-url', 'https://api.openai.com/v1', 'OpenAI API Base URL', 0, 'admin'),
('spring.ai.dashscope.api-key', 'sk-d87f6a7395d7472fb5efd67510659adc', '阿里云通义千问 API Key(示例值,请替换为实际Key)', 1, 'admin'),
('spring.ai.dashscope.base-url', 'https://dashscope.aliyuncs.com/api/v1', '阿里云 API Base URL', 0, 'admin');

SELECT '✅ ai_system_config: 插入4条系统配置(DashScope API Key已预配置)' AS insert_result;

-- ============================================
-- 第三步: 验证结果
-- ============================================
SELECT '========================================' AS '';
SELECT '✅ AI模块示例数据插入完成!' AS message;
SELECT '========================================' AS '';

SELECT '===== 数据统计 =====' AS section;
SELECT 
  CONCAT('ai_model_config: ', (SELECT COUNT(*) FROM ai_model_config), ' 条') AS model_config_count,
  CONCAT('ai_skill_category: ', (SELECT COUNT(*) FROM ai_skill_category), ' 条') AS skill_category_count,
  CONCAT('ai_skill_template: ', (SELECT COUNT(*) FROM ai_skill_template), ' 条') AS skill_template_count,
  CONCAT('ai_prompt(system): ', (SELECT COUNT(*) FROM ai_prompt WHERE is_system = 1), ' 条') AS prompt_system_count,
  CONCAT('ai_system_config: ', (SELECT COUNT(*) FROM ai_system_config), ' 条') AS system_config_count;

SELECT '========================================' AS '';
SELECT '预期结果:' AS tip;
SELECT '  ✅ ai_model_config: 2条(通义千问-Plus/Max)' AS expected;
SELECT '  ✅ ai_skill_category: 4个分类(ERP配置/SQL助手/数据分析/文本处理)' AS expected;
SELECT '  ✅ ai_skill_template: 1个示例Skill(erp-config-generator)' AS expected;
SELECT '  ✅ ai_prompt: 2个系统预设(SQL生成 + 配置助手)' AS expected;
SELECT '  ✅ ai_system_config: 4条(OpenAI/DashScope的API Key和Base URL)' AS expected;
SELECT '========================================' AS '';

SELECT '========================================' AS '';
SELECT '✅ 示例数据初始化完成! 系统已就绪,可以开始使用AI模块' AS final_message;
SELECT NOW() AS end_time;
SELECT '========================================' AS '';

-- ============================================
-- 第四步: 配置API Key(可选)
-- ============================================
SELECT '========================================' AS '';
SELECT '📌 API Key配置说明:' AS tip;
SELECT '========================================' AS '';
SELECT '当前状态: DashScope API Key已预配置为示例值' AS status;
SELECT '' AS '';
SELECT '⚠️  重要提示:' AS warning;
SELECT '  1. 当前API Key为示例值,请在生产环境中替换为实际的API Key' AS note1;
SELECT '  2. 如需修改,请执行以下SQL:' AS note2;
SELECT '' AS '';
SELECT "UPDATE ai_system_config SET config_value = '你的实际API-Key', update_time = NOW() WHERE config_key = 'spring.ai.dashscope.api-key';" AS sql_example;
SELECT '' AS '';
SELECT '  3. 配置后需要重启应用才能生效' AS note3;
SELECT '========================================' AS '';
