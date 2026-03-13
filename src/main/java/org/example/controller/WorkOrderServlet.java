package org.example.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@WebServlet("/api/workorder/*")
public class WorkOrderServlet extends HttpServlet {
    private static final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
    private String dbUrl;
    private String dbUsername;
    private String dbPassword;

    static {
        try {
            // 显式加载MySQL驱动
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("MySQL driver loaded successfully!");
        } catch (ClassNotFoundException e) {
            System.out.println("Failed to load MySQL driver: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void init() throws ServletException {
        ServletContext context = getServletContext();
        dbUrl = context.getInitParameter("db.url");
        dbUsername = context.getInitParameter("db.username");
        dbPassword = context.getInitParameter("db.password");

        System.out.println("=================================================");
        System.out.println("WorkOrderServlet initialized");
        System.out.println("dbUrl: " + dbUrl);
        System.out.println("dbUsername: " + dbUsername);
        System.out.println("=================================================");

        // 测试数据库连接
        try {
            Connection conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
            System.out.println("Database connection successful!");
            conn.close();
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();
        System.out.println("GET request: " + pathInfo);

        try {
            if (pathInfo == null || "/".equals(pathInfo)) {
                // 获取所有工单列表
                listWorkOrders(req, resp);
            } else if (pathInfo.startsWith("/detail/")) {
                // 获取工单详情
                String orderId = pathInfo.substring("/detail/".length());
                getWorkOrderDetail(orderId, resp);
            } else {
                sendError(resp, 404, "Invalid path: " + pathInfo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sendError(resp, 500, "数据库错误: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();
        System.out.println("POST request: " + pathInfo);

        try {
            if ("/create".equals(pathInfo)) {
                // 创建工单
                createWorkOrder(req, resp);
            } else if ("/update".equals(pathInfo)) {
                // 更新工单
                updateWorkOrder(req, resp);
            } else if ("/clearAll".equals(pathInfo)) {
                // 清理所有工单
                clearAllWorkOrders(req, resp);
            } else {
                sendError(resp, 404, "Invalid path: " + pathInfo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sendError(resp, 500, "数据库错误: " + e.getMessage());
        }
    }

    private void listWorkOrders(HttpServletRequest req, HttpServletResponse resp) throws SQLException, IOException {
        String status = req.getParameter("status");
        String priority = req.getParameter("priority");
        String keyword = req.getParameter("keyword");

        List<Map<String, Object>> workOrders = new ArrayList<>();
        String sql = "SELECT * FROM work_order WHERE 1=1";
        List<Object> params = new ArrayList<>();

        if (status != null && !status.isEmpty()) {
            sql += " AND status = ?";
            params.add(status);
        }
        if (priority != null && !priority.isEmpty()) {
            sql += " AND priority = ?";
            params.add(priority);
        }
        if (keyword != null && !keyword.isEmpty()) {
            sql += " AND (title LIKE ? OR description LIKE ? OR order_no LIKE ?)";
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
        }
        sql += " ORDER BY create_time DESC";

        System.out.println("SQL: " + sql);

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> order = new HashMap<>();
                order.put("id", rs.getLong("id"));
                order.put("orderNo", rs.getString("order_no"));
                order.put("title", rs.getString("title"));
                order.put("description", rs.getString("description"));
                order.put("priority", rs.getString("priority"));
                order.put("status", rs.getString("status"));
                order.put("category", rs.getString("category"));
                order.put("creator", rs.getString("creator"));
                order.put("creatorDept", rs.getString("creator_dept"));
                order.put("handler", rs.getString("handler"));
                order.put("handlerDept", rs.getString("handler_dept"));
                order.put("createTime", rs.getTimestamp("create_time"));
                order.put("updateTime", rs.getTimestamp("update_time"));
                // 新字段
                order.put("conversationId", rs.getString("conversation_id"));
                order.put("account", rs.getString("account"));
                order.put("customerId", rs.getString("customer_id"));
                order.put("customerName", rs.getString("customer_name"));
                order.put("source", rs.getString("source"));
                order.put("creatorId", rs.getString("creator_id"));
                order.put("creatorName", rs.getString("creator_name"));
                order.put("handlerId", rs.getString("handler_id"));
                order.put("handlerName", rs.getString("handler_name"));
                workOrders.add(order);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", workOrders);
        sendResponse(resp, result);
    }

    private void getWorkOrderDetail(String orderId, HttpServletResponse resp) throws SQLException, IOException {
        String sql = "SELECT * FROM work_order WHERE id = ?";

        System.out.println("Getting detail for ID: " + orderId);

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, Long.parseLong(orderId));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Map<String, Object> order = new HashMap<>();
                order.put("id", rs.getLong("id"));
                order.put("orderNo", rs.getString("order_no"));
                order.put("title", rs.getString("title"));
                order.put("description", rs.getString("description"));
                order.put("wechatContent", rs.getString("wechat_content"));
                order.put("priority", rs.getString("priority"));
                order.put("status", rs.getString("status"));
                order.put("category", rs.getString("category"));
                order.put("creator", rs.getString("creator"));
                order.put("creatorDept", rs.getString("creator_dept"));
                order.put("handler", rs.getString("handler"));
                order.put("handlerDept", rs.getString("handler_dept"));
                order.put("createTime", rs.getTimestamp("create_time"));
                order.put("updateTime", rs.getTimestamp("update_time"));
                order.put("completeTime", rs.getTimestamp("complete_time"));
                order.put("remark", rs.getString("remark"));
                // 新字段
                order.put("conversationId", rs.getString("conversation_id"));
                order.put("account", rs.getString("account"));
                order.put("customerId", rs.getString("customer_id"));
                order.put("customerName", rs.getString("customer_name"));
                order.put("source", rs.getString("source"));
                order.put("creatorId", rs.getString("creator_id"));
                order.put("creatorName", rs.getString("creator_name"));
                order.put("handlerId", rs.getString("handler_id"));
                order.put("handlerName", rs.getString("handler_name"));

                // 获取操作日志
                List<Map<String, Object>> logs = getWorkOrderLogs(conn, Long.parseLong(orderId));
                order.put("logs", logs);

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("data", order);
                sendResponse(resp, result);
            } else {
                sendError(resp, 404, "工单不存在");
            }
        }
    }

    private List<Map<String, Object>> getWorkOrderLogs(Connection conn, Long orderId) throws SQLException {
        List<Map<String, Object>> logs = new ArrayList<>();
        String sql = "SELECT * FROM work_order_log WHERE order_id = ? ORDER BY action_time ASC";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, orderId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> log = new HashMap<>();
                log.put("id", rs.getLong("id"));
                log.put("action", rs.getString("action"));
                log.put("operator", rs.getString("operator"));
                log.put("actionTime", rs.getTimestamp("action_time"));
                log.put("content", rs.getString("content"));
                logs.add(log);
            }
        }

        return logs;
    }

    private void createWorkOrder(HttpServletRequest req, HttpServletResponse resp) throws SQLException, IOException {
        StringBuilder json = new StringBuilder();
        String line;
        BufferedReader reader = req.getReader();
        while ((line = reader.readLine()) != null) {
            json.append(line);
        }

        String jsonStr = json.toString();
        System.out.println("Received JSON: " + jsonStr);

        Map<String, Object> params = gson.fromJson(jsonStr, Map.class);

        // 生成工单编号
        String orderNo = generateOrderNo();

        // 包含新表结构字段的SQL
        String sql = "INSERT INTO work_order (order_no, title, description, wechat_content, priority, status, category, " +
                     "creator, creator_dept, handler, handler_dept, remark, " +
                     "conversation_id, account, customer_id, customer_name, source, " +
                     "creator_id, creator_name, handler_id, handler_name) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        System.out.println("Creating work order: " + orderNo);

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            int idx = 1;
            stmt.setString(idx++, orderNo);
            stmt.setString(idx++, (String) params.get("title"));
            stmt.setString(idx++, (String) params.get("description"));
            stmt.setString(idx++, (String) params.get("wechatContent"));
            stmt.setString(idx++, (String) params.get("priority"));
            stmt.setString(idx++, "PENDING");
            stmt.setString(idx++, (String) params.get("category"));
            stmt.setString(idx++, (String) params.get("creator"));
            stmt.setString(idx++, (String) params.get("creatorDept"));
            stmt.setString(idx++, (String) params.get("handler"));
            stmt.setString(idx++, (String) params.get("handlerDept"));
            stmt.setString(idx++, (String) params.get("remark"));
            // 新字段
            stmt.setString(idx++, (String) params.get("conversationId"));
            stmt.setString(idx++, (String) params.get("account"));
            stmt.setString(idx++, (String) params.get("customerId"));
            stmt.setString(idx++, (String) params.get("customerName"));
            stmt.setString(idx++, (String) params.get("source"));
            stmt.setString(idx++, (String) params.get("creatorId"));
            stmt.setString(idx++, (String) params.get("creatorName"));
            stmt.setString(idx++, (String) params.get("handlerId"));
            stmt.setString(idx++, (String) params.get("handlerName"));

            int affectedRows = stmt.executeUpdate();
            System.out.println("Affected rows: " + affectedRows);

            if (affectedRows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    Long orderId = rs.getLong(1);
                    // 插入创建日志
                    String creator = (String) (params.get("creatorName") != null ? params.get("creatorName") : params.get("creator"));
                    insertLog(conn, orderId, "CREATE", creator, "创建工单");

                    Map<String, Object> result = new HashMap<>();
                    result.put("success", true);

                    Map<String, Object> data = new HashMap<>();
                    data.put("id", orderId);
                    data.put("orderNo", orderNo);
                    result.put("data", data);

                    System.out.println("Work order created successfully: " + orderId);
                    sendResponse(resp, result);
                }
            } else {
                sendError(resp, 500, "创建工单失败");
            }
        }
    }

    private void updateWorkOrder(HttpServletRequest req, HttpServletResponse resp) throws SQLException, IOException {
        StringBuilder json = new StringBuilder();
        String line;
        BufferedReader reader = req.getReader();
        while ((line = reader.readLine()) != null) {
            json.append(line);
        }

        String jsonStr = json.toString();
        System.out.println("Received JSON: " + jsonStr);

        Map<String, Object> params = gson.fromJson(jsonStr, Map.class);
        Long orderId = Long.parseLong(params.get("id").toString());
        String status = (String) params.get("status");
        String operator = (String) params.get("operator");

        String sql = "UPDATE work_order SET ";
        List<String> updates = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        if (params.get("title") != null) {
            updates.add("title = ?");
            values.add(params.get("title"));
        }
        if (params.get("description") != null) {
            updates.add("description = ?");
            values.add(params.get("description"));
        }
        if (params.get("priority") != null) {
            updates.add("priority = ?");
            values.add(params.get("priority"));
        }
        if (status != null) {
            updates.add("status = ?");
            values.add(status);
            if ("COMPLETED".equals(status)) {
                updates.add("complete_time = NOW()");
            }
        }
        if (params.get("handler") != null) {
            updates.add("handler = ?");
            values.add(params.get("handler"));
        }
        if (params.get("remark") != null) {
            updates.add("remark = ?");
            values.add(params.get("remark"));
        }

        sql += String.join(", ", updates) + " WHERE id = ?";
        values.add(orderId);

        System.out.println("Updating work order: " + orderId);

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < values.size(); i++) {
                stmt.setObject(i + 1, values.get(i));
            }

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                // 插入操作日志
                if (status != null) {
                    String action = "PROCESSING".equals(status) ? "PROCESS" :
                                   "COMPLETED".equals(status) ? "COMPLETE" :
                                   "CLOSED".equals(status) ? "CLOSE" : "UPDATE";
                    insertLog(conn, orderId, action, operator, "更新状态为: " + status);
                }

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", "更新成功");
                sendResponse(resp, result);
            } else {
                sendError(resp, 500, "更新工单失败");
            }
        }
    }

    private void insertLog(Connection conn, Long orderId, String action, String operator, String content) throws SQLException {
        String sql = "INSERT INTO work_order_log (order_id, action, operator, content) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, orderId);
            stmt.setString(2, action);
            stmt.setString(3, operator);
            stmt.setString(4, content);
            stmt.executeUpdate();
        }
    }

    private void clearAllWorkOrders(HttpServletRequest req, HttpServletResponse resp) throws SQLException, IOException {
        System.out.println("Clearing all work orders...");

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // 先删除日志表数据（外键关联）
            String deleteLogsSql = "DELETE FROM work_order_log";
            int logsDeleted = stmt.executeUpdate(deleteLogsSql);
            System.out.println("Deleted logs: " + logsDeleted);

            // 再删除工单表数据
            String deleteOrdersSql = "DELETE FROM work_order";
            int ordersDeleted = stmt.executeUpdate(deleteOrdersSql);
            System.out.println("Deleted work orders: " + ordersDeleted);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "已清理 " + ordersDeleted + " 个工单");
            Map<String, Object> data = new HashMap<>();
            data.put("ordersDeleted", ordersDeleted);
            data.put("logsDeleted", logsDeleted);
            result.put("data", data);
            sendResponse(resp, result);

            System.out.println("All work orders cleared successfully");
        }
    }

    private String generateOrderNo() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String dateStr = sdf.format(new Date());
        Random random = new Random();
        return "WO" + dateStr + String.format("%04d", random.nextInt(10000));
    }

    private void sendResponse(HttpServletResponse resp, Object data) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        String json = gson.toJson(data);
        System.out.println("Response: " + json);
        PrintWriter out = resp.getWriter();
        out.print(json);
        out.flush();
    }

    private void sendError(HttpServletResponse resp, int status, String message) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", message);
        String json = gson.toJson(result);
        System.out.println("Error Response: " + json);
        PrintWriter out = resp.getWriter();
        out.print(json);
        out.flush();
    }
}
