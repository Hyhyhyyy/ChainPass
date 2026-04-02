package com.chainpass.did.service;

import com.alibaba.fastjson2.JSON;
import com.chainpass.did.dto.*;
import com.chainpass.did.entity.*;
import com.chainpass.did.mapper.DIDMapper;
import com.chainpass.exception.BusinessException;
import com.chainpass.util.RedisCache;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * DID服务 - 去中心化身份核心服务
 *
 * 实现W3C DID标准的身份创建、验证、管理功能
 * 支持私钥加密存储
 * 支持DID文档缓存
 */
@Service
@RequiredArgsConstructor
public class DIDService {

    private static final Logger log = LoggerFactory.getLogger(DIDService.class);

    private final DIDMapper didMapper;
    private final RedisCache redisCache;

    @Value("${chainpass.did.key-secret:chainpass-did-key-secret-change-me}")
    private String keySecret;

    // DID缓存过期时间：5分钟
    private static final long DID_CACHE_TTL = 5 * 60 * 1000;
    private static final String DID_CACHE_PREFIX = "did:cache:";

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * 为用户创建DID
     *
     * @param userId 用户ID
     * @return DID文档
     */
    @Transactional
    public DIDDocument createDID(Long userId) {
        log.info("Creating DID for user: {}", userId);

        // 检查用户是否已有DID
        DIDRecord existingDID = didMapper.findByUserId(userId);
        if (existingDID != null) {
            throw new BusinessException("用户已拥有DID: " + existingDID.getDid());
        }

        try {
            // 1. 生成Ed25519密钥对
            KeyPair keyPair = generateEd25519KeyPair();
            String publicKeyBase64 = Base64.getEncoder().encodeToString(
                keyPair.getPublic().getEncoded());

            // 加密私钥存储
            String privateKeyEncrypted = encryptPrivateKey(keyPair.getPrivate().getEncoded());

            // 2. 生成DID标识符
            String didIdentifier = generateDIDIdentifier(keyPair.getPublic());
            String did = "did:chainpass:" + didIdentifier;

            // 3. 构建DID文档
            DIDDocument document = DIDDocument.builder()
                .id(did)
                .verificationMethod(List.of(
                    DIDDocument.VerificationMethod.builder()
                        .id(did + "#key-1")
                        .type("Ed25519VerificationKey2020")
                        .controller(did)
                        .publicKeyBase64(publicKeyBase64)
                        .build()
                ))
                .authentication(List.of(did + "#key-1"))
                .assertionMethod(List.of(did + "#key-1"))
                .service(List.of(
                    DIDDocument.Service.builder()
                        .id(did + "#vc-service")
                        .type("VerifiableCredentialService")
                        .serviceEndpoint("https://chainpass.io/vc/")
                        .build()
                ))
                .created(Instant.now().toString())
                .build();

            // 4. 存储到数据库（包含加密的私钥）
            DIDRecord record = DIDRecord.builder()
                .did(did)
                .userId(userId)
                .publicKey(publicKeyBase64)
                .privateKeyEncrypted(privateKeyEncrypted)
                .didDocument(JSON.toJSONString(document))
                .status(0) // 激活状态
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .expiresAt(Instant.now().plus(5 * 365, ChronoUnit.DAYS)) // 5年有效期
                .build();

            didMapper.insert(record);

            log.info("DID created successfully with encrypted private key: {}", did);
            return document;

        } catch (Exception e) {
            log.error("Failed to create DID for user: {}", userId, e);
            throw new BusinessException("创建DID失败: " + e.getMessage());
        }
    }

    /**
     * 导出用户私钥（用于用户自行安全保存）
     *
     * @param userId 用户ID
     * @return Base64编码的私钥
     */
    public String exportPrivateKey(Long userId) {
        DIDRecord record = didMapper.findByUserId(userId);
        if (record == null) {
            throw new BusinessException("用户DID不存在");
        }

        if (record.getPrivateKeyEncrypted() == null) {
            throw new BusinessException("私钥未存储，无法导出");
        }

        try {
            byte[] privateKeyBytes = decryptPrivateKey(record.getPrivateKeyEncrypted());
            return Base64.getEncoder().encodeToString(privateKeyBytes);
        } catch (Exception e) {
            log.error("Failed to export private key for user: {}", userId, e);
            throw new BusinessException("私钥导出失败: " + e.getMessage());
        }
    }

    /**
     * 根据用户ID获取DID（带缓存）
     */
    public DIDDocument getDIDByUserId(Long userId) {
        // 尝试从缓存获取
        String cacheKey = DID_CACHE_PREFIX + "user:" + userId;
        DIDDocument cached = redisCache.getCacheObject(cacheKey);
        if (cached != null) {
            return cached;
        }

        DIDRecord record = didMapper.findByUserId(userId);
        if (record == null) {
            return null;
        }

        DIDDocument document = JSON.parseObject(record.getDidDocument(), DIDDocument.class);

        // 写入缓存
        redisCache.setCacheObject(cacheKey, document, DID_CACHE_TTL, TimeUnit.MILLISECONDS);

        return document;
    }

    /**
     * 根据DID标识符获取DID文档（带缓存）
     */
    public DIDDocument getDIDByDid(String did) {
        // 尝试从缓存获取
        String cacheKey = DID_CACHE_PREFIX + "did:" + did;
        DIDDocument cached = redisCache.getCacheObject(cacheKey);
        if (cached != null) {
            return cached;
        }

        DIDRecord record = didMapper.findByDid(did);
        if (record == null) {
            return null;
        }

        DIDDocument document = JSON.parseObject(record.getDidDocument(), DIDDocument.class);

        // 写入缓存
        redisCache.setCacheObject(cacheKey, document, DID_CACHE_TTL, TimeUnit.MILLISECONDS);

        return document;
    }

    /**
     * 验证DID签名
     *
     * @param request 验证请求
     * @return 验证结果
     */
    public boolean verifySignature(VerifyDIDRequest request) {
        log.info("Verifying DID signature: {}", request.getDid());

        // 1. 获取DID记录
        DIDRecord record = didMapper.findByDid(request.getDid());
        if (record == null) {
            log.warn("DID not found: {}", request.getDid());
            return false;
        }

        // 2. 检查状态
        if (record.getStatus() != 0) {
            log.warn("DID is not active: {}, status: {}", request.getDid(), record.getStatus());
            return false;
        }

        // 3. 检查过期时间
        if (record.getExpiresAt().isBefore(Instant.now())) {
            log.warn("DID is expired: {}", request.getDid());
            return false;
        }

        try {
            // 4. 解析公钥
            byte[] publicKeyBytes = Base64.getDecoder().decode(record.getPublicKey());
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("Ed25519", "BC");
            PublicKey publicKey = keyFactory.generatePublic(keySpec);

            // 5. 验证签名
            byte[] signatureBytes = Base64.getDecoder().decode(request.getSignature());
            byte[] challengeBytes = request.getChallenge().getBytes(StandardCharsets.UTF_8);

            Signature verifier = Signature.getInstance("Ed25519", "BC");
            verifier.initVerify(publicKey);
            verifier.update(challengeBytes);

            boolean valid = verifier.verify(signatureBytes);
            log.info("DID signature verification result: {}", valid);
            return valid;

        } catch (Exception e) {
            log.error("Failed to verify DID signature: {}", request.getDid(), e);
            return false;
        }
    }

    /**
     * 吊销DID
     */
    @Transactional
    public void revokeDID(String did, String reason) {
        log.info("Revoking DID: {}, reason: {}", did, reason);

        int updated = didMapper.revokeDID(did, reason);
        if (updated == 0) {
            throw new BusinessException("DID不存在或已被吊销");
        }

        // 清除缓存
        String cacheKey = DID_CACHE_PREFIX + "did:" + did;
        redisCache.deleteObject(cacheKey);
    }

    /**
     * 检查DID是否有效
     */
    public boolean isValidDID(String did) {
        return didMapper.countValidDID(did) > 0;
    }

    /**
     * 获取用户DID响应
     */
    public DIDResponse getDIDResponse(Long userId) {
        DIDRecord record = didMapper.findByUserId(userId);
        if (record == null) {
            return null;
        }

        DIDDocument document = JSON.parseObject(record.getDidDocument(), DIDDocument.class);

        return new DIDResponse() {{
            setDid(record.getDid());
            setDocument(document);
            setPublicKey(record.getPublicKey());
            setStatus(getStatusText(record.getStatus()));
            setCreatedAt(record.getCreatedAt().toString());
            setExpiresAt(record.getExpiresAt().toString());
        }};
    }

    /**
     * 生成Ed25519密钥对
     */
    private KeyPair generateEd25519KeyPair() throws NoSuchAlgorithmException, NoSuchProviderException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("Ed25519", "BC");
        return keyGen.generateKeyPair();
    }

    /**
     * 根据公钥生成DID标识符
     * 使用公钥的SHA-256哈希的前32字节作为标识符
     */
    private String generateDIDIdentifier(PublicKey publicKey) throws NoSuchAlgorithmException {
        byte[] publicKeyBytes = publicKey.getEncoded();
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(publicKeyBytes);

        // 取前16字节，转换为十六进制字符串
        byte[] identifierBytes = new byte[16];
        System.arraycopy(hash, 0, identifierBytes, 0, 16);

        return bytesToHex(identifierBytes);
    }

    /**
     * 字节数组转十六进制字符串
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * 状态文本转换
     */
    private String getStatusText(Integer status) {
        return switch (status) {
            case 0 -> "ACTIVE";
            case 1 -> "SUSPENDED";
            case 2 -> "REVOKED";
            default -> "UNKNOWN";
        };
    }

    /**
     * 加密私钥
     * 使用AES-256-GCM加密
     */
    private String encryptPrivateKey(byte[] privateKeyBytes) {
        try {
            javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES/GCM/NoPadding");

            // 从配置密钥派生AES密钥
            byte[] keyBytes = MessageDigest.getInstance("SHA-256")
                .digest(keySecret.getBytes(StandardCharsets.UTF_8));
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
            throw new RuntimeException("私钥加密失败", e);
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

            byte[] keyBytes = MessageDigest.getInstance("SHA-256")
                .digest(keySecret.getBytes(StandardCharsets.UTF_8));
            javax.crypto.spec.SecretKeySpec secretKey = new javax.crypto.spec.SecretKeySpec(keyBytes, "AES");
            javax.crypto.spec.GCMParameterSpec gcmSpec = new javax.crypto.spec.GCMParameterSpec(128, iv);

            cipher.init(javax.crypto.Cipher.DECRYPT_MODE, secretKey, gcmSpec);
            return cipher.doFinal(encrypted);

        } catch (Exception e) {
            log.error("Failed to decrypt private key", e);
            throw new RuntimeException("私钥解密失败", e);
        }
    }
}