package com.hdtpt.pentachat.dataaccess;

import org.springframework.stereotype.Component;
import com.hdtpt.pentachat.datastore.MockDataStore;
import com.hdtpt.pentachat.transaction.model.Transaction;
import com.hdtpt.pentachat.users.model.User;
import com.hdtpt.pentachat.util.IdGenerator;
import com.hdtpt.pentachat.wallet.model.Wallet;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Mock implementation of DataApi
 * Uses MockDataStore for in-memory data storage
 * 
 * This implementation is used for development and testing
 * Later, replace with JpaDataApiImpl for production database
 */
@Component
public class MockDataApiImpl implements DataApi {
    private final MockDataStore dataStore;

    public MockDataApiImpl() {
        this.dataStore = new MockDataStore();
        initializeMockData();
    }

    // ============ USER OPERATIONS ============
    @Override
    public User createUser(String username, String password) {
        LocalDateTime now = LocalDateTime.now();
        User user = User.builder()
                .id(IdGenerator.generateId())
                .username(username)
                .password(password)
                .createdAt(now)
                .updatedAt(now)
                .build();
        dataStore.addUser(user);
        return user;
    }

    @Override
    public User findUserByUsername(String username) {
        return dataStore.findUserByUsername(username);
    }

    @Override
    public User findUserById(String userId) {
        return dataStore.findUserById(userId);
    }

    @Override
    public boolean userExists(String username) {
        return dataStore.userExists(username);
    }

    // ============ WALLET OPERATIONS ============
    @Override
    public Wallet getWalletByUserId(String userId) {
        return dataStore.findWalletByUserId(userId);
    }

    @Override
    public void updateWalletBalance(String userId, Double newBalance) {
        dataStore.updateWalletBalance(userId, newBalance);
    }

    @Override
    public void createWallet(String userId, Double initialBalance) {
        LocalDateTime now = LocalDateTime.now();
        Wallet wallet = Wallet.builder()
                .userId(userId)
                .balance(initialBalance)
                .createdAt(now)
                .updatedAt(now)
                .build();
        dataStore.addWallet(wallet);
    }

    // ============ TRANSACTION OPERATIONS ============
    @Override
    public Transaction createTransaction(
            Transaction.TransactionType type,
            String fromUserId,
            String toUserId,
            Double amount) {
        LocalDateTime now = LocalDateTime.now();
        Transaction transaction = Transaction.builder()
                .id(IdGenerator.generateId())
                .type(type)
                .fromUserId(fromUserId)
                .toUserId(toUserId)
                .amount(amount)
                .createdAt(now)
                .updatedAt(now)
                .build();
        dataStore.addTransaction(transaction);
        return transaction;
    }

    @Override
    public List<Transaction> getTransactionsByUserId(String userId) {
        return dataStore.findTransactionsByUserId(userId);
    }

    // ============ MESSAGE OPERATIONS ============

    @Override
    public com.hdtpt.pentachat.message.model.Message createMessage(String fromUserId, String toUserId, String content) {
        LocalDateTime now = LocalDateTime.now();
        com.hdtpt.pentachat.message.model.Message message = com.hdtpt.pentachat.message.model.Message.builder()
                .id(IdGenerator.generateId())
                .fromUserId(fromUserId)
                .toUserId(toUserId)
                .content(content)
                .createdAt(now)
                .updatedAt(now)
                .isRead(false)
                .build();
        dataStore.addMessage(message);
        return message;
    }

    @Override
    public List<com.hdtpt.pentachat.message.model.Message> getMessagesByToUserId(String toUserId) {
        return dataStore.findMessagesByToUserId(toUserId);
    }

    @Override
    public List<com.hdtpt.pentachat.message.model.Message> getConversationBetweenUsers(String userId1, String userId2) {
        return dataStore.findMessagesBetweenUsers(userId1, userId2);
    }

    @Override
    public void markMessageAsRead(String messageId) {
        dataStore.markMessageAsRead(messageId);
    }

    @Override
    public void deleteMessage(String messageId) {
        dataStore.deleteMessage(messageId);
    }

    // ============ INITIALIZATION ============
    /**
     * 
     * Initialize with mock data for development
     * 
     */
    private void initializeMockData() {
        // Create mock users
        User user1 = createUser("alice", "password123");
        User user2 = createUser("bob", "password456");
        // Create wallets with initial balance
        createWallet(user1.getId(), 1000.0);
        createWallet(user2.getId(), 500.0);
        // Create sample transactions
        createTransaction(
                Transaction.TransactionType.DEPOSIT,
                user1.getId(),
                null,
                500.0);
        createTransaction(
                Transaction.TransactionType.DEPOSIT,
                user2.getId(),
                null,
                250.0);
    }
}