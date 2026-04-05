package com.thacbao.productservice.repository.jpa;

import com.thacbao.productservice.dto.request.ProductFilterRequest;
import com.thacbao.productservice.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepositoryCustom {

    Page<Product> filterProducts(ProductFilterRequest filter, Pageable pageable);

    Page<Product> findByCollectionId(Integer collectionId, Pageable pageable);

    Page<Product> findByTopicId(Integer topicId, Pageable pageable);

    Page<Product> findByBrandId(Integer brandId, Pageable pageable);

    List<Product> findRelatedProducts(Integer productId, Integer subCategoryId, int limit);

    Page<Product> findProductsWithStock(Pageable pageable);

    BigDecimal[] getPriceRangeByCategory(Integer categoryId);

    List<String> getAvailableColors(ProductFilterRequest filter);

    List<String> getAvailableSizes(ProductFilterRequest filter);
}
