package com.yysystem.modules.quote.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yysystem.modules.quote.entity.CrmQuote;
import com.yysystem.modules.quote.entity.CrmQuoteDetail;
import com.yysystem.modules.quote.mapper.CrmQuoteDetailMapper;
import com.yysystem.modules.quote.mapper.CrmQuoteMapper;
import com.yysystem.modules.quote.service.CrmQuoteService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yysystem.modules.system.service.SysCodeRuleService;
import com.yysystem.modules.product.entity.PdmProduct;
import com.yysystem.modules.product.service.PdmProductService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yysystem.modules.crm.entity.CrmCustomer;
import com.yysystem.modules.crm.service.CrmCustomerService;
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
public class CrmQuoteServiceImpl extends ServiceImpl<CrmQuoteMapper, CrmQuote> implements CrmQuoteService {

    @Autowired
    private CrmQuoteDetailMapper crmQuoteDetailMapper;

    @Autowired
    private SysCodeRuleService sysCodeRuleService;

    @Autowired
    private PdmProductService pdmProductService;
    
    @Autowired
    private SysUserService sysUserService;
    
    @Autowired
    private CrmCustomerService crmCustomerService;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveQuote(CrmQuote crmQuote) {
        // 1. 生成报价单号
        if (StrUtil.isBlank(crmQuote.getQuoteNo())) {
            crmQuote.setQuoteNo(sysCodeRuleService.generateNextCode("QUOTE_NO"));
        }
        
        // 2. 默认值设置
        if (crmQuote.getCreateTime() == null) {
            crmQuote.setCreateTime(LocalDateTime.now());
        }
        if (crmQuote.getAuditStatus() == null) {
            crmQuote.setAuditStatus("PENDING");
        }
        if (crmQuote.getQuoteStatus() == null) {
            crmQuote.setQuoteStatus("WAITING");
        }
        if (crmQuote.getSalesmanId() == null) {
            try {
                org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
                    String username = authentication.getName();
                    SysUser user = sysUserService.getByUsername(username);
                    if (user != null) {
                        crmQuote.setSalesmanId(user.getId());
                    }
                }
            } catch (Exception ignored) {}
            if (crmQuote.getSalesmanId() == null) {
                crmQuote.setSalesmanId(1L);
            }
        }

        // 3. 计算总金额与总数量
        calculateTotal(crmQuote);
        
        // 3.1 查重校验
        Long customerId = crmQuote.getCustomerId();
        java.time.LocalDate quoteDate = crmQuote.getQuoteDate();
        java.math.BigDecimal quoteAmount = crmQuote.getQuoteAmount() == null ? java.math.BigDecimal.ZERO : crmQuote.getQuoteAmount();
        Integer productTotal = crmQuote.getProductTotal() == null ? 0 : crmQuote.getProductTotal();
        if (customerId != null && quoteDate != null) {
            long dup = this.count(new LambdaQueryWrapper<CrmQuote>()
                    .eq(CrmQuote::getCustomerId, customerId)
                    .eq(CrmQuote::getQuoteDate, quoteDate)
                    .eq(CrmQuote::getQuoteAmount, quoteAmount)
                    .eq(CrmQuote::getProductTotal, productTotal));
            if (dup > 0) {
                throw new IllegalArgumentException("报价单重复提交");
            }
        }

        // 4. 保存主表
        boolean result = this.save(crmQuote);

        // 5. 保存明细表
        if (result && CollUtil.isNotEmpty(crmQuote.getDetailList())) {
            CrmCustomer customer = crmQuote.getCustomerId() != null ? crmCustomerService.getById(crmQuote.getCustomerId()) : null;
            boolean isAgent = customer != null && (
                    "AGENT".equalsIgnoreCase(String.valueOf(customer.getCustomerCategory())) ||
                            (customer.getCustomerTag() != null && customer.getCustomerTag().contains("代理"))
            );
            for (CrmQuoteDetail detail : crmQuote.getDetailList()) {
                detail.setQuoteId(crmQuote.getId());
                if (detail.getProductCode() != null) {
                    PdmProduct product = pdmProductService.getOne(new LambdaQueryWrapper<PdmProduct>()
                            .eq(PdmProduct::getProductCode, detail.getProductCode()));
                    if (product != null) {
                        detail.setCostPrice(product.getCostPrice());
                        if (detail.getSalesPrice() == null || BigDecimal.ZERO.compareTo(detail.getSalesPrice()) == 0) {
                            BigDecimal price = isAgent
                                    ? (product.getChannelPrice() != null ? product.getChannelPrice() : product.getCostPrice())
                                    : (product.getTerminalPrice() != null ? product.getTerminalPrice() : product.getCostPrice());
                            detail.setSalesPrice(price);
                        }
                    }
                }
                crmQuoteDetailMapper.insert(detail);
            }
        }
        return result;
    }

    @Override
    public CrmQuote getQuoteById(Long id) {
        CrmQuote quote = this.getById(id);
        if (quote != null) {
            List<CrmQuoteDetail> details = crmQuoteDetailMapper.selectList(
                    new LambdaQueryWrapper<CrmQuoteDetail>().eq(CrmQuoteDetail::getQuoteId, id)
            );
            quote.setDetailList(details);
        }
        return quote;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateQuote(CrmQuote crmQuote) {
        // 1. 计算总金额与总数量
        calculateTotal(crmQuote);
        
        // 1.1 查重校验
        Long customerId = crmQuote.getCustomerId();
        java.time.LocalDate quoteDate = crmQuote.getQuoteDate();
        java.math.BigDecimal quoteAmount = crmQuote.getQuoteAmount() == null ? java.math.BigDecimal.ZERO : crmQuote.getQuoteAmount();
        Integer productTotal = crmQuote.getProductTotal() == null ? 0 : crmQuote.getProductTotal();
        if (customerId != null && quoteDate != null) {
            long dup = this.count(new LambdaQueryWrapper<CrmQuote>()
                    .eq(CrmQuote::getCustomerId, customerId)
                    .eq(CrmQuote::getQuoteDate, quoteDate)
                    .eq(CrmQuote::getQuoteAmount, quoteAmount)
                    .eq(CrmQuote::getProductTotal, productTotal)
                    .ne(CrmQuote::getId, crmQuote.getId()));
            if (dup > 0) {
                throw new IllegalArgumentException("报价单重复提交");
            }
        }
        
        // 2. 更新主表
        boolean result = this.updateById(crmQuote);

        // 3. 更新明细表（先删后插）
        if (result) {
            CrmCustomer customer = crmQuote.getCustomerId() != null ? crmCustomerService.getById(crmQuote.getCustomerId()) : null;
            boolean isAgent = customer != null && (
                    "AGENT".equalsIgnoreCase(String.valueOf(customer.getCustomerCategory())) ||
                            (customer.getCustomerTag() != null && customer.getCustomerTag().contains("代理"))
            );
            crmQuoteDetailMapper.delete(new LambdaQueryWrapper<CrmQuoteDetail>().eq(CrmQuoteDetail::getQuoteId, crmQuote.getId()));
            if (CollUtil.isNotEmpty(crmQuote.getDetailList())) {
                for (CrmQuoteDetail detail : crmQuote.getDetailList()) {
                    detail.setQuoteId(crmQuote.getId());
                    if (detail.getProductCode() != null) {
                        PdmProduct product = pdmProductService.getOne(new LambdaQueryWrapper<PdmProduct>()
                                .eq(PdmProduct::getProductCode, detail.getProductCode()));
                        if (product != null) {
                            detail.setCostPrice(product.getCostPrice());
                            if (detail.getSalesPrice() == null || BigDecimal.ZERO.compareTo(detail.getSalesPrice()) == 0) {
                                BigDecimal price = isAgent
                                        ? (product.getChannelPrice() != null ? product.getChannelPrice() : product.getCostPrice())
                                        : (product.getTerminalPrice() != null ? product.getTerminalPrice() : product.getCostPrice());
                                detail.setSalesPrice(price);
                            }
                        }
                    }
                    crmQuoteDetailMapper.insert(detail);
                }
            }
        }
        return result;
    }

    private void calculateTotal(CrmQuote crmQuote) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalCount = 0;
        BigDecimal costTotal = BigDecimal.ZERO;
        BigDecimal channelTotal = BigDecimal.ZERO;
        BigDecimal terminalTotal = BigDecimal.ZERO;

        if (CollUtil.isNotEmpty(crmQuote.getDetailList())) {
            for (CrmQuoteDetail detail : crmQuote.getDetailList()) {
                if (detail.getSalesPrice() != null && detail.getSalesQuantity() != null) {
                    BigDecimal amount = detail.getSalesPrice().multiply(new BigDecimal(detail.getSalesQuantity()));
                    totalAmount = totalAmount.add(amount);
                    totalCount += detail.getSalesQuantity();
                    channelTotal = channelTotal.add(amount);
                }
                if (detail.getProductCode() != null && detail.getSalesQuantity() != null) {
                    PdmProduct product = pdmProductService.getOne(new LambdaQueryWrapper<PdmProduct>()
                            .eq(PdmProduct::getProductCode, detail.getProductCode()));
                    BigDecimal qty = new BigDecimal(detail.getSalesQuantity());
                    BigDecimal cost = product != null && product.getCostPrice() != null ? product.getCostPrice() : BigDecimal.ZERO;
                    BigDecimal terminal = product != null && product.getTerminalPrice() != null ? product.getTerminalPrice() : cost;
                    costTotal = costTotal.add(cost.multiply(qty));
                    terminalTotal = terminalTotal.add(terminal.multiply(qty));
                }
            }
        }
        crmQuote.setQuoteAmount(totalAmount);
        crmQuote.setProductTotal(totalCount);
        crmQuote.setCostTotalAmount(costTotal);
        crmQuote.setChannelTotalAmount(channelTotal);
        crmQuote.setTerminalTotalAmount(terminalTotal);
    }

    @Override
    public Page<CrmQuoteDetail> listQuoteDetails(Long quoteId, Integer current, Integer size) {
        Page<CrmQuoteDetail> page = new Page<>(current == null ? 1 : current, size == null ? 10 : size);
        return crmQuoteDetailMapper.selectPage(page,
                new LambdaQueryWrapper<CrmQuoteDetail>().eq(CrmQuoteDetail::getQuoteId, quoteId));
    }
}
