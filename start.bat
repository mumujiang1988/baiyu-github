@echo off
chcp 65001 >nul
color 0A

echo ==========================================================
echo               RuoYi-WMS 前后端一键启动脚本
echo               后端：ruoyi-admin-wms (端口：8180)
echo               前端：baiyu-web (端口：8899)
echo ==========================================================
echo.

:: 获取脚本所在目录
set SCRIPT_DIR=%~dp0
set RUOYI_PATH=%SCRIPT_DIR%baiyu-ruoyi
set BACKEND_PATH=%RUOYI_PATH%\ruoyi-admin-wms
set FRONTEND_PATH=%SCRIPT_DIR%baiyu-web

:: ===================== 后端启动 =========================
echo [1/2] 正在启动后端服务...
echo 工作目录：%BACKEND_PATH%
start "RuoYi 后端服务" cmd /k "cd /d "%BACKEND_PATH%" && echo 后端编译中... && mvn clean install -DskipTests && echo. && echo 后端启动成功！访问地址：http://localhost:8180 && pause"

:: 等待后端开始编译
timeout /t 3 /nobreak >nul

echo.
:: ===================== 前端启动 =========================
echo [2/2] 正在启动前端项目...
echo 工作目录：%FRONTEND_PATH%
start "RuoYi 前端服务" cmd /k "cd /d "%FRONTEND_PATH%" && echo 前端启动中... && npm run dev"

echo.
echo ==========================================================
echo                服务启动中，请稍候...
echo ==========================================================
echo.
echo 提示信息:
echo   - 后端服务在新窗口运行，请等待编译完成
echo   - 前端服务在新窗口运行，请等待 Vite 启动完成
echo   - 后端访问地址：http://localhost:8180
echo   - 前端访问地址：http://localhost:8899
echo   - 关闭此窗口不会影响已启动的服务
echo.
echo ==========================================================
pause
