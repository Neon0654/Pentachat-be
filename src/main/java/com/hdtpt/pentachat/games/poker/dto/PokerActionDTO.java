package com.hdtpt.pentachat.games.poker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO đại diện cho một hành động của người chơi trong game Poker.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PokerActionDTO {
    public enum ActionType {
        BET,    // Cược
        FOLD,   // Bỏ bài
        CHECK,  // Xem bài (không cược)
        CALL,   // Theo cược
        RAISE,  // Tố thêm
        START   // Bắt đầu game (chỉ host)
    }

    private ActionType type;
    private Double amount;
    private String roomId;
}
