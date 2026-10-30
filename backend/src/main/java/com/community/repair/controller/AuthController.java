package com.community.repair.controller;

import com.community.repair.common.ApiResponse;
import com.community.repair.common.BusinessException;
import com.community.repair.common.ErrorCode;
import com.community.repair.dto.CurrentUserResponse;
import com.community.repair.dto.LoginRequest;
import com.community.repair.dto.LoginResponse;
import com.community.repair.security.JwtTokenProvider;
import com.community.repair.security.LoginUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        } catch (AuthenticationException ex) {
            // 不区分“用户不存在”和“密码错误”，避免账号枚举
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }
        LoginUser user = (LoginUser) authentication.getPrincipal();
        String token = tokenProvider.generate(user);
        return ApiResponse.ok(new LoginResponse(
                token,
                tokenProvider.getExpireMinutes() * 60,
                user.getId(),
                user.getUsername(),
                user.getRealName(),
                user.getRole()));
    }

    @GetMapping("/me")
    public ApiResponse<CurrentUserResponse> me(Authentication authentication) {
        LoginUser user = (LoginUser) authentication.getPrincipal();
        return ApiResponse.ok(new CurrentUserResponse(
                user.getId(), user.getUsername(), user.getRealName(), user.getRole()));
    }
}
