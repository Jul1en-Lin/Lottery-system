package com.julien.lotterysystem.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.julien.lotterysystem.common.constants.ErrorConstants;
import com.julien.lotterysystem.common.enums.ActivityStatusEnum;
import com.julien.lotterysystem.common.enums.PrizeStatusEnum;
import com.julien.lotterysystem.common.exception.LotteryException;
import com.julien.lotterysystem.common.utils.JacksonUtil;
import com.julien.lotterysystem.entity.dataobject.Activity;
import com.julien.lotterysystem.entity.dataobject.ActivityPrize;
import com.julien.lotterysystem.entity.errorcode.ErrorCode;
import com.julien.lotterysystem.entity.request.DrawPrizeRequest;
import com.julien.lotterysystem.mapper.ActivityMapper;
import com.julien.lotterysystem.mapper.ActivityPrizeMapper;
import com.julien.lotterysystem.mapper.ActivityUserMapper;
import com.julien.lotterysystem.service.DrawPrizeService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
        ActivityPrize activityPrize = activityPrizeMapper.selectById(new LambdaQueryWrapper<ActivityPrize>()
                .eq(ActivityPrize::getId, request.getPrizeId())
                .eq(ActivityPrize::getActivityId, request.getActivityId()));
        // 判断活动或奖品是否存在
        if (activity == null || activityPrize == null) {
            log.info(ErrorConstants.ACTIVITY_OR_PRISE_EMPTY.getErrMeg());
            // throw new LotteryException(ErrorConstants.ACTIVITY_OR_PRISE_EMPTY);
            return false;
        }
        // 检查活动状态
        if (activity.getStatus().equalsIgnoreCase(ActivityStatusEnum.END.name())) {
            log.info(ErrorConstants.ACTIVITY_STATUS_ERROR.getErrMeg());
            // throw new LotteryException(ErrorConstants.ACTIVITY_STATUS_ERROR);
            return false;
        }
        // 检查奖品状态
        if (activityPrize.getStatus().equalsIgnoreCase(PrizeStatusEnum.COMPLETED.name())) {
            log.info(ErrorConstants.PRIZE_STATUS_ERROR.getErrMeg());
            // throw new LotteryException(ErrorConstants.PRIZE_STATUS_ERROR);
            return false;
        }
        // 检查奖品数量与中奖者列表数量是否一致
        if (request.getWinnerList().isEmpty()
                || request.getWinnerList().size() != activityPrize.getPrizeAmount()) {
            log.info(ErrorConstants.WINNERS_OR_PRIZES_AMOUNT_MISMATCH_ERROR.getErrMeg());
            // throw new LotteryException(ErrorConstants.WINNERS_OR_PRIZES_AMOUNT_MISMATCH_ERROR);
            return false;
        }
        return true;
    }
}
