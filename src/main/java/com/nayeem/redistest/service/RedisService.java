package com.nayeem.redistest.service;

import com.nayeem.redistest.model.TestData;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, TestData> redisTemplate;
    private static final String KEY_PREFIX = "testdata:";

    public void saveTestData(TestData testData) {
        String key = KEY_PREFIX + testData.getId();
        testData.setCreatedAt(LocalDateTime.now());
        redisTemplate.opsForValue().set(key, testData, 1, TimeUnit.HOURS); // Expire after 1 hour
    }

    public TestData getTestData(String id) {
        String key = KEY_PREFIX + id;
        Object data = redisTemplate.opsForValue().get(key);
        if (data instanceof TestData) {
            return (TestData) data;
        }
        return null;
    }

    public boolean deleteTestData(String id) {
        String key = KEY_PREFIX + id;
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }

    public boolean existsTestData(String id) {
        String key = KEY_PREFIX + id;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public java.util.List<TestData> getAllTestData() {
        java.util.Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        
        java.util.List<TestData> allData = new java.util.ArrayList<>();
        for (String key : keys) {
            TestData data = redisTemplate.opsForValue().get(key);
            if (data != null) {
                allData.add(data);
            }
        }
        return allData;
    }
}
