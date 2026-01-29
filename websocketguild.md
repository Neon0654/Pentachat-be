# Hướng Dẫn Test WebSocket STOMP

## 📋 Checklist Test WebSocket

### ✅ Bước 1: Kiểm tra WebSocket server đã chạy
- [ ] Start ứng dụng: `mvn spring-boot:run`
- [ ] Kiểm tra log có dòng: `Mapped "{[/ws]}"` hoặc `WebSocket endpoint registered`
- [ ] Server chạy tại: `http://localhost:8080`

### ✅ Bước 2: Xác nhận endpoint WebSocket có thể kết nối
- [ ] WebSocket endpoint: `ws://localhost:8080/ws`
- [ ] Kết nối thành công không có lỗi

### ✅ Bước 3: Kiểm tra STOMP CONNECT trả về CONNECTED
- [ ] Gửi STOMP CONNECT frame
- [ ] Nhận được CONNECTED frame từ server

### ✅ Bước 4: Test SUBSCRIBE vào /topic/messages
- [ ] Subscribe destination: `/topic/messages`
- [ ] Nhận được subscription confirmation

### ✅ Bước 5: Gửi message test qua /app/chat.send
- [ ] Gửi JSON message tới `/app/chat.send`
- [ ] Client nhận được message từ `/topic/messages`

### ✅ Bước 6: Kiểm tra logs
- [ ] Server log hiển thị message received
- [ ] Xác nhận broadcast thành công

---

## 🔧 Code Tối Thiểu

### 1. WebSocketConfig.java
```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins("*");
    }
}
```

### 2. WebSocketController.java
```java
@Controller
@Slf4j
public class WebSocketController {

    @MessageMapping("/chat.send")
    @SendTo("/topic/messages")
    public ChatMessage sendMessage(ChatMessage message) {
        log.info("📨 Message: {} -> {}: {}", 
            message.getFrom(), message.getTo(), message.getContent());
        return message;
    }
}
```

### 3. ChatMessage.java (DTO)
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private String from;
    private String to;
    private String content;
    private LocalDateTime timestamp;
}
```

---

## 🧪 Cách Test

### Option 1: Test bằng Browser (Đơn giản nhất)

#### Bước 1: Tạo file `websocket-test.html`

```html
<!DOCTYPE html>
<html>
<head>
    <title>WebSocket Test</title>
    <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>
</head>
<body>
    <h1>WebSocket STOMP Test</h1>
    
    <div>
        <button onclick="connect()">1. Connect</button>
        <button onclick="subscribe()">2. Subscribe</button>
        <button onclick="sendMessage()">3. Send Message</button>
        <button onclick="disconnect()">4. Disconnect</button>
    </div>
    
    <h3>Status:</h3>
    <div id="status">Not connected</div>
    
    <h3>Received Messages:</h3>
    <div id="messages"></div>

    <script>
        let stompClient = null;

        function connect() {
            const socket = new SockJS('http://localhost:8080/ws');
            stompClient = Stomp.over(socket);
            
            stompClient.connect({}, function(frame) {
                document.getElementById('status').innerHTML = '✅ CONNECTED';
                console.log('Connected: ' + frame);
            }, function(error) {
                document.getElementById('status').innerHTML = '❌ ERROR: ' + error;
                console.error('Error: ' + error);
            });
        }

        function subscribe() {
            if (!stompClient || !stompClient.connected) {
                alert('Please connect first!');
                return;
            }
            
            stompClient.subscribe('/topic/messages', function(message) {
                const msg = JSON.parse(message.body);
                const div = document.getElementById('messages');
                div.innerHTML += `<p>📨 From: ${msg.from} | To: ${msg.to} | Content: ${msg.content}</p>`;
                console.log('Received:', msg);
            });
            
            document.getElementById('status').innerHTML = '✅ CONNECTED & SUBSCRIBED';
        }

        function sendMessage() {
            if (!stompClient || !stompClient.connected) {
                alert('Please connect first!');
                return;
            }
            
            const message = {
                from: 'user1',
                to: 'user2',
                content: 'Hello from WebSocket!',
                timestamp: new Date().toISOString()
            };
            
            stompClient.send('/app/chat.send', {}, JSON.stringify(message));
            console.log('Sent:', message);
        }

        function disconnect() {
            if (stompClient) {
                stompClient.disconnect();
                document.getElementById('status').innerHTML = '❌ DISCONNECTED';
            }
        }
    </script>
</body>
</html>
```

#### Bước 2: Mở file trong browser và test

1. Click **"1. Connect"** → Status hiển thị "✅ CONNECTED"
2. Click **"2. Subscribe"** → Status hiển thị "✅ CONNECTED & SUBSCRIBED"
3. Click **"3. Send Message"** → Nhận được message hiển thị bên dưới
4. Kiểm tra console log của server

---

### Option 2: Test bằng Postman (WebSocket Request)

#### Bước 1: Tạo WebSocket Request

1. Mở Postman
2. New → WebSocket Request
3. URL: `ws://localhost:8080/ws`
4. Click **Connect**

#### Bước 2: Gửi STOMP CONNECT

```
CONNECT
accept-version:1.1,1.0
heart-beat:10000,10000

^@
```

**Nhận được:**
```
CONNECTED
version:1.1
heart-beat:0,0

^@
```

#### Bước 3: SUBSCRIBE

```
SUBSCRIBE
id:sub-0
destination:/topic/messages

^@
```

#### Bước 4: Gửi MESSAGE

```
SEND
destination:/app/chat.send
content-type:application/json

{"from":"user1","to":"user2","content":"Hello WebSocket!","timestamp":"2026-01-29T18:00:00"}
^@
```

**Nhận được từ /topic/messages:**
```
MESSAGE
destination:/topic/messages
content-type:application/json
subscription:sub-0
message-id:xxx

{"from":"user1","to":"user2","content":"Hello WebSocket!","timestamp":"2026-01-29T18:00:00"}
^@
```

> ⚠️ **Lưu ý:** `^@` là ký tự NULL (Ctrl+Shift+2 trong Postman)

---

### Option 3: Test bằng wscat (Command Line)

#### Cài đặt wscat
```bash
npm install -g wscat
```

#### Kết nối
```bash
wscat -c ws://localhost:8080/ws
```

#### Gửi STOMP frames

**1. CONNECT:**
```
CONNECT
accept-version:1.1,1.0

```
(Nhấn Enter 2 lần)

**2. SUBSCRIBE:**
```
SUBSCRIBE
id:sub-0
destination:/topic/messages

```

**3. SEND MESSAGE:**
```
SEND
destination:/app/chat.send
content-type:application/json

{"from":"user1","to":"user2","content":"Test message"}
```

---

## 📊 Expected Results

### ✅ Kết quả mong đợi

#### 1. Server Logs
```
📨 WebSocket message received:
   From: user1
   To: user2
   Content: Hello WebSocket!
   Timestamp: 2026-01-29T18:00:00
```

#### 2. Client nhận được
```json
{
  "from": "user1",
  "to": "user2",
  "content": "Hello WebSocket!",
  "timestamp": "2026-01-29T18:00:00"
}
```

#### 3. WebSocket Status
- ✅ Connection: CONNECTED
- ✅ Subscription: ACTIVE
- ✅ Message: RECEIVED

---

## 🐛 Troubleshooting

### ❌ Lỗi: "Failed to connect"
**Nguyên nhân:** Server chưa chạy  
**Giải pháp:** Chạy `mvn spring-boot:run`

### ❌ Lỗi: "404 Not Found"
**Nguyên nhân:** Endpoint `/ws` chưa được đăng ký  
**Giải pháp:** Kiểm tra `WebSocketConfig` có `@EnableWebSocketMessageBroker`

### ❌ Lỗi: "No handler for destination /app/chat.send"
**Nguyên nhân:** Controller chưa có `@MessageMapping("/chat.send")`  
**Giải pháp:** Thêm method trong `WebSocketController`

### ❌ Không nhận được message
**Nguyên nhân:** Chưa subscribe hoặc destination sai  
**Giải pháp:** 
- Subscribe đúng `/topic/messages`
- Gửi tới đúng `/app/chat.send`

---

## 📝 Test Checklist Summary

| Step | Action | Expected Result | Status |
|------|--------|----------------|--------|
| 1 | Start server | Server running on port 8080 | ⬜ |
| 2 | Connect to `/ws` | CONNECTED frame received | ⬜ |
| 3 | Subscribe `/topic/messages` | Subscription confirmed | ⬜ |
| 4 | Send to `/app/chat.send` | Message sent successfully | ⬜ |
| 5 | Receive from `/topic/messages` | Message received | ⬜ |
| 6 | Check server logs | Log shows message details | ⬜ |

---

## 🎯 Quick Test Script (Browser Console)

Paste vào browser console để test nhanh:

```javascript
// 1. Connect
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    console.log('✅ Connected:', frame);
    
    // 2. Subscribe
    stompClient.subscribe('/topic/messages', function(message) {
        console.log('📨 Received:', JSON.parse(message.body));
    });
    
    // 3. Send message
    setTimeout(() => {
        const msg = {
            from: 'test-user',
            to: 'recipient',
            content: 'Quick test message',
            timestamp: new Date().toISOString()
        };
        stompClient.send('/app/chat.send', {}, JSON.stringify(msg));
        console.log('📤 Sent:', msg);
    }, 1000);
});
```

---

**Happy Testing! 🚀**
