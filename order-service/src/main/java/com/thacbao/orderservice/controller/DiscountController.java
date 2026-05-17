package com.thacbao.orderservice.controller;

import com.thacbao.common.dto.response.ApiResponse;
import com.thacbao.orderservice.dto.response.DiscountResponse;
import com.thacbao.orderservice.service.DiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/discounts")
@RequiredArgsConstructor
public class DiscountController {

    private final DiscountService discountService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DiscountResponse>>> getByType(
            @RequestParam String type) {
        return ResponseEntity.ok(ApiResponse.success(discountService.getAllByType(type)));
    }
}
