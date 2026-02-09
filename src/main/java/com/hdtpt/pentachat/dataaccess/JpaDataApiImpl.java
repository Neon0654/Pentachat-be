package com.hdtpt.pentachat.dataaccess;

import org.springframework.context.annotation.Primary;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hdtpt.pentachat.groups.model.Group;
import com.hdtpt.pentachat.groups.repository.GroupRepository;
import com.hdtpt.pentachat.message.model.Message;
import com.hdtpt.pentachat.message.repository.MessageRepository;
import com.hdtpt.pentachat.transaction.model.Transaction;
import com.hdtpt.pentachat.transaction.repository.TransactionRepository;
import com.hdtpt.pentachat.users.model.User;
import com.hdtpt.pentachat.users.repository.UserRepository;
import com.hdtpt.pentachat.util.IdGenerator;
import com.hdtpt.pentachat.wallet.model.Wallet;
import com.hdtpt.pentachat.wallet.repository.WalletRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * JPA implementation of DataApi
 * Uses Spring Data JPA repositories for database operations
 * 
 * @deprecated This class is being phased out in favor of domain-specific
 *             services.
 *             New code should use the following services instead:
 *             - {@link com.hdtpt.pentachat.users.service.UserService} for user
 *             operations
 *             - {@link com.hdtpt.pentachat.groups.service.GroupService} for
 *             group operations
 *             - {@link com.hdtpt.pentachat.wallet.service.WalletService} for
 *             wallet operations
 *             -
 *             {@link com.hdtpt.pentachat.transaction.service.TransactionService}
 *             for transaction operations
 *             - {@link com.hdtpt.pentachat.message.service.MessageService} for
 *             message operations
 * 
 *             This class is maintained for backward compatibility with existing
 *             code.
 */
@Repository
@Primary // Spring sẽ ưu tiên dùng DB này thay vì Mock khi chạy ứng dụng
@Deprecated
public class JpaDataApiImpl implements DataApi {

    private final UserRepository userRepo;
    private final WalletRepository walletRepo;
    private final TransactionRepository txRepo;
    private final MessageRepository messageRepo;
    private final GroupRepository groupRepo; // Thêm GroupRepository

    public JpaDataApiImpl(
            UserRepository userRepo,
            WalletRepository walletRepo,
            TransactionRepository txRepo,
            MessageRepository messageRepo,
            GroupRepository groupRepo) { // Inject GroupRepository vào đây
        this.userRepo = userRepo;
        this.walletRepo = walletRepo;
        this.txRepo = txRepo;
        this.messageRepo = messageRepo;
        this.groupRepo = groupRepo;
    }

    // ================= USER =================

    @Override
    public @NonNull User createUser(String username, String password) {
        LocalDateTime now = LocalDateTime.now();
        User user = User.builder()
                .id(IdGenerator.generateId())
                .username(username)
                .password(password)
                .createdAt(now)
                .updatedAt(now)
                .build();

        return Objects.requireNonNull(userRepo.save(user));
    }

    @Override
    public User findUserByUsername(String username) {
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    @Override
    public User findUserById(String userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User ID not found: " + userId));
    }

    @Override
    public boolean userExists(String username) {
        return userRepo.findByUsername(username).isPresent();
    }

    @Override
    public List<User> searchUsers(String query) {
        return userRepo.findByUsernameContainingIgnoreCase(query);
    }

    // ================= GROUP (PHẦN MỚI BỔ SUNG) =================

    @Override
    @Transactional
    public Group saveGroup(Group group) {
        return groupRepo.save(group);
    }

    @Override
    public Group findGroupById(String groupId) {
        return groupRepo.findById(groupId).orElse(null);
    }

    @Override
    public List<Group> findGroupsByUserId(String userId) {
        // Gọi hàm findByMemberId đã định nghĩa trong GroupRepository
        return groupRepo.findByMemberId(userId);
    }

    // ================= WALLET =================

    @Override
    public Wallet getWalletByUserId(String userId) {
        return walletRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found for user: " + userId));
    }

    @Override
    @Transactional
    public void updateWalletBalance(String userId, Double newBalance) {
        Wallet wallet = getWalletByUserId(userId);
        wallet.setBalance(newBalance);
    }

    @Override
    @Transactional
    public void createWallet(String userId, Double initialBalance) {
        if (walletRepo.existsById(userId)) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        Wallet wallet = Wallet.builder()
                .userId(userId)
                .balance(initialBalance)
                .createdAt(now)
                .updatedAt(now)
                .build();

        walletRepo.save(wallet);
    }

    // ================= TRANSACTION =================

    @Override
    @Transactional
    public Transaction createTransaction(
            Transaction.TransactionType type,
            String fromUserId,
            String toUserId,
            Double amount) {
        LocalDateTime now = LocalDateTime.now();
        Transaction tx = Transaction.builder()
                .id(IdGenerator.generateId())
                .type(type)
                .fromUserId(fromUserId)
                .toUserId(toUserId)
                .amount(amount)
                .createdAt(now)
                .updatedAt(now)
                .build();

        return txRepo.save(tx);
    }

    @Override
    public List<Transaction> getTransactionsByUserId(String userId) {
        return txRepo.findByFromUserIdOrToUserId(userId, userId);
    }

    // ================= MESSAGE =================

    @Override
    @Transactional
    public Message createMessage(String fromUserId, String toUserId, String content) {
        LocalDateTime now = LocalDateTime.now();
        Message message = Message.builder()
                .id(IdGenerator.generateId())
                .fromUserId(fromUserId)
                .toUserId(toUserId)
                .content(content)
                .createdAt(now)
                .updatedAt(now)
                .isRead(false)
                .build();

        return messageRepo.save(message);
    }

    @Override
    public List<Message> getMessagesByToUserId(String toUserId) {
        return messageRepo.findByToUserId(toUserId);
    }

    @Override
    public List<Message> getConversationBetweenUsers(String userId1, String userId2) {
        return messageRepo.findByFromUserIdAndToUserIdOrToUserIdAndFromUserId(
                userId1, userId2,
                userId1, userId2);
    }

    @Override
    @Transactional
    public void markMessageAsRead(String messageId) {
        Message message = messageRepo.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found: " + messageId));
        message.setIsRead(true);
    }

    @Override
    @Transactional
    public void deleteMessage(String messageId) {
        messageRepo.deleteById(messageId);
    }

    // ================= GROUP MESSAGE =================

    @Override
    @Transactional
    public Message createGroupMessage(String fromUserId, String groupId, String content) {
        LocalDateTime now = LocalDateTime.now();
        Message message = Message.builder()
                .id(IdGenerator.generateId())
                .fromUserId(fromUserId)
                .type(Message.MessageType.GROUP)
                .targetId(groupId)
                .content(content)
                .createdAt(now)
                .updatedAt(now)
                .isRead(false)
                .build();

        return messageRepo.save(message);
    }

    @Override
    public List<Message> getGroupHistory(String groupId) {
        return messageRepo.findGroupHistory(groupId);
    }

    @Override
    public List<Message> getMessagesByTargetIdAndType(String targetId, String type) {
        try {
            Message.MessageType messageType = Message.MessageType.valueOf(type.toUpperCase());
            return messageRepo.findByTargetIdAndType(targetId, messageType);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid message type: " + type);
        }
    }
}
