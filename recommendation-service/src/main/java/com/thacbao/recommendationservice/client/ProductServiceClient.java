package com.thacbao.recommendationservice.client;

import com.thacbao.common.dto.response.ApiResponse;
import com.thacbao.recommendationservice.dto.ProductSimpleDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "product-service", fallbackFactory = ProductServiceFallbackFactory.class)
public interface ProductServiceClient {

    @GetMapping("/internal/products/{productId}")
    ApiResponse<ProductSimpleDTO> getProductById(@PathVariable Integer productId);

    @GetMapping("/internal/products/batch")
    ApiResponse<List<ProductSimpleDTO>> getProductsByIds(@RequestParam List<Integer> ids);

    @GetMapping("/internal/products/popular")
    ApiResponse<List<ProductSimpleDTO>> getPopularProducts(@RequestParam int limit);

    @GetMapping("/internal/products/all-ids")
    ApiResponse<List<Integer>> getAllActiveProductIds();
}
