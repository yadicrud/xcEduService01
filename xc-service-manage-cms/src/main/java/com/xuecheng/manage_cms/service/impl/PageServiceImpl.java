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

        //??????Id???????????????
        if(!StringUtils.isEmpty(queryPageRequest.getSiteId())){
            cmsPage.setSiteId(queryPageRequest.getSiteId());
        }
        //??????Id???????????????
        if(!StringUtils.isEmpty(queryPageRequest.getTemplateId())){
            cmsPage.setTemplateId(queryPageRequest.getTemplateId());
        }
        //???????????????????????????
        if(!StringUtils.isEmpty(queryPageRequest.getPageAliase())){
            cmsPage.setPageAliase(queryPageRequest.getPageAliase());
        }
        //????????????
        Pageable pageable = PageRequest.of(page-1, size);
        //???????????????
        ExampleMatcher exampleMatcher = ExampleMatcher.matching();
        exampleMatcher = exampleMatcher.withMatcher("pageAliase",ExampleMatcher.GenericPropertyMatchers.contains());
        Example example = Example.of(cmsPage,exampleMatcher);

        Page<CmsPage> all = cmsPageRepository.findAll(example,pageable);

        //????????????
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
            cmsPage.setPageId(null);//?????????????????????spring data ????????????
            cmsPageRepository.save(cmsPage);
            //????????????
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
    //????????????
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

    //??????cms??????config
    @Override
    public CmsConfig getCmsConfigById(String id){
        Optional<CmsConfig> optional = cmsConfigRepository.findById(id);
        if(optional.isPresent()){
            CmsConfig cmsConfig = optional.get();
            return cmsConfig;
        }
        return null;
    }

    //???????????????
    /*  1??????????????????????????????????????????DataUrl???
        2??????????????????????????????DataUrl?????????????????????
        3????????????????????????
        4???????????????????????????
    */
    @Override
    public String getPageHtml(String pageID){
        try {
            //??????????????????
            Map model= this.getModelByPageId(pageID);
            if(model==null){
                new Exception(String.valueOf(CmsCode.CMS_GENERATEHTML_DATAISNULL));
            }
            //??????????????????
            String template = this.getTemplateById(pageID);
            if(template.isEmpty()){
                new Exception(String.valueOf(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL));
            }
            //???????????????
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

    //??????????????????
    private String getTemplateById(String pageId){
        CmsPage cmsPage =this.pageEdit(pageId);
        if(cmsPage==null){
            new Exception("??????????????????????????????");
        }
        String templateId=cmsPage.getTemplateId();
        if(templateId.isEmpty()){
            new Exception(String.valueOf(CmsCode.CMS_GENERATEHTML_TEMPLATEISNULL));
        }
        Optional<CmsTemplate> optional = cmsTemplateRepository.findById(templateId);
        if(optional.isPresent()){
            CmsTemplate cmsTemplate = optional.get();
            //????????????id
            String templateFileId = cmsTemplate.getTemplateFileId();
            //????????????????????????
            GridFSFile gridFSFile =
                    gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(templateFileId)));
            //?????????????????????
            GridFSDownloadStream gridFSDownloadStream =
                    gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
            //??????GridFsResource
            GridFsResource gridFsResource = new GridFsResource(gridFSFile,gridFSDownloadStream);
            try {
                String content = IOUtils.toString(gridFsResource.getInputStream(), "utf???8");
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
     * @Description: ??????????????????
     * @param: 
     * @return: 
     * @Auther: liuyadi
     * @Date: 2020/12/4
     */
    private Map getModelByPageId(String pageID){
        CmsPage cmsPage=this.pageEdit(pageID);
        if(cmsPage==null){
            new Exception("????????????????????????????????????");
        }
        String dataUrl = cmsPage.getDataUrl();
        if(dataUrl.isEmpty()){
            new Exception("DataUrl??????");
        }
        //??????resttemplate??????dataurl????????????
        ResponseEntity<Map> forEntity = restTemplate.getForEntity(dataUrl, Map.class);
        Map body = forEntity.getBody();
        return body;
    }

    //?????????
    public String generateHtml(String templateContent,Map model) throws IOException {
        try {
            //???????????????
            Configuration configuration = new Configuration();
            //???????????????
            StringTemplateLoader stringTemplateLoader = new StringTemplateLoader();
            stringTemplateLoader.putTemplate("template",templateContent);
            //?????????????????????
            configuration.setTemplateLoader(stringTemplateLoader);
            //????????????
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
     * @Description: ???????????????
     * @param:
     * @return:
     * @Auther: liuyadi
     * @Date: 2021/1/4
     */
    @Override
    public ResponseResult postPage(String pageID) {
        //?????????????????????
        String pageHtml = getPageHtml(pageID);

        //?????????????????????????????????Gridfs???
        CmsPage cmsPage = saveHtml(pageID,pageHtml);

        //???mq?????????
        sendPostPage(pageID);
        return new ResponseResult(CommonCode.SUCCESS);
    }
    /**
     * Created with IntelliJ IDEA.
     * 
     * @Description: ??????html???gridfs
     * @param: 
     * @return: 
     * @Auther: liuyadi
     * @Date: 2021/1/4
     */
    private CmsPage saveHtml(String pageId,String htmlContent){
        //??????????????????
        CmsPage cmsPage = pageEdit(pageId);
        if(cmsPage==null){
            new Exception("cmsPagew is null");
        }
        Object htmlFileId = null;
        try {
            //???htmlContent???????????????
            InputStream inputStream = IOUtils.toInputStream(htmlContent, "utf-8");
            //???html?????????????????????GridFs
            htmlFileId = gridFsTemplate.store(inputStream, cmsPage.getPageName());
        } catch (IOException e) {
            e.printStackTrace();
        }

        //???html id??????????????????Page???
        cmsPage.setHtmlFileId(htmlFileId.toString());
        cmsPageRepository.save(cmsPage);
        return cmsPage;
    }
    /**
     * Created with IntelliJ IDEA.
     *
     * @Description: ???mq?????????
     * @param:
     * @return:
     * @Auther: liuyadi
     * @Date: 2021/1/4
     */
    private void sendPostPage(String pageId){
        //??????????????????
        Map<String,String> msg = new HashMap<>();
        msg.put("pageId",pageId);
        //??????json
        String msgStr = JSON.toJSONString(msg);

        //????????????

        //??????????????????
        CmsPage cmsPage = pageEdit(pageId);
        if(cmsPage==null){
            new Exception("cmsPagew is null");
        };

        //????????????id??????routingkey
        String siteId = cmsPage.getSiteId();

        //????????????
        rabbitTemplate.convertAndSend(RabbitmqConfig.EX_ROUTING_CMS_POSTPAGE,siteId,msgStr);
    }



}
