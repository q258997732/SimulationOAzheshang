@echo off
chcp 65001 > nul
echo ==========================================
echo    OA工单管理系统 - RPA演示项目
echo    直接启动模式（使用已有WAR包）
echo ==========================================
echo.

REM 检查Docker是否安装
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误：未检测到Docker，请先安装Docker
    pause
    exit /b 1
)

REM 检查Docker Compose是否安装
docker-compose --version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误：未检测到Docker Compose，请先安装Docker Compose
    pause
    exit /b 1
)

REM 检查WAR文件是否存在
if not exist "target\simulation-oa.war" (
    echo 错误：WAR文件不存在
    echo 请确保 target\simulation-oa.war 文件存在
    echo.
    echo 如需编译，请运行: start.bat
    pause
    exit /b 1
)

echo WAR文件已就绪: target\simulation-oa.war
dir "target\simulation-oa.war"
echo.

REM 检查WAR包中是否包含MySQL驱动
echo 检查WAR包内容中的驱动:
jar -tf "target\simulation-oa.war" | findstr /i "mysql gson"
echo.

REM 停止并删除旧容器
echo 正在停止旧容器...
docker-compose down

REM 启动服务
echo 正在启动服务...
docker-compose up -d

if %errorlevel% equ 0 (
    echo.
    echo ==========================================
    echo    服务启动成功！
    echo ==========================================
    echo.
    echo 系统地址：http://localhost:8080
    echo.
    echo 数据库连接信息：
    echo   主机：localhost:3306
    echo   数据库：oadb
    echo   用户名：oauser
    echo   密码：oapass
    echo.
    echo 查看日志：docker-compose logs -f
    echo 停止服务：stop.bat
    echo.
    echo 请等待约30秒后访问系统...
    echo.
) else (
    echo 错误：服务启动失败
    echo 查看日志：docker-compose logs
    pause
    exit /b 1
)

pause
