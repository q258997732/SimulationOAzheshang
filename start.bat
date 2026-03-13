@echo off
chcp 65001 > nul
echo ==========================================
echo    OA工单管理系统 - RPA演示项目
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

REM 编译并打包项目
echo 正在编译并打包项目...
call mvn clean package -DskipTests

if %errorlevel% neq 0 (
    echo 错误：项目编译失败
    pause
    exit /b 1
)

echo 项目编译成功
echo.

REM 检查WAR文件是否生成
if not exist "target\simulation-oa.war" (
    echo 错误：WAR文件未生成
    pause
    exit /b 1
)

echo WAR文件生成成功
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
