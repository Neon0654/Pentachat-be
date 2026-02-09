package com.hdtpt.pentachat.dataaccess;

import org.springframework.stereotype.Component;
import com.hdtpt.pentachat.datastore.MockDataStore;
import com.hdtpt.pentachat.groups.model.Group; // Thêm import này
import com.hdtpt.pentachat.transaction.model.Transaction;
import com.hdtpt.pentachat.users.model.User;
import com.hdtpt.pentachat.util.IdGenerator;
import com.hdtpt.pentachat.wallet.model.Wallet;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mock implementation of DataApi
 * Uses MockDataStore for in-memory data storage
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

    @Override
    public List<User> searchUsers(String query) {
        return dataStore.searchUsers(query);
    }

    // ============ GROUP OPERATIONS (PHẦN MỚI) ============
    @Override
    public Group saveGroup(Group group) {
        // Ủy quyền việc lưu trữ cho dataStore
        return dataStore.saveGroup(group);
    }

    @Override
    public Group findGroupById(String groupId) {
        return dataStore.findGroupById(groupId);
    }

    @Override
    public List<Group> findGroupsByUserId(String userId) {
        return dataStore.findGroupsByUserId(userId);
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

    @Override
    public com.hdtpt.pentachat.message.model.Message createGroupMessage(String fromUserId, String groupId, String content) {
        LocalDateTime now = LocalDateTime.now();
        com.hdtpt.pentachat.message.model.Message message = com.hdtpt.pentachat.message.model.Message.builder()
                .id(IdGenerator.generateId())
                .fromUserId(fromUserId)
                .type(com.hdtpt.pentachat.message.model.Message.MessageType.GROUP)
                .targetId(groupId)
                .content(content)
                .createdAt(now)
                .updatedAt(now)
                .isRead(false)
                .build();
        dataStore.addMessage(message);
        return message;
    }

    @Override
    public List<com.hdtpt.pentachat.message.model.Message> getGroupHistory(String groupId) {
        return dataStore.findMessagesByTargetIdAndType(groupId, "GROUP");
    }

    @Override
    public List<com.hdtpt.pentachat.message.model.Message> getMessagesByTargetIdAndType(String targetId, String type) {
        return dataStore.findMessagesByTargetIdAndType(targetId, type);
    }

    // ============ INITIALIZATION ============
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

    // ============ SEARCH OPERATIONS ============
    @Override
    public List<User> searchUsers(String query) {
        // Gọi hàm searchUsers từ dataStore thay vì tự xử lý
        return dataStore.searchUsers(query);
    }
}