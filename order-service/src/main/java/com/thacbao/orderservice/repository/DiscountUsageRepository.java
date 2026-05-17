package com.thacbao.orderservice.repository;

import com.thacbao.orderservice.model.Discount;
import com.thacbao.orderservice.model.DiscountUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscountUsageRepository extends JpaRepository<DiscountUsage, Integer> {
    long countByUserIdAndDiscount(Integer userId, Discount discount);
}
