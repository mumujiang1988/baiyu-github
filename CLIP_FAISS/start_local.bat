@echo off
chcp 65001
echo ========================================
echo 企业产品以图搜系统 - 本地部署启动
echo ========================================
echo.

echo [1/2] 启动后端服务...
start "后端服务 - FastAPI" cmd /k "cd backend && python main.py"
timeout /t 5 /nobreak >nul

echo [2/2] 启动前端服务...
start "前端服务 - Vue3" cmd /k "cd frontend && npm run dev"

echo.
echo ========================================
echo 服务启动完成！
echo ========================================
echo.
echo 访问地址：
echo   前端: http://localhost:5173
echo   后端: http://localhost:8000
echo   API文档: http://localhost:8000/docs
echo.
echo 按任意键退出...
pause >nul
