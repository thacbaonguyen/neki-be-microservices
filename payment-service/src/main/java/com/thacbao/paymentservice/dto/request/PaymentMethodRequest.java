package com.thacbao.paymentservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodRequest {
    @NotBlank(message = "Tên phương thức thanh toán không được để trống")
    private String name;

    @NotBlank(message = "Mô tả không được để trống")
    @Size(max = 300, message = "Mô tả không được quá 300 ký tự")
    private String description;
}
