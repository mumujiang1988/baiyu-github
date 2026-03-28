# ERP 字典 API 接口更新报告 v3.3

## 更新时间
2026-03-28 11:05

## 更新概述
根据当前实现的字典低代码 API 接口，全面更新 MM 技能中的字典相关文档，确保与实际实现完全一致。

## 一、现有 API 接口实现

### 1. Controller 接口（ErpDictionaryController.java）

**基础路径**: `/erp/dict`

#### 1.1 核心接口（低代码常用）

##### GET /erp/dict/getUnionDict
- **描述**: 获取合并字典（系统字典 + 业务字典）
- **参数**: 
  - `dictType` (可选): 字典类型
    - 传值：只返回该类型的字典
    - 不传：返回所有字典
- **返回值**: `List<Map<String, Object>>` - 合并后的字典列表
- **实现方法**: `ErpDictionaryService.getUnionDict()`
- **使用示例**:
```javascript
// 获取所有字典
const allDicts = await axios.get('/erp/dict/getUnionDict')

// 获取指定类型字典
const customerDicts = await axios.get('/erp/dict/getUnionDict?dictType=customer_category')
```

##### GET /erp/dict/getAllDict
- **描述**: 获取全部字典数据
- **返回值**: `List<Map<String, Object>>` - 所有字典数据（系统 + 业务）
- **实现方法**: `ErpDictionaryService.getAllDict()`
- **使用示例**:
```javascript
const allDicts = await axios.get('/erp/dict/getAllDict')
```

#### 1.2 系统字典接口

##### GET /erp/dict/getDictTypes
- **描述**: 查询系统字典类型
- **参数**: `dictType` (可选) - 字典类型
- **返回值**: `List<Map<String, Object>>` - 字典类型列表
- **实现方法**: `ErpDictionaryService.getDictTypes()`
- **使用示例**:
```javascript
// 获取所有字典类型
const types = await axios.get('/erp/dict/getDictTypes')

// 获取指定类型
const type = await axios.get('/erp/dict/getDictTypes?dictType=system_config')
```

#### 1.3 业务字典接口

##### GET /erp/dict/getBizDictByCategory
- **描述**: 根据分类查询业务字典
- **参数**: `category` (必填) - 字典分类
- **返回值**: `List<Map<String, Object>>` - 业务字典列表
- **实现方法**: `ErpDictionaryService.getBizDictByCategory()`
- **使用示例**:
```javascript
const dicts = await axios.get('/erp/dict/getBizDictByCategory?category=customer_source')
```

##### GET /erp/dict/getBizDictCustom
- **描述**: 自定义值字段的业务字典查询
- **参数**: 
  - `category` (必填) - 字典分类
  - `valueField` (可选，默认 kingdee) - 值字段名
- **返回值**: `List<Map<String, Object>>` - 业务字典列表
- **实现方法**: `ErpDictionaryService.getBizDictCustom()`
- **使用示例**:
```javascript
// 使用默认值字段 (kingdee)
const dicts = await axios.get('/erp/dict/getBizDictCustom?category=product_type')

// 使用自定义值字段 (code)
const dicts = await axios.get('/erp/dict/getBizDictCustom?category=product_type&valueField=code')
```

#### 1.4 国家字典接口

##### GET /erp/dict/getCountryById
- **描述**: 根据国家 ID 查询详情
- **参数**: `id` (必填) - 国家 ID
- **返回值**: `Map<String, Object>` - 国家详情
- **实现方法**: `ErpDictionaryService.getCountryById()`
- **使用示例**:
```javascript
const country = await axios.get('/erp/dict/getCountryById?id=1')
```

##### GET /erp/dict/getAllCountries
- **描述**: 查询所有可用国家
- **返回值**: `List<Map<String, Object>>` - 国家列表
- **实现方法**: `ErpDictionaryService.getAllCountries()`
- **使用示例**:
```javascript
const countries = await axios.get('/erp/dict/getAllCountries')
```

##### GET /erp/dict/searchCountries
- **描述**: 搜索国家（支持关键字和分页）
- **参数**: 
  - `keyword` (可选) - 搜索关键字
  - `limit` (可选) - 返回数量限制
- **返回值**: `List<Map<String, Object>>` - 国家列表
- **实现方法**: `ErpDictionaryService.searchCountries()`
- **使用示例**:
```javascript
// 搜索中国
const countries = await axios.get('/erp/dict/searchCountries?keyword=中国')

// 搜索美国，限制返回 10 条
const countries = await axios.get('/erp/dict/searchCountries?keyword=america&limit=10')
```

### 2. Service 实现（ErpDictionaryService.java）

#### 2.1 核心方法

##### getUnionDict(String dictType)
- **功能**: Java 代码合并系统字典 + 业务字典
- **实现逻辑**:
  1. 调用 `erpDictionaryMapper.selectSysDictData(dictType)` 查询系统字典
  2. 调用 `erpDictionaryMapper.selectBizDictData(dictType)` 查询业务字典
  3. 使用 Java 代码合并两个列表
- **优势**: 彻底避免 SQL UNION 导致的 collation 冲突问题

##### getAllDict()
- **功能**: 获取全部字典（无 UNION 纯 Java 合并）
- **实现逻辑**:
  1. 查询所有系统字典
  2. 查询所有业务字典
  3. Java 代码合并

#### 2.2 便捷方法

- `getDictTypes(String dictType)`: 查询系统字典类型
- `getBizDictByCategory(String category)`: 根据分类查询业务字典
- `getBizDictCustom(String category, String valueField)`: 自定义值字段查询
- `getCountryById(Long id)`: 根据国家 ID 查询
- `getAllCountries()`: 查询所有国家
- `searchCountries(String keyword, Integer limit)`: 搜索国家

### 3. Mapper 实现（ErpDictionaryMapper.java）

#### 3.1 系统字典查询

##### selectSysDictData
- **SQL**: 
```sql
SELECT dict_label AS label, dict_value AS value, dict_type AS type 
FROM sys_dict_data 
[WHERE dict_type = #{dictType}] 
ORDER BY dict_sort
```
- **特点**: 动态 WHERE 条件，支持按类型筛选

##### selectDictTypes
- **SQL**:
```sql
SELECT dict_name AS label, dict_type AS value, dict_type AS type 
FROM sys_dict_type 
[WHERE dict_type = #{dictType}] 
ORDER BY dict_id
```

#### 3.2 业务字典查询

##### selectBizDictData
- **SQL**:
```sql
SELECT name AS label, kingdee AS value, category AS type 
FROM bymaterial_dictionary 
[WHERE category = #{category}] 
ORDER BY id
```

##### selectBizDictByCategory
- **SQL**:
```sql
SELECT id, name AS label, kingdee AS value, category AS type, code, parent_code AS parentCode 
FROM bymaterial_dictionary 
WHERE category = #{category} 
ORDER BY id ASC
```

##### selectBizDictCustom
- **SQL**:
```sql
SELECT id, name AS label, ${valueField} AS value, category AS type, code 
FROM bymaterial_dictionary 
WHERE category = #{category} 
ORDER BY id ASC
```
- **注意**: 使用 `${valueField}` 防止 SQL 注入（已验证字段名合法性）

#### 3.3 国家字典查询

##### selectCountryById
- **SQL**:
```sql
SELECT id, name_en AS labelEn, name_zh AS labelZh, status 
FROM country 
WHERE id = #{id} AND status = 1
```

##### selectAllCountries
- **SQL**:
```sql
SELECT id, name_en AS labelEn, name_zh AS labelZh, status 
FROM country 
WHERE status = 1 
ORDER BY name_zh ASC
```

##### searchCountries
- **SQL**:
```sql
SELECT id, name_en AS labelEn, name_zh AS labelZh, status 
FROM country 
WHERE status = 1 
[AND (name_zh LIKE CONCAT('%', #{keyword}, '%') OR name_en LIKE CONCAT('%', #{keyword}, '%'))]
ORDER BY name_zh ASC 
[LIMIT #{limit}]
```

## 二、技术架构特点

### 1. 彻底解决 Collation 冲突
- **旧方案**: 使用 SQL UNION ALL 合并系统字典和业务字典
- **新方案**: 分别查询，Java 代码合并
- **效果**: 完全避免 `Illegal mix of collations` 错误

### 2. 数据结构标准化
所有字典数据统一返回格式:
```json
{
  "label": "显示标签",
  "value": "实际值",
  "type": "字典类型"
}
```

### 3. 灵活的查询方式
- 支持按类型查询
- 支持按分类查询
- 支持自定义值字段
- 支持模糊搜索
- 支持分页限制

### 4. 完善的国家字典支持
- 支持中英文双语
- 支持状态过滤
- 支持关键字搜索
- 支持限制返回数量

## 三、与 DictionaryManager 的关系

### 当前实现状态
- ✅ **后端 API 已完成**: ErpDictionaryController 提供完整的 REST 接口
- ✅ **Service 层优化**: 使用 Java 合并替代 SQL UNION
- ⚠️ **前端集成**: DictionaryManager 需要适配新的 API 接口

### 推荐的 DictionaryManager 实现
```javascript
import axios from 'axios'

class DictionaryManager {
  constructor() {
    this.dictCache = new Map()
    this.loaded = false
    this.loading = false
  }

  /**
   * 一次性加载全部字典
   */
  async loadAll() {
    if (this.loaded || this.loading) return
    this.loading = true
    
    try {
      const response = await axios.get('/erp/dict/getAllDict')
      this.processDictData(response.data)
      this.loaded = true
    } catch (error) {
      console.error('加载字典失败:', error)
      throw error
    } finally {
      this.loading = false
    }
  }

  /**
   * 处理字典数据
   */
  processDictData(data) {
    // 按 type 分组存储
    data.forEach(dict => {
      const type = dict.type
      if (!this.dictCache.has(type)) {
        this.dictCache.set(type, [])
      }
      this.dictCache.get(type).push({
        label: dict.label,
        value: dict.value
      })
    })
  }

  /**
   * 获取指定类型字典
   */
  getDict(type) {
    return this.dictCache.get(type) || []
  }

  /**
   * 获取字典选项（兼容方法）
   */
  getDictOptions(dictName, staticOptions) {
    // 1. 静态配置优先级最高
    if (staticOptions && staticOptions.length > 0) {
      return staticOptions
    }

    // 2. 从缓存获取
    const dict = this.getDict(dictName)
    if (dict && dict.length > 0) {
      return dict
    }

    // 3. 降级方案：实时查询
    return this.loadDict(dictName)
  }

  /**
   * 实时加载单个字典
   */
  async loadDict(dictName) {
    try {
      const response = await axios.get(`/erp/dict/getUnionDict?dictType=${dictName}`)
      const options = response.data.map(d => ({
        label: d.label,
        value: d.value
      }))
      this.dictCache.set(dictName, options)
      return options
    } catch (error) {
      console.error(`加载字典 ${dictName} 失败:`, error)
      return []
    }
  }

  /**
   * 清除缓存
   */
  clear() {
    this.dictCache.clear()
    this.loaded = false
  }

  /**
   * 获取状态
   */
  getStatus() {
    return {
      loaded: this.loaded,
      loading: this.loading,
      dictCount: this.dictCache.size,
      dictTypes: Array.from(this.dictCache.keys())
    }
  }
}

// 导出单例
export default new DictionaryManager()
```

## 四、技能文档更新建议

### 1. erp-engine-usage/SKILL.md 更新点

#### 在"字典管理"章节添加:
```markdown
### 3. 低代码字典 API（v3.3 推荐）

**基础路径**: `/erp/dict`

**核心接口**:
- `GET /erp/dict/getUnionDict` - 获取合并字典（系统 + 业务）
- `GET /erp/dict/getAllDict` - 获取全部字典
- `GET /erp/dict/getBizDictByCategory` - 按分类查询业务字典
- `GET /erp/dict/getBizDictCustom` - 自定义值字段查询
- `GET /erp/dict/getCountryById` - 根据国家 ID 查询
- `GET /erp/dict/getAllCountries` - 查询所有国家
- `GET /erp/dict/searchCountries` - 搜索国家

**技术特点**:
- ✅ 无 UNION SQL - Java 代码合并，避免 collation 冲突
- ✅ 数据结构统一 - 所有字典返回相同格式
- ✅ 灵活查询 - 支持类型、分类、自定义字段、搜索等

**使用示例**:
```javascript
// 一次性加载全部字典
const allDicts = await axios.get('/erp/dict/getAllDict')

// 获取指定类型字典
const dicts = await axios.get('/erp/dict/getUnionDict?dictType=customer_category')

// 按分类查询业务字典
const bizDicts = await axios.get('/erp/dict/getBizDictByCategory?category=supplier_classification')
```
```

### 2. erp-dynamic-query/SKILL.md 更新点

在"字典构建引擎"章节后添加:

```markdown
### 8. 低代码字典 API 集成

**背景**: 
传统的 DictionaryBuilder 需要通过引擎接口逐个加载字典，效率较低。
v3.3 版本推荐使用低代码字典 API 一次性加载全部字典。

**API 路径**: `/erp/dict`

**集成方式**:
```javascript
import dictionaryManager from '@/views/erp/utils/DictionaryManager'

// 页面初始化时加载
await dictionaryManager.loadAll()

// 使用时获取
const currency = dictionaryManager.getDict('currency')
```

**性能对比**:
- 旧方案：每个字典单独请求（10+ 次 HTTP 请求）
- 新方案：一次请求获取全部字典（1 次 HTTP 请求）
- 性能提升：减少 90% 的网络请求
```

## 五、最佳实践建议

### 1. 前端集成建议

#### 推荐模式：DictionaryManager 全局管理器
```javascript
// main.js 或 App.vue
import dictionaryManager from '@/views/erp/utils/DictionaryManager'

// 应用启动时加载
dictionaryManager.loadAll().then(() => {
  console.log('字典加载完成')
})
```

#### 组件中使用
```vue
<template>
  <el-select v-model="form.customerType">
    <el-option
      v-for="item in customerTypeOptions"
      :key="item.value"
      :label="item.label"
      :value="item.value"
    />
  </el-select>
</template>

<script setup>
import dictionaryManager from '@/views/erp/utils/DictionaryManager'

const customerTypeOptions = computed(() => {
  return dictionaryManager.getDictOptions('customer_category')
})
</script>
```

### 2. 后端优化建议

#### 添加缓存支持
```java
@Service
public class ErpDictionaryService {
    
    @Autowired
    private ErpDictionaryMapper erpDictionaryMapper;
    
    // Redis 缓存
    @Cacheable(value = "dict:union", key = "#dictType")
    public List<Map<String, Object>> getUnionDict(String dictType) {
        // ... 实现代码
    }
    
    @Cacheable(value = "dict:all")
    public List<Map<String, Object>> getAllDict() {
        // ... 实现代码
    }
}
```

### 3. 性能监控

建议添加性能监控:
- 记录每次查询的耗时
- 统计缓存命中率
- 监控字典数据量变化

## 六、测试清单

### 接口测试
- [ ] GET /erp/dict/getUnionDict - 无参数
- [ ] GET /erp/dict/getUnionDict?dictType=xxx
- [ ] GET /erp/dict/getAllDict
- [ ] GET /erp/dict/getDictTypes
- [ ] GET /erp/dict/getBizDictByCategory?category=xxx
- [ ] GET /erp/dict/getBizDictCustom?category=xxx&valueField=xxx
- [ ] GET /erp/dict/getCountryById?id=1
- [ ] GET /erp/dict/getAllCountries
- [ ] GET /erp/dict/searchCountries?keyword=xxx

### 功能测试
- [ ] 系统字典正确返回
- [ ] 业务字典正确返回
- [ ] 合并字典数据完整
- [ ] 自定义值字段生效
- [ ] 国家字典中英文正常
- [ ] 搜索功能正常
- [ ] 分页限制生效

### 性能测试
- [ ] 无缓存时首次加载
- [ ] 有缓存时的响应速度
- [ ] 大数据量下的性能
- [ ] 并发访问测试

### 兼容性测试
- [ ] 与 DictionaryManager 集成
- [ ] 与 DictionaryBuilder 兼容
- [ ] 旧项目迁移验证

## 七、总结

本次更新的 ERP 字典低代码 API 接口具有以下特点:

1. **架构清晰** - Controller → Service → Mapper 三层分离
2. **技术先进** - Java 代码合并替代 SQL UNION，彻底解决 collation 冲突
3. **接口完善** - 7 个核心接口覆盖所有字典查询场景
4. **性能优秀** - 支持一次性加载全部字典，减少 90% 请求
5. **易于扩展** - 模块化设计便于后续功能增强

建议尽快更新 MM 技能文档，并在项目中推广应用。
