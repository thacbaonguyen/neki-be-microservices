package com.thacbao.productservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.thacbao.productservice.model.Category;
import com.thacbao.productservice.model.SubCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CategoryResponse {
    private Integer id;
    private String name;
    private String slug;
    private String description;
    private Integer displayOrder;
    private Boolean isActive;
    private List<SubCategoryResponse> subCategories;
    private LocalDateTime createdAt;

    public static CategoryResponse from(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .displayOrder(category.getDisplayOrder())
                .isActive(category.getIsActive())
                .createdAt(category.getCreatedAt())
                .build();
    }

    public static CategoryResponse fromWithSubCategories(Category category) {
        CategoryResponse response = from(category);
        if (category.getSubCategories() != null) {
            response.setSubCategories(
                    category.getSubCategories().stream()
                            .sorted(Comparator.comparing(SubCategory::getDisplayOrder))
                            .map(SubCategoryResponse::from)
                            .collect(Collectors.toList())
            );
        }
        return response;
    }
}
