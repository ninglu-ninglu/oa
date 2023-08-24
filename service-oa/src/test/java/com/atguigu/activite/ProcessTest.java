package com.atguigu.activite;

import com.atguigu.ServiceAuthApplication;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/*
@time 2023/8/22-21:14
@authon cheny
@name 哈哈
@version 1.0
*/
@SpringBootTest(classes = ServiceAuthApplication.class)
public class ProcessTest {

    @Autowired
    private RepositoryService repositoryService;

    @Test
    public void  deployProcess(){
        Deployment 请假流程 = repositoryService.createDeployment().addClasspathResource("process/qingjia.bpmn20.xml")
                .addClasspathResource("process/qingjia.png").name("请假流程").deploy();
        System.out.println(请假流程.getId());
        System.out.println(请假流程.getName());
    }

}
