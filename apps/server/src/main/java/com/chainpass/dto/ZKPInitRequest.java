package com.chainpass.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * ZKP 初始化请求
 */
public class ZKPInitRequest {

    @NotBlank(message = "会话ID不能为空")
    private String sessionId;

    @NotBlank(message = "公钥不能为空")
    private String publicKey;

    private String jwt;

    public ZKPInitRequest() {}

    // Getters and Setters
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getPublicKey() { return publicKey; }
    public void setPublicKey(String publicKey) { this.publicKey = publicKey; }

    public String getJwt() { return jwt; }
    public void setJwt(String jwt) { this.jwt = jwt; }
}