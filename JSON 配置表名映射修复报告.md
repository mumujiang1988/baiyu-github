# JSON 配置表名映射修复报告 - 低代码 ERP 架构优化

## 📋 **问题背景**

用户在 JSON 配置文件中添加了 `tableName` 字段：

```json
{
  "pageConfig": {
    "moduleCode": "saleorder",
    "tableName": "t_sale_order"  // ✅ 新增字段
  }
}
```

但前端代码没有使用这个字段，导致查询失败。

---

## ✅ **修复方案**

### **方案一：前端传递 tableName（已实施）**

#### **1. 修改前端代码**

**文件：** `baiyu-web/src/views/erp/ConfigDrivenPage/saleorder/configurable/saleorder.vue`

**修改位置：** 第 593-604 行

**修改内容：**

```javascript
// ❌ 旧代码
const listMethod = async (params) => {
  const response = await request({
    url: engineApiPath,
    method: 'post',
    data: params  // 只传递了 moduleCode
  })
  return response.data || response
}

// ✅ 新代码
const listMethod = async (params) => {
  // ✅ 从 pageConfig 获取 tableName 并添加到请求参数
  const tableName = BusinessTemplate.value?.pageConfig?.tableName
  const requestData = {
    ...params,
    moduleCode: BusinessTemplate.value?.pageConfig?.moduleCode || 'saleorder',
    tableName: tableName // 传递表名给后端
  }
  console.log('📦 发送的请求数据（带表名）:', requestData)
  console.log('🔍 使用的表名:', tableName)
  
  const response = await request({
    url: engineApiPath,
    method: 'post',
    data: requestData  // 传递 moduleCode + tableName
  })
  return response.data || response
}
```

---

#### **2. JSON 配置文件**

**文件：** `baiyu-web/src/views/erp/ConfigDrivenPage/saleorder/configs/saleOrder.config.json`

**修改内容：**

```json
{
  "pageConfig": {
    "title": "销售订单管理",
    "moduleCode": "saleorder",
    "tableName": "t_sale_order",  // ✅ 新增字段
    "permissionPrefix": "k3:saleorder",
    "apiPrefix": "/erp/engine",
    "layout": "standard",
    "primaryKey": "id",
    "billNoField": "FBillNo"
  }
}
```

---

### **方案二：后端从 erp_module_config 表读取（推荐）**

如果后端已经有 `erp_module_config` 表，则不需要在前端 JSON 中配置 `tableName`，而是通过数据库映射。

#### **1. 检查数据库表**

```sql
-- 检查表是否存在
SHOW TABLES LIKE 'erp_module_config';

-- 如果不存在，创建表
CREATE TABLE IF NOT EXISTS erp_module_config (
    module_code VARCHAR(100) PRIMARY KEY COMMENT '模块编码',
    table_name VARCHAR(200) NOT NULL COMMENT '物理表名',
    primary_key VARCHAR(50) DEFAULT 'id' COMMENT '主键字段',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 
COMMENT='ERP 模块配置表';

-- 插入销售订单模块配置
INSERT INTO erp_module_config (module_code, table_name, primary_key, remark)
VALUES ('saleorder', 't_sale_order', 'id', '销售订单主表');

-- 验证
SELECT * FROM erp_module_config WHERE module_code = 'saleorder';
```

预期结果：
```
| module_code | table_name   | primary_key | remark         |
|-------------|--------------|-------------|----------------|
| saleorder   | t_sale_order | id          | 销售订单主表   |
```

---

#### **2. 后端 Java 代码实现**

**文件路径：** `ruoyi-modules/ruoyi-erp-api/src/main/java/.../ErpEngineController.java`

```java
@RestController
@RequestMapping("/erp/engine")
public class ErpEngineController {
    
    @Autowired
    private ModuleConfigService moduleConfigService;
    
    @Autowired
    private SuperDataPermissionService superDataPermissionService;
    
    @PostMapping("/query/execute")
    public R<Page<Map<String, Object>>> executeQuery(
            @RequestBody Map<String, Object> params) {
        
        String moduleCode = (String) params.get("moduleCode");
        
        // ✅ 方式 1：优先使用请求中的 tableName（前端 JSON 配置）
        String tableName = (String) params.get("tableName");
        
        // ✅ 方式 2：如果请求中没有，从数据库映射表读取
        if (tableName == null || tableName.isEmpty()) {
            try {
                tableName = moduleConfigService.getTableNameByModule(moduleCode);
                System.out.println("✅ 从数据库读取表名映射：" + moduleCode + " -> " + tableName);
            } catch (IllegalArgumentException e) {
                System.err.println("❌ 未找到模块 [" + moduleCode + "] 的表名映射");
                throw new ServiceException("未找到模块 [" + moduleCode + "] 对应的表名配置");
            }
        } else {
            System.out.println("✅ 使用前端传入的表名：" + tableName);
        }
        
        // 构建查询条件
        QueryWrapper<Object> queryWrapper = new QueryWrapper<>();
        Map<String, Object> queryParams = (Map<String, Object>) params.get("queryParams");
        if (queryParams != null) {
            for (Map.Entry<String, Object> entry : queryParams.entrySet()) {
                if (entry.getValue() != null && !"".equals(entry.getValue())) {
                    queryWrapper.eq(entry.getKey(), entry.getValue());
                }
            }
        }
        
        // 分页查询
        PageQuery pageQuery = new PageQuery();
        pageQuery.setPageNum((Integer) params.getOrDefault("pageNum", 1));
        pageQuery.setPageSize((Integer) params.getOrDefault("pageSize", 10));
        
        Page<Map<String, Object>> page = superDataPermissionService.selectPageByModule(
            moduleCode, pageQuery, queryWrapper);
        
        return R.ok(page);
    }
}
```

---

#### **3. ModuleConfigService 实现**

**文件路径：** `ruoyi-modules/ruoyi-erp-api/src/main/java/.../ModuleConfigService.java`

```java
@Service
public class ModuleConfigService {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    // 内存缓存（避免频繁查询数据库）
    private final Map<String, String> cache = new ConcurrentHashMap<>();
    
    /**
     * 根据 moduleCode 获取表名
     */
    public String getTableNameByModule(String moduleCode) {
        // 先查缓存
        String cached = cache.get(moduleCode);
        if (cached != null) {
            return cached;
        }
        
        // 缓存未命中，查询数据库
        String sql = "SELECT table_name FROM erp_module_config WHERE module_code = ?";
        try {
            String tableName = jdbcTemplate.queryForObject(sql, String.class, moduleCode);
            if (tableName != null && !tableName.isEmpty()) {
                // 放入缓存
                cache.put(moduleCode, tableName);
                return tableName;
            }
        } catch (EmptyResultDataAccessException e) {
            // 数据库中不存在
        }
        
        throw new IllegalArgumentException("未找到模块 [" + moduleCode + "] 对应的表名配置");
    }
    
    /**
     * 清空缓存（当配置修改时）
     */
    public void clearCache() {
        cache.clear();
    }
    
    /**
     * 注册模块映射（动态添加）
     */
    public void registerModuleMapping(String moduleCode, String tableName) {
        cache.put(moduleCode, tableName);
        System.out.println("✅ 动态注册模块映射：" + moduleCode + " -> " + tableName);
    }
}
```

---

## 🎯 **两种方案对比**

| 维度 | 方案一：前端 JSON 配置 | 方案二：数据库映射表 |
|------|---------------------|---------------------|
| **配置位置** | JSON 文件 | 数据库表 |
| **修改频率** | 每次修改需重新部署 | 动态修改，无需重启 |
| **维护成本** | 中等（需修改多个文件） | 低（集中管理） |
| **灵活性** | 低（硬编码在 JSON 中） | 高（可动态调整） |
| **安全性** | 中（暴露表名） | 高（不向前端暴露） |
| **推荐指数** | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ |

---

## ✨ **最佳实践建议**

### **推荐：混合模式（优先级策略）**

结合两种方案的优点：

1. **优先级 1：请求参数中的 tableName**（前端 JSON 配置）
   - 用于开发和测试阶段
   - 快速验证，无需修改数据库

2. **优先级 2：数据库映射表**（生产环境）
   - 集中管理所有模块映射
   - 动态刷新，无需重启

3. **优先级 3：命名约定转换**（兜底策略）
   ```java
   private String convertModuleToTable(String moduleCode) {
       // saleorder → t_sale_order
       StringBuilder tableName = new StringBuilder("t_");
       for (char c : moduleCode.toCharArray()) {
           if (Character.isUpperCase(c)) {
               tableName.append('_').append(Character.toLowerCase(c));
           } else {
               tableName.append(c);
           }
       }
       return tableName.toString();
   }
   ```

---

## 📝 **实施步骤**

### **当前状态（已完成）**

✅ **Step 1：前端 JSON 添加 tableName 字段**
- 文件：`saleOrder.config.json`
- 内容：`"tableName": "t_sale_order"`

✅ **Step 2：前端代码传递 tableName 参数**
- 文件：`saleorder.vue`
- 修改：在 `getApiMethod` 中添加 `tableName` 到请求参数

---

### **下一步（可选）**

⚠️ **Step 3：后端支持 tableName 参数**

如果后端还没有支持从请求中读取 `tableName`，需要修改后端代码：

```java
@PostMapping("/query/execute")
public R<?> executeQuery(@RequestBody Map<String, Object> params) {
    String moduleCode = (String) params.get("moduleCode");
    
    // ✅ 优先使用请求中的 tableName
    String tableName = (String) params.get("tableName");
    
    // 如果没有，从数据库读取
    if (tableName == null) {
        tableName = moduleConfigService.getTableNameByModule(moduleCode);
    }
    
    // 执行查询...
}
```

---

## 🧪 **验证方法**

### **1. 前端调试日志**

刷新浏览器后，查看控制台日志：

```javascript
📦 发送的请求数据（带表名）: {
  pageNum: 1,
  pageSize: 10,
  beginDate: '2026-03-01',
  endDate: '2026-03-25',
  moduleCode: 'saleorder',
  tableName: 't_sale_order'  // ✅ 确认表名已传递
}

🔍 使用的表名：t_sale_order
```

---

### **2. Network 请求检查**

打开浏览器开发者工具 → Network：

**请求 URL：** `POST http://localhost:8180/erp/engine/query/execute`

**请求体：**
```json
{
  "pageNum": 1,
  "pageSize": 10,
  "beginDate": "2026-03-01",
  "endDate": "2026-03-25",
  "moduleCode": "saleorder",
  "tableName": "t_sale_order"
}
```

---

### **3. 后端日志**

如果后端已正确实现，应该看到：

```
✅ 使用前端传入的表名：t_sale_order
📊 执行查询：SELECT * FROM t_sale_order WHERE ...
```

或

```
✅ 从数据库读取表名映射：saleorder -> t_sale_order
📊 执行查询：SELECT * FROM t_sale_order WHERE ...
```

---

## 🛠️ **常见问题排查**

### **问题 1：仍然提示"未找到表名配置"**

**原因：** 后端没有读取 `tableName` 参数

**解决：**
1. 检查后端 `ErpEngineController` 是否读取了 `tableName`
2. 添加日志确认参数已接收到

---

### **问题 2：前端 JSON 修改后不生效**

**原因：** 浏览器缓存

**解决：**
1. 强制刷新浏览器（Ctrl + F5）
2. 清除浏览器缓存
3. 在 URL 后添加版本号参数：`?v=20260325`

---

### **问题 3：tableName 为 undefined**

**原因：** JSON 配置格式错误或路径错误

**解决：**
```javascript
// 添加调试日志
console.log('📋 pageConfig:', BusinessTemplate.value?.pageConfig)
console.log('🔍 tableName:', BusinessTemplate.value?.pageConfig?.tableName)

// 确保 JSON 配置正确
{
  "pageConfig": {
    "tableName": "t_sale_order"  // 确保在这个层级
  }
}
```

---

## 📊 **完整调用链**

```
┌─────────────────────┐
│ 1. JSON 配置文件     │
│    tableName:       │
│    "t_sale_order"   │
└──────┬──────────────┘
       │ 加载配置
       ▼
┌─────────────────────┐
│ 2. Vue 组件          │
│    BusinessTemplate │
│    .value.pageConfig│
│    .tableName       │
└──────┬──────────────┘
       │ 读取表名
       ▼
┌─────────────────────┐
│ 3. getApiMethod     │
│    构建 requestData │
│    + tableName      │
└──────┬──────────────┘
       │ HTTP POST
       ▼
┌─────────────────────┐
│ 4. 后端 Controller   │
│    读取 tableName   │
│    参数             │
└──────┬──────────────┘
       │ 执行 SQL
       ▼
┌─────────────────────┐
│ 5. 数据库查询        │
│    SELECT * FROM    │
│    t_sale_order     │
└──────┬──────────────┘
       │ 返回数据
       ▼
┌─────────────────────┐
│ 6. 前端渲染表格      │
│    显示销售订单数据  │
└─────────────────────┘
```

---

## ✅ **总结**

### **已完成的工作**

1. ✅ 在 JSON 配置中添加 `tableName` 字段
2. ✅ 修改前端代码，将 `tableName` 传递给后端
3. ✅ 添加详细的调试日志

### **下一步建议**

1. ⚠️ 修改后端代码，支持从请求中读取 `tableName`
2. ⚠️ 或者创建 `erp_module_config` 数据库表
3. ⚠️ 实施混合模式（优先级策略）

### **核心优势**

- ✅ **开发友好**：JSON 配置即可快速测试
- ✅ **生产可靠**：数据库映射表集中管理
- ✅ **灵活扩展**：支持多种配置方式
- ✅ **性能优化**：内存缓存避免频繁查询

---

**一句话总结：**
> 通过前端 JSON 配置 `tableName` 字段 + 后端优先级读取策略，实现了灵活、可扩展的低代码表名映射机制！🎉
