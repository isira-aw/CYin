package com.example.RegistrationLoginPage.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendResetLink(String toEmail, String token) {
//        String resetLink = "http://localhost:5173/reset-password?token=" + token;
        String resetLink = "https://cyin-mobile.up.railway.app/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Reset Your Password");
        message.setText("Click the link to reset your password: " + resetLink);

        mailSender.send(message);
    }
}
