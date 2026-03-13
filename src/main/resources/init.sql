-- 数据库: scrm_zhongxin
-- 工单表
CREATE TABLE IF NOT EXISTS work_order (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '工单ID',
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '工单编号',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '工单标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '工单描述',
  `wechat_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '企微聊天原始内容',
  `priority` enum('HIGH','MEDIUM','LOW') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'MEDIUM' COMMENT '优先级：HIGH-高，MEDIUM-中，LOW-低',
  `status` enum('PENDING','PROCESSING','COMPLETED','CLOSED') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'PENDING' COMMENT '状态：PENDING-待处理，PROCESSING-处理中，COMPLETED-已完成，CLOSED-已关闭',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '工单分类',
  `creator` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
  `creator_dept` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人部门',
  `handler` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '处理人',
  `handler_dept` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '处理人部门',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `complete_time` datetime DEFAULT NULL COMMENT '完成时间',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '备注',
  `conversation_id` varchar(200) DEFAULT NULL COMMENT '对话ID',
  `account` varchar(100) DEFAULT NULL COMMENT '账号',
  `customer_id` varchar(100) DEFAULT NULL,
  `customer_name` varchar(100) DEFAULT NULL,
  `third_party_url` varchar(100) DEFAULT NULL,
  `third_party_order_id` varchar(100) DEFAULT NULL,
  `creator_id` varchar(100) DEFAULT NULL,
  `creator_name` varchar(100) DEFAULT NULL,
  `handler_id` varchar(100) DEFAULT NULL,
  `handler_name` varchar(100) DEFAULT NULL,
  `finish_time` varchar(100) DEFAULT NULL,
  `source` varchar(100) DEFAULT NULL,
  `who_created` varchar(100) DEFAULT NULL,
  `when_created` datetime DEFAULT NULL,
  `who_modified` varchar(100) DEFAULT NULL,
  `when_modified` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='OA工单表';

-- 工单操作记录表
CREATE TABLE IF NOT EXISTS work_order_log (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `order_id` bigint NOT NULL COMMENT '工单ID',
  `action` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作类型：CREATE-创建，ASSIGN-指派，PROCESS-处理，COMPLETE-完成，CLOSE-关闭',
  `operator` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '操作人',
  `action_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '操作内容',
  `operator_id` varchar(100) DEFAULT NULL,
  `operator_name` varchar(100) DEFAULT NULL,
  `remark` varchar(200) DEFAULT NULL,
  `who_created` varchar(100) DEFAULT NULL,
  `when_created` datetime DEFAULT NULL,
  `who_modified` varchar(100) DEFAULT NULL,
  `when_modified` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `order_id` (`order_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='工单操作记录表';

-- 插入测试数据（包含新字段）
INSERT INTO work_order (order_no, title, description, priority, status, category, creator, creator_dept, handler, handler_dept, conversation_id, account, customer_id, customer_name, source) VALUES
('WO202403060001', '办公室网络无法连接', '三楼办公区网络中断，需要紧急处理', 'HIGH', 'PENDING', 'IT支持', '张三', '技术部', NULL, NULL, 'CONV001', 'ACC001', 'CUST001', '客户A', '企微'),
('WO202403060002', '申请开通ERP权限', '新员工入职需要开通ERP系统访问权限', 'MEDIUM', 'PROCESSING', '权限申请', '李四', '人事部', '王五', 'IT部', 'CONV002', 'ACC002', 'CUST002', '客户B', '企微'),
('WO202403060003', '会议室投影仪故障', '大会议室投影仪无法正常显示', 'LOW', 'COMPLETED', '设备维修', '赵六', '行政部', '钱七', '后勤部', 'CONV003', 'ACC003', 'CUST003', '客户C', '电话'),
('WO202403060004', '邮箱容量不足', '邮箱提示容量已满，需要扩容', 'MEDIUM', 'CLOSED', 'IT支持', '孙八', '财务部', '王五', 'IT部', 'CONV004', 'ACC004', 'CUST004', '客户D', '邮件');

-- 插入测试日志数据
INSERT INTO work_order_log (order_id, action, operator, content) VALUES
(1, 'CREATE', '张三', '创建工单'),
(2, 'CREATE', '李四', '创建工单'),
(2, 'ASSIGN', '王五', '指派给王五处理'),
(2, 'PROCESS', '王五', '开始处理'),
(3, 'CREATE', '赵六', '创建工单'),
(3, 'ASSIGN', '钱七', '指派给钱七处理'),
(3, 'COMPLETE', '钱七', '工单已完成'),
(4, 'CREATE', '孙八', '创建工单'),
(4, 'ASSIGN', '王五', '指派给王五处理'),
(4, 'COMPLETE', '王五', '权限已开通'),
(4, 'CLOSE', '孙八', '工单已关闭');
