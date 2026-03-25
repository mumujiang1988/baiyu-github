# ERP 配置化 API 模块

## 模块说明

本模块是 ERP 配置化方案的核心后端模块，提供以下功能：

- **动态查询引擎**: 根据 JSON 配置生成查询条件
- **表单验证引擎**: 动态字段验证
- **审批流程引擎**: 工作流审批
- **下推引擎**: 单据下推功能
- **页面配置管理**: 动态页面配置
- **通用数据权限**: 支持配置化页面的数据权限控制

## 技术栈

- Spring Boot 3.x
- MyBatis Plus 3.5.6
- Sa-Token 1.37.0
- RuoYi-WMS 3.6.3

## 包结构

`
com.ruoyi.erp
 controller.erp     # Controller 层
 service            # Service 接口
    engine         # 引擎组件
    impl           # Service 实现
 mapper             # Mapper 接口
 domain             # Domain 对象
     entity         # 实体类
     bo             # 业务对象
     vo             # 视图对象
`

## 主要组件

### Controller (4 个)

- ErpEngineController - 核心引擎控制器（20+ 接口）
- ErpPageConfigController - 页面配置控制器
- ErpApprovalFlowController - 审批流程控制器
- ErpPushRelationController - 下推关系控制器

### 引擎组件 (4 个)

- DynamicQueryEngine - 动态查询引擎
- FormValidationEngine - 表单验证引擎
- ApprovalWorkflowEngine - 审批流程引擎
- PushDownEngine - 下推引擎

### Service (8 个)

- ErpApprovalFlowService
- ErpPageConfigService
- ErpPushRelationService
- ISuperDataPermissionService
- 以及对应的实现类

## API 接口

### 核心接口（/erp/engine/*）

- POST /query/execute - 执行动态查询
- POST /validation/execute - 执行表单验证
- POST /approval/execute - 执行审批
- POST /push/execute - 执行下推
- GET /dictionary/{dictType} - 获取字典数据
- GET /custom/entry - 获取明细数据
- GET /custom/cost - 获取成本数据

### 配置管理接口（/erp/config/*）

- CRUD 操作页面配置
- 版本历史管理

### 审批流程接口（/erp/approval/*）

- CRUD 操作审批流程定义

### 下推关系接口（/erp/push-relation/*）

- CRUD 操作下推映射关系

## 数据库表

### 配置表

- erp_page_config - 页面配置表
- erp_page_config_history - 配置历史表
- erp_approval_flow - 审批流程表
- erp_push_relation - 下推关系表
- erp_approval_history - 审批历史表

## 依赖说明

### Maven 依赖

所有依赖通过父 POM 管理，无需单独指定版本。

### 内部依赖

- ruoyi-common-core
- ruoyi-common-security
- ruoyi-common-log
- ruoyi-common-web
- ruoyi-common-mybatis

## 使用方式

在需要使用此模块的项目的 pom.xml 中添加：

`xml
<dependency>
    <groupId>com.ruoyi</groupId>
    <artifactId>ruoyi-erp-api</artifactId>
    <version></version>
</dependency>
`

## 配置文件

application.yml 中需要添加：

`yaml
mybatis-plus:
  mapper-locations: |
    classpath*:/mapper/**/*Mapper.xml
    classpath*:/erp/mapper/**/*Mapper.xml
  typeAliasesPackage: com.ruoyi.**.domain,com.ruoyi.erp.domain
`

## 迁移记录

- **迁移日期**: 2026-03-24
- **来源模块**: ruoyi-system
- **迁移文件数**: 54 个 Java 文件
- **包名重构**: com.ruoyi.system.*  com.ruoyi.erp.*

## 待办事项

1.  SuperDataPermissionServiceImpl 需补充 getRelatedData() 方法
2.  ErpEngineController 需补充字典数据接口
3.  ErpEngineController 需补充 custom/entry 接口
4.  ErpEngineController 需补充 custom/cost 接口

## 作者

ERP Development Team

## 版本

v1.0 - 2026-03-24
