package com.atguigu.process.service;

import com.atguigu.model.process.ProcessRecord;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 审批记录 服务类
 * </p>
 *
 * @author cheny
 * @since 2023-08-23
 */
public interface OaProcessRecordService extends IService<ProcessRecord> {

    //
    void record(Long processId,Integer status,String description);
}
