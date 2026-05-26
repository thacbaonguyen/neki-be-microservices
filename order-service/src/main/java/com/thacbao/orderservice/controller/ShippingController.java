package com.thacbao.orderservice.controller;

import com.thacbao.common.dto.response.ApiResponse;
import com.thacbao.orderservice.service.ShippingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/shipping")
@RequiredArgsConstructor
public class ShippingController {

    private final ShippingService shippingService;

    @GetMapping("/fee")
    public ResponseEntity<ApiResponse<BigDecimal>> calculateFee(
            @RequestParam String district,
            @RequestParam String ward,
            @RequestParam BigDecimal orderAmount) {
        BigDecimal fee = shippingService.calculateShippingFee(district, ward, orderAmount);
        return ResponseEntity.ok(ApiResponse.success(fee));
    }
}
