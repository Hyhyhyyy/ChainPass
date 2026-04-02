package com.chainpass.filter;

import com.chainpass.entity.LoginUser;
import com.chainpass.service.AuthService;
import com.chainpass.util.JwtService;
import com.chainpass.util.RedisCache;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * JWT 认证过滤器
 *
 * 优化点：
 * 1. Token黑名单检查
 * 2. TTL滑动刷新策略（剩余时间<1/3时才刷新）
 */

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final RedisCache redisCache;
    private final JwtService jwtService;
    private final AuthService authService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 从请求头获取 Token
        String token = resolveToken(request);

        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 检查Token是否在黑名单中
            if (authService.isTokenBlacklisted(token)) {
                log.debug("Token is blacklisted");
                filterChain.doFilter(request, response);
                return;
            }

            // 从 Redis 获取用户信息
            String redisKey = "login:" + token;
            LoginUser loginUser = redisCache.getCacheObject(redisKey);

            if (loginUser == null) {
                // 检查是否是 ZKP 公钥认证
                String publicKeyKey = "loginPublic:" + token;
                String publicKeyStr = redisCache.getCacheObject(publicKeyKey);

                if (publicKeyStr != null) {
                    // ZKP 认证成功，创建认证对象
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    publicKeyStr,
                                    null,
                                    List.of(() -> "ROLE_USER")
                            );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("ZKP authentication successful");
                } else {
                    log.debug("Token not found in Redis: {}", token);
                }

                filterChain.doFilter(request, response);
                return;
            }

            // 设置认证信息
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            loginUser,
                            null,
                            loginUser.getAuthorities()
                    );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 滑动刷新Token TTL（仅当剩余时间<1/3时刷新）
            Long ttl = redisCache.getExpire(redisKey, TimeUnit.MILLISECONDS);
            long accessTokenTtl = jwtService.getAccessTokenTtl();

            if (ttl != null && ttl > 0 && ttl < accessTokenTtl / 3) {
                // 剩余时间不足1/3，刷新TTL
                redisCache.expire(redisKey, accessTokenTtl);
                log.debug("Token TTL refreshed, was: {}ms", ttl);
            }

            log.debug("User authenticated: {}", loginUser.getUsername());

        } catch (Exception e) {
            log.error("JWT authentication failed: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        // 兼容旧版 header
        String legacyToken = request.getHeader("authentication");
        if (StringUtils.hasText(legacyToken)) {
            return legacyToken;
        }

        return null;
    }
}