# RuoYi WMS Auto Start Script
# Function: Auto check Redis, database config, ensure services start normally

# Set UTF-8 encoding
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "========================================" -ForegroundColor Green
Write-Host "   RuoYi WMS Auto Start Script" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

# Get script directory
$SCRIPT_DIR = Split-Path -Parent $MyInvocation.MyCommand.Path
$PROJECT_ROOT = $SCRIPT_DIR
$BACKEND_DIR = "$PROJECT_ROOT\baiyu-ruoyi\ruoyi-admin-wms"
$FRONTEND_DIR = "$PROJECT_ROOT\baiyu-web"
$BACKEND_CONFIG = "$BACKEND_DIR\target\classes\application.yml"

Write-Host "[1/6] Checking project directories..." -ForegroundColor Yellow

if (-not (Test-Path $BACKEND_DIR)) {
    Write-Host "ERROR: Backend directory not exist: $BACKEND_DIR" -ForegroundColor Red
    exit 1
}

if (-not (Test-Path $FRONTEND_DIR)) {
    Write-Host "ERROR: Frontend directory not exist: $FRONTEND_DIR" -ForegroundColor Red
    exit 1
}

Write-Host "[OK] Project directories check completed" -ForegroundColor Green
Write-Host ""

Write-Host "[2/6] Checking Redis service..." -ForegroundColor Yellow

# Check Redis service
$redisService = Get-Service | Where-Object {$_.Name -like "*Redis*"}
if ($redisService) {
    Write-Host "Found Redis service: $($redisService.Name)" -ForegroundColor Cyan
    if ($redisService.Status -ne "Running") {
        Write-Host "Starting Redis service..." -ForegroundColor Cyan
        Start-Service $redisService.Name
        Start-Sleep -Seconds 2
        Write-Host "[OK] Redis service started" -ForegroundColor Green
    } else {
        Write-Host "[OK] Redis service is running" -ForegroundColor Green
    }
    
    # Test Redis connection
    try {
        $redisTest = redis-cli ping 2>&1
        if ($redisTest -eq "PONG") {
            Write-Host "[OK] Redis connection test success" -ForegroundColor Green
        } elseif ($redisTest -like "*NOAUTH*") {
            Write-Host "WARNING: Redis need password auth, please check config" -ForegroundColor Yellow
        } else {
            Write-Host "WARNING: Redis connection test return: $redisTest" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "WARNING: Cannot execute redis-cli command, please confirm Redis installed" -ForegroundColor Yellow
    }
} else {
    Write-Host "WARNING: Redis service not found, try start with Docker..." -ForegroundColor Yellow
    try {
        $dockerCheck = docker ps 2>&1
        if ($LASTEXITCODE -eq 0) {
            docker run -d -p 6379:6379 --name redis-by redis:latest 2>&1 | Out-Null
            Start-Sleep -Seconds 3
            Write-Host "[OK] Redis Docker container started" -ForegroundColor Green
        } else {
            Write-Host "ERROR: Docker not available, please install Redis manually" -ForegroundColor Red
            exit 1
        }
    } catch {
        Write-Host "ERROR: Redis start failed, please install Redis manually" -ForegroundColor Red
        exit 1
    }
}

Write-Host ""

Write-Host "[3/6] Checking backend config..." -ForegroundColor Yellow

# Check Redis password config
if (Test-Path $BACKEND_CONFIG) {
    $configContent = Get-Content $BACKEND_CONFIG -Raw
    
    # Check spring.data.redis.password
    $springRedisMatch = $configContent -match 'spring\.data:\s*\r?\n\s*redis:.*?password:\s*(.+?)\r?\n'
    if ($springRedisMatch) {
        $springPassword = $matches[1].Trim()
        if ($springPassword -eq "null" -or [string]::IsNullOrWhiteSpace($springPassword)) {
            Write-Host "WARNING: spring.data.redis.password not set or null" -ForegroundColor Yellow
        } else {
            Write-Host "[OK] spring.data.redis.password configured" -ForegroundColor Green
        }
    } else {
        Write-Host "WARNING: spring.data.redis.password config not found" -ForegroundColor Yellow
    }
    
    # Check redisson.password
    $redissonPattern = 'redisson:[\s\S]*?singleServerConfig:[\s\S]*?password:\s*["'']?(.+?)["'']?\s*'
    $redissonMatch = $configContent -match $redissonPattern
    if ($redissonMatch) {
        $redissonPassword = $matches[1].Trim().Trim('"').Trim("'")
        if ([string]::IsNullOrWhiteSpace($redissonPassword)) {
            Write-Host "WARNING: redisson.password is empty string" -ForegroundColor Yellow
        } else {
            Write-Host "[OK] redisson.password configured" -ForegroundColor Green
        }
    } else {
        Write-Host "WARNING: redisson.password config not found" -ForegroundColor Yellow
    }
} else {
    Write-Host "WARNING: Backend config file not exist: $BACKEND_CONFIG" -ForegroundColor Yellow
}

Write-Host "[OK] Config check completed" -ForegroundColor Green
Write-Host ""

Write-Host "[4/6] Checking backend JAR file..." -ForegroundColor Yellow

# Check if JAR file exists
$jarFile = "$BACKEND_DIR\target\ruoyi-admin-wms.jar"

if (-not (Test-Path $jarFile)) {
    Write-Host "ERROR: Backend JAR file not exist: $jarFile" -ForegroundColor Red
    Write-Host "Please compile backend first using: mvn clean package -DskipTests" -ForegroundColor Yellow
    exit 1
} else {
    $jarAge = (Get-Date) - (Get-Item $jarFile).LastWriteTime
    Write-Host "[OK] Backend JAR file found (age: $([math]::Round($jarAge.TotalMinutes, 1)) minutes)" -ForegroundColor Green
}

Write-Host ""

Write-Host "[5/6] Starting backend service..." -ForegroundColor Yellow
Write-Host "Backend URL: http://localhost:8180" -ForegroundColor Cyan
Write-Host "Tip: Backend will start in background, please wait..." -ForegroundColor Gray
Write-Host ""

# Start backend using JAR file
Push-Location $BACKEND_DIR
try {
    # Start backend with JAR (faster than spring-boot:run)
    Write-Host "Starting backend with JAR file..." -ForegroundColor Gray
    $backendJob = Start-Process java -ArgumentList "-jar", "$jarFile" -PassThru -NoNewWindow
    Write-Host "[OK] Backend service started (Process ID: $($backendJob.Id))" -ForegroundColor Green
} catch {
    Write-Host "ERROR: Backend start failed: $_" -ForegroundColor Red
    Pop-Location
    exit 1
}
Pop-Location

# Wait backend startup (max 120 seconds)
$maxWait = 120
$waited = 0
$backendReady = $false
$checkEndpoints = @(
    "http://localhost:8180/actuator/health",
    "http://localhost:8180/swagger-ui.html",
    "http://localhost:8180/doc.html",
    "http://localhost:8180/login"
)

Write-Host ""
Write-Host "Checking backend service readiness..." -ForegroundColor Cyan

while ($waited -lt $maxWait -and -not $backendReady) {
    Start-Sleep -Seconds 2
    $waited += 2
    
    # Show progress every 6 seconds
    if ($waited % 6 -eq 0) {
        Write-Host "  Waiting ${waited} seconds..." -ForegroundColor Gray
    }
    
    # Try multiple endpoints
    foreach ($endpoint in $checkEndpoints) {
        try {
            $response = Invoke-WebRequest -Uri $endpoint -UseBasicParsing -TimeoutSec 2 -ErrorAction SilentlyContinue
            if ($response.StatusCode -in @(200, 302, 401, 403, 404)) {
                $backendReady = $true
                Write-Host "[OK] Backend service ready (Endpoint: $endpoint, Wait time: ${waited}s)" -ForegroundColor Green
                break
            }
        } catch {
            # Continue try next endpoint
        }
    }
}

if (-not $backendReady) {
    Write-Host ""
    Write-Host "ERROR: Backend service startup timeout (${maxWait}s)" -ForegroundColor Red
    Write-Host "Possible reasons:" -ForegroundColor Yellow
    Write-Host "  1. Database connection slow (check remote database network)" -ForegroundColor Yellow
    Write-Host "  2. Redis connection issue (check password config)" -ForegroundColor Yellow
    Write-Host "  3. Port occupied (check port 8180)" -ForegroundColor Yellow
    Write-Host "  4. Insufficient JVM memory (try adjust Xms/Xmx params)" -ForegroundColor Yellow
    exit 1
}

Write-Host ""

Write-Host "[6/6] Starting frontend service..." -ForegroundColor Yellow
Write-Host "Frontend URL: http://localhost:8899" -ForegroundColor Cyan
Write-Host "Tip: Frontend will start in background..." -ForegroundColor Gray
Write-Host ""

Push-Location $FRONTEND_DIR
try {
    # Start frontend process (background)
    $frontendJob = Start-Process powershell -ArgumentList "-NoExit", "-Command", "`& { cd '$FRONTEND_DIR'; npm run dev }" -PassThru -NoNewWindow
    Write-Host "[OK] Frontend service started (Process ID: $($frontendJob.Id))" -ForegroundColor Green
} catch {
    Write-Host "ERROR: Frontend start failed: $_" -ForegroundColor Red
    Pop-Location
    exit 1
}
Pop-Location

Write-Host "[OK] Frontend service starting..." -ForegroundColor Green
Write-Host ""

Write-Host "========================================" -ForegroundColor Green
Write-Host "   Service Startup Complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Frontend URL: http://localhost:8899" -ForegroundColor Cyan
Write-Host "Backend URL: http://localhost:8180" -ForegroundColor Cyan
Write-Host ""
Write-Host "Press Ctrl+C to stop all services" -ForegroundColor Yellow
Write-Host ""

try {
    while ($true) {
        Start-Sleep -Seconds 1
    }
} finally {
    Write-Host ""
    Write-Host "Stopping all services..." -ForegroundColor Yellow
    
    # Services started in foreground will stop via Ctrl+C
    Write-Host "[OK] Backend service stopped" -ForegroundColor Green
    Write-Host "[OK] Frontend service stopped" -ForegroundColor Green
    Write-Host "All services stopped" -ForegroundColor Green
}
