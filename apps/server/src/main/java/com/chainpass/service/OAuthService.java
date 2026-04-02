package com.chainpass.service;

import com.chainpass.entity.LoginUser;
import com.chainpass.entity.User;
import com.chainpass.mapper.UserMapper;
import com.chainpass.util.JwtService;
import com.chainpass.util.RedisCache;
import com.chainpass.vo.LoginResponse;
import com.chainpass.vo.OAuthConfig;
import lombok.RequiredArgsConstructor;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;

/**
 * OAuth 服务
 */

@Service
@RequiredArgsConstructor
public class OAuthService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OAuthService.class);

    @Value("${chainpass.oauth2.gitee.client-id}")
    private String clientId;

    @Value("${chainpass.oauth2.gitee.client-secret}")
    private String clientSecret;

    @Value("${chainpass.oauth2.gitee.redirect-uri}")
    private String redirectUri;

    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final RedisCache redisCache;

    /**
     * 获取 Gitee OAuth 配置
     */
    public OAuthConfig getGiteeConfig(String giteeId) {
        String scope = "user_info";

        // 如果有 giteeId，检查用户是否存在
        if (giteeId != null && !giteeId.isEmpty()) {
            User user = selectUserByGiteeId(giteeId);
            if (user != null && user.getScope() != null) {
                scope = user.getScope();
            }
        }

        return OAuthConfig.builder()
                .clientId(clientId)
                .redirectUri(redirectUri)
                .responseType("code")
                .scope(scope)
                .build();
    }

    /**
     * 处理 Gitee 回调
     */
    public String handleGiteeCallback(String code) throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // 获取 Access Token
            HttpPost tokenPost = new HttpPost("https://gitee.com/oauth/token");
            tokenPost.addHeader("Content-Type", "application/x-www-form-urlencoded");
            tokenPost.addHeader("Accept", "application/json");

            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("grant_type", "authorization_code"));
            params.add(new BasicNameValuePair("client_id", clientId));
            params.add(new BasicNameValuePair("client_secret", clientSecret));
            params.add(new BasicNameValuePair("code", code));
            params.add(new BasicNameValuePair("redirect_uri", redirectUri));
            tokenPost.setEntity(new UrlEncodedFormEntity(params));

            String tokenResponse = httpClient.execute(tokenPost, response -> {
                int statusCode = response.getCode();
                if (statusCode == 200) {
                    return EntityUtils.toString(response.getEntity());
                }
                throw new RuntimeException("Failed to get token: " + statusCode);
            });

            JSONObject tokenJson = JSON.parseObject(tokenResponse);
            String accessToken = tokenJson.getString("access_token");
            String scope = tokenJson.getString("scope");

            // 获取用户信息
            HttpGet userGet = new HttpGet("https://gitee.com/api/v5/user?access_token=" + accessToken);

            String userResponse = httpClient.execute(userGet, response -> {
                int statusCode = response.getCode();
                if (statusCode == 200) {
                    return EntityUtils.toString(response.getEntity());
                }
                throw new RuntimeException("Failed to get user info: " + statusCode);
            });

            JSONObject userJson = JSON.parseObject(userResponse);
            String login = userJson.getString("login");
            String name = userJson.getString("name");
            String avatarUrl = userJson.getString("avatar_url");
            String email = userJson.getString("email");
            String id = userJson.getString("id");

            // 查找或创建用户
            User user = selectUserByGiteeId(id);
            boolean isNewUser = false;

            if (user == null) {
                user = new User();
                user.setUsername(login);
                user.setNickname(name != null ? name : login);
                user.setAvatar(avatarUrl);
                user.setEmail(email);
                user.setGiteeId(id);
                user.setScope(scope);
                user.setStatus(0);
                user.setDelFlag(0);
                userMapper.insert(user);
                isNewUser = true;
                log.info("New user registered via Gitee: {}", login);
            }

            // 生成 JWT
            String jwtAccessToken = jwtService.generateAccessToken(user.getId(), user.getUsername());
            String jwtRefreshToken = jwtService.generateRefreshToken(user.getId());

            // 存储到 Redis
            List<String> permissions = List.of("system:test:list");
            LoginUser loginUser = new LoginUser(user, permissions);
            redisCache.setCacheObject("login:" + jwtAccessToken, loginUser,
                    jwtService.getAccessTokenTtl(), TimeUnit.MILLISECONDS);
            redisCache.setCacheObject("refresh:" + jwtRefreshToken, user.getId(),
                    jwtService.getRefreshTokenTtl(), TimeUnit.MILLISECONDS);

            log.info("User logged in via Gitee: {}", user.getUsername());

            // 构建重定向 URL
            return String.format("http://localhost:5173/auth/oauth/callback?accessToken=%s&refreshToken=%s&giteeId=%s",
                    jwtAccessToken, jwtRefreshToken, id);
        }
    }

    private User selectUserByGiteeId(String giteeId) {
        return userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getGiteeId, giteeId)
        );
    }
}