package com.thacbao.productservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
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
public class SubCategoryResponse {
    private Integer id;
    private Integer categoryId;
    private String categoryName;
    private Integer parentId;
    private String name;
    private String slug;
    private String description;
    private Integer level;
    private Integer displayOrder;
    private Boolean isActive;
    private List<SubCategoryResponse> children;
    private LocalDateTime createdAt;

    public static SubCategoryResponse from(SubCategory subCategory) {
        return SubCategoryResponse.builder()
                .id(subCategory.getId())
                .categoryId(subCategory.getCategory().getId())
                .categoryName(subCategory.getCategory().getName())
                .parentId(subCategory.getParent() != null ? subCategory.getParent().getId() : null)
                .name(subCategory.getName())
                .slug(subCategory.getSlug())
                .description(subCategory.getDescription())
                .level(subCategory.getLevel())
                .displayOrder(subCategory.getDisplayOrder())
                .isActive(subCategory.getIsActive())
                .createdAt(subCategory.getCreatedAt())
                .build();
    }

    public static SubCategoryResponse fromWithChildren(SubCategory subCategory) {
        SubCategoryResponse response = from(subCategory);
        if (subCategory.getChildren() != null && !subCategory.getChildren().isEmpty()) {
            response.setChildren(
                    subCategory.getChildren().stream()
                            .sorted(Comparator.comparing(SubCategory::getDisplayOrder))
                            .map(SubCategoryResponse::fromWithChildren)
                            .collect(Collectors.toList())
            );
        }
        return response;
    }
}
