package com.xuecheng.order.mq;

import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.order.config.RabbitMQConfig;
import com.xuecheng.order.service.TaskService;
import io.netty.util.internal.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

@Component
public class ChooseCourseTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChooseCourseTask.class);

    @Autowired
    TaskService taskService;

    //定时添加选课任务
    @Scheduled(cron="0/3 * * * * *")
    public void sendChooseCourseTask(){
        //得到一分钟之前的时间
        Calendar calendar =new GregorianCalendar();
        calendar.setTime(new Date());
        calendar.add(GregorianCalendar.MINUTE,-1);
        //2018-04-04 22:58:54
        //calendar.set(2021,01,27,22,58,54);
        Date time = calendar.getTime();
        List<XcTask> taskList = taskService.findTaskList(time, 1000);

        for (XcTask xcTask:taskList) {
            if(taskService.getTask(xcTask.getId(),xcTask.getVersion())>0){
                String ex= xcTask.getMqExchange();
                String routingKey =xcTask.getMqRoutingkey();
                String taskId = xcTask.getId();
                taskService.publish(xcTask,ex,routingKey);
                LOGGER.info("send choose course task id:{}",taskId);
            }

        }
        LOGGER.info(taskList.toString());
        System.out.println(taskList);
    }

    @RabbitListener(queues = RabbitMQConfig.XC_LEARNING_FINISHADDCHOOSECOURSE)
    public void receiveFinishChooseCourseTask(XcTask xcTask){
        if(xcTask!=null && StringUtils.isNotEmpty(xcTask.getId())){
            taskService.finishTask(xcTask.getId());
        }



    }








    //定义任务调试策略
    //@Scheduled(cron="0/3 * * * * *")
    public void task1(){
        LOGGER.info("测试定时任务1开始");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LOGGER.info("结束1");
    }
    //@Scheduled(cron="0/3 * * * * *")
    public void task2(){
        LOGGER.info("测试定时任务2开始");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LOGGER.info("结束2");
    }

}
