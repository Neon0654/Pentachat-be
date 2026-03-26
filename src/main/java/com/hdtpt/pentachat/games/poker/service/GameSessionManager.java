package com.hdtpt.pentachat.games.poker.service;

import com.hdtpt.pentachat.games.poker.model.GameSession;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Quản lý các GameSession trong RAM.
 */
@Component
public class GameSessionManager {
    private final Map<String, GameSession> sessions = new ConcurrentHashMap<>();

    public void createSession(String roomId, Long hostId) {
        GameSession session = new GameSession(roomId);
        session.setHostId(hostId);
        sessions.put(roomId, session);
    }

    public GameSession getSession(String roomId) {
        return sessions.get(roomId);
    }

    public void removeSession(String roomId) {
        GameSession session = sessions.remove(roomId);
        if (session != null && session.getTurnTimer() != null) {
            session.getTurnTimer().cancel(false);
        }
    }

    public boolean exists(String roomId) {
        return sessions.containsKey(roomId);
    }
}
