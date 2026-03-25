# ERP 低代码文件清理脚本
# 用于清理 ruoyi-system 模块中残留的低代码旧文件
# 这些文件已迁移到 ruoyi-erp-api 模块

Write-Host "=== 开始清理 ruoyi-system 中的低代码旧文件 ===" -ForegroundColor Cyan

$basePath = "d:\baiyuyunma\baiyu-github\baiyu-github\baiyu-ruoyi\ruoyi-modules\ruoyi-system"

# 定义需要删除的文件列表
$filesToDelete = @(
    # Controller 层
    "$basePath\src\main\java\com\ruoyi\system\controller\erp\ErpApprovalFlowController.java",
    "$basePath\src\main\java\com\ruoyi\system\controller\erp\ErpEngineController.java",
    "$basePath\src\main\java\com\ruoyi\system\controller\erp\ErpPageConfigController.java",
    "$basePath\src\main\java\com\ruoyi\system\controller\erp\ErpPushRelationController.java",
    
    # Service 接口层
    "$basePath\src\main\java\com\ruoyi\system\service\ErpApprovalFlowService.java",
    "$basePath\src\main\java\com\ruoyi\system\service\ErpPageConfigService.java",
    "$basePath\src\main\java\com\ruoyi\system\service\ErpPushRelationService.java",
    "$basePath\src\main\java\com\ruoyi\system\service\ISuperDataPermissionService.java",
    
    # Service 实现层
    "$basePath\src\main\java\com\ruoyi\system\service\impl\ErpApprovalFlowServiceImpl.java",
    "$basePath\src\main\java\com\ruoyi\system\service\impl\ErpPageConfigServiceImpl.java",
    "$basePath\src\main\java\com\ruoyi\system\service\impl\ErpPushRelationServiceImpl.java",
    "$basePath\src\main\java\com\ruoyi\system\service\impl\SuperDataPermissionServiceImpl.java",
    
    # Entity 实体类
    "$basePath\src\main\java\com\ruoyi\system\domain\entity\ErpApprovalFlow.java",
    "$basePath\src\main\java\com\ruoyi\system\domain\entity\ErpApprovalHistory.java",
    "$basePath\src\main\java\com\ruoyi\system\domain\entity\ErpPageConfig.java",
    "$basePath\src\main\java\com\ruoyi\system\domain\entity\ErpPageConfigHistory.java",
    "$basePath\src\main\java\com\ruoyi\system\domain\entity\ErpPushRelation.java",
    
    # BO 业务对象
    "$basePath\src\main\java\com\ruoyi\system\domain\bo\ErpApprovalFlowBo.java",
    "$basePath\src\main\java\com\ruoyi\system\domain\bo\ErpApprovalHistoryBo.java",
    "$basePath\src\main\java\com\ruoyi\system\domain\bo\ErpPageConfigBo.java",
    "$basePath\src\main\java\com\ruoyi\system\domain\bo\ErpPageConfigHistoryBo.java",
    "$basePath\src\main\java\com\ruoyi\system\domain\bo\ErpPushRelationBo.java",
    
    # VO 视图对象
    "$basePath\src\main\java\com\ruoyi\system\domain\vo\ErpApprovalFlowVo.java",
    "$basePath\src\main\java\com\ruoyi\system\domain\vo\ErpApprovalHistoryVo.java",
    "$basePath\src\main\java\com\ruoyi\system\domain\vo\ErpPageConfigHistoryVo.java",
    "$basePath\src\main\java\com\ruoyi\system\domain\vo\ErpPageConfigVo.java",
    "$basePath\src\main\java\com\ruoyi\system\domain\vo\ErpPushRelationVo.java",
    
    # Mapper 数据访问层
    "$basePath\src\main\java\com\ruoyi\system\mapper\ErpApprovalFlowMapper.java",
    "$basePath\src\main\java\com\ruoyi\system\mapper\ErpApprovalHistoryMapper.java",
    "$basePath\src\main\java\com\ruoyi\system\mapper\ErpPageConfigHistoryMapper.java",
    "$basePath\src\main\java\com\ruoyi\system\mapper\ErpPageConfigMapper.java",
    "$basePath\src\main\java\com\ruoyi\system\mapper\ErpPushRelationMapper.java"
)

# XML Mapper 文件（如果存在）
$xmlMappers = @(
    "$basePath\src\main\resources\mapper\system\ErpPageConfigMapper.xml",
    "$basePath\src\main\resources\mapper\system\ErpApprovalFlowMapper.xml",
    "$basePath\src\main\resources\mapper\system\ErpPushRelationMapper.xml"
)

$deletedCount = 0
$errorCount = 0

Write-Host "`n[1] 删除 Java 源文件..." -ForegroundColor Yellow

foreach ($file in $filesToDelete) {
    if (Test-Path $file) {
        try {
            Remove-Item $file -Force
            Write-Host "✅ 已删除：$file" -ForegroundColor Gray
            $deletedCount++
        } catch {
            Write-Host "❌ 删除失败：$file - $_" -ForegroundColor Red
            $errorCount++
        }
    } else {
        Write-Host "⚠️  文件不存在：$file" -ForegroundColor DarkGray
    }
}

Write-Host "`n[2] 删除 XML Mapper 文件..." -ForegroundColor Yellow

foreach ($file in $xmlMappers) {
    if (Test-Path $file) {
        try {
            Remove-Item $file -Force
            Write-Host "✅ 已删除：$file" -ForegroundColor Gray
            $deletedCount++
        } catch {
            Write-Host "❌ 删除失败：$file - $_" -ForegroundColor Red
            $errorCount++
        }
    } else {
        Write-Host "⚠️  文件不存在：$file" -ForegroundColor DarkGray
    }
}

Write-Host "`n[3] 清理空目录..." -ForegroundColor Yellow

# 删除空的 erp 控制器目录
$emptyDirs = @(
    "$basePath\src\main\java\com\ruoyi\system\controller\erp"
)

foreach ($dir in $emptyDirs) {
    if (Test-Path $dir) {
        $files = Get-ChildItem $dir -File
        if ($files.Count -eq 0) {
            try {
                Remove-Item $dir -Force -Recurse
                Write-Host "✅ 已删除空目录：$dir" -ForegroundColor Gray
            } catch {
                Write-Host "❌ 删除目录失败：$dir - $_" -ForegroundColor Red
                $errorCount++
            }
        } else {
            Write-Host "⚠️  目录非空，保留：$dir" -ForegroundColor DarkGray
        }
    }
}

Write-Host "`n=== 清理完成 ===" -ForegroundColor Cyan
Write-Host "✅ 成功删除：$deletedCount 个文件" -ForegroundColor Green
Write-Host "❌ 失败：$errorCount 个文件" -ForegroundColor $(if ($errorCount -eq 0) { "Green" } else { "Red" })

if ($deletedCount -gt 0) {
    Write-Host "`n⚠️  重要提示：" -ForegroundColor Yellow
    Write-Host "1. 请重新编译项目以确保没有引用错误" -ForegroundColor White
    Write-Host "2. 检查是否有其他文件引用了这些已删除的类" -ForegroundColor White
    Write-Host "3. 如果需要恢复，可以从 Git 历史中找回" -ForegroundColor White
}
