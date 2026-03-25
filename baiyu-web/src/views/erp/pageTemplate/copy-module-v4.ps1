# ============================================
# ERP 配置化页面 - 快速复制脚本（增强版 v4.1）
# ============================================
# 描述：一键生成完整的 ERP 配置化页面前端代码
# 使用示例：
#   .\copy-module-v4.ps1 -ModuleName "saleOrder" -ModuleTitle "销售订单管理"
#   .\copy-module-v4.ps1 saleOrder "销售订单管理"  # 简化版（位置参数）
#   .\copy-module-v4.ps1 purchaseOrder "采购订单管理" -EnableAudit:$false
# ============================================

param(
    [Parameter(Mandatory=$true, Position=0, HelpMessage="模块编码（小驼峰命名，如：saleOrder）")]
    [ValidatePattern("^[a-z][a-zA-Z0-9]*$")]
    [string]$ModuleName,
    
    [Parameter(Mandatory=$true, Position=1, HelpMessage="模块中文名称（如：销售订单管理）")]
    [string]$ModuleTitle,
    
    # 路径配置（通常不需要修改，会自动基于脚本位置计算）
    [string]$SourcePath = "",
    [string]$DestBasePath = "",
    
    # 菜单配置
    [string]$ParentMenuName = "ERP 业务菜单",
    [int]$OrderNum = 1,
    [string]$Icon = "document",
    
    # 功能开关（默认全部启用）
    [switch]$EnableAudit = $true,      # 审核功能
    [switch]$EnableUnAudit = $true,   # 反审核功能
    [switch]$EnablePush = $true,      # 下推功能
    [switch]$EnableExport = $true     # 导出功能
)

# ============================================
# 工具函数
# ============================================

# 错误处理函数
function Handle-Error {
    param($message)
    Write-Host " ERROR: $message" -ForegroundColor Red
    exit 1
}

# 首字母大写函数
function Get-PascalCase {
    param([string]$text)
    if ([string]::IsNullOrEmpty($text)) { return $text }
    return $text.Substring(0,1).ToUpper() + $text.Substring(1)
}

# 生成清理 SQL 函数
function Generate-CleanupSql {
    param(
        [string]$ModuleName,
        [string]$ModuleTitle,
        [string]$ParentId,
        [string]$MenuId,
        [string]$ConfigId,
        [System.Collections.ArrayList]$Buttons
    )
    
    # 生成按钮权限列表（用于删除）
    $buttonPerms = @()
    foreach ($button in $Buttons) {
        $buttonPerms += "'$($button.perms)'"
    }
    $buttonPermsString = [string]::Join(", ", $buttonPerms)
    
    $cleanupSql = @"
-- ============================================
-- ERP 配置化页面 - 清理脚本
-- 模块：$ModuleTitle
-- 创建时间：$(Get-Date -Format "yyyy-MM-dd HH:mm:ss")
-- 说明：删除由 menu-config.sql 和 page-config-data.sql 创建的数据
-- 警告：执行此脚本将删除相关菜单、按钮权限和页面配置数据
-- ============================================

-- 开始事务（如果表支持事务）
SET autocommit=0;
START TRANSACTION;

-- ============================================
-- 1. 删除按钮权限
-- ============================================
-- 直接根据权限标识符删除，不依赖父菜单 ID（更安全可靠）
DELETE FROM sys_menu 
WHERE perms IN ($buttonPermsString);

SELECT ROW_COUNT() AS '已删除按钮数量';

-- ============================================
-- 2. 删除子菜单（业务页面）
-- ============================================
-- 使用变量避免 1093 错误
SET @parent_menu_id := (SELECT menu_id FROM (SELECT menu_id FROM sys_menu WHERE menu_name COLLATE utf8mb4_general_ci = 'ERP 业务菜单' COLLATE utf8mb4_general_ci LIMIT 1) AS tmp);

DELETE FROM sys_menu 
WHERE menu_name COLLATE utf8mb4_general_ci = '$ModuleTitle' COLLATE utf8mb4_general_ci
AND parent_id = @parent_menu_id;

SELECT ROW_COUNT() AS '已删除子菜单数量';

-- ============================================
-- 3. 删除页面配置数据
-- ============================================
DELETE FROM erp_page_config 
WHERE module_code = '$($ModuleName.ToLower())';

SELECT ROW_COUNT() AS '已删除配置数量';

-- ============================================
-- 4. 删除配置历史记录（如果存在）
-- ============================================
DELETE FROM erp_page_config_history 
WHERE config_id IN (
    SELECT config_id FROM erp_page_config WHERE module_code = '$($ModuleName.ToLower())'
);

SELECT ROW_COUNT() AS '已删除配置历史数量';

-- ============================================
-- 5. 可选：删除父菜单（谨慎使用）
-- ============================================
-- 注意：只有在确认没有其他子菜单时才执行以下 SQL
-- 取消下面的注释以启用此功能

-- DELETE FROM sys_menu 
-- WHERE menu_id = '$ParentId'
-- AND NOT EXISTS (
--     SELECT 1 FROM sys_menu WHERE parent_id = '$ParentId'
-- );

-- SELECT ROW_COUNT() AS '已删除父菜单数量';

-- ============================================
-- 提交事务
-- ============================================
COMMIT;

-- ============================================
-- 验证清理结果
-- ============================================
SELECT ' $ModuleTitle 数据清理完成！' AS message;

-- 验证菜单是否已删除
SELECT COUNT(*) AS remaining_menus 
FROM sys_menu 
WHERE menu_name COLLATE utf8mb4_general_ci = '$ModuleTitle' COLLATE utf8mb4_general_ci;

-- 验证配置是否已删除
SELECT COUNT(*) AS remaining_configs 
FROM erp_page_config 
WHERE module_code = '$($ModuleName.ToLower())';

-- 显示剩余的按钮权限（应该为 0）
SELECT COUNT(*) AS remaining_buttons 
FROM sys_menu 
WHERE perms IN ($buttonPermsString);

-- ============================================
-- 使用说明
-- ============================================
/*
💡 提示：
1. 此脚本用于清理测试数据或回滚模块
2. 脚本使用事务保护，要么全部成功，要么全部回滚
3. 父菜单删除默认禁用，需要手动取消注释
4. 执行前请确认模块名称和配置编码

 警告：
1. 执行此脚本将永久删除数据
2. 建议先备份相关数据
3. 生产环境慎用

使用示例：
mysql -u root -p test < cleanup-config.sql
*/

"@
    
    return $cleanupSql
}

# ============================================
# 雪花算法 ID 生成器（简化版 - 使用正数）
# ============================================
# 时间起始点：2026-01-01 00:00:00 UTC
$epoch = [Int64]1735689600000

# 全局序列号（保证同一毫秒内 ID 不重复）
$globalSequence = 0

function Get-SnowflakeId {
    param(
        [int]$workerId = 1,    # 工作机器 ID (0-31)
        [int]$datacenterId = 1 # 数据中心 ID (0-31)
    )
    
    # 获取当前时间戳（毫秒）
    $timestamp = [Int64]((Get-Date -UFormat %s) * 1000)
    $elapsedTime = $timestamp - $epoch
    
    # 序列号自增
    $script:globalSequence = ($script:globalSequence + 1) -band 0xFFF  # 12 位序列号
    
    # 构建雪花 ID (64 位，使用正数范围)
    # 格式：高 32 位 (秒级时间戳) + 低 32 位 (工作机器 ID+ 序列号)
    $highBits = [Int64]($elapsedTime -shr 10) -shl 32
    $lowBits = (($datacenterId -band 0x1F) -shl 27) -bor (($workerId -band 0x1F) -shl 22) -bor ($script:globalSequence -band 0x3FFFFF)
    $snowflakeId = $highBits -bor $lowBits
    
    # 确保为正数（清除符号位）
    $snowflakeId = $snowflakeId -band 0x7FFFFFFFFFFFFFFF
    
    return [string]$snowflakeId
}

# ============================================
# 主流程
# ============================================

# 自动计算路径（基于当前脚本位置）
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path

# 如果未指定 SourcePath，使用脚本所在目录
if ([string]::IsNullOrEmpty($SourcePath)) {
    $SourcePath = $ScriptDir
}

# 如果未指定 DestBasePath，使用脚本的上级目录\ConfigDrivenPage
if ([string]::IsNullOrEmpty($DestBasePath)) {
    $DestBasePath = Join-Path $ScriptDir "..\ConfigDrivenPage"
    # 转换为绝对路径（处理 .. 符号）
    try {
        $DestBasePath = (Resolve-Path $DestBasePath).Path
    } catch {
        # 如果目录不存在，创建它
        $DestBasePath = (New-Item -ItemType Directory -Path $DestBasePath -Force).FullName
    }
}

Write-Host "" -ForegroundColor Cyan
Write-Host "╔════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║   ERP 配置化页面生成器 v4.2           ║" -ForegroundColor Cyan
Write-Host "║   3 分钟完成新模块开发                ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""
Write-Host "📋 模块信息:" -ForegroundColor Yellow
Write-Host "  • 模块编码：$ModuleName" -ForegroundColor Cyan
Write-Host "  • 模块名称：$ModuleTitle" -ForegroundColor Cyan
Write-Host ""
Write-Host "⚙️  路径配置:" -ForegroundColor Yellow
Write-Host "  • 源路径：$SourcePath" -ForegroundColor Gray
Write-Host "  • 目标路径：$DestBasePath" -ForegroundColor Gray
Write-Host ""
Write-Host "⚙️  功能配置:" -ForegroundColor Yellow
$auditStatus = if ($EnableAudit) { " 启用" } else { " 禁用" }
$unAuditStatus = if ($EnableUnAudit) { " 启用" } else { " 禁用" }
$pushStatus = if ($EnablePush) { " 启用" } else { " 禁用" }
$exportStatus = if ($EnableExport) { " 启用" } else { " 禁用" }
Write-Host "  • 审核功能：$auditStatus" -ForegroundColor Gray
Write-Host "  • 反审核功能：$unAuditStatus" -ForegroundColor Gray
Write-Host "  • 下推功能：$pushStatus" -ForegroundColor Gray
Write-Host "  • 导出功能：$exportStatus" -ForegroundColor Gray
Write-Host ""

# [步骤 1] 验证源路径
Write-Host "[1/7] 验证源路径..." -ForegroundColor Yellow
if (-not (Test-Path $SourcePath)) {
    Handle-Error "源路径不存在：$SourcePath"
}
Write-Host "  ✓ 源路径验证通过" -ForegroundColor Green
Write-Host ""

# [步骤 2] 设置目标路径并检查
Write-Host "[2/7] 检查目标路径..." -ForegroundColor Yellow
# 使用全小写命名（避免路由大小写问题）
$lowerCaseName = $ModuleName.ToLower()
$moduleDest = Join-Path $DestBasePath $lowerCaseName
$configDir = Join-Path $moduleDest "configs"
$configurableDir = Join-Path $moduleDest "configurable"
$configFile = Join-Path $configDir "$ModuleName.config.json"
$vueFile = Join-Path $configurableDir "$lowerCaseName.vue"
$cssFile = Join-Path $configurableDir "$lowerCaseName.styles.css"

if (Test-Path $moduleDest) {
    Handle-Error "目标文件夹已存在：$moduleDest`n   请先删除该文件夹或选择其他模块名称"
}
Write-Host "  ✓ 目标路径检查通过" -ForegroundColor Green
Write-Host ""

# [步骤 3] 复制文件夹
Write-Host "[3/7] 复制模板文件..." -ForegroundColor Yellow
try {
    # 复制文件夹，排除 PowerShell 脚本
    Copy-Item -Path $SourcePath -Destination $moduleDest -Recurse -Force
    
    # 删除不需要的脚本文件
    $scriptFiles = @("copy-module-v2.ps1", "copy-module-v3.ps1", "copy-module-v4.ps1")
    foreach ($scriptFile in $scriptFiles) {
        $scriptPath = Join-Path $moduleDest $scriptFile
        if (Test-Path $scriptPath) {
            Remove-Item -Path $scriptPath -Force
        }
    }
    
    Write-Host "  ✓ 模板文件复制成功" -ForegroundColor Green
} catch {
    Handle-Error "复制文件失败：$_"
}
Write-Host ""

# [步骤 4] 重命名组件文件
Write-Host "[4/7] 重命名组件文件..." -ForegroundColor Yellow

$pascalCaseName = Get-PascalCase $ModuleName
$vueNewName = Join-Path $configurableDir "$lowerCaseName.vue"
$cssNewName = Join-Path $configurableDir "$lowerCaseName.styles.css"
$configNewName = Join-Path $configDir "$ModuleName.config.json"

try {
    # 重命名 Vue 文件（使用全小写）
    $vueOldName = Join-Path $configurableDir "BusinessConfigurable.vue"
    if (Test-Path $vueOldName) {
        Rename-Item -Path $vueOldName -NewName "$lowerCaseName.vue" -Force
    }
    
    # 重命名 CSS 文件（使用全小写）
    $cssOldName = Join-Path $configurableDir "BusinessConfigurable.styles.css"
    if (Test-Path $cssOldName) {
        Rename-Item -Path $cssOldName -NewName "$lowerCaseName.styles.css" -Force
    }
    
    # 重命名配置文件
    $configOldName = Join-Path $configDir "business.config.template.json"
    if (Test-Path $configOldName) {
        Rename-Item -Path $configOldName -NewName "$ModuleName.config.json" -Force
    }
    
    Write-Host "  ✓ 文件重命名完成" -ForegroundColor Green
} catch {
    Handle-Error "重命名文件失败：$_"
}
Write-Host ""

# [步骤 5] 更新 Vue 组件内容
Write-Host "[5/7] 更新 Vue 组件配置..." -ForegroundColor Yellow

try {
    $vueContent = Get-Content $vueNewName -Raw -Encoding UTF8
    
    # 更新组件名称（使用 PascalCase）
    $vueContent = $vueContent -replace 'name="BusinessConfigurable"', "name=`"$pascalCaseName`""
    
    # 更新配置文件引用
    $vueContent = $vueContent -replace 'business\.config\.template\.json', "$ModuleName.config.json"
    
    # 更新 CSS 文件引用（使用全小写）
    $vueContent = $vueContent -replace 'BusinessConfigurable\.styles\.css', "$lowerCaseName.styles.css"
    
    # 🔧 修复 DictionaryBuilder 导入路径（从相对路径改为绝对路径）
    $vueContent = $vueContent -replace "from '\.\./utils/DictionaryBuilder'", "from '@/views/erp/utils/DictionaryBuilder'"
    
    # 🔧 移除 preloadApiMethods 调用（这个函数不存在且不需要）
    $vueContent = $vueContent -replace "\s+// 2\. 再预加载 API 方法.*?await preloadApiMethods\(\)\s+", "`
  "
    
    # 调整初始化步骤编号
    $vueContent = $vueContent -replace '// 3\. 预加载字典数据', '// 2. 预加载字典数据'
    $vueContent = $vueContent -replace '// 4\. 初始化引擎配置', '// 3. 初始化引擎配置'
    
    # 写回文件
    Set-Content -Path $vueNewName -Value $vueContent -Encoding UTF8 -NoNewline
    
    Write-Host "  ✓ Vue 组件配置更新完成" -ForegroundColor Green
} catch {
    Handle-Error "更新 Vue 组件失败：$_"
}
Write-Host ""

# [步骤 6] 更新 JSON 配置文件内容
Write-Host "[6/7] 更新业务配置文件..." -ForegroundColor Yellow

try {
    $configContent = Get-Content $configNewName -Raw -Encoding UTF8
    $configObj = $configContent | ConvertFrom-Json
    
    # 更新页面配置（统一为小写）
    $configObj.pageConfig.title = $ModuleTitle
    $configObj.pageConfig.moduleCode = $lowerCaseName
    $configObj.pageConfig.permissionPrefix = "k3:$lowerCaseName"
    $configObj.pageConfig.apiPrefix = "/erp/engine"  #  使用通用引擎 API
    
    # 注意：apiConfig.modulePath 字段已废弃，不再设置
    
    # 更新业务配置
    $configObj.businessConfig.entityName = $ModuleTitle
    $configObj.businessConfig.entityNameSingular = $ModuleTitle.Replace("管理", "")
    
    # 保存配置文件（压缩格式，减少空格）
    $jsonString = $configObj | ConvertTo-Json -Depth 100 -Compress
    Set-Content -Path $configNewName -Value $jsonString -Encoding UTF8 -NoNewline
    
    Write-Host "  ✓ 业务配置文件更新完成（压缩格式）" -ForegroundColor Green
} catch {
    Handle-Error "更新配置文件失败：$_"
}
Write-Host ""

# [步骤 7] 生成 SQL 脚本
Write-Host "[7/7] 生成 SQL 脚本..." -ForegroundColor Yellow

# 生成菜单 SQL
$menuSqlFile = Join-Path $moduleDest "init-all-in-one.sql"

# 生成雪花 ID
# 使用模块名的哈希值作为 workerId 基础，确保不同模块 ID 不重复
$moduleHash = [Math]::Abs($ModuleName.GetHashCode()) % 31 + 1  # 1-31 之间
$parentId = Get-SnowflakeId -workerId $moduleHash -datacenterId 1      # 父菜单 ID
$menuId = Get-SnowflakeId -workerId $moduleHash -datacenterId 2       # 子菜单 ID
$buttonBaseId = Get-SnowflakeId -workerId $moduleHash -datacenterId 3 # 按钮 ID 基础值

# 读取 JSON 配置文件并生成配置 ID（必须在$menuSql 之前）
$configId = Get-SnowflakeId -workerId $moduleHash -datacenterId 10
try {
    $configJson = Get-Content $configNewName -Raw -Encoding UTF8
    # 转义单引号、反斜杠和特殊字符
    $configJsonEscaped = $configJson -replace '\\', '\\\\' -replace "'", "\\''"
} catch {
    Handle-Error "读取配置文件失败：$_"
}

# 生成按钮权限列表
$buttons = @()
$buttonIndex = 0

$buttons += @{ name = "查询"; perms = "k3:$($ModuleName.ToLower())`:query" }
if ($EnableAudit) {
    $buttons += @{ name = "新增"; perms = "k3:$($ModuleName.ToLower())`:add" }
    $buttons += @{ name = "修改"; perms = "k3:$($ModuleName.ToLower())`:edit" }
    $buttons += @{ name = "删除"; perms = "k3:$($ModuleName.ToLower())`:remove" }
    $buttons += @{ name = "审核"; perms = "k3:$($ModuleName.ToLower())`:audit" }
    if ($EnableUnAudit) {
        $buttons += @{ name = "反审核"; perms = "k3:$($ModuleName.ToLower())`:unAudit" }
    }
    if ($EnablePush) {
        $buttons += @{ name = "下推"; perms = "k3:$($ModuleName.ToLower())`:push" }
    }
    if ($EnableExport) {
        $buttons += @{ name = "导出"; perms = "k3:$($ModuleName.ToLower())`:export" }
    }
}

# 生成按钮 INSERT 语句（简化版用于临时表）
$buttonInsertsForTemp = @()
for ($i = 0; $i -lt $buttons.Count; $i++) {
    $button = $buttons[$i]
    $buttonId = Get-SnowflakeId -workerId $moduleHash -datacenterId (3 + $i)
    # 移除花括号，MySQL 不需要
    $buttonName = $button.name -replace '\{', '' -replace '\}', ''
    # 使用 @menu_id 作为父菜单 ID（按钮的父菜单是业务子菜单）
    $buttonInsertsForTemp += "('$buttonId', '$buttonName', @menu_id, $($i + 1), '$($button.perms)', '1')"
}

# 将数组转换为多行字符串
$buttonValuesString = [string]::Join(",`r`n", $buttonInsertsForTemp)

$menuSql = @"
-- ============================================
-- ERP 业务菜单 SQL - $ModuleTitle
-- 创建时间：$(Get-Date -Format "yyyy-MM-dd HH:mm:ss")
-- 说明：为 $ModuleTitle 创建菜单和按钮权限（所有 ID 使用雪花算法）
-- ============================================

-- ============================================
-- 第一部分：创建菜单和按钮权限
-- ============================================

-- 1. 确保父菜单存在（ERP 业务菜单）- parent_id=0 表示根菜单，使用 COLLATE 确保字符集一致
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, remark, create_by, create_time)
SELECT '$parentId', 'ERP 业务菜单' COLLATE utf8mb4_general_ci, 
       0,
       5, 'business', '', 0, 0, 'M', 1, 1, '', 'document', 
       'ERP 业务菜单目录', 'admin', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu 
    WHERE menu_name COLLATE utf8mb4_general_ci = 'ERP 业务菜单' COLLATE utf8mb4_general_ci
);

-- 获取父菜单 ID（根据 menu_name 查询，使用 COLLATE 确保字符集一致）
SET @parent_id := (SELECT menu_id FROM sys_menu WHERE menu_name COLLATE utf8mb4_general_ci = 'ERP 业务菜单' COLLATE utf8mb4_general_ci LIMIT 1);

-- 2. 创建子菜单：$ModuleTitle（根据 menu_name + parent_id 判断是否已存在，使用 COLLATE 确保字符集一致）
SET @menu_id := '$menuId';
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT @menu_id, '$ModuleTitle' COLLATE utf8mb4_general_ci, @parent_id, $OrderNum, '$lowerCaseName', 
        'erp/ConfigDrivenPage/$($ModuleName.ToLower())/configurable/$lowerCaseName', 
        '{"moduleCode":"$($ModuleName.ToLower())"}', 0, 0, 'C', 1, 1, 
        'k3:$($ModuleName.ToLower())`:query', '$Icon', 
        'admin', NOW(), '', NULL, '$ModuleTitle 配置化页面'
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu 
    WHERE menu_name COLLATE utf8mb4_general_ci = '$ModuleTitle' COLLATE utf8mb4_general_ci
    AND parent_id = @parent_id
);

-- 3. 创建按钮权限（每个按钮使用独立的雪花 ID，根据 menu_name + parent_id 判断是否已存在）
-- 先创建临时表存储按钮数据（显式指定字符集避免冲突）
CREATE TEMPORARY TABLE IF NOT EXISTS tmp_buttons (
    menu_id VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
    menu_name VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
    parent_id BIGINT,
    order_num INT,
    perms VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci,
    status CHAR(1) DEFAULT '1'
) ENGINE=MEMORY DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 清空临时表
DELETE FROM tmp_buttons;

-- 插入按钮数据到临时表（status=1 表示正常启用）
INSERT INTO tmp_buttons (menu_id, menu_name, parent_id, order_num, perms, status) VALUES
$buttonValuesString;

-- 从临时表插入到 sys_menu，跳过已存在的记录（使用 COLLATE 确保字符集一致）
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
SELECT t.menu_id, t.menu_name COLLATE utf8mb4_general_ci, @menu_id, t.order_num, '', '', '', 0, 0, 'F', 1, 1, t.perms COLLATE utf8mb4_general_ci, '#', 'admin', NOW(), '', NULL, ''
FROM tmp_buttons t
WHERE NOT EXISTS (
    SELECT 1 FROM sys_menu s 
    WHERE s.menu_name COLLATE utf8mb4_general_ci = t.menu_name COLLATE utf8mb4_general_ci
    AND s.parent_id = @menu_id
);

-- 清理临时表
DROP TEMPORARY TABLE IF EXISTS tmp_buttons;

-- ============================================
-- 第二部分：导入页面配置数据
-- ============================================

-- 1. 创建触发器（如果不存在）
DELIMITER \$\$

CREATE TRIGGER IF NOT EXISTS trg_erp_config_history 
AFTER UPDATE ON erp_page_config 
FOR EACH ROW 
BEGIN
  INSERT INTO erp_page_config_history (
    config_id, module_code, config_type, version, config_content, 
    change_reason, change_type, create_by
  ) VALUES (
    NEW.config_id, NEW.module_code, NEW.config_type, NEW.version, NEW.config_content,
    CONCAT('从版本 ', OLD.version, ' 更新到版本 ', NEW.version),
    'UPDATE', NEW.update_by
  );
END\$\$

DELIMITER ;

-- 2. 插入页面配置
INSERT INTO erp_page_config (
  config_id, module_code, config_name, config_type, config_content, 
  version, status, is_public, remark, create_by, create_time
) VALUES (
  $configId,
  '$($ModuleName.ToLower())',
  '$ModuleTitle 页面配置',
  'PAGE',
  '$configJsonEscaped',
  1,
  '1',
  '0',
  '$ModuleTitle 配置化页面初始配置',
  'admin',
  NOW()
)
ON DUPLICATE KEY UPDATE
  config_name = VALUES(config_name),
  config_content = VALUES(config_content),
  version = version + 1,
  update_by = VALUES(create_by),
  update_time = VALUES(create_time);

-- ============================================
-- 完成提示
-- ============================================
SELECT ' $ModuleTitle 菜单创建完成！' AS message;
SELECT '父菜单 ID: $parentId' AS parent_menu_info;
SELECT CONCAT('子菜单 ID: ', CAST(@menu_id AS CHAR)) AS menu_info;
SELECT CONCAT('包含按钮：', GROUP_CONCAT(menu_name ORDER BY order_num)) AS buttons
FROM sys_menu WHERE parent_id = @menu_id;
SELECT ' $ModuleTitle 配置数据导入成功！' AS message;
SELECT config_id, module_code, config_name, version FROM erp_page_config 
WHERE module_code = '$($ModuleName.ToLower())';

"@

try {
    # 保存合并后的 SQL 文件（包含菜单和配置数据）
    $menuSql | Set-Content -Path $menuSqlFile -Encoding UTF8 -NoNewline
    Write-Host "  ✓ 初始化 SQL 生成：$menuSqlFile" -ForegroundColor Green
    
    # 生成清理 SQL
    $cleanupSql = Generate-CleanupSql -ModuleName $ModuleName -ModuleTitle $ModuleTitle -ParentId $parentId -MenuId $menuId -ConfigId $configId -Buttons $buttons
    $cleanupSqlFile = Join-Path $moduleDest "cleanup-config.sql"
    $cleanupSql | Set-Content -Path $cleanupSqlFile -Encoding UTF8 -NoNewline
    Write-Host "  ✓ 清理 SQL 生成：$cleanupSqlFile" -ForegroundColor Green
} catch {
    Handle-Error "生成 SQL 文件失败：$_"
}
Write-Host ""

# ============================================
# 完成提示
# ============================================

Write-Host ""
Write-Host "╔════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║    模块生成成功！                   ║" -ForegroundColor Green
Write-Host "╚════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""
Write-Host "📁 模块位置:" -ForegroundColor White
Write-Host "  $moduleDest" -ForegroundColor Gray
Write-Host ""
Write-Host "📄 生成的文件:" -ForegroundColor White
Write-Host "  1. 🎨 Vue 组件：$vueNewName" -ForegroundColor Gray
Write-Host "  2. 🎨 样式文件：$cssNewName" -ForegroundColor Gray
Write-Host "  3. ⚙️  配置文件：$configNewName" -ForegroundColor Gray
Write-Host "  4. 💾 初始化 SQL: $menuSqlFile (包含菜单 + 配置数据)" -ForegroundColor Gray
Write-Host "  5. 🧹 清理 SQL: $cleanupSqlFile" -ForegroundColor Gray
Write-Host ""
Write-Host "══════════════════════════════════════" -ForegroundColor Cyan
Write-Host "📋 后续步骤（必须执行）:" -ForegroundColor Yellow
Write-Host "══════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""
Write-Host "1️⃣  执行初始化 SQL（创建菜单、权限和配置）" -ForegroundColor White
Write-Host "   📁 文件：$menuSqlFile" -ForegroundColor Gray
Write-Host "   💡 提示：支持幂等执行，可重复运行" -ForegroundColor Cyan
Write-Host ""
Write-Host "2️⃣  创建后端 API 文件" -ForegroundColor White
Write-Host "   📁 路径：src/api/k3/$($ModuleName).js" -ForegroundColor Gray
Write-Host "   📝 参考：现有的 saleOrder.js 实现 API 方法" -ForegroundColor Gray
Write-Host "   💡 提示：需要实现 list、get、add、update、delete、audit 等方法" -ForegroundColor Cyan
Write-Host ""
Write-Host "══════════════════════════════════════" -ForegroundColor Cyan
Write-Host "🧹 清理脚本（可选）:" -ForegroundColor Yellow
Write-Host "══════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""
Write-Host "  清理 SQL（回滚或测试用）" -ForegroundColor White
Write-Host "   📁 文件：$cleanupSqlFile" -ForegroundColor Gray
Write-Host "   💡 提示：用于删除测试数据或回滚模块" -ForegroundColor Cyan
Write-Host "     警告：执行将永久删除数据，请谨慎使用" -ForegroundColor Red
Write-Host ""
Write-Host "══════════════════════════════════════" -ForegroundColor Cyan
Write-Host "📝 可选配置（按需调整）:" -ForegroundColor Yellow
Write-Host "══════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""
Write-Host "3️⃣  编辑配置文件" -ForegroundColor White
Write-Host "   📁 文件：$configNewName" -ForegroundColor Gray
Write-Host "   📝 说明：根据业务需求调整搜索、表格、表单配置" -ForegroundColor Gray
Write-Host "   💡 提示：修改后刷新页面即可生效" -ForegroundColor Cyan
Write-Host ""
Write-Host "4️⃣  路由自动注册（无需手动配置）" -ForegroundColor White
Write-Host "   💡 说明：RuoYi 框架使用纯动态路由机制" -ForegroundColor Cyan
Write-Host "   📁 数据表：sys_menu" -ForegroundColor Gray
Write-Host "   📝 操作：执行菜单 SQL 后，系统会自动从数据库加载路由" -ForegroundColor Gray
Write-Host "   🔍 验证：登录系统后，在侧边栏查看新菜单" -ForegroundColor Gray
Write-Host ""
Write-Host "5️⃣  重启开发服务器并测试" -ForegroundColor White
Write-Host "   📝 命令：npm run dev" -ForegroundColor Gray
Write-Host ""
Write-Host "══════════════════════════════════════" -ForegroundColor Cyan
Write-Host "💡 技术支持" -ForegroundColor Cyan
Write-Host "══════════════════════════════════════" -ForegroundColor Cyan
Write-Host "  - 如有问题请查看项目文档" -ForegroundColor Gray
Write-Host "  - 更多使用说明请参考技能文档：SKILL.md" -ForegroundColor Gray
Write-Host "  - 联系开发团队获取帮助" -ForegroundColor Gray
Write-Host ""
Write-Host "  6. 重启开发服务器测试" -ForegroundColor Gray
Write-Host ""
Write-Host "💡 提示：如有问题请查看项目文档或联系开发团队" -ForegroundColor Cyan
Write-Host ""
