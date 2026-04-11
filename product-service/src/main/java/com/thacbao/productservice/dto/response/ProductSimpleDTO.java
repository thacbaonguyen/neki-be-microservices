package com.thacbao.productservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.thacbao.productservice.model.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductSimpleDTO {
    private Integer id;
    private String name;
    private String imageUrl;
    private BigDecimal price;
    private BigDecimal averageRating;
    private Integer totalSold;
    private Boolean isActive;
    private String slug;

    public static ProductSimpleDTO from(Product product) {
        String primaryImgUrl = product.getImages().stream()
                .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                .map(img -> img.getImageUrl())
                .findFirst()
                .orElse(product.getImages().isEmpty() ? null : product.getImages().iterator().next().getImageUrl());

        return ProductSimpleDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .imageUrl(primaryImgUrl)
                .price(product.getCurrentPrice())
                .averageRating(product.getAverageRating())
                .totalSold(product.getTotalSold())
                .isActive(product.getIsActive())
                .slug(product.getSlug())
                .build();
    }
}
