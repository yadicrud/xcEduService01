package com.xuecheng.manage_course.dao;

import com.github.pagehelper.PageHelper;
import com.xuecheng.framework.domain.course.Teachplan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeachPlanRepository extends JpaRepository<Teachplan,String> {

    //根据courseid与parentid查询teachplan
    public List<Teachplan> findByCourseidAndParentid(String courseId, String parentId);
}
