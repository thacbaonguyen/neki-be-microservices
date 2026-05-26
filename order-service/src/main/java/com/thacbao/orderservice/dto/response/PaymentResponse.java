package com.thacbao.orderservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Integer id;
    private String orderNumber;
    private BigDecimal amount;
    private String transactionId;
    private String status;
    private PaymentMethodResponse paymentMethod;
}
