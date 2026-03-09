package com.julien.lotterysystem.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
public class Md5Util {
    /**
     * 非对称加密 Md5（用户密码）
     * @return 盐+加密后的密码
     */
    public static String encryptPassword(String password) {
        if (!StringUtils.hasLength(password)) {
            log.error("密码不能为空");
            return null;
        }
        String salt = UUID.randomUUID().toString().replace("-","");
        String finalPassword = salt + password;
        // 加密
        String encryptPassword = DigestUtils.md5DigestAsHex(finalPassword.getBytes(StandardCharsets.UTF_8));
        return  salt + encryptPassword;
    }

    /**
     * Md5 校验密码
     * @param sqlPassword 数据库保存的密码
     */
    public static boolean verifyPassword(String inputPassword, String sqlPassword) {
        if (!StringUtils.hasLength(inputPassword) || !StringUtils.hasLength(sqlPassword)) {
            log.error("密码为空,inputPassword:{},sqlPassword:{}",inputPassword,sqlPassword);
            return false;
        }
        String salt = sqlPassword.substring(0, 32);
        String finalPassword = salt + inputPassword;
        // 加密后校验
        String verifyPassword = DigestUtils.md5DigestAsHex(finalPassword.getBytes(StandardCharsets.UTF_8));
//        System.out.println("salt + verifyPassword: " + (salt + verifyPassword));
//        System.out.println("sqlPassword: " + sqlPassword);
        return sqlPassword.equals(salt + verifyPassword);
    }
}
