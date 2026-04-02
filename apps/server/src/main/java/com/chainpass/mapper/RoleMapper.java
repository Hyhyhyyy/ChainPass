package com.chainpass.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chainpass.entity.Role;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 角色 Mapper
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {
}