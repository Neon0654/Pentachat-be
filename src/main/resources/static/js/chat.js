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
let searchTimeout = null; // Dùng cho debounce tìm kiếm

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

    // 5. Khởi tạo Module Tìm kiếm (Friends Modal)
    if (document.getElementById('friendsBtn')) {
        initializeFriendsManagement();
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
    setTimeout(connectWebSocket, 5000); // Thử kết nối lại sau 5s
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
// FRIENDS & SEARCH MODULE (Đã lược bỏ logic lỗi)
// ===================================

function initializeFriendsManagement() {
    const friendsModal = document.getElementById('friendsModal');
    const friendsBtn = document.getElementById('friendsBtn');
    const closeFriendsModalBtn = document.getElementById('closeFriendsModalBtn');
    const allUsersList = document.getElementById('allUsersList');
    const userSearchInput = document.getElementById('userSearchInput');

    // --- Hàm gọi API Tìm kiếm ---
    const performSearch = async (query) => {
        try {
            // Hiển thị loading nhẹ
            allUsersList.innerHTML = '<p style="padding:10px; color:var(--text-secondary);">Đang tìm...</p>';
            
            const response = await fetch(`${API_BASE_URL}/api/users/search?q=${query}`, {
                headers: {
                    'Content-Type': 'application/json',
                    'X-User-Id': userId,
                    'X-Session-Id': sessionId
                }
            });

            const resData = await response.json();

            if (resData.success) {
                renderUserList(allUsersList, resData.data);
            } else {
                allUsersList.innerHTML = `<p style="padding:10px; color:var(--error-color);">${resData.message}</p>`;
            }
        } catch (error) {
            console.error("❌ Search API Error:", error);
            allUsersList.innerHTML = '<p style="padding:10px; color:var(--error-color);">Lỗi kết nối Server.</p>';
        }
    };

    // --- Hàm hiển thị danh sách User ---
    const renderUserList = (container, users) => {
        container.innerHTML = '';
        if (!users || users.length === 0) {
            container.innerHTML = '<p style="padding:10px; color:var(--text-secondary);">Không tìm thấy người dùng nào.</p>';
            return;
        }

        users.forEach(user => {
            const div = document.createElement('div');
            div.className = 'user-list-item';
            div.innerHTML = `
                <span class="username">👤 ${escapeHtml(user.username)}</span>
                <div class="actions">
                    <button class="btn-action btn-add" data-id="${user.id}">Thêm bạn</button>
                </div>
            `;
            container.appendChild(div);
        });
    };

    // --- Events ---
    friendsBtn.addEventListener('click', () => {
        friendsModal.style.display = 'block';
        performSearch(''); // Load gợi ý khi mở modal
    });

    closeFriendsModalBtn.addEventListener('click', () => {
        friendsModal.style.display = 'none';
    });

    // Lắng nghe gõ phím với cơ chế Debounce (Chờ 300ms sau khi ngừng gõ mới gọi API)
    userSearchInput.addEventListener('input', (e) => {
        clearTimeout(searchTimeout);
        const query = e.target.value.trim();
        searchTimeout = setTimeout(() => {
            performSearch(query);
        }, 300);
    });

    // Tắt modal khi click ra ngoài
    window.addEventListener('click', (e) => {
        if (e.target === friendsModal) friendsModal.style.display = 'none';
    });
}

// ===================================
// UTILITY FUNCTIONS
// ===================================

function displayMessage(message) {
    const container = document.getElementById('messagesContainer');
    const welcome = container.querySelector('.welcome-message');
    if (welcome) welcome.remove();

    const div = document.createElement('div');
    div.className = `message ${message.from === currentUsername ? 'own' : ''}`;
    
    const time = message.timestamp ? formatTimestamp(message.timestamp) : 'Vừa xong';
    const recipient = message.to ? `<span class="message-recipient">→ ${escapeHtml(message.to)}</span>` : '';

    div.innerHTML = `
        <div class="message-header">
            <span class="message-sender">${escapeHtml(message.from)}</span>
            <span class="message-time">${time}</span>
            ${recipient}
        </div>
        <div class="message-bubble">${escapeHtml(message.content)}</div>
    `;
    container.appendChild(div);
    container.scrollTop = container.scrollHeight;
}

function updateConnectionStatus(status) {
    const el = document.getElementById('connectionStatus');
    if (!el) return;
    el.className = 'status-badge';
    if (status === 'connecting') {
        el.classList.add('status-connecting');
        el.textContent = 'Đang kết nối...';
    } else if (status === 'connected') {
        el.classList.add('status-connected');
        el.textContent = 'Đã kết nối';
    } else {
        el.classList.add('status-disconnected');
        el.textContent = 'Mất kết nối';
    }
}

function formatTimestamp(ts) {
    const d = new Date(ts);
    return d.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function handleLogout() {
    if (confirm('Bạn muốn đăng xuất?')) {
        localStorage.clear();
        window.location.href = 'index.html';
    }
}