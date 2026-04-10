# Dev environment startup script
# Uses Docker for dependencies, runs frontend/backend locally

Write-Host "Starting Visual Search Dev Environment..." -ForegroundColor Cyan
Write-Host ""

# Step 1: Start dependency services
Write-Host "[1/3] Starting dependency services..." -ForegroundColor Yellow
docker-compose -f docker-compose.dev.yml up -d

if ($LASTEXITCODE -ne 0) {
    Write-Host "Failed to start services!" -ForegroundColor Red
    exit 1
}

Write-Host "OK - Services started" -ForegroundColor Green
Write-Host ""

# Step 2: Wait for services
Write-Host "[2/3] Waiting for services to be ready..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

# Step 3: Show info
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Dependency Services Ready" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Service URLs:" -ForegroundColor White
Write-Host "  MySQL:   localhost:3307 (vs_user / vs_pass123)" -ForegroundColor Gray
Write-Host "  Milvus:  localhost:19530" -ForegroundColor Gray
Write-Host "  MinIO:   http://localhost:9000 (minioadmin / minioadmin)" -ForegroundColor Gray
Write-Host "  Rembg:   http://localhost:7000" -ForegroundColor Gray
Write-Host ""
Write-Host "Start local services:" -ForegroundColor Yellow
Write-Host ""
Write-Host "  Backend (new terminal):" -ForegroundColor White
Write-Host "    cd backend" -ForegroundColor Gray
Write-Host "    python -m uvicorn main:app --reload --host 0.0.0.0 --port 8000" -ForegroundColor Cyan
Write-Host ""
Write-Host "  Frontend (new terminal):" -ForegroundColor White
Write-Host "    cd frontend" -ForegroundColor Gray
Write-Host "    npm run dev" -ForegroundColor Cyan
Write-Host ""
Write-Host "Tips:" -ForegroundColor Yellow
Write-Host "  - Backend: http://localhost:8000" -ForegroundColor Gray
Write-Host "  - Frontend: http://localhost:5173" -ForegroundColor Gray
Write-Host "  - Auto-reload enabled" -ForegroundColor Gray
Write-Host ""
Write-Host "Stop all services:" -ForegroundColor Yellow
Write-Host "  docker-compose -f docker-compose.dev.yml down" -ForegroundColor Cyan
Write-Host ""
