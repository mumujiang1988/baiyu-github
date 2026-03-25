# ERP 公共配置管理 - 使用说明

## 📁 文件结构

```
erp/config/
├── index.vue              # 主页面 (包含所有遮罩页)
├── init-config-menu.sql   # 菜单初始化 SQL
└── README.md             # 本文档
```

---

## 🎯 核心设计理念

### **单页面 + 遮罩页架构**

-  **一个主页面**: `index.vue` - 显示在菜单
-  **三个对话框**:
  - `el-dialog` - 查看配置详情
  - `el-drawer` - 新增/编辑配置 (抽屉式)
  - `el-dialog` - 历史版本列表

### **优势对比**

| 特性 | 传统多页面 | 单页面 + 遮罩页 |
|------|-----------|----------------|
| **用户体验** |  页面跳转，有刷新感 |  原地打开，流畅 |
| **路由复杂度** |  需要多个路由配置 |  只需一个路由 |
| **状态管理** |  跨页面传参复杂 |  本地状态即可 |
| **返回逻辑** |  需要处理返回按钮 |  关闭对话框 |
| **代码维护** |  多个 .vue 文件 |  单个文件 |

---

## 🚀 快速开始

### Step 1: 执行数据库初始化

```bash
mysql -u root -p test < d:/baiyuyunma/gitee-baiyu/baiyu-web/src/views/erp/config/init-config-menu.sql
```

**执行后会创建**:
-  ERP 业务菜单 (父菜单)
-  公共配置管理 (子菜单)
-  10 个按钮权限 (查询/新增/修改/删除/导出/导入/复制/历史版本/回滚/状态管理)

---

### Step 2: 清理旧数据 (可选)

如果之前有旧的配置菜单，先清理:

```bash
mysql -u root -p test < d:/baiyuyunma/gitee-baiyu/baiyu-web/src/views/erp/cleanup-config-routes.sql
```

---

### Step 3: 访问系统

1. 访问：http://localhost:8900/
2. 左侧菜单：**ERP 业务菜单** → **公共配置管理**
3. 点击**新增配置**按钮，打开抽屉式编辑器
4. 点击**编辑**按钮，打开抽屉式编辑器
5. 点击**历史**按钮，打开历史版本对话框

---

## 💡 功能说明

### **1. 配置列表页**

-  搜索过滤 (模块编码/配置名称/配置类型/状态)
-  表格展示 (支持分页)
-  操作按钮 (查看/编辑/历史/更多)

---

### **2. 查看配置 (Dialog)**

**触发方式**: 点击列表中的"查看"按钮

**功能**:
-  显示配置详细信息
-  显示配置内容 (JSON 格式，只读)
-  可点击"编辑配置"按钮进入编辑模式

---

### **3. 新增/编辑配置 (Drawer)**

**触发方式**: 
- 点击"新增配置"按钮 → 新增模式
- 点击"编辑"按钮 → 编辑模式
- 查看页点击"编辑配置" → 编辑模式

**特点**:
-  抽屉式布局 (宽度 900px)
-  左侧：基本信息表单
-  右侧：CodeMirror 编辑器
-  JSON 格式实时验证
-  未保存关闭时有确认提示

**字段说明**:
- **模块编码**: 小驼峰命名，创建后不可修改
- **配置名称**: 如"销售订单页面配置"
- **配置类型**: PAGE/DICT/PUSH 等
- **是否公共**: 公共配置可被所有用户访问
- **配置内容**: JSON 格式的配置数据
- **变更原因**: 选填，记录修改原因

---

### **4. 历史版本 (Dialog)**

**触发方式**: 点击列表中的"历史"按钮

**功能**:
-  显示当前配置信息
-  显示历史版本列表
-  可查看版本详情
-  可回滚到指定版本

---

##  API 接口需求

需要确保以下后端接口可用:

```java
// 1. 查询配置列表
GET /erp/config/list
参数：pageNum, pageSize, moduleCode, configName, configType, status

// 2. 获取单个配置详情
GET /erp/config/{configId}

// 3. 保存配置 (新增/编辑)
POST /erp/config/save
参数：configId, moduleCode, configName, configType, configContent, isPublic, remark, changeReason

// 4. 删除配置
DELETE /erp/config/{configId}

// 5. 获取历史版本
GET /erp/config/history/{configId}

// 6. 回滚版本
POST /erp/config/rollback
参数：configId, targetVersion
```

---

## 📝 路由配置

前端路由会自动注册，无需手动配置:

```javascript
// router/index.js 中已自动识别
{
  path: '/erp/config',
  component: Layout,
  hidden: false,
  permissions: ['erp:config:query'],
  children: [
    {
      path: 'index',
      component: () => import('@/views/erp/config/index'),
      name: 'ConfigPage',
      meta: { title: '公共配置管理', icon: 'setting' }
    }
  ]
}
```

---

##  注意事项

### **1. 不需要创建独立路由**

 **不要创建**:
- `/erp/config/editor`
- `/erp/config/history`

 **只需要**:
- `/erp/config` (主页面)

---

### **2. 按钮权限标识**

确保用户有以下权限才能看到对应按钮:

| 按钮 | 权限标识 |
|------|---------|
| 查询 | `erp:config:query` |
| 新增 | `erp:config:add` |
| 编辑 | `erp:config:edit` |
| 删除 | `erp:config:remove` |
| 导出 | `erp:config:export` |
| 导入 | `erp:config:import` |
| 复制 | `erp:config:copy` |
| 历史 | `erp:config:history` |
| 回滚 | `erp:config:rollback` |

---

### **3. CodeMirror 配置**

使用了 `vue-codemirror` 组件，确保已安装依赖:

```json
{
  "vue-codemirror": "^1.0.0",
  "@codemirror/lang-json": "^6.0.0"
}
```

---

## 🎨 UI/UX 设计

### **响应式布局**

-  桌面端优化 (大抽屉、宽对话框)
-  表格支持横向滚动
-  对话框最大高度 95vh

### **交互体验**

-  加载状态提示
-  操作成功/失败反馈
-  危险操作二次确认
-  JSON 格式实时验证

---

##  数据示例

### **配置内容示例**

```json
{
  "pageConfig": {
    "title": "销售订单页面",
    "permissionPrefix": "sale:order",
    "primaryKey": "id"
  },
  "apiConfig": {
    "methods": {
      "list": "/sale/order/list",
      "get": "/sale/order/{id}",
      "add": "/sale/order/add",
      "edit": "/sale/order/edit",
      "delete": "/sale/order/{id}"
    }
  },
  "searchConfig": {
    "showSearch": true,
    "defaultExpand": true,
    "fields": [
      {
        "field": "orderNo",
        "label": "订单号",
        "component": "input"
      }
    ]
  },
  "tableConfig": {
    "rowKey": "id",
    "border": true,
    "stripe": true,
    "columns": [
      {
        "prop": "orderNo",
        "label": "订单号"
      }
    ]
  }
}
```

---

## 🔗 相关文件

- **主页面**: `d:/baiyuyunma/gitee-baiyu/baiyu-web/src/views/erp/config/index.vue`
- **初始化 SQL**: `d:/baiyuyunma/gitee-baiyu/baiyu-web/src/views/erp/config/init-config-menu.sql`
- **清理 SQL**: `d:/baiyuyunma/gitee-baiyu/baiyu-web/src/views/erp/cleanup-config-routes.sql`

---

## 📞 技术支持

如有问题，请参考:
1. RuoYi 框架文档
2. Element Plus 文档  
3. Vue 3 文档
4. CodeMirror 文档

---

**创建时间**: 2026-03-25  
**最后更新**: 2026-03-25  
**版本**: v1.0
