@echo off
chcp 65001
echo ========================================
echo 企业产品以图搜系统 - 快速启动脚本
echo ========================================
echo.

echo [1/3] 检查Python环境...
python --version
if errorlevel 1 (
    echo 错误：未安装Python，请先安装Python 3.8+
    pause
    exit /b 1
)

echo.
echo [2/3] 检查Node.js环境...
node --version
if errorlevel 1 (
    echo 错误：未安装Node.js，请先安装Node.js 16+
    pause
    exit /b 1
)

echo.
echo [3/3] 检查MySQL服务...
echo 请确保MySQL服务已启动，数据库已初始化
echo.

echo ========================================
echo 启动选项：
echo ========================================
echo 1. 启动后端服务
echo 2. 启动前端服务
echo 3. 同时启动前后端服务
echo 4. 安装后端依赖
echo 5. 安装前端依赖
echo 6. 初始化数据库
echo 7. 退出
echo ========================================
echo.

set /p choice=请选择操作（1-7）：

if "%choice%"=="1" goto start_backend
if "%choice%"=="2" goto start_frontend
if "%choice%"=="3" goto start_all
if "%choice%"=="4" goto install_backend
if "%choice%"=="5" goto install_frontend
if "%choice%"=="6" goto init_database
if "%choice%"=="7" goto end

:start_backend
echo.
echo 启动后端服务...
cd backend
python main.py
goto end

:start_frontend
echo.
echo 启动前端服务...
cd frontend
npm run dev
goto end

:start_all
echo.
echo 同时启动前后端服务...
start "后端服务" cmd /k "cd backend && python main.py"
timeout /t 3 /nobreak >nul
start "前端服务" cmd /k "cd frontend && npm run dev"
echo 前后端服务已启动
echo 后端：http://localhost:8000
echo 前端：http://localhost:5173
goto end

:install_backend
echo.
echo 安装后端依赖...
cd backend
pip install -r requirements.txt
echo 后端依赖安装完成
goto end

:install_frontend
echo.
echo 安装前端依赖...
cd frontend
npm install
echo 前端依赖安装完成
goto end

:init_database
echo.
echo 初始化数据库...
echo 请手动执行以下步骤：
echo 1. 登录MySQL：mysql -u root -p
echo 2. 执行脚本：source database/init.sql
echo 或者使用MySQL Workbench导入 database/init.sql
goto end

:end
echo.
echo 感谢使用企业产品以图搜系统！
pause
