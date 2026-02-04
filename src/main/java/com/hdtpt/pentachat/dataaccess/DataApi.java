package com.hdtpt.pentachat.dataaccess;

import com.hdtpt.pentachat.message.model.Message;
import com.hdtpt.pentachat.transaction.model.Transaction;
import com.hdtpt.pentachat.users.model.User;
import com.hdtpt.pentachat.wallet.model.Wallet;

import java.util.List;

/**
 * Data Access abstraction layer
 * 
 * This interface defines the contract for accessing data
 * Controllers and Services MUST interact ONLY with this interface
 * 
 * Implementations:
 * - MockDataApiImpl: Uses MockDataStore (for testing/development)
 * - JpaDataApiImpl: Uses JPA/Hibernate (for production, to be implemented
 * later)
 * 
 * By depending on this interface, switching from mock data to real database
 * requires ONLY creating a new implementation without touching
 * Controller/Service layers
 */
public interface DataApi {

    // ============ USER OPERATIONS ============

    /**
     * Create a new user
     */
    User createUser(String username, String password);

    /**
     * Find user by username
     */
    User findUserByUsername(String username);

    /**
     * Find user by ID
     */
    User findUserById(String userId);

    /**
     * Check if user exists by username
     */
    boolean userExists(String username);

    // ============ WALLET OPERATIONS ============

    /**
     * Get wallet by user ID
     */
    Wallet getWalletByUserId(String userId);

    /**
     * Update wallet balance
     */
    void updateWalletBalance(String userId, Double newBalance);

    /**
     * Create a wallet for user
     */
    void createWallet(String userId, Double initialBalance);

    // ============ TRANSACTION OPERATIONS ============

    /**
     * Create a transaction
     */
    Transaction createTransaction(
            Transaction.TransactionType type,
            String fromUserId,
            String toUserId,
            Double amount);

    /**
     * Get transactions for a user
     */
    List<Transaction> getTransactionsByUserId(String userId);

    // ============ MESSAGE OPERATIONS ============

    /**
     * Create and save a new message
     */
    Message createMessage(String fromUserId, String toUserId, String content);

    /**
     * Get all messages sent to a user (inbox)
     */
    List<Message> getMessagesByToUserId(String toUserId);

    /**
     * Get conversation between two users
     */
    List<Message> getConversationBetweenUsers(String userId1, String userId2);

    /**
     * Mark a message as read
     */
    void markMessageAsRead(String messageId);

    /**
     * Delete a message by ID
     */
    void deleteMessage(String messageId);

    List<User> searchUsers(String query);
}
