package com.mawgod.e_commerce.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Handles JWT creation, parsing, and validation.
 * Algorithm: HMAC-SHA256 (HS256).
 */
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    // ------------------------------------------------------------------ //
    //  Token generation                                                    //
    // ------------------------------------------------------------------ //

    /**
     * Generates a signed JWT for the given user.
     *
     * @param email  stored as the subject claim
     * @param userId stored as a custom "uid" claim
     * @param role   stored as a custom "role" claim
     */
    public String generateToken(String email, Long userId, String role) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getExpirationMs());

        return Jwts.builder()
                .subject(email)
                .claim("uid", userId)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey())
                .compact();
    }

    // ------------------------------------------------------------------ //
    //  Token parsing                                                       //
    // ------------------------------------------------------------------ //

    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public Long extractUserId(String token) {
        Object uid = parseClaims(token).get("uid");
        if (uid instanceof Integer) return ((Integer) uid).longValue();
        if (uid instanceof Long)    return (Long) uid;
        return Long.valueOf(uid.toString());
    }

    public String extractRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    /**
     * Returns true if the token is structurally valid, signed correctly,
     * and not yet expired.
     */
    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // ------------------------------------------------------------------ //
    //  Internal helpers                                                    //
    // ------------------------------------------------------------------ //

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey signingKey() {
        // Treat the configured secret as a raw string key.
        // Ensure app.jwt.secret is at least 32 characters for HS256.
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    }
}
