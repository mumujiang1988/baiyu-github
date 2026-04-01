# ERP 用户表格列配置 - 快速参考卡

**版本：** v1.0  
**日期：** 2026-04-01  
**技术栈：** Spring Boot 3.x + JdbcTemplate + SqlBuilder

---

## 📦 核心文件清单

### 后端文件（6 个）

```
ruoyi-modules/ruoyi-erp-api/src/main/java/com/ruoyi/erp/
├── domain/
│   ├── entity/ErpUserTableConfig.java          # 实体类 (可选，仅用于类型提示)
│   ├── bo/ErpUserTableConfigBo.java            # BO 对象
│   └── vo/ErpUserTableConfigVo.java            # VO 对象
├── service/
│   ├── ErpUserTableConfigService.java          # Service 接口
│   └── impl/ErpUserTableConfigServiceImpl.java # Service 实现 (JdbcTemplate)
└── controller/erp/
    └── ErpUserTableConfigController.java       # Controller
```

**说明：** 
- ✅ **无需 Mapper 接口和 XML** - 直接使用 JdbcTemplate + SqlBuilder
- ✅ **Entity 可选** - 可以只用于 IDE 类型提示，非必需

### 前端文件（3 个）

```
baiyu-web/src/
├── api/erp/
│   └── userTableConfig.js                      # API 封装
├── components/
│   └── ColumnSettingDialog/
│       └── index.vue                           # 列设置对话框组件
└── views/erp/pageTemplate/configurable/
    └── BusinessConfigurable/components/
        └── BusinessTable.vue                   # 集成列设置功能
```

### 数据库脚本（1 个）

```
DOC/erp_user_table_config.sql                   # 建表 SQL
```

---

## 🗄️ 数据库表结构

```sql
CREATE TABLE `erp_user_table_config` (
  `config_id` BIGINT NOT NULL AUTO_INCREMENT,
  `module_code` VARCHAR(50) NOT NULL,
  `user_id` BIGINT NOT NULL,
  `table_type` VARCHAR(20) NOT NULL DEFAULT 'main',
  `tab_name` VARCHAR(50) DEFAULT NULL,
  `column_config` JSON NOT NULL,
  `remark` VARCHAR(500),
  `create_by` VARCHAR(64),
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_by` VARCHAR(64),
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`config_id`),
  UNIQUE KEY `uk_module_user_tab` (`module_code`, `user_id`, `table_type`, `tab_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
```

**唯一键：** `module_code + user_id + table_type + tab_name`

---

## 🔌 API 接口

### 1. 获取用户配置

**请求：**
```http
GET /erp/user/config/get/{moduleCode}?tableType=main&tabName=entry
```

**响应：**
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": "[{\"prop\":\"FBillNo\",\"label\":\"单据编号\",\"width\":150,\"visible\":true}]"
}
```

**说明：**
- `moduleCode`: 必填，如 `saleorder`
- `tableType`: 可选，默认 `main`（主表格）或 `detail`（详情页表格）
- `tabName`: detail 类型时必填，如 `entry`、`cost`

---

### 2. 保存用户配置

**请求：**
```http
POST /erp/user/config/save
Content-Type: application/json

{
  "moduleCode": "saleorder",
  "tableType": "main",
  "tabName": null,
  "columnConfig": "[{\"prop\":\"FBillNo\",\"width\":150,\"visible\":true}]",
  "remark": "用户自定义配置"
}
```

**响应：**
```json
{
  "code": 200,
  "msg": "保存成功"
}
```

---

## 💻 前端使用示例

### 在 BusinessTable 中集成

```vue
<template>
  <el-card shadow="never" class="table-card">
    <!-- 工具栏 -->
    <div class="toolbar-row">
      <el-space wrap>
        <el-button icon="Setting" @click="openColumnSetting">
          列设置
        </el-button>
      </el-space>
    </div>
    
    <!-- 表格 -->
    <el-table :data="tableData">
      <template v-for="column in visibleColumns" :key="column.prop">
        <el-table-column
          v-if="column.visible"
          :prop="column.prop"
          :label="column.label"
          :width="column.width"
        />
      </template>
    </el-table>
    
    <!-- 列设置对话框 -->
    <ColumnSettingDialog
      ref="columnSettingRef"
      :module-code="moduleCode"
      :table-config="tableConfig"
      :detail-config="detailConfig"
      @update="handleColumnUpdate"
    />
  </el-card>
</template>

<script setup>
import { ref } from 'vue'
import ColumnSettingDialog from '@/components/ColumnSettingDialog/index.vue'

const columnSettingRef = ref(null)
const moduleCode = 'saleorder'

const openColumnSetting = () => {
  columnSettingRef.value?.open()
}

const handleColumnUpdate = (config) => {
  console.log('配置已更新:', config)
  // 重新渲染表格
}
</script>
```

---

## 🎯 配置 JSON 格式

### 列配置数组

```json
[
  {
    "prop": "字段名",
    "label": "列标题",
    "width": 150,
    "visible": true,
    "fixed": "left",
    "sortable": true,
    "resizable": true
  },
  {
    "prop": "FDate",
    "label": "日期",
    "width": 140,
    "visible": false
  }
]
```

### 字段说明

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| prop | String | ✓ | 字段名（数据库列名） |
| label | String | ✓ | 列标题（显示文本） |
| width | Number | ✗ | 列宽（像素，50-500） |
| visible | Boolean | ✓ | 是否显示 |
| fixed | String | ✗ | 固定列：`left` / `right` |
| sortable | Boolean | ✗ | 是否可排序 |
| resizable | Boolean | ✗ | 是否可调整宽度 |

---

## 🔧 后端关键技术点

### 1. JdbcTemplate + SqlBuilder (无 Mapper)

项目采用 **纯 JdbcTemplate** 方式，无需 MyBatis Mapper:

```java
// 构建查询条件
List<Map<String, Object>> conditions = new ArrayList<>();
Map<String, Object> condition = new HashMap<>();
condition.put("field", "module_code");
condition.put("operator", "eq");
condition.put("value", moduleCode);
conditions.add(condition);

// 使用 SqlBuilder 生成 WHERE 子句
SqlBuilder.SqlResult sqlResult = sqlBuilder.buildWhere(conditions);
String sql = "SELECT * FROM erp_user_table_config" + sqlResult.getSql();

// 直接执行查询，返回 Map 列表
List<Map<String, Object>> resultList = jdbcTemplate.queryForList(
    sql, 
    sqlResult.getParams().toArray()
);

// 转换为 VO
for (Map<String, Object> row : resultList) {
    ErpUserTableConfigVo vo = convertMapToVo(row);
}
```

### 2. 分页查询

```java
long pageNum = pageQuery.getPageNum();
long pageSize = pageQuery.getPageSize();
long offset = (pageNum - 1) * pageSize;

StringBuilder sql = new StringBuilder("SELECT * FROM erp_user_table_config");
sql.append(sqlResult.getSql());
sql.append(" ORDER BY create_time DESC");
sql.append(" LIMIT ? OFFSET ?");

List<Object> params = new ArrayList<>(sqlResult.getParams());
params.add(pageSize);
params.add(offset);

List<Map<String, Object>> resultList = jdbcTemplate.queryForList(
    sql.toString(), 
    params.toArray()
);
```

### 3. 新增或更新（UPSERT）

```java
// 先查询是否存在
String existingConfig = getUserColumnConfig(moduleCode, tableType, tabName);

if (existingConfig != null) {
    // 更新
    return updateConfig(moduleCode, userId, tableType, tabName, columnConfig, remark);
} else {
    // 新增
    return insertConfig(moduleCode, userId, tableType, tabName, columnConfig, remark, loginId);
}
```

### 4. Map 转 VO

```java
private ErpUserTableConfigVo convertMapToVo(Map<String, Object> map) {
    if (map == null || map.isEmpty()) {
        return null;
    }
    
    ErpUserTableConfigVo vo = new ErpUserTableConfigVo();
    vo.setConfigId(getLongValue(map, "config_id"));
    vo.setModuleCode(getStringValue(map, "module_code"));
    vo.setUserId(getLongValue(map, "user_id"));
    vo.setColumnConfig(getStringValue(map, "column_config"));
    vo.setCreateTime(getLocalDateTimeValue(map, "create_time"));
    
    return vo;
}

private Long getLongValue(Map<String, Object> map, String key) {
    Object value = map.get(key);
    return value != null ? Long.valueOf(value.toString()) : null;
}
```

---

## 🚀 快速部署步骤

### 步骤 1：执行数据库脚本

```bash
mysql -u root -p test < DOC/erp_user_table_config.sql
```

### 步骤 2：创建后端代码

复制 7 个 Java 文件到对应目录，然后编译：

```bash
cd baiyu-ruoyi/ruoyi-admin-wms
mvn clean install
```

### 步骤 3：创建前端代码

复制 3 个文件到对应目录，安装依赖：

```bash
cd baiyu-web
npm install vuedraggable@next
```

### 步骤 4：重启服务

```bash
# 后端
java -jar target/ruoyi-admin-wms.jar

# 前端
npm run dev
```

---

## ✅ 功能测试清单

### 基础功能

- [ ] 打开列设置对话框
- [ ] 拖拽调整列顺序
- [ ] 开关控制列显示/隐藏
- [ ] 输入框设置列宽（50-500）
- [ ] 点击"保存配置"按钮
- [ ] 刷新页面后配置保留

### 高级功能

- [ ] 主表格和详情页表格分别配置
- [ ] 不同用户配置独立
- [ ] 不同模块配置独立
- [ ] 点击"恢复默认"按钮
- [ ] 配置保存到数据库

---

## ⚠️ 注意事项

### 开发注意事项

1. **权限控制**
   - 建议添加 `@SaCheckPermission("erp:user-config:query")`
   - 保存操作需要 `@SaCheckPermission("erp:user-config:edit")`

2. **数据验证**
   - `columnConfig` JSON 格式需在前端校验
   - 后端可以添加 JSON Schema 验证

3. **性能优化**
   - 配置数据较小，无需缓存
   - 如果配置项增多，可考虑 Redis 缓存

4. **兼容性**
   - 旧用户没有配置记录时，返回 null，使用默认配置
   - 配置数据结构变更时，需要兼容处理

### 用户使用指南

1. **打开列设置** → 点击工具栏的"列设置"按钮
2. **调整列顺序** → 拖拽列名称左侧的拖动图标
3. **显示/隐藏列** → 点击列右侧的开关
4. **设置列宽** → 在输入框中输入数值（50-500）
5. **保存配置** → 点击"保存配置"按钮
6. **恢复默认** → 点击"恢复默认"按钮

---

## 📞 技术支持

如有问题，请参考完整方案文档：
`DOC/ERP 用户表格列配置功能设计方案.md`

---

**文档结束**
