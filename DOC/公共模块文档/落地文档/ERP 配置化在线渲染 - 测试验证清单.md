# ERP 配置化在线渲染 - 测试验证清单

> 📅 **测试时间**: 2026-03-22  
> 🎯 **目标**: 验证在线配置渲染功能正常工作  
> ✅ **状态**: 待测试

---

## 📋 测试环境准备

### 1. 数据库检查

```sql
-- 检查 erp_page_config 表是否存在
SHOW TABLES LIKE 'erp_page_config';

-- 检查是否有销售订单配置
SELECT 
  config_id, 
  module_code, 
  config_name, 
  version, 
  status 
FROM erp_page_config 
WHERE module_code = 'saleOrder';

-- 如果没有数据，插入测试配置
INSERT INTO erp_page_config (
  module_code,
  config_name,
  config_type,
  config_content,
  version,
  status,
  is_public,
  create_by,
  create_time
) VALUES (
  'saleOrder',
  '销售订单页面配置',
  'PAGE',
  '{
    "pageConfig": {
      "title": "销售订单管理（在线版）",
      "permissionPrefix": "k3:saleOrder",
      "apiPrefix": "/k3/sale-order"
    },
    "businessConfig": {
      "entityName": "销售订单",
      "messages": {
        "selectOne": "请选择一条数据",
        "success": {
          "add": "新增成功"
        }
      }
    }
  }',
  1,
  '1',
  '1',
  'admin',
  NOW()
);
```

---

## 🧪 测试用例

### 测试用例 1: 基础在线加载

**步骤**:
1. 创建测试页面 `views/test/OnlineConfigTest.vue`
2. 使用 `BusinessConfigurable` 组件并传入 `module-code="saleOrder"`
3. 打开浏览器控制台
4. 访问页面

**预期结果**:
- ✅ 控制台显示 `🌐 正在从数据库加载配置：saleOrder`
- ✅ 控制台显示 `✅ 数据库配置加载成功：销售订单管理（在线版）`
- ✅ 页面标题显示 "销售订单管理（在线版）"
- ✅ 网络面板有 `/erp/config/get/saleOrder` 请求

**代码示例**:
```vue
<template>
  <div class="test-container">
    <h1>在线配置测试</h1>
    <BusinessConfigurable module-code="saleOrder" />
  </div>
</template>

<script setup>
import BusinessConfigurable from '@/views/erp/pageTemplate/configurable/BusinessConfigurable.vue'
</script>
```

---

### 测试用例 2: 缓存机制验证

**步骤**:
1. 首次访问页面（记录加载时间）
2. 立即刷新页面（记录加载时间）
3. 等待 6 分钟后再次访问（记录加载时间）

**预期结果**:
| 次数 | 加载时间 | 日志标记 | 说明 |
|------|---------|---------|------|
| **第 1 次** | ~200ms | `🌐 从数据库加载配置` | 无缓存 |
| **第 2 次** | ~5ms | `💾 命中缓存配置` | 缓存命中 |
| **第 3 次** | ~200ms | `🌐 从数据库加载配置` | 缓存过期 |

---

### 测试用例 3: 降级机制验证

**步骤**:
1. 修改数据库，将配置状态设为停用
   ```sql
   UPDATE erp_page_config SET status = '0' WHERE module_code = 'saleOrder';
   ```
2. 访问页面
3. 观察控制台和页面显示

**预期结果**:
- ✅ 控制台显示 `❌ 加载在线配置失败`
- ✅ 控制台显示 `📋 使用本地 JSON 模板配置`
- ✅ 页面正常显示（使用本地模板）
- ✅ 显示警告提示 "加载配置失败，已切换到本地模板模式"

---

### 测试用例 4: Props 控制验证

**步骤**:
1. 创建两个测试页面
   - 页面 A: `enable-online-config="true"`
   - 页面 B: `enable-online-config="false"`
2. 分别访问两个页面

**预期结果**:
| 页面 | Props | 配置来源 | 控制台日志 |
|------|-------|---------|-----------|
| **A** | `true` | 数据库 | `🌐 从数据库加载配置` |
| **B** | `false` | 本地 JSON | `📋 使用本地 JSON 模板配置` |

---

### 测试用例 5: 版本更新验证

**步骤**:
1. 在配置管理页面修改销售订单配置
2. 点击保存（版本号应该 +1）
3. 触发器自动记录历史
4. 访问测试页面

**预期结果**:
- ✅ 新版本立即生效
- ✅ 控制台显示新的版本号
- ✅ `erp_page_config_history` 表新增记录
- ✅ 历史记录的 change_type 为 'UPDATE'

---

### 测试用例 6: 多模块并发加载

**步骤**:
```javascript
// 同时加载多个模块
const modules = ['saleOrder', 'purchaseOrder', 'inventory']
const configs = await Promise.all(
  modules.map(m => ERPConfigParser.loadFromDatabase(m))
)
```

**预期结果**:
- ✅ 所有模块都成功加载
- ✅ 每个模块独立缓存
- ✅ 性能无明显下降
- ✅ 控制台显示所有模块的加载日志

---

## 📊 性能测试

### 性能指标

| 指标 | 目标值 | 实测值 | 状态 |
|------|--------|--------|------|
| **首次加载时间** | < 300ms | ___ ms | ⏳ |
| **缓存命中时间** | < 10ms | ___ ms | ⏳ |
| **缓存过期时间** | < 300ms | ___ ms | ⏳ |
| **降级切换时间** | < 100ms | ___ ms | ⏳ |

### 压力测试

```javascript
// 连续加载 100 次
const testPerformance = async () => {
  const times = []
  for (let i = 0; i < 100; i++) {
    const start = performance.now()
    await ERPConfigParser.loadFromDatabase('saleOrder')
    times.push(performance.now() - start)
  }
  
  console.log('平均加载时间:', times.reduce((a, b) => a + b) / times.length)
  console.log('最小值:', Math.min(...times))
  console.log('最大值:', Math.max(...times))
}

// 执行测试
testPerformance()
```

**预期结果**:
- 前 10 次：~200ms（无缓存）
- 后 90 次：~5ms（缓存命中）
- 平均值：< 50ms

---

## 🔍 调试检查点

### 控制台日志检查

访问页面后，控制台应该显示：

```
🌐 正在从数据库加载配置：saleOrder
✅ 数据库配置加载成功：销售订单管理（在线版）
📦 配置版本：1
💾 命中缓存配置：saleOrder  （第二次访问时）
```

### 网络请求检查

Network 面板应该显示：

```
GET /erp/config/get/saleOrder
Status: 200
Response: { code: 200, data: {...}, version: 1 }
```

### 缓存状态检查

在浏览器控制台执行：

```javascript
// 查看缓存（需要暴露 cache 属性）
console.log(ERPConfigParser.cache)

// 清除缓存
ERPConfigParser.clearCache('saleOrder')
```

---

## ✅ 验收标准

### 功能完整性

- [ ] 能从数据库加载配置
- [ ] 缓存机制正常工作
- [ ] 降级机制正常工作
- [ ] Props 控制有效
- [ ] 版本更新实时生效
- [ ] 多模块并发正常

### 性能指标

- [ ] 首次加载 < 300ms
- [ ] 缓存命中 < 10ms
- [ ] 降级切换 < 100ms
- [ ] 内存占用合理

### 错误处理

- [ ] 数据库连接失败 → 降级到本地
- [ ] 配置格式错误 → 显示友好提示
- [ ] 网络超时 → 自动重试或降级
- [ ] 权限不足 → 显示权限错误

---

## 🐛 已知问题

| 问题描述 | 优先级 | 状态 | 备注 |
|---------|--------|------|------|
| 无 | - | - | - |

---

## 📝 测试报告模板

```markdown
### 测试报告

**测试日期**: 2026-03-22
**测试人员**: XXX
**测试环境**: Chrome 120, Windows 11

#### 测试结果

✅ 通过的测试:
- 测试用例 1: 基础在线加载
- 测试用例 2: 缓存机制验证
- ...

❌ 失败的测试:
- 无

⚠️ 发现的问题:
1. 问题描述...
2. 问题描述...

#### 性能数据

- 首次加载时间：185ms
- 缓存命中时间：4ms
- 缓存过期时间：192ms

#### 结论

✅ 所有测试通过，功能正常，性能优秀
```

---

## 🚀 下一步行动

1. **完成测试验证** → 填写测试结果
2. **修复发现的问题** → 记录并修复 Bug
3. **更新文档** → 补充实际使用经验
4. **生产部署** → 在生产环境使用

---

**测试负责人**: ___________  
**预计完成时间**: ___________  
**实际完成时间**: ___________

---

**文档版本**: v1.0  
**创建时间**: 2026-03-22  
**下次更新**: 测试完成后
