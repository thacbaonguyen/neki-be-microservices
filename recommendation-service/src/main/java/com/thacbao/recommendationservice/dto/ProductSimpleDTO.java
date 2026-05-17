package com.thacbao.recommendationservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductSimpleDTO {
    private Integer id;
    private String name;
    private String imageUrl;
    private BigDecimal price;
    private Double averageRating;
    private Integer totalSold;
    private Boolean isActive;
    private String slug;
}
