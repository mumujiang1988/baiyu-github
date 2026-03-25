# ERP 配置化方案 - 后端开发指南

> 📅 **版本**: v1.0  
> 🎯 **目标**: 提供完整的后端开发指导，包括设计理念、文件结构、使用方式、配置方式和 API 二开方法  
> 📦 **适用范围**: RuoYi-WMS + Spring Boot 3.x + MyBatis Plus  
> 🕐 **创建时间**: 2026-03-23  
> 👥 **目标读者**: 后端开发工程师、系统架构师

---

## 📋 目录

1. [设计理念](#设计理念)
2. [文件结构](#文件结构)
3. [核心引擎](#核心引擎)
4. [使用方式](#使用方式)
5. [配置方式](#配置方式)
6. [API 二开方法](#api-二开方法)
7. [最佳实践](#最佳实践)

---

## 🎨 设计理念

### 核心理念

**配置驱动开发（Configuration-Driven Development）**

```
┌─────────────────────────────────────────┐
│          业务人员配置 JSON               │
│    (无需编写代码，可视化配置)            │
└──────────────┬──────────────────────────┘
               ↓
┌─────────────────────────────────────────┐
│          四大核心引擎解析                │
│   查询 + 验证 + 审批 + 下推              │
└──────────────┬──────────────────────────┘
               ↓
┌─────────────────────────────────────────┐
│        自动生成业务逻辑                  │
│      (90% 标准功能零代码开发)            │
└─────────────────────────────────────────┘
```

### 六大设计原则

| 原则 | 说明 | 实现方式 |
|------|------|---------|
| **单一职责** | 每个类只负责一个功能 | 接口分离、服务分层 |
| **开闭原则** | 对扩展开放，对修改关闭 | 抽象基类 + 策略模式 |
| **里氏替换** | 子类可替换父类 | 继承统一基类 |
| **接口隔离** | 使用多个专用接口 | 细粒度接口设计 |
| **依赖倒置** | 依赖抽象不依赖具体 | 面向接口编程 |
| **最少知识** | 减少模块间耦合 | 迪米特法则 |

### 技术栈

- **框架**: Spring Boot 3.x
- **ORM**: MyBatis Plus 3.5.6
- **数据库**: MySQL 8.0+
- **缓存**: Redis
- **权限**: Sa-Token
- **工具**: Jackson, Lombok, Mapstruct

---

## 📁 文件结构

### 完整目录结构

```
ruoyi-system/src/main/java/com/ruoyi/system/
│
├── controller/erp/                      # ERP 配置化 Controller 层
│   ├── ErpEngineController.java         # ✨ 四大引擎统一入口（核心）
│   ├── ErpPageConfigController.java     # 页面配置管理
│   ├── ErpApprovalFlowController.java   # 审批流程管理
│   └── ErpPushRelationController.java   # 下推关系管理
│
├── service/                             # Service 层
│   ├── engine/                          # 🔧 核心引擎组件
│   │   ├── DynamicQueryEngine.java      # 动态查询引擎
│   │   ├── FormValidationEngine.java    # 表单验证引擎
│   │   ├── ApprovalWorkflowEngine.java  # 审批流程引擎
│   │   └── PushDownEngine.java          # 下推引擎
│   │
│   ├── ISuperDataPermissionService.java # 通用数据权限查询接口
│   ├── ErpPageConfigService.java        # 页面配置服务
│   ├── ErpApprovalFlowService.java      # 审批流程服务
│   └── ErpPushRelationService.java      # 下推关系服务
│
├── service/impl/                        # Service 实现层
│   ├── SuperDataPermissionServiceImpl.java  # 通用查询实现
│   ├── ErpPageConfigServiceImpl.java        # 配置服务实现
│   ├── ErpApprovalFlowServiceImpl.java      # 审批服务实现
│   └── ErpPushRelationServiceImpl.java      # 下推服务实现
│
├── mapper/                              # Mapper 层
│   ├── ErpPageConfigMapper.java
│   ├── ErpPageConfigHistoryMapper.java
│   ├── ErpPushRelationMapper.java
│   ├── ErpApprovalFlowMapper.java
│   └── ErpApprovalHistoryMapper.java
│
└── domain/                              # 实体类层
    ├── entity/                          # 实体类
    │   ├── ErpPageConfig.java
    │   ├── ErpPageConfigHistory.java
    │   ├── ErpPushRelation.java
    │   ├── ErpApprovalFlow.java
    │   └── ErpApprovalHistory.java
    ├── bo/                              # 业务对象
    │   ├── ErpPageConfigBo.java
    │   ├── ErpPushRelationBo.java
    │   └── ErpApprovalFlowBo.java
    └── vo/                              # 视图对象
        ├── ErpPageConfigVo.java
        ├── ErpPushRelationVo.java
        └── ErpApprovalFlowVo.java
```

### 核心文件说明

#### 1️⃣ ErpEngineController.java (核心中的核心)

**位置**: `controller/erp/ErpEngineController.java`

**作用**: 四大引擎的统一入口，所有配置化请求都通过此 Controller 分发

**主要接口**:

```java
@RestController
@RequestMapping("/erp/engine")
public class ErpEngineController extends BaseController {
    
    // ========== 动态查询引擎 =========
    @PostMapping("/query/execute")
    public R<?> executeDynamicQuery(@RequestBody Map<String, Object> params)
    
    // ========== 表单验证引擎 =========
    @PostMapping("/validation/execute")
    public R<?> executeFormValidation(@RequestBody Map<String, Object> params)
    
    // ========== 审批流程引擎 =========
    @PostMapping("/approval/execute")
    public R<?> executeApproval(@RequestBody Map<String, Object> params)
    @PostMapping("/approval/check-permission")
    public R<?> checkApprovalPermission(@RequestBody Map<String, Object> params)
    @GetMapping("/approval/history")
    public R<?> getApprovalHistory(@RequestParam Map<String, String> params)
    @PostMapping("/approval/transfer")
    public R<?> transferApproval(@RequestBody Map<String, Object> params)
    @PostMapping("/approval/withdraw")
    public R<?> withdrawApproval(@RequestBody Map<String, Object> params)
    
    // ========== 下推引擎 =========
    @PostMapping("/push/execute")
    public R<?> executePushDown(@RequestBody Map<String, Object> params)
    @PostMapping("/push/preview")
    public R<?> previewPushDown(@RequestBody Map<String, Object> params)
    @PostMapping("/push/batch")
    public R<?> batchPushDown(@RequestBody Map<String, Object> params)
    @PostMapping("/push/validate")
    public R<?> validatePushData(@RequestBody Map<String, Object> params)
}
```

#### 2️⃣ 四大引擎组件

**位置**: `service/engine/`

| 引擎 | 文件名 | 核心功能 |
|------|--------|---------|
| **动态查询** | `DynamicQueryEngine.java` | 根据 JSON 配置构建 QueryWrapper |
| **表单验证** | `FormValidationEngine.java` | 根据规则验证表单数据 |
| **审批流程** | `ApprovalWorkflowEngine.java` | 执行审批流程、权限检查 |
| **下推引擎** | `PushDownEngine.java` | 字段映射、数据转换 |

#### 3️⃣ 通用查询服务

**位置**: `service/ISuperDataPermissionService.java` + `service/impl/SuperDataPermissionServiceImpl.java`

**作用**: 支持根据 moduleCode 动态查询不同表的数据

**核心方法**:

```java
public interface ISuperDataPermissionService {
    /**
     * 根据模块编码分页查询
     */
    Page<Map<String, Object>> selectPageByModule(
        String moduleCode,
        PageQuery pageQuery,
        QueryWrapper<Object> queryWrapper
    );
}
```

---

## 🔧 核心引擎

### 一、动态查询引擎

#### 功能描述

根据 JSON 配置动态生成查询条件，支持 11 种查询操作符和字段白名单校验。

#### 支持的查询类型

```javascript
['like', 'left_like', 'right_like', 'in', 'between', 
 'gt', 'ge', 'lt', 'le', 'ne', 'eq']
```

#### 核心代码示例

```java
@Component
public class DynamicQueryEngine {
    
    private static final Set<String> ALLOWED_FIELDS = Set.of(
        "fbillNo", "fDocumentStatus", "fBillAmount", "fdate",
        "fCustomerNumber", "fCustomerName", "id", "createTime"
    );
    
    /**
     * 构建查询条件
     */
    public <T> QueryWrapper<T> buildQueryConditions(
            QueryWrapper<T> queryWrapper,
            Map<String, Object> searchConfig,
            Map<String, Object> queryParams) {
        
        List<Map<String, Object>> fields = 
            (List<Map<String, Object>>) searchConfig.get("fields");
        
        for (Map<String, Object> fieldConfig : fields) {
            String field = (String) fieldConfig.get("field");
            String searchType = (String) fieldConfig.get("searchType");
            
            // ✅ 字段白名单校验（防 SQL 注入）
            if (!isValidField(field)) {
                log.warn("非法字段访问尝试：{}", field);
                continue;
            }
            
            Object value = queryParams.get(field);
            
            // 根据搜索类型构建查询条件
            switch (searchType) {
                case "like":
                    queryWrapper.like(field, value.toString());
                    break;
                case "between":
                    handleBetweenCondition(queryWrapper, field, value);
                    break;
                // ... 其他类型
            }
        }
        
        return queryWrapper;
    }
}
```

---

### 二、表单验证引擎

#### 功能描述

根据 JSON 配置执行表单数据验证，支持多种验证规则和自定义验证器。

#### 支持的验证规则

```javascript
['required', 'email', 'phone', 'number', 'integer',
 'min', 'max', 'minLength', 'maxLength', 'pattern', 'range']
```

#### 核心代码示例

```java
@Component
public class FormValidationEngine {
    
    /**
     * 验证表单数据
     */
    public ValidationResult validate(Map<String, Object> formConfig, 
                                     Map<String, Object> formData) {
        ValidationResult result = new ValidationResult();
        result.setValid(true);
        
        List<?> sections = (List<?>) formConfig.get("sections");
        
        for (Object section : sections) {
            List<?> fields = ((Map<?, ?>) section).get("fields");
            
            for (Object fieldObj : fields) {
                Map<?, ?> field = (Map<?, ?>) fieldObj;
                String fieldName = (String) field.get("field");
                Object fieldValue = formData.get(fieldName);
                
                List<?> rules = (List<?>) field.get("rules");
                
                for (Object ruleObj : rules) {
                    Map<?, ?> rule = (Map<?, ?>) ruleObj;
                    String ruleType = (String) rule.get("type");
                    
                    ValidationError error = validateRule(
                        fieldName, fieldValue, rule
                    );
                    
                    if (error != null) {
                        result.addError(error);
                        result.setValid(false);
                    }
                }
            }
        }
        
        return result;
    }
}
```

---

### 三、审批流程引擎

#### 功能描述

基于配置的审批流程，自动判断当前审批步骤、执行审批操作和权限检查。

#### 审批动作

```javascript
['APPROVE', 'AUDIT', 'REJECT', 'WITHDRAW', 'TRANSFER']
```

#### 核心代码示例

```java
@Component
public class ApprovalWorkflowEngine {
    
    /**
     * 执行审批操作
     */
    public ApprovalResult executeApproval(ApprovalContext context, 
                                          List<Map<String, Object>> workflow,
                                          Map<String, Object> billData) {
        ApprovalResult result = new ApprovalResult();
        
        // 1. 获取当前审批步骤
        ApprovalStep currentStep = getCurrentStep(workflow, billData);
        
        // 2. 检查用户权限
        if (!canUserAudit(currentStep, context.getUserId(), context.getUserRoles())) {
            result.setSuccess(false);
            result.setMessage("无审批权限");
            return result;
        }
        
        // 3. 根据审批动作处理
        switch (context.getAction()) {
            case "APPROVE":
            case "AUDIT":
                result.setSuccess(true);
                result.setMessage("审批通过");
                result.setNextStep(getNextStep(workflow, currentStep));
                break;
            case "REJECT":
                result.setSuccess(true);
                result.setMessage("已驳回");
                result.setRejected(true);
                break;
        }
        
        return result;
    }
}
```

---

### 四、下推引擎

#### 功能描述

基于配置的映射关系，将源单据数据转换为下游单据数据。

#### 核心代码示例

```java
@Component
public class PushDownEngine {
    
    /**
     * 执行下推操作
     */
    public PushResult executePush(Map<String, Object> pushConfig,
                                  Map<String, Object> sourceData,
                                  String targetModule,
                                  Map<String, Object> confirmData) {
        PushResult result = new PushResult();
        
        // 1. 字段映射
        Map<String, Object> mappedData = mapFields(sourceData, pushConfig);
        
        // 2. 数据转换
        Map<String, Object> transformedData = transformData(mappedData, pushConfig);
        
        // 3. 应用默认值
        Map<String, Object> finalData = applyDefaults(transformedData, pushConfig);
        
        // 4. 合并确认数据
        if (confirmData != null) {
            finalData.putAll(confirmData);
        }
        
        // 5. 数据验证
        ValidationResult validation = validateData(finalData, pushConfig);
        if (!validation.isValid()) {
            result.setSuccess(false);
            result.setMessage("数据验证失败：" + validation.getErrorMessage());
            return result;
        }
        
        // 6. 保存目标单
        Object savedTarget = saveTargetData(targetModule, finalData);
        
        result.setSuccess(true);
        result.setMessage("下推成功");
        result.setData(savedTarget);
        
        return result;
    }
}
```

---

## 💻 使用方式

### 快速开始

#### 步骤 1: 配置数据库表

确保以下配置表已创建并初始化：

- `erp_page_config` - 页面配置表
- `erp_approval_flow` - 审批流程表
- `erp_push_relation` - 下推关系表
- `erp_approval_history` - 审批历史表

#### 步骤 2: 在配置管理后台添加配置

访问：`http://localhost:8080/erp/config`

新增页面配置，填写 JSON 内容：

```json
{
  "page": {
    "title": "销售订单",
    "icon": "Document"
  },
  "search": {
    "showSearch": true,
    "fields": [
      {
        "field": "fbillNo",
        "label": "单据编号",
        "component": "input",
        "searchType": "like"
      }
    ]
  },
  "table": {
    "rowKey": "id",
    "columns": [
      {
        "field": "fbillNo",
        "label": "单据编号",
        "width": 180
      }
    ]
  }
}
```

#### 步骤 3: 调用引擎接口

前端调用示例：

```javascript
// 执行动态查询
const response = await executeDynamicQuery({
  moduleCode: 'saleOrder',
  searchConfig: config.search,
  queryParams: {
    fbillNo: 'SO20240323001'
  },
  pageNum: 1,
  pageSize: 10
});
```

---

## ⚙️ 配置方式

### 一、页面配置 (erp_page_config)

#### 配置结构

```json
{
  "page": {
    "title": "页面标题",
    "icon": "图标名称"
  },
  "search": {
    "showSearch": true,
    "fields": [/* 查询字段配置 */]
  },
  "table": {
    "rowKey": "id",
    "border": true,
    "stripe": true,
    "columns": [/* 表格列配置 */]
  },
  "toolbar": {
    "actions": [/* 工具栏按钮配置 */]
  },
  "form": {
    "sections": [/* 表单字段配置 */]
  }
}
```

#### 查询字段配置

```json
{
  "field": "字段名",
  "label": "显示标签",
  "component": "组件类型 (input/select/daterange)",
  "searchType": "查询类型 (like/in/between)",
  "dictionary": "字典类型 (可选)",
  "props": {
    "placeholder": "请输入",
    "clearable": true,
    "style": {"width": "200px"}
  }
}
```

### 二、审批流程配置 (erp_approval_flow)

#### 配置结构

```json
{
  "flowName": "销售订单审批流程",
  "workflow": [
    {
      "step": 1,
      "role": "department_manager",
      "action": "audit",
      "condition": "amount > 10000",
      "allowTransfer": true,
      "allowWithdraw": true
    },
    {
      "step": 2,
      "role": "general_manager",
      "action": "approve",
      "condition": "amount > 50000"
    }
  ]
}
```

### 三、下推关系配置 (erp_push_relation)

#### 配置结构

```json
{
  "relationName": "销售订单 → 发货通知单",
  "mappingRules": {
    "fieldMapping": {
      "fbillNo": "fsourceBillNo",
      "fCustomerNumber": "fCustomerNumber",
      "fBillAmount": "fTotalAmount"
    },
    "defaultValue": {
      "fDocumentStatus": "暂存"
    },
    "transformation": {
      "fdate": "now()"
    }
  }
}
```

---

## 🔌 API 二开方法

### 一、新增自定义引擎

#### 场景：需要添加计算引擎

#### 步骤 1: 创建引擎类

```java
package com.ruoyi.system.service.engine;

@Component
@Slf4j
public class CalculationEngine {
    
    /**
     * 执行计算
     */
    public Map<String, Object> calculate(String expression, 
                                         Map<String, Object> context) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 实现计算逻辑
            Object calcResult = evaluateExpression(expression, context);
            
            result.put("success", true);
            result.put("data", calcResult);
            
        } catch (Exception e) {
            log.error("计算失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return result;
    }
}
```

#### 步骤 2: 在 Controller 中添加接口

```java
@RestController
@RequestMapping("/erp/engine")
public class ErpEngineController {
    
    private final CalculationEngine calculationEngine;
    
    @PostMapping("/calculation/execute")
    public R<?> executeCalculation(@RequestBody Map<String, Object> params) {
        try {
            String expression = (String) params.get("expression");
            Map<String, Object> context = 
                (Map<String, Object>) params.get("context");
            
            Map<String, Object> result = 
                calculationEngine.calculate(expression, context);
            
            return R.ok(result);
        } catch (Exception e) {
            return R.fail("计算失败：" + e.getMessage());
        }
    }
}
```

### 二、扩展现有引擎

#### 场景：为查询引擎添加新的查询类型

#### 步骤 1: 修改 DynamicQueryEngine

```java
switch (searchType) {
    case "like":
        queryWrapper.like(field, value.toString());
        break;
    
    // ✅ 新增：模糊匹配（前后都模糊）
    case "fuzzy_like":
        queryWrapper.like(field, "%" + value.toString() + "%");
        break;
    
    // ✅ 新增：正则匹配
    case "regex":
        queryWrapper.apply(field + " REGEXP ?", value.toString());
        break;
    
    default:
        queryWrapper.eq(field, value);
}
```

#### 步骤 2: 更新前端配置

```json
{
  "field": "fRemark",
  "label": "备注",
  "component": "input",
  "searchType": "fuzzy_like"  // ✅ 使用新的查询类型
}
```

### 三、自定义验证规则

#### 步骤 1: 创建验证器

```java
@Component
public class CustomValidator implements ConstraintValidator<CustomValid, String> {
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // 实现自定义验证逻辑
        return value != null && value.matches("^[A-Z]{3}\\d+$");
    }
}
```

#### 步骤 2: 在 FormValidationEngine 中使用

```java
private ValidationError validateRule(String fieldName, 
                                    Object fieldValue, 
                                    Map<?, ?> rule) {
    String ruleType = (String) rule.get("type");
    
    switch (ruleType) {
        case "custom_code":
            if (!isValidCustomCode(fieldValue.toString())) {
                return new ValidationError(fieldName, "编码格式不正确");
            }
            break;
    }
    
    return null;
}
```

### 四、集成外部服务

#### 场景：调用金蝶 K3 Cloud API

#### 步骤 1: 创建外部服务接口

```java
@Service
@RequiredArgsConstructor
public class KingdeeCloudService {
    
    /**
     * 同步数据到金蝶云
     */
    public SyncResult syncToKingdee(String moduleCode, Map<String, Object> data) {
        // 调用金蝶 K3 Cloud WebAPI
        // ...
        return new SyncResult(true, "同步成功");
    }
}
```

#### 步骤 2: 在下推引擎中集成

```java
public PushResult executePush(...) {
    // ... 原有逻辑
    
    // ✅ 同步到金蝶云
    if (needSyncToKingdee) {
        kingdeeCloudService.syncToKingdee(targetModule, finalData);
    }
    
    return result;
}
```

---

## 📊 最佳实践

### 1. 配置管理规范

✅ **推荐做法**:
- 使用版本控制（每次修改填写变更原因）
- 定期查看历史版本
- 重要修改前先备份配置
- 使用公共配置（`is_public=1`）共享配置

❌ **不推荐做法**:
- 直接修改数据库（使用配置管理后台）
- 不填写变更原因
- 跳过测试直接上线

### 2. 性能优化

✅ **Redis 缓存**:
```java
// 配置缓存 TTL: 1 小时
CacheUtils.put(CacheNames.ERP_CONFIG, moduleCode, content, 3600);

// 更新时清除缓存
CacheUtils.evict(CacheNames.ERP_CONFIG, config.getModuleCode());
```

✅ **数据库索引**:
```sql
-- 模块编码 + 配置类型唯一索引
UNIQUE KEY `uk_module_type` (`module_code`, `config_type`)

-- 查询优化索引
KEY `idx_status` (`status`)
KEY `idx_module_version` (`module_code`, `version`)
```

### 3. 安全加固

✅ **字段白名单**:
```java
private static final Set<String> ALLOWED_FIELDS = Set.of(
    "fbillNo", "fDocumentStatus", "fBillAmount", ...
);

if (!isValidField(field)) {
    log.warn("非法字段访问尝试：{}", field);
    continue;
}
```

✅ **权限控制**:
```java
@SaCheckPermission("erp:config:list")
@SaCheckPermission("erp:config:add")
@SaCheckPermission("erp:config:edit")
@SaCheckPermission("erp:config:remove")
```

✅ **事务控制**:
```java
@Transactional(rollbackFor = Exception.class)
public int updateByBo(ErpPageConfigBo bo) {
    // ...
}
```

### 4. 代码质量

✅ **代码审查清单**:
- [ ] 所有接口都有权限控制
- [ ] 所有写操作都有事务控制
- [ ] 所有 SQL 都有防注入措施
- [ ] 所有异常都有日志记录
- [ ] 所有缓存都有 TTL
- [ ] 所有配置都有版本控制

---

## 🎯 总结

### 核心优势

✅ **高复用** - 通用基类提供 80% 标准功能  
✅ **少冗余** - 避免重复代码，DRY 原则  
✅ **易扩展** - 开闭原则，新功能无需改旧代码  
✅ **配置化** - 业务逻辑可通过配置调整  
✅ **标准化** - 统一的接口规范和数据结构  

### 适用场景

✅ **适合配置化的场景**:
- CRUD 业务页面（销售订单、采购订单等）
- 单据管理页面（入库单、出库单等）
- 报表查询页面（库存明细、销售统计等）
- 基础资料维护（客户、供应商、物料等）

❌ **不适合配置化的场景**:
- 复杂业务逻辑（需要大量自定义代码）
- 特殊 UI 需求（高度定制化界面）
- 性能敏感场景（需要极致优化）

---

## 📚 相关文档

- [ERP 公共配置表-SQL 建表脚本](../落地 sql/ERP 公共配置表-SQL 建表脚本 2026-03-22.sql)
- [ERP 配置化前端架构设计方案](./ERP 配置化前端架构设计方案.md)
- [ERP 配置化方案 - 待实现功能清单](./ERP 配置化方案 - 待实现功能清单.md)
- [RuoYi通用配置化后端接口设计方案](./RuoYi通用配置化后端接口设计方案.md)

---

**文档版本**: v1.0  
**创建时间**: 2026-03-23  
**作者**: ERP 研发团队  
**最后更新**: 2026-03-23  
**审核状态**: 已审核 ✅
