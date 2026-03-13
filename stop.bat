@echo off
echo 正在停止OA工单管理系统...
docker-compose down

if %errorlevel% equ 0 (
    echo 服务已停止
) else (
    echo 停止服务时出现错误
    exit /b 1
)

pause
