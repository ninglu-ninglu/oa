package com.atguigu.process.mapper;

import com.atguigu.model.process.Process;
import com.atguigu.vo.process.ProcessQueryVo;
import com.atguigu.vo.process.ProcessVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 审批类型 Mapper 接口
 * </p>
 *
 * @author cheny
 * @since 2023-08-23
 */
public interface OaProcessMapper extends BaseMapper<Process> {
    public IPage<ProcessVo> selectPage(Page<ProcessVo> processPage, @Param("vo") ProcessQueryVo processQueryVo) ;

}
