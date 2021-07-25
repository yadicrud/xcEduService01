package com.xuecheng.manage_cms.service.impl;

import com.alibaba.fastjson.JSON;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsConfig;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsCode;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.model.response.*;
import com.xuecheng.manage_cms.config.RabbitmqConfig;
import com.xuecheng.manage_cms.dao.CmsConfigRepository;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import com.xuecheng.manage_cms.dao.CmsSiteRepository;
import com.xuecheng.manage_cms.dao.cmsTemplateRepository;
import com.xuecheng.manage_cms.service.PageService;
import freemarker.cache.StringTemplateLoader;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.hibernate.secure.internal.JaccSecurityListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;


import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
public class PageServiceImpl implements PageService {
    @Autowired
    CmsPageRepository cmsPageRepository;
    @Autowired
    CmsSiteRepository cmsSiteRepository;
    @Autowired
    cmsTemplateRepository cmsTemplateRepository;

    @Autowired
    CmsConfigRepository cmsConfigRepository;

    @Autowired
    RestTemplate restTemplate;
    @Autowired
    GridFsTemplate gridFsTemplate;
    @Autowired
    GridFSBucket gridFSBucket;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Override
    public QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest){
        CmsPage cmsPage = new CmsPage();
        if(queryPageRequest == null){
             queryPageRequest = new QueryPageRequest();
        }

        //站点Id：精确匹配
        if(!StringUtils.isEmpty(queryPageRequest.getSiteId())){
            cmsPage.setSiteId(queryPageRequest.getSiteId());
        }
        //模板Id：精确匹配
        if(!StringUtils.isEmpty(queryPageRequest.getTemplateId())){
            cmsPage.setTemplateId(queryPageRequest.getTemplateId());
        }
        //页面别名：模糊匹配
        if(!StringUtils.isEmpty(queryPageRequest.getPageAliase())){
            cmsPage.setPageAliase(queryPageRequest.getPageAliase());
        }
        //分页工具
        Pageable pageable = PageRequest.of(page-1, size);
        //条件匹配器
        ExampleMatcher exampleMatcher = ExampleMatcher.matching();
        exampleMatcher = exampleMatcher.withMatcher("pageAliase",ExampleMatcher.GenericPropertyMatchers.contains());
        Example example = Example.of(cmsPage,exampleMatcher);

        Page<CmsPage> all = cmsPageRepository.findAll(example,pageable);

        //结果封装
        QueryResult queryResult =new QueryResult();
        queryResult.setList(all.getContent());
        queryResult.setTotal(all.getTotalElements());

        //ResultCode resultCode =new ResultCode()

        QueryResponseResult queryResponseResult = new QueryResponseResult(CommonCode.SUCCESS,queryResult);
        return queryResponseResult;
    }

    @Override
    public List<CmsSite> findSiteList(){
        return cmsSiteRepository.findAll();
    }

    @Override
    public List<CmsTemplate> findTemplateList(){
        return cmsTemplateRepository.findAll();
    }

    @Override
    public CmsPageResult add(CmsPage cmsPage){
        CmsPage cmsPageFirst =new CmsPage();
        cmsPageFirst = cmsPageRepository.findByPageNameAndSiteIdAndPageWebPath(cmsPage.getPageName(),cmsPage.getSiteId(),cmsPage.getPageWebPath());
        if(cmsPageFirst==null){
            cmsPage.setPageId(null);//添加页面主键由spring data 自动生成
            cmsPageRepository.save(cmsPage);
            //返回结果
            CmsPageResult cmsPageResult = new CmsPageResult(CommonCode.SUCCESS,cmsPage);
            return cmsPageResult;
        }
            return new CmsPageResult(CmsCode.CMS_ADDPAGE_EXISTSNAME,cmsPage);
    }

    @Override
    public CmsPage pageEdit(String id){
        Optional<CmsPage> optional = cmsPageRepository.findById(id);
        if(optional.isPresent()){
            return optional.get();
        }
        return null;
    }

    @Override
    public CmsPageResult pageUpdate(String pageId,CmsPage cmsPage){
        CmsPage cmsPage1 = new CmsPage();
        cmsPage1=this.pageEdit(pageId);
        if(cmsPage1!=null){
            cmsPage1.setSiteId(cmsPage.getSiteId());
            cmsPage1.setPageAliase(cmsPage.getPageAliase());
            cmsPage1.setPageName(cmsPage.getPageName());
            cmsPage1.setTemplateId(cmsPage.getTemplateId());
            cmsPage1.setPageWebPath(cmsPage.getPageWebPath());
            cmsPage1.setPagePhysicalPath(cmsPage.getPagePhysicalPath());
            cmsPage1.setPageCreateTime(cmsPage.getPageCreateTime());
            cmsPage1.setDataUrl(cmsPage.getDataUrl());
        }
        CmsPage cmsPage2 = cmsPageRepository.save(cmsPage1);
        if(cmsPage2!=null){
            CmsPageResult cmsPageResult = new CmsPageResult(CommonCode.SUCCESS,cmsPage);
            return cmsPageResult;
        }
        return new CmsPageResult(CommonCode.FAIL,null);
    }
    //删除页面
    @Override
    public ResponseResult pageDelete(String pageId){
        CmsPage cmsPage =new CmsPage();
        cmsPage = this.pageEdit(pageId);
        if(cmsPage!=null){
            cmsPageRepository.deleteById(pageId);
            return new ResponseResult(CommonCode.SUCCESS);
        }
        return new ResponseResult(CommonCode.FAIL);
    }

    //根据cms查询config
    @Override
    public CmsConfig getCmsConfigById(String id){
        Optional<CmsConfig> optional = cmsConfigRepository.findById(id);
        if(optional.isPresent()){
            CmsConfig cmsConfig = optional.get();
            return cmsConfig;
        }
        return null;
    }

    //页面静态化
    /*  1、静态化程序首先读取页面获取DataUrl。
        2、静态化程序远程请求DataUrl得到数据模型。
        3、获取页面模板。
        4、执行页面静态化。
    */
    @Override
    public String getPageHtml(String pageID){
        try {
            //获取数据模型
            Map model= this.getModelByPageId(pageID);
            if(model==null){
                new Exception(String.valueOf(CmsCode.CMS_GENERATEHTML_DATAISNULL));
            }
            //获取数据模板
            String template = this.getTemplateById(pageID);
            if(template.isEmpty()){
                new Exception(String.valueOf(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL));
            }
            //执行静态化
            String  html = this.generateHtml(template, model);
            if(StringUtils.isEmpty(html)){
                new Exception(String.valueOf(CmsCode.CMS_GENERATEHTML_HTMLISNULL));
            }
            return html;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    //获取页面模板
    private String getTemplateById(String pageId){
        CmsPage cmsPage =this.pageEdit(pageId);
        if(cmsPage==null){
            new Exception("获取页面模板页面为空");
        }
        String templateId=cmsPage.getTemplateId();
        if(templateId.isEmpty()){
            new Exception(String.valueOf(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL));
        }
        Optional<CmsTemplate> optional = cmsTemplateRepository.findById(templateId);
        if(optional.isPresent()){
            CmsTemplate cmsTemplate = optional.get();
            //模板文件id
            String templateFileId = cmsTemplate.getTemplateFileId();
            //取出模板文件内容
            GridFSFile gridFSFile =
                    gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(templateFileId)));
            //打开下载流对象
            GridFSDownloadStream gridFSDownloadStream =
                    gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
            //创建GridFsResource
            GridFsResource gridFsResource = new GridFsResource(gridFSFile,gridFSDownloadStream);
            try {
                String content = IOUtils.toString(gridFsResource.getInputStream(), "utf‐8");
                return content;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Created with IntelliJ IDEA.
     * 
     * @Description: 获取数据模型
     * @param: 
     * @return: 
     * @Auther: liuyadi
     * @Date: 2020/12/4
     */
    private Map getModelByPageId(String pageID){
        CmsPage cmsPage=this.pageEdit(pageID);
        if(cmsPage==null){
            new Exception("获取数据模型时页面找不到");
        }
        String dataUrl = cmsPage.getDataUrl();
        if(dataUrl.isEmpty()){
            new Exception("DataUrl为空");
        }
        //通过resttemplate请求dataurl获取数据
        ResponseEntity<Map> forEntity = restTemplate.getForEntity(dataUrl, Map.class);
        Map body = forEntity.getBody();
        return body;
    }

    //静态化
    public String generateHtml(String templateContent,Map model) throws IOException {
        try {
            //生成配置类
            Configuration configuration = new Configuration();
            //模板加载器
            StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
            stringTemplateLoader.putTemplate("template",templateContent);
            //配置模板加载器
            configuration.setTemplateLoader(stringTemplateLoader);
            //获取模板
            Template template = configuration.getTemplate("template");
            String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
            return html;
        } catch (TemplateException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * Created with IntelliJ IDEA.
     *
     * @Description: 页面的发布
     * @param:
     * @return:
     * @Auther: liuyadi
     * @Date: 2021/1/4
     */
    @Override
    public ResponseResult postPage(String pageID) {
        //执行页面静态化
        String pageHtml = getPageHtml(pageID);

        //将页面静态化文件存储到Gridfs中
        CmsPage cmsPage = saveHtml(pageID,pageHtml);

        //向mq发消息
        sendPostPage(pageID);
        return new ResponseResult(CommonCode.SUCCESS);
    }
    /**
     * Created with IntelliJ IDEA.
     * 
     * @Description: 保存html到gridfs
     * @param: 
     * @return: 
     * @Auther: liuyadi
     * @Date: 2021/1/4
     */
    private CmsPage saveHtml(String pageId,String htmlContent){
        //取出页面信息
        CmsPage cmsPage = pageEdit(pageId);
        if(cmsPage==null){
            new Exception("cmsPagew is null");
        }
        Object htmlFileId = null;
        try {
            //将htmlContent转成输入流
            InputStream inputStream = IOUtils.toInputStream(htmlContent, "utf-8");
            //将html文件内容保存到GridFs
            htmlFileId = gridFsTemplate.store(inputStream, cmsPage.getPageName());
        } catch (IOException e) {
            e.printStackTrace();
        }

        //将html id更新到层面上Page中
        cmsPage.setHtmlFileId(htmlFileId.toString());
        cmsPageRepository.save(cmsPage);
        return cmsPage;
    }
    /**
     * Created with IntelliJ IDEA.
     *
     * @Description: 向mq发消息
     * @param:
     * @return:
     * @Auther: liuyadi
     * @Date: 2021/1/4
     */
    private void sendPostPage(String pageId){
        //创建消息对象
        Map<String,String> msg = new HashMap<>();
        msg.put("pageId",pageId);
        //转成json
        String msgStr = JSON.toJSONString(msg);

        //消息内容

        //得到页面信息
        CmsPage cmsPage = pageEdit(pageId);
        if(cmsPage==null){
            new Exception("cmsPagew is null");
        };

        //获取站点id作为routingkey
        String siteId = cmsPage.getSiteId();

        //发布消息
        rabbitTemplate.convertAndSend(RabbitmqConfig.EX_ROUTING_CMS_POSTPAGE,siteId,msgStr);
    }



}
