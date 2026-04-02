package com.chainpass.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chainpass.dto.CreateUserRequest;
import com.chainpass.dto.UpdateUserRequest;
import com.chainpass.entity.User;
import com.chainpass.service.UserService;
import com.chainpass.vo.ApiResponse;
import com.chainpass.vo.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 */

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    /**
     * 获取用户列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('system:user:list')")
    public ApiResponse<PageResponse<User>> getUserList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Integer status
    ) {
        Page<User> userPage = userService.getUserList(page, pageSize, username, email, status);

        PageResponse<User> response = new PageResponse<>();
        response.setList(userPage.getRecords());
        response.setTotal(userPage.getTotal());
        response.setPage(page);
        response.setPageSize(pageSize);

        return ApiResponse.success(response);
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:list')")
    public ApiResponse<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ApiResponse.success(user);
    }

    /**
     * 创建用户
     */
    @PostMapping
    @PreAuthorize("hasAuthority('system:user:add')")
    public ApiResponse<Void> createUser(@Valid @RequestBody CreateUserRequest request) {
        userService.createUser(request);
        log.info("User created: {}", request.getUsername());
        return ApiResponse.success();
    }

    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:edit')")
    public ApiResponse<Void> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        userService.updateUser(id, request);
        log.info("User updated: {}", id);
        return ApiResponse.success();
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:delete')")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        log.info("User deleted: {}", id);
        return ApiResponse.success();
    }

    /**
     * 修改用户状态
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('system:user:edit')")
    public ApiResponse<Void> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.updateUserStatus(id, status);
        log.info("User status updated: {} -> {}", id, status);
        return ApiResponse.success();
    }

    /**
     * 重置用户密码
     */
    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('system:user:edit')")
    public ApiResponse<String> resetPassword(@PathVariable Long id) {
        String newPassword = userService.resetPassword(id);
        log.info("Password reset for user: {}", id);
        return ApiResponse.success("密码已重置", newPassword);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/current")
    public ApiResponse<User> getCurrentUser() {
        User user = userService.getCurrentUser();
        return ApiResponse.success(user);
    }

    /**
     * 更新当前用户信息
     */
    @PutMapping("/current")
    public ApiResponse<Void> updateCurrentUser(@RequestBody User user) {
        userService.updateCurrentUser(user);
        return ApiResponse.success();
    }

    /**
     * 修改密码
     */
    @PutMapping("/current/password")
    public ApiResponse<Void> changePassword(
            @RequestParam String oldPassword,
            @RequestParam String newPassword
    ) {
        userService.changePassword(oldPassword, newPassword);
        return ApiResponse.success();
    }
}