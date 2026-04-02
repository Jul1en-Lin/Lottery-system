package com.julien.lotterysystem.utils;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.crypto.symmetric.AES;
import cn.hutool.crypto.symmetric.SymmetricAlgorithm;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AESUtilTest {

    /**
     * AES对称加密（手机号码）
     */
    @Test
    void encryptPhoneNumber() {
        String content = "114514";
        // 随机生成密钥
        byte[] key = SecureUtil.generateKey(SymmetricAlgorithm.AES.getValue()).getEncoded();
        // 构建
        AES aes = SecureUtil.aes(key);

        // 加密
        byte[] encrypt = aes.encrypt(content);
        System.out.println("加密后的字节数组：" + encrypt);
        // 解密
        byte[] decrypt = aes.decrypt(encrypt);
        System.out.println("解密后的字节数组：" + decrypt);
        // 加密为16进制表示
        String encryptHex = aes.encryptHex(content);
        System.out.println("加密后的16进制字符串：" + encryptHex);
        // 解密为字符串
        String decryptStr = aes.decryptStr(encryptHex, CharsetUtil.CHARSET_UTF_8);
        System.out.println("解密后的字符串：" + decryptStr);
    }

    /**
     * 非对称加密（用户密码）
     */
    @Test
     void encryptPassword() {
        //用无参构造方法时，自动生成随机的公钥 私钥密钥对
        RSA rsa = new RSA();

        //获得私钥
        rsa.getPrivateKey();
        rsa.getPrivateKeyBase64();
        // 获得公钥
        rsa.getPublicKey();
        rsa.getPublicKeyBase64();

        // 公钥加密，私钥解密
        byte[] encrypt = rsa.encrypt(StrUtil.bytes("我是一段测试aaaa", CharsetUtil.CHARSET_UTF_8), KeyType.PublicKey);
        byte[] decrypt = rsa.decrypt(encrypt, KeyType.PrivateKey);

    }
}
