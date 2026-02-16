package com.yysystem.modules.invoice.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yysystem.common.result.Result;
import com.yysystem.modules.crm.entity.CrmCustomer;
import com.yysystem.modules.crm.service.CrmCustomerService;
import com.yysystem.modules.contract.entity.CrmContract;
import com.yysystem.modules.contract.service.CrmContractService;
import com.yysystem.modules.invoice.entity.CrmInvoice;
import com.yysystem.modules.invoice.entity.CrmInvoiceDetail;
import com.yysystem.modules.invoice.service.CrmInvoiceService;
import com.yysystem.modules.system.entity.SysUser;
import com.yysystem.modules.system.service.SysUserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/crm/invoice")
public class CrmInvoiceController {

    @Autowired
    private CrmInvoiceService crmInvoiceService;
    @Autowired
    private CrmCustomerService crmCustomerService;
    @Autowired
    private CrmContractService crmContractService;
    @Autowired
    private SysUserService sysUserService;

    @PostMapping
    @PreAuthorize("hasAuthority('IM:create')")
    public Result<Long> save(@RequestBody CrmInvoice invoice) {
        return Result.success(crmInvoiceService.saveInvoice(invoice));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('IM:update')")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody CrmInvoice invoice) {
        invoice.setId(id);
        return Result.success(crmInvoiceService.updateInvoice(invoice));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('IM:delete')")
    public Result<Boolean> remove(@PathVariable Long id) {
        CrmInvoice inv = crmInvoiceService.getById(id);
        if (inv != null && ("DONE".equals(inv.getInvoiceStatus()) || "冲抵货款".equals(inv.getInvoiceStatus()))) {
            return Result.error("已开票的发票不可删除");
        }
        return Result.success(crmInvoiceService.removeById(id));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('IM:get')")
    public Result<CrmInvoice> getById(@PathVariable Long id) {
        CrmInvoice inv = crmInvoiceService.getInvoiceById(id);
        if (inv != null) {
            String username = null;
            org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
                username = authentication.getName();
            }
            if (username != null && !"admin".equalsIgnoreCase(username)) {
                SysUser user = sysUserService.getByUsername(username);
                if (user != null) {
                    String ut = user.getUserType();
                    boolean isFinance = ut != null && "finance".equalsIgnoreCase(ut);
                    Long uid = user.getId();
                    boolean owned = (uid != null) && (uid.equals(inv.getApplicantId()) || uid.equals(inv.getInvoicerId()));
                    if (!isFinance && !owned) {
                        return Result.error("无权查看他人发票");
                    }
                }
            }
            if (inv.getCustomerId() != null) {
                CrmCustomer c = crmCustomerService.getById(inv.getCustomerId());
                inv.setCustomerName(c != null ? c.getCustomerName() : null);
            }
            if (inv.getContractId() != null) {
                CrmContract ct = crmContractService.getById(inv.getContractId());
                inv.setContractNo(ct != null ? ct.getContractNo() : null);
            }
            if (inv.getInvoicerId() != null) {
                SysUser u = sysUserService.getById(inv.getInvoicerId());
                inv.setInvoicerName(u != null ? u.getName() : null);
            }
            if (inv.getApplicantId() != null) {
                SysUser u = sysUserService.getById(inv.getApplicantId());
                inv.setApplicantName(u != null ? u.getName() : null);
            }
        }
        return Result.success(inv);
    }

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('IM:list')")
    public Result<Page<CrmInvoice>> list(@RequestParam(defaultValue = "1") Integer current,
                                         @RequestParam(defaultValue = "10") Integer size,
                                         @RequestParam(required = false) Long contractId,
                                         @RequestParam(required = false) String customerName,
                                         @RequestParam(required = false) String invoiceNo) {
        LambdaQueryWrapper<CrmInvoice> wrapper = new LambdaQueryWrapper<>();
        if (contractId != null) {
            wrapper.eq(CrmInvoice::getContractId, contractId);
        }
        // 发票编号模糊查询
        if (invoiceNo != null && !invoiceNo.trim().isEmpty()) {
            wrapper.like(CrmInvoice::getInvoiceNo, invoiceNo.trim());
        }
        // 客户名称模糊查询
        if (customerName != null && !customerName.trim().isEmpty()) {
            // 先查询匹配的客户ID列表
            LambdaQueryWrapper<CrmCustomer> customerWrapper = new LambdaQueryWrapper<>();
            customerWrapper.like(CrmCustomer::getCustomerName, customerName.trim());
            List<CrmCustomer> customers = crmCustomerService.list(customerWrapper);
            if (!customers.isEmpty()) {
                List<Long> customerIds = customers.stream().map(CrmCustomer::getId).collect(Collectors.toList());
                wrapper.in(CrmInvoice::getCustomerId, customerIds);
            } else {
                // 没有匹配的客户，返回空结果
                return Result.success(new Page<>(current, size));
            }
        }
        try {
            String username = null;
            org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
                username = authentication.getName();
            }
            if (username != null && !"admin".equalsIgnoreCase(username)) {
                SysUser user = sysUserService.getByUsername(username);
                if (user != null) {
                    String ut = user.getUserType();
                    boolean isFinance = ut != null && "finance".equalsIgnoreCase(ut);
                    Long uid = user.getId();
                    if (!isFinance && uid != null) {
                        wrapper.and(w -> w.eq(CrmInvoice::getApplicantId, uid).or().eq(CrmInvoice::getInvoicerId, uid));
                    }
                }
            }
        } catch (Exception ignored) {}
        wrapper.orderByDesc(CrmInvoice::getApplyTime, CrmInvoice::getId);
        Page<CrmInvoice> page = crmInvoiceService.page(new Page<>(current, size), wrapper);
        return Result.success(page);
    }

    @GetMapping("/{id}/details")
    @PreAuthorize("hasAuthority('IM:get')")
    public Result<Page<CrmInvoiceDetail>> details(@PathVariable Long id,
                                                  @RequestParam(defaultValue = "1") Integer current,
                                                  @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(crmInvoiceService.listInvoiceDetails(id, current, size));
    }
}
