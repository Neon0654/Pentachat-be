// Authentication JavaScript
// Handles login and registration functionality

const API_BASE_URL = 'http://localhost:8080';

// DOM Elements
const loginForm = document.getElementById('loginForm');
const registerBtn = document.getElementById('registerBtn');
const errorMessage = document.getElementById('errorMessage');
const loginBtn = document.getElementById('loginBtn');

// Check if already logged in
window.addEventListener('DOMContentLoaded', () => {
    const sessionId = localStorage.getItem('sessionId');
    const username = localStorage.getItem('username');

    if (sessionId && username) {
        // Already logged in, redirect to chat
        window.location.href = 'chat.html';
    }
});

// Login Form Submit
loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();

    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value;

    if (!username || !password) {
        showError('Vui lòng nhập đầy đủ username và password');
        return;
    }

    await handleLogin(username, password);
});

// Register Button Click
registerBtn.addEventListener('click', async () => {
    const username = prompt('Nhập username mới:');
    if (!username) return;

    const password = prompt('Nhập password:');
    if (!password) return;

    await handleRegister(username, password);
});

// Handle Login
async function handleLogin(username, password) {
    try {
        setLoading(true);
        hideError();

        const response = await fetch(`${API_BASE_URL}/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ username, password })
        });

        const data = await response.json();

        if (response.ok && data.success) {
            // Save session info
            localStorage.setItem('sessionId', data.data.sessionId);
            localStorage.setItem('username', data.data.username);
            localStorage.setItem('userId', data.data.id);

            // Redirect to chat
            window.location.href = 'chat.html';
        } else {
            showError(data.message || 'Đăng nhập thất bại. Vui lòng kiểm tra lại username và password.');
        }
    } catch (error) {
        console.error('Login error:', error);
        showError('Không thể kết nối đến server. Vui lòng kiểm tra server có đang chạy không.');
    } finally {
        setLoading(false);
    }
}

// Handle Register
async function handleRegister(username, password) {
    try {
        setLoading(true);
        hideError();

        const response = await fetch(`${API_BASE_URL}/auth/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ username, password })
        });

        const data = await response.json();

        if (response.ok && data.success) {
            alert('✅ Đăng ký thành công! Bây giờ bạn có thể đăng nhập.');
            // Auto-fill username
            document.getElementById('username').value = username;
            document.getElementById('password').focus();
        } else {
            showError(data.message || 'Đăng ký thất bại. Username có thể đã tồn tại.');
        }
    } catch (error) {
        console.error('Register error:', error);
        showError('Không thể kết nối đến server. Vui lòng kiểm tra server có đang chạy không.');
    } finally {
        setLoading(false);
    }
}

// UI Helper Functions
function showError(message) {
    errorMessage.textContent = message;
    errorMessage.style.display = 'block';
}

function hideError() {
    errorMessage.style.display = 'none';
}

function setLoading(isLoading) {
    loginBtn.disabled = isLoading;
    registerBtn.disabled = isLoading;

    const btnText = loginBtn.querySelector('.btn-text');
    const btnLoader = loginBtn.querySelector('.btn-loader');

    if (isLoading) {
        btnText.style.display = 'none';
        btnLoader.style.display = 'inline';
    } else {
        btnText.style.display = 'inline';
        btnLoader.style.display = 'none';
    }
}
