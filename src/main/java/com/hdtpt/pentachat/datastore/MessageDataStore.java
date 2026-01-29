package com.hdtpt.pentachat.datastore;

import java.util.ArrayList;
import java.util.List;

import com.hdtpt.pentachat.message.Message;

/**
 * Mock data store for messages
 * Stores messages in memory
 */
public class MessageDataStore {
    private final List<Message> messages = new ArrayList<>();

    /**
     * Add a new message
     */
    public void addMessage(Message message) {
        messages.add(message);
    }

    /**
     * Find messages by toUserId
     */
    public List<Message> findMessagesByToUserId(String toUserId) {
        return messages.stream()
                .filter(m -> m.getToUserId().equals(toUserId))
                .toList();
    }

    /**
     * Find messages between two users
     */
    public List<Message> findMessagesBetweenUsers(String userId1, String userId2) {
        return messages.stream()
                .filter(m -> (m.getFromUserId().equals(userId1) && m.getToUserId().equals(userId2)) ||
                            (m.getFromUserId().equals(userId2) && m.getToUserId().equals(userId1)))
                .toList();
    }

    /**
     * Get all messages
     */
    public List<Message> getAllMessages() {
        return new ArrayList<>(messages);
    }
}
