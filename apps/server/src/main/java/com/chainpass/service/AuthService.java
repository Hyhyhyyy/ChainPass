package com.chainpass.service;

import com.chainpass.dto.LoginRequest;
import com.chainpass.entity.LoginUser;
import com.chainpass.entity.User;
import com.chainpass.mapper.UserMapper;
import com.chainpass.util.JwtService;
import com.chainpass.util.RedisCache;
import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务
 *
 * 包含登录速率限制、Token管理等安全功能
 */

@Service
@RequiredArgsConstructor
public class AuthService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RedisCache redisCache;
    private final UserMapper userMapper;

    // 登录速率限制配置
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final long LOGIN_LOCK_TIME = 15 * 60 * 1000; // 15分钟
    private static final long LOGIN_ATTEMPT_WINDOW = 60 * 1000; // 1分钟窗口

    /**
     * 用户登录
     */
    public com.chainpass.vo.LoginResponse login(String username, String password) {
        // 检查登录速率限制
        checkLoginRateLimit(username);

        try {
            // 认证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            LoginUser loginUser = (LoginUser) authentication.getPrincipal();
            User user = loginUser.getUser();

            // 清除失败计数
            clearLoginAttempts(username);

            // 生成 Token
            String accessToken = jwtService.generateAccessToken(user.getId(), user.getUsername());
            String refreshToken = jwtService.generateRefreshToken(user.getId());

            // 存储到 Redis
            String accessKey = "login:" + accessToken;
            String refreshKey = "refresh:" + refreshToken;

            redisCache.setCacheObject(accessKey, loginUser,
                    jwtService.getAccessTokenTtl(), TimeUnit.MILLISECONDS);
            redisCache.setCacheObject(refreshKey, user.getId(),
                    jwtService.getRefreshTokenTtl(), TimeUnit.MILLISECONDS);

            log.info("User logged in successfully: {}", username);

            return com.chainpass.vo.LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .userId(user.getId())
                    .username(user.getUsername())
                    .nickname(user.getNickname())
                    .avatar(user.getAvatar())
                    .giteeId(user.getGiteeId())
                    .build();

        } catch (Exception e) {
            // 记录失败尝试
            recordLoginFailure(username);
            throw e;
        }
    }

    /**
     * 用户登出
     */
    public void logout(String token) {
        if (token != null) {
            // 将Token加入黑名单
            String blacklistKey = "token:blacklist:" + token;
            redisCache.setCacheObject(blacklistKey, "logout",
                    jwtService.getAccessTokenTtl(), TimeUnit.MILLISECONDS);

            // 删除登录状态
            redisCache.deleteObject("login:" + token);
            log.info("User logged out and token blacklisted");
        }
    }

    /**
     * 刷新 Token
     */
    public com.chainpass.vo.LoginResponse refreshToken(String refreshToken) {
        // 验证刷新令牌
        if (!jwtService.validateToken(refreshToken) || !jwtService.isRefreshToken(refreshToken)) {
            throw new RuntimeException("无效的刷新令牌");
        }

        // 检查 Redis 中是否存在
        String refreshKey = "refresh:" + refreshToken;
        Long userId = redisCache.getCacheObject(refreshKey);

        if (userId == null) {
            throw new RuntimeException("刷新令牌已过期");
        }

        // 获取用户信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 获取旧的AccessToken并加入黑名单
        // 注：前端需要传递旧token，这里简化处理

        // 生成新的 Token
        String newAccessToken = jwtService.generateAccessToken(user.getId(), user.getUsername());
        String newRefreshToken = jwtService.generateRefreshToken(user.getId());

        // 删除旧的刷新令牌
        redisCache.deleteObject(refreshKey);

        // 存储新的 Token
        List<String> permissions = List.of("system:test:list"); // TODO: 从数据库获取权限
        LoginUser loginUser = new LoginUser(user, permissions);

        String accessKey = "login:" + newAccessToken;
        String newRefreshKey = "refresh:" + newRefreshToken;

        redisCache.setCacheObject(accessKey, loginUser,
                jwtService.getAccessTokenTtl(), TimeUnit.MILLISECONDS);
        redisCache.setCacheObject(newRefreshKey, user.getId(),
                jwtService.getRefreshTokenTtl(), TimeUnit.MILLISECONDS);

        log.info("Token refreshed for user: {}", user.getUsername());

        return com.chainpass.vo.LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .build();
    }

    /**
     * 检查登录速率限制
     */
    private void checkLoginRateLimit(String username) {
        String key = "login:attempts:" + username;
        Integer attempts = redisCache.getCacheObject(key);

        if (attempts != null && attempts >= MAX_LOGIN_ATTEMPTS) {
            Long ttl = redisCache.getExpire(key);
            log.warn("Login rate limit exceeded for user: {}, locked for {} seconds", username, ttl);
            throw new RuntimeException("登录尝试次数过多，请" + (ttl != null ? ttl / 60 : 15) + "分钟后再试");
        }
    }

    /**
     * 记录登录失败
     */
    private void recordLoginFailure(String username) {
        String key = "login:attempts:" + username;
        Integer attempts = redisCache.getCacheObject(key);

        if (attempts == null) {
            redisCache.setCacheObject(key, 1, LOGIN_LOCK_TIME, TimeUnit.MILLISECONDS);
        } else {
            redisCache.increment(key);
        }

        log.warn("Login attempt recorded for user: {}, total attempts: {}", username, attempts == null ? 1 : attempts + 1);
    }

    /**
     * 清除登录失败计数
     */
    private void clearLoginAttempts(String username) {
        String key = "login:attempts:" + username;
        redisCache.deleteObject(key);
    }

    /**
     * 检查Token是否在黑名单中
     */
    public boolean isTokenBlacklisted(String token) {
        String blacklistKey = "token:blacklist:" + token;
        return redisCache.hasKey(blacklistKey);
    }

    /**
     * 根据用户ID生成登录响应
     * 用于二维码扫码登录等场景
     */
    public com.chainpass.vo.LoginResponse generateLoginResponse(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 生成 Token
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = jwtService.generateRefreshToken(user.getId());

        // 存储到 Redis
        List<String> permissions = List.of("system:test:list"); // TODO: 从数据库获取权限
        LoginUser loginUser = new LoginUser(user, permissions);

        String accessKey = "login:" + accessToken;
        String refreshKey = "refresh:" + refreshToken;

        redisCache.setCacheObject(accessKey, loginUser,
                jwtService.getAccessTokenTtl(), TimeUnit.MILLISECONDS);
        redisCache.setCacheObject(refreshKey, user.getId(),
                jwtService.getRefreshTokenTtl(), TimeUnit.MILLISECONDS);

        log.info("Login response generated for user: {}", user.getUsername());

        return com.chainpass.vo.LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .giteeId(user.getGiteeId())
                .build();
    }
}