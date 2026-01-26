package com.hdtpt.pentachat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Transaction response DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionResponse {
    private String id;
    private String type;
    private String fromUserId;
    private String toUserId;
    private Double amount;
    private LocalDateTime createdAt;
}
