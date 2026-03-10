package com.julien.lotterysystem.common.config;

import com.julien.lotterysystem.common.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/",
                    "/login",
                    "/login.html",
                    "/signup",
                    "/signup.html",
                    "/forgot-password",
                    "/forgot-password.html",
                    "/admin/activity-center",
                    "/admin-activity-center.html",
                    "/css/**",
                    "/js/**",
                    "/user/admin/passwordLogin",
                    "/user/admin/emailLogin",
                    "/user/sendEmailCode",
                    "/user/emailRegister",
                    "/user/register",
                    "/user/admin/sendEmailCode");
    }
}
