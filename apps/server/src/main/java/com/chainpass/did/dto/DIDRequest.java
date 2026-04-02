package com.chainpass.did.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 创建DID请求
 */
class CreateDIDRequest {
    // 用户ID从Token获取，不需要传入

    public CreateDIDRequest() {}
}

/**
 * 验证DID签名请求
 */
class VerifyDIDRequest {
    /**
     * DID标识符
     */
    @NotBlank(message = "DID不能为空")
    private String did;

    /**
     * 挑战字符串
     */
    @NotBlank(message = "挑战字符串不能为空")
    private String challenge;

    /**
     * 签名(Base64编码)
     */
    @NotBlank(message = "签名不能为空")
    private String signature;

    public VerifyDIDRequest() {}

    // Getters and Setters
    public String getDid() { return did; }
    public void setDid(String did) { this.did = did; }

    public String getChallenge() { return challenge; }
    public void setChallenge(String challenge) { this.challenge = challenge; }

    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }
}

/**
 * DID响应
 */
public class DIDResponse {
    private String did;
    private com.chainpass.did.entity.DIDDocument document;
    private String publicKey;
    private String status;
    private String createdAt;
    private String expiresAt;

    public DIDResponse() {}

    // Getters and Setters
    public String getDid() { return did; }
    public void setDid(String did) { this.did = did; }

    public com.chainpass.did.entity.DIDDocument getDocument() { return document; }
    public void setDocument(com.chainpass.did.entity.DIDDocument document) { this.document = document; }

    public String getPublicKey() { return publicKey; }
    public void setPublicKey(String publicKey) { this.publicKey = publicKey; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getExpiresAt() { return expiresAt; }
    public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }
}