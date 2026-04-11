package com.thacbao.productservice.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CollectionRequest {
    @NotBlank @Size(max = 100) private String name;
    @Size(max = 500) private String description;
    private Boolean isActive;
    private Set<Integer> subCategoryIds;
}
