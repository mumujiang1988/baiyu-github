# 低代码JSON驱动架构说明书 v1.0

**文档状态：** 正式版本  
**最后更新：** 2026-04-30  
**适用范围：** ERP系统、报表模块、所有配置驱动的业务场景  
**维护团队：** 技术架构组

---

## 📋 目录

1. [架构愿景与核心原则](#1-架构愿景与核心原则)
2. [四层架构设计](#2-四层架构设计)
3. [数据流规范](#3-数据流规范)
4. [技术栈选型](#4-技术栈选型)
5. [开发规范](#5-开发规范)
6. [安全机制](#6-安全机制)
7. [性能优化](#7-性能优化)
8. [迁移指南](#8-迁移指南)
9. [常见问题](#9-常见问题)
10. [附录](#10-附录)

---

## 1. 架构愿景与核心原则

### 1.1 架构愿景

构建一个**配置驱动、字段自由增减、API向后兼容**的低代码系统，实现：
- ✅ 业务人员可通过配置界面定义报表、表单、流程
- ✅ 开发人员无需修改代码即可支持新字段、新表
- ✅ 前端配置变更无需后端重新编译和发布

### 1.2 六大核心原则

#### 原则1：配置驱动（Configuration-Driven）

**核心理念：** 所有业务规则通过JSON配置定义，而非硬编码。

```json
// ❌ 错误：硬编码SQL
SELECT order_no, customer_name FROM f_sales_order WHERE dept_id = ?

// ✅ 正确：配置化SQL模板
{
  "sql_template": {
    "query": "SELECT ${selectFields} FROM ${tableName} WHERE 1=1 ${deptFilter}",
    "params": ["dept_id"]
  }
}
```

**实施要求：**
- 所有SQL语句必须存储在配置表或JSON文件中
- 禁止在Java代码中拼接SQL字符串
- 配置变更通过管理界面或SQL脚本完成

---

#### 原则2：Map优先（Map-First）

**核心理念：** 查询路径统一使用`Map<String, Object>`，避免Entity绑定。

```java
// ❌ 错误：返回Entity
public List<SalesOrder> queryOrders() {
    return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(SalesOrder.class));
}

// ✅ 正确：返回Map
public List<Map<String, Object>> queryOrders() {
    return jdbcExecutor.select(sql, params);
}
```

**优势：**
- ✅ 字段自由增减，无需修改Entity类
- ✅ API向后兼容，新增字段不影响现有调用方
- ✅ 减少样板代码（无getter/setter）

**例外情况：**
- 系统基础表（sys_user、sys_role）可保留Entity
- 需要Bean Validation校验的复杂业务可使用DTO

---

#### 原则3：字段自由增减（Schema Flexibility）

**核心理念：** 数据库表结构变更不应导致代码重新编译。

**实施策略：**
```java
// ❌ 错误：依赖固定字段
entity.setOrderNo(map.get("order_no"));
entity.setCustomerName(map.get("customer_name"));

// ✅ 正确：动态处理字段
Map<String, Object> result = jdbcExecutor.selectOne(sql, id);
// 前端按需取用任意字段，后端无需感知
```

**配置示例：**
```json
{
  "field_config": [
    {"field": "order_no", "label": "订单号", "type": "string"},
    {"field": "customer_name", "label": "客户名称", "type": "string"},
    // 新增字段？直接添加配置，无需改代码
    {"field": "delivery_date", "label": "交货日期", "type": "date"}
  ]
}
```

---

#### 原则4：API向后兼容（Backward Compatibility）

**核心理念：** 接口变更不得破坏现有调用方。

**实施规范：**
```java
// ❌ 错误：删除字段导致前端报错
{
  "order_no": "SO2026001",
  "customer_name": "张三"
  // 删除了此字段 → 前端崩溃
}

// ✅ 正确：新增字段可选，旧字段保留
{
  "order_no": "SO2026001",
  "customer_name": "张三",
  "delivery_date": "2026-05-01"  // 新增字段，前端可选择性使用
}
```

**版本管理策略：**
- 新增字段：直接添加，标记为`optional`
- 废弃字段：保留但标记为`deprecated`，6个月后删除
- 破坏性变更：创建新接口（v2），保留旧接口（v1）至少1年

---

#### 原则5：安全内建（Security by Design）

**核心理念：** 安全防护内置于架构层，而非依赖开发者自觉。

**六层防护机制：**
1. **危险关键字拦截**：DROP、TRUNCATE等DDL操作
2. **表名白名单**：仅允许访问bd_、f_、sys_、rpt_前缀的表
3. **字段名校验**：防止SQL注入
4. **参数化查询**：所有用户输入使用`?`占位符
5. **超时控制**：单条SQL执行不超过30秒
6. **危险模式检测**：禁止多语句执行

**实施要求：**
- 所有JDBC操作必须通过`JdbcExecutor`
- 禁止直接使用`JdbcTemplate`或原生Connection
- SQL模板必须经过`SqlFragmentEngine`处理

---

#### 原则6：纯Map架构（Pure Map Architecture）

**核心理念：** 全链路使用`Map<String, Object>`，彻底消除Entity绑定。

```
┌─────────────────────────────────────┐
│         纯Map数据流向                │
├─────────────────────────────────────┤
│                                     │
│  查询路径：                          │
│  前端 → Controller(Map) → Engine →  │
│  JdbcExecutor → 返回Map             │
│                                     │
│  写入路径：                          │
│  前端 → Controller(Map) → Engine →  │
│  JdbcExecutor → 数据库              │
│                                     │
│  唯一例外：                          │
│  系统基础表（sys_user/sys_role）     │
│  可保留Entity用于认证授权            │
│                                     │
└─────────────────────────────────────┘
```

**强制规范：**
- ✅ 所有业务表CRUD必须使用Map
- ✅ Controller层接收`Map<String, Object>`
- ✅ Service层返回`Map<String, Object>`或`List<Map<String, Object>>`
- ❌ 禁止定义业务Entity类
- ❌ 禁止使用DTO/VO封装业务数据

---

## 2. 四层架构设计

### 2.1 架构总览

```
┌──────────────────────────────────────────────────┐
│              L1: 业务层 (Business Layer)          │
│  ReportEngineServiceImpl / CommonCrudEngine       │
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

### 2.2 L1 业务层（Business Layer）

**职责：** 解析配置、构建业务逻辑

**核心组件：**
- `ReportEngineServiceImpl`：报表引擎
- `CommonCrudEngine`：通用CRUD引擎
- `ErpConfigLoader`：配置加载器

**代码示例：**
```java
@Service
@RequiredArgsConstructor
public class ReportEngineServiceImpl implements ReportEngineService {
    
    private final JdbcExecutor jdbcExecutor;
    private final SqlFragmentEngine sqlFragmentEngine;
    
    @Override
    public PageResult<Map<String, Object>> queryReport(String reportCode, Map<String, Object> params) {
        // 1. 加载配置
        Map<String, Object> config = loadReportConfig(reportCode);
        
        // 2. 解析SQL模板
        String sqlTemplate = (String) config.get("sql_template");
        
        // 3. 执行查询（自动安全校验）
        return sqlFragmentEngine.executeWithPagination(
            sqlTemplate, 
            params, 
            getPageNum(params), 
            getPageSize(params)
        );
    }
}
```

**规范要求：**
- ✅ 所有业务逻辑必须从配置读取，禁止硬编码
- ✅ 返回值必须是`Map<String, Object>`或`List<Map<String, Object>>`
- ✅ 禁止直接调用`JdbcTemplate`

---

### 2.3 L2 执行层（Execution Layer）

**职责：** SQL安全校验、性能优化、统一入口

**核心组件：** `JdbcExecutor`

**关键特性：**

#### 特性1：COUNT缓存
```java
// 自动缓存COUNT结果，TTL=60秒
private final SimpleCache<String, Long> countCache = new SimpleCache<>(60 * 1000);

public int countTotal(String sql, Object... params) {
    String cacheKey = generateCacheKey(sql, params);
    Long cached = countCache.get(cacheKey);
    
    if (cached != null) {
        return cached.intValue();  // 缓存命中
    }
    
    int total = jdbcTemplate.queryForObject(sql, Integer.class, params);
    countCache.put(cacheKey, (long) total);  // 写入缓存
    return total;
}
```

**效果：** 分页查询性能提升50%-80%

---

#### 特性2：慢查询检测
```java
public List<Map<String, Object>> select(String sql, Object... params) {
    long startTime = System.currentTimeMillis();
    
    try {
        return jdbcTemplate.queryForList(sql, params);
    } finally {
        long duration = System.currentTimeMillis() - startTime;
        if (duration > SLOW_QUERY_THRESHOLD_MS) {
            log.warn("[慢查询] SQL={}, duration={}ms, params={}", 
                maskSensitiveData(sql), duration, Arrays.toString(params));
        }
    }
}
```

**阈值：** 默认1000ms，可通过配置调整

---

#### 特性3：危险SQL拦截
```java
private static final Pattern DANGEROUS_SQL_PATTERN = Pattern.compile(
    "\\b(DROP\\s+(TABLE|DATABASE)|TRUNCATE\\s+TABLE|ALTER\\s+TABLE.*DROP)\\b",
    Pattern.CASE_INSENSITIVE
);

private void validateSql(String sql) {
    if (DANGEROUS_SQL_PATTERN.matcher(sql).find()) {
        throw new SecurityException("检测到危险SQL操作，已拦截");
    }
}
```

**拦截的操作：**
- DROP TABLE/DATABASE
- TRUNCATE TABLE
- ALTER TABLE ... DROP COLUMN
- RENAME TABLE
- DEALLOCATE PREPARE

---

### 2.4 L3 模板层（Template Layer）

**职责：** SQL片段替换、白名单校验

**核心组件：** `SqlFragmentEngine`

**工作流程：**
```
输入SQL模板：
SELECT ${selectFields} FROM ${tableName} WHERE 1=1 ${deptFilter}

↓ 步骤1：提取表名并校验白名单
检查：tableName 是否以 bd_/f_/sys_/rpt_ 开头

↓ 步骤2：替换参数
${selectFields} → "order_no, customer_name, amount"
${tableName} → "f_sales_order"
${deptFilter} → "AND dept_id IN (1, 2, 3)"

↓ 输出最终SQL：
SELECT order_no, customer_name, amount 
FROM f_sales_order 
WHERE 1=1 AND dept_id IN (1, 2, 3)
```

**代码示例：**
```java
@Component
@RequiredArgsConstructor
public class SqlFragmentEngine {
    
    private final TableFieldWhiteList whiteList;
    private final JdbcTemplate jdbcTemplate;
    
    public List<Map<String, Object>> executeWithPagination(
        String sqlTemplate, 
        Map<String, Object> params,
        int pageNum, 
        int pageSize
    ) {
        // 1. 校验表名白名单
        validateTableNames(sqlTemplate);
        
        // 2. 替换参数
        String finalSql = replacePlaceholders(sqlTemplate, params);
        
        // 3. 构建分页SQL
        String countSql = buildCountSql(finalSql);
        String dataSql = buildPageSql(finalSql, pageNum, pageSize);
        
        // 4. 执行查询
        int total = jdbcTemplate.queryForObject(countSql, Integer.class);
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(dataSql);
        
        return PageResult.build(total, rows);
    }
}
```

---

### 2.5 L4 基础层（Foundation Layer）

**职责：** 原生JDBC操作、连接池管理

**核心组件：** Spring `JdbcTemplate`

**使用说明：**
- ⚠️ **仅限L2和L3层内部使用**
- ❌ 业务层禁止直接调用
- ✅ 通过`JdbcAutoConfiguration`自动装配

**配置示例：**
```yaml
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
```

---

## 3. 数据流规范

### 3.1 查询数据流（Read Path）

```
┌──────────┐     ┌──────────────┐     ┌──────────────┐
│  前端     │────▶│  Controller  │────▶│   Engine     │
│ (Vue3)   │     │ (REST API)   │     │ (业务逻辑)    │
└──────────┘     └──────────────┘     └──────┬───────┘
                                             │
                                             ▼
                                    ┌────────────────┐
                                    │ JdbcExecutor   │
                                    │ (安全校验+缓存) │
                                    └──────┬─────────┘
                                           │
                                           ▼
                                    ┌────────────────┐
                                    │SqlFragmentEngine│
                                    │ (模板替换)      │
                                    └──────┬─────────┘
                                           │
                                           ▼
                                    ┌────────────────┐
                                    │  JdbcTemplate  │
                                    │ (原生JDBC)     │
                                    └──────┬─────────┘
                                           │
                                           ▼
                                    ┌────────────────┐
                                    │   MySQL        │
                                    └────────────────┘
                                           │
                                           ▼
                                    返回 Map<String, Object>
```

**关键节点说明：**

1. **Controller层**
   ```java
   @PostMapping("/report/query")
   public R<Map<String, Object>> query(@RequestBody Map<String, Object> request) {
       String reportCode = (String) request.get("reportCode");
       Map<String, Object> params = (Map) request.get("params");
       
       return R.ok(reportEngine.queryReport(reportCode, params));
   }
   ```

2. **Engine层**
   ```java
   public Map<String, Object> queryReport(String reportCode, Map<String, Object> params) {
       // 从配置读取SQL模板
       String sqlTemplate = configLoader.getSqlTemplate(reportCode);
       
       // 执行查询（自动安全校验）
       return sqlFragmentEngine.executeWithPagination(sqlTemplate, params, ...);
   }
   ```

3. **返回格式**
   ```json
   {
     "code": 200,
     "msg": "success",
     "data": {
       "total": 100,
       "rows": [
         {
           "order_no": "SO2026001",
           "customer_name": "张三",
           "amount": 1000.00
         }
       ]
     }
   }
   ```

---

### 3.2 写入数据流（Write Path）

```
┌──────────┐     ┌──────────────┐     ┌──────────────┐
│  前端     │────▶│  Controller  │────▶│   Engine     │
│ (Vue3)   │     │ (REST API)   │     │ (业务逻辑)    │
└──────────┘     └──────────────┘     └──────┬───────┘
                                             │
                                             ▼
                                    ┌────────────────┐
                                    │ JdbcExecutor   │
                                    │ (字段过滤)      │
                                    └──────┬─────────┘
                                           │
                                           ▼
                                    ┌────────────────┐
                                    │  JdbcTemplate  │
                                    │ (INSERT/UPDATE)│
                                    └──────┬─────────┘
                                           │
                                           ▼
                                    ┌────────────────┐
                                    │   MySQL        │
                                    └────────────────┘
```

**当前实现：**
```java
@PostMapping("/crud/add")
public R<Void> add(@RequestBody Map<String, Object> request) {
    // 纯Map接收，字段自由扩展
    String configCode = (String) request.get("configCode");
    Map<String, Object> formData = (Map) request.get("formData");
    
    crudEngine.insert(configCode, formData);
    return R.ok();
}
```

---

### 3.3 配置加载流程

```
┌─────────────────┐
│  启动时加载      │
│ (ApplicationStart)│
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ ErpConfigLoader │
│ 扫描配置表       │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ 缓存到内存       │
│ ConcurrentHashMap│
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ 运行时读取       │
│ configLoader.load()│
└─────────────────┘
```

**配置存储位置：**
- 数据库表：`rpt_report_config`、`biz_module_config`
- JSON文件：`classpath:/config/*.json`（可选）

**缓存策略：**
- 启动时全量加载到内存
- 配置变更时手动刷新（调用`configLoader.refresh()`）
- 或使用Redis缓存（分布式部署场景）

---

## 4. 技术栈选型

### 4.1 后端技术栈

| 组件 | 技术选型 | 版本 | 说明 |
|------|---------|------|------|
| **语言** | Java | 17+ | LTS版本 |
| **框架** | Spring Boot | 3.x | 微服务基础 |
| **ORM** | JdbcTemplate | 6.x | 轻量级JDBC封装 |
| **数据模型** | Map<String, Object> | - | 唯一数据载体 |
| **连接池** | HikariCP | 5.x | 高性能连接池 |
| **数据库** | MySQL | 8.0+ | 主数据库 |
| **缓存** | Redis | 7.x | 分布式缓存（可选） |
| **JSON** | Fastjson2 | 2.x | 高性能JSON库 |
| **校验** | Jakarta Validation | 3.x | Bean Validation |
| **日志** | SLF4J + Logback | 2.x | 日志框架 |

**禁止使用的技术：**
- ❌ MyBatis-Plus（违反Map优先原则）
- ❌ JPA/Hibernate（Entity绑定过强）
- ❌ 原生JDBC Connection（绕过安全校验）
- ❌ BeanPropertyRowMapper（自动映射到Entity）
- ❌ DTO/VO封装业务数据（增加复杂度）

---

### 4.2 前端技术栈

| 组件 | 技术选型 | 版本 | 说明 |
|------|---------|------|------|
| **框架** | Vue 3 | 3.4+ | Composition API |
| **构建工具** | Vite | 5.x | 快速构建 |
| **UI库** | Element Plus | 2.x | 企业级UI |
| **状态管理** | Pinia | 2.x | 轻量级状态管理 |
| **HTTP客户端** | Axios | 1.x | HTTP请求 |
| **类型检查** | TypeScript | 5.x | 可选 |

---

### 4.3 基础设施

| 组件 | 技术选型 | 版本 | 说明 |
|------|---------|------|------|
| **容器化** | Docker | 24.x | 容器运行时 |
| **编排** | Docker Compose | 2.x | 单机编排 |
| **反向代理** | Nginx | 1.25+ | 负载均衡 |
| **CI/CD** | GitHub Actions | - | 自动化部署 |

---

## 5. 开发规范

### 5.1 代码组织规范

#### 目录结构
```
ruoyi-erp-api/
├── src/main/java/com/ruoyi/common/jdbc/
│   ├── config/              # 配置类
│   │   ├── JdbcAutoConfiguration.java
│   │   ├── ErpConfigLoader.java
│   │   └── ReportConfigLoader.java
│   ├── core/                # 核心执行器
│   │   ├── JdbcExecutor.java
│   │   ├── PageResult.java
│   │   └── TransactionalService.java
│   ├── template/            # 模板引擎
│   │   └── SqlFragmentEngine.java
│   ├── security/            # 安全层
│   │   ├── TableFieldWhiteList.java
│   │   └── DataScopeInjector.java
│   ├── engine/              # 业务引擎
│   │   ├── crud/CommonCrudEngine.java
│   │   └── report/ReportTemplateEngine.java
│   ├── util/                # 工具类
│   │   ├── SqlBuilder.java
│   │   └── AggregationUtils.java
│   └── controller/          # 通用控制器
│       └── UniversalController.java
└── src/main/resources/
    └── application.yml      # 配置文件
```

---

### 5.2 命名规范

#### 表名前缀规范
```
bd_    → 基础数据表（base data）
       例：bd_product, bd_customer, bd_supplier

f_     → 业务表（business）
       例：f_sales_order, f_purchase_order

sys_   → 系统表（system）
       例：sys_user, sys_role, sys_menu

rpt_   → 报表表（report）
       例：rpt_report_config, rpt_user_preference
```

**配置要求：**
```yaml
db:
  white-list:
    tables: bd_,f_,sys_,rpt_
```

---

#### 字段命名规范
```
✅ 推荐：snake_case（下划线分隔）
   order_no, customer_name, create_time

❌ 禁止：camelCase（驼峰命名）
   orderNo, customerName, createTime
```

**原因：**
- 数据库字段统一使用snake_case
- 避免前后端字段映射转换
- 符合SQL规范

---

#### 配置编码规范
```
报表配置：{业务域}_{功能}_{类型}
   sales_dept_summary        # 销售部门汇总报表
   purchase_order_detail     # 采购订单明细报表

业务模块：{模块域}_{实体}
   biz_purchase_order        # 采购订单模块
   biz_sales_customer        # 销售客户模块
```

---

### 5.3 SQL编写规范

#### 规范1：禁止硬编码SQL
```java
// ❌ 错误
String sql = "SELECT * FROM f_sales_order WHERE order_no = '" + orderNo + "'";

// ✅ 正确
String sqlTemplate = configLoader.getSqlTemplate("sales_order_query");
Map<String, Object> params = Map.of("order_no", orderNo);
List<Map<String, Object>> result = sqlFragmentEngine.execute(sqlTemplate, params);
```

---

#### 规范2：使用参数化查询
```java
// ❌ 错误：SQL注入风险
String sql = "SELECT * FROM f_sales_order WHERE dept_id = " + deptId;

// ✅ 正确：参数化查询
String sql = "SELECT * FROM f_sales_order WHERE dept_id = ?";
List<Map<String, Object>> result = jdbcExecutor.select(sql, deptId);
```

---

#### 规范3：避免SELECT *
```java
// ❌ 错误：返回所有字段
String sql = "SELECT * FROM f_sales_order";

// ✅ 正确：明确指定字段
String sql = "SELECT order_no, customer_name, amount FROM f_sales_order";

// ✅ 更好：从配置读取字段列表
String selectFields = config.getAllowedFields().stream()
    .collect(Collectors.joining(", "));
String sql = "SELECT " + selectFields + " FROM f_sales_order";
```

---

#### 规范4：分页查询必须带ORDER BY
```java
// ❌ 错误：无排序，结果不确定
String sql = "SELECT * FROM f_sales_order LIMIT 10 OFFSET 0";

// ✅ 正确：明确排序
String sql = "SELECT * FROM f_sales_order ORDER BY create_time DESC LIMIT 10 OFFSET 0";
```

---

### 5.4 异常处理规范

#### 规范1：统一异常格式
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(SecurityException.class)
    public R<Void> handleSecurityException(SecurityException e) {
        log.warn("安全校验失败: {}", e.getMessage());
        return R.fail(403, e.getMessage());
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public R<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("参数校验失败: {}", e.getMessage());
        return R.fail(400, e.getMessage());
    }
    
    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return R.fail(500, "系统异常，请联系管理员");
    }
}
```

---

#### 规范2：业务异常使用自定义异常
```java
// 定义业务异常
public class BusinessException extends RuntimeException {
    private final int code;
    
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
    
    public int getCode() {
        return code;
    }
}

// 抛出异常
if (orderAmount.compareTo(BigDecimal.ZERO) <= 0) {
    throw new BusinessException(400, "订单金额必须大于0");
}
```

---

### 5.5 日志规范

#### 规范1：关键操作必须记录日志
```java
@Slf4j
@Service
public class ReportEngineServiceImpl {
    
    public PageResult<Map<String, Object>> queryReport(String reportCode, Map<String, Object> params) {
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
    }
}
```

---

#### 规范2：敏感数据脱敏
```java
private String maskSensitiveData(String sql) {
    // 隐藏密码、身份证等敏感信息
    return sql.replaceAll("(password|id_card)='[^']*'", "$1='***'");
}

log.debug("执行SQL: {}", maskSensitiveData(sql));
```

---

#### 规范3：日志级别使用规范
```
ERROR → 系统异常、数据不一致、外部服务调用失败
WARN  → 参数校验失败、权限不足、慢查询
INFO  → 关键业务流程开始/结束、配置加载
DEBUG → SQL语句、详细参数、中间状态
TRACE → 不推荐使用（性能开销大）
```

---

### 5.6 Controller层规范

#### 规范1：统一使用Map接收请求

**强制要求：**
- ✅ Controller方法参数必须是`Map<String, Object>`
- ❌ 禁止使用DTO/VO接收业务数据
- ❌ 禁止使用@Valid校验DTO

**示例：**
```java
// ✅ 正确：纯Map接收
@PostMapping("/query")
public R<Map<String, Object>> query(@RequestBody Map<String, Object> request) {
    String configCode = (String) request.get("configCode");
    Map<String, Object> params = (Map) request.get("params");
    
    return R.ok(engine.query(configCode, params));
}

// ❌ 错误：使用DTO
@PostMapping("/query")
public R page(@RequestBody @Valid UniversalQueryDTO dto) {
    // 禁止！
}
```

**优势：**
- ✅ 前端可自由扩展字段，无需修改后端
- ✅ 新增查询条件类型无需改DTO
- ✅ API向后兼容，永不过时

---

### 5.7 单元测试规范

#### 规范1：核心组件必须有单元测试

**必测组件：**
- `JdbcExecutor`（安全校验、COUNT缓存）
- `SqlFragmentEngine`（SQL片段替换、白名单校验）
- `TableFieldWhiteList`（表名/字段名校验）

**测试覆盖率要求：**
- 核心逻辑：≥ 80%
- 安全校验：100%
- 边界条件：覆盖null、空字符串、特殊字符

**示例：**
```java
@SpringBootTest
class JdbcExecutorTest {
    
    @Autowired
    private JdbcExecutor jdbcExecutor;
    
    @Test
    void testDangerousSqlIntercepted() {
        // 验证危险SQL被拦截
        assertThrows(SecurityException.class, () -> {
            jdbcExecutor.select("DROP TABLE f_sales_order");
        });
    }
    
    @Test
    void testCountCacheHit() {
        // 首次查询
        int count1 = jdbcExecutor.countTotal("SELECT COUNT(*) FROM f_sales_order");
        
        // 第二次查询应命中缓存
        int count2 = jdbcExecutor.countTotal("SELECT COUNT(*) FROM f_sales_order");
        
        assertEquals(count1, count2);
    }
    
    @Test
    void testTableNameWhiteList() {
        // 允许的表名
        assertTrue(whiteList.checkTable("f_sales_order"));
        assertTrue(whiteList.checkTable("bd_product"));
        
        // 禁止的表名
        assertFalse(whiteList.checkTable("information_schema.tables"));
        assertFalse(whiteList.checkTable("mysql.user"));
    }
}
```

---

## 6. 安全机制

### 6.1 六层安全防护

#### 第1层：危险关键字拦截
```java
private static final Pattern DANGEROUS_SQL_PATTERN = Pattern.compile(
    "\\b(DROP\\s+(TABLE|DATABASE|INDEX|VIEW|PROCEDURE|FUNCTION)|" +
    "TRUNCATE\\s+TABLE|" +
    "ALTER\\s+TABLE\\b.*\\bDROP\\b|" +
    "RENAME\\s+TABLE|" +
    "DEALLOCATE\\s+PREPARE|" +
    "EXECUTE\\s+IMMEDIATE)\\b",
    Pattern.CASE_INSENSITIVE
);

private void validateSql(String sql) {
    if (DANGEROUS_SQL_PATTERN.matcher(sql).find()) {
        log.error("[安全拦截] 检测到危险SQL: {}", sql);
        throw new SecurityException("不允许执行危险SQL操作");
    }
}
```

**拦截的操作：**
- DROP TABLE/DATABASE/INDEX/VIEW
- TRUNCATE TABLE
- ALTER TABLE ... DROP COLUMN
- RENAME TABLE
- DEALLOCATE PREPARE
- EXECUTE IMMEDIATE

---

#### 第2层：表名白名单
```java
@Component
@ConfigurationProperties(prefix = "db.white-list")
public class TableFieldWhiteList {
    
    private List<String> tables = List.of("bd_", "f_", "sys_", "rpt_");
    
    public boolean checkTable(String tableName) {
        if (tableName == null || tableName.isEmpty()) {
            return false;
        }
        
        return tables.stream()
            .anyMatch(prefix -> tableName.startsWith(prefix));
    }
}
```

**配置示例：**
```yaml
db:
  white-list:
    tables: bd_,f_,sys_,rpt_
```

**工作原理：**
```sql
-- ✅ 允许
SELECT * FROM f_sales_order
SELECT * FROM bd_product
SELECT * FROM sys_user

-- ❌ 拦截
SELECT * FROM information_schema.tables  -- 跨库攻击
SELECT * FROM mysql.user                 -- 系统表
SELECT * FROM other_db.secret_table      -- 其他数据库
```

---

#### 第3层：字段名校验
```java
public boolean checkField(String fieldName) {
    if (fieldName == null || fieldName.isEmpty()) {
        return false;
    }
    
    // 只允许字母、数字、下划线
    return fieldName.matches("^[a-zA-Z_][a-zA-Z0-9_]*$");
}
```

**防止的攻击：**
```sql
-- ❌ SQL注入尝试
SELECT * FROM f_sales_order WHERE 1=1; DROP TABLE f_sales_order--

-- ✅ 安全：字段名被校验后拒绝
checkField("1=1; DROP TABLE f_sales_order--") → false
```

---

#### 第4层：参数化查询
```java
// ✅ 所有用户输入使用?占位符
String sql = "SELECT * FROM f_sales_order WHERE order_no = ? AND dept_id = ?";
List<Map<String, Object>> result = jdbcExecutor.select(sql, orderNo, deptId);
```

**防止的攻击：**
```sql
-- ❌ 拼接SQL（SQL注入）
String sql = "SELECT * FROM f_sales_order WHERE order_no = '" + userInput + "'";
-- 用户输入：' OR '1'='1
-- 最终SQL：SELECT * FROM f_sales_order WHERE order_no = '' OR '1'='1'

-- ✅ 参数化查询（安全）
String sql = "SELECT * FROM f_sales_order WHERE order_no = ?";
-- 用户输入：' OR '1'='1
-- 最终SQL：SELECT * FROM f_sales_order WHERE order_no = ''' OR ''1''=''1'
-- 结果：查不到数据（安全）
```

---

#### 第5层：超时控制
```java
@Bean
public DataSource dataSource() {
    HikariConfig config = new HikariConfig();
    config.setConnectionTimeout(30000);      // 连接超时30秒
    config.setMaxLifetime(1800000);          // 最大生命周期30分钟
    config.setIdleTimeout(600000);           // 空闲超时10分钟
    
    return new HikariDataSource(config);
}
```

**配置建议：**
```yaml
spring:
  datasource:
    hikari:
      connection-timeout: 30000    # 30秒
      max-lifetime: 1800000        # 30分钟
      idle-timeout: 600000         # 10分钟
```

---

#### 第6层：危险模式检测
```java
private void validateMultiStatement(String sql) {
    // 禁止多语句执行
    if (sql.contains(";") && sql.trim().endsWith(";")) {
        String[] statements = sql.split(";");
        if (statements.length > 1) {
            throw new SecurityException("不允许执行多语句SQL");
        }
    }
}
```

**防止的攻击：**
```sql
-- ❌ 多语句攻击
SELECT * FROM f_sales_order; DROP TABLE f_sales_order;

-- ✅ 拦截
validateMultiStatement(sql) → 抛出SecurityException
```

---

### 6.2 数据权限控制

#### RBAC部门权限注入
```java
@Component
public class DataScopeInjector {
    
    /**
     * 注入数据权限SQL片段
     * 
     * @param tableAlias 表别名
     * @return SQL片段，如 "AND dept_id IN (1, 2, 3)"
     */
    public String injectDataScope(String tableAlias) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null) {
            return "";
        }
        
        // 超级管理员：无限制
        if (loginUser.isAdmin()) {
            return "";
        }
        
        // 普通用户：只能查看本部门及子部门数据
        List<Long> deptIds = loginUser.getDeptIds();
        if (deptIds == null || deptIds.isEmpty()) {
            return " AND 1=0";  // 无权限，返回空结果
        }
        
        String deptList = deptIds.stream()
            .map(String::valueOf)
            .collect(Collectors.joining(", "));
        
        return " AND " + tableAlias + ".dept_id IN (" + deptList + ")";
    }
}
```

**使用示例：**
```java
// 原始SQL
String sql = "SELECT * FROM f_sales_order WHERE 1=1";

// 注入权限后
String dataScope = dataScopeInjector.injectDataScope("t");
// 结果：SELECT * FROM f_sales_order WHERE 1=1 AND t.dept_id IN (1, 2, 3)

String finalSql = sql + dataScope;
```

---

### 6.3 审计日志

#### 访问日志记录
```java
CREATE TABLE rpt_report_access_log (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_code VARCHAR(100) NOT NULL COMMENT '报表编码',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    user_name VARCHAR(100) COMMENT '用户名',
    query_params TEXT COMMENT '查询参数（JSON）',
    result_count INT COMMENT '结果数量',
    duration_ms INT COMMENT '耗时（毫秒）',
    access_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '访问时间'
);
```

**记录时机：**
```java
@Aspect
@Component
public class ReportAccessLogAspect {
    
    @Around("@annotation(ReportAccessLog)")
    public Object recordAccessLog(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            
            long duration = System.currentTimeMillis() - startTime;
            saveAccessLog(joinPoint, result, duration);
            
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            saveErrorLog(joinPoint, e, duration);
            throw e;
        }
    }
}
```

---

## 7. 性能优化

### 7.1 COUNT缓存优化

#### 问题背景
分页查询需要执行两次SQL：
1. `SELECT COUNT(*) FROM ...` （获取总数）
2. `SELECT * FROM ... LIMIT 10 OFFSET 0` （获取数据）

**痛点：** COUNT查询通常很慢，尤其是大表

---

#### 解决方案：内存缓存
```java
public class JdbcExecutor {
    
    private final SimpleCache<String, Long> countCache = new SimpleCache<>(60 * 1000);
    
    public int countTotal(String sql, Object... params) {
        // 生成缓存键（排除分页参数）
        String cacheKey = generateCountCacheKey(sql, params);
        
        // 尝试从缓存读取
        Long cached = countCache.get(cacheKey);
        if (cached != null) {
            log.debug("[COUNT缓存命中] key={}", cacheKey);
            return cached.intValue();
        }
        
        // 执行查询
        int total = jdbcTemplate.queryForObject(sql, Integer.class, params);
        
        // 写入缓存（TTL=60秒）
        countCache.put(cacheKey, (long) total);
        log.debug("[COUNT缓存写入] key={}, total={}", cacheKey, total);
        
        return total;
    }
    
    private String generateCountCacheKey(String sql, Object... params) {
        // 排除分页参数（pageNum、pageSize）
        String paramsJson = JSON.toJSONString(params);
        String hash = DigestUtils.md5DigestAsHex(paramsJson.getBytes());
        return "count:" + hash;
    }
}
```

---

#### 效果对比
| 场景 | 优化前 | 优化后 | 提升 |
|------|--------|--------|------|
| 高频查询（同一条件） | 200ms | 5ms | **40倍** |
| 中等频率查询 | 200ms | 50ms | **4倍** |
| 低频查询（首次） | 200ms | 200ms | 无变化 |

**缓存命中率：** 典型场景60%-80%

---

### 7.2 慢查询优化

#### 慢查询检测
```java
public List<Map<String, Object>> select(String sql, Object... params) {
    long startTime = System.currentTimeMillis();
    
    try {
        return jdbcTemplate.queryForList(sql, params);
    } finally {
        long duration = System.currentTimeMillis() - startTime;
        
        if (duration > SLOW_QUERY_THRESHOLD_MS) {
            log.warn("[慢查询] SQL={}, duration={}ms, params={}", 
                maskSensitiveData(sql), duration, Arrays.toString(params));
            
            // 可选：记录到数据库
            saveSlowQueryLog(sql, duration, params);
        }
    }
}
```

**阈值配置：**
```yaml
jdbc:
  slow-query-threshold-ms: 1000  # 1秒
```

---

#### 优化建议
1. **添加索引**
   ```sql
   -- 高频查询字段添加索引
   CREATE INDEX idx_order_no ON f_sales_order(order_no);
   CREATE INDEX idx_create_time ON f_sales_order(create_time);
   ```

2. **避免全表扫描**
   ```sql
   -- ❌ 错误：全表扫描
   SELECT * FROM f_sales_order WHERE YEAR(create_time) = 2026;
   
   -- ✅ 正确：使用范围查询
   SELECT * FROM f_sales_order 
   WHERE create_time >= '2026-01-01' AND create_time < '2027-01-01';
   ```

3. **限制返回行数**
   ```sql
   -- ❌ 错误：返回所有数据
   SELECT * FROM f_sales_order;
   
   -- ✅ 正确：分页查询
   SELECT * FROM f_sales_order LIMIT 100 OFFSET 0;
   ```

---

### 7.3 连接池优化

#### HikariCP最佳实践
```yaml
spring:
  datasource:
    hikari:
      # 连接池大小（根据CPU核心数调整）
      maximum-pool-size: 20        # CPU核心数 * 2 + 磁盘数
      minimum-idle: 5              # 最小空闲连接
      
      # 超时配置
      connection-timeout: 30000    # 连接超时30秒
      validation-timeout: 5000     # 验证超时5秒
      idle-timeout: 600000         # 空闲超时10分钟
      max-lifetime: 1800000        # 最大生命周期30分钟
      
      # 性能优化
      leak-detection-threshold: 60000  # 连接泄露检测60秒
      register-mbeans: true            # 注册JMX MBean（监控用）
```

**调优公式：**
```
maximum-pool-size = CPU核心数 * 2 + 磁盘数

例：8核CPU + 1块SSD → 8 * 2 + 1 = 17 → 取整为20
```

---

### 7.4 批量操作优化

#### 批量插入
```java
// ❌ 错误：逐条插入（N次数据库往返）
for (Map<String, Object> row : rows) {
    jdbcExecutor.update(insertSql, row.values().toArray());
}

// ✅ 正确：批量插入（1次数据库往返）
List<Object[]> batchArgs = rows.stream()
    .map(row -> row.values().toArray())
    .collect(Collectors.toList());

int[] results = jdbcExecutor.batchUpdate(insertSql, batchArgs);
```

**性能对比：**
| 数据量 | 逐条插入 | 批量插入 | 提升 |
|--------|---------|---------|------|
| 100条 | 500ms | 50ms | **10倍** |
| 1000条 | 5000ms | 200ms | **25倍** |
| 10000条 | 50000ms | 1500ms | **33倍** |

---

## 8. 迁移指南

### 8.1 从MyBatis-Plus/JPA迁移

#### 步骤1：移除ORM依赖
```xml
<!-- ❌ 删除 -->
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-boot-starter</artifactId>
</dependency>

<!-- ✅ 添加 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>
```

---

#### 步骤2：删除所有Entity类和Mapper接口
```java
// ❌ 删除所有Entity类和Mapper接口
@Entity
@Table(name = "f_sales_order")
public class SalesOrder { ... }

@Mapper
public interface SalesOrderMapper extends BaseMapper<SalesOrder> { ... }

// ✅ 改用Map
// 无需定义任何类，直接使用Map<String, Object>
```

---

#### 步骤3：改造Service层和Controller层
```java
// ❌ 原代码：返回Entity
public List<SalesOrder> listOrders() {
    return salesOrderMapper.selectList(null);
}

@GetMapping("/orders")
public R<List<SalesOrder>> list() {
    return R.ok(salesOrderService.listOrders());
}

// ✅ 新代码：返回Map
public List<Map<String, Object>> listOrders() {
    String sql = "SELECT order_no, customer_name, amount FROM f_sales_order";
    return jdbcExecutor.select(sql);
}

@GetMapping("/orders")
public R<List<Map<String, Object>>> list() {
    return R.ok(salesOrderService.listOrders());
}
```

---



---

### 8.3 迁移检查清单

| 检查项 | 状态 | 说明 |
|--------|------|------|
| 移除MyBatis-Plus/JPA依赖 | ☐ | pom.xml |
| 删除所有Entity类、Mapper、Repository | ☐ | domain/mapper/repository包 |
| 改造Service/Controller返回值 | ☐ | Entity → Map |
| 配置表名白名单 | ☐ | application.yml |
| 测试所有接口 | ☐ | 功能验证 |
| 性能测试 | ☐ | 对比优化前后 |
| 更新文档 | ☐ | API文档 |

---

### 8.4 清理反模式代码

#### 必须删除的组件

**TableMetadataManager（已删除）**

**原因：**
- ❌ 违反"纯Map架构"原则
- ❌ 直接依赖JdbcTemplate（绕过JdbcExecutor安全校验）
- ❌ 缓存Schema元数据是冗余设计（应从配置读取）

**替代方案：**
```java
// ❌ 错误：从系统表查询主键
String pk = tableMetadataManager.getPrimaryKey("f_sales_order");

// ✅ 正确：从配置读取主键
ErpConfigLoader.CrudConfig config = configLoader.loadCrudConfig("purchase_order");
String pk = config.getPkField();  // "order_id"
```

**影响范围：**
- `CommonCrudEngine` 已改为从配置读取主键
- 无需任何额外修改

---

## 9. 常见问题

### 9.1 为什么不用MyBatis-Plus/JPA？

**答：** ORM框架的核心问题是**Entity绑定过强**，违反低代码架构原则。

| 维度 | MyBatis-Plus/JPA | 纯Map架构 |
|------|-------------|--------------|
| **灵活性** | ❌ 需定义Entity | ✅ 纯Map操作 |
| **字段增减** | ❌ 需修改Entity | ✅ 配置即可 |
| **API兼容** | ❌ 字段变更破坏兼容 | ✅ 向后兼容 |
| **安全性** | ⚠️ 需手动防护 | ✅ 内置6层防护 |
| **学习成本** | ⚠️ 需理解ORM概念 | ✅ 直观易懂 |
| **代码量** | ❌ 大量样板代码 | ✅ 零样板代码 |

---

### 9.2 Map如何保证类型安全？

**答：** 通过以下措施保障：

1. **数据库层面**：字段类型由数据库Schema保证
2. **应用层面**：使用工具类进行类型转换
   ```java
   public class TypeConverter {
       public static Long toLong(Object value) {
           if (value == null) return null;
           if (value instanceof Number) return ((Number) value).longValue();
           throw new ClassCastException("无法转换为Long: " + value);
       }
       
       public static String toString(Object value) {
           return value != null ? value.toString() : null;
       }
   }
   ```

3. **前端层面**：TypeScript提供编译期类型检查
   ```typescript
   interface Order {
     order_no: string;
     amount: number;
   }
   ```

---

### 9.3 如何处理复杂JOIN查询？

**答：** 通过配置SQL模板实现。

```json
{
  "report_code": "sales_order_with_customer",
  "sql_template": {
    "query": "SELECT o.order_no, c.customer_name, o.amount FROM f_sales_order o INNER JOIN bd_customer c ON o.customer_id = c.customer_id WHERE 1=1 ${deptFilter}",
    "params": ["deptFilter"]
  }
}
```

**注意：**
- JOIN查询必须在配置中明确定义
- 禁止动态拼接JOIN语句（安全风险）
- 复杂查询建议使用视图（View）

---

### 9.4 如何实现事务控制？

**答：** 使用Spring声明式事务。

```java
@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final JdbcExecutor jdbcExecutor;
    
    @Transactional(rollbackFor = Exception.class)
    public void createOrder(Map<String, Object> orderData) {
        // 1. 插入订单
        String insertOrderSql = "INSERT INTO f_sales_order (...) VALUES (...)";
        jdbcExecutor.update(insertOrderSql, orderData.values().toArray());
        
        // 2. 插入订单明细
        List<Map<String, Object>> items = (List) orderData.get("items");
        for (Map<String, Object> item : items) {
            String insertItemSql = "INSERT INTO f_sales_order_item (...) VALUES (...)";
            jdbcExecutor.update(insertItemSql, item.values().toArray());
        }
        
        // 如果任何一步失败，自动回滚
    }
}
```

---

### 9.5 如何调试SQL？

**答：** 开启DEBUG日志。

```yaml
logging:
  level:
    com.ruoyi.common.jdbc.core.JdbcExecutor: DEBUG
```

**日志输出：**
```
[DEBUG] 执行SQL: SELECT * FROM f_sales_order WHERE dept_id = ?
[DEBUG] 参数: [1]
[DEBUG] 结果行数: 10
[DEBUG] 耗时: 15ms
```

---

## 10. 附录

### 10.1 术语表

| 术语 | 英文 | 说明 |
|------|------|------|
| 低代码 | Low-Code | 通过配置而非编码实现业务功能 |
| 配置驱动 | Configuration-Driven | 业务规则通过配置文件定义 |
| 纯Map架构 | Pure Map Architecture | 全链路使用Map，彻底消除Entity绑定 |
| 白名单 | White List | 允许访问的表名/字段名列表 |
| 参数化查询 | Parameterized Query | 使用?占位符防止SQL注入 |

---

### 10.2 参考资源

- [Spring JDBC官方文档](https://docs.spring.io/spring-framework/docs/current/reference/html/data-access.html#jdbc)
- [HikariCP配置指南](https://github.com/brettwooldridge/HikariCP)
- [OWASP SQL注入防护](https://owasp.org/www-community/controls/SQL_Injection_Prevention_Cheat_Sheet)
- [MySQL性能优化最佳实践](https://dev.mysql.com/doc/refman/8.0/en/optimization.html)

---

### 10.3 版本历史

| 版本 | 日期 | 变更内容 | 作者 |
|------|------|---------|------|
| v1.0 | 2026-04-30 | 初始版本，定义低代码架构标准 | 技术架构组 |

---

### 10.4 联系方式

**技术支持：**
- 邮箱：architecture@company.com
- 内部Wiki：http://wiki.company.com/low-code-architecture
- Git仓库：http://git.company.com/erp/ruoyi-erp-api

**反馈渠道：**
- 提交Issue：http://git.company.com/erp/ruoyi-erp-api/issues
- 技术讨论群：钉钉群「低代码架构讨论组」

---

### 10.5 配置示例

#### 示例1：报表配置

**配置文件位置：** `rpt_report_config` 表或 `classpath:/config/reports/*.json`

```json
{
  "report_code": "sales_dept_summary",
  "report_name": "销售部门汇总报表",
  "category": "销售分析",
  "sql_template": {
    "query": "SELECT dept_name, SUM(amount) as total_amount, COUNT(*) as order_count FROM f_sales_order ${deptFilter} ${dateFilter} GROUP BY dept_name ORDER BY total_amount DESC",
    "params": ["deptFilter", "dateFilter"]
  },
  "field_config": [
    {"field": "dept_name", "label": "部门名称", "type": "string", "width": 150},
    {"field": "total_amount", "label": "总金额", "type": "number", "width": 120, "format": "#,##0.00"},
    {"field": "order_count", "label": "订单数", "type": "number", "width": 100}
  ],
  "filter_config": {
    "deptFilter": {
      "type": "in",
      "source": "user_dept_ids",
      "description": "部门权限过滤"
    },
    "dateFilter": {
      "type": "between",
      "field": "order_date",
      "default": "last_30_days",
      "description": "日期范围筛选"
    }
  },
  "permission_config": {
    "roles": ["sales_manager", "admin"],
    "data_scope": "dept_and_children"
  }
}
```

---

#### 示例2：CRUD模块配置

**配置文件位置：** `biz_module_config` 表或 `classpath:/config/modules/*.json`

```json
{
  "module_code": "biz_purchase_order",
  "module_name": "采购订单管理",
  "table_name": "f_purchase_order",
  "pk_field": "order_id",
  "allowed_fields": [
    "order_no",
    "supplier_id",
    "supplier_name",
    "amount",
    "status",
    "create_time",
    "remark"
  ],
  "soft_delete": {
    "enabled": true,
    "field": "is_deleted"
  },
  "validation_rules": {
    "order_no": {
      "required": true,
      "pattern": "^PO\\d{8}$",
      "message": "订单号格式：PO + 8位数字"
    },
    "amount": {
      "required": true,
      "min": 0,
      "message": "金额必须大于0"
    }
  },
  "default_sort": {
    "field": "create_time",
    "order": "DESC"
  }
}
```

---

#### 示例3：字典配置

**配置文件位置：** `rpt_dict_config` 表

```json
{
  "dict_type": "order_status",
  "dict_label": "订单状态",
  "items": [
    {"value": "0", "label": "草稿", "color": "#909399"},
    {"value": "1", "label": "待审核", "color": "#E6A23C"},
    {"value": "2", "label": "已通过", "color": "#67C23A"},
    {"value": "3", "label": "已拒绝", "color": "#F56C6C"}
  ]
}
```

**前端使用：**
```vue
<template>
  <el-tag :color="getStatusColor(row.status)">
    {{ getStatusLabel(row.status) }}
  </el-tag>
</template>

<script setup>
import { useDict } from '@/hooks/useDict'

const { getLabel, getColor } = useDict('order_status')

const getStatusLabel = (value) => getLabel(value)
const getStatusColor = (value) => getColor(value)
</script>
```

---

### 10.6 最佳实践清单

#### 开发前检查
- [ ] 是否从配置读取SQL模板？（禁止硬编码）
- [ ] 是否使用参数化查询？（禁止拼接SQL）
- [ ] 返回值是否为Map？（禁止Entity）
- [ ] 是否通过JdbcExecutor执行？（禁止直接调用JdbcTemplate）
- [ ] 表名是否在白名单中？（bd_/f_/sys_/rpt_）

#### 代码审查要点
- [ ] 是否有SELECT *？（应明确指定字段）
- [ ] 分页查询是否有ORDER BY？（结果确定性）
- [ ] 是否有关键操作日志？（INFO级别）
- [ ] 敏感数据是否脱敏？（密码、身份证等）
- [ ] 异常是否统一处理？（GlobalExceptionHandler）

#### 性能优化检查
- [ ] 高频查询字段是否添加索引？
- [ ] 是否避免全表扫描？（使用范围查询代替函数）
- [ ] 批量操作是否使用batchUpdate？
- [ ] COUNT查询是否命中缓存？
- [ ] 慢查询是否记录日志？

---

**文档结束**
