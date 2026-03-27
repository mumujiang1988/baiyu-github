@echo off
chcp 65001 >nul
color 0A
echo ==========================================================
echo               RuoYi 极速启动脚本（你的专用版）
echo ==========================================================
echo.

echo 🔄 1. 清理缓存（解决编译异常）
call mvn clean -Dmaven.test.skip=true

echo.
echo 🚀 2. 多线程编译（速度翻倍）
call mvn compile -Dmaven.test.skip=true -T 1C

echo.
echo ✅ 3. 启动你的主项目：ruoyi-admin-wms
call mvn -f ruoyi-admin-wms/pom.xml spring-boot:run -Dmaven.test.skip=true

echo.
echo ==========================================================
echo                    项目启动成功
echo ==========================================================
pause