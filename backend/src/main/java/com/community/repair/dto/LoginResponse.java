package com.community.repair.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {

    private String token;

    /** 有效期，单位秒 */
    private long expiresIn;

    private Long userId;

    private String username;

    private String realName;

    private String role;
}
