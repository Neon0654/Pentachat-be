package com.hdtpt.pentachat.dataaccess;

import com.hdtpt.pentachat.groups.model.Group; // Thêm import này
import com.hdtpt.pentachat.message.model.Message;
import com.hdtpt.pentachat.transaction.model.Transaction;
import com.hdtpt.pentachat.users.model.User;
import com.hdtpt.pentachat.wallet.model.Wallet;

import java.util.List;

/**
 * Data Access abstraction layer
 * * Interface này định nghĩa các "hợp đồng" truy xuất dữ liệu.
 * Bổ sung thêm các thao tác liên quan đến Nhóm (Group).
 */
public interface DataApi {

    // ============ USER OPERATIONS ============
    User createUser(String username, String password);
    User findUserByUsername(String username);
    User findUserById(String userId);
    boolean userExists(String username);
    List<User> searchUsers(String query); // Hàm tìm kiếm đã thêm trước đó

    // ============ WALLET OPERATIONS ============
    Wallet getWalletByUserId(String userId);
    void updateWalletBalance(String userId, Double newBalance);
    void createWallet(String userId, Double initialBalance);

    // ============ TRANSACTION OPERATIONS ============
    Transaction createTransaction(
            Transaction.TransactionType type,
            String fromUserId,
            String toUserId,
            Double amount);
    List<Transaction> getTransactionsByUserId(String userId);

    // ============ MESSAGE OPERATIONS ============
    Message createMessage(String fromUserId, String toUserId, String content);
    List<Message> getMessagesByToUserId(String toUserId);
    List<Message> getConversationBetweenUsers(String userId1, String userId2);
    void markMessageAsRead(String messageId);
    void deleteMessage(String messageId);

    // ============ GROUP OPERATIONS (PHẦN MỚI THÊM) ============

    /**
     * Lưu thông tin nhóm mới hoặc cập nhật nhóm cũ
     */
    Group saveGroup(Group group);

    /**
     * Tìm nhóm theo ID
     */
    Group findGroupById(String groupId);

    /**
     * Tìm tất cả các nhóm mà một User tham gia (dựa trên memberIds)
     */
    List<Group> findGroupsByUserId(String userId);
}