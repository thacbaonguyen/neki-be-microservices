package com.thacbao.orderservice.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscountRequest {
    @NotBlank(message = "Tên mã giảm giá không được để trống")
    private String name;

    @NotBlank(message = "Mã giảm giá không được để trống")
    private String code;

    private Integer percent;
    private BigDecimal reduceAmount;

    @NotBlank(message = "Loại mã giảm giá không được để trống")
    private String discountType;

    @NotBlank(message = "Mô tả không được để trống")
    private String description;

    @JsonProperty("isActive")
    private boolean active;

    private Integer usageLimit;
    private Integer userUsageLimit;
    private BigDecimal minOrderAmount;
    private LocalDate startDate;
    private LocalDate endDate;
}
