package com.thacbao.paymentservice.controller;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.thacbao.common.dto.response.ApiResponse;
import com.thacbao.paymentservice.dto.request.CreatePaymentRequest;
import com.thacbao.paymentservice.dto.response.PaymentResponse;
import com.thacbao.paymentservice.service.PaymentLinkService;
import com.thacbao.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/payments")
@RequiredArgsConstructor
public class InternalPaymentController {

    private final PaymentService paymentService;
    private final PaymentLinkService paymentLinkService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(
            @RequestBody CreatePaymentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.createPayment(request)));
    }

    @PostMapping("/create-link")
    public ObjectNode createPaymentLink(@RequestBody CreatePaymentRequest request) {
        return paymentLinkService.createPaymentLink(request);
    }

    @GetMapping("/order/{orderNumber}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getByOrderNumber(
            @PathVariable String orderNumber) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getByOrderNumber(orderNumber)));
    }
}
