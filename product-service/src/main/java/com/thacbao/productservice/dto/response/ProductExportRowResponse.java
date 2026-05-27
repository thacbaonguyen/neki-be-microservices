package com.thacbao.productservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.thacbao.productservice.model.Product;
import com.thacbao.productservice.model.ProductImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductExportRowResponse {
    private Integer id;
    private String name;
    private String slug;
    private String categoryName;
    private String subCategoryName;
    private String brandName;
    private BigDecimal basePrice;
    private BigDecimal salePrice;
    private BigDecimal currentPrice;
    private String gender;
    private Boolean isFeatured;
    private Boolean isNew;
    private Boolean isActive;
    private Boolean isOnSale;
    private Boolean inStock;
    private Integer totalSold;
    private BigDecimal averageRating;
    private Integer reviewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String primaryImage;

    public static ProductExportRowResponse from(Product product) {
        return ProductExportRowResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .categoryName(product.getSubCategory().getCategory().getName())
                .subCategoryName(product.getSubCategory().getName())
                .brandName(product.getBrand().getName())
                .basePrice(product.getBasePrice())
                .salePrice(product.getSalePrice())
                .currentPrice(product.getCurrentPrice())
                .gender(product.getGender() != null ? product.getGender().getValue() : null)
                .isFeatured(product.getIsFeatured())
                .isNew(product.getIsNew())
                .isActive(product.getIsActive())
                .isOnSale(product.isOnSale())
                .inStock(product.getVariants().stream()
                        .anyMatch(v -> v.getInventory() != null
                                && v.getInventory().getQuantity() > v.getInventory().getReservedQuantity()))
                .totalSold(product.getTotalSold())
                .averageRating(product.getAverageRating())
                .reviewCount(product.getReviewCount())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .primaryImage(product.getImages().stream()
                        .filter(ProductImage::getIsPrimary)
                        .findFirst()
                        .map(ProductImage::getImageUrl)
                        .orElse(product.getImages().stream()
                                .findFirst()
                                .map(ProductImage::getImageUrl)
                                .orElse(null)))
                .build();
    }
}
