package com.xuecheng.manage_course.service;

import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.Teachplan;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_course.dao.CourseBaseRepository;
import com.xuecheng.manage_course.dao.TeachPlanMapper;
import com.xuecheng.manage_course.dao.TeachPlanRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * Created with IntelliJ IDEA.
 *
 * @Description:
 * @param:
 * @return:
 * @Auther: liuyadi
 * @Date: 2021/1/17
 */

@Service
public class CourseService {
    @Autowired
    TeachPlanMapper teachPlanMapper;

    @Autowired
    TeachPlanRepository teachPlanRepository;

    @Autowired
    CourseBaseRepository courseBaseRepository;
    //课程计划的查询
    public TeachplanNode findTeachPlan(String courseId){
        return teachPlanMapper.selectList(courseId);
    }

    //添加课程计划
    @Transactional
    public ResponseResult addTeachPlan(Teachplan teachplan){
        //处理parentId
        if(teachplan==null ||teachplan.getCourseid().isEmpty()||teachplan.getPname().isEmpty()){
            new Exception("teachplan不正确");
        }
        //获取课程id
        String courseId=teachplan.getCourseid();
        //获取parentId
        String parentid = teachplan.getParentid();

        if(StringUtils.isBlank(parentid)){
            parentid = this.getTeachPlanRoot(courseId);
        }
        Teachplan teachplanNew =new Teachplan();
        BeanUtils.copyProperties(teachplan,teachplanNew);
        teachplanNew.setParentid(parentid);
        teachplanNew.setParentid(courseId);
        Optional<Teachplan> optional = teachPlanRepository.findById(parentid);

        if(!optional.isPresent()){
            new Exception("无id");
        }
        if(optional.get().getParentid().equals("0")){
            teachplanNew.setGrade("1");
        }else if(optional.get().getParentid().equals("1")){
            teachplanNew.setGrade("2");
        }else{
            teachplanNew.setGrade("3");
        }
        teachPlanRepository.save(teachplanNew);

        return new ResponseResult(CommonCode.SUCCESS);
    }

    //查询课程根节点 查询不到自动添加根节点
    private String getTeachPlanRoot(String courseId){
        Optional<CourseBase> optional = courseBaseRepository.findById(courseId);
        if(!optional.isPresent()){
            return null;
        }

        List<Teachplan> list = teachPlanRepository.findByCourseidAndParentid(courseId, "0");
        if(list==null||list.size()<=0){
            //查询不到添加根节点
            Teachplan teachplan = new Teachplan();
            teachplan.setCourseid(courseId);
            teachplan.setParentid("0");
            teachplan.setStatus("0");
            teachplan.setGrade("1");
            teachplan.setPname(optional.get().getName());
            teachPlanRepository.save(teachplan);
            return teachplan.getId();
        }
        //返回根节点id
        return list.get(0).getId();
    }

}
