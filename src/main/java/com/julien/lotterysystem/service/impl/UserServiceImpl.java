package com.julien.lotterysystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.julien.lotterysystem.common.enums.UserIdentity;
import com.julien.lotterysystem.common.exception.LotteryException;
import com.julien.lotterysystem.common.utils.AESUtil;
import com.julien.lotterysystem.common.utils.Md5Util;
import com.julien.lotterysystem.common.utils.RegexUtil;
import com.julien.lotterysystem.entity.dataobject.Encrypt;
import com.julien.lotterysystem.entity.dataobject.User;
import com.julien.lotterysystem.entity.request.UserRequest;
import com.julien.lotterysystem.entity.response.UserResponse;
import com.julien.lotterysystem.mapper.UserMapper;
import com.julien.lotterysystem.service.UserService;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public UserResponse register(UserRequest request) {
        // 校验
        if (!checkRegisterInfo(request)) {
            throw new LotteryException(HttpStatus.BAD_REQUEST.value(), "注册信息校验失败");
        }

        // 存储数据
        User user = new User();
        user.setUserName(request.getUserName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(new Encrypt(request.getPhoneNumber()));
        user.setPassword(Md5Util.encryptPassword(request.getPassword()));
        user.setIdentity(request.getIdentity());
        try {
            userMapper.insert(user);
        } catch (Exception e) {
            log.error("插入用户失败",e);
            throw new RuntimeException(e);
        }

        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        return userResponse;
    }



    /**
     * 注册参数校验
     */
    private Boolean checkRegisterInfo(UserRequest request) {
        // 校验邮箱格式
        if (!RegexUtil.checkMail(request.getEmail())) {
            throw new LotteryException(HttpStatus.BAD_REQUEST.value(), "邮箱格式错误");
        }
        // 校验手机号格式

        if (!RegexUtil.checkMobile(request.getPhoneNumber())) {
            throw new LotteryException(HttpStatus.BAD_REQUEST.value(),"手机号格式错误");
        }

        // 检验密码（如管理员则不可为空）
        if (request.getIdentity().equalsIgnoreCase(UserIdentity.ADMIN.getIdentity())
                && !StringUtils.hasText(request.getPassword())) {
            throw new LotteryException(HttpStatus.BAD_REQUEST.value(),"管理员密码不能为空");
        }

        return checkEmailUsed(request) && checkPhoneUsed(request);
    }

    /**
     * 校验手机号是否被注册过
     */
    private Boolean checkPhoneUsed(UserRequest request) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getPhoneNumber, new Encrypt(request.getPhoneNumber())));
        if (count > 0) {
            throw new LotteryException(HttpStatus.BAD_REQUEST.value(),"该手机号已被注册");
        }
        return true;
    }
    /**
     * 校验邮箱是否被注册过
     */
    private Boolean checkEmailUsed(UserRequest request) {
        // 校验邮箱是否被注册过
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, request.getEmail()));
        if (count > 0) {
            throw new LotteryException(HttpStatus.BAD_REQUEST.value(),"该邮箱或手机号已被注册");
        }
        return true;
    }
}
