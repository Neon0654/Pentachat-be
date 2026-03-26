package com.hdtpt.pentachat.games.poker.controller;

import com.hdtpt.pentachat.games.poker.dto.PokerActionDTO;
import com.hdtpt.pentachat.games.poker.model.GameSession;
import com.hdtpt.pentachat.games.poker.service.GameSessionManager;
import com.hdtpt.pentachat.games.poker.service.PokerGameService;
import com.hdtpt.pentachat.security.SessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@Slf4j
@RequiredArgsConstructor
public class PokerWebSocketController {

    private final PokerGameService gameService;
    private final GameSessionManager sessionManager;

    @MessageMapping("/poker.action")
    public void processAction(
            @Payload PokerActionDTO action,
            @Header(name = "X-User-Id", required = false) String userIdHeader,
            @Header(name = "X-Session-Id", required = false) String sessionId) {
        Long userId = validateSession(userIdHeader, sessionId);
        if (userId == null) {
            log.warn("Rejected poker action due to missing/invalid session headers");
            return;
        }

        log.info("User {} sent poker action {} in room {}", userId, action.getType(), action.getRoomId());
        gameService.handleAction(userId, action);
    }

    @SubscribeMapping("/topic/poker/{roomId}")
    public com.hdtpt.pentachat.games.poker.dto.GameStateDTO onSubscribe(
            @PathVariable String roomId,
            @Header(name = "X-User-Id", required = false) String userIdHeader,
            @Header(name = "X-Session-Id", required = false) String sessionId) {
        Long userId = validateSession(userIdHeader, sessionId);
        if (userId == null) {
            log.warn("Rejected poker subscribe for room {} due to missing/invalid session headers", roomId);
            return null;
        }

        GameSession session = sessionManager.getSession(roomId);
        if (session != null) {
            log.info("User {} subscribed to poker room {}", userId, roomId);
            // Trả về state ngay lập tức cho người subscribe
            return gameService.getGameState(session);
        }
        return null;
    }

    private Long validateSession(String userIdHeader, String sessionId) {
        if (userIdHeader == null || sessionId == null) {
            return null;
        }

        try {
            Long userId = Long.valueOf(userIdHeader);
            SessionManager.SessionInfo sessionInfo = SessionManager.getUserSession(userId);
            if (sessionInfo != null && sessionId.equals(sessionInfo.sessionId)) {
                return userId;
            }
        } catch (NumberFormatException ignored) {
        }

        return null;
    }
}
