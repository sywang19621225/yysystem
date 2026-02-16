package com.yysystem.modules.crm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yysystem.modules.crm.entity.CrmCustomerTechInfo;
import com.yysystem.modules.crm.mapper.CrmCustomerTechInfoMapper;
import com.yysystem.modules.crm.service.CrmCustomerTechInfoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CrmCustomerTechInfoServiceImpl extends ServiceImpl<CrmCustomerTechInfoMapper, CrmCustomerTechInfo> implements CrmCustomerTechInfoService {
    @Override
    public List<CrmCustomerTechInfo> listByCustomerId(Long customerId) {
        return this.list(new LambdaQueryWrapper<CrmCustomerTechInfo>().eq(CrmCustomerTechInfo::getCustomerId, customerId));
    }

    @Override
    @Transactional
    public void upsertList(Long customerId, List<CrmCustomerTechInfo> list) {
        if (list == null) return;
        for (CrmCustomerTechInfo item : list) {
            item.setCustomerId(customerId);
            if (item.getId() != null && this.getById(item.getId()) != null) {
                this.updateById(item);
            } else {
                this.save(item);
            }
        }
    }
}
