# 🚀 ERP配置化方案 - 完整迁移部署指南

> **版本**: v1.0  
> **日期**: 2026-03-24  
> **适用场景**: 将ERP配置化模块迁移到新环境并完成接入

---

## 📋 目录

1. [迁移前准备](#迁移前准备)
2. [后端迁移流程](#后端迁移流程)
3. [前端迁移流程](#前端迁移流程)
4. [数据库初始化](#数据库初始化)
5. [配置与启动](#配置与启动)
6. [验证与测试](#验证与测试)
7. [常见问题](#常见问题)

---

## 🎯 一、迁移前准备

### 1.1 环境要求

| 组件 | 版本要求 | 说明 |
|------|---------|------|
| **JDK** | 17+ | Java运行环境 |
| **Maven** | 3.6+ | 项目构建工具 |
| **MySQL** | 8.0+ | 数据库 |
| **Redis** | 6.0+ | 缓存服务 |
| **Node.js** | 16+ | 前端构建环境 |
| **Nginx** | 1.18+ | 前端部署服务器 |

### 1.2 文件清单

#### 后端文件（必须）

```
baiyu-ruyi-cs/ruoyi-modules/ruoyi-erp-api/
├── src/main/java/com/ruoyi/erp/
│   ├── controller/erp/          # 4个Controller
│   │   ├── ErpEngineController.java
│   │   ├── ErpPageConfigController.java
│   │   ├── ErpApprovalFlowController.java
│   │   └── ErpPushRelationController.java
│   ├── service/                 # Service层
│   │   ├── engine/              # 4个引擎
│   │   │   ├── DynamicQueryEngine.java
│   │   │   ├── FormValidationEngine.java
│   │   │   ├── ApprovalWorkflowEngine.java
│   │   │   └── PushDownEngine.java
│   │   ├── impl/                # Service实现
│   │   ├── ErpApprovalFlowService.java
│   │   ├── ErpPageConfigService.java
│   │   ├── ErpPushRelationService.java
│   │   └── ISuperDataPermissionService.java
│   ├── mapper/                  # 5个Mapper
│   ├── domain/                  # Domain层
│   │   ├── entity/              # 5个Entity
│   │   ├── bo/                  # 5个BO
│   │   └── vo/                  # 5个VO
│   └── config/                  # 配置类（如有）
├── pom.xml                      # Maven配置
└── src/main/resources/
    └── mapper/                  # MyBatis XML（如有）
```

#### 前端文件（必须）

```
baiyu-web/src/views/erp/
├── pageTemplate/                # 配置化页面模板
│   ├── configurable/
│   │   ├── BusinessConfigurable.vue        # 核心组件（1739行）
│   │   └── BusinessConfigurable.styles.css
│   ├── components/
│   │   └── ExpandRowDetail.jsx
│   └── configs/
│       └── business.config.template.json   # 配置模板
├── ConfigDrivenPage/            # 业务页面示例
│   └── saleorder/
│       ├── configurable/
│       │   ├── saleorder.vue
│       │   └── saleorder.styles.css
│       ├── components/
│       │   └── ExpandRowDetail.jsx
│       └── configs/
│           └── saleOrder.config.json
└── config/                      # 配置管理页面
    ├── index.vue
    ├── editor.vue
    └── history.vue
```

#### 数据库脚本（必须）

```
DOC/公共模块文档/落地sql/
└── ERP配置化完整初始化脚本.sql
```

---

## 🔧 二、后端迁移流程

### 2.1 复制后端文件

#### 步骤1：复制模块目录

```bash
# 源路径
SOURCE="d:\baiyuyunma\gitee-baiyu\baiyu-ruyi-cs\ruoyi-modules\ruoyi-erp-api"

# 目标路径（新环境）
TARGET="/path/to/new/environment/baiyu-ruyi-cs/ruoyi-modules/ruoyi-erp-api"

# 复制整个模块
cp -r $SOURCE $TARGET
```

#### 步骤2：修改父pom.xml

在新环境的 `baiyu-ruyi-cs/pom.xml` 中添加：

```xml
<dependencyManagement>
    <dependencies>
        <!-- 其他依赖... -->
        
        <!-- ERP配置化模块 -->
        <dependency>
            <groupId>com.ruoyi</groupId>
            <artifactId>ruoyi-erp-api</artifactId>
            <version>${revision}</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

#### 步骤3：修改ruoyi-modules的pom.xml

在 `baiyu-ruyi-cs/ruoyi-modules/pom.xml` 中添加：

```xml
<modules>
    <module>ruoyi-demo</module>
    <module>ruoyi-generator</module>
    <module>ruoyi-system</module>
    <!-- 新增ERP配置化模块 -->
    <module>ruoyi-erp-api</module>
</modules>
```

#### 步骤4：修改ruoyi-admin-wms的pom.xml

在 `baiyu-ruyi-cs/ruoyi-admin-wms/pom.xml` 中添加依赖：

```xml
<dependencies>
    <!-- 现有依赖... -->
    
    <!-- ERP配置化模块 -->
    <dependency>
        <groupId>com.ruoyi</groupId>
        <artifactId>ruoyi-erp-api</artifactId>
    </dependency>
</dependencies>
```

### 2.2 修改模块pom.xml

检查 `ruoyi-erp-api/pom.xml` 的parent配置：

```xml
<parent>
    <groupId>com.ruoyi</groupId>
    <artifactId>ruoyi-wms</artifactId>
    <version>${revision}</version>
    <relativePath>../../pom.xml</relativePath>
</parent>
```

### 2.3 编译后端项目

```bash
# 进入项目根目录
cd /path/to/new/environment/baiyu-ruyi-cs

# 清理并编译
mvn clean install -DskipTests -T 4

# 如果编译失败，检查依赖是否完整
mvn dependency:tree
```

### 2.4 常见编译问题

#### 问题1：找不到父POM

**原因**：relativePath配置错误

**解决**：
```xml
<parent>
    <groupId>com.ruoyi</groupId>
    <artifactId>ruoyi-wms</artifactId>
    <version>${revision}</version>
    <relativePath>../../pom.xml</relativePath>  <!-- 确保路径正确 -->
</parent>
```

#### 问题2：依赖版本冲突

**原因**：版本号不一致

**解决**：
```bash
# 查看依赖树
mvn dependency:tree -Dverbose

# 排除冲突依赖
<dependency>
    <groupId>xxx</groupId>
    <artifactId>xxx</artifactId>
    <exclusions>
        <exclusion>
            <groupId>conflict-group</groupId>
            <artifactId>conflict-artifact</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

---

## 🎨 三、前端迁移流程

### 3.1 复制前端文件

#### 步骤1：复制配置化页面模板

```bash
# 源路径
SOURCE="d:\baiyuyunma\gitee-baiyu\baiyu-web\src\views\erp\pageTemplate"

# 目标路径
TARGET="/path/to/new/environment/baiyu-web/src/views/erp/pageTemplate"

# 复制目录
cp -r $SOURCE $TARGET
```

#### 步骤2：复制业务页面示例

```bash
# 源路径
SOURCE="d:\baiyuyunma\gitee-baiyu\baiyu-web\src\views\erp\ConfigDrivenPage"

# 目标路径
TARGET="/path/to/new/environment/baiyu-web/src/views/erp/ConfigDrivenPage"

# 复制目录
cp -r $SOURCE $TARGET
```

#### 步骤3：复制配置管理页面

```bash
# 源路径
SOURCE="d:\baiyuyunma\gitee-baiyu\baiyu-web\src\views\erp\config"

# 目标路径
TARGET="/path/to/new/environment/baiyu-web/src/views/erp/config"

# 复制目录
cp -r $SOURCE $TARGET
```

### 3.2 配置路由

在 `baiyu-web/src/router/index.js` 或路由配置文件中添加：

```javascript
{
  path: '/erp',
  component: Layout,
  redirect: '/erp/config',
  name: 'ERP',
  meta: { title: 'ERP管理', icon: 'document' },
  children: [
    {
      path: 'config',
      component: () => import('@/views/erp/config/index'),
      name: 'ErpConfig',
      meta: { title: '配置管理', icon: 'setting' }
    },
    {
      path: 'ConfigDrivenPage/saleorder',
      component: () => import('@/views/erp/ConfigDrivenPage/saleorder/configurable/saleorder'),
      name: 'SaleOrder',
      meta: { title: '销售订单管理', icon: 'document' }
    }
  ]
}
```

### 3.3 配置API接口

在 `baiyu-web/src/api/erp/` 目录下创建接口文件：

#### engine.js（引擎接口）

```javascript
import request from '@/utils/request'

// 动态查询
export function executeDynamicQuery(data) {
  return request({
    url: '/erp/engine/query/execute',
    method: 'post',
    data
  })
}

// 表单验证
export function executeFormValidation(data) {
  return request({
    url: '/erp/engine/validation/execute',
    method: 'post',
    data
  })
}

// 执行审批
export function executeApproval(data) {
  return request({
    url: '/erp/engine/approval/execute',
    method: 'post',
    data
  })
}

// 执行下推
export function executePushDown(data) {
  return request({
    url: '/erp/engine/push/execute',
    method: 'post',
    data
  })
}
```

#### config.js（配置接口）

```javascript
import request from '@/utils/request'

// 获取页面配置
export function getPageConfig(moduleCode) {
  return request({
    url: `/erp/config/get/${moduleCode}`,
    method: 'get'
  })
}

// 保存配置
export function saveConfig(data) {
  return request({
    url: '/erp/config',
    method: 'post',
    data
  })
}

// 更新配置
export function updateConfig(data) {
  return request({
    url: '/erp/config',
    method: 'put',
    data
  })
}
```

### 3.4 编译前端项目

```bash
# 进入前端项目目录
cd /path/to/new/environment/baiyu-web

# 安装依赖
npm install

# 开发环境运行
npm run dev

# 生产环境构建
npm run build
```

---

## 💾 四、数据库初始化

### 4.1 执行初始化脚本

#### 方式1：MySQL命令行

```bash
# 登录MySQL
mysql -u root -p

# 执行脚本
source /path/to/ERP配置化完整初始化脚本.sql

# 或直接执行
mysql -u root -p test < /path/to/ERP配置化完整初始化脚本.sql
```

#### 方式2：MySQL Workbench

1. 打开MySQL Workbench
2. 连接到数据库
3. File → Open SQL Script
4. 选择 `ERP配置化完整初始化脚本.sql`
5. 点击 Execute (闪电图标)

#### 方式3：Navicat

1. 打开Navicat
2. 连接到数据库
3. 右键数据库 → Execute SQL File
4. 选择脚本文件并执行

### 4.2 验证初始化结果

```sql
-- 查看表结构
SHOW TABLES LIKE 'erp_%';

-- 查看配置数据
SELECT * FROM erp_page_config;
SELECT * FROM erp_approval_flow;
SELECT * FROM erp_push_relation;

-- 查看菜单数据
SELECT * FROM sys_menu WHERE menu_name LIKE '%ERP%' OR menu_name LIKE '%销售订单%';

-- 查看索引
SHOW INDEX FROM erp_page_config;
SHOW INDEX FROM erp_approval_history;
```

### 4.3 初始化脚本内容说明

脚本包含以下内容：

| 部分 | 内容 | 说明 |
|------|------|------|
| **第一部分** | 创建核心表结构 | 5个表（page_config、config_history、approval_flow、approval_history、push_relation） |
| **第二部分** | 插入初始配置数据 | 销售订单页面配置、审批流程配置、下推关系配置 |
| **第三部分** | 创建菜单和权限 | ERP业务菜单目录、销售订单管理菜单、8个按钮权限 |
| **第四部分** | 添加性能优化索引 | 7个复合索引 |
| **第五部分** | 验证初始化结果 | 查看表、数据、索引 |

---

## ⚙️ 五、配置与启动

### 5.1 后端配置

#### application.yml配置

```yaml
# 数据源配置
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
    username: root
    password: your_password

# Redis配置
  redis:
    host: localhost
    port: 6379
    password: 
    database: 0

# MyBatis Plus配置
mybatis-plus:
  mapper-locations: classpath*:mapper/**/*Mapper.xml
  type-aliases-package: com.ruoyi.**.domain
  configuration:
    map-underscore-to-camel-case: true
```

#### 启动后端服务

```bash
# 方式1：Maven启动
cd /path/to/new/environment/baiyu-ruyi-cs/ruoyi-admin-wms
mvn spring-boot:run

# 方式2：JAR包启动
java -jar target/ruoyi-admin-wms.jar

# 方式3：后台启动
nohup java -jar target/ruoyi-admin-wms.jar > app.log 2>&1 &
```

### 5.2 前端配置

#### .env.development配置

```env
# 开发环境配置
VITE_APP_TITLE = RuoYi-WMS
VITE_APP_BASE_API = /dev-api
VITE_APP_ENV = development
```

#### .env.production配置

```env
# 生产环境配置
VITE_APP_TITLE = RuoYi-WMS
VITE_APP_BASE_API = /prod-api
VITE_APP_ENV = production
```

#### vite.config.js配置

```javascript
export default defineConfig({
  server: {
    port: 80,
    host: true,
    open: true,
    proxy: {
      '/dev-api': {
        target: 'http://localhost:8180',
        changeOrigin: true,
        rewrite: (p) => p.replace(/^\/dev-api/, '')
      }
    }
  }
})
```

### 5.3 Nginx配置（生产环境）

```nginx
server {
    listen 80;
    server_name localhost;
    
    # 前端静态资源
    location / {
        root /path/to/baiyu-web/dist;
        index index.html;
        try_files $uri $uri/ /index.html;
    }
    
    # 后端API代理
    location /prod-api/ {
        proxy_pass http://localhost:8180/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

---

## ✅ 六、验证与测试

### 6.1 后端验证

#### 1. 检查服务启动

```bash
# 查看日志
tail -f app.log

# 检查端口
netstat -tlnp | grep 8180

# 检查进程
ps -ef | grep ruoyi-admin-wms
```

#### 2. 测试API接口

```bash
# 测试配置查询
curl -X GET "http://localhost:8180/erp/config/get/saleOrder" \
  -H "Authorization: Bearer your_token"

# 测试动态查询
curl -X POST "http://localhost:8180/erp/engine/query/execute" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your_token" \
  -d '{
    "moduleCode": "saleOrder",
    "pageNum": 1,
    "pageSize": 10,
    "queryParams": {},
    "searchConfig": {}
  }'
```

### 6.2 前端验证

#### 1. 访问配置管理页面

```
http://localhost/erp/config
```

**验证点**：
- ✅ 页面正常加载
- ✅ 配置列表显示
- ✅ 新增/编辑/删除功能正常

#### 2. 访问销售订单页面

```
http://localhost/erp/ConfigDrivenPage/saleorder
```

**验证点**：
- ✅ 页面配置正确加载
- ✅ 表格列正确显示
- ✅ 搜索功能正常
- ✅ 工具栏按钮显示正确
- ✅ 权限控制生效

### 6.3 功能测试清单

| 功能 | 测试点 | 预期结果 | 状态 |
|------|--------|---------|------|
| **配置管理** | 新增配置 | 成功保存并显示 | ⬜ |
| | 编辑配置 | 成功更新并记录历史 | ⬜ |
| | 删除配置 | 成功删除并清理关联数据 | ⬜ |
| | 版本回滚 | 成功回滚到历史版本 | ⬜ |
| **动态查询** | 条件查询 | 返回正确数据 | ⬜ |
| | 分页查询 | 分页数据正确 | ⬜ |
| | 排序查询 | 排序结果正确 | ⬜ |
| **表单验证** | 必填验证 | 正确提示必填项 | ⬜ |
| | 格式验证 | 正确验证格式 | ⬜ |
| **审批流程** | 提交审批 | 状态正确变更 | ⬜ |
| | 审批通过 | 流程正确流转 | ⬜ |
| | 审批驳回 | 状态正确回退 | ⬜ |
| **下推功能** | 单据下推 | 正确生成下游单据 | ⬜ |
| | 字段映射 | 字段值正确映射 | ⬜ |

---

## 🔍 七、常见问题

### 7.1 后端问题

#### Q1：找不到ErpPageConfigMapper

**原因**：Mapper未正确扫描

**解决**：
```java
// 在启动类或配置类上添加
@MapperScan("com.ruoyi.**.mapper")
```

#### Q2：配置缓存不生效

**原因**：Redis未正确配置

**解决**：
```yaml
spring:
  redis:
    host: localhost
    port: 6379
    # 确保Redis服务已启动
```

#### Q3：权限验证失败

**原因**：权限标识不匹配

**解决**：
```sql
-- 检查权限配置
SELECT * FROM sys_menu WHERE perms LIKE '%saleorder%';

-- 确保用户有对应权限
SELECT * FROM sys_role_menu WHERE menu_id IN (
  SELECT menu_id FROM sys_menu WHERE perms LIKE '%saleorder%'
);
```

### 7.2 前端问题

#### Q4：页面配置加载失败

**原因**：API路径不正确

**解决**：
```javascript
// 检查API配置
const API_PREFIX = '/erp/engine'  // 确保与后端路径一致

// 检查代理配置
proxy: {
  '/dev-api': {
    target: 'http://localhost:8180',
    changeOrigin: true,
    rewrite: (p) => p.replace(/^\/dev-api/, '')
  }
}
```

#### Q5：字典数据不显示

**原因**：字典接口未实现或配置错误

**解决**：
```javascript
// 检查字典配置
"dictionaryConfig": {
  "documentStatus": [
    { "label": "暂存", "value": "Z", "type": "info" },
    { "label": "创建", "value": "A", "type": "success" }
  ]
}
```

#### Q6：表格列不显示

**原因**：配置中visible属性为false

**解决**：
```json
{
  "prop": "fbillNo",
  "label": "单据编号",
  "visible": true,  // 确保为true
  "width": 150
}
```

### 7.3 数据库问题

#### Q7：外键约束失败

**原因**：关联数据不存在

**解决**：
```sql
-- 先插入主表数据
INSERT INTO erp_page_config (...) VALUES (...);

-- 再插入从表数据
INSERT INTO erp_page_config_history (...) VALUES (...);
```

#### Q8：JSON字段解析失败

**原因**：JSON格式不正确

**解决**：
```sql
-- 验证JSON格式
SELECT JSON_VALID(config_content) FROM erp_page_config;

-- 如果返回0，说明JSON格式错误
-- 使用JSON函数修复
UPDATE erp_page_config 
SET config_content = JSON_OBJECT('key', 'value')
WHERE config_id = 1;
```

---

## 📊 八、迁移检查清单

### 8.1 后端检查清单

- ⬜ 复制ruoyi-erp-api模块完整
- ⬜ 修改父pom.xml添加依赖管理
- ⬜ 修改ruoyi-modules的pom.xml添加模块
- ⬜ 修改ruoyi-admin-wms的pom.xml添加依赖
- ⬜ 检查模块pom.xml的parent配置
- ⬜ 执行Maven编译成功
- ⬜ 检查Mapper扫描配置
- ⬜ 检查数据源配置
- ⬜ 检查Redis配置
- ⬜ 启动后端服务成功

### 8.2 前端检查清单

- ⬜ 复制pageTemplate目录完整
- ⬜ 复制ConfigDrivenPage目录完整
- ⬜ 复制config目录完整
- ⬜ 配置路由正确
- ⬜ 创建API接口文件
- ⬜ 配置代理正确
- ⬜ 安装依赖成功
- ⬜ 编译前端项目成功
- ⬜ 访问页面正常

### 8.3 数据库检查清单

- ⬜ 执行初始化脚本成功
- ⬜ 验证表结构正确
- ⬜ 验证初始数据存在
- ⬜ 验证菜单权限配置
- ⬜ 验证索引创建成功
- ⬜ 验证外键约束生效

### 8.4 功能验证清单

- ⬜ 配置管理功能正常
- ⬜ 动态查询功能正常
- ⬜ 表单验证功能正常
- ⬜ 审批流程功能正常
- ⬜ 下推功能正常
- ⬜ 权限控制生效
- ⬜ 缓存功能正常

---

## 🎯 九、总结

### 迁移完成标志

当以下所有项都完成时，迁移成功：

1. ✅ 后端编译成功，服务正常启动
2. ✅ 前端编译成功，页面正常访问
3. ✅ 数据库初始化成功，数据完整
4. ✅ 所有功能测试通过
5. ✅ 权限控制生效
6. ✅ 性能指标达标

### 预计工时

| 阶段 | 工时 | 说明 |
|------|------|------|
| 后端迁移 | 1小时 | 复制文件、修改配置、编译 |
| 前端迁移 | 1小时 | 复制文件、配置路由、编译 |
| 数据库初始化 | 0.5小时 | 执行脚本、验证数据 |
| 配置与启动 | 0.5小时 | 修改配置、启动服务 |
| 验证与测试 | 1小时 | 功能测试、问题修复 |
| **总计** | **4小时** | 完整迁移流程 |

---

**文档版本**: v1.0  
**最后更新**: 2026-03-24  
**维护人员**: ERP开发团队

🎯