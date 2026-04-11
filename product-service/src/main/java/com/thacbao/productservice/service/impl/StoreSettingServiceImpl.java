package com.thacbao.productservice.service.impl;

import com.thacbao.productservice.model.StoreSetting;
import com.thacbao.productservice.repository.jpa.StoreSettingRepository;
import com.thacbao.productservice.service.StoreSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StoreSettingServiceImpl implements StoreSettingService {

    private final StoreSettingRepository storeSettingRepository;

    @Override
    public Map<String, String> getAllSettings() {
        Map<String, String> settings = new HashMap<>();
        storeSettingRepository.findAll()
                .forEach(s -> settings.put(s.getSettingKey(), s.getSettingValue()));
        return settings;
    }

    @Override
    @Transactional
    public void updateSettings(Map<String, String> settings) {
        settings.forEach((key, value) -> {
            StoreSetting setting = storeSettingRepository.findBySettingKey(key)
                    .orElse(StoreSetting.builder().settingKey(key).build());
            setting.setSettingValue(value);
            storeSettingRepository.save(setting);
        });
    }
}
