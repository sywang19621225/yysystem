package com.yysystem.modules.system.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yysystem.modules.system.entity.SysDept;
import com.yysystem.modules.system.mapper.SysDeptMapper;
import com.yysystem.modules.system.service.SysDeptService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements SysDeptService {

    @Override
    public List<SysDept> getDeptTree() {
        List<SysDept> allDepts = this.list();
        return buildTree(allDepts);
    }

    private List<SysDept> buildTree(List<SysDept> depts) {
        List<SysDept> tree = new ArrayList<>();
        if (CollUtil.isEmpty(depts)) return tree;
        
        Map<Long, List<SysDept>> parentMap = depts.stream()
                .collect(Collectors.groupingBy(d -> ObjectUtil.defaultIfNull(d.getParentId(), 0L)));
        
        // Root nodes usually have parentId 0 or null
        List<SysDept> roots = parentMap.get(0L);
        if (roots != null) {
            roots.forEach(root -> {
                buildChildren(root, parentMap);
                tree.add(root);
            });
        }
        return tree;
    }

    private void buildChildren(SysDept parent, Map<Long, List<SysDept>> parentMap) {
        List<SysDept> children = parentMap.get(parent.getId());
        if (children != null) {
            parent.setChildren(children);
            children.forEach(child -> buildChildren(child, parentMap));
        }
    }
}
