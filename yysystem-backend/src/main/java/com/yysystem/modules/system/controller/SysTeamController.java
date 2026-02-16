package com.yysystem.modules.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yysystem.common.result.Result;
import com.yysystem.modules.system.entity.SysTeam;
import com.yysystem.modules.system.service.SysTeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/system/team")
public class SysTeamController {

    @Autowired
    private SysTeamService sysTeamService;

    @GetMapping("/list")
    public Result<Page<SysTeam>> list(@RequestParam(defaultValue = "1") Integer current,
                                      @RequestParam(defaultValue = "10") Integer size) {
        Page<SysTeam> page = new Page<>(current, size);
        return Result.success(sysTeamService.page(page));
    }

    @GetMapping("/tree")
    public Result<List<SysTeam>> getTeamTree() {
        return Result.success(sysTeamService.getTeamTree());
    }

    @PostMapping
    public Result<Boolean> save(@RequestBody SysTeam sysTeam) {
        sysTeam.setCreateTime(LocalDateTime.now());
        return Result.success(sysTeamService.save(sysTeam));
    }

    @PutMapping
    public Result<Boolean> update(@RequestBody SysTeam sysTeam) {
        return Result.success(sysTeamService.updateById(sysTeam));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(sysTeamService.removeById(id));
    }
}
