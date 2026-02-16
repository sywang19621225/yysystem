package com.yysystem.modules.auth.controller;

import com.yysystem.common.result.Result;
import com.yysystem.common.utils.JwtUtils;
import com.yysystem.modules.auth.dto.LoginRequest;
import com.yysystem.modules.system.entity.SysUser;
import com.yysystem.modules.system.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsService userDetailsService;
    
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private com.yysystem.modules.system.service.SysRoleService sysRoleService;
    @Autowired
    private com.yysystem.modules.system.service.SysPermissionService sysPermissionService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestBody LoginRequest loginRequest) {
        // 临时初始化一个 admin 用户 (如果数据库为空)
        if (sysUserService.getByUsername("admin") == null) {
             SysUser admin = new SysUser();
             admin.setUsername("admin");
             admin.setPassword(passwordEncoder.encode("123456"));
             admin.setName("超级管理员");
             admin.setStatus(1);
             admin.setUserType("admin");
             admin.setRoleId(1L); // 假设角色ID为1
             sysUserService.save(admin);
        } else {
            // 开发环境：如果admin用户存在，强制重置密码为 123456，防止忘记密码
            if ("admin".equals(loginRequest.getUsername())) {
                SysUser admin = sysUserService.getByUsername("admin");
                if (!passwordEncoder.matches("123456", admin.getPassword())) {
                    admin.setPassword(passwordEncoder.encode("123456"));
                    sysUserService.updateById(admin);
                }
            }
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        final UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());
        final String token = jwtUtils.generateToken(userDetails);

        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return Result.success(response);
    }
    
    @GetMapping("/info")
    public Result<SysUser> info() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null || (authentication.getPrincipal() instanceof String)) {
            return Result.error(401, "未登录或登录已过期");
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        SysUser sysUser = sysUserService.getByUsername(userDetails.getUsername());
        if (sysUser == null) {
            return Result.error(401, "未登录或登录已过期");
        }
        sysUser.setPassword(null); // 不返回密码
        if (sysUser.getRoleId() != null) {
            com.yysystem.modules.system.entity.SysRole role = sysRoleService.getById(sysUser.getRoleId());
            if (role != null) {
                sysUser.setRoleName(role.getRoleName());
                java.util.List<Long> ids = new java.util.ArrayList<>();
                String perms = role.getPermissions();
                if (perms != null && !perms.isEmpty()) {
                    java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\d+").matcher(perms);
                    while (m.find()) {
                        try { ids.add(Long.parseLong(m.group())); } catch (Exception ignored) {}
                    }
                }
                if (!ids.isEmpty()) {
                    java.util.List<com.yysystem.modules.system.entity.SysPermission> plist = sysPermissionService.listByIds(ids);
                    java.util.List<String> codes = new java.util.ArrayList<>();
                    for (com.yysystem.modules.system.entity.SysPermission p : plist) {
                        if (p.getPermCode() != null && !p.getPermCode().isEmpty()) codes.add(p.getPermCode());
                    }
                    sysUser.setPermissionCodes(codes);
                }
            }
        }
        return Result.success(sysUser);
    }
}
