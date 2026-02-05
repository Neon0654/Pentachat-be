package com.hdtpt.pentachat.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hdtpt.pentachat.auth.dto.request.LoginRequest;
import com.hdtpt.pentachat.auth.dto.request.RegisterRequest;
import com.hdtpt.pentachat.auth.dto.response.AuthResponse;
import com.hdtpt.pentachat.auth.service.AuthService;
import com.hdtpt.pentachat.dto.response.ApiResponse;
import com.hdtpt.pentachat.users.model.User;

import jakarta.validation.Valid;

/**
 * Authentication controller
 * Handles user registration and login endpoints
 * 
 * Controllers ONLY call Service layer
 * Does NOT access mock data directly
 * Does NOT contain business logic
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
<<<<<<< HEAD

=======
>>>>>>> c99ecabafa9ac82c979d4fa63bf5d7254224336b
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
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
                .sessionId(null) // No session created on registration
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

        // Create session for user
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
}
