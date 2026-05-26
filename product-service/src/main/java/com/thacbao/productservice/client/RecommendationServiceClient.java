package com.thacbao.productservice.client;

import com.thacbao.common.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "recommendation-service", fallback = RecommendationServiceFallback.class)
public interface RecommendationServiceClient {

    @GetMapping("/api/v1/recommendations/similar/{productId}")
    ApiResponse<Map<String, Object>> getSimilarProducts(
            @PathVariable("productId") Integer productId,
            @RequestParam("page") int page,
            @RequestParam("size") int size);
}
