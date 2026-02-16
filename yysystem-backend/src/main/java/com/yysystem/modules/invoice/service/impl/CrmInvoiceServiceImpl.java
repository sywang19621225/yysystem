package com.yysystem.modules.invoice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yysystem.modules.contract.entity.CrmContract;
import com.yysystem.modules.contract.service.CrmContractService;
import com.yysystem.modules.crm.entity.CrmCustomer;
import com.yysystem.modules.crm.service.CrmCustomerService;
import com.yysystem.modules.invoice.entity.CrmInvoice;
import com.yysystem.modules.invoice.entity.CrmInvoiceDetail;
import com.yysystem.modules.invoice.mapper.CrmInvoiceDetailMapper;
import com.yysystem.modules.invoice.mapper.CrmInvoiceMapper;
import com.yysystem.modules.invoice.service.CrmInvoiceService;
import com.yysystem.modules.system.entity.SysUser;
import com.yysystem.modules.system.service.SysUserService;
import com.yysystem.modules.system.entity.SysRole;
import com.yysystem.modules.system.service.SysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;
import java.util.Arrays;

@Service
public class CrmInvoiceServiceImpl extends ServiceImpl<CrmInvoiceMapper, CrmInvoice> implements CrmInvoiceService {

    @Autowired
    private CrmInvoiceDetailMapper crmInvoiceDetailMapper;
    @Autowired
    private CrmCustomerService crmCustomerService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private CrmContractService crmContractService;
    @Autowired
    private SysRoleService sysRoleService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveInvoice(CrmInvoice crmInvoice) {
        // 检查发票号是否重复
        if (crmInvoice.getInvoiceNo() != null && !crmInvoice.getInvoiceNo().trim().isEmpty()) {
            Long count = this.baseMapper.selectCount(new LambdaQueryWrapper<CrmInvoice>()
                    .eq(CrmInvoice::getInvoiceNo, crmInvoice.getInvoiceNo().trim()));
            if (count != null && count > 0) {
                throw new com.yysystem.common.exception.BizException("发票号已存在：" + crmInvoice.getInvoiceNo());
            }
        }
        
        // 检查合同是否已有待开票的申请
        if (crmInvoice.getContractId() != null) {
            Long waitingCount = this.baseMapper.selectCount(new LambdaQueryWrapper<CrmInvoice>()
                    .eq(CrmInvoice::getContractId, crmInvoice.getContractId())
                    .eq(CrmInvoice::getInvoiceStatus, "WAITING"));
            if (waitingCount != null && waitingCount > 0) {
                throw new com.yysystem.common.exception.BizException("该合同已有待开票的申请，请等待财务处理后再提交");
            }
        }
        
        // 检查合同是否已完全开票
        if (crmInvoice.getContractId() != null) {
            CrmContract contract = crmContractService.getById(crmInvoice.getContractId());
            if (contract != null && contract.getContractAmount() != null) {
                // 计算已开票金额（已完成的：已开票和冲抵货款）
                BigDecimal totalInvoiced = BigDecimal.ZERO;
                List<CrmInvoice> existingInvoices = this.list(new LambdaQueryWrapper<CrmInvoice>()
                        .eq(CrmInvoice::getContractId, crmInvoice.getContractId())
                        .in(CrmInvoice::getInvoiceStatus, "DONE", "冲抵货款"));
                for (CrmInvoice inv : existingInvoices) {
                    if (inv.getInvoiceAmount() != null) {
                        totalInvoiced = totalInvoiced.add(inv.getInvoiceAmount());
                    }
                }
                
                // 如果已开票金额 >= 合同金额，则不允许再开票
                if (totalInvoiced.compareTo(contract.getContractAmount()) >= 0) {
                    throw new com.yysystem.common.exception.BizException("该合同已完全开票，不能再开票");
                }
                
                // 如果本次开票后超过合同金额，给出提示
                BigDecimal newInvoiceAmount = crmInvoice.getInvoiceAmount() != null ? crmInvoice.getInvoiceAmount() : BigDecimal.ZERO;
                if (totalInvoiced.add(newInvoiceAmount).compareTo(contract.getContractAmount()) > 0) {
                    throw new com.yysystem.common.exception.BizException("开票金额超出合同剩余可开票金额，剩余可开票金额：" + contract.getContractAmount().subtract(totalInvoiced));
                }
            }
        }
        if (crmInvoice.getApplicantId() == null) {
            org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Object principal = null;
            if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
                principal = authentication.getPrincipal();
            }
            if (principal instanceof UserDetails) {
                String username = ((UserDetails) principal).getUsername();
                SysUser u = sysUserService.getByUsername(username);
                if (u != null) {
                    crmInvoice.setApplicantId(u.getId());
                }
            }
        }
        if (crmInvoice.getApplyTime() == null) {
            crmInvoice.setApplyTime(LocalDateTime.now());
        }
        if (crmInvoice.getInvoiceStatus() == null || crmInvoice.getInvoiceStatus().isEmpty()) {
            crmInvoice.setInvoiceStatus("WAITING");
        }
        // 如果前端没有传递开票金额，则根据明细计算；否则使用前端传递的金额
        if (crmInvoice.getInvoiceAmount() == null || crmInvoice.getInvoiceAmount().compareTo(BigDecimal.ZERO) == 0) {
            if (crmInvoice.getDetailList() != null && !crmInvoice.getDetailList().isEmpty()) {
                BigDecimal total = BigDecimal.ZERO;
                for (CrmInvoiceDetail d : crmInvoice.getDetailList()) {
                    BigDecimal price = d.getSalesPrice() != null ? d.getSalesPrice() : BigDecimal.ZERO;
                    int qty = d.getSalesQuantity() != null ? d.getSalesQuantity() : 0;
                    total = total.add(price.multiply(BigDecimal.valueOf(qty)));
                }
                crmInvoice.setInvoiceAmount(total);
            }
        }
        this.save(crmInvoice);
        if (crmInvoice.getDetailList() != null) {
            for (CrmInvoiceDetail d : crmInvoice.getDetailList()) {
                d.setInvoiceId(crmInvoice.getId());
                crmInvoiceDetailMapper.insert(d);
            }
        }
        if (crmInvoice.getContractId() != null) {
            updateContractInvoicedAmount(crmInvoice.getContractId());
        }
        return crmInvoice.getId();
    }

    @Override
    public CrmInvoice getInvoiceById(Long id) {
        CrmInvoice inv = this.getById(id);
        if (inv != null) {
            List<CrmInvoiceDetail> details = crmInvoiceDetailMapper.selectList(new LambdaQueryWrapper<CrmInvoiceDetail>().eq(CrmInvoiceDetail::getInvoiceId, id));
            inv.setDetailList(details);
            if (inv.getCustomerId() != null) {
                CrmCustomer customer = crmCustomerService.getById(inv.getCustomerId());
                inv.setCustomerName(customer != null ? customer.getCustomerName() : null);
            }
            if (inv.getContractId() != null) {
                CrmContract c = crmContractService.getById(inv.getContractId());
                inv.setContractNo(c != null ? c.getContractNo() : null);
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
        return inv;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateInvoice(CrmInvoice crmInvoice) {
        CrmInvoice existing = this.getById(crmInvoice.getId());
        // 检查发票号是否重复（排除当前记录）
        if (crmInvoice.getInvoiceNo() != null && !crmInvoice.getInvoiceNo().trim().isEmpty()) {
            Long count = this.baseMapper.selectCount(new LambdaQueryWrapper<CrmInvoice>()
                    .eq(CrmInvoice::getInvoiceNo, crmInvoice.getInvoiceNo().trim())
                    .ne(CrmInvoice::getId, crmInvoice.getId()));
            if (count != null && count > 0) {
                throw new com.yysystem.common.exception.BizException("发票号已存在：" + crmInvoice.getInvoiceNo());
            }
        }
        boolean isSales = false;
        org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = null;
        if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
            principal = authentication.getPrincipal();
        }
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            SysUser u = sysUserService.getByUsername(username);
            if (u != null) {
                String ut = String.valueOf(u.getUserType()).toLowerCase();
                boolean isAdmin = "admin".equals(ut);
                boolean isFinance = "finance".equals(ut);
                if (u.getRoleId() != null) {
                    SysRole role = sysRoleService.getById(u.getRoleId());
                    String rn = role != null ? String.valueOf(role.getRoleName()) : "";
                    isAdmin = isAdmin || rn.contains("管理员");
                    isFinance = isFinance || rn.contains("财务");
                }
                isSales = !isAdmin && !isFinance;
            }
        }
        if (existing != null && ("DONE".equals(existing.getInvoiceStatus()) || "冲抵货款".equals(existing.getInvoiceStatus())) && isSales) {
            CrmInvoice toUpdate = new CrmInvoice();
            toUpdate.setId(crmInvoice.getId());
            toUpdate.setInvoiceGiveDesc(crmInvoice.getInvoiceGiveDesc());
            toUpdate.setGiveTime(crmInvoice.getGiveTime());
            return super.updateById(toUpdate);
        }
        boolean result = this.updateById(crmInvoice);
        if (result && crmInvoice.getDetailList() != null) {
            crmInvoiceDetailMapper.delete(new LambdaQueryWrapper<CrmInvoiceDetail>().eq(CrmInvoiceDetail::getInvoiceId, crmInvoice.getId()));
            for (CrmInvoiceDetail d : crmInvoice.getDetailList()) {
                d.setInvoiceId(crmInvoice.getId());
                crmInvoiceDetailMapper.insert(d);
            }
            // 如果前端没有传递开票金额，则根据明细计算；否则使用前端传递的金额
            if (crmInvoice.getInvoiceAmount() == null || crmInvoice.getInvoiceAmount().compareTo(BigDecimal.ZERO) == 0) {
                BigDecimal total = BigDecimal.ZERO;
                for (CrmInvoiceDetail d : crmInvoice.getDetailList()) {
                    BigDecimal price = d.getSalesPrice() != null ? d.getSalesPrice() : BigDecimal.ZERO;
                    int qty = d.getSalesQuantity() != null ? d.getSalesQuantity() : 0;
                    total = total.add(price.multiply(BigDecimal.valueOf(qty)));
                }
                CrmInvoice toUpdate = this.getById(crmInvoice.getId());
                if (toUpdate != null) {
                    toUpdate.setInvoiceAmount(total);
                    super.updateById(toUpdate);
                }
            }
        }
        if (crmInvoice.getContractId() != null) {
            BigDecimal total = BigDecimal.ZERO;
            updateContractInvoicedAmount(crmInvoice.getContractId());
        }
        return result;
    }

    /**
     * 更新合同的已开票金额
     * 只统计已审核通过（audit_status = 'PASSED'）且开票状态为已完成的发票
     */
    private void updateContractInvoicedAmount(Long contractId) {
        if (contractId == null) return;
        
        BigDecimal total = BigDecimal.ZERO;
        // 已完成状态列表：DONE(已开票)、冲抵货款、收据
        List<String> completedStatuses = Arrays.asList("DONE", "冲抵货款", "收据");
        // 只统计已审核通过的发票
        List<CrmInvoice> invoices = this.list(new LambdaQueryWrapper<CrmInvoice>()
                .eq(CrmInvoice::getContractId, contractId)
                .in(CrmInvoice::getInvoiceStatus, completedStatuses)
                .eq(CrmInvoice::getAuditStatus, "PASSED"));
        for (CrmInvoice inv : invoices) {
            if (inv.getInvoiceAmount() != null) {
                total = total.add(inv.getInvoiceAmount());
            }
        }
        CrmContract contract = crmContractService.getById(contractId);
        if (contract != null) {
            contract.setInvoicedAmount(total);
            crmContractService.updateById(contract);
        }
    }

    @Override
    public Page<CrmInvoiceDetail> listInvoiceDetails(Long invoiceId, Integer current, Integer size) {
        Page<CrmInvoiceDetail> page = new Page<>(current == null ? 1 : current, size == null ? 10 : size);
        return crmInvoiceDetailMapper.selectPage(page, new LambdaQueryWrapper<CrmInvoiceDetail>().eq(CrmInvoiceDetail::getInvoiceId, invoiceId));
    }
}
