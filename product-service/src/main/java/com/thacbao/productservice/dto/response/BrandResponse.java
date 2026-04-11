package com.thacbao.productservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.thacbao.productservice.model.Brand;
import lombok.*;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BrandResponse {
    private Integer id;
    private String name;
    private String description;
    private Boolean isActive;
    private LocalDateTime createdAt;

    public static BrandResponse from(Brand brand) {
        return BrandResponse.builder().id(brand.getId()).name(brand.getName())
                .description(brand.getDescription()).isActive(brand.getIsActive()).createdAt(brand.getCreatedAt()).build();
    }
}
