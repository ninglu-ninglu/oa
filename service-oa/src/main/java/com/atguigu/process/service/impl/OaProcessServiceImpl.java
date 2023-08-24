package com.atguigu.process.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.auth.service.SysUserService;
import com.atguigu.model.process.Process;
import com.atguigu.model.process.ProcessRecord;
import com.atguigu.model.process.ProcessTemplate;
import com.atguigu.model.system.SysUser;
import com.atguigu.process.mapper.OaProcessMapper;
import com.atguigu.process.service.OaProcessRecordService;
import com.atguigu.process.service.OaProcessService;
import com.atguigu.process.service.OaProcessTemplateService;
import com.atguigu.security.custom.LoginUserInfoHelper;
import com.atguigu.vo.process.ApprovalVo;
import com.atguigu.vo.process.ProcessFormVo;
import com.atguigu.vo.process.ProcessQueryVo;
import com.atguigu.vo.process.ProcessVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

import org.activiti.bpmn.model.*;


/**
 * <p>
 * 审批类型 服务实现类
 * </p>
 *
 * @author cheny
 * @since 2023-08-23
 */
@Service
public class OaProcessServiceImpl extends ServiceImpl<OaProcessMapper, Process> implements OaProcessService {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private OaProcessTemplateService processTemplateService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private OaProcessRecordService processRecordService;

    @Autowired
    private HistoryService historyService;
    @Override
    public IPage<ProcessVo> selectPage(Page<ProcessVo> processPage, ProcessQueryVo processQueryVo) {
        IPage<ProcessVo> pageModel = baseMapper.selectPage(processPage, processQueryVo);
        return pageModel;
    }

    //部署流程定义
    @Override
    public void deployByZip(String depioypath) {
        InputStream inputStream=this.getClass().getClassLoader().getResourceAsStream(depioypath);
        ZipInputStream zipInputStream=new ZipInputStream(inputStream);
        Deployment deploy = repositoryService.createDeployment().addZipInputStream(zipInputStream).deploy();
    }

    //启动流程实例
    @Override
    public void startUp(ProcessFormVo processFormVo) {
        SysUser sysUser = sysUserService.getById(LoginUserInfoHelper.getUserId());

        ProcessTemplate processTemplate = processTemplateService.getById(processFormVo.getProcessTemplateId());

        Process process=new Process();
        //把processFormVo复制到process
        BeanUtils.copyProperties(processFormVo,process);
        //其他值
        String workNo = System.currentTimeMillis() + "";
        process.setProcessCode(workNo);
        process.setUserId(LoginUserInfoHelper.getUserId());
        process.setFormValues(processFormVo.getFormValues());
        process.setTitle(sysUser.getName() + "发起" + processTemplate.getName() + "申请");
        process.setStatus(1);
        baseMapper.insert(process);
        //启动实例

        //4.1 流程定义key
        //4.2 业务key  processId
        //4.3 流程参数 form表单json数据，转换map集合
        String key = processTemplate.getProcessDefinitionKey();
        String businessKey = String.valueOf(process.getId());
        String formValues = processFormVo.getFormValues();
        JSONObject jsonObject = JSON.parseObject(formValues);
        JSONObject formData = jsonObject.getJSONObject("formData");

        Map<String,Object> map=new HashMap<>();
        for (Map.Entry<String,Object> entry: formData.entrySet()){
            map.put(entry.getKey(),entry.getValue());
        }
        Map<String,Object> map1=new HashMap<>();
        map1.put("data",map);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(key, businessKey, map1);

        //查询下一个审批人
        List<Task> list=this.getCurrentTaskList(processInstance.getId());
        List<String> nameList=new ArrayList<>();
        for (Task task:list){
            String assigneeName = task.getAssignee();
            SysUser sysUser1=sysUserService.getUserByUserName(assigneeName);
            String name = sysUser1.getName();
            nameList.add(name);
            //给审批人推送消息
        }
        process.setProcessInstanceId(processInstance.getId());
        process.setDescription("等待"+ StringUtils.join(nameList.toArray(),",") +"审批");
        baseMapper.updateById(process);

        //记录操作审批信息
        processRecordService.record(process.getId(),1,"发起申请");
    }

    public IPage<ProcessVo> findfindPending(Page<Process> processPage) {
        TaskQuery query = taskService.createTaskQuery().taskAssignee(LoginUserInfoHelper.getUsername()).orderByTaskCreateTime().desc();

        //封装查询条件

        //开始位置，
        int i=(int) ((processPage.getCurrent()-1)*processPage.getSize());
        //每页记录数
        int size = (int)processPage.getSize();
        List<Task> tasks = query.listPage(i, size);
        long totalCount = query.count();
        List<ProcessVo> processVoList=new ArrayList<>();

        for (Task t:tasks){
            String processInstanceId = t.getProcessInstanceId();
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
            if (processInstance == null) {
                continue;
            }
            // 业务key
            String businessKey = processInstance.getBusinessKey();
            if (businessKey == null) {
                continue;
            }
            long l = Long.parseLong(businessKey);
            Process process = baseMapper.selectById(l);
//            Process process = this.getById(Long.parseLong(businessKey));
            ProcessVo processVo = new ProcessVo();
//            BeanUtils.copyProperties(process, processVo);
            BeanUtils.copyProperties(process,processVo);
            processVo.setTaskId(t.getId());
            processVoList.add(processVo);
        }

        IPage<ProcessVo> page = new Page<>(processPage.getCurrent(), processPage.getSize(), totalCount);
        page.setRecords(processVoList);
        return page;
    }

    @Override
    public Map<String, Object> show(Long id) {
        Process process = baseMapper.selectById(id);
        LambdaQueryWrapper<ProcessRecord> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(ProcessRecord::getProcessId,id);
        List<ProcessRecord> processRecordList = processRecordService.list(wrapper);
        ProcessTemplate processTemplate = processTemplateService.getById(process.getProcessTemplateId());
        boolean isApprove=true;
        List<Task> taskList = this.getCurrentTaskList(process.getProcessInstanceId());
        for (Task t:taskList){
            //判断任务审批人是否是用户
            String username = LoginUserInfoHelper.getUsername();
            if (t.getAssignee()==username){
                isApprove=true;
            }
        }
        Map<String,Object> map=new HashMap<>();
        map.put("process", process);
        map.put("processRecordList", processRecordList);
        map.put("processTemplate", processTemplate);
        map.put("isApprove", isApprove);
        return map;

    }

    @Override
    public void approve(ApprovalVo approvalVo) {
        String taskId = approvalVo.getTaskId();
        Map<String,Object> map=new HashMap<>();
        for (Map.Entry<String,Object> entry :map.entrySet()){

        }
        if (approvalVo.getStatus()==1){
            //设置流程变量，演示
            Map<String,Object> map1=new HashMap<>();
            taskService.complete(taskId,map1);
        }else {
            this.endTask(taskId);
        }
        //3 记录审批相关过程信息 oa_process_record
        String description = approvalVo.getStatus().intValue() ==1 ? "已通过" : "驳回";
        processRecordService.record(approvalVo.getProcessId(),
                approvalVo.getStatus(),description);

        Process process = baseMapper.selectById(approvalVo.getProcessId());
        List<Task> taskList=this.getCurrentTaskList(process.getProcessInstanceId());
        if(!CollectionUtils.isEmpty(taskList)){
            List<String> assignList=new ArrayList<>();
            for (Task t:taskList){
                //得到真实姓名
                String assignee = t.getAssignee();
                SysUser sysUser = sysUserService.getUserByUserName(assignee);
                assignList.add(sysUser.getName());

            }
            //更新流程
            process.setDescription("等待" + StringUtils.join(assignList.toArray(), ",") + "审批");
            process.setStatus(1);
        }else {
            if(approvalVo.getStatus().intValue() == 1) {
                process.setDescription("审批完成（通过）");
                process.setStatus(2);
            } else {
                process.setDescription("审批完成（驳回）");
                process.setStatus(-1);
            }
            baseMapper.updateById(process);
        }
    }

    @Override
    public Object findProcessed(Page<Process> pageParam) {
        //封装查询条件
        HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(LoginUserInfoHelper.getUsername()).finished()
                .orderByTaskCreateTime().desc();
        List<HistoricTaskInstance> list=query.listPage((int)(((pageParam.getCurrent())-1)*pageParam.getSize()),(int) pageParam.getSize());
        List<ProcessVo> processVoList=new ArrayList<>();
        for(HistoricTaskInstance item : list) {
            //流程实例id
            String processInstanceId = item.getProcessInstanceId();
            //根据流程实例id查询获取process信息
            LambdaQueryWrapper<Process> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Process::getProcessInstanceId,processInstanceId);
            Process process = baseMapper.selectOne(wrapper);
            // process -- processVo
            ProcessVo processVo = new ProcessVo();
            BeanUtils.copyProperties(process,processVo);
            processVo.setTaskId("0");
            //放到list
            processVoList.add(processVo);
        }
        IPage<ProcessVo> pageModel =
                new Page<ProcessVo>(pageParam.getCurrent(),pageParam.getSize(),
                        query.count());
        pageModel.setRecords(processVoList);
        return pageModel;
    }

    @Override
    public Object findStarted(Page<ProcessVo> pageParam) {
        ProcessQueryVo processQueryVo = new ProcessQueryVo();
        processQueryVo.setUserId(LoginUserInfoHelper.getUserId());
        IPage<ProcessVo> page = baseMapper.selectPage(pageParam, processQueryVo);
        for (ProcessVo item : page.getRecords()) {
            item.setTaskId("0");
        }
        return page;
    }


    //结束流程
    private void endTask(String taskId) {
        //1 根据任务id获取任务对象 Task
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();

        //2 获取流程定义模型 BpmnModel
        BpmnModel bpmnModel = repositoryService.getBpmnModel(task.getProcessDefinitionId());

        //3 获取结束流向节点
        List<EndEvent> endEventList = bpmnModel.getMainProcess().findFlowElementsOfType(EndEvent.class);
        if(CollectionUtils.isEmpty(endEventList)) {
            return;
        }
        FlowNode endFlowNode = (FlowNode)endEventList.get(0);

        //4 当前流向节点
        FlowNode currentFlowNode = (FlowNode)bpmnModel.getMainProcess().getFlowElement(task.getTaskDefinitionKey());

        //  临时保存当前活动的原始方向
        List originalSequenceFlowList = new ArrayList<>();
        originalSequenceFlowList.addAll(currentFlowNode.getOutgoingFlows());
        //5 清理当前流动方向
        currentFlowNode.getOutgoingFlows().clear();

        //6 创建新流向
        SequenceFlow newSequenceFlow = new SequenceFlow();
        newSequenceFlow.setId("newSequenceFlow");
        newSequenceFlow.setSourceFlowElement(currentFlowNode);
        newSequenceFlow.setTargetFlowElement(endFlowNode);

        //7 当前节点指向新方向
        List newSequenceFlowList = new ArrayList();
        newSequenceFlowList.add(newSequenceFlow);
        currentFlowNode.setOutgoingFlows(newSequenceFlowList);

        //8 完成当前任务
        taskService.complete(task.getId());
    }

    //当前
    private List<Task> getCurrentTaskList(String id) {
        List<Task> list = taskService.createTaskQuery().processInstanceId(id).list();
        return list;
    }
}
