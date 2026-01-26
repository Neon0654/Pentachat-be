package com.hdtpt.pentachat.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.hdtpt.pentachat.model.Wallet;

public interface WalletRepository extends JpaRepository<Wallet, String> {}
