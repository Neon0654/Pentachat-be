package com.hdtpt.pentachat.message;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA Repository for Message entity
 * Provides database access methods for messages
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, String> {

    /**
     * Find all messages sent to a specific user
     */
    List<Message> findByToUserId(String toUserId);

    /**
     * Find all messages sent from a specific user
     */
    List<Message> findByFromUserId(String fromUserId);

    /**
     * Find all messages between two users (conversation)
     * This will find messages where:
     * - fromUserId = userId1 AND toUserId = userId2
     * - OR fromUserId = userId2 AND toUserId = userId1
     */
    List<Message> findByFromUserIdAndToUserIdOrToUserIdAndFromUserId(
            String fromUserId1, String toUserId1,
            String toUserId2, String fromUserId2);
}
