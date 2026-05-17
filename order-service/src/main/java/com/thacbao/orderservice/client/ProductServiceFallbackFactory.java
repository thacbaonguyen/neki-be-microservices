package com.thacbao.orderservice.client;

import com.thacbao.common.dto.ProductVariantDTO;
import com.thacbao.common.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ProductServiceFallbackFactory implements FallbackFactory<ProductServiceClient> {

    @Override
    public ProductServiceClient create(Throwable cause) {
        log.error("Product Service unavailable: {}", cause.getMessage());
        return new ProductServiceClient() {
            @Override
            public ApiResponse<List<ProductVariantDTO>> getVariantsByIds(List<Integer> ids) {
                log.warn("Fallback: getVariantsByIds for ids={}", ids);
                return ApiResponse.error(503, "Product Service unavailable");
            }

            @Override
            public void reserveInventory(List<Map<String, Integer>> items) {
                log.warn("Fallback: reserveInventory");
            }

            @Override
            public void confirmInventory(List<Map<String, Integer>> items) {
                log.warn("Fallback: confirmInventory");
            }

            @Override
            public void restoreInventory(List<Map<String, Integer>> items) {
                log.warn("Fallback: restoreInventory");
            }

            @Override
            public void updateTotalSold(Integer productId, Integer additionalSold) {
                log.warn("Fallback: updateTotalSold for productId={}", productId);
            }

            @Override
            public void updateRating(Integer productId, BigDecimal rating, Integer count) {
                log.warn("Fallback: updateRating for productId={}", productId);
            }

            @Override
            public ApiResponse<List<Map<String, Object>>> getProductsDetailByIds(List<Integer> ids) {
                log.warn("Fallback: getProductsDetailByIds for ids={}", ids);
                return ApiResponse.error(503, "Product Service unavailable");
            }
        };
    }
}
