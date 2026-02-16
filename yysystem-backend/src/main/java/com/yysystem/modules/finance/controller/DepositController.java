package com.yysystem.modules.finance.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yysystem.common.result.Result;
import com.yysystem.modules.contract.entity.CrmContract;
import com.yysystem.modules.contract.service.CrmContractService;
import com.yysystem.modules.crm.entity.CrmCustomer;
import com.yysystem.modules.crm.service.CrmCustomerService;
import com.yysystem.modules.finance.entity.CrmFinance;
import com.yysystem.modules.finance.service.CrmFinanceService;
import com.yysystem.modules.system.entity.SysUser;
import com.yysystem.modules.system.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 保证金管理控制器
 */
@RestController
@RequestMapping("/api/crm/deposit")
public class DepositController {

    @Autowired
    private CrmFinanceService financeService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private CrmContractService contractService;
    @Autowired
    private CrmCustomerService customerService;

    /**
     * 获取保证金列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('DeM:list')")
    public Result<Page<CrmFinance>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String depositType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long contractId,
            @RequestParam(required = false) String keyword) {
        
        QueryWrapper<CrmFinance> wrapper = new QueryWrapper<>();
        // 查询保证金支出和质保金支出（两者都归到保证金管理）
        wrapper.in("category", "保证金支出", "质保金支出");
        
        if (depositType != null && !depositType.isEmpty()) {
            wrapper.like("remark", depositType);
        }
        
        if (contractId != null) {
            wrapper.eq("contract_id", contractId);
        }
        
        // 状态筛选在查询后处理，因为状态需要根据 extend_fields 计算
        
        // 处理关键字搜索条件
        if (keyword != null && !keyword.trim().isEmpty()) {
            String searchKey = keyword.trim();
            
            // 1. 搜索合同编号
            QueryWrapper<CrmContract> contractWrapper = new QueryWrapper<>();
            contractWrapper.like("contract_no", searchKey);
            List<CrmContract> contracts = contractService.list(contractWrapper);
            final List<Long> finalContractIds = contracts.stream().map(CrmContract::getId).collect(Collectors.toList());
            
            // 2. 搜索客户名称
            QueryWrapper<CrmCustomer> customerWrapper = new QueryWrapper<>();
            customerWrapper.like("customer_name", searchKey);
            List<CrmCustomer> customers = customerService.list(customerWrapper);
            final List<Long> finalCustomerIds = customers.stream().map(CrmCustomer::getId).collect(Collectors.toList());
            
            // 构建 OR 条件：备注、明细、合同ID、客户ID
            wrapper.nested(w -> buildKeywordSearchCondition(w, searchKey, finalContractIds, finalCustomerIds));
        }
        
        wrapper.orderByDesc("create_time");
        
        // 如果有状态筛选，需要先查询所有数据再过滤
        List<CrmFinance> allRecords;
        long totalCount;
        
        if (status != null && !status.isEmpty()) {
            // 状态筛选：先查询所有数据
            allRecords = financeService.list(wrapper);
            
            // 填充合同编号和客户名称
            fillContractAndCustomerInfo(allRecords);
            
            // 状态筛选
            allRecords = allRecords.stream()
                .filter(finance -> status.equals(calculateDepositStatus(finance)))
                .collect(Collectors.toList());
            
            totalCount = allRecords.size();
            
            // 手动分页
            int fromIndex = (current - 1) * size;
            int toIndex = Math.min(fromIndex + size, allRecords.size());
            if (fromIndex < allRecords.size()) {
                allRecords = allRecords.subList(fromIndex, toIndex);
            } else {
                allRecords = new ArrayList<>();
            }
        } else {
            // 无状态筛选：正常分页查询
            Page<CrmFinance> page = new Page<>(current, size);
            Page<CrmFinance> result = financeService.page(page, wrapper);
            allRecords = result.getRecords();
            totalCount = result.getTotal();
            
            // 填充合同编号和客户名称
            fillContractAndCustomerInfo(allRecords);
        }
        
        // 构建返回结果
        Page<CrmFinance> resultPage = new Page<>(current, size, totalCount);
        resultPage.setRecords(allRecords);
        
        return Result.success(resultPage);
    }

    /**
     * 获取保证金详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('DeM:get')")
    public Result<CrmFinance> getById(@PathVariable Long id) {
        CrmFinance finance = financeService.getById(id);
        if (finance == null || 
            (!"保证金支出".equals(finance.getCategory()) && !"质保金支出".equals(finance.getCategory()))) {
            return Result.error("保证金记录不存在");
        }
        return Result.success(finance);
    }

    /**
     * 填充合同编号和客户名称
     */
    private void fillContractAndCustomerInfo(List<CrmFinance> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        
        // 获取所有合同ID和客户ID
        List<Long> allContractIds = records.stream()
            .map(CrmFinance::getContractId)
            .distinct()
            .collect(Collectors.toList());
        List<Long> allCustomerIds = records.stream()
            .map(CrmFinance::getCustomerId)
            .distinct()
            .collect(Collectors.toList());
        
        // 批量查询合同和客户信息
        Map<Long, CrmContract> contractMap = allContractIds.isEmpty() ? 
            java.util.Collections.emptyMap() :
            contractService.listByIds(allContractIds).stream()
                .collect(Collectors.toMap(CrmContract::getId, c -> c));
        Map<Long, CrmCustomer> customerMap = allCustomerIds.isEmpty() ?
            java.util.Collections.emptyMap() :
            customerService.listByIds(allCustomerIds).stream()
                .collect(Collectors.toMap(CrmCustomer::getId, c -> c));
        
        // 填充信息
        for (CrmFinance finance : records) {
            if (finance.getContractId() != null) {
                CrmContract contract = contractMap.get(finance.getContractId());
                if (contract != null) {
                    finance.setContractNo(contract.getContractNo());
                    finance.setContractName(contract.getContractName());
                }
            }
            if (finance.getCustomerId() != null) {
                CrmCustomer customer = customerMap.get(finance.getCustomerId());
                if (customer != null) {
                    finance.setCustomerName(customer.getCustomerName());
                }
            }
        }
    }

    /**
     * 新增保证金
     */
    @PostMapping
    @PreAuthorize("hasAuthority('DeM:create')")
    public Result<Void> save(@RequestBody CrmFinance finance) {
        finance.setCategory("保证金支出");
        finance.setType("OUT");
        finance.setAuditStatus("PENDING");
        finance.setCreateTime(LocalDateTime.now());
        financeService.save(finance);
        return Result.success();
    }

    /**
     * 更新保证金
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('DeM:update')")
    public Result<Void> update(@PathVariable Long id, @RequestBody CrmFinance finance) {
        finance.setId(id);
        financeService.updateById(finance);
        return Result.success();
    }

    /**
     * 删除保证金
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DeM:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        financeService.removeById(id);
        return Result.success();
    }

    /**
     * 保证金审核
     */
    @PostMapping("/{id}/audit")
    @PreAuthorize("hasAuthority('DeM:update')")
    public Result<Void> audit(@PathVariable Long id, @RequestBody DepositAuditRequest request) {
        CrmFinance finance = financeService.getById(id);
        if (finance == null) {
            return Result.error("保证金记录不存在");
        }
        
        finance.setAuditStatus(request.getAuditStatus());
        if ("PASSED".equals(request.getAuditStatus())) {
            if (request.getAmount() != null) {
                finance.setAmount(request.getAmount());
            }
            finance.setArrivalAmount(request.getArrivalAmount());
            finance.setArrivalTime(request.getArrivalTime());
            finance.setPaymentTime(request.getPaymentTime());
            finance.setPaymentMethod(request.getPaymentMethod());
        }
        if (request.getRemark() != null) {
            finance.setRemark(request.getRemark());
        }
        
        financeService.updateById(finance);
        return Result.success();
    }

    /**
     * 保证金退回
     */
    @PostMapping("/{id}/refund")
    @PreAuthorize("hasAuthority('DeM:update')")
    public Result<Void> refund(@PathVariable Long id, @RequestBody DepositRefundRequest request) {
        CrmFinance finance = financeService.getById(id);
        if (finance == null) {
            return Result.error("保证金记录不存在");
        }
        
        // 构建退回信息JSON
        String extendFields = buildRefundExtendFields(finance.getExtendFields(), request);
        finance.setExtendFields(extendFields);
        // 保存实际退还日期
        finance.setActualRefundDate(request.getActualRefundDate());
        
        financeService.updateById(finance);
        return Result.success();
    }

    /**
     * 保证金转为代理费
     */
    @PostMapping("/{id}/transfer")
    @PreAuthorize("hasAuthority('DeM:update')")
    public Result<Void> transferToAgencyFee(@PathVariable Long id, @RequestBody DepositTransferRequest request) {
        CrmFinance finance = financeService.getById(id);
        if (finance == null) {
            return Result.error("保证金记录不存在");
        }
        
        // 构建转代理费信息JSON
        String extendFields = buildTransferExtendFields(finance.getExtendFields(), request);
        finance.setExtendFields(extendFields);
        
        financeService.updateById(finance);
        return Result.success();
    }

    /**
     * 货款转保证金（从合同剩余货款转为保证金）
     */
    @PostMapping("/convert-from-payment")
    @PreAuthorize("hasAuthority('DeM:create')")
    public Result<Void> convertFromPayment(@RequestBody DepositConvertRequest request) {
        // 获取当前登录用户
        Long creatorId = null;
        String username = null;
        org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
            username = authentication.getName();
        }
        if (username != null) {
            SysUser user = sysUserService.getByUsername(username);
            if (user != null) {
                creatorId = user.getId();
            }
        }
        
        // 创建保证金记录
        CrmFinance finance = new CrmFinance();
        finance.setContractId(request.getContractId());
        finance.setCustomerId(request.getCustomerId());
        finance.setCompanyId(request.getCompanyId());
        finance.setCompanyName(request.getCompanyName());
        finance.setCategory("保证金支出");
        finance.setType("OUT");
        finance.setAmount(request.getAmount());
        finance.setRemark(request.getRemark());
        finance.setDepositSourceType("CONVERT"); // 货款转保证金
        finance.setRefundDueDate(request.getRefundDueDate());
        // 货款转保证金：支付时间为转换时间，支付方式为"货款转付"
        finance.setPaymentTime(LocalDateTime.now());
        finance.setPaymentMethod("货款转付");
        finance.setAuditStatus("PASSED"); // 货款转保证金直接审核通过
        finance.setArrivalTime(LocalDateTime.now());
        finance.setArrivalAmount(request.getAmount());
        finance.setCreateTime(LocalDateTime.now());
        finance.setCreatorId(creatorId);
        
        financeService.save(finance);
        return Result.success();
    }

    /**
     * 获取保证金统计
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('DeM:list')")
    public Result<DepositStatistics> statistics() {
        QueryWrapper<CrmFinance> wrapper = new QueryWrapper<>();
        // 查询保证金支出和质保金支出
        wrapper.in("category", "保证金支出", "质保金支出");
        
        List<CrmFinance> list = financeService.list(wrapper);
        
        BigDecimal totalDeposit = BigDecimal.ZERO;
        BigDecimal totalRefund = BigDecimal.ZERO;
        int pendingCount = 0;
        int refundedCount = 0;
        
        for (CrmFinance finance : list) {
            totalDeposit = totalDeposit.add(finance.getAmount() != null ? finance.getAmount() : BigDecimal.ZERO);
            
            // 解析extendFields获取退回金额
            BigDecimal refundAmount = parseRefundAmount(finance.getExtendFields());
            if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
                totalRefund = totalRefund.add(refundAmount);
                refundedCount++;
            } else {
                pendingCount++;
            }
        }
        
        DepositStatistics stats = new DepositStatistics();
        stats.setTotalDeposit(totalDeposit);
        stats.setTotalRefund(totalRefund);
        stats.setTotalPending(totalDeposit.subtract(totalRefund));
        stats.setPendingCount(pendingCount);
        stats.setRefundedCount(refundedCount);
        
        return Result.success(stats);
    }

    private String buildRefundExtendFields(String existingFields, DepositRefundRequest request) {
        // 使用 StringBuilder 构建 JSON
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        // 读取并清理现有数据
        String existingJson = "";
        if (existingFields != null && !existingFields.isEmpty() && !"{}".equals(existingFields)) {
            try {
                // 尝试解析现有 JSON，提取有效的 clientRequestId
                if (existingFields.contains("clientRequestId")) {
                    int start = existingFields.indexOf("\"clientRequestId\":\"") + 18;
                    int end = existingFields.indexOf("\"", start);
                    if (start > 17 && end > start) {
                        String clientRequestId = existingFields.substring(start, end);
                        existingJson = "\"clientRequestId\":\"" + clientRequestId + "\"";
                    }
                }
            } catch (Exception e) {
                // 解析失败，使用空字符串
            }
        }
        if (!existingJson.isEmpty()) {
            sb.append(existingJson).append(",");
        }
        // 退回时间使用实际退还日期
        String refundTime = request.getActualRefundDate() != null ? request.getActualRefundDate().toString() : "";
        String refundRemark = request.getRefundRemark() != null ? request.getRefundRemark() : "";
        sb.append("\"depositRefundTime\":\"").append(refundTime).append("\",");
        sb.append("\"depositRefundAmount\":").append(request.getRefundAmount() != null ? request.getRefundAmount() : 0).append(",");
        sb.append("\"depositRefundRemark\":\"\"");
        sb.append("}");
        return sb.toString();
    }

    private String buildTransferExtendFields(String existingFields, DepositTransferRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        if (existingFields != null && !existingFields.isEmpty() && !"{}".equals(existingFields)) {
            String content = existingFields.substring(1, existingFields.length() - 1);
            sb.append(content);
            sb.append(",");
        }
        sb.append("\"transferToAgencyFee\":true,");
        sb.append("\"transferTime\":\"").append(request.getTransferTime()).append("\",");
        sb.append("\"transferAmount\":").append(request.getTransferAmount()).append(",");
        sb.append("\"transferRemark\":\"").append(request.getTransferRemark() != null ? request.getTransferRemark() : "").append("\"");
        sb.append("}");
        return sb.toString();
    }

    /**
     * 计算保证金状态
     * PENDING: 待退回
     * REFUNDED: 已退回
     * TRANSFERRED: 已转代理费
     */
    private String calculateDepositStatus(CrmFinance finance) {
        String extendFields = finance.getExtendFields();
        if (extendFields == null || extendFields.isEmpty()) {
            return "PENDING";
        }
        
        // 检查是否转为代理费
        if (extendFields.contains("\"transferToAgencyFee\":true")) {
            return "TRANSFERRED";
        }
        
        // 检查是否有退回金额
        BigDecimal refundAmount = parseRefundAmount(extendFields);
        if (refundAmount.compareTo(BigDecimal.ZERO) > 0) {
            // 如果退回金额大于等于保证金金额，视为已退回
            if (finance.getAmount() != null && refundAmount.compareTo(finance.getAmount()) >= 0) {
                return "REFUNDED";
            }
        }
        
        return "PENDING";
    }

    private BigDecimal parseRefundAmount(String extendFields) {
        if (extendFields == null || extendFields.isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            // 简单解析，实际应该使用JSON库
            if (extendFields.contains("\"depositRefundAmount\":")) {
                int start = extendFields.indexOf("\"depositRefundAmount\":") + "\"depositRefundAmount\":".length();
                int end = extendFields.indexOf(",", start);
                if (end == -1) {
                    end = extendFields.indexOf("}", start);
                }
                String amountStr = extendFields.substring(start, end).trim();
                return new BigDecimal(amountStr);
            }
        } catch (Exception e) {
            // 解析失败返回0
        }
        return BigDecimal.ZERO;
    }

    private void buildKeywordSearchCondition(QueryWrapper<CrmFinance> wrapper, String searchKey, List<Long> contractIds, List<Long> customerIds) {
        wrapper.like("remark", searchKey)
               .or()
               .like("collection_detail", searchKey);
        if (contractIds != null && !contractIds.isEmpty()) {
            wrapper.or().in("contract_id", contractIds);
        }
        if (customerIds != null && !customerIds.isEmpty()) {
            wrapper.or().in("customer_id", customerIds);
        }
    }

    // 内部请求类
    public static class DepositRefundRequest {
        private String refundTime;
        private LocalDate actualRefundDate;
        private BigDecimal refundAmount;
        private String refundRemark;
        
        public String getRefundTime() { return refundTime; }
        public void setRefundTime(String refundTime) { this.refundTime = refundTime; }
        public LocalDate getActualRefundDate() { return actualRefundDate; }
        public void setActualRefundDate(LocalDate actualRefundDate) { this.actualRefundDate = actualRefundDate; }
        public BigDecimal getRefundAmount() { return refundAmount; }
        public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }
        public String getRefundRemark() { return refundRemark; }
        public void setRefundRemark(String refundRemark) { this.refundRemark = refundRemark; }
    }

    public static class DepositTransferRequest {
        private String transferTime;
        private BigDecimal transferAmount;
        private String transferRemark;
        
        public String getTransferTime() { return transferTime; }
        public void setTransferTime(String transferTime) { this.transferTime = transferTime; }
        public BigDecimal getTransferAmount() { return transferAmount; }
        public void setTransferAmount(BigDecimal transferAmount) { this.transferAmount = transferAmount; }
        public String getTransferRemark() { return transferRemark; }
        public void setTransferRemark(String transferRemark) { this.transferRemark = transferRemark; }
    }

    public static class DepositStatistics {
        private BigDecimal totalDeposit;
        private BigDecimal totalRefund;
        private BigDecimal totalPending;
        private int pendingCount;
        private int refundedCount;
        
        public BigDecimal getTotalDeposit() { return totalDeposit; }
        public void setTotalDeposit(BigDecimal totalDeposit) { this.totalDeposit = totalDeposit; }
        public BigDecimal getTotalRefund() { return totalRefund; }
        public void setTotalRefund(BigDecimal totalRefund) { this.totalRefund = totalRefund; }
        public BigDecimal getTotalPending() { return totalPending; }
        public void setTotalPending(BigDecimal totalPending) { this.totalPending = totalPending; }
        public int getPendingCount() { return pendingCount; }
        public void setPendingCount(int pendingCount) { this.pendingCount = pendingCount; }
        public int getRefundedCount() { return refundedCount; }
        public void setRefundedCount(int refundedCount) { this.refundedCount = refundedCount; }
    }

    // 货款转保证金请求类
    public static class DepositConvertRequest {
        private Long contractId;
        private Long customerId;
        private Long companyId;
        private String companyName;
        private BigDecimal amount;
        private String remark;
        private LocalDate refundDueDate;
        
        public Long getContractId() { return contractId; }
        public void setContractId(Long contractId) { this.contractId = contractId; }
        public Long getCustomerId() { return customerId; }
        public void setCustomerId(Long customerId) { this.customerId = customerId; }
        public Long getCompanyId() { return companyId; }
        public void setCompanyId(Long companyId) { this.companyId = companyId; }
        public String getCompanyName() { return companyName; }
        public void setCompanyName(String companyName) { this.companyName = companyName; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
        public LocalDate getRefundDueDate() { return refundDueDate; }
        public void setRefundDueDate(LocalDate refundDueDate) { this.refundDueDate = refundDueDate; }
    }

    // 保证金审核请求类
    public static class DepositAuditRequest {
        private String auditStatus;
        private BigDecimal amount;
        private BigDecimal arrivalAmount;
        private LocalDateTime arrivalTime;
        private LocalDateTime paymentTime;
        private String paymentMethod;
        private String remark;
        
        public String getAuditStatus() { return auditStatus; }
        public void setAuditStatus(String auditStatus) { this.auditStatus = auditStatus; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public BigDecimal getArrivalAmount() { return arrivalAmount; }
        public void setArrivalAmount(BigDecimal arrivalAmount) { this.arrivalAmount = arrivalAmount; }
        public LocalDateTime getArrivalTime() { return arrivalTime; }
        public void setArrivalTime(LocalDateTime arrivalTime) { this.arrivalTime = arrivalTime; }
        public LocalDateTime getPaymentTime() { return paymentTime; }
        public void setPaymentTime(LocalDateTime paymentTime) { this.paymentTime = paymentTime; }
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
    }
}
