package com.thacbao.paymentservice.service;

import com.thacbao.common.enums.PaymentStatus;
import com.thacbao.paymentservice.dto.request.CreatePaymentRequest;
import com.thacbao.paymentservice.dto.response.PaymentResponse;
import vn.payos.model.webhooks.WebhookData;

public interface PaymentService {
    PaymentResponse createPayment(CreatePaymentRequest request);

    PaymentResponse updateStatus(Integer paymentId, PaymentStatus status);

    void handlePayOSWebhook(WebhookData data);

    void confirmPayment(String orderNumber);

    PaymentResponse getByOrderNumber(String orderNumber);
}
