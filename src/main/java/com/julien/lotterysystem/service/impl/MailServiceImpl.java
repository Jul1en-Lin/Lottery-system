package com.julien.lotterysystem.service.impl;

import com.julien.lotterysystem.common.exception.LotteryException;
import com.julien.lotterysystem.common.utils.RegexUtil;
import com.julien.lotterysystem.service.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MailServiceImpl implements MailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Override
    public void sendVerificationCode(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        // 校验邮箱格式
        if (!RegexUtil.checkMail(to)) {
            throw new LotteryException(HttpStatus.BAD_REQUEST.value(), "邮箱格式错误");
        }
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("【抽奖系统】邮箱登录验证码");
        message.setText("您的登录验证码为：" + code + "，有效期5分钟，请勿泄露给他人。");
        try {
            mailSender.send(message);
            log.info("验证码邮件发送成功，收件人：{}", to);
        } catch (Exception e) {
            log.error("验证码邮件发送失败，收件人：{}", to, e);
            throw new LotteryException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "验证码发送失败，请稍后重试");
        }
    }
}
