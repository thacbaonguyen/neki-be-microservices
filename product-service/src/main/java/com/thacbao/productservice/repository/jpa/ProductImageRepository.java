package com.thacbao.productservice.repository.jpa;

import com.thacbao.productservice.model.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Integer> {
    List<ProductImage> findByProductIdOrderByDisplayOrder(Integer productId);
    List<ProductImage> findByProductIdAndIsPrimaryTrue(Integer productId);
}
