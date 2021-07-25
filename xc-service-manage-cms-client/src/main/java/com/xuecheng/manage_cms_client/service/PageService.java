package com.xuecheng.manage_cms_client.service;


import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.manage_cms_client.dao.CmsPageRepository;
import com.xuecheng.manage_cms_client.dao.CmsSiteRepository;
import org.apache.commons.io.IOUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Optional;

@Service
public class PageService {


    public static final Logger LOGGER = (Logger) LoggerFactory.getLogger(PageService.class);

    @Autowired
    CmsPageRepository cmsPageRepository;

    @Autowired
    CmsSiteRepository cmsSiteRepository;

    @Autowired
    GridFsTemplate gridFsTemplate;

    @Autowired
    GridFSBucket gridFSBucket;


    //接收消息后下载文件 保存html页面到服务器的物理路径
    public void savePageToServerPath(String pageId){
        //根据pageid查询cmspage
        CmsPage cmsPage = this.findCmsPageById(pageId);

        //得到html文件id 从cmspage中获取htmlfeiledid
        String htmlFileId = cmsPage.getHtmlFileId();

        //从gridfs查询文件
        InputStream inputStream = this.getFileById(htmlFileId);
        if(inputStream==null){
            LOGGER.error("getFileById inputStream is null htmlFileId:{}",htmlFileId);
            return;
        }
        //得到站点的信息
        String siteId = cmsPage.getSiteId();
        CmsSite cmsSite = findCmsSiteById(siteId);

        //得到站点的物理路径
        String sitePhysicalPath=cmsSite.getSitePhysicalPath();
        //得到页面的物理路径
        String pagePath=sitePhysicalPath+cmsPage.getPagePhysicalPath()+cmsPage.getPageName();
        //将html文件保存到服务器的物理路径
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(new File(pageId));
            try {
                IOUtils.copy(inputStream,fileOutputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }finally {
            try {
                fileOutputStream.close();
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    /**
     * Created with IntelliJ IDEA.
     *
     * @Description: 根据文件id查询文件的内容
     * @param:
     * @return:
     * @Auther: liuyadi
     * @Date: 2021/1/2
     */
    public InputStream getFileById(String fileId){

        //文件对象
        GridFSFile gridFSFile =
                gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(fileId)));

        //打开下载流
        GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
        //定义gridfsResource  来操作流
        GridFsResource gridFsResource = new GridFsResource(gridFSFile,gridFSDownloadStream);
        try {
            return gridFsResource.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Created with IntelliJ IDEA.
     *
     * @Description: 根据页面id查询页面信息
     * @param:
     * @return:
     * @Auther: liuyadi
     * @Date: 2021/1/2
     */
    public CmsPage findCmsPageById(String pageId){
        Optional<CmsPage> optional = cmsPageRepository.findById(pageId);
        if(optional.isPresent()){
            return optional.get();
        }
        return null;
    }

    /**
     * Created with IntelliJ IDEA.
     *
     * @Description: 根据站点id查询站点信息
     * @param:
     * @return:
     * @Auther: liuyadi
     * @Date: 2021/1/2
     */
    public CmsSite findCmsSiteById(String siteId){
        Optional<CmsSite> optional = cmsSiteRepository.findById(siteId);
        if(optional.isPresent()){
            return optional.get();
        }
        return null;
    }




}
