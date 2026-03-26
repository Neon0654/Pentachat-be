package com.hdtpt.pentachat.games.poker.dto;

import com.hdtpt.pentachat.games.poker.model.GamePhase;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GameStateDTO {
    private String roomId;
    private Double pot;
    private Double currentRoundBet;
    private Long currentTurnUserId;
    private GamePhase phase;
    private List<String> communityCards;
    private List<PlayerStateDTO> players;
    private String log;
    private String lastActionLog;
}
