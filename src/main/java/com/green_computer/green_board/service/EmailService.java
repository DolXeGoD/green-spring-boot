package com.green_computer.green_board.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    @Async
    public void sendVerificationCode(String toEmail, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail); // 수신자
        message.setSubject("그린보드 서비스에서 발송한 인증번호입니다."); // 메일 제목
        message.setText("다음 인증번호를 서비스에 입력해주세요 : " + code); // 메일 내용

        mailSender.send(message);
    }
}
