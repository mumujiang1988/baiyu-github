# ERP 字典接口架构检查报告

**检查时间：** 2026-04-01  
**检查对象：** All 字典接口后端架构  
**对比标准：** 纯 JdbcTemplate + SqlBuilder 方案

---

## 📊 检查结果总结

### ❌ **结论：不是纯 JdbcTemplate + SqlBuilder 方案**

当前字典接口采用的是 **MyBatis 注解方式**,而非项目推荐的纯 JdbcTemplate + SqlBuilder 方案。

---

## 🔍 详细分析

### 1. 当前架构

```
Controller → Service → Mapper(@Mapper 接口 + @Select 注解) → MyBatis → MySQL
```

**技术栈:**
- ✅ Controller: `ErpDictionaryController`
- ✅ Service: `ErpDictionaryService`
- ❌ Mapper: `ErpDictionaryMapper` (使用 MyBatis 注解)
- ❌ SQL: 内嵌在 Java 注解中的 XML 片段

### 2. 核心问题

#### ❌ 问题 1: 使用了 MyBatis Mapper 接口

```java
// ErpDictionaryService.java
@Service
public class ErpDictionaryService {
    @Autowired
    private ErpDictionaryMapper erpDictionaryMapper;  // ❌ 使用了 MyBatis Mapper
    
    public List<Map<String, Object>> getUnionDict(String dictType) {
        // ❌ 调用 Mapper 方法
        List<Map<String, Object>> sysDicts = erpDictionaryMapper.selectSysDictData(dictType);
        // ...
    }
}
```

#### ❌ 问题 2: 使用@Select 注解内嵌 SQL

```java
// ErpDictionaryMapper.java
@Mapper
public interface ErpDictionaryMapper {
    
    @Select("<script>" +
            "SELECT dict_label AS label, dict_value AS value, dict_type AS type " +
            "FROM sys_dict_data " +
            "<if test='dictType != null and dictType != \"\"'>WHERE dict_type = #{dictType}</if> " +
            "ORDER BY dict_sort" +
            "</script>")
    List<Map<String, Object>> selectSysDictData(@Param("dictType") String dictType);
    
    // ... 其他方法都是类似的@Select 注解
}
```

#### ❌ 问题 3: 没有使用 SqlBuilder 动态构建 SQL

所有 SQL 都是硬编码在注解中，没有使用项目标准的 `SqlBuilder` 工具类。

---

## 📋 对比表格

| 特性 | 字典接口 (当前) | 推荐方案 (表格列配置) |
|------|---------------|---------------------|
| Mapper 接口 | ✅ 使用 | ❌ 不使用 |
| MyBatis 注解 | ✅ @Select 注解 | ❌ 无 |
| XML 文件 | ❌ 无 (空目录) | ❌ 无 |
| JdbcTemplate | ❌ 未使用 | ✅ 核心组件 |
| SqlBuilder | ❌ 未使用 | ✅ 核心组件 |
| 数据返回 | `List<Map<String, Object>>` | `List<Map<String, Object>>` |
| Entity 实体类 | ❌ 不需要 | ❌ 不需要 (可选) |
| SQL 构建方式 | MyBatis 注解 XML | Java 代码 + SqlBuilder |

---

## 🎯 架构差异

### 当前字典接口架构

```java
// Controller
@GetMapping("/getAllDict")
public List<Map<String, Object>> getAllDict() {
    return dictionaryService.getAllDict();
}

// Service
public List<Map<String, Object>> getAllDict() {
    List<Map<String, Object>> list = new ArrayList<>();
    
    // ❌ 调用 MyBatis Mapper
    List<Map<String, Object>> sysDicts = erpDictionaryMapper.selectSysDictData(null);
    list.addAll(sysDicts);
    
    List<Map<String, Object>> bizDicts = erpDictionaryMapper.selectBizDictData(null);
    list.addAll(bizDicts);
    
    return list;
}

// Mapper (使用 MyBatis 注解)
@Mapper
public interface ErpDictionaryMapper {
    @Select("SELECT fname AS label, CAST(fcustid AS CHAR) AS value FROM bd_customer")
    List<Map<String, Object>> selectCustomersDict();
}
```

### 推荐方案 (参考表格列配置)

```java
// Controller
@GetMapping("/get/{moduleCode}")
public ErpResponse<String> getUserConfig(@PathVariable String moduleCode) {
    String config = userConfigService.getUserColumnConfig(moduleCode, "main", null);
    return ErpResponse.ok("操作成功", config);
}

// Service (使用 JdbcTemplate + SqlBuilder)
@Service
public class ErpUserTableConfigServiceImpl implements ErpUserTableConfigService {
    private final JdbcTemplate jdbcTemplate;  // ✅ 注入 JdbcTemplate
    private final SqlBuilder sqlBuilder;      // ✅ 注入 SqlBuilder
    
    public String getUserColumnConfig(String moduleCode, String tableType, String tabName) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        // ✅ 使用 SqlBuilder 构建条件
        List<Map<String, Object>> conditions = new ArrayList<>();
        Map<String, Object> cond1 = new HashMap<>();
        cond1.put("field", "module_code");
        cond1.put("operator", "eq");
        cond1.put("value", moduleCode);
        conditions.add(cond1);
        
        SqlBuilder.SqlResult sqlResult = sqlBuilder.buildWhere(conditions);
        String sql = "SELECT * FROM erp_user_table_config" + sqlResult.getSql() + " LIMIT 1";
        
        // ✅ 使用 JdbcTemplate 执行查询
        List<Map<String, Object>> resultList = jdbcTemplate.queryForList(
            sql, sqlResult.getParams().toArray()
        );
        
        if (resultList.isEmpty()) {
            return null;
        }
        
        return getStringValue(resultList.get(0), "column_config");
    }
}
```

---

## ⚠️ 存在的问题

### 1. 技术栈不统一

- **表格列配置模块**: 采用 纯 JdbcTemplate + SqlBuilder
- **字典模块**: 采用 MyBatis 注解方式
- **其他 ERP 模块**: 可能存在多种技术栈混用的情况

### 2. 维护成本高

- MyBatis 注解中的 SQL 难以调试和维护
- 动态 SQL 使用 XML 片段语法，不够直观
- 无法复用 SqlBuilder 的安全校验机制

### 3. 性能差异

根据实际测试（参考方案重构总结文档）:
- MyBatis-Plus 简单查询：~15ms
- JdbcTemplate 简单查询：~8ms
- **性能差距：47%**

### 4. 安全性

- MyBatis 注解中使用 `${}` 可能导致 SQL 注入（如 `selectBizDictCustom` 方法）
- SqlBuilder 提供字段名校验和参数化查询，更安全

---

## 💡 优化建议

### 短期方案（保持现状）

如果字典接口运行稳定，可以暂时保持现状，原因：
- ✅ 字典接口已经实现完整，功能正常
- ✅ 使用 MyBatis 注解避免了 XML 文件的繁琐
- ✅ 返回类型统一为 `List<Map<String, Object>>`，前端调用一致

### 长期方案（逐步迁移）

**阶段 1：新增接口采用新方案**
- 新增的字典相关接口使用 JdbcTemplate + SqlBuilder
- 保持现有接口不变

**阶段 2：逐步重构旧接口**
- 优先重构复杂度高、性能瓶颈明显的接口
- 例如：`getAllDict()` 方法可以重构

**阶段 3：统一技术规范**
- 制定明确的开发规范
- 所有新增数据访问层统一使用 JdbcTemplate + SqlBuilder

---

## 📝 重构示例

### 原方案（MyBatis 注解）

```java
@Mapper
public interface ErpDictionaryMapper {
    @Select("SELECT fname AS label, CAST(fcustid AS CHAR) AS value FROM bd_customer " +
            "WHERE fdocumentStatus = 'C' ORDER BY fname ASC")
    List<Map<String, Object>> selectCustomersDict();
}
```

### 新方案（JdbcTemplate + SqlBuilder）

```java
@Service
public class ErpCustomerDictService {
    private final JdbcTemplate jdbcTemplate;
    private final SqlBuilder sqlBuilder;
    
    public List<Map<String, Object>> getCustomerDict() {
        // 构建条件
        List<Map<String, Object>> conditions = new ArrayList<>();
        Map<String, Object> cond = new HashMap<>();
        cond.put("field", "fdocumentStatus");
        cond.put("operator", "eq");
        cond.put("value", "C");
        conditions.add(cond);
        
        // 构建 SQL
        SqlBuilder.SqlResult sqlResult = sqlBuilder.buildWhere(conditions);
        StringBuilder sql = new StringBuilder(
            "SELECT fname AS label, CAST(fcustid AS CHAR) AS value FROM bd_customer"
        );
        sql.append(sqlResult.getSql());
        sql.append(" ORDER BY fname ASC");
        
        // 执行查询
        return jdbcTemplate.queryForList(sql.toString(), sqlResult.getParams().toArray());
    }
}
```

---

## 📊 工作量评估

如果要完全重构字典接口到 JdbcTemplate + SqlBuilder:

| 工作内容 | 预估工时 | 说明 |
|----------|---------|------|
| 重构 Service 层 | 4 小时 | 约 10 个方法需要重写 |
| 移除 Mapper 接口 | 1 小时 | 删除或注释掉 |
| 单元测试 | 2 小时 | 确保功能一致性 |
| 集成测试 | 1 小时 | 前后端联调 |
| **总计** | **8 小时** | 约 1 个工作日 |

**投资回报率：** ⭐⭐⭐ (中等)
- 性能提升有限（字典数据量小）
- 代码可维护性提升
- 技术栈统一

---

## ✅ 总结

### 当前状态
- ❌ **不是**纯 JdbcTemplate + SqlBuilder 方案
- ✅ 采用的是 MyBatis 注解方式
- ✅ 功能完整，运行正常
- ⚠️ 与项目推荐的技术栈不一致

### 建议
1. **短期**: 保持现状，新功能采用新方案
2. **中期**: 逐步重构核心接口
3. **长期**: 统一技术规范，全部采用 JdbcTemplate + SqlBuilder

### 优先级
- 🔴 高优先级：新功能统一采用新方案
- 🟡 中优先级：性能瓶颈明显的接口
- 🟢 低优先级：运行稳定的旧接口

---

**报告结束**
