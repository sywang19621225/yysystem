package com.yysystem.modules.finance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yysystem.modules.finance.entity.CrmFinance;

public interface CrmFinanceService extends IService<CrmFinance> {
    
    Long saveFinance(CrmFinance crmFinance);
    
    boolean updateFinance(CrmFinance crmFinance);
    
    boolean auditFinance(Long id, String status, String detail);
}
