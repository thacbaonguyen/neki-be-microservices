package com.thacbao.productservice.service;

import com.thacbao.productservice.dto.request.*;
import com.thacbao.productservice.dto.response.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {
    // === Admin CRUD ===
    ProductDetailResponse createProduct(ProductRequest request);
    ProductDetailResponse updateProduct(Integer id, ProductRequest request);
    void deleteProduct(Integer id);
    void toggleProductActive(Integer id);

    // === Product Images ===
    List<ProductImageResponse> addProductImages(Integer productId, List<MultipartFile> files, List<ProductImageRequest> imageRequests);
    void deleteProductImage(Integer imageId);
    void updateProductImageOrder(Integer imageId, Integer displayOrder);
    void setPrimaryImage(Integer productId, Integer imageId);

    // === Product Variants ===
    List<ProductVariantResponse> getVariantsByProductId(Integer productId);
    ProductVariantResponse addVariant(Integer productId, ProductVariantRequest request);
    ProductVariantResponse updateVariant(Integer variantId, ProductVariantRequest request);
    void deleteVariant(Integer variantId);
    void toggleVariantActive(Integer variantId);

    // === Inventory ===
    void updateInventory(Integer variantId, Integer quantity);
    void adjustInventory(Integer variantId, Integer quantity);
    void reserveInventory(Integer variantId, Integer quantity);
    void confirmInventory(Integer variantId, Integer quantity);
    void restoreInventory(Integer variantId, Integer quantity);

    // === Public Read ===
    ProductDetailResponse getProductById(Integer id);
    ProductDetailResponse getProductBySlug(String slug);
    Page<ProductListResponse> getProducts(ProductFilterRequest filter, Pageable pageable);
    Page<ProductListResponse> getFeaturedProducts(Pageable pageable);
    Page<ProductListResponse> getNewProducts(Pageable pageable);
    Page<ProductListResponse> getOnSaleProducts(Pageable pageable);
    Page<ProductListResponse> getBestSellers(Pageable pageable);
    List<ProductListResponse> getRelatedProducts(Integer productId, int limit);
    FilterOptionsResponse getFilterOptions(Integer categoryId);
    void incrementViewCount(Integer productId);

    // === Elasticsearch Search ===
    Page<ProductListResponse> searchProducts(ProductFilterRequest filter, Pageable pageable);

    // === Internal API (Feign) ===
    void updateRating(Integer productId, java.math.BigDecimal rating, Integer count);
    void updateTotalSold(Integer productId, Integer additionalSold);
    
    List<ProductSimpleDTO> getProductsByIds(List<Integer> ids);
    List<ProductSimpleDTO> getPopularProducts(int limit);
    List<Integer> getAllActiveProductIds();
    
    List<com.thacbao.common.dto.ProductVariantDTO> getVariantsByIds(List<Integer> variantIds);
    List<com.thacbao.productservice.model.Product> getProductEntitiesByIds(List<Integer> ids);
}
