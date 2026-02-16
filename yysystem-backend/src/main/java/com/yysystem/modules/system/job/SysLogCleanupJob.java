package com.yysystem.modules.system.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yysystem.modules.system.entity.SysLog;
import com.yysystem.modules.system.service.SysLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class SysLogCleanupJob {

    @Autowired
    private SysLogService sysLogService;

    // 每月1号凌晨3点执行一次，清理30天前的日志
    @Scheduled(cron = "0 0 3 1 * ?")
    public void cleanupMonthly() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        sysLogService.remove(new LambdaQueryWrapper<SysLog>()
                .lt(SysLog::getOperationTime, threshold));
    }
}

