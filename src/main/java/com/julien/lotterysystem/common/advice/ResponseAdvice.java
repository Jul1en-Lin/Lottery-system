package com.julien.lotterysystem.common.advice;

import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

public class ResponseAdvice implements ResponseBodyAdvice {

    private ObjectMapper objectMapper;

    // 默认放行
    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return false;
    }

    // TODO: 实现自定义响应体
    @Override
    public @Nullable Object beforeBodyWrite(@Nullable Object body, MethodParameter returnType, MediaType selectedContentType, Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        return null;
    }
}
