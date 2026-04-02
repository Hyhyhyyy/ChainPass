package com.chainpass.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chainpass.dto.CreateUserRequest;
import com.chainpass.dto.UpdateUserRequest;
import com.chainpass.entity.LoginUser;
import com.chainpass.entity.User;
import com.chainpass.exception.BusinessException;
import com.chainpass.mapper.UserMapper;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 用户服务
 */

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserService.class);

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    // 密码强度正则：至少8位，包含大小写字母和数字
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$"
    );

    /**
     * 验证密码强度
     */
    private void validatePasswordStrength(String password) {
        if (password == null || password.length() < 8) {
            throw new BusinessException("密码长度至少8位");
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new BusinessException("密码必须包含大小写字母和数字");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 查询用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        // TODO: 从数据库获取权限
        List<String> permissions = List.of("system:test:list", "system:user:list");

        log.debug("User loaded: {}", username);
        return new LoginUser(user, permissions);
    }

    /**
     * 获取用户列表
     */
    public Page<User> getUserList(Integer page, Integer pageSize, String username, String email, Integer status) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(username)) {
            queryWrapper.like(User::getUsername, username);
        }
        if (StringUtils.hasText(email)) {
            queryWrapper.like(User::getEmail, email);
        }
        if (status != null) {
            queryWrapper.eq(User::getStatus, status);
        }

        queryWrapper.orderByDesc(User::getCreatedAt);

        return userMapper.selectPage(new Page<>(page, pageSize), queryWrapper);
    }

    /**
     * 获取用户详情
     */
    public User getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    /**
     * 创建用户
     */
    public void createUser(CreateUserRequest request) {
        // 验证密码强度
        validatePasswordStrength(request.getPassword());

        // 检查用户名是否存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, request.getUsername());
        if (userMapper.selectCount(queryWrapper) > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 检查邮箱是否存在
        if (StringUtils.hasText(request.getEmail())) {
            LambdaQueryWrapper<User> emailQuery = new LambdaQueryWrapper<>();
            emailQuery.eq(User::getEmail, request.getEmail());
            if (userMapper.selectCount(emailQuery) > 0) {
                throw new BusinessException("邮箱已被使用");
            }
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(StringUtils.hasText(request.getNickname()) ? request.getNickname() : request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setStatus(0);
        user.setDelFlag(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.insert(user);
        log.info("User created: {}", request.getUsername());
    }

    /**
     * 更新用户
     */
    public void updateUser(Long id, UpdateUserRequest request) {
        User user = getUserById(id);

        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.updateById(user);
        log.info("User updated: {}", id);
    }

    /**
     * 删除用户（逻辑删除）
     */
    public void deleteUser(Long id) {
        User user = getUserById(id);
        user.setDelFlag(1);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        log.info("User deleted: {}", id);
    }

    /**
     * 修改用户状态
     */
    public void updateUserStatus(Long id, Integer status) {
        User user = getUserById(id);
        user.setStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        log.info("User status updated: {} -> {}", id, status);
    }

    /**
     * 重置密码
     */
    public String resetPassword(Long id) {
        User user = getUserById(id);
        String newPassword = UUID.randomUUID().toString().substring(0, 8);
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        log.info("Password reset for user: {}", id);
        return newPassword;
    }

    /**
     * 获取当前用户
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser) {
            LoginUser loginUser = (LoginUser) authentication.getPrincipal();
            return loginUser.getUser();
        }
        throw new BusinessException("未登录");
    }

    /**
     * 更新当前用户信息
     */
    public void updateCurrentUser(User userUpdate) {
        User currentUser = getCurrentUser();

        if (userUpdate.getNickname() != null) {
            currentUser.setNickname(userUpdate.getNickname());
        }
        if (userUpdate.getEmail() != null) {
            currentUser.setEmail(userUpdate.getEmail());
        }
        if (userUpdate.getPhone() != null) {
            currentUser.setPhone(userUpdate.getPhone());
        }
        if (userUpdate.getAvatar() != null) {
            currentUser.setAvatar(userUpdate.getAvatar());
        }
        currentUser.setUpdatedAt(LocalDateTime.now());

        userMapper.updateById(currentUser);
    }

    /**
     * 修改密码
     */
    public void changePassword(String oldPassword, String newPassword) {
        User currentUser = getCurrentUser();

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, currentUser.getPassword())) {
            throw new BusinessException("原密码错误");
        }

        // 验证新密码强度
        validatePasswordStrength(newPassword);

        // 更新密码
        currentUser.setPassword(passwordEncoder.encode(newPassword));
        currentUser.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(currentUser);
        log.info("Password changed for user: {}", currentUser.getUsername());
    }

    /**
     * 根据 Gitee ID 查询用户
     */
    public User selectUserByGiteeId(String giteeId) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getGiteeId, giteeId);
        return userMapper.selectOne(queryWrapper);
    }

    /**
     * 注册用户
     */
    public void registerUser(User user) {
        // 验证密码强度
        validatePasswordStrength(user.getPassword());

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus(0);
        user.setDelFlag(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);
        log.info("User registered: {}", user.getUsername());
    }
}