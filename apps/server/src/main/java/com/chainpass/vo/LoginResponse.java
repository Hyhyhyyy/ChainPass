package com.chainpass.vo;

import java.util.List;

/**
 * 登录响应
 */
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private List<String> permissions;

    public LoginResponse() {}

    public LoginResponse(String accessToken, String refreshToken, Long userId, String username,
                         String nickname, String avatar, List<String> permissions) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.username = username;
        this.nickname = nickname;
        this.avatar = avatar;
        this.permissions = permissions;
    }

    // Getters and Setters
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }

    // Builder
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String accessToken;
        private String refreshToken;
        private Long userId;
        private String username;
        private String nickname;
        private String avatar;
        private List<String> permissions;

        public Builder accessToken(String accessToken) { this.accessToken = accessToken; return this; }
        public Builder refreshToken(String refreshToken) { this.refreshToken = refreshToken; return this; }
        public Builder userId(Long userId) { this.userId = userId; return this; }
        public Builder username(String username) { this.username = username; return this; }
        public Builder nickname(String nickname) { this.nickname = nickname; return this; }
        public Builder avatar(String avatar) { this.avatar = avatar; return this; }
        public Builder permissions(List<String> permissions) { this.permissions = permissions; return this; }

        public LoginResponse build() {
            return new LoginResponse(accessToken, refreshToken, userId, username, nickname, avatar, permissions);
        }
    }
}
