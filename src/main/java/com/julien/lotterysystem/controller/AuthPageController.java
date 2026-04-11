package com.julien.lotterysystem.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

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
     * Fallback 路由：捕获所有未匹配的前端路由，返回 index.html
     * 确保 SPA 路由刷新时不会返回 404
     */
    @GetMapping("/{*path}")
    public String fallback() {
        return "forward:/index.html";
    }
}
