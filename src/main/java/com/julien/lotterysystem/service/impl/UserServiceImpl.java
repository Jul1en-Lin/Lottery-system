package com.julien.lotterysystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.julien.lotterysystem.common.enums.UserIdentity;
import com.julien.lotterysystem.common.exception.LotteryException;
import com.julien.lotterysystem.common.utils.Md5Util;
import com.julien.lotterysystem.common.utils.RegexUtil;
import com.julien.lotterysystem.entity.dataobject.Encrypt;
import com.julien.lotterysystem.entity.dataobject.User;
import com.julien.lotterysystem.entity.request.EmailLoginRequest;
import com.julien.lotterysystem.entity.request.EmailRegisterRequest;
import com.julien.lotterysystem.entity.request.UserRequest;
import com.julien.lotterysystem.entity.response.EmailLoginResponse;
import com.julien.lotterysystem.entity.response.UserResponse;
import com.julien.lotterysystem.mapper.UserMapper;
import com.julien.lotterysystem.service.MailService;
import com.julien.lotterysystem.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    /** Redis中邮箱验证码的key前缀 */
    private static final String EMAIL_CODE_PREFIX = "email:code:";
    /** 邮箱验证码过期时间（分钟） */
    private static final long EMAIL_CODE_EXPIRE_MINUTES = 1;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private MailService mailService;

    @Override
    public UserResponse register(UserRequest request) {
        // 校验
        if (!checkRegisterInfo(request)) {
            throw new LotteryException(HttpStatus.BAD_REQUEST.value(), "注册信息校验失败");
        }

        // 存储数据
        return insertUser(request);
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

    private UserResponse insertUser(UserRequest request) {
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
        log.info("用户注册成功，用户名：{}", user.getUserName());
        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        return userResponse;
    }
    @Override
    public void sendEmailCode(String email) {
        // 校验邮箱格式
        if (!RegexUtil.checkMail(email)) {
            throw new LotteryException(HttpStatus.BAD_REQUEST.value(), "邮箱格式错误");
        }

        // 防止频繁发送：检查Redis中是否已存在未过期的验证码
        String redisKey = EMAIL_CODE_PREFIX + email;
        Long ttl = stringRedisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
        if (ttl != null && ttl > (EMAIL_CODE_EXPIRE_MINUTES - 1) * 60) {
            throw new LotteryException(HttpStatus.BAD_REQUEST.value(), "验证码已发送，请稍后再试");
        }

        // 生成6位数字验证码
        String code = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));

        // 存入Redis，设置过期时间
        stringRedisTemplate.opsForValue().set(redisKey, code, EMAIL_CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

        // 发送邮件
        mailService.sendVerificationCode(email, code);
        log.info("邮箱验证码已发送，邮箱：{}", email);
    }

    @Override
    public EmailLoginResponse emailLogin(EmailLoginRequest request) {
        String email = request.getEmail();
        String code = request.getCode();

        // 校验邮箱格式
        if (!RegexUtil.checkMail(email)) {
            throw new LotteryException(HttpStatus.BAD_REQUEST.value(), "邮箱格式错误");
        }

        // 从Redis获取验证码
        String redisKey = EMAIL_CODE_PREFIX + email;
        String cachedCode = stringRedisTemplate.opsForValue().get(redisKey);

        if (!StringUtils.hasText(cachedCode)) {
            throw new LotteryException(HttpStatus.BAD_REQUEST.value(), "验证码已过期，请重新获取");
        }

        if (!cachedCode.equals(code)) {
            throw new LotteryException(HttpStatus.BAD_REQUEST.value(), "验证码错误");
        }

        // 验证码正确，删除Redis中的验证码（一次性使用）
        stringRedisTemplate.delete(redisKey);

        // 查询用户
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, email));
        if (user == null) {
            // 邮箱未注册，提示前端跳转注册页
            log.info("邮箱未注册，需跳转注册页，email：{}", email);
            return new EmailLoginResponse(false, null);
        }

        log.info("用户邮箱登录成功，userId：{}, email：{}", user.getId(), email);
        return new EmailLoginResponse(true, user.getId());
    }

    @Override
    public UserResponse emailRegister(EmailRegisterRequest request) {
        String email = request.getEmail();
        String code = request.getCode();

        // 校验邮箱格式
        if (!RegexUtil.checkMail(email)) {
            throw new LotteryException(HttpStatus.BAD_REQUEST.value(), "邮箱格式错误");
        }

        // 从Redis获取验证码
        String redisKey = EMAIL_CODE_PREFIX + email;
        String cachedCode = stringRedisTemplate.opsForValue().get(redisKey);

        if (!StringUtils.hasText(cachedCode)) {
            throw new LotteryException(HttpStatus.BAD_REQUEST.value(), "验证码已过期，请重新获取");
        }

        if (!cachedCode.equals(code)) {
            throw new LotteryException(HttpStatus.BAD_REQUEST.value(), "验证码错误");
        }

        // 验证码正确，删除Redis中的验证码
        stringRedisTemplate.delete(redisKey);

        // 校验邮箱是否已被注册
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, email));
        if (count > 0) {
            throw new LotteryException(HttpStatus.BAD_REQUEST.value(), "该邮箱已被注册");
        }

        // 校验手机号格式
        if (!RegexUtil.checkMobile(request.getPhoneNumber())) {
            throw new LotteryException(HttpStatus.BAD_REQUEST.value(), "手机号格式错误");
        }

        // 校验手机号是否已被注册
        Long phoneCount = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getPhoneNumber, new Encrypt(request.getPhoneNumber())));
        if (phoneCount > 0) {
            throw new LotteryException(HttpStatus.BAD_REQUEST.value(), "该手机号已被注册");
        }

        // 检验密码（管理员不可为空）
        if (request.getIdentity().equalsIgnoreCase(UserIdentity.ADMIN.getIdentity())
                && !StringUtils.hasText(request.getPassword())) {
            throw new LotteryException(HttpStatus.BAD_REQUEST.value(), "管理员密码不能为空");
        }

        // 存储用户
        User user = new User();
        user.setUserName(request.getUserName());
        user.setEmail(email);
        user.setPhoneNumber(new Encrypt(request.getPhoneNumber()));
        user.setPassword(StringUtils.hasText(request.getPassword())
                ? Md5Util.encryptPassword(request.getPassword()) : null);
        user.setIdentity(request.getIdentity());
        try {
            userMapper.insert(user);
        } catch (Exception e) {
            log.error("邮箱注册插入用户失败", e);
            throw new RuntimeException(e);
        }

        log.info("用户邮箱注册成功，userId：{}, email：{}", user.getId(), email);
        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        return userResponse;
    }
}
