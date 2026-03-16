package com.mawgod.e_commerce.dto.response;

public record AuthResponse(
        String token,
        String tokenType,
        Long   userId,
        String email,
        String name,
        String role
) {
    /** Convenience factory with "Bearer" token type. */
    public static AuthResponse bearer(String token, Long userId,
                                      String email, String name, String role) {
        return new AuthResponse(token, "Bearer", userId, email, name, role);
    }
}
