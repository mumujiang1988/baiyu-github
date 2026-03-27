# RuoYi-WMS One-Click Startup Script
# Includes: Stop Java processes, Clean build, Start backend, Start frontend

$ErrorActionPreference = "Stop"
$projectRoot = "D:\baiyuyunma\baiyu-github\baiyu-github"
$ruoyiPath = "$projectRoot\baiyu-ruoyi"
$backendPath = "$ruoyiPath\ruoyi-admin-wms"
$frontendPath = "$projectRoot\baiyu-web"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  RuoYi-WMS Startup Script" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Stop all Java processes
Write-Host "[1/4] Stopping Java processes..." -ForegroundColor Yellow
try {
    taskkill /f /im java.exe 2>$null
    Start-Sleep -Seconds 2
    Write-Host "Java processes stopped" -ForegroundColor Green
} catch {
    Write-Host "No running Java processes found" -ForegroundColor Gray
}
Write-Host ""

# Step 2: Clean target directories
Write-Host "[2/4] Cleaning old build files..." -ForegroundColor Yellow
$targetDirs = @(
    "$ruoyiPath\ruoyi-modules\ruoyi-system\target",
    "$ruoyiPath\ruoyi-modules\ruoyi-demo\target",
    "$ruoyiPath\ruoyi-modules\ruoyi-erp-api\target",
    "$ruoyiPath\ruoyi-modules\ruoyi-generator\target",
    "$backendPath\target"
)

foreach ($dir in $targetDirs) {
    if (Test-Path $dir) {
        Remove-Item -Recurse -Force $dir
        Write-Host "Cleaned: $dir" -ForegroundColor Green
    }
}
Write-Host ""

# Step 3: Maven clean build
Write-Host "[3/4] Running Maven clean install..." -ForegroundColor Yellow
Write-Host "Working directory: $ruoyiPath" -ForegroundColor Gray
Set-Location $ruoyiPath

try {
    mvn clean install -DskipTests
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Maven build successful" -ForegroundColor Green
    } else {
        throw "Maven build failed"
    }
} catch {
    Write-Host "Maven build failed, please check error messages" -ForegroundColor Red
    exit 1
}
Write-Host ""

# Step 4: Start backend and frontend
Write-Host "[4/4] Starting backend and frontend services..." -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Cyan
Write-Host "  Starting Backend Service..." -ForegroundColor Green
Write-Host "----------------------------------------" -ForegroundColor Cyan
Set-Location $backendPath

# 启动后端（新窗口）
Start-Process powershell -ArgumentList "-NoExit", "-Command", @"
Set-Location '$backendPath'
Write-Host '========================================' -ForegroundColor Cyan
Write-Host '  Starting RuoYi-WMS Backend...' -ForegroundColor Cyan
Write-Host '========================================' -ForegroundColor Cyan
Write-Host ''
mvn spring-boot:run
"@

Start-Sleep -Seconds 5

Write-Host "----------------------------------------" -ForegroundColor Cyan
Write-Host "  Starting Frontend Service..." -ForegroundColor Green
Write-Host "----------------------------------------" -ForegroundColor Cyan
Set-Location $frontendPath

# 启动前端（新窗口）
Start-Process powershell -ArgumentList "-NoExit", "-Command", @"
Set-Location '$frontendPath'
Write-Host '========================================' -ForegroundColor Cyan
Write-Host '  Starting RuoYi-WMS Frontend...' -ForegroundColor Cyan
Write-Host '========================================' -ForegroundColor Cyan
Write-Host ''
npm run dev
"@

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Startup Complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Service Information:" -ForegroundColor Yellow
Write-Host "  • Backend URL: http://localhost:8080" -ForegroundColor White
Write-Host "  • Frontend URL: http://localhost:80" -ForegroundColor White
Write-Host ""
Write-Host "Tips:" -ForegroundColor Yellow
Write-Host "  • Backend and frontend run in separate PowerShell windows" -ForegroundColor Gray
Write-Host "  • Close the PowerShell window to stop the corresponding service" -ForegroundColor Gray
Write-Host ""
