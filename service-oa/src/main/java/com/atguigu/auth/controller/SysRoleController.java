package com.atguigu.auth.controller;

import com.atguigu.auth.service.SysRoleService;
import com.atguigu.common.config.handler.GuiguException;
import com.atguigu.common.result.Result;
import com.atguigu.model.system.SysRole;
import com.atguigu.vo.system.AssginRoleVo;
import com.atguigu.vo.system.SysRoleQueryVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/*
@time 2023/8/14-18:05
@authon cheny
@name 哈哈
@version 1.0
*/
@Api(tags = "角色管理接口")
@RestController
@RequestMapping("/admin/system/sysRole")
public class SysRoleController {
    @Autowired
    private SysRoleService sysRoleService;

    //    @GetMapping("findAll")
//    public List<SysRole> findAll(){
//       List<SysRole> list = sysRoleService.list();
//       return list;
//    }
    @ApiOperation("获取角色")
    @GetMapping("/toAssign/{userId}")
    public Result toAssign(@PathVariable Long userId){
        Map<String, Object> map=sysRoleService.findroleDataByUserId(userId);
        return Result.ok(map);
    }
    @ApiOperation("为用户分配角色")
    @PostMapping("/doAssign")
    public Result doAssign(@RequestBody AssginRoleVo assginRoleVo){
        sysRoleService.doAssign(assginRoleVo);
        return Result.ok();
    }







    @ApiOperation(value = "查询所有角色")
    @GetMapping("findAll")
    public Result findAll() {
        List<SysRole> list = sysRoleService.list();

//        try {
//            int i=10/0;
//        } catch (Exception e) {
//            throw new GuiguException(20001,"执行了自定义异常");
//        }
        return Result.ok(list);
    }

    //条件分页查询
    @PreAuthorize("hasAuthority('bnt.sysRole.list')")
    @ApiOperation("条件分页查询")
    @GetMapping("{page}/{limit}")
    public Result pageQueryRole(@PathVariable() Long page, @PathVariable Long limit, SysRoleQueryVo sysRoleQueryVo){
        //创建一个分页对象
        Page<SysRole> page1=new Page<>(page,limit);

        LambdaQueryWrapper<SysRole> wrapper=new LambdaQueryWrapper<>();
        String roleName = sysRoleQueryVo.getRoleName();
        if (!StringUtils.isEmpty(roleName)){
            wrapper.like(SysRole::getRoleName,roleName);
        }

        IPage<SysRole> page2 = sysRoleService.page(page1, wrapper);
        return Result.ok(page2);
    }

    @PreAuthorize("hasAuthority('bnt.sysRole.add')")
    @ApiOperation("添加角色")
    @PostMapping("/save")
    public Result save(@RequestBody SysRole role){
        boolean is_success = sysRoleService.save(role);
        if (is_success){
            return Result.ok();
        }else {
            return  Result.fail();
        }
    }

    @PreAuthorize("hasAuthority('bnt.sysRole.list')")
    @ApiOperation("根据id查询")
    @GetMapping("get/{id}")
    public  Result get(@PathVariable Long id){
        SysRole sysRole = sysRoleService.getById(id);
        if (sysRole!=null){
            return Result.ok(sysRole);
        }else {
            return  Result.fail();
        }
    }

    @PreAuthorize("hasAuthority('bnt.sysRole.update')")
    @ApiOperation("修改角色")
    @PutMapping("update")
    public Result update(@RequestBody SysRole role){
        boolean is_success = sysRoleService.updateById(role);
        if (is_success){
            return Result.ok();
        }else {
            return  Result.fail();
        }
    }
    @PreAuthorize("hasAuthority('bnt.sysRole.remove')")
    @ApiOperation("根据id删除")
    @DeleteMapping("remove/{id}")
    public Result remove(@PathVariable Long id){
        boolean is_success = sysRoleService.removeById(id);
        if (is_success){
            return Result.ok();
        }else {
            return  Result.fail();
        }
    }
    //前端数组【1，2，3】
    @PreAuthorize("hasAuthority('bnt.sysRole.remove')")
    @ApiOperation("批量删除")
    @DeleteMapping("batchRemove")
    public Result batchRemove(@RequestBody List<Long> idlist){
        boolean is_success = sysRoleService.removeByIds(idlist);
        if (is_success){
            return Result.ok();
        }else {
            return  Result.fail();
        }
//       return sysRoleService.removeByIds(idlist)?Result.ok():Result.fail();
    }

}
