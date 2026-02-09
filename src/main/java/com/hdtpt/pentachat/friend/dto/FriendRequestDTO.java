package com.hdtpt.pentachat.friend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * FriendRequestDTO - Data Transfer Object cho friend request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendRequestDTO {
    
    private String id;
    
    @NotBlank(message = "fromUserId cannot be blank")
    private String fromUserId;
    
    @NotBlank(message = "toUserId cannot be blank")
    private String toUserId;
    
    private String status;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}
