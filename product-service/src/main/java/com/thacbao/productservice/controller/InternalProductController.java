package com.thacbao.productservice.controller;

import com.thacbao.common.dto.response.ApiResponse;
import com.thacbao.productservice.dto.request.ProductExportRequest;
import com.thacbao.productservice.dto.response.ProductExportCountResponse;
import com.thacbao.productservice.dto.response.ProductDetailResponse;
import com.thacbao.productservice.dto.response.ProductVariantResponse;
import com.thacbao.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Internal API for inter-service communication (called by other microservices via Feign, not by frontend).
 */
@RestController
@RequestMapping("/internal/products")
@RequiredArgsConstructor
public class InternalProductController {

    private final ProductService productService;

    @GetMapping("/variants")
    public ResponseEntity<ApiResponse<List<com.thacbao.common.dto.ProductVariantDTO>>> getVariantsByIds(@RequestParam List<Integer> ids) {
        return ResponseEntity.ok(ApiResponse.success(productService.getVariantsByIds(ids)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProductById(id)));
    }

    @PostMapping("/inventory/reserve")
    public ResponseEntity<Void> reserveInventory(@RequestBody List<Map<String, Integer>> items) {
        items.forEach(item -> productService.reserveInventory(item.get("variantId"), item.get("quantity")));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/inventory/confirm")
    public ResponseEntity<Void> confirmInventory(@RequestBody List<Map<String, Integer>> items) {
        items.forEach(item -> productService.confirmInventory(item.get("variantId"), item.get("quantity")));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/inventory/restore")
    public ResponseEntity<Void> restoreInventory(@RequestBody List<Map<String, Integer>> items) {
        items.forEach(item -> productService.restoreInventory(item.get("variantId"), item.get("quantity")));
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/rating")
    public ResponseEntity<Void> updateRating(
            @PathVariable Integer id,
            @RequestParam BigDecimal rating,
            @RequestParam Integer count) {
        productService.updateRating(id, rating, count);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/total-sold")
    public ResponseEntity<Void> updateTotalSold(
            @PathVariable Integer id,
            @RequestParam Integer additionalSold) {
        productService.updateTotalSold(id, additionalSold);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/batch")
    public ResponseEntity<ApiResponse<List<com.thacbao.productservice.dto.response.ProductSimpleDTO>>> getProductsByIds(@RequestParam List<Integer> ids) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProductsByIds(ids)));
    }

    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<com.thacbao.productservice.dto.response.ProductSimpleDTO>>> getPopularProducts(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.success(productService.getPopularProducts(limit)));
    }

    @GetMapping("/all-ids")
    public ResponseEntity<ApiResponse<List<Integer>>> getAllActiveProductIds() {
        return ResponseEntity.ok(ApiResponse.success(productService.getAllActiveProductIds()));
    }

    @GetMapping("/batch-detail")
    public ResponseEntity<ApiResponse<List<com.thacbao.productservice.dto.response.ProductListResponse>>> getProductsDetailByIds(@RequestParam List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success(java.util.Collections.emptyList()));
        }
        List<com.thacbao.productservice.model.Product> products = productService.getProductEntitiesByIds(ids);
        List<com.thacbao.productservice.dto.response.ProductListResponse> responses = products.stream()
                .map(com.thacbao.productservice.dto.response.ProductListResponse::from)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PostMapping("/export/count")
    public ResponseEntity<ApiResponse<ProductExportCountResponse>> countProductsForExport(
            @RequestBody(required = false) ProductExportRequest request) {
        ProductExportRequest resolved = request != null ? request : new ProductExportRequest();
        long rowCount = productService.countProductsForExport(resolved.resolvedFilters());
        return ResponseEntity.ok(ApiResponse.success(ProductExportCountResponse.builder()
                .rowCount(rowCount)
                .build()));
    }

    @PostMapping("/export/page")
    public ResponseEntity<ApiResponse<org.springframework.data.domain.Page<com.thacbao.productservice.dto.response.ProductExportRowResponse>>> getProductsForExport(
            @RequestBody(required = false) ProductExportRequest request) {
        ProductExportRequest resolved = request != null ? request : new ProductExportRequest();
        var page = productService.getProductsForExport(
                resolved.resolvedFilters(),
                PageRequest.of(resolved.resolvedPage(), resolved.resolvedSize()));
        return ResponseEntity.ok(ApiResponse.success(page));
    }

}
