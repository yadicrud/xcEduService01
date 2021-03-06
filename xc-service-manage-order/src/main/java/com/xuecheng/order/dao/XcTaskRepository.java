package com.xuecheng.order.dao;

import com.xuecheng.framework.domain.task.XcTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;

public interface XcTaskRepository extends JpaRepository<XcTask,String> {
    //查询某个时间之前的 n条任务
    Page<XcTask> findByUpdateTimeBefore(Pageable pageable, Date updateTime);

    //更新updateTime
    @Modifying
    @Query("update XcTask t set t.updateTime = :updateTime where t.id =:id")
    public int updateTaskTime(@Param("id") String id, @Param("updateTime")Date updateTime);

    //乐观锁更新版本号
    @Modifying
    @Query("update XcTask t set t.version = :version+1 where t.id =:id and t.version=:version")
    public int getTask(@Param("id") String id, @Param("version")int version);

}
