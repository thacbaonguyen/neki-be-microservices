package com.thacbao.productservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ColorRequest {
    @NotBlank(message = "Tên màu không được để trống")
    @Size(max = 50) private String name;

    @NotBlank(message = "Mã màu hex không được để trống")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Mã màu hex không hợp lệ")
    private String hexCode;
}
