# 批量替换 ruoyi-erp-api 模块中的 Page 为 TableDataInfo
Write-Host "开始替换 Page 为 TableDataInfo..." -ForegroundColor Green

$files = @(
    "ruoyi-modules/ruoyi-erp-api/src/main/java/com/ruoyi/erp/service/impl/ErpPageConfigServiceImpl.java",
    "ruoyi-modules/ruoyi-erp-api/src/main/java/com/ruoyi/erp/service/impl/ErpApprovalFlowServiceImpl.java",
    "ruoyi-modules/ruoyi-erp-api/src/main/java/com/ruoyi/erp/service/impl/ErpPushRelationServiceImpl.java",
    "ruoyi-modules/ruoyi-erp-api/src/main/java/com/ruoyi/erp/service/impl/SuperDataPermissionServiceImpl.java"
)

foreach ($file in $files) {
    $filePath = Join-Path "d:\baiyuyunma\baiyu-github\baiyu-github\baiyu-ruoyi" $file
    
    if (Test-Path $filePath) {
        Write-Host "处理文件：$file" -ForegroundColor Yellow
        
        $content = Get-Content $filePath -Raw -Encoding UTF8
        
        # 替换导入语句
        $content = $content -replace 'import com\.baomidou\.mybatisplus\.extension\.plugins\.pagination\.Page;', 'import com.ruoyi.common.mybatis.core.page.TableDataInfo;'
        
        # 替换返回类型（方法签名）
        $content = $content -replace 'public Page<(\w+)> selectPageList', 'public TableDataInfo<$1> selectPageList'
        $content = $content -replace 'public Page<(\w+)> selectHistoryPage', 'public TableDataInfo<$1> selectHistoryPage'
        $content = $content -replace 'public Page<Map<String, Object>> selectPageByModuleWithTableName', 'public TableDataInfo<Map<String, Object>> selectPageByModuleWithTableName'
        
        # 替换 Page 对象创建
        $content = $content -replace 'Page<(\w+)> \w+ = new Page<>\(([^)]+)\);', 'TableDataInfo<$1> tableDataInfo = new TableDataInfo<>(records, $2);'
        $content = $content -replace '\w+\.setRecords\((\w+)\);', '// setRecords removed'
        $content = $content -replace 'return \w+;', 'return tableDataInfo;'
        
        # 保存文件
        $content | Out-File $filePath -Encoding UTF8 -NoNewline
        
        Write-Host "  ✓ 完成" -ForegroundColor Green
    } else {
        Write-Host "  ✗ 文件不存在：$filePath" -ForegroundColor Red
    }
}

Write-Host "`n所有文件处理完成！" -ForegroundColor Green
