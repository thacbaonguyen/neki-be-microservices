package com.thacbao.productservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SizeRequest {
    @NotBlank @Size(max = 20) private String name;
    @NotBlank @Size(max = 50) private String categoryType;
    @Min(0) private Integer displayOrder;
}
