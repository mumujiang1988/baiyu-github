# ERP 详情页签 relationConfig 标准配置模板

## 📋 标准配置格式（强制）

```json
{
  "detail": {
    "enabled": true,
    "displayType": "drawer",
    "title": "{entityName}详情 - {billNo}",
    "width": "60%",
    "direction": "rtl",
    "loadStrategy": "lazy",
    "tabs": [
      {
        "name": "entry",
        "label": "明细表名称",
        "icon": "Document",
        "type": "table",
        "dataField": "entryList",
        "tableName": "子表表名",
        "relationConfig": {
          "enabled": true,
          "masterTable": "主表表名",
          "masterField": "主表关联字段",
          "detailTable": "子表表名",
          "detailField": "子表关联字段",
          "operator": "eq"
        },
        "queryConfig": {
          "enabled": true,
          "defaultConditions": [
            {
              "field": "子表关联字段",
              "operator": "eq",
              "value": "${billNo}",
              "description": "按单据编号查询明细"
            }
          ],
          "defaultOrderBy": [
            {"field": "ID", "direction": "ASC"}
          ]
        },
        "table": {
          "border": true,
          "stripe": true,
          "maxHeight": "500",
          "showOverflowTooltip": true,
          "columns": [
            {"prop": "字段名", "label": "列名", "width": 100}
          ]
        }
      }
    ]
  }
}
```

## ✅ 必填字段说明

### relationConfig（强制）
| 字段 | 类型 | 说明 | 示例 |
|------|------|------|------|
| masterTable | string | 主表表名 | `"receive_notice"` |
| masterField | string | 主表关联字段 | `"FBillNo"` |
| detailTable | string | 子表表名 | `"receive_notice_entry"` |
| detailField | string | 子表关联字段 | `"FBillNo"` |
| operator | string | 关联运算符 | `"eq"` |

**注意**: `enabled` 字段已废弃（2026-04-01），代码强制使用 relationConfig，无需开关标志。

### queryConfig.defaultConditions（强制）
| 字段 | 类型 | 说明 | 要求 |
|------|------|------|------|
| field | string | 查询字段 | 必须与 `detailField` 一致 |
| operator | string | 运算符 | 通常为 `"eq"` |
| value | string | 查询值 | 必须使用 `${billNo}` 模板变量 |
| description | string | 描述说明 | 便于维护理解 |

## 🚀 快速配置步骤

### 步骤 1：确定主表和子表
```sql
-- 示例：收料通知单
主表：receive_notice
子表：receive_notice_entry
关联字段：FBillNo
```

### 步骤 2：复制标准模板
从以下模块复制模板：
- ✅ 收料通知单（receivenotice）
- ⚠️ 禁止使用旧版本配置

### 步骤 3：替换表名和字段
```json
// 修改前
"masterTable": "receive_notice",
"masterField": "FBillNo",
"detailTable": "receive_notice_entry",
"detailField": "FBillNo"

// 修改后（示例：销售订单）
"masterTable": "t_sale_order",
"masterField": "FBillNo",
"detailTable": "t_sale_order_entry",
"detailField": "FBillNo"
```

### 步骤 4：验证配置
检查以下要点：
- [ ] `relationConfig.enabled` 是否为 `true`
- [ ] `masterField` 和 `detailField` 是否匹配
- [ ] `defaultConditions[0].field` 是否与 `detailField` 一致
- [ ] `value` 是否使用 `${billNo}` 格式

## ❌ 常见错误配置

### 错误 1：缺少 enabled 字段
```json
// ❌ 错误
"relationConfig": {
  "masterTable": "receive_notice",
  // 缺少 enabled: true
  ...
}

// ✅ 正确
"relationConfig": {
  "enabled": true,  // ← 必须显式声明
  "masterTable": "receive_notice",
  ...
}
```

### 错误 2：字段名不一致
```json
// ❌ 错误
"relationConfig": {
  "detailField": "FBillNo",  // 子表字段
},
"queryConfig": {
  "defaultConditions": [
    {
      "field": "F_Bill_No",  // ← 字段名不匹配！
      ...
    }
  ]
}

// ✅ 正确
"relationConfig": {
  "detailField": "FBillNo",
},
"queryConfig": {
  "defaultConditions": [
    {
      "field": "FBillNo",  // ← 保持一致
      ...
    }
  ]
}
```

### 错误 3：使用旧的兼容模式
```json
// ❌ 错误（旧版）
"subTableQueryConfigs": {
  "entry": {
    "tableName": "receive_notice_entry",
    "defaultConditions": [...]
  }
}

// ✅ 正确（新版）
"detail": {
  "tabs": [
    {
      "tableName": "receive_notice_entry",
      "relationConfig": {...},
      "queryConfig": {...}
    }
  ]
}
```

## 🔍 配置检查清单

在提交 SQL 配置前，请逐项检查：

- [ ] **relationConfig** 是否包含所有必填字段？
  - [ ] enabled: true
  - [ ] masterTable
  - [ ] masterField
  - [ ] detailTable
  - [ ] detailField
  - [ ] operator: "eq"

- [ ] **queryConfig** 是否正确配置？
  - [ ] defaultConditions[0].field === detailField
  - [ ] defaultConditions[0].value === "${billNo}"
  - [ ] defaultOrderBy 已配置

- [ ] **表格列配置** 是否与数据库一致？
  - [ ] columns[].prop 与数据库字段名完全匹配
  - [ ] 没有使用不存在的字段

- [ ] **其他检查项**
  - [ ] 表名使用 snake_case 格式
  - [ ] 字段名大小写与数据库一致
  - [ ] JSON 格式正确（可使用 JSON 验证工具）

## 📝 实际案例参考

### 案例 1：收料通知单（参考标准）
文件：`收料通知单初始化配置.sql`
```json
"relationConfig": {
  "enabled": true,
  "masterTable": "receive_notice",
  "masterField": "FBillNo",
  "detailTable": "receive_notice_entry",
  "detailField": "FBillNo",
  "operator": "eq"
}
```

### 案例 2：销售订单
文件：`销售订单初始化配置.sql`
```json
"relationConfig": {
  "enabled": true,
  "masterTable": "t_sale_order",
  "masterField": "FBillNo",
  "detailTable": "t_sale_order_entry",
  "detailField": "FBillNo",
  "operator": "eq"
}
```

### 案例 3：采购订单
```json
"relationConfig": {
  "enabled": true,
  "masterTable": "purchase_order",
  "masterField": "FBillNo",
  "detailTable": "purchase_order_entry",
  "detailField": "FBillNo",
  "operator": "eq"
}
```

## 🛠️ 调试技巧

### 浏览器控制台日志
配置正确时，控制台会显示：
```
🔗 [queryAllSubTables] entry: 使用关联配置
  - 主表字段：receive_notice.FBillNo
  - 子表字段：receive_notice_entry.FBillNo
  - 关联值：CGSL000011
  
✅ 替换模板变量：${billNo} -> CGSL000011
```

### 常见报错处理
如果看到以下错误：

**错误 1：子表数据为空**
```
📥 entry 查询结果：{rows: 0, total: 0}
```
→ 检查 `relationConfig` 字段是否与数据库一致

**错误 2：缺少 relationConfig**
```
❌ [queryAllSubTables] entry: 缺少 relationConfig 配置
```
→ 添加完整的 `relationConfig` 配置

**错误 3：模板变量未替换**
```
最终查询条件：{"value": "${billNo}"}
```
→ 检查 `detailField` 是否与查询条件的 `field` 匹配

## 📞 需要帮助？

如遇问题，请参考：
1. 收料通知单完整配置示例
2. 前端代码：`multiTableQueryBuilder.js` 第 101-127 行
3. 联系开发团队获取支持

---

**最后更新**: 2026-04-01  
**维护者**: ERP 开发团队
