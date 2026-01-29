package com.hdtpt.pentachat.gate;

import com.hdtpt.pentachat.dto.MessageDTO;

public class MessageHandler {

    public static void handle(MessageDTO message) {

        // Bảo vệ cổng kết nối
        ConnectionGuard.check(message);

        // Xử lý message hợp lệ
        System.out.println(
                "User " + message.getFrom() +
                " gửi cho " + message.getTo() +
                ": " + message.getContent()
        );
    }
}
