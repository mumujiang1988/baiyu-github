# AI模块SQL初始化脚本说明

**版本**: v2.2  
**更新日期**: 2026-04-28  
**维护状态**: ✅ 活跃维护

---

## 📋 脚本清单

AI模块包含**3个SQL脚本**,按顺序执行:

| 序号 | 脚本文件 | 说明 | 特性 |
|------|---------|------|------|
| 1 | `ai_module_structure.sql` | 表结构初始化脚本 | ✅ 完全重置(删除旧表重建)<br>✅ **6个表**(新增ai_system_config)<br>✅ utf8mb4字符集<br>✅ 复合索引优化<br>✅ JSON字段支持<br>✅ 幂等性设计 |
| 2 | `ai_menu_init.sql` | 菜单权限初始化脚本 | ✅ 模块化存储过程设计<br>✅ 雪花ID生成算法<br>✅ 事务控制+错误回滚<br>✅ 自动清理旧菜单<br>✅ 级联删除子菜单和按钮<br>✅ 自动分配admin角色权限<br>✅ **6个二级菜单 + 26个按钮权限** |
| 3 | `ai_sample_data.sql` | 示例数据初始化脚本 | ✅ 幂等性设计(删除旧数据重插)<br>✅ 4个默认AI模型配置<br>✅ 4个Skill分类<br>✅ 1个示例Skill模板<br>✅ 2个系统预设提示词<br>✅ **4条系统配置(API Key动态配置)** |

---

## 🚀 快速开始

### 方式一：命令行执行(推荐)

```bash
# 切换到SQL目录
cd d:/baiyu-ali/erpDOC/sql/ai

# 1. 创建表结构
mysql -u root -p test < ai_module_structure.sql

# 2. 初始化菜单
mysql -u root -p test < ai_menu_init.sql

# 3. 插入示例数据
mysql -u root -p test < ai_sample_data.sql
```

### 方式二：MySQL客户端执行

```sql
-- 切换到目标数据库
USE test;

-- 依次执行3个脚本
SOURCE d:/baiyu-ali/erpDOC/sql/ai/ai_module_structure.sql;
SOURCE d:/baiyu-ali/erpDOC/sql/ai/ai_menu_init.sql;
SOURCE d:/baiyu-ali/erpDOC/sql/ai/ai_sample_data.sql;
```

---

## ⚠️ 重要提示

### 执行前检查

1. **数据库备份**: 如果已有AI模块数据,请先备份!
   ```bash
   mysqldump -u root -p test > ai_backup_20260428.sql
   ```

2. **MySQL版本**: 确保 MySQL >= 5.7 (支持JSON类型)
   ```sql
   SELECT VERSION();  -- 应返回 5.7.0 或更高版本
   ```

3. **目标数据库**: 确认脚本中的`USE test;`与实际数据库名一致

### 执行后验证

```sql
-- 1. 检查表是否创建成功
SHOW TABLES LIKE 'ai_%';
-- 预期结果: 6个表(ai_model_config, ai_usage_log, ai_skill_category, ai_skill_template, ai_prompt, ai_system_config)

-- 2. 检查菜单是否创建成功
SELECT menu_name, menu_type FROM sys_menu WHERE menu_id >= (SELECT MIN(menu_id) FROM sys_menu WHERE path = 'ai');
-- 预期结果: 1个一级菜单 + 6个二级菜单 + 26个按钮

-- 3. 检查示例数据
SELECT COUNT(*) AS model_count FROM ai_model_config;  -- 预期: 4
SELECT COUNT(*) AS category_count FROM ai_skill_category;  -- 预期: 4
SELECT COUNT(*) AS skill_count FROM ai_skill_template;  -- 预期: 1
SELECT COUNT(*) AS prompt_count FROM ai_prompt WHERE is_system = 1;  -- 预期: 2
```

---

## 📊 菜单结构

执行`ai_menu_init.sql`后将创建以下菜单:

```
ERP智能工具 (一级目录, order_num=7, icon=tool)
├── AI工具中心 (二级菜单, component=erp/ai/index)
│   ├── 智能SQL生成 (按钮: ai:tools:sqlGenerate)
│   ├── 配置助手 (按钮: ai:tools:configAssistant)
│   ├── 数据分析 (按钮: ai:tools:dataAnalysis)
│   ├── 刷新 (按钮: ai:tools:refresh)
│   └── 导出 (按钮: ai:tools:export)
├── 使用日志 (二级菜单, component=erp/ai/AiUsageLog)
│   ├── 查询 (按钮: ai:log:query)
│   ├── 导出 (按钮: ai:log:export)
│   ├── 删除 (按钮: ai:log:remove)
│   └── 清空 (按钮: ai:log:clear)
├── 模型配置 (二级菜单, component=erp/ai/ModelConfig)
│   ├── 查询 (按钮: ai:config:query)
│   ├── 新增 (按钮: ai:config:add)
│   ├── 修改 (按钮: ai:config:edit)
│   ├── 删除 (按钮: ai:config:remove)
│   └── 测试连接 (按钮: ai:config:test)
├── Skill市场 (二级菜单, component=erp/ai/SkillMarket)
│   ├── 查询 (按钮: ai:skill:query)
│   ├── 执行 (按钮: ai:skill:execute)
│   ├── 分类管理 (按钮: ai:skill:categoryManage)
│   └── 模板管理 (按钮: ai:skill:templateManage)
├── 提示词管理 (二级菜单, component=ai/prompt/index) [新增 v2.2]
│   ├── 查询 (按钮: ai:prompt:query)
│   ├── 新增 (按钮: ai:prompt:add)
│   ├── 修改 (按钮: ai:prompt:edit)
│   ├── 删除 (按钮: ai:prompt:remove)
│   └── 复制 (按钮: ai:prompt:copy)
└── AI系统配置 (二级菜单, component=ai/systemConfig/index) [新增 v2.3]
    ├── 查询 (按钮: ai:system-config:query)
    ├── 修改 (按钮: ai:system-config:edit)
    └── 测试 (按钮: ai:system-config:test)
```

**统计信息**:
- 一级菜单: 1个
- 二级菜单: 6个
- 按钮权限: 26个 (5+4+5+4+5+3)

---

## 🔧 常见问题

### Q1: 执行脚本报错 "Table 'sys_menu' doesn't exist"
**A**: 确保已在若依系统中执行过基础建表脚本,`sys_menu`表必须存在。

### Q2: 菜单创建成功但前端不显示
**A**: 
1. 检查当前登录用户是否有该菜单的权限(查看`sys_role_menu`表)
2. 清除浏览器缓存并重新登录
3. 确认前端路由配置正确

### Q3: 如何为其他角色分配AI菜单权限?
**A**: 
1. 登录系统管理后台
2. 进入 `系统管理 -> 角色管理`
3. 选择目标角色,点击 `修改 -> 菜单权限`
4. 勾选 `ERP智能工具` 及其子菜单
5. 保存后,该角色的用户重新登录即可看到菜单

### Q4: 想调整菜单显示顺序怎么办?
**A**: 修改`ai_menu_init.sql`中父菜单的`order_num`字段值,数值越小越靠前。例如将AI工具从第7位调到第5位:
```sql
INSERT INTO sys_menu (..., order_num, ...)
VALUES (..., 5, ...);  -- 原来是7
```

### Q5: 如何卸载AI菜单?
**A**: 重新执行`ai_menu_init.sql`,脚本会自动清理旧数据。或者在系统管理后台手动删除菜单。

### Q6: 示例数据可以重复执行吗?
**A**: ✅ 可以!`ai_sample_data.sql`采用幂等性设计,会先删除旧数据再重新插入,支持重复执行。

### Q7: 如何自定义AI模型配置?
**A**: 
1. 登录系统,进入 `ERP智能工具 -> 模型配置`
2. 点击 `新增` 按钮
3. 填写模型信息(提供商、模型编码、API Key等)
4. 点击 `测试连接` 验证配置
5. 设置为默认模型(可选)

---

## 📝 技术细节

### 雪花ID生成算法
```
ID = timestamp_seconds * 1000000 + random(1-999999)
```
- 时间戳部分: 10位数字(~1.7e9)
- 随机部分: 6位数字(1-999999)
- 最大值: ~1.7e15,远小于BIGINT上限(9.2e18)
- 优势: 避免毫秒级时间戳导致的溢出问题

### 事务控制
- 所有菜单创建操作在一个事务中完成
- 任何步骤失败都会触发`ROLLBACK`
- 通过`EXIT HANDLER`捕获异常并输出错误信息

### 外键约束
- `ai_skill_template.category_code` → `ai_skill_category.category_code`
- 删除分类时自动级联删除相关Skill模板
- 插入Skill时必须指定有效的分类编码

---

## 🔄 版本历史

### v2.3 (2026-04-28) - API Key动态配置版本
- ✅ 新增: ai_system_config表(API Key动态配置)
- ✅ 新增: AiConfigLoader配置加载器
- ✅ 新增: AI系统配置管理菜单及页面
- ✅ 优化: SpringAiConfig移除硬编码API Key处理
- ✅ 符合: Spring AI官方规范(spring.ai.xxx.api-key)
- ✅ 合并: ai-system-config.sql到主脚本(简化流程)

### v2.2 (2026-04-28) - 提示词管理版本
- ✅ 新增: ai_prompt表(提示词管理)
- ✅ 新增: 提示词管理菜单及按钮权限
- ✅ 优化: SQL脚本模块化重构(3个文件)
- ✅ 优化: 完全重置机制(删除旧表重建)
- ✅ 优化: 幂等性设计(支持重复执行)

### v2.1 (2026-04-28) - P0/P1优化版本
- ✅ 优化: ai_usage_log表索引增强
- ✅ 优化: 菜单路径冲突检测
- ✅ 修复: 雪花ID溢出问题

### v2.0 (2026-04-28) - Skill平台版本
- ✅ 新增: ai_skill_category分类表
- ✅ 新增: ai_skill_template模板表
- ✅ 新增: Skill市场菜单
- ✅ 新增: 外键约束支持

### v1.0 (2026-04-28) - 初始版本
- ✅ 新增: ai_model_config模型配置表
- ✅ 新增: ai_usage_log使用日志表
- ✅ 新增: 基础菜单结构

---

## 📞 技术支持

**文档维护**: 随模块迭代同步更新  
**问题反馈**: 提交Issue或联系技术人员  
**最后更新**: 2026-04-28

---

**祝使用愉快!** 🎉
