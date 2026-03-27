@echo off
chcp 65001 >nul
color 0A
echo ==========================================================
echo             RuoYi 终极一键清理 + 编译 + 启动
echo                  解决99%启动/编译异常
echo ==========================================================
echo.

echo 【1/4】正在清理 Maven 缓存...
call mvn clean

echo.
echo 【2/4】正在清理所有模块损坏的 target 文件...
for /d %%i in (ruoyi-*) do (
    if exist "%%i\target" rd /s /q "%%i\target" >nul 2>&1
)

echo.
echo 【3/4】正在重新编译项目（跳过测试）...
call mvn compile -Dmaven.test.skip=true

echo.
echo 【4/4】正在启动 Ruoyi 后台服务...
cd ruoyi-admin
call mvn spring-boot:run

echo.
echo ==========================================================
echo                    项目启动完成！
echo ==========================================================
pause