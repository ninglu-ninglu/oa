package com.atguigu.auth.controller;

import com.atguigu.auth.service.SysMenuService;
import com.atguigu.auth.service.SysUserService;
import com.atguigu.common.config.handler.GuiguException;
import com.atguigu.common.jwt.JwtHelper;
import com.atguigu.common.result.Result;
import com.atguigu.common.utils.MD5;
import com.atguigu.model.system.SysUser;
import com.atguigu.vo.system.LoginVo;
import com.atguigu.vo.system.RouterVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
@time 2023/8/20-13:18
@authon cheny
@name 哈哈
@version 1.0
*/
@Api(tags = "后台登录管理")
@CrossOrigin
@RestController
@RequestMapping("/admin/system/index")
public class IndexController {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysMenuService sysMenuService;
    //login
//    @PostMapping("login")
//    public Result login(){
//        Map<String,Object> map=new HashMap<>();
//        map.put("token","admin-token");
//        return Result.ok(map);
//    }
    @PostMapping("login")
    public Result login(@RequestBody LoginVo loginVo){
        // 获取输入用户名和密码
        String username = loginVo.getUsername();
        //根据用户名查询数据库
        SysUser sysUser = sysUserService.getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        //判断用户是否存在
        if (sysUser==null){
            throw new GuiguException(201,"用户不存在");
        }
        //判断密码
        //获取数据库密码
        String password = sysUser.getPassword();
        //自己输入的密码
        //对自己的密码加密
        String password1 = MD5.encrypt(loginVo.getPassword());
        if (!password.equals(password1)){
            throw new GuiguException(201,"密码错误");
        }
        //判断用户是否被禁用
        if(sysUser.getStatus().intValue()==0){
            throw new GuiguException(201,"用户已被禁用");
        }
        //使用jwt根据用户id和用户名称生成token字符串
        String token = JwtHelper.createToken(sysUser.getId(), sysUser.getName());

        //返回
        Map<String,Object> map=new HashMap<>();
        map.put("token",token);
        return Result.ok(map);
    }


//    @GetMapping("info")
//    public Result info() {
//        Map<String, Object> map = new HashMap<>();
//        map.put("roles","[admin]");
//        map.put("name","admin");
//        map.put("avatar","https://oss.aliyuncs.com/aliyun_id_photo_bucket/default_handsome.jpg");
//        return Result.ok(map);
//    }
    @GetMapping("info")
    public Result info(HttpServletRequest response) {
        //从请求头获取信息
        String token = response.getHeader("token");
        //从token获取用户id和名称
        Long userId =JwtHelper.getUserId(token);
        //根据用户id查询数据库，获取用户信息
        SysUser sysUser = sysUserService.getById(userId);
        //根据用户id获取用户可以操作的列表
        //查询数据库动态构建路由
        List<RouterVo> routerList = sysMenuService.findUserMenuListByUserId(userId);
        //根据用户id获取可以操作的按钮
        List<String> permsList=sysMenuService.findUserPermsByUserId(userId);
        //返回
        Map<String, Object> map = new HashMap<>();
        map.put("roles","[admin]");
        map.put("name",sysUser.getName());
        map.put("avatar","https://oss.aliyuncs.com/aliyun_id_photo_bucket/default_handsome.jpg");
        //可以操作的菜单
        map.put("routers",routerList);
        //可以操作的按钮
        map.put("buttons",permsList);
        return Result.ok(map);
    }
    /**
     * 退出
     * @return
     */
    @PostMapping("logout")
    public Result logout(){
        return Result.ok();
    }

}
