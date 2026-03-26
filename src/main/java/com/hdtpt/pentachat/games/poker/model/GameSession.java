package com.hdtpt.pentachat.games.poker.model;

import com.hdtpt.pentachat.games.poker.dto.PlayerStateDTO;
import lombok.Builder;
import lombok.Data;

import java.util.*;
import java.util.concurrent.ScheduledFuture;

/**
 * Phiên chơi Poker đang diễn ra trong RAM.
 */
@Data
public class GameSession {
    private String roomId;
    private List<String> deck; // Bộ bài (in-memory)
    private Map<Long, List<String>> holeCards; // Bài trên tay mỗi người chơi
    private List<String> communityCards; // Bài chung trên bàn
    private Long currentTurnUserId; // Ai đang đánh
    private Double pot; // Tiền trong hũ
    private Double currentRoundBet; // Số tiền cược tối thiểu để theo vòng này
    private GamePhase phase; // Giai đoạn game
    private Map<Long, Double> playerBets; // Tiền cược của từng người chơi
    private Map<Long, PlayerStateDTO.PlayerStatus> playerStatus; // Trạng thái (ACTIVE, FOLDED...)
    private Map<Long, Double> playerChips; // Số tiền đem vào bàn
    private ScheduledFuture<?> turnTimer; // Bộ đếm thời gian mỗi lượt
    private List<Long> seatOrder; // Thứ tự người chơi tại bàn
    private String lastActionLog; // Nhật ký hành động cuối cùng
    private Long hostId; // ID của chủ phòng
    private Set<Long> playersActedThisRound; // ID của người chơi đã hành động trong vòng này

    public GameSession(String roomId) {
        this.roomId = roomId;
        this.communityCards = new ArrayList<>();
        this.holeCards = new HashMap<>();
        this.playerBets = new HashMap<>();
        this.playerStatus = new HashMap<>();
        this.playerChips = new HashMap<>();
        this.seatOrder = new ArrayList<>();
        this.pot = 0.0;
        this.currentRoundBet = 0.0;
        this.phase = GamePhase.WAITING;
        this.lastActionLog = "Đang chờ người chơi...";
        this.playersActedThisRound = new HashSet<>();
    }
}
