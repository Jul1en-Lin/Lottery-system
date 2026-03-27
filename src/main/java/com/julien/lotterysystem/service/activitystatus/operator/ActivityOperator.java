package com.julien.lotterysystem.service.activitystatus.operator;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.julien.lotterysystem.common.enums.PrizeStatusEnum;
import com.julien.lotterysystem.common.enums.UserStatusEnum;
import com.julien.lotterysystem.entity.dataobject.Activity;
import com.julien.lotterysystem.entity.dataobject.ActivityPrize;
import com.julien.lotterysystem.entity.dataobject.ActivityUser;
import com.julien.lotterysystem.entity.dto.ConvertActivityStatusDTO;
import com.julien.lotterysystem.mapper.ActivityMapper;
import com.julien.lotterysystem.mapper.ActivityPrizeMapper;
import com.julien.lotterysystem.mapper.ActivityUserMapper;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Slf4j
@Component
public class ActivityOperator extends AbstractActivityOperator {

    @Autowired
    private ActivityMapper activityMapper;
    @Autowired
    private ActivityPrizeMapper activityPrizeMapper;
    @Autowired
    private ActivityUserMapper activityUserMapper;

    /**
     * 活动关联奖品与人员状态扭转后再执行活动状态扭转
     */
    @Override
    public int sequence() {
        return 1;
    }

    @Override
    public boolean isNeedConvert(@Valid ConvertActivityStatusDTO activityStatusDTO) {
        try {
            // 判断活动状态
            Activity activity = activityMapper.selectById(activityStatusDTO.getActivityId());
            if (null == activity) {
                log.warn("查询活动不存在,id:{}", activityStatusDTO.getActivityId());
                return false;
            }
            if (activity.getStatus().equalsIgnoreCase(activityStatusDTO.getTargetActivityStatus().name())) {
                log.warn("活动状态不匹配,活动id:{}", activityStatusDTO.getActivityId());
                return false;
            }
            // 判断此活动的所有奖品是否都抽完
            Long prizeCount = activityPrizeMapper.selectCount(new LambdaQueryWrapper<ActivityPrize>()
                    .eq(ActivityPrize::getStatus, PrizeStatusEnum.INIT.name())
                    .eq(ActivityPrize::getActivityId, activityStatusDTO.getActivityId()));
            if (prizeCount > 0) {
                log.warn("活动奖品状态未扭转完,活动id:{}", activityStatusDTO.getActivityId());
                return false;
            }
            // 判断此活动的所有人员的状态是否都扭转完
            if (CollectionUtils.isEmpty(activityStatusDTO.getUserIds())) {
                log.warn("活动人员id列表为空,活动id:{}", activityStatusDTO.getActivityId());
                return false;
            }
            List<Long> userIds = activityStatusDTO.getUserIds();
            Long userCount = activityUserMapper.selectCount(new LambdaQueryWrapper<ActivityUser>()
                    .eq(ActivityUser::getActivityId, activityStatusDTO.getActivityId())
                    .eq(ActivityUser::getStatus, UserStatusEnum.INIT.name())
                    .in(ActivityUser::getUserId, userIds));
            if (userCount > 0) {
                log.warn("活动人员状态未扭转完,活动id:{},活动人员ids:{}",
                        activityStatusDTO.getActivityId(), userIds);
                return false;
            }
            return true;
        } catch (Exception e) {
            log.warn("检查活动扭转状态条件时查询异常,活动id:{}", activityStatusDTO.getActivityId());
            return false;
        }
    }

    @Override
    public Boolean convert(@Valid ConvertActivityStatusDTO activityStatusDTO) {
        try {
            Activity activity = activityMapper.selectById(activityStatusDTO.getActivityId());
            activity.setStatus(activityStatusDTO.getTargetActivityStatus().name());
            activityMapper.update(activity,new LambdaQueryWrapper<Activity>()
                    .eq(Activity::getId, activityStatusDTO.getActivityId()));
            return true;
        } catch (Exception e) {
            log.warn("扭转活动状态失败,活动id:{}", activityStatusDTO.getActivityId());
            return false;
        }
    }
}
