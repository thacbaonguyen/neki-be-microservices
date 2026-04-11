package com.thacbao.productservice.service;

import java.util.concurrent.TimeUnit;

public interface RedisService {
    void set(String key, Object value, long timeout, TimeUnit unit);
    Object get(String key);
    void delete(String key);
    boolean hasKey(String key);
}
