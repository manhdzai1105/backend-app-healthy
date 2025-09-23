package com.example.chat.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    @Value("${jwt.access-secret}")
    private String accessSecretRaw;

    @Value("${jwt.refresh-secret}")
    private String refreshSecretRaw;

    @Value("${jwt.access-token-expiration-ms}")
    private long accessTokenValidity;

    @Value("${jwt.refresh-token-expiration-ms}")
    private long refreshTokenValidity;

    private Key accessSecretKey;
    private Key refreshSecretKey;

    @PostConstruct
    public void init() {
        accessSecretKey = Keys.hmacShaKeyFor(accessSecretRaw.getBytes());
        refreshSecretKey = Keys.hmacShaKeyFor(refreshSecretRaw.getBytes());
    }

    public String generateAccessToken(Long accountId, String username, String role) {
        return generateToken(accountId, username, role, accessTokenValidity, accessSecretKey);
    }

    public String generateRefreshToken(Long accountId, String username, String role) {
        return generateToken(accountId, username, role, refreshTokenValidity, refreshSecretKey);
    }

    private String generateToken(Long accountId, String username, String role, long validityInMs, Key key) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validityInMs);

        return Jwts.builder()
                .claim("username", username)
                .claim("accountId", accountId)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isValidAccessToken(String token) {
        return validateToken(token, accessSecretKey);
    }

    public boolean isValidateRefreshToken(String token) {
        return validateToken(token, refreshSecretKey);
    }

    private boolean validateToken(String token, Key key) {
        try {
            getClaims(token, key);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Claims getClaims(String token, Key key) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Long getAccountIdFromAccessToken(String token) {
        return getClaims(token, accessSecretKey).get("accountId", Long.class);
    }

    public String getUsernameFromAccessToken(String token) {
        return getClaims(token, accessSecretKey).get("username", String.class);
    }

    public String getRoleFromAccessToken(String token) {
        return getClaims(token, accessSecretKey).get("role", String.class);
    }

    public Long getAccountIdFromRefreshToken(String token) {
        return getClaims(token, refreshSecretKey).get("accountId", Long.class);
    }
}
