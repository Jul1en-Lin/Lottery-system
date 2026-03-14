package com.julien.lotterysystem.common.config;

import com.julien.lotterysystem.common.converter.MultipartJackson2HttpMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * 注册自定义消息转换器，兼容 multipart/form-data 中 text 类型的 JSON 参数。
 */
@Configuration
public class MessageConverterConfig implements WebMvcConfigurer {

    private final MultipartJackson2HttpMessageConverter multipartJackson2HttpMessageConverter;
    private final String pictureDestPath;

    public MessageConverterConfig(MultipartJackson2HttpMessageConverter multipartJackson2HttpMessageConverter,
                                  @Value("${picture.destPath}") String pictureDestPath) {
        this.multipartJackson2HttpMessageConverter = multipartJackson2HttpMessageConverter;
        this.pictureDestPath = pictureDestPath;
    }

    /**
     * 将自定义转换器放在前面，优先处理 @RequestPart 场景下的 params 文本反序列化。
     */
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(0, multipartJackson2HttpMessageConverter);
    }

    /**
     * 映射上传图片目录，支持通过 /picture/** 直接访问磁盘中的图片文件。
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String normalized = pictureDestPath.replace("\\", "/");
        if (!normalized.endsWith("/")) {
            normalized = normalized + "/";
        }
        registry.addResourceHandler("/picture/**")
                .addResourceLocations("file:" + normalized);
    }
}
