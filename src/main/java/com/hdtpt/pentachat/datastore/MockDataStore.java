package com.hdtpt.pentachat.datastore;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.hdtpt.pentachat.groups.model.Group; // Đảm bảo đã có model Group
import com.hdtpt.pentachat.transaction.model.Transaction;
import com.hdtpt.pentachat.users.model.User;
import com.hdtpt.pentachat.wallet.model.Wallet;

/**
 * In-memory data store for mock data
 * Acts as an in-memory database
 */
public class MockDataStore {
    private final List<User> users = new ArrayList<>();
    private final List<Wallet> wallets = new ArrayList<>();
    private final List<Transaction> transactions = new ArrayList<>();
    private final List<com.hdtpt.pentachat.message.model.Message> messages = new ArrayList<>();
    
    // ============ GROUP DATA ============
    private final List<Group> groups = new ArrayList<>(); // Danh sách lưu trữ nhóm

    // ============ USER OPERATIONS ============
    public void addUser(User user) { users.add(user); }
    public User findUserByUsername(String username) {
        return users.stream().filter(u -> u.getUsername().equals(username)).findFirst().orElse(null);
    }
    public User findUserById(String id) {
        return users.stream().filter(u -> u.getId().equals(id)).findFirst().orElse(null);
    }
    public boolean userExists(String username) {
        return users.stream().anyMatch(u -> u.getUsername().equals(username));
    }
    public List<User> getAllUsers() { return new ArrayList<>(users); }
    public List<User> searchUsers(String query) {
        if (query == null || query.trim().isEmpty()) return List.of(); 
        return users.stream()
                .filter(u -> u.getUsername().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }

    // ============ GROUP OPERATIONS (PHẦN MỚI) ============

    /**
     * Lưu hoặc cập nhật nhóm
     */
    public Group saveGroup(Group group) {
        // Kiểm tra xem nhóm đã tồn tại chưa để cập nhật hoặc thêm mới
        groups.removeIf(g -> g.getId().equals(group.getId()));
        groups.add(group);
        return group;
    }

    /**
     * Tìm nhóm theo ID
     */
    public Group findGroupById(String groupId) {
        return groups.stream()
                .filter(g -> g.getId().equals(groupId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Tìm các nhóm mà User tham gia
     */
    public List<Group> findGroupsByUserId(String userId) {
        return groups.stream()
                .filter(g -> g.getMemberIds() != null && g.getMemberIds().contains(userId))
                .collect(Collectors.toList());
    }

    // ============ WALLET OPERATIONS ============
    public void addWallet(Wallet wallet) { wallets.add(wallet); }
    public Wallet findWalletByUserId(String userId) {
        return wallets.stream().filter(w -> w.getUserId().equals(userId)).findFirst().orElse(null);
    }
    public boolean updateWalletBalance(String userId, Double newBalance) {
        Wallet wallet = findWalletByUserId(userId);
        if (wallet != null) {
            wallet.setBalance(newBalance);
            return true;
        }
        return false;
    }

    // ============ TRANSACTION OPERATIONS ============
    public void addTransaction(Transaction transaction) { transactions.add(transaction); }
    public List<Transaction> findTransactionsByUserId(String userId) {
        return transactions.stream()
                .filter(t -> userId.equals(t.getFromUserId()) || userId.equals(t.getToUserId()))
                .toList();
    }

    // ============ MESSAGE OPERATIONS ============
    public void addMessage(com.hdtpt.pentachat.message.model.Message message) { messages.add(message); }
    public List<com.hdtpt.pentachat.message.model.Message> findMessagesByToUserId(String toUserId) {
        return messages.stream().filter(m -> m.getToUserId().equals(toUserId)).toList();
    }
    public List<com.hdtpt.pentachat.message.model.Message> findMessagesBetweenUsers(String userId1, String userId2) {
        return messages.stream()
                .filter(m -> (m.getFromUserId().equals(userId1) && m.getToUserId().equals(userId2)) ||
                        (m.getFromUserId().equals(userId2) && m.getToUserId().equals(userId1)))
                .toList();
    }
    public void markMessageAsRead(String messageId) {
        messages.stream().filter(m -> m.getId().equals(messageId)).findFirst().ifPresent(m -> m.setIsRead(true));
    }
    public void deleteMessage(String messageId) { messages.removeIf(m -> m.getId().equals(messageId)); }

    /**
     * Find messages by targetId and type
     */
    public List<com.hdtpt.pentachat.message.model.Message> findMessagesByTargetIdAndType(String targetId, String type) {
        try {
            com.hdtpt.pentachat.message.model.Message.MessageType messageType = 
                com.hdtpt.pentachat.message.model.Message.MessageType.valueOf(type.toUpperCase());
            return messages.stream()
                    .filter(m -> m.getTargetId() != null && 
                                m.getTargetId().equals(targetId) &&
                                m.getType() == messageType)
                    .toList();
        } catch (IllegalArgumentException e) {
            return new ArrayList<>();
        }
    }

    // ============ DATA RESET ============
    public void clearAll() {
        users.clear();
        wallets.clear();
        transactions.clear();
        messages.clear();
        groups.clear(); // Xóa cả nhóm khi reset
    }
<<<<<<< HEAD
    
    public List<User> searchUsers(String query) {
    if (query == null || query.trim().isEmpty()) {
        return List.of(); 
    }
    
    // Quét trực tiếp trên List users
    return users.stream()
            .filter(u -> u.getUsername().toLowerCase().contains(query.toLowerCase()))
            .collect(Collectors.toList());
}
}
=======
}
>>>>>>> origin/create_group
