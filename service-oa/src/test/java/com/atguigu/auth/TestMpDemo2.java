package com.atguigu.auth;


import com.atguigu.auth.service.SysRoleService;
import com.atguigu.model.system.SysRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/*
@time 2023/8/14-15:24
@authon cheny
@name 哈哈
@version 1.0
*/
@SpringBootTest
public class TestMpDemo2 {
    @Autowired
    private SysRoleService service;

    @Test
    public void getAll(){
        List<SysRole> list = service.list();
        System.out.println(list);
    }


}
