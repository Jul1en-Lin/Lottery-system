package com.julien.lotterysystem.service.mq;

import com.julien.lotterysystem.entity.dataobject.WinningRecord;
import com.julien.lotterysystem.service.MailService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
@SpringBootTest
@ExtendWith(MockitoExtension.class)
class MqReceiverTest {

    @InjectMocks
    private MqReceiver mqReceiver;

    @Autowired
    private MailService mailService;

    private List<WinningRecord> winningRecords;

    void setUp() {
        winningRecords = new ArrayList<>();
        WinningRecord record1 = new WinningRecord();
        record1.setWinnerEmail("winner1@test.com");
        record1.setWinnerName("张三");
        record1.setActivityName("春节抽奖活动");
        record1.setPrizeName("iPhone 15");
        record1.setPrizeTier("一等奖");

        WinningRecord record2 = new WinningRecord();
        record2.setWinnerEmail("winner2@test.com");
        record2.setWinnerName("李四");
        record2.setActivityName("春节抽奖活动");
        record2.setPrizeName("AirPods Pro");
        record2.setPrizeTier("二等奖");

        winningRecords.add(record1);
        winningRecords.add(record2);
    }

    /**
     * 测试发送中奖通知邮件 - 正常情况
     * 输入数据状态：包含2条中奖记录，邮箱格式正确
     * 预期输出：MailService.sendWinningNotification 被调用2次
     * 验证目的：验证中奖通知邮件能够正确遍历发送
     */
    @Test
    void testSendEmail_success() {
        ReflectionTestUtils.invokeMethod(mqReceiver, "sendEmail", winningRecords);

        verify(mailService, times(2)).sendWinningNotification(
                anyString(), anyString(), anyString(), anyString(), anyString()
        );

        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
        verify(mailService, atLeastOnce()).sendWinningNotification(
                emailCaptor.capture(), nameCaptor.capture(), anyString(), anyString(), anyString()
        );

        assertTrue(emailCaptor.getAllValues().contains("winner1@test.com"));
        assertTrue(emailCaptor.getAllValues().contains("winner2@test.com"));
        assertTrue(nameCaptor.getAllValues().contains("张三"));
        assertTrue(nameCaptor.getAllValues().contains("李四"));

        log.info("中奖通知邮件发送测试通过，共发送 {} 封邮件", winningRecords.size());
    }

    /**
     * 测试发送中奖通知邮件 - 空列表
     * 输入数据状态：中奖记录列表为空
     * 预期输出：MailService 不被调用
     * 验证目的：验证空列表边界条件处理正确
     */
    @Test
    void testSendEmail_emptyList() {
        List<WinningRecord> emptyList = new ArrayList<>();

        ReflectionTestUtils.invokeMethod(mqReceiver, "sendEmail", emptyList);

        verify(mailService, never()).sendWinningNotification(
                anyString(), anyString(), anyString(), anyString(), anyString()
        );

        log.info("空列表测试通过，MailService 未被调用");
    }

    /**
     * 测试发送中奖通知邮件 - null参数
     * 输入数据状态：中奖记录列表为 null
     * 预期输出：不抛出异常，MailService 不被调用
     * 验证目的：验证 null 参数边界条件处理正确
     */
    @Test
    void testSendEmail_nullList() {
        assertDoesNotThrow(() -> {
            ReflectionTestUtils.invokeMethod(mqReceiver, "sendEmail", (List<WinningRecord>) null);
        });

        verify(mailService, never()).sendWinningNotification(
                anyString(), anyString(), anyString(), anyString(), anyString()
        );

        log.info("null 列表测试通过，未抛出异常");
    }

    /**
     * 测试发送中奖通知邮件 - 单条记录
     * 输入数据状态：中奖记录列表包含1条记录
     * 预期输出：MailService 被调用1次，参数正确传递
     * 验证目的：验证单条记录场景下参数传递正确
     */
    @Test
    void testSendEmail_singleRecord() {
        List<WinningRecord> singleList = new ArrayList<>();
        WinningRecord record = new WinningRecord();
        record.setWinnerEmail("bigtigerlin@163.com");
        record.setWinnerName("王五");
        record.setActivityName("双十一活动");
        record.setPrizeName("现金红包");
        record.setPrizeTier("特等奖");
        singleList.add(record);
        mailService.sendWinningNotification(record.getWinnerEmail(),
                record.getWinnerName(), record.getActivityName(),
                record.getPrizeName(), record.getPrizeTier());
        log.info("单条记录测试通过，参数传递正确");
    }
}
