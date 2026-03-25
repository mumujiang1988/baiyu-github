# copy-module-v4.ps1 脚本优化说明

## 🎯 **优化内容**

### v4.2 版本改进（2026-03-25）

#### ✅ **移除硬编码路径**
**修复前：**
```powershell
[string]$SourcePath = "d:\baiyuyunma\baiyu-github\baiyu-github\baiyu-web\src\views\erp\pageTemplate"
[string]$DestBasePath = "d:\baiyuyunma\baiyu-github\baiyu-github\baiyu-web\src\views\erp\ConfigDrivenPage"
```

**修复后：**
```powershell
[string]$SourcePath = ""
[string]$DestBasePath = ""

# 自动计算路径（基于当前脚本位置）
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
if ([string]::IsNullOrEmpty($SourcePath)) {
    $SourcePath = $ScriptDir
}
if ([string]::IsNullOrEmpty($DestBasePath)) {
    $DestBasePath = Join-Path $ScriptDir "..\ConfigDrivenPage"
    # 转换为绝对路径
    $DestBasePath = (Resolve-Path $DestBasePath).Path -replace '\\$', ''
}
```

---

## 🔧 **技术实现**

### 1. **动态路径计算**
使用 PowerShell 内置变量和命令：
- `$MyInvocation.MyCommand.Path` - 获取当前脚本的完整路径
- `Split-Path -Parent` - 获取脚本所在目录
- `Join-Path` - 拼接路径
- `Resolve-Path` - 转换为绝对路径

### 2. **智能路径解析**
```powershell
# 脚本位置自动检测
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path

# 源路径 = 脚本所在目录
$SourcePath = $ScriptDir

# 目标路径 = 脚本所在目录的上级\ConfigDrivenPage
$DestBasePath = Join-Path $ScriptDir "..\ConfigDrivenPage"
```

### 3. **支持自定义路径**
仍然保留了参数，允许手动指定路径：
```powershell
.\copy-module-v4.ps1 saleOrder "销售订单管理" `
  -SourcePath "D:\自定义路径\pageTemplate" `
  -DestBasePath "D:\自定义路径\modules"
```

---

## 📋 **使用示例**

### 基础用法（推荐）
```powershell
# 进入脚本所在目录
cd d:\baiyuyunma\baiyu-github\baiyu-github\baiyu-web\src\views\erp\pageTemplate

# 运行脚本（自动计算路径）
.\copy-module-v4.ps1 saleOrder "销售订单管理"
```

### 高级用法（自定义路径）
```powershell
# 指定源路径和目标路径
.\copy-module-v4.ps1 purchaseOrder "采购订单管理" `
  -SourcePath "D:\projects\erp\templates" `
  -DestBasePath "D:\projects\erp\modules"
```

### 禁用某些功能
```powershell
# 只启用审核，禁用反审核和下推
.\copy-module-v4.ps1 inspection "检验管理" `
  -EnableAudit:$true `
  -EnableUnAudit:$false `
  -EnablePush:$false `
  -EnableExport:$true
```

---

## ✅ **优化效果**

| 指标 | 优化前 | 优化后 | 提升 |
|------|--------|--------|------|
| 硬编码路径 | 2 处 | 0 处 | ✅ 100% |
| 路径灵活性 | 低 | 高 | ✅ 显著提升 |
| 可移植性 | 差 | 优秀 | ✅ 跨项目通用 |
| 维护成本 | 高 | 低 | ✅ 大幅降低 |

---

## 🚀 **优势说明**

### 1. **零配置使用**
无需修改任何路径配置，直接运行即可：
```powershell
✅ .\copy-module-v4.ps1 moduleCode "模块名称"
```

### 2. **跨项目通用**
脚本可以复制到任何项目中使用，无需修改路径：
```
✅ 项目 A: D:\projectA\src\views\erp\pageTemplate
✅ 项目 B: E:\projectB\modules\erp\templates
✅ 项目 C: /home/user/projectC/src/views
```

### 3. **智能路径解析**
自动处理相对路径和绝对路径转换：
```powershell
# 自动解析为绝对路径
$DestBasePath = (Resolve-Path $DestBasePath).Path -replace '\\$', ''
```

### 4. **向后兼容**
保留手动指定路径的能力：
```powershell
# 旧代码仍然有效
.\copy-module-v4.ps1 -ModuleName "saleOrder" -SourcePath "xxx" -DestBasePath "xxx"
```

---

## 📝 **版本历史**

### v4.2 (2026-03-25)
- ✅ 移除硬编码路径
- ✅ 实现动态路径计算
- ✅ 支持自定义路径参数
- ✅ 显示路径配置信息
- ✅ 提升跨项目可移植性

### v4.1 (之前版本)
- ❌ 使用硬编码的绝对路径
- ❌ 需要在不同项目中手动修改
- ❌ 不利于代码复用

---

## 💡 **最佳实践**

### 1. **标准项目结构**
```
project/
├── src/
│   └── views/
│       └── erp/
│           ├── pageTemplate/          # 模板目录
│           │   └── copy-module-v4.ps1 # 复制脚本
│           └── ConfigDrivenPage/      # 生成的模块
│               └── moduleName/
```

### 2. **推荐使用方式**
```powershell
# ✅ 推荐：在模板目录下运行
cd src/views/erp/pageTemplate
.\copy-module-v4.ps1 moduleCode "模块名称"

# ❌ 不推荐：从其他目录运行并指定路径
.\copy-module-v4.ps1 moduleCode "模块名称" `
  -SourcePath "D:\very\long\path\to\pageTemplate"
```

### 3. **Git 版本控制**
```bash
# 将脚本加入版本控制
git add src/views/erp/pageTemplate/copy-module-v4.ps1

# 生成的模块也加入版本控制
git add src/views/erp/ConfigDrivenPage/moduleName/
```

---

## ⚠️ **注意事项**

### 1. **路径权限**
确保对源路径和目标路径有读写权限：
```powershell
# 检查路径权限
Test-Path $SourcePath
Test-Path $DestBasePath
```

### 2. **目标文件夹不存在**
如果 `ConfigDrivenPage` 目录不存在，需要先创建：
```powershell
New-Item -ItemType Directory -Path "src\views\erp\ConfigDrivenPage" -Force
```

### 3. **PowerShell 版本**
需要 PowerShell 3.0 或更高版本：
```powershell
# 检查 PowerShell 版本
$PSVersionTable.PSVersion
```

---

## 🎉 **总结**

通过移除硬编码路径，实现了：
- ✅ **零配置** - 开箱即用
- ✅ **高可移植** - 跨项目通用
- ✅ **易维护** - 无需手动修改路径
- ✅ **向后兼容** - 保留自定义能力

**优化完成时间**: 2026-03-25  
**版本号**: v4.2  
**状态**: ✅ 已完成测试
