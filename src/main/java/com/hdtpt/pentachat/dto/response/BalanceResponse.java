package com.hdtpt.pentachat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Balance response DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BalanceResponse {
    private String userId;
    private Double balance;
}
