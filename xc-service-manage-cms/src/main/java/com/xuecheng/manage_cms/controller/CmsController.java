package com.xuecheng.manage_cms.controller;

import com.xuecheng.api.cms.CmsPageControllerApi;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.manage_cms.service.impl.PageServiceImpl;
import lombok.Synchronized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.*;


@Controller
@ResponseBody
@RequestMapping("/cms/page")
public class CmsController implements CmsPageControllerApi {
    @Autowired
    PageServiceImpl pageServiceImpl;

    @Override
    @GetMapping("/list/{page}/{size}")
    public QueryResponseResult findList(@PathVariable("page") int page, @PathVariable("size") int size, QueryPageRequest queryPageRequest) {
        return pageServiceImpl.findList(page,size,queryPageRequest);
    }

    @Override
    @GetMapping("/siteList")
    public List<CmsSite> findSiteList(){
        return pageServiceImpl.findSiteList();
    }

    @Override
    @GetMapping("/templateList")
    public List<CmsTemplate> findTemplateList(){
        return pageServiceImpl.findTemplateList();
    }

    @Override
    @PostMapping("/add")
    public CmsPageResult add(@RequestBody CmsPage cmsPage){
        return pageServiceImpl.add(cmsPage);
    };
    @Override
    @GetMapping("/edit/{pageId}")
    public CmsPage pageEdit(@PathVariable("pageId") String pageId){
        return pageServiceImpl.pageEdit(pageId);
    };

    @Override
    @PostMapping("/update/{pageId}")
    public CmsPageResult update(@PathVariable("pageId") String pageId, @RequestBody CmsPage cmsPage){
        return pageServiceImpl.pageUpdate(pageId,cmsPage);
    }

    @Override
    @PostMapping("/delete/{pageId}")
    public ResponseResult pageDelete(@PathVariable String pageId){

        return pageServiceImpl.pageDelete(pageId);
    }

    @Override
    @PostMapping("/postPage/{pageId}")
    public ResponseResult post(@PathVariable("pageId") String pageId) {

        return pageServiceImpl.postPage(pageId);
    }

    @GetMapping("test/thread")
    public void testThread() {

        synchronized  (this){
            for (int i = 1;i<=5;i++) {
                ExecutorService threadPool=new ThreadPoolExecutor(1,1,
                        1L, TimeUnit.SECONDS,
                        new LinkedBlockingQueue<>(1024),
                        Executors.defaultThreadFactory(),
                        new ThreadPoolExecutor.AbortPolicy());
                int finalI = i;
                threadPool.execute(() -> this.outPrint(finalI));
            }

        }


    }

    private void outPrint(int i) {
        try {
            if(i == 1){
                Thread.sleep(10000);
            }
            System.out.println(i+"-----------------------------------------------------------------------------------------------");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
