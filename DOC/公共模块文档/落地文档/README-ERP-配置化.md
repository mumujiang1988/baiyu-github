# ERP 配置化前端模块 - 快速上手指南

> 📅 **创建时间**: 2026-03-22  
> 🎯 **目标**: 快速上手配置化前端开发  
> 📦 **适用范围**: RuoYi-WMS + Vue 3 + Element Plus  

---

## 📋 目录

1. [文件结构](#文件结构)
2. [快速开始](#快速开始)
3. [引擎 API 使用](#引擎 api 使用)
4. [配置示例](#配置示例)
5. [常见问题](#常见问题)

---

## 📁 文件结构

```
baiyu-web/src/
├── api/erp/
│   ├── config.js              # 配置管理 API
│   └── engine/                # 引擎 API（新增）
│       ├── index.js           # 统一导出
│       ├── query.js           # 动态查询引擎 API
│       ├── validation.js      # 表单验证引擎 API
│       ├── approval.js        # 审批流程引擎 API
│       └── push.js            # 下推引擎 API
├── router/
│   ├── erp-configurable.js    # 配置化路由（新增）
│   └── erp-menu.sql          # 菜单 SQL 脚本（新增）
└── views/k3/pageTemplate/
    └── configurable/
        ├── BusinessConfigurable.vue       # 通用配置化组件（已增强）
        ├── BusinessConfigurable.styles.css
        └── components/
```

---

## 🚀 快速开始

### 步骤 1: 执行菜单 SQL (导入数据库)

运行 `src/router/erp-menu.sql` 脚本，将配置化菜单数据导入到 `sys_menu` 表:

```bash
# 方式一：使用命令行
mysql -u root -p test < src/router/erp-menu.sql

# 方式二：在 Navicat 中直接执行 SQL 文件
```

**重要**: RuoYi 框架使用动态路由，所有菜单数据存储在数据库中，不需要额外的路由配置文件。

### 步骤 2: 清除缓存并重启

创建业务配置文件 (JSON 格式):

```json
{
  "pageConfig": {
    "moduleCode": "saleOrder",
    "entityName": "销售订单"
  },
  "searchConfig": {
    "showSearch": true,
    "fields": [...]
  },
  "tableConfig": {
    "columns": [...]
  },
  "approvalConfig": {
    "enabled": true,
    "workflow": [...]
  },
  "pushConfig": {
    "enabled": true,
    "targets": [...]
  }
}
```

### 步骤 4: 访问页面

浏览器访问：`http://localhost/sale-order`

---

## 🔧 引擎 API 使用

### 1. 动态查询引擎

```javascript
import { executeDynamicQuery } from '@/api/erp/engine/query'

// 执行动态查询
const result = await executeDynamicQuery({
  moduleCode: 'saleOrder',
  queryParams: {
    fbillNo: 'SO20260322001',
    fCustomer: '客户 A'
  },
  searchConfig: {
    fields: [
      {
        field: 'fbillNo',
        label: '单据编号',
        component: 'input',
        searchType: 'like'
      }
    ]
  }
})
```

### 2. 表单验证引擎

```javascript
import { executeValidation } from '@/api/erp/engine/validation'

// 执行表单验证
const validationResult = await executeValidation({
  moduleCode: 'saleOrder',
  formData: {
    fbillNo: 'SO20260322001',
    fBillAmount: 10000
  },
  validationConfig: {
    fields: [
      {
        field: 'fbillNo',
        rules: [
          { required: true, message: '单据编号不能为空' }
        ]
      },
      {
        field: 'fBillAmount',
        rules: [
          { type: 'number', min: 0, message: '金额必须大于 0' }
        ]
      }
    ]
  }
})

if (!validationResult.valid) {
  ElMessage.error(validationResult.errorMessage)
}
```

### 3. 审批流程引擎

```javascript
import { 
  getCurrentApprovalStep,
  executeApproval,
  getApprovalHistory 
} from '@/api/erp/engine/approval'

// 获取当前审批步骤
const step = await getCurrentApprovalStep({
  moduleCode: 'saleOrder',
  billId: '123456',
  billData: { fBillAmount: 100000 }
})

// 执行审批
await executeApproval({
  moduleCode: 'saleOrder',
  billId: '123456',
  action: 'AUDIT', // AUDIT | REJECT | TRANSFER
  opinion: '同意',
  step: step.step
})

// 加载审批历史
const history = await getApprovalHistory({
  moduleCode: 'saleOrder',
  billId: '123456'
})
```

### 4. 下推引擎

```javascript
import { 
  getPushTargets,
  executePushDown,
  previewPushDown 
} from '@/api/erp/engine/push'

// 获取可下推目标
const targets = await getPushTargets('saleOrder')

// 预览下推数据
const preview = await previewPushDown({
  sourceId: '123456',
  sourceModule: 'saleOrder',
  targetModule: 'deliveryOrder'
})

// 执行下推
await executePushDown({
  sourceId: '123456',
  sourceModule: 'saleOrder',
  targetModule: 'deliveryOrder',
  confirmData: {
    fDeliveryType: 'express'
  }
})
```

---

## 📝 配置示例

### 完整配置示例

```json
{
  "pageConfig": {
    "moduleCode": "saleOrder",
    "entityName": "销售订单",
    "apiConfig": {
      "modulePath": "@/api/k3/saleOrder",
      "methods": {
        "list": "listSaleOrder",
        "get": "getSaleOrder",
        "add": "addSaleOrder",
        "update": "updateSaleOrder",
        "delete": "delSaleOrder",
        "audit": "auditSaleOrder"
      }
    }
  },
  "searchConfig": {
    "showSearch": true,
    "defaultExpand": true,
    "fields": [
      {
        "field": "fbillNo",
        "label": "单据编号",
        "component": "input",
        "props": {
          "placeholder": "输入单据编号",
          "clearable": true
        },
        "searchType": "like"
      },
      {
        "field": "fCustomer",
        "label": "客户",
        "component": "select",
        "dictionary": "customers",
        "props": {
          "filterable": true,
          "clearable": true
        }
      }
    ]
  },
  "tableConfig": {
    "rowKey": "id",
    "border": true,
    "stripe": true,
    "columns": [
      {
        "prop": "fbillNo",
        "label": "单据编号",
        "width": 150,
        "fixed": "left"
      },
      {
        "prop": "fBillAmount",
        "label": "金额",
        "renderType": "currency",
        "align": "right"
      },
      {
        "prop": "fDocumentStatus",
        "label": "状态",
        "renderType": "tag",
        "dictionary": "billStatus"
      }
    ]
  },
  "approvalConfig": {
    "enabled": true,
    "workflow": [
      {
        "step": 1,
        "name": "销售经理审核",
        "role": "sales_manager",
        "condition": "fBillAmount <= 50000",
        "action": "audit",
        "required": true
      },
      {
        "step": 2,
        "name": "财务总监审核",
        "role": "finance_manager",
        "condition": "fBillAmount > 50000",
        "action": "audit",
        "required": true
      }
    ]
  },
  "pushConfig": {
    "enabled": true,
    "targets": [
      {
        "targetModule": "deliveryOrder",
        "targetLabel": "发货通知单",
        "mapping": {
          "sourceToTarget": {
            "fbillNo": "sourceBillNo",
            "fCustomer": "customer"
          }
        }
      }
    ]
  }
}
```

---

## ❓ 常见问题

### Q1: 如何添加新的配置化页面？

**A**: 
1. 复制 `BusinessConfigurable.vue` 
2. 创建对应的 JSON 配置文件
3. 在路由文件中添加路由配置
4. 执行菜单 SQL 添加权限

### Q2: 引擎 API 调用失败怎么办？

**A**: 
1. 检查后端引擎服务是否启动
2. 检查 API 路径是否正确
3. 查看浏览器控制台错误信息
4. 确认权限配置是否完整

### Q3: 如何自定义表单验证规则？

**A**: 在配置文件的 `validationConfig` 中添加验证规则:

```json
{
  "validationConfig": {
    "fields": [
      {
        "field": "fPhone",
        "rules": [
          { 
            "pattern": "^1[3-9]\\d{9}$",
            "message": "手机号格式不正确"
          }
        ]
      }
    ]
  }
}
```

### Q4: 审批流程如何配置条件？

**A**: 在 `workflow` 中使用 `condition` 字段:

```json
{
  "workflow": [
    {
      "step": 1,
      "condition": "fBillAmount > 10000 && fBillAmount <= 50000",
      "role": "manager"
    }
  ]
}
```

---

## 📞 技术支持

如遇到问题，请提供以下信息:

1. **问题描述**: 详细描述遇到的问题
2. **错误信息**: 完整的错误堆栈
3. **相关文件**: 涉及的文件路径
4. **复现步骤**: 导致问题的操作步骤

---

**文档版本**: v1.0  
**最后更新**: 2026-03-22  
**维护团队**: ERP 研发团队
