package com.mawgod.e_commerce.service;

import com.mawgod.e_commerce.dto.request.LoginRequest;
import com.mawgod.e_commerce.dto.request.RegisterRequest;
import com.mawgod.e_commerce.dto.response.AuthResponse;
import com.mawgod.e_commerce.entity.User;
import com.mawgod.e_commerce.entity.UserRole;
import com.mawgod.e_commerce.exception.DuplicateResourceException;
import com.mawgod.e_commerce.repository.UserRepository;
import com.mawgod.e_commerce.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository        userRepository;
    private final PasswordEncoder       passwordEncoder;
    private final JwtService            jwtService;
    private final AuthenticationManager authenticationManager;

    // ------------------------------------------------------------------ //
    //  Register                                                            //
    // ------------------------------------------------------------------ //

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("User", "email", request.email());
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(UserRole.CUSTOMER)
                .enabled(true)
                .build();

        User saved = userRepository.save(user);
        String token = jwtService.generateToken(
                saved.getEmail(), saved.getId(), saved.getRole().name());

        return AuthResponse.bearer(token, saved.getId(),
                saved.getEmail(), saved.getName(), saved.getRole().name());
    }

    // ------------------------------------------------------------------ //
    //  Login                                                               //
    // ------------------------------------------------------------------ //

    public AuthResponse login(LoginRequest request) {
        // Delegates to DaoAuthenticationProvider → verifies password via BCrypt
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalStateException("User not found after authentication"));

        String token = jwtService.generateToken(
                user.getEmail(), user.getId(), user.getRole().name());

        return AuthResponse.bearer(token, user.getId(),
                user.getEmail(), user.getName(), user.getRole().name());
    }
}
