package com.hdtpt.pentachat.friend.controller;

import com.hdtpt.pentachat.dto.response.ApiResponse;
import com.hdtpt.pentachat.friend.dto.FriendRequestDTO;
import com.hdtpt.pentachat.friend.model.FriendRequest;
import com.hdtpt.pentachat.friend.service.FriendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Friend Controller
 * Handles friend request operations
 * 
 * Endpoints:
 * - POST /api/friends/request : Gửi yêu cầu kết bạn
 * - POST /api/friends/accept/{requestId} : Chấp nhận yêu cầu kết bạn
 * - POST /api/friends/reject/{requestId} : Từ chối yêu cầu kết bạn
 * - GET /api/friends/pending/{userId} : Lấy danh sách yêu cầu đang chờ
 * - GET /api/friends/check/{userId1}/{userId2} : Kiểm tra xem có phải bạn bè
 * không
 */
@RestController
@RequestMapping("/api/friends")
@Slf4j
public class FriendController {

    private final FriendService friendService;

    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }

    /**
     * Gửi yêu cầu kết bạn
     * POST /api/friends/request
     * 
     * Input: {"fromUserId": "user1", "toUserId": "user2"}
     */
    @PostMapping("/request")
    public ResponseEntity<ApiResponse> sendFriendRequest(
            @Valid @RequestBody FriendRequestDTO request) {
        try {
            log.info("Sending friend request from {} to {}",
                    request.getFromUserId(), request.getToUserId());

            FriendRequest friendRequest = friendService.sendFriendRequest(
                    request.getFromUserId(),
                    request.getToUserId());

            FriendRequestDTO responseDTO = convertToDTO(friendRequest);

            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("Friend request sent successfully")
                    .data(responseDTO)
                    .build());

        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build());
        } catch (Exception e) {
            log.error("Error sending friend request: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.builder()
                    .success(false)
                    .message("Error sending friend request")
                    .build());
        }
    }

    /**
     * Chấp nhận yêu cầu kết bạn
     * POST /api/friends/accept/{requestId}
     */
    @PostMapping("/accept/{requestId}")
    public ResponseEntity<ApiResponse> acceptFriend(
            @PathVariable Long requestId) {
        try {
            log.info("Accepting friend request: {}", requestId);

            FriendRequest friendRequest = friendService.acceptFriend(requestId);
            FriendRequestDTO responseDTO = convertToDTO(friendRequest);

            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("Friend request accepted successfully")
                    .data(responseDTO)
                    .build());

        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build());
        } catch (Exception e) {
            log.error("Error accepting friend request: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.builder()
                    .success(false)
                    .message("Error accepting friend request")
                    .build());
        }
    }

    /**
     * Từ chối yêu cầu kết bạn
     * POST /api/friends/reject/{requestId}
     */
    @PostMapping("/reject/{requestId}")
    public ResponseEntity<ApiResponse> rejectFriend(
            @PathVariable Long requestId) {
        try {
            log.info("Rejecting friend request: {}", requestId);

            FriendRequest friendRequest = friendService.rejectFriend(requestId);
            FriendRequestDTO responseDTO = convertToDTO(friendRequest);

            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("Friend request rejected successfully")
                    .data(responseDTO)
                    .build());

        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build());
        } catch (Exception e) {
            log.error("Error rejecting friend request: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.builder()
                    .success(false)
                    .message("Error rejecting friend request")
                    .build());
        }
    }

    /**
     * Lấy danh sách yêu cầu kết bạn đang chờ
     * GET /api/friends/pending/{userId}
     */
    @GetMapping("/pending/{userId}")
    public ResponseEntity<ApiResponse> getPendingRequests(
            @PathVariable Long userId) {
        try {
            log.info("Getting pending requests for user: {}", userId);

            List<FriendRequest> requests = friendService.getPendingRequests(userId);
            List<FriendRequestDTO> responseDTOs = requests.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("Pending requests retrieved successfully")
                    .data(responseDTOs)
                    .build());

        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build());
        } catch (Exception e) {
            log.error("Error getting pending requests: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.builder()
                    .success(false)
                    .message("Error getting pending requests")
                    .build());
        }
    }

    /**
     * Kiểm tra xem hai user có phải bạn bè không
     * GET /api/friends/check/{userId1}/{userId2}
     */
    @GetMapping("/check/{userId1}/{userId2}")
    public ResponseEntity<ApiResponse> checkFriendship(
            @PathVariable Long userId1,
            @PathVariable Long userId2) {
        try {
            log.info("Checking friendship between {} and {}", userId1, userId2);

            boolean areFriends = friendService.areFriends(userId1, userId2);

            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message("Friendship status retrieved successfully")
                    .data(areFriends)
                    .build());

        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.builder()
                    .success(false)
                    .message(e.getMessage())
                    .build());
        } catch (Exception e) {
            log.error("Error checking friendship: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(ApiResponse.builder()
                    .success(false)
                    .message("Error checking friendship")
                    .build());
        }
    }

    /**
     * Convert FriendRequest entity to DTO
     */
    private FriendRequestDTO convertToDTO(FriendRequest friendRequest) {
        return FriendRequestDTO.builder()
                .id(friendRequest.getId())
                .fromUserId(friendRequest.getFromUserId())
                .toUserId(friendRequest.getToUserId())
                .status(friendRequest.getStatus())
                .createdAt(friendRequest.getCreatedAt())
                .updatedAt(friendRequest.getUpdatedAt())
                .build();
    }
}
