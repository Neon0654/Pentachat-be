package com.hdtpt.pentachat.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MessageDTO - Data Transfer Object for Message validation
 * Used by ConnectionGuard and internal messaging operations
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageDTO {
    private String from;
    private String to;
    private String content;
}
