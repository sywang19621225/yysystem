package com.yysystem.modules.purchase.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yysystem.modules.purchase.entity.ScmPurchaseContract;

public interface ScmPurchaseContractService extends IService<ScmPurchaseContract> {
    Long saveContract(ScmPurchaseContract contract);
    ScmPurchaseContract getContractById(Long id);
    boolean updateContract(ScmPurchaseContract contract);
    Long createFromRequest(Long requestId);
}

