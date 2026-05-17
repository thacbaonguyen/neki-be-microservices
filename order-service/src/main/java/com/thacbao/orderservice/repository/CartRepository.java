package com.thacbao.orderservice.repository;

import com.thacbao.orderservice.model.Cart;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Integer> {

    @EntityGraph(attributePaths = {"cartItems"})
    Optional<Cart> findByUserId(Integer userId);
}
