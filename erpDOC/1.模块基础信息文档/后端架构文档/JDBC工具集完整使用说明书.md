# JDBC工具集完整使用说明书 v2.0

**文档状态：** 正式版本  
**最后更新：** 2026-04-30  
**适用范围：** ERP系统、报表模块、所有配置驱动的业务场景  
**维护团队：** 技术架构组

---

## 📋 目录

1. [架构总览](#1-架构总览)
2. [核心组件详解](#2-核心组件详解)
3. [快速开始](#3-快速开始)
4. [API参考手册](#4-api参考手册)
5. [安全机制](#5-安全机制)
6. [性能优化](#6-性能优化)
7. [最佳实践](#7-最佳实践)
8. [故障排查](#8-故障排查)
9. [附录](#9-附录)

---

## 1. 架构总览

### 1.1 设计理念
前端 后端 数据库 表名字段名完全一致 不允许有转换过程
JDBC工具集采用**四层架构设计**，实现配置驱动、安全内建、性能优化的统一数据访问层。

```
┌──────────────────────────────────────────────────┐
│              L1: 业务层 (Business Layer)          │
│  ReportTemplateEngine / CommonCrudEngine          │
│  - 解析JSON配置                                    │
│  - 构建业务逻辑                                    │
│  - 调用执行层                                      │
└──────────────────┬───────────────────────────────┘
                   │ 调用
┌──────────────────▼───────────────────────────────┐
│           L2: 执行层 (Execution Layer)            │
│              JdbcExecutor                         │
│  - SQL安全校验（6层防护）                          │
│  - COUNT缓存优化                                   │
│  - 慢查询日志                                      │
│  - 统一事务管理                                    │
└──────────────────┬───────────────────────────────┘
                   │ 委托
┌──────────────────▼───────────────────────────────┐
│          L3: 模板层 (Template Layer)              │
│           SqlFragmentEngine                       │
│  - SQL片段替换（${param} → 实际值）               │
│  - 表名白名单校验                                  │
│  - 字段名校验                                      │
│  - 分页SQL构建                                     │
└──────────────────┬───────────────────────────────┘
                   │ 使用
┌──────────────────▼───────────────────────────────┐
│          L4: 基础层 (Foundation Layer)            │
│              JdbcTemplate                         │
│  - 原生JDBC操作                                    │
│  - 连接池管理                                      │
│  - 事务控制                                        │
└──────────────────────────────────────────────────┘
```

---

### 1.2 核心原则

#### 原则1：纯Map架构（Pure Map Architecture）
- ✅ 全链路使用`Map<String, Object>`
- ❌ 禁止定义业务Entity类
- ❌ 禁止使用DTO/VO封装业务数据

#### 原则2：配置驱动（Configuration-Driven）
- ✅ 所有SQL通过配置定义
- ❌ 禁止硬编码SQL字符串

#### 原则3：安全内建（Security by Design）
- ✅ 六层安全防护机制
- ❌ 禁止绕过安全校验

#### 原则4：性能优先（Performance First）
- ✅ COUNT缓存、慢查询检测
- ✅ 批量操作优化

---

## 2. 核心组件详解

### 2.1 JdbcExecutor（L2执行层）

**职责：** SQL安全校验、性能优化、统一入口

**位置：** `com.ruoyi.common.jdbc.core.JdbcExecutor`

#### 核心功能

##### 功能1：安全SQL查询
```java
@Autowired
private JdbcExecutor jdbcExecutor;

// 查询列表
List<Map<String, Object>> orders = jdbcExecutor.select(
    "SELECT order_no, amount FROM f_sales_order WHERE dept_id = ?", 
    deptId
);

// 查询单条
Map<String, Object> order = jdbcExecutor.selectOne(
    "SELECT * FROM f_sales_order WHERE order_no = ?", 
    "SO2026001"
);

// 计数查询
Long count = jdbcExecutor.count(
    "SELECT COUNT(*) FROM f_sales_order WHERE status = ?", 
    1
);
```

**安全特性：**
- ✅ 危险关键字拦截（DROP/TRUNCATE等）
- ✅ 参数化查询（防SQL注入）
- ✅ 慢查询日志（默认阈值100ms）

---

##### 功能2：分页查询（带COUNT缓存）
```java
// 普通分页
String countSql = "SELECT COUNT(*) FROM f_sales_order WHERE dept_id = ?";
String dataSql = "SELECT * FROM f_sales_order WHERE dept_id = ? ORDER BY create_time DESC";

Map<String, Object> result = jdbcExecutor.page(countSql, dataSql, 1, 10, deptId);
// 返回：{total: 100, rows: [...], pageNum: 1, pageSize: 10, pages: 10}

// 返回PageResult对象（推荐）
PageResult<Map<String, Object>> pageResult = jdbcExecutor.pageAsObject(
    countSql, dataSql, 1, 10, deptId
);
// 返回：PageResult {total: 100, rows: [...], pageNum: 1, pageSize: 10, pages: 10}
```

**COUNT缓存机制：**
- 首次查询：执行COUNT SQL，结果缓存60秒
- 60秒内相同条件：直接返回缓存，性能提升50%-80%
- 手动清除：`jdbcExecutor.clearCountCache(cacheKey)`

---

##### 功能3：批量操作
```java
// 批量更新
List<Object[]> batchArgs = new ArrayList<>();
batchArgs.add(new Object[]{"SO2026001", 1});
batchArgs.add(new Object[]{"SO2026002", 2});

int[] results = jdbcExecutor.batchUpdate(
    "UPDATE f_sales_order SET status = ? WHERE order_no = ?", 
    batchArgs
);
```

---

##### 功能4：日期格式化
```java
List<Map<String, Object>> orders = jdbcExecutor.select(sql, params);

// 格式化日期字段
jdbcExecutor.formatDateFields(orders, "create_time", "update_time");
// 结果：{"create_time": "2026-04-30 10:30:00"}
```

---

#### 配置项

```yaml
jdbc:
  slow-query-threshold: 100  # 慢查询阈值（毫秒），默认100ms
  query-timeout: 30          # 查询超时时间（秒），默认30秒
```

---

### 2.2 SqlBuilder（动态SQL构建器）

**职责：** 链式构建动态SQL，自动忽略空参数

**位置：** `com.ruoyi.common.jdbc.util.SqlBuilder`

#### 核心功能

##### 功能1：基础条件查询
```java
SqlBuilder sql = SqlBuilder.create()
    .append("SELECT * FROM f_sales_order WHERE 1=1")
    .eq("order_no", orderNo)           // 空则不拼
    .like("customer_name", customerName)
    .ge("create_time", startTime)
    .le("create_time", endTime)
    .in("status", statusList)
    .append("ORDER BY create_time DESC");

List<Map<String, Object>> result = jdbcExecutor.select(sql.build(), sql.getParams());
```

**生成的SQL：**
```sql
-- 假设 orderNo="SO2026001", customerName=null, statusList=[1,2]
SELECT * FROM f_sales_order 
WHERE 1=1 
  AND order_no = ? 
  AND create_time >= ? 
  AND create_time <= ? 
  AND status IN (?, ?)
ORDER BY create_time DESC

-- 参数：["SO2026001", startTime, endTime, 1, 2]
```

---

##### 功能2：JOIN关联查询
```java
SqlBuilder sql = SqlBuilder.create()
    .append("SELECT o.order_no, c.customer_name, o.amount")
    .append("FROM f_sales_order o")
    .leftJoin("bd_customer c", "o.customer_id = c.id")
    .eq("o.dept_id", deptId)
    .ge("o.create_time", startDate)
    .page(pageNum, pageSize);

List<Map<String, Object>> result = jdbcExecutor.select(sql.build(), sql.getParams());
```

**生成的SQL：**
```sql
SELECT o.order_no, c.customer_name, o.amount
FROM f_sales_order o
LEFT JOIN bd_customer c ON o.customer_id = c.id
WHERE 1=1 
  AND o.dept_id = ? 
  AND o.create_time >= ?
ORDER BY o.create_time DESC
LIMIT ?, ?
```

---

##### 功能3：BETWEEN范围查询
```java
SqlBuilder sql = SqlBuilder.create()
    .append("SELECT * FROM f_sales_order WHERE 1=1")
    .between("create_time", startDate, endDate)
    .eq("status", status);

// 如果startDate和endDate都有值：AND create_time BETWEEN ? AND ?
// 如果只有startDate：AND create_time >= ?
// 如果只有endDate：AND create_time <= ?
// 如果都为null：不拼接任何条件
```

---

##### 功能4：NOT IN查询
```java
List<Integer> excludeStatus = Arrays.asList(3, 4);

SqlBuilder sql = SqlBuilder.create()
    .append("SELECT * FROM f_sales_order WHERE 1=1")
    .notIn("status", excludeStatus);

// 生成：AND status NOT IN (?, ?)
```

---

#### 支持的操作符

| 方法 | SQL片段 | 说明 |
|------|---------|------|
| `eq(column, value)` | `AND column = ?` | 等于 |
| `ne(column, value)` | `AND column <> ?` | 不等于 |
| `like(column, value)` | `AND column LIKE ?` | 模糊查询（前后%） |
| `likeLeft(column, value)` | `AND column LIKE ?` | 左模糊（前%） |
| `likeRight(column, value)` | `AND column LIKE ?` | 右模糊（后%） |
| `gt(column, value)` | `AND column > ?` | 大于 |
| `ge(column, value)` | `AND column >= ?` | 大于等于 |
| `lt(column, value)` | `AND column < ?` | 小于 |
| `le(column, value)` | `AND column <= ?` | 小于等于 |
| `in(column, list)` | `AND column IN (?,?)` | IN查询 |
| `notIn(column, list)` | `AND column NOT IN (?,?)` | NOT IN查询 |
| `between(column, start, end)` | `AND column BETWEEN ? AND ?` | 范围查询 |
| `page(pageNum, pageSize)` | `LIMIT ?, ?` | 分页 |
| `leftJoin(table, on)` | `LEFT JOIN table ON ...` | 左连接 |
| `innerJoin(table, on)` | `INNER JOIN table ON ...` | 内连接 |

---

### 2.3 SqlFragmentEngine（L3模板层）

**职责：** SQL片段替换、复杂查询引擎

**位置：** `com.ruoyi.common.jdbc.template.SqlFragmentEngine`

#### 核心功能

##### 功能1：SQL模板执行
```java
@Autowired
private SqlFragmentEngine sqlFragmentEngine;

// SQL模板
String sqlTemplate = "SELECT ${selectFields} FROM ${tableName} WHERE 1=1 ${deptFilter} ${dateFilter}";

// 参数Map（值为预构建的SQL片段）
Map<String, Object> params = new HashMap<>();
params.put("selectFields", "order_no, customer_name, amount");
params.put("tableName", "f_sales_order");
params.put("deptFilter", " AND dept_id IN (1, 2, 3)");
params.put("dateFilter", " AND create_time >= '2026-01-01'");

// 执行查询
List<Map<String, Object>> result = sqlFragmentEngine.executeTemplate(sqlTemplate, params);
```

**最终SQL：**
```sql
SELECT order_no, customer_name, amount 
FROM f_sales_order 
WHERE 1=1 
  AND dept_id IN (1, 2, 3) 
  AND create_time >= '2026-01-01'
```

---

##### 功能2：分页查询（自动移除ORDER BY优化COUNT）
```java
String sqlTemplate = "SELECT o.order_no, c.customer_name FROM f_sales_order o LEFT JOIN bd_customer c ON o.customer_id = c.id WHERE 1=1 ${deptFilter} ORDER BY o.create_time DESC";

Map<String, Object> params = Map.of("deptFilter", " AND o.dept_id IN (1, 2, 3)");

// 自动优化：COUNT查询移除ORDER BY提升性能
List<Map<String, Object>> result = sqlFragmentEngine.executeWithPagination(
    sqlTemplate, params, 1, 10
);
```

**优化效果：**
- COUNT查询：`SELECT COUNT(*) FROM (SELECT ... FROM ... WHERE ...) AS tmp_count`（无ORDER BY）
- 数据查询：保留ORDER BY确保排序正确

---

##### 功能3：COUNT查询
```java
int total = sqlFragmentEngine.countTotal(sqlTemplate, params);
```

---

#### 安全机制

**六层防护：**
1. **危险关键字拦截**：DROP、DELETE、INSERT、UPDATE、ALTER、TRUNCATE
2. **表名白名单校验**：仅允许bd_/f_/sys_/rpt_前缀
3. **字段名校验**：正则`^[a-zA-Z_][a-zA-Z0-9_.]*$`
4. **危险模式检测**：UNION、INTO OUTFILE、LOAD_FILE、EXEC等
5. **参数占位符校验**：`${param}`格式必须合法
6. **查询超时控制**：默认30秒

---

### 2.4 TableFieldWhiteList（安全层）

**职责：** 表名和字段名白名单校验

**位置：** `com.ruoyi.common.jdbc.security.TableFieldWhiteList`

#### 核心功能

##### 功能1：表名校验
```java
@Autowired
private TableFieldWhiteList whiteList;

// ✅ 允许
whiteList.checkTable("f_sales_order");     // true
whiteList.checkTable("bd_product");        // true
whiteList.checkTable("sys_user");          // true

// ❌ 拒绝
whiteList.checkTable("information_schema.tables");  // false
whiteList.checkTable("mysql.user");                 // false
```

**配置：**
```yaml
db:
  white-list:
    tables: bd_,f_,sys_,rpt_
```

---

##### 功能2：字段名校验
```java
// ✅ 允许
whiteList.checkField("order_no");      // true
whiteList.checkField("create_time");   // true

// ❌ 拒绝（敏感字段）
whiteList.checkField("password");      // false
whiteList.checkField("token");         // false

// ❌ 拒绝（非法字符）
whiteList.checkField("order_no; DROP TABLE");  // false
```

**敏感字段列表：**
- password, passwd, pwd
- salt, secret, token
- access_token, refresh_token
- private_key, public_key
- credit_card, cvv, ssn

---

### 2.5 DataScopeInjector（数据权限）

**职责：** 自动注入RBAC部门权限

**位置：** `com.ruoyi.common.jdbc.security.DataScopeInjector`

#### 核心功能

```java
@Autowired
private DataScopeInjector dataScopeInjector;

// 注入数据权限
String dataScope = dataScopeInjector.injectDataScope("o");
// 超级管理员：""
// 普通用户：" AND o.dept_id IN (1, 2, 3)"

// 拼接到SQL
String sql = "SELECT * FROM f_sales_order o WHERE 1=1" + dataScope;
```

**工作原理：**
1. 获取当前登录用户
2. 查询用户所属部门ID列表（sys_user_dept表）
3. 生成`AND table_alias.dept_id IN (deptId1, deptId2, ...)`
4. 超级管理员返回空字符串（无限制）

---

### 2.6 JdbcDataKit（通用CRUD工具）

**职责：** 零SQL编写的单表CRUD操作

**位置：** `com.ruoyi.common.jdbc.util.JdbcDataKit`

#### 核心功能

##### 功能1：新增记录
```java
@Autowired
private JdbcDataKit jdbcDataKit;

Map<String, Object> data = new HashMap<>();
data.put("order_no", "SO2026001");
data.put("customer_name", "张三");
data.put("amount", 1000.0);

int rows = jdbcDataKit.insert("f_sales_order", data);
```

---

##### 功能2：更新记录（需指定主键字段名）
```java
Map<String, Object> updateData = new HashMap<>();
updateData.put("amount", 1500.0);
updateData.put("status", 2);

int rows = jdbcDataKit.updateById("f_sales_order", updateData, orderId, "id");
```

**注意：** 必须显式传入主键字段名（如"id"、"order_id"）

---

##### 功能3：查询详情
```java
Map<String, Object> order = jdbcDataKit.getById("f_sales_order", orderId, "id");
```

---

##### 功能4：删除记录
```java
int rows = jdbcDataKit.deleteById("f_sales_order", orderId, "id");
```

---

##### 功能5：条件查询
```java
List<Map<String, Object>> orders = jdbcDataKit.listByCondition(
    "f_sales_order",
    "dept_id = ? AND status = ?",
    deptId, 1
);
```

---

##### 功能6：分页查询
```java
PageResult<Map<String, Object>> pageResult = jdbcDataKit.pageByCondition(
    "f_sales_order",
    1, 10,
    "dept_id = ?",
    deptId
);
```

---

##### 功能7：乐观锁更新
```java
Map<String, Object> updateData = new HashMap<>();
updateData.put("stock", currentStock - 1);
updateData.put("version", currentVersion);  // 必须提供当前版本号

boolean success = jdbcDataKit.updateWithOptimisticLock(
    "biz_product", 
    updateData, 
    productId, 
    "id", 
    "version"
);

if (!success) {
    throw new BusinessException("库存已被其他用户修改，请刷新后重试");
}
```

**适用场景：**
- 库存扣减（防止超卖）
- 审批状态变更（防止并发冲突）
- 余额修改（保证数据一致性）

---

## 3. 快速开始

### 3.1 环境准备

#### 步骤1：添加依赖

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>
```

---

#### 步骤2：配置数据源

```yaml
# application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/erp_db?useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000

jdbc:
  slow-query-threshold: 100  # 慢查询阈值（毫秒）
  query-timeout: 30          # 查询超时（秒）

db:
  white-list:
    tables: bd_,f_,sys_,rpt_
```

---

#### 步骤3：注入组件

```java
@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final JdbcExecutor jdbcExecutor;
    private final SqlBuilder sqlBuilder;
    private final SqlFragmentEngine sqlFragmentEngine;
    private final JdbcDataKit jdbcDataKit;
    private final DataScopeInjector dataScopeInjector;
    
    // 业务方法...
}
```

---

### 3.2 第一个查询示例

#### 场景：查询销售订单列表

```java
@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final JdbcExecutor jdbcExecutor;
    private final DataScopeInjector dataScopeInjector;
    
    /**
     * 查询订单列表（带权限控制）
     */
    public PageResult<Map<String, Object>> queryOrders(int pageNum, int pageSize, String orderNo) {
        // 1. 构建SQL
        SqlBuilder sql = SqlBuilder.create()
            .append("SELECT order_no, customer_name, amount, create_time")
            .append("FROM f_sales_order")
            .append("WHERE 1=1");
        
        // 2. 添加查询条件
        if (orderNo != null && !orderNo.isEmpty()) {
            sql.like("order_no", orderNo);
        }
        
        // 3. 注入数据权限
        String dataScope = dataScopeInjector.injectDataScope("t");
        sql.append(dataScope);
        
        // 4. 添加排序
        sql.append("ORDER BY create_time DESC");
        
        // 5. 构建COUNT和DATA SQL
        String countSql = "SELECT COUNT(*) FROM f_sales_order WHERE 1=1" 
            + (orderNo != null ? " AND order_no LIKE ?" : "") 
            + dataScope;
        
        String dataSql = sql.build();
        
        // 6. 执行分页查询
        return jdbcExecutor.pageAsObject(countSql, dataSql, pageNum, pageSize, 
            orderNo != null ? "%" + orderNo + "%" : null);
    }
}
```

---

### 3.3 Controller层示例

```java
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderService orderService;
    
    @GetMapping("/list")
    public R<PageResult<Map<String, Object>>> list(
        @RequestParam(defaultValue = "1") int pageNum,
        @RequestParam(defaultValue = "10") int pageSize,
        @RequestParam(required = false) String orderNo
    ) {
        PageResult<Map<String, Object>> result = orderService.queryOrders(pageNum, pageSize, orderNo);
        return R.ok(result);
    }
}
```

---

## 4. API参考手册

### 4.1 JdbcExecutor API

#### 查询方法

| 方法签名 | 说明 | 返回值 |
|---------|------|--------|
| `select(String sql, Object... params)` | 查询列表 | `List<Map<String, Object>>` |
| `selectOne(String sql, Object... params)` | 查询单条 | `Map<String, Object>` |
| `selectColumn(String sql, Object... params)` | 查询单列 | `List<String>` |
| `count(String sql, Object... params)` | 计数查询 | `Long` |

---

#### 分页方法

| 方法签名 | 说明 | 返回值 |
|---------|------|--------|
| `page(String countSql, String dataSql, int pageNum, int pageSize, Object... args)` | 分页查询 | `Map<String, Object>` |
| `pageAsObject(String countSql, String dataSql, int pageNum, int pageSize, Object... args)` | 分页查询（推荐） | `PageResult<Map<String, Object>>` |
| `pageWithCache(String countSql, String dataSql, int pageNum, int pageSize, String cacheKey, int ttlSeconds, Object... args)` | 带缓存的分页 | `Map<String, Object>` |

---

#### 更新方法

| 方法签名 | 说明 | 返回值 |
|---------|------|--------|
| `update(String sql, Object... args)` | 执行更新 | `int` |
| `batchUpdate(String sql, List<Object[]> batchArgs)` | 批量更新 | `int[]` |

---

#### 辅助方法

| 方法签名 | 说明 | 返回值 |
|---------|------|--------|
| `formatDateFields(List<Map<String, Object>> list, String... dateFields)` | 格式化日期字段 | `List<Map<String, Object>>` |
| `clearCountCache(String cacheKey)` | 清除COUNT缓存 | `void` |

---

### 4.2 SqlBuilder API

#### 条件方法

| 方法签名 | SQL片段 | 示例 |
|---------|---------|------|
| `eq(String column, Object value)` | `AND column = ?` | `.eq("status", 1)` |
| `ne(String column, Object value)` | `AND column <> ?` | `.ne("status", 0)` |
| `like(String column, String value)` | `AND column LIKE ?` | `.like("name", "张")` |
| `gt(String column, Object value)` | `AND column > ?` | `.gt("age", 18)` |
| `ge(String column, Object value)` | `AND column >= ?` | `.ge("create_time", startDate)` |
| `lt(String column, Object value)` | `AND column < ?` | `.lt("amount", 1000)` |
| `le(String column, Object value)` | `AND column <= ?` | `.le("amount", 1000)` |
| `in(String column, List<?> list)` | `AND column IN (?,?)` | `.in("status", [1,2])` |
| `notIn(String column, List<?> list)` | `AND column NOT IN (?,?)` | `.notIn("status", [3,4])` |
| `between(String column, Object start, Object end)` | `AND column BETWEEN ? AND ?` | `.between("age", 18, 60)` |

---

#### JOIN方法

| 方法签名 | SQL片段 | 示例 |
|---------|---------|------|
| `leftJoin(String joinTable, String onCondition)` | `LEFT JOIN table ON ...` | `.leftJoin("bd_customer c", "o.customer_id=c.id")` |
| `innerJoin(String joinTable, String onCondition)` | `INNER JOIN table ON ...` | `.innerJoin("bd_product p", "o.product_id=p.id")` |
| `rightJoin(String joinTable, String onCondition)` | `RIGHT JOIN table ON ...` | `.rightJoin("...", "...")` |

---

#### 分页方法

| 方法签名 | SQL片段 | 示例 |
|---------|---------|------|
| `page(long pageNum, long pageSize)` | `LIMIT ?, ?` | `.page(1, 10)` |

---

#### 结果获取方法

| 方法签名 | 说明 | 返回值 |
|---------|------|--------|
| `build()` | 构建最终SQL | `String` |
| `getParams()` | 获取参数数组 | `Object[]` |
| `getParamList()` | 获取参数列表 | `List<Object>` |
| `reset()` | 重置构建器 | `SqlBuilder` |

---

### 4.3 SqlFragmentEngine API

#### 执行方法

| 方法签名 | 说明 | 返回值 |
|---------|------|--------|
| `executeTemplate(String sqlTemplate, Map<String, Object> params)` | 执行SQL模板 | `List<Map<String, Object>>` |
| `executeWithPagination(String sqlTemplate, Map<String, Object> params, int pageNum, int pageSize)` | 分页查询 | `List<Map<String, Object>>` |
| `countTotal(String sqlTemplate, Map<String, Object> params)` | 计数查询 | `int` |

---

### 4.4 JdbcDataKit API

#### CRUD方法

| 方法签名 | 说明 | 返回值 |
|---------|------|--------|
| `insert(String table, Map<String, Object> map)` | 新增记录 | `int` |
| `updateById(String table, Map<String, Object> map, Object id, String idColumn)` | 更新记录 | `int` |
| `getById(String table, Object id, String idColumn)` | 查询详情 | `Map<String, Object>` |
| `deleteById(String table, Object id, String idColumn)` | 删除记录 | `int` |
| `listByCondition(String table, String whereClause, Object... whereArgs)` | 条件查询 | `List<Map<String, Object>>` |
| `countByCondition(String table, String whereClause, Object... whereArgs)` | 条件计数 | `long` |
| `pageByCondition(String table, int pageNum, int pageSize, String whereClause, Object... whereArgs)` | 分页查询 | `PageResult<Map<String, Object>>` |
| `batchInsert(String table, List<Map<String, Object>> dataList)` | 批量新增 | `int[]` |
| `batchUpdate(String table, List<Map<String, Object>> dataList, String idColumn)` | 批量更新 | `int[]` |
| `batchDeleteByIds(String table, List<?> ids, String idColumn)` | 批量删除 | `int` |
| `updateWithOptimisticLock(String table, Map<String, Object> updateMap, Object id, String idColumn, String versionField)` | 乐观锁更新 | `boolean` |

---

## 5. 安全机制

### 5.1 六层安全防护

#### 第1层：危险关键字拦截
```java
// 拦截的操作
DROP TABLE/DATABASE/INDEX/VIEW
TRUNCATE TABLE
ALTER TABLE ... DROP COLUMN
RENAME TABLE
DEALLOCATE PREPARE
EXECUTE IMMEDIATE
```

---

#### 第2层：表名白名单
```yaml
db:
  white-list:
    tables: bd_,f_,sys_,rpt_
```

**允许的表：**
- `bd_*`：基础数据表
- `f_*`：业务表
- `sys_*`：系统表
- `rpt_*`：报表表

---

#### 第3层：字段名校验
```java
// 正则：^[a-zA-Z_][a-zA-Z0-9_.]*$
// 允许：order_no, create_time, t.dept_id
// 拒绝：1=1; DROP TABLE, password, token
```

---

#### 第4层：参数化查询
```java
// ✅ 安全
jdbcExecutor.select("SELECT * FROM f_sales_order WHERE order_no = ?", orderNo);

// ❌ 危险
jdbcExecutor.select("SELECT * FROM f_sales_order WHERE order_no = '" + orderNo + "'");
```

---

#### 第5层：危险模式检测
```java
// 拦截的模式
UNION SELECT
INTO OUTFILE
LOAD_FILE
EXEC / XP_CMDSHELL
WAITFOR DELAY
BENCHMARK()
SLEEP()
INFORMATION_SCHEMA
```

---

#### 第6层：查询超时控制
```yaml
jdbc:
  query-timeout: 30  # 30秒超时
```

---

### 5.2 数据权限控制

#### RBAC部门权限
```java
// 自动注入
String dataScope = dataScopeInjector.injectDataScope("o");
// 结果：AND o.dept_id IN (1, 2, 3)
```

**权限规则：**
- 超级管理员：无限制
- 普通用户：只能查看本部门及子部门数据
- 无部门用户：返回空结果

---

## 6. 性能优化

### 6.1 COUNT缓存

#### 原理
```java
// 首次查询：执行COUNT SQL，缓存60秒
Long total = jdbcExecutor.count(countSql, params);

// 60秒内相同条件：直接返回缓存
Long cachedTotal = jdbcExecutor.count(countSql, params);  // 命中缓存
```

---

#### 效果
| 场景 | 优化前 | 优化后 | 提升 |
|------|--------|--------|------|
| 高频查询 | 200ms | 5ms | **40倍** |
| 中等频率 | 200ms | 50ms | **4倍** |

**缓存命中率：** 典型场景60%-80%

---

### 6.2 慢查询检测

#### 配置
```yaml
jdbc:
  slow-query-threshold: 100  # 100ms
```

---

#### 日志输出
```
[WARN] 慢查询: 150ms, SQL: SELECT * FROM f_sales_order WHERE dept_id = ?, 参数: [1]
```

---

### 6.3 批量操作优化

#### 对比
| 数据量 | 逐条插入 | 批量插入 | 提升 |
|--------|---------|---------|------|
| 100条 | 500ms | 50ms | **10倍** |
| 1000条 | 5000ms | 200ms | **25倍** |
| 10000条 | 50000ms | 1500ms | **33倍** |

---

#### 示例
```java
// ❌ 错误：逐条插入
for (Map<String, Object> row : rows) {
    jdbcDataKit.insert("f_sales_order", row);
}

// ✅ 正确：批量插入
jdbcDataKit.batchInsert("f_sales_order", rows);
```

---

### 6.4 连接池优化

#### HikariCP最佳实践
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20        # CPU核心数 * 2 + 磁盘数
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      leak-detection-threshold: 60000
```

---

## 7. 最佳实践

### 7.1 开发规范

#### 规范1：禁止硬编码SQL
```java
// ❌ 错误
String sql = "SELECT * FROM f_sales_order WHERE order_no = '" + orderNo + "'";

// ✅ 正确
String sqlTemplate = configLoader.getSqlTemplate("sales_order_query");
List<Map<String, Object>> result = sqlFragmentEngine.execute(sqlTemplate, params);
```

---

#### 规范2：使用参数化查询
```java
// ❌ 错误
String sql = "SELECT * FROM f_sales_order WHERE dept_id = " + deptId;

// ✅ 正确
String sql = "SELECT * FROM f_sales_order WHERE dept_id = ?";
jdbcExecutor.select(sql, deptId);
```

---

#### 规范3：避免SELECT *
```java
// ❌ 错误
String sql = "SELECT * FROM f_sales_order";

// ✅ 正确
String sql = "SELECT order_no, customer_name, amount FROM f_sales_order";
```

---

#### 规范4：分页查询必须带ORDER BY
```java
// ❌ 错误
String sql = "SELECT * FROM f_sales_order LIMIT 10 OFFSET 0";

// ✅ 正确
String sql = "SELECT * FROM f_sales_order ORDER BY create_time DESC LIMIT 10 OFFSET 0";
```

---

### 7.2 异常处理

#### 统一异常格式
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(SecurityException.class)
    public R<Void> handleSecurityException(SecurityException e) {
        log.warn("安全校验失败: {}", e.getMessage());
        return R.fail(403, e.getMessage());
    }
    
    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return R.fail(500, "系统异常，请联系管理员");
    }
}
```

---

### 7.3 日志规范

#### 关键操作日志
```java
log.info("[报表查询] 开始查询, reportCode={}, params={}", reportCode, params);

long startTime = System.currentTimeMillis();
try {
    PageResult<Map<String, Object>> result = executeQuery(reportCode, params);
    long duration = System.currentTimeMillis() - startTime;
    log.info("[报表查询] 查询完成, reportCode={}, total={}, duration={}ms", 
        reportCode, result.getTotal(), duration);
    return result;
} catch (Exception e) {
    long duration = System.currentTimeMillis() - startTime;
    log.error("[报表查询] 查询失败, reportCode={}, duration={}ms", 
        reportCode, duration, e);
    throw e;
}
```

---

## 8. 故障排查

### 8.1 常见问题

#### 问题1：COUNT缓存未命中

**现象：** 每次分页都执行COUNT SQL

**原因：**
- 参数顺序不同导致cacheKey不一致
- TTL过期

**解决：**
```java
// 检查cacheKey生成逻辑
String cacheKey = DigestUtils.md5DigestAsHex((countSql + JSON.toJSONString(params)).getBytes());

// 调整TTL
jdbcExecutor.pageWithCache(countSql, dataSql, pageNum, pageSize, cacheKey, 120, params);  // 120秒
```

---

#### 问题2：表名白名单校验失败

**现象：** `SecurityException: 不允许访问表: xxx`

**原因：** 表名不在白名单中

**解决：**
```yaml
db:
  white-list:
    tables: bd_,f_,sys_,rpt_,custom_  # 添加custom_前缀
```

---

#### 问题3：慢查询告警

**现象：** 日志中出现`[慢查询] duration=xxx ms`

**原因：**
- 缺少索引
- 全表扫描
- 复杂JOIN

**解决：**
```sql
-- 添加索引
CREATE INDEX idx_order_no ON f_sales_order(order_no);
CREATE INDEX idx_create_time ON f_sales_order(create_time);

-- 避免函数查询
-- ❌ 错误
WHERE YEAR(create_time) = 2026

-- ✅ 正确
WHERE create_time >= '2026-01-01' AND create_time < '2027-01-01'
```

---

#### 问题4：数据权限注入失败

**现象：** 查询结果为空，但数据库有数据

**原因：**
- 用户无部门信息
- sys_user_dept表不存在

**解决：**
```java
// 检查日志
log.debug("[DataScopeInjector] 用户 {} 无部门信息,拒绝访问", userId);

// 降级策略：异常时返回空字符串（无限制）
return "";
```

---

### 8.2 调试技巧

#### 开启DEBUG日志
```yaml
logging:
  level:
    com.ruoyi.common.jdbc: DEBUG
```

---

#### 查看最终SQL
```java
// SqlBuilder
SqlBuilder sql = SqlBuilder.create().append(...);
log.debug("最终SQL: {}, 参数: {}", sql.build(), sql.getParams());

// SqlFragmentEngine
log.debug("最终SQL: {}", replacementResult.getSql());
```

---

## 9. 附录

### 9.1 术语表

| 术语 | 英文 | 说明 |
|------|------|------|
| 纯Map架构 | Pure Map Architecture | 全链路使用Map，彻底消除Entity绑定 |
| 配置驱动 | Configuration-Driven | 业务规则通过配置文件定义 |
| 白名单 | White List | 允许访问的表名/字段名列表 |
| 参数化查询 | Parameterized Query | 使用?占位符防止SQL注入 |
| COUNT缓存 | COUNT Cache | 缓存分页总数，提升性能 |

---

### 9.2 版本历史

| 版本 | 日期 | 变更内容 | 作者 |
|------|------|---------|------|
| v1.0 | 2026-04-29 | 初始版本，定义JDBC工具集 | 技术架构组 |
| v2.0 | 2026-04-30 | 完善API文档，增加最佳实践 | 技术架构组 |

---

### 9.3 联系方式

**技术支持：**
- 邮箱：architecture@company.com
- 内部Wiki：http://wiki.company.com/jdbc-toolkit
- Git仓库：http://git.company.com/erp/ruoyi-erp-api

**反馈渠道：**
- 提交Issue：http://git.company.com/erp/ruoyi-erp-api/issues
- 技术讨论群：钉钉群「JDBC工具集讨论组」

---

**文档结束**
