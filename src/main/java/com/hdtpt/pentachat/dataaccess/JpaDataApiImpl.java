package com.hdtpt.pentachat.dataaccess;

import org.springframework.context.annotation.Primary;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hdtpt.pentachat.model.Transaction;
import com.hdtpt.pentachat.model.User;
import com.hdtpt.pentachat.model.Wallet;
import com.hdtpt.pentachat.repository.TransactionRepository;
import com.hdtpt.pentachat.repository.UserRepository;
import com.hdtpt.pentachat.repository.WalletRepository;
import com.hdtpt.pentachat.util.IdGenerator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * JPA implementation of DataApi
 * Uses Spring Data JPA repositories for database operations
 * 
 * This implementation is used for production database access
<<<<<<< HEAD
 */

@Repository
@Primary // Spring sẽ ưu tiên dùng DB thay vì Mock
=======
 * DISABLED for mock data mode
 */

// @Repository - DISABLED: Using MockDataApiImpl for mock data mode
// @Primary - Disabled: Using MockDataApiImpl for mock data mode
>>>>>>> origin/master
public class JpaDataApiImpl implements DataApi {

    private final UserRepository userRepo;
    private final WalletRepository walletRepo;
    private final TransactionRepository txRepo;

    public JpaDataApiImpl(
            UserRepository userRepo,
            WalletRepository walletRepo,
            TransactionRepository txRepo
    ) {
        this.userRepo = userRepo;
        this.walletRepo = walletRepo;
        this.txRepo = txRepo;
    }

    // ================= USER =================

    @Override
    public @NonNull User createUser(String username, String password) {
        User user = User.builder()
                .id(IdGenerator.generateId())
                .username(username)
                .password(password)
                .build();

        return Objects.requireNonNull(userRepo.save(user));
    }

    @Override
    public User findUserByUsername(String username) {
        return userRepo.findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException("User not found: " + username)
                );
    }

    @Override
    public User findUserById(String userId) {
        return userRepo.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User ID not found: " + userId)
                );
    }

    @Override
    public boolean userExists(String username) {
        return userRepo.findByUsername(username).isPresent();
    }

    // ================= WALLET =================

    @Override
    public Wallet getWalletByUserId(String userId) {
        return walletRepo.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("Wallet not found for user: " + userId)
                );
    }

    @Override
    @Transactional
    public void updateWalletBalance(String userId, Double newBalance) {
        Wallet wallet = getWalletByUserId(userId);
        wallet.setBalance(newBalance);
        // Không cần save() – Hibernate tự flush
    }

    @Override
    @Transactional
    public void createWallet(String userId, Double initialBalance) {
        if (walletRepo.existsById(userId)) {
            return; // tránh tạo trùng wallet
        }

        Wallet wallet = Wallet.builder()
                .userId(userId)
                .balance(initialBalance)
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
            Double amount
    ) {
        Transaction tx = Transaction.builder()
                .id(IdGenerator.generateId())
                .type(type)
                .fromUserId(fromUserId)
                .toUserId(toUserId)
                .amount(amount)
                .createdAt(LocalDateTime.now())
                .build();

        return txRepo.save(tx);
    }

    @Override
    public List<Transaction> getTransactionsByUserId(String userId) {
        return txRepo.findByFromUserIdOrToUserId(userId, userId);
    }
}
