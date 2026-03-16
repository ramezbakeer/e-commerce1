package com.mawgod.e_commerce.security;

/**
 * Extra details attached to {@link org.springframework.security.authentication.UsernamePasswordAuthenticationToken}.
 * Kept as a lightweight value type.
 */
public record JwtAuthDetails(Long userId, String email) {}
