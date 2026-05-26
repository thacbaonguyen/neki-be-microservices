package com.thacbao.productservice.repository.jpa;

import com.thacbao.productservice.model.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, Integer> {
    Optional<Collection> findBySlug(String slug);
    boolean existsBySlug(String slug);
    boolean existsByName(String name);
    List<Collection> findByIsActiveTrue();

    @Query("SELECT c FROM Collection c LEFT JOIN FETCH c.subCategories WHERE c.id = :id")
    Optional<Collection> findByIdWithSubCategories(@Param("id") Integer id);
}
