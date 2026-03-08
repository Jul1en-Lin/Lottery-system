package com.julien.lotterysystem.common.utils;

import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.regex.Pattern;

public class RegexUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9](?:[A-Za-z0-9._%+\\-]{0,62}[A-Za-z0-9])?@(?:[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?\\.)+[A-Za-z]{2,10}$"
    );

    private static final Pattern COMMON_PROVIDER_DOMAIN_PATTERN = Pattern.compile(
        "^(?:[A-Za-z0-9-]+\\.)?(qq|foxmail|gmail|163|126|yeah|outlook|hotmail|live|sina|sohu)\\..+$"
    );

    /**
     * 校验邮箱：
     * @param content: xxx@xx.xxx(形如：abc@qq.com)
     */
    public static boolean checkMail(String content) {
        if (!StringUtils.hasText(content)) {
            return false;
        }
        String email = content.trim();
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return false;
        }

        String domain = email.substring(email.indexOf('@') + 1).toLowerCase(Locale.ROOT);
        if (COMMON_PROVIDER_DOMAIN_PATTERN.matcher(domain).matches()) {
            return isKnownProviderDomain(domain);
        }
        return true;
    }

    private static boolean isKnownProviderDomain(String domain) {
        return switch (domain) {
            case "qq.com", "vip.qq.com", "foxmail.com",
                 "gmail.com",
                 "163.com", "126.com", "yeah.net",
                 "outlook.com", "hotmail.com", "live.com",
                 "sina.com", "sina.cn",
                 "sohu.com" -> true;
            default -> false;
        };
    }

    /**
     * 校验手机号码
     * @param content: 手机号码以1开头的11位数字
     */
    public static boolean checkMobile(String content) {
        if (!StringUtils.hasText(content)) {
            return false;
        }
        /**
         * ^ 表示匹配字符串的开始。
         * 1 表示手机号码以数字1开头。
         * [3-9] 表示接下来的数字是3到9之间的任意一个数字。这是中国大陆手机号码的第二位数字，通常用来区分不同的运营商。
         * \d{9} 表示后面跟着9个数字，这代表手机号码的剩余部分。
         * $ 表示匹配字符串的结束。
         */
        String regex = "^1[3-9]\\d{9}$";
        return Pattern.matches(regex, content);
    }

    /**
     * 校验密码强度
    * @param content：密码强度正则，6到20位
     * @return
     */
    public static boolean checkPassword(String content){
        if (!StringUtils.hasText(content)) {
            return false;
        }
        /**
         * ^ 表示匹配字符串的开始。
         * [0-9A-Za-z] 表示匹配的字符可以是：
         * 0-9：任意一个数字（0到9）。
         * A-Z：任意一个大写字母（从A到Z）。
         * a-z：任意一个小写字母（从a到z）。
         * {6,20} 表示前面的字符集合（数字、大写字母和小写字母）可以重复出现6到20次。
         * $ 表示匹配字符串的结束。
         */
        String regex= "^[0-9A-Za-z]{6,20}$";
        return Pattern.matches(regex, content);
    }
}

