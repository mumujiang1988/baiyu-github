# ruoyi-erp-api 架构设计文档

**版本**: v3.0  
**最后更新**: 2026-03-30  
**作者**: JMH  
**状态**: ✅ 已实施  

---

## 📋 文档导航

- [1. 模块概述](#1-模块概述)
- [2. 技术栈](#2-技术栈)
- [3. 架构设计](#3-架构设计)
- [4. 核心功能模块](#4-核心功能模块)
- [5. 数据库设计](#5-数据库设计)
- [6. API 接口设计](#6-api-接口设计)
- [7. 关键技术实现](#7-关键技术实现)
- [8. 安全与权限](#8-安全与权限)
- [9. 性能优化](#9-性能优化)
- [10. 最佳实践](#10-最佳实践)

---

## 1. 模块概述

### 1.1 模块定位

`ruoyi-erp-api` 是 RuoYi-WMS 框架中的 **ERP 配置化 API 模块**，提供低代码开发的核心能力支撑。

**核心价值**：
- 🎯 为前端低代码页面提供统一的配置管理服务
- 🔧 支持动态表单、表格、查询、字典等配置
- 🚀 实现业务逻辑的可视化配置和热插拔
- 📦 提供审批流、下推关系、虚拟字段等高级特性

### 1.2 模块结构

```
ruoyi-erp-api/
├── controller/          # 控制器层（HTTP 接口）
│   ├── ErpDictionaryController.java    # 字典接口
│   └── erp/             # ERP 业务控制器目录
├── domain/              # 领域模型
│   ├── entity/          # 实体类
│   ├── vo/              # 视图对象
│   ├── bo/              # 业务对象
│   └── response/        # 响应对象
├── service/             # 服务层
│   ├── impl/            # 服务实现
│   └── engine/          # 引擎服务
├── mapper/              # 数据访问层
├── event/               # 事件定义
├── listener/            # 事件监听器
├── exception/           # 异常处理
└── utils/               # 工具类
    └── SqlBuilder.java  # SQL 构建器
```

---

## 2. 技术栈

### 2.1 核心技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.x | 后端框架 |
| MyBatis Plus | 3.5.5 | ORM 框架 |
| MySQL Connector | 8.x | 数据库驱动 |
| Spring JDBC | 3.x | JDBC 模板 |
| Sa-Token | - | 权限认证 |

### 2.2 辅助工具

| 组件 | 版本 | 说明 |
|------|------|------|
| Lombok | - | 简化代码 |
| MapStruct Plus | - | 对象映射 |
| Fastjson2 | 2.0.43 | JSON 处理 |
| Hutool | - | 工具类库 |

### 2.3 依赖结构

```xml
<dependencies>
    <!-- 框架核心 -->
    <dependency>
        <groupId>com.ruoyi</groupId>
        <artifactId>ruoyi-common-core</artifactId>
    </dependency>
    <dependency>
        <groupId>com.ruoyi</groupId>
        <artifactId>ruoyi-common-security</artifactId>
    </dependency>
    <dependency>
        <groupId>com.ruoyi</groupId>
        <artifactId>ruoyi-common-web</artifactId>
    </dependency>
    <dependency>
        <groupId>com.ruoyi</groupId>
        <artifactId>ruoyi-common-mybatis</artifactId>
    </dependency>
    
    <!-- 系统模块 -->
    <dependency>
        <groupId>com.ruoyi</groupId>
        <artifactId>ruoyi-system</artifactId>
    </dependency>
    
    <!-- Excel 导入导出 -->
    <dependency>
        <groupId>com.ruoyi</groupId>
        <artifactId>ruoyi-common-excel</artifactId>
    </dependency>
    
    <!-- MyBatis Plus -->
    <dependency>
        <groupId>com.baomidou</groupId>
        <artifactId>mybatis-plus-boot-starter</artifactId>
        <version>3.5.5</version>
    </dependency>
    
    <!-- Sa-Token -->
    <dependency>
        <groupId>cn.dev33</groupId>
        <artifactId>sa-token-spring-boot3-starter</artifactId>
    </dependency>
    
    <!-- Spring JDBC -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-jdbc</artifactId>
    </dependency>
</dependencies>
```

---

## 3. 架构设计

### 3.1 分层架构

```
┌─────────────────────────────────────────┐
│         Controller Layer                │
│  (ErpDictionaryController, etc.)        │
├─────────────────────────────────────────┤
│         Service Layer                   │
│  (Service + Engine)                     │
├─────────────────────────────────────────┤
│         Mapper Layer                    │
│  (Data Access)                          │
├─────────────────────────────────────────┤
│         Database                        │
│  (MySQL)                                │
└─────────────────────────────────────────┘
```

### 3.2 核心设计模式

#### 3.2.1 策略模式 - 动态查询引擎

```java
public class DynamicQueryEngine {
    // 根据配置的运算符动态构建查询条件
    public List<Map<String, Object>> execute(QueryConfig config) {
        // 1. 解析配置
        // 2. 选择对应的策略处理器
        // 3. 执行查询
    }
}
```

#### 3.2.2 责任链模式 - 表单验证引擎

```java
public class FormValidationEngine {
    private List<Validator> validators = new ArrayList<>();
    
    public ValidationResult validate(FormData data) {
        for (Validator validator : validators) {
            if (!validator.validate(data)) {
                return ValidationResult.fail(validator.getErrorMessage());
            }
        }
        return ValidationResult.success();
    }
}
```

#### 3.2.3 观察者模式 - 配置刷新事件

```java
// 事件定义
public class ConfigRefreshEvent extends ApplicationEvent {
    private final String moduleCode;
    private final Integer version;
}

// 事件发布
eventPublisher.publishEvent(new ConfigRefreshEvent(this, moduleCode, version));

// 事件监听
@EventListener
public void onConfigRefresh(ConfigRefreshEvent event) {
    // 处理配置刷新
}
```

### 3.3 数据流转

```
前端请求 → Controller → Service → Engine → Mapper → Database
                  ↓
              Cache (Redis)
                  ↓
              Event Bus (可选)
```

---

## 4. 核心功能模块

### 4.1 页面配置管理 (ErpPageConfigService)

**职责**: 管理低代码页面的配置信息

**核心方法**:
- `getPageConfig(String moduleCode)`: 获取页面配置（带缓存）
- `saveWithVersion(ErpPageConfigBo bo)`: 保存配置（版本管理）
- `rollbackToVersion(...)`: 回滚到指定版本
- `exportConfig(...) / importConfig(...)`: 导入导出配置

**技术特性**:
- ✅ Redis 缓存支持（TTL: 1 小时）
- ✅ 版本控制（自动递增）
- ✅ 历史记录追踪
- ✅ 配置刷新事件广播

**代码示例**:

```java
@Override
public String getPageConfig(String moduleCode) {
    // 1. 先查缓存
    Object cached = CacheUtils.get(CacheNames.ERP_CONFIG, moduleCode);
    if (ObjectUtil.isNotNull(cached)) {
        return cached.toString();
    }
    
    // 2. 缓存未命中，查数据库
    List<Map<String, Object>> conditions = buildConditions(moduleCode);
    SqlBuilder.SqlResult sqlResult = sqlBuilder.buildWhere(conditions);
    String sql = "SELECT * FROM erp_page_config" + sqlResult.getSql() + " ORDER BY version DESC LIMIT 1";
    List<Map<String, Object>> resultList = jdbcTemplate.queryForList(sql, sqlResult.getParams().toArray());
    
    // 3. 组合 JSON 返回
    String jsonString = buildJsonString(resultList.get(0));
    
    // 4. 放入缓存
    CacheUtils.put(CacheNames.ERP_CONFIG, moduleCode, jsonString);
    
    return jsonString;
}
```

### 4.2 字典服务 (ErpDictionaryService)

**职责**: 统一管理系统的字典数据

**核心接口**:
- `/erp/dict/getUnionDict`: 获取合并字典（系统 + 业务）
- `/erp/dict/getAllDict`: 获取全部字典
- `/erp/dict/getBizDictByCategory`: 按分类查询业务字典
- `/erp/dict/getCountryById`: 查询国家详情

**技术特性**:
- ✅ 多数据源融合（系统字典表 + 业务字典表）
- ✅ 自定义值字段支持
- ✅ 国家字典独立管理

### 4.3 审批流程引擎 (ApprovalWorkflowEngine)

**职责**: 处理单据的审批流程

**核心功能**:
- 审批流程配置解析
- 审批节点自动推进
- 审批权限校验
- 审批历史生成

**使用场景**:
- 采购订单审批
- 销售订单审批
- 库存调拨审批

### 4.4 下推引擎 (PushDownEngine)

**职责**: 处理单据之间的下推关系

**核心功能**:
- 下推关系配置管理
- 字段映射转换
- 数据一致性校验
- 下推历史记录

**典型场景**:
- 采购订单 → 入库单
- 销售订单 → 出库单
- 报价单 → 销售订单

### 4.5 虚拟字段服务 (VirtualFieldService)

**职责**: 提供动态计算字段能力

**核心功能**:
- 虚拟字段配置解析
- 表达式计算引擎
- 跨表数据关联
- 实时数据刷新

**示例配置**:

```json
{
  "fieldName": "totalAmount",
  "expression": "quantity * unitPrice",
  "dataType": "decimal",
  "visible": true
}
```

### 4.6 动态查询引擎 (DynamicQueryEngine)

**职责**: 根据配置动态构建查询

**支持的运算符**:
- 比较运算：`eq`, `ne`, `gt`, `ge`, `lt`, `le`
- 模糊匹配：`like`, `left_like`, `right_like`
- 集合运算：`in`, `between`
- 空值判断：`isNull`, `isNotNull`

**使用示例**:

```java
List<Map<String, Object>> conditions = new ArrayList<>();
Map<String, Object> condition = new HashMap<>();
condition.put("field", "status");
condition.put("operator", "eq");
condition.put("value", "1");
conditions.add(condition);

SqlBuilder.SqlResult sqlResult = sqlBuilder.buildWhere(conditions);
String sql = "SELECT * FROM table" + sqlResult.getSql();
List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, sqlResult.getParams().toArray());
```

### 4.7 表单验证引擎 (FormValidationEngine)

**职责**: 对表单数据进行验证

**验证规则**:
- 必填项检查
- 格式验证（邮箱、手机、身份证等）
- 长度限制
- 数值范围
- 自定义正则表达式

### 4.8 计算字段引擎 (ComputedFieldEngine)

**职责**: 处理复杂字段计算逻辑

**计算类型**:
- 算术运算（加减乘除）
- 聚合计算（求和、平均、计数）
- 函数调用（日期、字符串处理）
- 条件计算（IF-THEN-ELSE）

---

## 5. 数据库设计

### 5.1 核心表结构

#### 5.1.1 erp_page_config（页面配置表）

```sql
CREATE TABLE `erp_page_config` (
  `config_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '配置 ID',
  `module_code` VARCHAR(100) NOT NULL COMMENT '模块编码',
  `config_name` VARCHAR(200) COMMENT '配置名称',
  `config_type` VARCHAR(50) COMMENT '配置类型（PAGE/FORM/TABLE）',
  `status` CHAR(1) DEFAULT '1' COMMENT '状态（0-停用，1-启用）',
  `is_public` CHAR(1) DEFAULT '0' COMMENT '是否公共配置（0-否，1-是）',
  `parent_config_id` BIGINT COMMENT '父配置 ID',
  `version` INT DEFAULT 1 COMMENT '版本号',
  
  -- 九字段配置（JSON 格式）
  `page_config` TEXT COMMENT '页面配置',
  `form_config` TEXT COMMENT '表单配置',
  `table_config` TEXT COMMENT '表格配置',
  `search_config` TEXT COMMENT '查询配置',
  `action_config` TEXT COMMENT '操作配置',
  `api_config` TEXT COMMENT 'API 配置',
  `dict_config` TEXT COMMENT '字典配置',
  `business_config` TEXT COMMENT '业务配置',
  `detail_config` TEXT COMMENT '详情配置',
  
  `remark` VARCHAR(500) COMMENT '备注',
  `create_by` VARCHAR(64) COMMENT '创建人',
  `create_time` DATETIME COMMENT '创建时间',
  `update_by` VARCHAR(64) COMMENT '更新人',
  `update_time` DATETIME COMMENT '更新时间',
  
  UNIQUE KEY `uk_module_type` (`module_code`, `config_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 页面配置表';
```

#### 5.1.2 erp_page_config_history（配置历史表）

```sql
CREATE TABLE `erp_page_config_history` (
  `history_id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `config_id` BIGINT NOT NULL COMMENT '配置 ID',
  `module_code` VARCHAR(100) NOT NULL COMMENT '模块编码',
  `config_type` VARCHAR(50) COMMENT '配置类型',
  `version` INT NOT NULL COMMENT '版本号',
  
  -- 配置内容（同 erp_page_config）
  `page_config` TEXT,
  `form_config` TEXT,
  `table_config` TEXT,
  `search_config` TEXT,
  `action_config` TEXT,
  `api_config` TEXT,
  `dict_config` TEXT,
  `business_config` TEXT,
  
  `change_reason` VARCHAR(500) COMMENT '变更原因',
  `change_type` VARCHAR(50) COMMENT '变更类型（UPDATE/ROLLBACK）',
  `create_by` VARCHAR(64),
  `create_time` DATETIME,
  
  INDEX `idx_config_version` (`config_id`, `version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 页面配置历史表';
```

#### 5.1.3 erp_approval_flow（审批流程表）

```sql
CREATE TABLE `erp_approval_flow` (
  `flow_id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `bill_type` VARCHAR(100) NOT NULL COMMENT '单据类型',
  `flow_name` VARCHAR(200) COMMENT '流程名称',
  `node_sequence` JSON COMMENT '节点序列配置',
  `status` CHAR(1) DEFAULT '1' COMMENT '状态',
  `create_time` DATETIME,
  `update_time` DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 审批流程表';
```

#### 5.1.4 erp_approval_history（审批历史表）

```sql
CREATE TABLE `erp_approval_history` (
  `history_id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `bill_id` BIGINT NOT NULL COMMENT '单据 ID',
  `bill_type` VARCHAR(100) COMMENT '单据类型',
  `node_id` BIGINT COMMENT '审批节点 ID',
  `approver_id` BIGINT COMMENT '审批人 ID',
  `approve_status` CHAR(1) COMMENT '审批状态（0-待审，1-通过，2-驳回）',
  `approve_opinion` VARCHAR(500) COMMENT '审批意见',
  `approve_time` DATETIME COMMENT '审批时间',
  `create_time` DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 审批历史表';
```

#### 5.1.5 erp_push_relation（下推关系表）

```sql
CREATE TABLE `erp_push_relation` (
  `relation_id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `source_bill_type` VARCHAR(100) NOT NULL COMMENT '源单据类型',
  `target_bill_type` VARCHAR(100) NOT NULL COMMENT '目标单据类型',
  `field_mapping` JSON COMMENT '字段映射配置',
  `transform_rules` JSON COMMENT '转换规则',
  `enabled` CHAR(1) DEFAULT '1' COMMENT '是否启用',
  `create_time` DATETIME,
  `update_time` DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 下推关系表';
```

### 5.2 索引设计

| 表名 | 索引字段 | 索引类型 | 说明 |
|------|---------|---------|------|
| erp_page_config | module_code + config_type | 唯一索引 | 防止重复配置 |
| erp_page_config | status + is_public | 普通索引 | 查询优化 |
| erp_page_config_history | config_id + version | 普通索引 | 版本查询 |
| erp_approval_flow | bill_type + status | 普通索引 | 流程查询 |
| erp_approval_history | bill_id + approve_time | 普通索引 | 历史查询 |

### 5.3 数据字典

#### config_type 配置类型

| 值 | 说明 | 使用场景 |
|----|------|---------|
| PAGE | 页面配置 | 完整页面布局 |
| FORM | 表单配置 | 新增/编辑表单 |
| TABLE | 表格配置 | 列表展示配置 |
| SEARCH | 查询配置 | 搜索条件配置 |

#### status 状态

| 值 | 说明 |
|----|------|
| 0 | 停用 |
| 1 | 启用 |

#### is_public 公共性

| 值 | 说明 |
|----|------|
| 0 | 私有配置 |
| 1 | 公共配置 |

---

## 6. API 接口设计

### 6.1 页面配置接口

#### 6.1.1 获取页面配置

```http
GET /erp/page-config/getPageConfig?moduleCode={moduleCode}
```

**响应示例**:

```json
{
  "code": 200,
  "data": {
    "pageConfig": {...},
    "formConfig": {...},
    "tableConfig": {...},
    "searchConfig": {...},
    "actionConfig": {...},
    "apiConfig": {...},
    "dictionaryConfig": {...},
    "businessConfig": {...},
    "detailConfig": {...},
    "moduleCode": "purchase_order",
    "configName": "采购订单页面",
    "version": 3
  },
  "msg": "success"
}
```

#### 6.1.2 保存配置

```http
POST /erp/page-config/saveWithVersion
Content-Type: application/json

{
  "configId": 1,
  "moduleCode": "purchase_order",
  "configName": "采购订单页面",
  "configType": "PAGE",
  "pageConfig": "{...}",
  "formConfig": "{...}",
  "tableConfig": "{...}",
  "searchConfig": "{...}",
  "actionConfig": "{...}",
  "apiConfig": "{...}",
  "dictConfig": "{...}",
  "businessConfig": "{...}",
  "detailConfig": "{...}",
  "changeReason": "新增付款条件字段"
}
```

#### 6.1.3 回滚到指定版本

```http
POST /erp/page-config/rollbackToVersion
Content-Type: application/json

{
  "configId": 1,
  "targetVersion": 2,
  "reason": "v3 版本存在严重 Bug"
}
```

#### 6.1.4 导出配置

```http
GET /erp/page-config/export?configId=1
```

**响应**: JSON 文件下载

#### 6.1.5 导入配置

```http
POST /erp/page-config/import
Content-Type: multipart/form-data

file: config.json
```

### 6.2 字典接口

#### 6.2.1 获取合并字典

```http
GET /erp/dict/getUnionDict?dictType=customer_category
```

**响应示例**:

```json
[
  {
    "dictLabel": "企业客户",
    "dictValue": "enterprise",
    "dictType": "customer_category"
  },
  {
    "dictLabel": "个人客户",
    "dictValue": "individual",
    "dictType": "customer_category"
  }
]
```

#### 6.2.2 获取全部字典

```http
GET /erp/dict/getAllDict
```

#### 6.2.3 查询业务字典

```http
GET /erp/dict/getBizDictByCategory?category=price_type
```

### 6.3 审批流程接口

#### 6.3.1 发起审批

```http
POST /erp/approval/start
Content-Type: application/json

{
  "billType": "purchase_order",
  "billId": 123,
  "initiatorId": 1
}
```

#### 6.3.2 审批操作

```http
POST /erp/approval/approve
Content-Type: application/json

{
  "historyId": 456,
  "approveStatus": "1",
  "approveOpinion": "同意"
}
```

### 6.4 下推关系接口

#### 6.4.1 执行下推

```http
POST /erp/push/down
Content-Type: application/json

{
  "sourceBillType": "purchase_order",
  "sourceBillId": 123,
  "targetBillType": "stock_in"
}
```

---

## 7. 关键技术实现

### 7.1 SqlBuilder - 动态 SQL 构建器

**设计目标**: 完全替代 QueryWrapper，避免 MyBatis Plus 依赖冲突

**核心功能**:

```java
@Component
public class SqlBuilder {
    
    /**
     * 构建 WHERE 子句
     * @param conditions 条件列表 [{field, operator, value}]
     */
    public SqlResult buildWhere(List<Map<String, Object>> conditions) {
        // 实现逻辑
    }
    
    /**
     * 构建 ORDER BY 子句
     */
    public SqlResult buildOrderBy(List<Map<String, Object>> orderBy) {
        // 实现逻辑
    }
    
    /**
     * 构建 SELECT 字段列表
     */
    public String buildSelectFields(List<String> fields) {
        // 实现逻辑
    }
}
```

**支持的运算符**:

| 运算符 | 说明 | SQL 示例 |
|--------|------|---------|
| eq | 等于 | `field = ?` |
| ne | 不等于 | `field <> ?` |
| gt | 大于 | `field > ?` |
| ge | 大于等于 | `field >= ?` |
| lt | 小于 | `field < ?` |
| le | 小于等于 | `field <= ?` |
| like | 模糊匹配 | `field LIKE ?` |
| left_like | 左模糊 | `field LIKE ?%` |
| right_like | 右模糊 | `field LIKE %?` |
| in | 包含 | `field IN (?, ?, ...)` |
| between | 区间 | `field BETWEEN ? AND ?` |
| isNull | 为空 | `field IS NULL` |
| isNotNull | 不为空 | `field IS NOT NULL` |

**安全机制**:

1. **字段名校验**: 正则表达式 `^[a-zA-Z_][a-zA-Z0-9_]*$`
2. **SQL 关键词黑名单**: DROP, DELETE, UPDATE, INSERT 等
3. **参数化查询**: 所有值使用 `?` 占位符
4. **LIKE 特殊字符转义**: `%` → `\%`, `_` → `\_`

**使用示例**:

```java
@Autowired
private SqlBuilder sqlBuilder;
@Autowired
private JdbcTemplate jdbcTemplate;

public List<Map<String, Object>> queryData() {
    List<Map<String, Object>> conditions = new ArrayList<>();
    
    Map<String, Object> cond1 = new HashMap<>();
    cond1.put("field", "status");
    cond1.put("operator", "eq");
    cond1.put("value", "1");
    conditions.add(cond1);
    
    Map<String, Object> cond2 = new HashMap<>();
    cond2.put("field", "amount");
    cond2.put("operator", "gt");
    cond2.put("value", 1000);
    conditions.add(cond2);
    
    SqlBuilder.SqlResult sqlResult = sqlBuilder.buildWhere(conditions);
    String sql = "SELECT * FROM purchase_order" + sqlResult.getSql();
    
    return jdbcTemplate.queryForList(sql, sqlResult.getParams().toArray());
}
```

### 7.2 缓存管理机制

**缓存策略**:

```java
// 缓存键命名规范
CacheNames.ERP_CONFIG + ":" + moduleCode

// TTL 设置
@Cacheable(value = CacheNames.ERP_CONFIG, ttl = 3600) // 1 小时

// 缓存清除
CacheUtils.evict(CacheNames.ERP_CONFIG, moduleCode);
```

**缓存更新时机**:

1. 配置新增/修改/删除后
2. 配置状态变更后
3. 版本回滚后

**缓存失效处理**:

```java
// 双重检查锁（如果需要强一致性）
synchronized (this) {
    Object cached = CacheUtils.get(key);
    if (cached == null) {
        // 从数据库加载
    }
}
```

### 7.3 版本管理机制

**版本号规则**:

- 初始版本：v1
- 每次修改：版本号 +1
- 回滚操作：当前版本号 +1，内容为回滚目标版本的内容

**历史记录**:

```java
private void recordHistory(ErpPageConfig config, String changeReason) {
    String sql = """
        INSERT INTO erp_page_config_history (
            config_id, module_code, config_type, version,
            page_config, form_config, table_config,
            search_config, action_config, api_config,
            dict_config, business_config,
            change_reason, change_type, create_by, create_time
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """;
    jdbcTemplate.update(sql, /* 参数 */);
}
```

### 7.4 事件驱动架构

**事件定义**:

```java
public class ConfigRefreshEvent extends ApplicationEvent {
    private final String moduleCode;
    private final Integer version;
    
    public ConfigRefreshEvent(Object source, String moduleCode, Integer version) {
        super(source);
        this.moduleCode = moduleCode;
        this.version = version;
    }
    
    // getters
}
```

**事件发布**:

```java
@Autowired
private ApplicationEventPublisher eventPublisher;

public void updateConfig(ErpPageConfigBo bo) {
    // 1. 更新配置
    jdbcTemplate.update(updateSql, /* 参数 */);
    
    // 2. 清除缓存
    CacheUtils.evict(CacheNames.ERP_CONFIG, moduleCode);
    
    // 3. 广播事件
    eventPublisher.publishEvent(
        new ConfigRefreshEvent(this, moduleCode, newVersion)
    );
}
```

**事件监听**:

```java
@Component
public class ConfigRefreshListener implements ApplicationListener<ConfigRefreshEvent> {
    @Override
    public void onApplicationEvent(ConfigRefreshEvent event) {
        log.info("收到配置刷新事件，moduleCode: {}, version: {}", 
            event.getModuleCode(), event.getVersion());
        // 处理刷新逻辑
    }
}
```

### 7.5 JSON 处理技巧

**九字段组合返回**:

```java
String jsonString = String.format(
    "{\"pageConfig\":%s,\"formConfig\":%s,\"tableConfig\":%s,\"searchConfig\":%s,\"actionConfig\":%s,\"apiConfig\":%s,\"dictionaryConfig\":%s,\"businessConfig\":%s,\"detailConfig\":%s,\"moduleCode\":\"%s\",\"configName\":\"%s\",\"version\":%d}",
    config.getPageConfig(),
    config.getFormConfig(),
    config.getTableConfig(),
    config.getSearchConfig(),
    config.getActionConfig(),
    config.getApiConfig(),
    config.getDictConfig(),
    config.getBusinessConfig(),
    config.getDetailConfig(),
    escapeJson(config.getModuleCode()),
    escapeJson(config.getConfigName()),
    config.getVersion()
);
```

**注意事项**:
- ✅ 直接拼接 JSON 字符串，避免序列化/反序列化损耗
- ✅ 字符串字段需要转义（`escapeJson` 方法）
- ✅ JSON 字段不加引号（已经是 JSON 格式）

---

## 8. 安全与权限

### 8.1 接口权限控制

```java
@RestController
@RequestMapping("/erp/page-config")
@SaCheckPermission("erp:page-config")
public class ErpPageConfigController {
    
    @PostMapping("/save")
    @SaCheckPermission("erp:page-config:add")
    public AjaxResult save(@RequestBody ErpPageConfigBo bo) {
        // ...
    }
}
```

### 8.2 数据权限过滤

```java
// 在查询中自动添加数据权限条件
String dataScopeSql = getDataScopeFilter("t."); // t.是表别名
String sql = "SELECT * FROM erp_page_config t " + dataScopeSql;
```

### 8.3 SQL 注入防护

**SqlBuilder 防护机制**:

1. 字段名白名单校验
2. SQL 关键词黑名单过滤
3. 所有参数使用预编译
4. LIKE 语句特殊字符转义

### 8.4 敏感数据保护

```java
// 字典配置中的敏感字段加密存储
@Encrypt
private String apiKey;

// 日志中脱敏显示
log.info("配置已保存，moduleCode: {}", moduleCode);
```

---

## 9. 性能优化

### 9.1 缓存优化

**多级缓存策略**:

```
L1: 本地缓存 (Caffeine) - 热点数据
      ↓
L2: Redis 缓存 - 共享数据
      ↓
L3: 数据库 - 持久化数据
```

**缓存命中率提升**:

```java
// 批量查询时使用本地缓存
Map<String, Object> localCache = new ConcurrentHashMap<>();
for (String moduleCode : moduleCodes) {
    if (!localCache.containsKey(moduleCode)) {
        // 从 Redis 或数据库加载
    }
}
```

### 9.2 数据库优化

**索引优化**:

```sql
-- 复合索引（高频查询场景）
CREATE INDEX idx_status_module ON erp_page_config(status, module_code);

-- 覆盖索引（减少回表）
CREATE INDEX idx_module_version ON erp_page_config(module_code, version);
```

**分页优化**:

```java
// 深分页优化（使用游标）
String sql = "SELECT * FROM erp_page_config WHERE config_id > ? ORDER BY config_id LIMIT ?";
```

### 9.3 异步处理

**配置刷新异步化**:

```java
@Async
public void refreshConfigAsync(String moduleCode) {
    // 非关键路径的刷新操作
    clearRelatedCache(moduleCode);
    notifyRelatedModules(moduleCode);
}
```

### 9.4 批量操作

**批量插入**:

```java
// 使用批量插入而非循环单条插入
String sql = "INSERT INTO history (...) VALUES (?, ?, ...), (?, ?, ...)";
jdbcTemplate.batchUpdate(sql, batchArgs);
```

---

## 10. 最佳实践

### 10.1 开发规范

#### 10.1.1 Service 层规范

```java
@Service
@RequiredArgsConstructor  // 强制使用构造器注入
@Slf4j  // 统一日志
public class ErpPageConfigServiceImpl implements ErpPageConfigService {
    
    private final JdbcTemplate jdbcTemplate;  // final 字段
    private final SqlBuilder sqlBuilder;
    
    @Override
    @Transactional(rollbackFor = Exception.class)  // 事务注解
    public int insertByBo(ErpPageConfigBo bo) {
        // 1. 参数校验
        if (ObjectUtil.isNull(bo)) {
            throw new ServiceException("参数不能为空");
        }
        
        // 2. 业务逻辑
        // ...
        
        // 3. 日志记录
        log.info("新增配置成功，moduleCode: {}", bo.getModuleCode());
        
        return row;
    }
}
```

#### 10.1.2 Controller 层规范

```java
@RestController
@RequestMapping("/erp/page-config")
@RequiredArgsConstructor
public class ErpPageConfigController {
    
    private final ErpPageConfigService pageConfigService;
    
    @GetMapping("/get")
    public AjaxResult getPageConfig(@RequestParam String moduleCode) {
        String config = pageConfigService.getPageConfig(moduleCode);
        return AjaxResult.success(config);
    }
}
```

#### 10.1.3 异常处理规范

```java
try {
    // 业务逻辑
} catch (ServiceException e) {
    log.error("业务异常", e);
    throw e;  // 直接抛出
} catch (Exception e) {
    log.error("系统异常", e);
    throw new ServiceException("操作失败：" + e.getMessage());
}
```

### 10.2 日志规范

**日志级别使用**:

| 级别 | 使用场景 | 示例 |
|------|---------|------|
| ERROR | 系统异常、业务失败 | `log.error("保存配置失败", e)` |
| WARN | 警告信息、可恢复错误 | `log.warn("配置不存在，将创建新配置")` |
| INFO | 关键业务流程 | `log.info("配置保存成功，version: {}")` |
| DEBUG | 调试信息、详细参数 | `log.debug("查询条件：{}", conditions)` |
| TRACE | 最详细的跟踪信息 | `log.trace("进入方法，参数：{}")` |

**日志格式**:

```java
// 推荐格式
log.info("[getPageConfig] 缓存未命中，moduleCode: {}", moduleCode);

// 不推荐
log.info("缓存未命中，moduleCode: " + moduleCode);  // 字符串拼接
```

### 10.3 事务管理

**事务边界**:

```java
@Transactional(rollbackFor = Exception.class)
public void saveConfig(ErpPageConfigBo bo) {
    // 1. 插入主表
    insertMainTable(bo);
    
    // 2. 插入历史表
    insertHistory(bo);
    
    // 3. 清除缓存
    clearCache(bo.getModuleCode());
}
```

**事务注意事项**:

- ⚠️ 同一类中方法调用事务不生效（需注入自身）
- ⚠️ 异常捕获后事务不回滚（需手动抛出）
- ⚠️ 大事务拆分为小事务（避免长事务）

### 10.4 单元测试

```java
@SpringBootTest
class ErpPageConfigServiceTest {
    
    @Autowired
    private ErpPageConfigService pageConfigService;
    
    @Test
    void testGetPageConfig() {
        // Given
        String moduleCode = "test_purchase_order";
        
        // When
        String config = pageConfigService.getPageConfig(moduleCode);
        
        // Then
        assertNotNull(config);
        assertTrue(config.contains("pageConfig"));
    }
    
    @Test
    @Transactional
    void testSaveWithVersion() {
        // Given
        ErpPageConfigBo bo = new ErpPageConfigBo();
        bo.setModuleCode("test_po");
        bo.setConfigName("测试采购订单");
        
        // When
        int result = pageConfigService.saveWithVersion(bo);
        
        // Then
        assertEquals(1, result);
    }
}
```

### 10.5 代码审查清单

- [ ] 所有数据库操作使用 SqlBuilder（无 UNION、无 collation 冲突）
- [ ] 所有查询使用参数化（防 SQL 注入）
- [ ] 所有事务标注 `@Transactional`
- [ ] 所有异常有日志记录
- [ ] 所有缓存操作有 TTL
- [ ] 所有接口有权限控制
- [ ] 所有 BO/VO 使用 Mapstruct 转换
- [ ] 所有方法有 JavaDoc 注释

---

## 附录

### A. 常用工具类

#### A.1 MapstructUtils（对象转换）

```java
// Entity ↔ VO
ErpPageConfigVo vo = MapstructUtils.convert(entity, ErpPageConfigVo.class);

// Map ↔ VO
ErpPageConfigVo vo = MapstructUtils.convert(map, ErpPageConfigVo.class);

// BO ↔ Entity
ErpPageConfig entity = MapstructUtils.convert(bo, ErpPageConfig.class);
```

#### A.2 JsonUtils（JSON 处理）

```java
// 对象转 JSON
String json = JsonUtils.toJsonString(object);

// JSON 转对象
Object obj = JsonUtils.parseObject(jsonStr, Object.class);

// JSON 转 Map
Map<String, Object> map = JsonUtils.parseObject(jsonStr, Map.class);
```

#### A.3 CacheUtils（缓存操作）

```java
// 获取缓存
Object value = CacheUtils.get(CacheNames.ERP_CONFIG, key);

// 放入缓存
CacheUtils.put(CacheNames.ERP_CONFIG, key, value);

// 清除缓存
CacheUtils.evict(CacheNames.ERP_CONFIG, key);

// 批量清除
CacheUtils.evict(CacheNames.ERP_CONFIG_KEYS);
```

### B. 常见错误码

| 错误码 | 说明 | 解决方案 |
|--------|------|---------|
| 500 | 系统内部错误 | 查看日志定位具体原因 |
| 401 | 未授权 | 检查登录状态和 Token |
| 403 | 无权限 | 检查用户权限配置 |
| 404 | 资源不存在 | 检查参数是否正确 |

### C. 性能基准

| 操作 | 平均耗时 | 优化目标 |
|------|---------|---------|
| getPageConfig（缓存命中） | < 5ms | < 3ms |
| getPageConfig（缓存未命中） | < 50ms | < 30ms |
| saveWithVersion | < 100ms | < 80ms |
| getUnionDict | < 20ms | < 15ms |

### D. 相关文档

- [RuoYi-WMS 官方文档](https://docs-ruoyi.com/)
- [Spring Boot 3.x 官方文档](https://spring.io/projects/spring-boot)
- [MyBatis Plus 官方文档](https://baomidou.com/)
- [Sa-Token 官方文档](https://sa-token.cc/)

---

## 更新日志

| 版本 | 日期 | 更新内容 | 作者 |
|------|------|---------|------|
| v3.0 | 2026-03-30 | 全面审计并更新架构文档 | JMH |
| v2.0 | 2026-03-25 | 添加 SqlBuilder 架构升级 | JMH |
| v1.0 | 2026-03-22 | 初始版本 | JMH |

---

**文档状态**: ✅ 已完成  
**下次审查日期**: 2026-04-30
