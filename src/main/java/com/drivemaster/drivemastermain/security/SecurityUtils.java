package com.drivemaster.drivemastermain.security;

import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static CustomUserDetails currentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails;
        }
        throw new IllegalStateException("No authenticated CustomUserDetails found in security context");
    }

    public static Long currentUserId() {
        return currentUser().getUser().getId();
    }
}
