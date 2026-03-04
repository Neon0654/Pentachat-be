package com.hdtpt.pentachat.identity.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional; // Import Transactional

import com.hdtpt.pentachat.identity.dto.request.LoginRequest;
import com.hdtpt.pentachat.identity.dto.request.RegisterRequest;
import com.hdtpt.pentachat.identity.dto.request.ChangePasswordRequest;
import com.hdtpt.pentachat.identity.dto.response.AuthResponse;
import com.hdtpt.pentachat.identity.service.AuthService;
import com.hdtpt.pentachat.dto.response.ApiResponse;
import com.hdtpt.pentachat.identity.model.User;
import com.hdtpt.pentachat.identity.model.PasswordResetToken;
import com.hdtpt.pentachat.identity.repository.UserRepository;
import com.hdtpt.pentachat.identity.service.EmailService;
import com.hdtpt.pentachat.exception.AppException;
import com.hdtpt.pentachat.identity.repository.PasswordResetTokenRepository;
import com.hdtpt.pentachat.security.SessionManager;

import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import java.time.LocalDateTime;

/**
 * Authentication controller
 * Handles user registration, login, and password reset logic
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;

    public AuthController(
            AuthService authService,
            EmailService emailService,
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository) {
        this.authService = authService;
        this.emailService = emailService;
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
    }

    /**
     * Register a new user
     * POST /auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(request.getUsername(), request.getPassword());

        AuthResponse data = AuthResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .sessionId(null)
                .build();

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("User registered successfully")
                .data(data)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Login user
     * POST /auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = authService.login(request.getUsername(), request.getPassword());

        String sessionId = authService.createSession(user);

        AuthResponse data = AuthResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .sessionId(sessionId)
                .build();

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Login successful")
                .data(data)
                .build();

        return ResponseEntity.ok(response);
    }

    private Long getAuthenticatedUser(Long userId, String sessionId) {
        if (userId == null || sessionId == null) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "User ID and Session ID headers are required.");
        }
        SessionManager.SessionInfo sessionInfo = SessionManager.getUserSession(userId);
        if (sessionInfo == null || !sessionInfo.sessionId.equals(sessionId)) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Invalid session.");
        }
        return userId;
    }

    /**
     * Change password for logged-in user
     * POST /auth/change-password
     */
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse> changePassword(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Session-Id") String sessionId,
            @Valid @RequestBody ChangePasswordRequest request) {
        Long authenticatedUserId = getAuthenticatedUser(userId, sessionId);
        authService.changePassword(authenticatedUserId, request.getCurrentPassword(), request.getNewPassword());

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Password changed successfully")
                .data(null)
                .build();

        return ResponseEntity.ok(response);
    }

    // ==================================================================
    // === TÍNH NĂNG QUÊN MẬT KHẨU (NÂNG CẤP VERSION 2: DÙNG TOKEN) ===
    // ==================================================================

    // 1. Gửi Link Reset Password qua Email
    @PostMapping("/forgot-password")
    @Transactional // Đảm bảo tính toàn vẹn dữ liệu khi xóa token cũ
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String username = request.get("username");

        // Tìm user trong Database
        var userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Không tìm thấy tài khoản!"));
        }
        User user = userOpt.get();

        // Xóa token cũ của user này nếu có (tránh rác DB)
        tokenRepository.deleteByUserId(user.getId());

        // Tạo Token mới (ngẫu nhiên)
        String tokenString = UUID.randomUUID().toString();

        // Lưu Token vào DB
        PasswordResetToken tokenEntity = PasswordResetToken.builder()
                .token(tokenString)
                .userId(user.getId())
                .expiryDate(LocalDateTime.now().plusHours(24))
                .build();
        tokenRepository.save(tokenEntity);

        // Gửi Email chứa Link
        // LƯU Ý: Test trên máy bạn thì điền cứng email của bạn. Khi chạy thật thì dùng
        // user.getEmail()
        String emailNhan = "vothanhdanh1610@gmail.com";

        // Gọi hàm gửi Link (Bạn nhớ cập nhật file EmailService.java nhé)
        emailService.sendResetLink(emailNhan, tokenString);

        return ResponseEntity.ok(Map.of("success", true, "message", "Link đặt lại mật khẩu đã gửi vào Email!"));
    }

    // 2. Xử lý đặt lại mật khẩu mới (Khi người dùng bấm Link và nhập pass mới)
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        String newPassword = request.get("newPassword");

        // Tìm token trong DB
        var tokenOpt = tokenRepository.findByToken(token);
        if (tokenOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Link không hợp lệ hoặc đã bị xóa!"));
        }

        PasswordResetToken resetToken = tokenOpt.get();

        // Kiểm tra xem token có bị hết hạn không
        if (resetToken.isExpired()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Link đã hết hạn! Vui lòng yêu cầu lại."));
        }

        // Token hợp lệ -> Lấy user và đổi mật khẩu
        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "User not found"));
        user.setPassword(newPassword); // Nếu AuthService có mã hóa BCrypt, hãy mã hóa ở đây
        userRepository.save(user);

        // Đổi xong thì xóa token đi (chỉ dùng 1 lần)
        tokenRepository.delete(resetToken);

        return ResponseEntity.ok(Map.of("success", true, "message", "Đổi mật khẩu thành công! Hãy đăng nhập lại."));
    }
}
