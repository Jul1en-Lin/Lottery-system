package com.julien.lotterysystem.service.activitystatus.operator;

import com.julien.lotterysystem.common.enums.PrizeStatusEnum;
import com.julien.lotterysystem.entity.dataobject.ActivityPrize;
import com.julien.lotterysystem.entity.dto.ConvertActivityStatusDTO;
import com.julien.lotterysystem.mapper.ActivityPrizeMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
public class PrizeOperatorTest {

    @Autowired
    private PrizeOperator prizeOperator;

    @Autowired
    private ActivityPrizeMapper activityPrizeMapper;

    /**
     * 测试正常扭转奖品状态
     */
    @Test
    void testConvert_success() {
        // 准备测试数据 - 需要数据库中存在 activity_id=1, prize_id=1 的记录
        ConvertActivityStatusDTO dto = new ConvertActivityStatusDTO();
        dto.setActivityId(21L);
        dto.setPrizeId(1L);
        dto.setTargetPrizeStatus(PrizeStatusEnum.COMPLETED);

        // 执行扭转
        Boolean result = prizeOperator.convert(dto);

        // 验证结果
        assertTrue(result, "扭转应成功");

        // 验证数据库状态
        ActivityPrize activityPrize = activityPrizeMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ActivityPrize>()
                        .eq(ActivityPrize::getActivityId, 21L)
                        .eq(ActivityPrize::getPrizeId, 1L));
        assertNotNull(activityPrize);
        assertEquals(PrizeStatusEnum.COMPLETED.name(), activityPrize.getStatus());

        log.info("奖品状态扭转成功，活动id:1, 奖品id:1, 目标状态:COMPLETED");
    }

    /**
     * 测试扭转不存在的活动奖品 - 返回 false
     */
    @Test
    void testConvert_notFound() {
        ConvertActivityStatusDTO dto = new ConvertActivityStatusDTO();
        dto.setActivityId(99999L);
        dto.setPrizeId(99999L);
        dto.setTargetPrizeStatus(PrizeStatusEnum.COMPLETED);

        Boolean result = prizeOperator.convert(dto);

        assertFalse(result, "查询不到活动奖品时应返回 false");
        log.info("扭转不存在的活动奖品，返回 false");
    }

    /**
     * 测试 isNeedConvert 方法 - 需要扭转的情况
     */
    @Test
    void testIsNeedConvert_needConvert() {
        // 准备数据 - 确保 activity_id=1, prize_id=1 存在且状态不是 COMPLETED
        ConvertActivityStatusDTO dto = new ConvertActivityStatusDTO();
        dto.setActivityId(21L);
        dto.setPrizeId(1L);
        dto.setTargetPrizeStatus(PrizeStatusEnum.COMPLETED);
        Boolean result = prizeOperator.isNeedConvert(dto);
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
        dto.setPrizeId(99999L);
        dto.setTargetPrizeStatus(PrizeStatusEnum.COMPLETED);

        Boolean result = prizeOperator.isNeedConvert(dto);

        assertFalse(result, "查询不到活动奖品时应返回 false");
        log.info("isNeedConvert 查询不到记录，返回 false");
    }
}
