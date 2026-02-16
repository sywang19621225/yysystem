package com.yysystem.modules.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yysystem.modules.system.entity.SysTeam;
import com.yysystem.modules.system.entity.SysUser;
import com.yysystem.modules.system.mapper.SysTeamMapper;
import com.yysystem.modules.system.service.SysTeamService;
import com.yysystem.modules.system.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SysTeamServiceImpl extends ServiceImpl<SysTeamMapper, SysTeam> implements SysTeamService {

    @Autowired
    private SysUserService sysUserService;

    @Override
    public List<SysTeam> getTeamTree() {
        List<SysTeam> allTeams = this.list();
        if (CollUtil.isNotEmpty(allTeams)) {
            // Fill manager name
            for (SysTeam team : allTeams) {
                if (team.getManagerId() != null) {
                    SysUser user = sysUserService.getById(team.getManagerId());
                    if (user != null) {
                        team.setManagerName(user.getName());
                    }
                }
            }
        }
        return buildTree(allTeams);
    }

    private List<SysTeam> buildTree(List<SysTeam> teams) {
        List<SysTeam> tree = new ArrayList<>();
        if (CollUtil.isEmpty(teams)) return tree;
        
        Map<Long, List<SysTeam>> parentMap = teams.stream()
                .collect(Collectors.groupingBy(d -> ObjectUtil.defaultIfNull(d.getParentId(), 0L)));
        
        List<SysTeam> roots = parentMap.get(0L);
        if (roots != null) {
            roots.forEach(root -> {
                buildChildren(root, parentMap);
                tree.add(root);
            });
        }
        return tree;
    }

    private void buildChildren(SysTeam parent, Map<Long, List<SysTeam>> parentMap) {
        List<SysTeam> children = parentMap.get(parent.getId());
        if (children != null) {
            parent.setChildren(children);
            children.forEach(child -> buildChildren(child, parentMap));
        }
    }
}
