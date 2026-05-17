package com.thacbao.orderservice.repository;

import com.thacbao.common.enums.DiscountType;
import com.thacbao.orderservice.model.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, Integer> {
    List<Discount> findByDiscountType(DiscountType discountType);

    Optional<Discount> findByName(String name);

    Optional<Discount> findByCode(String code);
}
