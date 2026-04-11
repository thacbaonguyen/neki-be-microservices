package com.thacbao.productservice.service;

import java.util.Map;

public interface StoreSettingService {
    Map<String, String> getAllSettings();
    void updateSettings(Map<String, String> settings);
}
