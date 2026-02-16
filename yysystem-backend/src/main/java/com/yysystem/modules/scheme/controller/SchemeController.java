package com.yysystem.modules.scheme.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yysystem.common.result.Result;
import com.yysystem.modules.crm.entity.CrmCustomer;
import com.yysystem.modules.crm.service.CrmCustomerService;
import com.yysystem.modules.scheme.entity.CrmScheme;
import com.yysystem.modules.scheme.entity.CrmSchemeApproval;
import com.yysystem.modules.scheme.entity.CrmSchemeLog;
import com.yysystem.modules.scheme.mapper.CrmSchemeApprovalMapper;
import com.yysystem.modules.scheme.mapper.CrmSchemeLogMapper;
import com.yysystem.modules.scheme.service.LLMService;
import com.yysystem.modules.scheme.service.impl.SchemeServiceImpl;
import com.yysystem.modules.system.service.SysConfigService;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/scheme")
public class SchemeController {

    @Autowired
    private SchemeServiceImpl schemeService;

    @Autowired
    private CrmCustomerService customerService;

    @Autowired
    private LLMService llmService;

    @Autowired
    private SysConfigService sysConfigService;

    @Autowired
    private CrmSchemeApprovalMapper approvalMapper;

    @Autowired
    private CrmSchemeLogMapper logMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('SOL:list')")
    public Result<Page<CrmScheme>> list(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        Page<CrmScheme> page = new Page<>(current, size);
        QueryWrapper<CrmScheme> wrapper = new QueryWrapper<>();
        wrapper.eq("deleted", 0);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like("title", keyword)
                    .or()
                    .like("customer_name", keyword)
                    .or()
                    .like("scheme_no", keyword));
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("create_time");
        return Result.success(schemeService.page(page, wrapper));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SOL:get')")
    public Result<CrmScheme> getById(@PathVariable Long id) {
        return Result.success(schemeService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SOL:create')")
    public Result<Long> save(@RequestBody CrmScheme entity, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        String userName = getCurrentUserName(request);

        String schemeNo = generateSchemeNo();
        entity.setSchemeNo(schemeNo);
        entity.setSalesId(userId);
        entity.setSalesName(userName);
        entity.setStatus(10);
        entity.setCreateBy(userId);

        if (entity.getCustomerId() != null) {
            CrmCustomer customer = customerService.getById(entity.getCustomerId());
            if (customer != null) {
                entity.setCustomerName(customer.getCustomerName());
            }
        }

        schemeService.save(entity);

        // 记录操作日志
        schemeService.saveOperationLog(entity.getId(), "创建", "创建方案",
                userId, userName, null, "创建方案：" + entity.getTitle());

        return Result.success(entity.getId());
    }

    @PutMapping
    @PreAuthorize("hasAuthority('SOL:update')")
    public Result<Boolean> update(@RequestBody CrmScheme entity, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        String userName = getCurrentUserName(request);
        entity.setUpdateBy(userId);

        if (entity.getCustomerId() != null) {
            CrmCustomer customer = customerService.getById(entity.getCustomerId());
            if (customer != null) {
                entity.setCustomerName(customer.getCustomerName());
            }
        }

        CrmScheme oldScheme = schemeService.getById(entity.getId());
        boolean result = schemeService.updateById(entity);

        // 记录操作日志
        schemeService.saveOperationLog(entity.getId(), "修改", "修改方案信息",
                userId, userName, objectToJson(oldScheme), objectToJson(entity));

        return Result.success(result);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SOL:delete')")
    public Result<Boolean> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        String userName = getCurrentUserName(request);

        boolean result = schemeService.removeById(id);

        // 记录操作日志
        schemeService.saveOperationLog(id, "删除", "删除方案",
                userId, userName, null, "方案已删除");

        return Result.success(result);
    }

    /**
     * AI生成方案文本
     */
    @PostMapping("/{id}/generate-ai")
    @PreAuthorize("hasAuthority('SOL:update')")
    public Result<String> generateAI(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        String userName = getCurrentUserName(request);

        CrmScheme scheme = schemeService.getById(id);
        if (scheme == null) {
            return Result.error("方案不存在");
        }

        String customerName = scheme.getCustomerName();
        String requirements = scheme.getCustomerRequirements();

        String generatedText = llmService.generateSchemeText(customerName, requirements);

        scheme.setTextContentAi(generatedText);
        schemeService.updateById(scheme);

        // 记录操作日志
        schemeService.saveOperationLog(id, "AI生成", "AI生成方案文本",
                userId, userName, null, "AI生成方案文本完成");

        return Result.success(generatedText);
    }

    /**
     * AI生成解决方案（创建方案时使用）
     */
    @PostMapping("/generate-solution")
    @PreAuthorize("hasAuthority('SOL:create')")
    public Result<String> generateSolution(@RequestBody java.util.Map<String, Object> params, HttpServletRequest request) {
        String customerRequirements = (String) params.get("customerRequirements");
        Long customerId = params.get("customerId") != null ? Long.valueOf(params.get("customerId").toString()) : null;
        
        if (customerRequirements == null || customerRequirements.isEmpty()) {
            return Result.error("客户要求不能为空");
        }

        String customerName = null;
        if (customerId != null) {
            CrmCustomer customer = customerService.getById(customerId);
            if (customer != null) {
                customerName = customer.getCustomerName();
            }
        }

        String generatedSolution = llmService.generateSchemeText(customerName, customerRequirements);

        return Result.success(generatedSolution);
    }

    /**
     * 提交设计
     */
    @PostMapping("/{id}/submit-design")
    @PreAuthorize("hasAuthority('SOL:update')")
    public Result<Boolean> submitDesign(@PathVariable Long id, @RequestParam Long designerId,
                                        @RequestParam String designerName, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        String userName = getCurrentUserName(request);

        CrmScheme scheme = schemeService.getById(id);
        if (scheme == null) {
            return Result.error("方案不存在");
        }
        if (scheme.getStatus() != 10) {
            return Result.error("只有草稿状态可以提交设计");
        }

        Integer oldStatus = scheme.getStatus();
        scheme.setStatus(20);
        scheme.setDesignerId(designerId);
        scheme.setDesignerName(designerName);
        boolean result = schemeService.updateById(scheme);

        // 记录操作日志
        schemeService.saveOperationLog(id, "提交设计", "提交设计方案",
                userId, userName, "状态：" + oldStatus, "状态：20，设计师：" + designerName);

        return Result.success(result);
    }

    /**
     * 提交审批
     */
    @PostMapping("/{id}/submit-approval")
    @PreAuthorize("hasAuthority('SOL:update')")
    public Result<Boolean> submitApproval(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        String userName = getCurrentUserName(request);

        CrmScheme scheme = schemeService.getById(id);
        if (scheme == null) {
            return Result.error("方案不存在");
        }
        if (scheme.getStatus() != 20) {
            return Result.error("只有设计中状态可以提交审批");
        }

        Integer oldStatus = scheme.getStatus();
        scheme.setStatus(30);
        boolean result = schemeService.updateById(scheme);

        // 记录操作日志
        schemeService.saveOperationLog(id, "提交审批", "提交方案审批",
                userId, userName, "状态：" + oldStatus, "状态：30");

        return Result.success(result);
    }

    /**
     * 审批方案
     */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('SOL:approve')")
    public Result<Boolean> approve(@PathVariable Long id, @RequestBody CrmSchemeApproval approval,
                                   HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        String userName = getCurrentUserName(request);

        CrmScheme scheme = schemeService.getById(id);
        if (scheme == null) {
            return Result.error("方案不存在");
        }
        if (scheme.getStatus() != 30) {
            return Result.error("只有待审批状态可以进行审批");
        }

        Integer oldStatus = scheme.getStatus();

        // 更新方案状态
        if (approval.getApprovalStatus() == 1) {
            scheme.setStatus(50);
        } else {
            scheme.setStatus(40);
        }
        scheme.setApproverId(userId);
        scheme.setApproverName(userName);
        scheme.setApprovalOpinion(approval.getApprovalOpinion());
        scheme.setApprovalTime(java.time.LocalDateTime.now());
        schemeService.updateById(scheme);

        // 保存审批记录
        approval.setSchemeId(id);
        approval.setApproverId(userId);
        approval.setApproverName(userName);
        schemeService.saveApproval(approval);

        // 记录操作日志
        String action = approval.getApprovalStatus() == 1 ? "审批通过" : "审批驳回";
        schemeService.saveOperationLog(id, "审批", action,
                userId, userName, "状态：" + oldStatus,
                "状态：" + scheme.getStatus() + "，意见：" + approval.getApprovalOpinion());

        return Result.success(true);
    }

    /**
     * 报送客户
     */
    @PostMapping("/{id}/deliver")
    @PreAuthorize("hasAuthority('SOL:update')")
    public Result<Boolean> deliver(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        String userName = getCurrentUserName(request);

        CrmScheme scheme = schemeService.getById(id);
        if (scheme == null) {
            return Result.error("方案不存在");
        }
        if (scheme.getStatus() != 50) {
            return Result.error("只有通过状态可以报送客户");
        }

        Integer oldStatus = scheme.getStatus();
        scheme.setStatus(60);
        scheme.setDeliverTime(java.time.LocalDateTime.now());
        boolean result = schemeService.updateById(scheme);

        // 记录操作日志
        schemeService.saveOperationLog(id, "报送", "报送客户",
                userId, userName, "状态：" + oldStatus, "状态：60");

        return Result.success(result);
    }

    /**
     * 查询审批记录
     */
    @GetMapping("/{id}/approvals")
    @PreAuthorize("hasAuthority('SOL:get')")
    public Result<List<CrmSchemeApproval>> getApprovals(@PathVariable Long id) {
        QueryWrapper<CrmSchemeApproval> wrapper = new QueryWrapper<>();
        wrapper.eq("scheme_id", id);
        wrapper.orderByDesc("approval_time");
        return Result.success(approvalMapper.selectList(wrapper));
    }

    /**
     * 查询操作日志
     */
    @GetMapping("/{id}/logs")
    @PreAuthorize("hasAuthority('SOL:get')")
    public Result<List<CrmSchemeLog>> getLogs(@PathVariable Long id) {
        QueryWrapper<CrmSchemeLog> wrapper = new QueryWrapper<>();
        wrapper.eq("scheme_id", id);
        wrapper.orderByDesc("operation_time");
        return Result.success(logMapper.selectList(wrapper));
    }

    private String generateSchemeNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String seqKey = "SEQ:SCHEME_NO:" + dateStr;
        String val = sysConfigService.getValue(seqKey);
        long next = 1L;
        if (StrUtil.isNotBlank(val)) {
            try {
                next = Long.parseLong(val) + 1L;
            } catch (Exception ignored) {}
        }
        sysConfigService.setValue(seqKey, String.valueOf(next), "编号序列");
        return "FA" + dateStr + String.format("%04d", next);
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        // 从 SecurityContextHolder 获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof com.yysystem.common.security.CustomUserDetails) {
            com.yysystem.common.security.CustomUserDetails user = 
                (com.yysystem.common.security.CustomUserDetails) authentication.getPrincipal();
            return user.getUserId();
        }
        // 兼容旧方式
        Object userId = request.getAttribute("userId");
        return userId != null ? Long.valueOf(userId.toString()) : null;
    }

    private String getCurrentUserName(HttpServletRequest request) {
        // 从 SecurityContextHolder 获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof com.yysystem.common.security.CustomUserDetails) {
            com.yysystem.common.security.CustomUserDetails user = 
                (com.yysystem.common.security.CustomUserDetails) authentication.getPrincipal();
            return user.getRealName();
        }
        // 兼容旧方式
        Object userName = request.getAttribute("userName");
        return userName != null ? userName.toString() : "";
    }

    private String objectToJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return obj != null ? obj.toString() : null;
        }
    }
}
