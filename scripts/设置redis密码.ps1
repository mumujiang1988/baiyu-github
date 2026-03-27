# Set Redis Password Permanently for Windows
# This script will configure Redis with password: difyai123456

$redisPassword = "difyai123456"
$redisHost = "127.0.0.1"
$redisPort = "6379"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Redis Password Configuration Tool" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Check Redis Service
Write-Host "[Step 1/4] Checking Redis service status..." -ForegroundColor Yellow
$redisService = Get-Service -Name "Redis*" -ErrorAction SilentlyContinue

if ($redisService) {
    Write-Host "  Found Redis service: $($redisService.Name)" -ForegroundColor Green
    Write-Host "  Status: $($redisService.Status)" -ForegroundColor Gray
    
    if ($redisService.Status -ne "Running") {
        Write-Host "  Starting Redis service..." -ForegroundColor Yellow
        Start-Service -Name $redisService.Name
        Start-Sleep -Seconds 3
        Write-Host "  Redis service started" -ForegroundColor Green
    }
} else {
    Write-Host "  Warning: Redis service not found, trying to connect directly..." -ForegroundColor Yellow
}

# Step 2: Find redis-cli
Write-Host ""
Write-Host "[Step 2/4] Locating redis-cli tool..." -ForegroundColor Yellow
$redisCliPaths = @(
    "C:\Program Files\Redis\redis-cli.exe",
    "C:\Program Files (x86)\Redis\redis-cli.exe",
    ".\redis-cli.exe",
    "$env:ProgramFiles\Redis\redis-cli.exe"
)

$redisCli = $null
foreach ($path in $redisCliPaths) {
    if (Test-Path $path) {
        $redisCli = $path
        Write-Host "  Found redis-cli at: $redisCli" -ForegroundColor Green
        break
    }
}

if (-not $redisCli) {
    Write-Host "  Using default redis-cli command" -ForegroundColor Yellow
    $redisCli = "redis-cli"
}

# Step 3: Set Redis Password
Write-Host ""
Write-Host "[Step 3/4] Setting Redis password..." -ForegroundColor Yellow
Write-Host "  Password: $redisPassword" -ForegroundColor Gray

try {
    # Try to set password using CONFIG SET
    $result = & $redisCli -h $redisHost -p $redisPort CONFIG SET requirepass "$redisPassword" 2>&1
    
    if ($LASTEXITCODE -eq 0 -or $result -eq "OK") {
        Write-Host "  Password set successfully (temporary)" -ForegroundColor Green
        Write-Host "  Note: This setting will be lost after Redis restart" -ForegroundColor Yellow
    } else {
        Write-Host "  Failed to set password via CONFIG SET" -ForegroundColor Red
        Write-Host "  Error: $result" -ForegroundColor Red
    }
    
    # Test the connection with new password
    Write-Host ""
    Write-Host "[Step 4/4] Verifying password configuration..." -ForegroundColor Yellow
    
    $testResult = & $redisCli -h $redisHost -p $redisPort -a "$redisPassword" PING 2>&1
    
    if ($testResult -eq "PONG") {
        Write-Host "  SUCCESS! Redis connection verified with password" -ForegroundColor Green
    } else {
        Write-Host "  FAILED! Password verification failed" -ForegroundColor Red
        Write-Host "  Response: $testResult" -ForegroundColor Red
    }
} catch {
    Write-Host "  Error occurred: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Configuration Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "IMPORTANT NOTES:" -ForegroundColor Yellow
Write-Host ""
Write-Host "1. Temporary Configuration (Current):" -ForegroundColor White
Write-Host "   - Password has been set using CONFIG SET command" -ForegroundColor Gray
Write-Host "   - This setting will be lost when Redis restarts" -ForegroundColor Gray
Write-Host ""
Write-Host "2. Permanent Configuration Required:" -ForegroundColor White
Write-Host "   To make the password permanent, you MUST:" -ForegroundColor Gray
Write-Host ""
Write-Host "   a) Locate your Redis configuration file:" -ForegroundColor Cyan
Write-Host "      - Usually: C:\Program Files\Redis\redis.windows.conf" -ForegroundColor Gray
Write-Host "      - Or: C:\Program Files\Redis\redis.conf" -ForegroundColor Gray
Write-Host ""
Write-Host "   b) Edit the config file and add/modify this line:" -ForegroundColor Cyan
Write-Host "      requirepass $redisPassword" -ForegroundColor Green
Write-Host ""
Write-Host "   c) Restart Redis service:" -ForegroundColor Cyan
Write-Host "      redis-server --service-stop" -ForegroundColor Yellow
Write-Host "      redis-server --service-start" -ForegroundColor Yellow
Write-Host ""
Write-Host "3. Application Configuration (Already Set):" -ForegroundColor White
Write-Host "   - Spring Data Redis: password configured" -ForegroundColor Gray
Write-Host "   - Redisson: password configured" -ForegroundColor Gray
Write-Host ""
Write-Host "Quick Verification Command:" -ForegroundColor Yellow
Write-Host "redis-cli -h $redisHost -p $redisPort -a $redisPassword PING" -ForegroundColor Cyan
Write-Host ""
Write-Host "Press any key to exit..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
