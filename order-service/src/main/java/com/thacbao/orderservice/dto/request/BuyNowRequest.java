package com.thacbao.orderservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuyNowRequest {
    OrderRequest orderRequest;
    OrderItemRequest orderItemRequest;
}
