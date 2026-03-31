package com.julien.lotterysystem.service.mq;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.julien.lotterysystem.common.enums.ActivityStatusEnum;
import com.julien.lotterysystem.common.enums.PrizeStatusEnum;
import com.julien.lotterysystem.common.enums.UserStatusEnum;
import com.julien.lotterysystem.common.exception.LotteryException;
import com.julien.lotterysystem.common.utils.JacksonUtil;
import com.julien.lotterysystem.entity.dataobject.ActivityPrize;
import com.julien.lotterysystem.entity.dataobject.WinningRecord;
import com.julien.lotterysystem.entity.dto.ConvertActivityStatusDTO;
import com.julien.lotterysystem.entity.request.DrawPrizeRequest;
import com.julien.lotterysystem.mapper.ActivityPrizeMapper;
import com.julien.lotterysystem.mapper.WinningRecordMapper;
import com.julien.lotterysystem.service.ActivityService;
import com.julien.lotterysystem.service.DrawPrizeService;
import com.julien.lotterysystem.service.MailService;
import com.julien.lotterysystem.service.activitystatus.ActivityStatusManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import static com.julien.lotterysystem.common.config.RabbitConfig.QUEUE_NAME;

@Slf4j
@Configuration
@RabbitListener(queues = QUEUE_NAME)
public class MqReceiver {

    @Autowired
    private DrawPrizeService drawPrizeService;
    @Autowired
    private ActivityStatusManager activityStatusManager;
    @Autowired
    @Qualifier("lotteryAsyncExecutor")
    private Executor asyncExecutor;
    @Autowired
    private MailService mailService;
    @Autowired
    private ActivityPrizeMapper activityPrizeMapper;
    @Autowired
    private WinningRecordMapper winningRecordMapper;

    /**
     * 处理抽奖消息
     * @param message Map类型，包含 messageId 和 messageBody
     */
    @RabbitHandler
    public void process(Map<String, String> message) throws Exception {
        // 从队列中取出消息
        log.info("从消息队列中取出消息,message:{}", JacksonUtil.serialize(message));
        // 提取消息体
        String messageBody = message.get("messageBody");
        DrawPrizeRequest param = JacksonUtil.deSerialize(messageBody, DrawPrizeRequest.class);
        if (param == null) {
            log.error("消息体反序列化失败,message:{}", messageBody);
            return;
        }
        log.info("处理抽奖消息,param:{}", param);
        // 处理抽奖流程
        try {
            // 校验抽奖请求
            // checkDrawPrizeRequest 选择不抛出异常，选择返回boolean
            // 考虑到并发场景，如果前端发起两个相同的请求param1、param2
            // 场景：
            //      1. param1请求已经将状态扭转完成
            //      2. param2请求过来时校验失败，并抛出异常，被外层捕获，跳转到回滚操作
            // 因此，这里选择不抛出异常
            if (!drawPrizeService.checkDrawPrizeRequest(param)) {
                log.error("抽奖请求校验失败,param:{}", JacksonUtil.serialize(param));
                return;
            }

            // 重要！！
            // 状态扭转处理（活动状态、奖品状态、中奖者状态）
            // 设计模式：责任链 + 策略模式
            convertStatus(param);

            // 保存中奖者信息 + 缓存中奖信息
            List<WinningRecord> winningRecords = drawPrizeService.saveWinningRecord(param);

            // 异步方式通知中奖者（邮箱、短信）
            // 此处不会抛出异常，故回滚操作只专注于状态扭转和中奖者信息保存
            syncExecute(winningRecords);
        } catch (LotteryException e) {
            log.error("处理MQ消息异常:{},{}",e.getCode(),e.getMessage(),e);
            // 回滚状态扭转，保证事务一致性
            rollback(param);
            throw e; // 抛出异常——消息重试，重试失败后进行死信队列处理
        } catch (Exception e) {
            log.error("处理MQ消息异常:",e);
            // 回滚状态扭转，保证事务一致性
            rollback(param);
            throw e; // 抛出异常——消息重试，重试失败后进行死信队列处理
        }
    }

    private void rollback(DrawPrizeRequest param) {
        // 回滚也有先后顺序判断，判断是在哪一步出现异常，进行相应的回滚操作
        log.error("处理MQ消息异常，进行回滚操作");
        if (statusNeedRollback(param)) {
            statusRollback(param);
        }
        if (winnerNeedRollback(param)) {
            winnerRollback(param);
        }
    }


    /**
     * 判断活动+奖品+人员表相关状态是否已经扭转（正常思路）
     * 扭转奖品状态之后，活动状态是一定为正在进行的
     * 扭转状态时，保证了事务一致性，要么都扭转了，要么都没扭转（不包含活动）
     * 因此，只用判断人员/奖品是否扭转过，就能判断出状态是否全部扭转
     * 结论：判断奖品状态是否扭转，就能判断出全部状态是否扭转，不用判断活动是否已经扭转
     */
    private Boolean statusNeedRollback(DrawPrizeRequest param) {
        ActivityPrize activityPrize = activityPrizeMapper.selectOne(new LambdaQueryWrapper<ActivityPrize>()
                .eq(ActivityPrize::getActivityId, param.getActivityId())
                .eq(ActivityPrize::getPrizeId, param.getPrizeId()));
        return activityPrize != null
                && activityPrize.getStatus().equalsIgnoreCase(PrizeStatusEnum.COMPLETED.name());
    }

    /**
     * 回滚状态扭转 + 更新缓存
     * @param param
     */
    private void statusRollback(DrawPrizeRequest param) {
        ConvertActivityStatusDTO activityStatusDTO = new ConvertActivityStatusDTO();
        activityStatusDTO.setActivityId(param.getActivityId());
        activityStatusDTO.setTargetActivityStatus(ActivityStatusEnum.START);
        activityStatusDTO.setPrizeId(param.getPrizeId());
        activityStatusDTO.setTargetPrizeStatus(PrizeStatusEnum.INIT);
        activityStatusDTO.setUserIds(param.getWinnerList().stream()
                .map(DrawPrizeRequest.Winner::getUserId).toList());
        activityStatusDTO.setTargetUserStatus(UserStatusEnum.INIT);
        activityStatusManager.rollBackEvent(activityStatusDTO);

    }
    private Boolean winnerNeedRollback(DrawPrizeRequest param) {
        // 判断中奖者信息是否已经保存
        Long count = winningRecordMapper.selectCount(new LambdaQueryWrapper<WinningRecord>()
                .eq(WinningRecord::getActivityId, param.getActivityId())
                .eq(WinningRecord::getPrizeId, param.getPrizeId()));
        return count > 0;
    }

    /**
     * 回滚中奖者信息 + 更新缓存
     * 即删除
     */
    private void winnerRollback(DrawPrizeRequest param) {
        // 删除中奖者信息
        winningRecordMapper.delete(new LambdaQueryWrapper<WinningRecord>()
                .eq(WinningRecord::getActivityId, param.getActivityId())
                .eq(WinningRecord::getPrizeId, param.getPrizeId()));
        // 删除缓存
        drawPrizeService.deleteWinningRecordCache(param.getActivityId(), param.getPrizeId());

    }

    /**
     * 异步（并发）执行通知中奖者（邮箱、短信）
     * @param winningRecords 中奖者信息
     */
    private void syncExecute(List<WinningRecord> winningRecords) {
        if (winningRecords == null || winningRecords.isEmpty()) {
            return;
        }
        // TODO：扩展——采用策略模式
        // 只关注于异步的思想
        // 短信服务暂时不实现，只实现邮件通知
        // asyncExecutor.execute(() -> sendSms(winningRecords));
        asyncExecutor.execute(() -> sendEmail(winningRecords));
    }

    /**
     * 发送邮件通知中奖者
     * @param winningRecords 中奖者信息
     */
    private void sendEmail(List<WinningRecord> winningRecords) {
        // 做发送邮件前的校验
        if (CollectionUtils.isEmpty(winningRecords))
            return;
        for (WinningRecord record : winningRecords) {
            mailService.sendWinningNotification(
                    record.getWinnerEmail(),
                    record.getWinnerName(),
                    record.getActivityName(),
                    record.getPrizeName(),
                    record.getPrizeTier()
            );
        }
    }

    /**
     * 抽奖后扭转状态
     * @param param
     */
    private void convertStatus(DrawPrizeRequest param) {
        // 构建活动状态扭转DTO
        ConvertActivityStatusDTO activityStatusDTO = new ConvertActivityStatusDTO();
        activityStatusDTO.setActivityId(param.getActivityId());
        activityStatusDTO.setTargetActivityStatus(ActivityStatusEnum.END);
        activityStatusDTO.setPrizeId(param.getPrizeId());
        activityStatusDTO.setTargetPrizeStatus(PrizeStatusEnum.COMPLETED);
        activityStatusDTO.setUserIds(param.getWinnerList().stream()
                .map(DrawPrizeRequest.Winner::getUserId).toList());
        activityStatusDTO.setTargetUserStatus(UserStatusEnum.COMPLETED);
        // 策略处理状态扭转
        activityStatusManager.handlerEvent(activityStatusDTO);
    }
}
