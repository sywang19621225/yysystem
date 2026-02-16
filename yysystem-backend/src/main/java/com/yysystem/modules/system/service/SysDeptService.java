package com.yysystem.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yysystem.modules.system.entity.SysDept;
import java.util.List;

public interface SysDeptService extends IService<SysDept> {
    List<SysDept> getDeptTree();
}
