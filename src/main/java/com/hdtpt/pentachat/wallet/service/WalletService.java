package com.hdtpt.pentachat.wallet.service;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.hdtpt.pentachat.dataaccess.DataApi;
import com.hdtpt.pentachat.exception.AppException;
import com.hdtpt.pentachat.transaction.model.Transaction;
import com.hdtpt.pentachat.users.model.User;
import com.hdtpt.pentachat.wallet.model.Wallet;

import java.util.List;

/**
 * Wallet service
 * Handles wallet operations: deposit, withdraw, transfer, balance inquiry
 * 
 * Business logic layer - does NOT depend directly on mock data
 * Depends on DataApi interface for data access
 * Enforces business rules (e.g., sufficient balance checks)
 */
@Service
public class WalletService {
    private final DataApi dataApi;

    public WalletService(DataApi dataApi) {
        this.dataApi = dataApi;
    }

    /**
     * Get wallet balance for a user
     * @param userId user's ID
     * @return wallet with balance
     * @throws AppException if user or wallet not found
     */
    public Wallet getBalance(String userId) {
        // User user = dataApi.findUserById(userId);
        // if (user == null) {
        //     throw new AppException("User not found");
        // }

        Wallet wallet = dataApi.getWalletByUserId(userId);
        if (wallet == null) {
            throw new AppException("Wallet not found");
        }

        return wallet;
    }

    /**
     * Deposit money into user's wallet
     * @param userId user's ID
     * @param amount amount to deposit
     * @return updated wallet
     * @throws AppException if user/wallet not found or amount is invalid
     */
    public Wallet deposit(String userId, Double amount) {
        // Validate input
        if (amount == null || amount <= 0) {
            throw new AppException("Deposit amount must be greater than 0");
        }

        // Get current wallet
        Wallet wallet = getBalance(userId);

        // Update balance
        Double newBalance = wallet.getBalance() + amount;
        dataApi.updateWalletBalance(userId, newBalance);

        // Record transaction
        dataApi.createTransaction(
                Transaction.TransactionType.DEPOSIT,
                userId,
                null,
                amount
        );

        // Return updated wallet
        return dataApi.getWalletByUserId(userId);
    }

    /**
     * Withdraw money from user's wallet
     * @param userId user's ID
     * @param amount amount to withdraw
     * @return updated wallet
     * @throws AppException if user/wallet not found, amount invalid, or insufficient balance
     */
    public Wallet withdraw(String userId, Double amount) {
        // Validate input
        if (amount == null || amount <= 0) {
            throw new AppException("Withdrawal amount must be greater than 0");
        }

        // Get current wallet
        Wallet wallet = getBalance(userId);

        // Check sufficient balance
        if (wallet.getBalance() < amount) {
            throw new AppException("Insufficient balance. Current balance: " + wallet.getBalance());
        }

        // Update balance
        Double newBalance = wallet.getBalance() - amount;
        dataApi.updateWalletBalance(userId, newBalance);

        // Record transaction
        dataApi.createTransaction(
                Transaction.TransactionType.WITHDRAW,
                userId,
                null,
                amount
        );

        // Return updated wallet
        return dataApi.getWalletByUserId(userId);
    }

    /**
     * Transfer money from one user to another
     * @param fromUserId sender's ID
     * @param toUsername recipient's username
     * @param amount amount to transfer
     * @return updated wallet of sender
     * @throws AppException if users not found, amount invalid, or insufficient balance
     */

    @Transactional
    public Wallet transfer(String fromUserId, String toUsername, Double amount) {
        // Validate input
        if (amount == null || amount <= 0) {
            throw new AppException("Transfer amount must be greater than 0");
        }

        // Get sender's wallet and check balance
        Wallet fromWallet = getBalance(fromUserId);
        if (fromWallet.getBalance() < amount) {
            throw new AppException("Insufficient balance. Current balance: " + fromWallet.getBalance());
        }

        // Get recipient
        User toUser = dataApi.findUserByUsername(toUsername);
        // if (toUser == null) {
        //     throw new AppException("Recipient not found");
        // }

        // Check recipient is not the same as sender
        if (fromUserId.equals(toUser.getId())) {
            throw new AppException("Cannot transfer to yourself");
        }

        // Get recipient's wallet
        Wallet toWallet = dataApi.getWalletByUserId(toUser.getId());
        if (toWallet == null) {
            throw new AppException("Recipient wallet not found");
        }

        // Deduct from sender
        dataApi.updateWalletBalance(fromUserId, fromWallet.getBalance() - amount);

        // Add to recipient
        dataApi.updateWalletBalance(toUser.getId(), toWallet.getBalance() + amount);

        // Record transaction
        dataApi.createTransaction(
                Transaction.TransactionType.TRANSFER,
                fromUserId,
                toUser.getId(),
                amount
        );

        // Return updated sender's wallet
        return dataApi.getWalletByUserId(fromUserId);
    }

    /**
     * Get transaction history for a user
     * @param userId user's ID
     * @return list of transactions
     * @throws AppException if user not found
     */
    public List<Transaction> getTransactionHistory(String userId) {
        // User user = dataApi.findUserById(userId);
        // if (user == null) {
        //     throw new AppException("User not found");
        // }

        return dataApi.getTransactionsByUserId(userId);
    }
}
