package com.hdtpt.pentachat.identity.controller;

import com.hdtpt.pentachat.dto.response.ApiResponse;
import com.hdtpt.pentachat.exception.AppException;
import com.hdtpt.pentachat.security.SessionManager;
import com.hdtpt.pentachat.identity.model.User;
import com.hdtpt.pentachat.identity.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hdtpt.pentachat.identity.dto.response.DashboardStatsResponse;
import com.hdtpt.pentachat.message.repository.MessageRepository;
import com.hdtpt.pentachat.friend.repository.FriendRequestRepository;
import com.hdtpt.pentachat.groups.repository.GroupRepository;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final MessageRepository messageRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final GroupRepository groupRepository;

    public UserController(UserService userService,
            MessageRepository messageRepository,
            FriendRequestRepository friendRequestRepository,
            GroupRepository groupRepository) {
        this.userService = userService;
        this.messageRepository = messageRepository;
        this.friendRequestRepository = friendRequestRepository;
        this.groupRepository = groupRepository;
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

    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse> getDashboardStats(@RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Session-Id") String sessionId) {
        getAuthenticatedUser(userId, sessionId);

        long messageCount = messageRepository.findByToUserId(userId).size();
        long friendCount = friendRequestRepository.findAllFriends(userId).size();
        long groupCount = groupRepository.findByMemberId(userId).size();

        DashboardStatsResponse stats = DashboardStatsResponse.builder()
                .messageCount(messageCount)
                .friendCount(friendCount)
                .groupCount(groupCount)
                .build();

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Dashboard stats retrieved.")
                .data(stats)
                .build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAllUsers(@RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Session-Id") String sessionId) {
        Long authenticatedUserId = getAuthenticatedUser(userId, sessionId);

        List<User> users = userService.getAllUsers()
                .stream()
                .filter(user -> !user.getId().equals(authenticatedUserId))
                .collect(Collectors.toList());

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Users retrieved successfully.")
                .data(users)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse> searchUsers(@RequestParam("q") String query,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Session-Id") String sessionId) {
        getAuthenticatedUser(userId, sessionId); // Xác thực session

        List<User> users = userService.searchUsers(query)
                .stream()
                .filter(user -> !user.getId().equals(userId)) // Loại bỏ chính mình
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Search results retrieved.")
                .data(users)
                .build());
    }

}
