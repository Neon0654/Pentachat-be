package com.hdtpt.pentachat.websocket;

import lombok.Data;

@Data
public class ChatMessage {
    private int from;
    private int to;
    private String content;
}
