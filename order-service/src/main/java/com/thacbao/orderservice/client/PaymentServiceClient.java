package com.thacbao.orderservice.client;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thacbao.common.dto.response.ApiResponse;
import com.thacbao.orderservice.dto.request.CreatePaymentRequest;
import com.thacbao.orderservice.dto.response.PaymentMethodResponse;
import com.thacbao.orderservice.dto.response.PaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "payment-service")
public interface PaymentServiceClient {

    @GetMapping("/api/v1/payment-method")
    ApiResponse<List<PaymentMethodResponse>> getPaymentMethods();

    @PostMapping("/internal/payments/create")
    ApiResponse<PaymentResponse> createPayment(@RequestBody CreatePaymentRequest request);

    @PostMapping("/internal/payments/create-link")
    ObjectNode createPaymentLink(@RequestBody CreatePaymentRequest request);
}
