package com.yysystem.modules.expense.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yysystem.modules.expense.entity.CrmSalesCost;

public interface CrmSalesCostService extends IService<CrmSalesCost> {
    Long saveCost(CrmSalesCost cost);
    boolean updateCost(CrmSalesCost cost);
    CrmSalesCost getCostById(Long id);
}
