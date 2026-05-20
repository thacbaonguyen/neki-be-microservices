package com.thacbao.paymentservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.thacbao.common.enums.PaymentStatus;
import com.thacbao.paymentservice.model.Payment;
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
public class PaymentResponse {
    private Integer id;
    private String orderNumber;
    private BigDecimal amount;
    private String transactionId;
    private PaymentStatus status;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private PaymentMethodResponse paymentMethod;

    public static PaymentResponse from(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderNumber(payment.getOrderNumber())
                .amount(payment.getAmount())
                .transactionId(payment.getTransactionId())
                .status(payment.getStatus())
                .paidAt(payment.getPaidAt())
                .createdAt(payment.getCreatedAt())
                .paymentMethod(payment.getPaymentMethod() != null
                        ? PaymentMethodResponse.from(payment.getPaymentMethod())
                        : null)
                .build();
    }
}
