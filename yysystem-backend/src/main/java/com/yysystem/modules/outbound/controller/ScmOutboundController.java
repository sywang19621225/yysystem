package com.yysystem.modules.outbound.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yysystem.common.result.Result;
import com.yysystem.modules.outbound.entity.ScmOutbound;
import com.yysystem.modules.outbound.service.ScmOutboundService;
import com.yysystem.modules.outbound.entity.ScmOutboundDetail;
import com.yysystem.modules.outbound.mapper.ScmOutboundDetailMapper;
import com.yysystem.modules.system.service.SysCodeRuleService;
import com.yysystem.modules.system.service.SysUserService;
import com.yysystem.modules.system.entity.SysUser;
import com.yysystem.modules.contract.service.CrmContractService;
import com.yysystem.modules.contract.entity.CrmContract;
import com.yysystem.modules.contract.entity.CrmContractDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import com.yysystem.modules.product.service.PdmProductService;
import com.yysystem.modules.product.entity.PdmProduct;
import com.yysystem.modules.contract.mapper.CrmContractDetailMapper;
import com.yysystem.modules.crm.mapper.CrmCustomerMapper;
import com.yysystem.modules.purchase.mapper.ScmPurchaseContractMapper;
import com.yysystem.modules.purchase.mapper.ScmPurchaseRequestMapper;
import com.yysystem.modules.supplier.mapper.ScmSupplierMapper;
import com.yysystem.modules.inbound.mapper.ScmInboundDetailMapper;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/scm/outbound")
public class ScmOutboundController {
    private static final Logger log = LoggerFactory.getLogger(ScmOutboundController.class);

    @Autowired
    private ScmOutboundService scmOutboundService;
    @Autowired
    private ScmOutboundDetailMapper scmOutboundDetailMapper;
    @Autowired
    private SysCodeRuleService sysCodeRuleService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private CrmContractService crmContractService;
    @Autowired
    private PdmProductService pdmProductService;
    @Autowired
    private CrmContractDetailMapper crmContractDetailMapper;
    @Autowired
    private CrmCustomerMapper crmCustomerMapper;
    @Autowired
    private ScmPurchaseContractMapper scmPurchaseContractMapper;
    @Autowired
    private ScmSupplierMapper scmSupplierMapper;
    @Autowired
    private ScmInboundDetailMapper scmInboundDetailMapper;
    @Autowired
    private ScmPurchaseRequestMapper scmPurchaseRequestMapper;

    public static class ImportContractItemsRequest {
        private Long contractId;
        private List<ImportItem> items;
        public Long getContractId() { return contractId; }
        public void setContractId(Long contractId) { this.contractId = contractId; }
        public List<ImportItem> getItems() { return items; }
        public void setItems(List<ImportItem> items) { this.items = items; }
    }
    public static class ImportItem {
        private Long contractDetailId;
        private Integer quantity;
        public Long getContractDetailId() { return contractDetailId; }
        public void setContractDetailId(Long contractDetailId) { this.contractDetailId = contractDetailId; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }

    @PostMapping("/create-from-contract/{contractId}")
    public Result<Long> createFromContract(@PathVariable Long contractId) {
        return Result.success(scmOutboundService.createFromContract(contractId));
    }

    @PutMapping("/{id}/audit")
    public Result<Boolean> audit(@PathVariable Long id, @RequestParam String status, @RequestParam(required = false) String detail) {
        return Result.success(scmOutboundService.auditOutbound(id, status, detail));
    }

    /**
     * 解锁出库单（将已审核状态改为待审核，允许删除）
     */
    @PutMapping("/{id}/unlock")
    public Result<Boolean> unlock(@PathVariable Long id) {
        return Result.success(scmOutboundService.unlockOutbound(id));
    }

    @GetMapping("/{id}")
    public Result<ScmOutbound> getById(@PathVariable Long id) {
        ScmOutbound ob = scmOutboundService.getById(id);
        if (ob != null) {
            java.util.List<ScmOutboundDetail> details = scmOutboundDetailMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ScmOutboundDetail>()
                    .eq(ScmOutboundDetail::getOutStockId, id)
            );
            // 为每个明细添加入库数量和已退货数量
            for (ScmOutboundDetail detail : details) {
                if (cn.hutool.core.util.StrUtil.isNotBlank(detail.getProductCode())) {
                    // 查询该商品的总入库数量（已审核的入库单）
                    long totalInboundQty = 0L;
                    java.util.List<java.util.Map<String, Object>> inboundRows = scmInboundDetailMapper.selectMaps(
                        new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.yysystem.modules.inbound.entity.ScmInboundDetail>()
                            .select("COALESCE(SUM(quantity),0) AS total")
                            .eq("product_code", detail.getProductCode())
                            .apply("inbound_id IN (SELECT id FROM scm_inbound WHERE audit_status = 'PASSED')")
                    );
                    if (inboundRows != null && !inboundRows.isEmpty()) {
                        Object v = inboundRows.get(0).get("total");
                        if (v != null) totalInboundQty = Long.parseLong(String.valueOf(v));
                    }
                    // 查询该商品的已退货数量（已审核的退货单，不包括当前单据）
                    long totalReturnedQty = 0L;
                    java.util.List<java.util.Map<String, Object>> returnRows = scmOutboundDetailMapper.selectMaps(
                        new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ScmOutboundDetail>()
                            .select("COALESCE(SUM(out_quantity),0) AS total")
                            .eq("product_code", detail.getProductCode())
                            .ne("out_stock_id", id) // 排除当前单据
                            .apply("out_stock_id IN (SELECT id FROM scm_outbound WHERE out_stock_type = 'PURCHASE_RETURN' AND audit_status = 'PASSED')")
                    );
                    if (returnRows != null && !returnRows.isEmpty()) {
                        Object v = returnRows.get(0).get("total");
                        if (v != null) totalReturnedQty = Long.parseLong(String.valueOf(v));
                    }
                    // 设置扩展字段（使用remark字段临时存储，前端需要特殊处理）
                    detail.setRemark(String.format("INBOUND:%d,RETURNED:%d", totalInboundQty, totalReturnedQty));
                }
            }
            ob.setDetailList(details);
        }
        return Result.success(ob);
    }

    @GetMapping("/list")
    public Result<Page<ScmOutbound>> list(@RequestParam(defaultValue = "1") Integer current,
                                          @RequestParam(defaultValue = "10") Integer size,
                                          @RequestParam(required = false) String outStockNo,
                                          @RequestParam(required = false) String outStockType,
                                          @RequestParam(required = false) String supplierName,
                                          @RequestParam(required = false) String auditStatus) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ScmOutbound> wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();

        if (cn.hutool.core.util.StrUtil.isNotBlank(outStockNo)) {
            wrapper.like(ScmOutbound::getOutStockNo, outStockNo);
        }
        if (cn.hutool.core.util.StrUtil.isNotBlank(outStockType)) {
            wrapper.eq(ScmOutbound::getOutStockType, outStockType);
        }
        if (cn.hutool.core.util.StrUtil.isNotBlank(auditStatus)) {
            wrapper.eq(ScmOutbound::getAuditStatus, auditStatus);
        }

        wrapper.orderByDesc(ScmOutbound::getCreateTime);
        Page<ScmOutbound> page = scmOutboundService.page(new Page<>(current, size), wrapper);

        // 关联查询客户名称、供应商名称和合同名称
        if (page.getRecords() != null) {
            java.util.Iterator<ScmOutbound> iterator = page.getRecords().iterator();
            while (iterator.hasNext()) {
                ScmOutbound outbound = iterator.next();
                if (outbound.getCustomerId() != null) {
                    com.yysystem.modules.crm.entity.CrmCustomer customer =
                        crmCustomerMapper.selectById(outbound.getCustomerId());
                    if (customer != null) {
                        outbound.setCustomerName(customer.getCustomerName());
                    }
                }
                // 如果是采购退货，查询供应商名称和采购申请号
                if ("PURCHASE_RETURN".equals(outbound.getOutStockType())) {
                    if (outbound.getSupplierId() != null) {
                        com.yysystem.modules.supplier.entity.ScmSupplier supplier =
                            scmSupplierMapper.selectById(outbound.getSupplierId());
                        if (supplier != null) {
                            outbound.setSupplierName(supplier.getSupplierName());
                        }
                    }
                    // 查询采购申请号
                    if (outbound.getPurchaseContractId() != null) {
                        com.yysystem.modules.purchase.entity.ScmPurchaseRequest purchaseRequest =
                            scmPurchaseRequestMapper.selectById(outbound.getPurchaseContractId());
                        if (purchaseRequest != null) {
                            outbound.setPurchaseContractNo(purchaseRequest.getRequestNo());
                        }
                    }
                }
                // 查询合同名称
                if (outbound.getContractId() != null) {
                    com.yysystem.modules.contract.entity.CrmContract contract =
                        crmContractService.getById(outbound.getContractId());
                    if (contract != null) {
                        outbound.setContractName(contract.getContractName());
                    }
                }

                // 根据供应商名称过滤
                if (cn.hutool.core.util.StrUtil.isNotBlank(supplierName)) {
                    String actualSupplierName = outbound.getSupplierName();
                    if (actualSupplierName == null || !actualSupplierName.contains(supplierName)) {
                        iterator.remove();
                    }
                }
            }
        }

        return Result.success(page);
    }

    @Transactional(rollbackFor = Exception.class)
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        // 查询出库单信息
        ScmOutbound outbound = scmOutboundService.getById(id);
        if (outbound == null) {
            return Result.error("出库单不存在");
        }

        // 如果已审核通过，需要回滚库存
        if ("PASSED".equals(outbound.getAuditStatus())) {
            // 查询出库单明细
            List<ScmOutboundDetail> details = scmOutboundDetailMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ScmOutboundDetail>()
                    .eq(ScmOutboundDetail::getOutStockId, id)
            );

            // 回滚库存
            for (ScmOutboundDetail detail : details) {
                String productCode = detail.getProductCode();
                int quantity = detail.getOutQuantity() != null ? detail.getOutQuantity() : 1;

                // 查询商品并回滚库存
                PdmProduct product = pdmProductService.getOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PdmProduct>()
                        .eq(PdmProduct::getProductCode, productCode)
                );

                if (product != null) {
                    product.setStockQuantity(product.getStockQuantity() + quantity);
                    product.setAvailableStock(product.getAvailableStock() + quantity);
                    product.setTotalStock(product.getTotalStock() + quantity);
                    pdmProductService.updateById(product);
                }
            }
        }

        // 删除明细和主表
        scmOutboundDetailMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ScmOutboundDetail>().eq(ScmOutboundDetail::getOutStockId, id));
        return Result.success(scmOutboundService.removeById(id));
    }

    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody ScmOutbound outbound) {
        outbound.setId(id);

        // 获取原出库单信息
        ScmOutbound existingOutbound = scmOutboundService.getById(id);
        if (existingOutbound == null) {
            return Result.error("出库单不存在");
        }

        // 采购退货时校验退货数量
        if ("PURCHASE_RETURN".equals(existingOutbound.getOutStockType()) && outbound.getDetailList() != null) {
            for (ScmOutboundDetail detail : outbound.getDetailList()) {
                if (cn.hutool.core.util.StrUtil.isNotBlank(detail.getProductCode()) && existingOutbound.getPurchaseContractId() != null) {
                    // 查询采购数量
                    com.yysystem.modules.purchase.entity.ScmPurchaseRequestItem purchaseItem =
                        scmPurchaseRequestItemMapper.selectOne(
                            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.yysystem.modules.purchase.entity.ScmPurchaseRequestItem>()
                                .eq(com.yysystem.modules.purchase.entity.ScmPurchaseRequestItem::getRequestId, existingOutbound.getPurchaseContractId())
                                .eq(com.yysystem.modules.purchase.entity.ScmPurchaseRequestItem::getProductCode, detail.getProductCode())
                                .last("limit 1")
                        );
                    if (purchaseItem != null && purchaseItem.getPurchaseQuantity() != null) {
                        int purchaseQty = purchaseItem.getPurchaseQuantity();
                        int returnQty = detail.getOutQuantity() != null ? detail.getOutQuantity() : 0;
                        if (returnQty > purchaseQty) {
                            return Result.error("商品【" + detail.getProductName() + "】退货数量(" + returnQty + ")不能超过采购数量(" + purchaseQty + ")");
                        }
                    }
                }
            }
        }

        boolean success = scmOutboundService.updateById(outbound);

        // 更新明细
        if (success && outbound.getDetailList() != null) {
            for (ScmOutboundDetail detail : outbound.getDetailList()) {
                if (detail.getId() != null) {
                    // 更新现有明细
                    scmOutboundDetailMapper.updateById(detail);
                } else {
                    // 新增明细
                    detail.setOutStockId(id);
                    detail.setCreateTime(java.time.LocalDateTime.now());
                    scmOutboundDetailMapper.insert(detail);
                }
            }
        }

        return Result.success(success);
    }

    @GetMapping("/{id}/details")
    public Result<java.util.List<ScmOutboundDetail>> details(@PathVariable Long id) {
        return Result.success(scmOutboundDetailMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ScmOutboundDetail>().eq(ScmOutboundDetail::getOutStockId, id)));
    }

    @PutMapping("/detail/{detailId}")
    public Result<Boolean> updateDetail(@PathVariable Long detailId, @RequestBody ScmOutboundDetail detail) {
        detail.setId(detailId);
        // 校验最大数量不得超过合同订单数量（单次出库校验）
        if (detail.getContractDetailId() != null && detail.getOutQuantity() != null) {
            CrmContractDetail cd = crmContractDetailMapper.selectById(detail.getContractDetailId());
            if (cd != null && cd.getSalesQuantity() != null && detail.getOutQuantity() > cd.getSalesQuantity()) {
                return Result.error("本次发货数量不能超过合同订单数量");
            }
        }
        if (detail.getSalesPrice() != null && detail.getOutQuantity() != null) {
            detail.setAmount(detail.getSalesPrice().multiply(new java.math.BigDecimal(detail.getOutQuantity())));
        }
        return Result.success(scmOutboundDetailMapper.updateById(detail) > 0);
    }

    @DeleteMapping("/detail/{detailId}")
    public Result<Boolean> deleteDetail(@PathVariable Long detailId) {
        return Result.success(scmOutboundDetailMapper.deleteById(detailId) > 0);
    }

    @PostMapping
    public Result<Long> create(@RequestBody(required = false) ScmOutbound outbound) {
        ScmOutbound ob = outbound != null ? outbound : new ScmOutbound();
        ob.setOutStockNo(sysCodeRuleService.generateNextCode("OUT_STOCK_NO"));
        if (ob.getOutStockType() == null) ob.setOutStockType("SALES");
        ob.setAuditStatus("PENDING");
        ob.setOutStockDate(java.time.LocalDateTime.now());

        // 如果是采购退货，关联采购申请信息
        if ("PURCHASE_RETURN".equals(ob.getOutStockType()) && ob.getPurchaseContractId() != null) {
            com.yysystem.modules.purchase.entity.ScmPurchaseRequest purchaseRequest =
                scmPurchaseRequestMapper.selectById(ob.getPurchaseContractId());
            if (purchaseRequest != null) {
                ob.setSupplierId(purchaseRequest.getSupplierId());
                // 查询供应商名称
                if (purchaseRequest.getSupplierId() != null) {
                    com.yysystem.modules.supplier.entity.ScmSupplier supplier =
                        scmSupplierMapper.selectById(purchaseRequest.getSupplierId());
                    if (supplier != null) {
                        ob.setSupplierName(supplier.getSupplierName());
                    }
                }
            }
        }

        try {
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !(auth.getPrincipal() instanceof String)) {
                String username = auth.getName();
                SysUser u = sysUserService.getByUsername(username);
                if (u != null && u.getId() != null) {
                    ob.setCreatorId(u.getId());
                }
            }
        } catch (Exception ignored) {}
        if (ob.getCreatorId() == null) {
            ob.setCreatorId(1L);
        }
        ob.setCreateTime(java.time.LocalDateTime.now());
        scmOutboundService.save(ob);
        System.out.println("创建出库单成功，ID: " + ob.getId() + ", detailList: " + (ob.getDetailList() != null ? ob.getDetailList().size() : "null"));

        // 保存明细
        if (ob.getDetailList() != null && !ob.getDetailList().isEmpty()) {
            for (ScmOutboundDetail detail : ob.getDetailList()) {
                // 采购退货时校验退货数量不能超过采购数量
                if ("PURCHASE_RETURN".equals(ob.getOutStockType()) && ob.getPurchaseContractId() != null) {
                    if (cn.hutool.core.util.StrUtil.isNotBlank(detail.getProductCode())) {
                        // 查询采购数量
                        com.yysystem.modules.purchase.entity.ScmPurchaseRequestItem purchaseItem =
                            scmPurchaseRequestItemMapper.selectOne(
                                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.yysystem.modules.purchase.entity.ScmPurchaseRequestItem>()
                                    .eq(com.yysystem.modules.purchase.entity.ScmPurchaseRequestItem::getRequestId, ob.getPurchaseContractId())
                                    .eq(com.yysystem.modules.purchase.entity.ScmPurchaseRequestItem::getProductCode, detail.getProductCode())
                                    .last("limit 1")
                            );
                        if (purchaseItem != null && purchaseItem.getPurchaseQuantity() != null) {
                            int purchaseQty = purchaseItem.getPurchaseQuantity();
                            int returnQty = detail.getOutQuantity() != null ? detail.getOutQuantity() : 0;
                            if (returnQty > purchaseQty) {
                                return Result.error("商品【" + detail.getProductName() + "】退货数量(" + returnQty + ")不能超过采购数量(" + purchaseQty + ")");
                            }
                        }
                    }
                }
                detail.setOutStockId(ob.getId());
                detail.setCreateTime(java.time.LocalDateTime.now());
                scmOutboundDetailMapper.insert(detail);
                System.out.println("保存明细: " + detail.getProductName() + ", 数量: " + detail.getOutQuantity());
            }
        }

        return Result.success(ob.getId());
    }

    @PostMapping("/detail")
    public Result<Integer> addDetail(@RequestBody ScmOutboundDetail detail, @RequestParam(defaultValue = "1") Integer count) {
        int created = 0;
        ScmOutbound outbound = detail.getOutStockId() != null ? scmOutboundService.getById(detail.getOutStockId()) : null;
        Long contractId = outbound != null ? outbound.getContractId() : null;
        com.yysystem.modules.contract.entity.CrmContractDetail source = null;
        if (detail.getContractDetailId() != null) {
            source = crmContractDetailMapper.selectById(detail.getContractDetailId());
        } else if (contractId != null && cn.hutool.core.util.StrUtil.isNotBlank(detail.getProductCode())) {
            source = crmContractDetailMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.yysystem.modules.contract.entity.CrmContractDetail>()
                    .eq(com.yysystem.modules.contract.entity.CrmContractDetail::getContractId, contractId)
                    .eq(com.yysystem.modules.contract.entity.CrmContractDetail::getProductCode, detail.getProductCode())
                    .last("limit 1"));
        }
        int times = count != null ? count : 1;
        for (int i = 0; i < times; i++) {
            ScmOutboundDetail d = new ScmOutboundDetail();
            d.setOutStockId(detail.getOutStockId());
            d.setContractDetailId(detail.getContractDetailId());
            d.setProductName(cn.hutool.core.util.StrUtil.isNotBlank(detail.getProductName()) ? detail.getProductName() : (source != null ? source.getProductName() : null));
            d.setProductCode(detail.getProductCode());
            d.setProductSpec(cn.hutool.core.util.StrUtil.isNotBlank(detail.getProductSpec()) ? detail.getProductSpec() : (source != null ? source.getProductSpec() : null));
            d.setProductImage(cn.hutool.core.util.StrUtil.isNotBlank(detail.getProductImage()) ? detail.getProductImage() : (source != null ? source.getProductImage() : null));
            d.setProductUnit(detail.getProductUnit());
            d.setOutQuantity(1);
            if (cn.hutool.core.util.StrUtil.isBlank(d.getProductUnit())) {
                if (cn.hutool.core.util.StrUtil.isNotBlank(d.getProductCode())) {
                    PdmProduct product = pdmProductService.getOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PdmProduct>().eq(PdmProduct::getProductCode, d.getProductCode()));
                    d.setProductUnit(product != null && cn.hutool.core.util.StrUtil.isNotBlank(product.getProductUnit()) ? product.getProductUnit() : "件");
                } else {
                    d.setProductUnit("件");
                }
            }
            if (source != null && source.getSalesPrice() != null) {
                d.setSalesPrice(source.getSalesPrice());
            }
            if (source != null && cn.hutool.core.util.StrUtil.isNotBlank(source.getCustomRemark())) {
                d.setParameters(source.getCustomRemark());
            }
            d.setDeviceIp(detail.getDeviceIp());
            d.setRemoteMethod(detail.getRemoteMethod());
            d.setRemoteAccount(detail.getRemoteAccount());
            d.setRemotePassword(detail.getRemotePassword());
            d.setOperatingSystem(detail.getOperatingSystem());
            d.setSystemAccount(detail.getSystemAccount());
            d.setSystemPassword(detail.getSystemPassword());
            d.setOnsiteLocation(detail.getOnsiteLocation());
            d.setSchedulePowerTime(detail.getSchedulePowerTime());
            d.setSchedulePowerOnTime(detail.getSchedulePowerOnTime());
            d.setSchedulePowerOffTime(detail.getSchedulePowerOffTime());
            d.setProgramBackup(detail.getProgramBackup());
            
            // 查询商品信息，判断是否是需要生成二维码的类别
            boolean needQrCode = false;
            if (cn.hutool.core.util.StrUtil.isNotBlank(d.getProductCode())) {
                PdmProduct product = pdmProductService.getOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PdmProduct>()
                        .eq(PdmProduct::getProductCode, d.getProductCode())
                );
                if (product != null && product.getProductCategory() != null) {
                    String category = product.getProductCategory();
                    // 只有指定类别才生成二维码：自产商品、外购商品、自产软件、外购软件
                    needQrCode = category.equals("自产商品") || category.equals("外购商品") 
                              || category.equals("自产软件") || category.equals("外购软件");
                }
            }
            
            // 只有指定类别才生成二维码
            if (needQrCode) {
                // 生成新的二维码格式：商品编码+日期+2位数字
                String productCode = d.getProductCode() != null ? d.getProductCode() : "PRODUCT";
                String dateStr = new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date());
                String seqNum = String.format("%02d", created + 1); // 2位数序号
                String qr = productCode + dateStr + seqNum;
                d.setProductQrCode(qr);
            }
            
            created += scmOutboundDetailMapper.insert(d);
        }
        return Result.success(created);
    }

    @GetMapping("/{id}/param-preview")
    public Result<String> previewParam(@PathVariable Long id, @RequestParam String productCode) {
        ScmOutbound outbound = scmOutboundService.getById(id);
        if (outbound == null || outbound.getContractId() == null || productCode == null || productCode.trim().isEmpty()) {
            return Result.success("");
        }
        com.yysystem.modules.contract.entity.CrmContractDetail source = crmContractDetailMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.yysystem.modules.contract.entity.CrmContractDetail>()
                        .eq(com.yysystem.modules.contract.entity.CrmContractDetail::getContractId, outbound.getContractId())
                        .eq(com.yysystem.modules.contract.entity.CrmContractDetail::getProductCode, productCode)
                        .last("limit 1")
        );
        String param = source != null ? source.getProductRemark() : "";
        return Result.success(param == null ? "" : param);
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/{id}/apply-contract/{contractId}")
    public Result<Boolean> applyContract(@PathVariable Long id, @PathVariable Long contractId,
                                         @RequestParam(value = "limit", required = false, defaultValue = "200") Integer limit) {
        ScmOutbound outbound = scmOutboundService.getById(id);
        if (outbound == null) {
            return Result.error("出库单不存在");
        }
        CrmContract contract = crmContractService.getContractById(contractId);
        if (contract == null) {
            return Result.error("合同不存在");
        }
        outbound.setContractId(contract.getId());
        outbound.setCustomerId(contract.getCustomerId());
        outbound.setLinkmanId(contract.getLinkmanId());
        // 收货地址优先取合同收货地址，其次取合同地址
        String addr = (contract.getReceiveAddress() != null && !contract.getReceiveAddress().isEmpty())
                ? contract.getReceiveAddress() : contract.getAddress();
        outbound.setCustomerAddress(addr);
        outbound.setSalesmanId(contract.getSalesmanId());
        // 只有预定发货时间为空时，才使用合同交货日期
        if (contract.getDeliveryDate() != null && outbound.getScheduledShipTime() == null) {
            outbound.setScheduledShipTime(contract.getDeliveryDate().atStartOfDay());
        }
        // 计算销售总额
        java.math.BigDecimal totalSales = java.math.BigDecimal.ZERO;
        if (contract.getDetailList() != null) {
            for (CrmContractDetail d : contract.getDetailList()) {
                if (d.getSalesPrice() != null && d.getSalesQuantity() != null) {
                    totalSales = totalSales.add(d.getSalesPrice().multiply(new java.math.BigDecimal(d.getSalesQuantity())));
                }
            }
        }
        outbound.setTotalSales(totalSales);
        scmOutboundService.updateById(outbound);

        // 全局序号计数器，用于生成唯一的二维码序号
        int[] globalSeq = {0};
        
        if (contract.getDetailList() != null) {
            for (CrmContractDetail cd : contract.getDetailList()) {
                int qty = cd.getSalesQuantity() != null ? cd.getSalesQuantity() : 0;
                java.util.List<java.util.Map<String, Object>> sumRows = scmOutboundDetailMapper.selectMaps(
                        new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ScmOutboundDetail>()
                                .select("COALESCE(SUM(out_quantity),0) AS total")
                                .eq("contract_detail_id", cd.getId())
                );
                long shippedQty = 0L;
                if (sumRows != null && !sumRows.isEmpty()) {
                    Object v = sumRows.get(0).get("total");
                    if (v != null) {
                        shippedQty = Long.parseLong(String.valueOf(v));
                    }
                }
                int remain = Math.max(0, qty - (int) shippedQty);
                if (remain <= 0) {
                    continue;
                }
                String category = cd.getProductCategory();
                if ((category == null || category.isEmpty())) {
                    if (cd.getProductCode() != null) {
                        PdmProduct prodCat = pdmProductService.getOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PdmProduct>().eq(PdmProduct::getProductCode, cd.getProductCode()));
                        category = prodCat != null ? prodCat.getProductCategory() : category;
                    }
                    if ((category == null || category.isEmpty()) && cd.getProductName() != null) {
                        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PdmProduct> qw = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PdmProduct>()
                                .eq(PdmProduct::getProductName, cd.getProductName());
                        if (cd.getProductSpec() != null && !cd.getProductSpec().isEmpty()) {
                            qw.eq(PdmProduct::getProductSpec, cd.getProductSpec());
                        }
                        PdmProduct prodByName = pdmProductService.getOne(qw);
                        category = prodByName != null ? prodByName.getProductCategory() : category;
                    }
                }
                // 判断是否需要生成二维码（指定类别：自产商品、外购商品、自产软件、外购软件）
                boolean needQrCode = category != null && (category.equals("自产商品") || category.equals("外购商品") 
                                  || category.equals("自产软件") || category.equals("外购软件"));
                
                String productUnit = "件";
                if (cn.hutool.core.util.StrUtil.isNotBlank(cd.getProductCode())) {
                    PdmProduct prod = pdmProductService.getOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PdmProduct>().eq(PdmProduct::getProductCode, cd.getProductCode()));
                    productUnit = prod != null && cn.hutool.core.util.StrUtil.isNotBlank(prod.getProductUnit()) ? prod.getProductUnit() : "件";
                }
                
                int genCount = Math.max(0, Math.min(remain, limit != null ? limit : 200));
                
                if (needQrCode) {
                    // 需要生成二维码的类别：按数量拆分，每条数量1，生成唯一二维码
                    for (int i = 0; i < genCount; i++) {
                        ScmOutboundDetail d = new ScmOutboundDetail();
                        d.setOutStockId(outbound.getId());
                        d.setContractDetailId(cd.getId());
                        d.setProductName(cd.getProductName());
                        d.setProductCode(cd.getProductCode());
                        d.setProductSpec(cd.getProductSpec());
                        d.setProductImage(cd.getProductImage());
                        d.setParameters(cd.getCustomRemark());
                        d.setRemark(cd.getProductRemark());
                        d.setProductUnit(productUnit);
                        d.setSalesPrice(cd.getSalesPrice());
                        d.setAmount(cd.getSalesPrice() != null ? cd.getSalesPrice().multiply(new java.math.BigDecimal(1)) : java.math.BigDecimal.ZERO);
                        d.setOutQuantity(1);
                        
                        // 生成二维码（使用全局序号确保唯一）
                        globalSeq[0]++;
                        String productCode = cd.getProductCode() != null ? cd.getProductCode() : "PRODUCT";
                        String dateStr = new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date());
                        String seqNum = String.format("%02d", globalSeq[0]); // 2位数序号
                        String qr = productCode + dateStr + seqNum;
                        d.setProductQrCode(qr);
                        
                        scmOutboundDetailMapper.insert(d);
                    }
                } else {
                    // 其他类别：合并为一条记录，数量累加，不生成二维码
                    ScmOutboundDetail d = new ScmOutboundDetail();
                    d.setOutStockId(outbound.getId());
                    d.setContractDetailId(cd.getId());
                    d.setProductName(cd.getProductName());
                    d.setProductCode(cd.getProductCode());
                    d.setProductSpec(cd.getProductSpec());
                    d.setProductImage(cd.getProductImage());
                    d.setParameters(cd.getCustomRemark());
                    d.setRemark(cd.getProductRemark());
                    d.setProductUnit(productUnit);
                    d.setSalesPrice(cd.getSalesPrice());
                    java.math.BigDecimal unit = cd.getSalesPrice() != null ? cd.getSalesPrice() : java.math.BigDecimal.ZERO;
                    d.setOutQuantity(genCount);
                    d.setAmount(unit.multiply(new java.math.BigDecimal(genCount)));
                    scmOutboundDetailMapper.insert(d);
                }
            }
        }
        return Result.success(true);
    }

    @Transactional(rollbackFor = Exception.class)
    @PostMapping("/{id}/import-contract-details")
    public Result<Boolean> importContractDetails(@PathVariable Long id, @RequestBody ImportContractItemsRequest req) {
        ScmOutbound outbound = scmOutboundService.getById(id);
        if (outbound == null) {
            return Result.error("出库单不存在");
        }
        if (req.getContractId() != null) {
            CrmContract contract = crmContractService.getContractById(req.getContractId());
            if (contract == null) {
                return Result.error("合同不存在");
            }
            outbound.setContractId(contract.getId());
            outbound.setCustomerId(contract.getCustomerId());
            outbound.setLinkmanId(contract.getLinkmanId());
            outbound.setCustomerAddress(contract.getAddress());
            outbound.setSalesmanId(contract.getSalesmanId());
            // 只有预定发货时间为空时，才使用合同交货日期
            if (contract.getDeliveryDate() != null && outbound.getScheduledShipTime() == null) {
                outbound.setScheduledShipTime(contract.getDeliveryDate().atStartOfDay());
            }
            scmOutboundService.updateById(outbound);
        }
        if (req.getItems() == null || req.getItems().isEmpty()) {
            return Result.success(true);
        }
        // 限制单次导入的商品种类数，防止性能问题
        if (req.getItems().size() > 100) {
            return Result.error("单次导入商品种类过多（最多100种），请分批导入");
        }
        // 全局序号计数器，用于生成唯一的二维码序号
        int[] globalSeq = {0};
        for (ImportItem it : req.getItems()) {
            CrmContractDetail cd = crmContractDetailMapper.selectById(it.getContractDetailId());
            if (cd == null) continue;
            int qty = cd.getSalesQuantity() != null ? cd.getSalesQuantity() : 0;
            List<java.util.Map<String, Object>> sumRows = scmOutboundDetailMapper.selectMaps(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ScmOutboundDetail>()
                            .select("COALESCE(SUM(out_quantity),0) AS total")
                            .eq("contract_detail_id", cd.getId())
            );
            long shippedQty = 0L;
            if (sumRows != null && !sumRows.isEmpty()) {
                Object v = sumRows.get(0).get("total");
                if (v != null) shippedQty = Long.parseLong(String.valueOf(v));
            }
            int remain = Math.max(0, qty - (int) shippedQty);
            int want = it.getQuantity() != null ? it.getQuantity() : 0;
            // 优先使用前端传入的数量，如果超过剩余数量则使用剩余数量
            int genCount = Math.min(want, remain > 0 ? remain : want);
            if (genCount <= 0) continue;
            String productUnit = "件";
            if (cn.hutool.core.util.StrUtil.isNotBlank(cd.getProductCode())) {
                PdmProduct prod = pdmProductService.getOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PdmProduct>().eq(PdmProduct::getProductCode, cd.getProductCode()));
                productUnit = prod != null && cn.hutool.core.util.StrUtil.isNotBlank(prod.getProductUnit()) ? prod.getProductUnit() : "件";
            }
            String category = cd.getProductCategory();
            if ((category == null || category.isEmpty())) {
                if (cd.getProductCode() != null) {
                    PdmProduct prodCat = pdmProductService.getOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PdmProduct>().eq(PdmProduct::getProductCode, cd.getProductCode()));
                    category = prodCat != null ? prodCat.getProductCategory() : category;
                }
                if ((category == null || category.isEmpty()) && cd.getProductName() != null) {
                    com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PdmProduct> qw = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PdmProduct>()
                            .eq(PdmProduct::getProductName, cd.getProductName());
                    if (cd.getProductSpec() != null && !cd.getProductSpec().isEmpty()) {
                        qw.eq(PdmProduct::getProductSpec, cd.getProductSpec());
                    }
                    PdmProduct prodByName = pdmProductService.getOne(qw);
                    category = prodByName != null ? prodByName.getProductCategory() : category;
                }
            }
            // 判断是否需要生成二维码（指定类别：自产商品、外购商品、自产软件、外购软件）
            // 如果类别为空，默认不拆分（保守策略）
            boolean needQrCode = category != null && (category.equals("自产商品") || category.equals("外购商品")
                              || category.equals("自产软件") || category.equals("外购软件"));
            log.info("[SAVE] 商品={}, 编码={}, 类别={}, 需拆分={}, genCount={}", cd.getProductName(), cd.getProductCode(), category, needQrCode, genCount);
            
            if (needQrCode) {
                // 需要生成二维码的类别：按数量拆分，每条数量1，生成唯一二维码
                for (int i = 0; i < genCount; i++) {
                    ScmOutboundDetail d = new ScmOutboundDetail();
                    d.setOutStockId(outbound.getId());
                    d.setContractDetailId(cd.getId());
                    d.setProductName(cd.getProductName());
                    d.setProductCode(cd.getProductCode());
                    d.setProductSpec(cd.getProductSpec());
                    d.setProductImage(cd.getProductImage());
                    d.setParameters(cd.getCustomRemark());
                    d.setRemark(cd.getProductRemark());
                    d.setProductUnit(productUnit);
                    d.setSalesPrice(cd.getSalesPrice());
                    d.setAmount(cd.getSalesPrice() != null ? cd.getSalesPrice().multiply(new java.math.BigDecimal(1)) : java.math.BigDecimal.ZERO);
                    d.setOutQuantity(1);
                    
                    // 生成二维码（使用全局序号确保唯一）
                    globalSeq[0]++;
                    String productCode = cd.getProductCode() != null ? cd.getProductCode() : "PRODUCT";
                    String dateStr = new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date());
                    String seqNum = String.format("%02d", globalSeq[0]); // 2位数序号
                    String qr = productCode + dateStr + seqNum;
                    d.setProductQrCode(qr);
                    
                    scmOutboundDetailMapper.insert(d);
                }
            } else {
                // 其他类别：合并为一条记录，数量累加，不生成二维码
                ScmOutboundDetail d = new ScmOutboundDetail();
                d.setOutStockId(outbound.getId());
                d.setContractDetailId(cd.getId());
                d.setProductName(cd.getProductName());
                d.setProductCode(cd.getProductCode());
                d.setProductSpec(cd.getProductSpec());
                d.setProductImage(cd.getProductImage());
                d.setParameters(cd.getCustomRemark());
                d.setRemark(cd.getProductRemark());
                d.setProductUnit(productUnit);
                d.setSalesPrice(cd.getSalesPrice());
                java.math.BigDecimal unit = cd.getSalesPrice() != null ? cd.getSalesPrice() : java.math.BigDecimal.ZERO;
                d.setOutQuantity(genCount);
                d.setAmount(unit.multiply(new java.math.BigDecimal(genCount)));
                scmOutboundDetailMapper.insert(d);
            }
        }
        return Result.success(true);
    }

    @Autowired
    private com.yysystem.modules.purchase.mapper.ScmPurchaseRequestItemMapper scmPurchaseRequestItemMapper;

    /**
     * 获取采购申请已入库的商品及可退数量（用于采购退货）
     * @param excludeOutboundId 可选，排除指定出库单的退货数量（用于编辑时）
     */
    @GetMapping("/purchase-contract-inbound/{purchaseRequestId}")
    public Result<java.util.List<java.util.Map<String, Object>>> getPurchaseContractInbound(
            @PathVariable Long purchaseRequestId,
            @RequestParam(required = false) Long excludeOutboundId) {
        // 查询采购申请明细
        java.util.List<com.yysystem.modules.purchase.entity.ScmPurchaseRequestItem> details =
            scmPurchaseRequestItemMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.yysystem.modules.purchase.entity.ScmPurchaseRequestItem>()
                    .eq(com.yysystem.modules.purchase.entity.ScmPurchaseRequestItem::getRequestId, purchaseRequestId)
            );

        java.util.List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();

        for (com.yysystem.modules.purchase.entity.ScmPurchaseRequestItem pd : details) {
            String productCode = pd.getProductCode();
            String productName = pd.getProductName();

            if (cn.hutool.core.util.StrUtil.isBlank(productCode)) continue;

            // 查询该商品的总入库数量（已审核的入库单）
            long totalInboundQty = 0L;
            java.util.List<java.util.Map<String, Object>> inboundRows = scmInboundDetailMapper.selectMaps(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.yysystem.modules.inbound.entity.ScmInboundDetail>()
                    .select("COALESCE(SUM(quantity),0) AS total")
                    .eq("product_code", productCode)
                    .apply("inbound_id IN (SELECT id FROM scm_inbound WHERE audit_status = 'PASSED')")
            );
            if (inboundRows != null && !inboundRows.isEmpty()) {
                Object v = inboundRows.get(0).get("total");
                if (v != null) totalInboundQty = Long.parseLong(String.valueOf(v));
            }

            // 查询该商品的已退货数量（包括已审核和待审核的退货单，排除当前编辑的单据）
            long totalReturnedQty = 0L;
            String excludeSql = excludeOutboundId != null ? " AND id != " + excludeOutboundId : "";
            java.util.List<java.util.Map<String, Object>> returnRows = scmOutboundDetailMapper.selectMaps(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ScmOutboundDetail>()
                    .select("COALESCE(SUM(out_quantity),0) AS total")
                    .eq("product_code", productCode)
                    .apply("out_stock_id IN (SELECT id FROM scm_outbound WHERE out_stock_type = 'PURCHASE_RETURN' AND audit_status IN ('PASSED', 'PENDING')" + excludeSql + ")")
            );
            if (returnRows != null && !returnRows.isEmpty()) {
                Object v = returnRows.get(0).get("total");
                if (v != null) totalReturnedQty = Long.parseLong(String.valueOf(v));
            }

            // 可退数量 = 总入库 - 已退货
            long remainQty = Math.max(0, totalInboundQty - totalReturnedQty);

            // 只要有入库记录就显示（用于调试）
            java.util.Map<String, Object> row = new java.util.HashMap<>();
            row.put("purchaseContractDetailId", pd.getId());
            row.put("productCode", productCode);
            row.put("productName", productName);
            row.put("productSpec", pd.getProductSpec());
            row.put("productUnit", pd.getProductUnit());
            row.put("unitPrice", pd.getPurchasePrice());
            row.put("purchaseQty", pd.getPurchaseQuantity()); // 采购数量
            row.put("totalInboundQty", (int) totalInboundQty);
            row.put("totalReturnedQty", (int) totalReturnedQty);
            row.put("remainQty", (int) remainQty);
            result.add(row);
        }
        return Result.success(result);
    }

    /**
     * 导入采购退货明细
     */
    @PostMapping("/{id}/import-purchase-return")
    public Result<Boolean> importPurchaseReturn(@PathVariable Long id, @RequestBody ImportPurchaseReturnRequest req) {
        ScmOutbound outbound = scmOutboundService.getById(id);
        if (outbound == null) {
            return Result.error("出库单不存在");
        }
        if (!"PURCHASE_RETURN".equals(outbound.getOutStockType())) {
            return Result.error("不是采购退货单");
        }
        if (req.getItems() == null || req.getItems().isEmpty()) {
            return Result.success(true);
        }
        
        // 限制单次导入的商品种类数
        if (req.getItems().size() > 100) {
            return Result.error("单次导入商品种类过多（最多100种），请分批导入");
        }
        
        for (PurchaseReturnItem item : req.getItems()) {
            // 验证退货数量不超过可退数量
            long totalInboundQty = 0L;
            java.util.List<java.util.Map<String, Object>> inboundRows = scmInboundDetailMapper.selectMaps(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.yysystem.modules.inbound.entity.ScmInboundDetail>()
                    .select("COALESCE(SUM(quantity),0) AS total")
                    .eq("product_code", item.getProductCode())
                    .apply("inbound_id IN (SELECT id FROM scm_inbound WHERE audit_status = 'PASSED')")
            );
            if (inboundRows != null && !inboundRows.isEmpty()) {
                Object v = inboundRows.get(0).get("total");
                if (v != null) totalInboundQty = Long.parseLong(String.valueOf(v));
            }
            
            long totalReturnedQty = 0L;
            java.util.List<java.util.Map<String, Object>> returnRows = scmOutboundDetailMapper.selectMaps(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ScmOutboundDetail>()
                    .select("COALESCE(SUM(out_quantity),0) AS total")
                    .eq("product_code", item.getProductCode())
                    .apply("out_stock_id IN (SELECT id FROM scm_outbound WHERE out_stock_type = 'PURCHASE_RETURN' AND audit_status = 'PASSED')")
            );
            if (returnRows != null && !returnRows.isEmpty()) {
                Object v = returnRows.get(0).get("total");
                if (v != null) totalReturnedQty = Long.parseLong(String.valueOf(v));
            }
            
            long remainQty = Math.max(0, totalInboundQty - totalReturnedQty);
            int returnQty = item.getQuantity() != null ? item.getQuantity() : 0;
            
            if (returnQty > remainQty) {
                return Result.error("商品[" + item.getProductName() + "]退货数量[" + returnQty + "]超过可退数量[" + remainQty + "]");
            }
            
            // 创建退货明细
            ScmOutboundDetail detail = new ScmOutboundDetail();
            detail.setOutStockId(id);
            detail.setProductCode(item.getProductCode());
            detail.setProductName(item.getProductName());
            detail.setProductSpec(item.getProductSpec());
            detail.setProductUnit(item.getProductUnit());
            detail.setOutQuantity(returnQty);
            detail.setSalesPrice(item.getUnitPrice());
            if (item.getUnitPrice() != null) {
                detail.setAmount(item.getUnitPrice().multiply(new java.math.BigDecimal(returnQty)));
            }
            // 采购退货不生成二维码
            detail.setProductQrCode("");
            
            scmOutboundDetailMapper.insert(detail);
        }
        
        return Result.success(true);
    }
    
    public static class ImportPurchaseReturnRequest {
        private List<PurchaseReturnItem> items;
        public List<PurchaseReturnItem> getItems() { return items; }
        public void setItems(List<PurchaseReturnItem> items) { this.items = items; }
    }
    
    public static class PurchaseReturnItem {
        private String productCode;
        private String productName;
        private String productSpec;
        private String productUnit;
        private java.math.BigDecimal unitPrice;
        private Integer quantity;
        
        public String getProductCode() { return productCode; }
        public void setProductCode(String productCode) { this.productCode = productCode; }
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public String getProductSpec() { return productSpec; }
        public void setProductSpec(String productSpec) { this.productSpec = productSpec; }
        public String getProductUnit() { return productUnit; }
        public void setProductUnit(String productUnit) { this.productUnit = productUnit; }
        public java.math.BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(java.math.BigDecimal unitPrice) { this.unitPrice = unitPrice; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }

    @GetMapping("/contract-remaining/{contractId}")
    public Result<java.util.List<java.util.Map<String, Object>>> getContractRemaining(@PathVariable Long contractId) {
        java.util.List<CrmContractDetail> details = crmContractDetailMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CrmContractDetail>()
                        .eq(CrmContractDetail::getContractId, contractId)
        );
        java.util.List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
        for (CrmContractDetail cd : details) {
            int qty = cd.getSalesQuantity() != null ? cd.getSalesQuantity() : 0;
            long shippedQty = 0L;
            // 1) 统计已发货（按合同明细ID）
            {
                java.util.List<java.util.Map<String, Object>> sumRows = scmOutboundDetailMapper.selectMaps(
                        new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ScmOutboundDetail>()
                                .select("COALESCE(SUM(out_quantity),0) AS shipped")
                                .eq("contract_detail_id", cd.getId())
                );
                if (sumRows != null && !sumRows.isEmpty()) {
                    Object v = sumRows.get(0).get("shipped");
                    if (v != null) shippedQty += Long.parseLong(String.valueOf(v));
                }
            }
            {
                String code = cd.getProductCode();
                String name = cd.getProductName();
                String spec = cd.getProductSpec();
                if (code != null && !code.isEmpty()) {
                    com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ScmOutboundDetail> qw1 =
                            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ScmOutboundDetail>()
                                    .select("COALESCE(SUM(out_quantity),0) AS shipped_fallback")
                                    .isNull("contract_detail_id")
                                    .eq("product_code", code)
                                    .apply("out_stock_id IN (SELECT id FROM scm_outbound WHERE contract_id = {0})", contractId);
                    java.util.List<java.util.Map<String, Object>> sumRows2 = scmOutboundDetailMapper.selectMaps(qw1);
                    if (sumRows2 != null && !sumRows2.isEmpty()) {
                        Object v2 = sumRows2.get(0).get("shipped_fallback");
                        if (v2 != null) shippedQty += Long.parseLong(String.valueOf(v2));
                    }
                } else {
                    com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ScmOutboundDetail> qw2 =
                            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ScmOutboundDetail>()
                                    .select("COALESCE(SUM(out_quantity),0) AS shipped_fallback2")
                                    .isNull("contract_detail_id")
                                    .eq(name != null && !name.isEmpty(), "product_name", name)
                                    .eq(spec != null && !spec.isEmpty(), "product_spec", spec)
                                    .apply("out_stock_id IN (SELECT id FROM scm_outbound WHERE contract_id = {0})", contractId);
                    java.util.List<java.util.Map<String, Object>> sumRows3 = scmOutboundDetailMapper.selectMaps(qw2);
                    if (sumRows3 != null && !sumRows3.isEmpty()) {
                        Object v3 = sumRows3.get(0).get("shipped_fallback2");
                        if (v3 != null) shippedQty += Long.parseLong(String.valueOf(v3));
                    }
                }
            }
            int remain = Math.max(0, qty - (int) shippedQty);
            java.util.Map<String, Object> row = new java.util.HashMap<>();
            row.put("contractDetailId", cd.getId());
            row.put("salesQuantity", qty);
            row.put("shippedQuantity", (int) shippedQty);
            row.put("remainQuantity", remain);
            result.add(row);
        }
        return Result.success(result);
    }
}
