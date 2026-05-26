package com.thacbao.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCompletedEvent implements Serializable {
    private String orderNumber;
    private Integer userId;
    private String userEmail;
    private String userFullName;
    private String transactionId;
    private BigDecimal amount;
    private String paymentMethod;
}
