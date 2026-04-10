@echo off
chcp 65001 >nul
color 0A
echo ==============================================
echo          若依ERP 一键构建 & 启动脚本
echo ==============================================
echo.

:: ========== 阶段 1: 清理准备 ==========
echo === 终止 Java 进程 ===
taskkill /f /im java.exe 2>nul
taskkill /f /im javaw.exe 2>nul
timeout /t 2 /nobreak >nul

echo.
echo === 切换到项目目录 ===
cd /d "D:\baiyuyunma\baiyu-github\baiyu-github\baiyu-ruoyi"

echo.
echo === 清理 target 目录 ===
set "successCount=0"
set "totalCount=0"

call :cleanTarget ".\ruoyi-modules\ruoyi-system\target"
call :cleanTarget ".\ruoyi-modules\ruoyi-demo\target"
call :cleanTarget ".\ruoyi-modules\ruoyi-erp-api\target"
call :cleanTarget ".\ruoyi-modules\ruoyi-generator\target"
call :cleanTarget ".\ruoyi-admin-wms\target"
call :cleanTarget ".\ruoyi-common\ruoyi-common-mybatis\target"

echo 共清理 %successCount%/%totalCount% 个目录
echo.

:: ========== 阶段 2: Maven 编译 ==========
echo === 开始 Maven 多线程编译 ===
mvn clean install -DskipTests -T 16
if %errorlevel% neq 0 (
    echo.
    echo  Maven 编译失败
    pause
    exit /b 1
)
echo  Maven 编译成功！
echo.

:: ========== 阶段 3: 启动后端 ==========
echo === 启动后端服务 (ruoyi-admin-wms) ===
cd /d "D:\baiyuyunma\baiyu-github\baiyu-github\baiyu-ruoyi\ruoyi-admin-wms"

:: 使用独立 CMD 窗口启动后端
start "后端服务" cmd /k "cd /d D:\baiyuyunma\baiyu-github\baiyu-github\baiyu-ruoyi\ruoyi-admin-wms && echo 正在启动后端... && mvn spring-boot:run"

:: 等待后端启动
echo.
echo [INFO] 等待后端服务启动中...
timeout /t 10 /nobreak >nul

:: ========== 阶段 4: 启动前端 ==========
echo.
echo === 启动前端服务 ===
:: 使用独立 CMD 窗口启动前端
start "前端服务" cmd /k "cd /d D:\baiyuyunma\baiyu-github\baiyu-github\baiyu-web && echo 正在启动前端... && npm run dev"

echo.
echo ==============================================
echo  前后端服务已全部启动！
echo ==============================================
echo   - 后端地址：http://localhost:8080
echo   - 前端地址：http://localhost:80
echo.
echo 提示：已在两个独立 CMD 窗口启动服务
echo ==============================================
timeout /t 2

:: ========== 清理 target 函数 ==========
:cleanTarget
set /a totalCount+=1
if exist "%~1" (
    rd /s /q "%~1" 2>nul
    if exist "%~1" (
        echo ⚠ 清理失败：%~1
    ) else (
        echo √ 已清理：%~1
        set /a successCount+=1
    )
) else (
    echo - 跳过（不存在）：%~1
)
exit /b