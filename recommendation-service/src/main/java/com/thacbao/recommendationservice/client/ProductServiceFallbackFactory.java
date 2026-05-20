package com.thacbao.recommendationservice.client;

import com.thacbao.common.dto.response.ApiResponse;
import com.thacbao.recommendationservice.dto.ProductSimpleDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class ProductServiceFallbackFactory implements FallbackFactory<ProductServiceClient> {

    @Override
    public ProductServiceClient create(Throwable cause) {
        log.error("Product Service unavailable: {}", cause.getMessage());
        return new ProductServiceClient() {
            @Override
            public ApiResponse<ProductSimpleDTO> getProductById(Integer productId) {
                log.warn("Fallback: getProductById for id={}", productId);
                return ApiResponse.error(503, "Product Service unavailable");
            }

            @Override
            public ApiResponse<List<ProductSimpleDTO>> getProductsByIds(List<Integer> ids) {
                log.warn("Fallback: getProductsByIds for {} ids", ids.size());
                return ApiResponse.success(Collections.emptyList());
            }

            @Override
            public ApiResponse<List<ProductSimpleDTO>> getPopularProducts(int limit) {
                log.warn("Fallback: getPopularProducts limit={}", limit);
                return ApiResponse.success(Collections.emptyList());
            }

            @Override
            public ApiResponse<List<Integer>> getAllActiveProductIds() {
                log.warn("Fallback: getAllActiveProductIds");
                return ApiResponse.success(Collections.emptyList());
            }
        };
    }
}
