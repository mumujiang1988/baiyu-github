# ERP 配置 v3.0 快速上手指南

## 🚀 5 分钟快速部署

### 前提条件
- MySQL 数据库已启动
- 已有旧的 erp_page_config 表（6 字段版本）
- 后端服务已停止

---

## Step 1: 升级数据库（1 分钟）

```bash
# 进入 SQL 脚本目录
cd d:\baiyuyunma\baiyu-github\baiyu-github\baiyu-web\src\views\erp\脚本库

# 执行升级脚本
mysql -u root -p test < upgrade-to-v3.sql
```

**预期输出：**
```
✅ 备份完成：erp_page_config_backup_20260327
✅ search_config 字段已添加
✅ action_config 字段已添加
✅ 触发器已更新（包含 search_config 和 action_config）
🎉 表结构升级完成！
```

---

## Step 2: 导入新配置（1 分钟）

```bash
# 执行新配置导入
mysql -u root -p test < 拆分 json 导入-v3.sql
```

**预期输出：**
```
✅ 销售订单配置导入成功！

配置统计信息：
页面配置字段数：21
搜索字段数：6
工具栏按钮数：8
表格列数：13
字典数量：10
详情页签数：2
```

---

## Step 3: 编译启动后端（3 分钟）

```bash
# 清理编译
cd d:\baiyuyunma\baiyu-github\baiyu-github\baiyu-ruoyi
mvn clean package -DskipTests -pl '!ruoyi-modules/ruoyi-demo'

# 启动服务
java -jar baiyu-ruoyi/ruoyi-admin-wms/target/ruoyi-admin-wms.jar
```

**预期输出：**
```
Started ErpApplication in 15.123 seconds
Tomcat started on port(s): 8180 (http)
```

---

## Step 4: 验证功能（1 分钟）

### 访问页面
打开浏览器，访问：`http://localhost:8180/business/saleorder`

### 检查项
- ✅ 顶部显示搜索区域（日期、单据编号、客户简称等）
- ✅ 左侧显示工具栏按钮（新增、修改、删除、审核等）
- ✅ 中间显示表格数据
- ✅ 右侧显示列设置按钮

---

## 🎯 配置结构说明

### 8 个核心字段

| 序号 | 字段名 | 作用 | 数据来源 |
|------|--------|------|---------|
| 1 | page_config | 页面基础信息 | page.json |
| 2 | form_config | 表单字段定义 | form.json |
| 3 | table_config | 表格列配置 | table.json |
| 4 | **search_config** | **搜索区域配置** ✨ | **search.json** |
| 5 | **action_config** | **按钮操作配置** ✨ | **config.json** |
| 6 | dict_config | 字典数据源 | dict.json |
| 7 | business_config | 业务消息规则 | config.json |
| 8 | detail_config | 详情页签配置 | detail.json |

---

## 🔍 常见问题

### Q1: 升级后看不到搜索区域？
**A:** 检查前端是否更新到最新代码，确保 `ERPConfigParser.js` 支持 `searchConfig` 字段。

### Q2: 按钮不显示？
**A:** 检查 `actionConfig.toolbar` 是否正确配置，确认权限标识匹配。

### Q3: 报错"找不到字段"？
**A:** 确认数据库字段已添加成功，执行以下 SQL 验证：
```sql
SHOW COLUMNS FROM erp_page_config LIKE '%config';
```

---

## 📞 需要帮助？

查看完整文档：[docs/ERP 配置优化方案 v3.0.md](docs/ERP 配置优化方案 v3.0.md)

---

**最后更新**: 2026-03-27  
**维护团队**: JMH
