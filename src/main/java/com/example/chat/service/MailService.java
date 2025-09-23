package com.example.chat.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    /**
     * Gửi email text thường
     */
    public void sendSimple(String to, String subject, String body) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject(subject != null ? subject : "(No Subject)");
        msg.setText(body != null ? body : "");
        mailSender.send(msg);
    }

    /**
     * Gửi email HTML
     */
    public void sendHtml(String to, String subject, String htmlBody) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();

        // dùng MultipartMode để chắc chắn tránh lỗi parse
        MimeMessageHelper helper = new MimeMessageHelper(
                message,
                MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                "UTF-8"
        );

        helper.setTo(to);
        helper.setSubject(subject != null ? subject : "(No Subject)");
        helper.setText(htmlBody != null ? htmlBody : "", true); // true = HTML

        mailSender.send(message);
    }
}
