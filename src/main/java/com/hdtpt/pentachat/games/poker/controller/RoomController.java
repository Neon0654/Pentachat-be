package com.hdtpt.pentachat.games.poker.controller;

import com.hdtpt.pentachat.exception.AppException;
import com.hdtpt.pentachat.dto.response.ApiResponse;
import com.hdtpt.pentachat.games.model.Room;
import com.hdtpt.pentachat.games.poker.model.GameSession;
import com.hdtpt.pentachat.games.poker.service.GameSessionManager;
import com.hdtpt.pentachat.games.poker.service.PokerGameService;
import com.hdtpt.pentachat.games.poker.service.RoomService;
import com.hdtpt.pentachat.security.SessionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller cho các phòng Poker.
 */
@RestController
@RequestMapping("/api/poker/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final PokerGameService gameService;
    private final GameSessionManager gameSessionManager;

    @PostMapping
    public ResponseEntity<ApiResponse> createRoom(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Session-Id") String sessionId,
            @RequestBody Map<String, Object> payload) {
        validateSession(userId, sessionId);

        int maxPlayers = (int) payload.getOrDefault("maxPlayers", 6);
        Double minBet = Double.valueOf(payload.getOrDefault("minBet", 100).toString());

        Room room = roomService.createRoom(userId, maxPlayers, minBet);
        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Room created successfully")
                .data(room)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<Room>> listRooms() {
        return ResponseEntity.ok(roomService.listOpenRooms());
    }

    @GetMapping("/{roomCode}/state")
    public ResponseEntity<ApiResponse> getRoomState(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Session-Id") String sessionId,
            @PathVariable String roomCode) {
        validateSession(userId, sessionId);
        
        GameSession session = gameSessionManager.getSession(roomCode);
        if (session == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.builder()
                            .success(false)
                            .message("Room not found")
                            .build());
        }
        
        var state = gameService.getGameState(session);
        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Room state retrieved successfully")
                .data(state)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{roomCode}/join")
    public ResponseEntity<ApiResponse> joinRoom(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Session-Id") String sessionId,
            @PathVariable String roomCode) {
        validateSession(userId, sessionId);
        roomService.joinRoomInternal(roomCode, userId);

        GameSession session = gameSessionManager.getSession(roomCode);
        if (session != null) {
            gameService.broadcastState(session);
        }
        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Joined room successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{roomCode}/leave")
    public ResponseEntity<ApiResponse> leaveRoom(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Session-Id") String sessionId,
            @PathVariable String roomCode) {
        validateSession(userId, sessionId);
        roomService.leaveRoom(roomCode, userId);
        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Left room successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    private void validateSession(Long userId, String sessionId) {
        SessionManager.SessionInfo sessionInfo = SessionManager.getUserSession(userId);
        if (sessionInfo == null || !sessionInfo.sessionId.equals(sessionId)) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Invalid session.");
        }
    }
}
