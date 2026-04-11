package com.thacbao.productservice.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TopicRequest {
    @NotBlank @Size(max = 100) private String name;
    @Size(max = 500) private String description;
    private Boolean isActive;
}
