package com.thacbao.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCancelledEvent implements Serializable {
    private String orderNumber;
    private Integer userId;
    private String userEmail;
    private String userFullName;
    private String reason;
    private List<CancelledItemEvent> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CancelledItemEvent implements Serializable {
        private Integer variantId;
        private Integer quantity;
    }
}
