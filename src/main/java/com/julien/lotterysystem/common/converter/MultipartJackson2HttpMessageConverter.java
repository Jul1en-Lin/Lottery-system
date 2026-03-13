package com.julien.lotterysystem.common.converter;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import tools.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * 自定义 multipart 参数转换器。
 * 目标是让 @RequestPart("params") 能解析 text/plain 的 JSON 文本。
 */
@Component
public class MultipartJackson2HttpMessageConverter implements HttpMessageConverter<Object> {

    private static final List<MediaType> SUPPORTED_MEDIA_TYPES = Arrays.asList(
            MediaType.APPLICATION_JSON,
            MediaType.TEXT_PLAIN,
            MediaType.APPLICATION_OCTET_STREAM
    );

    private final ObjectMapper objectMapper;

    public MultipartJackson2HttpMessageConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean canRead(Class<?> clazz, MediaType mediaType) {
        return isSupportedMediaType(mediaType);
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        return false;
    }

    @Override
    public List<MediaType> getSupportedMediaTypes() {
        return SUPPORTED_MEDIA_TYPES;
    }

    @Override
    public Object read(Class<?> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        // multipart 中 params 为 text 时，按 UTF-8 文本读取后再转对象。
        byte[] bodyBytes = inputMessage.getBody().readAllBytes();
        String body = new String(bodyBytes, StandardCharsets.UTF_8);
        if (body.isBlank()) {
            throw new HttpMessageNotReadableException("请求体为空", inputMessage);
        }
        try {
            return objectMapper.readValue(body, clazz);
        }
        catch (Exception e) {
            throw new HttpMessageNotReadableException("params不是合法JSON", e, inputMessage);
        }
    }

    @Override
    public void write(Object o, MediaType contentType, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        // 该转换器只用于请求反序列化，不参与响应序列化。
        throw new HttpMessageNotWritableException("MultipartJackson2HttpMessageConverter不支持写入");
    }

    private boolean isSupportedMediaType(MediaType mediaType) {
        if (mediaType == null) {
            return true;
        }
        return SUPPORTED_MEDIA_TYPES.stream().anyMatch(mediaType::isCompatibleWith);
    }
}
