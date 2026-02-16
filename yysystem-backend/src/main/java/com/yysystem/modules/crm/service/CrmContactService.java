package com.yysystem.modules.crm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yysystem.modules.crm.entity.CrmContact;
import java.util.List;

public interface CrmContactService extends IService<CrmContact> {
    List<CrmContact> listByCustomerId(Long customerId);
    void setPrimary(Long id);
}
