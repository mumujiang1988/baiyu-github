# ERP 日期区间配置自动诊断脚本
# 用途：检查数据库中日期区间配置是否正确，并提供修复建议

param(
    [string]$moduleCode = "saleorder",
    [switch]$FixMode
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "ERP 日期区间配置自动诊断工具" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 数据库连接信息
$mysqlUser = "root"
$mysqlPassword = "hanzhiyun1988"
$mysqlHost = "localhost"
$mysqlDatabase = "test"

# MySQL 命令执行函数
function Invoke-MySqlQuery {
    param([string]$query)
    
    $tempFile = [System.IO.Path]::GetTempFileName()
    $mySqlCommand = "mysql -u $mysqlUser -p$mysqlPassword -h $mysqlHost $mysqlDatabase -N -e `"$query`" > `"$tempFile`" 2>&1"
    
    try {
        Invoke-Expression $mySqlCommand
        $result = Get-Content $tempFile -Raw
        Remove-Item $tempFile -Force
        return $result
    } catch {
        Remove-Item $tempFile -Force -ErrorAction SilentlyContinue
        throw "MySQL 查询失败：$_"
    }
}

# 步骤 1：检查数据库配置
Write-Host "[步骤 1] 检查数据库中的 search_config 配置..." -ForegroundColor Yellow

$query = @"
SELECT 
  module_code,
  config_name,
  JSON_EXTRACT(search_config, '$.showSearch') AS show_search,
  JSON_EXTRACT(search_config, '$.fields') AS search_fields
FROM erp_page_config
WHERE module_code = '$moduleCode';
"@

try {
    $configResult = Invoke-MySqlQuery -query $query
    
    if ([string]::IsNullOrWhiteSpace($configResult)) {
        Write-Host " 未找到模块 [$moduleCode] 的配置数据！" -ForegroundColor Red
        Write-Host ""
        Write-Host "建议：" -ForegroundColor Yellow
        Write-Host "  1. 检查是否已执行初始化 SQL 脚本" -ForegroundColor White
        Write-Host "  2. 确认模块代码是否正确：$moduleCode" -ForegroundColor White
        exit 1
    }
    
    Write-Host " 找到配置数据" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host " 数据库查询失败：$_" -ForegroundColor Red
    exit 1
}

# 步骤 2：检查 FDate 字段配置
Write-Host "[步骤 2] 检查 FDate 字段的详细配置..." -ForegroundColor Yellow

$query = @"
SELECT 
  JSON_EXTRACT(field_json, '$.field') AS field_name,
  JSON_EXTRACT(field_json, '$.component') AS component,
  JSON_EXTRACT(field_json, '$.defaultValue') AS default_value,
  JSON_EXTRACT(field_json, '$.queryOperator') AS query_operator
FROM erp_page_config,
JSON_TABLE(
  JSON_EXTRACT(search_config, '$.fields'),
  '$[*]' COLUMNS(
    field_json JSON PATH '$'
  )
) AS jt
WHERE module_code = '$moduleCode'
AND JSON_EXTRACT(field_json, '$.field') = 'FDate';
"@

try {
    $dateFieldConfig = Invoke-MySqlQuery -query $query
    
    if ([string]::IsNullOrWhiteSpace($dateFieldConfig)) {
        Write-Host " 未找到 FDate 字段配置！" -ForegroundColor Red
        Write-Host ""
        Write-Host "可能的原因：" -ForegroundColor Yellow
        Write-Host "  1. search_config 中没有 FDate 字段" -ForegroundColor White
        Write-Host "  2. 字段名可能不是 FDate（检查实际数据库字段）" -ForegroundColor White
    } else {
        Write-Host " FDate 字段配置存在" -ForegroundColor Green
        
        # 解析 JSON
        try {
            $fieldData = $dateFieldConfig | ConvertFrom-Json
            
            Write-Host ""
            Write-Host "字段详情:" -ForegroundColor Cyan
            Write-Host "  字段名：$($fieldData.field_name)" -ForegroundColor White
            Write-Host "  组件类型：$($fieldData.component)" -ForegroundColor White
            Write-Host "  默认值：$($fieldData.default_value)" -ForegroundColor White
            Write-Host "  查询操作符：$($fieldData.query_operator)" -ForegroundColor White
            
            # 验证 defaultValue
            if ($fieldData.component -eq "daterange") {
                Write-Host ""
                Write-Host " 组件类型正确：daterange" -ForegroundColor Green
                
                if ($fieldData.default_value) {
                    Write-Host " defaultValue 已配置" -ForegroundColor Green
                    
                    # 检查是否为数组格式
                    if ($fieldData.default_value -match '^\[') {
                        Write-Host " defaultValue 是数组格式" -ForegroundColor Green
                        
                        # 检查数组内容
                        if ($fieldData.default_value -match '"2010-01-01"' -and $fieldData.default_value -match '"today"') {
                            Write-Host " defaultValue 内容正确：[`"2010-01-01`", `"today`"]" -ForegroundColor Green
                        } else {
                            Write-Host "  defaultValue 内容可能不正确" -ForegroundColor Yellow
                            Write-Host "   期望：[`"2010-01-01`", `"today`"]" -ForegroundColor Gray
                            Write-Host "   实际：$($fieldData.default_value)" -ForegroundColor Gray
                        }
                    } else {
                        Write-Host " defaultValue 不是数组格式！" -ForegroundColor Red
                        Write-Host "   期望格式：[`"2010-01-01`", `"today`"]" -ForegroundColor Gray
                    }
                } else {
                    Write-Host " defaultValue 未配置！" -ForegroundColor Red
                }
                
                if ($fieldData.query_operator -eq "between") {
                    Write-Host " queryOperator 正确：between" -ForegroundColor Green
                } else {
                    Write-Host " queryOperator 不正确，应该是 between" -ForegroundColor Red
                }
            } else {
                Write-Host " 组件类型不正确，应该是 daterange" -ForegroundColor Red
                Write-Host "   当前：$($fieldData.component)" -ForegroundColor Gray
            }
        } catch {
            Write-Host "  JSON 解析失败，可能是格式问题" -ForegroundColor Yellow
            Write-Host "   原始数据：$dateFieldConfig" -ForegroundColor Gray
        }
    }
    
    Write-Host ""
} catch {
    Write-Host " 查询失败：$_" -ForegroundColor Red
}

# 步骤 3：检查前端文件
Write-Host "[步骤 3] 检查前端 BusinessConfigurable 文件..." -ForegroundColor Yellow

$basePath = "d:\baiyuyunma\baiyu-github\baiyu-github\baiyu-web\src\views\erp\pageTemplate\configurable"
$oldFile = Join-Path $basePath "BusinessConfigurable.vue"
$newFile = Join-Path $basePath "BusinessConfigurable\index.vue"

$oldFileExists = Test-Path $oldFile
$newFileExists = Test-Path $newFile

Write-Host ""
if ($oldFileExists) {
    Write-Host " 旧版文件存在：BusinessConfigurable.vue" -ForegroundColor Green
    $oldContent = Get-Content $oldFile -Raw
    
    # 检查是否包含 initDateRange 函数
    if ($oldContent -match 'const initDateRange\s*=\s*\([^)]*\)\s*=>') {
        Write-Host "    包含 initDateRange 函数" -ForegroundColor Green
        
        # 检查是否包含 daterange 处理逻辑
        if ($oldContent -match 'f\.component\s*===\s*[`"']daterange[`"']') {
            Write-Host "    包含 daterange 组件处理逻辑" -ForegroundColor Green
        } else {
            Write-Host "    缺少 daterange 组件处理逻辑" -ForegroundColor Red
        }
    }
} else {
    Write-Host " 旧版文件不存在：BusinessConfigurable.vue" -ForegroundColor Red
}

if ($newFileExists) {
    Write-Host " 新版文件存在：BusinessConfigurable/index.vue" -ForegroundColor Green
    $newContent = Get-Content $newFile -Raw
    
    # 检查是否包含 initDateRange 函数
    if ($newContent -match 'const initDateRange\s*=\s*\([^)]*\)\s*=>') {
        Write-Host "    包含 initDateRange 函数" -ForegroundColor Green
        
        # 检查是否只查找 beginDate 和 endDate
        if ($newContent -match 'beginDate.*endDate') {
            Write-Host "     仅查找 beginDate 和 endDate 字段" -ForegroundColor Yellow
            
            # 检查是否有 daterange 处理逻辑
            if ($newContent -match 'f\.component\s*===\s*[`"']daterange[`"']') {
                Write-Host "    也包含 daterange 组件处理逻辑" -ForegroundColor Green
            } else {
                Write-Host "    缺少 daterange 组件处理逻辑（这是问题所在！）" -ForegroundColor Red
            }
        }
    }
} else {
    Write-Host " 新版文件不存在：BusinessConfigurable/index.vue" -ForegroundColor Red
}

Write-Host ""

# 步骤 4：检查路由配置
Write-Host "[步骤 4] 检查路由配置..." -ForegroundColor Yellow

$routeFiles = @(
    "d:\baiyuyunma\baiyu-github\baiyu-github\baiyu-web\src\router\index.js",
    "d:\baiyuyunma\baiyu-github\baiyu-github\baiyu-web\src\router\routes.js",
    "d:\baiyuyunma\baiyu-github\baiyu-github\baiyu-web\src\router\routes\erp.js"
)

$routeFound = $false
foreach ($routeFile in $routeFiles) {
    if (Test-Path $routeFile) {
        Write-Host "   检查文件：$routeFile" -ForegroundColor Gray
        $routeContent = Get-Content $routeFile -Raw
        
        if ($routeContent -match "/erp/$moduleCode") {
            Write-Host "    找到 $moduleCode 路由配置" -ForegroundColor Green
            
            # 提取组件路径
            if ($routeContent -match "component:\s*\(.*?import\(['``](.*?)['``]\)") {
                $componentPath = $matches[1]
                Write-Host "   📁 组件路径：$componentPath" -ForegroundColor Cyan
                
                if ($componentPath -match "BusinessConfigurable/index") {
                    Write-Host "     使用新版文件（index.vue）" -ForegroundColor Yellow
                    Write-Host "   💡 提示：如果日期配置不生效，请修改 index.vue 的 initDateRange 函数" -ForegroundColor Cyan
                } elseif ($componentPath -match "BusinessConfigurable(?!/index)") {
                    Write-Host "    使用旧版文件（BusinessConfigurable.vue）" -ForegroundColor Green
                }
                
                $routeFound = $true
            }
            
            break
        }
    }
}

if (-not $routeFound) {
    Write-Host "     未在常见路由文件中找到配置" -ForegroundColor Yellow
}

Write-Host ""

# 步骤 5：生成修复建议
Write-Host "[步骤 5] 生成修复建议..." -ForegroundColor Yellow
Write-Host ""

$needFix = $false
if ($newFileExists) {
    $newContent = Get-Content $newFile -Raw
    if (($newContent -match 'beginDate.*endDate') -and ($newContent -notmatch 'f\.component\s*===\s*[`"\']daterange[`"\']')) {
        $needFix = $true
    }
}

if ($needFix) {
    Write-Host "🔧 推荐修复方案：修改 BusinessConfigurable/index.vue" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "文件位置：" -ForegroundColor White
    Write-Host "  $newFile" -ForegroundColor Gray
    Write-Host ""
    Write-Host "需要修改的函数：initDateRange (大约在第 982-1011 行)" -ForegroundColor White
    Write-Host ""
    Write-Host "在函数开头添加以下代码（处理 daterange 组件）：" -ForegroundColor White
    Write-Host ""
    Write-Host "// === 新增：处理 daterange 类型的单个字段 ===" -ForegroundColor Green
    Write-Host "const dateRangeField = searchFields.find(f => " -ForegroundColor Green
    Write-Host "  f.component === 'daterange' && " -ForegroundColor Green
    Write-Host "  f.defaultValue && " -ForegroundColor Green
    Write-Host "  Array.isArray(f.defaultValue) && " -ForegroundColor Green
    Write-Host "  f.defaultValue.length === 2" -ForegroundColor Green
    Write-Host ")" -ForegroundColor Green
    Write-Host ""
    Write-Host "if (dateRangeField) {" -ForegroundColor Green
    Write-Host "  const startDate = parseDynamicDate(dateRangeField.defaultValue[0])" -ForegroundColor Green
    Write-Host "  const endDate = parseDynamicDate(dateRangeField.defaultValue[1])" -ForegroundColor Green
    Write-Host "  if (startDate && endDate) {" -ForegroundColor Green
    Write-Host "    dateRange.value = [startDate, endDate]" -ForegroundColor Green
    Write-Host "    queryParams.value.beginDate = startDate" -ForegroundColor Green
    Write-Host "    queryParams.value.endDate = endDate" -ForegroundColor Green
    Write-Host "    return" -ForegroundColor Green
    Write-Host "  }" -ForegroundColor Green
    Write-Host "}" -ForegroundColor Green
    Write-Host ""
}

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "诊断完成！" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 输出总结
Write-Host "📊 总结报告:" -ForegroundColor Cyan
Write-Host ""

if ($newFileExists) {
    $newContent = Get-Content $newFile -Raw
    if ($newContent -match 'beginDate.*endDate' -and $newContent -not match 'f\.component\s*===\s*[`"']daterange[`"']') {
        Write-Host " 问题确认：使用了 index.vue 但缺少 daterange 处理逻辑" -ForegroundColor Red
        Write-Host ""
        Write-Host "这就是为什么日期区间配置 [`"2010-01-01`", `"today`"] 不生效的原因！" -ForegroundColor Yellow
        Write-Host "程序 fallback 到了默认逻辑：本月 1 号 到 今天" -ForegroundColor Yellow
        Write-Host ""
        Write-Host " 解决方案：按照上面的推荐修复方案修改代码" -ForegroundColor Green
    Write-Host " 代码看起来正常，问题可能在数据库配置" -ForegroundColor Green
}

Write-Host ""
