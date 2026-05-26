package com.thacbao.orderservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentRequest {
    private Integer orderId;
    private String orderNumber;
    private Integer paymentMethodId;
    private BigDecimal amount;
    private Integer userId;
    private String userEmail;
    private String userFullName;
    private String phoneDelivery;
    private String shippingAddress;
    private List<PaymentItemInfo> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentItemInfo {
        private String name;
        private long price;
        private int quantity;
    }
}
