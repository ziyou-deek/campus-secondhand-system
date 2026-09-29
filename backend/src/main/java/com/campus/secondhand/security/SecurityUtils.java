package com.campus.secondhand.security;

import com.campus.secondhand.common.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {
    private SecurityUtils() {}

    public static LoginUser getLoginUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof LoginUser)) {
            throw new BusinessException(401, "未登录");
        }
        return (LoginUser) auth.getPrincipal();
    }

    public static Long getUserId() {
        return getLoginUser().getId();
    }

    public static boolean isAdmin() {
        try {
            return "ADMIN".equals(getLoginUser().getRole());
        } catch (Exception e) {
            return false;
        }
    }
}
