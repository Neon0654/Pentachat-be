package com.hdtpt.pentachat.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Session Manager - Quản lý userId -> Session mapping
 * Giữ track thông tin user nào đang online
 * 
 * Map structure: userId -> Session info
 * Ví dụ: "user1" -> {sessionId: "abc123", isOnline: true}
 */
public class SessionManager {
    
    // Map để lưu userId -> session info
    private static final Map<String, SessionInfo> userSessions = new ConcurrentHashMap<>();

    /**
     * Lưu session của user
     */
    public static void addUserSession(String userId, String sessionId) {
        userSessions.put(userId, new SessionInfo(userId, sessionId, true));
    }

    /**
     * Xóa session của user (user logout)
     */
    public static void removeUserSession(String userId) {
        userSessions.remove(userId);
    }

    /**
     * Lấy session info của user
     */
    public static SessionInfo getUserSession(String userId) {
        return userSessions.get(userId);
    }

    /**
     * Kiểm tra user có online không
     */
    public static boolean isUserOnline(String userId) {
        SessionInfo session = userSessions.get(userId);
        return session != null && session.isOnline;
    }

    /**
     * Lấy tất cả active sessions
     */
    public static Map<String, SessionInfo> getAllSessions() {
        return new ConcurrentHashMap<>(userSessions);
    }

    /**
     * Inner class để lưu session info
     */
    public static class SessionInfo {
        public String userId;
        public String sessionId;
        public boolean isOnline;

        public SessionInfo(String userId, String sessionId, boolean isOnline) {
            this.userId = userId;
            this.sessionId = sessionId;
            this.isOnline = isOnline;
        }

        @Override
        public String toString() {
            return "SessionInfo{" +
                    "userId='" + userId + '\'' +
                    ", sessionId='" + sessionId + '\'' +
                    ", isOnline=" + isOnline +
                    '}';
        }
    }
}
