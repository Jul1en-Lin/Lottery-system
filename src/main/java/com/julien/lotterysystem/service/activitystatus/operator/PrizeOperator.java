package com.julien.lotterysystem.service.activitystatus.operator;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.julien.lotterysystem.entity.dataobject.ActivityPrize;
import com.julien.lotterysystem.entity.dto.ConvertActivityStatusDTO;
import com.julien.lotterysystem.mapper.ActivityMapper;
import com.julien.lotterysystem.mapper.ActivityPrizeMapper;
import com.julien.lotterysystem.mapper.PrizeMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PrizeOperator extends AbstractActivityOperator {

    @Autowired
    private PrizeMapper prizeMapper;
    @Autowired
    private ActivityMapper activityMapper;
    @Autowired
    private ActivityPrizeMapper activityPrizeMapper;

    @Override
    public int sequence() {
        return 0;
    }

    @Override
    public boolean isNeedConvert(ConvertActivityStatusDTO activityStatusDTO) {
        try {
            // 查询活动关联奖品
            ActivityPrize activityPrize = activityPrizeMapper
                    .selectOne(new LambdaQueryWrapper<ActivityPrize>()
                    .eq(ActivityPrize::getActivityId, activityStatusDTO.getActivityId())
                    .eq(ActivityPrize::getPrizeId, activityStatusDTO.getPrizeId()));
            if (null == activityPrize) {
                log.warn("查询活动或奖品不存在,活动id:{},奖品id:{}",
                        activityStatusDTO.getActivityId(), activityStatusDTO.getPrizeId());
                return false;
            }
            // 判断奖品状态
            if (activityPrize.getStatus()
                    .equalsIgnoreCase(activityStatusDTO.getTargetPrizeStatus().name())) {
                log.warn("奖品状态不匹配");
                return false;
            }
            return true;
        } catch (Exception e) {
            log.warn("检查奖品扭转状态条件时查询异常,活动id:{},奖品id:{}",
                    activityStatusDTO.getActivityId(), activityStatusDTO.getPrizeId());
            return false;
        }
    }

    @Override
    public Boolean convert(ConvertActivityStatusDTO activityStatusDTO) {
        try {
            // 查询活动关联奖品
            ActivityPrize activityPrize = activityPrizeMapper
                    .selectOne(new LambdaQueryWrapper<ActivityPrize>()
                    .eq(ActivityPrize::getActivityId, activityStatusDTO.getActivityId())
                    .eq(ActivityPrize::getPrizeId, activityStatusDTO.getPrizeId()));
            // 更新奖品目标状态
            activityPrize.setStatus(activityStatusDTO.getTargetPrizeStatus().name());
            activityPrizeMapper.update(activityPrize, new LambdaQueryWrapper<ActivityPrize>()
                .eq(ActivityPrize::getActivityId, activityStatusDTO.getActivityId())
                .eq(ActivityPrize::getPrizeId, activityStatusDTO.getPrizeId()));
            return true;
        } catch (Exception e) {
            log.warn("扭转奖品状态失败,活动id:{},奖品id:{}",
                    activityStatusDTO.getActivityId(), activityStatusDTO.getPrizeId());
            return false;
        }
    }
}
