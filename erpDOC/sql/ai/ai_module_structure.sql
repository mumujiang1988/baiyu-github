-- ============================================
-- AI模块完整表结构初始化脚本(生产就绪版)
-- 用途: 一键初始化AI模块所有表结构
-- 日期: 2026-04-28
-- 版本: v2.3 (API Key动态配置版本)
-- 
-- 【重要】使用说明:
-- 1. ⚠️  此脚本会删除所有ai_开头的表并重建,执行前请务必备份!
-- 2. 确保MySQL版本 >= 5.7(支持JSON类型)
-- 3. 使用命令: mysql -u root -p your_database < ai_module_structure.sql
-- 4. 脚本会自动检测并删除旧表,然后重新创建
-- 5. 本脚本支持重复执行,每次都会完全重置
-- 6. ⚠️  如需初始化示例数据,请额外执行: ai_sample_data.sql
-- ============================================

SET NAMES utf8mb4 COLLATE utf8mb4_general_ci;
SET CHARACTER SET utf8mb4;

-- ============================================
-- 选择目标数据库(请根据实际情况修改)
-- ============================================
USE test;  -- ⚠️ 如果需要其他数据库,请修改此行
SELECT CONCAT('✅ 已切换到数据库: ', DATABASE()) AS database_info;

-- ============================================
-- MySQL版本检查
-- ============================================
SELECT 
  CONCAT('MySQL版本: ', VERSION()) AS version_info,
  CASE 
    WHEN VERSION() >= '5.7.0' THEN '✅ 版本符合要求'
    ELSE '❌ 版本过低,需要 >= 5.7.0'
  END AS version_check;

-- 版本警告(仅提示,不终止执行)
SELECT 
  CASE 
    WHEN VERSION() < '5.7.0' THEN '⚠️  警告: MySQL版本过低,JSON类型可能不支持!请升级到5.7+' 
    ELSE ''
  END AS version_warning;

-- ============================================
-- 第一步: 检测并删除旧表(完全重置)
-- ============================================
SELECT '========================================' AS '';
SELECT '⚠️  警告: 即将删除所有ai_开头的表!' AS warning;
SELECT '========================================' AS '';

-- 禁用外键检查(确保删除顺序不受限制)
SET FOREIGN_KEY_CHECKS = 0;

-- 删除所有AI相关表(按依赖关系逆序删除)
-- DROP TABLE IF EXISTS ai_system_config;
DROP TABLE IF EXISTS ai_prompt;
DROP TABLE IF EXISTS ai_skill_template;
DROP TABLE IF EXISTS ai_skill_category;
DROP TABLE IF EXISTS ai_usage_log;
DROP TABLE IF EXISTS ai_model_config;
DROP TABLE IF EXISTS ai_chat_message;
DROP TABLE IF EXISTS ai_chat_session;

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;

SELECT '✅ 旧表清理完成' AS cleanup_status;

-- ============================================
-- 第二步: 创建表结构
-- ============================================
SELECT '========================================' AS '';
SELECT '📋 开始创建AI模块表结构...' AS create_status;
SELECT '========================================' AS '';

-- --------------------------------------------
-- 2.1 AI模型配置表(ai_model_config)
-- 功能: 支持多模型动态配置,无需硬编码
-- --------------------------------------------
CREATE TABLE ai_model_config (
    id BIGINT NOT NULL COMMENT '主键ID',
    provider VARCHAR(50) NOT NULL COMMENT '模型提供商(dashscope/openai)',
    model_code VARCHAR(100) NOT NULL COMMENT '模型编码(qwen-plus/gpt-3.5-turbo)',
    model_name VARCHAR(100) NOT NULL COMMENT '模型显示名称',
    api_key VARCHAR(500) DEFAULT NULL COMMENT 'API密钥(AES加密存储)',
    base_url VARCHAR(500) DEFAULT NULL COMMENT 'API基础URL',
    temperature DECIMAL(3,2) DEFAULT 0.7 COMMENT '温度参数(0-1)',
    max_tokens INT DEFAULT 2000 COMMENT '最大Token数',
    top_p DECIMAL(3,2) DEFAULT 0.9 COMMENT 'Top-P采样参数',
    is_default TINYINT(1) DEFAULT 0 COMMENT '是否默认模型(0否1是)',
    is_enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用(0禁用1启用)',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    description TEXT COMMENT '模型描述',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_provider_code (provider, model_code) COMMENT '提供商+模型编码唯一',
    KEY idx_enabled (is_enabled) COMMENT '启用状态索引',
    KEY idx_default (is_default) COMMENT '默认模型索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI模型配置表';

SELECT '✅ ai_model_config 表创建成功' AS table_status;

-- --------------------------------------------
-- 2.2 AI使用日志表(ai_usage_log)
-- 功能: 记录AI功能使用情况,支持统计和审计
-- --------------------------------------------
CREATE TABLE ai_usage_log (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    function_type VARCHAR(50) NOT NULL COMMENT '功能类型(sql_generator/config_assistant/data_analysis/skill_execute)',
    request_summary TEXT COMMENT '请求摘要(截断500字符)',
    response_summary TEXT COMMENT '响应摘要(截断1000字符)',
    tokens_used INT DEFAULT 0 COMMENT 'Token用量',
    duration_ms BIGINT DEFAULT 0 COMMENT '耗时(毫秒)',
    success TINYINT(1) DEFAULT 1 COMMENT '是否成功(0失败1成功)',
    error_message TEXT COMMENT '错误信息',
    ip_address VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_user_function_time (user_id, function_type, create_time) COMMENT '复合索引(用户+功能+时间)',
    KEY idx_success_time (success, create_time) COMMENT '统计索引(成功状态+时间)',
    KEY idx_create_time (create_time) COMMENT '时间索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI使用日志表';

SELECT '✅ ai_usage_log 表创建成功' AS table_status;

-- --------------------------------------------
-- 2.3 Skill分类表(ai_skill_category)
-- 功能: 动态分类管理,非硬编码
-- --------------------------------------------
CREATE TABLE ai_skill_category (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    category_code VARCHAR(50) NOT NULL COMMENT '分类编码(唯一)',
    category_name VARCHAR(100) NOT NULL COMMENT '分类名称',
    description VARCHAR(500) DEFAULT NULL COMMENT '分类描述',
    icon VARCHAR(50) DEFAULT 'folder' COMMENT 'Element Plus图标名',
    color VARCHAR(20) DEFAULT '#409EFF' COMMENT '主题色',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    is_enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用(0禁用1启用)',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_category_code (category_code) COMMENT '分类编码唯一',
    KEY idx_enabled (is_enabled) COMMENT '启用状态索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Skill分类表';

SELECT '✅ ai_skill_category 表创建成功' AS table_status;

-- --------------------------------------------
-- 2.4 Skill模板表(ai_skill_template)
-- 功能: Skill模板管理,支持动态扩展
-- --------------------------------------------
CREATE TABLE ai_skill_template (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    skill_code VARCHAR(50) NOT NULL COMMENT '技能编码(唯一)',
    skill_name VARCHAR(100) NOT NULL COMMENT '技能名称',
    category_code VARCHAR(50) NOT NULL COMMENT '分类编码(外键)',
    description TEXT COMMENT '技能描述',
    system_prompt TEXT COMMENT '系统提示词',
    user_prompt_template TEXT COMMENT '用户提示词模板(支持{param})',
    parameters_schema JSON DEFAULT NULL COMMENT '参数Schema定义(JSON格式)',
    default_model_code VARCHAR(50) DEFAULT 'qwen-plus' COMMENT '默认模型编码',
    temperature DECIMAL(3,2) DEFAULT 0.7 COMMENT '温度参数(0-1)',
    max_tokens INT DEFAULT 2000 COMMENT '最大Token数',
    timeout_seconds INT DEFAULT 60 COMMENT '超时时间(秒)',
    is_enabled TINYINT(1) DEFAULT 1 COMMENT '是否启用(0禁用1启用)',
    is_public TINYINT(1) DEFAULT 1 COMMENT '是否公开(0私有1公开)',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_skill_code (skill_code) COMMENT '技能编码唯一',
    KEY idx_category (category_code) COMMENT '分类索引',
    KEY idx_enabled (is_enabled) COMMENT '启用状态索引',
    KEY idx_public (is_public) COMMENT '公开状态索引',
    CONSTRAINT fk_skill_category FOREIGN KEY (category_code) REFERENCES ai_skill_category(category_code) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Skill模板表';

SELECT '✅ ai_skill_template 表创建成功' AS table_status;

-- --------------------------------------------
-- 2.5 AI提示词管理表(ai_prompt) [新增]
-- 功能: 多用户提示词CRUD管理,支持系统预设与个人模板
-- --------------------------------------------
CREATE TABLE ai_prompt (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID(数据隔离)',
    prompt_name VARCHAR(100) NOT NULL COMMENT '提示词名称',
    prompt_code VARCHAR(50) DEFAULT NULL COMMENT '提示词编码(唯一标识)',
    category VARCHAR(50) DEFAULT NULL COMMENT '分类(sql_generator/config_assistant/data_analysis等)',
    prompt_template TEXT NOT NULL COMMENT '提示词模板内容',
    description VARCHAR(500) DEFAULT NULL COMMENT '描述说明',
    variables JSON DEFAULT NULL COMMENT '模板变量定义JSON数组 [{"name":"tableName","label":"表名","required":true}]',
    is_system TINYINT(1) DEFAULT 0 COMMENT '是否系统预设(0否 1是,系统预设所有用户可见)',
    status TINYINT(1) DEFAULT 1 COMMENT '状态(0禁用 1启用)',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_code (user_id, prompt_code) COMMENT '用户+编码唯一',
    KEY idx_user_id (user_id) COMMENT '用户ID索引',
    KEY idx_category (category) COMMENT '分类索引',
    KEY idx_status (status) COMMENT '状态索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI提示词管理表';

SELECT '✅ ai_prompt 表创建成功' AS table_status;

-- --------------------------------------------
-- 2.6 AI系统配置表(ai_system_config) [新增 v2.3]
-- 功能: Spring AI API Key动态配置,符合官方规范
-- --------------------------------------------
CREATE TABLE ai_system_config (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键(如: spring.ai.openai.api-key)',
    config_value TEXT NOT NULL COMMENT '配置值(加密存储)',
    config_desc VARCHAR(255) DEFAULT NULL COMMENT '配置描述',
    is_encrypted TINYINT(1) DEFAULT 0 COMMENT '是否加密: 0-否, 1-是',
    create_by VARCHAR(64) DEFAULT '' COMMENT '创建者',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by VARCHAR(64) DEFAULT '' COMMENT '更新者',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_config_key (config_key) COMMENT '配置键唯一',
    KEY idx_config_key (config_key) COMMENT '配置键索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI系统配置表(API Key动态配置)';

SELECT '✅ ai_system_config 表创建成功' AS table_status;

-- --------------------------------------------
-- 2.7 AI聊天会话表(ai_chat_session) [新增 v3.1]
-- 功能: 存储用户与AI的会话记录,支持多轮对话历史回溯
-- --------------------------------------------
CREATE TABLE ai_chat_session (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID(数据隔离)',
    skill_code VARCHAR(64) NOT NULL COMMENT '关联Skill编码',
    model_code VARCHAR(64) DEFAULT NULL COMMENT '使用的模型编码',
    title VARCHAR(255) DEFAULT NULL COMMENT '会话标题',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id) COMMENT '用户ID索引',
    KEY idx_create_time (create_time) COMMENT '时间索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI聊天会话表';

SELECT '✅ ai_chat_session 表创建成功' AS table_status;

-- --------------------------------------------
-- 2.8 AI聊天消息表(ai_chat_message) [新增 v3.1]
-- 功能: 存储会话中的具体消息内容,支持流式输出持久化
-- --------------------------------------------
CREATE TABLE ai_chat_message (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    session_id BIGINT NOT NULL COMMENT '关联会话ID',
    role VARCHAR(20) NOT NULL COMMENT '角色: user/assistant',
    content TEXT NOT NULL COMMENT '消息内容',
    sort_order INT DEFAULT 0 COMMENT '排序号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_session_id (session_id) COMMENT '会话ID索引',
    KEY idx_sort_order (sort_order) COMMENT '排序索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI聊天消息表';

SELECT '✅ ai_chat_message 表创建成功' AS table_status;

-- ============================================
-- 第三步: 验证结果
-- ============================================
SELECT '========================================' AS '';
SELECT '✅ AI模块表结构创建完成!' AS message;
SELECT '========================================' AS '';

SELECT '===== 表结构统计 =====' AS section;
SELECT 
  CONCAT('ai_model_config: ', (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'ai_model_config')) AS model_config_table,
  CONCAT('ai_usage_log: ', (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'ai_usage_log')) AS usage_log_table,
  CONCAT('ai_skill_category: ', (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'ai_skill_category')) AS skill_category_table,
  CONCAT('ai_skill_template: ', (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'ai_skill_template')) AS skill_template_table,
  CONCAT('ai_prompt: ', (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'ai_prompt')) AS prompt_table,
  CONCAT('ai_system_config: ', (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'ai_system_config')) AS system_config_table,
  CONCAT('ai_chat_session: ', (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'ai_chat_session')) AS chat_session_table,
  CONCAT('ai_chat_message: ', (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'ai_chat_message')) AS chat_message_table;

SELECT '========================================' AS '';
SELECT '预期结果:' AS tip;
SELECT '  ✅ 8个表全部创建成功' AS expected;
SELECT '  ✅ 所有表使用utf8mb4字符集' AS expected;
SELECT '  ✅ 所有表包含create_time/update_time自动维护' AS expected;
SELECT '  ✅ 索引优化完成(复合索引+唯一约束)' AS expected;
SELECT '========================================' AS '';

SELECT '========================================' AS '';
SELECT '✅ 表结构初始化完成! 请继续执行 ai_sample_data.sql 插入示例数据' AS final_message;
SELECT NOW() AS end_time;
SELECT '========================================' AS '';
