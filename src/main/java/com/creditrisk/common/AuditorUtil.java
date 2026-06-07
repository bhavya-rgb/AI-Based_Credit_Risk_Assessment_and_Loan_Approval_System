package com.creditrisk.common;

import com.creditrisk.auth.SecurityPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class AuditorUtil {
    private AuditorUtil() {}

    public static Long currentUserIdOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof SecurityPrincipal principal)) {
            return null;
        }
        return principal.userId();
    }
}
