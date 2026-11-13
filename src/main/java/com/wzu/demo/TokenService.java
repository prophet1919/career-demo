package com.wzu.demo;

import io.jsonwebtoken.Claims;
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
    /** 校验并解析 token。签名不对 / 过期 / 格式错 都会抛异常，交给调用方处理 */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}