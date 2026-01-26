package com.hdtpt.pentachat.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        // Nhận JSON dạng text
        String payload = message.getPayload();

        // Convert JSON -> Object
        ChatMessage chatMessage = objectMapper.readValue(payload, ChatMessage.class);

        // In ra console để chứng minh đã nhận
        System.out.println("===== WEBSOCKET MESSAGE RECEIVED =====");
        System.out.println("From   : " + chatMessage.getFrom());
        System.out.println("To     : " + chatMessage.getTo());
        System.out.println("Content: " + chatMessage.getContent());
        System.out.println("=====================================");

        // Trả phản hồi lại client (không bắt buộc)
        session.sendMessage(new TextMessage("Server received message"));
    }
}
