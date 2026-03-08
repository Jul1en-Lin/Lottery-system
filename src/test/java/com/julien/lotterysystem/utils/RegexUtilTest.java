package com.julien.lotterysystem.utils;

import com.julien.lotterysystem.common.utils.RegexUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegexUtilTest {

    @Test
    void shouldAllowValidCommonEmails() {
        assertTrue(RegexUtil.checkMail("1577045857@qq.com"));
        assertTrue(RegexUtil.checkMail("vip_user@vip.qq.com"));
        assertTrue(RegexUtil.checkMail("test.user+tag@gmail.com"));
        assertTrue(RegexUtil.checkMail("dev_team@sub.example.com"));
        assertTrue(RegexUtil.checkMail("owner@startup.co"));
    }

    @Test
    void shouldRejectInvalidEmailFormats() {
        assertFalse(RegexUtil.checkMail(null));
        assertFalse(RegexUtil.checkMail(""));
        assertFalse(RegexUtil.checkMail("157704587@qq"));
        assertFalse(RegexUtil.checkMail("157704587@qq.c"));
        assertFalse(RegexUtil.checkMail(".user@qq.com"));
        assertFalse(RegexUtil.checkMail("user@qq..com"));
    }

    @Test
    void shouldRejectCommonProviderTypos() {
        assertFalse(RegexUtil.checkMail("157704587@qq.co"));
        assertFalse(RegexUtil.checkMail("test@gmail.co"));
        assertFalse(RegexUtil.checkMail("hello@163.co"));
        assertFalse(RegexUtil.checkMail("user@outlook.cn"));
    }
}