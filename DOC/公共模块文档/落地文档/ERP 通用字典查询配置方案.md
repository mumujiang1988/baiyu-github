# ERP 通用字典查询配置方案

## 📚 核心思路

**直接使用现有的通用查询引擎**,在 `erp_page_config` 表中配置字典查询的 SQL 语句,通过统一接口 `/erp/engine/dictionary/{moduleCode}` 访问。

---

## 🎯 实现步骤

### 1️⃣ 后端接口 (已存在)

```http
GET /erp/engine/dictionary/{moduleCode}
```

该接口已经存在于 `ErpEngineController.java` 中，会自动从 `erp_page_config` 表读取配置并执行 SQL 查询。

---

### 2️⃣ 数据库配置脚本

将所有字典配置作为 **DICT 类型** 记录插入 `erp_page_config` 表:

```sql
-- ========================================
-- ERP 通用字典配置 - SQL 脚本
-- ========================================

-- 1. 销售员字典配置
INSERT INTO erp_page_config (
    module_code, config_name, config_type, config_content, 
    version, status, is_public, remark
) VALUES (
    'salesperson_dict',
    '销售员字典配置',
    'DICT',
    '{
        "name": "销售员字典",
        "description": "查询所有可用销售员列表，包含部门信息和角色信息",
        "query": {
            "sql": "SELECT u.nick_name as label, u.user_id as value, d.dept_name as departmentName, e.salesman_id as fseller FROM sys_user u LEFT JOIN sys_dept d ON u.dept_id = d.dept_id LEFT JOIN sys_employee e ON u.staff_id = e.fid WHERE u.status = ''1'' AND u.del_flag = ''0'' ORDER BY u.nick_name",
            "resultType": "list"
        },
        "cacheable": true,
        "cacheTTL": 3600
    }',
    1, '1', '1', 'ERP 通用销售员字典查询配置'
);

-- 2. 部门字典配置
INSERT INTO erp_page_config (
    module_code, config_name, config_type, config_content, 
    version, status, is_public, remark
) VALUES (
    'department_dict',
    '部门字典配置',
    'DICT',
    '{
        "name": "部门字典",
        "description": "查询所有部门列表",
        "query": {
            "sql": "SELECT dept_name as label, dept_id as value FROM sys_dept WHERE del_flag = ''0'' ORDER BY dept_sort",
            "resultType": "list"
        },
        "cacheable": true,
        "cacheTTL": 3600
    }',
    1, '1', '1', 'ERP 通用部门字典查询配置'
);

-- 3. 角色字典配置
INSERT INTO erp_page_config (
    module_code, config_name, config_type, config_content, 
    version, status, is_public, remark
) VALUES (
    'role_dict',
    '角色字典配置',
    'DICT',
    '{
        "name": "角色字典",
        "description": "查询所有角色列表",
        "query": {
            "sql": "SELECT role_name as label, role_id as value FROM sys_role WHERE del_flag = ''0'' ORDER BY role_sort",
            "resultType": "list"
        },
        "cacheable": true,
        "cacheTTL": 3600
    }',
    1, '1', '1', 'ERP 通用角色字典查询配置'
);

-- 4. 国家地区字典配置
INSERT INTO erp_page_config (
    module_code, config_name, config_type, config_content, 
    version, status, is_public, remark
) VALUES (
    'country_dict',
    '国家地区字典配置',
    'DICT',
    '{
        "name": "国家地区字典",
        "description": "查询所有国家/地区列表，支持中英文搜索",
        "query": {
            "sql": "SELECT name_zh as label, id as value, name_en, status FROM country WHERE status = 1 ORDER BY name_zh",
            "resultType": "list"
        },
        "cacheable": true,
        "cacheTTL": 86400
    }',
    1, '1', '1', 'ERP 通用国家地区字典查询配置'
);

-- 5. 物料字典配置 - 币别
INSERT INTO erp_page_config (
    module_code, config_name, config_type, config_content, 
    version, status, is_public, remark
) VALUES (
    'currency_dict',
    '币别字典配置',
    'DICT',
    '{
        "name": "币别字典",
        "description": "查询所有币种列表",
        "query": {
            "sql": "SELECT name as label, code as value, kingdee, category_name FROM bymaterial_dictionary WHERE category = ''currency'' ORDER BY name",
            "resultType": "list"
        },
        "cacheable": true,
        "cacheTTL": 86400
    }',
    1, '1', '1', 'ERP 通用币别字典查询配置'
);

-- 6. 物料字典配置 - 产品类别
INSERT INTO erp_page_config (
    module_code, config_name, config_type, config_content, 
    version, status, is_public, remark
) VALUES (
    'product_category_dict',
    '产品类别字典配置',
    'DICT',
    '{
        "name": "产品类别字典",
        "description": "查询所有产品类别列表",
        "query": {
            "sql": "SELECT name as label, code as value, kingdee, category_name FROM bymaterial_dictionary WHERE category = ''product_category'' ORDER BY name",
            "resultType": "list"
        },
        "cacheable": true,
        "cacheTTL": 86400
    }',
    1, '1', '1', 'ERP 通用产品类别字典查询配置'
);

-- 7. 系统字典配置 - 性别
INSERT INTO erp_page_config (
    module_code, config_name, config_type, config_content, 
    version, status, is_public, remark
) VALUES (
    'user_sex_dict',
    '性别字典配置',
    'DICT',
    '{
        "name": "性别字典",
        "description": "查询用户性别选项",
        "query": {
            "sql": "SELECT dict_label as label, dict_value as value, list_class FROM sys_dict_data WHERE dict_type = ''sys_user_sex'' AND status = ''1'' ORDER BY dict_sort",
            "resultType": "list"
        },
        "cacheable": true,
        "cacheTTL": 86400
    }',
    1, '1', '1', 'ERP 通用性别字典查询配置'
);

-- 8. 系统字典配置 - 状态
INSERT INTO erp_page_config (
    module_code, config_name, config_type, config_content, 
    version, status, is_public, remark
) VALUES (
    'normal_disable_dict',
    '状态字典配置',
    'DICT',
    '{
        "name": "状态字典",
        "description": "查询正常/停用状态选项",
        "query": {
            "sql": "SELECT dict_label as label, dict_value as value, list_class FROM sys_dict_data WHERE dict_type = ''sys_normal_disable'' AND status = ''1'' ORDER BY dict_sort",
            "resultType": "list"
        },
        "cacheable": true,
        "cacheTTL": 86400
    }',
    1, '1', '1', 'ERP 通用状态字典查询配置'
);

-- 9. 计量单位字典配置
INSERT INTO erp_page_config (
    module_code, config_name, config_type, config_content, 
    version, status, is_public, remark
) VALUES (
    'unit_dict',
    '计量单位字典配置',
    'DICT',
    '{
        "name": "计量单位字典",
        "description": "查询所有计量单位列表",
        "query": {
            "sql": "SELECT name as label, code as value, kingdee FROM bymaterial_dictionary WHERE category = ''unit'' ORDER BY name",
            "resultType": "list"
        },
        "cacheable": true,
        "cacheTTL": 86400
    }',
    1, '1', '1', 'ERP 通用计量单位字典查询配置'
);

-- 10. 贸易方式字典配置
INSERT INTO erp_page_config (
    module_code, config_name, config_type, config_content, 
    version, status, is_public, remark
) VALUES (
    'trade_type_dict',
    '贸易方式字典配置',
    'DICT',
    '{
        "name": "贸易方式字典",
        "description": "查询所有贸易方式选项",
        "query": {
            "sql": "SELECT name as label, code as value FROM bymaterial_dictionary WHERE category = ''trade_way'' ORDER BY name",
            "resultType": "list"
        },
        "cacheable": true,
        "cacheTTL": 86400
    }',
    1, '1', '1', 'ERP 通用贸易方式字典查询配置'
);
```

---

### 3️⃣ 前端配置示例

#### business.config.template.json

```json
{
  "dictionaryConfig": {
    "salespersons": {
      "api": "/erp/engine/dictionary/salesperson_dict?moduleCode={moduleCode}",
      "labelField": "label",
      "valueField": "value",
      "extraFields": ["departmentName", "fseller"]
    },
    "department": {
      "api": "/erp/engine/dictionary/department_dict?moduleCode={moduleCode}",
      "labelField": "label",
      "valueField": "value"
    },
    "role": {
      "api": "/erp/engine/dictionary/role_dict?moduleCode={moduleCode}",
      "labelField": "label",
      "valueField": "value"
    },
    "nation": {
      "api": "/erp/engine/dictionary/country_dict?moduleCode={moduleCode}",
      "labelField": "label",
      "valueField": "value",
      "extraFields": ["name_en"]
    },
    "currency": {
      "api": "/erp/engine/dictionary/currency_dict?moduleCode={moduleCode}",
      "labelField": "label",
      "valueField": "value",
      "extraFields": ["kingdee"]
    },
    "productCategory": {
      "api": "/erp/engine/dictionary/product_category_dict?moduleCode={moduleCode}",
      "labelField": "label",
      "valueField": "value"
    },
    "userSex": {
      "api": "/erp/engine/dictionary/user_sex_dict?moduleCode={moduleCode}",
      "labelField": "label",
      "valueField": "value",
      "extraFields": ["list_class"]
    },
    "normalDisable": {
      "api": "/erp/engine/dictionary/normal_disable_dict?moduleCode={moduleCode}",
      "labelField": "label",
      "valueField": "value",
      "extraFields": ["list_class"]
    },
    "unit": {
      "api": "/erp/engine/dictionary/unit_dict?moduleCode={moduleCode}",
      "labelField": "label",
      "valueField": "value"
    },
    "tradeType": {
      "api": "/erp/engine/dictionary/trade_type_dict?moduleCode={moduleCode}",
      "labelField": "label",
      "valueField": "value"
    }
  }
}
```

---

### 4️⃣ 前端调用示例

#### Vue 组件中使用

```vue
<template>
  <el-form :model="formData" label-width="120px">
    <!-- 销售员选择 -->
    <el-form-item label="销售员">
      <el-select v-model="formData.fsalerId" filterable>
        <el-option
          v-for="item in salespersons"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        >
          <span>{{ item.label }}</span>
          <span style="float: right; color: #8492a6; font-size: 13px">{{ item.departmentName }}</span>
        </el-option>
      </el-select>
    </el-form-item>
    
    <!-- 国家选择 -->
    <el-form-item label="国家">
      <el-select v-model="formData.countryId" filterable>
        <el-option
          v-for="item in countries"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
    </el-form-item>
    
    <!-- 币别选择 -->
    <el-form-item label="结算币别">
      <el-select v-model="formData.currencyId" filterable>
        <el-option
          v-for="item in currencies"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
    </el-form-item>
    
    <!-- 性别选择 -->
    <el-form-item label="性别">
      <el-radio-group v-model="formData.sex">
        <el-radio
          v-for="item in userSex"
          :key="item.value"
          :label="item.value"
        >{{ item.label }}</el-radio>
      </el-radio-group>
    </el-form-item>
  </el-form>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { getDictionary } from '@/api/erp/engine';

const formData = ref({});
const salespersons = ref([]);
const countries = ref([]);
const currencies = ref([]);
const userSex = ref([]);

// 加载字典数据
const loadDictionaries = async () => {
  try {
    // 销售员字典
    const salesRes = await getDictionary('salesperson_dict');
    salespersons.value = salesRes.data || [];
    
    // 国家字典
    const countryRes = await getDictionary('country_dict');
    countries.value = countryRes.data || [];
    
    // 币别字典
    const currencyRes = await getDictionary('currency_dict');
    currencies.value = currencyRes.data || [];
    
    // 性别字典
    const sexRes = await getDictionary('user_sex_dict');
    userSex.value = sexRes.data || [];
  } catch (error) {
    console.error('加载字典数据失败:', error);
  }
};

onMounted(() => {
  loadDictionaries();
});
</script>
```

---

## 🔧 技术优势

### ✅ 零代码扩展
- 新增字典只需在数据库添加配置记录
- 无需修改 Java 代码
- 无需重启服务

### ✅ 统一管理
- 所有字典配置集中在 `erp_page_config` 表
- 支持版本管理和历史记录
- 支持公共配置和权限隔离

### ✅ 灵活查询
- 支持复杂 SQL 查询（多表关联、聚合函数等）
- 支持 Redis 缓存机制
- 支持动态过滤条件

### ✅ 安全可靠
- 配置存储在数据库中
- 支持权限控制
- 可追溯变更历史

---

## 📝 配置说明

### JSON 配置结构

```json
{
  "name": "字典名称",
  "description": "字典描述",
  "query": {
    "sql": "SQL 查询语句",
    "resultType": "list"  // 固定为 list
  },
  "cacheable": true,     // 是否启用缓存
  "cacheTTL": 3600,      // 缓存时间（秒）
  "filters": {           // 可选的过滤条件
    "status": "1"
  }
}
```

### SQL 编写规范

1. **字段别名**: 必须使用 `as label` 和 `as value` 映射显示字段和值字段
2. **额外字段**: 可以直接 SELECT 其他需要的字段（如 `departmentName`, `fseller`）
3. **排序**: 建议使用 `ORDER BY` 保证结果有序
4. **条件**: 建议添加状态过滤（如 `WHERE status = '1'`）

---

## 🎉 总结

这套方案**完全基于现有的配置化架构**,无需创建新的接口和服务,只需要:

1. ✅ 在 `erp_page_config` 表中配置字典 SQL
2. ✅ 前端在 JSON 配置中声明 API 路径
3. ✅ 使用统一的 `/erp/engine/dictionary/{moduleCode}` 接口

真正实现**配置化驱动、零代码扩展**的字典查询能力!
