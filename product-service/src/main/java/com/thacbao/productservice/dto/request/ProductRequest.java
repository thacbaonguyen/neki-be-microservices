package com.thacbao.productservice.dto.request;

import com.thacbao.productservice.model.Product;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {
    @NotNull(message = "SubCategory ID không được để trống")
    private Integer subCategoryId;

    @NotNull(message = "Brand ID không được để trống")
    private Integer brandId;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 255, message = "Tên sản phẩm tối đa 255 ký tự")
    private String name;

    @Size(max = 100, message = "excerpt sản phẩm tối đa 100 ký tự")
    private String excerpt;

    @Size(max = 2000, message = "Mô tả tối đa 2000 ký tự")
    private String description;

    @NotNull(message = "Giá gốc không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá gốc phải > 0")
    private BigDecimal basePrice;

    @DecimalMin(value = "0.0", message = "Giá sale phải >= 0")
    private BigDecimal salePrice;

    @NotNull(message = "Giới tính không được để trống")
    private Product.Gender gender;

    private Boolean isFeatured;
    private Boolean isNew;
    private Boolean isActive;

    @Size(max = 255, message = "Meta title tối đa 255 ký tự")
    private String metaTitle;

    @Size(max = 500, message = "Meta description tối đa 500 ký tự")
    private String metaDescription;

    @Size(max = 500, message = "Meta keywords tối đa 500 ký tự")
    private String metaKeywords;

    private Set<Integer> collectionIds;
    private Set<Integer> topicIds;

    private List<ProductVariantRequest> variants;
}
