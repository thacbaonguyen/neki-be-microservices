package com.thacbao.orderservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.thacbao.orderservice.model.Discount;
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
public class DiscountResponse {
    private Integer id;
    private String name;
    private String code;
    private String description;
    private String discountType;
    private Boolean isActive;
    private Integer percent;
    private BigDecimal reduceAmount;
    private Integer usageLimit;
    private Integer usedCount;
    private Integer userUsageLimit;
    private BigDecimal minOrderAmount;
    private java.time.LocalDate startDate;
    private java.time.LocalDate endDate;
    private LocalDateTime createdAt;

    public static DiscountResponse from(Discount discount) {
        return DiscountResponse.builder()
                .id(discount.getId())
                .name(discount.getName())
                .code(discount.getCode())
                .description(discount.getDescription())
                .discountType(discount.getDiscountType().getValue())
                .isActive(discount.isActive())
                .percent(discount.getPercent())
                .reduceAmount(discount.getReduceAmount())
                .usageLimit(discount.getUsageLimit())
                .usedCount(discount.getUsedCount())
                .userUsageLimit(discount.getUserUsageLimit())
                .minOrderAmount(discount.getMinOrderAmount())
                .startDate(discount.getStartDate())
                .endDate(discount.getEndDate())
                .createdAt(discount.getCreatedAt())
                .build();
    }
}
