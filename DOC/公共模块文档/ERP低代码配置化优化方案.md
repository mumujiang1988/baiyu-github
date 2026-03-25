# 🔧 ERP低代码配置化优化方案

> **文档日期**: 2026-03-24  
> **基于文档**: ERP配置化能力评估报告  
> **优化目标**: 代码优化、冗余清理、异常修复、功能扩展

---

## 📋 一、优化概览

### 1.1 优化目标

根据审计评估报告,本次优化主要解决以下问题:

| 优化类别 | 问题描述 | 优先级 | 预计工时 |
|---------|---------|--------|---------|
| **功能扩展** | 多字段计算引擎缺失 | 🔴 高 | 8小时 |
| **功能扩展** | 基础资料虚拟字段映射不完善 | 🔴 高 | 6小时 |
| **代码优化** | 字典查询使用公共接口配置化 | 🟡 中 | 4小时 |
| **异常修复** | 权限检查和异常处理机制 | 🔴 高 | 4小时 |
| **冗余清理** | 重复代码和配置优化 | 🟢 低 | 3小时 |

### 1.2 当前能力匹配度

```
完全满足能力: 5项 (71.4%)
├─ 增删改查配置 ✅
├─ 统一查询接口 ✅
├─ 表格字段配置 ✅
├─ 自定义页签 ✅
└─ 配置版本管理 ✅

部分满足能力: 2项 (28.6%)
├─ 字典接口 ⚠️ (80%)
└─ 基础资料映射 ⚠️ (80%)

不满足能力: 1项 (14.3%)
└─ 多字段计算 ❌ (60%)
```

---

## 🎯 二、功能扩展方案

### 2.1 多字段计算引擎实现

#### 2.1.1 需求分析

**业务场景**:
- 订单金额 = SUM(明细行.行金额)
- 净利率 = (总金额 - 成本 - 费用) / 总金额 × 100
- 折扣金额 = 原价 × 折扣率

**当前问题**:
- ❌ 缺少计算引擎
- ❌ 无法配置计算公式
- ❌ 数据变更无法触发计算

#### 2.1.2 配置化方案

**前端配置示例**:

```json
{
  "computedFieldConfig": {
    "enabled": true,
    "fields": [
      {
        "targetField": "fbillAmount",
        "formula": "SUM(entryList.fAllAmount)",
        "trigger": "entryListChange",
        "precision": 2,
        "description": "订单金额=明细金额汇总"
      },
      {
        "targetField": "fNetProfitRate",
        "formula": "(fBillAllAmount - fCbxj - fFyxj) / fBillAllAmount * 100",
        "trigger": "costChange",
        "precision": 2,
        "description": "净利率计算"
      },
      {
        "targetField": "fDiscountAmount",
        "formula": "fOriginalPrice * fDiscountRate",
        "trigger": "priceChange",
        "precision": 2,
        "description": "折扣金额计算"
      }
    ]
  }
}
```

**配置字段说明**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| targetField | String | ✅ | 目标字段名 |
| formula | String | ✅ | 计算公式 |
| trigger | String | ✅ | 触发事件 |
| precision | Integer | ❌ | 小数精度,默认2 |
| description | String | ❌ | 计算说明 |

#### 2.1.3 后端实现方案

**核心类设计**:

```java
/**
 * 计算字段配置实体
 */
@Data
public class ComputedFieldConfig {
    /** 目标字段 */
    private String targetField;
    /** 计算公式 */
    private String formula;
    /** 触发事件 */
    private String trigger;
    /** 小数精度 */
    private Integer precision = 2;
    /** 计算说明 */
    private String description;
}

/**
 * 计算字段引擎
 */
@Component
@Slf4j
public class ComputedFieldEngine {
    
    @Autowired
    private ExpressionParser expressionParser;
    
    /**
     * 执行字段计算
     * 
     * @param data 原始数据
     * @param configs 计算配置
     * @return 计算后的数据
     */
    public Map<String, Object> computeFields(
            Map<String, Object> data,
            List<ComputedFieldConfig> configs) {
        
        if (CollUtil.isEmpty(configs)) {
            return data;
        }
        
        Map<String, Object> result = new HashMap<>(data);
        
        for (ComputedFieldConfig config : configs) {
            try {
                // 解析并计算公式
                Object value = evaluateFormula(config.getFormula(), result);
                
                // 精度处理
                if (value instanceof Number && config.getPrecision() != null) {
                    value = NumberUtil.round((Number) value, config.getPrecision());
                }
                
                result.put(config.getTargetField(), value);
                
                log.debug("计算字段 {} = {}", config.getTargetField(), value);
                
            } catch (Exception e) {
                log.error("计算字段 {} 失败: {}", config.getTargetField(), e.getMessage());
                // 计算失败时保持原值
            }
        }
        
        return result;
    }
    
    /**
     * 批量计算字段
     */
    public List<Map<String, Object>> computeFieldsBatch(
            List<Map<String, Object>> dataList,
            List<ComputedFieldConfig> configs) {
        
        return dataList.stream()
            .map(data -> computeFields(data, configs))
            .collect(Collectors.toList());
    }
    
    /**
     * 解析并计算公式
     */
    private Object evaluateFormula(String formula, Map<String, Object> context) {
        // 支持的函数
        if (formula.startsWith("SUM(")) {
            return evaluateSum(formula, context);
        } else if (formula.startsWith("AVG(")) {
            return evaluateAvg(formula, context);
        } else if (formula.startsWith("COUNT(")) {
            return evaluateCount(formula, context);
        } else {
            // 普通表达式计算
            return expressionParser.evaluate(formula, context);
        }
    }
    
    /**
     * SUM函数计算
     */
    private Object evaluateSum(String formula, Map<String, Object> context) {
        // 提取字段路径: SUM(entryList.fAllAmount)
        String fieldPath = formula.substring(4, formula.length() - 1);
        String[] parts = fieldPath.split("\\.");
        
        String listField = parts[0];
        String sumField = parts[1];
        
        List<Map<String, Object>> list = (List<Map<String, Object>>) context.get(listField);
        if (CollUtil.isEmpty(list)) {
            return 0;
        }
        
        return list.stream()
            .mapToDouble(item -> NumberUtil.parseDouble(item.get(sumField)))
            .sum();
    }
    
    /**
     * AVG函数计算
     */
    private Object evaluateAvg(String formula, Map<String, Object> context) {
        String fieldPath = formula.substring(4, formula.length() - 1);
        String[] parts = fieldPath.split("\\.");
        
        String listField = parts[0];
        String avgField = parts[1];
        
        List<Map<String, Object>> list = (List<Map<String, Object>>) context.get(listField);
        if (CollUtil.isEmpty(list)) {
            return 0;
        }
        
        return list.stream()
            .mapToDouble(item -> NumberUtil.parseDouble(item.get(avgField)))
            .average()
            .orElse(0);
    }
    
    /**
     * COUNT函数计算
     */
    private Object evaluateCount(String formula, Map<String, Object> context) {
        String fieldPath = formula.substring(6, formula.length() - 1);
        List<?> list = (List<?>) context.get(fieldPath);
        return CollUtil.isEmpty(list) ? 0 : list.size();
    }
}

/**
 * 表达式解析器接口
 */
public interface ExpressionParser {
    /**
     * 计算表达式
     */
    Object evaluate(String expression, Map<String, Object> context);
}

/**
 * Aviator表达式解析器实现
 */
@Component
public class AviatorExpressionParser implements ExpressionParser {
    
    @Override
    public Object evaluate(String expression, Map<String, Object> context) {
        try {
            return AviatorEvaluator.execute(expression, context);
        } catch (Exception e) {
            throw new RuntimeException("表达式计算失败: " + expression, e);
        }
    }
}
```

**Controller集成**:

```java
@RestController
@RequestMapping("/erp/engine")
public class ErpEngineController {
    
    @Autowired
    private ComputedFieldEngine computedFieldEngine;
    
    /**
     * 查询数据并执行计算
     */
    @GetMapping("/data/{moduleCode}")
    public R<Page<Map<String, Object>>> queryData(
            @PathVariable String moduleCode,
            PageQuery pageQuery) {
        
        // 1. 查询原始数据
        Page<Map<String, Object>> page = superDataPermissionService
            .selectPageByModule(moduleCode, pageQuery, null);
        
        // 2. 获取计算配置
        ErpPageConfig config = erpPageConfigService.getByModuleCode(moduleCode);
        JSONObject configJson = JSON.parseObject(config.getConfigContent());
        List<ComputedFieldConfig> computeConfigs = parseComputeConfigs(configJson);
        
        // 3. 执行字段计算
        List<Map<String, Object>> computedRecords = computedFieldEngine
            .computeFieldsBatch(page.getRecords(), computeConfigs);
        page.setRecords(computedRecords);
        
        return R.ok(page);
    }
    
    /**
     * 解析计算配置
     */
    private List<ComputedFieldConfig> parseComputeConfigs(JSONObject configJson) {
        JSONObject computedConfig = configJson.getJSONObject("computedFieldConfig");
        if (computedConfig == null || !computedConfig.getBooleanValue("enabled")) {
            return Collections.emptyList();
        }
        
        return computedConfig.getJSONArray("fields")
            .toList()
            .stream()
            .map(obj -> JSON.parseObject(obj.toString(), ComputedFieldConfig.class))
            .collect(Collectors.toList());
    }
}
```

#### 2.1.4 前端集成方案

**Vue组件实现**:

```vue
<template>
  <div class="computed-field-container">
    <!-- 自动计算字段显示 -->
    <el-form-item :label="field.label">
      <el-input
        v-model="computedValue"
        :disabled="true"
        :placeholder="`自动计算: ${field.formula}`"
      />
    </el-form-item>
  </div>
</template>

<script>
export default {
  name: 'ComputedField',
  props: {
    field: {
      type: Object,
      required: true
    },
    formData: {
      type: Object,
      required: true
    }
  },
  computed: {
    computedValue() {
      // 前端实时计算预览
      return this.evaluateFormula(this.field.formula, this.formData);
    }
  },
  methods: {
    evaluateFormula(formula, context) {
      // 简单表达式计算
      try {
        if (formula.startsWith('SUM(')) {
          return this.evaluateSum(formula, context);
        }
        // 其他计算逻辑...
        return eval(formula); // 注意: 生产环境需要使用安全的表达式引擎
      } catch (e) {
        return '计算错误';
      }
    },
    evaluateSum(formula, context) {
      const fieldPath = formula.substring(4, formula.length - 1);
      const [listField, sumField] = fieldPath.split('.');
      const list = context[listField] || [];
      return list.reduce((sum, item) => sum + (item[sumField] || 0), 0);
    }
  }
};
</script>
```

---

### 2.2 基础资料虚拟字段映射

#### 2.2.1 需求分析

**业务场景**:
- 订单列表显示客户名称(通过客户编号关联)
- 明细表显示物料名称(通过物料ID关联)
- 显示部门名称(通过部门编码关联)

**当前问题**:
- ⚠️ 需要手动配置API查询
- ❌ 无法配置虚拟字段
- ❌ 缺少字段关联配置

#### 2.2.2 配置化方案

**前端配置示例**:

```json
{
  "virtualFieldConfig": {
    "enabled": true,
    "fields": [
      {
        "name": "customerName",
        "sourceField": "fCustomerNumber",
        "sourceTable": "t_customer",
        "sourceDisplayField": "customer_name",
        "displayType": "text",
        "cacheable": true,
        "description": "客户名称"
      },
      {
        "name": "materialName",
        "sourceField": "fMaterialId",
        "sourceTable": "t_material",
        "sourceDisplayField": "material_name",
        "displayType": "link",
        "linkConfig": {
          "url": "/material/detail/{id}",
          "target": "_blank"
        },
        "cacheable": true,
        "description": "物料名称"
      },
      {
        "name": "departmentName",
        "sourceField": "fDeptCode",
        "sourceTable": "t_department",
        "sourceDisplayField": "dept_name",
        "displayType": "tag",
        "tagConfig": {
          "color": "blue"
        },
        "cacheable": true,
        "description": "部门名称"
      }
    ]
  }
}
```

**配置字段说明**:

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | ✅ | 虚拟字段名 |
| sourceField | String | ✅ | 源字段名 |
| sourceTable | String | ✅ | 源数据表 |
| sourceDisplayField | String | ✅ | 显示字段 |
| displayType | String | ❌ | 显示类型:text/link/tag |
| cacheable | Boolean | ❌ | 是否缓存,默认true |

#### 2.2.3 后端实现方案

```java
/**
 * 虚拟字段配置实体
 */
@Data
public class VirtualFieldConfig {
    /** 虚拟字段名 */
    private String name;
    /** 源字段名 */
    private String sourceField;
    /** 源数据表 */
    private String sourceTable;
    /** 显示字段 */
    private String sourceDisplayField;
    /** 显示类型 */
    private String displayType = "text";
    /** 是否缓存 */
    private Boolean cacheable = true;
    /** 显示配置 */
    private JSONObject displayConfig;
    /** 说明 */
    private String description;
}

/**
 * 虚拟字段解析服务
 */
@Service
@Slf4j
public class VirtualFieldService {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private RedisCache redisCache;
    
    /**
     * 解析虚拟字段
     */
    public List<Map<String, Object>> resolveVirtualFields(
            List<Map<String, Object>> dataList,
            List<VirtualFieldConfig> configs) {
        
        if (CollUtil.isEmpty(configs) || CollUtil.isEmpty(dataList)) {
            return dataList;
        }
        
        // 按配置分组批量查询
        for (VirtualFieldConfig config : configs) {
            resolveSingleConfig(dataList, config);
        }
        
        return dataList;
    }
    
    /**
     * 解析单个配置
     */
    private void resolveSingleConfig(
            List<Map<String, Object>> dataList,
            VirtualFieldConfig config) {
        
        // 1. 收集所有源字段值
        Set<Object> sourceValues = dataList.stream()
            .map(data -> data.get(config.getSourceField()))
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        
        if (CollUtil.isEmpty(sourceValues)) {
            return;
        }
        
        // 2. 批量查询基础资料
        Map<Object, Object> valueMap = batchQueryBaseData(
            config.getSourceTable(),
            config.getSourceField(),
            config.getSourceDisplayField(),
            sourceValues,
            config.getCacheable()
        );
        
        // 3. 设置虚拟字段值
        for (Map<String, Object> data : dataList) {
            Object sourceValue = data.get(config.getSourceField());
            if (sourceValue != null) {
                Object displayValue = valueMap.get(sourceValue);
                data.put(config.getName(), displayValue);
            }
        }
    }
    
    /**
     * 批量查询基础资料
     */
    private Map<Object, Object> batchQueryBaseData(
            String tableName,
            String sourceField,
            String displayField,
            Set<Object> sourceValues,
            Boolean cacheable) {
        
        Map<Object, Object> result = new HashMap<>();
        Set<Object> needQueryValues = new HashSet<>();
        
        // 1. 尝试从缓存获取
        if (Boolean.TRUE.equals(cacheable)) {
            for (Object value : sourceValues) {
                String cacheKey = buildCacheKey(tableName, sourceField, value);
                Object cachedValue = redisCache.getCacheObject(cacheKey);
                if (cachedValue != null) {
                    result.put(value, cachedValue);
                } else {
                    needQueryValues.add(value);
                }
            }
        } else {
            needQueryValues.addAll(sourceValues);
        }
        
        // 2. 查询数据库
        if (CollUtil.isNotEmpty(needQueryValues)) {
            String sql = String.format(
                "SELECT %s, %s FROM %s WHERE %s IN (%s)",
                sourceField, displayField, tableName, sourceField,
                needQueryValues.stream().map(v -> "?").collect(Collectors.joining(","))
            );
            
            List<Map<String, Object>> queryResult = jdbcTemplate.queryForList(
                sql, 
                needQueryValues.toArray()
            );
            
            // 3. 缓存查询结果
            for (Map<String, Object> row : queryResult) {
                Object sourceValue = row.get(sourceField);
                Object displayValue = row.get(displayField);
                result.put(sourceValue, displayValue);
                
                if (Boolean.TRUE.equals(cacheable)) {
                    String cacheKey = buildCacheKey(tableName, sourceField, sourceValue);
                    redisCache.setCacheObject(cacheKey, displayValue, 1, TimeUnit.HOURS);
                }
            }
        }
        
        return result;
    }
    
    /**
     * 构建缓存键
     */
    private String buildCacheKey(String tableName, String sourceField, Object value) {
        return String.format("virtual_field:%s:%s:%s", tableName, sourceField, value);
    }
}

/**
 * Controller集成
 */
@RestController
@RequestMapping("/erp/engine")
public class ErpEngineController {
    
    @Autowired
    private VirtualFieldService virtualFieldService;
    
    /**
     * 查询数据并解析虚拟字段
     */
    @GetMapping("/data/{moduleCode}")
    public R<Page<Map<String, Object>>> queryData(
            @PathVariable String moduleCode,
            PageQuery pageQuery) {
        
        // 1. 查询原始数据
        Page<Map<String, Object>> page = superDataPermissionService
            .selectPageByModule(moduleCode, pageQuery, null);
        
        // 2. 获取虚拟字段配置
        ErpPageConfig config = erpPageConfigService.getByModuleCode(moduleCode);
        JSONObject configJson = JSON.parseObject(config.getConfigContent());
        List<VirtualFieldConfig> virtualConfigs = parseVirtualConfigs(configJson);
        
        // 3. 解析虚拟字段
        List<Map<String, Object>> resolvedRecords = virtualFieldService
            .resolveVirtualFields(page.getRecords(), virtualConfigs);
        page.setRecords(resolvedRecords);
        
        return R.ok(page);
    }
}
```

---

## 🔧 三、代码优化方案

### 3.1 字典查询配置化优化

#### 3.1.1 当前问题

评估报告指出:不需要专门的字典接口,应使用公共查询接口配置化获取字段数据。

**优化前**:
```java
// 专门的字典接口(不推荐)
@GetMapping("/dictionary/{dictType}")
public R<?> getDictionary(@PathVariable String dictType) {
    // 专门实现字典查询逻辑
}
```

**优化后**:
```java
// 使用公共查询接口配置化获取
@GetMapping("/query")
public R<?> queryData(@RequestParam String moduleCode, @RequestParam String tableName) {
    // 通过配置化方式查询任意表数据
    return superDataPermissionService.selectListByModule(moduleCode, null, null);
}
```

#### 3.1.2 配置化字典查询方案

**前端配置示例**:

```json
{
  "dictionaryConfig": {
    "salespersons": {
      "usePublicApi": true,
      "tableName": "t_seller",
      "labelField": "nick_name",
      "valueField": "f_seller",
      "filterConfig": {
        "status": "1"
      }
    },
    "currency": {
      "usePublicApi": true,
      "tableName": "sys_dict_data",
      "labelField": "dict_label",
      "valueField": "dict_value",
      "filterConfig": {
        "dict_type": "currency"
      }
    },
    "departments": {
      "usePublicApi": true,
      "tableName": "t_department",
      "labelField": "dept_name",
      "valueField": "dept_code",
      "filterConfig": {
        "status": "1"
      }
    }
  }
}
```

**前端调用方式**:

```javascript
// 统一使用公共查询接口
async function loadDictionary(dictKey) {
  const dictConfig = config.dictionaryConfig[dictKey];
  
  if (dictConfig.usePublicApi) {
    // 使用公共查询接口
    const response = await request({
      url: '/erp/engine/query',
      method: 'get',
      params: {
        moduleCode: moduleCode,
        tableName: dictConfig.tableName,
        ...dictConfig.filterConfig
      }
    });
    
    // 转换为字典格式
    return response.data.map(item => ({
      label: item[dictConfig.labelField],
      value: item[dictConfig.valueField]
    }));
  } else {
    // 使用自定义API
    return request({
      url: dictConfig.api,
      method: 'get'
    });
  }
}
```

### 3.2 代码结构优化

#### 3.2.1 提取公共基类

```java
/**
 * ERP引擎公共基类Controller
 */
public abstract class BaseErpEngineController {
    
    @Autowired
    protected SuperDataPermissionService superDataPermissionService;
    
    @Autowired
    protected ErpPageConfigService erpPageConfigService;
    
    @Autowired
    protected ComputedFieldEngine computedFieldEngine;
    
    @Autowired
    protected VirtualFieldService virtualFieldService;
    
    /**
     * 获取并解析配置
     */
    protected JSONObject getConfig(String moduleCode) {
        ErpPageConfig config = erpPageConfigService.getByModuleCode(moduleCode);
        return JSON.parseObject(config.getConfigContent());
    }
    
    /**
     * 处理数据(计算+虚拟字段)
     */
    protected List<Map<String, Object>> processData(
            List<Map<String, Object>> dataList,
            JSONObject config) {
        
        // 1. 执行字段计算
        List<ComputedFieldConfig> computeConfigs = parseComputeConfigs(config);
        dataList = computedFieldEngine.computeFieldsBatch(dataList, computeConfigs);
        
        // 2. 解析虚拟字段
        List<VirtualFieldConfig> virtualConfigs = parseVirtualConfigs(config);
        dataList = virtualFieldService.resolveVirtualFields(dataList, virtualConfigs);
        
        return dataList;
    }
    
    /**
     * 权限检查
     */
    protected void checkPermission(String moduleCode, String permission) {
        // 统一权限检查逻辑
        String permissionKey = String.format("erp:%s:%s", moduleCode, permission);
        StpUtil.checkPermission(permissionKey);
    }
}
```

---

## 🐛 四、异常处理修复方案

### 4.1 统一异常处理

#### 4.1.1 自定义异常类

```java
/**
 * ERP配置化业务异常
 */
public class ErpConfigException extends RuntimeException {
    
    private String moduleCode;
    private String errorCode;
    
    public ErpConfigException(String moduleCode, String message) {
        super(message);
        this.moduleCode = moduleCode;
    }
    
    public ErpConfigException(String moduleCode, String errorCode, String message) {
        super(message);
        this.moduleCode = moduleCode;
        this.errorCode = errorCode;
    }
}

/**
 * 计算字段异常
 */
public class ComputedFieldException extends ErpConfigException {
    
    private String fieldName;
    private String formula;
    
    public ComputedFieldException(String moduleCode, String fieldName, String formula, String message) {
        super(moduleCode, "COMPUTE_ERROR", message);
        this.fieldName = fieldName;
        this.formula = formula;
    }
}
```

#### 4.1.2 全局异常处理器

```java
/**
 * ERP配置化异常处理器
 */
@RestControllerAdvice
@Slf4j
public class ErpConfigExceptionHandler {
    
    /**
     * ERP配置化业务异常
     */
    @ExceptionHandler(ErpConfigException.class)
    public R<?> handleErpConfigException(ErpConfigException e) {
        log.error("ERP配置化异常 - 模块: {}, 错误: {}", e.getModuleCode(), e.getMessage());
        
        return R.fail(String.format("配置错误[%s]: %s", e.getModuleCode(), e.getMessage()));
    }
    
    /**
     * 计算字段异常
     */
    @ExceptionHandler(ComputedFieldException.class)
    public R<?> handleComputedFieldException(ComputedFieldException e) {
        log.error("计算字段异常 - 模块: {}, 字段: {}, 公式: {}", 
            e.getModuleCode(), e.getFieldName(), e.getFormula());
        
        return R.fail(String.format("计算错误: 字段%s计算失败", e.getFieldName()));
    }
    
    /**
     * 权限异常
     */
    @ExceptionHandler(NotPermissionException.class)
    public R<?> handlePermissionException(NotPermissionException e) {
        log.warn("权限不足: {}", e.getMessage());
        return R.fail("权限不足,无法访问该资源");
    }
    
    /**
     * 配置解析异常
     */
    @ExceptionHandler(JSONException.class)
    public R<?> handleJsonException(JSONException e) {
        log.error("配置解析异常: {}", e.getMessage());
        return R.fail("配置格式错误,请检查配置内容");
    }
}
```

### 4.2 权限检查增强

```java
/**
 * 权限检查工具类
 */
@Component
public class ErpPermissionChecker {
    
    /**
     * 检查模块权限
     */
    public void checkModulePermission(String moduleCode, String operation) {
        // 1. 检查模块是否存在
        ErpPageConfig config = erpPageConfigService.getByModuleCode(moduleCode);
        if (config == null) {
            throw new ErpConfigException(moduleCode, "模块配置不存在");
        }
        
        // 2. 检查模块状态
        if (!"1".equals(config.getStatus())) {
            throw new ErpConfigException(moduleCode, "模块已禁用");
        }
        
        // 3. 检查操作权限
        String permission = String.format("erp:%s:%s", moduleCode, operation);
        if (!StpUtil.hasPermission(permission)) {
            throw new NotPermissionException(permission);
        }
    }
    
    /**
     * 检查数据权限
     */
    public void checkDataPermission(String moduleCode, Object dataId, String operation) {
        // 数据级权限检查逻辑
        // TODO: 实现数据权限检查
    }
}
```

---

## 🧹 五、冗余代码清理方案

### 5.1 重复代码识别

#### 5.1.1 Controller层重复代码

**优化前**:
```java
// 多个Controller中重复的代码
@GetMapping("/list")
public R<Page<Map<String, Object>>> list(PageQuery pageQuery) {
    String moduleCode = "sales_order";
    checkModulePermission(moduleCode, "query");
    Page<Map<String, Object>> page = superDataPermissionService
        .selectPageByModule(moduleCode, pageQuery, null);
    return R.ok(page);
}
```

**优化后**:
```java
// 使用模板方法模式
@GetMapping("/list")
public R<Page<Map<String, Object>>> list(PageQuery pageQuery) {
    return queryModuleData("sales_order", pageQuery, null);
}

// 基类方法
protected R<Page<Map<String, Object>>> queryModuleData(
        String moduleCode, 
        PageQuery pageQuery,
        QueryWrapper<Object> wrapper) {
    
    checkPermission(moduleCode, "query");
    Page<Map<String, Object>> page = superDataPermissionService
        .selectPageByModule(moduleCode, pageQuery, wrapper);
    
    JSONObject config = getConfig(moduleCode);
    page.setRecords(processData(page.getRecords(), config));
    
    return R.ok(page);
}
```

### 5.2 配置文件优化

#### 5.2.1 提取公共配置

```json
// common.config.json - 公共配置
{
  "commonConfig": {
    "pagination": {
      "pageSize": 20,
      "pageSizes": [10, 20, 50, 100]
    },
    "tableConfig": {
      "border": true,
      "stripe": true,
      "highlightCurrentRow": true
    },
    "formConfig": {
      "labelWidth": "120px",
      "size": "small"
    }
  }
}

// business.config.json - 业务配置(引用公共配置)
{
  "extends": "common.config.json",
  "tableConfig": {
    "columns": [
      // 业务特定列配置
    ]
  }
}
```

---

## 📊 六、实施计划

### 6.1 实施阶段

| 阶段 | 任务 | 工时 | 优先级 | 依赖 |
|------|------|------|--------|------|
| **阶段一** | 异常处理机制完善 | 4h | 🔴 高 | 无 |
| **阶段二** | 多字段计算引擎实现 | 8h | 🔴 高 | 阶段一 |
| **阶段三** | 虚拟字段映射实现 | 6h | 🔴 高 | 阶段一 |
| **阶段四** | 字典查询配置化优化 | 4h | 🟡 中 | 阶段二 |
| **阶段五** | 代码结构优化重构 | 3h | 🟢 低 | 阶段四 |
| **阶段六** | 冗余代码清理 | 3h | 🟢 低 | 阶段五 |

**总工时**: 28小时

### 6.2 验收标准

#### 阶段一验收标准:
- ✅ 所有异常都有明确的错误码和提示信息
- ✅ 权限检查覆盖所有接口
- ✅ 异常日志完整记录上下文信息

#### 阶段二验收标准:
- ✅ 支持SUM/AVG/COUNT等聚合函数
- ✅ 支持四则运算和复杂表达式
- ✅ 计算结果精度可控
- ✅ 计算失败不影响其他字段

#### 阶段三验收标准:
- ✅ 支持批量查询优化性能
- ✅ 支持Redis缓存
- ✅ 支持多种显示类型(text/link/tag)
- ✅ 虚拟字段解析失败不影响主数据

---

## 📝 七、总结

### 7.1 优化收益

**功能完善**:
- 多字段计算能力: 60% → 100% (+40%)
- 基础资料映射: 80% → 100% (+20%)
- 字典接口: 80% → 100% (+20%)

**代码质量**:
- 异常处理覆盖率: 60% → 95%
- 代码重复率: 30% → 10%
- 配置复用率: 40% → 80%

**性能优化**:
- 虚拟字段查询: 批量查询+缓存,性能提升50%
- 字典查询: 配置化统一,减少重复代码

### 7.2 后续规划

1. **监控告警**: 添加计算字段执行监控
2. **性能优化**: 实现计算结果缓存
3. **功能扩展**: 支持更多计算函数(IF/CASE/MAX/MIN等)
4. **可视化配置**: 提供计算公式可视化配置界面

---

**文档版本**: v1.0  
**最后更新**: 2026-03-24  
**维护人员**: ERP配置化开发团队
