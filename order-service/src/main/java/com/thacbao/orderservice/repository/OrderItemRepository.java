package com.thacbao.orderservice.repository;

import com.thacbao.orderservice.model.OrderItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {

    List<OrderItem> findByOrderId(Integer orderId);

    List<OrderItem> findByVariantId(Integer variantId);

    @Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi WHERE oi.variantId = :variantId")
    Integer getTotalQuantitySoldByVariant(@Param("variantId") Integer variantId);

    @Query("SELECT oi.variantId, SUM(oi.quantity) as totalQty FROM OrderItem oi " +
            "GROUP BY oi.variantId ORDER BY totalQty DESC")
    List<Object[]> getTopSellingVariants();

    @Query("SELECT oi.productId, SUM(oi.quantity) as totalQty FROM OrderItem oi " +
            "GROUP BY oi.productId ORDER BY totalQty DESC")
    List<Object[]> getTopSellingProducts();

    void deleteByOrderId(Integer orderId);

    Page<OrderItem> findByOrderUserId(Integer userId, Pageable pageable);
}
