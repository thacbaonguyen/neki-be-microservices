package com.thacbao.productservice.repository.jpa;

import com.thacbao.productservice.model.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Integer> {
    List<ProductVariant> findByProductId(Integer productId);
    List<ProductVariant> findByProductIdAndIsActiveTrue(Integer productId);
}
