#!/bin/bash

echo "正在停止OA工单管理系统..."
docker-compose down

if [ $? -eq 0 ]; then
    echo "服务已停止"
else
    echo "停止服务时出现错误"
    exit 1
fi
