package com.julien.lotterysystem.common.interceptor;

import com.julien.lotterysystem.common.exception.LotteryException;
import com.julien.lotterysystem.common.utils.JwtUtil;
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
        String token = request.getHeader("token");
        log.info("从Header中获取token为:{}",token);
        // 校验token是否为空且是否有效
        if (!StringUtils.hasLength(token) || JwtUtil.parseJWT(token) == null) {
            log.info("token:{}",token);
            log.error("拦截器校验不通过");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            throw new LotteryException(HttpStatus.UNAUTHORIZED.value(),"拦截器校验不通过");
        }
        return true;
    }
}
