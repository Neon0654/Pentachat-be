# 💬 PentaChat - WebSocket Chat UI

Giao diện web cơ bản để test chức năng đăng nhập và chat realtime qua WebSocket.

## 📁 Cấu Trúc Files

```
fe_base/
├── index.html          # Trang đăng nhập
├── chat.html           # Trang chat realtime
├── css/
│   └── style.css       # Stylesheet với dark theme
└── js/
    ├── auth.js         # Xử lý authentication
    └── chat.js         # Xử lý WebSocket chat
```

## 🚀 Cách Sử Dụng

### Bước 1: Khởi động Backend Server

Đảm bảo Spring Boot server đang chạy:

```bash
mvn spring-boot:run
```

Server sẽ chạy tại: `http://localhost:8080`

### Bước 2: Mở Giao Diện Web

Mở file `index.html` bằng trình duyệt:

**Cách 1: Double-click file**
- Vào folder `fe_base`
- Double-click vào `index.html`

**Cách 2: Mở từ trình duyệt**
- Mở Chrome/Firefox/Edge
- Nhấn `Ctrl + O` (hoặc `Cmd + O` trên Mac)
- Chọn file `d:\softwave\Testing-Lab_copy\fe_base\index.html`

### Bước 3: Đăng Ký Tài Khoản (Lần Đầu)

1. Click nút **"Đăng Ký Tài Khoản Mới"**
2. Nhập username (ví dụ: `user1`)
3. Nhập password (ví dụ: `123456`)
4. Click OK

### Bước 4: Đăng Nhập

1. Nhập username và password vừa đăng ký
2. Click **"Đăng Nhập"**
3. Bạn sẽ được chuyển đến trang chat

### Bước 5: Test Chat Realtime

**Test với 1 user:**
1. Sau khi đăng nhập, bạn sẽ thấy giao diện chat
2. Nhập tin nhắn vào ô input
3. Click **"Gửi"** hoặc nhấn Enter
4. Tin nhắn sẽ xuất hiện trong danh sách

**Test với nhiều users (Recommended):**
1. Mở thêm 1 tab/cửa sổ trình duyệt mới
2. Mở lại `index.html`
3. Đăng ký và đăng nhập với username khác (ví dụ: `user2`)
4. Gửi tin nhắn từ tab 1
5. **Tin nhắn sẽ xuất hiện REALTIME ở tab 2!** ✨

## ✨ Tính Năng

### Trang Đăng Nhập (`index.html`)
- ✅ Form đăng nhập với username/password
- ✅ Nút đăng ký tài khoản mới
- ✅ Validation và error handling
- ✅ Auto-redirect nếu đã đăng nhập
- ✅ Modern UI với gradient background

### Trang Chat (`chat.html`)
- ✅ Hiển thị username hiện tại
- ✅ Status kết nối WebSocket (Đang kết nối/Đã kết nối/Mất kết nối)
- ✅ Danh sách tin nhắn với auto-scroll
- ✅ Gửi tin nhắn realtime
- ✅ Hiển thị timestamp (vừa xong, X phút trước, etc.)
- ✅ Phân biệt tin nhắn của mình và người khác
- ✅ Gửi tin nhắn cho người cụ thể (optional)
- ✅ Nút đăng xuất
- ✅ Auto-reconnect khi mất kết nối

### Styling
- ✅ Dark theme với gradient colors
- ✅ Smooth animations và transitions
- ✅ Responsive design (mobile-friendly)
- ✅ Chat bubbles đẹp mắt
- ✅ Custom scrollbar
- ✅ Loading states

## 🔧 Kỹ Thuật

### Authentication
- API: `POST /auth/login` và `POST /auth/register`
- Session management với `localStorage`
- Auto-redirect based on session

### WebSocket
- **Protocol**: SockJS + STOMP
- **Endpoint**: `ws://localhost:8080/ws`
- **Subscribe**: `/topic/messages` (public chat)
- **Send**: `/app/chat.send`
- **Auto-reconnect**: Tự động kết nối lại sau 5 giây nếu mất kết nối

### Message Format
```json
{
  "from": "username",
  "to": "recipient (optional)",
  "content": "message content",
  "timestamp": "2026-01-29T18:35:00.000Z"
}
```

## 🐛 Troubleshooting

### Lỗi: "Không thể kết nối đến server"
- ✅ Kiểm tra Spring Boot server có đang chạy không
- ✅ Kiểm tra port 8080 có bị chiếm không
- ✅ Mở Developer Console (F12) để xem lỗi chi tiết

### Lỗi: "WebSocket mất kết nối"
- ✅ Kiểm tra server có bị crash không
- ✅ Đợi 5 giây để auto-reconnect
- ✅ Refresh trang nếu vẫn không kết nối được

### Lỗi: "Đăng nhập thất bại"
- ✅ Kiểm tra username/password có đúng không
- ✅ Thử đăng ký tài khoản mới
- ✅ Kiểm tra API endpoint `/auth/login` có hoạt động không

### CORS Issues
Nếu gặp lỗi CORS khi gọi API, bạn có thể:
1. Serve files qua HTTP server thay vì file://
2. Hoặc cấu hình CORS trong Spring Boot (đã được config sẵn trong `WebSocketConfig`)

## 📸 Screenshots

### Trang Đăng Nhập
- Modern gradient background
- Clean login form
- Register button
- Info box với hướng dẫn

### Trang Chat
- Header với username và status
- Messages area với auto-scroll
- Input area với recipient field
- Real-time message updates

## 🎯 Next Steps

Để improve thêm, bạn có thể:
- [ ] Thêm typing indicators
- [ ] Thêm online users list
- [ ] Thêm emoji picker
- [ ] Thêm file upload
- [ ] Thêm message history từ database
- [ ] Thêm private messaging
- [ ] Thêm notifications

## 📝 Notes

- UI được thiết kế với dark theme hiện đại
- Sử dụng CDN cho SockJS và STOMP (không cần install)
- LocalStorage để lưu session (đơn giản cho demo)
- Responsive design cho mobile và desktop

---

**Enjoy chatting! 💬✨**
