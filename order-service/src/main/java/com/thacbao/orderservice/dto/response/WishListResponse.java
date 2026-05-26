package com.thacbao.orderservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.thacbao.orderservice.model.Wishlist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WishListResponse {
    private Integer id;
    private Integer userId;
    private LocalDateTime createdAt;
    private List<Integer> productIds;
    // Enriched product data for frontend
    private List<Map<String, Object>> product;

    public static WishListResponse from(Wishlist wishlist) {
        return WishListResponse.builder()
                .id(wishlist.getId())
                .userId(wishlist.getUserId())
                .createdAt(wishlist.getCreatedAt())
                .productIds(wishlist.getProductIds())
                .build();
    }
}
