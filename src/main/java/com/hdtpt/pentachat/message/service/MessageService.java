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
                .map(m -> {
                    MessageResponse.MessageResponseBuilder builder = MessageResponse.builder()
                            .id(m.getId())
                            .from(m.getFromUserId())
                            .content(m.getContent())
                            .createdAt(m.getCreatedAt())
                            .isRead(m.getIsRead());

                    // Support both personal and group messages
                    if (m.getType() != null) {
                        builder.type(m.getType().toString())
                               .targetId(m.getTargetId());
                    }

                    // For backward compatibility
                    if (m.getToUserId() != null) {
                        builder.to(m.getToUserId());
                    } else if (m.getTargetId() != null && 
                               m.getType() == Message.MessageType.PERSONAL) {
                        builder.to(m.getTargetId());
                    }

                    return builder.build();
                })
                .toList();
    }

    /**
     * Gửi tin nhắn nhóm
     * 
     * @param fromUserId User ID của người gửi
     * @param groupId    Group ID của nhóm đích
     * @param content    Nội dung tin nhắn
     * @return MessageResponse - Tin nhắn vừa được gửi
     */
    public MessageResponse pushToGroup(String fromUserId, String groupId, String content) {
        try {
            // Validate
            if (fromUserId == null || fromUserId.isEmpty()) {
                throw new IllegalArgumentException("fromUserId cannot be empty");
            }
            if (groupId == null || groupId.isEmpty()) {
                throw new IllegalArgumentException("groupId cannot be empty");
            }
            if (content == null || content.isEmpty()) {
                throw new IllegalArgumentException("content cannot be empty");
            }

            // Tạo group message trong database
            Message message = dataApi.createGroupMessage(fromUserId, groupId, content);

            // Convert to MessageResponse
            MessageResponse response = MessageResponse.builder()
                    .id(message.getId())
                    .from(message.getFromUserId())
                    .targetId(message.getTargetId())
                    .type(message.getType().toString())
                    .content(message.getContent())
                    .createdAt(message.getCreatedAt())
                    .isRead(message.getIsRead())
                    .build();

            log.info("Group message pushed to group {} from user {}: {}", groupId, fromUserId, content);

            // Notify all group members about new message
            notifyGroupNewMessage(groupId, response);

            return response;

        } catch (Exception e) {
            log.error("Error pushing group message to group {}: {}", groupId, e.getMessage());
            throw new RuntimeException("Failed to send group message: " + e.getMessage());
        }
    }

    /**
     * Lấy lịch sử tin nhắn của một nhóm
     * 
     * @param groupId ID của nhóm
     * @return List<MessageResponse> - Danh sách tin nhắn trong nhóm
     */
    public List<MessageResponse> getGroupHistory(String groupId) {
        try {
            if (groupId == null || groupId.isEmpty()) {
                throw new IllegalArgumentException("groupId cannot be empty");
            }

            List<Message> messages = dataApi.getGroupHistory(groupId);
            log.info("Retrieved {} messages from group {}", messages.size(), groupId);
            
            return convertToResponseList(messages);

        } catch (Exception e) {
            log.error("Error retrieving group history for group {}: {}", groupId, e.getMessage());
            throw new RuntimeException("Failed to retrieve group history: " + e.getMessage());
        }
    }

    /**
     * Thông báo cho tất cả members trong group có tin nhắn mới
     * (Đây là nơi bạn có thể integrate WebSocket để push real-time)
     */
    private void notifyGroupNewMessage(String groupId, MessageResponse message) {
        log.info("🔔 GROUP NOTIFICATION: Group {} has new message from {} - '{}'",
                groupId, message.getFrom(), message.getContent());

        // TODO: Implement WebSocket notification for group here
        // example: webSocketService.sendToGroup(groupId, message);
    }
}
