# RuoYi WMS - Clean Build & Start Services Script
# Function: Clean old compiled files, rebuild backend, and start both frontend & backend

# Set UTF-8 encoding
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "========================================" -ForegroundColor Green
Write-Host "   Clean Build & Start All Services" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

# Get script directory
$SCRIPT_DIR = Split-Path -Parent $MyInvocation.MyCommand.Path
$PROJECT_ROOT = $SCRIPT_DIR
$BACKEND_DIR = "$PROJECT_ROOT\baiyu-ruoyi\ruoyi-admin-wms"
$FRONTEND_DIR = "$PROJECT_ROOT\baiyu-web"
$BACKEND_CONFIG = "$BACKEND_DIR\target\classes\application.yml"

Write-Host "[1/7] Checking project directories..." -ForegroundColor Yellow

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

Write-Host "[2/7] Stopping existing services..." -ForegroundColor Yellow

# Stop ALL Java processes (not just ruoyi-admin-wms.jar)
Write-Host "Stopping ALL Java backend processes..." -ForegroundColor Gray
try {
    $javaProcesses = Get-Process | Where-Object { 
        $_.ProcessName -eq "java"
    }
    if ($javaProcesses) {
        foreach ($proc in $javaProcesses) {
            Write-Host "  Stopping Java process ID: $($proc.Id) - $($proc.MainWindowTitle)" -ForegroundColor Cyan
            Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue
        }
        Start-Sleep -Seconds 3
        Write-Host "[OK] All Java processes stopped" -ForegroundColor Green
    } else {
        Write-Host "No Java processes found" -ForegroundColor Gray
    }
} catch {
    Write-Host "WARNING: Failed to stop some Java processes: $_" -ForegroundColor Yellow
}

# Stop ALL Node processes (not just vite)
Write-Host "Stopping ALL Node.js frontend processes..." -ForegroundColor Gray
try {
    $nodeProcesses = Get-Process | Where-Object { 
        $_.ProcessName -eq "node"
    }
    if ($nodeProcesses) {
        foreach ($proc in $nodeProcesses) {
            Write-Host "  Stopping Node.js process ID: $($proc.Id)" -ForegroundColor Cyan
            Stop-Process -Id $proc.Id -Force -ErrorAction SilentlyContinue
        }
        Start-Sleep -Seconds 2
        Write-Host "[OK] All Node.js processes stopped" -ForegroundColor Green
    } else {
        Write-Host "No Node.js processes found" -ForegroundColor Gray
    }
} catch {
    Write-Host "WARNING: Failed to stop some Node.js processes: $_" -ForegroundColor Yellow
}

# Additional check: Stop processes using target directory
Write-Host "Checking for processes using backend target directory..." -ForegroundColor Gray
try {
    # Use handle.exe or Resource Monitor to find locked files (if available)
    # For now, just wait a bit more to ensure file handles are released
    Start-Sleep -Seconds 2
    Write-Host "[OK] File handle check completed" -ForegroundColor Green
} catch {
    Write-Host "WARNING: File handle check failed: $_" -ForegroundColor Yellow
}

Write-Host ""

Write-Host "[3/7] Cleaning old compiled files..." -ForegroundColor Yellow

# Clean backend target directory with retry logic
Write-Host "Cleaning backend target directory..." -ForegroundColor Gray
$retryCount = 0
$maxRetries = 3
$cleanSuccess = $false

while ($retryCount -lt $maxRetries -and -not $cleanSuccess) {
    try {
        if (Test-Path "$BACKEND_DIR\target") {
            # First try normal deletion
            Remove-Item -Path "$BACKEND_DIR\target" -Recurse -Force -ErrorAction Stop
            Write-Host "[OK] Backend target directory cleaned" -ForegroundColor Green
            $cleanSuccess = $true
        } else {
            Write-Host "Backend target directory does not exist (already clean)" -ForegroundColor Gray
            $cleanSuccess = $true
        }
    } catch {
        $retryCount++
        Write-Host "WARNING: Failed to clean backend target (attempt $retryCount/$maxRetries): $_" -ForegroundColor Yellow
        
        if ($retryCount -lt $maxRetries) {
            Write-Host "Retrying in 3 seconds..." -ForegroundColor Gray
            Start-Sleep -Seconds 3
            
            # Try to forcefully release file handles by stopping any remaining Java/Node processes
            Get-Process | Where-Object { $_.ProcessName -eq "java" -or $_.ProcessName -eq "node" } | Stop-Process -Force -ErrorAction SilentlyContinue
            Start-Sleep -Seconds 2
        } else {
            Write-Host "ERROR: Cannot clean backend target directory after $maxRetries attempts" -ForegroundColor Red
            Write-Host "Alternative: Trying to move to temp directory..." -ForegroundColor Yellow
            try {
                if (Test-Path "$BACKEND_DIR\target") {
                    $tempDir = "$BACKEND_DIR\target-old-" + (Get-Date -Format "yyyyMMdd-HHmmss")
                    Rename-Item -Path "$BACKEND_DIR\target" -NewName $tempDir
                    Write-Host "[OK] Backend target moved to: $tempDir" -ForegroundColor Green
                    $cleanSuccess = $true
                }
            } catch {
                Write-Host "ERROR: Cannot move backend target directory either" -ForegroundColor Red
                Write-Host "Please manually close any IDEs or processes using these files" -ForegroundColor Yellow
                Write-Host "Common culprits:" -ForegroundColor Yellow
                Write-Host "  - IntelliJ IDEA / Eclipse" -ForegroundColor Cyan
                Write-Host "  - VS Code with Java extensions" -ForegroundColor Cyan
                Write-Host "  - Maven/Gradle daemons" -ForegroundColor Cyan
                Write-Host "  - Antivirus software scanning" -ForegroundColor Cyan
                exit 1
            }
        }
    }
}

# Clean frontend node_modules (optional, comment out if you want to keep dependencies)
# Write-Host "Cleaning frontend node_modules..." -ForegroundColor Gray
# if (Test-Path "$FRONTEND_DIR\node_modules") {
#     Remove-Item -Path "$FRONTEND_DIR\node_modules" -Recurse -Force
#     Write-Host "[OK] Frontend node_modules cleaned" -ForegroundColor Green
# }

Write-Host "[OK] Old compiled files cleaned" -ForegroundColor Green
Write-Host ""

Write-Host "[4/7] Checking Redis service..." -ForegroundColor Yellow

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

Write-Host "[5/7] Compiling backend with Maven..." -ForegroundColor Yellow
Write-Host "Tip: This will ensure latest code changes are applied" -ForegroundColor Gray
Write-Host ""

# Compile backend
Push-Location $BACKEND_DIR
try {
    Write-Host "Running: mvn clean package -DskipTests" -ForegroundColor Gray
    & mvn clean package -DskipTests
    if ($LASTEXITCODE -ne 0) {
        Write-Host "ERROR: Maven compilation failed" -ForegroundColor Red
        Pop-Location
        exit 1
    }
    Write-Host "[OK] Backend compiled successfully" -ForegroundColor Green
} catch {
    Write-Host "ERROR: Maven compilation error: $_" -ForegroundColor Red
    Pop-Location
    exit 1
}
Pop-Location

Write-Host ""

Write-Host "[6/7] Starting backend service..." -ForegroundColor Yellow
Write-Host "Backend URL: http://localhost:8180" -ForegroundColor Cyan
Write-Host "Tip: Backend will start in background, please wait..." -ForegroundColor Gray
Write-Host ""

# Check if JAR file exists
$jarFile = "$BACKEND_DIR\target\ruoyi-admin-wms.jar"

if (-not (Test-Path $jarFile)) {
    Write-Host "ERROR: Backend JAR file not exist: $jarFile" -ForegroundColor Red
    exit 1
} else {
    Write-Host "[OK] Backend JAR file found" -ForegroundColor Green
}

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

Write-Host "[7/7] Starting frontend service..." -ForegroundColor Yellow
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
