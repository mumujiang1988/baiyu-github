# Visual Search 项目更新脚本
# 自动编译前后端并重新构建 Docker 镜像

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Visual Search 项目更新部署" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 1. 停止现有服务
Write-Host "[1/5] 停止现有服务..." -ForegroundColor Yellow
docker-compose down
Write-Host " 服务已停止" -ForegroundColor Green
Write-Host ""

# 2. 编译前端
Write-Host "[2/5] 编译前端..." -ForegroundColor Yellow
Set-Location frontend
npm run build
if ($LASTEXITCODE -ne 0) {
    Write-Host " 前端编译失败" -ForegroundColor Red
    exit 1
}
Set-Location ..
Write-Host " 前端编译完成" -ForegroundColor Green
Write-Host ""

# 3. 重新构建并启动所有服务
Write-Host "[3/5] 重新构建 Docker 镜像并启动服务..." -ForegroundColor Yellow
docker-compose up -d --build
if ($LASTEXITCODE -ne 0) {
    Write-Host " Docker 构建失败" -ForegroundColor Red
    exit 1
}
Write-Host " Docker 服务已启动" -ForegroundColor Green
Write-Host ""

# 4. 等待服务健康检查
Write-Host "[4/5] 等待服务就绪..." -ForegroundColor Yellow
Start-Sleep -Seconds 10
Write-Host " 服务已就绪" -ForegroundColor Green
Write-Host ""

# 5. 显示服务状态
Write-Host "[5/5] 服务状态:" -ForegroundColor Yellow
docker-compose ps
Write-Host ""

Write-Host "========================================" -ForegroundColor Green
Write-Host "   更新部署完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "访问地址:" -ForegroundColor Cyan
Write-Host "  前端: http://localhost:80" -ForegroundColor White
Write-Host "  后端: http://localhost:8080" -ForegroundColor White
Write-Host "  MinIO控制台: http://localhost:9001" -ForegroundColor White
Write-Host ""
