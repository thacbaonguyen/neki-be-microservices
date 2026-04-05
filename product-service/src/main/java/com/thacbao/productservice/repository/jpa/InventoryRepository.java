package com.thacbao.productservice.repository.jpa;

import com.thacbao.productservice.model.Inventory;
import com.thacbao.productservice.model.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Integer> {
    Optional<Inventory> findByVariant(ProductVariant variant);

    @Query("SELECT i FROM Inventory i WHERE i.variant.product.id = :productId")
    List<Inventory> findByProductId(@Param("productId") Integer productId);

    @Query("SELECT i FROM Inventory i WHERE i.quantity <= :threshold")
    List<Inventory> findLowStockItems(@Param("threshold") Integer threshold);
}
