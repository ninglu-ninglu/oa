package com.atguigu.auth.mapper;

import com.atguigu.model.system.SysRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

/*
@time 2023/8/14-15:22
@authon cheny
@name 哈哈
@version 1.0
*/
@Repository
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {
}
