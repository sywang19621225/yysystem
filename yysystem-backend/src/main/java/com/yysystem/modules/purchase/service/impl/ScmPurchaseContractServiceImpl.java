package com.yysystem.modules.purchase.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yysystem.modules.purchase.entity.ScmPurchaseContract;
import com.yysystem.modules.purchase.entity.ScmPurchaseContractDetail;
import com.yysystem.modules.purchase.entity.ScmPurchaseRequest;
import com.yysystem.modules.purchase.entity.ScmPurchaseRequestItem;
import com.yysystem.modules.purchase.mapper.ScmPurchaseContractDetailMapper;
import com.yysystem.modules.purchase.mapper.ScmPurchaseContractMapper;
import com.yysystem.modules.purchase.mapper.ScmPurchaseRequestItemMapper;
import com.yysystem.modules.purchase.mapper.ScmPurchaseRequestMapper;
import com.yysystem.modules.purchase.service.ScmPurchaseContractService;
import com.yysystem.modules.system.service.SysCodeRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ScmPurchaseContractServiceImpl extends ServiceImpl<ScmPurchaseContractMapper, ScmPurchaseContract> implements ScmPurchaseContractService {
    @Autowired
    private SysCodeRuleService sysCodeRuleService;
    @Autowired
    private ScmPurchaseContractDetailMapper detailMapper;
    @Autowired
    private ScmPurchaseRequestMapper requestMapper;
    @Autowired
    private ScmPurchaseRequestItemMapper requestItemMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveContract(ScmPurchaseContract contract) {
        if (contract.getContractNo() == null || contract.getContractNo().isEmpty()) {
            contract.setContractNo(sysCodeRuleService.generateNextCode("PURCHASE_CONTRACT_NO"));
        }
        if (contract.getCreateTime() == null) {
            contract.setCreateTime(LocalDateTime.now());
        }
        if (contract.getAuditStatus() == null) {
            contract.setAuditStatus("PENDING");
        }
        BigDecimal total = BigDecimal.ZERO;
        if (CollUtil.isNotEmpty(contract.getDetailList())) {
            for (ScmPurchaseContractDetail d : contract.getDetailList()) {
                BigDecimal price = d.getUnitPrice() != null ? d.getUnitPrice() : BigDecimal.ZERO;
                int qty = d.getQuantity() != null ? d.getQuantity() : 0;
                BigDecimal amount = price.multiply(new BigDecimal(qty));
                d.setAmount(amount);
                total = total.add(amount);
            }
        }
        contract.setContractAmount(total);
        boolean ok = this.save(contract);
        if (!ok) return null;
        if (CollUtil.isNotEmpty(contract.getDetailList())) {
            for (ScmPurchaseContractDetail d : contract.getDetailList()) {
                d.setContractId(contract.getId());
                detailMapper.insert(d);
            }
        }
        return contract.getId();
    }

    @Override
    public ScmPurchaseContract getContractById(Long id) {
        ScmPurchaseContract c = this.getById(id);
        if (c != null) {
            List<ScmPurchaseContractDetail> list = detailMapper.selectList(new LambdaQueryWrapper<ScmPurchaseContractDetail>().eq(ScmPurchaseContractDetail::getContractId, id));
            c.setDetailList(list);
        }
        return c;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateContract(ScmPurchaseContract contract) {
        BigDecimal total = BigDecimal.ZERO;
        if (CollUtil.isNotEmpty(contract.getDetailList())) {
            for (ScmPurchaseContractDetail d : contract.getDetailList()) {
                BigDecimal price = d.getUnitPrice() != null ? d.getUnitPrice() : BigDecimal.ZERO;
                int qty = d.getQuantity() != null ? d.getQuantity() : 0;
                BigDecimal amount = price.multiply(new BigDecimal(qty));
                d.setAmount(amount);
                total = total.add(amount);
            }
        }
        contract.setContractAmount(total);
        boolean ok = this.updateById(contract);
        if (ok) {
            detailMapper.delete(new LambdaQueryWrapper<ScmPurchaseContractDetail>().eq(ScmPurchaseContractDetail::getContractId, contract.getId()));
            if (CollUtil.isNotEmpty(contract.getDetailList())) {
                for (ScmPurchaseContractDetail d : contract.getDetailList()) {
                    d.setContractId(contract.getId());
                    detailMapper.insert(d);
                }
            }
        }
        return ok;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createFromRequest(Long requestId) {
        ScmPurchaseRequest req = requestMapper.selectById(requestId);
        if (req == null) return null;
        ScmPurchaseContract c = new ScmPurchaseContract();
        c.setContractNo(sysCodeRuleService.generateNextCode("PURCHASE_CONTRACT_NO"));
        c.setSupplierId(req.getSupplierId());
        c.setRequestId(req.getId());
        c.setContractName(req.getContractName() != null ? req.getContractName() : "采购合同");
        c.setAuditStatus("PENDING");
        c.setCreateTime(LocalDateTime.now());
        List<ScmPurchaseRequestItem> items = requestItemMapper.selectList(new LambdaQueryWrapper<ScmPurchaseRequestItem>().eq(ScmPurchaseRequestItem::getRequestId, requestId));
        BigDecimal total = BigDecimal.ZERO;
        if (CollUtil.isNotEmpty(items)) {
            for (ScmPurchaseRequestItem it : items) {
                ScmPurchaseContractDetail d = new ScmPurchaseContractDetail();
                d.setProductName(it.getProductName());
                d.setProductCode(it.getProductCode());
                d.setProductSpec(it.getProductSpec());
                d.setQuantity(it.getPurchaseQuantity());
                d.setUnitPrice(it.getPurchasePrice());
                BigDecimal price = it.getPurchasePrice() != null ? it.getPurchasePrice() : BigDecimal.ZERO;
                int qty = it.getPurchaseQuantity() != null ? it.getPurchaseQuantity() : 0;
                BigDecimal amount = price.multiply(new BigDecimal(qty));
                d.setAmount(amount);
                total = total.add(amount);
                d.setRequestItemId(it.getId());
                detailMapper.insert(d);
            }
        }
        c.setContractAmount(total);
        boolean ok = this.save(c);
        if (!ok) return null;
        return c.getId();
    }
}
