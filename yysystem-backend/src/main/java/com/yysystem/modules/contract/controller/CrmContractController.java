package com.yysystem.modules.contract.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yysystem.common.result.Result;
import com.yysystem.modules.contract.entity.CrmContract;
import com.yysystem.modules.contract.service.CrmContractService;
import com.yysystem.modules.finance.service.CrmFinanceService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yysystem.modules.finance.entity.CrmFinance;
import com.yysystem.modules.crm.entity.CrmCustomer;
import com.yysystem.modules.crm.service.CrmCustomerService;
import com.yysystem.modules.invoice.entity.CrmInvoice;
import com.yysystem.modules.invoice.service.CrmInvoiceService;
import com.yysystem.modules.outbound.entity.ScmOutbound;
import com.yysystem.modules.outbound.service.ScmOutboundService;
import com.yysystem.modules.outbound.entity.ScmOutboundDetail;
import com.yysystem.modules.outbound.mapper.ScmOutboundDetailMapper;
import com.yysystem.modules.contract.entity.CrmContractDetail;
import com.yysystem.modules.contract.mapper.CrmContractDetailMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import com.yysystem.modules.system.entity.SysUser;
import com.yysystem.modules.system.service.SysUserService;
import org.springframework.security.core.context.SecurityContextHolder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.HashMap;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;

@RestController
@RequestMapping("/api/crm/contract")
public class CrmContractController {

    @Autowired
    private CrmContractService crmContractService;

    @Autowired
    private CrmCustomerService crmCustomerService;
    
    @Autowired
    private CrmFinanceService crmFinanceService;
    
    @Autowired
    private CrmInvoiceService crmInvoiceService;

    @Autowired
    private ScmOutboundService scmOutboundService;

    @Autowired
    private ScmOutboundDetailMapper scmOutboundDetailMapper;

    @Autowired
    private CrmContractDetailMapper crmContractDetailMapper;

    @Autowired
    private SysUserService sysUserService;

    @PostMapping
    public Result<Long> save(@RequestBody CrmContract crmContract) {
        String username = null;
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
            username = authentication.getName();
        }
        if (username != null) {
            SysUser user = sysUserService.getByUsername(username);
            if (user != null && user.getUserType() != null && "finance".equalsIgnoreCase(user.getUserType())) {
                return Result.error("无权创建合同");
            }
        }
        return Result.success(crmContractService.saveContract(crmContract));
    }

    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody CrmContract crmContract) {
        crmContract.setId(id);
        String username = null;
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
            username = authentication.getName();
        }
        CrmContract exists = crmContractService.getById(id);
        if (exists != null && "PASSED".equalsIgnoreCase(String.valueOf(exists.getAuditStatus()))) {
            return Result.error("审核通过后不可修改");
        }
        if (username != null && !"admin".equalsIgnoreCase(username)) {
            SysUser user = sysUserService.getByUsername(username);
            if (user != null) {
                if (user.getUserType() != null && "finance".equalsIgnoreCase(user.getUserType())) {
                    return Result.error("财务只读不可编辑");
                }
                if (exists != null && exists.getSalesmanId() != null && user.getId() != null && !exists.getSalesmanId().equals(user.getId())) {
                    return Result.error("无权编辑他人合同");
                }
            }
        }
        return Result.success(crmContractService.updateContract(crmContract));
    }

    @PutMapping("/{id}/fee-status")
    public Result<Boolean> updateIntermediaryFeeStatus(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String username = null;
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
            username = authentication.getName();
        }
        if (username == null || !"admin".equalsIgnoreCase(username)) {
            return Result.error("仅管理员可操作中介支付状态");
        }
        String status = String.valueOf(body.getOrDefault("intermediaryFeeStatus", "UNPAID"));
        String payInfo = body.get("intermediaryPayInfo") == null ? null : String.valueOf(body.get("intermediaryPayInfo"));

        CrmContract exists = crmContractService.getById(id);
        if (exists == null) {
            return Result.error("合同不存在");
        }

        CrmContract partial = new CrmContract();
        partial.setId(id);
        partial.setIntermediaryFeeStatus(status);

        if (payInfo != null) {
            Map<String, Object> ext = new HashMap<>();
            try {
                String raw = exists.getExtendFields();
                if (raw != null && !raw.trim().isEmpty()) {
                    ext = new ObjectMapper().readValue(raw, new TypeReference<Map<String, Object>>() {});
                }
            } catch (Exception ignored) {}
            ext.put("intermediaryPayInfo", payInfo);
            ext.put("intermediaryPayTime", java.time.LocalDateTime.now().toString());
            java.util.List<java.util.Map<String, Object>> history = new java.util.ArrayList<>();
            try {
                Object h = ext.get("intermediaryPayHistory");
                if (h instanceof java.util.List) {
                    for (Object it : (java.util.List<?>) h) {
                        if (it instanceof java.util.Map) history.add(new java.util.HashMap<>((java.util.Map<String, Object>) it));
                    }
                }
            } catch (Exception ignored) {}
            java.util.Map<String, Object> record = new java.util.HashMap<>();
            record.put("time", java.time.LocalDateTime.now().toString());
            record.put("status", status);
            record.put("info", payInfo);
            record.put("operator", username);
            history.add(record);
            ext.put("intermediaryPayHistory", history);
            try {
                partial.setExtendFields(new ObjectMapper().writeValueAsString(ext));
            } catch (Exception ignored) {}
        }

        boolean ok = crmContractService.updateById(partial);
        return ok ? Result.success(true) : Result.error("更新失败");
    }

    /**
     * 编辑居间费信息
     */
    @PutMapping("/{id}/intermediary")
    public Result<Boolean> updateIntermediaryInfo(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String username = null;
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
            username = authentication.getName();
        }
        if (username == null || !("admin".equalsIgnoreCase(username) || "finance".equalsIgnoreCase(username))) {
            return Result.error("仅管理员或财务人员可编辑居间费信息");
        }

        CrmContract exists = crmContractService.getById(id);
        if (exists == null) {
            return Result.error("合同不存在");
        }

        BigDecimal intermediaryFee = body.get("intermediaryFee") != null ? new BigDecimal(String.valueOf(body.get("intermediaryFee"))) : null;
        String intermediaryCustomerName = body.get("intermediaryCustomerName") != null ? String.valueOf(body.get("intermediaryCustomerName")) : null;
        String payTime = body.get("payTime") != null ? String.valueOf(body.get("payTime")) : null;
        String payMethod = body.get("payMethod") != null ? String.valueOf(body.get("payMethod")) : null;
        String payRemark = body.get("payRemark") != null ? String.valueOf(body.get("payRemark")) : null;

        UpdateWrapper<CrmContract> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id);
        
        if (intermediaryFee != null) {
            updateWrapper.set("intermediary_fee", intermediaryFee);
        }
        if (intermediaryCustomerName != null) {
            updateWrapper.set("intermediary_customer_name", intermediaryCustomerName);
        }
        if (payTime != null && !payTime.isEmpty()) {
            try {
                // 尝试解析多种格式：yyyy-MM-dd HH:mm:ss 或 yyyy-MM-ddTHH:mm:ss
                String normalized = payTime.replace(" ", "T");
                if (normalized.length() == 10) {
                    // 只有日期，添加时间部分
                    normalized += "T00:00:00";
                }
                java.time.LocalDateTime payDateTime = java.time.LocalDateTime.parse(normalized);
                updateWrapper.set("intermediary_pay_time", payDateTime);
            } catch (Exception e) {
                // 解析失败，尝试使用 DateTimeFormatter
                try {
                    java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    java.time.LocalDateTime payDateTime = java.time.LocalDateTime.parse(payTime, formatter);
                    updateWrapper.set("intermediary_pay_time", payDateTime);
                } catch (Exception ex) {
                    return Result.error("支付时间格式错误: " + payTime);
                }
            }
        }
        if (payMethod != null) {
            updateWrapper.set("intermediary_pay_method", payMethod);
        }
        if (payRemark != null) {
            updateWrapper.set("intermediary_pay_remark", payRemark);
        }

        boolean ok = crmContractService.update(updateWrapper);
        return ok ? Result.success(true) : Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> remove(@PathVariable Long id) {
        String username = null;
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
            username = authentication.getName();
        }
        CrmContract exists = crmContractService.getById(id);
        if (exists != null && "PASSED".equalsIgnoreCase(String.valueOf(exists.getAuditStatus()))) {
            return Result.error("审核通过后不可删除");
        }
        if (username != null && !"admin".equalsIgnoreCase(username)) {
            SysUser user = sysUserService.getByUsername(username);
            if (user != null) {
                if (user.getUserType() != null && "finance".equalsIgnoreCase(user.getUserType())) {
                    return Result.error("财务只读不可删除");
                }
                if (exists != null && exists.getSalesmanId() != null && user.getId() != null && !exists.getSalesmanId().equals(user.getId())) {
                    return Result.error("无权删除他人合同");
                }
            }
        }
        return Result.success(crmContractService.removeById(id));
    }

    @PutMapping("/{id}/unlock")
    public Result<Boolean> unlock(@PathVariable Long id) {
        String username = null;
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
            username = authentication.getName();
        }
        if (username == null || !"admin".equalsIgnoreCase(username)) {
            return Result.error("仅管理员可解锁合同");
        }
        CrmContract exists = crmContractService.getById(id);
        if (exists == null) {
            return Result.error("合同不存在");
        }
        CrmContract upd = new CrmContract();
        upd.setId(id);
        upd.setAuditStatus("PENDING");
        String detail = String.valueOf(exists.getAuditDetail() != null ? exists.getAuditDetail() : "");
        String ts = java.time.LocalDateTime.now().toString();
        upd.setAuditDetail(detail + " | admin unlock at " + ts);
        boolean ok = crmContractService.updateById(upd);
        return ok ? Result.success(true) : Result.error("解锁失败");
    }

    @GetMapping("/{id:\\d+}")
    public Result<CrmContract> getById(@PathVariable("id") Long id) {
        CrmContract contract = crmContractService.getContractById(id);
        if (contract != null) {
            CrmCustomer cust = contract.getCustomerId() != null ? crmCustomerService.getById(contract.getCustomerId()) : null;
            if (cust != null) {
                contract.setCustomerName(cust.getCustomerName());
            }
            boolean isAgent = false;
            if (cust != null) {
                String cat = String.valueOf(cust.getCustomerCategory());
                String tag = cust.getCustomerTag() == null ? "" : cust.getCustomerTag();
                isAgent = "AGENT".equalsIgnoreCase(cat) || tag.contains("代理");
            }
            CrmCustomer end = null;
            String endName = contract.getEndCustomerName();
            if (endName != null && !endName.isEmpty()) {
                end = crmCustomerService.getOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CrmCustomer>()
                        .eq(CrmCustomer::getCustomerName, endName));
            }
            if (isAgent && end != null) {
                contract.setReceiver(end.getLinkman());
                contract.setReceiverPhone(end.getPhone());
                contract.setReceiveAddress(end.getAddress());
            } else {
                if (contract.getReceiver() == null || contract.getReceiver().isEmpty()) {
                    if (cust != null && cust.getLinkman() != null) {
                        contract.setReceiver(cust.getLinkman());
                    }
                }
                if (contract.getReceiverPhone() == null || contract.getReceiverPhone().isEmpty()) {
                    if (cust != null && cust.getPhone() != null) {
                        contract.setReceiverPhone(cust.getPhone());
                    }
                }
                if (contract.getReceiveAddress() == null || contract.getReceiveAddress().isEmpty()) {
                    if (cust != null && cust.getAddress() != null) {
                        contract.setReceiveAddress(cust.getAddress());
                    }
                }
            }
        }
        if (contract != null) {
            java.math.BigDecimal amount = contract.getContractAmount() != null ? contract.getContractAmount() : java.math.BigDecimal.ZERO;
            java.math.BigDecimal collection = contract.getTotalCollection() != null ? contract.getTotalCollection() : java.math.BigDecimal.ZERO;
            contract.setArrears(amount.subtract(collection));
            long rejectedCount = crmFinanceService.count(new LambdaQueryWrapper<CrmFinance>()
                    .eq(CrmFinance::getContractId, contract.getId())
                    .eq(CrmFinance::getAuditStatus, "REJECTED"));
            contract.setHasRejectedFinance(rejectedCount > 0);
            long rejectedIncome = crmFinanceService.count(new LambdaQueryWrapper<CrmFinance>()
                    .eq(CrmFinance::getContractId, contract.getId())
                    .eq(CrmFinance::getAuditStatus, "REJECTED")
                    .eq(CrmFinance::getType, "IN"));
            long rejectedExpense = crmFinanceService.count(new LambdaQueryWrapper<CrmFinance>()
                    .eq(CrmFinance::getContractId, contract.getId())
                    .eq(CrmFinance::getAuditStatus, "REJECTED")
                    .eq(CrmFinance::getType, "OUT"));
            contract.setHasRejectedIncome(rejectedIncome > 0);
            contract.setHasRejectedExpense(rejectedExpense > 0);
        }
        return Result.success(contract);
    }

    @GetMapping("/list")
    public Result<Page<CrmContract>> list(@RequestParam(defaultValue = "1") Integer current,
                                          @RequestParam(defaultValue = "10") Integer size,
                                          @RequestParam(required = false) String contractNo,
                                          @RequestParam(required = false) String customerName,
                                          @RequestParam(required = false) String auditStatus) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CrmContract> wrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        if (contractNo != null && !contractNo.isEmpty()) {
            wrapper.like("contract_no", contractNo);
        }
        if (auditStatus != null && !auditStatus.isEmpty()) {
            wrapper.eq("audit_status", auditStatus);
        }
        
        if (customerName != null && !customerName.isEmpty()) {
            java.util.List<Long> matchedCustomerIds = new java.util.ArrayList<>();
            java.util.List<CrmCustomer> customers = crmCustomerService.list(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CrmCustomer>()
                            .like(CrmCustomer::getCustomerName, customerName));
            for (CrmCustomer c : customers) {
                if (c.getId() != null) {
                    matchedCustomerIds.add(c.getId());
                }
            }
            if (matchedCustomerIds.isEmpty()) {
                Page<CrmContract> emptyPage = new Page<>(current, size);
                emptyPage.setRecords(new java.util.ArrayList<>());
                emptyPage.setTotal(0);
                return Result.success(emptyPage);
            }
            wrapper.in("customer_id", matchedCustomerIds);
        }

        try {
            String username = null;
            org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
                username = authentication.getName();
            }
            if (username != null && !"admin".equalsIgnoreCase(username)) {
                SysUser user = sysUserService.getByUsername(username);
                if (user != null && user.getId() != null) {
                    String ut = user.getUserType();
                    boolean isFinance = ut != null && "finance".equalsIgnoreCase(ut);
                    if (!isFinance) {
                        wrapper.eq("salesman_id", user.getId());
                    }
                }
            }
        } catch (Exception ignored) {}
        wrapper.orderByDesc("create_time", "id");
        Page<CrmContract> page = crmContractService.page(new Page<>(current, size), wrapper);
        List<CrmContract> records = page.getRecords();
        
        if (records.isEmpty()) {
            return Result.success(page);
        }
        
        java.util.Set<Long> customerIds = new java.util.HashSet<>();
        for (CrmContract record : records) {
            if (record.getCustomerId() != null) {
                customerIds.add(record.getCustomerId());
            }
        }
        java.util.Map<Long, CrmCustomer> customerMap = new java.util.HashMap<>();
        if (!customerIds.isEmpty()) {
            List<CrmCustomer> customers = crmCustomerService.listByIds(customerIds);
            for (CrmCustomer c : customers) {
                customerMap.put(c.getId(), c);
            }
        }
        
        java.util.List<Long> contractIds = new java.util.ArrayList<>();
        for (CrmContract record : records) {
            if (record.getId() != null) {
                contractIds.add(record.getId());
            }
        }
        
        java.util.Map<Long, Long> rejectedIncomeMap = new java.util.HashMap<>();
        java.util.Map<Long, Long> rejectedExpenseMap = new java.util.HashMap<>();
        java.util.Map<Long, java.math.BigDecimal> depositAmountMap = new java.util.HashMap<>();
        
        if (!contractIds.isEmpty()) {
            List<CrmFinance> finances = crmFinanceService.list(new LambdaQueryWrapper<CrmFinance>()
                    .in(CrmFinance::getContractId, contractIds));
            for (CrmFinance f : finances) {
                Long cid = f.getContractId();
                if (cid == null) continue;
                
                if ("REJECTED".equals(String.valueOf(f.getAuditStatus()))) {
                    if ("IN".equals(f.getType())) {
                        rejectedIncomeMap.merge(cid, 1L, Long::sum);
                    } else if ("OUT".equals(f.getType())) {
                        rejectedExpenseMap.merge(cid, 1L, Long::sum);
                    }
                }
                
                if ("OUT".equals(f.getType()) && f.getArrivalTime() != null && !"REJECTED".equals(String.valueOf(f.getAuditStatus()))) {
                    String category = f.getCategory() != null ? f.getCategory() : "";
                    String remark = f.getRemark() != null ? f.getRemark() : "";
                    if (category.contains("保证金") || remark.contains("保证金")) {
                        java.math.BigDecimal amt = f.getArrivalAmount() != null ? f.getArrivalAmount() :
                                (f.getAmount() != null ? f.getAmount() : java.math.BigDecimal.ZERO);
                        depositAmountMap.merge(cid, amt, java.math.BigDecimal::add);
                    }
                }
            }
        }
        
        for (CrmContract record : records) {
            Long customerId = record.getCustomerId();
            if (customerId != null) {
                CrmCustomer customer = customerMap.get(customerId);
                if (customer != null) {
                    record.setCustomerName(customer.getCustomerName());
                }
            }
            record.setDepositAmount(depositAmountMap.getOrDefault(record.getId(), java.math.BigDecimal.ZERO));
            record.setHasRejectedFinance(rejectedIncomeMap.getOrDefault(record.getId(), 0L) > 0 
                    || rejectedExpenseMap.getOrDefault(record.getId(), 0L) > 0);
            record.setHasRejectedIncome(rejectedIncomeMap.getOrDefault(record.getId(), 0L) > 0);
            record.setHasRejectedExpense(rejectedExpenseMap.getOrDefault(record.getId(), 0L) > 0);
        }
        return Result.success(page);
    }

    @GetMapping("/by-customer/{customerId:\\d+}")
    public Result<java.util.List<CrmContract>> listByCustomer(@PathVariable("customerId") Long customerId) {
        java.util.List<CrmContract> list = crmContractService.list(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CrmContract>()
                .eq("customer_id", customerId)
                .eq("audit_status", "PASSED"));
        
        if (list.isEmpty()) {
            return Result.success(list);
        }
        
        CrmCustomer customer = crmCustomerService.getById(customerId);
        String customerName = customer != null ? customer.getCustomerName() : null;
        
        java.util.List<Long> contractIds = new java.util.ArrayList<>();
        for (CrmContract c : list) {
            if (c.getId() != null) {
                contractIds.add(c.getId());
            }
        }
        
        java.util.Map<Long, Integer> contractQtyMap = new java.util.HashMap<>();
        java.util.Map<Long, Integer> outboundQtyMap = new java.util.HashMap<>();
        
        if (!contractIds.isEmpty()) {
            java.util.List<CrmContractDetail> allDetails = crmContractDetailMapper.selectList(
                    new LambdaQueryWrapper<CrmContractDetail>().in(CrmContractDetail::getContractId, contractIds));
            
            java.util.Set<Long> detailIds = new java.util.HashSet<>();
            for (CrmContractDetail d : allDetails) {
                int qty = d.getSalesQuantity() != null ? d.getSalesQuantity() : 0;
                contractQtyMap.merge(d.getContractId(), qty, Integer::sum);
                if (d.getId() != null) {
                    detailIds.add(d.getId());
                }
            }
            
            java.util.List<ScmOutbound> allOutbounds = scmOutboundService.list(new LambdaQueryWrapper<ScmOutbound>()
                    .in(ScmOutbound::getContractId, contractIds)
                    .eq(ScmOutbound::getAuditStatus, "PASSED"));
            java.util.Map<Long, String> outboundStatusMap = new java.util.HashMap<>();
            for (ScmOutbound o : allOutbounds) {
                outboundStatusMap.put(o.getId(), o.getAuditStatus());
            }
            
            if (!detailIds.isEmpty()) {
                java.util.List<ScmOutboundDetail> allOutboundDetails = scmOutboundDetailMapper.selectList(
                        new LambdaQueryWrapper<ScmOutboundDetail>()
                                .in(ScmOutboundDetail::getContractDetailId, detailIds));
                for (ScmOutboundDetail od : allOutboundDetails) {
                    if (od.getOutStockId() != null && "PASSED".equals(outboundStatusMap.get(od.getOutStockId()))) {
                        int qty = od.getOutQuantity() != null ? od.getOutQuantity() : 0;
                        for (CrmContractDetail d : allDetails) {
                            if (d.getId() != null && d.getId().equals(od.getContractDetailId()) && d.getContractId() != null) {
                                outboundQtyMap.merge(d.getContractId(), qty, Integer::sum);
                                break;
                            }
                        }
                    }
                }
            }
        }
        
        for (CrmContract record : list) {
            record.setCustomerName(customerName);
            int contractQty = contractQtyMap.getOrDefault(record.getId(), 0);
            int outboundQty = outboundQtyMap.getOrDefault(record.getId(), 0);
            String outboundStatus = "NONE";
            if (contractQty > 0) {
                if (outboundQty >= contractQty) {
                    outboundStatus = "COMPLETED";
                } else if (outboundQty > 0) {
                    outboundStatus = "PARTIAL";
                }
            }
            record.setOutboundStatus(outboundStatus);
            record.setProductTotal(contractQty);
        }
        return Result.success(list);
    }
    @GetMapping("/summary")
    public Result<java.util.Map<String, java.math.BigDecimal>> summary(@RequestParam(required = false) String contractNo) {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CrmContract> wrapper = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        if (contractNo != null && !contractNo.isEmpty()) {
            wrapper.like("contract_no", contractNo);
        }
        try {
            String username = null;
            org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
                username = authentication.getName();
            }
            if (username != null && !"admin".equalsIgnoreCase(username)) {
                SysUser user = sysUserService.getByUsername(username);
                if (user != null && user.getId() != null) {
                    wrapper.eq("salesman_id", user.getId());
                }
            }
        } catch (Exception ignored) {}
        java.util.List<CrmContract> list = crmContractService.list(wrapper);
        java.math.BigDecimal contractTotal = java.math.BigDecimal.ZERO;
        java.math.BigDecimal collectionTotal = java.math.BigDecimal.ZERO;
        java.math.BigDecimal advanceTotal = java.math.BigDecimal.ZERO;
        java.math.BigDecimal expenditureTotal = java.math.BigDecimal.ZERO;
        java.math.BigDecimal invoicedTotal = java.math.BigDecimal.ZERO;
        for (CrmContract c : list) {
            contractTotal = contractTotal.add(c.getContractAmount() != null ? c.getContractAmount() : java.math.BigDecimal.ZERO);
            collectionTotal = collectionTotal.add(c.getTotalCollection() != null ? c.getTotalCollection() : java.math.BigDecimal.ZERO);
            advanceTotal = advanceTotal.add(c.getAdvancePayment() != null ? c.getAdvancePayment() : java.math.BigDecimal.ZERO);
            expenditureTotal = expenditureTotal.add(c.getTotalExpenditure() != null ? c.getTotalExpenditure() : java.math.BigDecimal.ZERO);
            invoicedTotal = invoicedTotal.add(c.getInvoicedAmount() != null ? c.getInvoicedAmount() : java.math.BigDecimal.ZERO);
        }
        java.math.BigDecimal arrearsTotal = contractTotal.subtract(collectionTotal);
        java.util.Map<String, java.math.BigDecimal> data = new java.util.HashMap<>();
        data.put("contractTotal", contractTotal);
        data.put("collectionTotal", collectionTotal);
        data.put("arrearsTotal", arrearsTotal);
        data.put("advanceTotal", advanceTotal);
        data.put("expenditureTotal", expenditureTotal);
        data.put("invoicedTotal", invoicedTotal);
        return Result.success(data);
    }
    
    @GetMapping("/flags")
    public Result<java.util.List<java.util.Map<String, Object>>> flags(@RequestParam String ids) {
        String[] parts = ids.split(",");
        java.util.List<Long> idList = new java.util.ArrayList<>();
        for (String p : parts) {
            try {
                idList.add(Long.parseLong(p.trim()));
            } catch (Exception ignored) {}
        }
        
        if (idList.isEmpty()) {
            return Result.success(new java.util.ArrayList<>());
        }
        
        java.util.Map<Long, java.util.Map<String, Object>> resultMap = new java.util.HashMap<>();
        for (Long id : idList) {
            java.util.Map<String, Object> item = new java.util.HashMap<>();
            item.put("id", id);
            item.put("hasRejectedIncome", false);
            item.put("hasRejectedExpense", false);
            item.put("hasDepositExpense", false);
            item.put("hasPendingOutbound", false);
            item.put("hasOutbound", false);
            item.put("outboundStatus", "NONE");
            item.put("outboundQty", 0);
            item.put("contractQty", 0);
            resultMap.put(id, item);
        }
        
        java.util.List<CrmFinance> rejectedFinances = crmFinanceService.list(new LambdaQueryWrapper<CrmFinance>()
                .in(CrmFinance::getContractId, idList)
                .eq(CrmFinance::getAuditStatus, "REJECTED"));
        for (CrmFinance f : rejectedFinances) {
            java.util.Map<String, Object> item = resultMap.get(f.getContractId());
            if (item != null) {
                if ("IN".equals(f.getType())) {
                    item.put("hasRejectedIncome", true);
                } else if ("OUT".equals(f.getType())) {
                    item.put("hasRejectedExpense", true);
                }
            }
        }
        
        java.util.List<CrmFinance> depositFinances = crmFinanceService.list(new LambdaQueryWrapper<CrmFinance>()
                .in(CrmFinance::getContractId, idList)
                .eq(CrmFinance::getType, "OUT")
                .isNotNull(CrmFinance::getArrivalTime)
                .ne(CrmFinance::getAuditStatus, "REJECTED")
                .and(w -> w.like(CrmFinance::getCategory, "保证金")
                        .or().like(CrmFinance::getRemark, "保证金")
                        .or().apply("JSON_EXTRACT(extend_fields,'$.depositRefundAmount') is not null")));
        for (CrmFinance f : depositFinances) {
            java.util.Map<String, Object> item = resultMap.get(f.getContractId());
            if (item != null) {
                item.put("hasDepositExpense", true);
            }
        }
        
        java.util.List<ScmOutbound> allOutbounds = scmOutboundService.list(new LambdaQueryWrapper<ScmOutbound>()
                .in(ScmOutbound::getContractId, idList));
        java.util.Set<Long> pendingOutboundContractIds = new java.util.HashSet<>();
        java.util.Set<Long> totalOutboundContractIds = new java.util.HashSet<>();
        java.util.Map<Long, String> outboundStatusMap = new java.util.HashMap<>();
        for (ScmOutbound o : allOutbounds) {
            totalOutboundContractIds.add(o.getContractId());
            if (!"PASSED".equals(o.getAuditStatus())) {
                pendingOutboundContractIds.add(o.getContractId());
            }
            outboundStatusMap.put(o.getId(), o.getAuditStatus());
        }
        for (Long id : idList) {
            java.util.Map<String, Object> item = resultMap.get(id);
            if (item != null) {
                item.put("hasPendingOutbound", pendingOutboundContractIds.contains(id));
                item.put("hasOutbound", totalOutboundContractIds.contains(id));
            }
        }
        
        java.util.List<CrmContractDetail> allContractDetails = crmContractDetailMapper.selectList(
                new LambdaQueryWrapper<CrmContractDetail>().in(CrmContractDetail::getContractId, idList));
        java.util.Map<Long, java.util.List<CrmContractDetail>> contractDetailsMap = new java.util.HashMap<>();
        java.util.Map<Long, Integer> contractQtyMap = new java.util.HashMap<>();
        for (CrmContractDetail d : allContractDetails) {
            contractDetailsMap.computeIfAbsent(d.getContractId(), k -> new java.util.ArrayList<>()).add(d);
            int qty = d.getSalesQuantity() != null ? d.getSalesQuantity() : 0;
            contractQtyMap.merge(d.getContractId(), qty, Integer::sum);
        }
        for (Long id : idList) {
            java.util.Map<String, Object> item = resultMap.get(id);
            if (item != null) {
                item.put("contractQty", contractQtyMap.getOrDefault(id, 0));
            }
        }
        
        java.util.Set<Long> detailIds = new java.util.HashSet<>();
        for (CrmContractDetail d : allContractDetails) {
            if (d.getId() != null) {
                detailIds.add(d.getId());
            }
        }
        java.util.Map<Long, Integer> outboundQtyMap = new java.util.HashMap<>();
        if (!detailIds.isEmpty()) {
            java.util.List<ScmOutboundDetail> allOutboundDetails = scmOutboundDetailMapper.selectList(
                    new LambdaQueryWrapper<ScmOutboundDetail>()
                            .in(ScmOutboundDetail::getContractDetailId, detailIds));
            for (ScmOutboundDetail od : allOutboundDetails) {
                if (od.getOutStockId() != null && "PASSED".equals(outboundStatusMap.get(od.getOutStockId()))) {
                    int qty = od.getOutQuantity() != null ? od.getOutQuantity() : 0;
                    CrmContractDetail detail = null;
                    for (CrmContractDetail d : allContractDetails) {
                        if (d.getId() != null && d.getId().equals(od.getContractDetailId())) {
                            detail = d;
                            break;
                        }
                    }
                    if (detail != null && detail.getContractId() != null) {
                        outboundQtyMap.merge(detail.getContractId(), qty, Integer::sum);
                    }
                }
            }
        }
        
        for (Long id : idList) {
            java.util.Map<String, Object> item = resultMap.get(id);
            if (item != null) {
                int contractQty = (int) item.get("contractQty");
                int outboundQty = outboundQtyMap.getOrDefault(id, 0);
                item.put("outboundQty", outboundQty);
                
                String outboundStatus = "NONE";
                if (contractQty > 0) {
                    if (outboundQty >= contractQty) {
                        outboundStatus = "COMPLETED";
                    } else if (outboundQty > 0 || pendingOutboundContractIds.contains(id)) {
                        outboundStatus = "PARTIAL";
                    }
                } else {
                    if (totalOutboundContractIds.contains(id) || pendingOutboundContractIds.contains(id)) {
                        outboundStatus = "PARTIAL";
                    }
                }
                item.put("outboundStatus", outboundStatus);
            }
        }
        
        return Result.success(new java.util.ArrayList<>(resultMap.values()));
    }
    
    @PostMapping("/recalculate")
    public Result<Boolean> recalculate() {
        java.util.List<CrmContract> contracts = crmContractService.list();
        for (CrmContract c : contracts) {
            if (c.getId() == null) continue;
            java.util.List<CrmFinance> finList = crmFinanceService.list(new LambdaQueryWrapper<CrmFinance>()
                    .eq(CrmFinance::getContractId, c.getId())
                    .isNotNull(CrmFinance::getArrivalTime));
            java.math.BigDecimal inTotal = java.math.BigDecimal.ZERO;
            java.math.BigDecimal outTotal = java.math.BigDecimal.ZERO;
            for (CrmFinance f : finList) {
                if ("REJECTED".equalsIgnoreCase(String.valueOf(f.getAuditStatus()))) {
                    continue;
                }
                java.math.BigDecimal amt = f.getArrivalAmount() != null ? f.getArrivalAmount() :
                        (f.getAmount() != null ? f.getAmount() : java.math.BigDecimal.ZERO);
                if ("IN".equalsIgnoreCase(f.getType())) {
                    inTotal = inTotal.add(amt);
                } else if ("OUT".equalsIgnoreCase(f.getType())) {
                    java.math.BigDecimal refund = java.math.BigDecimal.ZERO;
                    try {
                        String ef = f.getExtendFields();
                        if (ef != null && !ef.trim().isEmpty()) {
                            java.util.Map<String, Object> m = new com.fasterxml.jackson.databind.ObjectMapper()
                                    .readValue(ef, new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>>() {});
                            Object v = m.get("depositRefundAmount");
                            if (v != null) {
                                refund = new java.math.BigDecimal(String.valueOf(v)).setScale(2, java.math.RoundingMode.HALF_UP);
                            }
                        }
                    } catch (Exception ignored) {}
                    java.math.BigDecimal net = amt.subtract(refund);
                    if (net.compareTo(java.math.BigDecimal.ZERO) < 0) net = java.math.BigDecimal.ZERO;
                    outTotal = outTotal.add(net);
                }
            }
            c.setTotalCollection(inTotal);
            c.setTotalExpenditure(outTotal);
            java.math.BigDecimal contractAmount = c.getContractAmount() != null ? c.getContractAmount() : java.math.BigDecimal.ZERO;

            // 计算客户未支付的质保金（客户应该支付但还没支付的质保金）
            java.math.BigDecimal warrantyDepositIn = java.math.BigDecimal.ZERO;  // 客户已支付的质保金
            java.math.BigDecimal warrantyDepositRefund = java.math.BigDecimal.ZERO;  // 客户退回的质保金（不应该有这种情况）
            // 计算客户未退回的保证金（你们支付给客户的保证金，客户应该退回）
            java.math.BigDecimal depositOut = java.math.BigDecimal.ZERO;  // 支付给客户的保证金
            java.math.BigDecimal depositBack = java.math.BigDecimal.ZERO;  // 客户退回的保证金（从extend_fields中读取）

            for (CrmFinance f : finList) {
                if ("REJECTED".equalsIgnoreCase(String.valueOf(f.getAuditStatus()))) {
                    continue;
                }
                String category = String.valueOf(f.getCategory() != null ? f.getCategory() : "");
                String remark = String.valueOf(f.getRemark() != null ? f.getRemark() : "");
                String type = String.valueOf(f.getType() != null ? f.getType() : "");
                java.math.BigDecimal amt = f.getArrivalAmount() != null ? f.getArrivalAmount() :
                        (f.getAmount() != null ? f.getAmount() : java.math.BigDecimal.ZERO);

                // 识别客户未支付的质保金（客户应该支付给你们的质保金）
                if ("IN".equals(type)) {
                    if (category.contains("质保金收款") || (category.contains("收款") && remark.contains("质保金") && !remark.contains("退款") && !remark.contains("退回"))) {
                        warrantyDepositIn = warrantyDepositIn.add(amt);
                    }
                }

                // 识别你们支付给客户的保证金（客户应该退回给你们）
                if ("OUT".equals(type)) {
                    if (category.contains("保证金支出") || (category.contains("支出") && remark.contains("保证金"))) {
                        depositOut = depositOut.add(amt);
                        // 从extend_fields中读取已退还的保证金金额
                        try {
                            String ef = f.getExtendFields();
                            if (ef != null && !ef.trim().isEmpty()) {
                                java.util.Map<String, Object> m = new com.fasterxml.jackson.databind.ObjectMapper()
                                        .readValue(ef, new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>>() {});
                                Object refundAmount = m.get("depositRefundAmount");
                                if (refundAmount != null) {
                                    depositBack = depositBack.add(new java.math.BigDecimal(String.valueOf(refundAmount)));
                                }
                            }
                        } catch (Exception ignored) {}
                    }
                }
            }

            // 客户未支付的质保金 = 合同质保金部分 - 已收到的质保金
            // 客户未退回的保证金 = 支付的保证金 - 已退回的保证金
            java.math.BigDecimal unreceivedWarrantyDeposit = warrantyDepositIn.subtract(warrantyDepositRefund);
            java.math.BigDecimal unreturnedDeposit = depositOut.subtract(depositBack);
            if (unreceivedWarrantyDeposit.compareTo(java.math.BigDecimal.ZERO) < 0) {
                unreceivedWarrantyDeposit = java.math.BigDecimal.ZERO;
            }
            if (unreturnedDeposit.compareTo(java.math.BigDecimal.ZERO) < 0) {
                unreturnedDeposit = java.math.BigDecimal.ZERO;
            }

            // 应收款 = 合同金额 - 累计收款 + 客户未退回的保证金
            // 说明：客户未支付的质保金已经包含在"合同金额 - 累计收款"中
            // 需要额外加上的是客户未退回的保证金（你们支付给客户的，客户应该退回）
            c.setArrears(contractAmount.subtract(inTotal).add(unreturnedDeposit));
            // 统计已开票金额：已开票(DONE)和冲抵货款都算已开票
            java.util.List<CrmInvoice> invList = crmInvoiceService.list(new LambdaQueryWrapper<CrmInvoice>()
                    .eq(CrmInvoice::getContractId, c.getId())
                    .in(CrmInvoice::getInvoiceStatus, "DONE", "冲抵货款"));
            java.math.BigDecimal invoicedTotal = java.math.BigDecimal.ZERO;
            for (CrmInvoice inv : invList) {
                java.math.BigDecimal iv = inv.getInvoiceAmount() != null ? inv.getInvoiceAmount() : java.math.BigDecimal.ZERO;
                invoicedTotal = invoicedTotal.add(iv);
            }
            c.setInvoicedAmount(invoicedTotal);
            crmContractService.updateById(c);
        }
        return Result.success(true);
    }
    
    @PostMapping("/create-from-quote/{quoteId}")
    public Result<Long> createFromQuote(@PathVariable Long quoteId) {
        return Result.success(crmContractService.createFromQuote(quoteId));
    }
}
