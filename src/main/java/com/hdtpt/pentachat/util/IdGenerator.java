package com.hdtpt.pentachat.util;

import java.util.UUID;

/**
 * Utility class for generating unique IDs
 */
public class IdGenerator {
    /**
     * Generate a unique ID using UUID
     * @return unique string ID
     */
    public static String generateId() {
        return UUID.randomUUID().toString();
    }
}
