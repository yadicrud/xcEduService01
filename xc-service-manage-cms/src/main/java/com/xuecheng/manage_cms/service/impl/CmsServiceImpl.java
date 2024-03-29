package com.xuecheng.manage_cms.service.impl;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.manage_cms.dao.CmsPageRepository;
import com.xuecheng.manage_cms.service.CmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CmsServiceImpl implements CmsService {
    @Autowired
    CmsPageRepository cmsPageRepository;

    @Override
    @Transactional
    public Page<CmsPage> findAll(Pageable pageable) {
        cmsPageRepository.findAll();
        return cmsPageRepository.findAll(pageable);
    }

}
