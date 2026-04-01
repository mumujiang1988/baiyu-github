# JdbcTemplate + SqlBuilder 接口配置化改造方案

**版本：** v1.0  
**生成时间：** 2026-04-01  
**架构状态：** JSON 配置驱动 ✅

---

## 📋 目录

1. [改造背景](#1-改造背景)
2. [核心目标](#2-核心目标)
3. [技术架构](#3-技术架构)
4. [JSON 配置模型](#4-json 配置模型)
5. [SqlBuilder 增强](#5-sqlbuilder 增强)
6. [标准实现模式](#6-标准实现模式)
7. [完整示例](#7-完整示例)
8. [最佳实践](#8-最佳实践)

---

## 1. 改造背景

### 1.1 现状分析

**当前问题：**
- ❌ 字典接口使用 MyBatis 注解，SQL 硬编码在 Java 代码中
- ❌ 每个接口方法都需要手动编写 SQL
- ❌ 查询条件、字段映射、排序规则分散在各个方法中
- ❌ 缺乏统一的配置管理机制

**示例对比：**

```java
// ❌ 当前方式 - MyBatis 注解
@Select("<script>" +
        "SELECT dict_label AS label, dict_value AS value " +
        "FROM sys_dict_data " +
        "<if test='dictType != null'>WHERE dict_type = #{dictType}</if> " +
        "ORDER BY dict_sort" +
        "</script>")
List<Map<String, Object>> selectSysDictData(@Param("dictType") String dictType);

// ✅ 目标方式 - JSON 配置驱动
@GetMapping("/dict/{dictType}")
public ErpResponse<List<Map<String, Object>>> getDict(@PathVariable String dictType) {
    // JSON 配置存储在数据库或配置文件中
    String configJson = pageConfigService.getPageConfig("customer_category");
    QueryConfig config = JSON.parseObject(configJson, QueryConfig.class);
    
    // 动态构建查询
    List<Map<String, Object>> result = dynamicQueryEngine.execute(config);
    return ErpResponse.ok(result);
}
```

### 1.2 改造必要性

1. **低代码需求**: JSON 配置驱动是低代码平台的核心
2. **可维护性**: 配置与代码分离，便于修改和维护
3. **可扩展性**: 新增查询无需修改代码，只需更新配置
4. **统一规范**: 所有接口采用相同的配置化模式

---

## 2. 核心目标

### 2.1 改造目标

| 目标 | 说明 | 优先级 |
|------|------|--------|
| **配置化** | 所有查询条件、字段映射、排序规则使用 JSON 配置 | P0 |
| **动态化** | 基于配置动态生成 SQL，无需硬编码 | P0 |
| **统一化** | 所有接口采用相同的配置结构和调用方式 | P0 |
| **可视化** | 配置可通过管理界面编辑和预览 | P1 |
| **缓存优化** | 配置数据支持 Redis 缓存 | P1 |

### 2.2 改造范围

**需要改造的接口类型：**
- ✅ 字典查询接口（`ErpDictionaryController`）
- ✅ 页面配置接口（`ErpPageConfigController`）
- ✅ 审批流程接口（`ErpApprovalFlowController`）
- ✅ 下推关系接口（`ErpPushRelationController`）
- ✅ 其他 CRUD 接口

**不需要改造的部分：**
- ⚠️ 复杂业务逻辑（仍需手写 SQL）
- ⚠️ 多表关联查询（视情况而定）
- ⚠️ 性能敏感的热点接口

---

## 3. 技术架构

### 3.1 整体架构

```
┌─────────────────────────────────────────────────────┐
│                   前端 Vue3                          │
│              配置化页面 / 动态表单                     │
└───────────────────┬─────────────────────────────────┘
                    │ HTTP Request
┌───────────────────▼─────────────────────────────────┐
│                 Controller 层                        │
│  ┌─────────────────────────────────────────────┐   │
│  │  @GetMapping("/dict/{dictType}")            │   │
│  │  public ErpResponse<?> getDict(...)         │   │
│  └─────────────────────────────────────────────┘   │
└───────────────────┬─────────────────────────────────┘
                    │ 调用
┌───────────────────▼─────────────────────────────────┐
│              DynamicQueryEngine                      │
│  ┌─────────────────────────────────────────────┐   │
│  │  1. 加载 JSON 配置                              │   │
│  │  2. 解析查询条件                             │   │
│  │  3. 使用 SqlBuilder 构建 SQL                  │   │
│  │  4. 执行查询并返回结果                       │   │
│  └─────────────────────────────────────────────┘   │
└───────────────────┬─────────────────────────────────┘
                    │ 依赖
┌───────────────────▼─────────────────────────────────┐
│           SqlBuilder + JdbcTemplate                  │
│  ┌─────────────┐  ┌─────────────┐                  │
│  │ SqlBuilder  │  │JdbcTemplate │                  │
│  │ (SQL 构建)   │  │ (SQL 执行)   │                  │
│  └─────────────┘  └─────────────┘                  │
└───────────────────┬─────────────────────────────────┘
                    │ JDBC
┌───────────────────▼─────────────────────────────────┐
│                   MySQL 数据库                        │
│  ┌─────────────┐  ┌─────────────┐                  │
│  │erp_page_config│  │业务表      │                  │
│  │(存储配置)    │  │(数据表)     │                  │
│  └─────────────┘  └─────────────┘                  │
└─────────────────────────────────────────────────────┘
```

### 3.2 核心组件

#### 3.2.1 DynamicQueryEngine（动态查询引擎）

**职责：**
- 加载 JSON 配置
- 解析查询条件
- 构建动态 SQL
- 执行查询并返回结果

**核心方法：**
```java
public class DynamicQueryEngine {
    
    /**
     * 执行动态查询
     * @param config 查询配置
     * @return 查询结果
     */
    public List<Map<String, Object>> execute(QueryConfig config) {
        // 1. 构建 WHERE 条件
        SqlResult whereSql = sqlBuilder.buildWhere(config.getConditions());
        
        // 2. 构建 ORDER BY
        SqlResult orderSql = sqlBuilder.buildOrderBy(config.getOrderBy());
        
        // 3. 构建完整 SQL
        String sql = buildSql(config, whereSql, orderSql);
        
        // 4. 执行查询
        return jdbcTemplate.queryForList(sql, getParams(config, whereSql));
    }
}
```

#### 3.2.2 QueryConfig（查询配置模型）

**JSON 结构：**
```json
{
  "moduleCode": "customer_category",
  "tableName": "sys_dict_data",
  "fields": ["dict_label", "dict_value", "dict_type"],
  "conditions": [
    {
      "field": "dict_type",
      "operator": "eq",
      "value": "${dictType}"
    }
  ],
  "orderBy": [
    {
      "field": "dict_sort",
      "direction": "ASC"
    }
  ],
  "cacheable": true,
  "cacheExpire": 3600
}
```

---

## 4. JSON 配置模型

### 4.1 完整配置结构

```typescript
interface QueryConfig {
  /**
   * 模块编码（唯一标识）
   */
  moduleCode: string;
  
  /**
   * 表名
   */
  tableName: string;
  
  /**
   * 查询字段列表
   */
  fields?: string[];
  
  /**
   * 查询条件列表
   */
  conditions?: QueryCondition[];
  
  /**
   * 排序规则列表
   */
  orderBy?: OrderRule[];
  
  /**
   * 是否支持缓存
   */
  cacheable?: boolean;
  
  /**
   * 缓存过期时间（秒）
   */
  cacheExpire?: number;
  
  /**
   * 备注说明
   */
  remark?: string;
}

/**
 * 查询条件
 */
interface QueryCondition {
  /**
   * 字段名
   */
  field: string;
  
  /**
   * 运算符
   */
  operator: 'eq' | 'ne' | 'gt' | 'ge' | 'lt' | 'le' | 
            'like' | 'left_like' | 'right_like' | 
            'in' | 'between' | 'isNull' | 'isNotNull';
  
  /**
   * 值（支持模板变量）
   */
  value: any;
  
  /**
   * 是否必填
   */
  required?: boolean;
}

/**
 * 排序规则
 */
interface OrderRule {
  /**
   * 字段名
   */
  field: string;
  
  /**
   * 排序方向
   */
  direction: 'ASC' | 'DESC';
}
```

### 4.2 配置示例

#### 示例 1：客户分类字典查询

```json
{
  "moduleCode": "customer_category_dict",
  "tableName": "sys_dict_data",
  "fields": [
    "dict_label AS label",
    "dict_value AS value",
    "dict_type AS type"
  ],
  "conditions": [
    {
      "field": "dict_type",
      "operator": "eq",
      "value": "${dictType}",
      "required": true
    },
    {
      "field": "status",
      "operator": "eq",
      "value": "1"
    }
  ],
  "orderBy": [
    {
      "field": "dict_sort",
      "direction": "ASC"
    }
  ],
  "cacheable": true,
  "cacheExpire": 3600,
  "remark": "客户分类字典查询配置"
}
```

#### 示例 2：销售人员查询

```json
{
  "moduleCode": "salespersons_dict",
  "tableName": "sys_user u",
  "joins": [
    {
      "type": "LEFT JOIN",
      "table": "sys_dept d",
      "on": "u.dept_id = d.dept_id"
    },
    {
      "type": "LEFT JOIN",
      "table": "sys_employee e",
      "on": "u.staff_id = e.fid"
    }
  ],
  "fields": [
    "u.nick_name AS label",
    "COALESCE(e.salesman_id, CAST(u.user_id AS CHAR)) AS value",
    "'salespersons' AS type",
    "d.dept_name AS departmentName",
    "e.salesman_id AS FSalerId"
  ],
  "conditions": [
    {
      "field": "u.status",
      "operator": "eq",
      "value": "1"
    },
    {
      "field": "u.del_flag",
      "operator": "eq",
      "value": "0"
    }
  ],
  "whereClause": "(u.dept_id IN ('1995775271620800513', '1995776039019048962') OR ur.role_id IN (1, 2016378335548186625))",
  "groupBy": [
    "u.user_id",
    "u.nick_name",
    "u.staff_id",
    "d.dept_name",
    "e.salesman_id"
  ],
  "orderBy": [
    {
      "field": "u.nick_name",
      "direction": "ASC"
    }
  ],
  "cacheable": true,
  "cacheExpire": 1800
}
```

#### 示例 3：分页查询配置

```json
{
  "moduleCode": "sale_order_list",
  "tableName": "k3_sale_order",
  "fields": [
    "FBillNo",
    "FDate",
    "FSaleOrgId",
    "FCustId",
    "FAllAmount",
    "FDocumentStatus"
  ],
  "conditions": [
    {
      "field": "FDocumentStatus",
      "operator": "eq",
      "value": "${status}",
      "required": false
    },
    {
      "field": "FDate",
      "operator": "between",
      "value": ["${startDate}", "${endDate}"],
      "required": false
    }
  ],
  "orderBy": [
    {
      "field": "FCreateDate",
      "direction": "DESC"
    }
  ],
  "pagination": {
    "enabled": true,
    "defaultPageSize": 20,
    "maxPageSize": 100
  },
  "cacheable": false
}
```

---

## 5. SqlBuilder 增强

### 5.1 现有功能

SqlBuilder 已支持的功能：
- ✅ 动态 WHERE 条件构建
- ✅ ORDER BY 子句构建
- ✅ SELECT 字段选择
- ✅ 字段名校验（防 SQL 注入）
- ✅ LIKE 特殊字符转义

### 5.2 需要增强的功能

#### 5.2.1 支持 JOIN 子句

```java
/**
 * 构建 JOIN 子句
 */
public SqlResult buildJoin(List<JoinConfig> joins) {
    if (joins == null || joins.isEmpty()) {
        return new SqlResult("", Collections.emptyList());
    }
    
    StringBuilder sql = new StringBuilder();
    List<Object> params = new ArrayList<>();
    
    for (JoinConfig join : joins) {
        sql.append(" ").append(join.getType()).append(" ")
           .append(join.getTable()).append(" ")
           .append(join.getAlias()).append(" ")
           .append("ON ").append(join.getOn());
    }
    
    return new SqlResult(sql.toString(), params);
}
```

#### 5.2.2 支持 GROUP BY 子句

```java
/**
 * 构建 GROUP BY 子句
 */
public SqlResult buildGroupBy(List<String> fields) {
    if (fields == null || fields.isEmpty()) {
        return new SqlResult("", Collections.emptyList());
    }
    
    StringBuilder sql = new StringBuilder(" GROUP BY ");
    
    for (int i = 0; i < fields.size(); i++) {
        validateFieldName(fields.get(i));
        if (i > 0) sql.append(", ");
        sql.append(fields.get(i));
    }
    
    return new SqlResult(sql.toString(), Collections.emptyList());
}
```

#### 5.2.3 支持 HAVING 子句

```java
/**
 * 构建 HAVING 子句
 */
public SqlResult buildHaving(List<Map<String, Object>> conditions) {
    // 类似 buildWhere，但用于聚合后的过滤
    StringBuilder sql = new StringBuilder();
    List<Object> params = new ArrayList<>();
    
    // ... 实现逻辑
    return new SqlResult(sql.toString(), params);
}
```

#### 5.2.4 支持 LIMIT/OFFSET

```java
/**
 * 构建分页子句
 */
public String buildLimit(int limit, int offset) {
    return String.format(" LIMIT %d OFFSET %d", limit, offset);
}
```

---

## 6. 标准实现模式

### 6.1 Controller 层

```java
package com.ruoyi.erp.controller.erp;

import com.ruoyi.erp.domain.response.ErpResponse;
import com.ruoyi.erp.service.DynamicQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 通用字典查询 Controller - JSON 配置驱动
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/erp/dict")
public class ErpDictionaryController {

    private final DynamicQueryService dynamicQueryService;

    /**
     * 通用字典查询接口
     * 
     * @param dictType 字典类型
     * @return 字典数据列表
     */
    @GetMapping("/{dictType}")
    public ErpResponse<?> getDict(@PathVariable String dictType) {
        try {
            log.info("[字典查询] dictType: {}", dictType);
            
            // 1. 加载配置
            Map<String, Object> config = dynamicQueryService.loadConfig("dict_" + dictType);
            
            // 2. 替换模板变量
            config = replaceTemplateVariables(config, Map.of("dictType", dictType));
            
            // 3. 执行查询
            var result = dynamicQueryService.executeQuery(config);
            
            return ErpResponse.ok(result);
        } catch (Exception e) {
            log.error("[字典查询] 失败 dictType={}", dictType, e);
            return ErpResponse.fail("查询失败：" + e.getMessage());
        }
    }
}
```

### 6.2 Service 层

```java
package com.ruoyi.erp.service.impl;

import com.ruoyi.common.redis.CacheUtils;
import com.ruoyi.erp.service.DynamicQueryService;
import com.ruoyi.erp.utils.SqlBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 动态查询 Service 实现
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class DynamicQueryServiceImpl implements DynamicQueryService {

    private final JdbcTemplate jdbcTemplate;
    private final SqlBuilder sqlBuilder;
    private final ObjectMapper objectMapper;

    @Override
    public Map<String, Object> loadConfig(String moduleCode) {
        // 1. 尝试从缓存读取
        String cacheKey = "query_config:" + moduleCode;
        String cached = (String) CacheUtils.get("erp_config", cacheKey);
        
        if (cached != null) {
            log.debug("[配置加载] 从缓存加载 moduleCode={}", moduleCode);
            return parseJson(cached);
        }
        
        // 2. 从数据库加载
        String sql = """
            SELECT config_json 
            FROM erp_page_config 
            WHERE module_code = ? 
            AND status = '1'
        """;
        
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, moduleCode);
        
        if (results.isEmpty()) {
            throw new RuntimeException("未找到配置：" + moduleCode);
        }
        
        String configJson = (String) results.get(0).get("config_json");
        
        // 3. 写入缓存
        CacheUtils.put("erp_config", cacheKey, configJson);
        
        log.debug("[配置加载] 从数据库加载 moduleCode={}", moduleCode);
        return parseJson(configJson);
    }

    @Override
    public List<Map<String, Object>> executeQuery(Map<String, Object> config) {
        // 1. 解析配置
        String tableName = (String) config.get("tableName");
        List<String> fields = (List<String>) config.get("fields");
        List<Map<String, Object>> conditions = parseConditions(config);
        List<Map<String, Object>> orderBy = parseOrderBy(config);
        
        // 2. 构建 SQL
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ").append(String.join(", ", fields));
        sql.append(" FROM ").append(tableName);
        
        // 3. 添加 WHERE 条件
        SqlResult whereSql = sqlBuilder.buildWhere(conditions);
        sql.append(whereSql.getSql());
        
        // 4. 添加排序
        SqlResult orderSql = sqlBuilder.buildOrderBy(orderBy);
        sql.append(orderSql.getSql());
        
        // 5. 执行查询
        log.debug("[执行查询] SQL: {}", sql);
        List<Map<String, Object>> result = jdbcTemplate.queryForList(
            sql.toString(), 
            whereSql.getParams().toArray()
        );
        
        log.debug("[执行查询] 返回 {} 条记录", result.size());
        return result;
    }

    // ========== 辅助方法 ==========

    private Map<String, Object> parseJson(String json) {
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            throw new RuntimeException("JSON 解析失败", e);
        }
    }

    private List<Map<String, Object>> parseConditions(Map<String, Object> config) {
        JsonNode conditionsNode = objectMapper.valueToTree(config.get("conditions"));
        if (conditionsNode == null || conditionsNode.isNull()) {
            return Collections.emptyList();
        }
        
        List<Map<String, Object>> conditions = new ArrayList<>();
        for (JsonNode node : conditionsNode) {
            Map<String, Object> condition = new HashMap<>();
            condition.put("field", node.get("field").asText());
            condition.put("operator", node.get("operator").asText());
            condition.put("value", resolveValue(node.get("value")));
            
            // 处理必填条件
            if (node.has("required") && node.get("required").asBoolean()) {
                if (condition.get("value") == null) {
                    throw new IllegalArgumentException("必填参数缺失：" + condition.get("field"));
                }
            }
            
            conditions.add(condition);
        }
        return conditions;
    }

    private List<Map<String, Object>> parseOrderBy(Map<String, Object> config) {
        JsonNode orderByNode = objectMapper.valueToTree(config.get("orderBy"));
        if (orderByNode == null || orderByNode.isNull()) {
            return Collections.emptyList();
        }
        
        List<Map<String, Object>> orderBy = new ArrayList<>();
        for (JsonNode node : orderByNode) {
            Map<String, Object> order = new HashMap<>();
            order.put("field", node.get("field").asText());
            order.put("direction", node.get("direction").asText());
            orderBy.add(order);
        }
        return orderBy;
    }

    private Object resolveValue(JsonNode valueNode) {
        if (valueNode.isArray()) {
            List<Object> values = new ArrayList<>();
            for (JsonNode node : valueNode) {
                values.add(resolveSingleValue(node));
            }
            return values;
        } else {
            return resolveSingleValue(valueNode);
        }
    }

    private Object resolveSingleValue(JsonNode valueNode) {
        String text = valueNode.asText();
        
        // 处理模板变量 ${variableName}
        if (text.startsWith("${") && text.endsWith("}")) {
            String varName = text.substring(2, text.length() - 1);
            // TODO: 从上下文获取变量值
            return null; // 暂时返回 null
        }
        
        // 处理普通值
        if (valueNode.isNumber()) {
            return valueNode.numberValue();
        } else if (valueNode.isBoolean()) {
            return valueNode.asBoolean();
        } else {
            return text;
        }
    }
}
```

---

## 7. 完整示例

### 7.1 客户分类字典查询

#### Step 1: 配置 JSON

```json
{
  "moduleCode": "customer_category",
  "tableName": "sys_dict_data",
  "fields": [
    "dict_label AS label",
    "dict_value AS value",
    "dict_type AS type"
  ],
  "conditions": [
    {
      "field": "dict_type",
      "operator": "eq",
      "value": "${dictType}"
    }
  ],
  "orderBy": [
    {
      "field": "dict_sort",
      "direction": "ASC"
    }
  ],
  "cacheable": true,
  "cacheExpire": 3600
}
```

#### Step 2: 保存到数据库

```sql
INSERT INTO erp_page_config (
    module_code, 
    config_json, 
    config_type,
    remark,
    create_by,
    create_time
) VALUES (
    'customer_category',
    '{"moduleCode":"customer_category","tableName":"sys_dict_data",...}',
    'QUERY_CONFIG',
    '客户分类字典查询配置',
    'admin',
    NOW()
);
```

#### Step 3: 调用接口

```javascript
// 前端调用
const response = await request({
  url: '/erp/dict/customer_category',
  method: 'get'
});

console.log(response.data);
// 输出：[
//   { label: '企业客户', value: 'enterprise', type: 'customer_category' },
//   { label: '个人客户', value: 'personal', type: 'customer_category' }
// ]
```

### 7.2 多表关联查询

#### 配置 JSON

```json
{
  "moduleCode": "salespersons_with_dept",
  "tableName": "sys_user u",
  "joins": [
    {
      "type": "LEFT JOIN",
      "table": "sys_dept d",
      "on": "u.dept_id = d.dept_id"
    }
  ],
  "fields": [
    "u.nick_name AS label",
    "u.user_id AS value",
    "d.dept_name AS deptName"
  ],
  "conditions": [
    {
      "field": "u.status",
      "operator": "eq",
      "value": "1"
    }
  ],
  "orderBy": [
    {
      "field": "u.nick_name",
      "direction": "ASC"
    }
  ]
}
```

---

## 8. 最佳实践

### 8.1 配置管理

#### ✅ 推荐做法

```json
{
  "moduleCode": "清晰的模块编码",
  "tableName": "表名或带别名的表",
  "fields": ["字段 1", "字段 2 AS 别名"],
  "conditions": [
    {
      "field": "条件字段",
      "operator": "eq",
      "value": "${参数名}",
      "required": true
    }
  ],
  "orderBy": [
    {
      "field": "排序字段",
      "direction": "ASC"
    }
  ],
  "cacheable": true,
  "cacheExpire": 3600,
  "remark": "配置说明"
}
```

#### ❌ 不推荐做法

```json
{
  // ❌ 模块编码不清晰
  "moduleCode": "test1",
  
  // ❌ 字段使用 SELECT *
  "fields": ["*"],
  
  // ❌ 条件过多（超过 10 个）
  "conditions": [...],
  
  // ❌ 没有排序规则
  "orderBy": null,
  
  // ❌ 没有缓存策略
  "cacheable": false
}
```

### 8.2 性能优化

#### 1. 合理使用缓存

```json
{
  "cacheable": true,
  "cacheExpire": 3600  // 1 小时过期
}
```

**适用场景：**
- ✅ 字典数据（变化少，查询频繁）
- ✅ 配置数据（几乎不变）
- ⚠️ 业务数据（谨慎使用）

#### 2. 避免 SELECT *

```json
// ✅ 推荐：明确指定字段
"fields": ["id", "name", "code"]

// ❌ 不推荐：使用 SELECT *
"fields": ["*"]
```

#### 3. 添加必要的索引

```sql
-- 为常用查询条件添加索引
CREATE INDEX idx_dict_type ON sys_dict_data(dict_type);
CREATE INDEX idx_status ON k3_sale_order(FDocumentStatus);
```

### 8.3 安全注意事项

#### 1. 字段名校验

```java
// SqlBuilder 会自动校验字段名
private static final Pattern FIELD_NAME_PATTERN = 
    Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

// 防止 SQL 注入
condition.put("field", "module_code");  // ✅ 合法
condition.put("field", "module_code; DROP TABLE users");  // ❌ 抛出异常
```

#### 2. 参数化查询

```java
// ✅ 推荐：使用参数占位符
condition.put("operator", "eq");
condition.put("value", userInput);

// ❌ 不推荐：字符串拼接
String sql = "SELECT * FROM table WHERE field = '" + userInput + "'";
```

### 8.4 错误处理

```java
try {
    // 1. 加载配置
    Map<String, Object> config = dynamicQueryService.loadConfig(moduleCode);
    
    // 2. 执行查询
    var result = dynamicQueryService.executeQuery(config);
    
    return ErpResponse.ok(result);
} catch (IllegalArgumentException e) {
    // 参数错误
    log.error("参数错误", e);
    return ErpResponse.fail(400, "参数错误：" + e.getMessage());
} catch (RuntimeException e) {
    // 配置不存在
    log.error("配置不存在", e);
    return ErpResponse.fail(404, "配置不存在");
} catch (Exception e) {
    // 系统异常
    log.error("系统异常", e);
    return ErpResponse.fail(500, "系统异常：" + e.getMessage());
}
```

---

## 🎉 总结

### 改造收益

1. **配置驱动**: JSON 配置替代硬编码 SQL
2. **动态灵活**: 新增查询无需修改代码
3. **统一规范**: 所有接口采用相同模式
4. **易于维护**: 配置集中管理，清晰可见
5. **性能优化**: 支持缓存，减少数据库压力

### 实施步骤

1. **Phase 1**: 完善 SqlBuilder（支持 JOIN、GROUP BY 等）
2. **Phase 2**: 实现 DynamicQueryService
3. **Phase 3**: 迁移字典接口到配置化模式
4. **Phase 4**: 推广到其他 CRUD 接口
5. **Phase 5**: 开发配置管理界面

### 后续优化

- [ ] 开发可视化配置编辑器
- [ ] 支持更复杂的查询（子查询、 UNION）
- [ ] 支持计算字段和虚拟字段
- [ ] 支持查询结果格式化
- [ ] 支持查询性能分析和优化建议

---

**文档版本:** v1.0  
**最后更新:** 2026-04-01  
**维护者:** AI Assistant - MM
