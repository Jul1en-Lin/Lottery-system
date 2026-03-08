package com.julien.lotterysystem.controller;

import com.julien.lotterysystem.entity.request.EmailLoginRequest;
import com.julien.lotterysystem.entity.request.EmailRegisterRequest;
import com.julien.lotterysystem.entity.request.UserRequest;
import com.julien.lotterysystem.entity.response.EmailLoginResponse;
import com.julien.lotterysystem.entity.response.UserResponse;
import com.julien.lotterysystem.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@Validated
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public UserResponse register(@Valid @RequestBody UserRequest request) {
        return userService.register(request);
    }

    /**
     * 发送邮箱验证码（登录和注册通用）
     */
    @PostMapping("/sendEmailCode")
    public void sendEmailCode(@RequestParam @NotBlank(message = "邮箱不能为空") String email) {
        userService.sendEmailCode(email);
    }

    /**
     * 邮箱验证码登录（未注册时返回 registered=false，前端跳转注册页——）
     */
    @PostMapping("/emailLogin")
    public EmailLoginResponse emailLogin(@Valid @RequestBody EmailLoginRequest request) {
        return userService.emailLogin(request);
    }

    /**
     * 邮箱验证码注册（需先调用 sendEmailCode 获取验证码）
     */
    @PostMapping("/emailRegister")
    public UserResponse emailRegister(@Valid @RequestBody EmailRegisterRequest request) {
        return userService.emailRegister(request);
    }
}
