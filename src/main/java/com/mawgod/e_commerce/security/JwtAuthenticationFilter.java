package com.mawgod.e_commerce.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Intercepts every request, extracts the Bearer token from the
 * Authorization header, validates it, and populates the SecurityContext.
 *
 * Requests without a valid token pass through unauthenticated (public
 * endpoints are allowed by SecurityConfig; protected ones receive 401).
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest  request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain         filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        if (!jwtService.isTokenValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Only set authentication if not already set
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String email = jwtService.extractEmail(token);
            Long   userId = jwtService.extractUserId(token);

            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // Wrap UserDetails with userId as the principal detail for easy access
            var authToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            authToken.setDetails(new JwtAuthDetails(userId, email));
            authToken.setDetails(new WebAuthenticationDetailsSource()
                    .buildDetails(request));

            // Store userId on the authentication object so controllers can retrieve it
            var authWithUserId = new UsernamePasswordAuthenticationToken(
                    new AuthenticatedUser(userId, email, userDetails.getAuthorities()),
                    null,
                    userDetails.getAuthorities());
            authWithUserId.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authWithUserId);
        }

        filterChain.doFilter(request, response);
    }
}
