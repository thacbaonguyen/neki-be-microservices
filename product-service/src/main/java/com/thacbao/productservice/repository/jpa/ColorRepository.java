package com.thacbao.productservice.repository.jpa;

import com.thacbao.productservice.model.Color;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ColorRepository extends JpaRepository<Color, Integer> {
    Optional<Color> findByName(String name);
    boolean existsByName(String name);
    boolean existsByHexCode(String hexCode);
    List<Color> findAllByOrderByName();
}
