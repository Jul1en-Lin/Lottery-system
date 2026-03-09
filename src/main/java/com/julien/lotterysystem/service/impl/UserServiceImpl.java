package com.julien.lotterysystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.julien.lotterysystem.common.enums.UserIdentity;
import com.julien.lotterysystem.common.exception.LotteryException;
import com.julien.lotterysystem.common.utils.JwtUtil;
import com.julien.lotterysystem.common.utils.Md5Util;
import com.julien.lotterysystem.common.utils.RegexUtil;
import com.julien.lotterysystem.entity.dataobject.Encrypt;
import com.julien.lotterysystem.entity.dataobject.User;
import com.julien.lotterysystem.entity.request.PasswordLoginRequest;
import com.julien.lotterysystem.entity.request.EmailLoginRequest;
import com.julien.lotterysystem.entity.request.EmailRegisterRequest;
import com.julien.lotterysystem.entity.request.UserRegisterRequest;
import com.julien.lotterysystem.entity.response.UserLoginResponse;
import com.julien.lotterysystem.entity.response.UserResponse;
import com.julien.lotterysystem.mapper.UserMapper;
import com.julien.lotterysystem.service.MailService;
import com.julien.lotterysystem.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    /** Redis中邮箱验证码的key前缀 */
    private static final String EMAIL_CODE_PREFIX = "email:code:";
    /** 邮箱验证码过期时间（分钟） */
    private static final long EMAIL_CODE_EXPIRE_MINUTES = 5;
    /** 防刷间隔（秒），过期时间内剩余TTL大于此值时拒绝重发 */
    private static final long EMAIL_CODE_RESEND_INTERVAL_SECONDS = 240;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private MailService mailService;

    @Override
    public UserResponse register(UserRegisterRequest request) {
        // 校验
        checkRegisterInfo(request);
        // 存储数据
        return insertUser(request);
    }

    /**
     * 用户注册参数校验（普通用户管理员通用）
     */
    private void checkRegisterInfo(UserRegisterRequest request) {
        // 校验格式
        checkUserInfoFormat(request);
        // 检验密码（如管理员则不可为空）
        if (request.getIdentity().equalsIgnoreCase(UserIdentity.ADMIN.getIdentity())
                && !StringUtils.hasText(request.getPassword())) {
            throw new LotteryException(HttpStatus.BAD_REQUEST.value(),"管理员密码不能为空");
        }
        checkEmailUsed(request);
        checkPhoneUsed(request);
    }

    /**
     * 检验参数格式是否正确
     */
    private void checkUserInfoFormat(UserRegisterRequest request) {
        // 校验邮箱格式
        validateEmail(request.getEmail());
        // 校验手机号格式
        validatePhoneNumber(request.getPhoneNumber());
    }

    /**
     * 校验手机号是否被注册过
     */
    private void checkPhoneUsed(UserRegisterRequest request) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getPhoneNumber, new Encrypt(request.getPhoneNumber())));
        if (count > 0) {
            throw new LotteryException(HttpStatus.BAD_REQUEST.value(),"该手机号已被注册");
        }
    }

    /**
     * 校验邮箱是否被注册过
     */
    private void checkEmailUsed(UserRegisterRequest request) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, request.getEmail()));
        if (count > 0) {
            throw new LotteryException(HttpStatus.BAD_REQUEST.value(),"该邮箱或手机号已被注册");
        }
    }

    /**
     * 校验邮箱格式
     */
    private void validateEmail(String email) {
        if (!RegexUtil.checkMail(email)) {
            throw new LotteryException(HttpStatus.BAD_REQUEST.value(), "邮箱格式错误");
        }
    }

    /**
     * 校验手机号格式
     */
    private void validatePhoneNumber(String phoneNumber) {
        if (!RegexUtil.checkMobile(phoneNumber)) {
            throw new LotteryException(HttpStatus.BAD_REQUEST.value(),"手机号格式错误");
        }
    }

    /**
     * 一键插入用户
     * @param request：用户请求对象
     */
    private UserResponse insertUser(UserRegisterRequest request) {
        User user = new User();
        user.setUserName(request.getUserName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(new Encrypt(request.getPhoneNumber()));
        // 非管理员可为空
        user.setPassword(StringUtils.hasText(request.getPassword())
                ? Md5Util.encryptPassword(request.getPassword()) : null);
        user.setIdentity(request.getIdentity());
        try {
            userMapper.insert(user);
        } catch (Exception e) {
            log.error("插入用户失败",e);
            throw new RuntimeException(e);
        }
        log.info("用户注册成功，用户名：{}", user.getUserName());
        return new UserResponse(user.getId());
    }


    /**
     * 发送邮箱验证码（管理员）
     */
    @Override
    public void sendAdminEmailCode(String email) {
        validateEmail(email);
        sendEmailCodeInternal(email);
    }

    /**
     * 发送邮箱验证码（内部方法）
     */
    private void sendEmailCodeInternal(String email) {
        // 防止频繁发送：检查Redis中是否已存在未过期的验证码
        String redisKey = EMAIL_CODE_PREFIX + email;
        Long ttl = stringRedisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
        if (ttl != null && ttl > EMAIL_CODE_RESEND_INTERVAL_SECONDS) {
            throw new LotteryException(HttpStatus.BAD_REQUEST.value(), "验证码已发送，请稍后再试");
        }
        // 生成6位数字验证码
        String code = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
        log.info("生成的邮箱验证码为：{}", code);
        // 发送邮件
        mailService.sendVerificationCode(email, code);

        // 存入Redis，设置过期时间
        // 先发送邮件，避免Redis存入无效验证码
        stringRedisTemplate.opsForValue().set(redisKey, code, EMAIL_CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        log.info("邮箱验证码已发送，邮箱：{}", email);
    }

    /**
     * 管理员邮箱验证码登录
     */
    @Override
    public UserLoginResponse adminEmailLogin(@Valid EmailLoginRequest request) {
        String email = request.getEmail();
        // 校验邮箱格式
        validateEmail(email);
        // 校验管理员邮箱是否存在
        User user = getAdminByEmail(email);
        // 校验邮箱验证码
        consumeEmailCode(email, request.getCode());
        log.info("管理员邮箱验证码登录成功，userId：{}, email：{}", user.getId(), email);
        // 生成jwt-token
        String token = generateJwtToken(user.getId(), user.getIdentity());
        return new UserLoginResponse(token,user.getId());
    }

    /**
     * 管理员密码登录
     */
    @Override
    public UserLoginResponse adminPasswordLogin(@Valid PasswordLoginRequest request) {
        String email = request.getEmail();
        // 校验邮箱格式
        validateEmail(email);
        // 校验管理员邮箱是否存在
        User user = getAdminByEmail(email);
        if (!StringUtils.hasText(user.getPassword())) {
            throw new LotteryException(HttpStatus.BAD_REQUEST.value(), "该管理员未设置密码，请使用邮箱验证登录");
        }
        if (!Md5Util.verifyPassword(request.getPassword(), user.getPassword())) {
            throw new LotteryException(HttpStatus.BAD_REQUEST.value(), "密码错误");
        }
        log.info("管理员密码登录成功，userId：{}, email：{}", user.getId(), email);
        // 生成jwt-token
        String token = generateJwtToken(user.getId(), user.getIdentity());
        return new UserLoginResponse(token,user.getId());
    }

    /**
     * 生成并返回Jwt-token
     * 负载claims：userId、identity，可扩展其他信息
     */
    private String generateJwtToken(Long userId, String identity) {
        HashMap<String,Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("identity", identity);
        return JwtUtil.genJwt(claims);
    }

    /**
     * 从Redis获取验证码并验证邮箱验证码
     */
    private void consumeEmailCode(String email, String code) {
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
    }

    /**
     * 根据邮箱查询管理员用户
     */
    private User getAdminByEmail(String email) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, email));
        if (user == null) {
            throw new LotteryException(HttpStatus.BAD_REQUEST.value(), "管理员账号不存在");
        }
        if (!UserIdentity.ADMIN.getIdentity().equalsIgnoreCase(user.getIdentity())) {
            throw new LotteryException(HttpStatus.FORBIDDEN.value(), "该邮箱属于普通用户，只有管理员才有权限登录");
        }
        return user;
    }

    /**
     * 邮箱验证码注册
     */
    @Override
    public UserResponse emailRegister(EmailRegisterRequest request) {
        String email = request.getEmail();
        String code = request.getCode();
        log.info("邮箱验证码注册请求，email：{}, code：{}", email, code);
        // 校验邮箱格式
        validateEmail(email);
        // 从Redis获取验证码并验证邮箱验证码
        consumeEmailCode(email, code);
        // 校验注册信息
        checkRegisterInfo(request); // 多了一层邮箱校验
        // 插入用户数据
        return insertUser(request);
    }
}
