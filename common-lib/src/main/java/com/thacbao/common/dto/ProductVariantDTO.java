package com.thacbao.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantDTO {
    private Integer id;
    private Integer productId;
    private String productName;
    private String colorName;
    private String sizeName;
    private BigDecimal price;
    private BigDecimal salePrice;
    private Integer stockQuantity;
    private String imageUrl;
    private Boolean isActive;
}
