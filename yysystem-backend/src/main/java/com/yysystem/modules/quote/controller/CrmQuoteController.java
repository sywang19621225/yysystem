package com.yysystem.modules.quote.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yysystem.common.result.Result;
import com.yysystem.modules.crm.entity.CrmCustomer;
import com.yysystem.modules.crm.service.CrmCustomerService;
import com.yysystem.modules.quote.entity.CrmQuote;
import com.yysystem.modules.quote.entity.CrmQuoteDetail;
import com.yysystem.modules.quote.service.CrmQuoteService;
import com.yysystem.modules.system.entity.SysUser;
import com.yysystem.modules.system.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/crm/quote")
public class CrmQuoteController {

    @Autowired
    private CrmQuoteService crmQuoteService;

    @Autowired
    private CrmCustomerService crmCustomerService;
    
    @Autowired
    private SysUserService sysUserService;

    @PostMapping
    @PreAuthorize("hasAuthority('QM:create')")
    public Result<Boolean> save(@RequestBody CrmQuote crmQuote) {
        return Result.success(crmQuoteService.saveQuote(crmQuote));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('QM:update')")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody CrmQuote crmQuote) {
        crmQuote.setId(id);
        return Result.success(crmQuoteService.updateQuote(crmQuote));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('QM:delete')")
    public Result<Boolean> remove(@PathVariable Long id) {
        return Result.success(crmQuoteService.removeById(id));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('QM:get')")
    public Result<CrmQuote> getById(@PathVariable Long id) {
        CrmQuote quote = crmQuoteService.getQuoteById(id);
        if (quote != null && quote.getCustomerId() != null) {
            CrmCustomer customer = crmCustomerService.getById(quote.getCustomerId());
            if (customer != null) {
                quote.setCustomerName(customer.getCustomerName());
            }
        }
        if (quote != null && quote.getSalesmanId() != null) {
            SysUser user = sysUserService.getById(quote.getSalesmanId());
            if (user != null) {
                quote.setSalesmanName(user.getName());
            }
        }
        return Result.success(quote);
    }

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('QM:list')")
    public Result<Page<CrmQuote>> list(@RequestParam(defaultValue = "1") Integer current,
                                       @RequestParam(defaultValue = "10") Integer size,
                                       @RequestParam(required = false) Long customerId) {
        Page<CrmQuote> page = crmQuoteService.page(new Page<>(current, size),
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CrmQuote>()
                        .eq(customerId != null, CrmQuote::getCustomerId, customerId)
                        .orderByDesc(CrmQuote::getCreateTime));
        List<CrmQuote> records = page.getRecords();
        for (CrmQuote record : records) {
            if (record.getCustomerId() != null) {
                CrmCustomer customer = crmCustomerService.getById(record.getCustomerId());
                if (customer != null) {
                    record.setCustomerName(customer.getCustomerName());
                }
            }
            if (record.getSalesmanId() != null) {
                SysUser user = sysUserService.getById(record.getSalesmanId());
                if (user != null) {
                    record.setSalesmanName(user.getName());
                }
            }
        }
        return Result.success(page);
    }

    @GetMapping("/{id}/details")
    @PreAuthorize("hasAuthority('QM:get')")
    public Result<Page<CrmQuoteDetail>> details(@PathVariable Long id,
                                                @RequestParam(defaultValue = "1") Integer current,
                                                @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(crmQuoteService.listQuoteDetails(id, current, size));
    }
}
