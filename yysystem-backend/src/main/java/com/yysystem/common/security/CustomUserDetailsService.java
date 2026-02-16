package com.yysystem.common.security;

import com.yysystem.modules.system.entity.SysUser;
import com.yysystem.modules.system.entity.SysRole;
import com.yysystem.modules.system.entity.SysPermission;
import com.yysystem.modules.system.service.SysUserService;
import com.yysystem.modules.system.service.SysRoleService;
import com.yysystem.modules.system.service.SysPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private SysRoleService sysRoleService;
    @Autowired
    private SysPermissionService sysPermissionService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser sysUser = sysUserService.getByUsername(username);
        if (sysUser == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if (sysUser.getRoleId() != null) {
            SysRole role = sysRoleService.getById(sysUser.getRoleId());
            if (role != null) {
                String perms = role.getPermissions();
                List<Long> ids = new ArrayList<>();
                if (perms != null && !perms.isEmpty()) {
                    try {
                        Pattern pattern = Pattern.compile("\\d+");
                        Matcher matcher = pattern.matcher(perms);
                        while (matcher.find()) {
                            try {
                                ids.add(Long.parseLong(matcher.group()));
                            } catch (Exception ignored) {}
                        }
                    } catch (Exception ignored) {}
                }
                if (!ids.isEmpty()) {
                    List<SysPermission> list = sysPermissionService.listByIds(ids);
                    List<String> codes = list.stream().map(SysPermission::getPermCode).filter(s -> s != null && !s.isEmpty()).collect(Collectors.toList());
                    for (String code : codes) {
                        authorities.add(new SimpleGrantedAuthority(code));
                    }
                }
            }
        }
        return new CustomUserDetails(sysUser.getId(), sysUser.getUsername(), sysUser.getPassword(), 
                sysUser.getName(), authorities);
    }
}

