package com.yysystem.modules.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yysystem.common.result.Result;
import com.yysystem.modules.system.entity.SysCodeRule;
import com.yysystem.modules.system.service.SysCodeRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sys/code-rule")
public class SysCodeRuleController {

    @Autowired
    private SysCodeRuleService sysCodeRuleService;

    @PostMapping
    public Result<Boolean> save(@RequestBody SysCodeRule sysCodeRule) {
        try {
            return Result.success(sysCodeRuleService.save(sysCodeRule));
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("保存失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<Boolean> update(@PathVariable Long id, @RequestBody SysCodeRule sysCodeRule) {
        sysCodeRule.setId(id);
        return Result.success(sysCodeRuleService.updateById(sysCodeRule));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> remove(@PathVariable Long id) {
        return Result.success(sysCodeRuleService.removeById(id));
    }

    @GetMapping("/{id}")
    public Result<SysCodeRule> getById(@PathVariable Long id) {
        return Result.success(sysCodeRuleService.getById(id));
    }

    @GetMapping("/list")
    public Result<Page<SysCodeRule>> list(@RequestParam(defaultValue = "1") Integer current,
                                          @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(sysCodeRuleService.page(new Page<>(current, size)));
    }
    
    @GetMapping("/generate/{code}")
    public Result<String> generate(@PathVariable String code) {
        return Result.success(sysCodeRuleService.generateNextCode(code));
    }
}
