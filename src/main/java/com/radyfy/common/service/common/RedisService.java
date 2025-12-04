package com.radyfy.common.service.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.radyfy.common.model.commons.RedisData;

import java.util.concurrent.TimeUnit;

@Component
public class RedisService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void saveData(String key, RedisData<?> data) {
        redisTemplate.opsForValue().set(key, data, 30, TimeUnit.MINUTES);
    }

    public void saveDataWithExpiry(String key, RedisData<?> data, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, data, timeout, unit);
    }

    public RedisData<?> getData(String key) {
        return (RedisData<?>) redisTemplate.opsForValue().get(key);
    }

    public void invalidateData(String key) {
        redisTemplate.delete(key);
    }
}
