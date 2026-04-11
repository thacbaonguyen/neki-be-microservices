package com.thacbao.productservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantRequest {
    @NotNull(message = "Color ID không được để trống")
    private Integer colorId;

    @NotNull(message = "Size ID không được để trống")
    private Integer sizeId;

    @DecimalMin(value = "0.0", message = "Giá bổ sung phải >= 0")
    private BigDecimal additionalPrice;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 0, message = "Số lượng phải >= 0")
    private Integer quantity;

    private Boolean isActive;
}
