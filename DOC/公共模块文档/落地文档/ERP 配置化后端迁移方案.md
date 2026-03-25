# 🚀 ERP 配置化后端模块迁移方案

**版本**: v1.0  
**日期**: 2026-03-24  
**目标**: 将 ruoyi-system 中的配置化功能迁移到独立的 ruoyi-erp-api 模块  
**预计工时**: 1 个工作日（7 小时）

---

## 📋 目录

1. [迁移目标](#迁移目标)
2. [现状分析](#现状分析)
3. [迁移策略](#迁移策略)
4. [实施步骤](#实施步骤)
5. [文件清单](#文件清单)
6. [自动化脚本](#自动化脚本)
7. [测试验证](#测试验证)
8. [回滚方案](#回滚方案)

---

## 🎯 迁移目标

### **核心目标**

✅ **模块化** - 创建独立的 ruoyi-erp-api Maven 模块  
✅ **无依赖** - 最小化外部依赖，仅依赖框架核心  
✅ **高内聚** - ERP 配置化功能完整集中在一个模块  
✅ **低耦合** - 与 ruoyi-system 解耦，独立演进  
✅ **可复用** - 其他项目可直接引用该模块  

### **成功标准**

- [ ] 所有文件成功迁移到新模块
- [ ] 包名从 `com.ruoyi.system.*` 改为 `com.ruoyi.erp.*`
- [ ] 编译通过，无错误无警告
- [ ] 所有接口正常运行
- [ ] 前端页面正常访问
- [ ] 单元测试通过率 100%

---

## 📊 现状分析

### **当前架构**

```
ruoyi-system (原模块)
├── controller/erp/              (4 个 Controller, 1764 行)
├── service/
│   ├── engine/                  (4 个引擎，1321 行)
│   └── impl/                    (4 个实现，980 行)
├── mapper/                      (5 个 Mapper)
└── domain/
    ├── entity/                  (5 个实体)
    ├── bo/                      (5 个 BO)
    └── vo/                      (5 个 VO)
```

### **依赖关系图**

```
ruoyi-admin-wms (主启动模块)
    ↓
ruoyi-modules/ruoyi-system (系统模块)
    ↓
├── ruoyi-common-core (核心工具)
├── ruoyi-common-security (安全组件)
├── mybatis-plus (ORM 框架)
├── sa-token (权限认证)
└── mysql-connector (数据库驱动)
```

### **问题识别**

🔴 **严重问题**
- ❌ 缺少 3 个关键 API 接口（字典、明细、成本）
- ⚠️ PushDownEngine 被注释但未说明原因

🟡 **中等问题**
- 📝 文档与实际代码存在差异
- 🔧 SuperDataPermissionService 可能依赖 system 模块的表

🟢 **轻微问题**
- 📦 包命名不够清晰（system 包含 ERP 功能）

---

## 🎨 迁移策略

### **方案选择：完全独立模块（方案 A）**

**理由：**
1. ✅ 符合微服务架构设计理念
2. ✅ 便于后续功能扩展和版本管理
3. ✅ 可以独立发布和部署
4. ✅ 降低系统模块复杂度
5. ✅ 提高代码复用性

### **模块定位**

```xml
<!-- 新模块在 Maven 体系中的位置 -->
<modules>
    <module>ruoyi-common</module>
    <module>ruoyi-admin-wms</module>
    <module>ruoyi-modules</module>
    <!-- 新增 -->
    <module>ruoyi-modules/ruoyi-erp-api</module>
</modules>
```

### **依赖关系重构**

**迁移前：**
```
ruoyi-admin-wms → ruoyi-system (包含 ERP 功能)
```

**迁移后：**
```
ruoyi-admin-wms → ruoyi-erp-api (独立 ERP 模块)
               ↘ ruoyi-system (纯系统功能)
```

---

## 🔧 实施步骤

### **Phase 1: 准备工作（0.5 小时）**

#### **Step 1.1: 创建模块目录结构**

```powershell
# 工作目录
$BASE_DIR = "d:\baiyuyunma\gitee-baiyu\baiyu-ruoyi"
$MODULE_DIR = "$BASE_DIR\ruoyi-modules\ruoyi-erp-api"

# 创建目录结构
New-Item -ItemType Directory -Force -Path "$MODULE_DIR\src\main\java\com\ruoyi\erp\controller\erp"
New-Item -ItemType Directory -Force -Path "$MODULE_DIR\src\main\java\com\ruoyi\erp\service\engine"
New-Item -ItemType Directory -Force -Path "$MODULE_DIR\src\main\java\com\ruoyi\erp\service\impl"
New-Item -ItemType Directory -Force -Path "$MODULE_DIR\src\main\java\com\ruoyi\erp\mapper"
New-Item -ItemType Directory -Force -Path "$MODULE_DIR\src\main\java\com\ruoyi\erp\domain\entity"
New-Item -ItemType Directory -Force -Path "$MODULE_DIR\src\main\java\com\ruoyi\erp\domain\bo"
New-Item -ItemType Directory -Force -Path "$MODULE_DIR\src\main\java\com\ruoyi\erp\domain\vo"
New-Item -ItemType Directory -Force -Path "$MODULE_DIR\src\test\java\com\ruoyi\erp"
New-Item -ItemType Directory -Force -Path "$MODULE_DIR\src\main\resources\mapper"
```

#### **Step 1.2: 创建 pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>com.ruoyi</groupId>
        <artifactId>ruoyi-modules</artifactId>
        <version>3.6.3</version>
    </parent>
    
    <artifactId>ruoyi-erp-api</artifactId>
    <name>ruoyi-erp-api</name>
    <description>ERP 配置化 API 模块 - 独立模块</description>
    
    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
    </properties>
    
    <dependencies>
        <!-- 框架核心依赖 -->
        <dependency>
            <groupId>com.ruoyi</groupId>
            <artifactId>ruoyi-common-core</artifactId>
        </dependency>
        
        <dependency>
            <groupId>com.ruoyi</groupId>
            <artifactId>ruoyi-common-security</artifactId>
        </dependency>
        
        <dependency>
            <groupId>com.ruoyi</groupId>
            <artifactId>ruoyi-common-log</artifactId>
        </dependency>
        
        <!-- MyBatis Plus -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
        </dependency>
        
        <!-- Sa-Token 权限认证 -->
        <dependency>
            <groupId>cn.dev33</groupId>
            <artifactId>sa-token-spring-boot3-starter</artifactId>
        </dependency>
        
        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <scope>provided</scope>
        </dependency>
        
        <!-- MapStruct (可选，用于对象转换) -->
        <dependency>
            <groupId>io.github.linpeilie</groupId>
            <artifactId>mapstruct-spring-annotations</artifactId>
            <optional>true</optional>
        </dependency>
        
        <!-- 数据库驱动（测试用） -->
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
        
        <!-- Spring Boot Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <source>17</source>
                    <target>17</target>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

---

### **Phase 2: 文件迁移（2 小时）**

#### **Step 2.1: 迁移 Controller 层**

**源目录**: `ruoyi-modules/ruoyi-system/src/main/java/com/ruoyi/system/controller/erp/`  
**目标目录**: `ruoyi-modules/ruoyi-erp-api/src/main/java/com/ruoyi/erp/controller/erp/`

**需要迁移的文件：**
1. ✅ `ErpEngineController.java` (1244 行) - **核心**
2. ✅ `ErpPageConfigController.java` (256 行)
3. ✅ `ErpApprovalFlowController.java` (132 行)
4. ✅ `ErpPushRelationController.java` (132 行)

**修改内容：**
```java
// 修改前
package com.ruoyi.system.controller.erp;
import com.ruoyi.system.service.engine.DynamicQueryEngine;
import com.ruoyi.system.service.ErpApprovalFlowService;

// 修改后
package com.ruoyi.erp.controller.erp;
import com.ruoyi.erp.service.engine.DynamicQueryEngine;
import com.ruoyi.erp.service.ErpApprovalFlowService;
```

#### **Step 2.2: 迁移 Service 层（引擎）**

**源目录**: `ruoyi-modules/ruoyi-system/src/main/java/com/ruoyi/system/service/engine/`  
**目标目录**: `ruoyi-modules/ruoyi-erp-api/src/main/java/com/ruoyi/erp/service/engine/`

**需要迁移的文件：**
1. ✅ `DynamicQueryEngine.java` (255 行)
2. ✅ `FormValidationEngine.java` (230 行)
3. ✅ `ApprovalWorkflowEngine.java` (458 行)
4. ✅ `PushDownEngine.java` (378 行) - 标注为"暂未使用"

**修改内容：**
```java
// 修改前
package com.ruoyi.system.service.engine;

// 修改后
package com.ruoyi.erp.service.engine;
```

#### **Step 2.3: 迁移 Service 层（接口和实现）**

**源目录**: `ruoyi-modules/ruoyi-system/src/main/java/com/ruoyi/system/service/`  
**目标目录**: `ruoyi-modules/ruoyi-erp-api/src/main/java/com/ruoyi/erp/service/`

**接口文件：**
1. ✅ `ErpApprovalFlowService.java`
2. ✅ `ErpPageConfigService.java`
3. ✅ `ErpPushRelationService.java`
4. ✅ `ISuperDataPermissionService.java`

**实现文件：**
1. ✅ `ErpApprovalFlowServiceImpl.java`
2. ✅ `ErpPageConfigServiceImpl.java`
3. ✅ `ErpPushRelationServiceImpl.java`
4. ✅ `SuperDataPermissionServiceImpl.java`

**修改内容：**
```java
// 修改前
package com.ruoyi.system.service;
import com.ruoyi.system.domain.entity.ErpPageConfig;

// 修改后
package com.ruoyi.erp.service;
import com.ruoyi.erp.domain.entity.ErpPageConfig;
```

#### **Step 2.4: 迁移 Mapper 层**

**源目录**: `ruoyi-modules/ruoyi-system/src/main/java/com/ruoyi/system/mapper/`  
**目标目录**: `ruoyi-modules/ruoyi-erp-api/src/main/java/com/ruoyi/erp/mapper/`

**需要迁移的文件：**
1. ✅ `ErpPageConfigMapper.java`
2. ✅ `ErpPageConfigHistoryMapper.java`
3. ✅ `ErpPushRelationMapper.java`
4. ✅ `ErpApprovalFlowMapper.java`
5. ✅ `ErpApprovalHistoryMapper.java`

**修改内容：**
```java
// 修改前
package com.ruoyi.system.mapper;
import com.ruoyi.system.domain.entity.ErpPageConfig;

// 修改后
package com.ruoyi.erp.mapper;
import com.ruoyi.erp.domain.entity.ErpPageConfig;
```

#### **Step 2.5: 迁移 Domain 层**

**Entity 实体类：**
```
源：ruoyi-modules/ruoyi-system/src/main/java/com/ruoyi/system/domain/entity/
目标：ruoyi-modules/ruoyi-erp-api/src/main/java/com/ruoyi/erp/domain/entity/

文件列表：
1. ErpPageConfig.java (68 行)
2. ErpPageConfigHistory.java (52 行)
3. ErpPushRelation.java (72 行)
4. ErpApprovalFlow.java (52 行)
5. ErpApprovalHistory.java (52 行)
```

**BO 业务对象：**
```
源：ruoyi-modules/ruoyi-system/src/main/java/com/ruoyi/system/domain/bo/
目标：ruoyi-modules/ruoyi-erp-api/src/main/java/com/ruoyi/erp/domain/bo/

文件列表：
1. ErpPageConfigBo.java (58 行)
2. ErpPushRelationBo.java (52 行)
3. ErpApprovalFlowBo.java (48 行)
```

**VO 视图对象：**
```
源：ruoyi-modules/ruoyi-system/src/main/java/com/ruoyi/system/domain/vo/
目标：ruoyi-modules/ruoyi-erp-api/src/main/java/com/ruoyi/erp/domain/vo/

文件列表：
1. ErpPageConfigVo.java (68 行)
2. ErpPushRelationVo.java (78 行)
3. ErpApprovalFlowVo.java (58 行)
```

---

### **Phase 3: 补充缺失接口（1 小时）**

#### **Step 3.1: 添加字典数据接口**

在 `ErpEngineController.java` 中添加：

```java
/**
 * 获取字典数据
 * ✅ 新增接口 - 解决前端字典数据加载问题
 */
@GetMapping("/dictionary/{dictType}")
public R<?> getDictionary(@PathVariable String dictType,
                          @RequestParam String moduleCode) {
    try {
        checkModulePermission(moduleCode, "query");
        
        // 方案 1: 调用现有的 DictionaryTableService（如果可用）
        // List<DictionaryTable> dictList = dictionaryTableService.selectByDictType(dictType);
        
        // 方案 2: 自己实现字典查询（推荐）
        List<Map<String, Object>> dictData = queryDictionaryData(dictType, moduleCode);
        
        return R.ok(dictData);
    } catch (Exception e) {
        log.error("获取字典数据失败，dictType: {}, moduleCode: {}", dictType, moduleCode, e);
        return R.fail("获取字典数据失败：" + e.getMessage());
    }
}

/**
 * 查询字典数据的辅助方法
 */
private List<Map<String, Object>> queryDictionaryData(String dictType, String moduleCode) {
    // TODO: 实现字典查询逻辑
    // 可以从 bymaterial_dictionary 表或 sys_dict_data 表查询
    return new ArrayList<>();
}
```

#### **Step 3.2: 添加自定义查询接口**

```java
/**
 * 获取订单明细数据
 * ✅ 新增接口 - 解决前端展开行/抽屉明细显示问题
 */
@GetMapping("/custom/entry")
public R<?> getEntryData(@RequestParam String moduleCode,
                         @RequestParam String billNo) {
    try {
        checkModulePermission(moduleCode, "query");
        
        // 调用数据权限服务查询明细
        List<Map<String, Object>> entryList = 
            dataPermissionService.getRelatedData(moduleCode, "entry", billNo);
        
        return R.ok(entryList);
    } catch (Exception e) {
        log.error("获取明细数据失败，moduleCode: {}, billNo: {}", moduleCode, billNo, e);
        return R.fail("获取明细数据失败：" + e.getMessage());
    }
}

/**
 * 获取成本数据
 * ✅ 新增接口 - 解决前端成本暂估数据显示问题
 */
@GetMapping("/custom/cost")
public R<?> getCostData(@RequestParam String moduleCode,
                        @RequestParam String billNo) {
    try {
        checkModulePermission(moduleCode, "query");
        
        // 调用数据权限服务查询成本
        Object costData = dataPermissionService.getRelatedData(moduleCode, "cost", billNo);
        
        return R.ok(costData);
    } catch (Exception e) {
        log.error("获取成本数据失败，moduleCode: {}, billNo: {}", moduleCode, billNo, e);
        return R.fail("获取成本数据失败：" + e.getMessage());
    }
}
```

---

### **Phase 4: 配置文件更新（0.5 小时）**

#### **Step 4.1: 更新父 pom.xml**

编辑 `baiyu-ruoyi/pom.xml`，添加新模块：

```xml
<modules>
    <module>ruoyi-common</module>
    <module>ruoyi-admin-wms</module>
    <module>ruoyi-modules</module>
    <!-- 新增 ERP 配置化模块 -->
    <module>ruoyi-modules/ruoyi-erp-api</module>
</modules>
```

#### **Step 4.2: 更新 ruoyi-admin-wms 的 pom.xml**

编辑 `ruoyi-admin-wms/pom.xml`，添加依赖：

```xml
<dependencies>
    <!-- 现有依赖保持不变 -->
    
    <!-- 新增：ERP 配置化模块依赖 -->
    <dependency>
        <groupId>com.ruoyi</groupId>
        <artifactId>ruoyi-erp-api</artifactId>
        <version>${revision}</version>
    </dependency>
</dependencies>
```

#### **Step 4.3: 更新 application.yml**

编辑 `ruoyi-admin-wms/src/main/resources/application.yml`：

```yaml
# MyBatis Plus 配置
mybatis-plus:
  # Mapper XML 扫描路径（添加新的 mapper 路径）
  mapper-locations: classpath*:mapper/**/*Mapper.xml,classpath*:/erp/mapper/**/*Mapper.xml
  
  # 类型别名扫描包（添加新的包）
  typeAliasesPackage: com.ruoyi.**.domain,com.ruoyi.erp.domain
  
  configuration:
    map-underscore-to-camel-case: true
    cache-enabled: false
```

---

### **Phase 5: 编译测试（1.5 小时）**

#### **Step 5.1: 清理旧的编译缓存**

```powershell
cd d:\baiyuyunma\gitee-baiyu\baiyu-ruoyi

# 清理所有 target 目录
Get-ChildItem -Recurse -Filter "target" | Remove-Item -Recurse -Force

# 清理 Maven 缓存
mvn clean
```

#### **Step 5.2: 编译新模块**

```powershell
cd ruoyi-modules\ruoyi-erp-api

# 编译模块
mvn clean compile

# 安装到本地仓库
mvn install
```

#### **Step 5.3: 编译主项目**

```powershell
cd ..\..\ruoyi-admin-wms

# 清理并重新编译
mvn clean compile

# 打包（可选）
mvn package -DskipTests
```

#### **Step 5.4: 运行测试**

```powershell
# 运行单元测试
mvn test

# 或者跳过测试直接启动
mvn spring-boot:run -DskipTests
```

---

### **Phase 6: 功能验证（1 小时）**

#### **Step 6.1: 接口测试清单**

| 接口类别 | 接口路径 | 测试状态 | 备注 |
|---------|---------|---------|------|
| **动态查询** | POST /erp/engine/query/execute | ⬜ | 测试查询销售订单 |
| **表单验证** | POST /erp/engine/validation/execute | ⬜ | 测试表单验证 |
| **审批流程** | POST /erp/engine/approval/execute | ⬜ | 测试审批操作 |
| **下推操作** | POST /erp/engine/push/execute | ⬜ | 测试下推功能 |
| **字典数据** | GET /erp/engine/dictionary/{dictType} | ⬜ | **新增接口** |
| **明细查询** | GET /erp/engine/custom/entry | ⬜ | **新增接口** |
| **成本查询** | GET /erp/engine/custom/cost | ⬜ | **新增接口** |
| **页面配置** | GET /erp/config/list | ⬜ | 测试配置查询 |
| **审批配置** | GET /erp/approval/list | ⬜ | 测试审批配置 |
| **下推配置** | GET /erp/push/list | ⬜ | 测试下推配置 |

#### **Step 6.2: 前端页面测试**

1. ⬜ 访问销售订单列表页
2. ⬜ 测试查询功能
3. ⬜ 测试新增/编辑功能
4. ⬜ 测试审核功能
5. ⬜ 测试下推功能
6. ⬜ 测试展开行明细
7. ⬜ 测试抽屉详情
8. ⬜ 测试字典数据加载

---

## 📦 文件清单

### **完整迁移文件列表（共 31 个文件）**

#### **Controller 层（4 个文件）**

| # | 文件名 | 行数 | 新路径 | 优先级 |
|---|-------|------|-------|--------|
| 1 | ErpEngineController.java | 1244 | `com.ruoyi.erp.controller.erp` | P0 |
| 2 | ErpPageConfigController.java | 256 | `com.ruoyi.erp.controller.erp` | P1 |
| 3 | ErpApprovalFlowController.java | 132 | `com.ruoyi.erp.controller.erp` | P1 |
| 4 | ErpPushRelationController.java | 132 | `com.ruoyi.erp.controller.erp` | P1 |

#### **Service 引擎层（4 个文件）**

| # | 文件名 | 行数 | 新路径 | 优先级 |
|---|-------|------|-------|--------|
| 5 | DynamicQueryEngine.java | 255 | `com.ruoyi.erp.service.engine` | P0 |
| 6 | FormValidationEngine.java | 230 | `com.ruoyi.erp.service.engine` | P0 |
| 7 | ApprovalWorkflowEngine.java | 458 | `com.ruoyi.erp.service.engine` | P0 |
| 8 | PushDownEngine.java | 378 | `com.ruoyi.erp.service.engine` | P2 |

#### **Service 接口层（4 个文件）**

| # | 文件名 | 行数 | 新路径 | 优先级 |
|---|-------|------|-------|--------|
| 9 | ErpApprovalFlowService.java | 48 | `com.ruoyi.erp.service` | P1 |
| 10 | ErpPageConfigService.java | 98 | `com.ruoyi.erp.service` | P1 |
| 11 | ErpPushRelationService.java | 58 | `com.ruoyi.erp.service` | P1 |
| 12 | ISuperDataPermissionService.java | 65 | `com.ruoyi.erp.service` | P1 |

#### **Service 实现层（4 个文件）**

| # | 文件名 | 行数 | 新路径 | 优先级 |
|---|-------|------|-------|--------|
| 13 | ErpApprovalFlowServiceImpl.java | 132 | `com.ruoyi.erp.service.impl` | P1 |
| 14 | ErpPageConfigServiceImpl.java | 456 | `com.ruoyi.erp.service.impl` | P1 |
| 15 | ErpPushRelationServiceImpl.java | 132 | `com.ruoyi.erp.service.impl` | P1 |
| 16 | SuperDataPermissionServiceImpl.java | 234 | `com.ruoyi.erp.service.impl` | P1 |

#### **Mapper 层（5 个文件）**

| # | 文件名 | 行数 | 新路径 | 优先级 |
|---|-------|------|-------|--------|
| 17 | ErpPageConfigMapper.java | 12 | `com.ruoyi.erp.mapper` | P1 |
| 18 | ErpPageConfigHistoryMapper.java | 12 | `com.ruoyi.erp.mapper` | P2 |
| 19 | ErpPushRelationMapper.java | 12 | `com.ruoyi.erp.mapper` | P1 |
| 20 | ErpApprovalFlowMapper.java | 12 | `com.ruoyi.erp.mapper` | P1 |
| 21 | ErpApprovalHistoryMapper.java | 12 | `com.ruoyi.erp.mapper` | P1 |

#### **Domain Entity 层（5 个文件）**

| # | 文件名 | 行数 | 新路径 | 优先级 |
|---|-------|------|-------|--------|
| 22 | ErpPageConfig.java | 68 | `com.ruoyi.erp.domain.entity` | P1 |
| 23 | ErpPageConfigHistory.java | 52 | `com.ruoyi.erp.domain.entity` | P2 |
| 24 | ErpPushRelation.java | 72 | `com.ruoyi.erp.domain.entity` | P1 |
| 25 | ErpApprovalFlow.java | 52 | `com.ruoyi.erp.domain.entity` | P1 |
| 26 | ErpApprovalHistory.java | 52 | `com.ruoyi.erp.domain.entity` | P1 |

#### **Domain BO 层（3 个文件）**

| # | 文件名 | 行数 | 新路径 | 优先级 |
|---|-------|------|-------|--------|
| 27 | ErpPageConfigBo.java | 58 | `com.ruoyi.erp.domain.bo` | P1 |
| 28 | ErpPushRelationBo.java | 52 | `com.ruoyi.erp.domain.bo` | P1 |
| 29 | ErpApprovalFlowBo.java | 48 | `com.ruoyi.erp.domain.bo` | P1 |

#### **Domain VO 层（3 个文件）**

| # | 文件名 | 行数 | 新路径 | 优先级 |
|---|-------|------|-------|--------|
| 30 | ErpPageConfigVo.java | 68 | `com.ruoyi.erp.domain.vo` | P1 |
| 31 | ErpPushRelationVo.java | 78 | `com.ruoyi.erp.domain.vo` | P1 |
| 32 | ErpApprovalFlowVo.java | 58 | `com.ruoyi.erp.domain.vo` | P1 |

**总计：32 个文件，约 5,500 行代码**

---

## 🤖 自动化脚本

### **迁移脚本：migrate-erp-backend.ps1**

```powershell
# ================================================================================================
# ERP 配置化后端模块迁移脚本
# 功能：自动化完成所有文件的迁移和包名重构
# 作者：ERP Development Team
# 日期：2026-03-24
# ================================================================================================

[CmdletBinding()]
param(
    [Parameter(Mandatory=$false)]
    [string]$SourceBase = "d:\baiyuyunma\gitee-baiyu\baiyu-ruoyi",
    
    [Parameter(Mandatory=$false)]
    [switch]$SkipBackup = $false,
    
    [Parameter(Mandatory=$false)]
    [switch]$DryRun = $false
)

$ErrorActionPreference = "Stop"
$startTime = Get-Date

Write-Host "================================================================================" -ForegroundColor Cyan
Write-Host "  ERP 配置化后端模块迁移脚本" -ForegroundColor Cyan
Write-Host "  开始时间：$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Cyan
Write-Host "================================================================================" -ForegroundColor Cyan

try {
    # ==================== Step 1: 验证环境 ====================
    Write-Host "`n[1/8] 验证环境..." -ForegroundColor Yellow
    
    if (-not (Test-Path "$SourceBase\ruoyi-modules\ruoyi-system")) {
        throw "未找到源模块目录：$SourceBase\ruoyi-modules\ruoyi-system"
    }
    
    if (-not (Test-Path "$SourceBase\pom.xml")) {
        throw "未找到父项目 pom.xml"
    }
    
    Write-Host "✓ 环境验证通过" -ForegroundColor Green
    
    # ==================== Step 2: 创建备份 ====================
    Write-Host "`n[2/8] 创建备份..." -ForegroundColor Yellow
    
    if (-not $SkipBackup) {
        $backupDir = "$SourceBase\backup-erp-migration-$(Get-Date -Format 'yyyyMMdd-HHmmss')"
        New-Item -ItemType Directory -Force -Path $backupDir | Out-Null
        
        # 备份源目录
        $sourceDirs = @(
            "ruoyi-modules\ruoyi-system\src\main\java\com\ruoyi\system\controller\erp",
            "ruoyi-modules\ruoyi-system\src\main\java\com\ruoyi\system\service\engine",
            "ruoyi-modules\ruoyi-system\src\main\java\com\ruoyi\system\service\impl",
            "ruoyi-modules\ruoyi-system\src\main\java\com\ruoyi\system\mapper"
        )
        
        foreach ($dir in $sourceDirs) {
            $fullPath = "$SourceBase\$dir"
            if (Test-Path $fullPath) {
                $destPath = $fullPath.Replace("ruoyi-system", "backup-erp")
                Copy-Item -Path $fullPath -Destination $destPath -Recurse -Force
                Write-Host "  ✓ 已备份：$dir" -ForegroundColor Green
            }
        }
        
        Write-Host "✓ 备份完成：$backupDir" -ForegroundColor Green
    } else {
        Write-Host "⊘ 跳过备份（用户指定）" -ForegroundColor Gray
    }
    
    # ==================== Step 3: 创建新模块目录 ====================
    Write-Host "`n[3/8] 创建新模块目录..." -ForegroundColor Yellow
    
    $newModuleDir = "$SourceBase\ruoyi-modules\ruoyi-erp-api"
    
    if (Test-Path $newModuleDir) {
        Write-Host "⚠ 警告：新模块目录已存在，将覆盖" -ForegroundColor Yellow
    }
    
    $directories = @(
        "src\main\java\com\ruoyi\erp\controller\erp",
        "src\main\java\com\ruoyi\erp\service\engine",
        "src\main\java\com\ruoyi\erp\service\impl",
        "src\main\java\com\ruoyi\erp\mapper",
        "src\main\java\com\ruoyi\erp\domain\entity",
        "src\main\java\com\ruoyi\erp\domain\bo",
        "src\main\java\com\ruoyi\erp\domain\vo",
        "src\test\java\com\ruoyi\erp",
        "src\main\resources\mapper"
    )
    
    foreach ($dir in $directories) {
        $fullPath = Join-Path $newModuleDir $dir
        New-Item -ItemType Directory -Force -Path $fullPath | Out-Null
        Write-Host "  ✓ 创建：$dir" -ForegroundColor Green
    }
    
    Write-Host "✓ 目录创建完成" -ForegroundColor Green
    
    # ==================== Step 4: 复制文件 ====================
    Write-Host "`n[4/8] 复制文件..." -ForegroundColor Yellow
    
    $fileMappings = @{
        "ruoyi-modules\ruoyi-system\src\main\java\com\ruoyi\system\controller\erp" = 
            "ruoyi-modules\ruoyi-erp-api\src\main\java\com\ruoyi\erp\controller\erp"
        
        "ruoyi-modules\ruoyi-system\src\main\java\com\ruoyi\system\service\engine" = 
            "ruoyi-modules\ruoyi-erp-api\src\main\java\com\ruoyi\erp\service\engine"
        
        "ruoyi-modules\ruoyi-system\src\main\java\com\ruoyi\system\service\impl" = 
            "ruoyi-modules\ruoyi-erp-api\src\main\java\com\ruoyi\erp\service\impl"
        
        "ruoyi-modules\ruoyi-system\src\main\java\com\ruoyi\system\mapper" = 
            "ruoyi-modules\ruoyi-erp-api\src\main\java\com\ruoyi\erp\mapper"
        
        "ruoyi-modules\ruoyi-system\src\main\java\com\ruoyi\system\domain\entity" = 
            "ruoyi-modules\ruoyi-erp-api\src\main\java\com\ruoyi\erp\domain\entity"
        
        "ruoyi-modules\ruoyi-system\src\main\java\com\ruoyi\system\domain\bo" = 
            "ruoyi-modules\ruoyi-erp-api\src\main\java\com\ruoyi\erp\domain\bo"
        
        "ruoyi-modules\ruoyi-system\src\main\java\com\ruoyi\system\domain\vo" = 
            "ruoyi-modules\ruoyi-erp-api\src\main\java\com\ruoyi\erp\domain\vo"
    }
    
    $fileCount = 0
    
    foreach ($pair in $fileMappings.GetEnumerator()) {
        $sourceDir = "$SourceBase\$($pair.Key)"
        $targetDir = "$SourceBase\$($pair.Value)"
        
        if (Test-Path $sourceDir) {
            Get-ChildItem -Path $sourceDir -Filter "*.java" | ForEach-Object {
                if (-not $DryRun) {
                    Copy-Item -Path $_.FullName -Destination $targetDir -Force
                    $fileCount++
                    Write-Host "  ✓ 复制：$($_.Name)" -ForegroundColor Green
                } else {
                    Write-Host "  ⊘ [DryRun] 将复制：$($_.Name)" -ForegroundColor Gray
                }
            }
        }
    }
    
    Write-Host "✓ 文件复制完成，共 $fileCount 个文件" -ForegroundColor Green
    
    # ==================== Step 5: 重构包名 ====================
    Write-Host "`n[5/8] 重构包名..." -ForegroundColor Yellow
    
    $javaFiles = Get-ChildItem -Path "$newModuleDir\src" -Filter "*.java" -Recurse
    $refactoredCount = 0
    
    foreach ($file in $javaFiles) {
        if (-not $DryRun) {
            $content = Get-Content $file.FullName -Raw -Encoding UTF8
            
            # 替换 package 声明
            $content = $content -replace 'package com\.ruoyi\.system\.', 'package com.ruoyi.erp.'
            
            # 替换 import 语句
            $content = $content -replace 'import com\.ruoyi\.system\.', 'import com.ruoyi.erp.'
            
            # 保存文件
            Set-Content -Path $file.FullName -Value $content -Encoding UTF8 -NoNewline
            
            $refactoredCount++
            Write-Host "  ✓ 重构：$($file.Name)" -ForegroundColor Green
        } else {
            Write-Host "  ⊘ [DryRun] 将重构：$($file.Name)" -ForegroundColor Gray
            $refactoredCount++
        }
    }
    
    Write-Host "✓ 包名重构完成，共 $refactoredCount 个文件" -ForegroundColor Green
    
    # ==================== Step 6: 创建 pom.xml ====================
    Write-Host "`n[6/8] 创建 pom.xml..." -ForegroundColor Yellow
    
    if (-not $DryRun) {
        $pomContent = @"
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    
    <parent>
        <groupId>com.ruoyi</groupId>
        <artifactId>ruoyi-modules</artifactId>
        <version>3.6.3</version>
    </parent>
    
    <artifactId>ruoyi-erp-api</artifactId>
    <name>ruoyi-erp-api</name>
    <description>ERP 配置化 API 模块</description>
    
    <dependencies>
        <!-- 框架核心 -->
        <dependency>
            <groupId>com.ruoyi</groupId>
            <artifactId>ruoyi-common-core</artifactId>
        </dependency>
        
        <dependency>
            <groupId>com.ruoyi</groupId>
            <artifactId>ruoyi-common-security</artifactId>
        </dependency>
        
        <!-- MyBatis Plus -->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
        </dependency>
        
        <!-- Sa-Token -->
        <dependency>
            <groupId>cn.dev33</groupId>
            <artifactId>sa-token-spring-boot3-starter</artifactId>
        </dependency>
        
        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
        </dependency>
    </dependencies>
</project>
"@
        
        Set-Content -Path "$newModuleDir\pom.xml" -Value $pomContent -Encoding UTF8
        Write-Host "✓ pom.xml 创建完成" -ForegroundColor Green
    } else {
        Write-Host "  ⊘ [DryRun] 将创建 pom.xml" -ForegroundColor Gray
    }
    
    # ==================== Step 7: 编译测试 ====================
    Write-Host "`n[7/8] 编译测试..." -ForegroundColor Yellow
    
    if (-not $DryRun) {
        Set-Location "$newModuleDir"
        
        Write-Host "  正在执行 mvn clean compile..." -ForegroundColor Cyan
        mvn clean compile
        
        if ($LASTEXITCODE -eq 0) {
            Write-Host "✓ 编译成功" -ForegroundColor Green
        } else {
            Write-Host "✗ 编译失败，请检查错误信息" -ForegroundColor Red
            throw "编译失败"
        }
        
        Set-Location "$SourceBase"
    } else {
        Write-Host "  ⊘ [DryRun] 将执行编译测试" -ForegroundColor Gray
    }
    
    # ==================== Step 8: 生成报告 ====================
    Write-Host "`n[8/8] 生成迁移报告..." -ForegroundColor Yellow
    
    $endTime = Get-Date
    $duration = New-TimeSpan -Start $startTime -End $endTime
    
    $report = @"
================================================================================
  ERP 配置化后端模块迁移报告
================================================================================

迁移时间：$(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')
持续时间：$($duration.Minutes) 分钟 $($duration.Seconds) 秒
迁移模式：$(if ($DryRun) {"干跑模式"} else {"正式迁移"})

文件统计:
  - 迁移文件数：$refactoredCount 个
  - 新增目录数：$directories.Count 个
  - 代码行数：约 5,500 行

新模块位置：$newModuleDir

下一步操作:
  1. 检查编译输出，确认无错误
  2. 运行单元测试验证功能
  3. 启动项目进行集成测试
  4. 更新相关文档

================================================================================
"@
    
    Write-Host $report -ForegroundColor Cyan
    
    # 保存报告
    $reportFile = "$SourceBase\MIGRATION-REPORT-$(Get-Date -Format 'yyyyMMdd-HHmmss').txt"
    Set-Content -Path $reportFile -Value $report -Encoding UTF8
    Write-Host "✓ 报告已保存至：$reportFile" -ForegroundColor Green
    
    Write-Host "`n================================================================================" -ForegroundColor Cyan
    Write-Host "  ✓ 迁移完成！" -ForegroundColor Green
    Write-Host "================================================================================" -ForegroundColor Cyan
    
} catch {
    Write-Host "`n================================================================================" -ForegroundColor Red
    Write-Host "  ✗ 迁移失败！" -ForegroundColor Red
    Write-Host "================================================================================" -ForegroundColor Red
    Write-Host "错误信息：$_" -ForegroundColor Red
    Write-Host "错误位置：$($_.InvocationInfo.ScriptName):$($_.InvocationInfo.ScriptLineNumber)" -ForegroundColor Red
    
    if (-not $SkipBackup) {
        Write-Host "`n提示：备份文件位于 $backupDir，可以手动恢复" -ForegroundColor Yellow
    }
    
    throw
} finally {
    Write-Host "`n脚本执行结束" -ForegroundColor Gray
}
```

---

## 🧪 测试验证

### **单元测试用例**

创建测试文件：`src/test/java/com/ruoyi/erp/controller/ErpEngineControllerTest.java`

```java
package com.ruoyi.erp.controller;

import com.ruoyi.common.core.domain.R;
import com.ruoyi.erp.controller.erp.ErpEngineController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ErpEngineControllerTest {
    
    @Autowired
    private ErpEngineController erpEngineController;
    
    @Test
    public void testExecuteDynamicQuery() {
        Map<String, Object> params = new HashMap<>();
        params.put("moduleCode", "saleOrder");
        params.put("pageNum", 1);
        params.put("pageSize", 10);
        
        R<?> result = erpEngineController.executeDynamicQuery(params);
        
        assertNotNull(result);
        assertEquals(200, result.getCode());
    }
    
    @Test
    public void testGetDictionary() {
        R<?> result = erpEngineController.getDictionary("currency", "saleOrder");
        
        assertNotNull(result);
        assertTrue(((Map<?, ?>) result.getData()).size() > 0);
    }
}
```

---

## 🔄 回滚方案

### **回滚步骤**

如果迁移失败，执行以下回滚操作：

```powershell
# Step 1: 停止应用
Stop-Process -Name "java" -Force -ErrorAction SilentlyContinue

# Step 2: 删除新模块
Remove-Item -Path "d:\baiyuyunma\gitee-baiyu\baiyu-ruoyi\ruoyi-modules\ruoyi-erp-api" -Recurse -Force

# Step 3: 恢复备份
$backupDir = Get-ChildItem "d:\baiyuyunma\gitee-baiyu\baiyu-ruoyi\backup-erp-*" | 
             Sort-Object LastWriteTime -Descending | 
             Select-Object -First 1

if ($backupDir) {
    Copy-Item -Path "$($backupDir.FullName)\*" -Destination "d:\baiyuyunma\gitee-baiyu\baiyu-ruoyi\ruoyi-modules\ruoyi-system" -Recurse -Force
    Write-Host "✓ 已恢复到备份状态" -ForegroundColor Green
}

# Step 4: 清理
mvn clean
```

---

## 📚 相关文档

- [ERP 配置化方案 - 后端开发指南.md](./ERP 配置化方案 - 后端开发指南.md)
- [ERP 通用引擎 API 使用指南.md](../ERP 通用引擎 API 使用指南.md)
- [配置化页面后端 API 体系](./配置化页面后端 API 体系.md)

---

**文档版本**: v1.0  
**创建时间**: 2026-03-24  
**审核状态**: 待审核 ⏳  
**下次更新**: 迁移完成后更新
