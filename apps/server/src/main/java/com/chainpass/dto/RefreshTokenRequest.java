package com.chainpass.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Token刷新请求
 */
public class RefreshTokenRequest {

    @NotBlank(message = "刷新令牌不能为空")
    private String refreshToken;

    public RefreshTokenRequest() {}

    // Getters and Setters
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}