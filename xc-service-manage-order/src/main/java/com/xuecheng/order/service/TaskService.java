package com.xuecheng.order.service;

import com.xuecheng.framework.domain.task.XcTask;
import com.xuecheng.framework.domain.task.XcTaskHis;
import com.xuecheng.order.dao.XcTaskHisRepository;
import com.xuecheng.order.dao.XcTaskRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Date;
import java.util.List;
import java.util.Optional;


@Service
public class TaskService {

    @Autowired
    XcTaskRepository xcTaskRepository;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    XcTaskHisRepository xcTaskHisRepository;

    /**
     * Created with IntelliJ IDEA.
     *
     * @Description:
     * @param:
     * @return:
     * @Auther: liuyadi
     * @Date: 2021/2/26
     */
    public List<XcTask> findTaskList(Date updateTime,int size){
        Pageable pageable=new PageRequest(0,size);
        Page<XcTask> listPage = xcTaskRepository.findByUpdateTimeBefore(pageable, updateTime);
        List<XcTask> content = listPage.getContent();

        return content;
    }
    
    /**
     * Created with IntelliJ IDEA.
     * 
     * @Description: 发布消息
     * @param: 
     * @return: 
     * @Auther: liuyadi
     * @Date: 2021/2/28
     */
    public void publish(XcTask xcTask,String ex,String routingKey) {
        Optional<XcTask> optional = xcTaskRepository.findById(xcTask.getId());

        if (optional.isPresent()) {
            rabbitTemplate.convertAndSend(ex, routingKey, xcTask);
            //更新时间
            XcTask xcTask1 = optional.get();
            xcTask1.setUpdateTime(new Date());
            xcTaskRepository.save(xcTask1);
        }
    }
    /**
     * Created with IntelliJ IDEA.
     *
     * @Description: 乐观锁跟新版本号
     * @param: [xcTask, ex, routingKey]
     * @return: void
     * @Auther: liuyadi
     * @Date: 2021/3/1
     */
    @Transactional
    public int getTask(String id,int version){
        int count = xcTaskRepository.getTask(id, version);
        return count;
    }
    /**
     * Created with IntelliJ IDEA.
     * 
     * @Description: 完成任务
     * @param: 
     * @return: 
     * @Auther: liuyadi
     * @Date: 2021/3/2
     */
    @Transactional
    public void finishTask(String taskid){
        Optional<XcTask> optional = xcTaskRepository.findById(taskid);
        if(optional.isPresent()){
            //当前任务
            XcTask xcTask =optional.get();
            //历史任务
            XcTaskHis xcTaskHis =new XcTaskHis();
            BeanUtils.copyProperties(xcTask,xcTaskHis);
            xcTaskHisRepository.save(xcTaskHis);
            xcTaskRepository.delete(xcTask);
        }


    }
}
