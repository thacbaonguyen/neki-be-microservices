package com.thacbao.orderservice.client;

import com.thacbao.common.dto.ProductVariantDTO;
import com.thacbao.common.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@FeignClient(name = "product-service", fallbackFactory = ProductServiceFallbackFactory.class)
public interface ProductServiceClient {

    @GetMapping("/internal/products/variants")
    ApiResponse<List<ProductVariantDTO>> getVariantsByIds(@RequestParam("ids") List<Integer> ids);

    @PostMapping("/internal/products/inventory/reserve")
    void reserveInventory(@RequestBody List<Map<String, Integer>> items);

    @PostMapping("/internal/products/inventory/confirm")
    void confirmInventory(@RequestBody List<Map<String, Integer>> items);

    @PostMapping("/internal/products/inventory/restore")
    void restoreInventory(@RequestBody List<Map<String, Integer>> items);

    @PutMapping("/internal/products/{productId}/total-sold")
    void updateTotalSold(@PathVariable("productId") Integer productId, @RequestParam("additionalSold") Integer additionalSold);

    @PutMapping("/internal/products/{productId}/rating")
    void updateRating(@PathVariable("productId") Integer productId,
                      @RequestParam("rating") BigDecimal rating,
                      @RequestParam("count") Integer count);

    @GetMapping("/internal/products/batch-detail")
    ApiResponse<List<Map<String, Object>>> getProductsDetailByIds(@RequestParam("ids") List<Integer> ids);
}
