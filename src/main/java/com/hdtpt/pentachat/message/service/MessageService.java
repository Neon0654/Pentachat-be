package com.hdtpt.pentachat.message.service;

import com.hdtpt.pentachat.dataaccess.DataApi;
import com.hdtpt.pentachat.message.dto.response.MessageResponse;
import com.hdtpt.pentachat.message.model.Message;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

/**
 * Message Service - Xử lý logic gửi/nhận messages
 * 
 * Chứa hàm pushToUser() để gửi tin nhắn cho user đích
 * Lưu messages trong database thông qua DataApi
 */
@Service
@Slf4j
public class MessageService {

    private final DataApi dataApi;

    public MessageService(DataApi dataApi) {
        this.dataApi = dataApi;
    }

    /**
     * Hàm pushToUser - Gửi tin nhắn cho user đích
     * 
     * @param fromUserId User ID của người gửi
     * @param toUserId   User ID của người nhận
     * @param content    Nội dung tin nhắn
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

            // Tạo message mới trong database
            Message message = dataApi.createMessage(fromUserId, toUserId, content);

            // Convert to MessageResponse
            MessageResponse response = MessageResponse.builder()
                    .id(message.getId())
                    .from(message.getFromUserId())
                    .to(message.getToUserId())
                    .content(message.getContent())
                    .createdAt(message.getCreatedAt())
                    .isRead(message.getIsRead())
                    .build();

            log.info("Message pushed to user {} from user {}: {}", toUserId, fromUserId, content);

            // Ở đây bạn có thể thêm logic để gửi push notification
            // hoặc WebSocket event để thông báo cho người nhận
            notifyUserNewMessage(toUserId, response);

            return response;

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
        List<Message> messages = dataApi.getMessagesByToUserId(userId);
        return convertToResponseList(messages);
    }

    /**
     * Lấy các tin nhắn giữa 2 user (conversation)
     */
    public List<MessageResponse> getConversation(String userId1, String userId2) {
        List<Message> messages = dataApi.getConversationBetweenUsers(userId1, userId2);
        return convertToResponseList(messages);
    }

    /**
     * Đánh dấu message là đã đọc
     */
    public void markAsRead(String userId, String messageId) {
        dataApi.markMessageAsRead(messageId);
    }

    /**
     * Xóa message
     */
    public void deleteMessage(String userId, String messageId) {
        dataApi.deleteMessage(messageId);
    }

    /**
     * Helper method: Convert List<Message> to List<MessageResponse>
     */
    private List<MessageResponse> convertToResponseList(List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return new ArrayList<>();
        }

        return messages.stream()
                .map(m -> MessageResponse.builder()
                        .id(m.getId())
                        .from(m.getFromUserId())
                        .to(m.getToUserId())
                        .content(m.getContent())
                        .createdAt(m.getCreatedAt())
                        .isRead(m.getIsRead())
                        .build())
                .toList();
    }
}
