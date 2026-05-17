package com.thacbao.orderservice.controller;

import com.thacbao.common.dto.response.ApiResponse;
import com.thacbao.orderservice.dto.response.InteractionBatchDTO;
import com.thacbao.orderservice.dto.response.UserInteractionDTO;
import com.thacbao.orderservice.service.InteractionExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/orders")
@RequiredArgsConstructor
public class InteractionInternalController {

    private final InteractionExportService interactionExportService;

    @GetMapping("/user/{userId}/interaction-products")
    public ResponseEntity<ApiResponse<UserInteractionDTO>> getUserInteractionProducts(@PathVariable Integer userId) {
        return ResponseEntity.ok(ApiResponse.success(interactionExportService.getUserInteractionProducts(userId)));
    }

    @GetMapping("/interactions/reviews")
    public ResponseEntity<ApiResponse<InteractionBatchDTO>> getReviewInteractions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "500") int size) {
        return ResponseEntity.ok(ApiResponse.success(interactionExportService.getReviewInteractions(page, size)));
    }

    @GetMapping("/interactions/orders")
    public ResponseEntity<ApiResponse<InteractionBatchDTO>> getOrderInteractions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "500") int size) {
        return ResponseEntity.ok(ApiResponse.success(interactionExportService.getOrderInteractions(page, size)));
    }

    @GetMapping("/interactions/wishlists")
    public ResponseEntity<ApiResponse<InteractionBatchDTO>> getWishlistInteractions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "500") int size) {
        return ResponseEntity.ok(ApiResponse.success(interactionExportService.getWishlistInteractions(page, size)));
    }
}
