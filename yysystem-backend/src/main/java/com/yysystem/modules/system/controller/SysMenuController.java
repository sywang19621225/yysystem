package com.yysystem.modules.system.controller;

import com.yysystem.common.result.Result;
import com.yysystem.modules.system.entity.SysMenu;
import com.yysystem.modules.system.service.SysMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/system/menu")
public class SysMenuController {

    @Autowired
    private SysMenuService sysMenuService;

    /**
     * 获取菜单树
     */
    @GetMapping("/tree")
    public Result<List<SysMenu>> tree() {
        return Result.success(sysMenuService.getTree());
    }
    
    /**
     * 获取所有菜单列表
     */
    @GetMapping("/list")
    public Result<List<SysMenu>> list() {
        return Result.success(sysMenuService.list());
    }
}
