package com.yysystem.modules.invoice.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yysystem.modules.invoice.entity.CrmInvoice;
import com.yysystem.modules.invoice.entity.CrmInvoiceDetail;

public interface CrmInvoiceService extends IService<CrmInvoice> {
    Long saveInvoice(CrmInvoice crmInvoice);
    boolean updateInvoice(CrmInvoice crmInvoice);
    CrmInvoice getInvoiceById(Long id);
    Page<CrmInvoiceDetail> listInvoiceDetails(Long invoiceId, Integer current, Integer size);
}
