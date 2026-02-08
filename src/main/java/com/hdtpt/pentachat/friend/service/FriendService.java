package com.hdtpt.pentachat.friend.service;

import com.hdtpt.pentachat.friend.model.FriendRequest;
import com.hdtpt.pentachat.friend.repository.FriendRequestRepository;
import com.hdtpt.pentachat.users.model.User;
import com.hdtpt.pentachat.users.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Friend Service - Xử lý logic quản lý quan hệ bạn bè
 * 
 * Chứa các hàm:
 * - sendFriendRequest(fromId, toId): Gửi yêu cầu kết bạn
 * - acceptFriend(requestId): Chấp nhận yêu cầu kết bạn
 * - rejectFriend(requestId): Từ chối yêu cầu kết bạn
 * - getPendingRequests(userId): Lấy danh sách yêu cầu đang chờ
 * - areFriends(userId1, userId2): Kiểm tra xem hai user có phải bạn bè không
 */
@Service
@Slf4j
public class FriendService {

    private final FriendRequestRepository friendRequestRepository;
    private final UserRepository userRepository;

    public FriendService(FriendRequestRepository friendRequestRepository, 
                        UserRepository userRepository) {
        this.friendRequestRepository = friendRequestRepository;
        this.userRepository = userRepository;
    }

    /**
     * Gửi yêu cầu kết bạn
     * 
     * @param fromId User ID của người gửi
     * @param toId   User ID của người nhận
     * @return FriendRequest - Yêu cầu kết bạn vừa được tạo
     */
    public FriendRequest sendFriendRequest(String fromId, String toId) {
        try {
            // Validate input
            if (fromId == null || fromId.isEmpty()) {
                throw new IllegalArgumentException("fromId cannot be empty");
            }
            if (toId == null || toId.isEmpty()) {
                throw new IllegalArgumentException("toId cannot be empty");
            }
            if (fromId.equals(toId)) {
                throw new IllegalArgumentException("Cannot send friend request to yourself");
            }

            // Kiểm tra xem cả hai user có tồn tại không
            Optional<User> fromUser = userRepository.findById(fromId);
            Optional<User> toUser = userRepository.findById(toId);
            
            if (fromUser.isEmpty()) {
                throw new IllegalArgumentException("From user does not exist");
            }
            if (toUser.isEmpty()) {
                throw new IllegalArgumentException("To user does not exist");
            }

            // Kiểm tra xem họ đã là bạn bè chưa
            if (friendRequestRepository.areFriends(fromId, toId)) {
                throw new IllegalArgumentException("Users are already friends");
            }

            // Kiểm tra xem yêu cầu đã tồn tại chưa
            Optional<FriendRequest> existingRequest = 
                friendRequestRepository.findByFromUserIdAndToUserId(fromId, toId);
            
            if (existingRequest.isPresent()) {
                FriendRequest req = existingRequest.get();
                if (req.getStatus().equals("PENDING")) {
                    throw new IllegalArgumentException("Friend request already sent");
                } else if (req.getStatus().equals("ACCEPTED")) {
                    throw new IllegalArgumentException("Users are already friends");
                }
            }

            // Tạo yêu cầu kết bạn mới
            FriendRequest friendRequest = FriendRequest.builder()
                    .id(UUID.randomUUID().toString())
                    .fromUserId(fromId)
                    .toUserId(toId)
                    .status("PENDING")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            FriendRequest saved = friendRequestRepository.save(friendRequest);
            log.info("Friend request sent from {} to {}", fromId, toId);
            return saved;

        } catch (Exception e) {
            log.error("Error sending friend request: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Chấp nhận yêu cầu kết bạn
     * 
     * @param requestId ID của yêu cầu kết bạn
     * @return FriendRequest - Yêu cầu kết bạn vừa được chấp nhận
     */
    public FriendRequest acceptFriend(String requestId) {
        try {
            // Validate input
            if (requestId == null || requestId.isEmpty()) {
                throw new IllegalArgumentException("requestId cannot be empty");
            }

            // Tìm yêu cầu kết bạn
            Optional<FriendRequest> optionalRequest = friendRequestRepository.findById(requestId);
            if (optionalRequest.isEmpty()) {
                throw new IllegalArgumentException("Friend request not found");
            }

            FriendRequest friendRequest = optionalRequest.get();

            // Kiểm tra xem yêu cầu đang ở trạng thái PENDING
            if (!friendRequest.getStatus().equals("PENDING")) {
                throw new IllegalArgumentException("Friend request is not in PENDING status");
            }

            // Cập nhật trạng thái thành ACCEPTED
            friendRequest.setStatus("ACCEPTED");
            friendRequest.setUpdatedAt(LocalDateTime.now());

            FriendRequest saved = friendRequestRepository.save(friendRequest);
            log.info("Friend request {} accepted", requestId);
            return saved;

        } catch (Exception e) {
            log.error("Error accepting friend request: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Từ chối yêu cầu kết bạn
     * 
     * @param requestId ID của yêu cầu kết bạn
     * @return FriendRequest - Yêu cầu kết bạn vừa được từ chối
     */
    public FriendRequest rejectFriend(String requestId) {
        try {
            // Validate input
            if (requestId == null || requestId.isEmpty()) {
                throw new IllegalArgumentException("requestId cannot be empty");
            }

            // Tìm yêu cầu kết bạn
            Optional<FriendRequest> optionalRequest = friendRequestRepository.findById(requestId);
            if (optionalRequest.isEmpty()) {
                throw new IllegalArgumentException("Friend request not found");
            }

            FriendRequest friendRequest = optionalRequest.get();

            // Kiểm tra xem yêu cầu đang ở trạng thái PENDING
            if (!friendRequest.getStatus().equals("PENDING")) {
                throw new IllegalArgumentException("Friend request is not in PENDING status");
            }

            // Cập nhật trạng thái thành REJECTED
            friendRequest.setStatus("REJECTED");
            friendRequest.setUpdatedAt(LocalDateTime.now());

            FriendRequest saved = friendRequestRepository.save(friendRequest);
            log.info("Friend request {} rejected", requestId);
            return saved;

        } catch (Exception e) {
            log.error("Error rejecting friend request: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Lấy danh sách yêu cầu kết bạn đang chờ của user
     * 
     * @param userId User ID
     * @return List<FriendRequest> - Danh sách yêu cầu đang chờ
     */
    public List<FriendRequest> getPendingRequests(String userId) {
        try {
            if (userId == null || userId.isEmpty()) {
                throw new IllegalArgumentException("userId cannot be empty");
            }

            List<FriendRequest> requests = 
                friendRequestRepository.findByToUserIdAndStatus(userId, "PENDING");
            log.info("Retrieved {} pending requests for user {}", requests.size(), userId);
            return requests;

        } catch (Exception e) {
            log.error("Error getting pending requests: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Kiểm tra xem hai user có phải bạn bè không
     * 
     * @param userId1 User ID 1
     * @param userId2 User ID 2
     * @return boolean - true nếu là bạn bè, false nếu không
     */
    public boolean areFriends(String userId1, String userId2) {
        try {
            if (userId1 == null || userId1.isEmpty()) {
                throw new IllegalArgumentException("userId1 cannot be empty");
            }
            if (userId2 == null || userId2.isEmpty()) {
                throw new IllegalArgumentException("userId2 cannot be empty");
            }

            return friendRequestRepository.areFriends(userId1, userId2);

        } catch (Exception e) {
            log.error("Error checking friendship: {}", e.getMessage());
            throw e;
        }
    }
}
