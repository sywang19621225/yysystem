package com.yysystem.modules.inventory.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yysystem.modules.inventory.entity.ScmInventoryCheck;

public interface ScmInventoryCheckService extends IService<ScmInventoryCheck> {
    Long createCheck(ScmInventoryCheck check);
}
