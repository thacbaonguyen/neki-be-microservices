package com.thacbao.productservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.thacbao.productservice.model.Product;
import com.thacbao.productservice.model.ProductImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductListResponse {
    private Integer id;
    private String name;
    private String excerpt;
    private String slug;
    private String categoryName;
    private String subCategoryName;
    private String brandName;
    private BigDecimal basePrice;
    private BigDecimal salePrice;
    private BigDecimal currentPrice;
    private BigDecimal discountPercentage;
    private Boolean isOnSale;
    private String gender;
    private Boolean isFeatured;
    private Boolean isNew;
    private Boolean isActive;
    private String primaryImage;
    private BigDecimal averageRating;
    private Integer reviewCount;
    private Integer totalSold;
    private Boolean inStock;
    private List<String> availableColors;

    public static ProductListResponse from(Product product) {
        return ProductListResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .excerpt(product.getExcerpt())
                .slug(product.getSlug())
                .categoryName(product.getSubCategory().getCategory().getName())
                .subCategoryName(product.getSubCategory().getName())
                .brandName(product.getBrand().getName())
                .basePrice(product.getBasePrice())
                .salePrice(product.getSalePrice())
                .currentPrice(product.getCurrentPrice())
                .discountPercentage(product.getDiscountPercentage())
                .isOnSale(product.isOnSale())
                .gender(product.getGender().getValue())
                .isFeatured(product.getIsFeatured())
                .isNew(product.getIsNew())
                .isActive(product.getIsActive())
                .primaryImage(getPrimaryImage(product))
                .averageRating(product.getAverageRating())
                .reviewCount(product.getReviewCount())
                .totalSold(product.getTotalSold())
                .inStock(hasStock(product))
                .availableColors(getAvailableColors(product))
                .build();
    }

    private static String getPrimaryImage(Product product) {
        return product.getImages().stream()
                .filter(ProductImage::getIsPrimary)
                .findFirst()
                .map(ProductImage::getImageUrl)
                .orElse(product.getImages().stream().findFirst().map(ProductImage::getImageUrl).orElse(null));
    }

    private static Boolean hasStock(Product product) {
        return product.getVariants().stream()
                .anyMatch(v -> v.getInventory() != null &&
                        v.getInventory().getQuantity() > v.getInventory().getReservedQuantity());
    }

    private static List<String> getAvailableColors(Product product) {
        return product.getVariants().stream()
                .map(v -> v.getColor().getName())
                .distinct()
                .collect(Collectors.toList());
    }
}
