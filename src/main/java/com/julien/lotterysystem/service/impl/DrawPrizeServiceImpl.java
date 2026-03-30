package com.julien.lotterysystem.service.impl;

import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.julien.lotterysystem.common.constants.ErrorConstants;
import com.julien.lotterysystem.common.enums.ActivityStatusEnum;
import com.julien.lotterysystem.common.enums.PrizeStatusEnum;
import com.julien.lotterysystem.common.exception.LotteryException;
import com.julien.lotterysystem.common.utils.JacksonUtil;
import com.julien.lotterysystem.entity.dataobject.*;
import com.julien.lotterysystem.entity.request.DrawPrizeRequest;
import com.julien.lotterysystem.mapper.*;
import com.julien.lotterysystem.service.DrawPrizeService;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.julien.lotterysystem.common.config.RabbitConfig.EXCHANGE_NAME;
import static com.julien.lotterysystem.common.config.RabbitConfig.ROUTING_KEY;

@Slf4j
@Service
public class DrawPrizeServiceImpl implements DrawPrizeService {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private ActivityMapper activityMapper;
    @Autowired
    private ActivityPrizeMapper activityPrizeMapper;
    @Autowired
    private ActivityUserMapper activityUserMapper;
    @Autowired
    private PrizeMapper prizeMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WinningRecordMapper winningRecordMapper;
    @Autowired
    private SqlSessionFactory sqlSessionFactory;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 缓存超时时间，单位：秒
    private final Long CACHE_TIMEOUT = 60 * 60L;
    // 中奖信息缓存key前缀
    private final String WINNING_RECORD_PREFIX = "winning_record_";

    /**
     * 异步抽奖接口，无需返回结果
     */
    @Override
    public void drawPrize(DrawPrizeRequest request) {
        Map<String,String> map = new HashMap<>();
        map.put("messageId", UUID.randomUUID().toString());
        map.put("messageBody", JacksonUtil.serialize(request));
        // 发送消息到队列
        // 需要指定交换机、与队列绑定的路由键、包装后的消息体
        rabbitTemplate.convertAndSend(EXCHANGE_NAME,ROUTING_KEY,map);
        log.info("成功发送抽奖消息到队列，消息体：{}", map);
    }

    /**
     * 校验抽奖请求
     */
    @Override
    public Boolean checkDrawPrizeRequest(DrawPrizeRequest request) {
        // 查库
        Activity activity = activityMapper.selectOne(new LambdaQueryWrapper<Activity>()
                .eq(Activity::getId, request.getActivityId()));
        ActivityPrize activityPrize = activityPrizeMapper.selectOne(new LambdaQueryWrapper<ActivityPrize>()
                .eq(ActivityPrize::getPrizeId, request.getPrizeId())
                .eq(ActivityPrize::getActivityId, request.getActivityId()));
        List<Long> winnerIds = request.getWinnerList().stream()
                .map(DrawPrizeRequest.Winner::getUserId)
                .toList();
        Long count = activityUserMapper.selectCount(new LambdaQueryWrapper<ActivityUser>()
                .eq(ActivityUser::getActivityId, request.getActivityId())
                .in(ActivityUser::getUserId, winnerIds));
        // 检查活动人员是否存在
        if (count != winnerIds.size()) {
            log.warn(ErrorConstants.WINNERS_EMPTY.getErrMeg());
            // throw new LotteryException(ErrorConstants.WINNERS_EMPTY);
            return false;
        }
        // 判断活动或奖品是否存在
        if (activity == null || activityPrize == null) {
            log.warn(ErrorConstants.ACTIVITY_OR_PRISE_EMPTY.getErrMeg());
            // throw new LotteryException(ErrorConstants.ACTIVITY_OR_PRISE_EMPTY);
            return false;
        }
        // 检查活动状态
        if (activity.getStatus().equalsIgnoreCase(ActivityStatusEnum.END.name())) {
            log.warn(ErrorConstants.ACTIVITY_STATUS_ERROR.getErrMeg());
            // throw new LotteryException(ErrorConstants.ACTIVITY_STATUS_ERROR);
            return false;
        }
        // 检查奖品状态
        if (activityPrize.getStatus().equalsIgnoreCase(PrizeStatusEnum.COMPLETED.name())) {
            log.warn(ErrorConstants.PRIZE_STATUS_ERROR.getErrMeg());
            // throw new LotteryException(ErrorConstants.PRIZE_STATUS_ERROR);
            return false;
        }
        // 检查奖品数量与中奖者列表数量是否一致
        if (request.getWinnerList().isEmpty()
                || request.getWinnerList().size() > activityPrize.getPrizeAmount()) {
            log.warn(ErrorConstants.WINNERS_OR_PRIZES_AMOUNT_MISMATCH_ERROR.getErrMeg());
            // throw new LotteryException(ErrorConstants.WINNERS_OR_PRIZES_AMOUNT_MISMATCH_ERROR);
            return false;
        }


        return true;
    }

    @Override
    public List<WinningRecord> saveWinningRecord(DrawPrizeRequest param) {
        log.info("保存中奖记录，参数：{}", JacksonUtil.serialize(param));
        Activity activity = activityMapper.selectById(param.getActivityId());
        Prize prize = prizeMapper.selectById(param.getPrizeId());
        ActivityPrize activityPrize = activityPrizeMapper.selectOne(new LambdaQueryWrapper<ActivityPrize>()
                .eq(ActivityPrize::getPrizeId, param.getPrizeId())
                .eq(ActivityPrize::getActivityId, param.getActivityId()));
        List<Long> winnerIds = param.getWinnerList().stream()
                .map(DrawPrizeRequest.Winner::getUserId)
                .toList();
        List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>()
                .in(User::getId, winnerIds));
        List<WinningRecord> winningRecords = users.stream()
                .map(user -> {
                    WinningRecord winningRecord = new WinningRecord();
                    // 活动模块
                    winningRecord.setActivityId(param.getActivityId());
                    winningRecord.setActivityName(activity.getActivityName());
                    // 奖品模块
                    winningRecord.setPrizeId(activityPrize.getPrizeId());
                    winningRecord.setPrizeName(prize.getName());
                    winningRecord.setPrizeTier(activityPrize.getPrizeTiers());
                    // 中奖者模块
                    winningRecord.setWinnerId(user.getId());
                    winningRecord.setWinnerName(user.getUserName());
                    winningRecord.setWinnerEmail(user.getEmail());
                    winningRecord.setWinnerPhoneNumber(user.getPhoneNumber());
                    winningRecord.setWinningTime(param.getWinningTime());
                    return winningRecord;
                }).toList();
        // 批量插入
        MybatisBatch<WinningRecord> userBatch = new MybatisBatch<>(sqlSessionFactory, winningRecords);
        MybatisBatch.Method<WinningRecord> userMethod = new MybatisBatch.Method<>(WinningRecordMapper.class);
        userBatch.execute(userMethod.insert());

        // 缓存中奖信息（分奖品维度和活动维度）
        // 1. 缓存奖品维度的中奖信息（key：WinningRecord_activityId_prizeId）
        String key1 = WINNING_RECORD_PREFIX + param.getActivityId() + "_" + param.getPrizeId();
        cacheWinningRecords(key1, winningRecords, CACHE_TIMEOUT);
        // 只有活动结束时才缓存活动维度的中奖信息
        if (activity.getStatus().equalsIgnoreCase(ActivityStatusEnum.END.name())) {
            // 2. 缓存活动维度的中奖信息（key：WinningRecord_activityId）
            // 获取活动维度的全量中奖信息
            List<WinningRecord> allWinningRecords = winningRecordMapper.selectList(new LambdaQueryWrapper<WinningRecord>()
                    .eq(WinningRecord::getActivityId, param.getActivityId()));
            String key2 = WINNING_RECORD_PREFIX + param.getActivityId();
            cacheWinningRecords(key2, allWinningRecords, CACHE_TIMEOUT);
        }

        return winningRecords;
    }

    /**
     * 缓存中奖信息
     * @param winningRecords 中奖信息列表
     */
    private void cacheWinningRecords(String key, List<WinningRecord> winningRecords, Long timeout) {
        if (!StringUtils.hasText(key) || CollectionUtils.isEmpty(winningRecords)) {
            log.warn("缓存中奖信息参数错误，key：{}，winningRecords：{}", key, winningRecords);
            return;
        }
        try {
            log.info("缓存中奖信息，key：{}，winningRecords：{}", key, winningRecords);
            redisTemplate.opsForValue().set(key, winningRecords, timeout, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("缓存中奖信息失败，key：{}", key, e);
        }
    }

    @Override
    public List<WinningRecord> getWinningRecord(Long activityId) {
        if (null == activityId) {
            log.warn("获取缓存中奖信息活动ID参数为空");
            return null;
        }
        String key = WINNING_RECORD_PREFIX + activityId;
        try {
            log.debug("获取缓存中奖信息，key：{}", key);
            return (List<WinningRecord>) redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("获取缓存中奖信息失败，key：{}", key, e);
            return null;
        }
    }

    @Override
    public List<WinningRecord> getWinningRecord(Long activityId, Long prizeId) {
        if (null == activityId || null == prizeId) {
            log.warn("获取缓存中奖信息活动ID或奖品ID参数为空");
            return null;
        }
        String key = WINNING_RECORD_PREFIX + activityId + "_" + prizeId;
        try {
            log.debug("获取缓存中奖信息，key：{}", key);
            return (List<WinningRecord>) redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("获取缓存中奖信息失败，key：{}", key, e);
            return null;
        }
    }
}
