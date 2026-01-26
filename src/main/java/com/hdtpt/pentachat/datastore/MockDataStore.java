package com.hdtpt.pentachat.datastore;

import java.util.ArrayList;
import java.util.List;

import com.hdtpt.pentachat.model.Transaction;
import com.hdtpt.pentachat.model.User;
import com.hdtpt.pentachat.model.Wallet;

/**
 * In-memory data store for mock data
 * Acts as an in-memory database
 * 
 * This class stores:
 * - List of Users
 * - List of Wallets
 * - List of Transactions
 * 
 * This is NOT a Spring component - just a plain Java class
 * It uses Java collections only (no database or ORM)
 */
public class MockDataStore {
    private final List<User> users = new ArrayList<>();
    private final List<Wallet> wallets = new ArrayList<>();
    private final List<Transaction> transactions = new ArrayList<>();

    // ============ USER OPERATIONS ============

    /**
     * Add a new user
     */
    public void addUser(User user) {
        users.add(user);
    }

    /**
     * Find user by username
     */
    public User findUserByUsername(String username) {
        return users.stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    /**
     * Find user by ID
     */
    public User findUserById(String id) {
        return users.stream()
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * Check if user exists by username
     */
    public boolean userExists(String username) {
        return users.stream()
                .anyMatch(u -> u.getUsername().equals(username));
    }

    /**
     * Get all users
     */
    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }

    // ============ WALLET OPERATIONS ============

    /**
     * Add a new wallet
     */
    public void addWallet(Wallet wallet) {
        wallets.add(wallet);
    }

    /**
     * Find wallet by user ID
     */
    public Wallet findWalletByUserId(String userId) {
        return wallets.stream()
                .filter(w -> w.getUserId().equals(userId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Update wallet balance
     * Returns true if update was successful
     */
    public boolean updateWalletBalance(String userId, Double newBalance) {
        Wallet wallet = findWalletByUserId(userId);
        if (wallet != null) {
            wallet.setBalance(newBalance);
            return true;
        }
        return false;
    }

    /**
     * Get all wallets
     */
    public List<Wallet> getAllWallets() {
        return new ArrayList<>(wallets);
    }

    // ============ TRANSACTION OPERATIONS ============

    /**
     * Add a new transaction
     */
    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
    }

    /**
     * Find transactions by user ID (both as sender and receiver)
     */
    public List<Transaction> findTransactionsByUserId(String userId) {
        return transactions.stream()
                .filter(t -> userId.equals(t.getFromUserId()) || userId.equals(t.getToUserId()))
                .toList();
    }

    /**
     * Get all transactions
     */
    public List<Transaction> getAllTransactions() {
        return new ArrayList<>(transactions);
    }

    // ============ DATA RESET ============

    /**
     * Clear all data (useful for testing)
     */
    public void clearAll() {
        users.clear();
        wallets.clear();
        transactions.clear();
    }
}
