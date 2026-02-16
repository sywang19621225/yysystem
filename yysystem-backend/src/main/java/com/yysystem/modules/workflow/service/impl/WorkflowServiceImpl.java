package com.yysystem.modules.workflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yysystem.modules.workflow.entity.WorkflowInstance;
import com.yysystem.modules.workflow.entity.WorkflowRule;
import com.yysystem.modules.workflow.entity.WorkflowTask;
import com.yysystem.modules.workflow.mapper.WorkflowInstanceMapper;
import com.yysystem.modules.workflow.mapper.WorkflowRuleMapper;
import com.yysystem.modules.workflow.mapper.WorkflowTaskMapper;
import com.yysystem.modules.workflow.service.WorkflowService;
import com.yysystem.modules.system.entity.SysRole;
import com.yysystem.modules.system.entity.SysUser;
import com.yysystem.modules.system.entity.SysDept;
import com.yysystem.modules.system.service.SysRoleService;
import com.yysystem.modules.system.service.SysUserService;
import com.yysystem.modules.system.service.SysDeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class WorkflowServiceImpl extends ServiceImpl<WorkflowInstanceMapper, WorkflowInstance> implements WorkflowService {
    @Autowired
    private WorkflowRuleMapper ruleMapper;
    @Autowired
    private WorkflowTaskMapper taskMapper;
    @Autowired
    private SysRoleService sysRoleService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private SysDeptService sysDeptService;
    @Autowired
    private com.yysystem.modules.expense.service.CrmSalesCostService crmSalesCostService;
    @Autowired
    private com.yysystem.modules.contract.service.CrmContractService crmContractService;
    @Autowired
    private com.yysystem.modules.purchase.service.ScmPurchaseRequestService scmPurchaseRequestService;
    @Override
    public Long startInstance(String module, Long businessId, Long applicantId, java.math.BigDecimal amount, String expenseType, Long departmentId) {
        WorkflowRule rule = ruleMapper.selectOne(new LambdaQueryWrapper<WorkflowRule>()
                .eq(WorkflowRule::getModule, module)
                .eq(WorkflowRule::getEnabled, 1));
        WorkflowInstance ins = new WorkflowInstance();
        ins.setModule(module);
        ins.setBusinessId(businessId);
        ins.setCurrentStepIndex(0);
        ins.setStatus("UNDER_REVIEW");
        ins.setCreatedBy(applicantId);
        ins.setDeptId(departmentId);
        ins.setCreateTime(LocalDateTime.now());
        this.getBaseMapper().insert(ins);
        List<String> steps = parseSteps(rule != null ? rule.getStepsJson() : defaultSteps(module));
        if (!steps.isEmpty()) {
            WorkflowTask t = new WorkflowTask();
            t.setInstanceId(ins.getId());
            t.setStepIndex(0);
            t.setAssigneeRole(steps.get(0));
             Long assignee = resolveAssigneeUserId(steps.get(0), departmentId);
             t.setAssigneeUserId(assignee);
            t.setStatus("PENDING");
            t.setCreateTime(LocalDateTime.now());
            taskMapper.insert(t);
        }
        return ins.getId();
    }
    private String defaultSteps(String module) {
        if ("expense".equals(module)) {
            return "[\"总经理\",\"财务经理\"]";
        }
        if ("contract".equals(module)) {
            return "[\"总经理\"]";
        }
        return "[\"部门主管\",\"财务经理\",\"分管副总\",\"总经理\"]";
    }
    private List<String> parseSteps(String json) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper m = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode n = m.readTree(json);
            List<String> list = new ArrayList<>();
            if (n != null && n.isArray()) {
                for (com.fasterxml.jackson.databind.JsonNode it : n) {
                    list.add(it.asText());
                }
            }
            return list;
        } catch (Exception e) {
            return java.util.Arrays.asList("部门主管","财务经理","分管副总","总经理");
        }
    }
    private Long resolveAssigneeUserId(String roleName, Long deptId) {
        // 1) 优先按部门匹配角色
        SysRole role = sysRoleService.lambdaQuery()
                .eq(SysRole::getRoleName, roleName)
                .eq(deptId != null, SysRole::getDeptId, deptId)
                .one();
        // 2) 若部门内无该角色，则向上递归部门
        if (role == null && deptId != null) {
            SysDept dept = sysDeptService.getById(deptId);
            int guard = 0;
            while (dept != null && dept.getParentId() != null && guard++ < 10) {
                role = sysRoleService.lambdaQuery()
                        .eq(SysRole::getRoleName, roleName)
                        .eq(SysRole::getDeptId, dept.getParentId())
                        .one();
                if (role != null) break;
                dept = sysDeptService.getById(dept.getParentId());
            }
        }
        // 3) 全局角色回退
        if (role == null) {
            role = sysRoleService.lambdaQuery()
                    .eq(SysRole::getRoleName, roleName)
                    .one();
        }
        // 4) 解析角色绑定的用户
        if (role != null && role.getUserIds() != null && !role.getUserIds().isEmpty()) {
            String[] parts = role.getUserIds().split(",");
            for (String p : parts) {
                try {
                    Long uid = Long.parseLong(p.trim());
                    SysUser u = sysUserService.getById(uid);
                    if (u != null && (u.getStatus() == null || u.getStatus() == 1)) {
                        return uid;
                    }
                } catch (Exception ignored) {}
            }
        }
        // 5) 若角色未绑定用户，则按用户的roleId与部门匹配一个候选
        if (role != null) {
            SysUser u = sysUserService.lambdaQuery()
                    .eq(SysUser::getRoleId, role.getId())
                    .eq(deptId != null, SysUser::getDeptId, deptId)
                    .one();
            if (u != null) return u.getId();
        }
        // 6) 最后回退：根据角色中文名选择用户类型
        if ("财务经理".equals(roleName)) {
            SysUser u = sysUserService.lambdaQuery().eq(SysUser::getUserType, "finance").one();
            if (u != null) return u.getId();
        }
        if ("总经理".equals(roleName)) {
            SysUser u = sysUserService.lambdaQuery().eq(SysUser::getUserType, "admin").one();
            if (u != null) return u.getId();
        }
        return null;
    }
    @Override
    public java.util.List<WorkflowTask> listMyPendingTasks(Long userId) {
        return taskMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<WorkflowTask>()
                .eq(WorkflowTask::getAssigneeUserId, userId)
                .eq(WorkflowTask::getStatus, "PENDING"));
    }
    @Override
    public boolean approveTask(Long taskId, Long userId, String remark) {
        WorkflowTask task = taskMapper.selectById(taskId);
        if (task == null || !"PENDING".equals(task.getStatus())) return false;
        task.setStatus("APPROVED");
        task.setActionTime(LocalDateTime.now());
        task.setRemark(remark);
        taskMapper.updateById(task);
        WorkflowInstance ins = this.getById(task.getInstanceId());
        if (ins == null) return true;
        boolean hasPendingSameStep = taskMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<WorkflowTask>()
                .eq(WorkflowTask::getInstanceId, ins.getId())
                .eq(WorkflowTask::getStepIndex, task.getStepIndex())
                .eq(WorkflowTask::getStatus, "PENDING")) > 0;
        if (hasPendingSameStep) return true;
        WorkflowRule rule = ruleMapper.selectOne(new LambdaQueryWrapper<WorkflowRule>()
                .eq(WorkflowRule::getModule, ins.getModule())
                .eq(WorkflowRule::getEnabled, 1));
        java.util.List<String> steps = parseSteps(rule != null ? rule.getStepsJson() : defaultSteps(ins.getModule()));
        int next = (task.getStepIndex() + 1);
        if (next < steps.size()) {
            ins.setCurrentStepIndex(next);
            this.updateById(ins);
            WorkflowTask nt = new WorkflowTask();
            nt.setInstanceId(ins.getId());
            nt.setStepIndex(next);
            nt.setAssigneeRole(steps.get(next));
            nt.setAssigneeUserId(resolveAssigneeUserId(steps.get(next), ins.getDeptId()));
            nt.setStatus("PENDING");
            nt.setCreateTime(LocalDateTime.now());
            taskMapper.insert(nt);
        } else {
            ins.setStatus("PASSED");
            this.updateById(ins);
            applyBusinessAuditResult(ins.getModule(), ins.getBusinessId(), "PASSED");
        }
        return true;
    }
    @Override
    public boolean rejectTask(Long taskId, Long userId, String remark) {
        WorkflowTask task = taskMapper.selectById(taskId);
        if (task == null || !"PENDING".equals(task.getStatus())) return false;
        task.setStatus("REJECTED");
        task.setActionTime(LocalDateTime.now());
        task.setRemark(remark);
        taskMapper.updateById(task);
        WorkflowInstance ins = this.getById(task.getInstanceId());
        if (ins != null) {
            ins.setStatus("REJECTED");
            this.updateById(ins);
            applyBusinessAuditResult(ins.getModule(), ins.getBusinessId(), "REJECTED");
        }
        return true;
    }
    @Override
    public boolean addSign(Long taskId, String roleName, Long userId) {
        WorkflowTask src = taskMapper.selectById(taskId);
        if (src == null) return false;
        WorkflowTask ad = new WorkflowTask();
        ad.setInstanceId(src.getInstanceId());
        ad.setStepIndex(src.getStepIndex());
        ad.setAssigneeRole(roleName);
        ad.setAssigneeUserId(userId != null ? userId : resolveAssigneeUserId(roleName, null));
        ad.setStatus("PENDING");
        ad.setCreateTime(LocalDateTime.now());
        taskMapper.insert(ad);
        return true;
    }
    private void applyBusinessAuditResult(String module, Long businessId, String status) {
        if ("expense".equals(module)) {
            com.yysystem.modules.expense.entity.CrmSalesCost upd = new com.yysystem.modules.expense.entity.CrmSalesCost();
            upd.setId(businessId);
            upd.setAuditStatus(status);
            crmSalesCostService.updateById(upd);
        } else if ("contract".equals(module)) {
            com.yysystem.modules.contract.entity.CrmContract upd = new com.yysystem.modules.contract.entity.CrmContract();
            upd.setId(businessId);
            upd.setAuditStatus(status);
            crmContractService.updateById(upd);
        } else if ("purchase_request".equals(module)) {
            com.yysystem.modules.purchase.entity.ScmPurchaseRequest upd = new com.yysystem.modules.purchase.entity.ScmPurchaseRequest();
            upd.setId(businessId);
            upd.setAuditStatus(status);
            scmPurchaseRequestService.updateById(upd);
        }
    }
    @Override
    public WorkflowInstance getInstanceById(Long id) {
        return this.getById(id);
    }
}
