@echo off
chcp 65001 >nul
color 0A
echo ==========================================================
echo               RuoYi 前后端 一键启动脚本
echo               后端：ruoyi-admin-wms
echo               前端：baiyu-web
echo ==========================================================
echo.

echo ===================== 后端启动 =========================
echo 正在启动后端服务（ruoyi-admin-wms）...
start "后端服务" cmd /k "cd /d ""%~dp0baiyu-ruoyi"" && mvn -f ruoyi-admin-wms/pom.xml spring-boot:run -Dmaven.test.skip=true"

echo.
echo ===================== 前端启动 =========================
echo 正在启动前端项目（baiyu-web）...
start "前端服务" cmd /k "cd /d ""%~dp0baiyu-web"" && npm run dev"

echo.
echo ==========================================================
echo                前后端启动中，请稍候...
echo ==========================================================
pause