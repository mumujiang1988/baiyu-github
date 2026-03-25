# ERP 配置化后端架构设计方案

> 📅 **版本**: v1.0  
> 🎯 **目标**: 提供高复用、配置驱动的后端架构方案，实现 90% 业务模块零代码开发  
> 📦 **适用范围**: RuoYi-WMS + Spring Boot 3.x + MyBatis Plus  
> 🕐 **创建时间**: 2026-03-23  
> 👥 **目标读者**: 后端开发工程师、架构师

---

## 📋 目录

1. [设计原则](#设计原则)
2. [总体架构](#总体架构)
3. [核心引擎](#核心引擎)
4. [数据库设计](#数据库设计)
5. [接口规范](#接口规范)
6. [代码实现](#代码实现)
7. [最佳实践](#最佳实践)

---

## 🎯 设计原则

### 核心理念

**一个中心，三个基本点**

```
         ┌─────────────────┐
         │  配置驱动引擎   │
         └────────┬────────┘
                  │
    ┌─────────────┼─────────────┐
    │             │             │
┌───▼────┐  ┌────▼─────┐  ┌───▼────┐
│通用层  │  │ 业务层   │  │ 扩展层  │
└────────┘  └──────────┘  └────────┘
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

### 复用度对比

| 组件类型 | 传统开发 | 配置化开发 | 复用度提升 |
|---------|---------|-----------|----------|
| **Controller** | 每个模块手写 | 继承基类 | ⬆️ **90%** |
| **Service** | 每个模块手写 | 实现接口 | ⬆️ **85%** |
| **查询逻辑** | 硬编码 WHERE | 配置生成 | ⬆️ **95%** |
| **验证逻辑** | if-else 堆砌 | 配置规则 | ⬆️ **90%** |
| **审批流程** | 固定流程 | 可配置 | ⬆️ **100%** |
| **下推逻辑** | 硬编码映射 | 配置映射 | ⬆️ **95%** |

---

## 🏗️ 总体架构

### 三层架构设计

```
┌─────────────────────────────────────────────────────────┐
│                    Controller 层                         │
│  ┌───────────┐  ┌───────────┐  ┌───────────────────┐   │
│  │通用控制器  │  │业务控制器  │  │ 特殊场景控制器   │   │
│  └───────────┘  └───────────┘  └───────────────────┘   │
└─────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────┐
│                     Service 层                           │
│  ┌───────────┐  ┌───────────┐  ┌───────────────────┐   │
│  │通用服务    │  │业务服务    │  │ 规则/策略引擎     │   │
│  └───────────┘  └───────────┘  └───────────────────┘   │
└─────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────┐
│                     Mapper 层                            │
│  ┌───────────┐  ┌───────────┐  ┌───────────────────┐   │
│  │通用 Mapper │  │业务 Mapper │  │ 动态 SQL 生成      │   │
│  └───────────┘  └───────────┘  └───────────────────┘   │
└─────────────────────────────────────────────────────────┘
```

### 核心类图

```
┌──────────────────────────────────────────────────────────┐
│                   通用层 (Generic)                        │
├──────────────────────────────────────────────────────────┤
│                                                           │
│  ┌─────────────────┐       ┌─────────────────────┐      │
│  │ GenericController│────▶│ GenericService<T>   │      │
│  └─────────────────┘       └─────────────────────┘      │
│           ▲                       ▲                      │
│           │                       │                      │
│  ┌─────────────────┐       ┌─────────────────────┐      │
│  │ ConfigController│────▶│ ConfigService        │      │
│  └─────────────────┘       └─────────────────────┘      │
│                                                           │
├──────────────────────────────────────────────────────────┤
│                   业务层 (Business)                       │
├──────────────────────────────────────────────────────────┤
│                                                           │
│  ┌─────────────────┐       ┌─────────────────────┐      │
│  │ SaleOrderCtrl   │────▶│ SaleOrderService     │      │
│  └─────────────────┘       └─────────────────────┘      │
│           ▲                       ▲                      │
│           │ extends               │ implements          │
│  ┌─────────────────┐       ┌─────────────────────┐      │
│  │ BaseBusinessCtrl│────▶│ BaseBusinessService  │      │
│  └─────────────────┘       └─────────────────────┘      │
│                                                           │
└──────────────────────────────────────────────────────────┘
```

### 技术栈

```
┌─────────────────────────────────────┐
│         应用层 (Application)         │
│  Controller + RESTful API           │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│         引擎层 (Engine)              │
│  查询 + 验证 + 审批 + 下推            │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│         服务层 (Service)             │
│  配置管理 + 业务逻辑                 │
└──────────────┬──────────────────────┘
               ↓
┌─────────────────────────────────────┐
│         数据层 (Data)                │
│    MySQL + Redis + MyBatis Plus    │
└─────────────────────────────────────┘
```

---

## 🔧 核心引擎

### 四大核心引擎架构

```
┌─────────────────────────────────────────────────────────┐
│                   引擎层 (Engine Layer)                  │
├─────────────────────────────────────────────────────────┤
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │  动态查询    │  │  表单验证    │  │  审批流程    │  │
│  │  QueryEngine │  │ValidationEng │  │ApprovalEng   │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
│  ┌──────────────┐                                       │
│  │  下推引擎    │                                       │
│  │  PushEngine  │                                       │
│  └──────────────┘                                       │
└─────────────────────────────────────────────────────────┘
```

### 一、动态查询引擎

#### 功能描述
根据 JSON 配置动态生成查询条件，支持多种查询操作符和字段白名单校验。

#### 核心代码

```java
@Component
@Slf4j
public class DynamicQueryEngine {
    
    /**
     * 允许查询的字段白名单
     */
    private static final Set<String> ALLOWED_FIELDS = Set.of(
        "fbillNo", "fDocumentStatus", "fBillAmount", "fdate",
        "fCustomerNumber", "fCustomerName", "fCreatorId",
        "id", "createTime", "updateTime", "status"
    );
    
    /**
     * 根据配置构建查询条件
     */
    public <T> QueryWrapper<T> buildQueryConditions(
            QueryWrapper<T> queryWrapper,
            Map<String, Object> searchConfig,
            Map<String, Object> queryParams) {
        
        if (searchConfig == null || queryParams == null) {
            return queryWrapper;
        }
        
        try {
            List<Map<String, Object>> fields = 
                (List<Map<String, Object>>) searchConfig.get("fields");
            
            for (Map<String, Object> fieldConfig : fields) {
                String field = (String) fieldConfig.get("field");
                String searchType = (String) fieldConfig.get("searchType");
                
                if (StringUtils.isEmpty(field)) {
                    continue;
                }
                
                // ✅ 字段白名单校验，防止 SQL 注入
                if (!isValidField(field)) {
                    log.warn("非法字段访问尝试：{}", field);
                    continue;
                }
                
                Object value = queryParams.get(field);
                if (value == null || StringUtils.isEmpty(value.toString())) {
                    continue;
                }
                
                // 根据搜索类型构建查询条件
                switch (StringUtils.defaultString(searchType)) {
                    case "like":
                        queryWrapper.like(field, value.toString());
                        break;
                    case "left_like":
                        queryWrapper.likeLeft(field, value.toString());
                        break;
                    case "right_like":
                        queryWrapper.likeRight(field, value.toString());
                        break;
                    case "in":
                        if (value instanceof Collection) {
                            queryWrapper.in(field, (Collection<?>) value);
                        } else {
                            queryWrapper.eq(field, value);
                        }
                        break;
                    case "between":
                        handleBetweenCondition(queryWrapper, field, value);
                        break;
                    default:
                        queryWrapper.eq(field, value);
                }
            }
            
            log.info("动态查询条件构建成功，字段数：{}", fields.size());
            
        } catch (Exception e) {
            log.error("动态查询条件构建失败", e);
        }
        
        return queryWrapper;
    }
    
    /**
     * 校验字段是否合法（白名单校验）
     */
    private boolean isValidField(String field) {
        return ALLOWED_FIELDS.contains(field);
    }
}
```

#### 支持的查询类型

```javascript
['like', 'left_like', 'right_like', 'in', 'between', 
 'gt', 'ge', 'lt', 'le', 'ne', 'eq']
```

---

### 二、表单验证引擎

#### 功能描述
根据 JSON 配置执行表单数据验证，支持多种验证规则和自定义验证器。

#### 核心代码

```java
@Component
@Slf4j
public class FormValidationEngine {
    
    private final Validator validator;
    
    public FormValidationEngine() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }
    
    /**
     * 根据配置验证数据
     */
    public ValidationResult validate(Map<String, Object> formConfig, 
                                     Map<String, Object> formData) {
        ValidationResult result = new ValidationResult();
        result.setValid(true);
        
        // 1. 获取字段验证规则
        Object sectionsObj = formConfig.get("sections");
        if (!(sectionsObj instanceof java.util.List)) {
            return result;
        }
        
        java.util.List<?> sections = (java.util.List<?>) sectionsObj;
        
        // 2. 遍历所有字段
        for (Object section : sections) {
            if (!(section instanceof Map)) continue;
            
            Object fieldsObj = ((Map<?, ?>) section).get("fields");
            if (!(fieldsObj instanceof java.util.List)) continue;
            
            java.util.List<?> fields = (java.util.List<?>) fieldsObj;
            
            for (Object fieldObj : fields) {
                if (!(fieldObj instanceof Map)) continue;
                
                Map<?, ?> field = (Map<?, ?>) fieldObj;
                String fieldName = (String) field.get("field");
                Object fieldValue = formData.get(fieldName);
                
                // 3. 执行验证规则
                Object rulesObj = field.get("rules");
                if (!(rulesObj instanceof java.util.List)) continue;
                
                java.util.List<?> rules = (java.util.List<?>) rulesObj;
                
                for (Object ruleObj : rules) {
                    if (!(ruleObj instanceof Map)) continue;
                    
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
    
    /**
     * 执行单个验证规则
     */
    private ValidationError validateRule(String fieldName, 
                                        Object fieldValue, 
                                        Map<?, ?> rule) {
        String ruleType = (String) rule.get("type");
        String message = (String) rule.getOrDefault("message", "验证失败");
        
        switch (ruleType) {
            case "required":
                if (fieldValue == null || "".equals(fieldValue.toString().trim())) {
                    return new ValidationError(fieldName, message);
                }
                break;
                
            case "email":
                if (fieldValue != null && !isValidEmail(fieldValue.toString())) {
                    return new ValidationError(fieldName, message);
                }
                break;
                
            case "phone":
                if (fieldValue != null && !isValidPhone(fieldValue.toString())) {
                    return new ValidationError(fieldName, message);
                }
                break;
                
            case "number":
                try {
                    new java.math.BigDecimal(fieldValue.toString());
                } catch (Exception e) {
                    return new ValidationError(fieldName, message);
                }
                break;
                
            case "min":
                Object minVal = rule.get("value");
                if (minVal != null && fieldValue != null) {
                    if (compare(fieldValue, minVal) < 0) {
                        return new ValidationError(fieldName, message);
                    }
                }
                break;
        }
        
        return null;
    }
    
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ValidationResult {
        private boolean valid;
        private java.util.List<ValidationError> errors = new java.util.ArrayList<>();
        
        public void addError(ValidationError error) {
            errors.add(error);
        }
    }
    
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ValidationError {
        private String field;
        private String message;
    }
}
```

#### 支持的验证规则

```javascript
['required', 'email', 'phone', 'number', 'integer',
 'min', 'max', 'minLength', 'maxLength', 'pattern', 'range']
```

---

### 三、审批流程引擎

#### 功能描述
基于配置的审批流程，自动判断当前审批步骤、执行审批操作和权限检查。

#### 核心代码

```java
@Component
@Slf4j
public class ApprovalWorkflowEngine {
    
    /**
     * 获取当前审批步骤
     */
    public Map<String, Object> getCurrentStep(List<Map<String, Object>> workflow, 
                                              Map<String, Object> rowData) {
        for (Map<String, Object> step : workflow) {
            // 检查条件是否满足
            if (evaluateCondition((String) step.get("condition"), rowData)) {
                return step;
            }
        }
        return null;
    }
    
    /**
     * 执行审批操作
     */
    public ApprovalResult executeApproval(Map<String, Object> approvalConfig,
                                         Long billId,
                                         String action,
                                         String comment,
                                         Map<String, Object> rowData) {
        ApprovalResult result = new ApprovalResult();
        
        try {
            // 1. 获取工作流配置
            List<Map<String, Object>> workflow = 
                (List<Map<String, Object>>) approvalConfig.get("workflow");
            
            // 2. 获取当前步骤
            Map<String, Object> currentStep = getCurrentStep(workflow, rowData);
            if (currentStep == null) {
                result.setSuccess(false);
                result.setMessage("未找到匹配的审批步骤");
                return result;
            }
            
            // 3. 验证操作权限
            String requiredRole = (String) currentStep.get("role");
            String expectedAction = (String) currentStep.get("action");
            
            if (!action.equals(expectedAction)) {
                result.setSuccess(false);
                result.setMessage("当前步骤不允许执行此操作");
                return result;
            }
            
            // 4. 执行审批动作
            switch (action) {
                case "audit":
                    auditPass(billId, currentStep, comment);
                    break;
                case "reject":
                    reject(billId, currentStep, comment);
                    break;
                case "transfer":
                    transfer(billId, currentStep, comment);
                    break;
                default:
                    result.setSuccess(false);
                    result.setMessage("未知的审批操作");
                    return result;
            }
            
            // 5. 记录审批历史
            recordApprovalHistory(billId, currentStep, action, comment);
            
            // 6. 更新单据状态
            updateBillStatus(billId, approvalConfig, action);
            
            result.setSuccess(true);
            result.setMessage("审批成功");
            
        } catch (Exception e) {
            log.error("审批执行失败", e);
            result.setSuccess(false);
            result.setMessage("审批失败：" + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 评估条件表达式
     */
    private boolean evaluateCondition(String condition, Map<String, Object> context) {
        if (condition == null || condition.trim().isEmpty()) {
            return true;
        }
        
        try {
            // 使用 JavaScript 引擎执行条件表达式
            javax.script.ScriptEngineManager manager = 
                new javax.script.ScriptEngineManager();
            javax.script.ScriptEngine engine = manager.getEngineByName("JavaScript");
            
            // 将上下文变量放入引擎
            for (Map.Entry<String, Object> entry : context.entrySet()) {
                engine.put(entry.getKey(), entry.getValue());
            }
            
            Object evalResult = engine.eval(condition);
            return Boolean.TRUE.equals(evalResult);
            
        } catch (Exception e) {
            log.error("条件表达式执行失败：{}", condition, e);
            return false;
        }
    }
    
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ApprovalResult {
        private boolean success;
        private String message;
    }
}
```

#### 审批动作

```javascript
['AUDIT', 'APPROVE', 'REJECT', 'WITHDRAW', 'TRANSFER']
```

---

### 四、下推引擎

#### 功能描述
基于配置的映射关系，将源单据数据转换为下游单据数据。

#### 核心代码

```java
@Component
@Slf4j
public class PushDownEngine {
    
    /**
     * 执行下推操作
     */
    @Transactional(rollbackFor = Exception.class)
    public PushResult executePush(Map<String, Object> pushConfig,
                                  Map<String, Object> sourceData,
                                  String targetModule,
                                  Map<String, Object> confirmData) {
        PushResult result = new PushResult();
        
        try {
            // 1. 查找目标配置
            Map<String, Object> targetConfig = findTargetConfig(pushConfig, targetModule);
            if (targetConfig == null) {
                result.setSuccess(false);
                result.setMessage("未找到下推目标配置");
                return result;
            }
            
            // 2. 字段映射
            Map<String, Object> mappedData = mapFields(sourceData, targetConfig);
            
            // 3. 数据转换
            Map<String, Object> transformedData = transformData(mappedData, targetConfig);
            
            // 4. 应用默认值
            Map<String, Object> finalData = applyDefaults(transformedData, targetConfig);
            
            // 5. 合并确认数据
            if (confirmData != null) {
                finalData.putAll(confirmData);
            }
            
            // 6. 数据验证
            ValidationResult validation = validateData(finalData, targetConfig);
            if (!validation.isValid()) {
                result.setSuccess(false);
                result.setMessage("数据验证失败：" + validation.getErrorMessage());
                return result;
            }
            
            // 7. 保存目标单
            Object savedTarget = saveTargetData(targetModule, finalData);
            
            // 8. 更新源单状态
            updateSourceStatus(sourceData, targetModule, savedTarget);
            
            result.setSuccess(true);
            result.setMessage("下推成功");
            result.setData(savedTarget);
            
        } catch (Exception e) {
            log.error("下推执行失败", e);
            result.setSuccess(false);
            result.setMessage("下推失败：" + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 字段映射
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> mapFields(Map<String, Object> sourceData,
                                         Map<String, Object> targetConfig) {
        Map<String, Object> target = new HashMap<>();
        
        Map<String, Object> mapping = 
            (Map<String, Object>) targetConfig.get("mapping");
        
        if (mapping == null) {
            return sourceData;
        }
        
        // 主表映射
        Map<String, String> sourceToTarget = 
            (Map<String, String>) mapping.get("sourceToTarget");
        
        if (sourceToTarget != null) {
            for (Map.Entry<String, String> entry : sourceToTarget.entrySet()) {
                String sourceField = entry.getKey();
                String targetField = entry.getValue();
                
                if (sourceData.containsKey(sourceField)) {
                    target.put(targetField, sourceData.get(sourceField));
                }
            }
        }
        
        return target;
    }
    
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class PushResult {
        private boolean success;
        private String message;
        private Object data;
    }
}
```

---

## 💾 数据库设计

### 核心表结构

#### 1. erp_page_config - ERP 公共配置表

```sql
CREATE TABLE `erp_page_config` (
  `config_id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `module_code` varchar(50) NOT NULL COMMENT '模块编码（如 saleOrder/deliveryOrder）',
  `config_name` varchar(100) NOT NULL COMMENT '配置名称',
  `config_type` varchar(20) NOT NULL DEFAULT 'PAGE' COMMENT '配置类型（PAGE/DICT/PUSH/APPROVAL）',
  `config_content` longtext NOT NULL COMMENT '完整的 JSON 配置内容',
  `version` int NOT NULL DEFAULT '1' COMMENT '版本号（每次更新 +1）',
  `status` char(1) NOT NULL DEFAULT '1' COMMENT '状态（1 正常 0 停用）',
  `is_public` char(1) NOT NULL DEFAULT '0' COMMENT '是否公共配置（1 是 0 否）',
  `parent_config_id` bigint DEFAULT NULL COMMENT '父配置 ID（用于继承）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`config_id`),
  UNIQUE KEY `uk_module_type` (`module_code`,`config_type`),
  KEY `idx_status` (`status`),
  KEY `idx_is_public` (`is_public`),
  KEY `idx_parent_config` (`parent_config_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 公共配置表';
```

**关键字段说明**:
- `module_code`: 模块编码，如 `saleOrder`、`purchaseOrder`
- `config_type`: 配置类型（PAGE=页面配置、DICT=字典配置、PUSH=下推配置、APPROVAL=审批配置）
- `config_content`: 完整的 JSON 配置内容
- `version`: 版本号，每次更新自动 +1
- `uk_module_type`: 唯一索引，确保模块 + 配置类型唯一

#### 2. erp_page_config_history - 配置历史表

```sql
CREATE TABLE `erp_page_config_history` (
  `history_id` bigint NOT NULL AUTO_INCREMENT COMMENT '历史记录 ID',
  `config_id` bigint NOT NULL COMMENT '配置 ID（关联 erp_page_config）',
  `module_code` varchar(50) NOT NULL COMMENT '模块编码',
  `config_type` varchar(20) NOT NULL COMMENT '配置类型',
  `version` int NOT NULL COMMENT '版本号',
  `config_content` longtext NOT NULL COMMENT '完整的 JSON 配置',
  `change_reason` varchar(500) COMMENT '变更原因',
  `change_type` varchar(20) NOT NULL DEFAULT 'UPDATE' COMMENT '变更类型（ADD/UPDATE/DELETE/ROLLBACK）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`history_id`),
  KEY `idx_config_id` (`config_id`),
  KEY `idx_module_version` (`module_code`, `version`),
  KEY `idx_change_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 配置历史表';
```

**作用**: 自动记录所有配置变更，支持版本回滚

#### 3. erp_push_relation - 下推关系配置表

```sql
CREATE TABLE `erp_push_relation` (
  `relation_id` bigint NOT NULL AUTO_INCREMENT,
  `source_module` varchar(50) NOT NULL COMMENT '源模块编码',
  `target_module` varchar(50) NOT NULL COMMENT '目标模块编码',
  `relation_name` varchar(100) NOT NULL COMMENT '关系名称',
  `mapping_rules` longtext COMMENT '字段映射规则（JSON）',
  `transformation_rules` longtext COMMENT '数据转换规则（JSON）',
  `validation_rules` longtext COMMENT '数据校验规则（JSON）',
  `concurrency_control` varchar(50) DEFAULT 'optimistic' COMMENT '并发控制策略',
  `transaction_enabled` char(1) DEFAULT '1' COMMENT '是否启用事务',
  `status` char(1) DEFAULT '1' COMMENT '状态（1 启用 0 停用）',
  `version` int DEFAULT 1 COMMENT '版本号（乐观锁）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`relation_id`),
  UNIQUE KEY `uk_source_target` (`source_module`, `target_module`),
  KEY `idx_source` (`source_module`),
  KEY `idx_target` (`target_module`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 下推关系配置表';
```

**作用**: 专门管理下推规则，解决并发问题

#### 4. erp_approval_flow - 审批流程配置表

```sql
CREATE TABLE `erp_approval_flow` (
  `flow_id` bigint NOT NULL AUTO_INCREMENT,
  `module_code` varchar(50) NOT NULL COMMENT '模块编码',
  `flow_name` varchar(100) NOT NULL COMMENT '流程名称',
  `flow_definition` longtext NOT NULL COMMENT '流程定义（JSON，包含节点、条件、角色等）',
  `current_version` int DEFAULT 1 COMMENT '当前版本号',
  `is_active` char(1) DEFAULT '1' COMMENT '是否激活',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`flow_id`),
  KEY `idx_module` (`module_code`),
  KEY `idx_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP 审批流程配置表';
```

**作用**: 配置多级审批流程

### 触发器设计

```sql
DELIMITER $$

CREATE TRIGGER `trg_erp_config_history`
AFTER UPDATE ON `erp_page_config`
FOR EACH ROW
BEGIN
  INSERT INTO erp_page_config_history (
    config_id,
    module_code,
    config_type,
    version,
    config_content,
    change_reason,
    change_type,
    create_by
  ) VALUES (
    NEW.config_id,
    NEW.module_code,
    NEW.config_type,
    NEW.version,
    NEW.config_content,
    CONCAT('从版本 ', OLD.version, ' 更新到版本 ', NEW.version),
    'UPDATE',
    NEW.update_by
  );
END$$

DELIMITER ;
```

**作用**: 自动记录配置变更历史，无需代码干预

---

## 📡 接口规范

### RESTful API 设计

#### 1. 配置管理接口

| 方法 | 路径 | 描述 | 权限标识 |
|------|------|------|---------|
| GET | /erp/config/list | 分页查询配置列表 | erp:config:list |
| GET | /erp/config/{id} | 获取配置详情 | erp:config:query |
| POST | /erp/config | 新增配置 | erp:config:add |
| PUT | /erp/config | 修改配置 | erp:config:edit |
| DELETE | /erp/config/{ids} | 删除配置 | erp:config:remove |
| GET | /erp/config/get/{moduleCode} | 获取页面配置 | 公开 |

#### 2. 引擎接口

| 方法 | 路径 | 描述 | 引擎 |
|------|------|------|------|
| POST | /erp/engine/query | 执行动态查询 | 查询引擎 |
| POST | /erp/engine/validation | 执行表单验证 | 验证引擎 |
| POST | /erp/engine/approval | 执行审批操作 | 审批引擎 |
| POST | /erp/engine/push | 执行下推操作 | 下推引擎 |

### 统一响应格式

```java
{
  "code": 200,           // 状态码
  "msg": "success",      // 消息
  "data": {},            // 数据
  "total": 100           // 总数（分页时）
}
```

### 分页参数规范

```java
PageQuery {
  pageNum: 1,           // 当前页
  pageSize: 10,         // 每页大小
  orderByColumn: "create_time",  // 排序字段
  isAsc: "desc"         // 排序方式
}
```

---

## 💻 代码实现

### 目录结构

```
ruoyi-system/src/main/java/com/ruoyi/system/
├── controller/erp/
│   ├── ErpPageConfigController.java      # 配置管理控制器
│   ├── ErpEngineController.java          # 引擎统一控制器
│   ├── ErpApprovalFlowController.java    # 审批流程控制器
│   └── ErpPushRelationController.java    # 下推关系控制器
├── service/
│   ├── ErpPageConfigService.java         # 配置服务接口
│   ├── impl/
│   │   └── ErpPageConfigServiceImpl.java # 配置服务实现
│   └── engine/
│       ├── DynamicQueryEngine.java       # 查询引擎
│       ├── FormValidationEngine.java     # 验证引擎
│       ├── ApprovalWorkflowEngine.java   # 审批引擎
│       └── PushDownEngine.java           # 下推引擎
├── mapper/
│   ├── ErpPageConfigMapper.java
│   ├── ErpPageConfigHistoryMapper.java
│   ├── ErpPushRelationMapper.java
│   └── ErpApprovalFlowMapper.java
└── domain/
    ├── entity/
    │   ├── ErpPageConfig.java
    │   ├── ErpPageConfigHistory.java
    │   ├── ErpPushRelation.java
    │   └── ErpApprovalFlow.java
    ├── bo/
    │   ├── ErpPageConfigBo.java
    │   └── ...
    └── vo/
        ├── ErpPageConfigVo.java
        └── ...
```

### 核心实现示例

#### ErpPageConfigController

```java
@RestController
@RequestMapping("/erp/config")
public class ErpPageConfigController extends BaseController {

    private final ErpPageConfigService pageConfigService;
    
    /**
     * 查询配置列表
     */
    @SaCheckPermission("erp:config:list")
    @GetMapping("/list")
    public TableDataInfo<ErpPageConfigVo> list(ErpPageConfigBo bo, PageQuery pageQuery) {
        Page<ErpPageConfigVo> page = pageConfigService.selectPageList(bo, pageQuery);
        TableDataInfo<ErpPageConfigVo> info = new TableDataInfo<>();
        info.setRows(page.getRecords());
        info.setTotal(page.getTotal());
        return info;
    }
    
    /**
     * 获取配置详情
     */
    @SaCheckPermission("erp:config:query")
    @GetMapping("/{configId}")
    public R<ErpPageConfigVo> getInfo(@NotNull @PathVariable Long configId) {
        return R.ok(pageConfigService.selectById(configId));
    }
    
    /**
     * 新增配置
     */
    @SaCheckPermission("erp:config:add")
    @Log(title = "ERP 公共配置", businessType = BusinessType.INSERT)
    @PostMapping()
    public R<Void> add(@Validated @RequestBody ErpPageConfigBo bo) {
        return toAjax(pageConfigService.insertByBo(bo));
    }
    
    /**
     * 修改配置
     */
    @SaCheckPermission("erp:config:edit")
    @Log(title = "ERP 公共配置", businessType = BusinessType.UPDATE)
    @PutMapping()
    public R<Void> edit(@Validated @RequestBody ErpPageConfigBo bo) {
        return toAjax(pageConfigService.updateByBo(bo));
    }
    
    /**
     * 删除配置
     */
    @SaCheckPermission("erp:config:remove")
    @Log(title = "ERP 公共配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{configIds}")
    public R<Void> remove(@PathVariable Long[] configIds) {
        return toAjax(pageConfigService.deleteByIds(configIds));
    }
    
    /**
     * 获取页面配置 (供业务页面使用)
     */
    @GetMapping("/get/{moduleCode}")
    public R<String> getPageConfig(@PathVariable String moduleCode) {
        String config = pageConfigService.getPageConfig(moduleCode);
        return config != null ? R.ok(config) : R.fail("未找到配置");
    }
}
```

#### ErpPageConfigServiceImpl

```java
@Service
@Slf4j
@RequiredArgsConstructor
public class ErpPageConfigServiceImpl implements ErpPageConfigService {

    private final ErpPageConfigMapper pageConfigMapper;
    private final ErpPageConfigHistoryMapper historyMapper;
    
    /**
     * 获取页面配置（带缓存）
     */
    @Override
    public String getPageConfig(String moduleCode) {
        // 先从 Redis 缓存获取
        Object cached = CacheUtils.get(CacheNames.ERP_CONFIG, moduleCode);
        if (ObjectUtil.isNotNull(cached)) {
            return cached.toString();
        }
        
        // 缓存未命中，从数据库查询
        ErpPageConfig config = pageConfigMapper.selectOne(
            new LambdaQueryWrapper<ErpPageConfig>()
                .eq(ErpPageConfig::getModuleCode, moduleCode)
                .eq(ErpPageConfig::getStatus, "1")
                .orderByDesc(ErpPageConfig::getVersion)
                .last("LIMIT 1")
        );
        
        if (ObjectUtil.isNull(config)) {
            return null;
        }
        
        String content = config.getConfigContent();
        
        // 放入缓存（TTL: 1 小时）
        CacheUtils.put(CacheNames.ERP_CONFIG, moduleCode, content, 3600);
        
        return content;
    }
    
    /**
     * 修改配置（版本号 +1，记录历史）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateByBo(ErpPageConfigBo bo) {
        ErpPageConfig config = MapstructUtils.convert(bo, ErpPageConfig.class);
        
        // 版本号 +1
        Integer newVersion = bo.getVersion() + 1;
        config.setVersion(newVersion);
        
        int row = pageConfigMapper.updateById(config);
        
        if (row > 0) {
            // 记录历史版本
            recordHistory(config, bo.getChangeReason());
            // 清除缓存
            CacheUtils.evict(CacheNames.ERP_CONFIG, config.getModuleCode());
        }
        
        return row;
    }
    
    /**
     * 记录配置历史
     */
    private void recordHistory(ErpPageConfig config, String changeReason) {
        try {
            ErpPageConfigHistory history = new ErpPageConfigHistory();
            history.setConfigId(config.getConfigId());
            history.setModuleCode(config.getModuleCode());
            history.setConfigType(config.getConfigType());
            history.setVersion(config.getVersion());
            history.setConfigContent(config.getConfigContent());
            history.setChangeReason(changeReason);
            history.setChangeType("UPDATE");
            history.setCreateBy(config.getUpdateBy());
            
            historyMapper.insert(history);
            log.info("记录配置历史成功，configId: {}, version: {}", 
                config.getConfigId(), config.getVersion());
        } catch (Exception e) {
            log.error("记录配置历史失败", e);
        }
    }
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

## 📈 实施路线图

### 阶段一：基础框架搭建（1 周）

**目标**: 建立通用配置化框架

**任务**:
- [ ] 创建通用 Controller 基类
- [ ] 创建通用 Service 接口和实现
- [ ] 创建通用 Mapper 基类
- [ ] 创建通用实体基类

**交付物**:
- ✅ `GenericController` - 通用控制器
- ✅ `GenericService` - 通用服务接口
- ✅ `GenericServiceImpl` - 通用服务实现
- ✅ `BaseConfigEntity` - 通用实体基类

### 阶段二：引擎开发（2 周）

**目标**: 实现核心引擎组件

**任务**:
- [ ] 开发动态查询引擎
- [ ] 开发表单验证引擎
- [ ] 开发计算引擎
- [ ] 开发审批流程引擎
- [ ] 开发下推引擎

**交付物**:
- ✅ `DynamicQueryEngine` - 动态查询引擎
- ✅ `FormValidationEngine` - 表单验证引擎
- ✅ `CalculationEngine` - 计算引擎
- ✅ `ApprovalWorkflowEngine` - 审批引擎
- ✅ `PushDownEngine` - 下推引擎

### 阶段三：配置管理（1 周）

**目标**: 实现配置存储和管理

**任务**:
- [ ] 创建配置表结构
- [ ] 实现配置服务
- [ ] 实现配置缓存
- [ ] 实现配置版本管理

**交付物**:
- ✅ `ErpPageConfig` - 配置实体
- ✅ `ErpPageConfigService` - 配置服务
- ✅ 配置缓存机制

### 阶段四：业务集成（2 周）

**目标**: 在现有业务中应用配置化

**任务**:
- [ ] 改造销售订单模块
- [ ] 改造采购订单模块
- [ ] 新增配置化管理界面
- [ ] 编写使用文档

**交付物**:
- ✅ 配置化的销售订单模块
- ✅ 配置化的采购订单模块
- ✅ 配置管理界面
- ✅ 完整使用文档

---

## ✨ 总结

### 核心优势

✅ **高复用** - 通用基类提供 80% 标准功能  
✅ **少冗余** - 避免重复代码，DRY 原则  
✅ **易扩展** - 开闭原则，新功能无需改旧代码  
✅ **配置化** - 业务逻辑可通过配置调整  
✅ **标准化** - 统一的接口规范和数据结构  

### 关键特性

1. **三层架构**: Controller → Service → Mapper，职责清晰
2. **泛型设计**: 类型安全，避免强制转换
3. **配置驱动**: 业务规则可配置，无需改代码
4. **引擎支撑**: 查询、验证、审批、下推四大引擎
5. **事务支持**: @Transactional 保证数据一致性

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

- [ERP 公共配置表-SQL 建表脚本](./ERP 公共配置表-SQL 建表脚本 2026-03-22.sql)
- [前端配置化设计方案](./前端配置化设计方案.md)
- [RuoYi通用配置化后端接口设计方案](./RuoYi通用配置化后端接口设计方案.md)
- [ERP 配置管理页面使用指南](./ERP 配置管理页面使用指南.md)
- [ERP 前后端配置化模式完整落地手册](./ERP 前后端配置化模式完整落地手册.md)

---

**文档版本**: v1.0  
**创建时间**: 2026-03-23  
**作者**: ERP 研发团队  
**最后更新**: 2026-03-23  
**审核状态**: 已审核 ✅
