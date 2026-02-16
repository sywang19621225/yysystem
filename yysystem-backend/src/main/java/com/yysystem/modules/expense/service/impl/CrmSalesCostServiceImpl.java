package com.yysystem.modules.expense.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yysystem.modules.expense.entity.CrmSalesCost;
import com.yysystem.modules.expense.entity.CrmSalesCostDetail;
import com.yysystem.modules.expense.mapper.CrmSalesCostDetailMapper;
import com.yysystem.modules.expense.mapper.CrmSalesCostMapper;
import com.yysystem.modules.expense.service.CrmSalesCostService;
import com.yysystem.modules.system.entity.SysUser;
import com.yysystem.modules.system.service.SysUserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CrmSalesCostServiceImpl extends ServiceImpl<CrmSalesCostMapper, CrmSalesCost> implements CrmSalesCostService {

    @Autowired
    private CrmSalesCostDetailMapper detailMapper;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private org.springframework.context.ApplicationContext applicationContext;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveCost(CrmSalesCost cost) {
        if (cost.getCreateTime() == null) {
            cost.setCreateTime(LocalDateTime.now());
        }
        try {
            if (cost.getApplicantId() == null || cost.getApplicantDeptId() == null) {
                String username = null;
                org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
                    username = authentication.getName();
                }
                if (username != null) {
                    SysUser user = sysUserService.getByUsername(username);
                    if (user != null) {
                        if (cost.getApplicantId() == null) cost.setApplicantId(user.getId());
                        if (cost.getApplicantDeptId() == null) cost.setApplicantDeptId(user.getDeptId());
                    }
                }
            }
            if (cost.getApplyTime() == null) {
                cost.setApplyTime(LocalDateTime.now());
            }
            if (cost.getExpenditureDate() == null) {
                cost.setExpenditureDate(cost.getApplyTime());
            }
            if (cost.getExpenditureType() == null || cost.getExpenditureType().isEmpty()) {
                cost.setExpenditureType("OTHER");
            }
        } catch (Exception ignored) {}
        if (cost.getCreatorId() == null) {
            Long creator = cost.getApplicantId();
            try {
                if (creator == null) {
                    String username = null;
                    org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                    if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
                        username = authentication.getName();
                    }
                    if (username != null) {
                        SysUser user = sysUserService.getByUsername(username);
                        if (user != null) creator = user.getId();
                    }
                }
            } catch (Exception ignored) {}
            cost.setCreatorId(creator);
        }
        if (cost.getTotalAmount() == null) {
            BigDecimal total = BigDecimal.ZERO;
            List<CrmSalesCostDetail> details = getDetailsFromExtend(cost);
            for (CrmSalesCostDetail d : details) {
                if (d.getAmount() != null) {
                    total = total.add(d.getAmount());
                }
            }
            cost.setTotalAmount(total);
        }
        boolean ok = this.save(cost);
        if (!ok) return null;
        try {
            com.yysystem.modules.workflow.service.WorkflowService wf = applicationContext.getBean(com.yysystem.modules.workflow.service.WorkflowService.class);
            wf.startInstance("expense", cost.getId(), cost.getApplicantId(), cost.getTotalAmount(), cost.getReimburseType(), cost.getApplicantDeptId());
            cost.setAuditStatus("UNDER_REVIEW");
            this.updateById(cost);
        } catch (Exception ignored) {}
        List<CrmSalesCostDetail> details = getDetailsFromExtend(cost);
        for (CrmSalesCostDetail d : details) {
            d.setSalesCostId(cost.getId());
            d.setCreateTime(LocalDateTime.now());
            detailMapper.insert(d);
        }
        return cost.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateCost(CrmSalesCost cost) {
        boolean ok = this.updateById(cost);
        if (ok) {
            detailMapper.delete(new LambdaQueryWrapper<CrmSalesCostDetail>().eq(CrmSalesCostDetail::getSalesCostId, cost.getId()));
            List<CrmSalesCostDetail> details = getDetailsFromExtend(cost);
            for (CrmSalesCostDetail d : details) {
                d.setSalesCostId(cost.getId());
                d.setCreateTime(LocalDateTime.now());
                detailMapper.insert(d);
            }
        }
        return ok;
    }

    @Override
    public CrmSalesCost getCostById(Long id) {
        CrmSalesCost c = this.getById(id);
        if (c != null) {
            List<CrmSalesCostDetail> details = detailMapper.selectList(new LambdaQueryWrapper<CrmSalesCostDetail>().eq(CrmSalesCostDetail::getSalesCostId, id));
            if (details != null && !details.isEmpty()) {
                java.util.Map<String, Object> ext = new java.util.HashMap<>();
                ext.put("details", details);
                c.setExtendFields(new com.fasterxml.jackson.databind.ObjectMapper().valueToTree(ext).toString());
            }
        }
        return c;
    }

    private List<CrmSalesCostDetail> getDetailsFromExtend(CrmSalesCost cost) {
        try {
            if (cost.getExtendFields() != null && !cost.getExtendFields().isEmpty()) {
                com.fasterxml.jackson.databind.JsonNode node = new com.fasterxml.jackson.databind.ObjectMapper().readTree(cost.getExtendFields());
                com.fasterxml.jackson.databind.JsonNode arr = node.get("details");
                if (arr != null && arr.isArray()) {
                    java.util.List<CrmSalesCostDetail> list = new java.util.ArrayList<>();
                    for (com.fasterxml.jackson.databind.JsonNode it : arr) {
                        CrmSalesCostDetail d = new CrmSalesCostDetail();
                        d.setCustomerId(it.hasNonNull("customerId") ? it.get("customerId").asLong() : null);
                        d.setCategory(it.hasNonNull("category") ? it.get("category").asText() : null);
                        d.setContent(it.hasNonNull("content") ? it.get("content").asText() : null);
                        d.setAmount(it.hasNonNull("amount") ? new BigDecimal(it.get("amount").asText()) : null);
                        d.setInvoiceType(it.hasNonNull("invoiceType") ? it.get("invoiceType").asText() : null);
                        d.setInvoiceAttachment(it.hasNonNull("invoiceAttachment") ? it.get("invoiceAttachment").asText() : null);
                        list.add(d);
                    }
                    return list;
                }
            }
        } catch (Exception ignored) {}
        return java.util.Collections.emptyList();
    }
}
