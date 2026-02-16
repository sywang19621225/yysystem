package com.yysystem.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yysystem.modules.system.entity.SysUser;

public interface SysUserService extends IService<SysUser> {
    SysUser getByUsername(String username);
}
