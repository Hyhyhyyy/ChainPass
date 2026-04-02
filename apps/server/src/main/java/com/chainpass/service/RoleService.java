package com.chainpass.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.chainpass.entity.Role;
import com.chainpass.exception.BusinessException;
import com.chainpass.mapper.RoleMapper;
import com.chainpass.mapper.RolePermissionMapper;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 角色服务
 */

@Service
@RequiredArgsConstructor
public class RoleService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RoleService.class);

    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;

    /**
     * 获取角色列表
     */
    public List<Role> getRoleList() {
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Role::getStatus, 0);
        queryWrapper.orderByAsc(Role::getId);
        return roleMapper.selectList(queryWrapper);
    }

    /**
     * 获取角色详情
     */
    public Role getRoleById(Long id) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }
        return role;
    }

    /**
     * 创建角色
     */
    @Transactional
    public void createRole(Role role) {
        // 检查角色编码是否存在
        LambdaQueryWrapper<Role> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Role::getRoleCode, role.getRoleCode());
        if (roleMapper.selectCount(queryWrapper) > 0) {
            throw new BusinessException("角色编码已存在");
        }

        role.setStatus(0);
        roleMapper.insert(role);
    }

    /**
     * 更新角色
     */
    public void updateRole(Role role) {
        getRoleById(role.getId());
        roleMapper.updateById(role);
    }

    /**
     * 删除角色
     */
    @Transactional
    public void deleteRole(Long id) {
        getRoleById(id);
        // 删除角色权限关联
        rolePermissionMapper.deleteByRoleId(id);
        // 删除角色
        roleMapper.deleteById(id);
    }

    /**
     * 分配权限
     */
    @Transactional
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        getRoleById(roleId);
        // 先删除旧的权限关联
        rolePermissionMapper.deleteByRoleId(roleId);
        // 添加新的权限关联
        if (permissionIds != null && !permissionIds.isEmpty()) {
            rolePermissionMapper.batchInsert(roleId, permissionIds);
        }
    }

    /**
     * 获取角色的权限ID列表
     */
    public List<Long> getRolePermissionIds(Long roleId) {
        return rolePermissionMapper.selectPermissionIdsByRoleId(roleId);
    }
}