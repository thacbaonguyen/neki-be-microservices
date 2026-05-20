package com.thacbao.paymentservice.controller;

import com.thacbao.common.dto.response.ApiResponse;
import com.thacbao.paymentservice.dto.request.PaymentMethodRequest;
import com.thacbao.paymentservice.service.PaymentMethodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/payment-method")
@RequiredArgsConstructor
public class AdminPaymentMethodController {

    private final PaymentMethodService paymentMethodService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> create(@Valid @RequestBody PaymentMethodRequest request) {
        paymentMethodService.create(request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> toggle(@PathVariable Integer id, @RequestParam boolean status) {
        paymentMethodService.update(id, status);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Integer id) {
        paymentMethodService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
