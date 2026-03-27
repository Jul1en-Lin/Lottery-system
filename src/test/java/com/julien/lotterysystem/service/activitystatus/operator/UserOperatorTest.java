package com.julien.lotterysystem.service.activitystatus.operator;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.julien.lotterysystem.common.enums.UserStatusEnum;
import com.julien.lotterysystem.entity.dataobject.ActivityUser;
import com.julien.lotterysystem.entity.dto.ConvertActivityStatusDTO;
import com.julien.lotterysystem.mapper.ActivityUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
public class UserOperatorTest {

    @Autowired
    private UserOperator userOperator;

    @Autowired
    private ActivityUserMapper activityUserMapper;

    /**
     * 测试正常扭转用户状态
     */
    @Test
    void testConvert_success() {
        // 准备测试数据 - 需要数据库中存在 activity_id=21 且对应 user_id 的记录
        ConvertActivityStatusDTO dto = new ConvertActivityStatusDTO();
        dto.setActivityId(21L);
        dto.setUserIds(Arrays.asList(4L,6L,7L));
        dto.setTargetUserStatus(UserStatusEnum.COMPLETED);

        // 执行扭转
        Boolean result = userOperator.convert(dto);

        // 验证结果
        assertTrue(result, "扭转应成功");

        // 验证数据库状态
        List<ActivityUser> activityUsers = activityUserMapper.selectList(
                new LambdaQueryWrapper<ActivityUser>()
                        .eq(ActivityUser::getActivityId, 21L)
                        .in(ActivityUser::getUserId, Arrays.asList(4L,6L,7L)));
        for (ActivityUser activityUser : activityUsers) {
            assertEquals(UserStatusEnum.COMPLETED.name(), activityUser.getStatus());
        }

        log.info("用户状态扭转成功，活动id:21, 目标状态:COMPLETED");
    }

    /**
     * 测试扭转不存在的活动用户 - 返回 false
     */
    @Test
    void testConvert_notFound() {
        ConvertActivityStatusDTO dto = new ConvertActivityStatusDTO();
        dto.setActivityId(99999L);
        dto.setUserIds(Arrays.asList(99999L));
        dto.setTargetUserStatus(UserStatusEnum.COMPLETED);

        Boolean result = userOperator.convert(dto);

        assertFalse(result, "查询不到活动用户时应返回 false");
        log.info("扭转不存在的活动用户，返回 false");
    }

    /**
     * 测试 isNeedConvert 方法 - 需要扭转的情况
     */
    @Test
    void testIsNeedConvert_needConvert() {
        // 准备数据 - 确保 activity_id=21 且用户存在
        ConvertActivityStatusDTO dto = new ConvertActivityStatusDTO();
        dto.setActivityId(21L);
        dto.setUserIds(Arrays.asList(4L,6L,7L));
        dto.setTargetUserStatus(UserStatusEnum.COMPLETED);

        Boolean result = userOperator.isNeedConvert(dto);

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
        dto.setUserIds(Arrays.asList(99999L));
        dto.setTargetUserStatus(UserStatusEnum.COMPLETED);

        Boolean result = userOperator.isNeedConvert(dto);

        assertFalse(result, "查询不到活动用户时应返回 false");
        log.info("isNeedConvert 查询不到记录，返回 false");
    }

    /**
     * 测试 isNeedConvert 方法 - 空用户列表
     */
    @Test
    void testIsNeedConvert_emptyUserIds() {
        ConvertActivityStatusDTO dto = new ConvertActivityStatusDTO();
        dto.setActivityId(21L);
        dto.setUserIds(Arrays.asList());
        dto.setTargetUserStatus(UserStatusEnum.COMPLETED);

        Boolean result = userOperator.isNeedConvert(dto);

        // 空列表查询可能返回空结果
        log.info("空用户列表 isNeedConvert 结果: {}", result);
    }

    /**
     * 测试 sequence 方法
     */
    @Test
    void testSequence() {
        int sequence = userOperator.sequence();
        assertEquals(0, sequence, "UserOperator 的 sequence 应返回 0");
        log.info("UserOperator sequence: {}", sequence);
    }
}
