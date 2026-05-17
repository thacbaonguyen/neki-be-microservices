package com.thacbao.paymentservice.service.impl;

import com.thacbao.common.enums.PaymentStatus;
import com.thacbao.common.event.PaymentCompletedEvent;
import com.thacbao.common.event.PaymentFailedEvent;
import com.thacbao.common.exception.NotFoundException;
import com.thacbao.paymentservice.dto.request.CreatePaymentRequest;
import com.thacbao.paymentservice.dto.response.PaymentResponse;
import com.thacbao.paymentservice.model.Payment;
import com.thacbao.paymentservice.model.PaymentMethod;
import com.thacbao.paymentservice.repository.PaymentMethodRepository;
import com.thacbao.paymentservice.repository.PaymentRepository;
import com.thacbao.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.PaymentLink;
import vn.payos.model.webhooks.WebhookData;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentEventPublisher eventPublisher;
    private final PayOS payOS;

    private static final String PAYOS_SUCCESS_CODE = "00";

    @Override
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        PaymentMethod paymentMethod = paymentMethodRepository.findById(request.getPaymentMethodId())
                .orElseThrow(() -> new NotFoundException("Payment method not found: " + request.getPaymentMethodId()));

        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .orderNumber(request.getOrderNumber())
                .userId(request.getUserId())
                .userEmail(request.getUserEmail())
                .paymentMethod(paymentMethod)
                .amount(request.getAmount())
                .status(PaymentStatus.PENDING)
                .build();

        return PaymentResponse.from(paymentRepository.save(payment));
    }

    @Override
    public PaymentResponse updateStatus(Integer paymentId, PaymentStatus status) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Payment not found: " + paymentId));
        payment.setStatus(status);
        if (status == PaymentStatus.PAID) {
            payment.setPaidAt(LocalDateTime.now());
        }
        return PaymentResponse.from(paymentRepository.save(payment));
    }

    @Override
    public void handlePayOSWebhook(WebhookData data) {
        log.info("Handling PayOS Webhook for order: {}", data.getOrderCode());
        Payment payment = paymentRepository.findByOrderNumber(String.valueOf(data.getOrderCode()))
                .orElseThrow(() -> new NotFoundException(
                        "Payment for order code " + data.getOrderCode() + " not found"));

        if (PAYOS_SUCCESS_CODE.equals(data.getCode())) {
            payment.setStatus(PaymentStatus.PAID);
            payment.setPaidAt(LocalDateTime.now());
            payment.setTransactionId(String.valueOf(data.getReference()));
            paymentRepository.save(payment);

            // Publish payment completed event → Order Service will update order status
            eventPublisher.publishPaymentCompleted(PaymentCompletedEvent.builder()
                    .orderNumber(payment.getOrderNumber())
                    .userId(payment.getUserId())
                    .userEmail(payment.getUserEmail())
                    .transactionId(payment.getTransactionId())
                    .amount(payment.getAmount())
                    .paymentMethod(payment.getPaymentMethod().getName())
                    .build());

            log.info("Payment for order {} completed, event published", payment.getOrderNumber());
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);

            // Publish payment failed event → Order Service will restore inventory
            eventPublisher.publishPaymentFailed(PaymentFailedEvent.builder()
                    .orderNumber(payment.getOrderNumber())
                    .userId(payment.getUserId())
                    .reason("PayOS payment failed with code: " + data.getCode())
                    .build());

            log.warn("Payment for order {} failed with code: {}", payment.getOrderNumber(), data.getCode());
        }
    }

    @Override
    public void confirmPayment(String orderNumber) {
        Payment payment = paymentRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new NotFoundException("Payment for order " + orderNumber + " not found"));

        // Idempotent: skip if already paid
        if (payment.getStatus() == PaymentStatus.PAID) {
            log.info("Payment for order {} already confirmed, skipping", orderNumber);
            return;
        }

        try {
            // Verify with PayOS API
            PaymentLink paymentInfo = payOS.paymentRequests().get(Long.parseLong(orderNumber));
            if ("PAID".equals(String.valueOf(paymentInfo.getStatus()))) {
                payment.setStatus(PaymentStatus.PAID);
                payment.setPaidAt(LocalDateTime.now());
                paymentRepository.save(payment);

                // Publish payment completed event
                eventPublisher.publishPaymentCompleted(PaymentCompletedEvent.builder()
                        .orderNumber(payment.getOrderNumber())
                        .userId(payment.getUserId())
                        .userEmail(payment.getUserEmail())
                        .transactionId(payment.getTransactionId())
                        .amount(payment.getAmount())
                        .paymentMethod(payment.getPaymentMethod().getName())
                        .build());

                log.info("Order {} confirmed via returnUrl callback", orderNumber);
            } else {
                log.warn("PayOS status for order {} is {}, not PAID", orderNumber, paymentInfo.getStatus());
            }
        } catch (Exception e) {
            log.error("Error verifying payment for order {}: {}", orderNumber, e.getMessage());
            throw new RuntimeException("Failed to verify payment with PayOS", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getByOrderNumber(String orderNumber) {
        Payment payment = paymentRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new NotFoundException("Payment for order " + orderNumber + " not found"));
        return PaymentResponse.from(payment);
    }
}
