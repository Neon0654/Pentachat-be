package com.hdtpt.pentachat.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hdtpt.pentachat.dto.request.LoginRequest;
import com.hdtpt.pentachat.dto.request.RegisterRequest;
import com.hdtpt.pentachat.dto.response.ApiResponse;
import com.hdtpt.pentachat.dto.response.AuthResponse;
import com.hdtpt.pentachat.model.User;
import com.hdtpt.pentachat.service.AuthService;

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
@RequestMapping("/auth")
public class AuthController {
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
                .message("Registration successful")
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

        AuthResponse data = AuthResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .message("Login successful")
                .build();

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Login successful")
                .data(data)
                .build();

        return ResponseEntity.ok(response);
    }
}
