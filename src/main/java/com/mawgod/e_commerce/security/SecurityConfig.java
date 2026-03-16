package com.mawgod.e_commerce.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Central Spring Security configuration.
 *
 * Access rules:
 * ┌────────────────────────────────────────────────────────┬─────────────┐
 * │ Endpoint                                               │ Access      │
 * ├────────────────────────────────────────────────────────┼─────────────┤
 * │ POST  /api/v1/auth/**                                  │ Public      │
 * │ GET   /api/v1/products/**                              │ Public      │
 * │ GET   /api/v1/categories/**                            │ Public      │
 * │ GET   /api/v1/cart  (guest session carts)              │ Public      │
 * │ POST/PATCH/DELETE /api/v1/cart/**                      │ Authenticated│
 * │ /api/v1/orders/**                                      │ Authenticated│
 * │ POST  /api/v1/products/** (admin write)                │ ADMIN only  │
 * │ POST/PATCH/DELETE /api/v1/categories/** (admin write)  │ ADMIN only  │
 * └────────────────────────────────────────────────────────┴─────────────┘
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsServiceImpl  userDetailsService;
    private final JwtAuthEntryPoint       jwtAuthEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF — stateless JWT API does not need it
            .csrf(AbstractHttpConfigurer::disable)

            // Stateless sessions — no HttpSession
            .sessionManagement(sm ->
                    sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Custom 401 handler
            .exceptionHandling(ex ->
                    ex.authenticationEntryPoint(jwtAuthEntryPoint))

            .authorizeHttpRequests(auth -> auth
                // Auth endpoints — always public
                .requestMatchers("/api/v1/auth/**").permitAll()

                // Product catalog reads — public
                .requestMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll()

                // Product / category writes — admin only
                .requestMatchers(HttpMethod.POST,   "/api/v1/products/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH,  "/api/v1/products/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/products/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST,   "/api/v1/categories/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/categories/**").hasRole("ADMIN")

                // Cart reads are public (supports guest session carts)
                .requestMatchers(HttpMethod.GET, "/api/v1/cart").permitAll()

                // Cart mutations and all order endpoints require authentication
                .requestMatchers("/api/v1/cart/**").authenticated()
                .requestMatchers("/api/v1/orders/**").authenticated()

                // Anything else requires authentication
                .anyRequest().authenticated()
            )

            // Register JWT filter before the default username/password filter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

            // Wire our DaoAuthenticationProvider
            .authenticationProvider(authenticationProvider());

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
