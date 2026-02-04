package com.hdtpt.pentachat.users.service;

import com.hdtpt.pentachat.users.model.User;
import com.hdtpt.pentachat.users.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    public List<User> searchUsers(String query) {
    return userRepository.findByUsernameContainingIgnoreCase(query);
}
}
