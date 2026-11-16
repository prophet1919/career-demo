package com.wzu.demo;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class TokenService {
    private final SecretKey key;
    private final long expireMs;

    public TokenService(@Value("${jwt.secret}") String secret,
                        @Value("${jwt.expire-hours}") long expireHours) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expireMs = expireHours * 60 * 60 * 1000L;
    }

    public String create(User u) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(String.valueOf(u.getId()))
                .claim("username", u.getUsername())
                .claim("role", u.getRole())
                .issuedAt(new Date(now))
                .expiration(new Date(now + expireMs))
                .signWith(key)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}