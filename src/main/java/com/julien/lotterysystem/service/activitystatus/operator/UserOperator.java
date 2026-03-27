package com.julien.lotterysystem.service.activitystatus.operator;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.julien.lotterysystem.entity.dataobject.ActivityUser;
import com.julien.lotterysystem.entity.dto.ConvertActivityStatusDTO;
import com.julien.lotterysystem.mapper.ActivityUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Slf4j
@Component
public class UserOperator extends AbstractActivityOperator {

    @Autowired
    private ActivityUserMapper activityUserMapper;

    @Override
    public int sequence() {
        return 0;
    }

    @Override
    public boolean isNeedConvert(ConvertActivityStatusDTO activityStatusDTO) {
        List<Long> userIds = activityStatusDTO.getUserIds();
        try {
            // 查询活动关联人员
            List<ActivityUser> activityUsers = activityUserMapper.selectList(new LambdaQueryWrapper<ActivityUser>()
                    .eq(ActivityUser::getActivityId, activityStatusDTO.getActivityId())
                    .in(ActivityUser::getUserId, userIds));
            if (CollectionUtils.isEmpty(activityUsers) || activityUsers.size() != userIds.size()) {
                log.warn("活动关联人员不存在或数量有误,活动id:{}", activityStatusDTO.getActivityId());
                return false;
            }
            // 判断活动人员状态
            for (ActivityUser activityUser : activityUsers) {
                if (activityUser.getStatus().equalsIgnoreCase(activityStatusDTO.getTargetUserStatus().name())) {
                    log.warn("活动人员状态不匹配");
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            log.warn("检查人员扭转状态条件时查询异常,活动id:{},人员ids:{}",
                    activityStatusDTO.getActivityId(), userIds);
            return false;
        }

    }

    @Override
    public Boolean convert(ConvertActivityStatusDTO activityStatusDTO) {
        List<Long> userIds = activityStatusDTO.getUserIds();
        try {
            // 查询活动关联人员
            List<ActivityUser> activityUsers = activityUserMapper.selectList(new LambdaQueryWrapper<ActivityUser>()
                    .eq(ActivityUser::getActivityId, activityStatusDTO.getActivityId())
                    .in(ActivityUser::getUserId, userIds));
            // 更新用户状态
            activityUsers.forEach(activityUser ->
                    activityUser.setStatus(activityStatusDTO.getTargetUserStatus().name()));
            for (ActivityUser activityUser : activityUsers) {
            activityUserMapper.update(activityUser, new LambdaQueryWrapper<ActivityUser>()
                    .eq(ActivityUser::getActivityId, activityStatusDTO.getActivityId())
                    .eq(ActivityUser::getUserId, activityUser.getUserId()));
            }
            return true;
        } catch (Exception e) {
            log.error("扭转用户状态失败,活动id:{},人员ids:{}",activityStatusDTO.getActivityId(),userIds);
            return false;
        }
    }
}
