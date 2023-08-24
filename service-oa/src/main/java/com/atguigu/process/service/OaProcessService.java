package com.atguigu.process.service;

import com.atguigu.model.process.Process;
import com.atguigu.vo.process.ApprovalVo;
import com.atguigu.vo.process.ProcessFormVo;
import com.atguigu.vo.process.ProcessQueryVo;
import com.atguigu.vo.process.ProcessVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * <p>
 * 审批类型 服务类
 * </p>
 *
 * @author cheny
 * @since 2023-08-23
 */
public interface OaProcessService extends IService<Process> {

    IPage<ProcessVo> selectPage(Page<ProcessVo> processPage, ProcessQueryVo processQueryVo);

    //部署流程定义
    void deployByZip(String depioypath);

    //启动流程实例
    void startUp(ProcessFormVo processFormVo);


    IPage<ProcessVo> findfindPending(Page<Process> processPage);

    Map<String, Object> show(Long id);

    void approve(ApprovalVo approvalVo);

    Object findProcessed(Page<Process> pageParam);

    Object findStarted(Page<ProcessVo> pageParam);
}
