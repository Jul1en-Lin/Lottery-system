package com.julien.lotterysystem.common.interceptor;


import com.julien.lotterysystem.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        log.info("【拦截检测】当前访问地址: {}", uri);

        // 1. 放行 OPTIONS 请求（跨域预检）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 2. 检查 Header 中的 token
        String token = request.getHeader("token");
        log.info("从 Header 中获取 token 为: {}", token);

        // 3. 校验 token 是否为空
        if (!StringUtils.hasLength(token)) {
            log.error("路径 {} 拦截失败：token 为空", uri);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }

        // 4. 解析 token
        Claims claims = JwtUtil.parseJWT(token);
        if (claims == null) {
            log.error("路径 {} 拦截失败：token 解析失败", uri);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }

        return true;
    }
}
