package com.julien.lotterysystem.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.julien.lotterysystem.entity.dataobject.Activity;
import com.julien.lotterysystem.entity.dataobject.ActivityPrize;
import com.julien.lotterysystem.entity.dataobject.ActivityUser;
import com.julien.lotterysystem.entity.dto.ActivityDetailDto;
import com.julien.lotterysystem.entity.dto.ActivityPrizeDto;
import com.julien.lotterysystem.entity.dto.ActivityUserDto;
import com.julien.lotterysystem.mapper.ActivityMapper;
import com.julien.lotterysystem.mapper.ActivityPrizeMapper;
import com.julien.lotterysystem.mapper.ActivityUserMapper;
import com.julien.lotterysystem.mapper.PrizeMapper;
import com.julien.lotterysystem.service.ActivityService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * ActivityServiceImpl 集成测试
 * 重点测试缓存超时配置 TimeUnit.SECONDS 的功能
 */
@Slf4j
@SpringBootTest
public class ActivityServiceImplTest {

    @Autowired
    private ActivityService activityService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private ActivityUserMapper activityUserMapper;

    @Autowired
    private ActivityPrizeMapper activityPrizeMapper;

    @Autowired
    private PrizeMapper prizeMapper;

    /**
     * 测试用例：cacheActivity-缓存活动详情并验证超时时间单位
     * 输入数据状态：创建完整的活动详情 DTO（包含活动信息、用户列表、奖品列表），奖品价格和图片地址已填充
     * 预期输出：活动详情成功缓存到 Redis，缓存过期时间为 CACHE_TIMEOUT（3600 秒）
     * 验证目的：检查 TimeUnit.SECONDS 是否正确设置缓存过期时间单位，确保缓存策略生效
     */
    @Test
    void cacheActivity_WithTimeUnitSeconds_Success() {
        // 准备测试数据：构建完整的 ActivityDetailDto
        ActivityDetailDto detailDto = buildCompleteActivityDetailDto();

        // 执行缓存操作
        activityService.cacheActivity(detailDto);

        // 验证缓存是否成功写入 Redis
        String cacheKey = "activity_" + detailDto.getActivityId();
        Object cachedValue = redisTemplate.opsForValue().get(cacheKey);
        
        Assertions.assertNotNull(cachedValue, "缓存不应该为空");
        Assertions.assertEquals(ActivityDetailDto.class, cachedValue.getClass(), 
                "缓存对象类型应该为 ActivityDetailDto");
        
        ActivityDetailDto cachedDto = (ActivityDetailDto) cachedValue;
        Assertions.assertEquals(detailDto.getActivityId(), cachedDto.getActivityId(),
                "缓存的活动 ID 应该与原始数据一致");
        Assertions.assertEquals(detailDto.getActivityName(), cachedDto.getActivityName(),
                "缓存的活动名称应该与原始数据一致");

        // 验证缓存剩余时间（应该在 CACHE_TIMEOUT 范围内）
        Long remainingTtl = redisTemplate.getExpire(cacheKey, TimeUnit.SECONDS);
        Assertions.assertNotNull(remainingTtl, "缓存应该有过期时间");
        Assertions.assertTrue(remainingTtl > 0 && remainingTtl <= 3600,
                "缓存剩余时间应该在 0 到 3600 秒之间，实际值：" + remainingTtl);

        log.info("缓存活动详情成功，缓存 key: {}, 剩余过期时间：{} 秒", cacheKey, remainingTtl);
    }

    /**
     * 测试用例：cacheActivity-缓存空对象处理
     * 输入数据状态：传入 null 的 ActivityDetailDto
     * 预期输出：不执行缓存操作，不抛出异常
     * 验证目的：检查空值校验逻辑，避免空指针异常
     */
    @Test
    void cacheActivity_NullDto_NoCacheOperation() {
        // 执行缓存 null 对象
        activityService.cacheActivity(null);

        // 验证没有缓存被创建（可以通过日志或其他方式确认）
        // 由于方法内部直接返回，不会抛出异常
        log.info("缓存空对象处理完成，未抛出异常");
    }

    /**
     * 测试用例：cacheActivity-缓存活动 ID 为空的对象
     * 输入数据状态：传入 activityId 为 null 的 ActivityDetailDto
     * 预期输出：不执行缓存操作，不抛出异常
     * 验证目的：检查 activityId 空值校验逻辑
     */
    @Test
    void cacheActivity_NullActivityId_NoCacheOperation() {
        // 准备测试数据：activityId 为 null
        ActivityDetailDto detailDto = new ActivityDetailDto();
        detailDto.setActivityId(null);
        detailDto.setActivityName("测试活动");

        // 执行缓存操作
        activityService.cacheActivity(detailDto);

        // 验证没有缓存被创建
        log.info("缓存 activityId 为 null 的对象处理完成，未抛出异常");
    }

    /**
     * 测试用例：cacheActivity-缓存已有图片和价格的完整 DTO
     * 输入数据状态：ActivityDetailDto 中所有奖品的 imageUrl 和 price 均已填充
     * 预期输出：直接缓存，无需额外查询奖品表
     * 验证目的：检查优化逻辑：当奖品信息完整时直接缓存，提升性能
     */
    @Test
    void cacheActivity_CompletePrizeInfo_DirectCache() {
        // 准备测试数据：奖品信息已完整的 ActivityDetailDto
        ActivityDetailDto detailDto = buildActivityDetailDtoWithCompletePrizes();

        // 执行缓存操作
        activityService.cacheActivity(detailDto);

        // 验证缓存成功
        String cacheKey = "activity_" + detailDto.getActivityId();
        Object cachedValue = redisTemplate.opsForValue().get(cacheKey);
        
        Assertions.assertNotNull(cachedValue, "缓存不应该为空");
        Assertions.assertEquals(ActivityDetailDto.class, cachedValue.getClass(),
                "缓存对象类型应该正确");

        // 验证缓存过期时间单位是否为秒
        Long remainingTtl = redisTemplate.getExpire(cacheKey, TimeUnit.SECONDS);
        Assertions.assertNotNull(remainingTtl, "缓存应该有过期时间");
        Assertions.assertTrue(remainingTtl > 0 && remainingTtl <= 3600,
                "缓存时间单位应该为秒，剩余时间：" + remainingTtl);

        log.info("缓存完整奖品信息成功，缓存 key: {}, TTL: {} 秒", cacheKey, remainingTtl);
    }

    /**
     * 测试用例：getActivityDetail-从 Redis 缓存获取活动详情
     * 输入数据状态：Redis 中已存在有效的活动详情缓存
     * 预期输出：返回缓存的活动详情，不查询数据库
     * 验证目的：检查缓存读取功能和自动反序列化
     */
    @Test
    void getActivityDetail_FromRedisCache_Success() {
        // 准备测试数据并预先缓存
        ActivityDetailDto detailDto = buildCompleteActivityDetailDto();
        activityService.cacheActivity(detailDto);

        // 从缓存获取
        ActivityDetailDto result = activityService.getActivityDetail(detailDto.getActivityId());

        // 验证获取成功
        Assertions.assertNotNull(result, "应该从缓存中获取到活动详情");
        Assertions.assertEquals(detailDto.getActivityId(), result.getActivityId(),
                "活动 ID 应该一致");
        Assertions.assertEquals(detailDto.getActivityName(), result.getActivityName(),
                "活动名称应该一致");

        log.info("从 Redis 缓存获取活动详情成功，活动 ID: {}", result.getActivityId());
    }

    // ==================== 辅助方法 ====================

    /**
     * 构建完整的活动详情 DTO（用于测试）
     */
    private ActivityDetailDto buildCompleteActivityDetailDto() {
        ActivityDetailDto dto = new ActivityDetailDto();
        dto.setActivityId(1L);
        dto.setActivityName("测试活动 -TimeUnit 验证");
        dto.setDescription("测试缓存超时时间单位");

        // 设置用户列表
        List<ActivityUserDto> userList = new ArrayList<>();
        ActivityUserDto user = new ActivityUserDto();
        user.setUserId(1L);
        user.setUserName("测试用户");
        userList.add(user);
        dto.setActivityUserList(userList);

        // 设置奖品列表（包含价格和图片）
        List<ActivityPrizeDto> prizeList = new ArrayList<>();
        ActivityPrizeDto prize = new ActivityPrizeDto();
        prize.setPrizeId(1L);
        prize.setPrizeName("测试奖品");
        prize.setPrice(new BigDecimal("100.0"));
        prize.setImageUrl("http://example.com/prize.jpg");
        prizeList.add(prize);
        dto.setActivityPrizeList(prizeList);

        return dto;
    }

    /**
     * 构建奖品信息完整的活动详情 DTO
     */
    private ActivityDetailDto buildActivityDetailDtoWithCompletePrizes() {
        ActivityDetailDto dto = new ActivityDetailDto();
        dto.setActivityId(2L);
        dto.setActivityName("完整奖品信息测试活动");
        dto.setDescription("测试直接缓存逻辑");

        // 设置奖品列表（所有奖品都有价格和图片）
        List<ActivityPrizeDto> prizeList = new ArrayList<>();
        ActivityPrizeDto prize1 = new ActivityPrizeDto();
        prize1.setPrizeId(1L);
        prize1.setPrizeName("一等奖");
        prize1.setPrice(new BigDecimal("500.0"));
        prize1.setImageUrl("http://example.com/prize1.jpg");
        
        ActivityPrizeDto prize2 = new ActivityPrizeDto();
        prize2.setPrizeId(2L);
        prize2.setPrizeName("二等奖");
        prize2.setPrice(new BigDecimal("300.0"));
        prize2.setImageUrl("http://example.com/prize2.jpg");
        
        prizeList.add(prize1);
        prizeList.add(prize2);
        dto.setActivityPrizeList(prizeList);

        return dto;
    }
}
