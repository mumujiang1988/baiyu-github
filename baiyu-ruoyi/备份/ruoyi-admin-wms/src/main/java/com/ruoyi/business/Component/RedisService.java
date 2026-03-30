package com.ruoyi.business.Component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.data.redis.core.StringRedisTemplate;

@Component
public class RedisService {

    @Autowired
    private StringRedisTemplate redis;

    public void set(String key, String value) {
        redis.opsForValue().set(key, value);
    }

    public String get(String key) {
        return redis.opsForValue().get(key);
    }

    public void del(String key) {
        redis.delete(key);
    }
}
