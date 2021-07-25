package com.xuecheng.api.cms;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.CmsSite;
import com.xuecheng.framework.domain.cms.CmsTemplate;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsName;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;

import java.util.List;

public interface CmsPageControllerApi {
    //页面查询
    public QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest);
    public List<CmsSite> findSiteList();
    public List<CmsTemplate> findTemplateList();
    public CmsPageResult add(CmsPage cmsPage);
    public CmsPage pageEdit(String pageId);
    public CmsPageResult update(String pageId,CmsPage cmsPage);
    public ResponseResult pageDelete(String pageId);
    //页面发布
    public ResponseResult post(String pageId);
}
