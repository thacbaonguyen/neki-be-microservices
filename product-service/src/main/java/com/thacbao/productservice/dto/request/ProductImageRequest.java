package com.thacbao.productservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageRequest {
    @NotBlank(message = "URL ảnh không được để trống")
    private String imageUrl;
    private Integer colorId;
    @Min(value = 0, message = "Thứ tự hiển thị phải >= 0")
    private Integer displayOrder;
    private Boolean isPrimary;
}
