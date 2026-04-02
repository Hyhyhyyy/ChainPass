package com.chainpass.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 缓存工具类
 */
@Component
public class RedisCache {

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    /**
     * 缓存对象
     */
    public <T> void setCacheObject(String key, T value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 缓存对象（带过期时间）
     */
    public <T> void setCacheObject(String key, T value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 缓存对象（带毫秒过期时间）
     */
    public <T> void setCacheObject(String key, T value, long millis) {
        redisTemplate.opsForValue().set(key, value, millis, TimeUnit.MILLISECONDS);
    }

    /**
     * 获取缓存对象
     */
    @SuppressWarnings("unchecked")
    public <T> T getCacheObject(String key) {
        return (T) redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除缓存对象
     */
    public boolean deleteObject(String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }

    /**
     * 删除多个缓存对象
     */
    public boolean deleteObject(Collection<String> keys) {
        Boolean result = redisTemplate.delete(keys);
        return Boolean.TRUE.equals(result);
    }

    /**
     * 设置过期时间
     */
    public boolean expire(String key, long timeout, TimeUnit unit) {
        return Boolean.TRUE.equals(redisTemplate.expire(key, timeout, unit));
    }

    /**
     * 设置过期时间（毫秒）
     */
    public boolean expire(String key, long millis) {
        return expire(key, millis, TimeUnit.MILLISECONDS);
    }

    /**
     * 判断 key 是否存在
     */
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 获取过期时间（秒）
     */
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 获取过期时间（指定单位）
     */
    public Long getExpire(String key, TimeUnit unit) {
        return redisTemplate.getExpire(key, unit);
    }

    /**
     * 递增操作
     */
    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    /**
     * 递增操作（指定步长）
     */
    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 递减操作
     */
    public Long decrement(String key) {
        return redisTemplate.opsForValue().decrement(key);
    }

    /**
     * 缓存 List
     */
    public <T> long setCacheList(String key, List<T> dataList) {
        Long count = redisTemplate.opsForList().rightPushAll(key, dataList.toArray());
        return count != null ? count : 0;
    }

    /**
     * 获取缓存 List
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getCacheList(String key) {
        return (List<T>) redisTemplate.opsForList().range(key, 0, -1);
    }

    /**
     * 缓存 Set
     */
    public <T> void setCacheSet(String key, Set<T> dataSet) {
        redisTemplate.opsForSet().add(key, dataSet.toArray());
    }

    /**
     * 获取缓存 Set
     */
    @SuppressWarnings("unchecked")
    public <T> Set<T> getCacheSet(String key) {
        return (Set<T>) redisTemplate.opsForSet().members(key);
    }

    /**
     * 缓存 Map
     */
    public <T> void setCacheMap(String key, Map<String, T> dataMap) {
        if (dataMap != null) {
            redisTemplate.opsForHash().putAll(key, dataMap);
        }
    }

    /**
     * 获取缓存 Map
     */
    @SuppressWarnings("unchecked")
    public <T> Map<String, T> getCacheMap(String key) {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        Map<String, T> result = new java.util.HashMap<>();
        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            result.put(String.valueOf(entry.getKey()), (T) entry.getValue());
        }
        return result;
    }

    /**
     * 获取 keys
     */
    public Collection<Object> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }
}