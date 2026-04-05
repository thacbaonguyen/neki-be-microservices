package com.thacbao.productservice.repository.jpa;

import com.thacbao.productservice.model.StoreSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoreSettingRepository extends JpaRepository<StoreSetting, Integer> {
    Optional<StoreSetting> findBySettingKey(String settingKey);
}
