package com.thacbao.orderservice.repository;

import com.thacbao.common.enums.OrderStatus;
import com.thacbao.orderservice.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer>, OrderRepositoryCustom {

    Optional<Order> findByOrderNumber(String orderNumber);

    Page<Order> findByUserId(Integer userId, Pageable pageable);

    Page<Order> findByUserIdAndStatus(Integer userId, OrderStatus status, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.orderNumber = :orderNumber AND o.userEmail = :email")
    Optional<Order> findByOrderNumberAndEmail(@Param("orderNumber") String orderNumber, @Param("email") String email);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    long countByStatus(@Param("status") OrderStatus status);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt BETWEEN :start AND :end")
    long countByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.userId = :userId")
    long countByUserId(@Param("userId") Integer userId);

    @Query("SELECT COALESCE(SUM(o.finalAmount), 0) FROM Order o WHERE o.status IN ('DELIVERED', 'CONFIRMED', 'SHIPPED')")
    BigDecimal getTotalRevenue();

    @Query("SELECT COALESCE(SUM(o.finalAmount), 0) FROM Order o WHERE o.createdAt BETWEEN :start AND :end AND o.status IN ('DELIVERED', 'CONFIRMED', 'SHIPPED')")
    BigDecimal getRevenueByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(AVG(o.finalAmount), 0) FROM Order o WHERE o.status IN ('DELIVERED', 'CONFIRMED', 'SHIPPED')")
    BigDecimal getAverageOrderValue();

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status NOT IN ('CANCELLED')")
    long countValidOrders();

    @Query("""
    SELECT MONTH(o.createdAt), COUNT(o), COALESCE(SUM(o.finalAmount), 0)
    FROM Order o
    WHERE YEAR(o.createdAt) = :year AND o.status NOT IN ('CANCELLED')
    GROUP BY MONTH(o.createdAt)
    ORDER BY MONTH(o.createdAt)
""")
    List<Object[]> getMonthlyStatistics(@Param("year") int year);

    @Query("""
    SELECT o FROM Order o
    LEFT JOIN FETCH o.orderItems oi
    LEFT JOIN FETCH o.reviews
    WHERE o.id = :orderId
""")
    Optional<Order> findByIdWithFullDetails(@Param("orderId") Integer orderId);

    @Query("""
    SELECT DISTINCT o
    FROM Order o
    JOIN o.orderItems oi
    WHERE o.userId = :userId
      AND o.status = com.thacbao.common.enums.OrderStatus.DELIVERED
      AND oi.productId = :productId
      AND NOT EXISTS (
          SELECT 1 FROM Review r
          WHERE r.productId = :productId
            AND r.userId = :userId
            AND r.order.id = o.id
      )
    ORDER BY o.createdAt ASC
""")
    Page<Order> findOrdersUserCanReview(
            @Param("userId") Integer userId,
            @Param("productId") Integer productId,
            Pageable pageable
    );
}
