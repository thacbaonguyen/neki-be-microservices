package com.thacbao.recommendationservice.repository;

import com.thacbao.recommendationservice.model.ProductSimilarity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductSimilarityRepository extends JpaRepository<ProductSimilarity, Integer> {

    @Query("SELECT ps FROM ProductSimilarity ps WHERE ps.productId1 = :productId ORDER BY ps.score DESC")
    List<ProductSimilarity> findSimilarProducts(Integer productId, Pageable pageable);

    @Modifying
    @Query("DELETE FROM ProductSimilarity")
    void deleteAllInBatchCustom();
}
