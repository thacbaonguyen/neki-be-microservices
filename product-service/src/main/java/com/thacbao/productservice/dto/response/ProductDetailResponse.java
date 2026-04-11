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
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductDetailResponse {
    private Integer id;
    private String name;
    private String excerpt;
    private String slug;
    private String description;
    private SubCategoryResponse subCategory;
    private BrandResponse brand;
    private BigDecimal basePrice;
    private BigDecimal salePrice;
    private BigDecimal currentPrice;
    private BigDecimal discountPercentage;
    private Boolean isOnSale;
    private String gender;
    private Boolean isFeatured;
    private Boolean isNew;
    private Boolean isActive;
    private String metaTitle;
    private String metaDescription;
    private String metaKeywords;
    private BigDecimal averageRating;
    private Integer reviewCount;
    private Integer totalSold;
    private Long viewCount;
    private List<CollectionResponse> collections;
    private List<TopicResponse> topics;
    private List<ProductImageResponse> images;
    private List<ProductVariantResponse> variants;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProductDetailResponse from(Product product) {
        return ProductDetailResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .excerpt(product.getExcerpt())
                .slug(product.getSlug())
                .description(product.getDescription())
                .subCategory(SubCategoryResponse.from(product.getSubCategory()))
                .brand(BrandResponse.from(product.getBrand()))
                .basePrice(product.getBasePrice())
                .salePrice(product.getSalePrice())
                .currentPrice(product.getCurrentPrice())
                .discountPercentage(product.getDiscountPercentage())
                .isOnSale(product.isOnSale())
                .gender(product.getGender().getValue())
                .isFeatured(product.getIsFeatured())
                .isNew(product.getIsNew())
                .isActive(product.getIsActive())
                .metaTitle(product.getMetaTitle())
                .metaDescription(product.getMetaDescription())
                .metaKeywords(product.getMetaKeywords())
                .averageRating(product.getAverageRating())
                .reviewCount(product.getReviewCount())
                .totalSold(product.getTotalSold())
                .viewCount(product.getViewCount())
                .collections(product.getCollections().stream().map(CollectionResponse::from).collect(Collectors.toList()))
                .topics(product.getTopics().stream().map(TopicResponse::from).collect(Collectors.toList()))
                .images(product.getImages().stream()
                        .sorted(Comparator.comparing(ProductImage::getDisplayOrder))
                        .map(ProductImageResponse::from).collect(Collectors.toList()))
                .variants(product.getVariants().stream()
                        .map(v -> ProductVariantResponse.from(v, product.getCurrentPrice())).collect(Collectors.toList()))
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
