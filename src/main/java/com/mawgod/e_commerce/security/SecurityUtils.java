package com.mawgod.e_commerce.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Convenience helpers for accessing the authenticated principal.
 *
 * Usage in controllers/services:
 * <pre>
 *   Long userId = SecurityUtils.getCurrentUserId();
 * </pre>
 */
public final class SecurityUtils {

    private SecurityUtils() {}

    /**
     * Returns the ID of the currently authenticated user.
     *
     * @throws IllegalStateException if no authentication is present (should
     *                               not happen on protected endpoints)
     */
    public static Long getCurrentUserId() {
        AuthenticatedUser user = getCurrentUser();
        return user.id();
    }

    /**
     * Returns the email of the currently authenticated user.
     */
    public static String getCurrentUserEmail() {
        return getCurrentUser().email();
    }

    /**
     * Returns the full {@link AuthenticatedUser} principal.
     */
    public static AuthenticatedUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user in the current context");
        }
        if (auth.getPrincipal() instanceof AuthenticatedUser user) {
            return user;
        }
        throw new IllegalStateException(
                "Unexpected principal type: " + auth.getPrincipal().getClass());
    }

    /**
     * Returns true when the current user has the ADMIN role.
     */
    public static boolean isAdmin() {
        return getCurrentUser().authorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
