package com.yysystem.modules.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yysystem.modules.system.entity.SysLog;
import com.yysystem.modules.system.mapper.SysLogMapper;
import com.yysystem.modules.system.service.SysLogService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SysLogServiceImpl extends ServiceImpl<SysLogMapper, SysLog> implements SysLogService {

    @Async
    @Override
    public void log(String operation, String content, String operator) {
        SysLog log = new SysLog();
        log.setOperation(operation);
        log.setContent(content);
        log.setOperator(operator);
        log.setOperationTime(LocalDateTime.now());
        this.save(log);
    }
}
