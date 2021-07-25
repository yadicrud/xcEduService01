package com.xuecheng.framework.domain.cms.request;

import lombok.Data;

@Data
public class QueryPageRequest {
     //站点d
     private String siteId;
     //页面name
     private String pageName;
     //页面id
     private String pageId;
     //模板id
     private String templateId;
     //别名
     private String pageAliase;

}
