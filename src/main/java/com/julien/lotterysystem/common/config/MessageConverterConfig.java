package com.julien.lotterysystem.common.config;

import com.julien.lotterysystem.common.converter.MultipartJackson2HttpMessageConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * 注册自定义消息转换器，兼容 multipart/form-data 中 text 类型的 JSON 参数。
 */
@Configuration
public class MessageConverterConfig implements WebMvcConfigurer {

    private final MultipartJackson2HttpMessageConverter multipartJackson2HttpMessageConverter;

    public MessageConverterConfig(MultipartJackson2HttpMessageConverter multipartJackson2HttpMessageConverter) {
        this.multipartJackson2HttpMessageConverter = multipartJackson2HttpMessageConverter;
    }

    /**
     * 将自定义转换器放在前面，优先处理 @RequestPart 场景下的 params 文本反序列化。
     */
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(0, multipartJackson2HttpMessageConverter);
    }
}
