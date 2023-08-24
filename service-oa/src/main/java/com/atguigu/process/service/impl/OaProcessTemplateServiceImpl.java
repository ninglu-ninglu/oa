package com.atguigu.process.service.impl;

import com.atguigu.model.process.ProcessTemplate;
import com.atguigu.model.process.ProcessType;
import com.atguigu.process.mapper.OaProcessTemplateMapper;
import com.atguigu.process.service.OaProcessService;
import com.atguigu.process.service.OaProcessTemplateService;
import com.atguigu.process.service.OaProcessTypeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * <p>
 * 审批模板 服务实现类
 * </p>
 *
 * @author cheny
 * @since 2023-08-23
 */
@Service
public class OaProcessTemplateServiceImpl extends ServiceImpl<OaProcessTemplateMapper, ProcessTemplate> implements OaProcessTemplateService {

    @Autowired
    private OaProcessTypeService oaProcessTypeService;

    @Autowired
    private OaProcessService oaProcessService;
    @Override
    public IPage<ProcessTemplate> selectPage(Page<ProcessTemplate> objectPage) {
        Page<ProcessTemplate> processTemplatePage = baseMapper.selectPage(objectPage, null);

        List<ProcessTemplate> processTemplateList = processTemplatePage.getRecords();
        for (ProcessTemplate p:processTemplateList) {
            Long processTypeId = p.getProcessTypeId();
            LambdaQueryWrapper<ProcessType> wrapper=new LambdaQueryWrapper<>();
            wrapper.eq(ProcessType::getId,processTypeId);
            ProcessType one = oaProcessTypeService.getOne(wrapper);
            if (one==null){
                continue;
            }
            p.setProcessTypeName(one.getName());
        }

        return processTemplatePage;
    }

    @Override
    public void publish(Long id) {
        //完成修改
        ProcessTemplate processTemplate = baseMapper.selectById(id);
        processTemplate.setStatus(1);
        baseMapper.updateById(processTemplate);

        //TODO 部署流程定义，后续完善
        if(StringUtils.isEmpty(processTemplate.getProcessDefinitionPath())){
            oaProcessService.deployByZip(processTemplate.getProcessDefinitionPath());
        }
    }
}
