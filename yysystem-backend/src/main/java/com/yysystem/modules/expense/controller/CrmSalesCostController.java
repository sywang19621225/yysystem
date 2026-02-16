package com.yysystem.modules.expense.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yysystem.common.result.Result;
import com.yysystem.modules.expense.entity.CrmSalesCost;
import com.yysystem.modules.expense.service.CrmSalesCostService;
import com.yysystem.modules.system.entity.SysUser;
import com.yysystem.modules.system.service.SysUserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/crm/expense")
public class CrmSalesCostController {

    @Autowired
    private CrmSalesCostService salesCostService;
    @Autowired
    private SysUserService sysUserService;

    @PostMapping
    @PreAuthorize("hasAuthority('ER:create')")
    public Result<Long> save(@RequestBody CrmSalesCost cost) {
        String username = null;
        org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
            username = authentication.getName();
        }
        if (username != null) {
            SysUser user = sysUserService.getByUsername(username);
            if (user != null) {
                String ut = user.getUserType();
                boolean isFinance = ut != null && "finance".equalsIgnoreCase(ut);
                if (isFinance) return Result.error("财务不可创建报销");
                if (cost.getApplicantId() == null) cost.setApplicantId(user.getId());
                if (cost.getApplicantDeptId() == null) cost.setApplicantDeptId(user.getDeptId());
            }
        }
        Long id = salesCostService.saveCost(cost);
        return id == null ? Result.error("保存失败") : Result.success(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ER:update')")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody CrmSalesCost cost) {
        cost.setId(id);
        return Result.success(salesCostService.updateCost(cost));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ER:delete')")
    public Result<Boolean> remove(@PathVariable Long id) {
        return Result.success(salesCostService.removeById(id));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ER:get')")
    public Result<CrmSalesCost> getById(@PathVariable Long id) {
        return Result.success(salesCostService.getCostById(id));
    }

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('ER:list')")
    public Result<Page<CrmSalesCost>> list(@RequestParam(defaultValue = "1") Integer current,
                                           @RequestParam(defaultValue = "10") Integer size,
                                           @RequestParam(required = false) Long customerId,
                                           @RequestParam(required = false) Long applicantId) {
        LambdaQueryWrapper<CrmSalesCost> wrapper = new LambdaQueryWrapper<>();
        if (customerId != null) {
            wrapper.eq(CrmSalesCost::getCustomerId, customerId);
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
                    if (!isFinance && user.getId() != null) {
                        wrapper.eq(CrmSalesCost::getApplicantId, user.getId());
                    } else if (isFinance || "admin".equalsIgnoreCase(username)) {
                        if (applicantId != null) {
                            wrapper.eq(CrmSalesCost::getApplicantId, applicantId);
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        wrapper.orderByDesc(CrmSalesCost::getCreateTime);
        Page<CrmSalesCost> page = salesCostService.page(new Page<>(current, size), wrapper);
        return Result.success(page);
    }

    @PutMapping("/{id}/finance")
    @PreAuthorize("hasAuthority('ER:update')")
    public Result<Boolean> financeUpdate(@PathVariable Long id,
                                         @RequestBody CrmSalesCost payload) {
        String username = null;
        org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
            username = authentication.getName();
        }
        if (username != null) {
            SysUser user = sysUserService.getByUsername(username);
            if (user != null) {
                String ut = user.getUserType();
                boolean isFinance = ut != null && "finance".equalsIgnoreCase(ut);
                if (!isFinance && !"admin".equalsIgnoreCase(username)) {
                    return Result.error("仅财务可更新报销信息");
                }
                CrmSalesCost upd = new CrmSalesCost();
                upd.setId(id);
                upd.setReimburseTime(payload.getReimburseTime());
                upd.setReimburseMethod(payload.getReimburseMethod());
                upd.setFinanceUserId(user.getId());
                return Result.success(salesCostService.updateById(upd));
            }
        }
        return Result.error("登录状态异常");
    }

    @PutMapping("/{id}/receipt")
    @PreAuthorize("hasAuthority('ER:update')")
    public Result<Boolean> receiptFeedback(@PathVariable Long id,
                                           @RequestBody CrmSalesCost payload) {
        String username = null;
        org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
            username = authentication.getName();
        }
        CrmSalesCost existed = salesCostService.getById(id);
        if (existed == null) return Result.error("记录不存在");
        if (username != null && !"admin".equalsIgnoreCase(username)) {
            SysUser user = sysUserService.getByUsername(username);
            if (user != null && user.getId() != null && !user.getId().equals(existed.getApplicantId())) {
                return Result.error("仅申请人可回告收款");
            }
        }
        CrmSalesCost upd = new CrmSalesCost();
        upd.setId(id);
        upd.setReceiptFeedback(payload.getReceiptFeedback());
        return Result.success(salesCostService.updateById(upd));
    }
}
