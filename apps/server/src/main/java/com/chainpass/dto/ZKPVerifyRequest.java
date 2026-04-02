package com.chainpass.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * ZKP 验证请求
 */
public class ZKPVerifyRequest {

    @NotBlank(message = "会话ID不能为空")
    private String sessionId;

    @NotBlank(message = "签名不能为空")
    private String signature;

    @NotBlank(message = "公钥不能为空")
    private String publicKey;

    public ZKPVerifyRequest() {}

    // Getters and Setters
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }

    public String getPublicKey() { return publicKey; }
    public void setPublicKey(String publicKey) { this.publicKey = publicKey; }
}