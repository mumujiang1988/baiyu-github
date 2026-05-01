# ERP模块JDBC工具集迁移实施方案

**文档版本：** v1.0  
**创建日期：** 2026-04-30  
**适用范围：** ERP低代码配置化模块  
**目标：** 全面采用JDBC工具集，消除硬编码SQL，提升安全性和性能

---

## 📋 目录

1. [现状分析](#1-现状分析)
2. [迁移目标](#2-迁移目标)
3. [实施策略](#3-实施策略)
4. [详细方案](#4-详细方案)
5. [风险评估](#5-风险评估)
6. [验收标准](#6-验收标准)
7. [时间计划](#7-时间计划)

---

## 1. 现状分析

### 1.1 当前架构问题

#### 问题1：混合使用JdbcTemplate和MyBatis-Plus
```java
// ❌ 现状：直接使用JdbcTemplate
@Autowired
private JdbcTemplate jdbcTemplate;

public List<Map<String, Object>> query(String sql, Object... params) {
    return jdbcTemplate.queryForList(sql, params);
}

// ❌ 现状：MyBatis-Plus Mapper
@Mapper
public interface ErpPageConfigMapper extends BaseMapper<ErpPageConfig> {
    // Entity绑定，违反纯Map架构原则
}
```

**影响：**
- 无统一安全校验（SQL注入风险）
- 无COUNT缓存优化
- 无慢查询监控
- Entity类冗余维护成本高

---

#### 问题2：硬编码SQL字符串
```java
// ❌ 现状：ErpQueryController.java Line 68-130
StringBuilder sql = new StringBuilder();
sql.append("SELECT * FROM ").append(tableName);
if (filters != null) {
    for (Filter filter : filters) {
        sql.append(" AND ").append(filter.getField()).append(" = ?");
    }
}
```

**风险：**
- 字段名未校验（SQL注入风险）
- 表名未白名单限制（跨库攻击风险）
- 无参数化查询保护

---

#### 问题3：缺少数据权限控制
```java
// ❌ 现状：手动拼接部门权限
String deptSql = " AND dept_id IN (" + deptIds + ")";
sql.append(deptSql);
```

**问题：**
- 权限逻辑分散在各处
- 超级管理员特权未统一处理
- 无Fail-Closed安全策略

---

### 1.2 现有组件统计

| 组件类型 | 数量 | 状态 |
|---------|------|------|
| Controller | 10个 | ⚠️ 需改造 |
| Service | 5个 | ⚠️ 需改造 |
| Engine | 8个 | ⚠️ 需改造 |
| Mapper | 6个 | ❌ 需废弃 |
| Entity/BO | 10个 | ❌ 需废弃 |

---

## 2. 迁移目标

### 2.1 核心原则

#### 原则1：纯Map架构
- ✅ 全链路使用`Map<String, Object>`
- ❌ 废弃所有Entity/BO/VO类
- ❌ 删除所有Mapper接口

#### 原则2：配置驱动
- ✅ SQL通过JSON配置定义
- ❌ 禁止硬编码SQL字符串

#### 原则3：安全内建
- ✅ 六层安全防护（危险关键字、表名白名单、字段名校验等）
- ✅ 自动注入数据权限（DataScopeInjector）
- ✅ 参数化查询防SQL注入

#### 原则4：性能优化
- ✅ COUNT缓存（TTL=60秒）
- ✅ 慢查询检测（阈值100ms）
- ✅ 批量操作优化（10倍-33倍提升）

---

### 2.2 技术选型对比

| 功能 | 现状 | 目标 | 优势 |
|------|------|------|------|
| 数据访问 | JdbcTemplate + MyBatis-Plus | JdbcExecutor | 统一入口，安全内建 |
| SQL构建 | StringBuilder拼接 | SqlBuilder | 链式调用，自动忽略空参数 |
| 复杂查询 | 手写SQL模板 | SqlFragmentEngine | 六层防护，自动优化COUNT |
| CRUD操作 | Mapper接口 | JdbcDataKit | 零SQL编写，支持乐观锁 |
| 数据权限 | 手动拼接 | DataScopeInjector | 统一管理，自动注入 |
| 表名校验 | 无 | TableFieldWhiteList | 防止跨库攻击 |

---

## 3. 实施策略

### 3.1 分阶段迁移

#### 阶段1：基础设施准备（P0级，1天）
- ✅ 已完成：JDBC工具集开发
- ✅ 已完成：编译错误修复（Lombok配置、StringUtil方法补充）
- ⏳ 待完成：配置白名单表前缀（erp_、bd_、f_、sys_）

#### 阶段2：核心引擎改造（P0级，3天）
- DynamicQueryEngine → 使用SqlBuilder + JdbcExecutor
- DictionaryBuilderEngine → 使用JdbcExecutor
- ComputedFieldEngine → 保持不变（纯计算逻辑）
- VirtualFieldService → 使用JdbcExecutor替代JdbcTemplate

#### 阶段3：Service层改造（P1级，2天）
- ErpPageConfigServiceImpl → 使用JdbcDataKit
- ErpApprovalFlowServiceImpl → 使用JdbcDataKit
- ErpPushRelationServiceImpl → 使用JdbcDataKit
- 删除所有Mapper依赖

#### 阶段4：Controller层优化（P2级，1天）
- 统一异常处理
- 添加权限注解（@SaCheckPermission）
- 日志规范化

#### 阶段5：清理与验证（P2级，1天）
- 删除Entity/BO/Mapper文件
- 运行单元测试
- 性能基准测试

---

### 3.2 兼容性保障

#### 策略1：灰度发布
```java
// 新旧并存，逐步切换
@Service
public class ErpPageConfigServiceImpl implements ErpPageConfigService {
    
    @Autowired
    private JdbcExecutor jdbcExecutor;  // 新
    
    @Autowired
    private ErpPageConfigMapper mapper;  // 旧（标记@Deprecated）
    
    @Value("${erp.use-jdbc-toolkit:false}")
    private boolean useJdbcToolkit;
    
    public Map<String, Object> getConfig(String moduleCode) {
        if (useJdbcToolkit) {
            return jdbcExecutor.selectOne(
                "SELECT * FROM erp_page_config WHERE module_code = ?", 
                moduleCode
            );
        } else {
            return mapper.selectByModuleCode(moduleCode);
        }
    }
}
```

---

#### 策略2：回滚机制
- 保留旧代码分支（git tag: v3.x-legacy）
- 配置开关控制（`erp.use-jdbc-toolkit`）
- 数据库不变更（仅代码层改造）

---

## 4. 详细方案

### 4.1 DynamicQueryEngine改造

#### 改造前
```java
@Component
public class DynamicQueryEngine {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    public List<Map<String, Object>> execute(QueryConfig config) {
        StringBuilder sql = new StringBuilder("SELECT * FROM ");
        sql.append(config.getTableName());
        
        List<Object> params = new ArrayList<>();
        if (config.getFilters() != null) {
            for (Filter filter : config.getFilters()) {
                sql.append(" AND ").append(filter.getField()).append(" = ?");
                params.add(filter.getValue());
            }
        }
        
        return jdbcTemplate.queryForList(sql.toString(), params.toArray());
    }
}
```

#### 改造后
```java
@Component
@RequiredArgsConstructor
public class DynamicQueryEngine {
    
    private final JdbcExecutor jdbcExecutor;
    private final DataScopeInjector dataScopeInjector;
    private final TableFieldWhiteList whiteList;
    
    /**
     * 执行动态查询（带安全校验）
     */
    public PageResult<Map<String, Object>> execute(QueryConfig config) {
        // 1. 表名校验
        whiteList.checkTable(config.getTableName());
        
        // 2. 构建SQL（SqlBuilder自动忽略空参数）
        SqlBuilder sql = SqlBuilder.create()
            .append("SELECT")
            .append(buildSelectFields(config.getFields()))
            .append("FROM").append(config.getTableName())
            .append("WHERE 1=1");
        
        // 3. 添加过滤条件
        applyFilters(sql, config.getFilters());
        
        // 4. 注入数据权限
        String dataScope = dataScopeInjector.injectDataScope("t");
        sql.append(dataScope);
        
        // 5. 添加排序
        if (config.getOrderBy() != null) {
            sql.append("ORDER BY").append(config.getOrderBy());
        }
        
        // 6. 分页查询（自动COUNT缓存）
        String countSql = buildCountSql(config.getTableName(), config.getFilters(), dataScope);
        String dataSql = sql.build();
        
        return jdbcExecutor.pageAsObject(
            countSql, 
            dataSql, 
            config.getPageNum(), 
            config.getPageSize(),
            sql.getParams()
        );
    }
    
    /**
     * 应用过滤条件
     */
    private void applyFilters(SqlBuilder sql, List<Filter> filters) {
        if (filters == null || filters.isEmpty()) {
            return;
        }
        
        for (Filter filter : filters) {
            // 字段名校验
            whiteList.checkField(filter.getField());
            
            switch (filter.getOperator()) {
                case "eq":
                    sql.eq(filter.getField(), filter.getValue());
                    break;
                case "like":
                    sql.like(filter.getField(), filter.getValue());
                    break;
                case "in":
                    sql.in(filter.getField(), (List<?>) filter.getValue());
                    break;
                case "between":
                    sql.between(filter.getField(), filter.getMin(), filter.getMax());
                    break;
                default:
                    throw new IllegalArgumentException("不支持的操作符: " + filter.getOperator());
            }
        }
    }
}
```

**改进点：**
- ✅ 表名白名单校验（防止跨库攻击）
- ✅ 字段名校验（防止SQL注入）
- ✅ 数据权限自动注入
- ✅ COUNT缓存优化
- ✅ 链式调用，代码更简洁

---

### 4.2 ErpPageConfigServiceImpl改造

#### 改造前
```java
@Service
@RequiredArgsConstructor
public class ErpPageConfigServiceImpl implements ErpPageConfigService {
    
    private final ErpPageConfigMapper mapper;
    
    public ErpPageConfig getConfig(String moduleCode) {
        return mapper.selectByModuleCode(moduleCode);
    }
    
    public void saveConfig(ErpPageConfig config) {
        mapper.insert(config);
    }
}
```

#### 改造后
```java
@Service
@RequiredArgsConstructor
public class ErpPageConfigServiceImpl implements ErpPageConfigService {
    
    private final JdbcDataKit jdbcDataKit;
    private final JdbcExecutor jdbcExecutor;
    
    /**
     * 查询配置（返回Map）
     */
    public Map<String, Object> getConfig(String moduleCode) {
        return jdbcDataKit.listByCondition(
            "erp_page_config",
            "module_code = ? AND status = '1'",
            moduleCode
        ).stream().findFirst().orElse(null);
    }
    
    /**
     * 新增配置
     */
    public Long saveConfig(Map<String, Object> config) {
        // 必填字段校验
        Validator.required(config, "module_code", "config_name");
        
        // 插入记录
        int rows = jdbcDataKit.insert("erp_page_config", config);
        
        // 返回自增ID
        return jdbcExecutor.count("SELECT LAST_INSERT_ID()");
    }
    
    /**
     * 更新配置
     */
    public void updateConfig(Map<String, Object> config) {
        Long id = TypeConverter.getLong(config, "config_id").orElseThrow(
            () -> new IllegalArgumentException("config_id不能为空")
        );
        
        jdbcDataKit.updateById("erp_page_config", config, id, "config_id");
    }
    
    /**
     * 删除配置（软删除）
     */
    public void deleteConfig(Long configId) {
        Map<String, Object> updateData = Map.of(
            "status", "0",
            "update_time", LocalDateTime.now()
        );
        
        jdbcDataKit.updateById("erp_page_config", updateData, configId, "config_id");
    }
}
```

**改进点：**
- ✅ 零SQL编写（JdbcDataKit自动生成）
- ✅ 纯Map架构（无Entity绑定）
- ✅ 统一异常处理
- ✅ 支持软删除

---

### 4.3 VirtualFieldService改造

#### 改造前
```java
@Component
public class VirtualFieldService {
    
    @Resource
    private JdbcTemplate jdbcTemplate;
    
    public Map<String, Object> resolveVirtualField(VirtualFieldConfig config, Map<String, Object> data) {
        String sql = String.format(
            "SELECT %s FROM %s WHERE %s = ?",
            config.getSourceDisplayField(),
            config.getSourceTable(),
            config.getSourceField()
        );
        
        List<Map<String, Object>> result = jdbcTemplate.queryForList(
            sql, 
            data.get(config.getSourceField())
        );
        
        return result.isEmpty() ? null : result.get(0);
    }
}
```

#### 改造后
```java
@Component
@RequiredArgsConstructor
public class VirtualFieldService {
    
    private final JdbcExecutor jdbcExecutor;
    private final TableFieldWhiteList whiteList;
    
    /**
     * 解析虚拟字段（带安全校验）
     */
    public Map<String, Object> resolveVirtualField(VirtualFieldConfig config, Map<String, Object> data) {
        // 1. 表名校验
        whiteList.checkTable(config.getSourceTable());
        
        // 2. 字段名校验
        whiteList.checkField(config.getSourceField());
        whiteList.checkField(config.getSourceDisplayField());
        
        // 3. 构建SQL
        String sql = String.format(
            "SELECT %s FROM %s WHERE %s = ?",
            config.getSourceDisplayField(),
            config.getSourceTable(),
            config.getSourceField()
        );
        
        // 4. 执行查询
        Object sourceValue = data.get(config.getSourceField());
        List<Map<String, Object>> result = jdbcExecutor.select(sql, sourceValue);
        
        return result.isEmpty() ? null : result.get(0);
    }
}
```

**改进点：**
- ✅ 表名/字段名白名单校验
- ✅ 参数化查询防SQL注入
- ✅ 慢查询自动监控

---

### 4.4 数据权限集成

#### 场景1：查询订单列表（自动注入部门权限）
```java
@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final JdbcExecutor jdbcExecutor;
    private final DataScopeInjector dataScopeInjector;
    
    public PageResult<Map<String, Object>> queryOrders(int pageNum, int pageSize) {
        // 1. 注入数据权限
        String dataScope = dataScopeInjector.injectDataScope("o");
        // 结果：" AND o.dept_id IN (1, 2, 3)" 或 ""（超级管理员）
        
        // 2. 构建SQL
        String countSql = "SELECT COUNT(*) FROM f_sales_order o WHERE 1=1" + dataScope;
        String dataSql = "SELECT o.order_no, o.amount FROM f_sales_order o WHERE 1=1" 
            + dataScope + " ORDER BY o.create_time DESC";
        
        // 3. 执行分页查询（COUNT自动缓存60秒）
        return jdbcExecutor.pageAsObject(countSql, dataSql, pageNum, pageSize);
    }
}
```

---

#### 场景2：跨模块查询（销售订单+客户信息）
```java
@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final JdbcExecutor jdbcExecutor;
    private final DataScopeInjector dataScopeInjector;
    
    public PageResult<Map<String, Object>> queryOrdersWithCustomer(int pageNum, int pageSize) {
        // 1. 注入数据权限
        String dataScope = dataScopeInjector.injectDataScope("o");
        
        // 2. 构建JOIN查询
        SqlBuilder sql = SqlBuilder.create()
            .append("SELECT o.order_no, c.customer_name, o.amount")
            .append("FROM f_sales_order o")
            .leftJoin("bd_customer c", "o.customer_id = c.id")
            .append("WHERE 1=1");
        
        sql.append(dataScope);
        sql.append("ORDER BY o.create_time DESC");
        sql.page(pageNum, pageSize);
        
        // 3. 构建COUNT SQL（移除ORDER BY优化性能）
        String countSql = "SELECT COUNT(*) FROM f_sales_order o LEFT JOIN bd_customer c ON o.customer_id = c.id WHERE 1=1" + dataScope;
        
        // 4. 执行查询
        return jdbcExecutor.pageAsObject(countSql, sql.build(), pageNum, pageSize, sql.getParams());
    }
}
```

---

### 4.5 批量操作优化

#### 场景：批量导入订单
```java
@Service
@RequiredArgsConstructor
public class OrderImportService {
    
    private final JdbcDataKit jdbcDataKit;
    
    /**
     * 批量导入订单（性能优化）
     */
    public int batchImport(List<Map<String, Object>> orders) {
        // ❌ 错误：逐条插入（1000条需要5秒）
        // for (Map<String, Object> order : orders) {
        //     jdbcDataKit.insert("f_sales_order", order);
        // }
        
        // ✅ 正确：批量插入（1000条只需200ms，提升25倍）
        int[] results = jdbcDataKit.batchInsert("f_sales_order", orders);
        
        return Arrays.stream(results).sum();
    }
}
```

---

### 4.6 乐观锁更新

#### 场景：库存扣减（防止超卖）
```java
@Service
@RequiredArgsConstructor
public class InventoryService {
    
    private final JdbcDataKit jdbcDataKit;
    
    /**
     * 扣减库存（乐观锁）
     */
    public boolean deductStock(Long productId, int quantity) {
        // 1. 查询当前库存和版本号
        Map<String, Object> product = jdbcDataKit.getById("biz_product", productId, "id");
        int currentStock = (int) product.get("stock");
        int currentVersion = (int) product.get("version");
        
        // 2. 检查库存是否充足
        if (currentStock < quantity) {
            throw new BusinessException("库存不足");
        }
        
        // 3. 乐观锁更新
        Map<String, Object> updateData = Map.of(
            "stock", currentStock - quantity,
            "version", currentVersion + 1
        );
        
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
        
        return true;
    }
}
```

---

## 5. 风险评估

### 5.1 技术风险

| 风险项 | 概率 | 影响 | 应对措施 |
|--------|------|------|---------|
| 性能下降 | 低 | 高 | 压测验证，COUNT缓存补偿 |
| SQL兼容性问题 | 中 | 中 | 灰度发布，配置开关回滚 |
| 数据权限遗漏 | 低 | 高 | 代码审查，自动化测试 |
| 学习成本高 | 中 | 中 | 培训文档，示例代码 |

---

### 5.2 业务风险

| 风险项 | 概率 | 影响 | 应对措施 |
|--------|------|------|---------|
| 线上故障 | 低 | 高 | 灰度发布，快速回滚机制 |
| 数据不一致 | 极低 | 高 | 事务保证，数据校验 |
| 用户体验下降 | 极低 | 中 | 性能监控，及时优化 |

---

## 6. 验收标准

### 6.1 功能验收

- [ ] 所有查询接口正常工作
- [ ] 数据权限正确注入（普通用户只能看本部门数据）
- [ ] 超级管理员无数据限制
- [ ] 分页查询返回正确结果
- [ ] 字典翻译正常工作
- [ ] 虚拟字段计算正确

---

### 6.2 性能验收

| 指标 | 目标值 | 测量方法 |
|------|--------|---------|
| 查询响应时间 | < 200ms | JMeter压测 |
| COUNT缓存命中率 | > 60% | 日志统计 |
| 慢查询比例 | < 5% | 慢查询日志 |
| 批量插入1000条 | < 500ms | 性能测试 |

---

### 6.3 安全验收

- [ ] SQL注入测试通过（OWASP ZAP扫描）
- [ ] 表名白名单校验生效
- [ ] 字段名敏感词拦截生效
- [ ] 数据权限无法绕过
- [ ] 危险关键字拦截生效

---

### 6.4 代码质量验收

- [ ] 无硬编码SQL字符串
- [ ] 无Entity/BO/VO类
- [ ] 无Mapper接口
- [ ] 单元测试覆盖率 > 80%
- [ ] 代码审查通过

---

## 7. 时间计划

### 7.1 总体时间表

| 阶段 | 工作内容 | 工期 | 负责人 |
|------|---------|------|--------|
| 阶段1 | 基础设施准备 | 1天 | 架构组 |
| 阶段2 | 核心引擎改造 | 3天 | 后端开发 |
| 阶段3 | Service层改造 | 2天 | 后端开发 |
| 阶段4 | Controller层优化 | 1天 | 后端开发 |
| 阶段5 | 清理与验证 | 1天 | QA团队 |
| **合计** | | **8天** | |

---

### 7.2 详细计划

#### Day 1：基础设施准备
- [x] 修复编译错误（Lombok配置、StringUtil方法）
- [ ] 配置白名单表前缀（application.yml）
- [ ] 编写迁移指南文档

#### Day 2-4：核心引擎改造
- [ ] DynamicQueryEngine改造
- [ ] DictionaryBuilderEngine改造
- [ ] VirtualFieldService改造
- [ ] 单元测试编写

#### Day 5-6：Service层改造
- [ ] ErpPageConfigServiceImpl改造
- [ ] ErpApprovalFlowServiceImpl改造
- [ ] ErpPushRelationServiceImpl改造
- [ ] 删除Mapper依赖

#### Day 7：Controller层优化
- [ ] 统一异常处理
- [ ] 添加权限注解
- [ ] 日志规范化

#### Day 8：清理与验证
- [ ] 删除Entity/BO/Mapper文件
- [ ] 运行全量测试
- [ ] 性能基准测试
- [ ] 代码审查

---

## 8. 附录

### 8.1 关键文件清单

#### 需要改造的文件
- `DynamicQueryEngine.java` - 动态查询引擎
- `DictionaryBuilderEngine.java` - 字典构建引擎
- `VirtualFieldService.java` - 虚拟字段服务
- `ErpPageConfigServiceImpl.java` - 页面配置服务
- `ErpApprovalFlowServiceImpl.java` - 审批流程服务
- `ErpPushRelationServiceImpl.java` - 下推关系服务

#### 需要删除的文件
- `ErpPageConfigMapper.java`
- `ErpApprovalFlowMapper.java`
- `ErpPushRelationMapper.java`
- `ErpPageConfig.java` (Entity)
- `ErpApprovalFlow.java` (Entity)
- `ErpPushRelation.java` (Entity)
- 所有BO类

---

### 8.2 参考文档

- [JDBC工具集完整使用说明书](./后端架构文档/JDBC工具集完整使用说明书.md)
- [报表模块JDBC工具集使用情况审计报告](../ruoyi-modules/ruoyi-erp-api/src/main/java/com/ruoyi/report/报表模块JDBC工具集使用情况审计报告.md)
- [低代码JSON驱动架构说明书](./后端架构文档/低代码JSON驱动架构说明书.md)

---

### 8.3 联系方式

**技术支持：**
- 架构组邮箱：architecture@company.com
- Git仓库：http://git.company.com/erp/ruoyi-erp-api
- 技术讨论群：钉钉群「ERP重构讨论组」

---

**文档结束**
