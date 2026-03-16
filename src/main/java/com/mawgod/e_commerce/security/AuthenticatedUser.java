package com.mawgod.e_commerce.security;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * Custom principal stored in the SecurityContext after JWT validation.
 * Controllers retrieve the current user via SecurityUtils.
 */
public record AuthenticatedUser(
        Long id,
        String email,
        Collection<? extends GrantedAuthority> authorities
) {}
