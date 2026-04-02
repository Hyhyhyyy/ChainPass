package com.chainpass.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chainpass.entity.LoginLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 登录日志 Mapper
 */
@Mapper
public interface LoginLogMapper extends BaseMapper<LoginLog> {
}