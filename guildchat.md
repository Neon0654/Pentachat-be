# Hướng Dẫn Test Message API với Postman

## 📋 Mục lục
1. [Chuẩn bị](#chuẩn-bị)
2. [Đăng ký và Đăng nhập](#đăng-ký-và-đăng-nhập)
3. [Gửi tin nhắn](#gửi-tin-nhắn)
4. [Xem inbox](#xem-inbox)
5. [Xem conversation](#xem-conversation)
6. [Đánh dấu đã đọc](#đánh-dấu-đã-đọc)
7. [Xóa tin nhắn](#xóa-tin-nhắn)
8. [Kiểm tra user status](#kiểm-tra-user-status)

---

## Chuẩn bị

### 1. Khởi động ứng dụng
```bash
mvn spring-boot:run
```

Ứng dụng sẽ chạy tại: `http://localhost:8080`

### 2. Mở Postman
- Tạo một Collection mới tên "PentaChat API"
- Base URL: `http://localhost:8080`

---

## Đăng ký và Đăng nhập

### 1️⃣ Đăng ký user mới

**Endpoint:** `POST http://localhost:8080/auth/register`

**Headers:**
```
Content-Type: application/json
```

**Body (raw JSON):**
```json
{
  "username": "alice",
  "password": "password123"
}
```

**Response thành công:**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "id": "usr_abc123",
    "username": "alice"
  }
}
```

> 💡 **Lưu lại `id` của user để dùng cho các request sau!**

### 2️⃣ Đăng ký thêm user thứ 2

Tương tự, đăng ký user "bob":
```json
{
  "username": "bob",
  "password": "password456"
}
```

### 3️⃣ Đăng nhập

**Endpoint:** `POST http://localhost:8080/auth/login`

**Body:**
```json
{
  "username": "alice",
  "password": "password123"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "userId": "usr_abc123",
    "username": "alice",
    "sessionId": "ses_xyz789"
  }
}
```

---

## Gửi tin nhắn

### 📤 Gửi message từ Alice đến Bob

**Endpoint:** `POST http://localhost:8080/message/send`

**Headers:**
```
Content-Type: application/json
```

**Body:**
```json
{
  "from": "usr_alice_id",
  "to": "usr_bob_id",
  "content": "Xin chào Bob! Bạn khỏe không?"
}
```

> ⚠️ **Thay `usr_alice_id` và `usr_bob_id` bằng ID thực tế từ bước đăng ký!**

**Response thành công:**
```json
{
  "success": true,
  "message": "Message sent successfully. Recipient online: false",
  "data": {
    "id": "msg_123456",
    "from": "usr_alice_id",
    "to": "usr_bob_id",
    "content": "Xin chào Bob! Bạn khỏe không?",
    "createdAt": "2026-01-29T17:58:00",
    "isRead": false
  }
}
```

### Gửi thêm vài tin nhắn để test

**Alice → Bob:**
```json
{
  "from": "usr_alice_id",
  "to": "usr_bob_id",
  "content": "Hôm nay bạn có rảnh không?"
}
```

**Bob → Alice:**
```json
{
  "from": "usr_bob_id",
  "to": "usr_alice_id",
  "content": "Mình khỏe! Bạn thế nào?"
}
```

---

## Xem inbox

### 📬 Lấy inbox của Bob

**Endpoint:** `GET http://localhost:8080/message/inbox/{userId}`

**Example:** `GET http://localhost:8080/message/inbox/usr_bob_id`

**Response:**
```json
{
  "success": true,
  "message": "Inbox retrieved successfully",
  "data": [
    {
      "id": "msg_123456",
      "from": "usr_alice_id",
      "to": "usr_bob_id",
      "content": "Xin chào Bob! Bạn khỏe không?",
      "createdAt": "2026-01-29T17:58:00",
      "isRead": false
    },
    {
      "id": "msg_123457",
      "from": "usr_alice_id",
      "to": "usr_bob_id",
      "content": "Hôm nay bạn có rảnh không?",
      "createdAt": "2026-01-29T17:58:30",
      "isRead": false
    }
  ]
}
```

---

## Xem conversation

### 💬 Lấy conversation giữa Alice và Bob

**Endpoint:** `GET http://localhost:8080/message/conversation/{userId1}/{userId2}`

**Example:** `GET http://localhost:8080/message/conversation/usr_alice_id/usr_bob_id`

**Response:**
```json
{
  "success": true,
  "message": "Conversation retrieved successfully",
  "data": [
    {
      "id": "msg_123456",
      "from": "usr_alice_id",
      "to": "usr_bob_id",
      "content": "Xin chào Bob! Bạn khỏe không?",
      "createdAt": "2026-01-29T17:58:00",
      "isRead": false
    },
    {
      "id": "msg_123457",
      "from": "usr_alice_id",
      "to": "usr_bob_id",
      "content": "Hôm nay bạn có rảnh không?",
      "createdAt": "2026-01-29T17:58:30",
      "isRead": false
    },
    {
      "id": "msg_123458",
      "from": "usr_bob_id",
      "to": "usr_alice_id",
      "content": "Mình khỏe! Bạn thế nào?",
      "createdAt": "2026-01-29T17:59:00",
      "isRead": false
    }
  ]
}
```

> 💡 Conversation trả về tất cả messages giữa 2 users (cả 2 chiều)

---

## Đánh dấu đã đọc

### ✅ Đánh dấu message đã đọc

**Endpoint:** `POST http://localhost:8080/message/read/{userId}/{messageId}`

**Example:** `POST http://localhost:8080/message/read/usr_bob_id/msg_123456`

**Response:**
```json
{
  "success": true,
  "message": "Message marked as read",
  "data": null
}
```

Sau khi đánh dấu, gọi lại inbox sẽ thấy `isRead: true`:
```json
{
  "id": "msg_123456",
  "from": "usr_alice_id",
  "to": "usr_bob_id",
  "content": "Xin chào Bob! Bạn khỏe không?",
  "createdAt": "2026-01-29T17:58:00",
  "isRead": true  // ✅ Đã đổi thành true
}
```

---

## Xóa tin nhắn

### 🗑️ Xóa message

**Endpoint:** `DELETE http://localhost:8080/message/{userId}/{messageId}`

**Example:** `DELETE http://localhost:8080/message/usr_bob_id/msg_123456`

**Response:**
```json
{
  "success": true,
  "message": "Message deleted successfully",
  "data": null
}
```

---

## Kiểm tra user status

### 👤 Kiểm tra user có online không

**Endpoint:** `GET http://localhost:8080/message/status/{userId}`

**Example:** `GET http://localhost:8080/message/status/usr_alice_id`

**Response (nếu online):**
```json
{
  "success": true,
  "message": "User status retrieved",
  "data": {
    "userId": "usr_alice_id",
    "username": "alice",
    "sessionId": "ses_xyz789",
    "loginTime": "2026-01-29T17:50:00"
  }
}
```

**Response (nếu offline):**
```json
{
  "success": true,
  "message": "User status retrieved",
  "data": "User is offline"
}
```

---

## 🧪 Test Flow hoàn chỉnh

### Scenario: Alice gửi tin nhắn cho Bob

1. **Đăng ký 2 users:**
   - Alice: `POST /auth/register`
   - Bob: `POST /auth/register`

2. **Alice đăng nhập:**
   - `POST /auth/login` với username "alice"

3. **Alice gửi tin nhắn cho Bob:**
   - `POST /message/send`
   ```json
   {
     "from": "alice_user_id",
     "to": "bob_user_id",
     "content": "Hello Bob!"
   }
   ```

4. **Bob đăng nhập:**
   - `POST /auth/login` với username "bob"

5. **Bob xem inbox:**
   - `GET /message/inbox/bob_user_id`
   - Sẽ thấy tin nhắn từ Alice

6. **Bob đánh dấu đã đọc:**
   - `POST /message/read/bob_user_id/message_id`

7. **Bob reply:**
   - `POST /message/send`
   ```json
   {
     "from": "bob_user_id",
     "to": "alice_user_id",
     "content": "Hi Alice!"
   }
   ```

8. **Xem conversation:**
   - `GET /message/conversation/alice_user_id/bob_user_id`
   - Sẽ thấy cả 2 tin nhắn

---

## 📝 Lưu ý quan trọng

### ✅ Điều kiện thành công:
- Server đang chạy tại `localhost:8080`
- Database đã được cấu hình đúng
- User IDs phải tồn tại trong database

### ❌ Lỗi thường gặp:

**1. "fromUserId cannot be empty"**
- Kiểm tra lại body request có đầy đủ `from`, `to`, `content` không

**2. "User not found"**
- User ID không tồn tại, cần đăng ký user trước

**3. "Message not found"**
- Message ID không đúng hoặc đã bị xóa

**4. Connection refused**
- Server chưa chạy, chạy lại `mvn spring-boot:run`

---

## 🎯 Tips & Tricks

### Sử dụng Postman Variables

Tạo Environment trong Postman:
```
baseUrl = http://localhost:8080
aliceId = usr_abc123
bobId = usr_def456
```

Sau đó dùng: `{{baseUrl}}/message/send`

### Lưu response vào variable

Trong Tests tab của request, thêm:
```javascript
var jsonData = pm.response.json();
pm.environment.set("messageId", jsonData.data.id);
```

Sau đó có thể dùng `{{messageId}}` cho các request tiếp theo!

---

## 🔍 Kiểm tra Database

Nếu muốn xem messages trong database:

```sql
SELECT * FROM messages;
SELECT * FROM messages WHERE toUserId = 'usr_bob_id';
SELECT * FROM messages WHERE isRead = 0;
```

---

**Happy Testing! 🚀**
