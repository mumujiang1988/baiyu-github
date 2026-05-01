# AI模块SQL脚本合并说明

**日期**: 2026-04-28  
**版本**: v2.3 (API Key动态配置版本)  

---

## 📋 合并概述

已将`ai-system-config.sql`的内容合并到主脚本中,简化初始化流程。

---

## ✅ 合并内容

### 1. ai_module_structure.sql (表结构脚本)

**新增内容**:
- ✅ 在删除表阶段添加: `DROP TABLE IF EXISTS ai_system_config;`
- ✅ 新增表结构定义(第2.6节): `ai_system_config`表
- ✅ 更新验证统计: 从5个表改为6个表

**表结构**:
```sql
CREATE TABLE ai_system_config (
    id BIGINT NOT NULL AUTO_INCREMENT,
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value TEXT NOT NULL,
    config_desc VARCHAR(255),
    is_encrypted TINYINT(1) DEFAULT 0,
    create_by VARCHAR(64),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_by VARCHAR(64),
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_config_key (config_key),
    KEY idx_config_key (config_key)
);
```

---

### 2. ai_sample_data.sql (示例数据脚本)

**新增内容**:
- ✅ 在清理阶段添加: `DELETE FROM ai_system_config;`
- ✅ 新增数据插入(第2.5节): 4条默认配置
- ✅ 更新验证统计: 增加ai_system_config计数

**默认数据**:
```sql
INSERT INTO ai_system_config VALUES
('spring.ai.openai.api-key', '', 'OpenAI API Key', 1, 'admin'),
('spring.ai.openai.base-url', 'https://api.openai.com/v1', '...', 0, 'admin'),
('spring.ai.dashscope.api-key', '', '阿里云API Key', 1, 'admin'),
('spring.ai.dashscope.base-url', 'https://dashscope.aliyuncs.com/api/v1', '...', 0, 'admin');
```

---

## 🗑️ 已删除文件

- ❌ `ai-system-config.sql` (已合并,不再需要)

---

## 📊 当前脚本清单

| 序号 | 脚本文件 | 说明 | 表数量 |
|------|---------|------|--------|
| 1 | `ai_module_structure.sql` | 表结构初始化(v2.3) | **6个表** |
| 2 | `ai_menu_init.sql` | 菜单权限初始化(v2.3) | 6菜单+26按钮 |
| 3 | `ai_sample_data.sql` | 示例数据初始化(v2.3) | 包含系统配置 |

---

## 🚀 执行顺序

```bash
# 1. 创建表结构(6个表)
mysql -u root -p test < ai_module_structure.sql

# 2. 初始化菜单(6个二级菜单+26个按钮)
mysql -u root -p test < ai_menu_init.sql

# 3. 插入示例数据(包含4条系统配置)
mysql -u root -p test < ai_sample_data.sql
```

---

## ✅ 验证检查

执行完成后,应看到:

### 表结构验证
```sql
SHOW TABLES LIKE 'ai_%';
-- 预期: 6个表
-- ai_model_config
-- ai_usage_log
-- ai_skill_category
-- ai_skill_template
-- ai_prompt
-- ai_system_config ← 新增
```

### 数据验证
```sql
SELECT COUNT(*) FROM ai_system_config;
-- 预期: 4条记录

SELECT config_key, config_desc FROM ai_system_config;
-- 预期:
-- spring.ai.openai.api-key       | OpenAI API Key
-- spring.ai.openai.base-url      | OpenAI API Base URL
-- spring.ai.dashscope.api-key    | 阿里云通义千问 API Key
-- spring.ai.dashscope.base-url   | 阿里云 API Base URL
```

---

## 🎯 优势

### 合并前
- ❌ 需要执行4个SQL脚本
- ❌ 文件分散,管理不便
- ❌ 容易遗漏ai-system-config.sql

### 合并后
- ✅ 只需执行3个SQL脚本
- ✅ 结构清晰,易于维护
- ✅ 不会遗漏配置表

---

## 📝 注意事项

1. **幂等性**: 所有脚本支持重复执行
2. **向后兼容**: 保留ai_model_config.api_key字段(标记废弃)
3. **配置优先级**: ai_system_config > application.yml
4. **加密存储**: API Key自动AES加密

---

## 🔗 相关文档

- [API Key配置化方案审计报告](../AI模块/API_Key配置化方案审计报告.md)
- [AI系统配置管理-数据库动态配置方案](../AI模块/AI系统配置管理-数据库动态配置方案.md)
- [SQL脚本README](README.md)

---

**合并完成!** 🎉
