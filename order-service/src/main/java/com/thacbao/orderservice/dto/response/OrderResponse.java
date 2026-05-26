package com.thacbao.orderservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.thacbao.orderservice.model.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderResponse {
    private Integer id;
    private String orderNumber;
    private Integer userId;
    private String userEmail;
    private String userFullName;
    private BigDecimal totalAmount;
    private BigDecimal shippingFee;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private String province;
    private String district;
    private String ward;
    private String addressDetail;
    private String note;
    private String status;
    private String phoneDelivery;
    private Integer paymentMethodId;
    private Set<OrderItemResponse> orderItems;
    private JsonNode paymentLink;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static OrderResponse from(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUserId())
                .userEmail(order.getUserEmail())
                .userFullName(order.getUserFullName())
                .totalAmount(order.getTotalAmount())
                .shippingFee(order.getShippingFee())
                .discountAmount(order.getDiscountAmount())
                .finalAmount(order.getFinalAmount())
                .province(order.getProvince())
                .district(order.getDistrict())
                .ward(order.getWard())
                .addressDetail(order.getAddressDetail())
                .note(order.getNote())
                .status(order.getStatus().getValue())
                .phoneDelivery(order.getPhoneDelivery())
                .paymentMethodId(order.getPaymentMethodId())
                .orderItems(order.getOrderItems() != null
                        ? order.getOrderItems().stream().map(OrderItemResponse::from).collect(Collectors.toSet())
                        : null)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
