@echo off
chcp 65001 >nul
color 0A
echo ==========================================================
echo               RuoYi 干净稳定启动脚本（防锁版）
echo ==========================================================
echo.

echo 🔄 1. 停止所有 Java 进程（防止文件占用/锁定）
taskkill /f /im java.exe >nul 2>&1
taskkill /f /im javaw.exe >nul 2>&1
timeout /t 1 /nobreak >nul

echo 🔄 2. 安全清理 Maven 编译产物（不报错、不锁定）
call mvn clean -Dmaven.test.skip=true -q

echo 🚀 3. 多线程安全编译（速度快、不冲突）
call mvn compile -Dmaven.test.skip=true -T 1C -q

echo ✅ 4. 启动主项目：ruoyi-admin-wms
cd /d ruoyi-admin-wms
call mvn spring-boot:run -Dmaven.test.skip=true

echo.
echo ==========================================================
echo                    项目启动完成
echo ==========================================================
pause