package com.julien.lotterysystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.julien.lotterysystem.common.enums.UserIdentity;
import com.julien.lotterysystem.common.utils.AESUtil;
import com.julien.lotterysystem.common.utils.Md5Util;
import com.julien.lotterysystem.common.utils.RegexUtil;
import com.julien.lotterysystem.entity.dataobject.User;
import com.julien.lotterysystem.entity.request.UserRequest;
import com.julien.lotterysystem.entity.response.UserResponse;
import com.julien.lotterysystem.mapper.UserMapper;
import com.julien.lotterysystem.service.UserService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public UserResponse register(UserRequest request) {
        // 校验、二次处理
        checkRegisterInfo(request);

        // 数据加密
        // 1. 密码加密（MD5）
        String encryptedPassword = Md5Util.encryptPassword(request.getPassword());
        // 2. 手机号加密（AES）
        String encryptedPhoneNumber = AESUtil.encryptPhoneNumber(request.getPhoneNumber());

        // 存储数据

    }



    /**
     * 注册参数校验
     */
    private void checkRegisterInfo(UserRequest request) {
        // 校验邮箱格式
        if (!RegexUtil.checkMail(request.getEmail())) {
            throw new IllegalArgumentException("邮箱格式错误");
        }
        // 校验手机号格式
        if (!RegexUtil.checkMobile(request.getPhoneNumber())) {
            throw new IllegalArgumentException("手机号格式错误");
        }

        // 检验密码（管理员不可为空）
        if (request.getIdentity().equalsIgnoreCase(UserIdentity.ADMIN.getIdentity())
                && !StringUtils.hasText(request.getPassword())) {
            throw new IllegalArgumentException("管理员密码不能为空");
        }

        checkEmailUsed(request);
        checkPhoneUsed(request);



    }

    /**
     * 校验手机号是否被注册过
     */
    private void checkPhoneUsed(UserRequest request) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getPhoneNumber, request.getPhoneNumber()));
        if (count > 0) {
            throw new IllegalArgumentException("该手机号已被注册");
        }
    }
    /**
     * 校验邮箱是否被注册过
     */
    private void checkEmailUsed(UserRequest request) {
        // 校验邮箱是否被注册过
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, request.getEmail()));
        if (count > 0) {
            throw new IllegalArgumentException("该邮箱或手机号已被注册");
        }
    }
}
