package com.thacbao.recommendationservice.controller;

import com.thacbao.common.dto.response.ApiResponse;
import com.thacbao.recommendationservice.dto.ProductSimpleDTO;
import com.thacbao.recommendationservice.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/similar/{productId}")
    public ResponseEntity<ApiResponse<Page<ProductSimpleDTO>>> getSimilarProducts(
            @PathVariable Integer productId, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                recommendationService.getSimilarProducts(productId, pageable)));
    }

    @GetMapping("/for-you")
    public ResponseEntity<ApiResponse<List<ProductSimpleDTO>>> getRecommendedForYou(
            @RequestHeader(value = "X-User-Id", required = false) Integer userId,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.success(
                recommendationService.getRecommendedForYou(userId, limit)));
    }

    @PostMapping("/recalculate")
    public ResponseEntity<ApiResponse<Void>> recalculate() {
        recommendationService.calculateSimilarities();
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
