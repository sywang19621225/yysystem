package com.yysystem.modules.workflow.service;

public interface WorkflowService {
    Long startInstance(String module, Long businessId, Long applicantId, java.math.BigDecimal amount, String expenseType, Long departmentId);
    java.util.List<com.yysystem.modules.workflow.entity.WorkflowTask> listMyPendingTasks(Long userId);
    boolean approveTask(Long taskId, Long userId, String remark);
    boolean rejectTask(Long taskId, Long userId, String remark);
    boolean addSign(Long taskId, String roleName, Long userId);
    com.yysystem.modules.workflow.entity.WorkflowInstance getInstanceById(Long id);
}
