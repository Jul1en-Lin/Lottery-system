package com.julien.lotterysystem.service.mq;

import com.julien.lotterysystem.common.enums.ActivityStatusEnum;
import com.julien.lotterysystem.common.enums.PrizeStatusEnum;
import com.julien.lotterysystem.common.enums.UserStatusEnum;
import com.julien.lotterysystem.common.exception.LotteryException;
import com.julien.lotterysystem.common.utils.JacksonUtil;
import com.julien.lotterysystem.entity.dataobject.WinningRecord;
import com.julien.lotterysystem.entity.dto.ConvertActivityStatusDTO;
import com.julien.lotterysystem.entity.request.DrawPrizeRequest;
import com.julien.lotterysystem.service.ActivityService;
import com.julien.lotterysystem.service.DrawPrizeService;
import com.julien.lotterysystem.service.activitystatus.ActivityStatusManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
@RabbitListener(queues = "LotteryQueue")
public class MqReceiver {

    @Autowired
    private DrawPrizeService drawPrizeService;
    @Autowired
    private ActivityStatusManager activityStatusManager;

    /**
     * 处理抽奖消息
     * @param message Map类型，包含 messageId 和 messageBody
     */
    @RabbitHandler
    public void process(Map<String, String> message) {
        // 从队列中取出消息
        log.info("从消息队列中取出消息,message:{}", JacksonUtil.serialize(message));
        // 提取消息体
        String messageBody = message.get("messageBody");
        DrawPrizeRequest param = JacksonUtil.deSerialize(messageBody, DrawPrizeRequest.class);
        // 处理抽奖流程

        try {
            // 校验抽奖请求
            if (param == null || !drawPrizeService.checkDrawPrizeRequest(param)) {
                log.error("抽奖请求校验失败,param:{}", JacksonUtil.serialize(param));
                return;
            }

            // 重要！！
            // 状态扭转处理（活动状态、奖品状态、中奖者状态）
            // 设计模式：责任链 + 策略模式
            convertStatus(param);
            // 保存中奖者信息 + 缓存中奖信息
            List<WinningRecord> winningRecords = drawPrizeService.saveWinningRecord(param);
            // 异步处理中奖信息

        } catch (LotteryException e) {
            log.error("处理MQ消息异常:{},{}",e.getCode(),e.getMessage(),e);
        } catch (Exception e) {
            log.error("处理MQ消息异常:",e);
        }
    }

    /**
     * 扭转状态
     * @param param
     */
    private void convertStatus(DrawPrizeRequest param) {
        // 构建活动状态扭转DTO
        ConvertActivityStatusDTO activityStatusDTO = new ConvertActivityStatusDTO();
        activityStatusDTO.setActivityId(param.getActivityId());
        activityStatusDTO.setTargetActivityStatus(ActivityStatusEnum.START);
        activityStatusDTO.setPrizeId(param.getPrizeId());
        activityStatusDTO.setTargetPrizeStatus(PrizeStatusEnum.INIT);
        activityStatusDTO.setUserIds(param.getWinnerList().stream()
                .map(DrawPrizeRequest.Winner::getUserId).toList());
        activityStatusDTO.setTargetUserStatus(UserStatusEnum.INIT);
        // 策略处理状态扭转
        activityStatusManager.handlerEvent(activityStatusDTO);
    }
}
