# ERP 用户表格列配置功能设计方案

**版本：** v2.0  
**日期：** 2026-04-01  
**作者：** AI Assistant  
**状态：** 设计完成  
**架构：** 纯 JdbcTemplate + SqlBuilder（无 MyBatis Mapper）

---

## 📋 目录

- [1. 需求概述](#1-需求概述)
- [2. 功能特性](#2-功能特性)
- [3. 技术架构](#3-技术架构)
- [4. 数据库设计](#4-数据库设计)
- [5. 后端实现](#5-后端实现)
- [6. 前端实现](#6-前端实现)
- [7. 集成步骤](#7-集成步骤)
- [8. 使用示例](#8-使用示例)
- [9. 注意事项](#9-注意事项)
- [10. 后续优化](#10-后续优化)

---

## 1. 需求概述

### 1.1 背景
在 RuoYi-WMS ERP 低代码架构中，用户希望能够自定义表格的显示方式，包括：
- 哪些列显示/隐藏
- 列的显示顺序
- 列的宽度调整

### 1.2 目标
实现一个**用户级表格列配置功能**，满足以下要求：
1. ✅ **个性化配置**：每个用户的配置独立存储
2. ✅ **模块隔离**：不同页面（module_code）的配置分开
3. ✅ **多表格支持**：支持主表格和详情页签中的表格
4. ✅ **持久化存储**：配置保存到数据库，下次访问自动加载
5. ✅ **恢复默认**：支持一键恢复到系统默认配置

### 1.3 使用场景
- 销售人员查看销售订单列表时，只关心客户、金额、状态等字段
- 财务人员查看时，更关注税额、结算币别等字段
- 管理员可以查看所有字段

---

## 2. 功能特性

### 2.1 核心功能

| 功能 | 说明 | 优先级 |
|------|------|--------|
| 列显示/隐藏 | 用户可以控制每列是否显示 | P0 |
| 列顺序调整 | 通过拖拽调整列的顺序 | P0 |
| 列宽设置 | 设置每列的宽度（50-500px） | P1 |
| 配置保存 | 自动保存到数据库 | P0 |
| 配置加载 | 进入页面时自动加载用户配置 | P0 |
| 恢复默认 | 一键恢复到系统默认配置 | P1 |

### 2.2 支持的表格类型

| 表格类型 | 说明 | 配置键 |
|----------|------|--------|
| 主表格 | 列表页面的主要表格 | `main` |
| 详情页表格 - 页签 1 | 详情抽屉中的第一个表格页签 | `detail` + `tabName` |
| 详情页表格 - 页签 2 | 详情抽屉中的第二个表格页签 | `detail` + `tabName` |

### 2.3 配置隔离策略

```
唯一键 = module_code + user_id + table_type + tab_name
```

示例：
- `saleorder + 1001 + main + null`：销售订单主表格 - 用户 1001
- `saleorder + 1001 + detail + entry`：销售订单明细页签 - 用户 1001
- `saleorder + 1002 + main + null`：销售订单主表格 - 用户 1002

---

## 3. 技术架构

### 3.1 架构图

```
┌─────────────────────────────────────────────────────┐
│                    前端 Vue3                         │
│  ┌──────────────────────────────────────────────┐   │
│  │  BusinessTable.vue                           │   │
│  │  - 集成 ColumnSettingDialog 组件             │   │
│  │  - 应用用户配置到表格列                       │   │
│  └──────────────────────────────────────────────┘   │
│                          ▲                           │
│                          │ API 调用                  │
│  ┌──────────────────────────────────────────────┐   │
│  │  api/erp/userTableConfig.js                  │   │
│  │  - getUserColumnConfig()                     │   │
│  │  - saveUserColumnConfig()                    │   │
│  └──────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────┘
                         │
                         │ HTTP Request
                         ▼
┌─────────────────────────────────────────────────────┐
│              后端 Spring Boot 3.x                    │
│  ┌──────────────────────────────────────────────┐   │
│  │  ErpUserTableConfigController                │   │
│  │  - GET /erp/user/config/get/{moduleCode}     │   │
│  │  - POST /erp/user/config/save                │   │
│  └──────────────────────────────────────────────┘   │
│                          │                           │
│  ┌──────────────────────────────────────────────┐   │
│  │  ErpUserTableConfigService                   │   │
│  │  - getUserColumnConfig()                     │   │
│  │  - saveUserColumnConfig()                    │   │
│  └──────────────────────────────────────────────┘   │
│                          │                           │
│  ┌──────────────────────────────────────────────┐   │
│  │  JdbcTemplate + SqlBuilder                   │   │
│  │  - 动态构建 SQL                               │   │
│  │  - 参数化查询                                 │   │
│  └──────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────┘
                         │
                         │ JDBC
                         ▼
┌─────────────────────────────────────────────────────┐
│                  MySQL 数据库                        │
│  ┌──────────────────────────────────────────────┐   │
│  │  erp_user_table_config 表                    │   │
│  │  - config_id                                 │   │
│  │  - module_code                               │   │
│  │  - user_id                                   │   │
│  │  - table_type                                │   │
│  │  - tab_name                                  │   │
│  │  - column_config (JSON)                      │   │
│  └──────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────┘
```

### 3.2 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 前端框架 | Vue 3 | 3.4+ |
| UI 组件库 | Element Plus | 2.5+ |
| 拖拽库 | vuedraggable | 4.1+ |
| 后端框架 | Spring Boot | 3.2+ |
| 数据访问 | JdbcTemplate + SqlBuilder | 原生 JDBC |
| 数据库 | MySQL | 8.0+ |
| 认证框架 | Sa-Token | 1.37+ |

---

## 4. 数据库设计

### 4.1 表结构

```
-- ============================================
-- ERP 用户表格列配置表
-- 用途：存储每个用户对每个模块的表格列自定义配置
-- ============================================

DROP TABLE IF EXISTS `erp_user_table_config`;
CREATE TABLE `erp_user_table_config` (
  `config_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `module_code` VARCHAR(50) NOT NULL COMMENT '模块编码 (如 saleorder)',
  `user_id` BIGINT NOT NULL COMMENT '用户 ID',
  `table_type` VARCHAR(20) NOT NULL DEFAULT 'main' COMMENT '表格类型：main=主表格，detail=详情页表格',
  `tab_name` VARCHAR(50) DEFAULT NULL COMMENT '页签名称 (detail 类型必填，如 entry/cost)',
  `column_config` JSON NOT NULL COMMENT '列配置 JSON',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`config_id`),
  UNIQUE KEY `uk_module_user_tab` (`module_code`, `user_id`, `table_type`, `tab_name`),
  KEY `idx_module_code` (`module_code`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='ERP 用户表格列配置表';
```

### 4.2 字段说明

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| config_id | BIGINT | ✓ | 主键，自增 |
| module_code | VARCHAR(50) | ✓ | 模块编码，如 `saleorder` |
| user_id | BIGINT | ✓ | 用户 ID，从登录会话获取 |
| table_type | VARCHAR(20) | ✓ | 表格类型：`main` 或 `detail` |
| tab_name | VARCHAR(50) | - | 详情页签名称，如 `entry`、`cost` |
| column_config | JSON | ✓ | 列配置 JSON 数据 |
| remark | VARCHAR(500) | - | 备注信息 |
| create_by | VARCHAR(64) | - | 创建者账号 |
| create_time | DATETIME | - | 创建时间 |
| update_by | VARCHAR(64) | - | 更新者账号 |
| update_time | DATETIME | - | 更新时间 |

### 4.3 索引设计

- **主键索引**：`config_id`
- **唯一索引**：`module_code + user_id + table_type + tab_name`（防止重复配置）
- **普通索引**：`module_code`（加速按模块查询）
- **普通索引**：`user_id`（加速按用户查询）

### 4.4 数据示例

```
{
  "config_id": 1,
  "module_code": "saleorder",
  "user_id": 1001,
  "table_type": "main",
  "tab_name": null,
  "column_config": [
    {
      "prop": "FBillNo",
      "label": "单据编号",
      "width": 150,
      "visible": true
    },
    {
      "prop": "F_ora_BaseProperty",
      "label": "客户简称",
      "width": 150,
      "visible": true
    },
    {
      "prop": "FSalerId",
      "label": "销售员",
      "width": 120,
      "visible": false
    }
  ],
  "create_time": "2026-04-01 10:00:00"
}
```

---

## 5. 后端实现（纯 JdbcTemplate + SqlBuilder）

### 架构说明

**重要：** 本项目已全面弃用 MyBatis-Plus，采用纯 **JdbcTemplate + SqlBuilder** 方式。

**优势：**
- ✅ 无需 Mapper 接口和 XML 文件
- ✅ 无需 Entity 实体类（可选保留用于 IDE 提示）
- ✅ 更灵活的 SQL 控制
- ✅ 更高的执行效率

**核心组件：**
1. `JdbcTemplate` - Spring 提供的原生 JDBC 操作工具
2. `SqlBuilder` - 项目自研的动态 SQL 构建工具
3. `Map<String, Object>` - 查询结果直接存储在 Map 中

### 5.1 实体类

**文件路径：** `ruoyi-modules/ruoyi-erp-api/src/main/java/com/ruoyi/erp/domain/entity/ErpUserTableConfig.java`

```java
package com.ruoyi.erp.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * ERP 用户表格列配置表实体类
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("erp_user_table_config")
public class ErpUserTableConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 ID */
    @TableId(value = "config_id", type = IdType.AUTO)
    private Long configId;

    /** 模块编码 */
    @TableField("module_code")
    private String moduleCode;

    /** 用户 ID */
    @TableField("user_id")
    private Long userId;

    /** 表格类型 */
    @TableField("table_type")
    private String tableType;

    /** 页签名称 */
    @TableField("tab_name")
    private String tabName;

    /** 列配置 JSON */
    @TableField("column_config")
    private String columnConfig;

    /** 备注 */
    @TableField("remark")
    private String remark;

    /** 创建者 */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private String createBy;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新者 */
    @TableField(value = "update_by", fill = FieldFill.UPDATE)
    private String updateBy;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}
```

### 5.2 BO 对象

**文件路径：** `ruoyi-modules/ruoyi-erp-api/src/main/java/com/ruoyi/erp/domain/bo/ErpUserTableConfigBo.java`

```java
package com.ruoyi.erp.domain.bo;

import lombok.Data;

/**
 * ERP 用户表格列配置业务对象
 */
@Data
public class ErpUserTableConfigBo {
    private String moduleCode;
    private String tableType;
    private String tabName;
    private String columnConfig;
    private String remark;
}
```

### 5.3 VO 对象

**文件路径：** `ruoyi-modules/ruoyi-erp-api/src/main/java/com/ruoyi/erp/domain/vo/ErpUserTableConfigVo.java`

```java
package com.ruoyi.erp.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * ERP 用户表格列配置视图对象
 */
@Data
public class ErpUserTableConfigVo {
    private Long configId;
    private String moduleCode;
    private Long userId;
    private String tableType;
    private String tabName;
    private String columnConfig;
    private String remark;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
```

### 5.4 SqlBuilder 工具类（核心）

**文件路径：** `ruoyi-modules/ruoyi-erp-api/src/main/java/com/ruoyi/erp/utils/SqlBuilder.java`

项目已有 SqlBuilder 工具类用于动态构建 SQL，这是 ERP 模块的核心工具。

**主要方法：**

```java
// 1. 构建 WHERE 子句
public SqlResult buildWhere(List<Map<String, Object>> conditions)

// 2. 构建 ORDER BY 子句  
public SqlResult buildOrderBy(List<Map<String, Object>> orderBy)

// 3. 构建 SELECT 字段列表
public String buildSelectFields(List<String> fields)
```

**支持的运算符：**

| 运算符 | 说明 | 示例 |
|--------|------|------|
| `eq` | 等于 | `field = ?` |
| `ne` | 不等于 | `field <> ?` |
| `gt` | 大于 | `field > ?` |
| `ge` | 大于等于 | `field >= ?` |
| `lt` | 小于 | `field < ?` |
| `le` | 小于等于 | `field <= ?` |
| `like` | 模糊匹配 | `field LIKE %?%` |
| `left_like` | 左模糊 | `field LIKE ?%` |
| `right_like` | 右模糊 | `field LIKE %?` |
| `in` | IN 查询 | `field IN (?, ?, ?)` |
| `between` | 范围 | `field BETWEEN ? AND ?` |
| `isNull` | IS NULL | `field IS NULL` |
| `isNotNull` | IS NOT NULL | `field IS NOT NULL` |

**使用示例：**

```java
// 构建查询条件
List<Map<String, Object>> conditions = new ArrayList<>();

Map<String, Object> cond1 = new HashMap<>();
cond1.put("field", "module_code");
cond1.put("operator", "eq");
cond1.put("value", "saleorder");
conditions.add(cond1);

Map<String, Object> cond2 = new HashMap<>();
cond2.put("field", "user_id");
cond2.put("operator", "eq");
cond2.put("value", userId);
conditions.add(cond2);

// 生成 SQL
SqlBuilder.SqlResult result = sqlBuilder.buildWhere(conditions);
// result.getSql() => " WHERE module_code = ? AND user_id = ?"
// result.getParams() => ["saleorder", userId]
```

**安全特性：**
- ✅ 自动参数化查询（防 SQL 注入）
- ✅ 字段名校验（只允许字母、数字、下划线）
- ✅ 空值自动跳过
- ✅ 类型安全检查

### 5.5 Service 接口

**文件路径：** `ruoyi-modules/ruoyi-erp-api/src/main/java/com/ruoyi/erp/service/ErpUserTableConfigService.java`

```java
package com.ruoyi.erp.service;

import com.ruoyi.common.mybatis.core.page.TableDataInfo;
import com.ruoyi.common.mybatis.core.page.PageQuery;
import com.ruoyi.erp.domain.bo.ErpUserTableConfigBo;
import com.ruoyi.erp.domain.vo.ErpUserTableConfigVo;

/**
 * ERP 用户表格列配置 Service 接口
 */
public interface ErpUserTableConfigService {

    /**
     * 查询用户表格列配置列表
     */
    TableDataInfo<ErpUserTableConfigVo> selectPageList(ErpUserTableConfigBo bo, PageQuery pageQuery);

    /**
     * 获取用户的表格列配置
     * 
     * @param moduleCode 模块编码
     * @param tableType 表格类型
     * @param tabName 页签名称
     * @return 列配置 JSON
     */
    String getUserColumnConfig(String moduleCode, String tableType, String tabName);

    /**
     * 保存用户的表格列配置
     * 
     * @param moduleCode 模块编码
     * @param tableType 表格类型
     * @param tabName 页签名称
     * @param columnConfig 列配置 JSON
     * @param remark 备注
     * @return 影响行数
     */
    int saveUserColumnConfig(String moduleCode, String tableType, String tabName, String columnConfig, String remark);
}
```

### 5.6 Service 实现（完整代码）

**文件路径：** `ruoyi-modules/ruoyi-erp-api/src/main/java/com/ruoyi/erp/service/impl/ErpUserTableConfigServiceImpl.java`

```
package com.ruoyi.erp.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import com.ruoyi.common.mybatis.core.page.PageQuery;
import com.ruoyi.common.mybatis.core.page.TableDataInfo;
import com.ruoyi.common.core.utils.StringUtils;
import com.ruoyi.erp.domain.bo.ErpUserTableConfigBo;
import com.ruoyi.erp.domain.vo.ErpUserTableConfigVo;
import com.ruoyi.erp.service.ErpUserTableConfigService;
import com.ruoyi.erp.utils.SqlBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * ERP 用户表格列配置 Service 业务层处理
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ErpUserTableConfigServiceImpl implements ErpUserTableConfigService {

    private final JdbcTemplate jdbcTemplate;
    private final SqlBuilder sqlBuilder;

    @Override
    public TableDataInfo<ErpUserTableConfigVo> selectPageList(ErpUserTableConfigBo bo, PageQuery pageQuery) {
        // 1. 构建查询条件
        List<Map<String, Object>> conditions = buildConditionsFromBo(bo);
        SqlBuilder.SqlResult sqlResult = sqlBuilder.buildWhere(conditions);
        
        // 2. 构建分页 SQL
        StringBuilder sql = new StringBuilder("SELECT * FROM erp_user_table_config");
        sql.append(sqlResult.getSql());
        sql.append(" ORDER BY create_time DESC");
        
        long pageNum = pageQuery.getPageNum();
        long pageSize = pageQuery.getPageSize();
        long offset = (pageNum - 1) * pageSize;
        
        sql.append(" LIMIT ? OFFSET ?");
        List<Object> params = new ArrayList<>(sqlResult.getParams());
        params.add(pageSize);
        params.add(offset);
        
        // 3. 执行查询
        List<Map<String, Object>> resultList = jdbcTemplate.queryForList(
            sql.toString(), params.toArray()
        );
        
        // 4. 查询总数
        String countSql = "SELECT COUNT(*) FROM erp_user_table_config" + sqlResult.getSql();
        Long total = jdbcTemplate.queryForObject(
            countSql, Long.class, sqlResult.getParams().toArray()
        );
        
        // 5. 转换为 VO
        List<ErpUserTableConfigVo> voList = new ArrayList<>();
        for (Map<String, Object> row : resultList) {
            ErpUserTableConfigVo vo = convertMapToVo(row);
            if (vo != null) {
                voList.add(vo);
            }
        }
        
        return new TableDataInfo<>(voList, total);
    }

    @Override
    public String getUserColumnConfig(String moduleCode, String tableType, String tabName) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 构建查询条件
        List<Map<String, Object>> conditions = new ArrayList<>();
        
        Map<String, Object> cond1 = new HashMap<>();
        cond1.put("field", "module_code");
        cond1.put("operator", "eq");
        cond1.put("value", moduleCode);
        conditions.add(cond1);
        
        Map<String, Object> cond2 = new HashMap<>();
        cond2.put("field", "user_id");
        cond2.put("operator", "eq");
        cond2.put("value", userId);
        conditions.add(cond2);
        
        Map<String, Object> cond3 = new HashMap<>();
        cond3.put("field", "table_type");
        cond3.put("operator", "eq");
        cond3.put("value", tableType);
        conditions.add(cond3);
        
        // tab_name 可能为 null
        if (tabName != null) {
            Map<String, Object> cond4 = new HashMap<>();
            cond4.put("field", "tab_name");
            cond4.put("operator", "eq");
            cond4.put("value", tabName);
            conditions.add(cond4);
        } else {
            Map<String, Object> cond4 = new HashMap<>();
            cond4.put("field", "tab_name");
            cond4.put("operator", "is_null");
            conditions.add(cond4);
        }
        
        SqlBuilder.SqlResult sqlResult = sqlBuilder.buildWhere(conditions);
        String sql = "SELECT * FROM erp_user_table_config" + sqlResult.getSql() + " LIMIT 1";
        
        List<Map<String, Object>> resultList = jdbcTemplate.queryForList(
            sql, sqlResult.getParams().toArray()
        );
        
        if (resultList.isEmpty()) {
            return null;
        }
        
        return getStringValue(resultList.get(0), "column_config");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveUserColumnConfig(String moduleCode, String tableType, 
                                    String tabName, String columnConfig, String remark) {
        Long userId = StpUtil.getLoginIdAsLong();
        String loginId = StpUtil.getLoginIdAsString();
        
        // 先查询是否存在
        String existingConfig = getUserColumnConfig(moduleCode, tableType, tabName);
        
        if (existingConfig != null) {
            // 更新
            return updateConfig(moduleCode, userId, tableType, tabName, 
                              columnConfig, remark, loginId);
        } else {
            // 新增
            return insertConfig(moduleCode, userId, tableType, tabName, 
                              columnConfig, remark, loginId);
        }
    }

    /**
     * 从 BO 构建查询条件
     */
    private List<Map<String, Object>> buildConditionsFromBo(ErpUserTableConfigBo bo) {
        List<Map<String, Object>> conditions = new ArrayList<>();
        
        if (StringUtils.isNotEmpty(bo.getModuleCode())) {
            Map<String, Object> cond = new HashMap<>();
            cond.put("field", "module_code");
            cond.put("operator", "eq");
            cond.put("value", bo.getModuleCode());
            conditions.add(cond);
        }
        
        if (StringUtils.isNotEmpty(bo.getTableType())) {
            Map<String, Object> cond = new HashMap<>();
            cond.put("field", "table_type");
            cond.put("operator", "eq");
            cond.put("value", bo.getTableType());
            conditions.add(cond);
        }
        
        return conditions;
    }

    /**
     * 插入配置
     */
    private int insertConfig(String moduleCode, Long userId, String tableType,
                            String tabName, String columnConfig, String remark, 
                            String loginId) {
        String sql = """
            INSERT INTO erp_user_table_config (
                module_code, user_id, table_type, tab_name,
                column_config, remark, create_by, create_time,
                update_by, update_time
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        return jdbcTemplate.update(sql,
            moduleCode, userId, tableType, tabName,
            columnConfig, remark, loginId, LocalDateTime.now(),
            loginId, LocalDateTime.now()
        );
    }

    /**
     * 更新配置
     */
    private int updateConfig(String moduleCode, Long userId, String tableType,
                            String tabName, String columnConfig, String remark,
                            String loginId) {
        String sql = """
            UPDATE erp_user_table_config 
            SET column_config = ?, 
                remark = ?,
                update_by = ?,
                update_time = ?
            WHERE module_code = ? 
              AND user_id = ? 
              AND table_type = ? 
              AND IFNULL(tab_name, '') = IFNULL(?, '')
        """;
        
        return jdbcTemplate.update(sql,
            columnConfig, remark, loginId, LocalDateTime.now(),
            moduleCode, userId, tableType, tabName
        );
    }

    /**
     * Map 转 VO 的辅助方法
     */
    private ErpUserTableConfigVo convertMapToVo(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        
        ErpUserTableConfigVo vo = new ErpUserTableConfigVo();
        vo.setConfigId(getLongValue(map, "config_id"));
        vo.setModuleCode(getStringValue(map, "module_code"));
        vo.setUserId(getLongValue(map, "user_id"));
        vo.setTableType(getStringValue(map, "table_type"));
        vo.setTabName(getStringValue(map, "tab_name"));
        vo.setColumnConfig(getStringValue(map, "column_config"));
        vo.setRemark(getStringValue(map, "remark"));
        vo.setCreateTime(getLocalDateTimeValue(map, "create_time"));
        vo.setUpdateTime(getLocalDateTimeValue(map, "update_time"));
        
        return vo;
    }

    /**
     * 获取 Long 类型值
     */
    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.valueOf(value.toString());
    }

    /**
     * 获取 String 类型值
     */
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * 获取 LocalDateTime 类型值
     */
    private LocalDateTime getLocalDateTimeValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof LocalDateTime) {
            return (LocalDateTime) value;
        }
        if (value instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) value).toLocalDateTime();
        }
        return null;
    }
}
```

**关键点说明：**

1. **构造器注入**：通过 `@RequiredArgsConstructor` 自动注入 `JdbcTemplate` 和 `SqlBuilder`
2. **参数化查询**：所有 SQL 都使用 `?` 占位符，防止 SQL 注入
3. **Map 转 VO**：提供 `convertMapToVo()` 方法将查询结果转换为 VO
4. **事务管理**：`saveUserColumnConfig()` 方法添加 `@Transactional` 注解
5. **日志记录**：关键操作都有日志输出，方便调试

### 5.7 Controller

**文件路径：** `ruoyi-modules/ruoyi-erp-api/src/main/java/com/ruoyi/erp/controller/erp/ErpUserTableConfigController.java`

```
package com.ruoyi.erp.controller.erp;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.ruoyi.common.log.annotation.Log;
import com.ruoyi.common.web.core.BaseController;
import com.ruoyi.common.mybatis.core.page.PageQuery;
import com.ruoyi.common.mybatis.core.page.TableDataInfo;
import com.ruoyi.common.log.enums.BusinessType;
import com.ruoyi.erp.domain.bo.ErpUserTableConfigBo;
import com.ruoyi.erp.domain.vo.ErpUserTableConfigVo;
import com.ruoyi.erp.domain.response.ErpResponse;
import com.ruoyi.erp.service.ErpUserTableConfigService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * ERP 用户表格列配置 信息操作处理
 */
@Slf4j
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/erp/user/config")
public class ErpUserTableConfigController extends BaseController {

    private final ErpUserTableConfigService userConfigService;

    /**
     * 获取用户的表格列配置
     */
    @GetMapping("/get/{moduleCode}")
    public ErpResponse<String> getUserConfig(
            @PathVariable String moduleCode,
            @RequestParam(defaultValue = "main") String tableType,
            @RequestParam(required = false) String tabName) {
        try {
            log.info("[ErpUserTableConfigController] 获取用户表格配置，moduleCode: {}, tableType: {}, tabName: {}", 
                moduleCode, tableType, tabName);
            
            String config = userConfigService.getUserColumnConfig(moduleCode, tableType, tabName);
            
            if (config == null) {
                log.info("[ErpUserTableConfigController] 未找到用户配置，返回默认值");
                return ErpResponse.ok("未找到配置", null);
            }
            
            log.info("[ErpUserTableConfigController] 配置获取成功");
            return ErpResponse.ok("操作成功", config);
        } catch (Exception e) {
            log.error("[ErpUserTableConfigController] 获取配置失败", e);
            return ErpResponse.fail("获取失败：" + e.getMessage());
        }
    }

    /**
     * 保存用户的表格列配置
     */
    @Log(title = "用户表格配置", businessType = BusinessType.UPDATE)
    @PostMapping("/save")
    public ErpResponse<Void> saveUserConfig(@RequestBody Map<String, Object> params) {
        try {
            String moduleCode = (String) params.get("moduleCode");
            String tableType = (String) params.getOrDefault("tableType", "main");
            String tabName = (String) params.get("tabName");
            String columnConfig = (String) params.get("columnConfig");
            String remark = (String) params.getOrDefault("remark", "");
            
            log.info("[ErpUserTableConfigController] 保存用户表格配置，moduleCode: {}, tableType: {}, tabName: {}", 
                moduleCode, tableType, tabName);
            
            userConfigService.saveUserColumnConfig(moduleCode, tableType, tabName, columnConfig, remark);
            
            return ErpResponse.ok("保存成功");
        } catch (Exception e) {
            log.error("[ErpUserTableConfigController] 保存配置失败", e);
            return ErpResponse.fail("保存失败：" + e.getMessage());
        }
    }
}
```

---

## 6. 前端实现

### 6.1 API 封装

**文件路径：** `baiyu-web/src/api/erp/userTableConfig.js`

```
/**
 * ERP 用户表格列配置 API
 * @description 提供用户自定义表格列配置的接口封装
 */

import request from '@/utils/request'

/**
 * 获取用户的表格列配置
 * @param {string} moduleCode - 模块编码
 * @param {string} tableType - 表格类型 (main/detail)
 * @param {string} tabName - 页签名称 (detail 类型需要)
 * @returns {Promise}
 */
export function getUserColumnConfig(moduleCode, tableType = 'main', tabName = null) {
  const params = { tableType }
  if (tabName) {
    params.tabName = tabName
  }
  
  return request({
    url: `/erp/user/config/get/${moduleCode}`,
    method: 'get',
    params
  })
}

/**
 * 保存用户的表格列配置
 * @param {Object} config - 配置对象
 * @param {string} config.moduleCode - 模块编码
 * @param {string} config.tableType - 表格类型
 * @param {string} config.tabName - 页签名称
 * @param {string|array} config.columnConfig - 列配置 JSON 字符串或对象
 * @param {string} config.remark - 备注
 * @returns {Promise}
 */
export function saveUserColumnConfig(config) {
  return request({
    url: '/erp/user/config/save',
    method: 'post',
    data: {
      moduleCode: config.moduleCode,
      tableType: config.tableType,
      tabName: config.tabName,
      columnConfig: typeof config.columnConfig === 'string' 
        ? config.columnConfig 
        : JSON.stringify(config.columnConfig),
      remark: config.remark || ''
    }
  })
}
```

### 6.2 列设置对话框组件

**文件路径：** `baiyu-web/src/components/ColumnSettingDialog/index.vue`

（完整代码见上文设计稿，此处省略）

**核心功能：**
1. 支持两个页签：主表格设置、详情页表格设置
2. 使用 `vuedraggable` 实现拖拽排序
3. 开关控制列的显示/隐藏
4. 输入框设置列宽
5. 实时保存配置到数据库

### 6.3 集成到 BusinessTable

**修改文件：** `baiyu-web/src/views/erp/pageTemplate/configurable/BusinessConfigurable/components/BusinessTable.vue`

**添加内容：**
1. 引入 `ColumnSettingDialog` 组件
2. 在工具栏添加"列设置"按钮
3. 加载用户配置并应用到表格列
4. 监听配置更新事件

---

## 7. 集成步骤

### 7.1 数据库脚本执行

```
# 1. 连接到 MySQL 数据库
mysql -u root -p test

# 2. 执行建表脚本
source d:/baiyuyunma/baiyu-github/baiyu-github/DOC/erp_user_table_config.sql
```

### 7.2 后端代码部署

**步骤 1：** 创建实体类、BO、VO
```bash
# 在对应目录下创建文件
- ErpUserTableConfig.java
- ErpUserTableConfigBo.java
- ErpUserTableConfigVo.java
```

**步骤 2：** 创建 Mapper 接口
```

```

**说明：** 项目采用 JdbcTemplate + SqlBuilder 方式，不需要 XML 映射文件

**步骤 3：** 创建 Service
```bash
# 创建接口和实现
- ErpUserTableConfigService.java
- ErpUserTableConfigServiceImpl.java
```

**步骤 4：** 创建 Controller
```bash
# 创建控制器
- ErpUserTableConfigController.java
```

**步骤 5：** 重启后端服务
```bash
cd baiyu-ruoyi/ruoyi-admin-wms
mvn clean install
java -jar target/ruoyi-admin-wms.jar
```

### 7.3 前端代码部署

**步骤 1：** 创建 API 文件
```bash
# 创建 API 封装
baiyu-web/src/api/erp/userTableConfig.js
```

**步骤 2：** 创建组件
```bash
# 创建列设置对话框组件
baiyu-web/src/components/ColumnSettingDialog/index.vue
```

**步骤 3：** 安装依赖
```bash
cd baiyu-web
npm install vuedraggable@next
```

**步骤 4：** 集成到 BusinessTable
```bash
# 修改 BusinessTable.vue，添加列设置功能
```

**步骤 5：** 重启前端服务
```bash
npm run dev
```

---

## 8. 使用示例

### 8.1 API 调用示例

#### 获取主表格配置
```
import { getUserColumnConfig } from '@/api/erp/userTableConfig'

const response = await getUserColumnConfig('saleorder', 'main')
console.log(response.data) 
// 输出：[{"prop":"FBillNo","label":"单据编号","width":150,"visible":true}, ...]
```

#### 获取详情页表格配置
```
const response = await getUserColumnConfig('saleorder', 'detail', 'entry')
console.log(response.data)
// 输出：详情页签 entry 的列配置
```

#### 保存配置
```
import { saveUserColumnConfig } from '@/api/erp/userTableConfig'

await saveUserColumnConfig({
  moduleCode: 'saleorder',
  tableType: 'main',
  columnConfig: [
    { prop: 'FBillNo', label: '单据编号', width: 150, visible: true },
    { prop: 'FDate', label: '日期', width: 140, visible: false }
  ],
  remark: '用户自定义配置'
})
```

### 8.2 页面使用示例

#### 在 BusinessTable 中使用

```
<template>
  <el-card shadow="never" class="table-card">
    <!-- 工具栏 -->
    <div class="toolbar">
      <el-button icon="Setting" @click="openColumnSetting">
        列设置
      </el-button>
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

## 9. 注意事项

### 9.1 开发注意事项

1. **权限控制**
   - 当前未加权限，建议添加 `@SaCheckPermission("erp:user-config:query")`
   - 保存操作也需要相应权限注解

2. **数据验证**
   - `columnConfig` JSON 格式需要在前端校验
   - 后端可以添加 JSON Schema 验证

3. **性能优化**
   - 配置数据较小，无需缓存
   - 如果配置项增多，可考虑 Redis 缓存

4. **兼容性**
   - 旧用户没有配置记录时，返回 null，使用默认配置
   - 配置数据结构变更时，需要兼容处理

### 9.2 用户使用指南

1. **打开列设置**
   - 点击工具栏的"列设置"按钮

2. **调整列顺序**
   - 拖拽列名称左侧的拖动图标

3. **显示/隐藏列**
   - 点击列右侧的开关

4. **设置列宽**
   - 在输入框中输入数值（50-500）

5. **保存配置**
   - 点击"保存配置"按钮

6. **恢复默认**
   - 点击"恢复默认"按钮

---

## 10. 后续优化

### 10.1 功能增强

| 优化项 | 说明 | 优先级 |
|--------|------|--------|
| 固定列设置 | 支持设置哪些列固定到左侧/右侧 | P2 |
| 排序设置 | 支持设置列的默认排序规则 | P2 |
| 格式化设置 | 支持设置数字精度、日期格式等 | P3 |
| 条件格式 | 根据值设置不同的样式 | P3 |
| 配置导出导入 | 支持将配置导出为 JSON 文件 | P3 |

### 10.2 性能优化

1. **批量保存**
   - 将多个表格的配置合并为一次请求

2. **增量更新**
   - 只提交变化的列配置，减少数据传输

3. **本地缓存**
   - 使用 localStorage 临时存储，减少服务器请求

### 10.3 用户体验

1. **预览功能**
   - 调整配置时实时预览效果

2. **快捷键**
   - Ctrl+S 快速保存配置

3. **配置对比**
   - 显示当前配置与默认配置的差异

4. **批量操作**
   - 全选/全不显、批量设置列宽

---

## 附录

### A. 相关文件清单

**后端文件（6 个）：**
```
ruoyi-modules/ruoyi-erp-api/src/main/java/com/ruoyi/erp/
├── domain/
│   ├── entity/ErpUserTableConfig.java          # 可选，仅用于 IDE 提示
│   ├── bo/ErpUserTableConfigBo.java            # 必须
│   └── vo/ErpUserTableConfigVo.java            # 必须
├── service/
│   ├── ErpUserTableConfigService.java          # 必须
│   └── impl/ErpUserTableConfigServiceImpl.java # 必须 (JdbcTemplate)
└── controller/erp/
    └── ErpUserTableConfigController.java       # 必须
```

**说明：**
- ✅ **无需 Mapper 接口和 XML** - 直接使用 JdbcTemplate + SqlBuilder
- ✅ **Entity 可选** - 可以只用于 IDE 类型提示，非必需

**前端文件（3 个）：**
```
baiyu-web/src/
├── api/erp/userTableConfig.js
├── components/ColumnSettingDialog/index.vue
└── views/erp/pageTemplate/configurable/BusinessConfigurable/components/BusinessTable.vue
```

**数据库文件：**
```
DOC/erp_user_table_config.sql
```

### B. API 接口文档

| 接口 | 方法 | 权限 | 说明 |
|------|------|------|------|
| `/erp/user/config/get/{moduleCode}` | GET | 无 | 获取用户表格列配置 |
| `/erp/user/config/save` | POST | 无 | 保存用户表格列配置 |

### C. 配置 JSON 格式

```
[
  {
    "prop": "字段名",
    "label": "列标题",
    "width": 150,
    "visible": true,
    "fixed": "left",
    "sortable": true,
    "renderType": "text"
  }
]
```

---

**文档结束**
