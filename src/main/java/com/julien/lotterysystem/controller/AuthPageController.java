package com.julien.lotterysystem.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 页面路由转发：把浏览器访问的路径映射到对应的前端页面。
 */
@Controller
public class AuthPageController {

    @GetMapping("/")
    public String root() {
        return "redirect:/login";
    }

    /**
     * 处理活动详情页动态路由 /activity/{id}
     * Vue Router history 模式需要后端支持
     */
    @GetMapping("/activity/{id}")
    public String activityDetail(@PathVariable String id) {
        return "forward:/activity-detail.html";
    }

    @GetMapping("/login")
    public String login() {
        return "forward:/login.html";
    }

    @GetMapping("/signup")
    public String signup() {
        return "forward:/signup.html";
    }

    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "forward:/forgot-password.html";
    }

    @GetMapping("/admin/activity-center")
    public String adminActivityCenter() {
        return "forward:/admin-activity-center.html";
    }

    @GetMapping("/homepage")
    public String homepage() {
        return "forward:/homepage.html";
    }

    @GetMapping("/activities")
    public String activities() {
        return "forward:/activity-list.html";
    }

    @GetMapping("/activity-list")
    public String activityList() {
        return "forward:/activity-list.html";
    }

    @GetMapping("/activity-detail")
    public String activityDetail() {
        return "forward:/activity-detail.html";
    }

    @GetMapping("/user-center")
    public String userCenter() {
        return "forward:/user-center.html";
    }

    @GetMapping("/prize-record")
    public String prizeRecord() {
        return "forward:/prize-record.html";
    }

    /**
     * Vue Router 前端路由支持
     */
    @GetMapping("/prizes")
    public String prizes() {
        return "forward:/prize-record.html";
    }

    @GetMapping("/user")
    public String user() {
        return "forward:/user-center.html";
    }
}