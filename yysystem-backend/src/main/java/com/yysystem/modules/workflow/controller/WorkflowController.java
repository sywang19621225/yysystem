package com.yysystem.modules.workflow.controller;

import com.yysystem.common.result.Result;
import com.yysystem.modules.workflow.entity.WorkflowTask;
import com.yysystem.modules.workflow.service.WorkflowService;
import com.yysystem.modules.system.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/workflow")
public class WorkflowController {
    @Autowired
    private WorkflowService workflowService;
    @Autowired
    private SysUserService userService;
    @Autowired
    private com.yysystem.modules.expense.service.CrmSalesCostService crmSalesCostService;
    @Autowired
    private com.yysystem.modules.contract.service.CrmContractService crmContractService;
    @GetMapping("/tasks/my")
    public Result<List<java.util.Map<String, Object>>> myTasks() {
        String username = null;
        org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
            username = authentication.getName();
        }
        Long userId = null;
        if (username != null) {
            com.yysystem.modules.system.entity.SysUser u = userService.getByUsername(username);
            userId = u != null ? u.getId() : null;
        }
        if (userId == null) return Result.success(java.util.Collections.emptyList());
        List<WorkflowTask> list = workflowService.listMyPendingTasks(userId);
        java.util.List<java.util.Map<String, Object>> out = new java.util.ArrayList<>();
        for (WorkflowTask t : list) {
            java.util.Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", t.getId());
            m.put("instanceId", t.getInstanceId());
            m.put("stepIndex", t.getStepIndex());
            m.put("assigneeRole", t.getAssigneeRole());
            m.put("status", t.getStatus());
            com.yysystem.modules.workflow.entity.WorkflowInstance ins = workflowService.getInstanceById(t.getInstanceId());
            String module = ins != null ? ins.getModule() : "";
            Long bizId = ins != null ? ins.getBusinessId() : null;
            m.put("module", module);
            m.put("businessId", bizId);
            if ("expense".equals(module) && bizId != null) {
                com.yysystem.modules.expense.entity.CrmSalesCost c = crmSalesCostService.getById(bizId);
                if (c != null) {
                    m.put("businessTitle", String.valueOf(c.getReimburseType()));
                    m.put("businessAmount", c.getTotalAmount());
                }
            } else if ("contract".equals(module) && bizId != null) {
                com.yysystem.modules.contract.entity.CrmContract c = crmContractService.getById(bizId);
                if (c != null) {
                    m.put("businessTitle", String.valueOf(c.getContractNo()));
                    m.put("businessAmount", c.getContractAmount());
                }
            }
            out.add(m);
        }
        return Result.success(out);
    }
    @PostMapping("/task/{id}/approve")
    public Result<Boolean> approve(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        String remark = body != null ? String.valueOf(body.getOrDefault("remark", "")) : "";
        Long userId = null;
        String username = null;
        org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
            username = authentication.getName();
        }
        if (username != null) {
            com.yysystem.modules.system.entity.SysUser u = userService.getByUsername(username);
            userId = u != null ? u.getId() : null;
        }
        boolean ok = workflowService.approveTask(id, userId, remark);
        return ok ? Result.success(true) : Result.error("审批失败");
    }
    @PostMapping("/task/{id}/reject")
    public Result<Boolean> reject(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        String remark = body != null ? String.valueOf(body.getOrDefault("remark", "")) : "";
        Long userId = null;
        String username = null;
        org.springframework.security.core.Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String)) {
            username = authentication.getName();
        }
        if (username != null) {
            com.yysystem.modules.system.entity.SysUser u = userService.getByUsername(username);
            userId = u != null ? u.getId() : null;
        }
        boolean ok = workflowService.rejectTask(id, userId, remark);
        return ok ? Result.success(true) : Result.error("驳回失败");
    }
    @PostMapping("/task/{id}/add-sign")
    public Result<Boolean> addSign(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String roleName = String.valueOf(body.getOrDefault("roleName", ""));
        Long userId = null;
        try {
            Object uid = body.get("userId");
            if (uid != null) userId = Long.parseLong(String.valueOf(uid));
        } catch (Exception ignored) {}
        boolean ok = workflowService.addSign(id, roleName, userId);
        return ok ? Result.success(true) : Result.error("加签失败");
    }
}
