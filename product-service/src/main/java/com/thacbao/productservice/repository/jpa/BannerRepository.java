package com.thacbao.productservice.repository.jpa;

import com.thacbao.productservice.model.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BannerRepository extends JpaRepository<Banner, Integer> {
    Optional<Banner> findFirstByIsPinTrueOrderByCreatedAtDesc();
}
