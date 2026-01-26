package com.hdtpt.pentachat.service;

import com.hdtpt.pentachat.dto.response.MessageResponse;
import com.hdtpt.pentachat.model.Message;
import com.hdtpt.pentachat.util.IdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Message Service - Xử lý logic gửi/nhận messages
 * 
 * Chứa hàm pushToUser() để gửi tin nhắn cho user đích
 * Lưu messages trong memory (mock data)
 */
@Service
@Slf4j
public class MessageService {

    // Mock data: lưu messages trong memory
    private final Map<String, List<MessageResponse>> inboxMap = new ConcurrentHashMap<>();

    /**
     * Hàm pushToUser - Gửi tin nhắn cho user đích
     * 
     * @param fromUserId User ID của người gửi
     * @param toUserId User ID của người nhận
     * @param content Nội dung tin nhắn
     * @return MessageResponse - Tin nhắn vừa được gửi
     */
    public MessageResponse pushToUser(String fromUserId, String toUserId, String content) {
        try {
            // Validate
            if (fromUserId == null || fromUserId.isEmpty()) {
                throw new IllegalArgumentException("fromUserId cannot be empty");
            }
            if (toUserId == null || toUserId.isEmpty()) {
                throw new IllegalArgumentException("toUserId cannot be empty");
            }
            if (content == null || content.isEmpty()) {
                throw new IllegalArgumentException("content cannot be empty");
            }

            // Tạo message mới
            String messageId = IdGenerator.generateId();
            MessageResponse message = MessageResponse.builder()
                    .id(messageId)
                    .from(fromUserId)
                    .to(toUserId)
                    .content(content)
                    .createdAt(LocalDateTime.now())
                    .isRead(false)
                    .build();

            // Thêm vào inbox của người nhận
            inboxMap.computeIfAbsent(toUserId, k -> new ArrayList<>()).add(message);

            log.info("Message pushed to user {} from user {}: {}", toUserId, fromUserId, content);

            // Ở đây bạn có thể thêm logic để gửi push notification 
            // hoặc WebSocket event để thông báo cho người nhận
            notifyUserNewMessage(toUserId, message);

            return message;

        } catch (Exception e) {
            log.error("Error pushing message to user {}: {}", toUserId, e.getMessage());
            throw new RuntimeException("Failed to send message: " + e.getMessage());
        }
    }

    /**
     * Thông báo cho user có tin nhắn mới
     * (Đây là nơi bạn có thể integrate WebSocket để push real-time)
     */
    private void notifyUserNewMessage(String userId, MessageResponse message) {
        log.info("🔔 NOTIFICATION: User {} has new message from {} - '{}'", 
                userId, message.getFrom(), message.getContent());
        
        // TODO: Implement WebSocket notification here
        // example: webSocketService.sendToUser(userId, message);
    }

    /**
     * Lấy inbox của user
     */
    public List<MessageResponse> getUserInbox(String userId) {
        return inboxMap.getOrDefault(userId, new ArrayList<>());
    }

    /**
     * Lấy các tin nhắn giữa 2 user (conversation)
     */
    public List<MessageResponse> getConversation(String userId1, String userId2) {
        List<MessageResponse> conversation = new ArrayList<>();
        
        // Lấy messages mà user1 gửi cho user2
        conversation.addAll(
            getUserInbox(userId2).stream()
                .filter(m -> m.getFrom().equals(userId1))
                .toList()
        );

        // Lấy messages mà user2 gửi cho user1
        conversation.addAll(
            getUserInbox(userId1).stream()
                .filter(m -> m.getFrom().equals(userId2))
                .toList()
        );

        return conversation;
    }

    /**
     * Đánh dấu message là đã đọc
     */
    public void markAsRead(String userId, String messageId) {
        inboxMap.getOrDefault(userId, new ArrayList<>()).stream()
                .filter(m -> m.getId().equals(messageId))
                .forEach(m -> m.setIsRead(true));
    }

    /**
     * Xóa message
     */
    public void deleteMessage(String userId, String messageId) {
        List<MessageResponse> inbox = inboxMap.getOrDefault(userId, new ArrayList<>());
        inbox.removeIf(m -> m.getId().equals(messageId));
    }
}
