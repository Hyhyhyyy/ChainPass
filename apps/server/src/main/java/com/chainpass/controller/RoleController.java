package com.chainpass.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chainpass.entity.Role;
import com.chainpass.service.RoleService;
import com.chainpass.vo.ApiResponse;
import com.chainpass.vo.PageResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器
 */

@RestController
@RequestMapping("/role")
@RequiredArgsConstructor
public class RoleController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RoleController.class);

    private final RoleService roleService;

    /**
     * 获取角色列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('system:role:list')")
    public ApiResponse<List<Role>> getRoleList() {
        List<Role> roles = roleService.getRoleList();
        return ApiResponse.success(roles);
    }

    /**
     * 获取角色详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:role:list')")
    public ApiResponse<Role> getRoleById(@PathVariable Long id) {
        Role role = roleService.getRoleById(id);
        return ApiResponse.success(role);
    }

    /**
     * 创建角色
     */
    @PostMapping
    @PreAuthorize("hasAuthority('system:role:add')")
    public ApiResponse<Void> createRole(@RequestBody Role role) {
        roleService.createRole(role);
        log.info("Role created: {}", role.getRoleName());
        return ApiResponse.success();
    }

    /**
     * 更新角色
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:role:edit')")
    public ApiResponse<Void> updateRole(@PathVariable Long id, @RequestBody Role role) {
        role.setId(id);
        roleService.updateRole(role);
        log.info("Role updated: {}", id);
        return ApiResponse.success();
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:role:delete')")
    public ApiResponse<Void> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        log.info("Role deleted: {}", id);
        return ApiResponse.success();
    }

    /**
     * 分配权限
     */
    @PostMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('system:role:edit')")
    public ApiResponse<Void> assignPermissions(@PathVariable Long id, @RequestBody List<Long> permissionIds) {
        roleService.assignPermissions(id, permissionIds);
        log.info("Permissions assigned to role: {}", id);
        return ApiResponse.success();
    }

    /**
     * 获取角色的权限ID列表
     */
    @GetMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('system:role:list')")
    public ApiResponse<List<Long>> getRolePermissions(@PathVariable Long id) {
        List<Long> permissionIds = roleService.getRolePermissionIds(id);
        return ApiResponse.success(permissionIds);
    }
}