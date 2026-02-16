package com.yysystem.common.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * 自定义UserDetails，携带用户ID
 */
public class CustomUserDetails extends User {
    
    private final Long userId;
    private final String realName;
    
    public CustomUserDetails(Long userId, String username, String password, 
                             Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.userId = userId;
        this.realName = username;
    }
    
    public CustomUserDetails(Long userId, String username, String password, 
                             String realName, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.userId = userId;
        this.realName = realName;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public String getRealName() {
        return realName;
    }
}
