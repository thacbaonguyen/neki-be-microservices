package com.thacbao.productservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductExportRequest {
    private ProductFilterRequest filters;
    private String sortBy;
    private String sortDirection;
    private Integer page;
    private Integer size;

    public ProductFilterRequest resolvedFilters() {
        ProductFilterRequest resolved = filters != null ? filters : new ProductFilterRequest();
        resolved.setIncludeInactive(true);
        if (sortBy != null && !sortBy.isBlank()) {
            resolved.setSortBy(sortBy);
        }
        if (sortDirection != null && !sortDirection.isBlank()) {
            resolved.setSortDirection(sortDirection);
        }
        return resolved;
    }

    public int resolvedPage() {
        return page != null && page >= 0 ? page : 0;
    }

    public int resolvedSize() {
        if (size == null || size <= 0) {
            return 500;
        }
        return Math.min(size, 1000);
    }
}
