package com.example.demo;

import com.hdtpt.pentachat.dto.MessageDTO;
import com.hdtpt.pentachat.gate.MessageHandler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProjectGaugeApplicationTests {

    @Test
    void should_throw_exception_when_missing_userId() {
        MessageDTO message = new MessageDTO();
        message.setTo(2);
        message.setContent("hi");

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> MessageHandler.handle(message)
        );

        assertTrue(exception.getMessage().contains("Missing userId"));
    }

    @Test
    void should_allow_when_userId_exists() {
        MessageDTO message = new MessageDTO();
        message.setFrom(1);
        message.setTo(2);
        message.setContent("hi");

        assertDoesNotThrow(() -> MessageHandler.handle(message));
    }
}
