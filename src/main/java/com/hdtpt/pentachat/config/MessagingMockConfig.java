package com.hdtpt.pentachat.config;

import com.hdtpt.pentachat.service.SessionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

/**
 * Messaging Mock Configuration
 * 
 * Initializes mock data for messaging system
 * Simulates users being online
 * 
 * This is ONLY for testing messaging features
 * Does NOT affect wallet or other features
 */
@Configuration
@Slf4j
public class MessagingMockConfig implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        // Initialize mock users for testing
        initializeMockUsers();
        
        log.info("✅ Messaging Mock Data initialized successfully!");
        log.info("📝 You can now test messaging with these users: user1, user2, user3");
    }

    /**
     * Initialize mock user sessions
     * Simulates users being online
     */
    private void initializeMockUsers() {
        // Add mock users to session
        SessionManager.addUserSession("user1", "session-001");
        SessionManager.addUserSession("user2", "session-002");
        SessionManager.addUserSession("user3", "session-003");
        SessionManager.addUserSession("alice", "session-alice");
        SessionManager.addUserSession("bob", "session-bob");

        log.info("🟢 Mock Users Added to SessionManager:");
        log.info("   - user1 (session-001)");
        log.info("   - user2 (session-002)");
        log.info("   - user3 (session-003)");
        log.info("   - alice (session-alice)");
        log.info("   - bob (session-bob)");
        
        log.info("\n📌 Now you can test messaging API:");
        log.info("   POST /message/send");
        log.info("   {\"from\": \"user1\", \"to\": \"user2\", \"content\": \"hello\"}");
    }
}
