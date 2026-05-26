package com.thacbao.productservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.thacbao.productservice.model.Collection;
import com.thacbao.productservice.model.SubCategory;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CollectionResponse {
    private Integer id;
    private String name;
    private String slug;
    private String description;
    private Boolean isActive;
    private Set<Integer> subCategoryIds;
    private LocalDateTime createdAt;

    public static CollectionResponse from(Collection collection) {
        return CollectionResponse.builder()
                .id(collection.getId()).name(collection.getName()).slug(collection.getSlug())
                .description(collection.getDescription()).isActive(collection.getIsActive())
                .subCategoryIds(collection.getSubCategories() != null
                        ? collection.getSubCategories().stream().map(SubCategory::getId).collect(Collectors.toSet())
                        : Set.of())
                .createdAt(collection.getCreatedAt()).build();
    }
}
