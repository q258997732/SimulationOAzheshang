// 工单API封装
const WorkOrderAPI = {
    // 获取工单列表
    async list(params = {}) {
        const query = new URLSearchParams(params).toString();
        const response = await fetch(`/api/workorder/?${query}`);
        return response.json();
    },

    // 获取工单详情
    async detail(id) {
        const response = await fetch(`/api/workorder/detail/${id}`);
        return response.json();
    },

    // 创建工单
    async create(data) {
        const response = await fetch('/api/workorder/create', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });
        return response.json();
    },

    // 更新工单
    async update(data) {
        const response = await fetch('/api/workorder/update', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });
        return response.json();
    },

    // 清理所有工单
    async clearAll() {
        const response = await fetch('/api/workorder/clearAll', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        return response.json();
    }
};

// 工具函数
const Utils = {
    // 显示提示信息
    showMessage(message, type = 'success') {
        const alertDiv = document.createElement('div');
        alertDiv.className = `alert alert-${type}`;
        alertDiv.textContent = message;
        document.body.insertBefore(alertDiv, document.body.firstChild);

        setTimeout(() => {
            alertDiv.remove();
        }, 3000);
    },

    // 格式化日期
    formatDate(dateStr) {
        if (!dateStr) return '-';
        const date = new Date(dateStr);
        return date.toLocaleString('zh-CN');
    },

    // 获取状态文本和样式
    getStatusInfo(status) {
        const statusMap = {
            'PENDING': { text: '待处理', class: 'status-pending' },
            'PROCESSING': { text: '处理中', class: 'status-processing' },
            'COMPLETED': { text: '已完成', class: 'status-completed' },
            'CLOSED': { text: '已关闭', class: 'status-closed' }
        };
        return statusMap[status] || { text: status, class: '' };
    },

    // 获取优先级文本和样式
    getPriorityInfo(priority) {
        const priorityMap = {
            'HIGH': { text: '高', class: 'priority-high' },
            'MEDIUM': { text: '中', class: 'priority-medium' },
            'LOW': { text: '低', class: 'priority-low' }
        };
        return priorityMap[priority] || { text: priority, class: '' };
    },

    // 获取操作类型文本
    getActionText(action) {
        const actionMap = {
            'CREATE': '创建',
            'ASSIGN': '指派',
            'PROCESS': '处理',
            'COMPLETE': '完成',
            'CLOSE': '关闭',
            'UPDATE': '更新'
        };
        return actionMap[action] || action;
    }
};

// 工单列表页面
class WorkOrderList {
    constructor() {
        this.init();
    }

    init() {
        this.renderFilter();
        this.loadWorkOrders();
    }

    renderFilter() {
        const container = document.getElementById('filter-container');
        container.innerHTML = `
            <div class="filter-bar">
                <select id="filter-status" onchange="workOrderList.loadWorkOrders()">
                    <option value="">所有状态</option>
                    <option value="PENDING">待处理</option>
                    <option value="PROCESSING">处理中</option>
                    <option value="COMPLETED">已完成</option>
                    <option value="CLOSED">已关闭</option>
                </select>
                <select id="filter-priority" onchange="workOrderList.loadWorkOrders()">
                    <option value="">所有优先级</option>
                    <option value="HIGH">高</option>
                    <option value="MEDIUM">中</option>
                    <option value="LOW">低</option>
                </select>
                <input type="text" id="filter-keyword" placeholder="搜索工单..." onkeyup="if(event.keyCode===13) workOrderList.loadWorkOrders()">
                <button class="btn btn-primary btn-sm" onclick="workOrderList.loadWorkOrders()">搜索</button>
                <button class="btn btn-success btn-sm" onclick="window.location.href='create.html'">新建工单</button>
                <button class="btn btn-danger btn-sm" onclick="workOrderList.clearAll()">清理所有工单</button>
            </div>
        `;
    }

    async loadWorkOrders() {
        const status = document.getElementById('filter-status')?.value || '';
        const priority = document.getElementById('filter-priority')?.value || '';
        const keyword = document.getElementById('filter-keyword')?.value || '';

        const tbody = document.getElementById('workorder-list');
        tbody.innerHTML = '<tr><td colspan="10" class="loading">加载中...</td></tr>';

        try {
            const result = await WorkOrderAPI.list({ status, priority, keyword });
            if (result.success) {
                this.renderTable(result.data);
            } else {
                tbody.innerHTML = `<tr><td colspan="10" class="loading">${result.message}</td></tr>`;
            }
        } catch (error) {
            tbody.innerHTML = `<tr><td colspan="10" class="loading">加载失败: ${error.message}</td></tr>`;
        }
    }

    renderTable(workOrders) {
        const tbody = document.getElementById('workorder-list');
        if (!workOrders || workOrders.length === 0) {
            tbody.innerHTML = '<tr><td colspan="10" class="loading">暂无数据</td></tr>';
            return;
        }

        tbody.innerHTML = workOrders.map(order => {
            const statusInfo = Utils.getStatusInfo(order.status);
            const priorityInfo = Utils.getPriorityInfo(order.priority);
            return `
                <tr>
                    <td>${order.orderNo}</td>
                    <td>${order.title}</td>
                    <td>${order.category || '-'}</td>
                    <td><span class="status-tag ${priorityInfo.class}">${priorityInfo.text}</span></td>
                    <td><span class="status-tag ${statusInfo.class}">${statusInfo.text}</span></td>
                    <td>${order.creator || '-'}</td>
                    <td>${order.handler || '-'}</td>
                    <td>${Utils.formatDate(order.createTime)}</td>
                    <td>
                        <button class="btn btn-primary btn-sm" onclick="workOrderList.viewDetail(${order.id})">详情</button>
                    </td>
                </tr>
            `;
        }).join('');
    }

    viewDetail(id) {
        window.location.href = `detail.html?id=${id}`;
    }

    async clearAll() {
        if (!confirm('警告：此操作将删除所有工单数据，且无法恢复！\n\n确定要继续吗？')) {
            return;
        }

        if (!confirm('再次确认：是否确实要删除所有工单？')) {
            return;
        }

        try {
            const result = await WorkOrderAPI.clearAll();
            if (result.success) {
                Utils.showMessage('所有工单已清理完成');
                this.loadWorkOrders();
            } else {
                Utils.showMessage(result.message || '清理失败', 'error');
            }
        } catch (error) {
            Utils.showMessage('清理失败: ' + error.message, 'error');
        }
    }
}

// 工单创建页面
class WorkOrderCreate {
    constructor() {
        this.init();
    }

    init() {
        this.bindEvents();
    }

    bindEvents() {
        document.getElementById('submit-btn').addEventListener('click', () => this.submit());
        document.getElementById('cancel-btn').addEventListener('click', () => {
            window.location.href = 'index.html';
        });
    }

    async submit() {
        const form = document.getElementById('workorder-form');
        const formData = {
            title: document.getElementById('title').value.trim(),
            description: document.getElementById('description').value.trim(),
            wechatContent: document.getElementById('wechat-content').value.trim(),
            priority: document.getElementById('priority').value,
            category: document.getElementById('category').value.trim(),
            creator: document.getElementById('creator').value.trim(),
            creatorDept: document.getElementById('creator-dept').value.trim(),
            handler: document.getElementById('handler').value.trim(),
            handlerDept: document.getElementById('handler-dept').value.trim(),
            remark: document.getElementById('remark').value.trim()
        };

        // 简单验证
        if (!formData.title) {
            Utils.showMessage('请输入工单标题', 'error');
            return;
        }
        if (!formData.description) {
            Utils.showMessage('请输入工单描述', 'error');
            return;
        }
        if (!formData.creator) {
            Utils.showMessage('请输入创建人', 'error');
            return;
        }

        try {
            const result = await WorkOrderAPI.create(formData);
            if (result.success) {
                Utils.showMessage('工单创建成功');
                setTimeout(() => {
                    window.location.href = 'index.html';
                }, 1000);
            } else {
                Utils.showMessage(result.message || '创建失败', 'error');
            }
        } catch (error) {
            Utils.showMessage('创建失败: ' + error.message, 'error');
        }
    }
}

// 工单详情页面
class WorkOrderDetail {
    constructor() {
        this.workOrderId = new URLSearchParams(window.location.search).get('id');
        this.init();
    }

    async init() {
        if (!this.workOrderId) {
            Utils.showMessage('参数错误', 'error');
            setTimeout(() => {
                window.location.href = 'index.html';
            }, 1000);
            return;
        }

        await this.loadDetail();
        this.bindEvents();
    }

    async loadDetail() {
        try {
            const result = await WorkOrderAPI.detail(this.workOrderId);
            if (result.success) {
                this.renderDetail(result.data);
            } else {
                Utils.showMessage(result.message || '加载失败', 'error');
            }
        } catch (error) {
            Utils.showMessage('加载失败: ' + error.message, 'error');
        }
    }

    renderDetail(data) {
        const statusInfo = Utils.getStatusInfo(data.status);
        const priorityInfo = Utils.getPriorityInfo(data.priority);

        document.getElementById('detail-order-no').textContent = data.orderNo;
        document.getElementById('detail-title').textContent = data.title;
        document.getElementById('detail-status').innerHTML = `<span class="status-tag ${statusInfo.class}">${statusInfo.text}</span>`;
        document.getElementById('detail-priority').innerHTML = `<span class="status-tag ${priorityInfo.class}">${priorityInfo.text}</span>`;
        document.getElementById('detail-category').textContent = data.category || '-';
        document.getElementById('detail-creator').textContent = data.creator || '-';
        document.getElementById('detail-creator-dept').textContent = data.creatorDept || '-';
        document.getElementById('detail-handler').textContent = data.handler || '-';
        document.getElementById('detail-handler-dept').textContent = data.handlerDept || '-';
        document.getElementById('detail-create-time').textContent = Utils.formatDate(data.createTime);
        document.getElementById('detail-update-time').textContent = Utils.formatDate(data.updateTime);

        document.getElementById('detail-description').textContent = data.description || '无描述';

        if (data.wechatContent) {
            document.getElementById('wechat-content-container').innerHTML = `
                <div class="wechat-content">
                    <h4>企微聊天原始内容</h4>
                    <p>${data.wechatContent}</p>
                </div>
            `;
        }

        if (data.remark) {
            document.getElementById('detail-remark').textContent = data.remark;
            document.getElementById('detail-remark-container').style.display = 'block';
        }

        // 渲染操作日志
        if (data.logs && data.logs.length > 0) {
            const logsHtml = data.logs.map(log => `
                <div class="log-item">
                    <div class="log-time">${Utils.formatDate(log.actionTime)}</div>
                    <div class="log-content">
                        <div>
                            <span class="log-action">${Utils.getActionText(log.action)}</span>
                            <span class="log-operator">${log.operator || '系统'}</span>
                        </div>
                        <div class="log-detail">${log.content || ''}</div>
                    </div>
                </div>
            `).join('');
            document.getElementById('log-list').innerHTML = logsHtml;
        } else {
            document.getElementById('log-list').innerHTML = '<div class="loading">暂无操作记录</div>';
        }

        // 根据状态显示/隐藏操作按钮
        const actionButtons = document.getElementById('action-buttons');
        if (data.status === 'PENDING') {
            actionButtons.innerHTML = `
                <button class="btn btn-warning" onclick="workOrderDetail.updateStatus('PROCESSING')">开始处理</button>
                <button class="btn btn-success" onclick="workOrderDetail.updateStatus('COMPLETED')">完成工单</button>
            `;
        } else if (data.status === 'PROCESSING') {
            actionButtons.innerHTML = `
                <button class="btn btn-success" onclick="workOrderDetail.updateStatus('COMPLETED')">完成工单</button>
                <button class="btn btn-danger" onclick="workOrderDetail.updateStatus('CLOSED')">关闭工单</button>
            `;
        } else if (data.status === 'COMPLETED') {
            actionButtons.innerHTML = `
                <button class="btn btn-danger" onclick="workOrderDetail.updateStatus('CLOSED')">关闭工单</button>
            `;
        } else {
            actionButtons.innerHTML = '';
        }
    }

    bindEvents() {
        document.getElementById('back-btn').addEventListener('click', () => {
            window.location.href = 'index.html';
        });
    }

    async updateStatus(status) {
        const operator = prompt('请输入操作人姓名:', '系统管理员');
        if (!operator) return;

        try {
            const result = await WorkOrderAPI.update({
                id: this.workOrderId,
                status: status,
                operator: operator
            });

            if (result.success) {
                Utils.showMessage('操作成功');
                await this.loadDetail();
            } else {
                Utils.showMessage(result.message || '操作失败', 'error');
            }
        } catch (error) {
            Utils.showMessage('操作失败: ' + error.message, 'error');
        }
    }
}

// 页面初始化
let workOrderList, workOrderCreate, workOrderDetail;

document.addEventListener('DOMContentLoaded', () => {
    const path = window.location.pathname;

    if (path.includes('create.html')) {
        workOrderCreate = new WorkOrderCreate();
    } else if (path.includes('detail.html')) {
        workOrderDetail = new WorkOrderDetail();
    } else {
        workOrderList = new WorkOrderList();
    }
});
