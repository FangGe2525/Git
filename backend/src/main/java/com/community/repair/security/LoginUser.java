package com.community.repair.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * 登录态用户，携带业务主键与角色，供 JWT 签发和鉴权使用。
 */
@Getter
public class LoginUser implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;
    private final String realName;
    private final String role;

    public LoginUser(Long id, String username, String password, String realName, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.realName = realName;
        this.role = role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
