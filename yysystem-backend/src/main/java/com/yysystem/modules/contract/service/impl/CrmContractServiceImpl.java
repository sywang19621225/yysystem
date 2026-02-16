package com.yysystem.modules.contract.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yysystem.modules.contract.entity.CrmContract;
import com.yysystem.modules.contract.entity.CrmContractDetail;
import com.yysystem.modules.contract.mapper.CrmContractDetailMapper;
import com.yysystem.modules.contract.mapper.CrmContractMapper;
import com.yysystem.modules.contract.service.CrmContractService;
import com.yysystem.modules.quote.entity.CrmQuote;
import com.yysystem.modules.quote.entity.CrmQuoteDetail;
import com.yysystem.modules.quote.service.CrmQuoteService;
import com.yysystem.modules.system.service.SysCodeRuleService;
import com.yysystem.modules.product.entity.PdmProduct;
import com.yysystem.modules.product.service.PdmProductService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.yysystem.common.exception.BizException;

@Service
public class CrmContractServiceImpl extends ServiceImpl<CrmContractMapper, CrmContract> implements CrmContractService {

    @Autowired
    private CrmContractDetailMapper crmContractDetailMapper;

    @Autowired
    private SysCodeRuleService sysCodeRuleService;
    
    @Autowired
    private CrmQuoteService crmQuoteService;
    @Autowired
    private PdmProductService pdmProductService;
    @Autowired
    private org.springframework.context.ApplicationContext applicationContext;
    
    @Autowired
    private com.yysystem.modules.crm.service.CrmCustomerService crmCustomerService;
    @Autowired
    private com.yysystem.modules.crm.service.CrmContactService crmContactService;
    @Autowired
    private com.yysystem.modules.finance.service.CrmFinanceService crmFinanceService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveContract(CrmContract crmContract) {
        // 1. 生成合同编号
        if (StrUtil.isBlank(crmContract.getContractNo())) {
            crmContract.setContractNo(sysCodeRuleService.generateNextCode("CONTRACT_NO"));
        }

        // 2. 默认值
        if (crmContract.getCreateTime() == null) {
            crmContract.setCreateTime(LocalDateTime.now());
        }
        if (crmContract.getAuditStatus() == null) {
            crmContract.setAuditStatus("PENDING");
        }
        // 禁止手工修改累计收款与预付款，初始化为0
        crmContract.setAdvancePayment(java.math.BigDecimal.ZERO);
        if (crmContract.getTotalCollection() == null) {
            crmContract.setTotalCollection(BigDecimal.ZERO);
        }
        if (crmContract.getTotalExpenditure() == null) {
            crmContract.setTotalExpenditure(BigDecimal.ZERO);
        }
        if (crmContract.getInvoicedAmount() == null) {
            crmContract.setInvoicedAmount(BigDecimal.ZERO);
        }
        if (crmContract.getSalesmanId() == null) {
            crmContract.setSalesmanId(1L); // TODO: 获取当前登录用户
        }

        // 3. 计算金额
        calculateTotal(crmContract);
        
        // 3.1 设置客户名称和联系人姓名
        if (crmContract.getCustomerId() != null) {
            com.yysystem.modules.crm.entity.CrmCustomer customer = crmCustomerService.getById(crmContract.getCustomerId());
            if (customer != null) {
                crmContract.setCustomerName(customer.getCustomerName());
            }
        }
        
        if (crmContract.getLinkmanId() != null) {
            com.yysystem.modules.crm.entity.CrmContact contact = crmContactService.getById(crmContract.getLinkmanId());
            if (contact != null) {
                crmContract.setLinkmanName(contact.getName());
            }
        }
        
        // 3.2 防重复提交校验
        Long customerId = crmContract.getCustomerId();
        java.time.LocalDate quoteDate = crmContract.getQuoteDate();
        java.math.BigDecimal contractAmount = crmContract.getContractAmount() == null ? java.math.BigDecimal.ZERO : crmContract.getContractAmount();
        if (customerId != null && quoteDate != null) {
            long dup = this.count(new LambdaQueryWrapper<CrmContract>()
                    .eq(CrmContract::getCustomerId, customerId)
                    .eq(CrmContract::getQuoteDate, quoteDate)
                    .eq(CrmContract::getContractAmount, contractAmount));
            if (dup > 0) {
                throw new BizException("合同重复提交");
            }
        }

        // 4. 保存主表
        boolean result = this.save(crmContract);
        if (!result) {
            throw new BizException("保存合同失败");
        }
        try {
            com.yysystem.modules.workflow.service.WorkflowService wf = applicationContext.getBean(com.yysystem.modules.workflow.service.WorkflowService.class);
            wf.startInstance("contract", crmContract.getId(), crmContract.getSalesmanId(), crmContract.getContractAmount(), null, null);
            crmContract.setAuditStatus("UNDER_REVIEW");
            this.updateById(crmContract);
        } catch (Exception ignored) {}

        // 5. 保存明细
        if (CollUtil.isNotEmpty(crmContract.getDetailList())) {
            for (CrmContractDetail detail : crmContract.getDetailList()) {
                if (StrUtil.isBlank(detail.getProductRemark())) {
                    PdmProduct prod = null;
                    String code = detail.getProductCode();
                    if (StrUtil.isNotBlank(code)) {
                        prod = pdmProductService.getOne(new LambdaQueryWrapper<PdmProduct>().eq(PdmProduct::getProductCode, code));
                    } else if (StrUtil.isNotBlank(detail.getProductName())) {
                        LambdaQueryWrapper<PdmProduct> qw = new LambdaQueryWrapper<PdmProduct>().eq(PdmProduct::getProductName, detail.getProductName());
                        if (StrUtil.isNotBlank(detail.getProductSpec())) {
                            qw.eq(PdmProduct::getProductSpec, detail.getProductSpec());
                        }
                        prod = pdmProductService.getOne(qw);
                    }
                    if (prod != null && StrUtil.isNotBlank(prod.getRemark())) {
                        detail.setProductRemark(prod.getRemark());
                    }
                }
                detail.setContractId(crmContract.getId());
                detail.setCreateTime(LocalDateTime.now());
                detail.setCreatorId(1L); // TODO
                crmContractDetailMapper.insert(detail);
            }
        }
        return crmContract.getId();
    }

    @Override
    public CrmContract getContractById(Long id) {
        CrmContract contract = this.getById(id);
        if (contract != null) {
            List<CrmContractDetail> details = crmContractDetailMapper.selectList(
                    new LambdaQueryWrapper<CrmContractDetail>().eq(CrmContractDetail::getContractId, id)
            );
            contract.setDetailList(details);
        }
        return contract;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateContract(CrmContract crmContract) {
        CrmContract existing = this.getById(crmContract.getId());
        if (existing == null) {
            throw new BizException("合同不存在");
        }
        
        // 调试日志：打印接收到的数据
        System.out.println("=== updateContract 调试开始 ===");
        System.out.println("合同ID: " + crmContract.getId());
        System.out.println("合同金额: " + crmContract.getContractAmount());
        System.out.println("明细数量: " + (crmContract.getDetailList() != null ? crmContract.getDetailList().size() : 0));
        if (crmContract.getDetailList() != null && !crmContract.getDetailList().isEmpty()) {
            for (int i = 0; i < crmContract.getDetailList().size(); i++) {
                CrmContractDetail detail = crmContract.getDetailList().get(i);
                System.out.println("明细" + i + " - ID: " + detail.getId() + ", 商品: " + detail.getProductName() + ", 价格: " + detail.getSalesPrice() + ", 数量: " + detail.getSalesQuantity());
            }
        }
        
        // PENDING状态下允许修改所有字段，包括财务相关字段
        boolean isPending = "PENDING".equals(existing.getAuditStatus());
        if (!isPending) {
            // 非PENDING状态下保留原有财务数据
            crmContract.setAdvancePayment(existing.getAdvancePayment());
            crmContract.setTotalCollection(existing.getTotalCollection());
            crmContract.setTotalExpenditure(existing.getTotalExpenditure());
            crmContract.setInvoicedAmount(existing.getInvoicedAmount());
        }
        
        // 设置客户名称和联系人姓名
        if (crmContract.getCustomerId() != null) {
            com.yysystem.modules.crm.entity.CrmCustomer customer = crmCustomerService.getById(crmContract.getCustomerId());
            if (customer != null) {
                crmContract.setCustomerName(customer.getCustomerName());
            }
        }
        
        if (crmContract.getLinkmanId() != null) {
            com.yysystem.modules.crm.entity.CrmContact contact = crmContactService.getById(crmContract.getLinkmanId());
            if (contact != null) {
                crmContract.setLinkmanName(contact.getName());
            }
        }
        
        boolean passedLocked = "PASSED".equals(existing.getAuditStatus());
        boolean incomingHasDetails = CollUtil.isNotEmpty(crmContract.getDetailList());
        if (passedLocked) {
            // PASSED状态下锁定金额相关字段
            crmContract.setContractAmount(existing.getContractAmount());
            crmContract.setProductTotal(existing.getProductTotal());
            crmContract.setCostTotalAmount(existing.getCostTotalAmount());
        } else {
            // PENDING状态下允许修改所有字段
            if (incomingHasDetails) {
                calculateTotal(crmContract);
            }
            // PENDING状态下，即使没有传入明细，也允许清空金额字段（不强制保留原有值）
        }
        // 计算应收款 = 合同金额 - 累计收款 + 客户未退回的保证金
        BigDecimal contractAmount = crmContract.getContractAmount() != null ? crmContract.getContractAmount() : BigDecimal.ZERO;
        BigDecimal totalCollection = crmContract.getTotalCollection() != null ? crmContract.getTotalCollection() : BigDecimal.ZERO;
        BigDecimal unreturnedDeposit = calcUnreturnedDeposit(crmContract.getId());
        crmContract.setArrears(contractAmount.subtract(totalCollection).add(unreturnedDeposit));
        
        boolean result = this.updateById(crmContract);
        if (result) {
            if (!passedLocked) {
                // 处理明细数据：更新、新增、删除
                if (incomingHasDetails) {
                    // 收集前端传递的明细ID
                    java.util.Set<Long> incomingDetailIds = crmContract.getDetailList().stream()
                            .map(CrmContractDetail::getId)
                            .filter(id -> id != null)
                            .collect(java.util.stream.Collectors.toSet());
                    
                    // 1. 删除前端未传递的明细（ID不在incomingDetailIds中的记录）
                    if (!incomingDetailIds.isEmpty()) {
                        crmContractDetailMapper.delete(new LambdaQueryWrapper<CrmContractDetail>()
                                .eq(CrmContractDetail::getContractId, crmContract.getId())
                                .notIn(CrmContractDetail::getId, incomingDetailIds));
                    } else {
                        // 如果前端没有传递任何明细ID，则删除该合同下的所有明细
                        crmContractDetailMapper.delete(new LambdaQueryWrapper<CrmContractDetail>()
                                .eq(CrmContractDetail::getContractId, crmContract.getId()));
                    }
                    
                    // 2. 更新或新增明细
                    for (CrmContractDetail detail : crmContract.getDetailList()) {
                        System.out.println("处理明细 - ID: " + detail.getId() + ", 商品: " + detail.getProductName() + ", 价格: " + detail.getSalesPrice() + ", 数量: " + detail.getSalesQuantity());
                        
                        if (StrUtil.isBlank(detail.getProductRemark())) {
                            PdmProduct prod = null;
                            String code = detail.getProductCode();
                            if (StrUtil.isNotBlank(code)) {
                                prod = pdmProductService.getOne(new LambdaQueryWrapper<PdmProduct>().eq(PdmProduct::getProductCode, code));
                                System.out.println("通过商品编码查找商品: " + code + ", 结果: " + (prod != null ? "找到" : "未找到"));
                            } else if (StrUtil.isNotBlank(detail.getProductName())) {
                                LambdaQueryWrapper<PdmProduct> qw = new LambdaQueryWrapper<PdmProduct>().eq(PdmProduct::getProductName, detail.getProductName());
                                if (StrUtil.isNotBlank(detail.getProductSpec())) {
                                    qw.eq(PdmProduct::getProductSpec, detail.getProductSpec());
                                }
                                prod = pdmProductService.getOne(qw);
                                System.out.println("通过商品名称查找商品: " + detail.getProductName() + ", 结果: " + (prod != null ? "找到" : "未找到"));
                            }
                            if (prod != null && StrUtil.isNotBlank(prod.getRemark())) {
                                detail.setProductRemark(prod.getRemark());
                                System.out.println("设置商品备注: " + prod.getRemark());
                            } else if (prod == null) {
                                System.out.println("商品不存在，保持原有备注: " + detail.getProductRemark());
                            }
                        }
                        detail.setContractId(crmContract.getId());
                        if (detail.getId() != null) {
                            // 有ID，更新现有记录
                            detail.setCreateTime(null); // 保持原有创建时间
                            int updateResult = crmContractDetailMapper.updateById(detail);
                            System.out.println("更新明细结果: " + updateResult);
                        } else {
                            // 无ID，新增记录
                            detail.setCreateTime(LocalDateTime.now());
                            detail.setCreatorId(1L); // TODO
                            int insertResult = crmContractDetailMapper.insert(detail);
                            System.out.println("新增明细结果: " + insertResult);
                        }
                    }
                } else {
                    // 前端未传递明细数据，保持原有明细不变（不做任何操作）
                }
            }
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createFromQuote(Long quoteId) {
        CrmQuote quote = crmQuoteService.getQuoteById(quoteId);
        if (quote == null) {
            throw new RuntimeException("报价单不存在");
        }

        CrmContract contract = new CrmContract();
        contract.setCustomerId(quote.getCustomerId());
        contract.setLinkmanId(quote.getLinkmanId());
        contract.setPhone(quote.getPhone());
        contract.setAddress(quote.getAddress());
        contract.setQuoteDate(quote.getQuoteDate());
        contract.setSalesmanId(quote.getSalesmanId());
        contract.setRemark(quote.getRemark());
        
        // 转换明细
        List<CrmContractDetail> contractDetails = new ArrayList<>();
        if (CollUtil.isNotEmpty(quote.getDetailList())) {
            for (CrmQuoteDetail quoteDetail : quote.getDetailList()) {
                CrmContractDetail contractDetail = new CrmContractDetail();
                BeanUtils.copyProperties(quoteDetail, contractDetail, "id", "quoteId");
                contractDetails.add(contractDetail);
            }
        }
        contract.setDetailList(contractDetails);
        
        this.saveContract(contract);
        
        // 更新报价单状态
        quote.setQuoteStatus("CONFIRMED");
        quote.setContractId(contract.getId());
        crmQuoteService.updateQuote(quote);
        
        return contract.getId();
    }

    private void calculateTotal(CrmContract crmContract) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal costTotal = BigDecimal.ZERO;
        int productTotal = 0;
        if (CollUtil.isNotEmpty(crmContract.getDetailList())) {
            for (CrmContractDetail detail : crmContract.getDetailList()) {
                if (detail.getSalesPrice() != null && detail.getSalesQuantity() != null) {
                    BigDecimal amount = detail.getSalesPrice().multiply(new BigDecimal(detail.getSalesQuantity()));
                    totalAmount = totalAmount.add(amount);
                    productTotal += detail.getSalesQuantity();
                }
                if (detail.getCostPrice() != null && detail.getSalesQuantity() != null) {
                    BigDecimal cost = detail.getCostPrice().multiply(new BigDecimal(detail.getSalesQuantity()));
                    costTotal = costTotal.add(cost);
                }
            }
        }
        crmContract.setContractAmount(totalAmount);
        crmContract.setCostTotalAmount(costTotal);
        crmContract.setProductTotal(productTotal);
        // 计算欠款 = 合同金额 - 累计收款 + 客户未退回的保证金
        BigDecimal totalCollection = crmContract.getTotalCollection() != null ? crmContract.getTotalCollection() : BigDecimal.ZERO;
        BigDecimal unreturnedDeposit = calcUnreturnedDeposit(crmContract.getId());
        crmContract.setArrears(totalAmount.subtract(totalCollection).add(unreturnedDeposit));
    }

    private BigDecimal calcUnreturnedDeposit(Long contractId) {
        if (contractId == null) return BigDecimal.ZERO;
        BigDecimal activeDepositOut = BigDecimal.ZERO;
        BigDecimal activeDepositBack = BigDecimal.ZERO;
        BigDecimal convertDepositTotal = BigDecimal.ZERO;
        java.util.List<com.yysystem.modules.finance.entity.CrmFinance> records = crmFinanceService.list(
                new LambdaQueryWrapper<com.yysystem.modules.finance.entity.CrmFinance>()
                        .eq(com.yysystem.modules.finance.entity.CrmFinance::getContractId, contractId)
                        .isNotNull(com.yysystem.modules.finance.entity.CrmFinance::getArrivalTime)
                        .ne(com.yysystem.modules.finance.entity.CrmFinance::getAuditStatus, "REJECTED"));
        for (com.yysystem.modules.finance.entity.CrmFinance r : records) {
            if (!"OUT".equals(r.getType())) continue;
            String category = String.valueOf(r.getCategory() != null ? r.getCategory() : "");
            String remark = String.valueOf(r.getRemark() != null ? r.getRemark() : "");
            if (category.contains("保证金") || remark.contains("保证金")) {
                BigDecimal amt = r.getArrivalAmount() != null ? r.getArrivalAmount() : (r.getAmount() != null ? r.getAmount() : BigDecimal.ZERO);
                BigDecimal refund = extractDepositRefundAmount(r.getExtendFields());
                boolean isConvert = "CONVERT".equals(r.getDepositSourceType());
                if (isConvert) {
                    convertDepositTotal = convertDepositTotal.add(amt);
                } else {
                    activeDepositOut = activeDepositOut.add(amt);
                    if (refund != null) {
                        activeDepositBack = activeDepositBack.add(refund);
                    }
                }
            }
        }
        BigDecimal activeUnreturned = activeDepositOut.subtract(activeDepositBack);
        BigDecimal result = activeUnreturned.subtract(convertDepositTotal);
        return result.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : result;
    }

    private BigDecimal extractDepositRefundAmount(String extendFields) {
        if (extendFields == null || extendFields.trim().isEmpty()) return null;
        try {
            java.util.Map<String, Object> m = new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(extendFields, new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>>() {});
            Object v = m.get("depositRefundAmount");
            if (v == null) return null;
            return new BigDecimal(String.valueOf(v)).setScale(2, java.math.RoundingMode.HALF_UP);
        } catch (Exception ignored) {
            return null;
        }
    }
}
