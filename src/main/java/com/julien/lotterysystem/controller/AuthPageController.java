package com.julien.lotterysystem.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthPageController {

    @GetMapping("/")
    public String root() {
        return "redirect:/login";
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
}