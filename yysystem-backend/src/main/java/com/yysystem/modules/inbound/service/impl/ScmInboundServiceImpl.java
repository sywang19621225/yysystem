package com.yysystem.modules.inbound.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yysystem.modules.inbound.entity.ScmInbound;
import com.yysystem.modules.inbound.entity.ScmInboundDetail;
import com.yysystem.modules.inbound.mapper.ScmInboundDetailMapper;
import com.yysystem.modules.inbound.mapper.ScmInboundMapper;
import com.yysystem.modules.inbound.service.ScmInboundService;
import com.yysystem.modules.inventory.entity.ScmStockFlow;
import com.yysystem.modules.inventory.service.ScmStockFlowService;
import com.yysystem.modules.product.entity.PdmProduct;
import com.yysystem.modules.product.service.PdmProductService;
import com.yysystem.modules.outbound.entity.ScmOutbound;
import com.yysystem.modules.outbound.entity.ScmOutboundDetail;
import com.yysystem.modules.outbound.mapper.ScmOutboundDetailMapper;
import com.yysystem.modules.outbound.mapper.ScmOutboundMapper;
import com.yysystem.modules.purchase.entity.ScmPurchase;
import com.yysystem.modules.purchase.entity.ScmPurchaseDetail;
import com.yysystem.modules.purchase.entity.ScmPurchaseRequest;
import com.yysystem.modules.purchase.entity.ScmPurchaseRequestItem;
import com.yysystem.modules.purchase.mapper.ScmPurchaseRequestItemMapper;
import com.yysystem.modules.purchase.service.ScmPurchaseRequestService;
import com.yysystem.modules.system.service.SysCodeRuleService;
import com.yysystem.modules.system.service.SysLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScmInboundServiceImpl extends ServiceImpl<ScmInboundMapper, ScmInbound> implements ScmInboundService {

    @Autowired
    private ScmInboundDetailMapper scmInboundDetailMapper;

    @Autowired
    private PdmProductService pdmProductService;

    @Autowired
    private SysCodeRuleService sysCodeRuleService;

    @Autowired
    private ScmStockFlowService scmStockFlowService;

    @Autowired
    private SysLogService sysLogService;
    
    @Autowired
    private ScmPurchaseRequestService scmPurchaseRequestService;
    
    @Autowired
    private ScmPurchaseRequestItemMapper scmPurchaseRequestItemMapper;
    
    @Autowired
    private ScmOutboundMapper scmOutboundMapper;
    
    @Autowired
    private ScmOutboundDetailMapper scmOutboundDetailMapper;

    @Override
    public Page<ScmInbound> getInboundList(Page<ScmInbound> page, String inboundType, String inboundNo, String supplierName, String auditStatus) {
        LambdaQueryWrapper<ScmInbound> wrapper = new LambdaQueryWrapper<>();
        
        // 按入库类型过滤
        if (StrUtil.isNotBlank(inboundType)) {
            wrapper.eq(ScmInbound::getInboundType, inboundType);
        }
        
        // 按入库单号模糊查询
        if (StrUtil.isNotBlank(inboundNo)) {
            wrapper.like(ScmInbound::getInboundNo, inboundNo);
        }
        
        // 按供应商名称模糊查询
        if (StrUtil.isNotBlank(supplierName)) {
            wrapper.like(ScmInbound::getSupplierName, supplierName);
        }
        
        // 按审核状态过滤
        if (StrUtil.isNotBlank(auditStatus)) {
            wrapper.eq(ScmInbound::getAuditStatus, auditStatus);
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(ScmInbound::getCreateTime);
        
        return this.page(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createFromPurchase(Long purchaseId) {
        // 采购管理功能已停用，此方法暂时禁用
        throw new RuntimeException("采购管理功能已停用");
        /*
        ScmPurchase purchase = scmPurchaseService.getPurchaseById(purchaseId);
        if (purchase == null) {
            throw new RuntimeException("采购单不存在");
        }

        ScmInbound inbound = new ScmInbound();
        inbound.setInboundNo(sysCodeRuleService.generateNextCode("INBOUND_NO"));
        inbound.setInboundType("PURCHASE");
        inbound.setWarehouseId(1L); // 默认仓库
        inbound.setInboundTime(LocalDateTime.now());
        inbound.setAuditStatus("PENDING");
        inbound.setCreatorId(1L); // TODO
        inbound.setCreateTime(LocalDateTime.now());
        
        List<ScmInboundDetail> details = new ArrayList<>();
        BigDecimal totalCost = BigDecimal.ZERO;
        
        if (CollUtil.isNotEmpty(purchase.getDetailList())) {
            for (ScmPurchaseDetail pd : purchase.getDetailList()) {
                ScmInboundDetail id = new ScmInboundDetail();
                id.setPurchaseId(pd.getPurchaseId());
                id.setProductName(pd.getProductName());
                id.setProductCode(pd.getProductCode());
                id.setProductSpec(pd.getProductSpec());
                id.setProductImage(pd.getProductImage());
                id.setProductUnit(pd.getProductUnit());
                id.setQuantity(pd.getQuantity());
                id.setCostPrice(pd.getUnitPrice());
                
                BigDecimal amount = pd.getUnitPrice().multiply(new BigDecimal(pd.getQuantity()));
                id.setCostAmount(amount);
                totalCost = totalCost.add(amount);
                
                // 查询当前库存（仅展示）
                PdmProduct product = pdmProductService.getOne(new LambdaQueryWrapper<PdmProduct>().eq(PdmProduct::getProductCode, pd.getProductCode()));
                if (product != null) {
                    id.setStockQuantity(product.getStockQuantity());
                } else {
                    id.setStockQuantity(0);
                }
                
                details.add(id);
            }
        }
        inbound.setTotalCost(totalCost);
        
        this.save(inbound);
        
        if (CollUtil.isNotEmpty(details)) {
            for (ScmInboundDetail detail : details) {
                detail.setInboundId(inbound.getId());
                scmInboundDetailMapper.insert(detail);
            }
        }
        
        // 更新采购单入库进度
        purchase.setInboundProgress("DONE"); // 简化逻辑
        purchase.setPurchaseInbound(1);
        scmPurchaseService.updatePurchase(purchase);
        
        // 记录日志
        sysLogService.log("采购入库", "从采购单创建入库单：采购单号=" + purchase.getPurchaseNo() + ", 入库单号=" + inbound.getInboundNo(), "system");
        
        return inbound.getId();
        */
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean auditInbound(Long id, String status, String detail) {
        ScmInbound inbound = this.getById(id);
        if (inbound == null) {
            throw new RuntimeException("入库单不存在");
        }
        if ("PASSED".equals(inbound.getAuditStatus())) {
            throw new RuntimeException("已审核通过");
        }

        inbound.setAuditStatus(status);
        inbound.setAuditDetail(detail);
        boolean result = this.updateById(inbound);

        if (result && "PASSED".equals(status)) {
            addStock(inbound);
            // 记录日志
            sysLogService.log("采购入库", "审核通过入库单：入库单号=" + inbound.getInboundNo(), "system");
        } else if (result && "REJECTED".equals(status)) {
            // 记录日志
            sysLogService.log("采购入库", "驳回入库单：入库单号=" + inbound.getInboundNo() + ", 原因=" + detail, "system");
        }
        return result;
    }

    @Override
    public ScmInbound getInboundById(Long id) {
        ScmInbound inbound = this.getById(id);
        if (inbound != null) {
            List<ScmInboundDetail> details = scmInboundDetailMapper.selectList(
                    new LambdaQueryWrapper<ScmInboundDetail>().eq(ScmInboundDetail::getInboundId, id)
            );
            inbound.setDetailList(details);
            
            // 如果有采购申请表ID，获取供应商名称
            if (inbound.getPurchaseRequestId() != null) {
                ScmPurchaseRequest request = scmPurchaseRequestService.getById(inbound.getPurchaseRequestId());
                if (request != null) {
                    inbound.setSupplierName(request.getSupplierName());
                }
            }
        }
        return inbound;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createFromPurchaseRequest(Long requestId) {
        return createFromPurchaseRequestWithType(requestId, "PURCHASE");
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTransferFromPurchase(Long requestId) {
        return createFromPurchaseRequestWithType(requestId, "TRANSFER");
    }
    
    private Long createFromPurchaseRequestWithType(Long requestId, String inboundType) {
        // 1. 获取采购申请表
        ScmPurchaseRequest request = scmPurchaseRequestService.getById(requestId);
        if (request == null) {
            throw new RuntimeException("采购申请表不存在");
        }
        
        // 2. 获取采购申请表明细
        List<ScmPurchaseRequestItem> items = scmPurchaseRequestItemMapper.selectList(
            new LambdaQueryWrapper<ScmPurchaseRequestItem>()
                .eq(ScmPurchaseRequestItem::getRequestId, requestId)
        );
        
        if (CollUtil.isEmpty(items)) {
            throw new RuntimeException("采购申请表明细不存在");
        }
        
        // 3. 创建入库单主表
        ScmInbound inbound = new ScmInbound();
        inbound.setInboundNo(sysCodeRuleService.generateNextCode("INBOUND_NO"));
        inbound.setInboundType(inboundType); // 使用传入的入库类型
        inbound.setWarehouseId(1L); // 默认仓库
        inbound.setInboundTime(LocalDateTime.now());
        inbound.setAuditStatus("PENDING");
        inbound.setCreatorId(1L); // TODO: 从当前登录用户获取
        inbound.setCreateTime(LocalDateTime.now());
        // 设置采购单编号
        inbound.setPurchaseOrderNo(request.getPurchaseOrderNo());
        // 设置采购申请表ID
        inbound.setPurchaseRequestId(requestId);
        // 设置供应商名称
        inbound.setSupplierName(request.getSupplierName());
        this.save(inbound);
        
        // 4. 创建入库单明细
        BigDecimal totalCost = BigDecimal.ZERO;
        List<ScmInboundDetail> inboundDetails = new ArrayList<>();
        
        for (ScmPurchaseRequestItem item : items) {
            ScmInboundDetail inboundDetail = new ScmInboundDetail();
            inboundDetail.setInboundId(inbound.getId());
            inboundDetail.setProductCode(item.getProductCode());
            inboundDetail.setProductName(item.getProductName());
            inboundDetail.setProductSpec(item.getProductSpec());
            inboundDetail.setProductUnit("台"); // 假设默认单位为台，后续可从商品表获取
            inboundDetail.setQuantity(item.getPurchaseQuantity());
            inboundDetail.setCostPrice(item.getUnitPrice());
            BigDecimal amount = item.getUnitPrice().multiply(new BigDecimal(item.getPurchaseQuantity()));
            inboundDetail.setCostAmount(amount);
            totalCost = totalCost.add(amount);
            inboundDetails.add(inboundDetail);
        }
        
        // 5. 保存入库单明细
        if (CollUtil.isNotEmpty(inboundDetails)) {
            for (ScmInboundDetail detail : inboundDetails) {
                detail.setInboundId(inbound.getId());
                scmInboundDetailMapper.insert(detail);
            }
        }
        
        // 6. 更新入库单总金额
        inbound.setTotalCost(totalCost);
        this.updateById(inbound);
        
        // 7. 记录日志
        String typeName = "PURCHASE".equals(inboundType) ? "采购入库" : "外转入库";
        sysLogService.log(typeName, "从采购申请表创建" + typeName + "单：采购申请单号=" + request.getRequestNo() + ", 入库单号=" + inbound.getInboundNo(), "system");
        
        return inbound.getId();
    }

    private void addStock(ScmInbound inbound) {
        List<ScmInboundDetail> details = scmInboundDetailMapper.selectList(
                new LambdaQueryWrapper<ScmInboundDetail>().eq(ScmInboundDetail::getInboundId, inbound.getId())
        );

        if (CollUtil.isNotEmpty(details)) {
            for (ScmInboundDetail detail : details) {
                if (StrUtil.isNotBlank(detail.getProductCode())) {
                    PdmProduct product = pdmProductService.getOne(
                            new LambdaQueryWrapper<PdmProduct>().eq(PdmProduct::getProductCode, detail.getProductCode())
                    );
                    if (product != null) {
                        int qty = detail.getQuantity() != null ? detail.getQuantity() : 0;
                        int beforeStock = product.getStockQuantity();
                        product.setStockQuantity(product.getStockQuantity() + qty);
                        product.setAvailableStock(product.getAvailableStock() + qty);
                        product.setTotalStock(product.getTotalStock() + qty);
                        // 更新成本价（加权平均等逻辑，这里暂不处理，保持原样或更新为最新进价）
                        product.setCostPrice(detail.getCostPrice());
                        pdmProductService.updateById(product);
                        
                        // Record Flow
                        ScmStockFlow flow = new ScmStockFlow();
                        flow.setProductCode(product.getProductCode());
                        flow.setWarehouseId(inbound.getWarehouseId());
                        flow.setFlowType("IN");
                        flow.setQuantity(qty);
                        flow.setBeforeStock(beforeStock);
                        flow.setAfterStock(product.getStockQuantity());
                        flow.setRefNo(inbound.getInboundNo());
                        scmStockFlowService.recordFlow(flow);
                        
                        // 记录日志
                        sysLogService.log("库存管理", "商品入库：商品编码=" + product.getProductCode() + ", 商品名称=" + product.getProductName() + ", 入库数量=" + qty + ", 入库单号=" + inbound.getInboundNo(), "system");
                    }
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveInbound(ScmInbound inbound) {
        // 1. 生成入库单号
        inbound.setInboundNo(sysCodeRuleService.generateNextCode("INBOUND_NO"));
        inbound.setAuditStatus("PENDING");
        inbound.setCreatorId(1L); // TODO: 从当前登录用户获取
        inbound.setCreateTime(LocalDateTime.now());
        
        // 2. 保存入库单主表
        this.save(inbound);
        
        // 3. 保存入库单明细
        if (CollUtil.isNotEmpty(inbound.getDetailList())) {
            BigDecimal totalCost = BigDecimal.ZERO;
            for (ScmInboundDetail detail : inbound.getDetailList()) {
                detail.setInboundId(inbound.getId());
                // 计算成本金额
                BigDecimal amount = detail.getCostPrice().multiply(new BigDecimal(detail.getQuantity()));
                detail.setCostAmount(amount);
                totalCost = totalCost.add(amount);
                scmInboundDetailMapper.insert(detail);
            }
            // 4. 更新入库单总金额
            inbound.setTotalCost(totalCost);
            this.updateById(inbound);
        }
        
        // 记录日志
        sysLogService.log("采购入库", "新增入库单：入库单号=" + inbound.getInboundNo(), "system");
        
        return inbound.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateInbound(ScmInbound inbound) {
        // 1. 更新入库单主表
        boolean result = this.updateById(inbound);
        
        // 2. 更新入库单明细
        if (result && CollUtil.isNotEmpty(inbound.getDetailList())) {
            // 计算总金额
            BigDecimal totalCost = BigDecimal.ZERO;
            
            for (ScmInboundDetail detail : inbound.getDetailList()) {
                detail.setInboundId(inbound.getId());
                // 计算成本金额
                BigDecimal amount = detail.getCostPrice().multiply(new BigDecimal(detail.getQuantity()));
                detail.setCostAmount(amount);
                totalCost = totalCost.add(amount);
                
                if (detail.getId() == null) {
                    // 新增明细
                    scmInboundDetailMapper.insert(detail);
                } else {
                    // 更新明细
                    scmInboundDetailMapper.updateById(detail);
                }
            }
            
            // 3. 更新入库单总金额
            inbound.setTotalCost(totalCost);
            this.updateById(inbound);
        }
        
        // 记录日志
        sysLogService.log("采购入库", "更新入库单：入库单号=" + inbound.getInboundNo(), "system");
        
        return result;
    }

    @Override
    public List<ScmInboundDetail> getInboundDetails(Long id) {
        return scmInboundDetailMapper.selectList(
                new LambdaQueryWrapper<ScmInboundDetail>().eq(ScmInboundDetail::getInboundId, id)
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addInboundDetail(ScmInboundDetail detail) {
        // 计算成本金额
        BigDecimal amount = detail.getCostPrice().multiply(new BigDecimal(detail.getQuantity()));
        detail.setCostAmount(amount);
        
        // 保存明细
        scmInboundDetailMapper.insert(detail);
        
        // 更新入库单总金额
        updateInboundTotalCost(detail.getInboundId());
        
        // 记录日志
        sysLogService.log("采购入库", "新增入库单明细：入库单号=" + (detail.getInboundId() != null ? this.getById(detail.getInboundId()).getInboundNo() : "") + ", 商品名称=" + detail.getProductName(), "system");
        
        return detail.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateInboundDetail(ScmInboundDetail detail) {
        // 计算成本金额
        BigDecimal amount = detail.getCostPrice().multiply(new BigDecimal(detail.getQuantity()));
        detail.setCostAmount(amount);
        
        // 更新明细
        boolean result = scmInboundDetailMapper.updateById(detail) > 0;
        
        // 更新入库单总金额
        updateInboundTotalCost(detail.getInboundId());
        
        // 记录日志
        sysLogService.log("采购入库", "更新入库单明细：入库单号=" + (detail.getInboundId() != null ? this.getById(detail.getInboundId()).getInboundNo() : "") + ", 商品名称=" + detail.getProductName(), "system");
        
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteInboundDetail(Long id) {
        // 获取明细信息
        ScmInboundDetail detail = scmInboundDetailMapper.selectById(id);
        if (detail == null) {
            return true;
        }
        
        // 删除明细
        boolean result = scmInboundDetailMapper.deleteById(id) > 0;
        
        // 更新入库单总金额
        updateInboundTotalCost(detail.getInboundId());
        
        // 记录日志
        sysLogService.log("采购入库", "删除入库单明细：入库单号=" + (detail.getInboundId() != null ? this.getById(detail.getInboundId()).getInboundNo() : "") + ", 商品名称=" + detail.getProductName(), "system");
        
        return result;
    }
    
    /**
     * 更新入库单总金额
     */
    private void updateInboundTotalCost(Long inboundId) {
        if (inboundId == null) {
            return;
        }
        
        // 查询所有明细
        List<ScmInboundDetail> details = scmInboundDetailMapper.selectList(
                new LambdaQueryWrapper<ScmInboundDetail>().eq(ScmInboundDetail::getInboundId, inboundId)
        );
        
        // 计算总金额
        BigDecimal totalCost = BigDecimal.ZERO;
        for (ScmInboundDetail detail : details) {
            totalCost = totalCost.add(detail.getCostAmount());
        }
        
        // 更新入库单总金额
        ScmInbound inbound = new ScmInbound();
        inbound.setId(inboundId);
        inbound.setTotalCost(totalCost);
        this.updateById(inbound);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createFromSalesReturn(Long outboundId) {
        // 1. 获取发货单
        ScmOutbound outbound = scmOutboundMapper.selectById(outboundId);
        if (outbound == null) {
            throw new RuntimeException("发货单不存在");
        }
        
        // 2. 获取发货单明细
        List<ScmOutboundDetail> details = scmOutboundDetailMapper.selectList(
            new LambdaQueryWrapper<ScmOutboundDetail>()
                .eq(ScmOutboundDetail::getOutStockId, outboundId)
        );
        
        if (CollUtil.isEmpty(details)) {
            throw new RuntimeException("发货单明细不存在");
        }
        
        // 3. 创建入库单主表
        ScmInbound inbound = new ScmInbound();
        inbound.setInboundNo(sysCodeRuleService.generateNextCode("INBOUND_NO"));
        inbound.setInboundType("SALES_RETURN");
        inbound.setWarehouseId(1L); // 默认仓库
        inbound.setInboundTime(LocalDateTime.now());
        inbound.setAuditStatus("PENDING");
        inbound.setCreatorId(1L); // TODO: 从当前登录用户获取
        inbound.setCreateTime(LocalDateTime.now());
        inbound.setRemark("销售退货，来源发货单：" + outbound.getOutStockNo());
        this.save(inbound);
        
        // 4. 创建入库单明细
        BigDecimal totalCost = BigDecimal.ZERO;
        List<ScmInboundDetail> inboundDetails = new ArrayList<>();
        
        for (ScmOutboundDetail detail : details) {
            ScmInboundDetail inboundDetail = new ScmInboundDetail();
            inboundDetail.setInboundId(inbound.getId());
            inboundDetail.setProductCode(detail.getProductCode());
            inboundDetail.setProductName(detail.getProductName());
            inboundDetail.setProductSpec(detail.getProductSpec());
            inboundDetail.setProductUnit(detail.getProductUnit());
            inboundDetail.setQuantity(detail.getOutQuantity());
            // 销售退货使用成本价（从商品表获取）
            PdmProduct product = pdmProductService.getOne(
                new LambdaQueryWrapper<PdmProduct>().eq(PdmProduct::getProductCode, detail.getProductCode())
            );
            if (product != null) {
                inboundDetail.setCostPrice(product.getCostPrice());
                BigDecimal amount = product.getCostPrice().multiply(new BigDecimal(detail.getOutQuantity()));
                inboundDetail.setCostAmount(amount);
                totalCost = totalCost.add(amount);
            }
            inboundDetails.add(inboundDetail);
        }
        
        // 5. 保存入库单明细
        if (CollUtil.isNotEmpty(inboundDetails)) {
            for (ScmInboundDetail detail : inboundDetails) {
                detail.setInboundId(inbound.getId());
                scmInboundDetailMapper.insert(detail);
            }
        }
        
        // 6. 更新入库单总金额
        inbound.setTotalCost(totalCost);
        this.updateById(inbound);
        
        // 7. 记录日志
        sysLogService.log("销售退货", "从发货单创建退货入库单：发货单号=" + outbound.getOutStockNo() + ", 入库单号=" + inbound.getInboundNo(), "system");
        
        return inbound.getId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTransferInbound(ScmInbound inbound) {
        // 1. 生成入库单号
        inbound.setInboundNo(sysCodeRuleService.generateNextCode("INBOUND_NO"));
        inbound.setInboundType("TRANSFER");
        inbound.setAuditStatus("PENDING");
        inbound.setCreatorId(1L); // TODO: 从当前登录用户获取
        inbound.setCreateTime(LocalDateTime.now());
        
        // 2. 保存入库单主表
        this.save(inbound);
        
        // 3. 保存入库单明细
        if (CollUtil.isNotEmpty(inbound.getDetailList())) {
            BigDecimal totalCost = BigDecimal.ZERO;
            for (ScmInboundDetail detail : inbound.getDetailList()) {
                detail.setInboundId(inbound.getId());
                // 计算成本金额
                BigDecimal amount = detail.getCostPrice().multiply(new BigDecimal(detail.getQuantity()));
                detail.setCostAmount(amount);
                totalCost = totalCost.add(amount);
                scmInboundDetailMapper.insert(detail);
            }
            // 4. 更新入库单总金额
            inbound.setTotalCost(totalCost);
            this.updateById(inbound);
        }
        
        // 记录日志
        sysLogService.log("外转入库", "新增外转入库单：入库单号=" + inbound.getInboundNo(), "system");
        
        return inbound.getId();
    }
}
