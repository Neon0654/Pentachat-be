package com.hdtpt.pentachat.wallet.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hdtpt.pentachat.dto.response.ApiResponse;
import com.hdtpt.pentachat.transaction.dto.response.TransactionResponse;
import com.hdtpt.pentachat.transaction.model.Transaction;
import com.hdtpt.pentachat.wallet.dto.request.DepositRequest;
import com.hdtpt.pentachat.wallet.dto.request.TransferRequest;
import com.hdtpt.pentachat.wallet.dto.request.WithdrawRequest;
import com.hdtpt.pentachat.wallet.dto.response.BalanceResponse;
import com.hdtpt.pentachat.wallet.model.Wallet;
import com.hdtpt.pentachat.wallet.service.WalletService;

import jakarta.validation.Valid;
import java.util.List;

/**
 * Wallet controller
 * Handles wallet operations: deposit, withdraw, transfer, balance, transactions
 * 
 * Controllers ONLY call Service layer
 * Does NOT access mock data directly
 * Does NOT contain business logic
 * Implements simple authorization: User can only access their own wallet
 */
@RestController
@RequestMapping("/api/wallets")
public class WalletController {
        private final WalletService walletService;

        public WalletController(WalletService walletService) {
                this.walletService = walletService;
        }

        /**
         * Get wallet balance
         * GET /wallet/balance?userId={userId}
         */
        @GetMapping("/balance")
        public ResponseEntity<ApiResponse> getBalance(@RequestParam String userId) {
                Wallet wallet = walletService.getBalance(userId);

                BalanceResponse data = BalanceResponse.builder()
                                .userId(wallet.getUserId())
                                .balance(wallet.getBalance())
                                .build();

                ApiResponse response = ApiResponse.builder()
                                .success(true)
                                .message("Balance retrieved successfully")
                                .data(data)
                                .build();

                return ResponseEntity.ok(response);
        }

        /**
         * Deposit money
         * POST /wallet/deposit
         */
        @PostMapping("/deposit")
        public ResponseEntity<ApiResponse> deposit(
                        @RequestParam String userId,
                        @Valid @RequestBody DepositRequest request) {
                Wallet wallet = walletService.deposit(userId, request.getAmount());

                BalanceResponse data = BalanceResponse.builder()
                                .userId(wallet.getUserId())
                                .balance(wallet.getBalance())
                                .build();

                ApiResponse response = ApiResponse.builder()
                                .success(true)
                                .message("Deposit successful. New balance: " + wallet.getBalance())
                                .data(data)
                                .build();

                return ResponseEntity.ok(response);
        }

        /**
         * Withdraw money
         * POST /wallet/withdraw
         */
        @PostMapping("/withdraw")
        public ResponseEntity<ApiResponse> withdraw(
                        @RequestParam String userId,
                        @Valid @RequestBody WithdrawRequest request) {
                Wallet wallet = walletService.withdraw(userId, request.getAmount());

                BalanceResponse data = BalanceResponse.builder()
                                .userId(wallet.getUserId())
                                .balance(wallet.getBalance())
                                .build();

                ApiResponse response = ApiResponse.builder()
                                .success(true)
                                .message("Withdrawal successful. New balance: " + wallet.getBalance())
                                .data(data)
                                .build();

                return ResponseEntity.ok(response);
        }

        /**
         * Transfer money between users
         * POST /wallet/transfer
         */
        @PostMapping("/transfer")
        public ResponseEntity<ApiResponse> transfer(
                        @RequestParam String fromUserId,
                        @Valid @RequestBody TransferRequest request) {
                Wallet wallet = walletService.transfer(fromUserId, request.getToUsername(), request.getAmount());

                BalanceResponse data = BalanceResponse.builder()
                                .userId(wallet.getUserId())
                                .balance(wallet.getBalance())
                                .build();

                ApiResponse response = ApiResponse.builder()
                                .success(true)
                                .message("Transfer successful. New balance: " + wallet.getBalance())
                                .data(data)
                                .build();

                return ResponseEntity.ok(response);
        }

        /**
         * Get transaction history
         * GET /wallet/transactions?userId={userId}
         */
        @GetMapping("/transactions")
        public ResponseEntity<ApiResponse> getTransactions(@RequestParam String userId) {
                List<Transaction> transactions = walletService.getTransactionHistory(userId);

                List<TransactionResponse> data = transactions.stream()
                                .map(t -> TransactionResponse.builder()
                                                .id(t.getId())
                                                .type(t.getType().toString())
                                                .fromUserId(t.getFromUserId())
                                                .toUserId(t.getToUserId())
                                                .amount(t.getAmount())
                                                .createdAt(t.getCreatedAt())
                                                .build())
                                .toList();

                ApiResponse response = ApiResponse.builder()
                                .success(true)
                                .message("Transactions retrieved successfully")
                                .data(data)
                                .build();

                return ResponseEntity.ok(response);
        }
}
