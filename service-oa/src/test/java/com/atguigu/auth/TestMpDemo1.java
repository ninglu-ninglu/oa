package com.atguigu.auth;


import com.atguigu.auth.mapper.SysRoleMapper;
import com.atguigu.model.system.SysRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

/*
@time 2023/8/14-15:24
@authon cheny
@name 哈哈
@version 1.0
*/
@SpringBootTest
public class TestMpDemo1 {
    @Autowired
    private SysRoleMapper mapper;

    @Test
    public void getAll(){
        List<SysRole> list = mapper.selectList(null);
        System.out.println(list);
    }
    @Test
    public void add(){
        SysRole sysRole=new SysRole();
        sysRole.setRoleName("角色管理员1");
        sysRole.setRoleCode("role1");
        sysRole.setDescription("角色管理员1");

        int result = mapper.insert(sysRole);
        System.out.println(result); //影响的行数
        System.out.println(sysRole.getId()); //id自动回填
    }
    @Test
    public void del(){
        int i = mapper.deleteById(10L);
        System.out.println(i);
    }
    @Test
    public void testDeleteBatchIds() {
        int result = mapper.deleteBatchIds(Arrays.asList(1, 2));
        System.out.println(result);
    }

}
