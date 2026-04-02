package com.chainpass.vc.service;

import com.chainpass.exception.BusinessException;
import com.chainpass.util.RedisCache;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.security.*;
import java.security.spec.*;
import java.time.Instant;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/**
 * VC签发者密钥服务
 *
 * 管理系统Ed25519密钥对，用于签发可验证凭证
 * 密钥安全存储：私钥加密后存Redis，公钥公开
 */
@Service
@RequiredArgsConstructor
public class IssuerKeyService {

    private static final Logger log = LoggerFactory.getLogger(IssuerKeyService.class);

    private final RedisCache redisCache;

    @Value("${chainpass.issuer.key-secret:chainpass-issuer-key-secret-change-me}")
    private String keySecret;

    // 密钥在Redis中的存储key
    private static final String ISSUER_PUBLIC_KEY = "issuer:publicKey";
    private static final String ISSUER_PRIVATE_KEY = "issuer:privateKey";
    private static final String ISSUER_KEY_CREATED = "issuer:keyCreated";

    // 系统DID标识
    public static final String ISSUER_DID = "did:chainpass:issuer";

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private KeyPair issuerKeyPair;

    /**
     * 启动时初始化或加载签发者密钥
     */
    @PostConstruct
    public void initIssuerKeys() {
        log.info("Initializing issuer keys...");

        // 尝试从Redis加载已有密钥
        String publicKeyStr = redisCache.getCacheObject(ISSUER_PUBLIC_KEY);
        String privateKeyStr = redisCache.getCacheObject(ISSUER_PRIVATE_KEY);

        if (publicKeyStr != null && privateKeyStr != null) {
            try {
                // 解密私钥并加载
                byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyStr);
                byte[] privateKeyBytes = decryptPrivateKey(privateKeyStr);

                KeyFactory keyFactory = KeyFactory.getInstance("Ed25519", "BC");
                PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
                PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));

                issuerKeyPair = new KeyPair(publicKey, privateKey);
                log.info("Issuer keys loaded from cache");
                return;
            } catch (Exception e) {
                log.warn("Failed to load issuer keys from cache, will generate new ones: {}", e.getMessage());
            }
        }

        // 生成新密钥对
        generateAndStoreKeys();
    }

    /**
     * 生成新的Ed25519密钥对并安全存储
     */
    private void generateAndStoreKeys() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("Ed25519", "BC");
            issuerKeyPair = keyGen.generateKeyPair();

            // 存储公钥（公开）
            String publicKeyBase64 = Base64.getEncoder().encodeToString(
                issuerKeyPair.getPublic().getEncoded());
            redisCache.setCacheObject(ISSUER_PUBLIC_KEY, publicKeyBase64);

            // 加密存储私钥
            String encryptedPrivateKey = encryptPrivateKey(
                issuerKeyPair.getPrivate().getEncoded());
            redisCache.setCacheObject(ISSUER_PRIVATE_KEY, encryptedPrivateKey);

            // 记录创建时间（密钥有效期）
            redisCache.setCacheObject(ISSUER_KEY_CREATED, Instant.now().toString());

            log.info("New issuer keys generated and stored securely");
            log.info("Issuer public key: {}", publicKeyBase64.substring(0, 20) + "...");

        } catch (Exception e) {
            log.error("Failed to generate issuer keys", e);
            throw new BusinessException("初始化签发者密钥失败: " + e.getMessage());
        }
    }

    /**
     * 使用Ed25519私钥签名数据
     *
     * @param data 待签名的数据（通常是凭证哈希）
     * @return Base64编码的签名
     */
    public String sign(String data) {
        if (issuerKeyPair == null || issuerKeyPair.getPrivate() == null) {
            throw new BusinessException("签发者密钥未初始化");
        }

        try {
            Signature signer = Signature.getInstance("Ed25519", "BC");
            signer.initSign(issuerKeyPair.getPrivate());
            signer.update(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            byte[] signature = signer.sign();
            return Base64.getEncoder().encodeToString(signature);

        } catch (Exception e) {
            log.error("Failed to sign data", e);
            throw new BusinessException("签名失败: " + e.getMessage());
        }
    }

    /**
     * 使用Ed25519公钥验证签名
     *
     * @param data 原始数据
     * @param signatureBase64 Base64编码的签名
     * @return 验证结果
     */
    public boolean verify(String data, String signatureBase64) {
        if (issuerKeyPair == null || issuerKeyPair.getPublic() == null) {
            throw new BusinessException("签发者密钥未初始化");
        }

        try {
            byte[] signatureBytes = Base64.getDecoder().decode(signatureBase64);

            Signature verifier = Signature.getInstance("Ed25519", "BC");
            verifier.initVerify(issuerKeyPair.getPublic());
            verifier.update(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            return verifier.verify(signatureBytes);

        } catch (Exception e) {
            log.error("Failed to verify signature", e);
            return false;
        }
    }

    /**
     * 获取签发者公钥（Base64编码）
     */
    public String getPublicKeyBase64() {
        if (issuerKeyPair == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(issuerKeyPair.getPublic().getEncoded());
    }

    /**
     * 获取签发者DID文档中的验证方法ID
     */
    public String getVerificationMethodId() {
        return ISSUER_DID + "#key-1";
    }

    /**
     * 加密私钥存储
     * 使用AES-256-GCM加密（简化实现，生产环境建议使用专业HSM）
     */
    private String encryptPrivateKey(byte[] privateKeyBytes) {
        // 使用配置的密钥进行简单加密
        // 生产环境应使用专业的密钥管理服务
        try {
            javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding");

            // 从配置密钥派生AES密钥
            byte[] keyBytes = java.security.MessageDigest.getInstance("SHA-256")
                .digest(keySecret.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            javax.crypto.spec.SecretKeySpec secretKey = new javax.crypto.spec.SecretKeySpec(keyBytes, "AES");

            // 生成随机IV
            byte[] iv = new byte[12];
            java.security.SecureRandom random = new java.security.SecureRandom();
            random.nextBytes(iv);
            javax.crypto.spec.GCMParameterSpec gcmSpec = new javax.crypto.spec.GCMParameterSpec(128, iv);

            cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, secretKey, gcmSpec);
            byte[] encrypted = cipher.doFinal(privateKeyBytes);

            // 组合IV和加密数据
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

            return Base64.getEncoder().encodeToString(combined);

        } catch (Exception e) {
            log.error("Failed to encrypt private key", e);
            throw new BusinessException("私钥加密失败");
        }
    }

    /**
     * 解密私钥
     */
    private byte[] decryptPrivateKey(String encryptedBase64) {
        try {
            byte[] combined = Base64.getDecoder().decode(encryptedBase64);

            // 分离IV和加密数据
            byte[] iv = new byte[12];
            byte[] encrypted = new byte[combined.length - 12];
            System.arraycopy(combined, 0, iv, 0, 12);
            System.arraycopy(combined, 12, encrypted, 0, encrypted.length);

            javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding");

            byte[] keyBytes = java.security.MessageDigest.getInstance("SHA-256")
                .digest(keySecret.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            javax.crypto.spec.SecretKeySpec secretKey = new javax.crypto.spec.SecretKeySpec(keyBytes, "AES");
            javax.crypto.spec.GCMParameterSpec gcmSpec = new javax.crypto.spec.GCMParameterSpec(128, iv);

            cipher.init(javax.crypto.Cipher.DECRYPT_MODE, secretKey, gcmSpec);
            return cipher.doFinal(encrypted);

        } catch (Exception e) {
            log.error("Failed to decrypt private key", e);
            throw new BusinessException("私钥解密失败");
        }
    }

    /**
     * 检查密钥是否有效（未过期）
     * 密钥建议每1-2年轮换
     */
    public boolean isKeyValid() {
        String createdStr = redisCache.getCacheObject(ISSUER_KEY_CREATED);
        if (createdStr == null) {
            return false;
        }

        Instant created = Instant.parse(createdStr);
        // 密钥有效期2年
        Instant expiresAt = created.plus(730, java.time.temporal.ChronoUnit.DAYS);

        return Instant.now().isBefore(expiresAt);
    }

    /**
     * 强制重新生成密钥（密钥轮换）
     */
    public void rotateKeys() {
        log.warn("Rotating issuer keys...");
        redisCache.deleteObject(ISSUER_PUBLIC_KEY);
        redisCache.deleteObject(ISSUER_PRIVATE_KEY);
        redisCache.deleteObject(ISSUER_KEY_CREATED);
        generateAndStoreKeys();
        log.info("Issuer keys rotated successfully");
    }
}