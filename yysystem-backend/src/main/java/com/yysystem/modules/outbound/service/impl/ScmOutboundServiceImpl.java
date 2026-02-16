package com.yysystem.modules.outbound.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yysystem.modules.contract.entity.CrmContract;
import com.yysystem.modules.contract.entity.CrmContractDetail;
import com.yysystem.modules.contract.service.CrmContractService;
import com.yysystem.modules.outbound.entity.ScmOutbound;
import com.yysystem.modules.outbound.entity.ScmOutboundDetail;
import com.yysystem.modules.outbound.mapper.ScmOutboundDetailMapper;
import com.yysystem.modules.outbound.mapper.ScmOutboundMapper;
import com.yysystem.modules.outbound.service.ScmOutboundService;
import com.yysystem.modules.product.entity.PdmProduct;
import com.yysystem.modules.product.service.PdmProductService;
import com.yysystem.modules.system.service.SysCodeRuleService;
import com.yysystem.modules.inventory.entity.ScmStockFlow;
import com.yysystem.modules.inventory.service.ScmStockFlowService;
import com.yysystem.modules.scm.stock.entity.ScmStock;
import com.yysystem.modules.scm.stock.mapper.ScmStockMapper;
import com.yysystem.modules.warehouse.entity.PdmWarehouse;
import com.yysystem.modules.warehouse.mapper.PdmWarehouseMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScmOutboundServiceImpl extends ServiceImpl<ScmOutboundMapper, ScmOutbound> implements ScmOutboundService {

    @Autowired
    private ScmOutboundMapper scmOutboundMapper;

    @Autowired
    private ScmOutboundDetailMapper scmOutboundDetailMapper;

    @Autowired
    private CrmContractService crmContractService;

    @Autowired
    private SysCodeRuleService sysCodeRuleService;

    @Autowired
    private PdmProductService pdmProductService;

    @Autowired
    private ScmStockFlowService scmStockFlowService;

    @Autowired
    private ScmStockMapper scmStockMapper;

    @Autowired
    private PdmWarehouseMapper pdmWarehouseMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean auditOutbound(Long id, String status, String detail) {
        ScmOutbound outbound = this.getById(id);
        if (outbound == null) {
            throw new RuntimeException("出库单不存在");
        }
        outbound.setAuditStatus(status);
        outbound.setAuditDetail(detail);

        // 如果审核通过，减少库存并记录流水
        if ("PASSED".equals(status)) {
            // 根据出库分类确定仓库
            Long warehouseId = getWarehouseIdByCategory(outbound.getOutboundCategory());

            // 查询出库单明细
            List<ScmOutboundDetail> detailList = scmOutboundDetailMapper.selectList(
                new LambdaQueryWrapper<ScmOutboundDetail>()
                    .eq(ScmOutboundDetail::getOutStockId, id)
            );

            // 遍历明细，减少库存并记录流水
            for (ScmOutboundDetail outboundDetail : detailList) {
                String productCode = outboundDetail.getProductCode();
                int quantity = outboundDetail.getOutQuantity() != null ? outboundDetail.getOutQuantity() : 1;

                // 查询商品库存
                PdmProduct product = pdmProductService.getOne(
                    new LambdaQueryWrapper<PdmProduct>()
                        .eq(PdmProduct::getProductCode, productCode)
                );

                if (product != null && product.getStockQuantity() != null && product.getStockQuantity() >= quantity) {
                    // 记录变动前库存
                    int beforeStock = product.getStockQuantity();
                    Long actualWarehouseId = product.getWarehouseId();

                    // 减少商品库存
                    product.setStockQuantity(product.getStockQuantity() - quantity);
                    product.setAvailableStock(product.getAvailableStock() - quantity);
                    product.setTotalStock(product.getTotalStock() - quantity);
                    pdmProductService.updateById(product);

                    // 记录变动后库存
                    int afterStock = beforeStock - quantity;

                    // 记录库存流水
                    ScmStockFlow stockFlow = new ScmStockFlow();
                    stockFlow.setProductCode(productCode);
                    stockFlow.setWarehouseId(actualWarehouseId);
                    stockFlow.setFlowType("OUT"); // 出库
                    stockFlow.setQuantity(quantity);
                    stockFlow.setBeforeStock(beforeStock);
                    stockFlow.setAfterStock(afterStock);
                    stockFlow.setRefNo(outbound.getOutStockNo());
                    stockFlow.setCreateTime(LocalDateTime.now());

                    // 保存流水记录
                    scmStockFlowService.recordFlow(stockFlow);
                } else {
                    // 库存不足或不存在，抛出异常
                    throw new RuntimeException("商品[" + outboundDetail.getProductName() + "]在指定仓库库存不足或不存在");
                }
            }
        }

        return this.updateById(outbound);
    }

    /**
     * 根据出库分类获取仓库ID
     * 销售出库 -> 合肥仓库
     * 外转出库 -> 外转仓库
     * 采购退货 -> 合肥仓库（或指定的退货仓库）
     */
    private Long getWarehouseIdByCategory(String outboundCategory) {
        if (StrUtil.isBlank(outboundCategory)) {
            // 默认使用合肥仓库
            return getWarehouseIdByName("合肥仓库");
        }

        switch (outboundCategory) {
            case "外转":
            case "外转出库":
            case "OUTER_TRANSFER":
                return getWarehouseIdByName("外转仓库");
            case "采购退货":
            case "PURCHASE_RETURN":
                // 采购退货默认从合肥仓库退货
                return getWarehouseIdByName("合肥仓库");
            case "销售":
            case "销售出库":
            case "SALES":
            default:
                return getWarehouseIdByName("合肥仓库");
        }
    }

    /**
     * 根据仓库名称获取仓库ID
     */
    private Long getWarehouseIdByName(String warehouseName) {
        PdmWarehouse warehouse = pdmWarehouseMapper.selectOne(
            new LambdaQueryWrapper<PdmWarehouse>()
                .eq(PdmWarehouse::getWarehouseName, warehouseName)
                .last("limit 1")
        );
        if (warehouse != null) {
            return warehouse.getId();
        }
        // 如果找不到指定仓库，返回第一个仓库
        List<PdmWarehouse> warehouses = pdmWarehouseMapper.selectList(
            new LambdaQueryWrapper<PdmWarehouse>().last("limit 1")
        );
        return CollUtil.isNotEmpty(warehouses) ? warehouses.get(0).getId() : null;
    }

    /**
     * 根据商品编码获取商品单位
     */
    private String getProductUnit(String productCode) {
        if (StrUtil.isNotBlank(productCode)) {
            PdmProduct product = pdmProductService.getOne(
                    new LambdaQueryWrapper<PdmProduct>().eq(PdmProduct::getProductCode, productCode)
            );
            if (product != null && StrUtil.isNotBlank(product.getProductUnit())) {
                return product.getProductUnit();
            }
        }
        return "件";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createFromContract(Long contractId) {
        CrmContract contract = crmContractService.getContractById(contractId);
        if (contract == null) {
            throw new RuntimeException("合同不存在");
        }

        ScmOutbound outbound = new ScmOutbound();
        outbound.setOutStockNo(sysCodeRuleService.generateNextCode("OUT_STOCK_NO"));
        outbound.setContractId(contract.getId());
        outbound.setCustomerId(contract.getCustomerId());
        outbound.setLinkmanId(contract.getLinkmanId());
        outbound.setCustomerAddress(contract.getAddress());
        outbound.setSalesmanId(contract.getSalesmanId());
        outbound.setOutStockType("SALES"); // 销售出库
        outbound.setOutStockDate(LocalDateTime.now());
        // 只有预定发货时间为空时，才使用合同交货日期
        if (contract.getDeliveryDate() != null && outbound.getScheduledShipTime() == null) {
            outbound.setScheduledShipTime(contract.getDeliveryDate().atStartOfDay());
        }
        outbound.setAuditStatus("PENDING");
        outbound.setCreateTime(LocalDateTime.now());
        outbound.setCreatorId(1L); // TODO

        // 复制明细
        List<ScmOutboundDetail> outboundDetails = new ArrayList<>();
        if (CollUtil.isNotEmpty(contract.getDetailList())) {
            for (CrmContractDetail contractDetail : contract.getDetailList()) {
                int qty = contractDetail.getSalesQuantity() != null ? contractDetail.getSalesQuantity() : 0;
                // 只统计非取消/非驳回状态的出库单明细
                Long shipped = scmOutboundDetailMapper.selectCount(
                        new LambdaQueryWrapper<ScmOutboundDetail>()
                                .eq(ScmOutboundDetail::getContractDetailId, contractDetail.getId())
                                .inSql(ScmOutboundDetail::getOutStockId,
                                        "SELECT id FROM scm_outbound WHERE audit_status NOT IN ('CANCELLED', 'REJECTED')")
                );
                int remain = Math.max(0, qty - (shipped == null ? 0 : shipped.intValue()));
                if (remain <= 0) {
                    continue;
                }

                // 判断商品分类
                String productCategory = "";
                if (StrUtil.isNotBlank(contractDetail.getProductCode())) {
                    PdmProduct product = pdmProductService.getOne(
                            new LambdaQueryWrapper<PdmProduct>().eq(PdmProduct::getProductCode, contractDetail.getProductCode())
                    );
                    if (product != null && product.getProductCategory() != null) {
                        productCategory = product.getProductCategory();
                    }
                }

                // 判断是否需要拆分（指定类别：自产商品、外购商品、自产软件、外购软件）
                boolean needSplit = productCategory.equals("自产商品") || productCategory.equals("外购商品")
                                 || productCategory.equals("自产软件") || productCategory.equals("外购软件");

                if (needSplit) {
                    // 需要拆分的类别：按数量生成多条记录，每条1个，生成二维码
                    for (int i = 0; i < remain; i++) {
                        ScmOutboundDetail outboundDetail = new ScmOutboundDetail();
                        outboundDetail.setContractDetailId(contractDetail.getId());
                        outboundDetail.setProductName(contractDetail.getProductName());
                        outboundDetail.setProductCode(contractDetail.getProductCode());
                        outboundDetail.setProductSpec(contractDetail.getProductSpec());
                        outboundDetail.setProductImage(contractDetail.getProductImage());
                        outboundDetail.setParameters(contractDetail.getCustomRemark());
                        outboundDetail.setRemark(contractDetail.getProductRemark());
                        outboundDetail.setProductUnit(getProductUnit(contractDetail.getProductCode()));
                        outboundDetail.setSalesPrice(contractDetail.getSalesPrice());
                        outboundDetail.setAmount(contractDetail.getSalesPrice() != null ? contractDetail.getSalesPrice() : BigDecimal.ZERO);
                        outboundDetail.setOutQuantity(1);

                        // 生成二维码
                        String productCode = contractDetail.getProductCode() != null ? contractDetail.getProductCode() : "PRODUCT";
                        String dateStr = new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date());
                        String seqNum = String.format("%02d", i + 1);
                        String qr = productCode + dateStr + seqNum;
                        outboundDetail.setProductQrCode(qr);

                        outboundDetails.add(outboundDetail);
                    }
                } else {
                    // 不需要拆分的类别：只生成一条记录，数量为剩余数量，不生成二维码
                    ScmOutboundDetail outboundDetail = new ScmOutboundDetail();
                    outboundDetail.setContractDetailId(contractDetail.getId());
                    outboundDetail.setProductName(contractDetail.getProductName());
                    outboundDetail.setProductCode(contractDetail.getProductCode());
                    outboundDetail.setProductSpec(contractDetail.getProductSpec());
                    outboundDetail.setProductImage(contractDetail.getProductImage());
                    outboundDetail.setParameters(contractDetail.getCustomRemark());
                    outboundDetail.setRemark(contractDetail.getProductRemark());
                    outboundDetail.setProductUnit(getProductUnit(contractDetail.getProductCode()));
                    outboundDetail.setSalesPrice(contractDetail.getSalesPrice());
                    outboundDetail.setAmount(contractDetail.getSalesPrice() != null ? contractDetail.getSalesPrice().multiply(new BigDecimal(remain)) : BigDecimal.ZERO);
                    outboundDetail.setOutQuantity(remain);
                    // 不生成二维码
                    outboundDetail.setProductQrCode("");

                    outboundDetails.add(outboundDetail);
                }
            }
        }
        // 检查是否还有未出库的商品
        if (CollUtil.isEmpty(outboundDetails)) {
            throw new RuntimeException("该合同所有商品已全部出库，无法生成出库单");
        }

        // 计算销售总额（按实际出库明细单价*数量）
        BigDecimal totalSales = BigDecimal.ZERO;
        for (ScmOutboundDetail d : outboundDetails) {
            if (d.getSalesPrice() != null) {
                totalSales = totalSales.add(d.getSalesPrice().multiply(new BigDecimal(d.getOutQuantity() != null ? d.getOutQuantity() : 1)));
            }
        }
        outbound.setTotalSales(totalSales);

        scmOutboundMapper.insert(outbound);

        // 批量插入明细
        if (CollUtil.isNotEmpty(outboundDetails)) {
            for (ScmOutboundDetail detail : outboundDetails) {
                detail.setOutStockId(outbound.getId());
                scmOutboundDetailMapper.insert(detail);
            }
        }

        return outbound.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unlockOutbound(Long id) {
        ScmOutbound outbound = this.getById(id);
        if (outbound == null) {
            throw new RuntimeException("出库单不存在");
        }

        // 只有已审核通过的单据才能解锁
        if (!"PASSED".equals(outbound.getAuditStatus())) {
            throw new RuntimeException("只有已审核通过的单据才能解锁");
        }

        // 查询出库单明细
        List<ScmOutboundDetail> detailList = scmOutboundDetailMapper.selectList(
            new LambdaQueryWrapper<ScmOutboundDetail>()
                .eq(ScmOutboundDetail::getOutStockId, id)
        );

        // 恢复库存（与删除时的逻辑相同）
        for (ScmOutboundDetail outboundDetail : detailList) {
            String productCode = outboundDetail.getProductCode();
            int quantity = outboundDetail.getOutQuantity() != null ? outboundDetail.getOutQuantity() : 1;

            // 查询商品并恢复库存
            PdmProduct product = pdmProductService.getOne(
                new LambdaQueryWrapper<PdmProduct>()
                    .eq(PdmProduct::getProductCode, productCode)
            );

            if (product != null) {
                product.setStockQuantity(product.getStockQuantity() + quantity);
                product.setAvailableStock(product.getAvailableStock() + quantity);
                product.setTotalStock(product.getTotalStock() + quantity);
                pdmProductService.updateById(product);
            }

            // 记录库存流水（解锁回滚）
            ScmStockFlow stockFlow = new ScmStockFlow();
            stockFlow.setProductCode(productCode);
            stockFlow.setWarehouseId(product != null ? product.getWarehouseId() : null);
            stockFlow.setFlowType("IN"); // 入库（回滚）
            stockFlow.setQuantity(quantity);
            stockFlow.setBeforeStock(product != null ? product.getStockQuantity() - quantity : 0);
            stockFlow.setAfterStock(product != null ? product.getStockQuantity() : quantity);
            stockFlow.setRefNo(outbound.getOutStockNo());
            stockFlow.setRemark("解锁回滚");
            stockFlow.setCreateTime(LocalDateTime.now());
            scmStockFlowService.recordFlow(stockFlow);
        }

        // 将状态改为待审核（PENDING），允许删除
        outbound.setAuditStatus("PENDING");
        outbound.setAuditDetail("已解锁，允许删除");
        return this.updateById(outbound);
    }
}
