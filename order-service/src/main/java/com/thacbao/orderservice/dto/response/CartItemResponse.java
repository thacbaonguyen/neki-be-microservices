package com.thacbao.orderservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.thacbao.orderservice.model.CartItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartItemResponse {
    private Integer id;
    private Integer variantId;
    private Integer quantity;

    // These will be enriched via Feign call to Product Service
    private String productName;
    private String productSlug;
    private String productImage;
    private String colorName;
    private String sizeName;
    private String subCategoryName;
    private java.math.BigDecimal price;
    private Integer stockQuantity;

    public static CartItemResponse from(CartItem cartItem) {
        return CartItemResponse.builder()
                .id(cartItem.getId())
                .variantId(cartItem.getVariantId())
                .quantity(cartItem.getQuantity())
                .build();
    }
}
