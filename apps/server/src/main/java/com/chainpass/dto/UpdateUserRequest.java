package com.chainpass.dto;

import java.util.List;

/**
 * 更新用户请求
 */
public class UpdateUserRequest {

    private String nickname;

    private String email;

    private String phone;

    private String avatar;

    private Integer status;

    private List<Long> roles;

    public UpdateUserRequest() {}

    // Getters and Setters
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public List<Long> getRoles() { return roles; }
    public void setRoles(List<Long> roles) { this.roles = roles; }
}