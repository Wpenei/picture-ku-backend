package com.qingmeng.smartpictureku.utils;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * &#064;description: 多级缓存工具类
 *
 * @author Wang
 * &#064;date: 2025/3/12
 */
@Component
public class MultiLevelCacheUtils {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private final Cache<String, String> LOCAL_CACHE =
            Caffeine.newBuilder().initialCapacity(1024)
                    .maximumSize(10000L)
                    // 缓存 5 分钟移除
                    .expireAfterWrite(5L, TimeUnit.MINUTES)
                    .build();

    private MultiLevelCacheUtils() {
        // 工具类不需要实例化
    }

    /**
     * 从缓存中获取数据
     * @param cacheKey 缓存Key
     * @param clazz 返回值类型
     * @return 返回值
     */
    public <T> T getCache(String cacheKey, Class<T> clazz) {
        // 1. 从本地缓存中获取缓存数据
        String cacheValue = LOCAL_CACHE.getIfPresent(cacheKey);
        if (cacheValue != null) {
            // 3. 如果缓存命中, 返回缓存数据
            return JSONUtil.toBean(cacheValue, clazz);
        }
        // 2. 从 Redis 中获取缓存数据
        String cacheByRedisValue = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cacheByRedisValue != null) {
            // 添加到本地缓存中
            LOCAL_CACHE.put(cacheKey, cacheByRedisValue);
            // 3. 如果缓存命中, 返回缓存数据
            // 3. 如果缓存命中, 返回缓存数据
            return JSONUtil.toBean(cacheByRedisValue, clazz);
        }
        // 缓存中都没有，返回空
        return null;
    }

    /**
     * 将数据存入缓存
     * @param cacheKey 缓存Key
     * @param value 缓存值
     */
    public <T> void putCache(String cacheKey, T value) {
        // 1. 将数据存入本地缓存
        LOCAL_CACHE.put(cacheKey, JSONUtil.toJsonStr(value));
        // 5.将查询结果存入 redis
        // 过期时间 设置为 5 ~ 10 分钟, 随机数,防止缓存雪崩
        int cacheExpireTime = 300 + RandomUtil.randomInt(0, 300);
        // 2. 将数据存入 Redis 缓存
        stringRedisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(value), cacheExpireTime, TimeUnit.SECONDS);
    }


}
