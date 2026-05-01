# ERP低代码配置化模块 - 完整技术文档

**模块名称**: ERP智能配置引擎（ERP Low-Code Configurable Module）  
**版本**: v4.0  
**创建时间**: 2026-03-22  
**最后更新**: 2026-04-28  
**技术栈**: Spring Boot + MyBatis-Plus + JdbcTemplate + Vue3 + Element Plus

---

## 一、模块概述

### 1.1 功能简介

ERP低代码配置化模块是基于JSON配置驱动的动态页面生成引擎，通过配置化的方式实现ERP业务页面的快速构建和灵活调整，无需编写前端代码即可实现：

1. **动态页面生成**: 基于JSON配置自动生成列表、表单、详情页
2. **智能查询引擎**: 支持复杂查询条件动态构建
3. **字典翻译引擎**: 自动将字典编码转换为显示文本
4. **虚拟字段计算**: 支持运行时动态计算字段值
5. **审批流程引擎**: 内置工作流审批功能
6. **下推关系管理**: 实现单据之间的数据流转
7. **数据权限控制**: 细粒度的字段级权限管理
8. **缓存优化机制**: Redis缓存提升性能

### 1.2 核心特性

- ✅ **纯配置驱动**: 所有页面行为通过JSON配置控制
- ✅ **9字段强制拆分**: page/form/table/search/action/api/dict/business/detail
- ✅ **JDBC低代码架构**: 动态SQL构建，无硬编码
- ✅ **版本控制**: 配置变更历史追踪
- ✅ **权限集成**: 与若依权限体系无缝对接
- ✅ **高性能**: Redis缓存 + SQL优化

---

## 二、目录结构

### 2.1 后端目录

```
backend/By-middleground-web/ruoyi-modules/ruoyi-erp-api/src/main/java/com/ruoyi/erp/
├── controller/
│   ├── ErpCacheController.java              # 缓存管理控制器
│   ├── ErpDictionaryController.java         # 字典管理控制器
│   ├── base/
│   │   └── BaseErpEngineController.java     # 引擎基类控制器
│   └── erp/
│       ├── ErpApprovalController.java       # 审批引擎控制器
│       ├── ErpApprovalFlowController.java   # 审批流程配置控制器
│       ├── ErpPageConfigController.java     # 页面配置CRUD控制器
│       ├── ErpPushController.java           # 下推引擎控制器
│       ├── ErpPushRelationController.java   # 下推关系配置控制器
│       ├── ErpQueryController.java          # 动态查询引擎控制器
│       ├── ErpRelationController.java       # 单据关系控制器
│       └── ErpValidationController.java     # 表单验证引擎控制器
├── domain/
│   ├── bo/
│   │   ├── ErpApprovalFlowBo.java           # 审批流程BO
│   │   ├── ErpApprovalHistoryBo.java        # 审批历史BO
│   │   ├── ErpPageConfigBo.java             # 页面配置BO
│   │   ├── ErpPageConfigHistoryBo.java      # 配置历史BO
│   │   └── ErpPushRelationBo.java           # 下推关系BO
│   ├── entity/
│   │   ├── ErpApprovalFlow.java             # 审批流程实体
│   │   ├── ErpApprovalHistory.java          # 审批历史实体
│   │   ├── ErpPageConfig.java               # 页面配置实体
│   │   ├── ErpPageConfigHistory.java        # 配置历史实体
│   │   └── ErpPushRelation.java             # 下推关系实体
│   └── response/
│       └── ErpResponse.java                 # 统一响应封装
├── event/
│   └── ConfigRefreshEvent.java              # 配置刷新事件
├── exception/
│   ├── ComputedFieldException.java          # 计算字段异常
│   ├── ErpConfigException.java              # 配置异常
│   ├── ErpConfigExceptionHandler.java       # 全局异常处理器
│   └── VirtualFieldException.java           # 虚拟字段异常
├── listener/
│   └── ConfigRefreshListener.java           # 配置刷新监听器
├── mapper/
│   ├── ErpApprovalFlowMapper.java           # 审批流程Mapper
│   ├── ErpApprovalHistoryMapper.java        # 审批历史Mapper
│   ├── ErpDictionaryMapper.java             # 字典Mapper
│   ├── ErpPageConfigHistoryMapper.java      # 配置历史Mapper
│   ├── ErpPageConfigMapper.java             # 页面配置Mapper
│   └── ErpPushRelationMapper.java           # 下推关系Mapper
├── service/
│   ├── engine/                              # 核心引擎层
│   │   ├── ApprovalWorkflowEngine.java      # 审批工作流引擎
│   │   ├── ComputedFieldConfig.java         # 计算字段配置
│   │   ├── ComputedFieldEngine.java         # 计算字段引擎
│   │   ├── DictionaryBuilderEngine.java     # 字典构建引擎
│   │   ├── DictionaryConfig.java            # 字典配置
│   │   ├── DynamicQueryEngine.java          # 动态查询引擎
│   │   ├── FormConfig.java                  # 表单配置
│   │   ├── FormValidationEngine.java        # 表单验证引擎
│   │   ├── PushDownEngine.java              # 下推引擎
│   │   ├── TableColumnConfig.java           # 表格列配置
│   │   ├── VirtualFieldConfig.java          # 虚拟字段配置
│   │   └── VirtualFieldService.java         # 虚拟字段服务
│   ├── impl/
│   │   ├── ErpApprovalFlowServiceImpl.java  # 审批流程服务实现
│   │   ├── ErpApprovalHistoryServiceImpl.java # 审批历史服务实现
│   │   ├── ErpPageConfigServiceImpl.java    # 页面配置服务实现
│   │   ├── ErpPushRelationServiceImpl.java  # 下推关系服务实现
│   │   └── SuperDataPermissionServiceImpl.java # 超级数据权限服务
│   ├── ErpApprovalFlowService.java          # 审批流程服务
│   ├── ErpApprovalHistoryService.java       # 审批历史服务
│   ├── ErpDictionaryService.java            # 字典服务
│   ├── ErpPageConfigService.java            # 页面配置服务
│   ├── ErpPushRelationService.java          # 下推关系服务
│   └── ISuperDataPermissionService.java     # 超级数据权限服务
└── utils/
    ├── ConfigParser.java                    # 配置解析工具
    ├── DataProcessor.java                   # 数据处理工具
    ├── ErpPermissionChecker.java            # 权限检查器
    ├── ErpResponseUtils.java                # 响应工具类
    ├── JdbcResultUtils.java                 # JDBC结果工具
    └── SqlBuilder.java                      # SQL构建器
```

### 2.2 前端目录

```
frontend/src/views/erp/
├── ai/                                      # AI智能工具（独立模块）
│   ├── index.vue
│   ├── AiUsageLog.vue
│   └── components/
├── api/                                     # API接口定义
│   └── erp.js
├── config/                                  # 配置管理中心
│   ├── index.vue                            # 配置列表页
│   └── components/
│       └── JsonEditor.vue                   # JSON编辑器组件
├── pageTemplate/                            # 页面模板
│   └── configurable/
│       └── BusinessConfigurable/            # 业务可配置页面
│           ├── BusinessConfigurable.vue     # 主组件
│           ├── components/                  # 子组件
│           ├── composables/                 # 组合式函数
│           ├── hooks/                       # 自定义Hooks
│           ├── stores/                      # Pinia状态管理
│           └── utils/                       # 工具函数
├── report/                                  # 报表模块（独立）
└── utils/                                   # ERP工具函数
    ├── configLoader.js                      # 配置加载器
    ├── permissionHelper.js                  # 权限助手
    └── ...
```

### 2.3 数据库脚本目录

```
erpDOC/sql/erp/
├── 0.erp表结构初始化.sql                     # 核心表结构（9字段拆分）
├── 1.配置页菜单脚本_优化版.sql               # 菜单初始化
├── 2.字典类型补齐.sql                        # 字典数据
├── 3.单据关系图初始化脚本.sql                # 单据关系
├── 5.单据关系图-存储过程版.sql               # 关系查询（存储过程）
├── 5.单据关系图-直接SQL查询.sql              # 关系查询（直接SQL）
├── 8.统一业务配置表排序规则.sql              # 字符集优化
├── dicapi.json                               # 字典API数据
├── *.sql                                     # 各业务单据初始化配置
└── *.md                                      # 相关说明文档
```

---

## 三、数据库设计

### 3.1 核心表结构

#### erp_page_config（页面配置表）

**设计理念**: 9字段强制拆分，支持配置复用和高性能查询

| 字段 | 类型 | 说明 |
|------|------|------|
| config_id | BIGINT | 主键ID |
| module_code | VARCHAR(100) | 模块编码（唯一） |
| config_name | VARCHAR(200) | 配置名称 |
| config_type | VARCHAR(50) | 配置类型（PAGE/FORM/TABLE/DICT/BUSINESS） |
| **page_config** | JSON | 页面基础配置 |
| **form_config** | JSON | 表单UI组件配置 |
| **table_config** | JSON | 表格列配置 |
| **search_config** | JSON | 查询表单配置 |
| **action_config** | JSON | 按钮操作配置 |
| **api_config** | JSON | API接口配置 |
| **dict_config** | JSON | 字典数据源配置 |
| **business_config** | JSON | 业务规则配置 |
| **detail_config** | JSON | 详情页配置 |
| version | INT | 版本号 |
| status | CHAR(1) | 状态（0禁用 1启用） |
| is_public | CHAR(1) | 是否公共配置 |
| parent_config_id | BIGINT | 父配置ID（配置继承） |

**索引**:
- PRIMARY KEY (`config_id`)
- UNIQUE KEY `uk_module_code` (`module_code`)
- KEY `idx_status` (`status`)
- KEY `idx_parent` (`parent_config_id`)

#### erp_page_config_history（配置历史表）

用于版本控制和回滚，结构与主表类似，增加：
- `history_id`: 历史ID
- `change_reason`: 变更原因
- `change_type`: 变更类型（CREATE/UPDATE/DELETE/ROLLBACK）
- `create_time`: 变更时间

#### erp_approval_flow（审批流程表）

| 字段 | 类型 | 说明 |
|------|------|------|
| flow_id | BIGINT | 流程ID |
| module_code | VARCHAR(100) | 模块编码 |
| flow_name | VARCHAR(200) | 流程名称 |
| flow_nodes | JSON | 流程节点配置 |
| status | CHAR(1) | 状态 |

#### erp_push_relation（下推关系表）

| 字段 | 类型 | 说明 |
|------|------|------|
| relation_id | BIGINT | 关系ID |
| source_module | VARCHAR(100) | 源模块 |
| target_module | VARCHAR(100) | 目标模块 |
| mapping_rules | JSON | 字段映射规则 |
| push_conditions | JSON | 下推条件 |

### 3.2 9字段配置详解

#### page_config（页面基础配置）
```json
{
  "title": "销售订单",
  "layout": "vertical",
  "showSearch": true,
  "showPagination": true
}
```

#### form_config（表单配置）
```json
{
  "fields": [
    {
      "prop": "orderNo",
      "label": "订单号",
      "type": "input",
      "required": true
    }
  ]
}
```

#### table_config（表格配置）
```json
{
  "columns": [
    {
      "prop": "orderNo",
      "label": "订单号",
      "width": 150
    }
  ]
}
```

#### search_config（搜索配置）
```json
{
  "fields": [
    {
      "field": "orderNo",
      "label": "订单号",
      "searchType": "like"
    }
  ]
}
```

#### action_config（按钮配置）
```json
{
  "buttons": [
    {
      "code": "add",
      "label": "新增",
      "permission": "erp:sale_order:add"
    }
  ]
}
```

#### api_config（API配置）
```json
{
  "queryUrl": "/erp/engine/query/execute",
  "addUrl": "/erp/engine/push/create",
  "updateUrl": "/erp/engine/push/update"
}
```

#### dict_config（字典配置）
```json
{
  "dictionaries": [
    {
      "field": "orderStatus",
      "dictType": "erp_order_status"
    }
  ]
}
```

#### business_config（业务配置）
```json
{
  "computedFields": [
    {
      "field": "totalAmount",
      "formula": "price * quantity"
    }
  ],
  "validations": [
    {
      "field": "quantity",
      "rule": "greaterThan",
      "value": 0
    }
  ]
}
```

#### detail_config（详情配置）
```json
{
  "sections": [
    {
      "title": "基本信息",
      "fields": ["orderNo", "customerName"]
    }
  ]
}
```

---

## 四、核心功能详解

### 4.1 动态查询引擎

**功能描述**: 根据JSON配置动态构建SQL查询语句

**技术实现**:
- `DynamicQueryEngine`: 核心查询引擎
- `SqlBuilder`: SQL构建器（防注入）
- `ConfigParser`: 配置解析器

**API接口**:
```
POST /erp/engine/query/execute
Body: {
  "moduleCode": "sale_order",
  "tableName": "t_sale_order",
  "queryConfig": {
    "fields": [...],
    "filters": [...]
  },
  "pageNum": 1,
  "pageSize": 10
}
```

**工作流程**:
1. 解析queryConfig配置
2. 构建WHERE条件（参数化查询）
3. 应用数据权限过滤
4. 执行JDBC查询
5. 字典翻译和数据加工
6. 返回分页结果

### 4.2 字典翻译引擎

**功能描述**: 自动将字典编码转换为显示文本

**技术实现**:
- `DictionaryBuilderEngine`: 字典构建引擎
- 支持多种数据源（静态字典、API接口、SQL查询）
- 缓存优化（Redis）

**配置示例**:
```json
{
  "dictionaries": [
    {
      "field": "orderStatus",
      "dictType": "erp_order_status",
      "dataSource": "static"
    },
    {
      "field": "customerId",
      "dictType": "customer_list",
      "dataSource": "api",
      "apiUrl": "/erp/dict/customers"
    }
  ]
}
```

### 4.3 虚拟字段计算引擎

**功能描述**: 运行时动态计算字段值（如合计、平均值）

**技术实现**:
- `ComputedFieldEngine`: 计算引擎
- 支持表达式解析（SpEL）
- 依赖字段自动追踪

**配置示例**:
```json
{
  "computedFields": [
    {
      "field": "totalAmount",
      "formula": "price * quantity",
      "dependencies": ["price", "quantity"]
    },
    {
      "field": "avgPrice",
      "formula": "SUM(price) / COUNT(*)",
      "scope": "rowGroup"
    }
  ]
}
```

### 4.4 审批流程引擎

**功能描述**: 内置工作流审批功能

**技术实现**:
- `ApprovalWorkflowEngine`: 审批工作流引擎
- 支持多级审批
- 审批历史记录

**API接口**:
```
POST /erp/engine/approval/submit    # 提交审批
POST /erp/engine/approval/approve   # 审批通过
POST /erp/engine/approval/reject    # 审批拒绝
GET  /erp/engine/approval/history   # 审批历史
```

### 4.5 下推引擎

**功能描述**: 实现单据之间的数据流转（如销售订单→发货通知单）

**技术实现**:
- `PushDownEngine`: 下推引擎
- 字段映射规则
- 数据校验

**API接口**:
```
POST /erp/engine/push/create        # 创建下推单据
GET  /erp/engine/push/relations     # 查询下推关系
```

### 4.6 表单验证引擎

**功能描述**: 基于配置的表单验证

**技术实现**:
- `FormValidationEngine`: 验证引擎
- 支持多种验证规则
- 自定义验证函数

**配置示例**:
```json
{
  "validations": [
    {
      "field": "quantity",
      "rule": "greaterThan",
      "value": 0,
      "message": "数量必须大于0"
    },
    {
      "field": "email",
      "rule": "pattern",
      "pattern": "^[\\w-]+@[\\w-]+\\.[\\w-]+$"
    }
  ]
}
```

---

## 五、技术架构

### 5.1 后端架构

#### 分层设计
```
Controller层 → Service层 → Engine层 → Utils层
```

- **Controller**: RESTful API入口，权限校验
- **Service**: 业务逻辑，事务管理
- **Engine**: 核心引擎（查询、字典、计算、审批、下推）
- **Utils**: 工具类（SQL构建、配置解析、数据处理）

#### 关键技术

**1. JdbcTemplate动态查询**
```java
// 动态构建SQL
StringBuilder sql = new StringBuilder("SELECT * FROM ").append(tableName);
List<Object> params = new ArrayList<>();

// 添加WHERE条件
if (filters != null && !filters.isEmpty()) {
    sql.append(" WHERE ");
    for (int i = 0; i < filters.size(); i++) {
        if (i > 0) sql.append(" AND ");
        sql.append(filters.get(i).getField()).append(" = ?");
        params.add(filters.get(i).getValue());
    }
}

// 执行查询
List<Map<String, Object>> result = jdbcTemplate.queryForList(
    sql.toString(), 
    params.toArray()
);
```

**2. 配置缓存机制**
```java
@Cacheable(value = "erp:config", key = "#moduleCode")
public Map<String, Object> getConfig(String moduleCode) {
    // 从数据库加载配置
}
```

**3. 事件驱动配置刷新**
```java
// 发布配置刷新事件
eventPublisher.publishEvent(new ConfigRefreshEvent(this, moduleCode));

// 监听器清除缓存
@EventListener
public void handleConfigRefresh(ConfigRefreshEvent event) {
    cacheManager.evict("erp:config:" + event.getModuleCode());
}
```

### 5.2 前端架构

#### 组件设计
```
BusinessConfigurable.vue (主组件)
├── SearchArea.vue (搜索区域)
├── TableArea.vue (表格区域)
├── FormDialog.vue (表单弹窗)
├── DetailDrawer.vue (详情抽屉)
└── ActionButtons.vue (操作按钮)
```

#### 状态管理
- **Pinia Store**: 配置状态、数据状态
- **Composables**: 可复用的组合式函数
- **Hooks**: 自定义Hooks（useConfig、useData、usePermission）

#### 配置加载流程
```javascript
// 1. 加载配置
const config = await loadConfig(moduleCode)

// 2. 解析配置
const { pageConfig, tableConfig, searchConfig } = parseConfig(config)

// 3. 渲染页面
renderPage(pageConfig)
renderTable(tableConfig)
renderSearch(searchConfig)
```

---

## 六、API接口清单

### 6.1 配置管理

| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/erp/config/list` | GET | 查询配置列表 | erp:config:list |
| `/erp/config/{id}` | GET | 查询配置详情 | erp:config:query |
| `/erp/config` | POST | 新增配置 | erp:config:add |
| `/erp/config` | PUT | 更新配置 | erp:config:edit |
| `/erp/config/{id}` | DELETE | 删除配置 | erp:config:remove |
| `/erp/config/export` | GET | 导出配置 | erp:config:export |
| `/erp/config/import` | POST | 导入配置 | erp:config:import |

### 6.2 动态查询

| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/erp/engine/query/execute` | POST | 执行动态查询 | erp:{module}:query |

### 6.3 审批管理

| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/erp/engine/approval/submit` | POST | 提交审批 | erp:{module}:audit |
| `/erp/engine/approval/approve` | POST | 审批通过 | erp:{module}:audit |
| `/erp/engine/approval/reject` | POST | 审批拒绝 | erp:{module}:audit |
| `/erp/engine/approval/history` | GET | 审批历史 | erp:{module}:query |

### 6.4 下推管理

| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/erp/engine/push/create` | POST | 创建下推单据 | erp:{module}:push |
| `/erp/engine/push/relations` | GET | 查询下推关系 | erp:{module}:query |

### 6.5 字典管理

| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/erp/dict/list` | GET | 查询字典列表 | erp:dict:list |
| `/erp/dict/{dictType}` | GET | 查询字典数据 | 公开 |

### 6.6 缓存管理

| 接口 | 方法 | 说明 | 权限 |
|------|------|------|------|
| `/erp/cache/clear-all` | DELETE | 清空所有缓存 | erp:cache:clear |
| `/erp/cache/clear/{moduleCode}` | DELETE | 清空模块缓存 | erp:cache:clear |

---

## 七、部署指南

### 7.1 数据库初始化

```bash
# 1. 进入SQL目录
cd d:/baiyu-ali/erpDOC/sql/erp

# 2. 执行核心表结构
mysql -u root -p test < 0.erp表结构初始化.sql

# 3. 执行菜单脚本
mysql -u root -p test < 1.配置页菜单脚本_优化版.sql

# 4. 执行字典数据
mysql -u root -p test < 2.字典类型补齐.sql

# 5. 执行业务单据配置（按需）
mysql -u root -p test < 销售订单初始化配置.sql
mysql -u root -p test < 采购订单初始化配置.sql
# ... 其他单据
```

### 7.2 后端部署

```bash
# 1. 编译项目
cd d:/baiyu-ali/backend/By-middleground-web
mvn clean package -DskipTests

# 2. 启动服务
java -jar ruoyi-admin-wms/target/ruoyi-admin-wms.jar
```

### 7.3 前端部署

```bash
# 1. 安装依赖
cd d:/baiyu-ali/frontend
npm install

# 2. 启动开发服务器
npm run dev

# 3. 生产构建
npm run build:prod
```

---

## 八、开发指南

### 8.1 新增业务模块

**步骤1**: 创建数据库表
```sql
CREATE TABLE t_custom_module (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  field1 VARCHAR(100),
  field2 DECIMAL(10,2),
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

**步骤2**: 创建JSON配置
```json
{
  "pageConfig": {
    "title": "自定义模块",
    "showSearch": true
  },
  "tableConfig": {
    "columns": [
      {"prop": "field1", "label": "字段1"},
      {"prop": "field2", "label": "字段2"}
    ]
  },
  "apiConfig": {
    "queryUrl": "/erp/engine/query/execute"
  }
}
```

**步骤3**: 插入配置到数据库
```sql
INSERT INTO erp_page_config (
  module_code, config_name, config_type,
  page_config, table_config, api_config,
  status, create_by
) VALUES (
  'custom_module', '自定义模块', 'PAGE',
  '{...}', '{...}', '{...}',
  '1', 'admin'
);
```

**步骤4**: 配置菜单权限
```sql
INSERT INTO sys_menu (...) VALUES (...);
```

**步骤5**: 前端访问
```
http://localhost/erp/pageTemplate/configurable/BusinessConfigurable?moduleCode=custom_module
```

### 8.2 自定义验证规则

```javascript
// 在business_config中添加自定义验证
{
  "validations": [
    {
      "field": "customField",
      "rule": "custom",
      "validator": "validateCustomField"
    }
  ]
}

// 在前端实现验证函数
function validateCustomField(value) {
  if (value < 0) {
    return '自定义字段不能为负数'
  }
  return true
}
```

---

## 九、性能优化

### 9.1 缓存策略

- **配置缓存**: Redis缓存，TTL 24小时
- **字典缓存**: 本地缓存 + Redis二级缓存
- **查询缓存**: 根据查询条件生成缓存Key

### 9.2 SQL优化

- **参数化查询**: 防止SQL注入
- **索引优化**: 常用查询字段建立索引
- **分页优化**: LIMIT OFFSET避免全表扫描

### 9.3 前端优化

- **懒加载**: 路由和组件懒加载
- **虚拟滚动**: 大数据量表格优化
- **防抖节流**: 搜索和滚动事件优化

---

## 十、常见问题

### Q1: 配置修改后不生效？
**A**: 清理缓存后重试
```bash
DELETE /erp/cache/clear/{moduleCode}
```

### Q2: 字典翻译不显示？
**A**: 检查dict_config配置是否正确，确认字典数据已初始化

### Q3: 查询速度慢？
**A**: 
1. 检查是否有合适的索引
2. 查看执行计划（EXPLAIN）
3. 启用查询缓存

### Q4: 如何调试配置？
**A**: 
1. 使用JsonEditor查看配置
2. 浏览器控制台查看网络请求
3. 后端日志查看SQL执行情况

---

## 十一、版本历史

### v4.0 (2026-03-28) - 9字段强制拆分

**重大变更**:
- ✅ 从单字段config_content拆分为9个独立JSON字段
- ✅ 新增search_config、action_config、api_config
- ✅ 完全重构数据库表结构
- ✅ 不向后兼容旧版本

**新增功能**:
- ✅ 动态查询引擎
- ✅ 字典翻译引擎
- ✅ 虚拟字段计算
- ✅ 审批流程引擎
- ✅ 下推引擎
- ✅ 表单验证引擎

**性能优化**:
- ✅ Redis缓存机制
- ✅ SQL参数化查询
- ✅ 配置懒加载

### v3.x (2026-03-22) - 初始版本

- ✅ 基础配置管理
- ✅ 单字段JSON配置
- ✅ 简单查询功能

---

## 十二、相关文档

- [AI模块完整指南](../sql/ai/README.md)
- [报表模块文档](./report/)
- [数据库表名前缀一致性审计报告](./数据库表名前缀一致性审计报告.md)
- [排序规则冲突修复说明](./排序规则冲突修复说明_v4.2.md)
- [图标优化说明](./图标优化说明_v4.4.md)

---

## 十三、技术支持

**开发团队**: ERP研发团队  
**问题反馈**: 提交Issue或联系技术人员  
**文档维护**: 随模块迭代同步更新

---

**最后更新**: 2026-04-28  
**文档版本**: v4.0  
**维护状态**: ✅ 活跃维护
