package com.atguigu.auth.service.impl;

import com.atguigu.auth.mapper.SysRoleMapper;
import com.atguigu.auth.service.SysRoleService;
import com.atguigu.auth.service.SysUserRoleService;
import com.atguigu.model.system.SysRole;
import com.atguigu.model.system.SysUserRole;
import com.atguigu.vo.system.AssginRoleVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/*
@time 2023/8/14-17:55
@authon cheny
@name 哈哈
@version 1.0
*/
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    @Autowired
    private SysUserRoleService sysUserRoleService;
    //获取角色
    @Override
    public Map<String, Object> findroleDataByUserId(Long userId) {
        //查询所有的角色
        List<SysRole> alllist = baseMapper.selectList(null);
        //拥有的角色id
        LambdaQueryWrapper<SysUserRole> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(SysUserRole::getUserId,userId);
        List<SysUserRole> list = sysUserRoleService.list(lambdaQueryWrapper);
        //对角色进行分类
        List<Long> collect = list.stream().map(c -> c.getRoleId()).collect(Collectors.toList());

        List<SysRole> assginRoleList=new ArrayList<>();
        for (SysRole sysRole:alllist) {
            if (collect.contains(sysRole.getId())){
                assginRoleList.add(sysRole);
            }
        }
        Map<String, Object> roleMap = new HashMap<>();
        roleMap.put("assginRoleList", assginRoleList);
        roleMap.put("alllist", alllist);
        return roleMap;

    }
    //为用户分配角色
    @Override
    public void doAssign(AssginRoleVo assginRoleVo) {
        //把之前角色删除
        LambdaQueryWrapper<SysUserRole> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId,assginRoleVo.getUserId());
        sysUserRoleService.remove(wrapper);
        //重新分配角色
        List<Long> roleIdList = assginRoleVo.getRoleIdList();
        for (Long roleId:roleIdList){
            if (StringUtils.isEmpty(roleId)){
                continue;
            }
            SysUserRole sysUserRole=new SysUserRole();
            sysUserRole.setRoleId(roleId);
            sysUserRole.setUserId(assginRoleVo.getUserId());
            sysUserRoleService.save(sysUserRole);
        }
    }
}
