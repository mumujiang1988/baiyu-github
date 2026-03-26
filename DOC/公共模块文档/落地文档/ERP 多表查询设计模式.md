# ERP 多表查询设计模式

**版本：** v1.0  
**日期：** 2026-03-26  
**适用范围：** RuoYi-WMS ERP 配置化开发

---

## 📋 问题背景

在当前的 `table.json` 配置中，只有主表 `t_sale_order`，没有明细表 `t_sale_order_entry` 和成本表 `t_sale_order_cost`。当有多个表格查询需求时，如何体现查询的目标表？

---

## 🎯 架构设计原则

### 1. 单表定位原则
每个 `table_config` **只配置一个主表**，用于该表格的 CRUD 操作：
```json
{
  "tableName": "t_sale_order",  // ← 只配置主表
  "primaryKey": "id",
  "queryBuilder": { ... },
  "columns": [ ... ]
}
```

### 2. 多表关联方案

#### ✅ 方案一：虚拟字段 + 字典映射（推荐）

**适用场景：** 显示关联表的少量字段（如客户名称、物料名称等）

**实现方式：**
1. 在 `columns` 中定义虚拟字段
2. 使用 `dictionary` 属性关联到字典配置
3. 后端通过 `DataProcessor` 自动填充

**示例配置：**
```json
{
  "tableName": "t_sale_order",
  "columns": [
    {
      "prop": "FBillNo",
      "label": "单据编号",
      "width": 150,
      "renderType": "text"
    },
    {
      "prop": "F_ora_BaseProperty",
      "label": "客户简称",
      "width": 150,
      "renderType": "text",
      "dictionary": "customers"  // ← 通过字典关联 bd_customer 表
    },
    {
      "prop": "FSalerId",
      "label": "销售员",
      "width": 120,
      "renderType": "text",
      "dictionary": "salespersons"  // ← 通过字典关联 sys_user 表
    }
  ]
}
```

**字典配置（dict.json）：**
```json
{
  "dicts": [
    {
      "dictKey": "customers",
      "dictType": "dynamic",
      "table": "bd_customer",  // ← 关联表
      "conditions": [
        {"field": "deleted", "operator": "isNull"}
      ],
      "orderBy": [
        {"field": "fname", "direction": "ASC"}
      ],
      "fieldMapping": {
        "valueField": "fnumber",
        "labelField": "fname"
      }
    }
  ]
}
```

**优点：**
- ✅ 配置简单，无需编写 SQL
- ✅ 自动缓存，性能优秀
- ✅ 支持多级关联

**缺点：**
- ❌ 只能获取单个字段值
- ❌ 无法进行复杂关联查询

---

#### ✅ 方案二：数据库视图（推荐用于复杂报表）

**适用场景：** 需要频繁查询多表关联数据

**步骤 1：创建数据库视图**
```sql
CREATE OR REPLACE VIEW v_sale_order_full AS
SELECT 
    so.id,
    so.FBillNo,
    so.FDate,
    so.FSalerId,
    so.FBillAmount,
    -- 客户信息
    c.fname AS customerName,
    c.fnumber AS customerCode,
    -- 明细汇总
    SUM(soe.FQty) AS totalQty,
    SUM(soe.FAmount) AS totalEntryAmount,
    -- 成本信息
    SUM(sc.FCost) AS totalCost
FROM t_sale_order so
LEFT JOIN bd_customer c ON so.FCustId = c.fnumber
LEFT JOIN t_sale_order_entry soe ON so.id = soe.FBillNo
LEFT JOIN t_sale_order_cost sc ON so.id = sc.FBillNo
WHERE so.deleted = 0
GROUP BY so.id;
```

**步骤 2：配置 table.json**
```json
{
  "tableName": "v_sale_order_full",  // ← 使用视图作为表名
  "primaryKey": "id",
  "queryBuilder": {
    "fields": [
      {
        "field": "customerName",
        "label": "客户名称",
        "component": "input",
        "op": "like"
      },
      {
        "field": "totalQty",
        "label": "总数量",
        "component": "input-number",
        "op": "gt"
      }
    ]
  },
  "columns": [
    {
      "prop": "FBillNo",
      "label": "单据编号"
    },
    {
      "prop": "customerName",
      "label": "客户名称"  // ← 视图中的字段
    },
    {
      "prop": "totalQty",
      "label": "总数量"  // ← 聚合字段
    },
    {
      "prop": "totalCost",
      "label": "总成本"  // ← 来自成本表
    }
  ]
}
```

**优点：**
- ✅ 支持任意复杂的多表关联
- ✅ 查询性能好（预定义 JOIN）
- ✅ 配置简单，与单表一致

**缺点：**
- ❌ 需要 DBA 权限创建视图
- ❌ 视图结构变更需同步更新配置

---

#### ✅ 方案三：自定义 API（最灵活）

**适用场景：** 超复杂业务逻辑、跨系统查询

**步骤 1：创建自定义 Controller**
```java
@RestController
@RequestMapping("/erp/custom/saleorder")
public class SaleOrderCustomController {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    /**
     * 销售订单完整查询（含明细和成本）
     */
    @PostMapping("/query/full")
    public R<?> queryFullOrder(@RequestBody Map<String, Object> params) {
        try {
            String sql = """
                SELECT 
                    so.*,
                    c.fname AS customerName,
                    GROUP_CONCAT(soe.FMaterialId) AS materialIds,
                    SUM(sc.FCost) AS totalCost
                FROM t_sale_order so
                LEFT JOIN bd_customer c ON so.FCustId = c.fnumber
                LEFT JOIN t_sale_order_entry soe ON so.id = soe.FBillNo
                LEFT JOIN t_sale_order_cost sc ON so.id = sc.FBillNo
                WHERE so.deleted = 0
                GROUP BY so.id
                ORDER BY so.FCreateDate DESC
            """;
            
            List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
            return R.ok(result);
        } catch (Exception e) {
            log.error("查询失败", e);
            return R.fail("查询失败：" + e.getMessage());
        }
    }
}
```

**步骤 2：前端调用自定义接口**
```javascript
// BusinessConfigurable.vue
const getList = async () => {
  const response = await request({
    url: '/erp/custom/saleorder/query/full',
    method: 'post',
    data: searchParams
  });
  
  tableData.value = response.data;
};
```

**优点：**
- ✅ 完全灵活，可实现任意逻辑
- ✅ 支持跨系统、API 调用
- ✅ 可处理复杂业务计算

**缺点：**
- ❌ 需要编写 Java 代码
- ❌ 维护成本高
- ❌ 不符合配置化理念

---

#### ✅ 方案四：多表格配置（分离查询）

**适用场景：** 同一页面需要展示多个独立表格

**实现方式：**
为每个表创建独立的配置：

**配置 1：销售订单主表（table.json）**
```json
{
  "tableName": "t_sale_order",
  "pageId": "saleorder-main"
}
```

**配置 2：销售订单明细表（新建 table-entry.json）**
```json
{
  "tableName": "t_sale_order_entry",
  "pageId": "saleorder-entry",
  "queryBuilder": {
    "fields": [
      {
        "field": "FBillNo",
        "label": "单据编号",
        "component": "input",
        "op": "eq"
      }
    ]
  },
  "columns": [
    {
      "prop": "FMaterialId",
      "label": "物料编码"
    },
    {
      "prop": "FQty",
      "label": "数量"
    },
    {
      "prop": "FAmount",
      "label": "金额"
    }
  ]
}
```

**配置 3：销售订单成本表（新建 table-cost.json）**
```json
{
  "tableName": "t_sale_order_cost",
  "pageId": "saleorder-cost"
}
```

**前端使用：**
```vue
<template>
  <div>
    <!-- 主表格 -->
    <BusinessConfigurable moduleCode="saleorder-main" />
    
    <!-- 明细表格 -->
    <BusinessConfigurable moduleCode="saleorder-entry" />
    
    <!-- 成本表格 -->
    <BusinessConfigurable moduleCode="saleorder-cost" />
  </div>
</template>
```

**优点：**
- ✅ 职责清晰，每个表独立维护
- ✅ 符合配置化规范
- ✅ 易于调试和维护

**缺点：**
- ❌ 需要多次数据库查询
- ❌ 数据一致性需前端保证

---

#### ✅ 方案五：单页多 Tab 签（⭐⭐⭐⭐⭐ 强烈推荐用于页签切换）

**适用场景：** 一个页面有多个 Tab 页签，每个页签显示不同表格的数据

**核心思路：**
- 每个 Tab 对应一个独立的 `moduleCode` 配置
- 通过 URL 参数或路由传递当前激活的 Tab
- 使用 `el-tabs` 组件实现页签切换

---

##### 📌 实现步骤

**步骤 1：创建多个独立配置**

为每个 Tab 创建独立的 ERP 配置：

**配置 1：销售订单主表（module_code = 'saleorder-main'）**
```sql
INSERT INTO erp_page_config (
    module_code, config_name, config_type,
    page_config, form_config, table_config,
    dict_config, business_config
) VALUES (
    'saleorder-main',
    '销售订单主表',
    'PAGE',
    -- page_config
    '{
      "pageId": "saleorder-main",
      "pageName": "销售订单主表",
      "permission": "k3:saleorder:main:query",
      "layout": "standard",
      "apiPrefix": "/erp/engine",
      "tableName": "t_sale_order"
    }',
    -- form_config / table_config / dict_config / business_config ...
);```

**配置 2：销售订单明细表（module_code = 'saleorder-entry'）**
```sql
INSERT INTO erp_page_config (...) VALUES (
    'saleorder-entry',
    '销售订单明细表',
    'PAGE',
    '{
      "pageId": "saleorder-entry",
      "pageName": "销售订单明细表",
      "permission": "k3:saleorder:entry:query",
      "layout": "standard",
      "apiPrefix": "/erp/engine",
      "tableName": "t_sale_order_entry"
    }',
    -- ... 其他配置
);
```

**配置 3：销售订单成本表（module_code = 'saleorder-cost'）**
```sql
INSERT INTO erp_page_config (...) VALUES (
    'saleorder-cost',
    '销售订单成本表',
    'PAGE',
    '{
      "pageId": "saleorder-cost",
      "pageName": "销售订单成本表",
      "permission": "k3:saleorder:cost:query",
      "layout": "standard",
      "apiPrefix": "/erp/engine",
      "tableName": "t_sale_order_cost"
    }',
    -- ... 其他配置
);
```

---

**步骤 2：前端实现 Tab 切换**

**方式 A：使用 URL 参数控制 Tab**

```vue
<!-- MultiTabPage.vue -->
<template>
  <div class="multi-tab-container">
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <el-tab-pane label="销售订单主表" name="saleorder-main"></el-tab-pane>
      <el-tab-pane label="销售订单明细" name="saleorder-entry"></el-tab-pane>
      <el-tab-pane label="销售订单成本" name="saleorder-cost"></el-tab-pane>
    </el-tabs>
    
    <!-- 动态加载 BusinessConfigurable -->
    <div v-if="currentModuleCode">
      <BusinessConfigurable 
        :key="currentModuleCode" 
        :moduleCode="currentModuleCode" 
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BusinessConfigurable from '@/views/erp/pageTemplate/configurable/BusinessConfigurable.vue'

const route = useRoute()
const router = useRouter()

// 当前激活的 Tab
const activeTab = ref('saleorder-main')

// 当前模块编码（从 URL 参数读取）
const currentModuleCode = computed(() => {
  return route.query.tab || 'saleorder-main'
})

// Tab 切换处理
const handleTabChange = (tabName) => {
  // 更新 URL 参数
  router.push({
    query: {
      ...route.query,
      tab: tabName
    }
  })
}

// 初始化：从 URL 读取 Tab
onMounted(() => {
  if (route.query.tab) {
    activeTab.value = route.query.tab
  }
})
</script>

<style scoped>
.multi-tab-container {
  padding: 20px;
}
</style>
```

**方式 B：路由配置方式（推荐）**

```javascript
// router/index.js
{
  path: '/business/saleorder-tabs',
  component: () => import('@/views/erp/MultiTabPage.vue'),
  name: 'SaleOrderTabs',
  meta: { title: '销售订单多表查询' },
  children: [
    {
      path: 'main',
      component: () => import('@/views/erp/pageTemplate/configurable/BusinessConfigurable.vue'),
      props: () => ({ moduleCode: 'saleorder-main' })
    },
    {
      path: 'entry',
      component: () => import('@/views/erp/pageTemplate/configurable/BusinessConfigurable.vue'),
      props: () => ({ moduleCode: 'saleorder-entry' })
    },
    {
      path: 'cost',
      component: () => import('@/views/erp/pageTemplate/configurable/BusinessConfigurable.vue'),
      props: () => ({ moduleCode: 'saleorder-cost' })
    }
  ]
}
```

```vue
<!-- MultiTabPage.vue - 简化版 -->
<template>
  <div class="multi-tab-container">
    <el-tabs v-model="activeTab" type="border-card">
      <el-tab-pane 
        v-for="tab in tabConfig" 
        :key="tab.name"
        :label="tab.label" 
        :name="tab.name"
      >
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const tabConfig = [
  { name: 'saleorder-main', label: '销售订单主表' },
  { name: 'saleorder-entry', label: '销售订单明细' },
  { name: 'saleorder-cost', label: '销售订单成本' }
]

const activeTab = ref(route.params.pathMatch || 'saleorder-main')

watch(() => route.params.pathMatch, (newVal) => {
  if (newVal) {
    activeTab.value = newVal
  }
})

const handleTabChange = (tabName) => {
  router.push(`/business/saleorder-tabs/${tabName}`)
}
</script>
```

---

**步骤 3：菜单配置**

在系统菜单表中配置入口：

```sql
-- 父菜单：销售订单多表查询
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, icon) 
VALUES ('销售订单多表查询', 2, 1, 'saleorder-tabs', 'erp/MultiTabPage', 1, 0, 'M', '1', '1', '#');

-- 子菜单 1：销售订单主表
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, icon, query_param) 
VALUES ('销售订单主表', @parent_id, 1, 'main', 'pageTemplate/configurable/BusinessConfigurable', 1, 0, 'C', '1', '1', '#', '{"moduleCode":"saleorder-main"}');

-- 子菜单 2：销售订单明细
INSERT INTO sys_menu (...) VALUES 
('销售订单明细', @parent_id, 2, 'entry', 'pageTemplate/configurable/BusinessConfigurable', 1, 0, 'C', '1', '1', '#', '{"moduleCode":"saleorder-entry"}');

-- 子菜单 3：销售订单成本
INSERT INTO sys_menu (...) VALUES 
('销售订单成本', @parent_id, 3, 'cost', 'pageTemplate/configurable/BusinessConfigurable', 1, 0, 'C', '1', '1', '#', '{"moduleCode":"saleorder-cost"}');
```

---

**优点：**
- ✅ **用户体验优秀**：Tab 切换流畅，无需刷新整个页面
- ✅ **配置完全隔离**：每个 Tab 独立配置，互不干扰
- ✅ **URL 可分享**：可以直接访问特定 Tab（如 `/business/saleorder-tabs/entry`）
- ✅ **支持浏览器前进后退**：完整的浏览器历史记录
- ✅ **缓存独立**：每个 Tab 可以独立缓存配置

**缺点：**
- ❌ 需要创建多个配置（但这是标准做法）
- ❌ 首次加载所有配置可能稍慢（可通过懒加载优化）

---

**优化技巧：**

1. **懒加载优化**：只在切换到 Tab 时才加载对应配置
```javascript
const loadedModules = ref(new Set())
const components = ref({})

const loadModule = async (moduleCode) => {
  if (!loadedModules.value.has(moduleCode)) {
    // 动态加载配置
    const config = await ERPConfigParser.loadFromDatabase(moduleCode)
    components.value[moduleCode] = config
    loadedModules.value.add(moduleCode)
  }
}

const handleTabChange = async (tabName) => {
  await loadModule(tabName)
  router.push({ query: { tab: tabName } })
}
```

2. **保持 Tab 状态**：使用 `keep-alive` 缓存组件状态
```vue
<keep-alive>
  <component :is="components[currentModuleCode]" v-if="currentModuleCode" />
</keep-alive>
```

3. **预加载相邻 Tab**：提升切换速度
```javascript
// 预加载下一个 Tab 的配置
const preloadAdjacentTabs = (currentTab) => {
  const currentIndex = tabConfig.findIndex(t => t.name === currentTab)
  const prevTab = tabConfig[currentIndex - 1]
  const nextTab = tabConfig[currentIndex + 1]
  
  if (prevTab) loadModule(prevTab.name)
  if (nextTab) loadModule(nextTab.name)
}
```

---

## 📊 方案对比

| 方案 | 复杂度 | 灵活性 | 性能 | 维护成本 | 推荐场景 |
|------|--------|--------|------|----------|----------|
| **虚拟字段 + 字典** | ⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐ | 简单关联字段显示 |
| **数据库视图** | ⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐ | 固定多表关联报表 |
| **自定义 API** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | 超复杂业务逻辑 |
| **多表格配置** | ⭐⭐ | ⭐⭐ | ⭐⭐⭐ | ⭐⭐ | 独立表格展示 |
| **单页多 Tab 签** | ⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐ | **页签切换场景（首选）** |

---

## 🛠️ 实施建议

### 针对销售订单模块

#### 当前配置（仅主表）
```json
// table.json
{
  "tableName": "t_sale_order",  // ✅ 正确，这是主表配置
  "primaryKey": "id",
  "queryBuilder": { ... },
  "columns": [ ... ]
}
```

#### 如果需要查询明细表

**推荐方案：使用视图**

1. **创建视图**
```sql
CREATE OR REPLACE VIEW v_sale_order_entry_vw AS
SELECT 
    soe.id,
    soe.FBillNo,
    soe.FEntryId,
    soe.FMaterialId,
    soe.FQty,
    soe.FPrice,
    soe.FAmount,
    m.name AS materialName,  -- 物料名称
    u.nick_name AS creatorName  -- 创建人名称
FROM t_sale_order_entry soe
LEFT JOIN by_material m ON soe.FMaterialId = m.materialId
LEFT JOIN sys_user u ON soe.FCreatorId = u.user_id
WHERE soe.deleted = 0;
```

2. **创建新的配置（table-entry-view.json）**
```json
{
  "tableName": "v_sale_order_entry_vw",
  "primaryKey": "id",
  "queryBuilder": {
    "enabled": true,
    "fields": [
      {
        "field": "FBillNo",
        "label": "单据编号",
        "component": "input",
        "op": "right_like"
      },
      {
        "field": "materialName",
        "label": "物料名称",
        "component": "input",
        "op": "like"
      }
    ]
  },
  "columns": [
    {
      "prop": "FBillNo",
      "label": "单据编号",
      "width": 150
    },
    {
      "prop": "materialName",
      "label": "物料名称",
      "width": 200
    },
    {
      "prop": "FQty",
      "label": "数量",
      "width": 100,
      "renderType": "number"
    },
    {
      "prop": "FAmount",
      "label": "金额",
      "width": 120,
      "renderType": "currency"
    }
  ]
}
```

3. **导入配置到数据库**
```sql
-- 使用拆分json导入脚本
INSERT INTO erp_page_config (
    module_code, config_name, config_type,
    page_config, form_config, table_config,
    dict_config, business_config
) VALUES (
    'saleorder-entry',  -- 新模块编码
    '销售订单明细查询',
    'PAGE',
    -- page_config
    '{
      "pageId": "saleorder-entry",
      "pageName": "销售订单明细查询",
      "permission": "k3:saleorder:entry:query",
      "layout": "standard",
      "apiPrefix": "/erp/engine",
      "tableName": "v_sale_order_entry_vw"  -- ← 关键：使用视图
    }',
    -- form_config / table_config / dict_config / business_config ...
);
```

---

## 🔧 常见问题

### Q1: 为什么不在 table.json 中直接配置多个表？

**答：** 这违背了配置化设计的**单一职责原则**。

- 每个 `table_config` 对应一个**独立的业务表格**
- 多表关联应该通过**数据库层面**（视图）或**业务层面**（字典映射）解决
- 保持配置的简洁性和可维护性

### Q2: 如果就是要动态切换查询不同的表怎么办？

**答：** 使用**多页面配置**模式。

例如，创建一个通用的"订单查询"页面，通过 URL 参数切换表：

```javascript
// router/index.js
{
  path: '/business/order-query',
  component: BusinessConfigurable,
  props: (route) => ({
    moduleCode: route.query.tableType || 'saleorder-main'
  })
}

// 使用时：
// /business/order-query?tableType=saleorder-main  → 查主表
// /business/order-query?tableType=saleorder-entry → 查明细表
```

### Q3: 如何在列表中显示明细表的汇总数据？

**答：** 使用**虚拟字段 + DataProcessor**。

1. 在 `columns` 中定义虚拟字段：
```json
{
  "prop": "totalEntryAmount",
  "label": "明细总金额",
  "width": 120,
  "renderType": "currency",
  "virtual": true  // ← 标记为虚拟字段
}
```

2. 在 `DataProcessor` 中计算：
```java
private void processVirtualFields(
    List<Map<String, Object>> dataList, 
    String moduleCode) {
    
    if ("saleorder".equals(moduleCode)) {
        for (Map<String, Object> record : dataList) {
            // 查询明细表汇总
            BigDecimal totalAmount = jdbcTemplate.queryForObject(
                "SELECT SUM(FAmount) FROM t_sale_order_entry WHERE FBillNo = ?",
                BigDecimal.class,
                record.get("FBillNo")
            );
            record.put("totalEntryAmount", totalAmount);
        }
    }
}
```

---

## 📝 最佳实践

### 1. 优先使用字典映射
- 80% 的关联字段显示需求都可以通过字典解决
- 配置简单，性能优秀

### 2. 复杂报表使用视图
- 固定格式的多表关联报表
- 需要聚合计算的場景

### 3. 避免过度使用自定义 API
- 仅在万不得已时使用
- 优先考虑能否通过视图改造

### 4. 保持配置一致性
- 所有表的配置风格统一
- 字段命名遵循数据库规范

---

## 🎯 总结

**核心思想：** 
- `tableName` 指定的是**当前表格的主查询表**
- 关联表数据通过**字典映射**、**视图**或**虚拟字段**实现
- 保持配置的**简洁性**和**可维护性**

**选择建议：**
1. **简单关联** → 用字典映射
2. **固定多表** → 用数据库视图
3. **复杂逻辑** → 用自定义 API
4. **独立展示** → 用多表格配置
5. **页签切换** → **用单页多 Tab 签（强烈推荐）**

---

## 📚 相关文档

- [ERP 配置化开发规范](./ERP 低代码配置化优化方案.md)
- [字典构建器使用指南](./字典构建器改造完成报告.md)
- [SQL 迁移脚本规范](./ERP 迁移脚本一致性审计与索引修复流程.md)
