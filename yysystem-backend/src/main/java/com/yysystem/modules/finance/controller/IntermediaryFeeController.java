package com.yysystem.modules.finance.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yysystem.common.result.Result;
import com.yysystem.modules.contract.entity.CrmContract;
import com.yysystem.modules.contract.service.CrmContractService;
import com.yysystem.modules.crm.entity.CrmCustomer;
import com.yysystem.modules.crm.service.CrmCustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 居间费管理控制器
 */
@RestController
@RequestMapping("/api/crm/intermediary-fee")
public class IntermediaryFeeController {

    @Autowired
    private CrmContractService contractService;

    @Autowired
    private CrmCustomerService customerService;

    /**
     * 获取居间费列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('IFM:list')")
    public Result<Page<IntermediaryFeeVO>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String contractNo,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String status) {

        QueryWrapper<CrmContract> wrapper = new QueryWrapper<>();
        // 只查询有居间费的合同
        wrapper.isNotNull("intermediary_fee");
        wrapper.gt("intermediary_fee", 0);

        if (contractNo != null && !contractNo.trim().isEmpty()) {
            wrapper.like("contract_no", contractNo.trim());
        }

        if (customerName != null && !customerName.trim().isEmpty()) {
            QueryWrapper<CrmCustomer> customerWrapper = new QueryWrapper<>();
            customerWrapper.like("customer_name", customerName.trim());
            List<CrmCustomer> customers = customerService.list(customerWrapper);
            if (!customers.isEmpty()) {
                List<Long> customerIds = customers.stream()
                    .map(CrmCustomer::getId)
                    .collect(Collectors.toList());
                wrapper.in("customer_id", customerIds);
            } else {
                wrapper.eq("id", -1L);
            }
        }

        if (status != null && !status.trim().isEmpty()) {
            wrapper.eq("intermediary_fee_status", status.trim());
        }

        wrapper.orderByDesc("create_time");

        Page<CrmContract> page = new Page<>(current, size);
        Page<CrmContract> result = contractService.page(page, wrapper);

        // 转换为 VO
        List<IntermediaryFeeVO> voList = new ArrayList<>();
        if (result.getRecords() != null && !result.getRecords().isEmpty()) {
            // 获取客户信息
            List<Long> customerIds = result.getRecords().stream()
                .map(CrmContract::getCustomerId)
                .distinct()
                .collect(Collectors.toList());

            Map<Long, CrmCustomer> customerMap = customerIds.isEmpty() ?
                Collections.emptyMap() :
                customerService.listByIds(customerIds).stream()
                    .collect(Collectors.toMap(CrmCustomer::getId, c -> c));

            for (CrmContract contract : result.getRecords()) {
                IntermediaryFeeVO vo = new IntermediaryFeeVO();
                vo.setId(contract.getId());
                vo.setContractNo(contract.getContractNo());
                vo.setContractName(contract.getContractName());
                vo.setContractAmount(contract.getContractAmount());
                vo.setIntermediaryFee(contract.getIntermediaryFee());
                vo.setIntermediaryCustomerId(contract.getIntermediaryCustomerId());
                vo.setIntermediaryCustomerName(contract.getIntermediaryCustomerName());
                vo.setIntermediaryFeeStatus(contract.getIntermediaryFeeStatus());
                vo.setCreateTime(contract.getCreateTime());

                // 获取客户名称
                if (contract.getCustomerId() != null) {
                    CrmCustomer customer = customerMap.get(contract.getCustomerId());
                    if (customer != null) {
                        vo.setCustomerName(customer.getCustomerName());
                    }
                }

                // 获取支付信息（从新字段）
                if (contract.getIntermediaryPayTime() != null) {
                    // 格式化为 yyyy-MM-dd HH:mm:ss
                    vo.setPayTime(contract.getIntermediaryPayTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                }
                vo.setPayMethod(contract.getIntermediaryPayMethod());
                vo.setPayRemark(contract.getIntermediaryPayRemark());

                // 兼容旧数据：解析 extendFields 获取支付信息（仅当新字段为空时）
                parseExtendFields(contract.getExtendFields(), vo);

                voList.add(vo);
            }
        }

        Page<IntermediaryFeeVO> voPage = new Page<>(current, size, result.getTotal());
        voPage.setRecords(voList);

        return Result.success(voPage);
    }

    /**
     * 获取居间费统计
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasAuthority('IFM:list')")
    public Result<IntermediaryFeeStatistics> statistics() {
        QueryWrapper<CrmContract> wrapper = new QueryWrapper<>();
        wrapper.isNotNull("intermediary_fee");
        wrapper.gt("intermediary_fee", 0);

        List<CrmContract> list = contractService.list(wrapper);

        BigDecimal totalFee = BigDecimal.ZERO;
        BigDecimal paidFee = BigDecimal.ZERO;
        BigDecimal unpaidFee = BigDecimal.ZERO;
        int totalCount = list.size();
        int paidCount = 0;
        int unpaidCount = 0;

        for (CrmContract contract : list) {
            BigDecimal fee = contract.getIntermediaryFee() != null ? contract.getIntermediaryFee() : BigDecimal.ZERO;
            totalFee = totalFee.add(fee);

            if ("PAID".equals(contract.getIntermediaryFeeStatus())) {
                paidFee = paidFee.add(fee);
                paidCount++;
            } else {
                unpaidFee = unpaidFee.add(fee);
                unpaidCount++;
            }
        }

        IntermediaryFeeStatistics stats = new IntermediaryFeeStatistics();
        stats.setTotalFee(totalFee);
        stats.setPaidFee(paidFee);
        stats.setUnpaidFee(unpaidFee);
        stats.setTotalCount(totalCount);
        stats.setPaidCount(paidCount);
        stats.setUnpaidCount(unpaidCount);

        return Result.success(stats);
    }

    /**
     * 更新居间费支付状态
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('IFM:update')")
    public Result<Boolean> updateStatus(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String status = String.valueOf(body.getOrDefault("status", "UNPAID"));
        String payRemark = body.get("payRemark") == null ? null : String.valueOf(body.get("payRemark"));
        String payMethod = body.get("payMethod") == null ? null : String.valueOf(body.get("payMethod"));
        BigDecimal payAmount = body.get("payAmount") != null ? new BigDecimal(String.valueOf(body.get("payAmount"))) : null;

        CrmContract contract = contractService.getById(id);
        if (contract == null) {
            return Result.error("合同不存在");
        }

        CrmContract update = new CrmContract();
        update.setId(id);
        update.setIntermediaryFeeStatus(status);

        // 使用新字段存储支付信息
        if ("PAID".equals(status)) {
            update.setIntermediaryPayTime(LocalDateTime.now());
            update.setIntermediaryPayMethod(payMethod);
            update.setIntermediaryPayRemark(payRemark);
        }

        // 同时更新 extendFields 用于历史记录
        Map<String, Object> ext = new HashMap<>();
        try {
            String raw = contract.getExtendFields();
            if (raw != null && !raw.trim().isEmpty()) {
                ext = new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(raw, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
            }
        } catch (Exception ignored) {}

        // 支付历史
        List<Map<String, Object>> history = new ArrayList<>();
        try {
            Object h = ext.get("intermediaryPayHistory");
            if (h instanceof List) {
                for (Object it : (List<?>) h) {
                    if (it instanceof Map) history.add(new HashMap<>((Map<String, Object>) it));
                }
            }
        } catch (Exception ignored) {}

        Map<String, Object> record = new HashMap<>();
        record.put("time", LocalDateTime.now().toString());
        record.put("status", status);
        record.put("method", payMethod);
        record.put("remark", payRemark);
        record.put("amount", payAmount != null ? payAmount.toString() : null);
        history.add(record);
        ext.put("intermediaryPayHistory", history);

        try {
            update.setExtendFields(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(ext));
        } catch (Exception ignored) {}

        boolean ok = contractService.updateById(update);
        return ok ? Result.success(true) : Result.error("更新失败");
    }

    private void parseExtendFields(String extendFields, IntermediaryFeeVO vo) {
        if (extendFields == null || extendFields.isEmpty()) {
            return;
        }
        try {
            Map<String, Object> ext = new com.fasterxml.jackson.databind.ObjectMapper()
                .readValue(extendFields, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});

            // 仅当新字段为空时，才使用旧数据
            if (vo.getPayTime() == null && ext.get("intermediaryPayTime") != null) {
                vo.setPayTime(String.valueOf(ext.get("intermediaryPayTime")));
            }
            if (vo.getPayInfo() == null && ext.get("intermediaryPayInfo") != null) {
                vo.setPayInfo(String.valueOf(ext.get("intermediaryPayInfo")));
            }
            if (ext.get("intermediaryPayHistory") != null) {
                vo.setPayHistory(ext.get("intermediaryPayHistory"));
            }
        } catch (Exception ignored) {}
    }

    /**
     * 居间费 VO
     */
    public static class IntermediaryFeeVO {
        private Long id;
        private String contractNo;
        private String contractName;
        private BigDecimal contractAmount;
        private String customerName;
        private BigDecimal intermediaryFee;
        private Long intermediaryCustomerId;
        private String intermediaryCustomerName;
        private String intermediaryFeeStatus;
        private String payTime;
        private String payInfo;
        private String payMethod;
        private String payRemark;
        private Object payHistory;
        private LocalDateTime createTime;

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getContractNo() { return contractNo; }
        public void setContractNo(String contractNo) { this.contractNo = contractNo; }
        public String getContractName() { return contractName; }
        public void setContractName(String contractName) { this.contractName = contractName; }
        public BigDecimal getContractAmount() { return contractAmount; }
        public void setContractAmount(BigDecimal contractAmount) { this.contractAmount = contractAmount; }
        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        public BigDecimal getIntermediaryFee() { return intermediaryFee; }
        public void setIntermediaryFee(BigDecimal intermediaryFee) { this.intermediaryFee = intermediaryFee; }
        public Long getIntermediaryCustomerId() { return intermediaryCustomerId; }
        public void setIntermediaryCustomerId(Long intermediaryCustomerId) { this.intermediaryCustomerId = intermediaryCustomerId; }
        public String getIntermediaryCustomerName() { return intermediaryCustomerName; }
        public void setIntermediaryCustomerName(String intermediaryCustomerName) { this.intermediaryCustomerName = intermediaryCustomerName; }
        public String getIntermediaryFeeStatus() { return intermediaryFeeStatus; }
        public void setIntermediaryFeeStatus(String intermediaryFeeStatus) { this.intermediaryFeeStatus = intermediaryFeeStatus; }
        public String getPayTime() { return payTime; }
        public void setPayTime(String payTime) { this.payTime = payTime; }
        public String getPayInfo() { return payInfo; }
        public void setPayInfo(String payInfo) { this.payInfo = payInfo; }
        public String getPayMethod() { return payMethod; }
        public void setPayMethod(String payMethod) { this.payMethod = payMethod; }
        public String getPayRemark() { return payRemark; }
        public void setPayRemark(String payRemark) { this.payRemark = payRemark; }
        public Object getPayHistory() { return payHistory; }
        public void setPayHistory(Object payHistory) { this.payHistory = payHistory; }
        public LocalDateTime getCreateTime() { return createTime; }
        public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    }

    /**
     * 居间费统计
     */
    public static class IntermediaryFeeStatistics {
        private BigDecimal totalFee;
        private BigDecimal paidFee;
        private BigDecimal unpaidFee;
        private int totalCount;
        private int paidCount;
        private int unpaidCount;

        // Getters and Setters
        public BigDecimal getTotalFee() { return totalFee; }
        public void setTotalFee(BigDecimal totalFee) { this.totalFee = totalFee; }
        public BigDecimal getPaidFee() { return paidFee; }
        public void setPaidFee(BigDecimal paidFee) { this.paidFee = paidFee; }
        public BigDecimal getUnpaidFee() { return unpaidFee; }
        public void setUnpaidFee(BigDecimal unpaidFee) { this.unpaidFee = unpaidFee; }
        public int getTotalCount() { return totalCount; }
        public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
        public int getPaidCount() { return paidCount; }
        public void setPaidCount(int paidCount) { this.paidCount = paidCount; }
        public int getUnpaidCount() { return unpaidCount; }
        public void setUnpaidCount(int unpaidCount) { this.unpaidCount = unpaidCount; }
    }
}
