package com.community.repair.common;

import com.community.repair.enums.Role;
import com.community.repair.security.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 从 SecurityContext 取当前登录用户。
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static LoginUser current() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser user)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "未登录或登录已过期");
        }
        return user;
    }

    /** 客服与社区工作人员：具备调度、代报修、代确认等管理权限 */
    public static boolean isStaff(LoginUser user) {
        return Role.CS.name().equals(user.getRole()) || Role.COMMUNITY.name().equals(user.getRole());
    }
}
