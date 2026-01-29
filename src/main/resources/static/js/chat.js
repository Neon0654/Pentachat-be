// WebSocket Chat JavaScript
// Handles real-time messaging using SockJS and STOMP

const API_BASE_URL = 'http://localhost:8080';
const WS_URL = 'http://localhost:8080/ws';

// Global variables
let stompClient = null;
let currentUsername = null;
let isConnected = false;

// DOM Elements
const messagesContainer = document.getElementById('messagesContainer');
const messageForm = document.getElementById('messageForm');
const messageInput = document.getElementById('messageInput');
const recipientInput = document.getElementById('recipientInput');
const sendBtn = document.getElementById('sendBtn');
const logoutBtn = document.getElementById('logoutBtn');
const connectionStatus = document.getElementById('connectionStatus');
const currentUsernameDisplay = document.getElementById('currentUsername');

// Initialize on page load
window.addEventListener('DOMContentLoaded', () => {
    // Check if user is logged in
    const sessionId = localStorage.getItem('sessionId');
    const username = localStorage.getItem('username');

    if (!sessionId || !username) {
        // Not logged in, redirect to login page
        window.location.href = 'index.html';
        return;
    }

    currentUsername = username;
    currentUsernameDisplay.textContent = username;

    // Connect to WebSocket
    connectWebSocket();
});

// Connect to WebSocket
function connectWebSocket() {
    console.log('🔌 Connecting to WebSocket...');
    updateConnectionStatus('connecting');

    try {
        // Create SockJS connection
        const socket = new SockJS(WS_URL);
        stompClient = Stomp.over(socket);

        // Disable debug logging (optional)
        stompClient.debug = (msg) => {
            console.log('STOMP:', msg);
        };

        // Connect to STOMP
        stompClient.connect({}, onConnected, onError);
    } catch (error) {
        console.error('❌ WebSocket connection error:', error);
        updateConnectionStatus('disconnected');
    }
}

// On WebSocket Connected
function onConnected() {
    console.log('✅ WebSocket connected!');
    isConnected = true;
    updateConnectionStatus('connected');
    sendBtn.disabled = false;

    // Subscribe to the public topic
    stompClient.subscribe('/topic/messages', onMessageReceived);

    // Send a join notification
    const joinMessage = {
        from: currentUsername,
        to: '',
        content: `${currentUsername} đã tham gia chat!`,
        timestamp: new Date().toISOString()
    };

    // Optional: send join message
    // stompClient.send('/app/chat.send', {}, JSON.stringify(joinMessage));
}

// On WebSocket Error
function onError(error) {
    console.error('❌ WebSocket error:', error);
    isConnected = false;
    updateConnectionStatus('disconnected');
    sendBtn.disabled = true;

    // Try to reconnect after 5 seconds
    setTimeout(() => {
        console.log('🔄 Attempting to reconnect...');
        connectWebSocket();
    }, 5000);
}

// On Message Received
function onMessageReceived(payload) {
    try {
        const message = JSON.parse(payload.body);
        console.log('📨 Message received:', message);
        displayMessage(message);
    } catch (error) {
        console.error('❌ Error parsing message:', error);
    }
}

// Send Message
messageForm.addEventListener('submit', (e) => {
    e.preventDefault();

    const content = messageInput.value.trim();
    const recipient = recipientInput.value.trim();

    if (!content) return;

    if (!isConnected) {
        alert('⚠️ Chưa kết nối đến server. Vui lòng đợi...');
        return;
    }

    const message = {
        from: currentUsername,
        to: recipient || '',
        content: content,
        timestamp: new Date().toISOString()
    };

    try {
        // Send message via STOMP
        stompClient.send('/app/chat.send', {}, JSON.stringify(message));

        // Clear input
        messageInput.value = '';
        messageInput.focus();
    } catch (error) {
        console.error('❌ Error sending message:', error);
        alert('⚠️ Không thể gửi tin nhắn. Vui lòng thử lại.');
    }
});

// Display Message in UI
function displayMessage(message) {
    // Remove welcome message if exists
    const welcomeMsg = messagesContainer.querySelector('.welcome-message');
    if (welcomeMsg) {
        welcomeMsg.remove();
    }

    // Create message element
    const messageDiv = document.createElement('div');
    messageDiv.className = 'message';

    // Add 'own' class if message is from current user
    if (message.from === currentUsername) {
        messageDiv.classList.add('own');
    }

    // Format timestamp
    const timestamp = message.timestamp ? formatTimestamp(message.timestamp) : 'Vừa xong';

    // Build message HTML
    let messageHTML = `
        <div class="message-header">
            <span class="message-sender">${escapeHtml(message.from)}</span>
            <span class="message-time">${timestamp}</span>
    `;

    // Add recipient if specified
    if (message.to) {
        messageHTML += `<span class="message-recipient">→ ${escapeHtml(message.to)}</span>`;
    }

    messageHTML += `
        </div>
        <div class="message-bubble">
            ${escapeHtml(message.content)}
        </div>
    `;

    messageDiv.innerHTML = messageHTML;

    // Add to container
    messagesContainer.appendChild(messageDiv);

    // Auto-scroll to bottom
    messagesContainer.scrollTop = messagesContainer.scrollHeight;
}

// Update Connection Status
function updateConnectionStatus(status) {
    connectionStatus.className = 'status-badge';

    switch (status) {
        case 'connecting':
            connectionStatus.classList.add('status-connecting');
            connectionStatus.textContent = 'Đang kết nối...';
            break;
        case 'connected':
            connectionStatus.classList.add('status-connected');
            connectionStatus.textContent = 'Đã kết nối';
            break;
        case 'disconnected':
            connectionStatus.classList.add('status-disconnected');
            connectionStatus.textContent = 'Mất kết nối';
            break;
    }
}

// Format Timestamp
function formatTimestamp(timestamp) {
    try {
        const date = new Date(timestamp);
        const now = new Date();
        const diff = now - date;

        // If less than 1 minute ago
        if (diff < 60000) {
            return 'Vừa xong';
        }

        // If less than 1 hour ago
        if (diff < 3600000) {
            const minutes = Math.floor(diff / 60000);
            return `${minutes} phút trước`;
        }

        // If today
        if (date.toDateString() === now.toDateString()) {
            return date.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
        }

        // Otherwise show date and time
        return date.toLocaleString('vi-VN', {
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    } catch (error) {
        return 'Không rõ';
    }
}

// Escape HTML to prevent XSS
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// Logout
logoutBtn.addEventListener('click', () => {
    if (confirm('Bạn có chắc muốn đăng xuất?')) {
        // Disconnect WebSocket
        if (stompClient && isConnected) {
            stompClient.disconnect(() => {
                console.log('👋 Disconnected from WebSocket');
            });
        }

        // Clear session
        localStorage.removeItem('sessionId');
        localStorage.removeItem('username');
        localStorage.removeItem('userId');

        // Redirect to login
        window.location.href = 'index.html';
    }
});

// Handle page unload
window.addEventListener('beforeunload', () => {
    if (stompClient && isConnected) {
        stompClient.disconnect();
    }
});
