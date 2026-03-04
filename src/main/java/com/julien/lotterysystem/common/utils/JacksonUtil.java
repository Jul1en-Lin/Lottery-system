package com.julien.lotterysystem.common.utils;


import com.julien.lotterysystem.entity.response.Result;
import org.springframework.boot.json.JsonParseException;
import org.springframework.util.ReflectionUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.Callable;

// TODO：尝试Hutool的Json工具
public class JacksonUtil {

    private static final ObjectMapper OBJECT_MAPPER;
    static {
        OBJECT_MAPPER = new ObjectMapper();
    }


    /**
     * 借鉴 package org.springframework.boot.json 包下 JacksonJsonParser 反序列化类的公共tryParse方法，便于统一异常处理
     * 包装成公共方法
     */
    private static <T> T tryParse(Callable<T> parser, Class<? extends Exception> check) {
        try {
            return parser.call();
        }
        catch (Exception ex) {
            if (check.isAssignableFrom(ex.getClass())) {
                throw new JsonParseException(ex);
            }
            ReflectionUtils.rethrowRuntimeException(ex);
            throw new IllegalStateException(ex);
        }
    }

    /**
     * 进一步封装 tryParse 方法，默认处理 JacksonException 异常
     */
    private static <T> T tryParse(Callable<T> parser) {
        return tryParse(parser, JacksonException.class);
    }

    /**
     * 序列化
     */
    public static String serialize(Object object) {
        return JacksonUtil.tryParse(() -> OBJECT_MAPPER.writeValueAsString(object));
    }

    /**
     * 反序列化 Result 类型的 JSON 字符串
     * @param json
     * @param result
     * @return 反序列化后的 Result 对象
     */
    public static <T> Result<T> deSerialize(String json, Result<T> result) {
        return JacksonUtil.tryParse(() -> OBJECT_MAPPER.readValue(json, result.getClass()));
    }

    /**
     * 反序列化 List 类型的 JSON 字符串
     * @param json
     * @param results
     * @return 反序列化后的 List 对象
     * @param <T>
     */
    public static <T> T deSerialize(String json, Class<?> results) {
        JavaType Type = OBJECT_MAPPER.getTypeFactory().constructParametricType(results, Result.class);
        return JacksonUtil.tryParse(() -> OBJECT_MAPPER.readValue(json, Type));
    }
}
