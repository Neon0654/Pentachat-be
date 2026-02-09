package com.hdtpt.pentachat.transaction.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hdtpt.pentachat.transaction.model.Transaction;
import com.hdtpt.pentachat.transaction.repository.TransactionRepository;
import com.hdtpt.pentachat.util.IdGenerator;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class for Transaction-related business logic
 * Handles transaction creation and retrieval operations
 */
@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Create a new transaction
     * 
     * @param type       Transaction type (DEPOSIT, WITHDRAW, TRANSFER)
     * @param fromUserId Sender user ID
     * @param toUserId   Recipient user ID (null for DEPOSIT/WITHDRAW)
     * @param amount     Transaction amount
     * @return Created transaction entity
     */
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

        return transactionRepository.save(tx);
    }

    /**
     * Get all transactions for a user (sent or received)
     * 
     * @param userId User ID
     * @return List of transactions
     */
    public List<Transaction> getTransactionsByUserId(String userId) {
        return transactionRepository.findByFromUserIdOrToUserId(userId, userId);
    }
}
