package com.thacbao.notificationservice.listener;

import com.thacbao.common.event.PasswordResetEvent;
import com.thacbao.common.event.UserRegisteredEvent;
import com.thacbao.notificationservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static com.thacbao.common.constant.RabbitMQConstants.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventListener {

    private final EmailService emailService;

    @RabbitListener(queues = NOTIFICATION_USER_REGISTERED_QUEUE)
    public void handleUserRegistered(UserRegisteredEvent event) {
        log.info("Received user.registered event for: {}", event.getEmail());
        try {
            if ("OTP_VERIFICATION".equals(event.getType())) {
                emailService.sendOtpEmail(event.getEmail(), event.getOtpCode());
            } else if ("WELCOME".equals(event.getType())) {
                emailService.sendWelcomeEmail(event.getEmail(), event.getFullName());
            }
        } catch (Exception e) {
            log.error("Error handling user.registered event: {}", e.getMessage());
        }
    }

    @RabbitListener(queues = NOTIFICATION_USER_PASSWORD_QUEUE)
    public void handlePasswordReset(PasswordResetEvent event) {
        log.info("Received user.password.reset event for: {}", event.getEmail());
        try {
            emailService.sendPasswordResetEmail(event.getEmail(), event.getOtpCode());
        } catch (Exception e) {
            log.error("Error handling password reset event: {}", e.getMessage());
        }
    }
}
