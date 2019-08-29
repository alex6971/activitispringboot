package com.zhq;

import org.activiti.bpmn.model.*;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.*;
import org.activiti.engine.identity.Group;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipInputStream;

/**
 * @program: activitispringboot
 * @description: 单元测试
 * @author: HQ Zheng
 * @create: 2019-08-27 21:23
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ActivitiApplicationTest {

    private  static final Logger LOGGER=LoggerFactory.getLogger(ActivitiApplicationTest.class);

    @Autowired
    ProcessEngine processEngine;//流程引擎

    /**
     * 提供一系列管理流程定义和流程部署的API。
     */
    @Autowired
    private RepositoryService repositoryService;//存储服务

    /**
     * 在流程运行时对流程实例进行管理与控制。
     */
    @Autowired
    private RuntimeService runtimeService;//运行时服务

    /**
     * 对流程任务进行管理，例如任务提醒、任务完成和创建任务分本任务等。
     */
    @Autowired
    private TaskService taskService;//任务服务

    /**
     * 提供对流程角色数据进行管理的API，这些角色数据包括用户组、用户以及它们之间的关系。
     */
    @Autowired
    private IdentityService identityService;//角色服务

   /* *//**
     * 提供对流程引擎进行管理和维护的服务。
     *//*
    @Autowired
    private ManagementService managementService;//管理和维护服务

    *//**
     * 对流程的历史数据进行操作，包括查询、删除这些历史数据。
     *//*
    @Autowired
    private HistoryService historyService;//历史数据服务

    *//**
     * 使用该服务，可以不需要重新部署流程模型，就可以实现对流程模型的部分修改
     *//*
    @Autowired
    private DynamicBpmnService dynamicBpmnService;//流程模型服务
*/
    /**
     * 测试部署一个bpmn流程文件
     * 部署后在act_re_deployment、act_re_procdef、act_ge_bytearray生成记录
     */
    @Test
    public void testInsertFirstBPMN(){
        //部署一个流程
        repositoryService.createDeployment().addClasspathResource("processes/firstbpm.bpmn").deploy();
    }

    /**
     * 测试启动一个流程，然后正常走完流程
     */
    @Test
    public void testStartFirstBPMN(){
        //通过key启动一个流程
        ProcessInstance myProcess = runtimeService.startProcessInstanceByKey("myProcess");
        //获取当前流程节点
        Task task = taskService.createTaskQuery().processInstanceId(myProcess.getId()).singleResult();
        while (Boolean.TRUE){
            if(task==null){
                System.out.println("流程结束："+task);
                break;
            }else{
                System.out.println("当前流程节点："+task.getName());
                //完成当前节点
                taskService.complete(task.getId());
                //再次获取当前节点
                task = taskService.createTaskQuery().processInstanceId(myProcess.getId()).singleResult();
            }
        }
    }

    /**
     * 对象查询Query：
     * asc desc count list listPage singleResult 用法举例
     * 用户组表查询举例
     */
    @Test
    public void testObjectQuery(){
        //1.查询list()
        List<Group> groupList = identityService.createGroupQuery().list();
        if(groupList==null||groupList.size()==0){//如果，没有测试数据，创建10条
            for(int i=0;i<10;i++){
                Group group=identityService.newGroup(i+"");//创建一个用户组
                group.setName("Group_"+i);
                group.setType("Type_"+i);
                identityService.saveGroup(group);
            }
        }else{//遍历打印输出
            for (Group group : groupList) {
                System.out.println(group.getId()+","+group.getName()+","+group.getType());
            }
            System.out.println("查询list()结束#################################");
        }
        
        //2.分页查询listPage()
        groupList = identityService.createGroupQuery().listPage(1, 5);
        for (Group group : groupList) {
            System.out.println(group.getId()+","+group.getName()+","+group.getType());
        }
        System.out.println("分页查询listPage()结束#################################");

        // 3.count()数量
        long count = identityService.createGroupQuery().count();
        System.out.println("数量count()："+count);
        System.out.println("查询count()数量结束#################################");

        //4.desc和asc，多个字段排序举例
        groupList = identityService.createGroupQuery().orderByGroupName().desc().orderByGroupType().desc().list();
        for (Group group : groupList) {
            System.out.println(group.getId()+","+group.getName()+","+group.getType());
        }
        System.out.println("desc和asc结束#################################");

        //5.查询singResult(),假如数据库中有多个会抛出异常
        Group group = identityService.createGroupQuery().groupName("Group_0").singleResult();
        System.out.println(group.getId()+","+group.getName()+","+group.getType());
        System.out.println("查询singResult()结束#################################");
    }

    /**
     * 原生SQL查询Query
     * 用户组表查询举例
     */
    @Test
    public void testNativeSql(){
        Group group = identityService.createNativeGroupQuery()
                .sql("select * from act_id_group where name_=#{name}")
                .parameter("name", "Group_2").singleResult();
        System.out.println(group.getId()+","+group.getName()+","+group.getType());
    }

    /**
     * 部署工作流
     * addZipInputStream举例
     * @throws FileNotFoundException
     */
    @Test
    public void testAddZipInputStream() throws IOException {
        DeploymentBuilder deployment = repositoryService.createDeployment();
        FileInputStream fileInputStream=new FileInputStream(new File(this.getClass().getResource("/testzip.zip").getFile()));
        ZipInputStream zipInputStream=new ZipInputStream(fileInputStream);
        deployment.addZipInputStream(zipInputStream);
        deployment.deploy();
    }

    /**
     * 部署工作流
     * addBpmModel举例
     */
    @Test
    public void testAddBpmModel(){
        //创建BPM模型对象
        BpmnModel model=new BpmnModel();

        //创建一个流程定义
        Process process=new Process();
        model.addProcess(process);
        process.setId("testBpmModel");
        process.setName("Test BpmModel");

        //开始事件
        StartEvent startEvent=new StartEvent();
        startEvent.setId("startEvent");
        process.addFlowElement(startEvent);

        //用户任务
        UserTask userTask=new UserTask();
        userTask.setId("userTask");
        userTask.setName("User Task");
        process.addFlowElement(userTask);

        //结束事情
        EndEvent endEvent=new EndEvent();
        endEvent.setId("endEvent");
        endEvent.setName("End Event");
        process.addFlowElement(endEvent);

        //添加流程顺序
        process.addFlowElement(new SequenceFlow("startEvent","userTask"));
        process.addFlowElement(new SequenceFlow("userTask","endEvent"));

        //部署流程
        DeploymentBuilder deployment = repositoryService.createDeployment();
        deployment.addBpmnModel("Test AddBpmModel",model);
        deployment.deploy();

    }

}
