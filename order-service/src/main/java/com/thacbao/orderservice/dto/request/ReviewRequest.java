package com.thacbao.orderservice.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewRequest {
    @NotNull(message = "ID không được để trống")
    private Integer productId;

    private Integer orderId;

    @NotNull(message = "Rating không được để trống")
    @Min(value = 1, message = "Rating phải lớn hơn 1")
    @Max(value = 5, message = "Rating phải nhỏ hơn 5")
    private Integer rating;

    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    @NotBlank(message = "Nội dung không được để trống")
    private String comment;
}
