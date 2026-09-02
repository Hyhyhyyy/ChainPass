package com.chainpass.util;

import com.alibaba.fastjson2.JSONObject;
import com.chainpass.entity.LoginUser;
import com.chainpass.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RedisCacheTest {

    private RedisCache redisCache;
    private ValueOperations<Object, Object> valueOperations;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() throws Exception {
        RedisTemplate<Object, Object> redisTemplate = mock(RedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        redisCache = new RedisCache();
        var field = RedisCache.class.getDeclaredField("redisTemplate");
        field.setAccessible(true);
        field.set(redisCache, redisTemplate);
    }

    @Test
    void convertsJsonObjectBackToRequestedDomainType() {
        User user = new User();
        user.setId(3L);
        user.setUsername("demo");
        user.setPassword("hashed");
        user.setStatus(0);
        LoginUser original = new LoginUser(user, List.of("identity:read"));
        when(valueOperations.get("login:token")).thenReturn(JSONObject.from(original));

        LoginUser restored = redisCache.getCacheObject("login:token", LoginUser.class);

        assertEquals(3L, restored.getUserId());
        assertEquals("demo", restored.getUsername());
        assertEquals(List.of("identity:read"), restored.getPermissions());
    }

    @Test
    void convertsJsonIntegerToLong() {
        when(valueOperations.get("refresh:token")).thenReturn(3);

        assertEquals(3L, redisCache.getCacheObject("refresh:token", Long.class));
    }

    @Test
    void keepsNullCacheMiss() {
        when(valueOperations.get("missing")).thenReturn(null);

        assertNull(redisCache.getCacheObject("missing", LoginUser.class));
    }
}
