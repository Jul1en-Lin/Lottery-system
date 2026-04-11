package com.julien.lotterysystem.common.advice;

import com.julien.lotterysystem.entity.response.Result;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import tools.jackson.databind.ObjectMapper;

@ControllerAdvice
public class ResponseAdvice implements ResponseBodyAdvice {


    private final ObjectMapper objectMapper;
    public ResponseAdvice(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    // 默认放行
    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return true;
    }

    @Override
    public @Nullable Object beforeBodyWrite(@Nullable Object body, MethodParameter returnType, MediaType selectedContentType, Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        // 如果是 text/html 类型的响应（如 SPA fallback 的 index.html），不进行包装
        if (selectedContentType != null && selectedContentType.includes(MediaType.TEXT_HTML)) {
            return body;
        }

        if (body instanceof Result<?>){
            return body;
        }
        else if (body instanceof String){
            return objectMapper.writeValueAsString(Result.success(body));
        }
        return Result.success(body);
    }
}
