package com.thacbao.productservice.repository.jpa;

import com.thacbao.productservice.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    Optional<Category> findBySlug(String slug);
    boolean existsBySlug(String slug);
    boolean existsByName(String name);
    List<Category> findByIsActiveTrue();

    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.subCategories WHERE c.isActive = true ORDER BY c.displayOrder")
    List<Category> findAllActiveWithSubCategories();

    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.subCategories ORDER BY c.displayOrder")
    List<Category> findAllOrderByDisplayOrder();
}
