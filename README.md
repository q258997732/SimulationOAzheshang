# OA工单管理系统（RPA演示）

OA工单管理系统，用于RPA流程自动化演示。

## 功能特性

- **工单列表页**：查看所有工单，支持按状态、优先级、关键词筛选
- **工单发起页**：从企微聊天内容转换创建工单
- **工单详情查看**：查看工单详细信息和操作日志
- **工单状态流转**：待处理 → 处理中 → 已完成 → 已关闭

## 技术栈

- **JDK**：1.8
- **前端**：HTML + CSS + JavaScript
- **后端**：Java Servlet (javax.servlet-api 4.0.1)
- **数据库**：MySQL 8.0 (scrm_zhongxin)
- **部署**：Docker + Docker Compose
- **JSON处理**：Gson 2.10.1

## 项目结构

```
SimulationOAzheshang/
├── src/
│   ├── main/
│   │   ├── java/org/example/
│   │   │   ├── controller/WorkOrderServlet.java
│   │   │   └── filter/EncodingFilter.java
│   │   ├── resources/init.sql          # 数据库初始化SQL
│   │   └── webapp/                     # Web静态资源
├── lib/                                 # 依赖jar包
│   ├── mysql-connector-java-8.0.17.jar
│   └── gson-2.10.1.jar
├── target/simulation-oa.war            # 编译后的WAR包
├── docker-compose.yml                   # Docker编排
├── pom.xml                              # Maven配置
├── start.sh / start.bat                 # 编译并启动
├── start_direct.sh / start_direct.bat   # 直接启动（使用已有WAR包）
└── stop.sh / stop.bat                   # 停止服务
```

## 快速开始

### 前置要求

- Docker
- Docker Compose
- Maven + JDK 1.8（如需编译）

### 启动服务

**方式一：编译并启动（需要Maven+JDK）**

```bash
# Linux/Mac
./start.sh

# Windows
start.bat
```

**方式二：直接启动（使用已有WAR包，无需编译环境）**

```bash
# Linux/Mac
./start_direct.sh

# Windows
start_direct.bat
```

### 停止服务

```bash
# Linux/Mac
./stop.sh

# Windows
stop.bat
```

### 访问系统

启动完成后，访问：http://localhost:8080

## 数据库连接信息

```
主机：localhost:3306
数据库：scrm_zhongxin
用户名：oauser
密码：oapass
```

## API接口

### 获取工单列表
```
GET /api/workorder/
参数：status, priority, keyword
```

### 获取工单详情
```
GET /api/workorder/detail/{id}
```

### 创建工单
```
POST /api/workorder/create
Content-Type: application/json
Body: {title, description, wechatContent, priority, category, creator, creatorDept, handler, handlerDept, remark, ...}
```

### 更新工单
```
POST /api/workorder/update
Content-Type: application/json
Body: {id, status, operator, ...}
```

## 数据库表结构

### work_order（工单表）
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 工单ID（主键）|
| order_no | VARCHAR(50) | 工单编号（唯一）|
| title | VARCHAR(200) | 工单标题 |
| description | TEXT | 工单描述 |
| wechat_content | TEXT | 企微聊天原始内容 |
| priority | ENUM | 优先级（HIGH/MEDIUM/LOW）|
| status | ENUM | 状态（PENDING/PROCESSING/COMPLETED/CLOSED）|
| category | VARCHAR(50) | 工单分类 |
| creator | VARCHAR(100) | 创建人 |
| creator_dept | VARCHAR(100) | 创建人部门 |
| handler | VARCHAR(100) | 处理人 |
| handler_dept | VARCHAR(100) | 处理人部门 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |
| complete_time | DATETIME | 完成时间 |
| remark | TEXT | 备注 |
| conversation_id | VARCHAR(200) | 对话ID |
| account | VARCHAR(100) | 账号 |
| customer_id | VARCHAR(100) | 客户ID |
| customer_name | VARCHAR(100) | 客户名称 |
| source | VARCHAR(100) | 来源 |

### work_order_log（工单操作记录表）
| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 记录ID（主键）|
| order_id | BIGINT | 工单ID |
| action | VARCHAR(50) | 操作类型 |
| operator | VARCHAR(100) | 操作人 |
| action_time | DATETIME | 操作时间 |
| content | TEXT | 操作内容 |

## RPA集成说明

本系统支持RPA自动化：
1. **监听企微聊天**：实时监听企业微信消息
2. **自动识别工单**：识别聊天中的工单请求关键词
3. **自动填写表单**：解析聊天内容，自动提取工单信息
4. **自动提交**：自动提交工单并返回工单编号

## 许可证

MIT License
