package com.yysystem.modules.finance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yysystem.modules.contract.entity.CrmContract;
import com.yysystem.modules.contract.service.CrmContractService;
import com.yysystem.modules.finance.entity.CrmFinance;
import com.yysystem.modules.finance.mapper.CrmFinanceMapper;
import com.yysystem.modules.finance.service.CrmFinanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.yysystem.common.exception.BizException;

@Service
public class CrmFinanceServiceImpl extends ServiceImpl<CrmFinanceMapper, CrmFinance> implements CrmFinanceService {

    @Autowired
    private CrmContractService crmContractService;
    @Autowired
    private com.yysystem.modules.invoice.service.CrmInvoiceService crmInvoiceService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveFinance(CrmFinance crmFinance) {
        String clientRequestId = extractClientRequestId(crmFinance.getExtendFields());
        if (clientRequestId != null && !clientRequestId.trim().isEmpty()) {
            CrmFinance existed = this.getOne(new LambdaQueryWrapper<CrmFinance>()
                    .select(CrmFinance::getId)
                    .apply("JSON_UNQUOTE(JSON_EXTRACT(extend_fields,'$.clientRequestId')) = {0}", clientRequestId)
                    .last("limit 1"));
            if (existed != null && existed.getId() != null) {
                return existed.getId();
            }
            crmFinance.setExtendFields(ensureClientRequestId(crmFinance.getExtendFields(), clientRequestId));
        }
        if (crmFinance.getCreateTime() == null) {
            crmFinance.setCreateTime(LocalDateTime.now());
        }
        if (crmFinance.getAuditStatus() == null) {
            crmFinance.setAuditStatus("PENDING");
        }
        if (crmFinance.getCreatorId() == null) {
            crmFinance.setCreatorId(1L); // TODO
        }

        try {
            boolean result = this.save(crmFinance);
            if (!result) {
                throw new BizException("保存收支记录失败");
            }
        } catch (DuplicateKeyException e) {
            if (clientRequestId != null && !clientRequestId.trim().isEmpty()) {
                CrmFinance existed = this.getOne(new LambdaQueryWrapper<CrmFinance>()
                        .select(CrmFinance::getId)
                        .apply("JSON_UNQUOTE(JSON_EXTRACT(extend_fields,'$.clientRequestId')) = {0}", clientRequestId)
                        .last("limit 1"));
                if (existed != null && existed.getId() != null) {
                    return existed.getId();
                }
            }
            throw e;
        }

        if (crmFinance.getContractId() != null) {
            recalcContractAggregates(crmFinance.getContractId());
        }
        
        return crmFinance.getId();
    }

    @Override
    public boolean updateFinance(CrmFinance crmFinance) {
        CrmFinance existing = this.getById(crmFinance.getId());
        if (existing != null) {
            if (crmFinance.getArrivalTime() != null || crmFinance.getArrivalAmount() != null) {
                java.math.BigDecimal applyAmount = crmFinance.getAmount() != null ? crmFinance.getAmount() : existing.getAmount();
                java.math.BigDecimal arrivalAmount = crmFinance.getArrivalAmount();
                if (arrivalAmount == null) {
                    throw new com.yysystem.common.exception.BizException("请填写到账金额");
                }
                java.math.BigDecimal a1 = applyAmount == null ? java.math.BigDecimal.ZERO : applyAmount.setScale(2, java.math.RoundingMode.HALF_UP);
                java.math.BigDecimal a2 = arrivalAmount.setScale(2, java.math.RoundingMode.HALF_UP);
                if (a1.compareTo(a2) != 0) {
                    throw new com.yysystem.common.exception.BizException("到账金额必须与申请金额一致");
                }
            }
        }
        boolean result = this.updateById(crmFinance);
        CrmFinance updated = this.getById(crmFinance.getId());
        if (updated != null && "IN".equalsIgnoreCase(String.valueOf(updated.getType())) && updated.getArrivalTime() != null) {
            if (updated.getInvoiceNo() == null || updated.getInvoiceNo().trim().isEmpty()) {
                throw new com.yysystem.common.exception.BizException("请填写发票号码");
            }
        }
        if (result && updated != null && updated.getContractId() != null) {
            recalcContractAggregates(updated.getContractId());
            if (updated.getArrivalTime() != null && "IN".equalsIgnoreCase(String.valueOf(updated.getType())) && updated.getInvoiceNo() != null) {
                com.yysystem.modules.invoice.entity.CrmInvoice target =
                        crmInvoiceService.getOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.yysystem.modules.invoice.entity.CrmInvoice>()
                                .eq(com.yysystem.modules.invoice.entity.CrmInvoice::getInvoiceNo, updated.getInvoiceNo())
                                .last("limit 1")
                        );
                if (target != null) {
                    target.setArrivalTime(updated.getArrivalTime());
                    crmInvoiceService.updateById(target);
                }
            }
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean auditFinance(Long id, String status, String detail) {
        CrmFinance finance = this.getById(id);
        if (finance == null) {
            throw new BizException("记录不存在");
        }
        if ("REJECTED".equals(status) && finance.getArrivalTime() != null) {
            throw new BizException("已到账记录禁止退回");
        }
        
        // 防止重复审核
        if ("PASSED".equals(finance.getAuditStatus())) {
             // 如果是自动审核调用的，可能状态还没变，但这里是查出来的
             // 实际上如果是saveFinance里调用的，状态是PENDING
             // 如果是外部调用的，状态可能是PASSED
             // 允许重复调用 auditFinance("PASSED") 只要不重复加钱?
             // 不，应该严格控制
             // throw new BizException("该记录已审核通过");
             // 为兼容saveFinance中的自动审核，这里先不抛异常，或者检查是否已处理
        }
        
        if ("PASSED".equals(finance.getAuditStatus())) {
             return true; // 幂等
        }

        finance.setAuditStatus(status);
        finance.setAuditDetail(detail);
        boolean result = this.updateById(finance);

        if (result && finance.getContractId() != null) {
            recalcContractAggregates(finance.getContractId());
        }
        
        return result;
    }

    private void recalcContractAggregates(Long contractId) {
        CrmContract contract = crmContractService.getById(contractId);
        if (contract == null) return;
        BigDecimal sumIn = BigDecimal.ZERO;
        BigDecimal sumOut = BigDecimal.ZERO;
        BigDecimal depositOut = BigDecimal.ZERO;  // 支付给客户的保证金
        BigDecimal depositBack = BigDecimal.ZERO;  // 客户退回的保证金
        java.util.List<CrmFinance> records = this.list(new LambdaQueryWrapper<CrmFinance>()
                .eq(CrmFinance::getContractId, contractId));
        for (CrmFinance r : records) {
            if (r.getArrivalTime() == null) continue;
            if ("REJECTED".equals(r.getAuditStatus())) continue;
            BigDecimal amt = r.getArrivalAmount() != null ? r.getArrivalAmount() : (r.getAmount() != null ? r.getAmount() : BigDecimal.ZERO);
            String category = String.valueOf(r.getCategory() != null ? r.getCategory() : "");
            String remark = String.valueOf(r.getRemark() != null ? r.getRemark() : "");
            if ("IN".equals(r.getType())) {
                sumIn = sumIn.add(amt);
            } else if ("OUT".equals(r.getType())) {
                BigDecimal refund = extractDepositRefundAmount(r.getExtendFields());
                if (refund != null && refund.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal net = amt.subtract(refund);
                    if (net.compareTo(BigDecimal.ZERO) < 0) net = BigDecimal.ZERO;
                    sumOut = sumOut.add(net);
                } else {
                    sumOut = sumOut.add(amt);
                }
                // 识别保证金支出，计算客户未退回的保证金
                if (category.contains("保证金") || remark.contains("保证金")) {
                    depositOut = depositOut.add(amt);
                    if (refund != null) {
                        depositBack = depositBack.add(refund);
                    }
                }
            }
        }
        contract.setTotalCollection(sumIn);
        contract.setTotalExpenditure(sumOut);
        BigDecimal totalAmount = contract.getContractAmount() != null ? contract.getContractAmount() : BigDecimal.ZERO;
        // 客户未退回的保证金 = 支付的保证金 - 已退回的保证金
        BigDecimal unreturnedDeposit = depositOut.subtract(depositBack);
        if (unreturnedDeposit.compareTo(BigDecimal.ZERO) < 0) {
            unreturnedDeposit = BigDecimal.ZERO;
        }
        // 应收款 = 合同金额 - 累计收款 + 客户未退回的保证金
        contract.setArrears(totalAmount.subtract(sumIn).add(unreturnedDeposit));
        crmContractService.updateById(contract);
    }

    private BigDecimal extractDepositRefundAmount(String extendFields) {
        if (extendFields == null || extendFields.trim().isEmpty()) return null;
        try {
            java.util.Map<String, Object> m = new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(extendFields, new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>>() {});
            Object v = m.get("depositRefundAmount");
            if (v == null) return null;
            BigDecimal n = new BigDecimal(String.valueOf(v));
            return n.setScale(2, java.math.RoundingMode.HALF_UP);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String extractClientRequestId(String extendFields) {
        if (extendFields == null || extendFields.trim().isEmpty()) return null;
        try {
            java.util.Map<String, Object> m = new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(extendFields, new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>>() {});
            Object v = m.get("clientRequestId");
            if (v == null) return null;
            String s = String.valueOf(v).trim();
            return s.isEmpty() ? null : s;
        } catch (Exception ignored) {
            return null;
        }
    }

    private String ensureClientRequestId(String extendFields, String clientRequestId) {
        if (clientRequestId == null || clientRequestId.trim().isEmpty()) return extendFields;
        java.util.Map<String, Object> m = new java.util.HashMap<>();
        try {
            if (extendFields != null && !extendFields.trim().isEmpty()) {
                m = new com.fasterxml.jackson.databind.ObjectMapper()
                        .readValue(extendFields, new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>>() {});
            }
        } catch (Exception ignored) {
            m = new java.util.HashMap<>();
        }
        m.put("clientRequestId", clientRequestId);
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(m);
        } catch (Exception ignored) {
            return extendFields;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(java.io.Serializable id) {
        CrmFinance finance = null;
        try {
            finance = this.getById((Long) id);
        } catch (Exception ignored) {}
        if (finance != null && finance.getArrivalTime() != null) {
            throw new BizException("已到账记录禁止删除");
        }
        boolean res = super.removeById(id);
        if (res && finance != null && finance.getContractId() != null) {
            recalcContractAggregates(finance.getContractId());
        }
        return res;
    }
}
