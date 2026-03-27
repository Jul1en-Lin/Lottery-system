package com.julien.lotterysystem.service.activitystatus.operator;

import com.julien.lotterysystem.common.enums.ActivityStatusEnum;
import com.julien.lotterysystem.entity.dataobject.Activity;
import com.julien.lotterysystem.entity.dto.ConvertActivityStatusDTO;
import com.julien.lotterysystem.mapper.ActivityMapper;
import com.julien.lotterysystem.mapper.ActivityPrizeMapper;
import com.julien.lotterysystem.mapper.ActivityUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
public class ActivityOperatorTest {

    @Autowired
    private ActivityOperator activityOperator;

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private ActivityPrizeMapper activityPrizeMapper;

    @Autowired
    private ActivityUserMapper activityUserMapper;

    /**
     * 测试正常扭转活动状态
     */
    @Test
    void testConvert_success() {
        // 准备测试数据 - 需要数据库中存在 id=21 的活动记录
        ConvertActivityStatusDTO dto = new ConvertActivityStatusDTO();
        dto.setActivityId(21L);
        dto.setUserIds(List.of(4L,6L,7L));
        dto.setTargetActivityStatus(ActivityStatusEnum.END);

        // 执行扭转
        Boolean result = activityOperator.convert(dto);

        // 验证结果
        assertTrue(result, "扭转应成功");

        // 验证数据库状态
        Activity activity = activityMapper.selectById(21L);
        assertNotNull(activity);
        assertEquals(ActivityStatusEnum.END.name(), activity.getStatus());

        log.info("活动状态扭转成功，活动id:21, 目标状态:COMPLETED");
    }

    /**
     * 测试扭转不存在的活动 - 返回 false
     */
    @Test
    void testConvert_notFound() {
        ConvertActivityStatusDTO dto = new ConvertActivityStatusDTO();
        dto.setActivityId(99999L);
        dto.setTargetActivityStatus(ActivityStatusEnum.END);

        Boolean result = activityOperator.convert(dto);

        assertFalse(result, "查询不到活动时应返回 false");
        log.info("扭转不存在的活动，返回 false");
    }

    /**
     * 测试 isNeedConvert 方法 - 需要扭转的情况
     */
    @Test
    void testIsNeedConvert_needConvert() {
        // 准备数据 - 确保 activity_id=21 存在
        ConvertActivityStatusDTO dto = new ConvertActivityStatusDTO();
        dto.setActivityId(21L);
        dto.setUserIds(List.of(4L,6L,7L));
        dto.setTargetActivityStatus(ActivityStatusEnum.END);

        Boolean result = activityOperator.isNeedConvert(dto);

        // 注意：结果取决于数据库中实际的状态
        log.info("isNeedConvert 结果: {}", result);
    }

    /**
     * 测试 isNeedConvert 方法 - 查询不到的情况
     */
    @Test
    void testIsNeedConvert_notFound() {
        ConvertActivityStatusDTO dto = new ConvertActivityStatusDTO();
        dto.setActivityId(99999L);
        dto.setTargetActivityStatus(ActivityStatusEnum.END);

        Boolean result = activityOperator.isNeedConvert(dto);

        assertFalse(result, "查询不到活动时应返回 false");
        log.info("isNeedConvert 查询不到记录，返回 false");
    }

    /**
     * 测试 isNeedConvert 方法 - 奖品未全部抽完的情况
     */
    @Test
    void testIsNeedConvert_prizeNotCompleted() {
        // 需要数据库中存在奖品状态为 INIT 的记录
        ConvertActivityStatusDTO dto = new ConvertActivityStatusDTO();
        dto.setActivityId(21L);
        dto.setTargetActivityStatus(ActivityStatusEnum.END);

        Boolean result = activityOperator.isNeedConvert(dto);

        // 如果存在未抽完的奖品，应返回 false
        log.info("奖品未抽完时 isNeedConvert 结果: {}", result);
    }

    /**
     * 测试 sequence 方法
     */
    @Test
    void testSequence() {
        int sequence = activityOperator.sequence();
        assertEquals(1, sequence, "ActivityOperator 的 sequence 应返回 1");
        log.info("ActivityOperator sequence: {}", sequence);
    }
}
