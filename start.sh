#!/bin/bash

echo "=========================================="
echo "   OA工单管理系统 - RPA演示项目"
echo "=========================================="
echo ""

# 检查Docker是否安装
if ! command -v docker &> /dev/null; then
    echo "错误：未检测到Docker，请先安装Docker"
    exit 1
fi

# 检查Docker Compose是否安装
if ! command -v docker-compose &> /dev/null; then
    echo "错误：未检测到Docker Compose，请先安装Docker Compose"
    exit 1
fi

# 编译并打包项目
echo "正在编译并打包项目..."
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "错误：项目编译失败"
    exit 1
fi

echo "项目编译成功"
echo ""

# 检查WAR文件是否生成
if [ ! -f "target/simulation-oa.war" ]; then
    echo "错误：WAR文件未生成"
    exit 1
fi

echo "WAR文件生成成功"
echo ""

# 停止并删除旧容器
echo "正在停止旧容器..."
docker-compose down

# 启动服务
echo "正在启动服务..."
docker-compose up -d

if [ $? -eq 0 ]; then
    echo ""
    echo "=========================================="
    echo "   服务启动成功！"
    echo "=========================================="
    echo ""
    echo "系统地址：http://localhost:8080"
    echo ""
    echo "数据库连接信息："
    echo "  主机：localhost:3306"
    echo "  数据库：oadb"
    echo "  用户名：oauser"
    echo "  密码：oapass"
    echo ""
    echo "查看日志：docker-compose logs -f"
    echo "停止服务：./stop.sh"
    echo ""
    echo "请等待约30秒后访问系统..."
    echo ""
else
    echo "错误：服务启动失败"
    echo "查看日志：docker-compose logs"
    exit 1
fi
