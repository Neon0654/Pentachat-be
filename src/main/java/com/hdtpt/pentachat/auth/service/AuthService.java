package com.hdtpt.pentachat.auth.service;

import org.springframework.stereotype.Service;

import com.hdtpt.pentachat.dataaccess.DataApi;
import com.hdtpt.pentachat.exception.AppException;
import com.hdtpt.pentachat.security.SessionManager;
import com.hdtpt.pentachat.users.model.User;

/**
 * Authentication service
 * Handles user registration and login
 * 
 * Business logic layer - does NOT depend directly on mock data
 * Depends on DataApi interface for data access
 */
@Service
public class AuthService {
    private final DataApi dataApi;

    public AuthService(DataApi dataApi) {
        this.dataApi = dataApi;
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
        if (dataApi.userExists(username)) {
            throw new AppException("Username already exists");
        }

        // Create user
        User newUser = dataApi.createUser(username, password);

        // Create wallet for new user with initial balance 0
        dataApi.createWallet(newUser.getId(), 0.0);

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
        User user = dataApi.findUserByUsername(username);
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
