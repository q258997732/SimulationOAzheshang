#!/bin/bash

echo "=========================================="
echo "   OA工单管理系统 - RPA演示项目"
echo "   直接启动模式（使用已有WAR包）"
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

# 检查WAR文件是否存在
if [ ! -f "target/simulation-oa.war" ]; then
    echo "错误：WAR文件不存在"
    echo "请确保 target/simulation-oa.war 文件存在"
    echo ""
    echo "如需编译，请运行: ./start.sh"
    exit 1
fi

echo "WAR文件已就绪: target/simulation-oa.war"
ls -lh "target/simulation-oa.war"
echo ""

# 检查WAR包中是否包含MySQL驱动
echo "检查WAR包内容中的MySQL驱动:"
jar -tf "target/simulation-oa.war" | grep -E "mysql|gson"
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
