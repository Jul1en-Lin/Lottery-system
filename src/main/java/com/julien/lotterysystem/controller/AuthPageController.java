package com.julien.lotterysystem.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * SPA 页面路由转发：所有前端路由都返回 index.html
 * Vue Router 的 history 模式需要后端支持
 */
@Controller
public class AuthPageController {

    /**
     * 根路径重定向到登录页
     */
    @GetMapping("/")
    public String root() {
        return "redirect:/login";
    }

    /**
     * 登录页
     */
    @GetMapping("/login")
    public String login() {
        return "forward:/index.html";
    }

    /**
     * 注册页
     */
    @GetMapping("/signup")
    public String signup() {
        return "forward:/index.html";
    }

    /**
     * 忘记密码页
     */
    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "forward:/index.html";
    }

    /**
     * 首页
     */
    @GetMapping("/homepage")
    public String homepage() {
        return "forward:/index.html";
    }

    /**
     * 活动列表页
     */
    @GetMapping("/activities")
    public String activities() {
        return "forward:/index.html";
    }

    /**
     * 活动详情页（动态路由）
     * 使用正则表达式限制只匹配数字ID，避免匹配API路径如 /activity/queryList
     */
    @GetMapping("/activity/{id:\\d+}")
    public String activityDetail(@PathVariable String id) {
        return "forward:/index.html";
    }

    /**
     * 用户中心
     */
    @GetMapping("/user")
    public String user() {
        return "forward:/index.html";
    }

    /**
     * 奖品记录页
     */
    @GetMapping("/prizes")
    public String prizes() {
        return "forward:/index.html";
    }

    /**
     * 管理员活动管理中心
     */
    @GetMapping("/admin")
    public String admin() {
        return "forward:/index.html";
    }

    /**
     * Fallback 路由：捕获所有未匹配的前端路由，直接返回 index.html 内容
     * 确保 SPA 路由刷新时返回正确的 HTML 内容
     */
    @GetMapping("/{*path}")
    @ResponseBody
    public ResponseEntity<String> fallback(@PathVariable String path) {
        try {
            Resource resource = new ClassPathResource("static/index.html");
            InputStream inputStream = resource.getInputStream();
            String html = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(html);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error loading index.html: " + e.getMessage());
        }
    }
}
