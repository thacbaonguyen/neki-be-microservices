package com.thacbao.paymentservice.controller;

import com.thacbao.common.dto.response.ApiResponse;
import com.thacbao.paymentservice.dto.response.PaymentMethodResponse;
import com.thacbao.paymentservice.service.PaymentMethodService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payment-method")
@RequiredArgsConstructor
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentMethodResponse>>> getPaymentMethods() {
        return ResponseEntity.ok(ApiResponse.success(paymentMethodService.getAll()));
    }
}
