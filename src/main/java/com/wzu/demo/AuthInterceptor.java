package com.wzu.demo;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final TokenService tokenService;

    public AuthInterceptor(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new BizException(401, "未登录或 token 缺失");
        }
        String token = header.substring(7).trim();
        try {
            Claims claims = tokenService.parse(token);
            request.setAttribute("userId", claims.getSubject());
            request.setAttribute("role", claims.get("role", String.class));
            return true;
        } catch (Exception e) {
            throw new BizException(401, "token 无效或已过期");
        }
    }
}