package com.julien.lotterysystem.service;


import com.julien.lotterysystem.entity.request.EmailLoginRequest;
import com.julien.lotterysystem.entity.request.EmailRegisterRequest;
import com.julien.lotterysystem.entity.request.UserRequest;
import com.julien.lotterysystem.entity.response.EmailLoginResponse;
import com.julien.lotterysystem.entity.response.UserResponse;
import jakarta.validation.Valid;

public interface UserService {

    UserResponse register(@Valid UserRequest request);

    /**
     * 发送邮箱验证码（登录和注册通用）
     */
    void sendEmailCode(String email);

    /**
     * 邮箱验证码登录，返回是否已注册
     */
    EmailLoginResponse emailLogin(@Valid EmailLoginRequest request);

    /**
     * 邮箱验证码注册
     */
    UserResponse emailRegister(@Valid EmailRegisterRequest request);
}
