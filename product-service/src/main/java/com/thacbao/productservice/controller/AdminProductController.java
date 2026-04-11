package com.thacbao.productservice.controller;

import com.thacbao.common.dto.response.ApiResponse;
import com.thacbao.productservice.dto.request.ProductFilterRequest;
import com.thacbao.productservice.dto.request.ProductImageRequest;
import com.thacbao.productservice.dto.request.ProductRequest;
import com.thacbao.productservice.dto.request.ProductVariantRequest;
import com.thacbao.productservice.dto.response.*;
import com.thacbao.productservice.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;

    // Admin listing — includes inactive products
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductListResponse>>> listProducts(
            ProductFilterRequest filter, Pageable pageable) {
        filter.setIncludeInactive(true);
        return ResponseEntity.ok(ApiResponse.success(productService.getProducts(filter, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProduct(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProductById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductDetailResponse>> createProduct(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(ApiResponse.success(productService.createProduct(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> updateProduct(
            @PathVariable Integer id, @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(ApiResponse.success(productService.updateProduct(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Integer id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<Void> toggleActive(@PathVariable Integer id) {
        productService.toggleProductActive(id);
        return ResponseEntity.ok().build();
    }

    // Images
    @PostMapping("/{id}/images")
    public ResponseEntity<ApiResponse<List<ProductImageResponse>>> addImages(
            @PathVariable Integer id,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "imageRequests", required = false) List<ProductImageRequest> imageRequests) {
        return ResponseEntity.ok(ApiResponse.success(productService.addProductImages(id, files, imageRequests != null ? imageRequests : List.of())));
    }

    @DeleteMapping("/images/{imageId}")
    public ResponseEntity<Void> deleteImage(@PathVariable Integer imageId) {
        productService.deleteProductImage(imageId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/images/{imageId}/order")
    public ResponseEntity<Void> updateImageOrder(@PathVariable Integer imageId, @RequestParam Integer displayOrder) {
        productService.updateProductImageOrder(imageId, displayOrder);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{productId}/images/{imageId}/primary")
    public ResponseEntity<Void> setPrimaryImage(@PathVariable Integer productId, @PathVariable Integer imageId) {
        productService.setPrimaryImage(productId, imageId);
        return ResponseEntity.ok().build();
    }

    // Variants
    @GetMapping("/{productId}/variants")
    public ResponseEntity<ApiResponse<List<ProductVariantResponse>>> getVariants(@PathVariable Integer productId) {
        return ResponseEntity.ok(ApiResponse.success(productService.getVariantsByProductId(productId)));
    }

    @PostMapping("/{productId}/variants")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> addVariant(
            @PathVariable Integer productId, @Valid @RequestBody ProductVariantRequest request) {
        return ResponseEntity.ok(ApiResponse.success(productService.addVariant(productId, request)));
    }

    @PutMapping("/variants/{variantId}")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> updateVariant(
            @PathVariable Integer variantId, @Valid @RequestBody ProductVariantRequest request) {
        return ResponseEntity.ok(ApiResponse.success(productService.updateVariant(variantId, request)));
    }

    @DeleteMapping("/variants/{variantId}")
    public ResponseEntity<Void> deleteVariant(@PathVariable Integer variantId) {
        productService.deleteVariant(variantId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/variants/{variantId}/status")
    public ResponseEntity<Void> toggleVariantStatus(@PathVariable Integer variantId) {
        productService.toggleVariantActive(variantId);
        return ResponseEntity.ok().build();
    }

    // Inventory
    @PatchMapping("/variants/{variantId}/inventory")
    public ResponseEntity<Void> updateInventory(@PathVariable Integer variantId, @RequestParam Integer quantity) {
        productService.updateInventory(variantId, quantity);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/variants/{variantId}/inventory/adjust")
    public ResponseEntity<Void> adjustInventory(
            @PathVariable Integer variantId,
            @RequestParam Integer quantity,
            @RequestParam(required = false, defaultValue = "Manual adjustment") String reason) {
        productService.adjustInventory(variantId, quantity);
        return ResponseEntity.ok().build();
    }
}
