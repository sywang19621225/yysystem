package com.yysystem.modules.contract.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yysystem.modules.contract.entity.CrmContract;

public interface CrmContractService extends IService<CrmContract> {
    
    Long saveContract(CrmContract crmContract);
    
    CrmContract getContractById(Long id);
    
    boolean updateContract(CrmContract crmContract);

    /**
     * 从报价单生成合同
     * @param quoteId 报价单ID
     * @return 合同ID
     */
    Long createFromQuote(Long quoteId);
}
