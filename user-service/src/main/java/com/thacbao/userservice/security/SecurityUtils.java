package com.thacbao.userservice.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    public static UserPrincipal getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            return (UserPrincipal) authentication.getPrincipal();
        }
        return null;
    }

    public static Integer getCurrentUserId() {
        UserPrincipal user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    public static String getCurrentEmail() {
        UserPrincipal user = getCurrentUser();
        return user != null ? user.getEmail() : null;
    }

    public static boolean hasRole(String role) {
        UserPrincipal user = getCurrentUser();
        if (user == null) return false;
        return user.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + role.toUpperCase()));
    }
}
