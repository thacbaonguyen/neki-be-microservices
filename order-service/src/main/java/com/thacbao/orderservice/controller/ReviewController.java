package com.thacbao.orderservice.controller;

import com.thacbao.common.dto.response.ApiResponse;
import com.thacbao.orderservice.dto.request.ReviewRequest;
import com.thacbao.orderservice.dto.response.ReviewResponse;
import com.thacbao.orderservice.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReviewResponse>> create(
            @Valid @RequestBody ReviewRequest request,
            @RequestHeader("X-User-Id") Integer userId,
            @RequestHeader(value = "X-User-FullName", required = false) String userFullName) {
        return ResponseEntity.ok(ApiResponse.success(reviewService.create(request, userId, userFullName)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ReviewResponse>> update(
            @PathVariable Integer id,
            @Valid @RequestBody ReviewRequest request,
            @RequestHeader("X-User-Id") Integer userId) {
        return ResponseEntity.ok(ApiResponse.success(reviewService.update(id, request, userId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Integer id,
            @RequestHeader("X-User-Id") Integer userId) {
        reviewService.delete(id, userId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getByProduct(
            @PathVariable Integer productId, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(reviewService.getAllReviewByProduct(productId, pageable)));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getAllReviewsAdmin(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(reviewService.getAllReviewsAdmin(pageable)));
    }
}
