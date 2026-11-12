package com.wzu.demo;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class TokenService {
    // 必须 >= 32 字节，短了 jjwt 直接抛 WeakKeyException
    private static final String SECRET = "wzu-career-demo-secret-key-2026-please-change";
    private static final long EXPIRE_MS = 2 * 60 * 60 * 1000L;   // 2 小时

    private final SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    public String create(User u) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(String.valueOf(u.getId()))
                .claim("username", u.getUsername())
                .claim("role", u.getRole())
                .issuedAt(new Date(now))
                .expiration(new Date(now + EXPIRE_MS))
                .signWith(key)
                .compact();
    }
}