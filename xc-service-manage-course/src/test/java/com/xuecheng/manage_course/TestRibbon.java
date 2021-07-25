package com.xuecheng.manage_course;


import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.course.CourseBase;
import com.xuecheng.framework.domain.course.ext.TeachplanNode;
import com.xuecheng.manage_course.dao.CourseBaseRepository;
import com.xuecheng.manage_course.dao.CourseMapper;
import com.xuecheng.manage_course.dao.TeachPlanMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;


import java.util.*;

/**
 * @author Administrator
 * @version 1.0
 **/
@SpringBootTest
@RunWith(SpringRunner.class)
public class TestRibbon {
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    private SpringClientFactory factory;

    @Test
    public void testRibbon() {

        //确定要获取的服务名称
        String serviceId = "XC-SERVICE-MANAGE-CMS";

        //ribbon客户端从eurekeserver中获取服务列表 根据服务名获取服务列表
        //ResponseEntity<Map> forEntity = restTemplate.getForEntity("http://"+serviceId+"/cms/page/templateList", Map.class);


        for(int i=0;i<=10;i++){
            ResponseEntity<CmsPage> forEntity = restTemplate.getForEntity("http://" + serviceId
                    + "/cms/page/edit/5a754adf6abb500ad05688d9", CmsPage.class);
            CmsPage cmsPage = forEntity.getBody();
            //Map body = forEntity.getBody();
            System.out.println(cmsPage);
        }
//        ILoadBalancer lb =  factory.getLoadBalancer("XC-SERVICE-MANAGE-CMS");
//        List<Server> allServers = lb.getAllServers();
        HashSet<String> hashSet = new HashSet();
        LinkedList mylist=new LinkedList();
        Hashtable balance = new Hashtable();
        ArrayList<String> arrayList = new ArrayList();
    }


}
