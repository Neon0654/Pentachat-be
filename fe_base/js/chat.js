// WebSocket Chat JavaScript
// ===================================

// --- Constants ---
const API_BASE_URL = 'http://localhost:8080';
const WS_URL = 'http://localhost:8080/ws';

// --- Global State ---
let stompClient = null;
let currentUsername = null;
let userId = null;
let sessionId = null;
let isConnected = false;

// --- Main Initialization ---
window.addEventListener('DOMContentLoaded', () => {
    // 1. Authenticate and Setup Basic Info
    sessionId = localStorage.getItem('sessionId');
    currentUsername = localStorage.getItem('username');
    userId = localStorage.getItem('userId');

    if (!sessionId || !currentUsername || !userId) {
        window.location.href = 'index.html';
        return;
    }

    // 2. Initialize Core Chat Page Elements
    const messageForm = document.getElementById('messageForm');
    const logoutBtn = document.getElementById('logoutBtn');
    document.getElementById('currentUsername').textContent = currentUsername;

    // 3. Connect WebSocket
    connectWebSocket();

    // 4. Setup Event Listeners for Core Chat
    messageForm.addEventListener('submit', sendMessage);
    logoutBtn.addEventListener('click', handleLogout);

    // 5. Initialize Friends Management Module (if on chat page)
    if (document.getElementById('friendsBtn')) {
        initializeFriendsManagement();
    }
});

// ===================================
// CORE CHAT FUNCTIONS
// ===================================

function connectWebSocket() {
    console.log('🔌 Connecting to WebSocket...');
    updateConnectionStatus('connecting');
    try {
        const socket = new SockJS(WS_URL);
        stompClient = Stomp.over(socket);
        stompClient.debug = null; // Disable excessive logging
        stompClient.connect({}, onConnected, onError);
    } catch (error) {
        console.error('❌ WebSocket connection error:', error);
        updateConnectionStatus('disconnected');
    }
}

function onConnected() {
    console.log('✅ WebSocket connected!');
    isConnected = true;
    updateConnectionStatus('connected');
    document.getElementById('sendBtn').disabled = false;
    stompClient.subscribe('/topic/messages', onMessageReceived);
}

function onError(error) {
    console.error('❌ WebSocket error:', error);
    isConnected = false;
    updateConnectionStatus('disconnected');
    document.getElementById('sendBtn').disabled = true;
    setTimeout(connectWebSocket, 5000); // Attempt to reconnect
}

function onMessageReceived(payload) {
    try {
        const message = JSON.parse(payload.body);
        console.log('📨 Message received:', message);
        displayMessage(message);
    } catch (error) {
        console.error('❌ Error parsing message:', error);
    }
}

function sendMessage(event) {
    event.preventDefault();
    const messageInput = document.getElementById('messageInput');
    const recipientInput = document.getElementById('recipientInput');
    const content = messageInput.value.trim();
    const recipient = recipientInput.value.trim();

    if (content && isConnected) {
        const message = {
            from: currentUsername,
            to: recipient || '',
            content: content,
            timestamp: new Date().toISOString()
        };
        stompClient.send('/app/chat.send', {}, JSON.stringify(message));
        messageInput.value = '';
        messageInput.focus();
    } else if (!isConnected) {
        alert('⚠️ Chưa kết nối đến server. Vui lòng đợi...');
    }
}

function displayMessage(message) {
    const messagesContainer = document.getElementById('messagesContainer');
    const welcomeMsg = messagesContainer.querySelector('.welcome-message');
    if (welcomeMsg) welcomeMsg.remove();

    const messageDiv = document.createElement('div');
    messageDiv.className = 'message';
    if (message.from === currentUsername) messageDiv.classList.add('own');

    const timestamp = message.timestamp ? formatTimestamp(message.timestamp) : 'Vừa xong';
    let recipientHTML = message.to ? `<span class="message-recipient">→ ${escapeHtml(message.to)}</span>` : '';

    messageDiv.innerHTML = `
        <div class="message-header">
            <span class="message-sender">${escapeHtml(message.from)}</span>
            <span class="message-time">${timestamp}</span>
            ${recipientHTML}
        </div>
        <div class="message-bubble">${escapeHtml(message.content)}</div>
    `;
    messagesContainer.appendChild(messageDiv);
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
}

function handleLogout() {
    if (confirm('Bạn có chắc muốn đăng xuất?')) {
        if (stompClient && isConnected) {
            stompClient.disconnect(() => console.log('👋 Disconnected from WebSocket'));
        }
        localStorage.removeItem('sessionId');
        localStorage.removeItem('username');
        localStorage.removeItem('userId');
        window.location.href = 'index.html';
    }
}

// ===================================
// FRIENDS MANAGEMENT MODULE
// ===================================

function initializeFriendsManagement() {
    // --- DOM Elements ---
    const friendsModal = document.getElementById('friendsModal');
    const friendsBtn = document.getElementById('friendsBtn');
    const closeFriendsModalBtn = document.getElementById('closeFriendsModalBtn');
    const allUsersList = document.getElementById('allUsersList');
    const incomingRequestsList = document.getElementById('incomingRequestsList');
    const friendsList = document.getElementById('friendsList');

    // --- API Helper ---
    const api = {
        _fetch: async (endpoint, options = {}) => {
            const mergedOptions = {
                ...options,
                headers: {
                    'Content-Type': 'application/json',
                    'X-User-Id': userId,
                    'X-Session-Id': sessionId,
                    ...options.headers
                }
            };
            try {
                const response = await fetch(`${API_BASE_URL}${endpoint}`, mergedOptions);
                const responseData = await response.json();
                if (!response.ok || !responseData.success) throw new Error(responseData.message || 'API request failed');
                return responseData.data;
            } catch (error) {
                console.error(`API Error on ${endpoint}:`, error);
                alert(`Lỗi: ${error.message}`);
                return null;
            }
        },
        getAllUsers: () => api._fetch('/api/users'),
        getFriends: () => api._fetch(`/api/friendships/${userId}`),
        getIncomingRequests: () => api._fetch(`/api/friendships/requests/incoming/${userId}`),
        sendFriendRequest: (recipientId) => api._fetch('/api/friendships/request', { method: 'POST', body: JSON.stringify({ recipientId }) }),
        acceptFriendRequest: (requesterId) => api._fetch('/api/friendships/accept', { method: 'POST', body: JSON.stringify({ requesterId }) }),
        rejectFriendRequest: (requesterId) => api._fetch('/api/friendships/reject', { method: 'POST', body: JSON.stringify({ requesterId }) }),
        deleteFriend: (friendId) => api._fetch('/api/friendships/delete', { method: 'DELETE', body: JSON.stringify({ friendId }) })
    };

    // --- Render Functions ---
    const renderList = (element, items, renderItem, emptyMsg = "Không có dữ liệu") => {
        element.innerHTML = (!items || items.length === 0) ? `<p class="empty-list-msg">${emptyMsg}</p>` : '';
        if (items) items.forEach(item => element.appendChild(renderItem(item)));
    };
    const createUserItem = (user) => {
        const item = document.createElement('div');
        item.className = 'user-list-item';
        item.innerHTML = `<span class="username">${escapeHtml(user.username)}</span><div class="actions"><button class="btn-action btn-add" data-user-id="${user.id}">Thêm bạn</button></div>`;
        return item;
    };
    const createRequestItem = (request) => {
        const item = document.createElement('div');
        item.className = 'user-list-item';
        item.innerHTML = `<span class="username">${escapeHtml(request.username)}</span><div class="actions"><button class="btn-action btn-accept" data-user-id="${request.userId}">Chấp nhận</button><button class="btn-action btn-reject" data-user-id="${request.userId}">Từ chối</button></div>`;
        return item;
    };
    const createFriendItem = (friend) => {
        const item = document.createElement('div');
        item.className = 'user-list-item';
        item.innerHTML = `<span class="username">${escapeHtml(friend.username)}</span><div class="actions"><button class="btn-action btn-unfriend" data-user-id="${friend.userId}">Hủy bạn</button></div>`;
        return item;
    };

    // --- Data Loading ---
    const loadAllFriendData = async () => {
        const [users, requests, friends] = await Promise.all([api.getAllUsers(), api.getIncomingRequests(), api.getFriends()]);
        const friendIds = new Set((friends || []).map(f => f.userId));
        const requestIds = new Set((requests || []).map(r => r.userId));
        const filteredUsers = (users || []).filter(u => !friendIds.has(u.id) && !requestIds.has(u.id));
        renderList(allUsersList, filteredUsers, createUserItem, "Không tìm thấy người dùng nào.");
        renderList(incomingRequestsList, requests, createRequestItem, "Không có lời mời nào.");
        renderList(friendsList, friends, createFriendItem, "Chưa có bạn bè.");
    };

    // --- Event Listeners ---
    friendsBtn.addEventListener('click', () => {
        friendsModal.style.display = 'block';
        loadAllFriendData();
    });
    closeFriendsModalBtn.addEventListener('click', () => friendsModal.style.display = 'none');
    window.addEventListener('click', (event) => {
        if (event.target == friendsModal) friendsModal.style.display = 'none';
    });
    friendsModal.addEventListener('click', async (event) => {
        const target = event.target;
        const targetUserId = target.getAttribute('data-user-id');
        if (!targetUserId) return;
        let actionCompleted = false;
        if (target.classList.contains('btn-add')) {
            await api.sendFriendRequest(targetUserId);
            actionCompleted = true;
        } else if (target.classList.contains('btn-accept')) {
            await api.acceptFriendRequest(targetUserId);
            actionCompleted = true;
        } else if (target.classList.contains('btn-reject')) {
            await api.rejectFriendRequest(targetUserId);
            actionCompleted = true;
        } else if (target.classList.contains('btn-unfriend') && confirm(`Bạn có chắc muốn hủy kết bạn?`)) {
            await api.deleteFriend(targetUserId);
            actionCompleted = true;
        }
        if (actionCompleted) loadAllFriendData(); // Refresh list only after an action
    });
}


// ===================================
// UTILITY FUNCTIONS
// ===================================

function updateConnectionStatus(status) {
    const el = document.getElementById('connectionStatus');
    if (!el) return;
    el.className = 'status-badge';
    switch (status) {
        case 'connecting':
            el.classList.add('status-connecting');
            el.textContent = 'Đang kết nối...';
            break;
        case 'connected':
            el.classList.add('status-connected');
            el.textContent = 'Đã kết nối';
            break;
        case 'disconnected':
            el.classList.add('status-disconnected');
            el.textContent = 'Mất kết nối';
            break;
    }
}

function formatTimestamp(timestamp) {
    try {
        const date = new Date(timestamp);
        const now = new Date();
        if ((now - date) < 60000) return 'Vừa xong';
        if ((now - date) < 3600000) return `${Math.floor((now - date) / 60000)} phút trước`;
        if (date.toDateString() === now.toDateString()) return date.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
        return date.toLocaleString('vi-VN', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
    } catch (error) {
        return 'Không rõ';
    }
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

window.addEventListener('beforeunload', () => {
    if (stompClient && isConnected) {
        stompClient.disconnect();
    }
});
