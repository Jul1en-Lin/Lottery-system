package com.julien.lotterysystem.service;


import com.julien.lotterysystem.entity.request.PasswordLoginRequest;
import com.julien.lotterysystem.entity.request.EmailLoginRequest;
import com.julien.lotterysystem.entity.request.EmailRegisterRequest;
import com.julien.lotterysystem.entity.request.UserRegisterRequest;
import com.julien.lotterysystem.entity.response.UserLoginResponse;
import com.julien.lotterysystem.entity.response.UserResponse;
import jakarta.validation.Valid;

public interface UserService {

    /**
     * 用户注册（普通用户管理员通用）
     */
    UserResponse register(@Valid UserRegisterRequest request);


    /**
     * 向管理员邮箱发送登录验证码
     */
    void sendAdminEmailCode(String email);

    /**
     * 管理员邮箱验证码登录
     */
    UserLoginResponse adminEmailLogin(@Valid EmailLoginRequest request);

    /**
     * 管理员密码登录
     */
    UserLoginResponse adminPasswordLogin(@Valid PasswordLoginRequest request);

    /**
     * 邮箱验证码注册
     */
    UserResponse emailRegister(@Valid EmailRegisterRequest request);
}
