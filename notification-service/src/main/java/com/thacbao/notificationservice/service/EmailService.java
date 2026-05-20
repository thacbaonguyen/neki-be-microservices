package com.thacbao.notificationservice.service;

public interface EmailService {

    void sendOtpEmail(String to, String otp);

    void sendPasswordResetEmail(String to, String otp);

    void sendWelcomeEmail(String to, String fullName);

    void sendPasswordChangedEmail(String to, String fullName);

    void sendAccountBlockedEmail(String to, String fullName);

    void sendOrderConfirmationEmail(String to, String orderNumber, String totalAmount);

    void sendOrderCancelledEmail(String to, String fullName, String orderNumber, String reason);

    void sendOrderStatusUpdatedEmail(String to, String fullName, String orderNumber, String oldStatus, String newStatus);

    void sendPaymentConfirmationEmail(String to, String orderNumber, String amount, String paymentMethod);
}
