package com.thacbao.productservice.dto.request;

import com.thacbao.productservice.model.Product;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductFilterRequest {
    private Integer categoryId;
    private Integer subCategoryId;
    private Integer brandId;
    private Integer collectionId;
    private Integer topicId;

    private Product.Gender gender;

    @DecimalMin(value = "0.0", message = "Giá tối thiểu phải >= 0")
    private BigDecimal minPrice;

    @DecimalMin(value = "0.0", message = "Giá tối đa phải >= 0")
    private BigDecimal maxPrice;

    private List<Integer> colorIds;
    private List<Integer> sizeIds;

    private Boolean isFeatured;
    private Boolean isNew;
    private Boolean isOnSale;
    private Boolean isActive;
    private Boolean inStock;

    private String keyword;
    private String sortBy;
    private String sortDirection;
    private Boolean includeInactive;
}
