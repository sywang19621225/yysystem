package com.yysystem.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yysystem.modules.system.entity.SysLog;

public interface SysLogService extends IService<SysLog> {
    void log(String operation, String content, String operator);
}
