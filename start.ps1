# RuoYi-WMS One-Click Startup Script
# Optimized version with separate console windows

$ErrorActionPreference = "Continue"
$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$ruoyiPath = "$projectRoot\baiyu-ruoyi"
$backendPath = "$ruoyiPath\ruoyi-admin-wms"
$frontendPath = "$projectRoot\baiyu-web"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  RuoYi-WMS 前后端一键启动脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "后端：ruoyi-admin-wms (端口：8180)" -ForegroundColor White
Write-Host "前端：baiyu-web (端口：8899)" -ForegroundColor White
Write-Host ""

# Step 1: Start backend in new window
Write-Host "[1/2] 正在启动后端服务..." -ForegroundColor Yellow
$backendArgs = @(
    "-NoExit",
    "-Command",
    "Set-Location '$backendPath'; " +
    "Write-Host '后端编译中...' -ForegroundColor Green; " +
    "mvn clean install -DskipTests; " +
    "if (`$LASTEXITCODE -eq 0) { " +
    "  Write-Host '' ; " +
    "  Write-Host '后端启动成功！' -ForegroundColor Green; " +
    "  Write-Host '访问地址：http://localhost:8180' -ForegroundColor Cyan; " +
    "} else { " +
    "  Write-Host '后端启动失败，请检查错误信息' -ForegroundColor Red; " +
    "  pause; " +
    "}"
)
Start-Process powershell -ArgumentList $backendArgs -WindowStyle Normal -Verb RunAs

# Wait for backend to start compiling
Start-Sleep -Seconds 3

# Step 2: Start frontend in new window
Write-Host "[2/2] 正在启动前端项目..." -ForegroundColor Yellow
$frontendArgs = @(
    "-NoExit",
    "-Command",
    "Set-Location '$frontendPath'; " +
    "Write-Host '前端启动中...' -ForegroundColor Green; " +
    "npm run dev"
)
Start-Process powershell -ArgumentList $frontendArgs -WindowStyle Normal

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  服务启动完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "服务信息:" -ForegroundColor Yellow
Write-Host "  后端 URL: http://localhost:8180" -ForegroundColor White
Write-Host "  前端 URL: http://localhost:8899" -ForegroundColor White
Write-Host ""
Write-Host "提示信息:" -ForegroundColor Yellow
Write-Host "  - 后端和前端分别在独立窗口运行" -ForegroundColor Gray
Write-Host "  - 请等待后端 Maven 编译完成（首次启动较慢）" -ForegroundColor Gray
Write-Host "  - 前端 Vite 启动后会显示访问地址" -ForegroundColor Gray
Write-Host "  - 关闭此窗口不会影响已启动的服务" -ForegroundColor Gray
Write-Host "  - 停止服务：直接关闭对应的 PowerShell 窗口即可" -ForegroundColor Gray
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
pause
