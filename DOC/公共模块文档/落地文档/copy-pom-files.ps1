# 从 baiyu-ruyi-cs 复制所有 pom.xml 到 baiyu-ruoyi
# 用于修复 Maven 多模块项目结构

$ErrorActionPreference = "Stop"

$sourceBase = "d:\baiyuyunma\baiyu-github\baiyu-github\baiyu-ruyi-cs"
$targetBase = "d:\baiyuyunma\baiyu-github\baiyu-github\baiyu-ruoyi"

Write-Host "========================================" -ForegroundColor Green
Write-Host "  开始复制 pom.xml 文件" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

# 1. 复制根 pom.xml
Write-Host "[1/15] 复制根 pom.xml..." -ForegroundColor Yellow
if (Test-Path "$sourceBase\pom.xml") {
    Copy-Item -Path "$sourceBase\pom.xml" -Destination "$targetBase\pom.xml" -Force
    Write-Host "   已复制：pom.xml" -ForegroundColor Green
} else {
    Write-Host "   源文件不存在：$sourceBase\pom.xml" -ForegroundColor Red
}

# 2. 复制 ruoyi-admin-wms/pom.xml
Write-Host "[2/15] 复制 ruoyi-admin-wms/pom.xml..." -ForegroundColor Yellow
if (Test-Path "$sourceBase\ruoyi-admin-wms\pom.xml") {
    Copy-Item -Path "$sourceBase\ruoyi-admin-wms\pom.xml" -Destination "$targetBase\ruoyi-admin-wms\pom.xml" -Force
    Write-Host "   已复制：ruoyi-admin-wms/pom.xml" -ForegroundColor Green
} else {
    Write-Host "   源文件不存在" -ForegroundColor Red
}

# 3. 复制 ruoyi-common/pom.xml
Write-Host "[3/15] 复制 ruoyi-common/pom.xml..." -ForegroundColor Yellow
if (Test-Path "$sourceBase\ruoyi-common\pom.xml") {
    Copy-Item -Path "$sourceBase\ruoyi-common\pom.xml" -Destination "$targetBase\ruoyi-common\pom.xml" -Force
    Write-Host "   已复制：ruoyi-common/pom.xml" -ForegroundColor Green
} else {
    Write-Host "   源文件不存在" -ForegroundColor Red
}

# 4. 复制 ruoyi-common-bom/pom.xml
Write-Host "[4/15] 复制 ruoyi-common-bom/pom.xml..." -ForegroundColor Yellow
if (Test-Path "$sourceBase\ruoyi-common\ruoyi-common-bom\pom.xml") {
    Copy-Item -Path "$sourceBase\ruoyi-common\ruoyi-common-bom\pom.xml" -Destination "$targetBase\ruoyi-common\ruoyi-common-bom\pom.xml" -Force
    Write-Host "   已复制" -ForegroundColor Green
}

# 5-18. 复制 ruoyi-common 子模块
$commonModules = @(
    "ruoyi-common-core", "ruoyi-common-doc", "ruoyi-common-encrypt", 
    "ruoyi-common-excel", "ruoyi-common-idempotent", "ruoyi-common-json",
    "ruoyi-common-log", "ruoyi-common-mail", "ruoyi-common-mybatis",
    "ruoyi-common-oss", "ruoyi-common-ratelimiter", "ruoyi-common-redis",
    "ruoyi-common-satoken", "ruoyi-common-security"
)

for ($i = 0; $i -lt $commonModules.Length; $i++) {
    $module = $commonModules[$i]
    Write-Host "[$(5+$i)/18] 复制 ruoyi-common/$module/pom.xml..." -ForegroundColor Yellow
    $sourcePath = "$sourceBase\ruoyi-common\$module\pom.xml"
    $targetPath = "$targetBase\ruoyi-common\$module\pom.xml"
    
    if (Test-Path $sourcePath) {
        Copy-Item -Path $sourcePath -Destination $targetPath -Force
        Write-Host "   已复制：ruoyi-common/$module/pom.xml" -ForegroundColor Green
    } else {
        Write-Host "    跳过：$module (源文件不存在)" -ForegroundColor Gray
    }
}

# 19. 复制 ruoyi-modules/pom.xml
Write-Host "[19/23] 复制 ruoyi-modules/pom.xml..." -ForegroundColor Yellow
if (Test-Path "$sourceBase\ruoyi-modules\pom.xml") {
    Copy-Item -Path "$sourceBase\ruoyi-modules\pom.xml" -Destination "$targetBase\ruoyi-modules\pom.xml" -Force
    Write-Host "   已复制：ruoyi-modules/pom.xml" -ForegroundColor Green
} else {
    Write-Host "   源文件不存在" -ForegroundColor Red
}

# 20-23. 复制 ruoyi-modules 子模块
$modulesModules = @("ruoyi-demo", "ruoyi-erp-api", "ruoyi-generator", "ruoyi-system")

for ($i = 0; $i -lt $modulesModules.Length; $i++) {
    $module = $modulesModules[$i]
    Write-Host "[$(20+$i)/23] 复制 ruoyi-modules/$module/pom.xml..." -ForegroundColor Yellow
    $sourcePath = "$sourceBase\ruoyi-modules\$module\pom.xml"
    $targetPath = "$targetBase\ruoyi-modules\$module\pom.xml"
    
    if (Test-Path $sourcePath) {
        Copy-Item -Path $sourcePath -Destination $targetPath -Force
        Write-Host "   已复制：ruoyi-modules/$module/pom.xml" -ForegroundColor Green
    } else {
        Write-Host "    跳过：$module (源文件不存在)" -ForegroundColor Gray
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  复制完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "下一步操作：" -ForegroundColor Cyan
Write-Host "1. 在 IDEA 中重新打开 baiyu-ruoyi 项目" -ForegroundColor White
Write-Host "2. 右键根目录 pom.xml → Maven → Reload Project" -ForegroundColor White
Write-Host "3. 执行 mvn clean compile -DskipTests" -ForegroundColor White
Write-Host "4. 重启后端服务" -ForegroundColor White
Write-Host ""
