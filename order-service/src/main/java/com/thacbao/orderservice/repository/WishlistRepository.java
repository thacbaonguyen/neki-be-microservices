package com.thacbao.orderservice.repository;

import com.thacbao.orderservice.model.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Integer> {

    Optional<Wishlist> findByUserId(Integer userId);

    boolean existsByUserIdAndProductIdsContaining(Integer userId, Integer productId);
}
