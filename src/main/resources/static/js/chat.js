// WebSocket Chat & Friends Search JavaScript
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
let searchTimeout = null;

// --- Main Initialization ---
window.addEventListener('DOMContentLoaded', () => {
    // 1. Lấy thông tin xác thực
    sessionId = localStorage.getItem('sessionId');
    currentUsername = localStorage.getItem('username');
    userId = localStorage.getItem('userId');

    if (!sessionId || !currentUsername || !userId) {
        window.location.href = 'index.html';
        return;
    }

    // 2. Hiển thị UI cơ bản
    const messageForm = document.getElementById('messageForm');
    const logoutBtn = document.getElementById('logoutBtn');
    document.getElementById('currentUsername').textContent = currentUsername;

    // 3. Kết nối WebSocket
    connectWebSocket();

    // 4. Lắng nghe sự kiện Chat
    messageForm.addEventListener('submit', sendMessage);
    logoutBtn.addEventListener('click', handleLogout);

    // 5. Khởi tạo các Module
    if (document.getElementById('friendsBtn')) {
        initializeFriendsManagement();
    }
    
    if (document.getElementById('createGroupBtn')) {
        initializeGroupManagement();
    }
});

// ===================================
// CORE CHAT FUNCTIONS (WebSocket)
// ===================================

function connectWebSocket() {
    updateConnectionStatus('connecting');
    try {
        const socket = new SockJS(WS_URL);
        stompClient = Stomp.over(socket);
        stompClient.debug = null; 
        stompClient.connect({}, onConnected, onError);
    } catch (error) {
        console.error('❌ WebSocket error:', error);
        updateConnectionStatus('disconnected');
    }
}

function onConnected() {
    isConnected = true;
    updateConnectionStatus('connected');
    document.getElementById('sendBtn').disabled = false;
    stompClient.subscribe('/topic/messages', onMessageReceived);
}

function onError(error) {
    isConnected = false;
    updateConnectionStatus('disconnected');
    document.getElementById('sendBtn').disabled = true;
    setTimeout(connectWebSocket, 5000);
}

function onMessageReceived(payload) {
    try {
        const message = JSON.parse(payload.body);
        displayMessage(message);
    } catch (error) {
        console.error('❌ Parse error:', error);
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
    }
}

// ===================================
// FRIENDS & SEARCH MODULE
// ===================================

function initializeFriendsManagement() {
    const friendsModal = document.getElementById('friendsModal');
    const friendsBtn = document.getElementById('friendsBtn');
    const closeFriendsModalBtn = document.getElementById('closeFriendsModalBtn');
    const allUsersList = document.getElementById('allUsersList');
    const userSearchInput = document.getElementById('userSearchInput');

    const performSearch = async (query) => {
        try {
            allUsersList.innerHTML = '<p style="padding:10px; color:var(--text-secondary);">Đang tìm...</p>';
            const response = await fetch(`${API_BASE_URL}/api/users/search?q=${query}`, {
                headers: {
                    'Content-Type': 'application/json',
                    'X-User-Id': userId,
                    'X-Session-Id': sessionId
                }
            });
            const resData = await response.json();
            if (resData.success) renderUserList(allUsersList, resData.data);
        } catch (error) {
            console.error("❌ Search error:", error);
        }
    };

    const renderUserList = (container, users) => {
        container.innerHTML = '';
        if (!users || users.length === 0) {
            container.innerHTML = '<p style="padding:10px; color:var(--text-secondary);">Không tìm thấy ai.</p>';
            return;
        }
        users.forEach(user => {
            const div = document.createElement('div');
            div.className = 'user-list-item';
            div.innerHTML = `<span>👤 ${escapeHtml(user.username)}</span><div class="actions"><button class="btn-action btn-add" data-id="${user.id}">Thêm bạn</button></div>`;
            container.appendChild(div);
        });
    };

    friendsBtn.addEventListener('click', () => { friendsModal.style.display = 'block'; performSearch(''); });
    closeFriendsModalBtn.addEventListener('click', () => friendsModal.style.display = 'none');
    userSearchInput.addEventListener('input', (e) => {
        clearTimeout(searchTimeout);
        const query = e.target.value.trim();
        searchTimeout = setTimeout(() => performSearch(query), 300);
    });
}

// ===================================
// GROUP MANAGEMENT MODULE
// ===================================

function initializeGroupManagement() {
    const createGroupModal = document.getElementById('createGroupModal');
    const createGroupBtn = document.getElementById('createGroupBtn');
    const closeGroupModalBtn = document.getElementById('closeGroupModalBtn');
    const submitCreateGroupBtn = document.getElementById('submitCreateGroupBtn');
    const memberSelectionList = document.getElementById('memberSelectionList');
    const groupNameInput = document.getElementById('groupNameInput');
    const myGroupsList = document.getElementById('myGroupsList');

    let selectedUsers = new Set();

    // 1. Mở Modal: Load đồng thời danh sách User (để tạo) và danh sách Nhóm (để vào)
    createGroupBtn.addEventListener('click', async () => {
        createGroupModal.style.display = 'block';
        selectedUsers.clear();
        groupNameInput.value = '';
        
        // Load danh sách user
        const userRes = await fetch(`${API_BASE_URL}/api/users`, {
            headers: { 'X-User-Id': userId, 'X-Session-Id': sessionId }
        });
        const userData = await userRes.json();
        if (userData.success) renderSelectionList(userData.data);

        // Load danh sách nhóm của tôi
        loadMyGroups();
    });

    // 2. Hàm Load danh sách nhóm tham gia
    async function loadMyGroups() {
        myGroupsList.innerHTML = '<p style="padding:10px;">Đang tải nhóm...</p>';
        try {
            const response = await fetch(`${API_BASE_URL}/api/groups/my`, {
                headers: { 'X-User-Id': userId, 'X-Session-Id': sessionId }
            });
            const resData = await response.json();
            if (resData.success) {
                renderMyGroups(resData.data);
            }
        } catch (error) {
            console.error("Lỗi load nhóm:", error);
            myGroupsList.innerHTML = '<p>Không thể tải danh sách nhóm.</p>';
        }
    }

    // 3. Hiển thị danh sách nhóm và xử lý nút Truy cập
    function renderMyGroups(groups) {
        myGroupsList.innerHTML = groups.length === 0 ? '<p style="padding:10px;">Bạn chưa tham gia nhóm nào.</p>' : '';
        groups.forEach(group => {
            const div = document.createElement('div');
            div.className = 'user-list-item';
            div.style.justifyContent = 'space-between';
            div.innerHTML = `
                <span><b>#</b> ${escapeHtml(group.name)}</span>
                <button class="btn-action btn-access" data-id="${group.id}" data-name="${group.name}">Truy cập</button>
            `;
            myGroupsList.appendChild(div);
        });
    }

    // 4. Sự kiện khi nhấn Truy cập nhóm
    myGroupsList.addEventListener('click', (e) => {
        if (e.target.classList.contains('btn-access')) {
            const gId = e.target.getAttribute('data-id');
            const gName = e.target.getAttribute('data-name');
            
            // Set ID nhóm vào ô người nhận
            document.getElementById('recipientInput').value = gId; 
            
            // Thông báo trên màn hình chat
            const msgContainer = document.getElementById('messagesContainer');
            msgContainer.innerHTML = `<div class="welcome-message">🔔 Bạn đang chat trong nhóm: <b>${gName}</b> (ID: ${gId})</div>`;
            
            createGroupModal.style.display = 'none';
        }
    });

    // 5. Render danh sách chọn thành viên (Checkbox)
    function renderSelectionList(users) {
        memberSelectionList.innerHTML = '';
        users.forEach(user => {
            if(user.id === userId) return; // Không chọn chính mình
            const item = document.createElement('div');
            item.className = 'user-select-item';
            item.innerHTML = `<span>👤 ${user.username}</span><input type="checkbox" value="${user.id}">`;
            item.querySelector('input').addEventListener('change', (e) => {
                if (e.target.checked) selectedUsers.add(user.id);
                else selectedUsers.delete(user.id);
            });
            memberSelectionList.appendChild(item);
        });
    }

    // 6. Gửi yêu cầu Tạo Nhóm
    submitCreateGroupBtn.addEventListener('click', async () => {
        const groupName = groupNameInput.value.trim();
        if (!groupName) return alert("Vui lòng nhập tên nhóm!");

        const requestBody = { name: groupName, memberIds: Array.from(selectedUsers) };
        try {
            const response = await fetch(`${API_BASE_URL}/api/groups`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json', 'X-User-Id': userId, 'X-Session-Id': sessionId },
                body: JSON.stringify(requestBody)
            });
            const data = await response.json();
            if (data.success) {
                alert('🎉 ' + data.message);
                loadMyGroups(); // Load lại danh sách nhóm bên phải
                groupNameInput.value = '';
            }
        } catch (error) {
            console.error("Lỗi tạo nhóm:", error);
        }
    });

    closeGroupModalBtn.addEventListener('click', () => createGroupModal.style.display = 'none');
}

// ===================================
// UTILITY FUNCTIONS
// ===================================

function displayMessage(message) {
    const container = document.getElementById('messagesContainer');
    const div = document.createElement('div');
    div.className = `message ${message.from === currentUsername ? 'own' : ''}`;
    div.innerHTML = `
        <div class="message-header">
            <span class="message-sender">${escapeHtml(message.from)}</span>
        </div>
        <div class="message-bubble">${escapeHtml(message.content)}</div>
    `;
    container.appendChild(div);
    container.scrollTop = container.scrollHeight;
}

function updateConnectionStatus(status) {
    const el = document.getElementById('connectionStatus');
    if (!el) return;
    el.textContent = status === 'connected' ? 'Đã kết nối' : 'Đang kết nối...';
}

function handleLogout() {
    if (confirm('Bạn muốn đăng xuất?')) {
        localStorage.clear();
        window.location.href = 'index.html';
    }
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}