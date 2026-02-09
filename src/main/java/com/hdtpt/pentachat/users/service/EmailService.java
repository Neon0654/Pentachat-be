package com.hdtpt.pentachat.users.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    public void sendResetLink(String toEmail, String token) {
        // Tạo đường link dẫn về trang web của bạn
        String resetUrl = "http://localhost:8080/reset-password.html?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("Pentachat Support <noreply@pentachat.com>");
        message.setTo(toEmail);
        message.setSubject("Yêu cầu đặt lại mật khẩu");
        message.setText("Xin chào,\n\n"
                + "Bạn đã yêu cầu đặt lại mật khẩu. Vui lòng bấm vào link dưới đây để đổi mật khẩu mới:\n\n"
                + resetUrl + "\n\n"
                + "Link này sẽ hết hạn sau 24 giờ.\n"
                + "Nếu bạn không yêu cầu, hãy bỏ qua email này.");

        mailSender.send(message);
        System.out.println("📧 Đã gửi link reset đến: " + toEmail);
    }
}