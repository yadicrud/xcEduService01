package com.xuecheng.manage_cms.dao;

import com.xuecheng.framework.domain.cms.CmsPage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;


public interface CmsPageRepository extends MongoRepository<CmsPage,String> {
     CmsPage findByPageNameAndSiteIdAndPageWebPath(String pageName,String SiteId,String WebPath);
}
