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
    // 6. Khởi tạo Module Quản lý Game (Game Modal
    }
    if (document.getElementById('gamesBtn')) {
        initializeGameManagement();
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
    // Đăng ký nhận thông báo mời chơi game
    stompClient.subscribe('/topic/notifications.' + userId, (payload) => {
        console.log("🔔 Nhận được thông báo game thực tế!");
        const invite = JSON.parse(payload.body);

        const confirmJoin = confirm(`🎮 Người dùng ${invite.inviterId} mời bạn vào phòng: ${invite.roomId}. Chấp nhận chứ?`);

        if (confirmJoin) {
            enterGameLobby(invite.roomId);
            acceptInvite(invite.roomId, invite.id);
        }
    });
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
// GAME MANAGEMENT MODULE
// ===================================
function initializeGameManagement() {
    const gameModal = document.getElementById('gameModal');
    const gamesBtn = document.getElementById('gamesBtn');
    const selectionSection = document.getElementById('gameSelectionSection');
    const roomSection = document.getElementById('gameRoomSection');
    const gameCards = document.querySelectorAll('.game-card');
    const backBtn = document.getElementById('backToGamesBtn');

    // 1. [MỚI THÊM] Lệnh mở Modal khi bấm nút "Trò chơi"
    if (gamesBtn) {
        gamesBtn.onclick = () => {
            gameModal.style.display = 'block';
            // Reset về màn hình chọn game mỗi khi mở
            selectionSection.style.display = 'block';
            roomSection.style.display = 'none';
        };
    }

   

    // Nút quay lại menu chọn game
    if (backBtn) {
        backBtn.onclick = () => {
            selectionSection.style.display = 'block';
            roomSection.style.display = 'none';
        };
    }

    // Xử lý khi chọn game cụ thể
    gameCards.forEach(card => {
        card.onclick = () => {
            const gameName = card.getAttribute('data-game');
            const roomId = gameName.toUpperCase() + "_" + Math.floor(Math.random() * 1000);
            
            selectionSection.style.display = 'none';
            roomSection.style.display = 'block';
            document.getElementById('currentRoomDisplay').textContent = roomId;
            enterGameLobby(roomId);
            loadFriendsToInvite(roomId);
            updateRoomMembers(roomId);
        };
    });
}
// Biến để lưu kênh đang nghe, tránh nghe trùng nhiều phòng
let currentRoomSubscription = null;

let roomSubscription = null; // Biến toàn cục để quản lý subscription

function enterGameLobby(roomId) {
    // Hiện giao diện sảnh chờ
    document.getElementById('gameModal').style.display = 'block';
    document.getElementById('gameSelectionSection').style.display = 'none';
    document.getElementById('gameRoomSection').style.display = 'block';
    document.getElementById('currentRoomDisplay').textContent = roomId;

    // --- PHẦN QUAN TRỌNG NHẤT ---
    // 1. Nếu đang nghe phòng cũ thì hủy đi
    if (roomSubscription) roomSubscription.unsubscribe();

    // 2. Đăng ký nghe kênh riêng của phòng này
    roomSubscription = stompClient.subscribe('/topic/room.' + roomId, (payload) => {
        console.log("📢 Nhận tín hiệu cập nhật danh sách thành viên!");
        // Khi nhận được bất kỳ tin nhắn nào từ kênh này, tự động gọi API load lại danh sách
        updateRoomMembers(roomId); 
    });

    // 3. Load danh sách lần đầu để hiện tên mình (chủ phòng)
    updateRoomMembers(roomId);
}

async function sendGameInvite(roomId, inviteeId) {
    try {
        const res = await fetch(`${API_BASE_URL}/api/rooms/${roomId}/invite/${inviteeId}`, {
            method: 'POST',
            headers: { 'X-User-Id': userId, 'X-Session-Id': sessionId }
        });
        if (res.ok) alert("🚀 Đã gửi lời mời!");
    } catch (e) { console.error("Lỗi gửi mời:", e); }
}

async function acceptInvite(roomId, inviteId) {
    try {
        const res = await fetch(`${API_BASE_URL}/api/rooms/${roomId}/accept/${inviteId}`, {
            method: 'POST',
            headers: { 'X-User-Id': userId, 'X-Session-Id': sessionId }
        });
        
        if (res.ok) {
            // Gửi một tin nhắn ẩn lên WebSocket để báo cho cả phòng
            stompClient.send("/app/chat.send", {}, JSON.stringify({
                from: currentUsername,
                to: roomId, // Gửi vào ID phòng
                content: "ĐÃ VÀO PHÒNG", // Tín hiệu nhận biết
                timestamp: new Date().toISOString()
            }));
            
            enterGameLobby(roomId);
        }
    } catch (e) { console.error("Lỗi chấp nhận:", e); }
}

async function updateRoomMembers(roomId) {
    try {
        const res = await fetch(`${API_BASE_URL}/api/rooms/${roomId}/members`);
        const resData = await res.json();
        const list = document.getElementById('joinedMembersList');
        list.innerHTML = '';

        // NẾU DATABASE CHƯA CÓ AI (Phòng mới tạo) -> HIỆN CHÍNH MÌNH
        if (!resData.data.members || resData.data.members.length === 0) {
            const div = document.createElement('div');
            div.className = 'user-list-item';
            div.innerHTML = `<span>🎮 ${currentUsername} 👑 (Chủ phòng)</span><small style="color:green">Online</small>`;
            list.appendChild(div);
            return;
        }

        // Nếu đã có dữ liệu từ Server
        const realOwner = resData.data.owner;
        resData.data.members.forEach(username => {
            const div = document.createElement('div');
            div.className = 'user-list-item';
            const isOwner = (username === realOwner) ? ' 👑 (Chủ phòng)' : '';
            div.innerHTML = `<span>🎮 ${username}${isOwner}</span><small style="color:green">Online</small>`;
            list.appendChild(div);
        });
    } catch (e) { console.error("Lỗi cập nhật sảnh:", e); }
}

// Hàm gọi khi đóng Modal hoặc nhấn nút thoát
async function leaveRoom() {
    const roomId = document.getElementById('currentRoomDisplay').textContent;
    if (!roomId) return;

    try {
        await fetch(`${API_BASE_URL}/api/rooms/${roomId}/leave`, {
            method: 'POST',
            headers: { 
                'X-User-Id': userId, 
                'X-Session-Id': sessionId 
            }
        });
        
        // Dọn dẹp giao diện và ngắt kết nối kênh phòng
        document.getElementById('gameModal').style.display = 'none';
        if (roomSubscription) {
            roomSubscription.unsubscribe();
            roomSubscription = null;
        }
    } catch (e) {
        console.error("Lỗi khi rời phòng:", e);
    }
}



// Hàm xử lý khi đóng Modal hoặc nhấn thoát
async function handleLeaveRoom() {
    const roomId = document.getElementById('currentRoomDisplay').textContent;
    if (!roomId) return;

    await fetch(`${API_BASE_URL}/api/rooms/${roomId}/leave`, {
        method: 'POST',
        headers: { 'X-User-Id': userId, 'X-Session-Id': sessionId }
    });

    // Đóng modal và dọn dẹp
    document.getElementById('gameModal').style.display = 'none';
    if (roomSubscription) roomSubscription.unsubscribe();
}

// Gán sự kiện cho nút đóng Modal

async function loadFriendsToInvite(roomId) {
    const list = document.getElementById('friendsToInviteList');
    list.innerHTML = '<p>Đang tải...</p>';
    const res = await fetch(`${API_BASE_URL}/api/users`, {
        headers: { 'X-User-Id': userId, 'X-Session-Id': sessionId }
    });
    const data = await res.json();
    list.innerHTML = '';
    if (data.success && data.data) {
        data.data.forEach(user => {
            if(user.id === userId) return;
            const div = document.createElement('div');
            div.className = 'user-list-item';
            div.innerHTML = `<span>👤 ${escapeHtml(user.username)}</span><button class="btn-action">Mời</button>`;
            div.querySelector('button').onclick = () => sendGameInvite(roomId, user.id);
            list.appendChild(div);
        });
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
    div.innerHTML = `
        <div class="message-header">
            <span class="message-sender">${escapeHtml(message.from)}</span>
            <span class="message-time">${formatTimestamp(message.timestamp)}</span>
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
    el.className = `status-badge status-${status}`;
}

function formatTimestamp(ts) {
    return new Date(ts).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
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
}// Hàm tổng hợp để thoát phòng sạch sẽ
async function handleExit() {
    const roomId = document.getElementById('currentRoomDisplay').textContent;
    if (!roomId) return;

    try {
        // 1. Gọi API báo cho Server là tao out rồi
        await fetch(`${API_BASE_URL}/api/rooms/${roomId}/leave`, {
            method: 'POST',
            headers: { 'X-User-Id': userId, 'X-Session-Id': sessionId }
        });

        // 2. Đóng giao diện
        document.getElementById('gameModal').style.display = 'none';

        // 3. Hủy đăng ký nghe WebSocket phòng này
        if (roomSubscription) {
            roomSubscription.unsubscribe();
            roomSubscription = null;
        }
    } catch (e) { console.error("Lỗi thoát phòng:", e); }
}


const closeBtn = document.getElementById('closeGameModalBtn');
if (closeBtn) closeBtn.onclick = handleExit;