package com.thacbao.notificationservice.service.impl;

import com.thacbao.notificationservice.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.name:NEKI E-Commerce}")
    private String appName;

    @Value("${app.url:http://localhost:3000}")
    private String appUrl;

    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        mailSender.send(message);
    }

    @Override
    @Async
    public void sendOtpEmail(String to, String otp) {
        try {
            String subject = "Xác thực tài khoản - " + appName;
            String content = buildOtpEmailTemplate(otp, "xác thực tài khoản");
            sendHtmlEmail(to, subject, content);
            log.info("OTP email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send OTP email to: {}", to, e);
        }
    }

    @Override
    @Async
    public void sendPasswordResetEmail(String to, String otp) {
        try {
            String subject = "Đặt lại mật khẩu - " + appName;
            String content = buildOtpEmailTemplate(otp, "đặt lại mật khẩu");
            sendHtmlEmail(to, subject, content);
            log.info("Password reset email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", to, e);
        }
    }

    @Override
    @Async
    public void sendWelcomeEmail(String to, String fullName) {
        try {
            String subject = "Chào mừng đến với " + appName;
            String content = buildWelcomeEmailTemplate(fullName);
            sendHtmlEmail(to, subject, content);
            log.info("Welcome email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", to, e);
        }
    }

    @Override
    @Async
    public void sendPasswordChangedEmail(String to, String fullName) {
        try {
            String subject = "Mật khẩu đã thay đổi - " + appName;
            String content = buildPasswordChangedEmailTemplate(fullName);
            sendHtmlEmail(to, subject, content);
            log.info("Password changed email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send password changed email to: {}", to, e);
        }
    }

    @Override
    @Async
    public void sendAccountBlockedEmail(String to, String fullName) {
        try {
            String subject = "Tài khoản bị khóa - " + appName;
            String content = buildAccountBlockedEmailTemplate(fullName);
            sendHtmlEmail(to, subject, content);
            log.info("Account blocked email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send account blocked email to: {}", to, e);
        }
    }

    @Override
    @Async
    public void sendOrderConfirmationEmail(String to, String orderNumber, String totalAmount) {
        try {
            String subject = "Xác nhận đơn hàng #" + orderNumber + " - " + appName;
            String content = buildOrderConfirmationEmailTemplate(orderNumber, totalAmount);
            sendHtmlEmail(to, subject, content);
            log.info("Order confirmation email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send order confirmation email to: {}", to, e);
        }
    }

    @Override
    @Async
    public void sendOrderCancelledEmail(String to, String fullName, String orderNumber, String reason) {
        try {
            String subject = "Đơn hàng #" + orderNumber + " đã bị hủy - " + appName;
            String content = buildOrderCancelledEmailTemplate(fullName, orderNumber, reason);
            sendHtmlEmail(to, subject, content);
            log.info("Order cancelled email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send order cancelled email to: {}", to, e);
        }
    }

    @Override
    @Async
    public void sendOrderStatusUpdatedEmail(String to, String fullName, String orderNumber,
                                             String oldStatus, String newStatus) {
        try {
            String subject = "Cập nhật đơn hàng #" + orderNumber + " - " + appName;
            String content = buildOrderStatusUpdatedEmailTemplate(fullName, orderNumber, oldStatus, newStatus);
            sendHtmlEmail(to, subject, content);
            log.info("Order status updated email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send order status email to: {}", to, e);
        }
    }

    @Override
    @Async
    public void sendPaymentConfirmationEmail(String to, String orderNumber, String amount, String paymentMethod) {
        try {
            String subject = "Thanh toán thành công đơn hàng #" + orderNumber + " - " + appName;
            String content = buildPaymentConfirmationEmailTemplate(orderNumber, amount, paymentMethod);
            sendHtmlEmail(to, subject, content);
            log.info("Payment confirmation email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send payment confirmation email to: {}", to, e);
        }
    }

    // ===== HTML Templates =====

    private String buildOtpEmailTemplate(String otp, String purpose) {
        return """
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                        .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                        .otp-box { background: white; border: 2px dashed #667eea; border-radius: 10px; padding: 20px; text-align: center; margin: 20px 0; }
                        .otp-code { font-size: 32px; font-weight: bold; color: #667eea; letter-spacing: 8px; }
                        .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header"><h1>%s</h1></div>
                        <div class="content">
                            <p>Mã OTP để %s:</p>
                            <div class="otp-box"><div class="otp-code">%s</div></div>
                            <p>Mã có hiệu lực trong <strong>5 phút</strong>. Không chia sẻ mã này với bất kỳ ai.</p>
                            <p style="margin-top: 30px;">Trân trọng,<br><strong>%s Team</strong></p>
                        </div>
                        <div class="footer"><p>© %d %s. All rights reserved.</p></div>
                    </div>
                </body>
                </html>
                """.formatted(appName, purpose, otp, appName, LocalDateTime.now().getYear(), appName);
    }

    private String buildWelcomeEmailTemplate(String fullName) {
        return """
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: linear-gradient(135deg, #11998e 0%%, #38ef7d 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                        .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                        .button { display: inline-block; background: #11998e; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                        .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header"><h1>Chào mừng đến với %s!</h1></div>
                        <div class="content">
                            <p>Xin chào <strong>%s</strong>,</p>
                            <p>Tài khoản của bạn đã được xác thực thành công. Bắt đầu khám phá ngay!</p>
                            <div style="text-align: center;"><a href="%s" class="button">Mua sắm ngay</a></div>
                            <p style="margin-top: 30px;">Trân trọng,<br><strong>%s Team</strong></p>
                        </div>
                        <div class="footer"><p>© %d %s. All rights reserved.</p></div>
                    </div>
                </body>
                </html>
                """.formatted(appName, fullName, appUrl, appName, LocalDateTime.now().getYear(), appName);
    }

    private String buildPasswordChangedEmailTemplate(String fullName) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        return """
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: #28a745; color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                        .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                        .alert { background: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0; }
                        .info-box { background: white; border-radius: 8px; padding: 15px; margin: 20px 0; }
                        .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header"><h1>🔐 Mật khẩu đã được thay đổi</h1></div>
                        <div class="content">
                            <p>Xin chào <strong>%s</strong>,</p>
                            <p>Mật khẩu tài khoản của bạn đã được thay đổi thành công.</p>
                            <div class="info-box"><strong>Thời gian:</strong> %s</div>
                            <div class="alert"><strong>Bạn không thực hiện thao tác này?</strong><br>Vui lòng liên hệ với chúng tôi ngay.</div>
                            <p style="margin-top: 30px;">Trân trọng,<br><strong>%s Team</strong></p>
                        </div>
                        <div class="footer"><p>© %d %s. All rights reserved.</p></div>
                    </div>
                </body>
                </html>
                """.formatted(fullName, timestamp, appName, LocalDateTime.now().getYear(), appName);
    }

    private String buildAccountBlockedEmailTemplate(String fullName) {
        return """
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: #dc3545; color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                        .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                        .warning { background: #f8d7da; border-left: 4px solid #dc3545; padding: 15px; margin: 20px 0; }
                        .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header"><h1>Tài khoản đã bị khóa</h1></div>
                        <div class="content">
                            <p>Xin chào <strong>%s</strong>,</p>
                            <div class="warning"><strong>Tài khoản của bạn đã bị khóa</strong><br>Do vi phạm điều khoản sử dụng.</div>
                            <p>Nếu đây là nhầm lẫn, vui lòng liên hệ bộ phận hỗ trợ.</p>
                            <p style="margin-top: 30px;">Trân trọng,<br><strong>%s Team</strong></p>
                        </div>
                        <div class="footer"><p>© %d %s. All rights reserved.</p></div>
                    </div>
                </body>
                </html>
                """.formatted(fullName, appName, LocalDateTime.now().getYear(), appName);
    }

    private String buildOrderConfirmationEmailTemplate(String orderNumber, String totalAmount) {
        return """
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                        .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                        .order-box { background: white; border-radius: 8px; padding: 20px; margin: 20px 0; }
                        .button { display: inline-block; background: #667eea; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                        .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header"><h1>Đơn hàng đã được xác nhận!</h1></div>
                        <div class="content">
                            <p>Cảm ơn bạn đã mua hàng tại %s!</p>
                            <div class="order-box">
                                <h3>Thông tin đơn hàng:</h3>
                                <p><strong>Mã đơn hàng:</strong> #%s</p>
                                <p><strong>Tổng tiền:</strong> %s VNĐ</p>
                                <p><strong>Trạng thái:</strong> Đang xử lý</p>
                            </div>
                            <div style="text-align: center;"><a href="%s/orders/%s" class="button">Xem chi tiết đơn hàng</a></div>
                            <p style="margin-top: 30px;">Trân trọng,<br><strong>%s Team</strong></p>
                        </div>
                        <div class="footer"><p>© %d %s. All rights reserved.</p></div>
                    </div>
                </body>
                </html>
                """.formatted(appName, orderNumber, totalAmount, appUrl, orderNumber, appName,
                LocalDateTime.now().getYear(), appName);
    }

    private String buildOrderCancelledEmailTemplate(String fullName, String orderNumber, String reason) {
        return """
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: #dc3545; color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                        .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                        .info-box { background: white; border-radius: 8px; padding: 15px; margin: 20px 0; }
                        .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header"><h1>Đơn hàng đã bị hủy</h1></div>
                        <div class="content">
                            <p>Xin chào <strong>%s</strong>,</p>
                            <div class="info-box">
                                <p><strong>Mã đơn hàng:</strong> #%s</p>
                                <p><strong>Lý do:</strong> %s</p>
                            </div>
                            <p>Nếu có thắc mắc, vui lòng liên hệ bộ phận hỗ trợ.</p>
                            <p style="margin-top: 30px;">Trân trọng,<br><strong>%s Team</strong></p>
                        </div>
                        <div class="footer"><p>© %d %s. All rights reserved.</p></div>
                    </div>
                </body>
                </html>
                """.formatted(fullName, orderNumber, reason, appName, LocalDateTime.now().getYear(), appName);
    }

    private String buildOrderStatusUpdatedEmailTemplate(String fullName, String orderNumber,
                                                         String oldStatus, String newStatus) {
        return """
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: linear-gradient(135deg, #f093fb 0%%, #f5576c 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                        .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                        .status-box { background: white; border-radius: 8px; padding: 20px; margin: 20px 0; text-align: center; }
                        .status-arrow { font-size: 24px; margin: 0 10px; }
                        .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header"><h1>Cập nhật đơn hàng #%s</h1></div>
                        <div class="content">
                            <p>Xin chào <strong>%s</strong>,</p>
                            <div class="status-box">
                                <strong>%s</strong> <span class="status-arrow">→</span> <strong>%s</strong>
                            </div>
                            <div style="text-align: center;"><a href="%s/orders/%s" style="display: inline-block; background: #f5576c; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px;">Xem đơn hàng</a></div>
                            <p style="margin-top: 30px;">Trân trọng,<br><strong>%s Team</strong></p>
                        </div>
                        <div class="footer"><p>© %d %s. All rights reserved.</p></div>
                    </div>
                </body>
                </html>
                """.formatted(orderNumber, fullName, oldStatus, newStatus, appUrl, orderNumber, appName,
                LocalDateTime.now().getYear(), appName);
    }

    private String buildPaymentConfirmationEmailTemplate(String orderNumber, String amount, String paymentMethod) {
        return """
                <!DOCTYPE html>
                <html>
                <head><meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: linear-gradient(135deg, #11998e 0%%, #38ef7d 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                        .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                        .payment-box { background: white; border-radius: 8px; padding: 20px; margin: 20px 0; }
                        .footer { text-align: center; margin-top: 20px; color: #666; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header"><h1>💳 Thanh toán thành công!</h1></div>
                        <div class="content">
                            <p>Thanh toán cho đơn hàng của bạn đã được xác nhận.</p>
                            <div class="payment-box">
                                <p><strong>Mã đơn hàng:</strong> #%s</p>
                                <p><strong>Số tiền:</strong> %s VNĐ</p>
                                <p><strong>Phương thức:</strong> %s</p>
                            </div>
                            <p style="margin-top: 30px;">Trân trọng,<br><strong>%s Team</strong></p>
                        </div>
                        <div class="footer"><p>© %d %s. All rights reserved.</p></div>
                    </div>
                </body>
                </html>
                """.formatted(orderNumber, amount, paymentMethod, appName,
                LocalDateTime.now().getYear(), appName);
    }
}
