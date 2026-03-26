package com.hdtpt.pentachat.games.poker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

/**
 * Trạng thái của một người chơi tại bàn cược.
 */
@Data
@Builder
public class PlayerStateDTO {
    public enum PlayerStatus {
        ACTIVE,     // Đang chơi
        FOLDED,     // Đã bỏ bài
        ALL_IN,     // Đã cược hết (Theo ván hết bài)
        WAITING     // Đang chờ cho ván mới
    }

    private Long userId;
    private String username;
    private int seatIndex;
    private Double chips;           // Số tiền người chơi còn trong bàn
    private PlayerStatus status;
    private Double betThisRound;    // Số tiền đã cược trong vòng hiện tại
    @JsonProperty("isHost")
    private boolean isHost;
}
