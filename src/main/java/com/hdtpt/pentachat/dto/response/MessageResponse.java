package com.hdtpt.pentachat.dto.response;

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
    private String to;
    private String content;
    private LocalDateTime createdAt;
    private Boolean isRead;
}
