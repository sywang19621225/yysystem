package com.yysystem.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yysystem.modules.system.entity.SysTeam;
import java.util.List;

public interface SysTeamService extends IService<SysTeam> {
    List<SysTeam> getTeamTree();
}
