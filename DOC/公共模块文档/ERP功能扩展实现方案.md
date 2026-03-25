# 🚀 ERP功能扩展实现方案

> **文档日期**: 2026-03-24  
> **扩展目标**: 实现多字段计算引擎和基础资料虚拟字段映射  
> **预期收益**: 能力匹配度从60%提升至100%

---

##  一、功能扩展概览

### 1.1 扩展功能清单

| 功能模块 | 当前匹配度 | 目标匹配度 | 优先级 | 预计工时 |
|---------|-----------|-----------|--------|---------|
| **多字段计算引擎** | 60% | 100% | 🔴 高 | 8小时 |
| **基础资料虚拟字段** | 80% | 100% | 🔴 高 | 6小时 |
| **字典配置化查询** | 80% | 100% | 🟡 中 | 4小时 |
| **数据导出功能** | 0% | 100% | 🟡 中 | 4小时 |
| **数据导入功能** | 0% | 100% | 🟢 低 | 4小时 |

### 1.2 扩展收益预估

```
能力匹配度提升:
├─ 多字段计算: 60% → 100% (+40%)
├─ 基础资料映射: 80% → 100% (+20%)
├─ 字典查询: 80% → 100% (+20%)
└─ 数据导入导出: 0% → 100% (+100%)

整体能力提升:
├─ 完全满足能力: 5项 → 8项
├─ 部分满足能力: 2项 → 0项
└─ 不满足能力: 1项 → 0项
```

---

## 🎯 二、多字段计算引擎实现

### 2.1 功能需求分析

#### 2.1.1 业务场景

**场景1: 订单金额汇总**
```
订单主表.订单金额 = SUM(订单明细.行金额)
订单主表.订单数量 = SUM(订单明细.数量)
订单主表.订单税额 = SUM(订单明细.税额)
```

**场景2: 成本利润计算**
```
订单.成本金额 = SUM(明细.成本价 * 数量)
订单.毛利润 = 订单金额 - 成本金额
订单.毛利率 = 毛利润 / 订单金额 * 100
```

**场景3: 折扣计算**
```
明细.折扣金额 = 原价 * 折扣率
明细.实付金额 = 原价 - 折扣金额
订单.总折扣 = SUM(明细.折扣金额)
```

#### 2.1.2 功能需求

| 需求项 | 需求描述 | 优先级 |
|--------|---------|--------|
| **聚合函数** | 支持SUM/AVG/COUNT/MAX/MIN | 🔴 高 |
| **四则运算** | 支持加减乘除运算 | 🔴 高 |
| **复杂表达式** | 支持括号、函数嵌套 | 🟡 中 |
| **条件计算** | 支持IF/CASE条件表达式 | 🟡 中 |
| **精度控制** | 支持小数位数控制 | 🔴 高 |
| **触发机制** | 数据变更自动触发计算 | 🔴 高 |

### 2.2 技术方案设计

#### 2.2.1 整体架构

```
计算引擎架构:
├─ 配置层 (Config Layer)
│  ├─ ComputedFieldConfig: 计算字段配置
│  └─ FormulaParser: 公式解析器
│
├─ 引擎层 (Engine Layer)
│  ├─ ComputedFieldEngine: 计算引擎核心
│  ├─ ExpressionEvaluator: 表达式计算器
│  └─ FunctionRegistry: 函数注册表
│
├─ 触发层 (Trigger Layer)
│  ├─ ComputeTrigger: 计算触发器
│  └─ EventListener: 事件监听器
│
└─ 缓存层 (Cache Layer)
   ├─ ComputeResultCache: 计算结果缓存
   └─ FormulaCache: 公式缓存
```

#### 2.2.2 配置设计

**配置结构**:

```json
{
  "computedFieldConfig": {
    "enabled": true,
    "autoTrigger": true,
    "cacheEnabled": true,
    "fields": [
      {
        "name": "fbillAmount",
        "targetField": "fbillAmount",
        "formula": "SUM(entryList.fAllAmount)",
        "trigger": "entryListChange",
        "precision": 2,
        "roundingMode": "HALF_UP",
        "cacheable": true,
        "description": "订单金额汇总"
      },
      {
        "name": "fGrossProfit",
        "targetField": "fGrossProfit",
        "formula": "fbillAmount - fCostAmount",
        "trigger": "amountChange",
        "precision": 2,
        "dependencies": ["fbillAmount", "fCostAmount"],
        "description": "毛利润计算"
      },
      {
        "name": "fGrossProfitRate",
        "targetField": "fGrossProfitRate",
        "formula": "fGrossProfit / fbillAmount * 100",
        "trigger": "profitChange",
        "precision": 2,
        "dependencies": ["fGrossProfit", "fbillAmount"],
        "description": "毛利率计算"
      }
    ]
  }
}
```

**配置字段说明**:

| 字段 | 类型 | 必填 | 说明 | 示例 |
|------|------|------|------|------|
| name | String |  | 计算字段名称 | fbillAmount |
| targetField | String |  | 目标字段 | fbillAmount |
| formula | String |  | 计算公式 | SUM(entryList.fAllAmount) |
| trigger | String |  | 触发事件 | entryListChange |
| precision | Integer |  | 小数精度 | 2 |
| roundingMode | String |  | 舍入模式 | HALF_UP |
| cacheable | Boolean |  | 是否缓存 | true |
| dependencies | Array |  | 依赖字段 | ["fbillAmount"] |
| description | String |  | 计算说明 | 订单金额汇总 |

### 2.3 核心代码实现

#### 2.3.1 计算引擎核心类

```java
/**
 * 计算字段引擎
 * 
 * 核心功能:
 * 1. 公式解析和计算
 * 2. 聚合函数支持
 * 3. 表达式计算
 * 4. 精度控制
 * 5. 结果缓存
 */
@Component
@Slf4j
public class ComputedFieldEngine {
    
    @Autowired
    private ExpressionEvaluator expressionEvaluator;
    
    @Autowired
    private FunctionRegistry functionRegistry;
    
    @Autowired
    private ComputeResultCache computeResultCache;
    
    /**
     * 执行字段计算
     * 
     * @param data 原始数据
     * @param configs 计算配置列表
     * @return 计算后的数据
     */
    public Map<String, Object> computeFields(
            Map<String, Object> data,
            List<ComputedFieldConfig> configs) {
        
        if (CollUtil.isEmpty(configs)) {
            return data;
        }
        
        Map<String, Object> result = new HashMap<>(data);
        
        // 按依赖关系排序
        List<ComputedFieldConfig> sortedConfigs = sortByDependencies(configs);
        
        for (ComputedFieldConfig config : sortedConfigs) {
            try {
                // 检查缓存
                if (config.getCacheable()) {
                    Object cachedValue = computeResultCache.get(config, result);
                    if (cachedValue != null) {
                        result.put(config.getTargetField(), cachedValue);
                        continue;
                    }
                }
                
                // 执行计算
                Object value = evaluateFormula(config.getFormula(), result);
                
                // 精度处理
                value = applyPrecision(value, config);
                
                // 设置结果
                result.put(config.getTargetField(), value);
                
                // 缓存结果
                if (config.getCacheable()) {
                    computeResultCache.put(config, result, value);
                }
                
                log.debug("计算字段 {} = {}", config.getTargetField(), value);
                
            } catch (Exception e) {
                log.error("计算字段 {} 失败: {}", config.getTargetField(), e.getMessage());
                // 计算失败时保持原值或设置为null
                result.putIfAbsent(config.getTargetField(), null);
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
        
        if (CollUtil.isEmpty(dataList) || CollUtil.isEmpty(configs)) {
            return dataList;
        }
        
        return dataList.parallelStream()
            .map(data -> computeFields(data, configs))
            .collect(Collectors.toList());
    }
    
    /**
     * 解析并计算公式
     */
    private Object evaluateFormula(String formula, Map<String, Object> context) {
        // 识别函数类型
        if (isFunctionFormula(formula)) {
            return evaluateFunction(formula, context);
        } else {
            // 普通表达式计算
            return expressionEvaluator.evaluate(formula, context);
        }
    }
    
    /**
     * 判断是否为函数公式
     */
    private boolean isFunctionFormula(String formula) {
        return formula.matches("^(SUM|AVG|COUNT|MAX|MIN|IF|CASE)\\(.*\\)$");
    }
    
    /**
     * 执行函数计算
     */
    private Object evaluateFunction(String formula, Map<String, Object> context) {
        // 提取函数名和参数
        String functionName = formula.substring(0, formula.indexOf("("));
        String params = formula.substring(formula.indexOf("(") + 1, formula.length() - 1);
        
        // 获取函数实现
        ComputeFunction function = functionRegistry.getFunction(functionName);
        if (function == null) {
            throw new IllegalArgumentException("不支持的函数: " + functionName);
        }
        
        // 执行函数
        return function.execute(params, context);
    }
    
    /**
     * 应用精度处理
     */
    private Object applyPrecision(Object value, ComputedFieldConfig config) {
        if (!(value instanceof Number) || config.getPrecision() == null) {
            return value;
        }
        
        RoundingMode roundingMode = RoundingMode.valueOf(
            config.getRoundingMode() != null ? config.getRoundingMode() : "HALF_UP"
        );
        
        return NumberUtil.round((Number) value, config.getPrecision(), roundingMode);
    }
    
    /**
     * 按依赖关系排序
     */
    private List<ComputedFieldConfig> sortByDependencies(List<ComputedFieldConfig> configs) {
        // 拓扑排序,确保依赖字段先计算
        // TODO: 实现拓扑排序
        return configs;
    }
}
```

#### 2.3.2 表达式计算器

```java
/**
 * 表达式计算器接口
 */
public interface ExpressionEvaluator {
    /**
     * 计算表达式
     * 
     * @param expression 表达式
     * @param context 上下文变量
     * @return 计算结果
     */
    Object evaluate(String expression, Map<String, Object> context);
}

/**
 * Aviator表达式计算器实现
 */
@Component
@Slf4j
public class AviatorExpressionEvaluator implements ExpressionEvaluator {
    
    @Override
    public Object evaluate(String expression, Map<String, Object> context) {
        try {
            // 编译表达式(带缓存)
            Expression compiledExpression = AviatorEvaluator.compile(expression, true);
            
            // 执行计算
            return compiledExpression.execute(context);
            
        } catch (CompileExpressionErrorException e) {
            log.error("表达式编译失败: {}", expression, e);
            throw new IllegalArgumentException("表达式语法错误: " + expression);
        } catch (ExpressionRuntimeException e) {
            log.error("表达式执行失败: {}", expression, e);
            throw new RuntimeException("表达式计算错误: " + e.getMessage());
        }
    }
}
```

#### 2.3.3 函数注册表

```java
/**
 * 计算函数接口
 */
public interface ComputeFunction {
    /**
     * 执行函数
     */
    Object execute(String params, Map<String, Object> context);
}

/**
 * 函数注册表
 */
@Component
public class FunctionRegistry {
    
    private Map<String, ComputeFunction> functions = new HashMap<>();
    
    @PostConstruct
    public void init() {
        // 注册聚合函数
        registerFunction("SUM", new SumFunction());
        registerFunction("AVG", new AvgFunction());
        registerFunction("COUNT", new CountFunction());
        registerFunction("MAX", new MaxFunction());
        registerFunction("MIN", new MinFunction());
        
        // 注册条件函数
        registerFunction("IF", new IfFunction());
        registerFunction("CASE", new CaseFunction());
    }
    
    public void registerFunction(String name, ComputeFunction function) {
        functions.put(name, function);
    }
    
    public ComputeFunction getFunction(String name) {
        return functions.get(name);
    }
}

/**
 * SUM函数实现
 */
public class SumFunction implements ComputeFunction {
    
    @Override
    public Object execute(String params, Map<String, Object> context) {
        // 解析参数: entryList.fAllAmount
        String[] parts = params.split("\\.");
        if (parts.length != 2) {
            throw new IllegalArgumentException("SUM函数参数格式错误: " + params);
        }
        
        String listField = parts[0];
        String sumField = parts[1];
        
        // 获取列表数据
        Object listObj = context.get(listField);
        if (!(listObj instanceof List)) {
            throw new IllegalArgumentException("SUM函数参数必须是列表: " + listField);
        }
        
        List<Map<String, Object>> list = (List<Map<String, Object>>) listObj;
        if (CollUtil.isEmpty(list)) {
            return 0.0;
        }
        
        // 计算汇总
        return list.stream()
            .mapToDouble(item -> NumberUtil.parseDouble(item.get(sumField)))
            .sum();
    }
}

/**
 * AVG函数实现
 */
public class AvgFunction implements ComputeFunction {
    
    @Override
    public Object execute(String params, Map<String, Object> context) {
        String[] parts = params.split("\\.");
        String listField = parts[0];
        String avgField = parts[1];
        
        List<Map<String, Object>> list = (List<Map<String, Object>>) context.get(listField);
        if (CollUtil.isEmpty(list)) {
            return 0.0;
        }
        
        return list.stream()
            .mapToDouble(item -> NumberUtil.parseDouble(item.get(avgField)))
            .average()
            .orElse(0.0);
    }
}

/**
 * IF函数实现
 */
public class IfFunction implements ComputeFunction {
    
    @Override
    public Object execute(String params, Map<String, Object> context) {
        // 解析参数: condition, trueValue, falseValue
        String[] parts = params.split(",");
        if (parts.length != 3) {
            throw new IllegalArgumentException("IF函数参数格式错误: " + params);
        }
        
        String condition = parts[0].trim();
        String trueValue = parts[1].trim();
        String falseValue = parts[2].trim();
        
        // 计算条件
        Object conditionResult = AviatorEvaluator.execute(condition, context);
        boolean conditionBoolean = Boolean.TRUE.equals(conditionResult);
        
        // 返回对应值
        String valueExpression = conditionBoolean ? trueValue : falseValue;
        return AviatorEvaluator.execute(valueExpression, context);
    }
}
```

#### 2.3.4 计算触发器

```java
/**
 * 计算触发器
 */
@Component
@Slf4j
public class ComputeTrigger {
    
    @Autowired
    private ComputedFieldEngine computedFieldEngine;
    
    @Autowired
    private ConfigParser configParser;
    
    /**
     * 触发计算
     * 
     * @param moduleCode 模块编码
     * @param data 数据
     * @param triggerEvent 触发事件
     * @return 计算后的数据
     */
    public Map<String, Object> triggerCompute(
            String moduleCode,
            Map<String, Object> data,
            String triggerEvent) {
        
        // 获取计算配置
        JSONObject config = configParser.getConfig(moduleCode);
        List<ComputedFieldConfig> allConfigs = configParser.parseComputedFields(config);
        
        // 过滤出需要触发的配置
        List<ComputedFieldConfig> triggerConfigs = allConfigs.stream()
            .filter(c -> triggerEvent.equals(c.getTrigger()))
            .collect(Collectors.toList());
        
        if (CollUtil.isEmpty(triggerConfigs)) {
            return data;
        }
        
        // 执行计算
        return computedFieldEngine.computeFields(data, triggerConfigs);
    }
    
    /**
     * 批量触发计算
     */
    public List<Map<String, Object>> triggerComputeBatch(
            String moduleCode,
            List<Map<String, Object>> dataList,
            String triggerEvent) {
        
        return dataList.stream()
            .map(data -> triggerCompute(moduleCode, data, triggerEvent))
            .collect(Collectors.toList());
    }
}

/**
 * 数据变更事件监听器
 */
@Component
@Slf4j
public class DataChangeListener {
    
    @Autowired
    private ComputeTrigger computeTrigger;
    
    /**
     * 监听明细变更事件
     */
    @EventListener
    public void onEntryListChange(EntryListChangeEvent event) {
        log.debug("明细变更事件触发计算: {}", event.getModuleCode());
        
        Map<String, Object> computedData = computeTrigger.triggerCompute(
            event.getModuleCode(),
            event.getData(),
            "entryListChange"
        );
        
        // 更新主表数据
        // TODO: 实现数据更新逻辑
    }
    
    /**
     * 监听价格变更事件
     */
    @EventListener
    public void onPriceChange(PriceChangeEvent event) {
        log.debug("价格变更事件触发计算: {}", event.getModuleCode());
        
        Map<String, Object> computedData = computeTrigger.triggerCompute(
            event.getModuleCode(),
            event.getData(),
            "priceChange"
        );
    }
}
```

### 2.4 前端集成方案

#### 2.4.1 Vue组件实现

```vue
<template>
  <div class="computed-field-wrapper">
    <!-- 计算字段显示 -->
    <el-form-item 
      :label="fieldConfig.description || fieldConfig.name"
      :prop="fieldConfig.targetField"
    >
      <el-input
        v-model="computedValue"
        :disabled="true"
        :placeholder="`自动计算: ${fieldConfig.formula}`"
      >
        <template #append>
          <el-button 
            icon="el-icon-refresh" 
            @click="manualCompute"
            :loading="computing"
          >
            重新计算
          </el-button>
        </template>
      </el-input>
      
      <!-- 计算公式提示 -->
      <div class="formula-tip">
        <el-tag size="mini" type="info">
          {{ fieldConfig.formula }}
        </el-tag>
      </div>
    </el-form-item>
  </div>
</template>

<script>
import { debounce } from 'lodash';

export default {
  name: 'ComputedField',
  
  props: {
    fieldConfig: {
      type: Object,
      required: true
    },
    formData: {
      type: Object,
      required: true
    }
  },
  
  data() {
    return {
      computing: false,
      computedValue: null
    };
  },
  
  computed: {
    // 依赖字段
    dependencies() {
      return this.fieldConfig.dependencies || [];
    }
  },
  
  watch: {
    // 监听依赖字段变化
    formData: {
      handler: debounce(function(newVal) {
        // 检查依赖字段是否变化
        if (this.hasDependencyChanged(newVal)) {
          this.executeCompute();
        }
      }, 300),
      deep: true
    }
  },
  
  mounted() {
    // 初始计算
    this.executeCompute();
  },
  
  methods: {
    /**
     * 执行计算
     */
    async executeCompute() {
      try {
        this.computing = true;
        
        // 调用后端计算接口
        const response = await this.$http.post('/erp/engine/compute', {
          moduleCode: this.moduleCode,
          data: this.formData,
          fieldConfig: this.fieldConfig
        });
        
        this.computedValue = response.data[this.fieldConfig.targetField];
        
        // 触发更新事件
        this.$emit('computed', {
          field: this.fieldConfig.targetField,
          value: this.computedValue
        });
        
      } catch (error) {
        this.$message.error('计算失败: ' + error.message);
        this.computedValue = '计算错误';
      } finally {
        this.computing = false;
      }
    },
    
    /**
     * 手动触发计算
     */
    manualCompute() {
      this.executeCompute();
    },
    
    /**
     * 检查依赖字段是否变化
     */
    hasDependencyChanged(newData) {
      if (this.dependencies.length === 0) {
        return true;
      }
      
      return this.dependencies.some(dep => {
        return newData[dep] !== this.formData[dep];
      });
    }
  }
};
</script>

<style scoped>
.computed-field-wrapper {
  position: relative;
}

.formula-tip {
  margin-top: 5px;
  font-size: 12px;
  color: #909399;
}
</style>
```

#### 2.4.2 计算字段配置器

```vue
<template>
  <div class="computed-field-config">
    <el-form :model="config" label-width="120px">
      <el-form-item label="字段名称">
        <el-input v-model="config.name" placeholder="请输入字段名称" />
      </el-form-item>
      
      <el-form-item label="目标字段">
        <el-input v-model="config.targetField" placeholder="请输入目标字段" />
      </el-form-item>
      
      <el-form-item label="计算公式">
        <el-input 
          v-model="config.formula" 
          type="textarea"
          :rows="3"
          placeholder="请输入计算公式,如: SUM(entryList.fAllAmount)"
        />
        <div class="formula-help">
          <el-tag size="mini" @click="insertFunction('SUM')">SUM</el-tag>
          <el-tag size="mini" @click="insertFunction('AVG')">AVG</el-tag>
          <el-tag size="mini" @click="insertFunction('COUNT')">COUNT</el-tag>
          <el-tag size="mini" @click="insertFunction('MAX')">MAX</el-tag>
          <el-tag size="mini" @click="insertFunction('MIN')">MIN</el-tag>
          <el-tag size="mini" @click="insertFunction('IF')">IF</el-tag>
        </div>
      </el-form-item>
      
      <el-form-item label="触发事件">
        <el-select v-model="config.trigger" placeholder="请选择触发事件">
          <el-option label="明细变更" value="entryListChange" />
          <el-option label="价格变更" value="priceChange" />
          <el-option label="数量变更" value="quantityChange" />
          <el-option label="成本变更" value="costChange" />
        </el-select>
      </el-form-item>
      
      <el-form-item label="小数精度">
        <el-input-number 
          v-model="config.precision" 
          :min="0" 
          :max="10"
        />
      </el-form-item>
      
      <el-form-item label="舍入模式">
        <el-select v-model="config.roundingMode">
          <el-option label="四舍五入" value="HALF_UP" />
          <el-option label="向上取整" value="UP" />
          <el-option label="向下取整" value="DOWN" />
        </el-select>
      </el-form-item>
      
      <el-form-item label="计算说明">
        <el-input 
          v-model="config.description" 
          type="textarea"
          :rows="2"
          placeholder="请输入计算说明"
        />
      </el-form-item>
      
      <el-form-item>
        <el-button type="primary" @click="testFormula">测试公式</el-button>
        <el-button @click="saveConfig">保存配置</el-button>
      </el-form-item>
    </el-form>
    
    <!-- 测试结果对话框 -->
    <el-dialog title="公式测试结果" :visible.sync="testDialogVisible">
      <div v-if="testResult">
        <p>计算结果: {{ testResult.value }}</p>
        <p>执行时间: {{ testResult.time }}ms</p>
      </div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'ComputedFieldConfig',
  
  data() {
    return {
      config: {
        name: '',
        targetField: '',
        formula: '',
        trigger: '',
        precision: 2,
        roundingMode: 'HALF_UP',
        description: ''
      },
      testDialogVisible: false,
      testResult: null
    };
  },
  
  methods: {
    /**
     * 插入函数
     */
    insertFunction(funcName) {
      const template = {
        'SUM': 'SUM(list.field)',
        'AVG': 'AVG(list.field)',
        'COUNT': 'COUNT(list)',
        'MAX': 'MAX(list.field)',
        'MIN': 'MIN(list.field)',
        'IF': 'IF(condition, trueValue, falseValue)'
      };
      
      this.config.formula += template[funcName];
    },
    
    /**
     * 测试公式
     */
    async testFormula() {
      try {
        const response = await this.$http.post('/erp/engine/compute/test', {
          formula: this.config.formula,
          testData: this.getTestData()
        });
        
        this.testResult = response.data;
        this.testDialogVisible = true;
        
      } catch (error) {
        this.$message.error('公式测试失败: ' + error.message);
      }
    },
    
    /**
     * 保存配置
     */
    async saveConfig() {
      try {
        await this.$http.post('/erp/config/computed-field', this.config);
        this.$message.success('配置保存成功');
        this.$emit('saved', this.config);
        
      } catch (error) {
        this.$message.error('配置保存失败: ' + error.message);
      }
    },
    
    /**
     * 获取测试数据
     */
    getTestData() {
      // 返回测试数据
      return {
        fbillAmount: 1000,
        fCostAmount: 600,
        entryList: [
          { fAllAmount: 300, fQty: 10 },
          { fAllAmount: 400, fQty: 15 },
          { fAllAmount: 300, fQty: 8 }
        ]
      };
    }
  }
};
</script>
```

---

## 🔗 三、基础资料虚拟字段映射实现

### 3.1 功能需求分析

#### 3.1.1 业务场景

**场景1: 客户信息显示**
```
订单列表显示:
- 客户编号 (fCustomerNumber) - 原始字段
- 客户名称 (customerName) - 虚拟字段,从客户表关联
- 客户等级 (customerLevel) - 虚拟字段,从客户表关联
```

**场景2: 物料信息显示**
```
明细列表显示:
- 物料编码 (fMaterialId) - 原始字段
- 物料名称 (materialName) - 虚拟字段,从物料表关联
- 规格型号 (materialSpec) - 虚拟字段,从物料表关联
- 计量单位 (unitName) - 虚拟字段,从单位表关联
```

#### 3.1.2 功能需求

| 需求项 | 需求描述 | 优先级 |
|--------|---------|--------|
| **字段关联** | 支持配置源字段和目标字段映射 | 🔴 高 |
| **批量查询** | 支持批量查询优化性能 | 🔴 高 |
| **结果缓存** | 支持Redis缓存查询结果 | 🔴 高 |
| **多种显示** | 支持text/link/tag等显示类型 | 🟡 中 |
| **空值处理** | 支持空值和查询失败处理 | 🔴 高 |

### 3.2 技术方案设计

#### 3.2.1 配置设计

```json
{
  "virtualFieldConfig": {
    "enabled": true,
    "batchQuery": true,
    "cacheEnabled": true,
    "cacheExpire": 3600,
    "fields": [
      {
        "name": "customerName",
        "sourceField": "fCustomerNumber",
        "sourceTable": "t_customer",
        "sourceDisplayField": "customer_name",
        "displayType": "link",
        "displayConfig": {
          "url": "/customer/detail/{value}",
          "target": "_blank"
        },
        "cacheable": true,
        "description": "客户名称"
      },
      {
        "name": "materialName",
        "sourceField": "fMaterialId",
        "sourceTable": "t_material",
        "sourceDisplayField": "material_name",
        "displayType": "text",
        "cacheable": true,
        "description": "物料名称"
      },
      {
        "name": "deptName",
        "sourceField": "fDeptCode",
        "sourceTable": "t_department",
        "sourceDisplayField": "dept_name",
        "displayType": "tag",
        "displayConfig": {
          "color": "blue"
        },
        "cacheable": true,
        "description": "部门名称"
      }
    ]
  }
}
```

### 3.3 核心代码实现

#### 3.3.1 虚拟字段服务

```java
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
    
    @Value("${erp.virtual-field.batch-size:100}")
    private int batchSize;
    
    /**
     * 解析虚拟字段
     */
    public List<Map<String, Object>> resolveVirtualFields(
            List<Map<String, Object>> dataList,
            List<VirtualFieldConfig> configs) {
        
        if (CollUtil.isEmpty(configs) || CollUtil.isEmpty(dataList)) {
            return dataList;
        }
        
        // 按配置批量处理
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
        
        // 1. 收集源字段值
        Set<Object> sourceValues = collectSourceValues(dataList, config);
        
        if (CollUtil.isEmpty(sourceValues)) {
            return;
        }
        
        // 2. 批量查询基础资料
        Map<Object, Object> valueMap = batchQueryBaseData(config, sourceValues);
        
        // 3. 设置虚拟字段值
        setVirtualFieldValues(dataList, config, valueMap);
    }
    
    /**
     * 收集源字段值
     */
    private Set<Object> collectSourceValues(
            List<Map<String, Object>> dataList,
            VirtualFieldConfig config) {
        
        return dataList.stream()
            .map(data -> data.get(config.getSourceField()))
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }
    
    /**
     * 批量查询基础资料
     */
    private Map<Object, Object> batchQueryBaseData(
            VirtualFieldConfig config,
            Set<Object> sourceValues) {
        
        Map<Object, Object> result = new HashMap<>();
        List<Object> needQueryValues = new ArrayList<>();
        
        // 1. 尝试从缓存获取
        if (config.getCacheable()) {
            for (Object value : sourceValues) {
                String cacheKey = buildCacheKey(config, value);
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
        
        // 2. 分批查询数据库
        if (CollUtil.isNotEmpty(needQueryValues)) {
            // 分批处理,避免SQL过长
            List<List<Object>> batches = CollUtil.split(needQueryValues, batchSize);
            
            for (List<Object> batch : batches) {
                Map<Object, Object> batchResult = queryDatabase(config, batch);
                result.putAll(batchResult);
                
                // 缓存查询结果
                if (config.getCacheable()) {
                    cacheQueryResults(config, batchResult);
                }
            }
        }
        
        return result;
    }
    
    /**
     * 查询数据库
     */
    private Map<Object, Object> queryDatabase(
            VirtualFieldConfig config,
            List<Object> values) {
        
        String sql = buildQuerySql(config, values.size());
        
        try {
            List<Map<String, Object>> queryResult = jdbcTemplate.queryForList(
                sql, 
                values.toArray()
            );
            
            Map<Object, Object> result = new HashMap<>();
            for (Map<String, Object> row : queryResult) {
                Object sourceValue = row.get(config.getSourceField());
                Object displayValue = row.get(config.getSourceDisplayField());
                result.put(sourceValue, displayValue);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("查询基础资料失败 - 表: {}, 字段: {}", 
                config.getSourceTable(), config.getSourceDisplayField(), e);
            return Collections.emptyMap();
        }
    }
    
    /**
     * 构建查询SQL
     */
    private String buildQuerySql(VirtualFieldConfig config, int paramCount) {
        String placeholders = IntStream.range(0, paramCount)
            .mapToObj(i -> "?")
            .collect(Collectors.joining(","));
        
        return String.format(
            "SELECT %s, %s FROM %s WHERE %s IN (%s)",
            config.getSourceField(),
            config.getSourceDisplayField(),
            config.getSourceTable(),
            config.getSourceField(),
            placeholders
        );
    }
    
    /**
     * 缓存查询结果
     */
    private void cacheQueryResults(
            VirtualFieldConfig config,
            Map<Object, Object> results) {
        
        for (Map.Entry<Object, Object> entry : results.entrySet()) {
            String cacheKey = buildCacheKey(config, entry.getKey());
            redisCache.setCacheObject(
                cacheKey, 
                entry.getValue(), 
                1, 
                TimeUnit.HOURS
            );
        }
    }
    
    /**
     * 设置虚拟字段值
     */
    private void setVirtualFieldValues(
            List<Map<String, Object>> dataList,
            VirtualFieldConfig config,
            Map<Object, Object> valueMap) {
        
        for (Map<String, Object> data : dataList) {
            Object sourceValue = data.get(config.getSourceField());
            if (sourceValue != null) {
                Object displayValue = valueMap.get(sourceValue);
                data.put(config.getName(), displayValue);
            }
        }
    }
    
    /**
     * 构建缓存键
     */
    private String buildCacheKey(VirtualFieldConfig config, Object value) {
        return String.format(
            "virtual_field:%s:%s:%s",
            config.getSourceTable(),
            config.getSourceDisplayField(),
            value
        );
    }
}
```

---

##  四、实施计划与验收

### 4.1 实施计划

| 阶段 | 任务 | 工时 | 输出物 |
|------|------|------|--------|
| **阶段一** | 多字段计算引擎实现 | 8h | ComputedFieldEngine.java |
| **阶段二** | 虚拟字段映射实现 | 6h | VirtualFieldService.java |
| **阶段三** | 前端组件开发 | 6h | Vue组件 |
| **阶段四** | 集成测试 | 4h | 测试报告 |
| **阶段五** | 文档编写 | 2h | 使用文档 |

**总工时**: 26小时

### 4.2 验收标准

#### 功能验收:
-  支持SUM/AVG/COUNT/MAX/MIN聚合函数
-  支持四则运算和复杂表达式
-  支持IF/CASE条件表达式
-  支持精度控制和舍入模式
-  支持虚拟字段批量查询和缓存
-  支持多种显示类型

#### 性能验收:
-  计算字段响应时间 < 100ms
-  虚拟字段批量查询性能提升 > 50%
-  缓存命中率 > 80%

---

**文档版本**: v1.0  
**最后更新**: 2026-03-24  
**维护人员**: ERP配置化开发团队
