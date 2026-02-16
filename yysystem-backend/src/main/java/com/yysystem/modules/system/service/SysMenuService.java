package com.yysystem.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yysystem.modules.system.entity.SysMenu;

import java.util.List;

public interface SysMenuService extends IService<SysMenu> {
    /**
     * 获取菜单树
     * @return
     */
    List<SysMenu> getTree();
}
