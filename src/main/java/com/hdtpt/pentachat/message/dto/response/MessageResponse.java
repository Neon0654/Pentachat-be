package com.hdtpt.pentachat.message.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageResponse {
    private String id;
    private String from;
    
    // For backward compatibility
    private String to;
    
    // For group messages
    private String targetId;  // userId for PERSONAL, groupId for GROUP
    private String type;       // PERSONAL or GROUP
    
    private String content;
    private LocalDateTime createdAt;
    private Boolean isRead;
}
