package com.julien.lotterysystem.service;


import com.julien.lotterysystem.common.enums.UserIdentityEnum;
import com.julien.lotterysystem.entity.request.PasswordLoginRequest;
import com.julien.lotterysystem.entity.request.EmailLoginRequest;
import com.julien.lotterysystem.entity.request.EmailRegisterRequest;
import com.julien.lotterysystem.entity.request.UserRegisterRequest;
import com.julien.lotterysystem.entity.response.UserInfoResponse;
import com.julien.lotterysystem.entity.response.UserLoginResponse;
import com.julien.lotterysystem.entity.response.UserResponse;
import jakarta.validation.Valid;

import java.util.List;

public interface UserService {

    /**
     * 用户注册（普通用户管理员通用）
     */
    UserResponse register(@Valid UserRegisterRequest request);


    /**
    * 发送邮箱验证码（通用注册）
    */
    void sendEmailCode(String email);

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

    /**
     * 获取用户列表信息
     * @param identity: 若空则返回全部，否则返回指定身份的用户
     */
    List<UserInfoResponse> getListInfo(UserIdentityEnum identity);
}

