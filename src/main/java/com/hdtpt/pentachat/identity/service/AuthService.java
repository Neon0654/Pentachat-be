package com.hdtpt.pentachat.identity.service;

import org.springframework.stereotype.Service;

import com.hdtpt.pentachat.identity.repository.UserRepository;
import com.hdtpt.pentachat.finance.service.WalletService;
import com.hdtpt.pentachat.exception.AppException;
import com.hdtpt.pentachat.security.SessionManager;
import com.hdtpt.pentachat.identity.model.User;

/**
 * Authentication service
 * Handles user registration and login
 * 
 * Business logic layer - does NOT depend directly on mock data
 * Depends on DataApi interface for data access
 */
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final ProfileService profileService;
    private final WalletService walletService;

    public AuthService(UserRepository userRepository,
            ProfileService profileService,
            WalletService walletService) {
        this.userRepository = userRepository;
        this.profileService = profileService;
        this.walletService = walletService;
    }

    /**
     * Register a new user
     * 
     * @param username user's username
     * @param password user's password
     * @return registered user
     * @throws AppException if username already exists
     */
    public User register(String username, String password) {
        // Validate input
        if (username == null || username.trim().isEmpty()) {
            throw new AppException("Username cannot be empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new AppException("Password cannot be empty");
        }

        // Check if user already exists
        if (userRepository.existsByUsername(username)) {
            throw new AppException("Username already exists");
        }

        // Create user
        User newUser = User.builder()
                .username(username)
                .password(password)
                .build();
        newUser = userRepository.save(newUser);

        // Create wallet and profile for new user
        profileService.createProfile(newUser.getId());
        walletService.createWallet(newUser.getId(), 0.0);

        return newUser;
    }

    /**
     * Login user
     * 
     * @param username user's username
     * @param password user's password
     * @return user if credentials are correct
     * @throws AppException if user not found or password is incorrect
     */
    public User login(String username, String password) {
        // Validate input
        if (username == null || username.trim().isEmpty()) {
            throw new AppException("Username cannot be empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new AppException("Password cannot be empty");
        }

        // Find user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException("Invalid username or password"));

        if (!user.getPassword().equals(password)) {
            throw new AppException("Invalid username or password");
        }

        return user;
    }

    /**
     * Create session for user
     * 
     * @param user logged-in user
     * @return session ID
     */
    public String createSession(User user) {
        return SessionManager.createSession(user.getId(), user.getUsername());
    }
}
