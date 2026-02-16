package com.yysystem.modules.scheme.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yysystem.modules.scheme.entity.CrmScheme;
import com.yysystem.modules.scheme.entity.CrmSchemeApproval;
import com.yysystem.modules.scheme.entity.CrmSchemeLog;
import com.yysystem.modules.scheme.mapper.CrmSchemeApprovalMapper;
import com.yysystem.modules.scheme.mapper.CrmSchemeLogMapper;
import com.yysystem.modules.scheme.mapper.CrmSchemeMapper;
import com.yysystem.modules.scheme.service.SchemeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class SchemeServiceImpl extends ServiceImpl<CrmSchemeMapper, CrmScheme> implements SchemeService {

    @Autowired
    private CrmSchemeApprovalMapper approvalMapper;

    @Autowired
    private CrmSchemeLogMapper logMapper;

    /**
     * 保存审批记录
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveApproval(CrmSchemeApproval approval) {
        approvalMapper.insert(approval);
    }

    /**
     * 记录操作日志
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveOperationLog(Long schemeId, String operationType, String operationDesc, 
                                  Long operatorId, String operatorName, String oldValue, String newValue) {
        CrmSchemeLog operationLog = new CrmSchemeLog();
        operationLog.setSchemeId(schemeId);
        operationLog.setOperationType(operationType);
        operationLog.setOperationDesc(operationDesc);
        operationLog.setOperatorId(operatorId);
        operationLog.setOperatorName(operatorName);
        operationLog.setOldValue(oldValue);
        operationLog.setNewValue(newValue);
        logMapper.insert(operationLog);
    }
}
