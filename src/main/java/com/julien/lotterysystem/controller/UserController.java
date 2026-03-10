package com.julien.lotterysystem.controller;

import com.julien.lotterysystem.entity.request.PasswordLoginRequest;
import com.julien.lotterysystem.entity.request.EmailLoginRequest;
import com.julien.lotterysystem.entity.request.EmailRegisterRequest;
import com.julien.lotterysystem.entity.request.UserRegisterRequest;
import com.julien.lotterysystem.entity.response.UserLoginResponse;
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
    public UserResponse register(@Valid @RequestBody UserRegisterRequest request) {
        return userService.register(request);
    }

    /**
     * 向邮箱发送注册验证码
     */
    @PostMapping("/sendEmailCode")
    public void sendEmailCode(@RequestParam @NotBlank(message = "邮箱不能为空") String email) {
        userService.sendEmailCode(email);
    }

    /**
     * 向管理员邮箱发送登录验证码
     */
    @PostMapping("/admin/sendEmailCode")
    public void sendAdminEmailCode(@RequestParam @NotBlank(message = "邮箱不能为空") String email) {
        userService.sendAdminEmailCode(email);
    }

    /**
     * 管理员邮箱验证码登录
     */
    @PostMapping("/admin/emailLogin")
    public UserLoginResponse adminEmailLogin(@Valid @RequestBody EmailLoginRequest request) {
        return userService.adminEmailLogin(request);
    }

    /**
     * 管理员密码登录
     */
    @PostMapping("/admin/passwordLogin")
    public UserLoginResponse adminPasswordLogin(@Valid @RequestBody PasswordLoginRequest request) {
        return userService.adminPasswordLogin(request);
    }

    /**
     * 邮箱验证码注册（需先调用 sendEmailCode 获取验证码）
     */
    @PostMapping("/emailRegister")
    public UserResponse emailRegister(@Valid @RequestBody EmailRegisterRequest request) {
        return userService.emailRegister(request);
    }
}
