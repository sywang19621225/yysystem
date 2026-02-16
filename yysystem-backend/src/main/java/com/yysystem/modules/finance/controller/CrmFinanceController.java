package com.yysystem.modules.finance.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yysystem.common.result.Result;
import com.yysystem.modules.crm.entity.CrmCustomer;
import com.yysystem.modules.crm.service.CrmCustomerService;
import com.yysystem.modules.finance.entity.CrmFinance;
import com.yysystem.modules.finance.service.CrmFinanceService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yysystem.modules.system.entity.SysUser;
import com.yysystem.modules.system.service.SysUserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/crm/finance")
public class CrmFinanceController {

    @Autowired
    private CrmFinanceService crmFinanceService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private CrmCustomerService crmCustomerService;

    @PostMapping
    @PreAuthorize("hasAuthority('IER:create')")
    public Result<Long> save(@RequestBody CrmFinance crmFinance) {
        return Result.success(crmFinanceService.saveFinance(crmFinance));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('IER:update')")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody CrmFinance crmFinance) {
        crmFinance.setId(id);
        return Result.success(crmFinanceService.updateFinance(crmFinance));
    }
    
    @PutMapping("/{id}/audit")
    @PreAuthorize("hasAuthority('IER:update')")
    public Result<Boolean> audit(@PathVariable Long id, @RequestParam String status, @RequestParam(required = false) String detail) {
        return Result.success(crmFinanceService.auditFinance(id, status, detail));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('IER:delete')")
    public Result<Boolean> remove(@PathVariable Long id) {
        return Result.success(crmFinanceService.removeById(id));
    }

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('IER:list')")
    public Result<Page<CrmFinance>> list(@RequestParam(defaultValue = "1") Integer current,
                                         @RequestParam(defaultValue = "10") Integer size,
                                         @RequestParam(required = false) Long contractId,
                                         @RequestParam(required = false) Long customerId,
                                         @RequestParam(required = false) String type,
                                         @RequestParam(required = false) String auditStatus,
                                         @RequestParam(required = false) String customerName) {
        LambdaQueryWrapper<CrmFinance> wrapper = new LambdaQueryWrapper<>();
        // 排除保证金支出（保证金支出归到保证金管理页面处理）
        wrapper.ne(CrmFinance::getCategory, "保证金支出");
        if (contractId != null) {
            wrapper.eq(CrmFinance::getContractId, contractId);
        }
        if (customerId != null) {
            wrapper.eq(CrmFinance::getCustomerId, customerId);
        }
        // 客户名称模糊查询
        if (customerName != null && !customerName.trim().isEmpty()) {
            // 先查询匹配的客户ID列表
            LambdaQueryWrapper<CrmCustomer> customerWrapper = new LambdaQueryWrapper<>();
            customerWrapper.like(CrmCustomer::getCustomerName, customerName.trim());
            List<CrmCustomer> customers = crmCustomerService.list(customerWrapper);
            if (!customers.isEmpty()) {
                List<Long> customerIds = customers.stream().map(CrmCustomer::getId).collect(Collectors.toList());
                wrapper.in(CrmFinance::getCustomerId, customerIds);
            } else {
                // 没有匹配的客户，返回空结果
                return Result.success(new Page<>(current, size));
            }
        }
        if (type != null && !type.trim().isEmpty()) {
            wrapper.eq(CrmFinance::getType, type.trim());
        }
        if (auditStatus != null && !auditStatus.trim().isEmpty()) {
            wrapper.eq(CrmFinance::getAuditStatus, auditStatus.trim());
        }
        try {
            String username = null;
            org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
                username = authentication.getName();
            }
            if (username != null && !"admin".equalsIgnoreCase(username)) {
                SysUser user = sysUserService.getByUsername(username);
                if (user != null) {
                    String ut = user.getUserType();
                    boolean isFinance = ut != null && "finance".equalsIgnoreCase(ut);
                    if (!isFinance && user.getId() != null) {
                        wrapper.eq(CrmFinance::getCreatorId, user.getId());
                    }
                }
            }
        } catch (Exception ignored) {}
        wrapper.orderByDesc(CrmFinance::getCreateTime, CrmFinance::getId);
        return Result.success(crmFinanceService.page(new Page<>(current, size), wrapper));
    }

    /**
     * 获取统计数据（收入总额、支出总额）
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('IER:list')")
    public Result<FinanceStatistics> statistics(@RequestParam(required = false) Long contractId,
                                                 @RequestParam(required = false) Long customerId,
                                                 @RequestParam(required = false) String type,
                                                 @RequestParam(required = false) String auditStatus,
                                                 @RequestParam(required = false) String customerName) {
        LambdaQueryWrapper<CrmFinance> wrapper = new LambdaQueryWrapper<>();
        // 排除保证金支出
        wrapper.ne(CrmFinance::getCategory, "保证金支出");
        
        if (contractId != null) {
            wrapper.eq(CrmFinance::getContractId, contractId);
        }
        if (customerId != null) {
            wrapper.eq(CrmFinance::getCustomerId, customerId);
        }
        // 客户名称模糊查询
        if (customerName != null && !customerName.trim().isEmpty()) {
            LambdaQueryWrapper<CrmCustomer> customerWrapper = new LambdaQueryWrapper<>();
            customerWrapper.like(CrmCustomer::getCustomerName, customerName.trim());
            List<CrmCustomer> customers = crmCustomerService.list(customerWrapper);
            if (!customers.isEmpty()) {
                List<Long> customerIds = customers.stream().map(CrmCustomer::getId).collect(Collectors.toList());
                wrapper.in(CrmFinance::getCustomerId, customerIds);
            } else {
                wrapper.eq(CrmFinance::getId, -1L);
            }
        }
        if (type != null && !type.trim().isEmpty()) {
            wrapper.eq(CrmFinance::getType, type.trim());
        }
        if (auditStatus != null && !auditStatus.trim().isEmpty()) {
            wrapper.eq(CrmFinance::getAuditStatus, auditStatus.trim());
        }
        
        List<CrmFinance> list = crmFinanceService.list(wrapper);
        
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        int pendingCount = 0;
        
        for (CrmFinance finance : list) {
            BigDecimal amount = finance.getAmount() != null ? finance.getAmount() : BigDecimal.ZERO;
            if ("IN".equals(finance.getType())) {
                totalIncome = totalIncome.add(amount);
            } else if ("OUT".equals(finance.getType())) {
                totalExpense = totalExpense.add(amount);
            }
            if (finance.getArrivalTime() == null) {
                pendingCount++;
            }
        }
        
        FinanceStatistics stats = new FinanceStatistics();
        stats.setTotalIncome(totalIncome);
        stats.setTotalExpense(totalExpense);
        stats.setPendingCount(pendingCount);
        stats.setTotalCount(list.size());
        
        return Result.success(stats);
    }

    /**
     * 导出收支数据
     */
    @GetMapping("/export")
    @PreAuthorize("hasAuthority('IER:list')")
    public void export(HttpServletResponse response,
                       @RequestParam(required = false) Long contractId,
                       @RequestParam(required = false) Long customerId,
                       @RequestParam(required = false) String type,
                       @RequestParam(required = false) String auditStatus,
                       @RequestParam(required = false) String customerName) throws Exception {
        LambdaQueryWrapper<CrmFinance> wrapper = new LambdaQueryWrapper<>();
        if (contractId != null) {
            wrapper.eq(CrmFinance::getContractId, contractId);
        }
        if (customerId != null) {
            wrapper.eq(CrmFinance::getCustomerId, customerId);
        }
        // 客户名称模糊查询
        if (customerName != null && !customerName.trim().isEmpty()) {
            LambdaQueryWrapper<CrmCustomer> customerWrapper = new LambdaQueryWrapper<>();
            customerWrapper.like(CrmCustomer::getCustomerName, customerName.trim());
            List<CrmCustomer> customers = crmCustomerService.list(customerWrapper);
            if (!customers.isEmpty()) {
                List<Long> customerIds = customers.stream().map(CrmCustomer::getId).collect(Collectors.toList());
                wrapper.in(CrmFinance::getCustomerId, customerIds);
            } else {
                wrapper.eq(CrmFinance::getId, -1L);
            }
        }
        if (type != null && !type.trim().isEmpty()) {
            wrapper.eq(CrmFinance::getType, type.trim());
        }
        if (auditStatus != null && !auditStatus.trim().isEmpty()) {
            wrapper.eq(CrmFinance::getAuditStatus, auditStatus.trim());
        }
        wrapper.orderByDesc(CrmFinance::getCreateTime, CrmFinance::getId);
        List<CrmFinance> list = crmFinanceService.list(wrapper);

        // 获取所有客户信息
        List<Long> allCustomerIds = list.stream()
            .map(CrmFinance::getCustomerId)
            .filter(id -> id != null)
            .distinct()
            .collect(Collectors.toList());
        List<CrmCustomer> customers = allCustomerIds.isEmpty() ? 
            java.util.Collections.emptyList() : 
            crmCustomerService.listByIds(allCustomerIds);
        java.util.Map<Long, String> customerNameMap = customers.stream()
            .collect(Collectors.toMap(CrmCustomer::getId, CrmCustomer::getCustomerName, (a, b) -> a));

        List<FinanceExportData> exportDataList = list.stream().map(finance -> {
            FinanceExportData data = new FinanceExportData();
            data.setId(finance.getId());
            data.setType("IN".equals(finance.getType()) ? "收入" : "支出");
            data.setAmount(finance.getAmount() != null ? finance.getAmount() : BigDecimal.ZERO);
            data.setCustomerName(customerNameMap.getOrDefault(finance.getCustomerId(), "-"));
            data.setCategory(finance.getCategory() != null ? finance.getCategory() : "-");
            data.setRemark(finance.getRemark() != null ? finance.getRemark() : "-");
            data.setAuditStatus(getAuditStatusText(finance.getAuditStatus()));
            data.setCollectionDetail(finance.getCollectionDetail() != null ? finance.getCollectionDetail() : "-");
            data.setArrivalTime(finance.getArrivalTime() != null ? finance.getArrivalTime() : null);
            data.setCreateTime(finance.getCreateTime() != null ? finance.getCreateTime() : null);
            return data;
        }).collect(Collectors.toList());

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("销售收支数据", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        EasyExcel.write(response.getOutputStream(), FinanceExportData.class).sheet("收支数据").doWrite(exportDataList);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('IER:get')")
    public Result<CrmFinance> getById(@PathVariable Long id) {
        CrmFinance f = crmFinanceService.getById(id);
        if (f == null) return Result.success(null);
        String username = null;
        org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
            username = authentication.getName();
        }
        if (username != null && !"admin".equalsIgnoreCase(username)) {
            SysUser user = sysUserService.getByUsername(username);
            if (user != null) {
                String ut = user.getUserType();
                boolean isFinance = ut != null && "finance".equalsIgnoreCase(ut);
                if (!isFinance && user.getId() != null && f.getCreatorId() != null && !f.getCreatorId().equals(user.getId())) {
                    return Result.error("无权查看他人收支记录");
                }
            }
        }
        return Result.success(f);
    }

    private String getAuditStatusText(String status) {
        if (status == null) return "-";
        switch (status.toUpperCase()) {
            case "PENDING": return "待审核";
            case "APPROVED": return "已审核";
            case "REJECTED": return "已驳回";
            default: return status;
        }
    }

    /**
     * 统计数据类
     */
    public static class FinanceStatistics {
        private BigDecimal totalIncome;
        private BigDecimal totalExpense;
        private int pendingCount;
        private int totalCount;
        
        public BigDecimal getTotalIncome() { return totalIncome; }
        public void setTotalIncome(BigDecimal totalIncome) { this.totalIncome = totalIncome; }
        public BigDecimal getTotalExpense() { return totalExpense; }
        public void setTotalExpense(BigDecimal totalExpense) { this.totalExpense = totalExpense; }
        public int getPendingCount() { return pendingCount; }
        public void setPendingCount(int pendingCount) { this.pendingCount = pendingCount; }
        public int getTotalCount() { return totalCount; }
        public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
    }

    /**
     * 导出数据类
     */
    public static class FinanceExportData {
        @ExcelProperty("ID")
        private Long id;

        @ExcelProperty("类型")
        private String type;

        @ExcelProperty("金额")
        private BigDecimal amount;

        @ExcelProperty("客户名称")
        private String customerName;

        @ExcelProperty("分类")
        private String category;

        @ExcelProperty("备注")
        private String remark;

        @ExcelProperty("审核状态")
        private String auditStatus;

        @ExcelProperty("明细")
        private String collectionDetail;

        @ExcelProperty("到账时间")
        private LocalDateTime arrivalTime;

        @ExcelProperty("创建时间")
        private LocalDateTime createTime;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
        public String getAuditStatus() { return auditStatus; }
        public void setAuditStatus(String auditStatus) { this.auditStatus = auditStatus; }
        public String getCollectionDetail() { return collectionDetail; }
        public void setCollectionDetail(String collectionDetail) { this.collectionDetail = collectionDetail; }
        public LocalDateTime getArrivalTime() { return arrivalTime; }
        public void setArrivalTime(LocalDateTime arrivalTime) { this.arrivalTime = arrivalTime; }
        public LocalDateTime getCreateTime() { return createTime; }
        public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    }
}
