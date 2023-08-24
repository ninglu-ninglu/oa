package com.atguigu.process.controller;


import com.atguigu.common.result.Result;
import com.atguigu.model.process.Process;
import com.atguigu.process.service.OaProcessService;
import com.atguigu.process.service.impl.OaProcessServiceImpl;
import com.atguigu.vo.process.ProcessQueryVo;
import com.atguigu.vo.process.ProcessVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 审批类型 前端控制器
 * </p>
 *
 * @author cheny
 * @since 2023-08-23
 */
@RestController
@RequestMapping(value = "/admin/process")
public class OaProcessController {

    @Autowired
    private OaProcessService oaProcessService;

    @PreAuthorize("hasAuthority('bnt.process.list')")
    @ApiOperation(value = "获取分页列表")
    @GetMapping("{page}/{limit}")
    public Result index(@PathVariable Long page, @PathVariable Long limit, ProcessQueryVo processQueryVo) {

        //审批管理列表
        Page<ProcessVo> processPage=new Page(page,limit);
        IPage<ProcessVo> pageModul=oaProcessService.selectPage(processPage,processQueryVo);
        return Result.ok(pageModul);

    }

}

