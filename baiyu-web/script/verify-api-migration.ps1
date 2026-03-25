# ERP 前端 API 文件迁移验证脚本
# 
# 功能：验证 API 文件迁移是否成功
# 使用方式：.\verify-api-migration.ps1
# 
# @author ERP Development Team
# @date 2026-03-25

param(
    [switch]$Verbose,
    [switch]$FixAll
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "ERP 前端 API 文件迁移验证工具" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 颜色配置
$successColor = "Green"
$errorColor = "Red"
$infoColor = "Yellow"
$warningColor = "Orange"

# 测试结果统计
$testResults = @{
    Total = 0
    Passed = 0
    Failed = 0
    Warning = 0
}

# ========================================
# 测试 1: 检查新的 API 目录是否存在
# ========================================
Write-Host "[测试 1] 检查新的 API 目录结构..." -ForegroundColor $infoColor
$testResults.Total++

$newApiPath = "src\views\erp\api"
if (Test-Path $newApiPath) {
    Write-Host "  ✅ PASS: 新 API 目录存在 - $newApiPath" -ForegroundColor $successColor
    $testResults.Passed++
    
    # 显示目录结构
    if ($Verbose) {
        Write-Host "  📁 目录结构:" -ForegroundColor $infoColor
        Get-ChildItem -Path $newApiPath -Recurse -File | Select-Object FullName | Format-Table -AutoSize
    }
} else {
    Write-Host "  ❌ FAIL: 新 API 目录不存在 - $newApiPath" -ForegroundColor $errorColor
    $testResults.Failed++
}

Write-Host ""

# ========================================
# 测试 2: 检查旧的 API 目录
# ========================================
Write-Host "[测试 2] 检查旧的 API 目录状态..." -ForegroundColor $infoColor
$testResults.Total++

$oldApiPath = "src\api\erp"
if (Test-Path $oldApiPath) {
    Write-Host "  ⚠️  WARNING: 旧 API 目录仍然存在 - $oldApiPath" -ForegroundColor $warningColor
    Write-Host "     建议：确认所有引用都已更新后，可以删除此目录" -ForegroundColor $warningColor
    $testResults.Warning++
} else {
    Write-Host "  ✅ PASS: 旧 API 目录已删除" -ForegroundColor $successColor
    $testResults.Passed++
}

Write-Host ""

# ========================================
# 测试 3: 检查 Vue 文件中的引用路径
# ========================================
Write-Host "[测试 3] 检查 Vue 文件中的 API 引用..." -ForegroundColor $infoColor
$testResults.Total++

$vueFiles = @(
    "src\views\erp\config\index.vue",
    "src\views\erp\pageTemplate\configurable\BusinessConfigurable.vue",
    "src\views\erp\ConfigDrivenPage\saleorder\configurable\saleorder.vue"
)

$incorrectImports = 0
foreach ($vueFile in $vueFiles) {
    if (Test-Path $vueFile) {
        $content = Get-Content $vueFile -Raw
        
        # 检查是否存在旧的引用
        if ($content -match "from ['`]@/api/erp/") {
            Write-Host "  ❌ FAIL: $vueFile 仍然使用旧的引用路径" -ForegroundColor $errorColor
            $incorrectImports++
            
            if ($Verbose) {
                $matches = [regex]::Matches($content, "from ['`]@/api/erp/[^'`"]*")
                foreach ($match in $matches) {
                    Write-Host "     错误引用：$($match.Value)" -ForegroundColor $errorColor
                }
            }
        } else {
            Write-Host "  ✅ PASS: $vueFile 引用路径正确" -ForegroundColor $successColor
        }
    } else {
        Write-Host "  ⚠️  WARNING: 文件不存在 - $vueFile" -ForegroundColor $warningColor
    }
}

if ($incorrectImports -eq 0) {
    $testResults.Passed++
} else {
    $testResults.Failed += $incorrectImports
}

Write-Host ""

# ========================================
# 测试 4: 检查 API 文件内容
# ========================================
Write-Host "[测试 4] 检查 API 文件完整性..." -ForegroundColor $infoColor
$testResults.Total++

$requiredApiFiles = @(
    "src\views\erp\api\config.js",
    "src\views\erp\api\engine\index.js",
    "src\views\erp\api\engine\query.js",
    "src\views\erp\api\engine\validation.js",
    "src\views\erp\api\engine\approval.js",
    "src\views\erp\api\engine\push.js"
)

$missingFiles = 0
foreach ($file in $requiredApiFiles) {
    if (Test-Path $file) {
        Write-Host "  ✅ PASS: $file" -ForegroundColor $successColor
    } else {
        Write-Host "  ❌ FAIL: $file 不存在" -ForegroundColor $errorColor
        $missingFiles++
    }
}

if ($missingFiles -eq 0) {
    $testResults.Passed++
} else {
    $testResults.Failed += $missingFiles
}

Write-Host ""

# ========================================
# 测试 5: 检查 README 文档
# ========================================
Write-Host "[测试 5] 检查文档完整性..." -ForegroundColor $infoColor
$testResults.Total++

$readmePath = "src\views\erp\api\README.md"
if (Test-Path $readmePath) {
    Write-Host "  ✅ PASS: README 文档存在 - $readmePath" -ForegroundColor $successColor
    $testResults.Passed++
} else {
    Write-Host "  ❌ FAIL: README 文档不存在 - $readmePath" -ForegroundColor $errorColor
    $testResults.Failed++
}

Write-Host ""

# ========================================
# 测试 6: 自动修复（如果启用）
# ========================================
if ($FixAll) {
    Write-Host "[测试 6] 执行自动修复..." -ForegroundColor $infoColor
    $testResults.Total++
    
    # 查找并修复所有 Vue 文件中的错误引用
    $allVueFiles = Get-ChildItem -Path "src\views\erp" -Filter "*.vue" -Recurse
    $fixedCount = 0
    
    foreach ($file in $allVueFiles) {
        $content = Get-Content $file.FullName -Raw
        
        # 替换错误的引用
        if ($content -match "@/api/erp/engine/query") {
            $content = $content -replace "@/api/erp/engine/query", "../../api/engine/query"
            Set-Content -Path $file.FullName -Value $content -NoNewline
            Write-Host "  ✅ 已修复：$($file.Name)" -ForegroundColor $successColor
            $fixedCount++
        }
        
        if ($content -match "@/api/erp/config") {
            $content = $content -replace "@/api/erp/config", "../api/config"
            Set-Content -Path $file.FullName -Value $content -NoNewline
            Write-Host "  ✅ 已修复：$($file.Name)" -ForegroundColor $successColor
            $fixedCount++
        }
    }
    
    if ($fixedCount -gt 0) {
        Write-Host "  ✅ 共修复 $fixedCount 处错误引用" -ForegroundColor $successColor
    } else {
        Write-Host "  ℹ️  无需修复" -ForegroundColor $infoColor
    }
    
    $testResults.Passed++
}

Write-Host ""

# ========================================
# 测试结果汇总
# ========================================
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "测试结果汇总" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "总测试数：$($testResults.Total)" -ForegroundColor $infoColor
Write-Host "通过：$($testResults.Passed)" -ForegroundColor $successColor
Write-Host "失败：$($testResults.Failed)" -ForegroundColor $errorColor
Write-Host "警告：$($testResults.Warning)" -ForegroundColor $warningColor
Write-Host ""

if ($testResults.Failed -eq 0 -and $testResults.Warning -eq 0) {
    Write-Host "🎉 所有测试通过！API 文件迁移成功！" -ForegroundColor $successColor
    Write-Host ""
    Write-Host "下一步：" -ForegroundColor $infoColor
    Write-Host "  1. 运行项目测试所有功能" -ForegroundColor White
    Write-Host "  2. 确认无编译错误" -ForegroundColor White
    Write-Host "  3. 考虑删除旧的 api/erp 目录" -ForegroundColor White
} elseif ($testResults.Failed -eq 0) {
    Write-Host "⚠️  测试基本通过，但存在警告" -ForegroundColor $warningColor
    Write-Host ""
    Write-Host "建议：" -ForegroundColor $infoColor
    Write-Host "  - 确认旧的 api/erp 目录是否可以删除" -ForegroundColor White
} else {
    Write-Host "❌ 存在失败的测试，请检查上述错误" -ForegroundColor $errorColor
    Write-Host ""
    Write-Host "建议：" -ForegroundColor $infoColor
    Write-Host "  - 使用 -FixAll 参数尝试自动修复" -ForegroundColor White
    Write-Host "  - 手动检查失败的测试项" -ForegroundColor White
}

Write-Host ""
Write-Host "相关文档：" -ForegroundColor $infoColor
Write-Host "  - src/views/erp/api/README.md" -ForegroundColor White
Write-Host ""

# 返回退出码
if ($testResults.Failed -eq 0) {
    exit 0
} else {
    exit 1
}
