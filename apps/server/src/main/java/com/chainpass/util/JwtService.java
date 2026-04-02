package com.chainpass.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 服务
 */

@Component
public class JwtService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JwtService.class);

    // HS256算法要求密钥至少256位(32字节)，建议64字符以上
    private static final int MIN_SECRET_LENGTH = 64;

    @Value("${chainpass.jwt.secret}")
    private String secret;

    @Value("${chainpass.jwt.access-token-ttl}")
    private long accessTokenTtl;

    @Value("${chainpass.jwt.refresh-token-ttl}")
    private long refreshTokenTtl;

    /**
     * 启动时验证JWT密钥安全性
     */
    @PostConstruct
    public void validateSecretKey() {
        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalStateException("JWT密钥未配置，请设置chainpass.jwt.secret环境变量");
        }
        if (secret.length() < MIN_SECRET_LENGTH) {
            log.error("JWT密钥长度不足: 当前{}字符, 建议{}字符以上", secret.length(), MIN_SECRET_LENGTH);
            throw new IllegalStateException(
                "JWT密钥长度不足，当前" + secret.length() + "字符，至少需要" + MIN_SECRET_LENGTH + "字符。" +
                "请使用: openssl rand -base64 64 生成安全密钥"
            );
        }
        // 检查是否使用了默认密钥
        if (secret.contains("default") || secret.contains("please-change")) {
            log.error("检测到不安全的默认JWT密钥，请立即更换！");
            throw new IllegalStateException("禁止使用默认JWT密钥，生产环境必须配置安全密钥");
        }
        log.info("JWT密钥验证通过，长度: {}字符", secret.length());
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成访问令牌
     */
    public String generateAccessToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("type", "access");
        return createToken(claims, accessTokenTtl);
    }

    /**
     * 生成刷新令牌
     */
    public String generateRefreshToken(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("type", "refresh");
        return createToken(claims, refreshTokenTtl);
    }

    /**
     * 创建 Token
     */
    private String createToken(Map<String, Object> claims, long ttl) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + ttl);

        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 解析 Token
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("Failed to parse token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 验证 Token 是否有效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims != null && !isTokenExpired(claims);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断 Token 是否过期
     */
    private boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }

    /**
     * 从 Token 获取用户ID
     */
    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            return claims.get("userId", Long.class);
        }
        return null;
    }

    /**
     * 从 Token 获取用户名
     */
    public String getUsername(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            return claims.get("username", String.class);
        }
        return null;
    }

    /**
     * 判断是否为刷新令牌
     */
    public boolean isRefreshToken(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            return "refresh".equals(claims.get("type", String.class));
        }
        return false;
    }

    public long getAccessTokenTtl() {
        return accessTokenTtl;
    }

    public long getRefreshTokenTtl() {
        return refreshTokenTtl;
    }
}