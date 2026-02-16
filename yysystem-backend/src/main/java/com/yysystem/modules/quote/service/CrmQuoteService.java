package com.yysystem.modules.quote.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yysystem.modules.quote.entity.CrmQuote;
import com.yysystem.modules.quote.entity.CrmQuoteDetail;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface CrmQuoteService extends IService<CrmQuote> {
    
    boolean saveQuote(CrmQuote crmQuote);
    
    CrmQuote getQuoteById(Long id);
    
    boolean updateQuote(CrmQuote crmQuote);

    Page<CrmQuoteDetail> listQuoteDetails(Long quoteId, Integer current, Integer size);
}
