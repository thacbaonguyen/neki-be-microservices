package com.thacbao.orderservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.thacbao.orderservice.model.Order;
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
public class OrderSummaryResponse {
    private Integer id;
    private String status;
    private String orderNumber;
    private BigDecimal finalAmount;
    private Integer totalItems;
    private LocalDateTime createdAt;

    public static OrderSummaryResponse from(Order order) {
        return OrderSummaryResponse.builder()
                .id(order.getId())
                .status(order.getStatus().getValue())
                .orderNumber(order.getOrderNumber())
                .finalAmount(order.getFinalAmount())
                .totalItems(order.getOrderItems() != null ? order.getOrderItems().size() : 0)
                .createdAt(order.getCreatedAt())
                .build();
    }
}
