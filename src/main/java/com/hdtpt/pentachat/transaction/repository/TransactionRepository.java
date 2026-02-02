package com.hdtpt.pentachat.transaction.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.hdtpt.pentachat.transaction.model.Transaction;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, String> {
    List<Transaction> findByFromUserIdOrToUserId(String fromUserId, String toUserId);
}

