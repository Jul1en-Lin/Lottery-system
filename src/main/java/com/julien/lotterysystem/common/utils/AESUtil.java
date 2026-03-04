package com.julien.lotterysystem.common.utils;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import cn.hutool.crypto.symmetric.SymmetricAlgorithm;

public class AESUtil {

    /**
     * AES 对称加密（手机号码）
     */
    public static void encryptPhoneNumber(String phoneNumber) {

        // 随机生成密钥
        byte[] key = SecureUtil.generateKey(SymmetricAlgorithm.AES.getValue()).getEncoded();
        // 构建
        AES aes = SecureUtil.aes(key);
        // 加密为16进制字符串表示
        String encryptHex = aes.encryptHex(phoneNumber);
        System.out.println("加密后的16进制字符串：" + encryptHex);
    }

    public static void decryptPhoneNumber(String encryptHex,AES aes) {
        // 解密为字符串
        String decryptStr = aes.decryptStr(encryptHex, CharsetUtil.CHARSET_UTF_8);
        System.out.println("解密后的字符串：" + decryptStr);
    }
}
