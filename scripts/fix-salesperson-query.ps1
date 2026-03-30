# 🔧 销售人员编码查询问题 - 一键修复脚本

**版本**: v1.0  
**生成时间**: 2026-03-30  
**用途**: 自动修复前端字段绑定错误

---

## 📋 **修复步骤**

### **Step 1: 定位问题文件（自动）**

```bash
# 在 PowerShell 中执行以下命令

# 1. 搜索所有使用销售人员选择器的页面
Write-Host "🔍 正在搜索使用销售人员选择器的页面..." -ForegroundColor Cyan
Select-String -Path "baiyu-web/src/views/erp/**/*.vue" -Pattern "salespersons|销售人员|销售员" -CaseSensitive:$false | 
  Select-Object Path, LineNumber, Line | 
  Format-Table -AutoSize

# 2. 搜索可能错误的字段绑定
Write-Host "`n🔍 正在搜索可能的错误字段绑定..." -ForegroundColor Cyan
Select-String -Path "baiyu-web/src/views/erp/**/*.vue" -Pattern "FSalerId|F_SaleManID|f_saler_id" -CaseSensitive:$false | 
  Select-Object Path, LineNumber, Line | 
  Format-Table -AutoSize

# 3. 搜索 value 绑定（可能是 user_id）
Write-Host "`n🔍 正在搜索 value 绑定..." -ForegroundColor Cyan
Select-String -Path "baiyu-web/src/views/erp/**/*.vue" -Pattern ":value=\"opt\.value\"" | 
  Select-Object Path, LineNumber, Line | 
  Format-Table -AutoSize
```

---

### **Step 2: 手动修复模板**

找到问题文件后，使用以下模板进行修复:

#### **修复前** ❌
```vue
<template>
  <div class="salesperson-selector">
    <el-select v-model="formData.FSalerId" placeholder="请选择销售人员">
      <el-option
        v-for="opt in dictionaryManager.getDict('salespersons')"
        :key="opt.value"
        :label="opt.label"
        :value="opt.value"  <!-- ❌ 错误：使用 user_id -->
      />
    </el-select>
  </div>
</template>

<script setup>
// 查询条件构建
const buildQueryParams = () => {
  return {
    conditions: [
      {
        field: 'FSalerId',  // ❌ 字段名不一致
        operator: 'eq',
        value: formData.FSalerId  // ❌ 这是 user_id (BIGINT)
      }
    ]
  }
}
</script>
```

#### **修复后** ✅
```vue
<template>
  <div class="salesperson-selector">
    <el-select v-model="formData.fseller" placeholder="请选择销售人员">
      <el-option
        v-for="opt in dictionaryManager.getDict('salespersons')"
        :key="opt.fseller"
        :label="opt.label"
        :value="opt.fseller"  <!-- ✅ 正确：使用 salesman_id -->
      />
    </el-select>
  </div>
</template>

<script setup>
// 查询条件构建
const buildQueryParams = () => {
  return {
    conditions: [
      {
        field: 'fseller',  // ✅ 字段名与后端一致
        operator: 'eq',
        value: formData.fseller  // ✅ 这是 salesman_id (VARCHAR)
      }
    ]
  }
}
</script>
```

---

### **Step 3: 批量修复脚本（PowerShell）**

如果你找到了多个需要修复的文件，可以使用以下脚本批量替换:

```powershell
# 批量修复脚本 - 谨慎使用！使用前请先备份文件

$files = Get-ChildItem -Path "baiyu-web/src/views/erp" -Recurse -Filter "*.vue"

foreach ($file in $files) {
    $content = Get-Content $file.FullName -Raw -Encoding UTF8
    
    # 检查是否包含销售人员相关代码
    if ($content -match 'salespersons') {
        Write-Host "📝 发现目标文件：$($file.Name)" -ForegroundColor Yellow
        
        # 显示当前绑定情况
        if ($content -match ':value="opt\.value"') {
            Write-Host "  ⚠️  发现 :value=`"opt.value`" 绑定" -ForegroundColor Red
        }
        
        if ($content -match 'FSalerId|F_SaleManID') {
            Write-Host "  ⚠️  发现 FSalerId 或 F_SaleManID 字段" -ForegroundColor Red
        }
        
        # 询问是否修复
        $response = Read-Host "  是否修复此文件？(y/n)"
        if ($response -eq 'y') {
            # 备份文件
            Copy-Item $file.FullName "$($file.FullName).backup"
            
            # 执行替换（根据实际情况调整）
            $content = $content -replace 'v-model="formData\.FSalerId"', 'v-model="formData.fseller"'
            $content = $content -replace 'v-model="formData\.F_SaleManID"', 'v-model="formData.fseller"'
            $content = $content -replace ':value="opt\.value"', ':value="opt.fseller"'
            $content = $content -replace ':key="opt\.value"', ':key="opt.fseller"'
            $content = $content -replace "field: ['\"]FSalerId['\"]", "field: 'fseller'"
            $content = $content -replace "field: ['\"]F_SaleManID['\"]", "field: 'fseller'"
            
            # 保存文件
            Set-Content -Path $file.FullName -Value $content -Encoding UTF8 -NoNewline
            Write-Host "  ✅ 已修复并备份" -ForegroundColor Green
        }
    }
}

Write-Host "`n✅ 批量修复完成！" -ForegroundColor Green
```

---

### **Step 4: 验证修复结果**

#### **方法 1: 浏览器控制台验证**

打开修复后的页面，在控制台执行:

```javascript
// 1. 获取字典数据
const salespersons = dictionaryManager.getDict('salespersons')
console.log('销售人员字典:', salespersons)

// 2. 检查表单绑定
console.log('表单中的 fseller:', formData.fseller)
console.log('类型:', typeof formData.fseller)

// 3. 模拟选择操作
console.log('\n=== 模拟选择销售员 ===')
const target = salespersons.find(s => s.fseller === '100949')
console.log('选择的销售员:', target)
console.log('应该传递的值:', target.fseller)

// 4. 检查查询条件
console.log('\n=== 查询条件 ===')
console.log({
  field: 'fseller',
  operator: 'eq',
  value: target.fseller  // 应该是字符串 "100949"
})
```

#### **方法 2: Network 面板验证**

1. 打开浏览器开发者工具
2. 切换到 Network 标签
3. 点击查询按钮
4. 找到表格查询请求
5. 查看 Request Payload:

```json
{
  "moduleCode": "sale_order",
  "conditions": [
    {
      "field": "fseller",      // ✅ 字段名正确
      "operator": "eq",
      "value": "100949"        // ✅ 值是字符串 "100949"
    }
  ]
}
```

---

## 🎯 **常见修复场景**

### **场景 1: 销售订单列表页**

**文件路径**: `baiyu-web/src/views/erp/sale/order/index.vue`

**修复内容**:
```diff
- <el-select v-model="formData.FSalerId">
+ <el-select v-model="formData.fseller">
    <el-option
-     :value="opt.value"
+     :value="opt.fseller"
-     :key="opt.value"
+     :key="opt.fseller"
    />
  </el-select>

- field: 'FSalerId'
+ field: 'fseller'
```

---

### **场景 2: 销售统计报表**

**文件路径**: `baiyu-web/src/views/erp/sale/statistics/index.vue`

**修复内容**:
```diff
const queryConditions = computed(() => {
  return {
    conditions: [
      {
-       field: 'F_SaleManID',
+       field: 'fseller',
        operator: 'eq',
-       value: selectedSalerId  // 这是 user_id
+       value: selectedSalerId  // 现在是 salesman_id
      }
    ]
  }
})
```

---

### **场景 3: 客户跟进记录**

**文件路径**: `baiyu-web/src/views/erp/crm/followup/index.vue`

**修复内容**:
```diff
- const salerId = ref('')  // 存储的是 user_id
+ const fseller = ref('')  // 存储的是 salesman_id

- dictionaryManager.getDict('salespersons').find(
-   s => s.value === salerId.value
- )
+ dictionaryManager.getDict('salespersons').find(
+   s => s.fseller === fseller.value
+ )
```

---

## ⚠️ **注意事项**

### **1. 备份优先**

修复前务必备份所有修改的文件:

```bash
# PowerShell - 备份所有 Vue 文件
Get-ChildItem -Path "baiyu-web/src/views/erp" -Recurse -Filter "*.vue" | 
  ForEach-Object {
    Copy-Item $_.FullName "$($_.FullName).backup"
  }
```

### **2. 逐步验证**

每修复一个文件后:
1. ✅ 保存文件
2. ✅ 刷新页面
3. ✅ 测试查询功能
4. ✅ 确认无控制台错误

### **3. 类型转换**

如果修复后出现类型错误，可能需要显式转换:

```javascript
// 确保是字符串类型
value: String(formData.fseller)

// 或者
value: formData.fseller?.toString()
```

---

## 🐛 **常见问题排查**

### **问题 1: 修复后下拉框不显示选项**

**原因**: key 值不匹配

**解决方案**:
```diff
<el-option
- :key="opt.value"
+ :key="opt.fseller || opt.value"  // 兼容两种情况
  :label="opt.label"
  :value="opt.fseller"
/>
```

---

### **问题 2: 查询报错 "字段不存在"**

**原因**: 后端表中没有 `fseller` 字段

**解决方案**:
1. 检查后端表的实际字段名
2. 可能是 `F_SellerId`, `FSalerID` 等
3. 使用实际的数据库字段名

```sql
-- 查看销售订单表的字段
DESCRIBE t_sale_order;
```

---

### **问题 3: 选择后表单值为空**

**原因**: v-model 绑定对象属性而非基本类型

**解决方案**:
```javascript
// ❌ 错误
const formData = ref({
  fseller: {}  // 对象
})

// ✅ 正确
const formData = ref({
  fseller: ''  // 字符串
})
```

---

## 📊 **修复进度跟踪表**

| 文件名 | 问题类型 | 修复状态 | 验证结果 | 备注 |
|--------|---------|---------|---------|------|
| index.vue | value 绑定错误 | ⏳ 待修复 | - | - |
| list.vue | FSalerId 字段错误 | ⏳ 待修复 | - | - |
| statistics.vue | 查询条件错误 | ⏳ 待修复 | - | - |

---

## 🎉 **修复完成检查清单**

全部修复完成后，请确认:

- [ ] 所有使用销售人员选择器的页面都已修复
- [ ] 选择器绑定到 `formData.fseller`
- [ ] 选项的 `:value` 和 `:key` 都使用 `opt.fseller`
- [ ] 查询条件字段名为 `fseller`
- [ ] 浏览器控制台无错误
- [ ] Network 请求中的值是正确的销售员编码
- [ ] 查询结果与选择的销售员一致
- [ ] 已备份所有修改的文件

---

## 📞 **需要帮助？**

如果使用脚本过程中遇到任何问题，请:

1. 截图错误信息
2. 提供具体的文件路径
3. 说明修复前后的变化

我可以帮你远程调试和修复！

---

**🚀 开始修复吧！如有问题随时找我。**
