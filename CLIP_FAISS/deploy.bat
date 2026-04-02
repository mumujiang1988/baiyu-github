@echo off
chcp 65001
REM ========================================
REM 企业产品以图搜系统 - Windows部署脚本
REM ========================================

setlocal enabledelayedexpansion

:menu
cls
echo ========================================
echo 企业产品以图搜系统 - 部署管理
echo ========================================
echo.
echo 1. 部署系统
echo 2. 启动服务
echo 3. 停止服务
echo 4. 重启服务
echo 5. 查看状态
echo 6. 查看日志
echo 7. 清理系统
echo 8. 退出
echo.
set /p choice=请选择操作（1-8）：

if "%choice%"=="1" goto deploy
if "%choice%"=="2" goto start
if "%choice%"=="3" goto stop
if "%choice%"=="4" goto restart
if "%choice%"=="5" goto status
if "%choice%"=="6" goto logs
if "%choice%"=="7" goto clean
if "%choice%"=="8" goto end

:deploy
echo.
echo [INFO] 开始部署系统...
echo.

REM 检查Docker
docker --version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker未安装，请先安装Docker Desktop
    pause
    goto menu
)

docker-compose --version >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Docker Compose未安装
    pause
    goto menu
)

echo [INFO] Docker环境检查通过

REM 创建目录
echo [INFO] 创建必要的目录...
if not exist "backend\uploads" mkdir backend\uploads
if not exist "backend\logs" mkdir backend\logs
if not exist "monitoring\grafana\dashboards" mkdir monitoring\grafana\dashboards

REM 构建镜像
echo [INFO] 构建Docker镜像...
docker-compose build

REM 启动服务
echo [INFO] 启动服务...
docker-compose up -d

REM 等待服务就绪
echo [INFO] 等待服务就绪...
timeout /t 10 /nobreak >nul

REM 检查状态
docker-compose ps

echo.
echo ========================================
echo 部署完成！
echo ========================================
echo.
echo 访问地址：
echo   前端: http://localhost
echo   后端API: http://localhost:8000
echo   API文档: http://localhost:8000/docs
echo   Grafana: http://localhost:3000
echo.
pause
goto menu

:start
echo.
echo [INFO] 启动服务...
docker-compose up -d
echo [INFO] 服务已启动
pause
goto menu

:stop
echo.
echo [INFO] 停止服务...
docker-compose down
echo [INFO] 服务已停止
pause
goto menu

:restart
echo.
echo [INFO] 重启服务...
docker-compose restart
echo [INFO] 服务已重启
pause
goto menu

:status
echo.
echo [INFO] 服务状态：
docker-compose ps
pause
goto menu

:logs
echo.
echo [INFO] 查看日志（按Ctrl+C退出）...
docker-compose logs -f
goto menu

:clean
echo.
echo [WARN] 清理所有容器、镜像和数据卷...
set /p confirm=确认清理吗？(y/n):
if /i "%confirm%"=="y" (
    docker-compose down -v --rmi-all
    echo [INFO] 清理完成
) else (
    echo [INFO] 取消清理
)
pause
goto menu

:end
echo.
echo 感谢使用企业产品以图搜系统！
exit /b 0
