package com.hdtpt.pentachat.message.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hdtpt.pentachat.message.model.Message;
import com.hdtpt.pentachat.message.model.Message.MessageType;

import java.util.List;

/**
 * JPA Repository for Message entity
 * Provides database access methods for both personal and group messages
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, String> {

    /**
     * Find all messages sent to a specific user (backward compatibility)
     */
    List<Message> findByToUserId(String toUserId);

    /**
     * Find all messages sent from a specific user
     */
    List<Message> findByFromUserId(String fromUserId);

    /**
     * Find all messages between two users (conversation)
     */
    List<Message> findByFromUserIdAndToUserIdOrToUserIdAndFromUserId(
            String fromUserId1, String toUserId1,
            String toUserId2, String fromUserId2);

    /**
     * Find all messages for a specific target (user or group) with type
     */
    @Query("SELECT m FROM Message m WHERE m.targetId = :targetId AND m.type = :type ORDER BY m.createdAt ASC")
    List<Message> findByTargetIdAndType(@Param("targetId") String targetId, @Param("type") MessageType type);

    /**
     * Find all group messages for a specific group ID
     */
    @Query("SELECT m FROM Message m WHERE m.targetId = :groupId AND m.type = 'GROUP' ORDER BY m.createdAt ASC")
    List<Message> findGroupHistory(@Param("groupId") String groupId);

    /**
     * Find all personal messages for a specific user
     */
    @Query("SELECT m FROM Message m WHERE m.targetId = :userId AND m.type = 'PERSONAL' ORDER BY m.createdAt ASC")
    List<Message> findPersonalMessages(@Param("userId") String userId);
}
