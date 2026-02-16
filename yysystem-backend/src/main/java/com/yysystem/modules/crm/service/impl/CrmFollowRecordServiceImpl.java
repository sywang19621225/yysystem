package com.yysystem.modules.crm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yysystem.modules.crm.entity.CrmFollowRecord;
import com.yysystem.modules.crm.mapper.CrmFollowRecordMapper;
import com.yysystem.modules.crm.service.CrmFollowRecordService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CrmFollowRecordServiceImpl extends ServiceImpl<CrmFollowRecordMapper, CrmFollowRecord> implements CrmFollowRecordService {

    @Override
    public boolean save(CrmFollowRecord entity) {
        try {
            org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
                entity.setCreateBy(authentication.getName());
            }
        } catch (Exception ignored) {}
        
        return super.save(entity);
    }
}
