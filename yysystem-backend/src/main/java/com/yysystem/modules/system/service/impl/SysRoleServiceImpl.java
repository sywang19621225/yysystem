package com.yysystem.modules.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yysystem.modules.system.entity.SysRole;
import com.yysystem.modules.system.mapper.SysRoleMapper;
import com.yysystem.modules.system.service.SysRoleService;
import org.springframework.stereotype.Service;

@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {
}
