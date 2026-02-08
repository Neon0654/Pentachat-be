package com.hdtpt.pentachat.message.service;

import com.hdtpt.pentachat.dataaccess.MockDataApiImpl;
import com.hdtpt.pentachat.message.dto.response.MessageResponse;
import com.hdtpt.pentachat.message.model.Message;
import com.hdtpt.pentachat.message.model.Message.MessageType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Message Service - Group Messaging Tests")
public class MessageServiceGroupTest {

    private MessageService messageService;
    private MockDataApiImpl dataApi;

    @BeforeEach
    void setUp() {
        dataApi = new MockDataApiImpl();
        messageService = new MessageService(dataApi);
    }

    @Test
    @DisplayName("Should send message to group and save to database")
    void testPushToGroup_Success() {
        // Arrange
        String fromUserId = "user1";
        String groupId = "group123";
        String content = "Hello everyone!";

        // Act
        MessageResponse response = messageService.pushToGroup(fromUserId, groupId, content);

        // Assert
        assertNotNull(response);
        assertEquals(fromUserId, response.getFrom());
        assertEquals(groupId, response.getTargetId());
        assertEquals("GROUP", response.getType());
        assertEquals(content, response.getContent());
        assertFalse(response.getIsRead());
        assertNotNull(response.getId());
        assertNotNull(response.getCreatedAt());
    }

    @Test
    @DisplayName("Should throw exception when fromUserId is empty")
    void testPushToGroup_EmptyFromUserId() {
        // Arrange
        String fromUserId = "";
        String groupId = "group123";
        String content = "Hello";

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            messageService.pushToGroup(fromUserId, groupId, content)
        );
    }

    @Test
    @DisplayName("Should throw exception when groupId is empty")
    void testPushToGroup_EmptyGroupId() {
        // Arrange
        String fromUserId = "user1";
        String groupId = "";
        String content = "Hello";

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            messageService.pushToGroup(fromUserId, groupId, content)
        );
    }

    @Test
    @DisplayName("Should throw exception when content is empty")
    void testPushToGroup_EmptyContent() {
        // Arrange
        String fromUserId = "user1";
        String groupId = "group123";
        String content = "";

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            messageService.pushToGroup(fromUserId, groupId, content)
        );
    }

    @Test
    @DisplayName("Should retrieve group history with multiple messages")
    void testGetGroupHistory_MultipleMessages() {
        // Arrange
        String fromUserId1 = "user1";
        String fromUserId2 = "user2";
        String groupId = "group123";

        messageService.pushToGroup(fromUserId1, groupId, "First message");
        messageService.pushToGroup(fromUserId2, groupId, "Second message");
        messageService.pushToGroup(fromUserId1, groupId, "Third message");

        // Act
        List<MessageResponse> history = messageService.getGroupHistory(groupId);

        // Assert
        assertEquals(3, history.size());
        assertEquals("First message", history.get(0).getContent());
        assertEquals("Second message", history.get(1).getContent());
        assertEquals("Third message", history.get(2).getContent());
        assertTrue(history.stream().allMatch(m -> "GROUP".equals(m.getType())));
        assertTrue(history.stream().allMatch(m -> groupId.equals(m.getTargetId())));
    }

    @Test
    @DisplayName("Should return empty list for non-existent group")
    void testGetGroupHistory_EmptyForNonExistentGroup() {
        // Arrange
        String nonExistentGroupId = "non-existent-group";

        // Act
        List<MessageResponse> history = messageService.getGroupHistory(nonExistentGroupId);

        // Assert
        assertNotNull(history);
        assertEquals(0, history.size());
    }

    @Test
    @DisplayName("Should retrieve group history ordered by creation time")
    void testGetGroupHistory_OrderedByTime() throws InterruptedException {
        // Arrange
        String groupId = "group123";
        
        messageService.pushToGroup("user1", groupId, "Message 1");
        Thread.sleep(10); // Small delay to ensure different timestamps
        messageService.pushToGroup("user2", groupId, "Message 2");
        Thread.sleep(10);
        messageService.pushToGroup("user3", groupId, "Message 3");

        // Act
        List<MessageResponse> history = messageService.getGroupHistory(groupId);

        // Assert
        assertEquals(3, history.size());
        assertTrue(history.get(0).getCreatedAt().isBefore(history.get(1).getCreatedAt()));
        assertTrue(history.get(1).getCreatedAt().isBefore(history.get(2).getCreatedAt()));
    }

    @Test
    @DisplayName("Should separate group messages from personal messages")
    void testMessageTypeSeparation() {
        // Arrange
        String groupId = "group123";
        String userId1 = "user1";
        String userId2 = "user2";

        // Send group message
        messageService.pushToGroup(userId1, groupId, "Group message");

        // Act
        List<MessageResponse> groupHistory = messageService.getGroupHistory(groupId);

        // Assert
        assertEquals(1, groupHistory.size());
        assertEquals("GROUP", groupHistory.get(0).getType());
        assertEquals(groupId, groupHistory.get(0).getTargetId());
    }

    @Test
    @DisplayName("Should throw exception when groupId is null")
    void testGetGroupHistory_NullGroupId() {
        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            messageService.getGroupHistory(null)
        );
    }

    @Test
    @DisplayName("Should save multiple groups independently")
    void testMultipleGroups() {
        // Arrange
        String group1 = "group1";
        String group2 = "group2";
        String userId = "user1";

        messageService.pushToGroup(userId, group1, "M1");
        messageService.pushToGroup(userId, group1, "M2");
        messageService.pushToGroup(userId, group2, "M3");
        messageService.pushToGroup(userId, group2, "M4");
        messageService.pushToGroup(userId, group2, "M5");

        // Act
        List<MessageResponse> history1 = messageService.getGroupHistory(group1);
        List<MessageResponse> history2 = messageService.getGroupHistory(group2);

        // Assert
        assertEquals(2, history1.size());
        assertEquals(3, history2.size());
        assertTrue(history1.stream().allMatch(m -> group1.equals(m.getTargetId())));
        assertTrue(history2.stream().allMatch(m -> group2.equals(m.getTargetId())));
    }
}
