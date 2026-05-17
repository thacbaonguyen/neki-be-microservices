package com.thacbao.orderservice.repository;

import com.thacbao.orderservice.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    Optional<CartItem> findByIdAndCart_UserId(Integer cartItemId, Integer userId);

    Optional<CartItem> findByCartIdAndVariantId(Integer cartId, Integer variantId);
}
