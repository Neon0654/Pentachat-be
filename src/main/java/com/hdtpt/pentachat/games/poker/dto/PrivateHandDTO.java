package com.hdtpt.pentachat.games.poker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * Các lá bài riêng của người chơi.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrivateHandDTO {
    private List<String> holeCards; // Chỉ gửi riêng cho từng người chơi
}
