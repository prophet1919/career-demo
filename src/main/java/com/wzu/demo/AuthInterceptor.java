package com.wzu.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final TokenService tokenService;
    private static final Logger log = LoggerFactory.getLogger(AuthInterceptor.class);

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

        Claims claims;
        try {
            claims = tokenService.parse(token);
        } catch (Exception e) {
            // 隐患 #4：把异常类型记下来，否则分不清"过期"还是"伪造"
            log.warn("token 校验失败 type={} uri={}", e.getClass().getSimpleName(), request.getRequestURI());
            throw new BizException(401, "token 无效或已过期");
        }

        String role = claims.get("role", String.class);
        request.setAttribute("userId", claims.getSubject());
        request.setAttribute("role", role);

        // 检查 @RequireRole —— 先看方法，再看类
        if (handler instanceof HandlerMethod) {
            HandlerMethod hm = (HandlerMethod) handler;
            RequireRole need = hm.getMethodAnnotation(RequireRole.class);
            if (need == null) {
                need = hm.getBeanType().getAnnotation(RequireRole.class);
            }
            if (need != null && !need.value().equals(role)) {
                throw new BizException(403, "权限不足，需要 " + need.value());
            }
        }
        return true;
    }
}