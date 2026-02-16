package com.yysystem.modules.crm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yysystem.modules.crm.entity.CrmCustomerTechInfo;
import java.util.List;

public interface CrmCustomerTechInfoService extends IService<CrmCustomerTechInfo> {
    List<CrmCustomerTechInfo> listByCustomerId(Long customerId);
    void upsertList(Long customerId, List<CrmCustomerTechInfo> list);
}
