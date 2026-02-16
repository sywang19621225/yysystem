package com.yysystem.modules.crm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yysystem.modules.crm.entity.CrmCustomer;

public interface CrmCustomerService extends IService<CrmCustomer> {
    void receive(Long id, Long userId);
    void assign(Long id, Long userId);
    void returnToPool(Long id);
}
