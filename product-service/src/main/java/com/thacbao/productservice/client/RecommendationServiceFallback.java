package com.thacbao.productservice.client;

import com.thacbao.common.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

@Component
@Slf4j
public class RecommendationServiceFallback implements RecommendationServiceClient {

    @Override
    public ApiResponse<Map<String, Object>> getSimilarProducts(Integer productId, int page, int size) {
        log.warn("Fallback: getSimilarProducts for productId={}", productId);
        return ApiResponse.success(Collections.emptyMap());
    }
}
