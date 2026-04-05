package com.thacbao.productservice.repository.jpa;

import com.thacbao.productservice.model.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SizeRepository extends JpaRepository<Size, Integer> {
    Optional<Size> findByNameAndCategoryType(String name, String categoryType);
    boolean existsByNameAndCategoryType(String name, String categoryType);
    List<Size> findByCategoryTypeOrderByDisplayOrder(String categoryType);
    List<Size> findAllByOrderByCategoryTypeAscDisplayOrderAsc();
}
