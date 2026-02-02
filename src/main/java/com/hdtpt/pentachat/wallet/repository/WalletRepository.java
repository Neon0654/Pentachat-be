package com.hdtpt.pentachat.wallet.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.hdtpt.pentachat.wallet.model.Wallet;

public interface WalletRepository extends JpaRepository<Wallet, String> {}
