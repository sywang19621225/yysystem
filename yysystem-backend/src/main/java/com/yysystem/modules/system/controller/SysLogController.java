package com.yysystem.modules.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yysystem.common.result.Result;
import com.yysystem.modules.system.entity.SysLog;
import com.yysystem.modules.system.service.SysLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system/log")
public class SysLogController {

    @Autowired
    private SysLogService sysLogService;

    @GetMapping("/list")
    public Result<Page<SysLog>> list(@RequestParam(defaultValue = "1") Integer current,
                                     @RequestParam(defaultValue = "10") Integer size) {
        Page<SysLog> page = new Page<>(current, size);
        return Result.success(sysLogService.page(page, new LambdaQueryWrapper<SysLog>().orderByDesc(SysLog::getOperationTime)));
    }
}
