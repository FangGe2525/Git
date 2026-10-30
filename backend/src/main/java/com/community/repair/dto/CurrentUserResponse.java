package com.community.repair.dto;

/**
 * 当前登录用户信息（不含令牌与密码）。
 */
public record CurrentUserResponse(Long id, String username, String realName, String role) {
}
