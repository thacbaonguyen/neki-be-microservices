package com.thacbao.productservice.repository.jpa;

import com.thacbao.productservice.model.Category;
import com.thacbao.productservice.model.SubCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubCategoryRepository extends JpaRepository<SubCategory, Integer> {
    Optional<SubCategory> findBySlug(String slug);
    boolean existsBySlug(String slug);
    boolean existsByName(String name);
    List<SubCategory> findByCategoryAndIsActiveTrue(Category category);

    @Query("SELECT sc FROM SubCategory sc WHERE sc.category = :category AND sc.parent IS NULL AND sc.isActive = true ORDER BY sc.displayOrder")
    List<SubCategory> findRootSubCategoriesByCategory(@Param("category") Category category);

    @Query("SELECT sc FROM SubCategory sc WHERE sc.parent = :parent AND sc.isActive = true ORDER BY sc.displayOrder")
    List<SubCategory> findChildrenByParent(@Param("parent") SubCategory parent);

    @Query("SELECT sc FROM SubCategory sc WHERE sc.category = :category AND sc.isActive = true AND NOT EXISTS (SELECT 1 FROM SubCategory child WHERE child.parent = sc)")
    List<SubCategory> findLeafSubCategoriesByCategory(@Param("category") Category category);

    List<SubCategory> findByCategoryAndLevel(Category category, Integer level);
    List<SubCategory> findByLevelAndIsActiveTrue(Integer level);

    @Query("SELECT sc FROM SubCategory sc LEFT JOIN FETCH sc.children WHERE sc.category = :category AND sc.parent IS NULL AND sc.isActive = true ORDER BY sc.displayOrder")
    List<SubCategory> findHierarchyByCategory(@Param("category") Category category);
}
