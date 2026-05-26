package com.thacbao.orderservice.repository;

import com.thacbao.orderservice.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {

    @Query("""
    SELECT
        COALESCE(AVG(r.rating), 0),
        COUNT(r)
    FROM Review r
    WHERE r.productId = :productId
""")
    Object[] getRatingStats(@Param("productId") Integer productId);

    Page<Review> findByProductId(Integer productId, Pageable pageable);

    Page<Review> findByUserIdAndRatingGreaterThanEqual(Integer userId, int minRating, Pageable pageable);

    boolean existsByUserIdAndProductId(Integer userId, Integer productId);
}
