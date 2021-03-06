package com.xuecheng.api.course;

import com.xuecheng.framework.domain.course.Teachplan;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.model.response.ResponseResult;

public interface CourseControlApi {
    //课程计划查询
    public TeachplanNode findTeachplanList(String courseId);
    //添加课程计划
    public ResponseResult addTeachPlan(Teachplan teachplan);
}
