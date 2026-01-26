package com.hdtpt.pentachat.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.hdtpt.pentachat.model.Transaction;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, String> {
    List<Transaction> findByFromUserIdOrToUserId(String fromUserId, String toUserId);
}

