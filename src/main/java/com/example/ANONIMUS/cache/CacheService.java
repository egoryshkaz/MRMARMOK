package com.example.ANONIMUS.cache;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CacheService {
    private final Map<String, Object> cache = new ConcurrentHashMap<>();

    public void put(String key, Object value) {
        cache.put(key, value);
    }

    public <T> T get(String key, Class<T> type) {
        return type.cast(cache.get(key));
    }

    public void evict(String key) {
        cache.remove(key);
    }

    public boolean containsKey(String key) {
        return cache.containsKey(key);
    }
    @SuppressWarnings("unchecked")
    public <T> List<T> getList(String key, Class<T> type) {
        return (List<T>) cache.get(key);
    }
}