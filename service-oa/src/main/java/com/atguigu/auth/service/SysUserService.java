package com.atguigu.auth.service;


import com.atguigu.model.system.SysUser;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author cheny
 * @since 2023-08-21
 */
public interface SysUserService extends IService<SysUser> {

    void updateStatus(Long id, Integer status);

    SysUser getUserByUserName(String username);

    Map<String, Object> getCurrentUser();
}
