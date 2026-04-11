package com.thacbao.productservice.controller;

import com.thacbao.common.dto.response.ApiResponse;
import com.thacbao.productservice.dto.request.ProductFilterRequest;
import com.thacbao.productservice.dto.response.FilterOptionsResponse;
import com.thacbao.productservice.dto.response.ProductDetailResponse;
import com.thacbao.productservice.dto.response.ProductListResponse;
import com.thacbao.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductListResponse>>> getProducts(
            @ModelAttribute ProductFilterRequest filter, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProducts(filter, pageable)));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProductBySlug(slug)));
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProductById(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProductById(id)));
    }

    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<Page<ProductListResponse>>> getFeaturedProducts(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(productService.getFeaturedProducts(pageable)));
    }

    @GetMapping("/new")
    public ResponseEntity<ApiResponse<Page<ProductListResponse>>> getNewProducts(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(productService.getNewProducts(pageable)));
    }

    @GetMapping("/sale")
    public ResponseEntity<ApiResponse<Page<ProductListResponse>>> getOnSaleProducts(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(productService.getOnSaleProducts(pageable)));
    }

    @GetMapping("/best-sellers")
    public ResponseEntity<ApiResponse<Page<ProductListResponse>>> getBestSellers(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(productService.getBestSellers(pageable)));
    }

    @GetMapping("/{id}/related")
    public ResponseEntity<ApiResponse<List<ProductListResponse>>> getRelatedProducts(
            @PathVariable Integer id, @RequestParam(defaultValue = "8") int limit) {
        return ResponseEntity.ok(ApiResponse.success(productService.getRelatedProducts(id, limit)));
    }

    @GetMapping("/filter-options")
    public ResponseEntity<ApiResponse<FilterOptionsResponse>> getFilterOptions(
            @RequestParam(required = false) Integer categoryId) {
        return ResponseEntity.ok(ApiResponse.success(productService.getFilterOptions(categoryId)));
    }

    @PostMapping("/{id}/view")
    public ResponseEntity<Void> incrementViewCount(@PathVariable Integer id) {
        productService.incrementViewCount(id);
        return ResponseEntity.ok().build();
    }
}
