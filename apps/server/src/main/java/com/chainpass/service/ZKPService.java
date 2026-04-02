package com.chainpass.service;

import com.chainpass.dto.ZKPVerifyRequest;
import com.chainpass.entity.LoginUser;
import com.chainpass.entity.User;
import com.chainpass.util.JwtService;
import com.chainpass.util.RedisCache;
import com.chainpass.vo.LoginResponse;
import com.chainpass.vo.ZKPChallenge;
import lombok.RequiredArgsConstructor;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.stereotype.Service;

import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * ZKP 零知识证明认证服务
 */

@Service
@RequiredArgsConstructor
public class ZKPService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ZKPService.class);

    private final RedisCache redisCache;
    private final JwtService jwtService;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    // Session 过期时间：5分钟
    private static final long SESSION_TIMEOUT = 5 * 60 * 1000;

    /**
     * 初始化认证，生成挑战
     */
    public ZKPChallenge initAuth() {
        String sessionId = UUID.randomUUID().toString().replace("-", "");
        String challenge = UUID.randomUUID().toString().replace("-", "");
        long expiresAt = System.currentTimeMillis() + SESSION_TIMEOUT;

        // 存储 challenge
        String key = "zkp:challenge:" + sessionId;
        redisCache.setCacheObject(key, challenge, SESSION_TIMEOUT, TimeUnit.MILLISECONDS);

        return ZKPChallenge.builder()
                .sessionId(sessionId)
                .challenge(challenge)
                .expiresAt(expiresAt)
                .build();
    }

    /**
     * 存储公钥
     */
    public void storePublicKey(String sessionId, String publicKeyStr, String jwt) {
        String key = "zkp:publickey:" + sessionId;
        String jwtKey = "zkp:jwt:" + sessionId;

        redisCache.setCacheObject(key, publicKeyStr, SESSION_TIMEOUT, TimeUnit.MILLISECONDS);
        if (jwt != null) {
            redisCache.setCacheObject(jwtKey, jwt, SESSION_TIMEOUT, TimeUnit.MILLISECONDS);
        }

        log.info("Public key stored for session: {}", sessionId);
    }

    /**
     * 验证认证
     */
    public LoginResponse verifyAuth(ZKPVerifyRequest request) {
        String sessionId = request.getSessionId();
        String signature = request.getSignature();
        String publicKeyStr = request.getPublicKey();

        // 获取存储的 challenge
        String challengeKey = "zkp:challenge:" + sessionId;
        String challenge = redisCache.getCacheObject(challengeKey);

        if (challenge == null) {
            throw new RuntimeException("会话已过期，请重新认证");
        }

        try {
            // 解析公钥
            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyStr);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("Ed25519", "BC");
            PublicKey publicKey = keyFactory.generatePublic(keySpec);

            // 验证签名
            byte[] signatureBytes = Base64.getDecoder().decode(signature);
            byte[] challengeBytes = challenge.getBytes("UTF-8");

            boolean verified = verifySignature(challengeBytes, signatureBytes, publicKey);

            if (!verified) {
                throw new RuntimeException("签名验证失败");
            }

            // 认证成功，创建登录凭证
            User user = new User();
            user.setId(-1L); // ZKP 用户使用特殊 ID
            user.setUsername("ZKP_USER_" + sessionId.substring(0, 8));
            user.setNickname("零知识证明用户");
            user.setStatus(0);

            // 生成 Token
            String accessToken = jwtService.generateAccessToken(user.getId(), user.getUsername());
            String refreshToken = jwtService.generateRefreshToken(user.getId());

            // 存储 LoginUser
            List<String> permissions = List.of("system:test:list");
            LoginUser loginUser = new LoginUser(user, permissions);

            String accessKey = "login:" + accessToken;
            redisCache.setCacheObject(accessKey, loginUser,
                    jwtService.getAccessTokenTtl(), TimeUnit.MILLISECONDS);

            // 清理临时数据
            redisCache.deleteObject(challengeKey);
            redisCache.deleteObject("zkp:publickey:" + sessionId);
            redisCache.deleteObject("zkp:jwt:" + sessionId);

            log.info("ZKP authentication successful for session: {}", sessionId);

            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .userId(user.getId())
                    .username(user.getUsername())
                    .nickname(user.getNickname())
                    .build();

        } catch (Exception e) {
            log.error("ZKP verification failed: {}", e.getMessage());
            throw new RuntimeException("认证失败: " + e.getMessage());
        }
    }

    /**
     * 检查会话状态
     */
    public boolean checkSession(String sessionId) {
        String key = "zkp:challenge:" + sessionId;
        return redisCache.hasKey(key);
    }

    /**
     * 验证 Ed25519 签名
     */
    private boolean verifySignature(byte[] message, byte[] signature, PublicKey publicKey) {
        try {
            Signature verifier = Signature.getInstance("Ed25519", "BC");
            verifier.initVerify(publicKey);
            verifier.update(message);
            return verifier.verify(signature);
        } catch (Exception e) {
            log.error("Signature verification error: {}", e.getMessage());
            return false;
        }
    }
}